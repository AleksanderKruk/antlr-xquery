package com.github.akruk.antlrxquery.evaluator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
import com.github.akruk.antlrxquery.AntlrXqueryParser.AbbrevReverseStepContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.AdditiveExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.AndExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ArgumentContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ArrowExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ArrowFunctionSpecifierContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.AxisStepContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.CastableExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ComparisonExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ContextItemExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.CountClauseContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.EnclosedExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ExprSingleContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.FLWORExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ForBindingContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ForClauseContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ForwardAxisContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ForwardStepContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.FunctionCallContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.IfExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.InstanceofExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.IntersectExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.LetBindingContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.LetClauseContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.LiteralContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.MultiplicativeExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.NameTestContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.NodeTestContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.OrExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.OrderByClauseContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.OrderSpecContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.OtherwiseExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ParenthesizedExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.PathExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.PositionalVarContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.PostfixContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.PostfixExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.PredicateContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.QnameContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.QuantifiedExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.RangeExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.RelativePathExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ReturnClauseContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ReverseAxisContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ReverseStepContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.StepExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.StringConcatExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.SwitchExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.TreatExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.UnaryExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.UnionExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.VarNameContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.VarRefContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.WhereClauseContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.WhileClauseContext;
import com.github.akruk.antlrxquery.contextmanagement.dynamiccontext.XQueryDynamicContextManager;
import com.github.akruk.antlrxquery.contextmanagement.dynamiccontext.baseimplementation.XQueryBaseDynamicContextManager;
import com.github.akruk.antlrxquery.evaluator.functioncaller.XQueryFunctionCaller;
import com.github.akruk.antlrxquery.evaluator.functioncaller.defaults.BaseFunctionCaller;
import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;
import com.github.akruk.antlrxquery.values.XQueryValue;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;
import com.github.akruk.antlrxquery.values.factories.defaults.XQueryMemoizedValueFactory;
import com.github.akruk.antlrxquery.values.XQueryBoolean;

public class XQueryEvaluatorVisitor extends AntlrXqueryParserBaseVisitor<XQueryValue> {
    final XQueryValue root;
    final Parser parser;
    final XQueryDynamicContextManager contextManager;
    final XQueryValueFactory valueFactory;
    final XQueryFunctionCaller functionCaller;

    XQueryValue matchedNodes;
    Stream<List<TupleElement>> visitedTupleStream;
    XQueryAxis currentAxis;
    List<XQueryValue> visitedArgumentList;
    XQueryVisitingContext context;

    private record TupleElement(String name, XQueryValue value, String positionalName, XQueryValue index){};

    private enum XQueryAxis {
        CHILD,
        DESCENDANT,
        SELF,
        DESCENDANT_OR_SELF,
        FOLLOWING_SIBLING,
        FOLLOWING,
        PARENT,
        ANCESTOR,
        PRECEDING_SIBLING,
        PRECEDING,
        ANCESTOR_OR_SELF,
        FOLLOWING_OR_SELF, FOLLOWING_SIBLING_OR_SELF, PRECEDING_SIBLING_OR_SELF, PRECEDING_OR_SELF,
    }



    public XQueryEvaluatorVisitor(final ParseTree tree, final Parser parser) {
        this(tree, parser, new XQueryBaseDynamicContextManager(),
                            new XQueryMemoizedValueFactory(),
                            new BaseFunctionCaller());
    }

    public XQueryEvaluatorVisitor(
            final ParseTree tree,
            final Parser parser,
            final XQueryDynamicContextManager contextManager,
            final XQueryValueFactory valueFactory,
            final XQueryFunctionCaller functionCaller)
    {
        this.root = valueFactory.node(tree);
        this.context = new XQueryVisitingContext();
        this.context.setItem(root);
        this.context.setPosition(0);
        this.context.setSize(0);
        this.parser = parser;
        this.valueFactory = valueFactory;
        this.functionCaller = functionCaller;
        this.contextManager = contextManager;
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
            final var newTuple = new ArrayList<TupleElement>(tuple.size() + newVariableCount);
            newTuple.addAll(tuple);
            for (final LetBindingContext streamVariable : ctx.letBinding()) {
                final String variableName = streamVariable.varName().getText();
                final XQueryValue assignedValue = streamVariable.exprSingle().accept(this);
                final var element = new TupleElement(variableName, assignedValue, null, null);
                newTuple.add(element);
                contextManager.provideVariable(variableName, assignedValue);
            }
            return newTuple;
        });
        return null;
    }



    @Override
    public XQueryValue visitForClause(final ForClauseContext ctx) {
        final int numberOfVariables = (int) ctx.forBinding().size();
        visitedTupleStream = visitedTupleStream.flatMap(tuple -> {
            final List<List<TupleElement>> newTupleLike = tuple.stream().map(e -> List.of(e)).collect(Collectors.toList());
            for (final ForBindingContext streamVariable : ctx.forBinding()) {
                final String variableName = streamVariable.varName().getText();
                final List<XQueryValue> sequence = streamVariable.exprSingle().accept(this).sequence();
                final PositionalVarContext positional = streamVariable.positionalVar();
                final int sequenceSize = sequence.size();
                if (positional != null) {
                    final List<TupleElement> elementsWithIndex = new ArrayList<>(numberOfVariables);
                    final String positionalName = positional.varName().getText();
                    for (int i = 0; i < sequenceSize; i++) {
                        final var value = sequence.get(i);
                        final var element = new TupleElement(variableName, value, positionalName, valueFactory.number(i + 1));
                        elementsWithIndex.add(element);
                    }
                    newTupleLike.add(elementsWithIndex);
                } else {
                    final List<TupleElement> elementsWithoutIndex = sequence.stream()
                            .map(value -> new TupleElement(variableName, value, null, null))
                            .toList();
                    newTupleLike.add(elementsWithoutIndex);
                }
            }
            return cartesianProduct(newTupleLike);
        }).map(tuple -> {
            // the newly declared variables need to be provided to the context
            final List<TupleElement> addedVariables = tuple.subList(tuple.size() - numberOfVariables, tuple.size());
            provideVariables(addedVariables);
            return tuple;
        });
        return null;
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
            final var newTuple = new ArrayList<TupleElement>(tuple.size() + 1);
            newTuple.addAll(tuple);
            final var element = new TupleElement(countVariableName, valueFactory.number(index.i++), null, null);
            contextManager.provideVariable(element.name, element.value);
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
    public XQueryValue visitVarRef(final VarRefContext ctx) {
        final String variableName = ctx.varName().getText();
        final XQueryValue variableValue = contextManager.getVariable(variableName);
        return variableValue;
    }

    @Override
    public XQueryValue visitReturnClause(final ReturnClauseContext ctx) {
        final List<XQueryValue> results = visitedTupleStream.map((_) -> {
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
    public XQueryValue visitLiteral(final LiteralContext ctx) {
        if (ctx.STRING() != null) {
            final String text = ctx.getText();
            final String removepars = ctx.getText().substring(1, text.length() - 1);
            final String string = unescapeString(removepars);
            return valueFactory.string(string);
        }

        if (ctx.INTEGER() != null) {
            return valueFactory.number(new BigDecimal(ctx.INTEGER().getText()));
        }

        return valueFactory.number(new BigDecimal(ctx.DECIMAL().getText()));
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




    // TODO: ESCAPE characters
    // &lt ...
    private String unescapeString(final String str) {
        return str.replace("\"\"", "\"").replace("''", "'");
    }

    @Override
    public XQueryValue visitFunctionCall(final FunctionCallContext ctx) {
        final var functionName = ctx.functionName().getText();
        // TODO: error handling missing function
        final var savedArgs = saveVisitedArguments();
        ctx.argumentList().accept(this);
        final var value = functionCaller.call(functionName, valueFactory, context, visitedArgumentList);
        visitedArgumentList = savedArgs;
        return value;
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
        final List<String> variableNames = ctx.varName().stream()
            .map(VarNameContext::qname)
            .map(QnameContext::getText)
            .toList();
        final int variableExpressionCount = ctx.exprSingle().size()-1;
        final List<List<XQueryValue>> sequences = new ArrayList<>(variableExpressionCount);
        for (final var expr : ctx.exprSingle().subList(0, variableExpressionCount)) {
            final var sequenceValue = expr.accept(this);
            sequences.add(sequenceValue.sequence());
        }

        final var criterionNode = ctx.exprSingle().getLast();
        if (ctx.EVERY() != null) {
            final boolean every = cartesianProduct(sequences).allMatch(variableProduct -> {
                for (int i = 0; i < variableNames.size(); i++) {
                    contextManager.provideVariable(variableNames.get(i), variableProduct.get(i));
                }
                return criterionNode.accept(this).booleanValue();
            });
            return XQueryBoolean.of(every);
        }
        if (ctx.SOME() != null) {
            final boolean some = cartesianProduct(sequences).anyMatch(variableProduct -> {
                for (int i = 0; i < variableNames.size(); i++) {
                    contextManager.provideVariable(variableNames.get(i), variableProduct.get(i));
                }
                return criterionNode.accept(this).booleanValue();
            });
            return XQueryBoolean.of(some);
        }
        return null;
    }


    @Override
    public XQueryValue visitOrExpr(final OrExprContext ctx) {
        try {
            var value = ctx.andExpr(0).accept(this);
            if (ctx.OR().isEmpty())
                return value;
            if (!value.isBooleanValue()) {
                // TODO: type error
            }
            // Short circuit
            if (value.booleanValue()) {
                return XQueryBoolean.TRUE;
            }
            final var orCount = ctx.OR().size();
            for (int i = 1; i <= orCount; i++) {
                final var visitedExpression = ctx.andExpr(i).accept(this);
                value = value.or(valueFactory, visitedExpression);
                // Short circuit
                if (value.booleanValue()) {
                    return XQueryBoolean.TRUE;
                }
            }
            return value;
        } catch (final XQueryUnsupportedOperation e) {
            // TODO: error handling
            return null;
        }
    }




    private XQueryValue handleNodeComp(final ComparisonExprContext ctx) {
        try {
            final var visitedLeft = ctx.otherwiseExpr(0).accept(this);
            if (visitedLeft.isSequence() && visitedLeft.empty(valueFactory).booleanValue())
                return valueFactory.emptySequence();
            final ParseTree nodeLeft = getSingleNode(visitedLeft);
            final var visitedRight = ctx.otherwiseExpr(1).accept(this);
            if (visitedRight.isSequence() && visitedRight.empty(valueFactory).booleanValue())
                return valueFactory.emptySequence();
            final ParseTree nodeRight = getSingleNode(visitedRight);
            final boolean result = switch (ctx.nodeComp().getText()) {
                case "is" -> nodeLeft == nodeRight;
                case "<<" -> getFollowing(nodeLeft).contains(nodeRight);
                case ">>" -> getPreceding(nodeLeft).contains(nodeRight);
                default -> false;
            };
            return valueFactory.bool(result);
        } catch (final XQueryUnsupportedOperation e) {
            return null;
        }

    }

    private ParseTree getSingleNode(final XQueryValue visitedLeft) throws XQueryUnsupportedOperation {
        ParseTree nodeLeft;
        if (visitedLeft.isAtomic()) {
            nodeLeft = visitedLeft.node();
        } else {
            final List<XQueryValue> sequenceLeft = visitedLeft.exactlyOne(valueFactory).sequence();
            nodeLeft = sequenceLeft.get(0).node();
        }
        return nodeLeft;
    }




    @Override
    public XQueryValue visitEnclosedExpr(EnclosedExprContext ctx) {
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
            .mapToObj(i->valueFactory.number(i))
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
            matchedNodes = switch (ctx.pathOperator(i-1).getText()) {
                case "//" -> {
                    final List<ParseTree> descendantsOrSelf = getAllDescendantsOrSelf(matchedTreeNodes());
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
            context.setItem(stepResult);
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
    //     var result = match;
    //     for (var predicate : ctx.predicate()) {
    //         predicate.accept(this);
    //     }
    //     return matchedTreeNodes();
    // }

    @Override
    public XQueryValue visitPostfixExpr(final PostfixExprContext ctx) {
        if (ctx.postfix().isEmpty()) {
            return ctx.primaryExpr().accept(this);
        }

        final var savedContext = saveContext();
        final var savedArgs = saveVisitedArguments();
        var value = ctx.primaryExpr().accept(this);
        int index = 1;
        context.setSize(ctx.postfix().size());
        for (final var postfix : ctx.postfix()) {
            context.setItem(value);
            context.setPosition(index);
            value = postfix.accept(this);
            index++;
        }
        context = savedContext;
        visitedArgumentList = savedArgs;
        return value;
    }

    @Override
    public XQueryValue visitPostfix(final PostfixContext ctx) {
        if (ctx.predicate() != null) {
            return ctx.predicate().accept(this);
        }
        final var contextItem = context.getItem();
        if (!contextItem.isFunction()) {
            // TODO: error
            return null;
        }
        final var function = contextItem.functionValue();
        final var value = function.call(valueFactory, context, visitedArgumentList);
        return value;
    }

    XQueryValue handleAsItemGetter(final List<XQueryValue> sequence,
                                    final XQueryValue visitedExpression)
    {
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
                List<XQueryValue> items = new ArrayList<>();
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
    public XQueryValue visitPredicate(final PredicateContext ctx) {
        final var contextValue = context.getItem();
        final var sequence = contextValue.atomize();
        final var filteredValues = new ArrayList<XQueryValue>(sequence.size());
        final var savedContext = saveContext();
        int index = 1;
        context.setSize(sequence.size());
        for (final var contextItem : sequence) {
            context.setItem(contextItem);
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
    public XQueryValue visitContextItemExpr(final ContextItemExprContext ctx) {
        return context.getItem();
    }

    @Override
    public XQueryValue visitForwardStep(final ForwardStepContext ctx) {
        if (ctx.forwardAxis() != null) {
            ctx.forwardAxis().accept(this);
        }
        else {
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
        final var matchedParents = getAllParents(matchedTreeNodes());
        return nodeSequence(matchedParents);
    }

    @Override
    public XQueryValue visitNodeTest(final NodeTestContext ctx) {
        return ctx.nameTest().accept(this);
    }

    private final Predicate<String> canBeTokenName = Pattern.compile("^[\\p{IsUppercase}].*").asPredicate();
    @Override
    public XQueryValue visitNameTest(final NameTestContext ctx) {
        var matchedTreeNodes = matchedTreeNodes();
        final List<ParseTree> stepNodes = switch (currentAxis) {
            case ANCESTOR -> getAllAncestors(matchedTreeNodes);
            case ANCESTOR_OR_SELF -> getAllAncestorsOrSelf(matchedTreeNodes);
            case CHILD -> getAllChildren(matchedTreeNodes);
            case DESCENDANT -> getAllDescendants(matchedTreeNodes);
            case DESCENDANT_OR_SELF -> getAllDescendantsOrSelf(matchedTreeNodes);
            case FOLLOWING -> getAllFollowing(matchedTreeNodes);
            case FOLLOWING_SIBLING -> getAllFollowingSiblings(matchedTreeNodes);
            case PARENT -> getAllParents(matchedTreeNodes);
            case PRECEDING -> getAllPreceding(matchedTreeNodes);
            case PRECEDING_SIBLING -> getAllPrecedingSiblings(matchedTreeNodes);
            case FOLLOWING_OR_SELF -> getAllFollowingOrSelf(matchedTreeNodes);
            case FOLLOWING_SIBLING_OR_SELF -> getAllFollowingSiblingsOrSelf(matchedTreeNodes);
            case PRECEDING_OR_SELF -> getAllPrecedingOrSelf(matchedTreeNodes);
            case PRECEDING_SIBLING_OR_SELF -> getAllPrecedingSiblingsOrSelf(matchedTreeNodes);
            case SELF -> matchedTreeNodes;
            default -> matchedTreeNodes;
        };
        if (ctx.wildcard() != null) {
            return switch(ctx.wildcard().getText()) {
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
        }
        else { // test for rule
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



    private List<ParseTree> getPrecedingSiblingsOrSelf(final ParseTree node) {
        final var newMatched = new ArrayList<ParseTree>();
        final var preceding = getPrecedingSiblings(node);
        newMatched.add(node);
        newMatched.addAll(preceding);
        return newMatched;
    }


    private List<ParseTree> getAllPrecedingSiblingsOrSelf(final List<ParseTree> matchedTreeNodes) {
        final var result = new ArrayList<ParseTree>();
        for (final var node : matchedTreeNodes) {
            final var followingSiblings = getPrecedingSiblingsOrSelf(node);
            result.addAll(followingSiblings);
        }
        return result;
    }


    private List<ParseTree> getFollowingSiblingsOrSelf(final ParseTree node) {
        final var newMatched = new ArrayList<ParseTree>();
        final var following = getFollowingSiblings(node);
        newMatched.add(node);
        newMatched.addAll(following);
        return newMatched;
    }


    private List<ParseTree> getAllFollowingSiblingsOrSelf(final List<ParseTree> matchedTreeNodes) {
        final var result = new ArrayList<ParseTree>();
        for (final var node : matchedTreeNodes) {
            final var followingSiblings = getFollowingSiblingsOrSelf(node);
            result.addAll(followingSiblings);
        }
        return result;
    }

    private List<ParseTree> getPrecedingOrSelf(final ParseTree node) {
        final var newMatched = new ArrayList<ParseTree>();
        final var following = getPreceding(node);
        newMatched.add(node);
        newMatched.addAll(following);
        return newMatched;
    }


    private List<ParseTree> getAllPrecedingOrSelf(final List<ParseTree> matchedTreeNodes) {
        final var result = new ArrayList<ParseTree>();
        for (final var node : matchedTreeNodes) {
            final var followingSiblings = getPrecedingOrSelf(node);
            result.addAll(followingSiblings);
        }
        return result;
    }


    private List<ParseTree> getFollowingOrSelf(final ParseTree node) {
        final var newMatched = new ArrayList<ParseTree>();
        final var following = getFollowing(node);
        newMatched.add(node);
        newMatched.addAll(following);
        return newMatched;
    }


    private List<ParseTree> getAllFollowingOrSelf(final List<ParseTree> matchedTreeNodes) {
        final var result = new ArrayList<ParseTree>();
        for (final var node : matchedTreeNodes) {
            final var followingSiblings = getFollowingOrSelf(node);
            result.addAll(followingSiblings);
        }
        return result;
    }

    private List<ParseTree> getAllFollowing(final List<ParseTree> nodes) {
        final var result = new ArrayList<ParseTree>();
        for (final var node : nodes) {
            final var followingSiblings = getFollowing(node);
            result.addAll(followingSiblings);
        }
        return result;
    }

    private List<ParseTree> getFollowing(final ParseTree node) {
        final List<ParseTree> ancestors = getAncestors(node);
        final List<ParseTree> ancestorFollowingSiblings = getAllFollowingSiblings(ancestors);
        final List<ParseTree> followingSiblingDescendants =  getAllDescendants(ancestorFollowingSiblings);
        final List<ParseTree> thisNodeDescendants = getDescendants(node);
        final List<ParseTree> thisNodefollowingSiblings = getFollowingSiblings(node);
        final List<ParseTree> thisNodeFollowingSiblingDescendants = getAllDescendantsOrSelf(thisNodefollowingSiblings);
        final List<ParseTree> following = new ArrayList<>(ancestorFollowingSiblings.size()
                                                    + followingSiblingDescendants.size()
                                                    + followingSiblingDescendants.size()
                                                    + thisNodeDescendants.size()
                                                    + thisNodeFollowingSiblingDescendants.size());
        following.addAll(ancestorFollowingSiblings);
        following.addAll(followingSiblingDescendants);
        following.addAll(thisNodeDescendants);
        following.addAll(thisNodeFollowingSiblingDescendants);
        return following;
    }


    private List<ParseTree> getAllPreceding(final List<ParseTree> nodes) {
        final var result = new ArrayList<ParseTree>();
        for (final var node : nodes) {
            final var precedingSiblings = getPreceding(node);
            result.addAll(precedingSiblings);
        }
        return result;
    }


    private List<ParseTree> getPreceding(final ParseTree node) {
        final List<ParseTree> ancestors = getAncestors(node);
        final List<ParseTree> ancestorPrecedingSiblings = getAllPrecedingSiblings(ancestors);
        final List<ParseTree> precedingSiblingDescendants =  getAllDescendantsOrSelf(ancestorPrecedingSiblings);
        final List<ParseTree> thisNodePrecedingSiblings = getPrecedingSiblings(node);
        final List<ParseTree> thisNodePrecedingSiblingDescendants = getAllDescendantsOrSelf(thisNodePrecedingSiblings);
        final List<ParseTree> following = new ArrayList<>(ancestors.size()
                                                    + precedingSiblingDescendants.size()
                                                    + thisNodePrecedingSiblingDescendants.size());
        following.addAll(ancestors);
        following.addAll(precedingSiblingDescendants);
        following.addAll(thisNodePrecedingSiblingDescendants);
        return following;
    }

    private List<ParseTree> getAllFollowingSiblings(final List<ParseTree> nodes) {
        final var result = new ArrayList<ParseTree>();
        for (final var node : nodes) {
            final var followingSiblings = getFollowingSiblings(node);
            result.addAll(followingSiblings);
        }
        return result;
    }

    private List<ParseTree> getFollowingSiblings(final ParseTree node) {
        final var parent = node.getParent();
        if (parent == null)
            return List.of();
        final var parentsChildren = getChildren(parent);
        final var nodeIndex = parentsChildren.indexOf(node);
        final var followingSibling = parentsChildren.subList(nodeIndex+1, parentsChildren.size());
        return followingSibling;
    }



    private List<ParseTree> getAllPrecedingSiblings(final List<ParseTree> nodes) {
        final var result = new ArrayList<ParseTree>();
        for (final var node : nodes) {
            final var precedingSiblings = getPrecedingSiblings(node);
            result.addAll(precedingSiblings);
        }
        return result;
    }


    private List<ParseTree> getPrecedingSiblings(final ParseTree node) {
        final var parent = node.getParent();
        if (parent == null)
            return List.of();
        final var parentsChildren = getChildren(parent);
        final var nodeIndex = parentsChildren.indexOf(node);
        final var precedingSibling = parentsChildren.subList(0, nodeIndex);
        return precedingSibling;
    }


    private List<ParseTree> getAllDescendantsOrSelf(final List<ParseTree> nodes) {
        final var newMatched = new ArrayList<ParseTree>();
        for (final var node :nodes) {
            final var descendants = getDescendantsOrSelf(node);
            newMatched.addAll(descendants);
        }
        return newMatched;
    }


    private List<ParseTree> getDescendantsOrSelf(final ParseTree node) {
        final var newMatched = new ArrayList<ParseTree>();
        final var descendants = getDescendants(node);
        newMatched.add(node);
        newMatched.addAll(descendants);

        return newMatched;
    }

    private List<ParseTree> getAllDescendants(final List<ParseTree> nodes) {
        final var allDescendants = new ArrayList<ParseTree>();
        for (final var node : nodes) {
            final var descendants = getDescendants(node);
            allDescendants.addAll(descendants);
        }
        return allDescendants;
    }


    private List<ParseTree> getDescendants(final ParseTree treenode) {
        final List<ParseTree> allDescendants = new ArrayList<>();
        final List<ParseTree> children = getChildren(treenode);
        while (children.size() != 0) {
            final var child = children.removeFirst();
            allDescendants.add(child);
            final var descendants = getChildren(child);
            for (final ParseTree descendantTree : descendants.reversed()) {
                children.addFirst(descendantTree);
            }
        }
        return allDescendants;
    }


    private List<ParseTree> getChildren(final ParseTree treenode) {
        final List<ParseTree> children = IntStream.range(0, treenode.getChildCount())
            .mapToObj(i->treenode.getChild(i))
            .collect(Collectors.toList());
        return children;
    }


    private List<ParseTree> getAllChildren(final List<ParseTree> nodes) {
        final var newMatched = new ArrayList<ParseTree>();
        for (final var node : nodes) {
            final var children = getChildren(node);
            newMatched.addAll(children);
        }
        return newMatched;
    }


    private List<ParseTree> getAllAncestors(final List<ParseTree> nodes) {
        final var newMatched = new ArrayList<ParseTree>();
        for (final var valueNode : nodes) {
            final var ancestors = getAncestors(valueNode);
            newMatched.addAll(ancestors);
        }
        return newMatched.reversed();
    }

    private List<ParseTree> getAncestors(final ParseTree node) {
        final List<ParseTree> newMatched = new ArrayList<ParseTree>();
        ParseTree parent = node.getParent();
        while (parent != null) {
            newMatched.add(parent);
            parent = parent.getParent();
        }
        return newMatched.reversed();
    }

    private List<ParseTree> getAllParents(final List<ParseTree> nodes) {
        final List<ParseTree> newMatched = nodes.stream()
            .map(ParseTree::getParent)
            .toList();
        return newMatched;
    }

    private List<ParseTree> getAllAncestorsOrSelf(final List<ParseTree> nodes) {
        // TODO: Correct sequence
        final var newMatched = new ArrayList<ParseTree>();
        final var ancestorPart = getAllAncestors(nodes);
        newMatched.addAll(ancestorPart);
        newMatched.addAll(nodes);
        return newMatched;
    }

    @Override
    public XQueryValue visitForwardAxis(final ForwardAxisContext ctx) {
        if (ctx.CHILD() != null) currentAxis = XQueryAxis.CHILD;
        if (ctx.DESCENDANT() != null) currentAxis = XQueryAxis.DESCENDANT;
        if (ctx.SELF() != null) currentAxis = XQueryAxis.SELF;
        if (ctx.DESCENDANT_OR_SELF() != null) currentAxis = XQueryAxis.DESCENDANT_OR_SELF;
        if (ctx.FOLLOWING_SIBLING() != null) currentAxis = XQueryAxis.FOLLOWING_SIBLING;
        if (ctx.FOLLOWING() != null) currentAxis = XQueryAxis.FOLLOWING;
        if (ctx.FOLLOWING_SIBLING_OR_SELF() != null) currentAxis = XQueryAxis.FOLLOWING_SIBLING_OR_SELF;
        if (ctx.FOLLOWING_OR_SELF() != null) currentAxis = XQueryAxis.FOLLOWING_OR_SELF;
        return null;
    }

    @Override
    public XQueryValue visitReverseAxis(final ReverseAxisContext ctx) {
        if (ctx.PARENT() != null) currentAxis = XQueryAxis.PARENT;
        if (ctx.ANCESTOR() != null) currentAxis = XQueryAxis.ANCESTOR;
        if (ctx.PRECEDING_SIBLING_OR_SELF() != null) currentAxis = XQueryAxis.PRECEDING_SIBLING_OR_SELF;
        if (ctx.PRECEDING_OR_SELF() != null) currentAxis = XQueryAxis.PRECEDING_OR_SELF;
        if (ctx.PRECEDING_SIBLING() != null) currentAxis = XQueryAxis.PRECEDING_SIBLING;
        if (ctx.PRECEDING() != null) currentAxis = XQueryAxis.PRECEDING;
        if (ctx.ANCESTOR_OR_SELF() != null) currentAxis = XQueryAxis.ANCESTOR_OR_SELF;
        return null;
    }

    @Override
    public XQueryValue visitStringConcatExpr(final StringConcatExprContext ctx) {
        try {
            var value = ctx.rangeExpr(0).accept(this);
            if (ctx.CONCATENATION().isEmpty())
                return value;
            final var operationCount = ctx.CONCATENATION().size();
            for (int i = 1; i <= operationCount; i++) {
                final var visitedExpression = ctx.rangeExpr(i).accept(this);
                value = value.concatenate(valueFactory, visitedExpression);
            }
            return value;
        } catch (final XQueryUnsupportedOperation e) {
            // TODO: handle exception
            return null;
        }
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
            final var visitedFunction = ctx.arrowFunctionSpecifier(i).accept(this);
            ctx.argumentList(i).accept(this); // visitedArgumentList is set to function's args
            contextArgument = visitedFunction.functionValue().call(valueFactory, context, visitedArgumentList);
            visitedArgumentList = new ArrayList<>();
            visitedArgumentList.add(contextArgument);
        }
        visitedArgumentList = savedArgs;
        return contextArgument;
    }

    @Override
    public XQueryValue visitArrowFunctionSpecifier(final ArrowFunctionSpecifierContext ctx) {
        if (ctx.ID() != null)
            return functionCaller.getFunctionReference(ctx.ID().getText(), valueFactory);
        if (ctx.varRef() != null)
            return ctx.varRef().accept(this);
        return ctx.parenthesizedExpr().accept(this);

    }

    @Override
    public XQueryValue visitAndExpr(final AndExprContext ctx) {
        try {
            var value = ctx.comparisonExpr(0).accept(this);
            if (ctx.AND().isEmpty())
                return value;
            if (!value.isBooleanValue()) {
                // TODO: type error
            }
            // Short circuit
            if (!value.booleanValue()) {
                return XQueryBoolean.FALSE;
            }
            final var orCount = ctx.AND().size();
            for (int i = 1; i <= orCount; i++) {
                final var visitedExpression = ctx.comparisonExpr(i).accept(this);
                value = value.and(valueFactory, visitedExpression);
                // Short circuit
                if (!value.booleanValue()) {
                    return XQueryBoolean.FALSE;
                }
            }
            return value;
        } catch (final XQueryUnsupportedOperation e) {
            //TODO: add proper error support
            return null;
        }
    }

    @Override
    public XQueryValue visitAdditiveExpr(final AdditiveExprContext ctx) {
        try {
            var value = ctx.multiplicativeExpr(0).accept(this);
            if (ctx.additiveOperator().isEmpty())
                return value;
            if (!value.isNumericValue()) {
                // TODO: type error
            }
            final var orCount = ctx.additiveOperator().size();
            for (int i = 1; i <= orCount; i++) {
                final var visitedExpression = ctx.multiplicativeExpr(i).accept(this);
                value = switch (ctx.additiveOperator(i-1).getText()) {
                    case "+" -> value.add(valueFactory, visitedExpression);
                    case "-" -> value.subtract(valueFactory, visitedExpression);
                    default -> null;
                };
            }
            return value;
        } catch (final XQueryUnsupportedOperation e) {
            //TODO: add proper error support
            return null;
        }
    }

    @Override
    public XQueryValue visitComparisonExpr(final ComparisonExprContext ctx) {
        try {
            if (ctx.generalComp() != null)
                return handleGeneralComparison(ctx);
            if (ctx.valueComp() != null)
                return handleValueComparison(ctx);
            if (ctx.nodeComp() != null)
                return handleNodeComp(ctx);
            return ctx.otherwiseExpr(0).accept(this);
        } catch (final XQueryUnsupportedOperation e) {
            //TODO: add proper error support
            return null;
        }
    }

    private XQueryValue handleGeneralComparison(final ComparisonExprContext ctx) throws XQueryUnsupportedOperation {
        final var value = ctx.otherwiseExpr(0).accept(this);
        final var visitedExpression = ctx.otherwiseExpr(1).accept(this);
        return switch(ctx.generalComp().getText()) {
            case "=" -> value.generalEqual(valueFactory, visitedExpression);
            case "!=" -> value.generalUnequal(valueFactory, visitedExpression);
            case ">" -> value.generalGreaterThan(valueFactory, visitedExpression);
            case "<" -> value.generalLessThan(valueFactory, visitedExpression);
            case "<=" -> value.generalLessEqual(valueFactory, visitedExpression);
            case ">=" -> value.generalGreaterEqual(valueFactory, visitedExpression);
            default -> null;
        };
    }

    private XQueryValue handleValueComparison(final ComparisonExprContext ctx) throws XQueryUnsupportedOperation {
        final var value = ctx.otherwiseExpr(0).accept(this);
        final var visitedExpression = ctx.otherwiseExpr(1).accept(this);
        if (value.isSequence() && value.empty(valueFactory).booleanValue()) {
            return valueFactory.emptySequence();
        }
        if (visitedExpression.isSequence() && visitedExpression.empty(valueFactory).booleanValue()) {
            return valueFactory.emptySequence();
        }
        return switch(ctx.valueComp().getText()) {
            case "eq" -> value.valueEqual(valueFactory, visitedExpression);
            case "ne" -> value.valueUnequal(valueFactory, visitedExpression);
            case "lt" -> value.valueLessThan(valueFactory, visitedExpression);
            case "gt" -> value.valueGreaterThan(valueFactory, visitedExpression);
            case "le" -> value.valueLessEqual(valueFactory, visitedExpression);
            case "ge" -> value.valueGreaterEqual(valueFactory, visitedExpression);
            default -> null;
        };
    }


    @Override
    public XQueryValue visitOtherwiseExpr(OtherwiseExprContext ctx) {
        if (ctx.OTHERWISE().isEmpty())
            return ctx.stringConcatExpr(0).accept(this);
        final int length = ctx.stringConcatExpr().size();
        for (int i = 0; i < length-1; i++) {
            var expr = ctx.stringConcatExpr(i);
            XQueryValue exprValue = expr.accept(this);
            if (exprValue.isSequence() && exprValue.sequence().isEmpty())
                continue;
            return exprValue;
        }
        return ctx.stringConcatExpr(length-1).accept(this);
    }


    @Override
    public XQueryValue visitMultiplicativeExpr(final MultiplicativeExprContext ctx) {
        try {
            var value = ctx.unionExpr(0).accept(this);
            if (ctx.multiplicativeOperator().isEmpty())
                return value;
            final var orCount = ctx.multiplicativeOperator().size();
            for (int i = 1; i <= orCount; i++) {
                final var visitedExpression = ctx.unionExpr(i).accept(this);
                value = switch (ctx.multiplicativeOperator(i-1).getText()) {
                    case "*" -> value.multiply(valueFactory, visitedExpression);
                    case "x" -> value.multiply(valueFactory, visitedExpression);
                    case "div" -> value.divide(valueFactory, visitedExpression);
                    case "÷" -> value.divide(valueFactory, visitedExpression);
                    case "idiv" -> value.integerDivide(valueFactory, visitedExpression);
                    case "mod" -> value.modulus(valueFactory, visitedExpression);
                    default -> null;
                };
            }
            return value;
        } catch (final XQueryUnsupportedOperation e) {
            // TODO: handle exception
            return null;
        }
    }


    @Override
    public XQueryValue visitUnionExpr(final UnionExprContext ctx) {
        try {
            var value = ctx.intersectExpr(0).accept(this);
            if (ctx.unionOperator().isEmpty())
                return value;
            if (!value.isSequence()) {
                // TODO: type error
            }
            final var unionCount = ctx.unionOperator().size();
            for (int i = 1; i <= unionCount; i++) {
                final var visitedExpression = ctx.intersectExpr(i).accept(this);
                value = value.union(valueFactory, visitedExpression);
            }
            return value;
        } catch (final XQueryUnsupportedOperation e) {
            // TODO: handle exception
            return null;
        }

    }

    @Override
    public XQueryValue visitIntersectExpr(final IntersectExprContext ctx) {
        try {
            var value = ctx.instanceofExpr(0).accept(this);
            if (ctx.exceptOrIntersect().isEmpty())
                return value;
            if (!value.isSequence()) {
                // TODO: type error
                return null;
            }
            final var operatorCount = ctx.exceptOrIntersect().size();
            for (int i = 1; i <= operatorCount; i++) {
                final var visitedExpression = ctx.instanceofExpr(i).accept(this);
                final boolean isExcept = ctx.exceptOrIntersect(i-1).EXCEPT() != null;
                if (isExcept) {
                    value = value.except(valueFactory, visitedExpression);
                } else {
                    value = value.intersect(valueFactory, visitedExpression);
                }
            }
            return value;
        } catch (final XQueryUnsupportedOperation e) {
            // TODO: handle exception
            return null;
        }
    }

    @Override
    public XQueryValue visitInstanceofExpr(final InstanceofExprContext ctx) {
        // TODO: handle
        return ctx.treatExpr(0).accept(this);
    }

    @Override
    public XQueryValue visitTreatExpr(final TreatExprContext ctx) {
        // TODO: handle
        return ctx.castableExpr(0).accept(this);
    }

    @Override
    public XQueryValue visitCastableExpr(final CastableExprContext ctx) {
        // TODO: handle
        return ctx.arrowExpr().accept(this);
    }



    @Override
    public XQueryValue visitUnaryExpr(final UnaryExprContext ctx) {
        try {
            final var value = ctx.simpleMapExpr().accept(this);
            if (ctx.MINUS() == null)
                return value;
            if (!value.isNumericValue()) {
                // TODO: type error
            }
            return value.multiply(valueFactory, valueFactory.number(new BigDecimal(-1)));
        } catch (final XQueryUnsupportedOperation e) {
            // TODO: handle exception
            return null;
        }
    }

    @Override
    public XQueryValue visitSwitchExpr(final SwitchExprContext ctx) {
        final Map<XQueryValue, ParseTree> valueToExpression = ctx.switchCaseClause().stream()
                .flatMap(clause -> clause.switchCaseOperand()
                                            .stream().map(operand -> Map.entry(operand.accept(this), clause.exprSingle())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        final XQueryValue switchedValue = ctx.switchedExpr.accept(this);
        final ParseTree toBeExecuted = valueToExpression.getOrDefault(switchedValue, ctx.defaultExpr);
        return toBeExecuted.accept(this);
    }


    @Override
    public XQueryValue visitArgument(final ArgumentContext ctx) {
        final var value =  super.visitArgument(ctx);
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

    private Stream<List<TupleElement>> saveVisitedTupleStream() {
        final Stream<List<TupleElement>> saved = visitedTupleStream;
        visitedTupleStream = Stream.of(List.of());
        return  saved;
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




    private Comparator<List<TupleElement>> ascendingEmptyGreatest(final ParseTree expr) {
        return (tuple1, tuple2) -> {
            provideVariables(tuple1);
            final XQueryValue value1 = expr.accept(this);
            provideVariables(tuple2);
            final XQueryValue value2 = expr.accept(this);
            final boolean value1IsEmptySequence = value1.isSequence() && value1.sequence().isEmpty();
            final boolean value2IsEmptySequence = value2.isSequence() && value2.sequence().isEmpty();
            if (value1IsEmptySequence && !value2IsEmptySequence) {
                //  empty greatest
                return 1;
            }
            return compareValues(value1, value2);
        };
    };

    private Comparator<List<TupleElement>> ascendingEmptyLeast(final ParseTree expr) {
        return (tuple1, tuple2) -> {
            provideVariables(tuple1);
            final XQueryValue value1 = expr.accept(this);
            provideVariables(tuple2);
            final XQueryValue value2 = expr.accept(this);
            final boolean value1IsEmptySequence = value1.isSequence() && value1.sequence().isEmpty();
            final boolean value2IsEmptySequence = value2.isSequence() && value2.sequence().isEmpty();
            if (value1IsEmptySequence && !value2IsEmptySequence) {
                //  empty greatest
                return -1;
            }
            return compareValues(value1, value2);
        };
    };

    private Comparator<List<TupleElement>> descendingEmptyGreatest(final ParseTree expr) {
        return (tuple1, tuple2) -> {
            provideVariables(tuple1);
            final XQueryValue value1 = expr.accept(this);
            provideVariables(tuple2);
            final XQueryValue value2 = expr.accept(this);
            final boolean value1IsEmptySequence = value1.isSequence() && value1.sequence().isEmpty();
            final boolean value2IsEmptySequence = value2.isSequence() && value2.sequence().isEmpty();
            if (value1IsEmptySequence && !value2IsEmptySequence) {
                //  empty greatest
                return -1;
            }
            return -compareValues(value1, value2);
        };
    };

    private Comparator<List<TupleElement>> descendingEmptyLeast(final ParseTree expr) {
        return (tuple1, tuple2) -> {
            provideVariables(tuple1);
            final XQueryValue value1 = expr.accept(this);
            provideVariables(tuple2);
            final XQueryValue value2 = expr.accept(this);
            final boolean value1IsEmptySequence = value1.isSequence() && value1.sequence().isEmpty();
            final boolean value2IsEmptySequence = value2.isSequence() && value2.sequence().isEmpty();
            if (value1IsEmptySequence && !value2IsEmptySequence) {
                //  empty greatest
                return -1;
            }
            return -compareValues(value1, value2);
        };
    };


    private Comparator<List<TupleElement>> comparatorFromNthOrderSpec(final List<OrderSpecContext> orderSpecs, final int[] modifierMaskArray, final int i) {
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
    public XQueryValue visitWhileClause(final WhileClauseContext ctx) {
        final var filteringExpression = ctx.exprSingle();
        visitedTupleStream = visitedTupleStream.takeWhile(_ -> {
            final XQueryValue filter = filteringExpression.accept(this);
            return filter.effectiveBooleanValue();
        });
        return null;
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
    public XQueryValue visitOrderByClause(final OrderByClauseContext ctx) {
        final int sortingExprCount = ctx.orderSpecList().orderSpec().size();
        final var orderSpecs = ctx.orderSpecList().orderSpec();
        final int[] modifierMaskArray = orderSpecs.stream()
            .map(OrderSpecContext::orderModifier)
            .mapToInt(m->{
                final int isDescending = m.DESCENDING() != null? 1 : 0;
                final int isEmptyLeast = m.LEAST() != null? 1 : 0;
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
        }).map(tuple->{
            provideVariables(tuple);
            return tuple;
        });
        return null;
    }

    private int compareValues(final XQueryValue value1, final XQueryValue value2) {
        if (value1.valueEqual(valueFactory, value2).booleanValue()) {
            return 0;
        } else {
            if (value1.valueLessThan(valueFactory, value2).booleanValue()) {
                return -1;
            }
            ;
            return 1;
        }
    }

    private void provideVariables(final List<TupleElement> tuple) {
        for (final var e : tuple) {
            contextManager.provideVariable(e.name, e.value);
            if (e.positionalName != null)
                contextManager.provideVariable(e.positionalName, e.index);
        }
    }
}
