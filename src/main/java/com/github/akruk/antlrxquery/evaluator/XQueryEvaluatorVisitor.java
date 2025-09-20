package com.github.akruk.antlrxquery.evaluator;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.*;
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
import com.github.akruk.antlrxquery.XQueryAxis;
import com.github.akruk.antlrxquery.charescaper.XQueryCharEscaper;
import com.github.akruk.antlrxquery.AntlrXqueryParser.*;
import com.github.akruk.antlrxquery.evaluator.dynamiccontext.XQueryDynamicContextManager;
import com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.XQueryEvaluatingFunctionManager;
import com.github.akruk.antlrxquery.evaluator.values.*;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;
import com.github.akruk.antlrxquery.evaluator.values.factories.defaults.XQueryMemoizedValueFactory;
import com.github.akruk.antlrxquery.evaluator.values.operations.*;
import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver;
import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver.ResolvedName;
import com.github.akruk.antlrxquery.semanticanalyzer.XQuerySemanticAnalyzer;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;
import com.github.akruk.nodegetter.NodeGetter;

public class XQueryEvaluatorVisitor extends AntlrXqueryParserBaseVisitor<XQueryValue> {
    private final XQueryValue root;
    private final Parser parser;
    private final XQueryDynamicContextManager contextManager;
    private final XQueryValueFactory valueFactory;
    private final XQueryEvaluatingFunctionManager functionManager;

    // Implementations of operators and other evaluation logic
    private final ValueComparisonOperator valueComparisonOperator;
    private final GeneralComparisonOperator generalComparisonOperator;
    private final NodeComparisonOperator nodeComparisonOperator;
    private final ValueBooleanOperator booleanOperator;
    private final NodeOperator nodeOperator;
    private final EffectiveBooleanValue effectiveBooleanValue;
    private final ValueAtomizer atomizer;
    private final Stringifier stringifier;
    private final Caster caster;
    private final NodeGetter nodeGetter;

    // Functions used in logic
    private final XQueryFunction concat;
    private final XQueryFunction addition;
    private final XQueryFunction subtraction;
    private final XQueryFunction multiplication;
    private final XQueryFunction division;
    private final XQueryFunction integerDivision;
    private final XQueryFunction modulus;
    private final XQueryFunction unaryPlus;
    private final XQueryFunction unaryMinus;
    private final XQueryFunction string;

    private final XQueryValue emptySequence;

    private final XQuerySemanticAnalyzer semanticAnalyzer;
    // private final XQueryTypeFactory typeFactory;


    private XQueryValue matchedNodes;
    private XQueryAxis currentAxis;
    private List<XQueryValue> visitedPositionalArguments;
    private XQueryVisitingContext context;
    private Stream<List<VariableCoupling>> visitedTupleStream;
    private Map<String, XQueryValue> visitedKeywordArguments;


    private record VariableCoupling(Variable item, Variable key, Variable value, Variable position) {}
    private record Variable(String name, XQueryValue value){}

    public XQueryEvaluatorVisitor(final ParseTree tree, final Parser parser, final XQuerySemanticAnalyzer analyzer, final XQueryTypeFactory typeFactory) {
        this(tree, parser, new XQueryMemoizedValueFactory(typeFactory), analyzer, typeFactory);
    }

    public XQueryEvaluatorVisitor(
            final ParseTree tree,
            final Parser parser,
            final XQueryValueFactory valueFactory,
            final XQuerySemanticAnalyzer analyzer,
            final XQueryTypeFactory typeFactory)
    {
        this.semanticAnalyzer = analyzer;
        this.root = valueFactory.node(tree);
        this.context = new XQueryVisitingContext();
        this.context.setValue(root);
        this.context.setPosition(0);
        this.context.setSize(0);
        this.parser = parser;
        this.valueFactory = valueFactory;
        // this.typeFactory = typeFactory;
        this.effectiveBooleanValue = new EffectiveBooleanValue(valueFactory);
        this.stringifier = new Stringifier(valueFactory, effectiveBooleanValue);
        this.valueComparisonOperator = new ValueComparisonOperator(valueFactory);
        this.atomizer = new ValueAtomizer();
        this.nodeGetter = new NodeGetter();
        this.generalComparisonOperator = new GeneralComparisonOperator(valueFactory, atomizer, valueComparisonOperator);
        this.nodeComparisonOperator = new NodeComparisonOperator(valueFactory, root.node);
        this.booleanOperator = new ValueBooleanOperator(this, valueFactory, effectiveBooleanValue);
        this.nodeOperator = new NodeOperator(valueFactory);
        this.functionManager = new XQueryEvaluatingFunctionManager(this, parser, valueFactory, nodeGetter, typeFactory, effectiveBooleanValue, atomizer, valueComparisonOperator);
        this.contextManager = new XQueryDynamicContextManager();
        this.concat = functionManager.getFunctionReference("fn", "concat", 2).functionValue;
        this.string = functionManager.getFunctionReference("fn", "string", 1).functionValue;
        this.addition = functionManager.getFunctionReference("op", "numeric-add", 2).functionValue;
        this.subtraction = functionManager.getFunctionReference("op", "numeric-subtract", 2).functionValue;
        this.multiplication = functionManager.getFunctionReference("op", "numeric-multiply", 2).functionValue;
        this.division = functionManager.getFunctionReference("op", "numeric-divide", 2).functionValue;
        this.integerDivision = functionManager.getFunctionReference("op", "numeric-integer-divide", 2).functionValue;
        this.modulus = functionManager.getFunctionReference("op", "numeric-mod", 2).functionValue;
        this.unaryPlus = functionManager.getFunctionReference("op", "numeric-unary-plus", 1).functionValue;
        this.unaryMinus = functionManager.getFunctionReference("op", "numeric-unary-minus", 1).functionValue;
        this.emptySequence = valueFactory.emptySequence();
        this.caster = new Caster(typeFactory, valueFactory, stringifier, effectiveBooleanValue);
        contextManager.enterContext();
    }

    @Override
    public XQueryValue visitXquery(XqueryContext ctx) {
        if (ctx.libraryModule() != null)
            return null;
        return visitMainModule(ctx.mainModule());
    }


    @Override
    public XQueryValue visitFLWORExpr(final FLWORExprContext ctx) {
        final var savedTupleStream = saveVisitedTupleStream();
        contextManager.enterScope();
        // visitedTupleStream will be manipulated to prepare result stream
        visitInitialClause(ctx.initialClause());
        for (final var clause : ctx.intermediateClause()) {
            clause.accept(this);
        }
        // at this point visitedTupleStream should contain all tuples
        final var expressionValue = visitReturnClause(ctx.returnClause());
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
                final String variableName = streamVariable.varNameAndType().varRef().qname().getText();
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
        final String variableName = ctx.varNameAndType().varRef().qname().getText();
        final List<XQueryValue> sequence = visitExprSingle(ctx.exprSingle()).sequence;
        final PositionalVarContext positional = ctx.positionalVar();
        final boolean allowingEmpty = ctx.allowingEmpty() != null;
        String positionalName = null;
        Variable positionalVar = null;
        if (positional != null) {
            positionalName = positional.varRef().qname().getText();
            positionalVar = new Variable(positionalName, valueFactory.number(0));
        }

        if (sequence.isEmpty() && allowingEmpty) {
            final var emptyVar = new Variable(variableName, emptySequence);
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
        final String variableName = ctx.varNameAndType().varRef().qname().getText();
        final XQueryValue arrayValue = visitExprSingle(ctx.exprSingle());
        final PositionalVarContext positional = ctx.positionalVar();

        final List<XQueryValue> arrayMembers = arrayValue.arrayMembers;

        if (positional != null) {
            final String positionalName = positional.varRef().qname().getText();
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
        final XQueryValue mapValue = visitExprSingle(ctx.exprSingle());
        final PositionalVarContext positional = ctx.positionalVar();

        final ForEntryKeyBindingContext keyBinding = ctx.forEntryKeyBinding();
        final ForEntryValueBindingContext valueBinding = ctx.forEntryValueBinding();

        final Map<XQueryValue, XQueryValue> mapEntries = mapValue.mapEntries;
        final List<VariableCoupling> tupleElements = new ArrayList<>();

        int index = 1;
        for (final Map.Entry<XQueryValue, XQueryValue> entry : mapEntries.entrySet()) {
            Variable positionVar = null;
            if (positional != null) {
                final String positionalName = positional.varRef().qname().getText();
                final XQueryValue position = valueFactory.number(index);
                positionVar = new Variable(positionalName, position);
            }

            Variable keyVar = null;
            if (keyBinding != null) {
                final String keyName = keyBinding.varNameAndType().varRef().qname().getText();
                final XQueryValue keyValue = entry.getKey();
                keyVar = new Variable(keyName, keyValue);
            }

            Variable valueVar = null;
            if (valueBinding != null) {
                final String valueName = valueBinding.varNameAndType().varRef().qname().getText();
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
        final String countVariableName = ctx.varRef().qname().getText();
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
            return effectiveBooleanValue.effectiveBooleanValue(filter).booleanValue;
        });
        return null;
    }

    @Override
    public XQueryValue visitReturnClause(final ReturnClauseContext ctx) {
        final List<XQueryValue> results = visitedTupleStream.map((tupleStream) -> {
            provideVariables(tupleStream);
            final XQueryValue value = visitExprSingle(ctx.exprSingle());
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
            return effectiveBooleanValue.effectiveBooleanValue(filter).booleanValue;
        });
        return null;
    }

    @Override
    public XQueryValue visitVarRef(final VarRefContext ctx) {
        final String variableName = ctx.qname().getText();
        final XQueryValue variableValue = contextManager.getVariable(variableName);
        return variableValue;
    }

    @Override
    public XQueryValue visitLiteral(final LiteralContext ctx) {
        if (ctx.STRING() != null) {
            return handleString(ctx);
        }

        final var numeric = ctx.numericLiteral();
        if (numeric.IntegerLiteral() != null) {
            return handleInteger(numeric.IntegerLiteral());
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

    private XQueryValue handleInteger(final TerminalNode integerLiteral) {
        final String value = integerLiteral.getText().replace("_", "");
        return valueFactory.number(new BigDecimal(value));
    }

    @Override
    public XQueryValue visitParenthesizedExpr(final ParenthesizedExprContext ctx) {
        // Empty parentheses mean an empty sequence '()'
        if (ctx.expr() == null) {
            return valueFactory.sequence(List.of());
        }
        return visitExpr(ctx.expr());
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
            if (expressionValue.isError)
                return expressionValue;
            if (expressionValue.size == 1) {
                result.add(expressionValue.sequence.get(0));
                continue;
            }
            // If the result is not atomic we atomize it
            // and extend the result list
            final var atomizedValues = atomizer.atomize(expressionValue);
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
        final List<XQueryValue> savedArgs = saveVisitedArguments();
        final Map<String, XQueryValue> savedKwargs = saveVisitedKeywordArguments();
        ctx.argumentList().accept(this);
        final String functionQname = ctx.functionName().getText();
        final XQueryValue callResult = callFunction(functionQname, visitedPositionalArguments, visitedKeywordArguments);
        visitedPositionalArguments = savedArgs;
        visitedKeywordArguments = savedKwargs;
        return callResult;
    }

    private XQueryValue callFunction(
            final String qname,
            final List<XQueryValue> args,
            final Map<String, XQueryValue> kwargs)
    {
        final String[] parts = resolveNamespace(qname);
        final String namespace = parts.length == 2 ? parts[0] : "fn";
        final String functionName = parts.length == 2 ? parts[1] : parts[0];
        final var value = functionManager.call(namespace, functionName, context, args, kwargs);
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
                .map(binding -> binding.varNameAndType().varRef().qname().getText())
                .toList();

        final List<List<XQueryValue>> sequences = quantifierBindings.stream()
                .map(binding -> binding.exprSingle().accept(this).sequence)
                .toList();

        final ExprSingleContext criterionNode = ctx.exprSingle();

        if (ctx.EVERY() != null) {
            final boolean every = cartesianProduct(sequences).allMatch(variableProduct -> {
                for (int i = 0; i < variableNames.size(); i++) {
                    contextManager.provideVariable(variableNames.get(i), variableProduct.get(i));
                }
                return effectiveBooleanValue.effectiveBooleanValue(criterionNode.accept(this)).booleanValue;
            });
            return valueFactory.bool(every);
        }

        if (ctx.SOME() != null) {
            final boolean some = cartesianProduct(sequences).anyMatch(variableProduct -> {
                for (int i = 0; i < variableNames.size(); i++) {
                    contextManager.provideVariable(variableNames.get(i), variableProduct.get(i));
                }
                final XQueryValue accept = criterionNode.accept(this);
                return effectiveBooleanValue.effectiveBooleanValue(accept).booleanValue;
            });
            return valueFactory.bool(some);
        }

        return null;
    }




    private XQueryValue handleNodeComp(final ComparisonExprContext ctx) {
        final var visitedLeft = ctx.otherwiseExpr(0).accept(this);
        final var visitedRight = ctx.otherwiseExpr(1).accept(this);
        return switch (ctx.nodeComp().getText()) {
            case "is" -> nodeComparisonOperator.is(visitedLeft, visitedRight);
            case "is-not" -> nodeComparisonOperator.isNot(visitedLeft, visitedRight);
            case "precedes" -> nodeComparisonOperator.precedes(visitedLeft, visitedRight);
            case "<<" -> nodeComparisonOperator.precedes(visitedLeft, visitedRight);
            case "follows" -> nodeComparisonOperator.follows(visitedLeft, visitedRight);
            case ">>" -> nodeComparisonOperator.follows(visitedLeft, visitedRight);
            default -> null; // shouldn't be reachable
        };
    }

    @Override
    public XQueryValue visitEnclosedExpr(final EnclosedExprContext ctx) {
        if (ctx.expr() == null)
            return emptySequence;
        return visitExpr(ctx.expr());
    }

    @Override
    public XQueryValue visitRangeExpr(final RangeExprContext ctx) {
        final var fromValue = ctx.additiveExpr(0).accept(this);
        if (ctx.TO() == null)
            return fromValue;
        final var toValue = ctx.additiveExpr(1).accept(this);
        if (toValue.isEmptySequence)
            return emptySequence;
        if (fromValue.isEmptySequence)
            return emptySequence;
        final int fromInt = fromValue.numericValue.intValue();
        final int toInt = toValue.numericValue.intValue();
        if (fromInt > toInt)
            return emptySequence;
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
            matchedNodes = nodeSequence(List.of(root.node));
            currentAxis = XQueryAxis.CHILD;
            final var resultingNodeSequence = visitRelativePathExpr(ctx.relativePathExpr());
            matchedNodes = savedNodes;
            currentAxis = savedAxis;
            return resultingNodeSequence;
        }
        final boolean useDescendantOrSelfAxis = ctx.SLASHES() != null;
        if (useDescendantOrSelfAxis) {
            final var savedNodes = saveMatchedModes();
            final var savedAxis = saveAxis();
            matchedNodes = nodeSequence(List.of(root.node));
            currentAxis = XQueryAxis.DESCENDANT_OR_SELF;
            final var resultingNodeSequence = visitRelativePathExpr(ctx.relativePathExpr());
            matchedNodes = savedNodes;
            currentAxis = savedAxis;
            return resultingNodeSequence;
        }
        return visitRelativePathExpr(ctx.relativePathExpr());
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
        return matchedNodes.sequence.stream().map(v->v.node).toList();
    }

    @Override
    public XQueryValue visitStepExpr(final StepExprContext ctx) {
        if (ctx.postfixExpr() != null)
            return ctx.postfixExpr().accept(this);
        return visitAxisStep(ctx.axisStep());
    }

    @Override
    public XQueryValue visitAxisStep(final AxisStepContext ctx) {
        XQueryValue stepResult = null;
        if (ctx.reverseStep() != null)
            stepResult = visitReverseStep(ctx.reverseStep());
        else if (ctx.forwardStep() != null)
            stepResult = visitForwardStep(ctx.forwardStep());
        if (ctx.predicateList().predicate().isEmpty()) {
            return stepResult;
        }
        final var savedContext = saveContext();
        final var savedArgs = saveVisitedArguments();
        int index = 1;
        context.setSize(stepResult.sequence.size());
        for (final var predicate : ctx.predicateList().predicate()) {
            context.setValue(stepResult);
            context.setPosition(index);
            stepResult = predicate.accept(this);
            index++;
        }
        context = savedContext;
        visitedPositionalArguments = savedArgs;
        return stepResult;
    }

    @Override
    public XQueryValue visitFilterExpr(final FilterExprContext ctx) {
        final var savedContext = saveContext();
        final var savedArgs = saveVisitedArguments();
        final var value = ctx.postfixExpr().accept(this);
        context.setValue(value);
        final var filtered = visitPredicate(ctx.predicate());
        context = savedContext;
        visitedPositionalArguments = savedArgs;
        return filtered;
    }


    @Override
    public XQueryValue visitPredicate(final PredicateContext ctx) {
        final var contextValue = context.getValue();
        final var filteredSequence = atomizer.atomize(contextValue);
        final var filteredValues = new ArrayList<XQueryValue>(filteredSequence.size());
        final var savedContext = saveContext();
        int index = 1;
        context.setSize(filteredSequence.size());
        for (final var contextItem : filteredSequence) {
            context.setValue(contextItem);
            context.setPosition(index);
            final XQueryValue visitedExpression = visitExpr(ctx.expr());
            if (visitedExpression.sequence.stream().allMatch(v->v.isNumeric)) {
                final XQueryValue items = handleAsItemGetter(filteredSequence, visitedExpression);
                context = savedContext;
                return items;
            }

            if (effectiveBooleanValue.effectiveBooleanValue(visitedExpression).booleanValue) {
                filteredValues.add(contextItem);
            }
            index++;
        }
        context = savedContext;
        return valueFactory.sequence(filteredValues);
    }

    @Override
    public XQueryValue visitArrowTarget(ArrowTargetContext ctx)
    {
        if (ctx.functionCall() != null) {
            ctx.functionCall().argumentList().accept(this);
            final String functionQname = ctx.functionCall().functionName().getText();
            return callFunction(functionQname, visitedPositionalArguments, visitedKeywordArguments);
        }
        return ctx.restrictedDynamicCall().accept(this);
    }


    @Override
    public XQueryValue visitVarDecl(VarDeclContext ctx)
    {
        if (ctx.EXTERNAL() != null)
            return null;
        var name = ctx.varNameAndType().varRef().qname().getText();
        var value = visitVarValue(ctx.varValue());
        contextManager.provideVariable(name, value);
        return null;
    }


    @Override
    public XQueryValue visitDynamicFunctionCall(final DynamicFunctionCallContext ctx)
    {
        // TODO: verify logic
        final var contextItem = context.getValue();
        final var function = contextItem.functionValue;
        final var value = function.call(context, visitedPositionalArguments);
        return value;
    }




    @Override
    public XQueryValue visitFunctionDecl(FunctionDeclContext ctx)
    {
        if (ctx.EXTERNAL() != null) {
            return null;
        }
        String qname = ctx.qname().getText();
        ResolvedName resolved = namespaceResolver.resolve(qname);
        var argNames = new ArrayList<String>();
        Map<String, ParseTree> defaults = new HashMap<String, ParseTree>();
        contextManager.enterScope();
        if (ctx.paramListWithDefaults() != null) {
            var params = ctx.paramListWithDefaults().paramWithDefault();
            for (ParamWithDefaultContext param : params) {
                var argName = param.varNameAndType().varRef().qname().anyName().getText();
                var defaultValue = param.exprSingle();
                if (defaultValue != null) {
                    defaults.put(argName, defaultValue);
                }
                argNames.add(argName);
            }
        }
        var body = ctx.functionBody().enclosedExpr();
        functionManager.registerFunction(
            resolved.namespace(), resolved.name(),
            (context, positionalArguments) -> {
                var saved = saveContext();
                contextManager.enterContext();
                this.context = context;
                for (int i = 0; i < positionalArguments.size(); i++) {
                    var arg = positionalArguments.get(i);
                    var argname = argNames.get(i);
                    contextManager.provideVariable(argname, arg);
                }
                var result = visitEnclosedExpr(body);
                contextManager.leaveContext();
                context = saved;
                return result;
            },
            argNames, defaults);

        contextManager.leaveScope();
        return null;
    }


    XQueryValue handleAsItemGetter(final List<XQueryValue> sequence,
            final XQueryValue getter)
    {
        if (getter.isEmptySequence)
            return emptySequence;
        if (getter.size == 1) {
            final int i = getter.numericValue.intValue() - 1;
            if (i >= sequence.size() || i < 0) {
                return emptySequence;
            }
            return sequence.get(i);
        }
        final List<XQueryValue> items = new ArrayList<>();
        for (final var sequenceIndex : getter.sequence) {
            final int i = sequenceIndex.numericValue.intValue() - 1;
            if (i >= sequence.size() || i < 0) {
                continue;
            }
            items.add(sequence.get(i));
        }
        return valueFactory.sequence(items);
    }


    @Override
    public XQueryValue visitContextItemExpr(final ContextItemExprContext ctx) {
        return context.getValue();
    }

    @Override
    public XQueryValue visitForwardStep(final ForwardStepContext ctx) {
        if (ctx.forwardAxis() != null) {
            visitForwardAxis(ctx.forwardAxis());
        } else {
            // the first slash will work
            // because of the fake root
            // '/*' will return the real root
            if (currentAxis == null) {
                currentAxis = XQueryAxis.CHILD;
            }
        }
        return visitNodeTest(ctx.nodeTest());
    }

    @Override
    public XQueryValue visitReverseStep(final ReverseStepContext ctx) {
        if (ctx.abbrevReverseStep() != null) {
            return visitAbbrevReverseStep(ctx.abbrevReverseStep());
        }
        visitReverseAxis(ctx.reverseAxis());
        return visitNodeTest(ctx.nodeTest());
    }

    @Override
    public XQueryValue visitAbbrevReverseStep(final AbbrevReverseStepContext ctx) {
        final var matchedParents = nodeGetter.getAllParents(matchedTreeNodes());
        return nodeSequence(matchedParents);
    }

    @Override
    public XQueryValue visitNodeTest(final NodeTestContext ctx) {
        var matchedTreeNodes = matchedTreeNodes();
        final Function<NodeGetter, Function<List<ParseTree>,List<ParseTree>>> axisFunctionSelector = AXIS_DISPATCH_TABLE[currentAxis.ordinal()];
        final Function<List<ParseTree>, List<ParseTree>> axisFunction = axisFunctionSelector.apply(nodeGetter);
        final List<ParseTree> stepNodes = axisFunction.apply(matchedTreeNodes);

        if (ctx.wildcard() != null) {
            return nodeSequence(stepNodes);
        }
        final Set<String> names = ctx.pathNameTestUnion().qname().stream()
            .map(QnameContext::getText).collect(Collectors.toSet());

        matchedTreeNodes = new ArrayList<>(stepNodes.size());

        final boolean[] isToken = new boolean[names.size()];
        final int[] ruleOrTokenIndices = new int[names.size()];
        int i = 0;
        for (final String name : names) {
            final boolean isToken_ = canBeTokenName.test(name);
            isToken[i] = isToken_;
            if (isToken_) {
                ruleOrTokenIndices[i] = parser.getTokenType(name);
            } else {
                ruleOrTokenIndices[i] = parser.getRuleIndex(name);
            }
            i++;
        }
        for (var node : stepNodes) {
            int j = 0;
            for (final String _ : names) {
                final int targetRuleOrTokenIndex = ruleOrTokenIndices[j];
                if (isToken[j]) {
                    if (!(node instanceof final TerminalNode tokenNode))
                        continue;
                    final Token token = tokenNode.getSymbol();
                    if (token.getType() == targetRuleOrTokenIndex) {
                        matchedTreeNodes.add(tokenNode);
                    }
                }
                else {
                    if (!(node instanceof final ParserRuleContext testedRule))
                        continue;
                    if (testedRule.getRuleIndex() == targetRuleOrTokenIndex) {
                        matchedTreeNodes.add(testedRule);
                    }
                }
                j++;
            }

        }
        return nodeSequence(matchedTreeNodes);
    }


    private final Predicate<String> canBeTokenName = Pattern.compile("^[\\p{IsUppercase}].*").asPredicate();




    private static final Function<NodeGetter, Function<List<ParseTree>, List<ParseTree>>>[] AXIS_DISPATCH_TABLE;

    static {
        @SuppressWarnings("unchecked")
        final
        Function<NodeGetter, Function<List<ParseTree>, List<ParseTree>>>[] table =
            (Function<NodeGetter, Function<List<ParseTree>, List<ParseTree>>>[])
                new Function[XQueryAxis.values().length];

        table[XQueryAxis.ANCESTOR.ordinal()] = nodeGetter -> nodeGetter::getAllAncestors;
        table[XQueryAxis.ANCESTOR_OR_SELF.ordinal()] = nodeGetter -> nodeGetter::getAllAncestorsOrSelf;
        table[XQueryAxis.CHILD.ordinal()] = nodeGetter -> nodeGetter::getAllChildren;
        table[XQueryAxis.DESCENDANT.ordinal()] = nodeGetter -> nodeGetter::getAllDescendants;
        table[XQueryAxis.DESCENDANT_OR_SELF.ordinal()] = nodeGetter -> nodeGetter::getAllDescendantsOrSelf;
        table[XQueryAxis.FOLLOWING.ordinal()] = nodeGetter -> nodeGetter::getAllFollowing;
        table[XQueryAxis.FOLLOWING_SIBLING.ordinal()] = nodeGetter -> nodeGetter::getAllFollowingSiblings;
        table[XQueryAxis.FOLLOWING_OR_SELF.ordinal()] = nodeGetter -> nodeGetter::getAllFollowingOrSelf;
        table[XQueryAxis.FOLLOWING_SIBLING_OR_SELF.ordinal()] = nodeGetter -> nodeGetter::getAllFollowingSiblingsOrSelf;
        table[XQueryAxis.PARENT.ordinal()] = nodeGetter -> nodeGetter::getAllParents;
        table[XQueryAxis.PRECEDING.ordinal()] = nodeGetter -> nodeGetter::getAllPreceding;
        table[XQueryAxis.PRECEDING_SIBLING.ordinal()] = nodeGetter -> nodeGetter::getAllPrecedingSiblings;
        table[XQueryAxis.PRECEDING_OR_SELF.ordinal()] = nodeGetter -> nodeGetter::getAllPrecedingOrSelf;
        table[XQueryAxis.PRECEDING_SIBLING_OR_SELF.ordinal()] = nodeGetter -> nodeGetter::getAllPrecedingSiblingsOrSelf;
        table[XQueryAxis.SELF.ordinal()] = _ -> nodes -> nodes; // identity for SELF

        AXIS_DISPATCH_TABLE = table;
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
        final List<XQueryValue> arguments = ctx.rangeExpr().stream().map(this::visit).toList();
        final var concatenated = concat.call(context, arguments);
        return concatenated;
    }

    @Override
    public XQueryValue visitArrowExpr(final ArrowExprContext ctx) {
        final boolean notSequenceArrow = ctx.sequenceArrowTarget().isEmpty();
        final boolean notMappingArrow = ctx.mappingArrowTarget().isEmpty();
        if (notSequenceArrow && notMappingArrow) {
            return visitUnaryExpr(ctx.unaryExpr());
        }
        final var savedArgs = saveVisitedArguments();
        final var savedKwargs = saveVisitedKeywordArguments();

        var contextArgument = ctx.unaryExpr().accept(this);
        visitedPositionalArguments.add(contextArgument);
        for (var arrowexpr : ctx.children.subList(1, ctx.children.size())) {
            contextArgument = arrowexpr.accept(this);
            visitedPositionalArguments = new ArrayList<>();
            visitedPositionalArguments.add(contextArgument);
            visitedKeywordArguments = new HashMap<>();
        }

        visitedPositionalArguments = savedArgs;
        visitedKeywordArguments = savedKwargs;
        return contextArgument;
    }

    @Override
    public XQueryValue visitMappingArrowTarget(MappingArrowTargetContext ctx) {
        XQueryValue mappedSequence = visitedPositionalArguments.get(visitedPositionalArguments.size()-1);
        ArrayList<XQueryValue> resultingSequence = new ArrayList<>(mappedSequence.size);
        for (XQueryValue el : mappedSequence.sequence) {
            visitedPositionalArguments = new ArrayList<>();
            visitedPositionalArguments.add(el);
            var call = ctx.arrowTarget().accept(this);
            if (call.isError)
                return call;
            resultingSequence.add(call);
        }
        return valueFactory.sequence(resultingSequence);
    }

    @Override
    public XQueryValue visitRestrictedDynamicCall(RestrictedDynamicCallContext ctx) {
        var function = ctx.children.get(0).accept(this);
        if (function.isError)
            return function;
        ctx.positionalArgumentList().accept(this);
        return function.functionValue.call(context, visitedPositionalArguments);
    }


    // public XQueryValue visitSequenceArrowTarget(final SequenceArrowTargetContext ctx) {
    //     if (ctx.ID() != null) {
    //         final ResolvedName parts = namespaceResolver.resolve(ctx.ID().getText());
    //         final String namespace = parts.namespace();
    //         final String localName = parts.name();
    //         return functionManager.getFunctionReference(namespace, localName, visitedArgumentList.size());
    //     }
    //     if (ctx.varRef() != null)
    //         return visitVarRef(ctx.varRef());
    //     return visitParenthesizedExpr(ctx.parenthesizedExpr());

    // }






    final NamespaceResolver namespaceResolver = new NamespaceResolver("fn");

    private String[] resolveNamespace(final String functionName) {
        final String[] parts = functionName.split(":", 2);
        return parts;
    }

    @Override
    public XQueryValue visitOrExpr(final OrExprContext ctx) {
        final var value = ctx.andExpr(0).accept(this);
        if (ctx.OR().isEmpty())
            return value;
        return booleanOperator.or(ctx.andExpr());
    }


    @Override
    public XQueryValue visitAndExpr(final AndExprContext ctx) {
        if (ctx.AND().isEmpty())
            return ctx.comparisonExpr(0).accept(this);
        return booleanOperator.and(ctx.comparisonExpr());
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
                case "+" -> addition.call(context, List.of(value, visitedExpression));
                case "-" -> subtraction.call(context, List.of(value, visitedExpression));
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
            case "=" ->  generalComparisonOperator.generalEquals(value, visitedExpression);
            case "!=" -> generalComparisonOperator.generalUnequals(value, visitedExpression);
            case ">" ->  generalComparisonOperator.generalGreaterThan(value, visitedExpression);
            // Operators such as < and > can use the full-width forms ＜ and ＞ to avoid the need for XML escaping.
            case "＞" ->  generalComparisonOperator.generalGreaterThan(value, visitedExpression);
            case "＜" -> generalComparisonOperator.generalLessThan(value, visitedExpression);
            case "<" ->  generalComparisonOperator.generalLessThan(value, visitedExpression);
            case "<=" -> generalComparisonOperator.generalLessEqual(value, visitedExpression);
            case ">=" -> generalComparisonOperator.generalGreaterEqual(value, visitedExpression);
            default -> null;
        };
    }

    private XQueryValue handleValueComparison(final ComparisonExprContext ctx) {
        final var value = ctx.otherwiseExpr(0).accept(this);
        final var visitedExpression = ctx.otherwiseExpr(1).accept(this);
        if (value.isEmptySequence) {
            return emptySequence;
        }
        if (visitedExpression.isEmptySequence) {
            return emptySequence;
        }
        return switch (ctx.valueComp().getText()) {
            case "eq" -> valueComparisonOperator.valueEquals(value, visitedExpression);
            case "ne" -> valueComparisonOperator.valueUnequal(value, visitedExpression);
            case "lt" -> valueComparisonOperator.valueLessThan(value, visitedExpression);
            case "gt" -> valueComparisonOperator.valueGreaterThan(value, visitedExpression);
            case "le" -> valueComparisonOperator.valueLessEqual(value, visitedExpression);
            case "ge" -> valueComparisonOperator.valueGreaterEqual(value, visitedExpression);
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
            if (exprValue.isEmptySequence)
                continue;
            return exprValue;
        }
        return ctx.stringConcatExpr(length - 1).accept(this);
    }

    // private final Num

    @Override
    public XQueryValue visitMultiplicativeExpr(final MultiplicativeExprContext ctx) {
        var value = ctx.unionExpr(0).accept(this);
        if (ctx.multiplicativeOperator().isEmpty())
            return value;
        final var orCount = ctx.multiplicativeOperator().size();
        for (int i = 1; i <= orCount; i++) {
            final var visitedExpression = ctx.unionExpr(i).accept(this);
            value = switch (ctx.multiplicativeOperator(i-1).getText()) {
                case "*" -> multiplication.call(context, List.of(value, visitedExpression));
                case "x" -> multiplication.call(context, List.of(value, visitedExpression));
                case "div" -> division.call(context, List.of(value, visitedExpression));
                case "÷" -> division.call(context, List.of(value, visitedExpression));
                case "idiv" -> integerDivision.call(context, List.of(value, visitedExpression));
                case "mod" -> modulus.call(context, List.of(value, visitedExpression));
                default -> null;
            };
        }
        return value;
    }

    @Override
    public XQueryValue visitUnionExpr(final UnionExprContext ctx) {
        if (ctx.unionOperator().isEmpty())
            return ctx.intersectExpr(0).accept(this);
        final var values = ctx.intersectExpr().stream().map(this::visit).toList();
        return nodeOperator.union(values);
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
                value = nodeOperator.except(List.of(value, visitedExpression));
            } else {
                value = nodeOperator.intersect(List.of(value, visitedExpression));
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
        List<XQueryValue> sequence = atomizer.atomize(current);

        // for each subsequent “! expr”
        for (int i = 1; i < terms.size(); i++) {
            final List<XQueryValue> nextSequence = new ArrayList<>();
            for (final XQueryValue item : sequence) {
                context.setValue(item);
                final XQueryValue mapped = terms.get(i).accept(this);
                nextSequence.addAll(atomizer.atomize(mapped));
            }
            sequence = nextSequence;
        }

        return valueFactory.sequence(sequence);
    }

    @Override
    public XQueryValue visitUnaryExpr(final UnaryExprContext ctx) {
        final var value = visitSimpleMapExpr(ctx.simpleMapExpr());
        if (ctx.PLUS() != null)
            return unaryPlus.call(context, List.of(value));
        if (ctx.MINUS() != null)
            return unaryMinus.call(context, List.of(value));
        return value;
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
        visitedPositionalArguments.add(value);
        return value;
    }

    private List<XQueryValue> saveVisitedArguments() {
        final var saved = visitedPositionalArguments;
        visitedPositionalArguments = new ArrayList<>();
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
            final boolean value1IsEmptySequence = value1.isEmptySequence;
            final boolean value2IsEmptySequence = value2.isEmptySequence;
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
            final boolean value1IsEmptySequence = value1.isEmptySequence;
            final boolean value2IsEmptySequence = value2.isEmptySequence;
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
            final boolean value1IsEmptySequence = value1.isEmptySequence;
            final boolean value2IsEmptySequence = value2.isEmptySequence;
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
            final boolean value1IsEmptySequence = value1.isEmptySequence;
            final boolean value2IsEmptySequence = value2.isEmptySequence;
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
        final var effectiveBooleanValue = this.effectiveBooleanValue.effectiveBooleanValue(condition);
        final var isBraced = ctx.bracedAction() != null;
        if (isBraced) {
            if (effectiveBooleanValue.booleanValue) {
                return ctx.bracedAction().enclosedExpr().accept(this);
            } else {
                return emptySequence;
            }
        } else {
            if (effectiveBooleanValue.booleanValue)
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
        final String windowVarName = ctx.varNameAndType().varRef().qname().getText();
        final XQueryValue sequence = visitExprSingle(ctx.exprSingle());

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
        final String windowVarName = ctx.varNameAndType().varRef().qname().getText();
        final XQueryValue sequence = visitExprSingle(ctx.exprSingle());

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
            condition.windowVars().currentVar().varRef().qname().getText() : null;
    }

    private String getStartPositionalVarName(final WindowStartConditionContext condition) {
        return condition != null && condition.windowVars() != null && condition.windowVars().positionalVar() != null ?
            condition.windowVars().positionalVar().varRef().qname().getText() : null;
    }

    private String getStartPreviousVarName(final WindowStartConditionContext condition) {
        return condition != null && condition.windowVars() != null && condition.windowVars().previousVar() != null ?
            condition.windowVars().previousVar().varRef().qname().getText() : null;
    }

    private String getStartNextVarName(final WindowStartConditionContext condition) {
        return condition != null && condition.windowVars() != null && condition.windowVars().nextVar() != null ?
            condition.windowVars().nextVar().varRef().qname().getText() : null;
    }

    private String getEndCurrentVarName(final WindowEndConditionContext condition) {
        return condition != null && condition.windowVars() != null && condition.windowVars().currentVar() != null ?
            condition.windowVars().currentVar().varRef().qname().getText() : null;
    }

    private String getEndPositionalVarName(final WindowEndConditionContext condition) {
        return condition != null && condition.windowVars() != null && condition.windowVars().positionalVar() != null ?
            condition.windowVars().positionalVar().varRef().qname().getText() : null;
    }

    private String getEndPreviousVarName(final WindowEndConditionContext condition) {
        return condition != null && condition.windowVars() != null && condition.windowVars().previousVar() != null ?
            condition.windowVars().previousVar().varRef().qname().getText() : null;
    }

    private String getEndNextVarName(final WindowEndConditionContext condition) {
        return condition != null && condition.windowVars() != null && condition.windowVars().nextVar() != null ?
            condition.windowVars().nextVar().varRef().qname().getText() : null;
    }

    private Stream<List<VariableCoupling>> processTumblingWindowSubSequences(final XQueryValue sequence, final TumblingWindowClauseContext ctx,
        final String windowVarName, final String startVarName, final String startPosVarName, final String startPrevVarName, final String startNextVarName,
        final String endVarName, final String endPosVarName, final String endPrevVarName, final String endNextVarName, final List<VariableCoupling> initialTupleElements) {

        final List<XQueryValue> sequenceList = sequence.sequence;
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

        final List<XQueryValue> sequenceList = sequence.sequence;
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
            final XQueryValue startPrevValue = startIndex > 0 ? subSequence.get(0) : emptySequence;
            final Variable startPrevVar = new Variable(startPrevVarName, startPrevValue);
            windowTupleElements.add(new VariableCoupling(startPrevVar, null, null, null));
        }
        if (startNextVarName != null) {
            final XQueryValue startNextValue = startIndex < subSequence.size() - 1 ? subSequence.get(1) : emptySequence;
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
            final var vl =subSequence.size() > 1 ? subSequence.get(subSequence.size() - 2) : emptySequence;
            final Variable endPrevVar = new Variable(endPrevVarName, vl);
            windowTupleElements.add(new VariableCoupling(endPrevVar, null, null, null));
        }
        if (endNextVarName != null) {
            final Variable endNextVar = new Variable(endNextVarName, emptySequence);
            windowTupleElements.add(new VariableCoupling(endNextVar, null, null, null));
        }
    }


    private boolean isStartConditionMet(final WindowStartConditionContext ctx, final int currentIndex, final List<XQueryValue> sequenceList) {
        if (ctx != null && ctx.exprSingle() != null) {
            provideVariablesForCondition(ctx, currentIndex, sequenceList);
            final var visited = ctx.exprSingle().accept(this);
            return effectiveBooleanValue.effectiveBooleanValue(visited).booleanValue;
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
                final XQueryValue accepted = ctx.exprSingle().accept(this);
                if (effectiveBooleanValue.effectiveBooleanValue(accepted).booleanValue) {
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
            final String currentVarName = vars.currentVar().varRef().qname().getText();
            contextManager.provideVariable(currentVarName, sequenceList.get(currentIndex));
        }
    }

    private void providePositionalVariable(final WindowVarsContext vars, final int currentIndex) {
        if (vars.positionalVar() != null) {
            final String positionalVarName = vars.positionalVar().varRef().qname().getText();
            contextManager.provideVariable(positionalVarName, valueFactory.number(currentIndex + 1));
        }
    }

    private void providePreviousVariable(final WindowVarsContext vars, final int currentIndex, final List<XQueryValue> sequenceList) {
        if (vars.previousVar() != null) {
            final String previousVarName = vars.previousVar().varRef().qname().getText();
            contextManager.provideVariable(previousVarName, currentIndex > 0 ? sequenceList.get(currentIndex - 1) : emptySequence);
        }
    }

    private void provideNextVariable(final WindowVarsContext vars, final int currentIndex, final List<XQueryValue> sequenceList) {
        if (vars.nextVar() != null) {
            final String nextVarName = vars.nextVar().varRef().qname().getText();
            contextManager.provideVariable(nextVarName, currentIndex < sequenceList.size() - 1 ? sequenceList.get(currentIndex + 1) : emptySequence);
        }
    }



    private int compareValues(final XQueryValue value1, final XQueryValue value2) {
        if (valueComparisonOperator.valueEquals(value1, value2).booleanValue) {
            return 0;
        } else {
            if (valueComparisonOperator.valueLessThan(value1, value2).booleanValue) {
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
            final XQueryValue contentValue = visitStringConstructorContent(ctx.stringConstructorContent());
            result.append(contentValue.stringValue);
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
                result.append(charsValue.stringValue);

            } else if (child instanceof ConstructorInterpolationContext) {
                // interpolation - evaluate and append
                final ConstructorInterpolationContext interpolationCtx = (ConstructorInterpolationContext) child;
                final XQueryValue interpolationValue = interpolationCtx.accept(this);
                result.append(interpolationValue.stringValue);
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
            // { expr } -> expr.stringValue
            final XQueryValue exprValue = visitExpr(ctx.expr());
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
        return atomizer.atomize(value).stream()
                .map(v->string.call(context, List.of(v)).stringValue)
                .collect(Collectors.joining(" "));
    }


    @Override
    public XQueryValue visitCurlyArrayConstructor(CurlyArrayConstructorContext ctx) {
        final XQueryValue enclosedValue = visitEnclosedExpr(ctx.enclosedExpr());
        final List<XQueryValue> atomized = atomizer.atomize(enclosedValue);
        return valueFactory.array(atomized);
    }


    @Override
    public XQueryValue visitSquareArrayConstructor(final SquareArrayConstructorContext ctx) {
        final List<XQueryValue> values = ctx.exprSingle().stream().map(this::visit).toList();
        return valueFactory.array(values);
    }


    @Override
    public XQueryValue visitMapConstructor(final MapConstructorContext ctx) {
        final var map = ctx.mapConstructorEntry().stream()
            .collect(Collectors.toMap(
                entry -> entry.mapKeyExpr().accept(this),
                entry -> entry.mapValueExpr().accept(this),
                (existing, _) -> existing, // merge function (w przypadku duplikatów kluczy)
                LinkedHashMap::new // supplier - tworzy LinkedHashMap
            ));
        return valueFactory.map(Collections.unmodifiableMap(map));
    }

    @Override
    public XQueryValue visitPipelineExpr(final PipelineExprContext ctx) {
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

    @Override
    public XQueryValue visitLookupExpr(final LookupExprContext ctx) {
        final var target = ctx.postfixExpr().accept(this);
        final var keySpecifier = getKeySpecifier(ctx);
        return evaluateLookup(target, keySpecifier);

    }

    private XQueryValue evaluateLookup(final XQueryValue target, final XQueryValue keySpecifier) {
        if (keySpecifier == null) {
            return evaluateWildcardLookup(target);
        } else {
            return evaluateKeyLookup(target, keySpecifier);
        }
    }

    private XQueryValue evaluateKeyLookup(final XQueryValue target, final XQueryValue keySpecifier) {
        final int resultsize = target.size * keySpecifier.size;
        final ArrayList<XQueryValue> results = new ArrayList<>(resultsize);
        for (final XQueryValue element : target.sequence) {
            switch (element.valueType) {
                case ARRAY:
                    if (!keySpecifier.isNumeric)
                        continue;
                    final int index = keySpecifier.numericValue.intValue()-1;
                    if (index > element.arrayMembers.size() || index < 0)
                        valueFactory.error(XQueryError.ArrayIndexOutOfBounds, getLookupArrayErrorMessage(keySpecifier, element));
                    results.add(element.arrayMembers.get(index));
                    break;
                case MAP:
                    final XQueryValue value = element.mapEntries.get(keySpecifier);
                    if (value != null)
                        results.add(value);
                    break;
                default:
                    break;
            }

        }
        return valueFactory.sequence(results);
    }

    private String getLookupArrayErrorMessage(final XQueryValue keySpecifier, final XQueryValue element) {
        return "Index: " + keySpecifier.numericValue + " is out of bounds for array " + element + " of size " + element.size;
    }

    private XQueryValue evaluateWildcardLookup(final XQueryValue target) {
        final int resultsize = target.size;
        final ArrayList<XQueryValue> results = new ArrayList<>(resultsize*resultsize);
        for (final XQueryValue element : target.sequence) {
            switch (element.valueType) {
                case ARRAY:
                    results.addAll(element.arrayMembers);
                    break;
                case MAP:
                    results.addAll(element.mapEntries.values());
                    break;
                default:
                    return valueFactory.error(XQueryError.InvalidArgumentType,
                    "Target of a lookup must be a sequence of arrays and maps, encountered: " + element);
            }
        }
        return valueFactory.sequence(results);
    }

    @Override
    public XQueryValue visitUnaryLookup(final UnaryLookupContext ctx) {
        final var target = context.getValue();
        final var keySpecifier = ctx.lookup().keySpecifier().accept(this);
        return evaluateLookup(target, keySpecifier);
    }


    XQueryValue getKeySpecifier(final LookupExprContext ctx) {
        final KeySpecifierContext keySpecifier = ctx.lookup().keySpecifier();
        if (keySpecifier.qname() != null) {
            return valueFactory.string(keySpecifier.qname().getText());
        }
        if (keySpecifier.STRING() != null ) {
            return handleString(keySpecifier);
        }
        if (keySpecifier.IntegerLiteral() != null) {
            return handleInteger(keySpecifier.IntegerLiteral());
        }
        return keySpecifier.accept(this);
    }

    private XQueryValue handleString(final ParserRuleContext ctx) {
        final String content = processStringLiteral(ctx);
        return valueFactory.string(content);
    }

    private String processStringLiteral(final ParserRuleContext ctx) {
        final String rawText = ctx.getText();
        final String content = unescapeString(rawText.substring(1, rawText.length() - 1));
        valueFactory.string(content);
        return content;
    }


    @Override
    public XQueryValue visitInstanceofExpr(final InstanceofExprContext ctx) {
        if (ctx.INSTANCE() == null)
            return visitTreatExpr(ctx.treatExpr());
        final var visited = visitTreatExpr(ctx.treatExpr());
        final var expectedType = ctx.sequenceType().accept(this.semanticAnalyzer);
        final boolean result = visited.type.isSubtypeOf(expectedType);
        return valueFactory.bool(result);
    }


    @Override
    public XQueryValue visitTreatExpr(final TreatExprContext ctx) {
        if (ctx.TREAT() == null)
            return visitCastableExpr(ctx.castableExpr());
        final var type = ctx.sequenceType().accept(semanticAnalyzer);
        final var expr =  visitCastableExpr(ctx.castableExpr());
        if (!expr.type.isSubtypeOf(type)) {
            return valueFactory.error(XQueryError.TreatAsTypeMismatch,
            "Type: " + expr.type + " cannot be treated as " + type);
        }
        return expr;
    }

    @Override
    public XQueryValue visitCastableExpr(final CastableExprContext ctx)
    {
        if (ctx.CASTABLE() == null)
            return visitCastExpr(ctx.castExpr());
        final XQuerySequenceType targetType = semanticAnalyzer.visitCastTarget(ctx.castTarget());
        final XQueryValue testedValue = visitCastExpr(ctx.castExpr());
        final boolean isCastable = !caster.cast(targetType, testedValue).isError;
        return valueFactory.bool(isCastable);
    }


    @Override
    public XQueryValue visitCastExpr(final CastExprContext ctx) {
        if (ctx.CAST() == null)
            return visitPipelineExpr(ctx.pipelineExpr());
        final XQuerySequenceType targetType = semanticAnalyzer.visitCastTarget(ctx.castTarget());
        final XQueryValue testedValue = visitPipelineExpr(ctx.pipelineExpr());
        final XQueryValue cast = caster.cast(targetType, testedValue);
        return cast;
    }

}
