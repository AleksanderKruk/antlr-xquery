package com.github.akruk.antlrxquery.semanticanalyzer;

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

import com.github.akruk.antlrxquery.AntlrXqueryParser.AbbrevReverseStepContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ArgumentContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ArrowFunctionSpecifierContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.AxisStepContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ContextItemExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.CountClauseContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ExprSingleContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.FLWORExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ForBindingContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ForClauseContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ForwardAxisContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ForwardStepContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.FunctionCallContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.IfExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.LetBindingContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.LetClauseContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.LiteralContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.NameTestContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.NodeTestContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.OrExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.OrderByClauseContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.OrderSpecContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ParenthesizedExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.PathExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.PositionalVarContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.PostfixContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.PostfixExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.PredicateContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.QnameContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.QuantifiedExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.RelativePathExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ReturnClauseContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ReverseAxisContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ReverseStepContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.StepExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.SwitchExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.VarNameContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.VarRefContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.WhereClauseContext;
import com.github.akruk.antlrxquery.contextmanagement.XQueryContextManager;
import com.github.akruk.antlrxquery.contextmanagement.dynamiccontext.XQueryBaseDynamicContextManager;
import com.github.akruk.antlrxquery.AntlrXqueryParserBaseVisitor;
import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;
import com.github.akruk.antlrxquery.evaluator.functioncaller.XQueryFunctionCaller;
import com.github.akruk.antlrxquery.evaluator.functioncaller.defaults.BaseFunctionCaller;
import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;
import com.github.akruk.antlrxquery.typesystem.XQueryType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;
import com.github.akruk.antlrxquery.values.XQueryBoolean;
import com.github.akruk.antlrxquery.values.factories.defaults.XQueryBaseValueFactory;

public class XQuerySemanticAnalyzer extends AntlrXqueryParserBaseVisitor<Boolean>  {
    XQueryContextManager contextManager;

    Parser parser;
    List<Boolean> visitedArgumentList;
    // XQueryAxis currentAxis;
    XQueryVisitingSemanticContext context;
    XQueryTypeFactory typeFactory;
    XQueryFunctionCaller functionCaller;
    Stream<List<TupleElement>> visitedTupleStream;
    List<String> errors;

    XQueryType visitedType;

    private XQueryType saveVisitedType() {
        var saved = visitedType;
        visitedType = null;
        return saved;
    }

    private record TupleElement(String name, Boolean value, String positionalName, Boolean index){};

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

    // public XQuerySemanticAnalyzer(final ParseTree tree, final Parser parser) {
    //     this(tree, parser, new XQueryBaseContextManager(), new XQueryBaseValueFactory(), new BaseFunctionCaller());
    // }

    public XQuerySemanticAnalyzer(
            final ParseTree tree,
            final Parser parser,
            final XQueryContextManager contextManager,
            final XQueryTypeFactory valueFactory,
            final XQueryFunctionCaller functionCaller) {
        this.context = new XQueryVisitingSemanticContext();
        this.parser = parser;
        this.typeFactory = valueFactory;
        this.functionCaller = functionCaller;
        this.contextManager = contextManager;
        contextManager.enterContext();
        this.context.setItemType(typeFactory.anyNode());
        this.context.setPositionType(typeFactory.number());
    }

    @Override
    public Boolean visitFLWORExpr(FLWORExprContext ctx) {
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
    public Boolean visitLetClause(LetClauseContext ctx) {
        final int newVariableCount = ctx.letBinding().size();
        visitedTupleStream = visitedTupleStream.map(tuple -> {
            var newTuple = new ArrayList<TupleElement>(tuple.size() + newVariableCount);
            newTuple.addAll(tuple);
            for (LetBindingContext streamVariable : ctx.letBinding()) {
                String variableName = streamVariable.varName().getText();
                Boolean assignedValue = streamVariable.exprSingle().accept(this);
                var element = new TupleElement(variableName, assignedValue, null, null);
                newTuple.add(element);
                contextManager.provideVariable(variableName, assignedValue);
            }
            return newTuple;
        });
        return null;
    }



    @Override
    public Boolean visitForClause(ForClauseContext ctx) {
        final int numberOfVariables = (int) ctx.forBinding().size();
        visitedTupleStream = visitedTupleStream.flatMap(tuple -> {
            List<List<TupleElement>> newTupleLike = tuple.stream().map(e -> List.of(e)).collect(Collectors.toList());
            for (ForBindingContext streamVariable : ctx.forBinding()) {
                String variableName = streamVariable.varName().getText();
                List<Boolean> sequence = streamVariable.exprSingle().accept(this).sequence();
                PositionalVarContext positional = streamVariable.positionalVar();
                int sequenceSize = sequence.size();
                if (positional != null) {
                    List<TupleElement> elementsWithIndex = new ArrayList<>(numberOfVariables);
                    String positionalName = positional.varName().getText();
                    for (int i = 0; i < sequenceSize; i++) {
                        var value = sequence.get(i);
                        var element = new TupleElement(variableName, value, positionalName, typeFactory.number(i + 1));
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
            provideVariables(addedVariables);
            return tuple;
        });
        return null;
    }

    private class MutableInt {
        public int i = 0;
    }
    @Override
    public Boolean visitCountClause(CountClauseContext ctx) {
        final String countVariableName = ctx.varName().getText();
        final MutableInt index = new MutableInt();
        index.i = 1;
        visitedTupleStream = visitedTupleStream.map(tuple -> {
            var newTuple = new ArrayList<TupleElement>(tuple.size() + 1);
            newTuple.addAll(tuple);
            var element = new TupleElement(countVariableName, typeFactory.number(index.i++), null, null);
            contextManager.provideVariable(element.name, element.value);
            newTuple.add(element);
            return newTuple;
        });
        return null;
    }

    @Override
    public Boolean visitWhereClause(WhereClauseContext ctx) {
        final var filteringExpression = ctx.exprSingle();
        visitedTupleStream = visitedTupleStream.filter(tuple -> {
            Boolean filter = filteringExpression.accept(this);
            return filter.effectiveBooleanValue();
        });
        return null;
    }

    @Override
    public Boolean visitVarRef(VarRefContext ctx) {
        String variableName = ctx.varName().getText();
        Boolean variableValue = contextManager.getVariable(variableName);
        return variableValue;
    }

    @Override
    public Boolean visitReturnClause(ReturnClauseContext ctx) {
        List<Boolean> results = visitedTupleStream.map((tuple) -> {
            Boolean value = ctx.exprSingle().accept(this);
            return value;
        }).toList();
        if (results.size() == 1) {
            var value = results.get(0);
            return value;
        }
        return typeFactory.sequence(results);
    }

    @Override
    public Boolean visitLiteral(final LiteralContext ctx) {
        if (ctx.STRING() != null) {
            final String text = ctx.getText();
            final String removepars = ctx.getText().substring(1, text.length() - 1);
            final String string = unescapeString(removepars);
            return typeFactory.string(string);
        }

        if (ctx.INTEGER() != null) {
            return typeFactory.number(new BigDecimal(ctx.INTEGER().getText()));
        }

        return typeFactory.number(new BigDecimal(ctx.DECIMAL().getText()));
    }

    @Override
    public Boolean visitParenthesizedExpr(final ParenthesizedExprContext ctx) {
        // Empty parentheses mean an empty sequence '()'
        if (ctx.expr() == null) {
            return typeFactory.sequence(List.of());
        }
        return ctx.expr().accept(this);
    }

    @Override
    public Boolean visitExpr(final ExprContext ctx) {
        // Only one expression
        // e.g. 13
        if (ctx.exprSingle().size() == 1) {
            return ctx.exprSingle(0).accept(this);
        }
        // More than one expression
        // are turned into a flattened list
        final List<Boolean> result = new ArrayList<>();
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
        return typeFactory.sequence(result);
    }


    // TODO: ESCAPE characters
    // &lt ...
    private String unescapeString(final String str) {
        return str.replace("\"\"", "\"").replace("''", "'");
    }

    @Override
    public Boolean visitFunctionCall(final FunctionCallContext ctx) {
        final var functionName = ctx.functionName().getText();
        // TODO: error handling missing function
        final var savedArgs = saveVisitedArguments();
        ctx.argumentList().accept(this);
        final var value = functionCaller.call(functionName, typeFactory, context, visitedArgumentList);
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
    public Boolean visitQuantifiedExpr(QuantifiedExprContext ctx) {
        List<String> variableNames = ctx.varName().stream()
            .map(VarNameContext::qname)
            .map(QnameContext::getText)
            .toList();
        int variableExpressionCount = ctx.exprSingle().size()-1;
        List<List<Boolean>> sequences = new ArrayList<>(variableExpressionCount);
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
    public Boolean visitOrExpr(final OrExprContext ctx) {
        try {
            Boolean value = null;
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




    private Boolean handleNodeComp(OrExprContext ctx) {
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
            return typeFactory.bool(result);
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }

    }

    private ParseTree getSingleNode(final Boolean visitedLeft) throws XQueryUnsupportedOperation {
        ParseTree nodeLeft;
        if (visitedLeft.isAtomic()) {
            nodeLeft = visitedLeft.node();
        } else {
            List<Boolean> sequenceLeft = visitedLeft.exactlyOne(typeFactory).sequence();
            nodeLeft = sequenceLeft.get(0).node();
        }
        return nodeLeft;
    }


    private Boolean handleRangeExpr(OrExprContext ctx) {
        var fromValue = ctx.orExpr(0).accept(this);
        var toValue = ctx.orExpr(1).accept(this);
        int fromInt = fromValue.numericValue().intValue();
        int toInt = toValue.numericValue().intValue();
        if (fromInt > toInt)
            return typeFactory.emptySequence();
        List<Boolean> values = IntStream.rangeClosed(fromInt, toInt)
            .mapToObj(i->typeFactory.number(i))
            .collect(Collectors.toList());
        return typeFactory.sequence(values);
    }

    @Override
    public Boolean visitPathExpr(PathExprContext ctx) {
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
    public Boolean visitRelativePathExpr(RelativePathExprContext ctx) {
        if (ctx.pathOperator().isEmpty()) {
            return ctx.stepExpr(0).accept(this);
        }
        Boolean visitedNodeSequence = ctx.stepExpr(0).accept(this);
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

    private Boolean nodeSequence(List<ParseTree> treenodes) {
        List<Boolean> nodeSequence = treenodes.stream()
            .map(typeFactory::node)
            .collect(Collectors.toList());
        return typeFactory.sequence(nodeSequence);
    }

    private List<ParseTree> matchedTreeNodes() {
        return matchedNodes.sequence().stream().map(Boolean::node).toList();
    }

    @Override
    public Boolean visitStepExpr(StepExprContext ctx) {
        if (ctx.postfixExpr() != null)
            return ctx.postfixExpr().accept(this);
        return ctx.axisStep().accept(this);
    }


    @Override
    public Boolean visitAxisStep(AxisStepContext ctx) {
        Boolean stepResult = null;
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
    // public Boolean visitPredicateList(PredicateListContext ctx) {
    //     var result = match;
    //     for (var predicate : ctx.predicate()) {
    //         predicate.accept(this);
    //     }
    //     return matchedTreeNodes();
    // }

    @Override
    public Boolean visitPostfixExpr(PostfixExprContext ctx) {
        if (ctx.postfix().isEmpty()) {
            return ctx.primaryExpr().accept(this);
        }

        final var savedContext = saveContext();
        final var savedArgs = saveVisitedArguments();
        var value = ctx.primaryExpr().accept(this);
        int index = 1;
        context.setSize(ctx.postfix().size());
        for (var postfix : ctx.postfix()) {
            context.setItemType(value);
            context.setPosition(index);
            value = postfix.accept(this);
            index++;
        }
        context = savedContext;
        visitedArgumentList = savedArgs;
        return value;
    }

    @Override
    public Boolean visitPostfix(PostfixContext ctx) {
        if (ctx.predicate() != null) {
            return ctx.predicate().accept(this);
        }
        final var contextItem = context.getItem();
        if (!contextItem.isFunction()) {
            // TODO: error
            return null;
        }
        final var function = contextItem.functionValue();
        final var value = function.call(typeFactory, context, visitedArgumentList);
        return value;
    }

    @Override
    public Boolean visitPredicate(PredicateContext ctx) {
        final var contextValue = context.getItem();
        if (contextValue.isAtomic()) {
            // TODO: error
            return null;
        }
        final var sequence = contextValue.sequence();
        final var filteredValues = new ArrayList<Boolean>(sequence.size());
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
                    return typeFactory.emptySequence();
                }
                return sequence.get(i);
            }
            if (visitedExpression.effectiveBooleanValue()) {
                filteredValues.add(contextItem);
            }
            index++;
        }
        context = savedContext;
        return typeFactory.sequence(filteredValues);
    }



    @Override
    public Boolean visitContextItemExpr(ContextItemExprContext ctx) {
        return context.getItem();
    }

    @Override
    public Boolean visitForwardStep(ForwardStepContext ctx) {
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
    public Boolean visitReverseStep(ReverseStepContext ctx) {
        if (ctx.abbrevReverseStep() != null) {
            return ctx.abbrevReverseStep().accept(this);
        }
        ctx.reverseAxis().accept(this);
        return ctx.nodeTest().accept(this);
    }

    @Override
    public Boolean visitAbbrevReverseStep(AbbrevReverseStepContext ctx) {
        var matchedParents = getAllParents(matchedTreeNodes());
        return nodeSequence(matchedParents);
    }

    @Override
    public Boolean visitNodeTest(NodeTestContext ctx) {
        return ctx.nameTest().accept(this);
    }

    private Predicate<String> canBeTokenName = Pattern.compile("^[\\p{IsUppercase}].*").asPredicate();
    @Override
    public Boolean visitNameTest(NameTestContext ctx) {
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
    public Boolean visitForwardAxis(ForwardAxisContext ctx) {
        if (ctx.CHILD() != null) currentAxis = XQueryAxis.CHILD;
        if (ctx.DESCENDANT() != null) currentAxis = XQueryAxis.DESCENDANT;
        if (ctx.SELF() != null) currentAxis = XQueryAxis.SELF;
        if (ctx.DESCENDANT_OR_SELF() != null) currentAxis = XQueryAxis.DESCENDANT_OR_SELF;
        if (ctx.FOLLOWING_SIBLING() != null) currentAxis = XQueryAxis.FOLLOWING_SIBLING;
        if (ctx.FOLLOWING() != null) currentAxis = XQueryAxis.FOLLOWING;
        return null;
    }

    @Override
    public Boolean visitReverseAxis(ReverseAxisContext ctx) {
        if (ctx.PARENT() != null) currentAxis = XQueryAxis.PARENT;
        if (ctx.ANCESTOR() != null) currentAxis = XQueryAxis.ANCESTOR;
        if (ctx.PRECEDING_SIBLING() != null) currentAxis = XQueryAxis.PRECEDING_SIBLING;
        if (ctx.PRECEDING() != null) currentAxis = XQueryAxis.PRECEDING;
        if (ctx.ANCESTOR_OR_SELF() != null) currentAxis = XQueryAxis.ANCESTOR_OR_SELF;
        return null;
    }

    private Boolean handleConcatenation(final OrExprContext ctx) throws XQueryUnsupportedOperation {
        var value = ctx.orExpr(0).accept(this);
        if (!value.isStringValue()) {
            // TODO: type error
        }
        final var operationCount = ctx.CONCATENATION().size();
        for (int i = 1; i <= operationCount; i++) {
            final var visitedExpression = ctx.orExpr(i).accept(this);
            value = value.concatenate(typeFactory, visitedExpression);
            i++;
        }

        return value;
    }


    private Boolean handleArrowExpr(final OrExprContext ctx)
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
            contextArgument = visitedFunction.functionValue().call(typeFactory, context, visitedArgumentList);
            visitedArgumentList = new ArrayList<>();
            visitedArgumentList.add(contextArgument);
            i++;
        }
        visitedArgumentList = savedArgs;
        return contextArgument;
    }

    @Override
    public Boolean visitArrowFunctionSpecifier(ArrowFunctionSpecifierContext ctx) {
        if (ctx.ID() != null)
            return functionCaller.getFunctionReference(ctx.ID().getText(), typeFactory);
        if (ctx.varRef() != null)
            return ctx.varRef().accept(this);
        return ctx.parenthesizedExpr().accept(this);

    }



    private Boolean handleOrExpr(final OrExprContext ctx) throws XQueryUnsupportedOperation {
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
            value = value.or(typeFactory, visitedExpression);
            // Short circuit
            if (value.booleanValue()) {
                return XQueryBoolean.TRUE;
            }
            i++;
        }

        return value;
    }


    private Boolean handleAndExpr(final OrExprContext ctx) throws XQueryUnsupportedOperation {
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
            value = value.and(typeFactory, visitedExpression);
            // Short circuit
            if (!value.booleanValue()) {
                return XQueryBoolean.FALSE;
            }
            i++;
        }

        return value;
    }


    private Boolean handleAdditiveExpr(final OrExprContext ctx) throws XQueryUnsupportedOperation {
        var value = ctx.orExpr(0).accept(this);
        if (!value.isNumericValue()) {
            // TODO: type error
        }
        final var orCount = ctx.additiveOperator().size();
        for (int i = 1; i <= orCount; i++) {
            final var visitedExpression = ctx.orExpr(i).accept(this);
            value = switch (ctx.additiveOperator(i-1).getText()) {
                case "+" -> value.add(typeFactory, visitedExpression);
                case "-" -> value.subtract(typeFactory, visitedExpression);
                default -> null;
            };
            i++;
        }
        return value;
    }



    private Boolean handleGeneralComparison(final OrExprContext ctx) throws XQueryUnsupportedOperation {
        final var value = ctx.orExpr(0).accept(this);
        final var visitedExpression = ctx.orExpr(1).accept(this);
        return switch(ctx.generalComp().getText()) {
            case "=" -> value.generalEqual(typeFactory, visitedExpression);
            case "!=" -> value.generalUnequal(typeFactory, visitedExpression);
            case ">" -> value.generalGreaterThan(typeFactory, visitedExpression);
            case "<" -> value.generalLessThan(typeFactory, visitedExpression);
            case "<=" -> value.generalLessEqual(typeFactory, visitedExpression);
            case ">=" -> value.generalGreaterEqual(typeFactory, visitedExpression);
            default -> null;
        };
    }

    private Boolean handleValueComparison(final OrExprContext ctx) throws XQueryUnsupportedOperation {
        final var value = ctx.orExpr(0).accept(this);
        final var visitedExpression = ctx.orExpr(1).accept(this);
        return switch(ctx.valueComp().getText()) {
            case "eq" -> value.valueEqual(typeFactory, visitedExpression);
            case "ne" -> value.valueUnequal(typeFactory, visitedExpression);
            case "lt" -> value.valueLessThan(typeFactory, visitedExpression);
            case "gt" -> value.valueGreaterThan(typeFactory, visitedExpression);
            case "le" -> value.valueLessEqual(typeFactory, visitedExpression);
            case "ge" -> value.valueGreaterEqual(typeFactory, visitedExpression);
            default -> null;
        };
    }


    private Boolean handleMultiplicativeExpr(final OrExprContext ctx) throws XQueryUnsupportedOperation {
        var value = ctx.orExpr(0).accept(this);
        if (!value.isNumericValue()) {
            // TODO: type error
        }
        final var orCount = ctx.multiplicativeOperator().size();
        for (int i = 1; i <= orCount; i++) {
            final var visitedExpression = ctx.orExpr(i).accept(this);
            value = switch (ctx.multiplicativeOperator(i-1).getText()) {
                case "*" -> value.multiply(typeFactory, visitedExpression);
                case "div" -> value.divide(typeFactory, visitedExpression);
                case "idiv" -> value.integerDivide(typeFactory, visitedExpression);
                case "mod" -> value.modulus(typeFactory, visitedExpression);
                default -> null;
            };
            i++;
        }
        return value;
    }

    private Boolean handleUnionExpr(final OrExprContext ctx) throws XQueryUnsupportedOperation {
        var value = ctx.orExpr(0).accept(this);
        if (!value.isSequence()) {
            // TODO: type error
        }
        final var unionCount = ctx.unionOperator().size();
        for (int i = 1; i <= unionCount; i++) {
            final var visitedExpression = ctx.orExpr(i).accept(this);
            value = value.union(typeFactory, visitedExpression);
            i++;
        }
        return value;
    }

    private Boolean handleIntersectionExpr(final OrExprContext ctx) throws XQueryUnsupportedOperation {
        var value = ctx.orExpr(0).accept(this);
        if (!value.isSequence()) {
            // TODO: type error
            return null;
        }
        final var operatorCount = ctx.INTERSECT().size();
        for (int i = 1; i <= operatorCount; i++) {
            final var visitedExpression = ctx.orExpr(i).accept(this);
            value = value.intersect(typeFactory, visitedExpression);
            i++;
        }
        return value;
    }


    private Boolean handleSequenceSubtractionExpr(final OrExprContext ctx) throws XQueryUnsupportedOperation {
        var value = ctx.orExpr(0).accept(this);
        if (!value.isSequence()) {
            // TODO: type error
            return null;
        }
        final var operatorCount = ctx.EXCEPT().size();
        for (int i = 1; i <= operatorCount; i++) {
            final var visitedExpression = ctx.orExpr(i).accept(this);
            value = value.except(typeFactory, visitedExpression);
            i++;
        }
        return value;
    }


    private Boolean handleUnaryArithmeticExpr(final OrExprContext ctx) throws XQueryUnsupportedOperation {
        final var value = ctx.orExpr(0).accept(this);
        if (!value.isNumericValue()) {
            // TODO: type error
        }
        return value.multiply(typeFactory, typeFactory.number(new BigDecimal(-1)));
    }

    @Override
    public Boolean visitSwitchExpr(SwitchExprContext ctx) {
        Map<Boolean, ParseTree> valueToExpression = ctx.switchCaseClause().stream()
                .flatMap(clause -> clause.switchCaseOperand()
                                            .stream().map(operand -> Map.entry(operand.accept(this), clause.exprSingle())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Boolean switchedValue = ctx.switchedExpr.accept(this);
        ParseTree toBeExecuted = valueToExpression.getOrDefault(switchedValue, ctx.defaultExpr);
        return toBeExecuted.accept(this);
    }


    @Override
    public Boolean visitArgument(final ArgumentContext ctx) {
        final var value =  super.visitArgument(ctx);
        visitedArgumentList.add(value);
        return value;
    }

    private List<Boolean> saveVisitedArguments() {
        final var saved = visitedArgumentList;
        visitedArgumentList = new ArrayList<>();
        return saved;
    }

    private Boolean saveMatchedModes() {
        final Boolean saved = matchedNodes;
        matchedNodes = typeFactory.sequence(List.of());
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




    private Comparator<List<TupleElement>> ascendingEmptyGreatest(ParseTree expr) {
        return (tuple1, tuple2) -> {
            provideVariables(tuple1);
            Boolean value1 = expr.accept(this);
            provideVariables(tuple2);
            Boolean value2 = expr.accept(this);
            boolean value1IsEmptySequence = value1.isSequence() && value1.sequence().isEmpty();
            boolean value2IsEmptySequence = value2.isSequence() && value2.sequence().isEmpty();
            if (value1IsEmptySequence && !value2IsEmptySequence) {
                //  empty greatest
                return 1;
            }
            return compareValues(value1, value2);
        };
    };

    private Comparator<List<TupleElement>> ascendingEmptyLeast(ParseTree expr) {
        return (tuple1, tuple2) -> {
            provideVariables(tuple1);
            Boolean value1 = expr.accept(this);
            provideVariables(tuple2);
            Boolean value2 = expr.accept(this);
            boolean value1IsEmptySequence = value1.isSequence() && value1.sequence().isEmpty();
            boolean value2IsEmptySequence = value2.isSequence() && value2.sequence().isEmpty();
            if (value1IsEmptySequence && !value2IsEmptySequence) {
                //  empty greatest
                return -1;
            }
            return compareValues(value1, value2);
        };
    };

    private Comparator<List<TupleElement>> descendingEmptyGreatest(ParseTree expr) {
        return (tuple1, tuple2) -> {
            provideVariables(tuple1);
            Boolean value1 = expr.accept(this);
            provideVariables(tuple2);
            Boolean value2 = expr.accept(this);
            boolean value1IsEmptySequence = value1.isSequence() && value1.sequence().isEmpty();
            boolean value2IsEmptySequence = value2.isSequence() && value2.sequence().isEmpty();
            if (value1IsEmptySequence && !value2IsEmptySequence) {
                //  empty greatest
                return -1;
            }
            return -compareValues(value1, value2);
        };
    };

    private Comparator<List<TupleElement>> descendingEmptyLeast(ParseTree expr) {
        return (tuple1, tuple2) -> {
            provideVariables(tuple1);
            Boolean value1 = expr.accept(this);
            provideVariables(tuple2);
            Boolean value2 = expr.accept(this);
            boolean value1IsEmptySequence = value1.isSequence() && value1.sequence().isEmpty();
            boolean value2IsEmptySequence = value2.isSequence() && value2.sequence().isEmpty();
            if (value1IsEmptySequence && !value2IsEmptySequence) {
                //  empty greatest
                return -1;
            }
            return -compareValues(value1, value2);
        };
    };


    private Comparator<List<TupleElement>> comparatorFromNthOrderSpec(List<OrderSpecContext> orderSpecs, int[] modifierMaskArray, int i) {
        final OrderSpecContext orderSpec = orderSpecs.get(0);
        final ExprSingleContext expr = orderSpec.exprSingle();
        int modifierMask = modifierMaskArray[i];
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
    public Boolean visitIfExpr(IfExprContext ctx) {
        var visitedExpression = ctx.condition.accept(this);
        if (visitedExpression.effectiveBooleanValue())
            return ctx.ifValue.accept(this);
        else
            return ctx.elseValue.accept(this);
    }

    @Override
    public Boolean visitOrderByClause(OrderByClauseContext ctx) {
        final int sortingExprCount = ctx.orderSpecList().orderSpec().size();
        final var orderSpecs = ctx.orderSpecList().orderSpec();
        final int[] modifierMaskArray = orderSpecs.stream()
            .map(OrderSpecContext::orderModifier)
            .mapToInt(m->{
                int isDescending = m.DESCENDING() != null? 1 : 0;
                int isEmptyLeast = m.LEAST() != null? 1 : 0;
                int mask = (isDescending << 1) | isEmptyLeast;
                return mask;
            })
            .toArray();
        visitedTupleStream = visitedTupleStream.sorted((tuple1, tuple2) -> {
            var comparator = comparatorFromNthOrderSpec(orderSpecs, modifierMaskArray, 0);
            for (int i = 1; i < sortingExprCount; i++) {
                var nextComparator = comparatorFromNthOrderSpec(orderSpecs, modifierMaskArray, i);
                comparator = comparator.thenComparing(nextComparator);
            }
            return comparator.compare(tuple1, tuple2);
        }).map(tuple->{
            provideVariables(tuple);
            return tuple;
        });
        return null;
    }

    private int compareValues(Boolean value1, Boolean value2) {
        if (value1.valueEqual(typeFactory, value2).booleanValue()) {
            return 0;
        } else {
            if (value1.valueLessThan(typeFactory, value2).booleanValue()) {
                return -1;
            }
            ;
            return 1;
        }
    }

    private void provideVariables(List<TupleElement> tuple) {
        for (var e : tuple) {
            contextManager.provideVariable(e.name, e.value);
            if (e.positionalName != null)
                contextManager.provideVariable(e.positionalName, e.index);
        }
    }


}
