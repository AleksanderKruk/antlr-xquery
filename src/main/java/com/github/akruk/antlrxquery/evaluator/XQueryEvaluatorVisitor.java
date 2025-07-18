package com.github.akruk.antlrxquery.evaluator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import com.github.akruk.antlrxquery.AntlrXqueryParserBaseVisitor;
import com.github.akruk.antlrxquery.charescaper.XQueryCharEscaper;
import com.github.akruk.antlrxquery.contextmanagement.dynamiccontext.XQueryDynamicContextManager;
import com.github.akruk.antlrxquery.AntlrXqueryParser.*;
import com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.XQueryEvaluatingFunctionManager;
import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver;
import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver.ResolvedName;
import com.github.akruk.antlrxquery.values.XQueryFunction;
import com.github.akruk.antlrxquery.values.XQueryValue;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;
import com.github.akruk.antlrxquery.values.factories.defaults.XQueryMemoizedValueFactory;
import com.github.akruk.nodegetter.NodeGetter;

public class XQueryEvaluatorVisitor extends AntlrXqueryParserBaseVisitor<XQueryValue> {
    final private XQueryValue root;
    final private Parser parser;
    final private XQueryDynamicContextManager contextManager;
    final private XQueryValueFactory valueFactory;
    final private XQueryEvaluatingFunctionManager functionManager;
    final private XQueryFunction concat;

    private XQueryValue matchedNodes;
    private Stream<List<VariableCoupling>> visitedTupleStream;
    private XQueryAxis currentAxis;
    private List<XQueryValue> visitedArgumentList;
    private XQueryVisitingContext context;
    private NodeGetter nodeGetter = new NodeGetter();
    private Map<String, XQueryValue> visitedKeywordArguments;

    private record VariableCoupling(Variable item, Variable key, Variable value, Variable position) {}
    private record Variable(String name, XQueryValue value){}

    private enum XQueryAxis {
        CHILD, DESCENDANT, SELF, DESCENDANT_OR_SELF, FOLLOWING_SIBLING, FOLLOWING, PARENT, ANCESTOR, PRECEDING_SIBLING, PRECEDING, ANCESTOR_OR_SELF, FOLLOWING_OR_SELF, FOLLOWING_SIBLING_OR_SELF, PRECEDING_SIBLING_OR_SELF, PRECEDING_OR_SELF,
    }

    public XQueryEvaluatorVisitor(final ParseTree tree, final Parser parser) {
        this(tree, parser, new XQueryMemoizedValueFactory());
    }

    public XQueryEvaluatorVisitor(final ParseTree tree, final Parser parser, final XQueryValueFactory valueFactory) {
        this.root = valueFactory.node(tree);
        this.context = new XQueryVisitingContext();
        this.context.setValue(root);
        this.context.setPosition(0);
        this.context.setSize(0);
        this.parser = parser;
        this.valueFactory = valueFactory;
        this.functionManager = new XQueryEvaluatingFunctionManager(this, parser, valueFactory, nodeGetter);
        this.contextManager = new XQueryDynamicContextManager();
        this.concat = functionManager.getFunctionReference("fn", "concat", 2).functionValue();
        contextManager.enterContext();
    }

    public XQueryEvaluatorVisitor(
            final ParseTree tree,
            final Parser parser,
            final XQueryDynamicContextManager contextManager,
            final XQueryValueFactory valueFactory,
            final XQueryEvaluatingFunctionManager functionCaller)
    {
        this.root = valueFactory.node(tree);
        this.context = new XQueryVisitingContext();
        this.context.setValue(root);
        this.context.setPosition(0);
        this.context.setSize(0);
        this.parser = parser;
        this.valueFactory = valueFactory;
        this.functionManager = functionCaller;
        this.contextManager = contextManager;
        this.concat = functionManager.getFunctionReference("fn", "concat", 2).functionValue();
        contextManager.enterContext();
    }

    @Override
    public XQueryValue visitFLWORExpr(final FLWORExprContext ctx) {
        final var savedTupleStream = saveVisitedTupleStream();
        contextManager.enterScope();
        // visitedTupleStream will be manipulated to prepare result stream
        ctx.initialClause().accept(this);
        for (final var clause : ctx.intermediateClause()) {
            clause.accept(this);
        }
        // at this point visitedTupleStream should contain all tuples
        final var expressionValue = ctx.returnClause().accept(this);
        contextManager.leaveScope();
        visitedTupleStream = savedTupleStream;
        return expressionValue;
    }

    @Override
    public XQueryValue visitLetClause(final LetClauseContext ctx) {
        final int newVariableCount = ctx.letBinding().size();
        visitedTupleStream = visitedTupleStream.map(tuple -> {
            final var newTuple = new ArrayList<VariableCoupling>(tuple.size() + newVariableCount);
            newTuple.addAll(tuple);
            for (final LetBindingContext streamVariable : ctx.letBinding()) {
                final String variableName = streamVariable.varName().getText();
                final XQueryValue assignedValue = streamVariable.exprSingle().accept(this);
                final var element = new VariableCoupling(new Variable(variableName, assignedValue), null, null, null);
                newTuple.add(element);
                contextManager.provideVariable(variableName, assignedValue);
            }
            return newTuple;
        });
        return null;
    }

    @Override
    public XQueryValue visitOrderByClause(final OrderByClauseContext ctx) {
        final int sortingExprCount = ctx.orderSpecList().orderSpec().size();
        final var orderSpecs = ctx.orderSpecList().orderSpec();
        final int[] modifierMaskArray = orderSpecs.stream()
                .map(OrderSpecContext::orderModifier)
                .mapToInt(m -> {
                    final int isDescending = m.DESCENDING() != null ? 1 : 0;
                    final int isEmptyLeast = m.LEAST() != null ? 1 : 0;
                    final int mask = (isDescending << 1) | isEmptyLeast;
                    return mask;
                })
                .toArray();
        visitedTupleStream = visitedTupleStream.sorted((tuple1, tuple2) -> {
            var comparator = comparatorFromNthOrderSpec(orderSpecs, modifierMaskArray, 0);
            for (int i = 1; i < sortingExprCount; i++) {
                final var nextComparator = comparatorFromNthOrderSpec(orderSpecs, modifierMaskArray, i);
                comparator = comparator.thenComparing(nextComparator);
            }
            return comparator.compare(tuple1, tuple2);
        }).map(tuple -> {
            provideVariables(tuple);
            return tuple;
        });
        return null;
    }

    @Override
    public XQueryValue visitForClause(final ForClauseContext ctx) {
        final int numberOfVariables = ctx.forBinding().size();
        visitedTupleStream = visitedTupleStream.flatMap(tuple -> {
            final List<List<VariableCoupling>> newTupleLike = tuple.stream().map(e -> List.of(e))
                    .collect(Collectors.toList());

            for (final ForBindingContext forBinding : ctx.forBinding()) {
                final List<VariableCoupling> tupleElements = processForBinding(forBinding);
                newTupleLike.add(tupleElements);
            }

            return cartesianProduct(newTupleLike);
        }).map(tuple -> {
            final List<VariableCoupling> addedVariables = tuple.subList(tuple.size() - numberOfVariables, tuple.size());
            provideVariables(addedVariables);
            return tuple;
        });
        return null;
    }

    private List<VariableCoupling> processForBinding(final ForBindingContext forBinding) {
        if (forBinding.forItemBinding() != null) {
            return processForItemBinding(forBinding.forItemBinding());
        }
        if (forBinding.forMemberBinding() != null) {
            return processForMemberBinding(forBinding.forMemberBinding());
        }
        if (forBinding.forEntryBinding() != null) {
            return processForEntryBinding(forBinding.forEntryBinding());
        }
        throw new IllegalStateException("Unknown for binding type");
    }

    private List<VariableCoupling> processForItemBinding(final ForItemBindingContext ctx) {
        final String variableName = ctx.varNameAndType().qname().getText();
        final List<XQueryValue> sequence = ctx.exprSingle().accept(this).sequence();
        final PositionalVarContext positional = ctx.positionalVar();
        final boolean allowingEmpty = ctx.allowingEmpty() != null;
        String positionalName = null;
        Variable positionalVar = null;
        if (positional != null) {
            positionalName = positional.varName().getText();
            positionalVar = new Variable(positionalName, valueFactory.number(0));
        }

        if (sequence.isEmpty() && allowingEmpty) {
            final var emptyVar = new Variable(variableName, valueFactory.emptySequence());
            final var element =  new VariableCoupling(emptyVar, null, null, positionalVar);
            return List.of(element);
        }

        if (positional != null) {
            final List<VariableCoupling> elementsWithIndex = new ArrayList<>();
            for (int i = 0; i < sequence.size(); i++) {
                final XQueryValue value = sequence.get(i);
                final VariableCoupling element = new VariableCoupling(new Variable(variableName, value),
                                                                        null,
                                                                        null,
                                                                        new Variable(positionalName, valueFactory.number(i + 1)));
                elementsWithIndex.add(element);
            }
            return elementsWithIndex;
        }

        return sequence.stream()
                .map(value -> new VariableCoupling(new Variable(variableName, value), null, null, null))
                .toList();
    }

    private List<VariableCoupling> processForMemberBinding(final ForMemberBindingContext ctx) {
        final String variableName = ctx.varNameAndType().qname().getText();
        final XQueryValue arrayValue = ctx.exprSingle().accept(this);
        final PositionalVarContext positional = ctx.positionalVar();

        final List<XQueryValue> arrayMembers = arrayValue.arrayMembers();

        if (positional != null) {
            final String positionalName = positional.varName().getText();
            final List<VariableCoupling> elementsWithIndex = new ArrayList<>();
            for (int i = 0; i < arrayMembers.size(); i++) {
                final XQueryValue member = arrayMembers.get(i);
                final VariableCoupling element = new VariableCoupling(new Variable(variableName, member),
                                                                        null,
                                                                        null,
                                                                        new Variable(positionalName, valueFactory.number(i + 1)));
                elementsWithIndex.add(element);
            }
            return elementsWithIndex;
        }

        return arrayMembers.stream()
                .map(value -> new VariableCoupling(new Variable(variableName, value), null, null, null))
                .toList();
    }

    private List<VariableCoupling> processForEntryBinding(final ForEntryBindingContext ctx) {
        final XQueryValue mapValue = ctx.exprSingle().accept(this);
        final PositionalVarContext positional = ctx.positionalVar();

        final ForEntryKeyBindingContext keyBinding = ctx.forEntryKeyBinding();
        final ForEntryValueBindingContext valueBinding = ctx.forEntryValueBinding();

        final Map<XQueryValue, XQueryValue> mapEntries = mapValue.mapEntries();
        final List<VariableCoupling> tupleElements = new ArrayList<>();

        int index = 1;
        for (final Map.Entry<XQueryValue, XQueryValue> entry : mapEntries.entrySet()) {
            Variable positionVar = null;
            if (positional != null) {
                final String positionalName = positional.varName().getText();
                final XQueryValue position = valueFactory.number(index);
                positionVar = new Variable(positionalName, position);
            }

            Variable keyVar = null;
            if (keyBinding != null) {
                final String keyName = keyBinding.varNameAndType().qname().getText();
                final XQueryValue keyValue = entry.getKey();
                keyVar = new Variable(keyName, keyValue);
            }

            Variable valueVar = null;
            if (valueBinding != null) {
                final String valueName = valueBinding.varNameAndType().qname().getText();
                final XQueryValue valueValue = entry.getValue();
                valueVar = new Variable(valueName, valueValue);
            }

            tupleElements.add(new VariableCoupling(null, keyVar, valueVar, positionVar));
            index++;
        }

        return tupleElements;
    }

    private class MutableInt {
        public int i = 0;
    }

    @Override
    public XQueryValue visitCountClause(final CountClauseContext ctx) {
        final String countVariableName = ctx.varName().getText();
        final MutableInt index = new MutableInt();
        index.i = 1;
        visitedTupleStream = visitedTupleStream.map(tuple -> {
            final var newTuple = new ArrayList<VariableCoupling>(tuple.size() + 1);
            newTuple.addAll(tuple);
            final var element = new VariableCoupling(new Variable(countVariableName, valueFactory.number(index.i++)), null, null, null);
            contextManager.provideVariable(element.item.name, element.item.value);
            newTuple.add(element);
            return newTuple;
        });
        return null;
    }

    @Override
    public XQueryValue visitWhereClause(final WhereClauseContext ctx) {
        final var filteringExpression = ctx.exprSingle();
        visitedTupleStream = visitedTupleStream.filter(_ -> {
            final XQueryValue filter = filteringExpression.accept(this);
            return filter.effectiveBooleanValue();
        });
        return null;
    }

    @Override
    public XQueryValue visitReturnClause(final ReturnClauseContext ctx) {
        final List<XQueryValue> results = visitedTupleStream.map((tupleStream) -> {
            provideVariables(tupleStream);
            final XQueryValue value = ctx.exprSingle().accept(this);
            return value;
        }).toList();
        if (results.size() == 1) {
            final var value = results.get(0);
            return value;
        }
        return valueFactory.sequence(results);
    }

    @Override
    public XQueryValue visitWhileClause(final WhileClauseContext ctx) {
        final var filteringExpression = ctx.exprSingle();
        visitedTupleStream = visitedTupleStream.takeWhile(_ -> {
            final XQueryValue filter = filteringExpression.accept(this);
            return filter.effectiveBooleanValue();
        });
        return null;
    }

    @Override
    public XQueryValue visitVarRef(final VarRefContext ctx) {
        final String variableName = ctx.varName().getText();
        final XQueryValue variableValue = contextManager.getVariable(variableName);
        return variableValue;
    }

    @Override
    public XQueryValue visitLiteral(final LiteralContext ctx) {
        if (ctx.STRING() != null) {
            final String rawText = ctx.getText();
            final String content = unescapeString(rawText.substring(1, rawText.length() - 1));
            return valueFactory.string(content);
        }

        final var numeric = ctx.numericLiteral();
        if (numeric.IntegerLiteral() != null) {
            final String value = numeric.IntegerLiteral().getText().replace("_", "");
            return valueFactory.number(new BigDecimal(value));
        }

        if (numeric.HexIntegerLiteral() != null) {
            final String raw = numeric.HexIntegerLiteral().getText();
            final String hex = raw.replace("_", "").substring(2);
            return valueFactory.number(new BigDecimal(new java.math.BigInteger(hex, 16)));
        }

        if (numeric.BinaryIntegerLiteral() != null) {
            final String raw = numeric.BinaryIntegerLiteral().getText();
            final String binary = raw.replace("_", "").substring(2);
            return valueFactory.number(new BigDecimal(new java.math.BigInteger(binary, 2)));
        }

        if (numeric.DecimalLiteral() != null) {
            final String cleaned = numeric.DecimalLiteral().getText().replace("_", "");
            return valueFactory.number(new BigDecimal(cleaned));
        }

        if (numeric.DoubleLiteral() != null) {
            final String cleaned = numeric.DoubleLiteral().getText().replace("_", "");
            return valueFactory.number(new BigDecimal(cleaned));
        }
        return null;
    }

    @Override
    public XQueryValue visitParenthesizedExpr(final ParenthesizedExprContext ctx) {
        // Empty parentheses mean an empty sequence '()'
        if (ctx.expr() == null) {
            return valueFactory.sequence(List.of());
        }
        return ctx.expr().accept(this);
    }

    @Override
    public XQueryValue visitExpr(final ExprContext ctx) {
        // Only one expression
        // e.g. 13
        if (ctx.exprSingle().size() == 1) {
            return ctx.exprSingle(0).accept(this);
        }
        // More than one expression
        // are turned into a flattened list
        final List<XQueryValue> result = new ArrayList<>();
        for (final var exprSingle : ctx.exprSingle()) {
            final var expressionValue = exprSingle.accept(this);
            if (expressionValue.isAtomic()) {
                result.add(expressionValue);
                continue;
            }
            // If the result is not atomic we atomize it
            // and extend the result list
            final var atomizedValues = expressionValue.atomize();
            result.addAll(atomizedValues);
        }
        return valueFactory.sequence(result);
    }

    private String unescapeString(final String str) {
        final var charEscaper = new XQueryCharEscaper();
        return charEscaper.escapeChars(str);
    }


    @Override
    public XQueryValue visitFunctionCall(final FunctionCallContext ctx) {
        final String[] parts = resolveNamespace(ctx.functionName().getText());
        final String namespace = parts.length == 2 ? parts[0] : "fn";
        final String functionName = parts.length == 2 ? parts[1] : parts[0];
        // TODO: error handling missing function
        final var savedArgs = saveVisitedArguments();
        final var savedKwargs = saveVisitedKeywordArguments();
        ctx.argumentList().accept(this);
        final var value = functionManager.call(namespace, functionName, context, visitedArgumentList, visitedKeywordArguments);
        visitedArgumentList = savedArgs;
        visitedKeywordArguments = savedKwargs;
        return value;
    }

    private Map<String, XQueryValue> saveVisitedKeywordArguments() {
        final var saved = visitedKeywordArguments;
        visitedKeywordArguments = new HashMap<>();
        return saved;
    }

    public static <T> Stream<List<T>> cartesianProduct(final List<List<T>> lists) {
        if (lists.isEmpty()) {
            return Stream.of(List.of());
        }

        final int size = lists.size();
        return lists.get(0).stream()
                .flatMap(firstElement -> cartesianProduct(lists.subList(1, size))
                        .map(rest -> {
                            final List<T> combination = new ArrayList<>(size);
                            combination.add(firstElement);
                            combination.addAll(rest);
                            return combination;
                        }));
    }



    @Override
    public XQueryValue visitQuantifiedExpr(final QuantifiedExprContext ctx) {
        final List<QuantifierBindingContext> quantifierBindings = ctx.quantifierBinding();

        final List<String> variableNames = quantifierBindings.stream()
                .map(binding -> binding.varNameAndType().qname().getText())
                .toList();

        final List<List<XQueryValue>> sequences = quantifierBindings.stream()
                .map(binding -> binding.exprSingle().accept(this).sequence())
                .toList();

        final ExprSingleContext criterionNode = ctx.exprSingle();

        if (ctx.EVERY() != null) {
            final boolean every = cartesianProduct(sequences).allMatch(variableProduct -> {
                for (int i = 0; i < variableNames.size(); i++) {
                    contextManager.provideVariable(variableNames.get(i), variableProduct.get(i));
                }
                return criterionNode.accept(this).effectiveBooleanValue();
            });
            return valueFactory.bool(every);
        }

        if (ctx.SOME() != null) {
            final boolean some = cartesianProduct(sequences).anyMatch(variableProduct -> {
                for (int i = 0; i < variableNames.size(); i++) {
                    contextManager.provideVariable(variableNames.get(i), variableProduct.get(i));
                }
                return criterionNode.accept(this).effectiveBooleanValue();
            });
            return valueFactory.bool(some);
        }

        return null;
    }


    @Override
    public XQueryValue visitOrExpr(final OrExprContext ctx) {
        var value = ctx.andExpr(0).accept(this);
        if (ctx.OR().isEmpty())
            return value;
        // Short circuit
        if (value.effectiveBooleanValue()) {
            return valueFactory.bool(true);
        }
        final var orCount = ctx.OR().size();
        for (int i = 1; i <= orCount; i++) {
            final var visitedExpression = ctx.andExpr(i).accept(this);
            value = value.or(visitedExpression);
            // Short circuit
            if (value.effectiveBooleanValue()) {
                return valueFactory.bool(true);
            }
        }
        return value;
    }



    private XQueryValue handleNodeComp(final ComparisonExprContext ctx) {
        final var visitedLeft = ctx.otherwiseExpr(0).accept(this);
        if (visitedLeft.isSequence() && visitedLeft.empty().effectiveBooleanValue())
            return valueFactory.emptySequence();
        final ParseTree nodeLeft = getSingleNode(visitedLeft);
        final var visitedRight = ctx.otherwiseExpr(1).accept(this);
        if (visitedRight.isSequence() && visitedRight.empty().effectiveBooleanValue())
            return valueFactory.emptySequence();
        final ParseTree nodeRight = getSingleNode(visitedRight);
        final boolean result = switch (ctx.nodeComp().getText()) {
            case "is" -> nodeLeft == nodeRight;
            case "<<" -> nodeGetter.getFollowing(nodeLeft).contains(nodeRight);
            case ">>" -> nodeGetter.getPreceding(nodeLeft).contains(nodeRight);
            default -> false;
        };
        return valueFactory.bool(result);
    }

    private ParseTree getSingleNode(final XQueryValue visitedLeft) {
        ParseTree nodeLeft;
        if (visitedLeft.isAtomic()) {
            nodeLeft = visitedLeft.node();
        } else {
            final List<XQueryValue> sequenceLeft = visitedLeft.atomize();
            nodeLeft = sequenceLeft.get(0).node();
        }
        return nodeLeft;
    }

    @Override
    public XQueryValue visitEnclosedExpr(final EnclosedExprContext ctx) {
        if (ctx.expr() == null)
            return valueFactory.emptySequence();
        return ctx.expr().accept(this);
    }

    @Override
    public XQueryValue visitRangeExpr(final RangeExprContext ctx) {
        final var fromValue = ctx.additiveExpr(0).accept(this);
        if (ctx.TO() == null)
            return fromValue;
        final var toValue = ctx.additiveExpr(1).accept(this);
        if (toValue.isSequence())
            return valueFactory.emptySequence();
        if (fromValue.isSequence())
            return valueFactory.emptySequence();
        final int fromInt = fromValue.numericValue().intValue();
        final int toInt = toValue.numericValue().intValue();
        if (fromInt > toInt)
            return valueFactory.emptySequence();
        final List<XQueryValue> values = IntStream.rangeClosed(fromInt, toInt)
                .mapToObj(i -> valueFactory.number(i))
                .collect(Collectors.toList());
        return valueFactory.sequence(values);
    }

    @Override
    public XQueryValue visitPathExpr(final PathExprContext ctx) {
        final boolean pathExpressionFromRoot = ctx.SLASH() != null;
        if (pathExpressionFromRoot) {
            final var savedNodes = saveMatchedModes();
            final var savedAxis = saveAxis();
            // TODO: Context nodes
            matchedNodes = nodeSequence(List.of(root.node()));
            currentAxis = XQueryAxis.CHILD;
            final var resultingNodeSequence = ctx.relativePathExpr().accept(this);
            matchedNodes = savedNodes;
            currentAxis = savedAxis;
            return resultingNodeSequence;
        }
        final boolean useDescendantOrSelfAxis = ctx.SLASHES() != null;
        if (useDescendantOrSelfAxis) {
            final var savedNodes = saveMatchedModes();
            final var savedAxis = saveAxis();
            matchedNodes = nodeSequence(List.of(root.node()));
            currentAxis = XQueryAxis.DESCENDANT_OR_SELF;
            final var resultingNodeSequence = ctx.relativePathExpr().accept(this);
            matchedNodes = savedNodes;
            currentAxis = savedAxis;
            return resultingNodeSequence;
        }
        return ctx.relativePathExpr().accept(this);
    }

    @Override
    public XQueryValue visitRelativePathExpr(final RelativePathExprContext ctx) {
        if (ctx.pathOperator().isEmpty()) {
            return ctx.stepExpr(0).accept(this);
        }
        final XQueryValue visitedNodeSequence = ctx.stepExpr(0).accept(this);
        matchedNodes = visitedNodeSequence;
        final var operationCount = ctx.pathOperator().size();
        for (int i = 1; i <= operationCount; i++) {
            matchedNodes = switch (ctx.pathOperator(i - 1).getText()) {
                case "//" -> {
                    final List<ParseTree> descendantsOrSelf = nodeGetter.getAllDescendantsOrSelf(matchedTreeNodes());
                    matchedNodes = nodeSequence(descendantsOrSelf);
                    yield ctx.stepExpr(i).accept(this);
                }
                case "/" -> ctx.stepExpr(i).accept(this);
                default -> null;
            };
        }
        return matchedNodes;
    }

    private XQueryValue nodeSequence(final List<ParseTree> treenodes) {
        final List<XQueryValue> nodeSequence = treenodes.stream()
                .map(valueFactory::node)
                .collect(Collectors.toList());
        return valueFactory.sequence(nodeSequence);
    }

    private List<ParseTree> matchedTreeNodes() {
        return matchedNodes.sequence().stream().map(XQueryValue::node).toList();
    }

    @Override
    public XQueryValue visitStepExpr(final StepExprContext ctx) {
        if (ctx.postfixExpr() != null)
            return ctx.postfixExpr().accept(this);
        return ctx.axisStep().accept(this);
    }

    @Override
    public XQueryValue visitAxisStep(final AxisStepContext ctx) {
        XQueryValue stepResult = null;
        if (ctx.reverseStep() != null)
            stepResult = ctx.reverseStep().accept(this);
        else if (ctx.forwardStep() != null)
            stepResult = ctx.forwardStep().accept(this);
        if (ctx.predicateList().predicate().isEmpty()) {
            return stepResult;
        }
        final var savedContext = saveContext();
        final var savedArgs = saveVisitedArguments();
        int index = 1;
        context.setSize(stepResult.sequence().size());
        for (final var predicate : ctx.predicateList().predicate()) {
            context.setValue(stepResult);
            context.setPosition(index);
            stepResult = predicate.accept(this);
            index++;
        }
        context = savedContext;
        visitedArgumentList = savedArgs;
        return stepResult;
    }

    // @Override
    // public XQueryValue visitPredicateList(PredicateListContext ctx) {
    // var result = match;
    // for (var predicate : ctx.predicate()) {
    // predicate.accept(this);
    // }
    // return matchedTreeNodes();
    // }

    @Override
    public XQueryValue visitFilterExpr(FilterExprContext ctx) {
        final var savedContext = saveContext();
        final var savedArgs = saveVisitedArguments();
        final var value = ctx.postfixExpr().accept(this);
        context.setValue(value);
        final var filtered = ctx.predicate().accept(this);
        context = savedContext;
        visitedArgumentList = savedArgs;
        return filtered;
    }

    @Override
    public XQueryValue visitPredicate(final PredicateContext ctx) {
        final var contextValue = context.getValue();
        final var sequence = contextValue.atomize();
        final var filteredValues = new ArrayList<XQueryValue>(sequence.size());
        final var savedContext = saveContext();
        int index = 1;
        context.setSize(sequence.size());
        for (final var contextItem : sequence) {
            context.setValue(contextItem);
            context.setPosition(index);
            final XQueryValue visitedExpression = ctx.expr().accept(this);
            final XQueryValue items = handleAsItemGetter(sequence, visitedExpression);
            if (items != null) {
                context = savedContext;
                return items;
            }

            if (visitedExpression.effectiveBooleanValue()) {
                filteredValues.add(contextItem);
            }
            index++;
        }
        context = savedContext;
        return valueFactory.sequence(filteredValues);
    }

    @Override
    public XQueryValue visitDynamicFunctionCall(DynamicFunctionCallContext ctx) {
        // TODO: verify logic
        final var contextItem = context.getValue();
        final var function = contextItem.functionValue();
        final var value = function.call(context, visitedArgumentList);
        return value;
    }

    // @Override
    // public XQueryValue visitLookupExpr(LookupExprContext ctx) {
    //     var map = ctx.postfixExpr().accept(this);
    //     if (map.mapEntries() == null)
    //         return XQueryError.Map;
    //     return matchedNodes;
    // }


    // @Override
    // public XQueryValue visitPostfixExpr(final PostfixExprContext ctx) {
    //     if (ctx.) {
    //         return ctx.primaryExpr().accept(this);
    //     }

    //     final var savedContext = saveContext();
    //     final var savedArgs = saveVisitedArguments();
    //     var value = ctx.primaryExpr().accept(this);
    //     int index = 1;
    //     context.setSize(ctx.postfix().size());
    //     for (final var postfix : ctx.postfix()) {
    //         context.setValue(value);
    //         context.setPosition(index);
    //         value = postfix.accept(this);
    //         index++;
    //     }
    //     context = savedContext;
    //     visitedArgumentList = savedArgs;
    //     return value;
    // }

    XQueryValue handleAsItemGetter(final List<XQueryValue> sequence,
            final XQueryValue visitedExpression) {
        if (visitedExpression.isNumericValue()) {
            final int i = visitedExpression.numericValue().intValue() - 1;
            if (i >= sequence.size() || i < 0) {
                return valueFactory.emptySequence();
            }
            return sequence.get(i);
        }
        if (visitedExpression.isSequence()) {
            if (visitedExpression.sequence().isEmpty())
                return valueFactory.emptySequence();
            final boolean allNumericValues = visitedExpression.sequence()
                    .stream()
                    .allMatch(XQueryValue::isNumericValue);
            if (allNumericValues) {
                final List<XQueryValue> items = new ArrayList<>();
                for (final var sequenceIndex : visitedExpression.sequence()) {
                    final int i = sequenceIndex.numericValue().intValue() - 1;
                    if (i >= sequence.size() || i < 0) {
                        continue;
                    }
                    items.add(sequence.get(i));
                }
                return valueFactory.sequence(items);
            }
        }
        return null;
    }


    @Override
    public XQueryValue visitContextItemExpr(final ContextItemExprContext ctx) {
        return context.getValue();
    }

    @Override
    public XQueryValue visitForwardStep(final ForwardStepContext ctx) {
        if (ctx.forwardAxis() != null) {
            ctx.forwardAxis().accept(this);
        } else {
            // the first slash will work
            // because of the fake root
            // '/*' will return the real root
            if (currentAxis == null) {
                currentAxis = XQueryAxis.CHILD;
            }
        }
        return ctx.nodeTest().accept(this);
    }

    @Override
    public XQueryValue visitReverseStep(final ReverseStepContext ctx) {
        if (ctx.abbrevReverseStep() != null) {
            return ctx.abbrevReverseStep().accept(this);
        }
        ctx.reverseAxis().accept(this);
        return ctx.nodeTest().accept(this);
    }

    @Override
    public XQueryValue visitAbbrevReverseStep(final AbbrevReverseStepContext ctx) {
        final var matchedParents = nodeGetter.getAllParents(matchedTreeNodes());
        return nodeSequence(matchedParents);
    }

    @Override
    public XQueryValue visitNodeTest(final NodeTestContext ctx) {
        return ctx.nameTest().accept(this);
    }

    private static final Function<NodeGetter, Function<List<ParseTree>, List<ParseTree>>>[] AXIS_DISPATCH_TABLE;

    static {
        @SuppressWarnings("unchecked")
        Function<NodeGetter, Function<List<ParseTree>, List<ParseTree>>>[] table =
            (Function<NodeGetter, Function<List<ParseTree>, List<ParseTree>>>[])
                new Function[XQueryAxis.values().length];

        table[XQueryAxis.ANCESTOR.ordinal()] = ng -> ng::getAllAncestors;
        table[XQueryAxis.ANCESTOR_OR_SELF.ordinal()] = ng -> ng::getAllAncestorsOrSelf;
        table[XQueryAxis.CHILD.ordinal()] = ng -> ng::getAllChildren;
        table[XQueryAxis.DESCENDANT.ordinal()] = ng -> ng::getAllDescendants;
        table[XQueryAxis.DESCENDANT_OR_SELF.ordinal()] = ng -> ng::getAllDescendantsOrSelf;
        table[XQueryAxis.FOLLOWING.ordinal()] = ng -> ng::getAllFollowing;
        table[XQueryAxis.FOLLOWING_SIBLING.ordinal()] = ng -> ng::getAllFollowingSiblings;
        table[XQueryAxis.FOLLOWING_OR_SELF.ordinal()] = ng -> ng::getAllFollowingOrSelf;
        table[XQueryAxis.FOLLOWING_SIBLING_OR_SELF.ordinal()] = ng -> ng::getAllFollowingSiblingsOrSelf;
        table[XQueryAxis.PARENT.ordinal()] = ng -> ng::getAllParents;
        table[XQueryAxis.PRECEDING.ordinal()] = ng -> ng::getAllPreceding;
        table[XQueryAxis.PRECEDING_SIBLING.ordinal()] = ng -> ng::getAllPrecedingSiblings;
        table[XQueryAxis.PRECEDING_OR_SELF.ordinal()] = ng -> ng::getAllPrecedingOrSelf;
        table[XQueryAxis.PRECEDING_SIBLING_OR_SELF.ordinal()] = ng -> ng::getAllPrecedingSiblingsOrSelf;
        table[XQueryAxis.SELF.ordinal()] = _ -> nodes -> nodes; // identity for SELF

        AXIS_DISPATCH_TABLE = table;
    }

    private final Predicate<String> canBeTokenName = Pattern.compile("^[\\p{IsUppercase}].*").asPredicate();

    @Override
    public XQueryValue visitNameTest(final NameTestContext ctx) {
        var matchedTreeNodes = matchedTreeNodes();
        final Function<NodeGetter, Function<List<ParseTree>,List<ParseTree>>> axisFunctionSelector = AXIS_DISPATCH_TABLE[currentAxis.ordinal()];
        final Function<List<ParseTree>, List<ParseTree>> axisFunction = axisFunctionSelector.apply(nodeGetter);
        final List<ParseTree> stepNodes = axisFunction.apply(matchedTreeNodes);

        if (ctx.wildcard() != null) {
            return switch (ctx.wildcard().getText()) {
                case "*" -> nodeSequence(stepNodes);
                // case "*:" -> ;
                // case ":*" -> ;
                default -> throw new AssertionError("Invalid wildcard");
            };
        }
        matchedTreeNodes = new ArrayList<>(stepNodes.size());
        final String name = ctx.qname().getText();
        if (canBeTokenName.test(name)) {
            // test for token type
            final int tokenType = parser.getTokenType(name);
            for (final ParseTree node : stepNodes) {
                // We skip nodes that are not terminal
                // i.e. are not tokens
                if (!(node instanceof TerminalNode))
                    continue;
                final TerminalNode tokenNode = (TerminalNode) node;
                final Token token = tokenNode.getSymbol();
                if (token.getType() == tokenType) {
                    matchedTreeNodes.add(tokenNode);
                }
            }
        } else { // test for rule
            final int ruleIndex = parser.getRuleIndex(name);
            for (final ParseTree node : stepNodes) {
                // Token nodes are being skipped
                if (!(node instanceof ParserRuleContext))
                    continue;
                final ParserRuleContext testedRule = (ParserRuleContext) node;
                if (testedRule.getRuleIndex() == ruleIndex) {
                    matchedTreeNodes.add(testedRule);
                }
            }
        }
        return nodeSequence(matchedTreeNodes);
    }


    @Override
    public XQueryValue visitForwardAxis(final ForwardAxisContext ctx) {
        if (ctx.CHILD() != null)
            currentAxis = XQueryAxis.CHILD;
        if (ctx.DESCENDANT() != null)
            currentAxis = XQueryAxis.DESCENDANT;
        if (ctx.SELF() != null)
            currentAxis = XQueryAxis.SELF;
        if (ctx.DESCENDANT_OR_SELF() != null)
            currentAxis = XQueryAxis.DESCENDANT_OR_SELF;
        if (ctx.FOLLOWING_SIBLING() != null)
            currentAxis = XQueryAxis.FOLLOWING_SIBLING;
        if (ctx.FOLLOWING() != null)
            currentAxis = XQueryAxis.FOLLOWING;
        if (ctx.FOLLOWING_SIBLING_OR_SELF() != null)
            currentAxis = XQueryAxis.FOLLOWING_SIBLING_OR_SELF;
        if (ctx.FOLLOWING_OR_SELF() != null)
            currentAxis = XQueryAxis.FOLLOWING_OR_SELF;
        return null;
    }

    @Override
    public XQueryValue visitReverseAxis(final ReverseAxisContext ctx) {
        if (ctx.PARENT() != null)
            currentAxis = XQueryAxis.PARENT;
        if (ctx.ANCESTOR() != null)
            currentAxis = XQueryAxis.ANCESTOR;
        if (ctx.PRECEDING_SIBLING_OR_SELF() != null)
            currentAxis = XQueryAxis.PRECEDING_SIBLING_OR_SELF;
        if (ctx.PRECEDING_OR_SELF() != null)
            currentAxis = XQueryAxis.PRECEDING_OR_SELF;
        if (ctx.PRECEDING_SIBLING() != null)
            currentAxis = XQueryAxis.PRECEDING_SIBLING;
        if (ctx.PRECEDING() != null)
            currentAxis = XQueryAxis.PRECEDING;
        if (ctx.ANCESTOR_OR_SELF() != null)
            currentAxis = XQueryAxis.ANCESTOR_OR_SELF;
        return null;
    }

    @Override
    public XQueryValue visitStringConcatExpr(final StringConcatExprContext ctx) {
        final var firstValue = ctx.rangeExpr(0).accept(this);
        if (ctx.CONCATENATION().isEmpty())
            return firstValue;
        List<XQueryValue> arguments = ctx.rangeExpr().stream().map(this::visit).toList();
        final var concatenated = concat.call(context, arguments);
        return concatenated;
    }

    @Override
    public XQueryValue visitArrowExpr(final ArrowExprContext ctx) {
        if (ctx.ARROW().isEmpty())
            return ctx.unaryExpr().accept(this);
        final var savedArgs = saveVisitedArguments();
        var contextArgument = ctx.unaryExpr().accept(this);
        visitedArgumentList.add(contextArgument);
        // var isString = !value.isStringValue();
        // var isFunction = !func
        final var arrowCount = ctx.ARROW().size();
        for (int i = 0; i < arrowCount; i++) {
            ctx.argumentList(i).accept(this); // visitedArgumentList is set to function's args
                                              // it has to be called before visiting arrowFunctionSpecifier
                                              // because arity of arguments is needed for function lookup
            final var visitedFunction = ctx.arrowFunctionSpecifier(i).accept(this);
            // TODO: add semantic check for no keyword args
            contextArgument = visitedFunction.functionValue().call(context, visitedArgumentList);
            visitedArgumentList = new ArrayList<>();
            visitedArgumentList.add(contextArgument);
        }
        visitedArgumentList = savedArgs;
        return contextArgument;
    }

    final NamespaceResolver namespaceResolver = new NamespaceResolver("fn");

    @Override
    public XQueryValue visitArrowFunctionSpecifier(final ArrowFunctionSpecifierContext ctx) {
        if (ctx.ID() != null) {
            final ResolvedName parts = namespaceResolver.resolve(ctx.ID().getText());
            final String namespace = parts.namespace();
            final String localName = parts.name();
            return functionManager.getFunctionReference(namespace, localName, visitedArgumentList.size());
        }
        if (ctx.varRef() != null)
            return ctx.varRef().accept(this);
        return ctx.parenthesizedExpr().accept(this);

    }

    private String[] resolveNamespace(final String functionName) {
        final String[] parts = functionName.split(":", 2);
        return parts;
    }

    @Override
    public XQueryValue visitAndExpr(final AndExprContext ctx) {
        var value = ctx.comparisonExpr(0).accept(this);
        if (ctx.AND().isEmpty())
            return value;
        if (!value.effectiveBooleanValue()) {
            return valueFactory.bool(false);
        }
        final var andCount = ctx.AND().size();
        for (int i = 1; i <= andCount; i++) {
            final var visitedExpression = ctx.comparisonExpr(i).accept(this);
            value = value.and(visitedExpression);
            if (!value.effectiveBooleanValue()) {
                return valueFactory.bool(false);
            }
        }
        return value;
    }

    @Override
    public XQueryValue visitAdditiveExpr(final AdditiveExprContext ctx) {
        var value = ctx.multiplicativeExpr(0).accept(this);
        if (ctx.additiveOperator().isEmpty())
            return value;
        final var operatorCount = ctx.additiveOperator().size();
        for (int i = 1; i <= operatorCount; i++) {
            final var visitedExpression = ctx.multiplicativeExpr(i).accept(this);
            value = switch (ctx.additiveOperator(i-1).getText()) {
                case "+" -> value.add(visitedExpression);
                case "-" -> value.subtract(visitedExpression);
                default -> null;
            };
        }
        return value;
    }

    @Override
    public XQueryValue visitComparisonExpr(final ComparisonExprContext ctx) {
        if (ctx.generalComp() != null)
            return handleGeneralComparison(ctx);
        if (ctx.valueComp() != null)
            return handleValueComparison(ctx);
        if (ctx.nodeComp() != null)
            return handleNodeComp(ctx);
        return ctx.otherwiseExpr(0).accept(this);
    }

    private XQueryValue handleGeneralComparison(final ComparisonExprContext ctx) {
        final var value = ctx.otherwiseExpr(0).accept(this);
        final var visitedExpression = ctx.otherwiseExpr(1).accept(this);
        return switch (ctx.generalComp().getText()) {
            case "=" -> value.generalEqual(visitedExpression);
            case "!=" -> value.generalUnequal(visitedExpression);
            case ">" -> value.generalGreaterThan(visitedExpression);
            case "<" -> value.generalLessThan(visitedExpression);
            case "<=" -> value.generalLessEqual(visitedExpression);
            case ">=" -> value.generalGreaterEqual(visitedExpression);
            default -> null;
        };
    }

    private XQueryValue handleValueComparison(final ComparisonExprContext ctx) {
        final var value = ctx.otherwiseExpr(0).accept(this);
        final var visitedExpression = ctx.otherwiseExpr(1).accept(this);
        if (value.isSequence() && value.empty().effectiveBooleanValue()) {
            return valueFactory.emptySequence();
        }
        if (visitedExpression.isSequence() && visitedExpression.empty().effectiveBooleanValue()) {
            return valueFactory.emptySequence();
        }
        return switch (ctx.valueComp().getText()) {
            case "eq" -> value.valueEqual(visitedExpression);
            case "ne" -> value.valueUnequal(visitedExpression);
            case "lt" -> value.valueLessThan(visitedExpression);
            case "gt" -> value.valueGreaterThan(visitedExpression);
            case "le" -> value.valueLessEqual(visitedExpression);
            case "ge" -> value.valueGreaterEqual(visitedExpression);
            default -> null;
        };
    }

    @Override
    public XQueryValue visitOtherwiseExpr(final OtherwiseExprContext ctx) {
        if (ctx.OTHERWISE().isEmpty())
            return ctx.stringConcatExpr(0).accept(this);
        final int length = ctx.stringConcatExpr().size();
        for (int i = 0; i < length - 1; i++) {
            final var expr = ctx.stringConcatExpr(i);
            final XQueryValue exprValue = expr.accept(this);
            if (exprValue.isSequence() && exprValue.sequence().isEmpty())
                continue;
            return exprValue;
        }
        return ctx.stringConcatExpr(length - 1).accept(this);
    }

    @Override
    public XQueryValue visitMultiplicativeExpr(final MultiplicativeExprContext ctx) {
        var value = ctx.unionExpr(0).accept(this);
        if (ctx.multiplicativeOperator().isEmpty())
            return value;
        final var orCount = ctx.multiplicativeOperator().size();
        for (int i = 1; i <= orCount; i++) {
            final var visitedExpression = ctx.unionExpr(i).accept(this);
            value = switch (ctx.multiplicativeOperator(i-1).getText()) {
                case "*" -> value.multiply(visitedExpression);
                case "x" -> value.multiply(visitedExpression);
                case "div" -> value.divide(visitedExpression);
                case "÷" -> value.divide(visitedExpression);
                case "idiv" -> value.integerDivide(visitedExpression);
                case "mod" -> value.modulus(visitedExpression);
                default -> null;
            };
        }
        return value;
    }

    @Override
    public XQueryValue visitUnionExpr(final UnionExprContext ctx) {
        var value = ctx.intersectExpr(0).accept(this);
        if (ctx.unionOperator().isEmpty())
            return value;
        final var unionCount = ctx.unionOperator().size();
        for (int i = 1; i <= unionCount; i++) {
            final var visitedExpression = ctx.intersectExpr(i).accept(this);
            value = value.union(visitedExpression);
        }
        return value;

    }

    @Override
    public XQueryValue visitIntersectExpr(final IntersectExprContext ctx) {
        var value = ctx.instanceofExpr(0).accept(this);
        if (ctx.exceptOrIntersect().isEmpty())
            return value;
        final var operatorCount = ctx.exceptOrIntersect().size();
        for (int i = 1; i <= operatorCount; i++) {
            final var visitedExpression = ctx.instanceofExpr(i).accept(this);
            final boolean isExcept = ctx.exceptOrIntersect(i-1).EXCEPT() != null;
            if (isExcept) {
                value = value.except(visitedExpression);
            } else {
                value = value.intersect(visitedExpression);
            }
        }
        return value;
    }

    @Override
    public XQueryValue visitSimpleMapExpr(final SimpleMapExprContext ctx) {
        final List<PathExprContext> terms = ctx.pathExpr();
        // if there's only one term, no mapping needed
        if (terms.size() == 1) {
            return terms.get(0).accept(this);
        }

        // start with the initial sequence
        final XQueryValue current = terms.get(0).accept(this);
        List<XQueryValue> sequence = current.atomize();

        // for each subsequent “! expr”
        for (int i = 1; i < terms.size(); i++) {
            final List<XQueryValue> nextSequence = new ArrayList<>();
            for (final XQueryValue item : sequence) {
                context.setValue(item);
                final XQueryValue mapped = terms.get(i).accept(this);
                nextSequence.addAll(mapped.atomize());
            }
            sequence = nextSequence;
        }

        return valueFactory.sequence(sequence);
    }

    @Override
    public XQueryValue visitUnaryExpr(final UnaryExprContext ctx) {
        final var value = ctx.simpleMapExpr().accept(this);
        if (ctx.MINUS() == null)
            return value;
        return value.multiply(valueFactory.number(new BigDecimal(-1)));
    }

    @Override
    public XQueryValue visitSwitchExpr(final SwitchExprContext ctx) {
        final SwitchComparandContext switchComparand = ctx.switchComparand();

        final XQueryValue switchedValue = switchComparand.switchedExpr != null
            ? switchComparand.switchedExpr.accept(this)
            : null;

        final SwitchCasesContext switchCasesCtx = ctx.switchCases();
        final SwitchCasesContext switchCases = switchCasesCtx != null
            ? switchCasesCtx
            : ctx.bracedSwitchCases().switchCases();

        final List<SwitchCaseClauseContext> caseClauseList = switchCases.switchCaseClause();

        final Map<XQueryValue, ParseTree> valueToExpression = caseClauseList.stream()
                .flatMap(clause -> {
                    final ExprSingleContext exprSingle = clause.exprSingle();
                    return clause.switchCaseOperand().stream()
                            .map(operand -> Map.entry(operand.expr().accept(this), exprSingle));
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        final ParseTree toBeExecuted = valueToExpression.getOrDefault(switchedValue, switchCases.defaultExpr);

        return toBeExecuted.accept(this);
    }

    @Override
    public XQueryValue visitArgument(final ArgumentContext ctx) {
        final var value = super.visitArgument(ctx);
        visitedArgumentList.add(value);
        return value;
    }

    private List<XQueryValue> saveVisitedArguments() {
        final var saved = visitedArgumentList;
        visitedArgumentList = new ArrayList<>();
        return saved;
    }

    private XQueryValue saveMatchedModes() {
        final XQueryValue saved = matchedNodes;
        matchedNodes = valueFactory.sequence(List.of());
        return saved;
    }

    private Stream<List<VariableCoupling>> saveVisitedTupleStream() {
        final Stream<List<VariableCoupling>> saved = visitedTupleStream;
        visitedTupleStream = Stream.of(List.of());
        return saved;
    }

    private XQueryVisitingContext saveContext() {
        final XQueryVisitingContext saved = context;
        context = new XQueryVisitingContext();
        return saved;
    }

    private XQueryAxis saveAxis() {
        final var saved = currentAxis;
        currentAxis = null;
        return saved;
    }

    private Comparator<List<VariableCoupling>> ascendingEmptyGreatest(final ParseTree expr) {
        return (tuple1, tuple2) -> {
            provideVariables(tuple1);
            final XQueryValue value1 = expr.accept(this);
            provideVariables(tuple2);
            final XQueryValue value2 = expr.accept(this);
            final boolean value1IsEmptySequence = value1.isSequence() && value1.sequence().isEmpty();
            final boolean value2IsEmptySequence = value2.isSequence() && value2.sequence().isEmpty();
            if (value1IsEmptySequence && !value2IsEmptySequence) {
                // empty greatest
                return 1;
            }
            return compareValues(value1, value2);
        };
    };

    private Comparator<List<VariableCoupling>> ascendingEmptyLeast(final ParseTree expr) {
        return (tuple1, tuple2) -> {
            provideVariables(tuple1);
            final XQueryValue value1 = expr.accept(this);
            provideVariables(tuple2);
            final XQueryValue value2 = expr.accept(this);
            final boolean value1IsEmptySequence = value1.isSequence() && value1.sequence().isEmpty();
            final boolean value2IsEmptySequence = value2.isSequence() && value2.sequence().isEmpty();
            if (value1IsEmptySequence && !value2IsEmptySequence) {
                // empty greatest
                return -1;
            }
            return compareValues(value1, value2);
        };
    };

    private Comparator<List<VariableCoupling>> descendingEmptyGreatest(final ParseTree expr) {
        return (tuple1, tuple2) -> {
            provideVariables(tuple1);
            final XQueryValue value1 = expr.accept(this);
            provideVariables(tuple2);
            final XQueryValue value2 = expr.accept(this);
            final boolean value1IsEmptySequence = value1.isSequence() && value1.sequence().isEmpty();
            final boolean value2IsEmptySequence = value2.isSequence() && value2.sequence().isEmpty();
            if (value1IsEmptySequence && !value2IsEmptySequence) {
                // empty greatest
                return -1;
            }
            return -compareValues(value1, value2);
        };
    };

    private Comparator<List<VariableCoupling>> descendingEmptyLeast(final ParseTree expr) {
        return (tuple1, tuple2) -> {
            provideVariables(tuple1);
            final XQueryValue value1 = expr.accept(this);
            provideVariables(tuple2);
            final XQueryValue value2 = expr.accept(this);
            final boolean value1IsEmptySequence = value1.isSequence() && value1.sequence().isEmpty();
            final boolean value2IsEmptySequence = value2.isSequence() && value2.sequence().isEmpty();
            if (value1IsEmptySequence && !value2IsEmptySequence) {
                // empty greatest
                return -1;
            }
            return -compareValues(value1, value2);
        };
    };

    private Comparator<List<VariableCoupling>> comparatorFromNthOrderSpec(final List<OrderSpecContext> orderSpecs,
            final int[] modifierMaskArray, final int i) {
        final OrderSpecContext orderSpec = orderSpecs.get(0);
        final ExprSingleContext expr = orderSpec.exprSingle();
        final int modifierMask = modifierMaskArray[i];
        return switch (modifierMask) {
            // ascending, empty greatest
            case 0b00 -> ascendingEmptyGreatest(expr);
            // ascending, empty least
            case 0b01 -> ascendingEmptyLeast(expr);
            // descending, empty greatest
            case 0b10 -> descendingEmptyGreatest(expr);
            // descending, empty least
            case 0b11 -> descendingEmptyLeast(expr);
            default -> null;
        };
    }

    @Override
    public XQueryValue visitIfExpr(final IfExprContext ctx) {
        final var condition = ctx.expr().accept(this);
        final var effectiveBooleanValue = condition.effectiveBooleanValue();
        final var isBraced = ctx.bracedAction() != null;
        if (isBraced) {
            if (effectiveBooleanValue) {
                return ctx.bracedAction().enclosedExpr().accept(this);
            } else {
                return valueFactory.emptySequence();
            }
        } else {
            if (effectiveBooleanValue)
                return ctx.unbracedActions().exprSingle(0).accept(this);
            else
                return ctx.unbracedActions().exprSingle(1).accept(this);
        }

    }

    @Override
    public XQueryValue visitWindowClause(final WindowClauseContext ctx) {
        if (ctx.tumblingWindowClause() != null) {
            return visitTumblingWindowClause(ctx.tumblingWindowClause());
        } else if (ctx.slidingWindowClause() != null) {
            return visitSlidingWindowClause(ctx.slidingWindowClause());
        }
        return null;
    }



    public XQueryValue visitTumblingWindowClause(final TumblingWindowClauseContext ctx) {
        final String windowVarName = ctx.varNameAndType().qname().getText();
        final XQueryValue sequence = ctx.exprSingle().accept(this);

        final String startVarName = getStartCurrentVarName(ctx.windowStartCondition());
        final String startPosVarName = getStartPositionalVarName(ctx.windowStartCondition());
        final String startPrevVarName = getStartPreviousVarName(ctx.windowStartCondition());
        final String startNextVarName = getStartNextVarName(ctx.windowStartCondition());

        final String endVarName = getEndCurrentVarName(ctx.windowEndCondition());
        final String endPosVarName = getEndPositionalVarName(ctx.windowEndCondition());
        final String endPrevVarName = getEndPreviousVarName(ctx.windowEndCondition());
        final String endNextVarName = getEndNextVarName(ctx.windowEndCondition());

        visitedTupleStream = visitedTupleStream.flatMap(tuple ->
            processTumblingWindowSubSequences(sequence, ctx, windowVarName,
                startVarName, startPosVarName, startPrevVarName, startNextVarName,
                endVarName, endPosVarName, endPrevVarName, endNextVarName,
                new ArrayList<>(tuple))
        );

        return null;
    }

    public XQueryValue visitSlidingWindowClause(final SlidingWindowClauseContext ctx) {
        final String windowVarName = ctx.varNameAndType().qname().getText();
        final XQueryValue sequence = ctx.exprSingle().accept(this);

        final String startVarName = getStartCurrentVarName(ctx.windowStartCondition());
        final String startPosVarName = getStartPositionalVarName(ctx.windowStartCondition());
        final String startPrevVarName = getStartPreviousVarName(ctx.windowStartCondition());
        final String startNextVarName = getStartNextVarName(ctx.windowStartCondition());

        final String endVarName = getEndCurrentVarName(ctx.windowEndCondition());
        final String endPosVarName = getEndPositionalVarName(ctx.windowEndCondition());
        final String endPrevVarName = getEndPreviousVarName(ctx.windowEndCondition());
        final String endNextVarName = getEndNextVarName(ctx.windowEndCondition());

        visitedTupleStream = visitedTupleStream.flatMap(tuple ->
            processSlidingWindowSubSequences(sequence, ctx, windowVarName,
                startVarName, startPosVarName, startPrevVarName, startNextVarName,
                endVarName, endPosVarName, endPrevVarName, endNextVarName,
                new ArrayList<>(tuple))
        );

        return null;
    }

    private String getStartCurrentVarName(final WindowStartConditionContext condition) {
        return condition != null && condition.windowVars() != null && condition.windowVars().currentVar() != null ?
            condition.windowVars().currentVar().varRef().varName().getText() : null;
    }

    private String getStartPositionalVarName(final WindowStartConditionContext condition) {
        return condition != null && condition.windowVars() != null && condition.windowVars().positionalVar() != null ?
            condition.windowVars().positionalVar().varName().getText() : null;
    }

    private String getStartPreviousVarName(final WindowStartConditionContext condition) {
        return condition != null && condition.windowVars() != null && condition.windowVars().previousVar() != null ?
            condition.windowVars().previousVar().varRef().varName().getText() : null;
    }

    private String getStartNextVarName(final WindowStartConditionContext condition) {
        return condition != null && condition.windowVars() != null && condition.windowVars().nextVar() != null ?
            condition.windowVars().nextVar().varRef().varName().getText() : null;
    }

    private String getEndCurrentVarName(final WindowEndConditionContext condition) {
        return condition != null && condition.windowVars() != null && condition.windowVars().currentVar() != null ?
            condition.windowVars().currentVar().varRef().varName().getText() : null;
    }

    private String getEndPositionalVarName(final WindowEndConditionContext condition) {
        return condition != null && condition.windowVars() != null && condition.windowVars().positionalVar() != null ?
            condition.windowVars().positionalVar().varName().getText() : null;
    }

    private String getEndPreviousVarName(final WindowEndConditionContext condition) {
        return condition != null && condition.windowVars() != null && condition.windowVars().previousVar() != null ?
            condition.windowVars().previousVar().varRef().varName().getText() : null;
    }

    private String getEndNextVarName(final WindowEndConditionContext condition) {
        return condition != null && condition.windowVars() != null && condition.windowVars().nextVar() != null ?
            condition.windowVars().nextVar().varRef().varName().getText() : null;
    }

    private Stream<List<VariableCoupling>> processTumblingWindowSubSequences(final XQueryValue sequence, final TumblingWindowClauseContext ctx,
        final String windowVarName, final String startVarName, final String startPosVarName, final String startPrevVarName, final String startNextVarName,
        final String endVarName, final String endPosVarName, final String endPrevVarName, final String endNextVarName, final List<VariableCoupling> initialTupleElements) {

        final List<XQueryValue> sequenceList = sequence.sequence();
        final List<List<VariableCoupling>> allTuples = new ArrayList<>();
        int startIndex = 0;

        while (startIndex < sequenceList.size()) {
            final WindowStartConditionContext windowStartCondition = ctx.windowStartCondition();
            if (isStartConditionMet(windowStartCondition, startIndex, sequenceList)) {
                final WindowEndConditionContext windowEndCondition = ctx.windowEndCondition();
                final int endIndex = findEndIndex(windowStartCondition, windowEndCondition, startIndex, sequenceList);

                if (endIndex < sequenceList.size() || !isOnlyEnd(windowEndCondition)) {
                    final List<XQueryValue> subSequence = sequenceList.subList(startIndex, endIndex + 1);
                    final List<VariableCoupling> windowTupleElements = new ArrayList<>(initialTupleElements);

                    addWindowVariables(windowTupleElements, windowVarName, subSequence, startIndex, endIndex,
                        startVarName, startPosVarName, startPrevVarName, startNextVarName,
                        endVarName, endPosVarName, endPrevVarName, endNextVarName);

                    allTuples.add(windowTupleElements);
                    if (endIndex + 1 > sequenceList.size() - 1)
                        break;
                    startIndex = endIndex + 1;
                } else {
                    break;
                }
            } else {
                if (startIndex + 1 > sequenceList.size() - 1)
                    break;
                startIndex++;
            }
        }

        return allTuples.stream();
    }

    private Stream<List<VariableCoupling>> processSlidingWindowSubSequences(final XQueryValue sequence, final SlidingWindowClauseContext ctx,
        final String windowVarName, final String startVarName, final String startPosVarName, final String startPrevVarName, final String startNextVarName,
        final String endVarName, final String endPosVarName, final String endPrevVarName, final String endNextVarName, final List<VariableCoupling> initialTupleElements) {

        final List<XQueryValue> sequenceList = sequence.sequence();
        final List<List<VariableCoupling>> allTuples = new ArrayList<>();
        int startIndex = 0;

        while (startIndex < sequenceList.size()) {
            final WindowStartConditionContext windowStartCondition = ctx.windowStartCondition();
            if (isStartConditionMet(windowStartCondition, startIndex, sequenceList)) {
                final WindowEndConditionContext windowEndCondition = ctx.windowEndCondition();
                final int endIndex = findEndIndex(windowStartCondition, windowEndCondition, startIndex, sequenceList);

                if (endIndex < sequenceList.size() || !isOnlyEnd(windowEndCondition)) {
                    final List<XQueryValue> subSequence = sequenceList.subList(startIndex, endIndex + 1);
                    final List<VariableCoupling> windowTupleElements = new ArrayList<>(initialTupleElements);

                    addWindowVariables(windowTupleElements, windowVarName, subSequence, startIndex, endIndex,
                        startVarName, startPosVarName, startPrevVarName, startNextVarName,
                        endVarName, endPosVarName, endPrevVarName, endNextVarName);

                    allTuples.add(windowTupleElements);
                    if (startIndex + 1 > sequenceList.size() - 1)
                        break;
                    startIndex++;
                } else {
                    break;
                }
            } else {
                if (startIndex + 1 > sequenceList.size() - 1)
                    break;
                startIndex++;
            }
        }

        return allTuples.stream();
    }

    private void addWindowVariables(final List<VariableCoupling> windowTupleElements, final String windowVarName, final List<XQueryValue> subSequence,
        final int startIndex, final int endIndex, final String startVarName, final String startPosVarName, final String startPrevVarName, final String startNextVarName,
        final String endVarName, final String endPosVarName, final String endPrevVarName, final String endNextVarName) {

        windowTupleElements.add(new VariableCoupling(new Variable(windowVarName, valueFactory.sequence(subSequence)), null, null, null));

        addStartVariables(windowTupleElements, subSequence, startIndex, startVarName, startPosVarName, startPrevVarName, startNextVarName);
        addEndVariables(windowTupleElements, subSequence, endIndex, endVarName, endPosVarName, endPrevVarName, endNextVarName);
    }

    private void addStartVariables(final List<VariableCoupling> windowTupleElements, final List<XQueryValue> subSequence, final int startIndex,
        final String startVarName, final String startPosVarName, final String startPrevVarName, final String startNextVarName) {

        if (startVarName != null) {
            final Variable startVar = new Variable(startVarName, subSequence.get(0));
            windowTupleElements.add(new VariableCoupling(startVar, null, null, null));
        }
        if (startPosVarName != null) {
            final Variable startPosVar = new Variable(startPosVarName, valueFactory.number(startIndex + 1));
            windowTupleElements.add(new VariableCoupling(startPosVar, null, null, null));
        }
        if (startPrevVarName != null) {
            final XQueryValue startPrevValue = startIndex > 0 ? subSequence.get(0) : valueFactory.emptySequence();
            final Variable startPrevVar = new Variable(startPrevVarName, startPrevValue);
            windowTupleElements.add(new VariableCoupling(startPrevVar, null, null, null));
        }
        if (startNextVarName != null) {
            final XQueryValue startNextValue = startIndex < subSequence.size() - 1 ? subSequence.get(1) : valueFactory.emptySequence();
            final Variable startNextVar = new Variable(startNextVarName, startNextValue);
            windowTupleElements.add(new VariableCoupling(startNextVar, null, null, null));
        }
    }

    private void addEndVariables(final List<VariableCoupling> windowTupleElements, final List<XQueryValue> subSequence, final int endIndex,
        final String endVarName, final String endPosVarName, final String endPrevVarName, final String endNextVarName) {

        if (endVarName != null) {
            final Variable endVar = new Variable(endVarName, subSequence.get(subSequence.size() - 1));
            windowTupleElements.add(new VariableCoupling(endVar, null, null, null));
        }
        if (endPosVarName != null) {
            final Variable endPosVar = new Variable(endPosVarName, valueFactory.number(endIndex + 1));
            windowTupleElements.add(new VariableCoupling(endPosVar, null, null, null));
        }
        if (endPrevVarName != null) {
            var vl =subSequence.size() > 1 ? subSequence.get(subSequence.size() - 2) : valueFactory.emptySequence();
            final Variable endPrevVar = new Variable(endPrevVarName, vl);
            windowTupleElements.add(new VariableCoupling(endPrevVar, null, null, null));
        }
        if (endNextVarName != null) {
            final Variable endNextVar = new Variable(endNextVarName, valueFactory.emptySequence());
            windowTupleElements.add(new VariableCoupling(endNextVar, null, null, null));
        }
    }

    private boolean isStartConditionMet(final WindowStartConditionContext ctx, final int currentIndex, final List<XQueryValue> sequenceList) {
        if (ctx != null && ctx.exprSingle() != null) {
            provideVariablesForCondition(ctx, currentIndex, sequenceList);
            return ctx.exprSingle().accept(this).effectiveBooleanValue();
        }
        return true;
    }

    private int findEndIndex(final WindowStartConditionContext startCtx,
            final WindowEndConditionContext ctx,
            final int startIndex,
            final List<XQueryValue> sequenceList)
    {
        int endIndex = startIndex;
        if (ctx != null && ctx.exprSingle() != null) {
            while (endIndex < sequenceList.size()) {
                provideVariablesForCondition(startCtx, startIndex, sequenceList);
                provideVariablesForCondition(ctx, endIndex, sequenceList);
                if (ctx.exprSingle().accept(this).effectiveBooleanValue()) {
                    break;
                }
                if (endIndex + 1 > sequenceList.size()-1)
                    break;
                endIndex++;
            }
        } else {
            endIndex = sequenceList.size() - 1;
        }
        return endIndex;
    }

    private boolean isOnlyEnd(final WindowEndConditionContext ctx) {
        return ctx != null && ctx.ONLY() != null;
    }

    private void provideVariablesForCondition(final WindowStartConditionContext ctx, final int currentIndex, final List<XQueryValue> sequenceList) {
        final var windowVars = ctx.windowVars();
        provideCurrentVariable(windowVars, currentIndex, sequenceList);
        providePositionalVariable(windowVars, currentIndex);
        providePreviousVariable(windowVars, currentIndex, sequenceList);
        provideNextVariable(windowVars, currentIndex, sequenceList);
    }

    private void provideVariablesForCondition(final WindowEndConditionContext ctx, final int currentIndex, final List<XQueryValue> sequenceList) {
        final var windowVars = ctx.windowVars();
        provideCurrentVariable(windowVars, currentIndex, sequenceList);
        providePositionalVariable(windowVars, currentIndex);
        providePreviousVariable(windowVars, currentIndex, sequenceList);
        provideNextVariable(windowVars, currentIndex, sequenceList);
    }

    private void provideCurrentVariable(final WindowVarsContext vars, final int currentIndex, final List<XQueryValue> sequenceList) {
        if (vars.currentVar() != null) {
            final String currentVarName = vars.currentVar().varRef().varName().getText();
            contextManager.provideVariable(currentVarName, sequenceList.get(currentIndex));
        }
    }

    private void providePositionalVariable(final WindowVarsContext vars, final int currentIndex) {
        if (vars.positionalVar() != null) {
            final String positionalVarName = vars.positionalVar().varName().getText();
            contextManager.provideVariable(positionalVarName, valueFactory.number(currentIndex + 1));
        }
    }

    private void providePreviousVariable(final WindowVarsContext vars, final int currentIndex, final List<XQueryValue> sequenceList) {
        if (vars.previousVar() != null) {
            final String previousVarName = vars.previousVar().varRef().varName().getText();
            contextManager.provideVariable(previousVarName, currentIndex > 0 ? sequenceList.get(currentIndex - 1) : valueFactory.emptySequence());
        }
    }

    private void provideNextVariable(final WindowVarsContext vars, final int currentIndex, final List<XQueryValue> sequenceList) {
        if (vars.nextVar() != null) {
            final String nextVarName = vars.nextVar().varRef().varName().getText();
            contextManager.provideVariable(nextVarName, currentIndex < sequenceList.size() - 1 ? sequenceList.get(currentIndex + 1) : valueFactory.emptySequence());
        }
    }

    private int compareValues(final XQueryValue value1, final XQueryValue value2) {
        if (value1.valueEqual(value2).effectiveBooleanValue()) {
            return 0;
        } else {
            if (value1.valueLessThan(value2).effectiveBooleanValue()) {
                return -1;
            }
            ;
            return 1;
        }
    }


    private void provideVariables(final List<VariableCoupling> tuple) {
        for (final var e : tuple) {
            if (e.item != null)
                contextManager.provideVariable(e.item.name, e.item.value);
            if (e.key != null)
                contextManager.provideVariable(e.key.name, e.key.value);
            if (e.value != null)
                contextManager.provideVariable(e.value.name, e.value.value);
            if (e.position != null)
                contextManager.provideVariable(e.position.name, e.position.value);
        }
    }

    @Override
    public XQueryValue visitStringConstructor(final StringConstructorContext ctx) {
        final StringBuilder result = new StringBuilder();

        // Przetwórz zawartość string constructora
        if (ctx.stringConstructorContent() != null) {
            final XQueryValue contentValue = ctx.stringConstructorContent().accept(this);
            result.append(contentValue.stringValue());
        }

        return valueFactory.string(result.toString());
    }

    @Override
    public XQueryValue visitStringConstructorContent(final StringConstructorContentContext ctx) {
        final StringBuilder result = new StringBuilder();

        for (int i = 0; i < ctx.getChildCount(); i++) {
            final var child = ctx.getChild(i);

            if (child instanceof ConstructorCharsContext) {
                // simple chars - unescape and append
                final ConstructorCharsContext charsCtx = (ConstructorCharsContext) child;
                final XQueryValue charsValue = charsCtx.accept(this);
                result.append(charsValue.stringValue());

            } else if (child instanceof ConstructorInterpolationContext) {
                // interpolation - evaluate and append
                final ConstructorInterpolationContext interpolationCtx = (ConstructorInterpolationContext) child;
                final XQueryValue interpolationValue = interpolationCtx.accept(this);
                result.append(interpolationValue.stringValue());
            }
        }

        return valueFactory.string(result.toString());
    }

    @Override
    public XQueryValue visitConstructorChars(final ConstructorCharsContext ctx) {
        final StringBuilder result = new StringBuilder();

        // Iterujemy przez wszystkie dzieci w kolejności wystąpienia
        for (int i = 0; i < ctx.getChildCount(); i++) {
            final ParseTree child = ctx.getChild(i);

            if (child instanceof TerminalNode) {
                final TerminalNode terminal = (TerminalNode) child;
                result.append(terminal.getText());
            }
        }

        return valueFactory.string(unescapeConstructorChars(result.toString()));
    }

    @Override
    public XQueryValue visitConstructorInterpolation(final ConstructorInterpolationContext ctx) {
        // Is { expr } or {} ?
        if (ctx.expr() != null) {
            // { expr } -> expr.stringValue()
            final XQueryValue exprValue = ctx.expr().accept(this);
            return valueFactory.string(processInterpolationValue(exprValue));
        } else {
            // {} -> empty string
            return valueFactory.string("");
        }
    }

    private String unescapeConstructorChars(final String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        final StringBuilder result = new StringBuilder();
        final int length = str.length();

        for (int i = 0; i < length; i++) {
            final char ch = str.charAt(i);

            if (ch == '`' && i + 1 < length) {
                final char nextChar = str.charAt(i + 1);
                if (nextChar == '`') {
                    // Escaped backtick: `` -> `
                    result.append('`');
                    i++;
                } else if (nextChar == '{') {
                    // Escaped opening brace sequence: `{ -> {
                    result.append('{');
                    i++;
                } else {
                    // Normal backtick
                    result.append(ch);
                }
            } else if (ch == '\\' && i + 1 < length) {
                final char nextChar = str.charAt(i + 1);
                switch (nextChar) {
                    case '\\':
                        // Escaped backslash: \\ -> \
                        result.append('\\');
                        i++;
                        break;
                    case 'n':
                        // Newline: \n -> newline
                        result.append('\n');
                        i++;
                        break;
                    case 't':
                        // Tab: \t -> tab
                        result.append('\t');
                        i++;
                        break;
                    case 'r':
                        // Carriage return: \r -> CR
                        result.append('\r');
                        i++;
                        break;
                    case '"':
                        // Escaped double quote: \" -> "
                        result.append('"');
                        i++;
                        break;
                    case '\'':
                        // Escaped single quote: \' -> '
                        result.append('\'');
                        i++;
                        break;
                    case '{':
                        // Escaped opening brace: \{ -> {
                        result.append('{');
                        i++;
                        break;
                    case '}':
                        // Escaped closing brace: \} -> }
                        result.append('}');
                        i++;
                        break;
                    default:
                        // Unrecognized escape, ignore...
                        result.append(ch);
                        break;
                }
            } else {
                // Normal character
                result.append(ch);
            }
        }

        return result.toString();
    }

    private String processInterpolationValue(final XQueryValue value) {
        return value.atomize().stream()
                .map(XQueryValue::stringValue)
                .collect(Collectors.joining(" "));
    }


    @Override
    public XQueryValue visitCurlyArrayConstructor(CurlyArrayConstructorContext ctx) {
        return valueFactory.array(ctx.enclosedExpr().accept(this).atomize());
    }


    @Override
    public XQueryValue visitSquareArrayConstructor(SquareArrayConstructorContext ctx) {
        List<XQueryValue> values = ctx.exprSingle().stream().map(this::visit).toList();
        return valueFactory.array(values);
    }


    @Override
    public XQueryValue visitMapConstructor(MapConstructorContext ctx) {
        var map = ctx.mapConstructorEntry().stream()
            .collect(Collectors.toMap(
                entry -> entry.mapKeyExpr().accept(this),
                entry -> entry.mapValueExpr().accept(this),
                (existing, _) -> existing, // merge function (w przypadku duplikatów kluczy)
                LinkedHashMap::new // supplier - tworzy LinkedHashMap
            ));
        return valueFactory.map(Collections.unmodifiableMap(map));
    }

    @Override
    public XQueryValue visitPipelineExpr(PipelineExprContext ctx) {
        if (ctx.PIPE_ARROW().isEmpty())
            return ctx.arrowExpr(0).accept(this);
        final var saved = saveContext();
        final int size = ctx.arrowExpr().size();
        XQueryValue contextValue = ctx.arrowExpr(0).accept(this);
        for (var i = 1; i < size; i++ ) {
            final var contextualizedExpr = ctx.arrowExpr(i);
            context.setValue(contextValue);
            contextValue = contextualizedExpr.accept(this);
        }
        context = saved;
        return contextValue;
    }


}
