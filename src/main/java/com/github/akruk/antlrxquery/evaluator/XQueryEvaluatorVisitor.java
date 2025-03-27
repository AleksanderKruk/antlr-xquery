package com.github.akruk.antlrxquery.evaluator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import com.github.akruk.antlrxquery.AntlrXqueryParser.AnyIdContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ArgumentContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ArrowFunctionSpecifierContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.AxisStepContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ContextItemExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.CountClauseContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.FLWORExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ForBindingContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ForClauseContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ForwardAxisContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ForwardStepContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.FunctionCallContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.LetBindingContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.LetClauseContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.LiteralContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.NameTestContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.NodeTestContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.OrExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ParenthesizedExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.PathExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.PositionalVarContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.PostfixContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.PostfixExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.PredicateContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.QuantifiedExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.RelativePathExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ReturnClauseContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ReverseAxisContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ReverseStepContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.StepExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.VarNameContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.VarRefContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.WhereClauseContext;
import com.github.akruk.antlrxquery.evaluator.contextmanagement.XQueryContextManager;
import com.github.akruk.antlrxquery.evaluator.contextmanagement.baseimplementations.XQueryBaseContextManager;
import com.github.akruk.antlrxquery.evaluator.functioncaller.XQueryFunctionCaller;
import com.github.akruk.antlrxquery.evaluator.functioncaller.defaults.BaseFunctionCaller;
import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;
import com.github.akruk.antlrxquery.values.XQuerySequence;
import com.github.akruk.antlrxquery.values.XQueryValue;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;
import com.github.akruk.antlrxquery.values.factories.defaults.XQueryBaseValueFactory;
import com.github.akruk.antlrxquery.values.XQueryBoolean;
import com.github.akruk.antlrxquery.values.XQueryNumber;

class XQueryEvaluatorVisitor extends AntlrXqueryParserBaseVisitor<XQueryValue> {
    XQueryValue root;
    Parser parser;
    List<XQueryValue> visitedArgumentList;
    XQueryValue matchedNodes;
    XQueryAxis currentAxis;
    XQueryVisitingContext context;
    XQueryContextManager contextManager;
    XQueryValueFactory valueFactory;
    XQueryFunctionCaller functionCaller;
    Stream<List<TupleElement>> visitedTupleStream;

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
    }



    public XQueryEvaluatorVisitor(final ParseTree tree, final Parser parser) {
        this(tree, parser, new XQueryBaseContextManager(), new XQueryBaseValueFactory(), new BaseFunctionCaller());
    }

    public XQueryEvaluatorVisitor(
            final ParseTree tree,
            final Parser parser,
            final XQueryContextManager contextManager,
            final XQueryValueFactory valueFactory,
            final XQueryFunctionCaller functionCaller) {
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
    public XQueryValue visitFLWORExpr(FLWORExprContext ctx) {
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
    public XQueryValue visitLetClause(LetClauseContext ctx) {
        final int newVariableCount = ctx.letBinding().size();
        visitedTupleStream = visitedTupleStream.map(tuple -> {
            var newTuple = new ArrayList<TupleElement>(tuple.size() + newVariableCount);
            newTuple.addAll(tuple);
            for (LetBindingContext streamVariable : ctx.letBinding()) {
                String variableName = streamVariable.varName().getText();
                XQueryValue assignedValue = streamVariable.exprSingle().accept(this);
                var element = new TupleElement(variableName, assignedValue, null, null);
                newTuple.add(element);
                contextManager.provideVariable(variableName, assignedValue);
            }
            return newTuple;
        });
        return null;
    }



    @Override
    public XQueryValue visitForClause(ForClauseContext ctx) {
        final int numberOfVariables = (int) ctx.forBinding().size();
        visitedTupleStream = visitedTupleStream.flatMap(tuple -> {
            List<List<TupleElement>> newTupleLike = tuple.stream().map(e -> List.of(e)).collect(Collectors.toList());
            for (ForBindingContext streamVariable : ctx.forBinding()) {
                String variableName = streamVariable.varName().getText();
                List<XQueryValue> sequence = streamVariable.exprSingle().accept(this).sequence();
                PositionalVarContext positional = streamVariable.positionalVar();
                int sequenceSize = sequence.size();
                if (positional != null) {
                    List<TupleElement> elementsWithIndex = new ArrayList<>(numberOfVariables);
                    String positionalName = positional.varName().getText();
                    for (int i = 0; i < sequenceSize; i++) {
                        var value = sequence.get(i);
                        var element = new TupleElement(variableName, value, positionalName, valueFactory.number(i + 1));
                        elementsWithIndex.add(element);
                    }
                    newTupleLike.add(elementsWithIndex);
                } else {
                    List<TupleElement> elementsWithoutIndex = sequence.stream()
                            .map(value -> new TupleElement(variableName, value, null, null))
                            .toList();
                    newTupleLike.add(elementsWithoutIndex);
                }
            }
            return cartesianProduct(newTupleLike);
        }).map(tuple -> {
            // the newly declared variables need to be provided to the context
            List<TupleElement> addedVariables = tuple.subList(tuple.size() - numberOfVariables, tuple.size());
            for (TupleElement element : addedVariables) {
                contextManager.provideVariable(element.name, element.value);
                if (element.positionalName != null)
                    contextManager.provideVariable(element.positionalName, element.index);
            }
            return tuple;
        });
        return null;
    }

    private class MutableInt {
        public int i = 0;
    }
    @Override
    public XQueryValue visitCountClause(CountClauseContext ctx) {
        final String countVariableName = ctx.varName().getText();
        final MutableInt index = new MutableInt();
        index.i = 1;
        visitedTupleStream = visitedTupleStream.map(tuple -> {
            var newTuple = new ArrayList<TupleElement>(tuple.size() + 1);
            newTuple.addAll(tuple);
            var element = new TupleElement(countVariableName, valueFactory.number(index.i++), null, null);
            contextManager.provideVariable(element.name, element.value);
            newTuple.add(element);
            return newTuple;
        });
        return null;
    }

    @Override
    public XQueryValue visitWhereClause(WhereClauseContext ctx) {
        final var filteringExpression = ctx.exprSingle();
        visitedTupleStream = visitedTupleStream.filter(tuple -> {
            XQueryValue filter = filteringExpression.accept(this);
            return filter.effectiveBooleanValue();
        });
        return null;
    }

    @Override
    public XQueryValue visitVarRef(VarRefContext ctx) {
        String variableName = ctx.varName().getText();
        XQueryValue variableValue = contextManager.getVariable(variableName);
        return variableValue;
    }

    @Override
    public XQueryValue visitReturnClause(ReturnClauseContext ctx) {
        List<XQueryValue> results = visitedTupleStream.map((tuple) -> {
            XQueryValue value = ctx.exprSingle().accept(this);
            return value;
        }).toList();
        if (results.size() == 1) {
            var value = results.get(0);
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


    public static <T> Stream<List<T>> cartesianProduct(List<List<T>> lists) {
        if (lists.isEmpty()) {
            return Stream.of(List.of());
        }

        final int size = lists.size();
        return lists.get(0).stream()
                .flatMap(firstElement -> cartesianProduct(lists.subList(1, size))
                        .map(rest -> {
                            List<T> combination = new ArrayList<>(size);
                            combination.add(firstElement);
                            combination.addAll(rest);
                            return combination;
                        }));
    }


    @Override
    public XQueryValue visitQuantifiedExpr(QuantifiedExprContext ctx) {
        List<String> variableNames = ctx.varName().stream().map(VarNameContext::anyId)
                                                        .map(AnyIdContext::getText)
                                                        .toList();
        int variableExpressionCount = ctx.exprSingle().size()-1;
        List<List<XQueryValue>> sequences = new ArrayList<>(variableExpressionCount);
        for (var expr : ctx.exprSingle().subList(0, variableExpressionCount)) {
            var sequenceValue = expr.accept(this);
            sequences.add(sequenceValue.sequence());
        }

        final var criterionNode = ctx.exprSingle().getLast();
        if (ctx.EVERY() != null) {
            boolean every = cartesianProduct(sequences).allMatch(variableProduct -> {
                for (int i = 0; i < variableNames.size(); i++) {
                    contextManager.provideVariable(variableNames.get(i), variableProduct.get(i));
                }
                return criterionNode.accept(this).booleanValue();
            });
            return XQueryBoolean.of(every);
        }
        if (ctx.SOME() != null) {
            boolean some = cartesianProduct(sequences).anyMatch(variableProduct -> {
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
            XQueryValue value = null;
            if (ctx.orExpr().size() == 0) {
                value = ctx.pathExpr(0).accept(this);
            }
            if (!ctx.OR().isEmpty())
                return handleOrExpr(ctx);
            if (!ctx.AND().isEmpty())
                return handleAndExpr(ctx);
            if (ctx.TO() != null)
                return handleRangeExpr(ctx);
            if (!ctx.additiveOperator().isEmpty())
                return handleAdditiveExpr(ctx);
            if (!ctx.multiplicativeOperator().isEmpty())
                return handleMultiplicativeExpr(ctx);
            if (!ctx.unionOperator().isEmpty())
                return handleUnionExpr(ctx);
            if (!ctx.INTERSECT().isEmpty())
                return handleIntersectionExpr(ctx);
            if (!ctx.EXCEPT().isEmpty())
                return handleSequenceSubtractionExpr(ctx);
            if (ctx.MINUS() != null)
                return handleUnaryArithmeticExpr(ctx);
            if (ctx.generalComp() != null)
                return handleGeneralComparison(ctx);
            if (ctx.valueComp() != null)
                return handleValueComparison(ctx);
            if (!ctx.CONCATENATION().isEmpty())
                return handleConcatenation(ctx);
            if (!ctx.ARROW().isEmpty())
                return handleArrowExpr(ctx);
            if (ctx.nodeComp() != null)
                return handleNodeComp(ctx);
            return value;
        } catch (final XQueryUnsupportedOperation e) {
            // TODO: error handling
            return null;
        }
    }




    private XQueryValue handleNodeComp(OrExprContext ctx) {
        try {
            final var visitedLeft = ctx.orExpr(0).accept(this);
            ParseTree nodeLeft = getSingleNode(visitedLeft);
            final var visitedRight = ctx.orExpr(1).accept(this);
            ParseTree nodeRight = getSingleNode(visitedRight);
            boolean result = switch (ctx.nodeComp().getText()) {
                case "is" -> nodeLeft == nodeRight;
                case "<<" -> getFollowing(nodeLeft).contains(nodeRight);
                case ">>" -> getPreceding(nodeLeft).contains(nodeRight);
                default -> false;
            };
            return valueFactory.bool(result);
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }

    }

    private ParseTree getSingleNode(final XQueryValue visitedLeft) throws XQueryUnsupportedOperation {
        ParseTree nodeLeft;
        if (visitedLeft.isAtomic()) {
            nodeLeft = visitedLeft.node();
        } else {
            List<XQueryValue> sequenceLeft = visitedLeft.exactlyOne(valueFactory).sequence();
            nodeLeft = sequenceLeft.get(0).node();
        }
        return nodeLeft;
    }


    private XQueryValue handleRangeExpr(OrExprContext ctx) {
        var fromValue = ctx.orExpr(0).accept(this);
        var toValue = ctx.orExpr(1).accept(this);
        int fromInt = fromValue.numericValue().intValue();
        int toInt = toValue.numericValue().intValue();
        if (fromInt > toInt)
            return XQuerySequence.EMPTY;
        List<XQueryValue> values = IntStream.rangeClosed(fromInt, toInt)
            .mapToObj(i->valueFactory.number(i))
            .collect(Collectors.toList());
        return valueFactory.sequence(values);
    }

    @Override
    public XQueryValue visitPathExpr(PathExprContext ctx) {
        boolean pathExpressionFromRoot = ctx.SLASH() != null;
        if (pathExpressionFromRoot) {
            final var savedNodes = saveMatchedModes();
            final var savedAxis = saveAxis();
            // TODO: Context nodes
            matchedNodes = nodeSequence(List.of(root.node()));
            currentAxis = XQueryAxis.CHILD;
            var resultingNodeSequence = ctx.relativePathExpr().accept(this);
            matchedNodes = savedNodes;
            currentAxis = savedAxis;
            return resultingNodeSequence;
        }
        boolean useDescendantOrSelfAxis = ctx.SLASHES() != null;
        if (useDescendantOrSelfAxis) {
            final var savedNodes = saveMatchedModes();
            final var savedAxis = saveAxis();
            matchedNodes = nodeSequence(List.of(root.node()));
            currentAxis = XQueryAxis.DESCENDANT_OR_SELF;
            var resultingNodeSequence = ctx.relativePathExpr().accept(this);
            matchedNodes = savedNodes;
            currentAxis = savedAxis;
            return resultingNodeSequence;
        }
        return ctx.relativePathExpr().accept(this);
    }

    @Override
    public XQueryValue visitRelativePathExpr(RelativePathExprContext ctx) {
        if (ctx.pathOperator().isEmpty()) {
            return ctx.stepExpr(0).accept(this);
        }
        XQueryValue visitedNodeSequence = ctx.stepExpr(0).accept(this);
        matchedNodes = visitedNodeSequence;
        var operationCount = ctx.pathOperator().size();
        for (int i = 1; i <= operationCount; i++) {
            matchedNodes = switch (ctx.pathOperator(i-1).getText()) {
                case "//" -> {
                    List<ParseTree> descendantsOrSelf = getAllDescendantsOrSelf(matchedTreeNodes());
                    matchedNodes = nodeSequence(descendantsOrSelf);
                    yield ctx.stepExpr(i).accept(this);
                }
                case "/" -> ctx.stepExpr(i).accept(this);
                default -> null;
            };
            i++;
        }
        return matchedNodes;
    }

    private XQueryValue nodeSequence(List<ParseTree> treenodes) {
        List<XQueryValue> nodeSequence = treenodes.stream()
            .map(valueFactory::node)
            .collect(Collectors.toList());
        return valueFactory.sequence(nodeSequence);
    }

    private List<ParseTree> matchedTreeNodes() {
        return matchedNodes.sequence().stream().map(XQueryValue::node).toList();
    }

    @Override
    public XQueryValue visitStepExpr(StepExprContext ctx) {
        if (ctx.postfixExpr() != null)
            return ctx.postfixExpr().accept(this);
        return ctx.axisStep().accept(this);
    }


    @Override
    public XQueryValue visitAxisStep(AxisStepContext ctx) {
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
        for (var predicate : ctx.predicateList().predicate()) {
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
    public XQueryValue visitPostfixExpr(PostfixExprContext ctx) {
        if (ctx.postfix().isEmpty()) {
            return ctx.primaryExpr().accept(this);
        }

        final var savedContext = saveContext();
        final var savedArgs = saveVisitedArguments();
        var value = ctx.primaryExpr().accept(this);
        int index = 1;
        context.setSize(ctx.postfix().size());
        for (var postfix : ctx.postfix()) {
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
    public XQueryValue visitPostfix(PostfixContext ctx) {
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

    @Override
    public XQueryValue visitPredicate(PredicateContext ctx) {
        final var contextValue = context.getItem();
        if (contextValue.isAtomic()) {
            // TODO: error
            return null;
        }
        final var sequence = contextValue.sequence();
        final var filteredValues = new ArrayList<XQueryValue>(sequence.size());
        final var savedContext = saveContext();
        int index = 1;
        context.setSize(sequence.size());
        for (final var contextItem : sequence) {
            context.setItem(contextItem);
            context.setPosition(index);
            final var visitedExpression = ctx.expr().accept(this);
            if (visitedExpression.isNumericValue()) {
                context = savedContext;
                int i = visitedExpression.numericValue().intValue() - 1;
                if (i >= sequence.size() || i < 0) {
                    return XQuerySequence.EMPTY;
                }
                return sequence.get(i);
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
    public XQueryValue visitContextItemExpr(ContextItemExprContext ctx) {
        return context.getItem();
    }

    @Override
    public XQueryValue visitForwardStep(ForwardStepContext ctx) {
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
    public XQueryValue visitReverseStep(ReverseStepContext ctx) {
        if (ctx.abbrevReverseStep() != null) {
            return ctx.abbrevReverseStep().accept(this);
        }
        ctx.reverseAxis().accept(this);
        return ctx.nodeTest().accept(this);
    }

    @Override
    public XQueryValue visitAbbrevReverseStep(AbbrevReverseStepContext ctx) {
        var matchedParents = getAllParents(matchedTreeNodes());
        return nodeSequence(matchedParents);
    }

    @Override
    public XQueryValue visitNodeTest(NodeTestContext ctx) {
        return ctx.nameTest().accept(this);
    }

    private Predicate<String> canBeTokenName = Pattern.compile("^[\\p{IsUppercase}].*").asPredicate();
    @Override
    public XQueryValue visitNameTest(NameTestContext ctx) {
        var matchedTreeNodes = matchedTreeNodes();
        List<ParseTree> stepNodes = switch (currentAxis) {
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
        String name = ctx.ID().toString();
        if (canBeTokenName.test(name)) {
            // test for token type
            int tokenType = parser.getTokenType(name);
            // TODO: error values
            if (tokenType == Token.INVALID_TYPE)
                return null;
            for (ParseTree node : stepNodes) {
                // We skip nodes that are not terminal
                // i.e. are not tokens
                if (!(node instanceof TerminalNode))
                    continue;
                TerminalNode tokenNode = (TerminalNode) node;
                Token token = tokenNode.getSymbol();
                if (token.getType() == tokenType) {
                    matchedTreeNodes.add(tokenNode);
                }
            }
        }
        else { // test for rule
            int ruleIndex = parser.getRuleIndex(name);
            // TODO: error values
            if (ruleIndex == -1) return null;
            for (ParseTree node : stepNodes) {
                // Token nodes are being skipped
                if (!(node instanceof ParserRuleContext))
                    continue;
                ParserRuleContext testedRule = (ParserRuleContext) node;
                if (testedRule.getRuleIndex() == ruleIndex) {
                    matchedTreeNodes.add(testedRule);
                }
            }
        }
        return nodeSequence(matchedTreeNodes);
    }

    private List<ParseTree> getAllFollowing(List<ParseTree> nodes) {
        var result = new ArrayList<ParseTree>();
        for (var node : nodes) {
            var followingSiblings = getFollowing(node);
            result.addAll(followingSiblings);
        }
        return result;
    }

    private List<ParseTree> getFollowing(ParseTree node) {
        List<ParseTree> ancestors = getAncestors(node);
        List<ParseTree> ancestorFollowingSiblings = getAllFollowingSiblings(ancestors);
        List<ParseTree> followingSiblingDescendants =  getAllDescendants(ancestorFollowingSiblings);
        List<ParseTree> thisNodeDescendants = getDescendants(node);
        List<ParseTree> thisNodefollowingSiblings = getFollowingSiblings(node);
        List<ParseTree> thisNodeFollowingSiblingDescendants = getAllDescendantsOrSelf(thisNodefollowingSiblings);
        List<ParseTree> following = new ArrayList<>(ancestorFollowingSiblings.size()
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


    private List<ParseTree> getAllPreceding(List<ParseTree> nodes) {
        var result = new ArrayList<ParseTree>();
        for (var node : nodes) {
            var precedingSiblings = getPreceding(node);
            result.addAll(precedingSiblings);
        }
        return result;
    }


    private List<ParseTree> getPreceding(ParseTree node) {
        List<ParseTree> ancestors = getAncestors(node);
        List<ParseTree> ancestorPrecedingSiblings = getAllPrecedingSiblings(ancestors);
        List<ParseTree> precedingSiblingDescendants =  getAllDescendantsOrSelf(ancestorPrecedingSiblings);
        List<ParseTree> thisNodePrecedingSiblings = getPrecedingSiblings(node);
        List<ParseTree> thisNodePrecedingSiblingDescendants = getAllDescendantsOrSelf(thisNodePrecedingSiblings);
        List<ParseTree> following = new ArrayList<>(ancestors.size()
                                                    + precedingSiblingDescendants.size()
                                                    + thisNodePrecedingSiblingDescendants.size());
        following.addAll(ancestors);
        following.addAll(precedingSiblingDescendants);
        following.addAll(thisNodePrecedingSiblingDescendants);
        return following;
    }

    private List<ParseTree> getAllFollowingSiblings(List<ParseTree> nodes) {
        var result = new ArrayList<ParseTree>();
        for (var node : nodes) {
            var followingSiblings = getFollowingSiblings(node);
            result.addAll(followingSiblings);
        }
        return result;
    }

    private List<ParseTree> getFollowingSiblings(ParseTree node) {
        var parent = node.getParent();
        if (parent == null)
            return List.of();
        var parentsChildren = getChildren(parent);
        var nodeIndex = parentsChildren.indexOf(node);
        var followingSibling = parentsChildren.subList(nodeIndex+1, parentsChildren.size());
        return followingSibling;
    }



    private List<ParseTree> getAllPrecedingSiblings(List<ParseTree> nodes) {
        var result = new ArrayList<ParseTree>();
        for (var node : nodes) {
            var precedingSiblings = getPrecedingSiblings(node);
            result.addAll(precedingSiblings);
        }
        return result;
    }


    private List<ParseTree> getPrecedingSiblings(ParseTree node) {
        var parent = node.getParent();
        if (parent == null)
            return List.of();
        var parentsChildren = getChildren(parent);
        var nodeIndex = parentsChildren.indexOf(node);
        var precedingSibling = parentsChildren.subList(0, nodeIndex);
        return precedingSibling;
    }


    private List<ParseTree> getAllDescendantsOrSelf(List<ParseTree> nodes) {
        var newMatched = new ArrayList<ParseTree>();
        for (var node :nodes) {
            var descendants = getDescendantsOrSelf(node);
            newMatched.addAll(descendants);
        }
        return newMatched;
    }


    private List<ParseTree> getDescendantsOrSelf(ParseTree node) {
        var newMatched = new ArrayList<ParseTree>();
        var descendants = getDescendants(node);
        newMatched.add(node);
        newMatched.addAll(descendants);

        return newMatched;
    }

    private List<ParseTree> getAllDescendants(List<ParseTree> nodes) {
        var allDescendants = new ArrayList<ParseTree>();
        for (var node : nodes) {
            var descendants = getDescendants(node);
            allDescendants.addAll(descendants);
        }
        return allDescendants;
    }


    private List<ParseTree> getDescendants(ParseTree treenode) {
        List<ParseTree> allDescendants = new ArrayList<>();
        List<ParseTree> children = getChildren(treenode);
        while (children.size() != 0) {
            var child = children.removeFirst();
            allDescendants.add(child);
            var descendants = getChildren(child);
            for (ParseTree descendantTree : descendants.reversed()) {
                children.addFirst(descendantTree);
            }
        }
        return allDescendants;
    }


    private List<ParseTree> getChildren(ParseTree treenode) {
        List<ParseTree> children = IntStream.range(0, treenode.getChildCount())
            .mapToObj(i->treenode.getChild(i))
            .collect(Collectors.toList());
        return children;
    }


    private List<ParseTree> getAllChildren(List<ParseTree> nodes) {
        var newMatched = new ArrayList<ParseTree>();
        for (var node : nodes) {
            var children = getChildren(node);
            newMatched.addAll(children);
        }
        return newMatched;
    }


    private List<ParseTree> getAllAncestors(List<ParseTree> nodes) {
        var newMatched = new ArrayList<ParseTree>();
        for (var valueNode : nodes) {
            var ancestors = getAncestors(valueNode);
            newMatched.addAll(ancestors);
        }
        return newMatched.reversed();
    }

    private List<ParseTree> getAncestors(ParseTree node) {
        List<ParseTree> newMatched = new ArrayList<ParseTree>();
        ParseTree parent = node.getParent();
        while (parent != null) {
            newMatched.add(parent);
            parent = parent.getParent();
        }
        return newMatched.reversed();
    }

    private List<ParseTree> getAllParents(List<ParseTree> nodes) {
        List<ParseTree> newMatched = nodes.stream()
            .map(ParseTree::getParent)
            .toList();
        return newMatched;
    }

    private List<ParseTree> getAllAncestorsOrSelf(List<ParseTree> nodes) {
        // TODO: Correct sequence
        var newMatched = new ArrayList<ParseTree>();
        var ancestorPart = getAllAncestors(nodes);
        newMatched.addAll(ancestorPart);
        newMatched.addAll(nodes);
        return newMatched;
    }

    @Override
    public XQueryValue visitForwardAxis(ForwardAxisContext ctx) {
        if (ctx.CHILD() != null) currentAxis = XQueryAxis.CHILD;
        if (ctx.DESCENDANT() != null) currentAxis = XQueryAxis.DESCENDANT;
        if (ctx.SELF() != null) currentAxis = XQueryAxis.SELF;
        if (ctx.DESCENDANT_OR_SELF() != null) currentAxis = XQueryAxis.DESCENDANT_OR_SELF;
        if (ctx.FOLLOWING_SIBLING() != null) currentAxis = XQueryAxis.FOLLOWING_SIBLING;
        if (ctx.FOLLOWING() != null) currentAxis = XQueryAxis.FOLLOWING;
        return null;
    }

    @Override
    public XQueryValue visitReverseAxis(ReverseAxisContext ctx) {
        if (ctx.PARENT() != null) currentAxis = XQueryAxis.PARENT;
        if (ctx.ANCESTOR() != null) currentAxis = XQueryAxis.ANCESTOR;
        if (ctx.PRECEDING_SIBLING() != null) currentAxis = XQueryAxis.PRECEDING_SIBLING;
        if (ctx.PRECEDING() != null) currentAxis = XQueryAxis.PRECEDING;
        if (ctx.ANCESTOR_OR_SELF() != null) currentAxis = XQueryAxis.ANCESTOR_OR_SELF;
        return null;
    }

    private XQueryValue handleConcatenation(final OrExprContext ctx) throws XQueryUnsupportedOperation {
        var value = ctx.orExpr(0).accept(this);
        if (!value.isStringValue()) {
            // TODO: type error
        }
        final var operationCount = ctx.CONCATENATION().size();
        for (int i = 1; i <= operationCount; i++) {
            final var visitedExpression = ctx.orExpr(i).accept(this);
            value = value.concatenate(valueFactory, visitedExpression);
            i++;
        }

        return value;
    }


    private XQueryValue handleArrowExpr(final OrExprContext ctx)
            throws XQueryUnsupportedOperation {
        final var savedArgs = saveVisitedArguments();
        var contextArgument = ctx.orExpr(0).accept(this);
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
            i++;
        }
        visitedArgumentList = savedArgs;
        return contextArgument;
    }

    @Override
    public XQueryValue visitArrowFunctionSpecifier(ArrowFunctionSpecifierContext ctx) {
        if (ctx.ID() != null)
            return functionCaller.getFunctionReference(ctx.ID().getText(), valueFactory);
        if (ctx.varRef() != null)
            return ctx.varRef().accept(this);
        return ctx.parenthesizedExpr().accept(this);

    }



    private XQueryValue handleOrExpr(final OrExprContext ctx) throws XQueryUnsupportedOperation {
        var value = ctx.orExpr(0).accept(this);
        if (!value.isBooleanValue()) {
            // TODO: type error
        }
        // Short circuit
        if (value.booleanValue()) {
            return XQueryBoolean.TRUE;
        }
        final var orCount = ctx.OR().size();
        for (int i = 1; i <= orCount; i++) {
            final var visitedExpression = ctx.orExpr(i).accept(this);
            value = value.or(valueFactory, visitedExpression);
            // Short circuit
            if (value.booleanValue()) {
                return XQueryBoolean.TRUE;
            }
            i++;
        }

        return value;
    }


    private XQueryValue handleAndExpr(final OrExprContext ctx) throws XQueryUnsupportedOperation {
        var value = ctx.orExpr(0).accept(this);
        if (!value.isBooleanValue()) {
            // TODO: type error
        }
        // Short circuit
        if (!value.booleanValue()) {
            return XQueryBoolean.FALSE;
        }
        final var orCount = ctx.AND().size();
        for (int i = 1; i <= orCount; i++) {
            final var visitedExpression = ctx.orExpr(i).accept(this);
            value = value.and(valueFactory, visitedExpression);
            // Short circuit
            if (!value.booleanValue()) {
                return XQueryBoolean.FALSE;
            }
            i++;
        }

        return value;
    }


    private XQueryValue handleAdditiveExpr(final OrExprContext ctx) throws XQueryUnsupportedOperation {
        var value = ctx.orExpr(0).accept(this);
        if (!value.isNumericValue()) {
            // TODO: type error
        }
        final var orCount = ctx.additiveOperator().size();
        for (int i = 1; i <= orCount; i++) {
            final var visitedExpression = ctx.orExpr(i).accept(this);
            value = switch (ctx.additiveOperator(i-1).getText()) {
                case "+" -> value.add(valueFactory, visitedExpression);
                case "-" -> value.subtract(valueFactory, visitedExpression);
                default -> null;
            };
            i++;
        }
        return value;
    }



    private XQueryValue handleGeneralComparison(final OrExprContext ctx) throws XQueryUnsupportedOperation {
        final var value = ctx.orExpr(0).accept(this);
        final var visitedExpression = ctx.orExpr(1).accept(this);
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

    private XQueryValue handleValueComparison(final OrExprContext ctx) throws XQueryUnsupportedOperation {
        final var value = ctx.orExpr(0).accept(this);
        final var visitedExpression = ctx.orExpr(1).accept(this);
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


    private XQueryValue handleMultiplicativeExpr(final OrExprContext ctx) throws XQueryUnsupportedOperation {
        var value = ctx.orExpr(0).accept(this);
        if (!value.isNumericValue()) {
            // TODO: type error
        }
        final var orCount = ctx.multiplicativeOperator().size();
        for (int i = 1; i <= orCount; i++) {
            final var visitedExpression = ctx.orExpr(i).accept(this);
            value = switch (ctx.multiplicativeOperator(i-1).getText()) {
                case "*" -> value.multiply(valueFactory, visitedExpression);
                case "div" -> value.divide(valueFactory, visitedExpression);
                case "idiv" -> value.integerDivide(valueFactory, visitedExpression);
                case "mod" -> value.modulus(valueFactory, visitedExpression);
                default -> null;
            };
            i++;
        }
        return value;
    }

    private XQueryValue handleUnionExpr(final OrExprContext ctx) throws XQueryUnsupportedOperation {
        var value = ctx.orExpr(0).accept(this);
        if (!value.isSequence()) {
            // TODO: type error
        }
        final var unionCount = ctx.unionOperator().size();
        for (int i = 1; i <= unionCount; i++) {
            final var visitedExpression = ctx.orExpr(i).accept(this);
            value = value.union(valueFactory, visitedExpression);
            i++;
        }
        return value;
    }

    private XQueryValue handleIntersectionExpr(final OrExprContext ctx) throws XQueryUnsupportedOperation {
        var value = ctx.orExpr(0).accept(this);
        if (!value.isSequence()) {
            // TODO: type error
            return null;
        }
        final var operatorCount = ctx.INTERSECT().size();
        for (int i = 1; i <= operatorCount; i++) {
            final var visitedExpression = ctx.orExpr(i).accept(this);
            value = value.intersect(valueFactory, visitedExpression);
            i++;
        }
        return value;
    }


    private XQueryValue handleSequenceSubtractionExpr(final OrExprContext ctx) throws XQueryUnsupportedOperation {
        var value = ctx.orExpr(0).accept(this);
        if (!value.isSequence()) {
            // TODO: type error
            return null;
        }
        final var operatorCount = ctx.EXCEPT().size();
        for (int i = 1; i <= operatorCount; i++) {
            final var visitedExpression = ctx.orExpr(i).accept(this);
            value = value.except(valueFactory, visitedExpression);
            i++;
        }
        return value;
    }


    private XQueryValue handleUnaryArithmeticExpr(final OrExprContext ctx) throws XQueryUnsupportedOperation {
        final var value = ctx.orExpr(0).accept(this);
        if (!value.isNumericValue()) {
            // TODO: type error
        }
        return value.multiply(valueFactory, valueFactory.number(new BigDecimal(-1)));
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

}
