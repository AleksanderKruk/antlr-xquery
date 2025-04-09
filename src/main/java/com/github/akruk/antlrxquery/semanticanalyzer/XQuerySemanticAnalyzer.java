package com.github.akruk.antlrxquery.semanticanalyzer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.github.akruk.antlrxquery.AntlrXqueryParser.*;
import com.github.akruk.antlrxquery.contextmanagement.XQueryContextManager;
import com.github.akruk.antlrxquery.contextmanagement.dynamiccontext.baseimplementation.XQueryBaseDynamicContextManager;
import com.github.akruk.antlrxquery.contextmanagement.semanticcontext.XQuerySemanticContext;
import com.github.akruk.antlrxquery.contextmanagement.semanticcontext.XQuerySemanticContextManager;
import com.github.akruk.antlrxquery.AntlrXqueryParserBaseVisitor;
import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;
import com.github.akruk.antlrxquery.evaluator.functioncaller.XQueryFunctionCaller;
import com.github.akruk.antlrxquery.evaluator.functioncaller.defaults.BaseFunctionCaller;
import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;
import com.github.akruk.antlrxquery.typesystem.XQueryType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryEnumBasedType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryOccurence;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryTypes;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;
import com.github.akruk.antlrxquery.values.factories.defaults.XQueryBaseValueFactory;

public class XQuerySemanticAnalyzer extends AntlrXqueryParserBaseVisitor<XQueryType>  {
    XQuerySemanticContextManager contextManager;

    Parser parser;
    List<XQueryType> visitedArgumentTypesList;
    XQueryVisitingSemanticContext context;
    XQueryTypeFactory typeFactory;
    XQueryValueFactory valueFactory;
    XQueryFunctionCaller functionCaller;
    Stream<List<TupleElement>> visitedTupleStream;
    List<String> errors;


    private record TupleElement(String name, XQueryType type, String positionalName){};

    public XQuerySemanticAnalyzer(
            final Parser parser,
            final XQuerySemanticContextManager contextManager,
            final XQueryTypeFactory typeFactory,
            final XQueryValueFactory valueFactory,
            final XQueryFunctionCaller functionCaller) {
        this.context = new XQueryVisitingSemanticContext();
        this.parser = parser;
        this.typeFactory = typeFactory;
        this.valueFactory = valueFactory;
        this.functionCaller = functionCaller;
        this.contextManager = contextManager;
        contextManager.enterContext();
        this.context.setItemType(typeFactory.anyNode());
        this.context.setPositionType(typeFactory.number());
    }

    @Override
    public XQueryType visitFLWORExpr(FLWORExprContext ctx) {
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
    public XQueryType visitLetClause(LetClauseContext ctx) {
        final int newVariableCount = ctx.letBinding().size();
        visitedTupleStream = visitedTupleStream.map(tuple -> {
            var newTuple = new ArrayList<TupleElement>(tuple.size() + newVariableCount);
            newTuple.addAll(tuple);
            for (LetBindingContext streamVariable : ctx.letBinding()) {
                String variableName = streamVariable.varName().getText();
                XQueryType assignedValue = streamVariable.exprSingle().accept(this);
                var element = new TupleElement(variableName, assignedValue, null, null);
                newTuple.add(element);
                contextManager.entypeVariable(variableName, assignedValue);
            }
            return newTuple;
        });
        return null;
    }



    @Override
    public XQueryType visitForClause(ForClauseContext ctx) {
        final int numberOfVariables = (int) ctx.forBinding().size();
        visitedTupleStream = visitedTupleStream.flatMap(tuple -> {
            List<List<TupleElement>> newTupleLike = tuple.stream().map(e -> List.of(e)).collect(Collectors.toList());
            for (ForBindingContext streamVariable : ctx.forBinding()) {
                String variableName = streamVariable.varName().getText();
                List<XQueryType> sequence = streamVariable.exprSingle().accept(this).sequence();
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
    public XQueryType visitCountClause(CountClauseContext ctx) {
        final String countVariableName = ctx.varName().getText();
        final MutableInt index = new MutableInt();
        index.i = 1;
        visitedTupleStream = visitedTupleStream.map(tuple -> {
            var newTuple = new ArrayList<TupleElement>(tuple.size() + 1);
            newTuple.addAll(tuple);
            var element = new TupleElement(countVariableName, typeFactory.number(index.i++), null, null);
            contextManager.entypeVariable(element.name, element.type);
            newTuple.add(element);
            return newTuple;
        });
        return null;
    }

    @Override
    public XQueryType visitWhereClause(WhereClauseContext ctx) {
        final var filteringExpression = ctx.exprSingle();
        visitedTupleStream = visitedTupleStream.map(tuple -> {
            XQueryType filter = filteringExpression.accept(this);
            if (!hasEffectiveBooleanValue(filter)) {
                addError(filteringExpression, "Where clause expression needs to have effective boolean value");
            }
            return tuple;
        });
        return null;
    }

    @Override
    public XQueryType visitVarRef(VarRefContext ctx) {
        final String variableName = ctx.varName().getText();
        final XQueryType variableValue = contextManager.getVariable(variableName);
        return variableValue;
    }

    @Override
    public XQueryType visitReturnClause(ReturnClauseContext ctx) {
        final List<XQueryType> results = visitedTupleStream.map((tuple) -> {
            XQueryType value = ctx.exprSingle().accept(this);
            return value;
        }).toList();
        if (results.size() == 1) {
            var value = results.get(0);
            return value;
        }
        return typeFactory.sequence(results);
    }

    @Override
    public XQueryType visitLiteral(final LiteralContext ctx) {
        if (ctx.STRING() != null) {
            final String text = ctx.getText();
            final String removepars = ctx.getText().substring(1, text.length() - 1);
            final String string = unescapeString(removepars);
            valueFactory.string(string);
            return typeFactory.string();
        }

        if (ctx.INTEGER() != null) {
            valueFactory.number(new BigDecimal(ctx.INTEGER().getText()));
            return typeFactory.number();
        }
        valueFactory.number(new BigDecimal(ctx.DECIMAL().getText()));
        return typeFactory.number();
    }

    @Override
    public XQueryType visitParenthesizedExpr(final ParenthesizedExprContext ctx) {
        // Empty parentheses mean an empty sequence '()'
        if (ctx.expr() == null) {
            valueFactory.sequence(List.of());
            return typeFactory.emptySequence();
        }
        return ctx.expr().accept(this);
    }

    @Override
    public XQueryType visitExpr(final ExprContext ctx) {
        // Only one expression
        // e.g. 13
        if (ctx.exprSingle().size() == 1) {
            return ctx.exprSingle(0).accept(this);
        }
        // More than one expression
        // are turned into a flattened list
        final boolean allOccurencesAreZero = ctx.exprSingle().stream()
            .map(e->e.accept(this))
            .allMatch(XQueryType::isZero);
        if (allOccurencesAreZero)
            return typeFactory.emptySequence();
        final boolean allCanBeZero = ctx.exprSingle().stream()
            .map(e->e.accept(this))
            .allMatch(type->type.isZeroOrMore() || type.isZeroOrOne());

        final var firstExpr = ctx.exprSingle(0);
        final var firstExprType = firstExpr.accept(this);
        final int size = ctx.exprSingle().size();
        for (int i = 1; i < size; i++) {
            final var exprSingle = ctx.exprSingle(i);
            final XQueryType expressionType = exprSingle.accept(this);
            final boolean mustBeAnyItem = !expressionType.isSubtypeItemtypeOf(firstExprType);
            if (mustBeAnyItem && allCanBeZero) {
                return typeFactory.zeroOrMore(typeFactory.anyItem());
            } else if (mustBeAnyItem) {
                return typeFactory.oneOrMore(typeFactory.anyItem());
            }
        }
        return allCanBeZero? typeFactory.zeroOrMore(firstExprType)
                           : typeFactory.oneOrMore(firstExprType);
    }


    // TODO: ESCAPE characters
    // &lt ...
    private String unescapeString(final String str) {
        return str.replace("\"\"", "\"").replace("''", "'");
    }

    @Override
    public XQueryType visitFunctionCall(final FunctionCallContext ctx) {
        final var functionName = ctx.functionName().getText();
        // TODO: error handling missing function
        final var savedArgs = saveVisitedArguments();
        ctx.argumentList().accept(this);
        final var value = functionCaller.call(functionName, typeFactory, context, visitedArgumentTypesList);
        visitedArgumentTypesList = savedArgs;
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
    public XQueryType visitQuantifiedExpr(QuantifiedExprContext ctx) {
        List<String> variableNames = ctx.varName().stream()
            .map(VarNameContext::qname)
            .map(QnameContext::getText)
            .toList();
        int variableExpressionCount = ctx.exprSingle().size()-1;
        List<List<XQueryType>> sequences = new ArrayList<>(variableExpressionCount);
        for (var expr : ctx.exprSingle().subList(0, variableExpressionCount)) {
            var sequenceValue = expr.accept(this);
            sequences.add(sequenceValue.sequence());
        }

        final var criterionNode = ctx.exprSingle().getLast();
        if (ctx.EVERY() != null) {
            boolean every = cartesianProduct(sequences).allMatch(variableProduct -> {
                for (int i = 0; i < variableNames.size(); i++) {
                    contextManager.entypeVariable(variableNames.get(i), variableProduct.get(i));
                }
                return criterionNode.accept(this).booleanValue();
            });
            return XQueryType.of(every);
        }
        if (ctx.SOME() != null) {
            boolean some = cartesianProduct(sequences).anyMatch(variableProduct -> {
                for (int i = 0; i < variableNames.size(); i++) {
                    contextManager.entypeVariable(variableNames.get(i), variableProduct.get(i));
                }
                return criterionNode.accept(this).booleanValue();
            });
            return XQueryType.of(some);
        }
        return null;
    }


    @Override
    public XQueryType visitOrExpr(final OrExprContext ctx) {
        try {
            XQueryType value = null;
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




    private XQueryType handleNodeComp(OrExprContext ctx) {
        final var anyElement = XQueryEnumBasedType.anyElement();
        final var visitedLeft = ctx.orExpr(0).accept(this);
        if (!visitedLeft.isSubtypeOf(anyElement)) {
            addError(ctx.orExpr(0), "Operands of node comparison must be a one-item sequence of type 'element'");
        }
        final var visitedRight = ctx.orExpr(1).accept(this);
        if (!visitedRight.isSubtypeOf(anyElement)) {
            addError(ctx.orExpr(1), "Operands of node comparison must be a one-item sequence of type 'element'");
        }
        return typeFactory.boolean_();

    }


    private XQueryType handleRangeExpr(OrExprContext ctx) {
        var fromValue = ctx.orExpr(0).accept(this);
        var toValue = ctx.orExpr(1).accept(this);
        int fromInt = fromValue.numericValue().intValue();
        int toInt = toValue.numericValue().intValue();
        if (fromInt > toInt)
            return typeFactory.emptySequence();
        List<XQueryType> values = IntStream.rangeClosed(fromInt, toInt)
            .mapToObj(i->typeFactory.number(i))
            .collect(Collectors.toList());
        return typeFactory.sequence(values);
    }

    @Override
    public XQueryType visitPathExpr(PathExprContext ctx) {
        boolean pathExpressionFromRoot = ctx.SLASH() != null;
        if (pathExpressionFromRoot) {
            final var savedNodes = saveMatchedModes();
            final var savedAxis = saveAxis();
            // TODO: Context nodes
            var resultingNodeSequence = ctx.relativePathExpr().accept(this);
            return resultingNodeSequence;
        }
        boolean useDescendantOrSelfAxis = ctx.SLASHES() != null;
        if (useDescendantOrSelfAxis) {
            final var savedNodes = saveMatchedModes();
            final var savedAxis = saveAxis();
            var resultingNodeSequence = ctx.relativePathExpr().accept(this);
            return resultingNodeSequence;
        }
        return ctx.relativePathExpr().accept(this);
    }

    @Override
    public XQueryType visitRelativePathExpr(RelativePathExprContext ctx) {
        if (ctx.pathOperator().isEmpty()) {
            return ctx.stepExpr(0).accept(this);
        }
        XQueryType visitedNodeSequence = ctx.stepExpr(0).accept(this);
        matchedNodes = visitedNodeSequence;
        var operationCount = ctx.pathOperator().size();
        for (int i = 1; i <= operationCount; i++) {
            matchedNodes = switch (ctx.pathOperator(i-1).getText()) {
                case "//" -> {
                    List<ParseTree> descendantsOrSelf = getAllDescendantsOrSelf(matchedTreeNodes());
                    yield ctx.stepExpr(i).accept(this);
                }
                case "/" -> ctx.stepExpr(i).accept(this);
                default -> null;
            };
            i++;
        }
        return matchedNodes;
    }

    @Override
    public XQueryType visitStepExpr(StepExprContext ctx) {
        if (ctx.postfixExpr() != null)
            return ctx.postfixExpr().accept(this);
        return ctx.axisStep().accept(this);
    }


    @Override
    public XQueryType visitAxisStep(AxisStepContext ctx) {
        XQueryType stepResult = null;
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
        visitedArgumentTypesList = savedArgs;
        return stepResult;
    }

    // @Override
    // public XQueryType visitPredicateList(PredicateListContext ctx) {
    //     var result = match;
    //     for (var predicate : ctx.predicate()) {
    //         predicate.accept(this);
    //     }
    //     return matchedTreeNodes();
    // }

    @Override
    public XQueryType visitPostfixExpr(PostfixExprContext ctx) {
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
        visitedArgumentTypesList = savedArgs;
        return value;
    }

    @Override
    public XQueryType visitPostfix(PostfixContext ctx) {
        if (ctx.predicate() != null) {
            return ctx.predicate().accept(this);
        }
        final var contextItem = context.getItem();
        if (!contextItem.isFunction()) {
            // TODO: error
            return null;
        }
        final var function = contextItem.functionValue();
        final var value = function.call(typeFactory, context, visitedArgumentTypesList);
        return value;
    }

    @Override
    public XQueryType visitPredicate(PredicateContext ctx) {
        final var contextValue = context.getItem();
        if (contextValue.isAtomic()) {
            // TODO: error
            return null;
        }
        final var sequence = contextValue.sequence();
        final var filteredValues = new ArrayList<XQueryType>(sequence.size());
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
            if (visitedExpression.effectiveXQueryTypeValue()) {
                filteredValues.add(contextItem);
            }
            index++;
        }
        context = savedContext;
        return typeFactory.sequence(filteredValues);
    }



    @Override
    public XQueryType visitContextItemExpr(ContextItemExprContext ctx) {
        return context.getItem();
    }

    @Override
    public XQueryType visitForwardStep(ForwardStepContext ctx) {
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
    public XQueryType visitReverseStep(ReverseStepContext ctx) {
        if (ctx.abbrevReverseStep() != null) {
            return ctx.abbrevReverseStep().accept(this);
        }
        ctx.reverseAxis().accept(this);
        return ctx.nodeTest().accept(this);
    }

    @Override
    public XQueryType visitAbbrevReverseStep(AbbrevReverseStepContext ctx) {
        var matchedParents = getAllParents(matchedTreeNodes());
        return nodeSequence(matchedParents);
    }

    @Override
    public XQueryType visitNodeTest(NodeTestContext ctx) {
        return ctx.nameTest().accept(this);
    }

    private Predicate<String> canBeTokenName = Pattern.compile("^[\\p{IsUppercase}].*").asPredicate();

    @Override
    public XQueryType visitNameTest(NameTestContext ctx) {
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

    private XQueryType handleConcatenation(final OrExprContext ctx) {
        final XQueryType string = typeFactory.string();
        final var operationCount = ctx.CONCATENATION().size();
        for (int i = 0; i <= operationCount; i++) {
            final var visitedType = ctx.orExpr(i).accept(this);
            if (!visitedType.isSubtypeOf(string)) {
                addError(ctx.orExpr(i), "Operands of 'or expression' need to be string");
            }
            i++;
        }
        return string;
    }


    private XQueryType handleArrowExpr(final OrExprContext ctx) {
        final var savedArgs = saveVisitedArguments();
        var contextArgument = ctx.orExpr(0).accept(this);
        visitedArgumentTypesList.add(contextArgument);
        // var isString = !value.isStringValue();
        // var isFunction = !func
        final var arrowCount = ctx.ARROW().size();
        for (int i = 0; i < arrowCount; i++) {
            final var visitedFunction = ctx.arrowFunctionSpecifier(i).accept(this);
            ctx.argumentList(i).accept(this); // visitedArgumentList is set to function's args
            contextArgument = visitedFunction.functionValue().call(typeFactory, context, visitedArgumentTypesList);
            visitedArgumentTypesList = new ArrayList<>();
            visitedArgumentTypesList.add(contextArgument);
            i++;
        }
        visitedArgumentTypesList = savedArgs;
        return contextArgument;
    }

    @Override
    public XQueryType visitArrowFunctionSpecifier(ArrowFunctionSpecifierContext ctx) {
        if (ctx.ID() != null)
            return functionCaller.getFunctionReference(ctx.ID().getText(), typeFactory);
        if (ctx.varRef() != null)
            return ctx.varRef().accept(this);
        return ctx.parenthesizedExpr().accept(this);

    }



    private XQueryType handleOrExpr(final OrExprContext ctx) {
        final XQueryType boolean_ = typeFactory.boolean_();
        final var orCount = ctx.OR().size();
        for (int i = 0; i <= orCount; i++) {
            final var visitedType = ctx.orExpr(i).accept(this);
            if (!visitedType.isSubtypeOf(boolean_)) {
                addError(ctx.orExpr(i), "Operands of 'or expression' need to be boolean");
            }
            i++;
        }
        return boolean_;
    }


    private XQueryType handleAndExpr(final OrExprContext ctx) {
        final XQueryType boolean_ = typeFactory.boolean_();
        final var orCount = ctx.AND().size();
        for (int i = 0; i <= orCount; i++) {
            final var visitedType = ctx.orExpr(i).accept(this);
            if (!visitedType.isSubtypeOf(boolean_)) {
                addError(ctx.orExpr(i), "Operands of 'or expression' need to be boolean");
            }
            i++;
        }
        return boolean_;
    }


    private XQueryType handleAdditiveExpr(final OrExprContext ctx) {
        final XQueryType number = typeFactory.number();
        final var orCount = ctx.additiveOperator().size();
        for (int i = 0; i <= orCount; i++) {
            final OrExprContext operandExpr = ctx.orExpr(i);
            final var operand = operandExpr.accept(this);
            if (!operand.isSubtypeOf(number)) {
                addError(operandExpr, "Operands in additive expression must be numeric");
            }
            i++;
        }
        return typeFactory.number();
    }

    private XQueryType handleGeneralComparison(final OrExprContext ctx) {
        final var leftHandSide = ctx.orExpr(0).accept(this);
        final var rightHandSide = ctx.orExpr(1).accept(this);
        if (!leftHandSide.isSubtypeOf(rightHandSide)) {
            String msg = String.format("The types: %s and %s in general comparison are not comparable",
                    leftHandSide.toString(), rightHandSide.toString());
            addError(ctx, msg);
        }
        return typeFactory.boolean_();
    }

    private XQueryType handleValueComparison(final OrExprContext ctx) {
        final var leftHandSide = ctx.orExpr(0).accept(this);
        final var rightHandSide = ctx.orExpr(1).accept(this);
        if (!leftHandSide.isOne()) {
            addError(ctx.orExpr(0), "Left hand side of 'or expression' must be a one-item sequence");
        }
        if (!rightHandSide.isOne()) {
            addError(ctx.orExpr(1), "Right hand side of 'or expression' must be a one-item sequence");
        }
        if (!leftHandSide.isSubtypeOf(rightHandSide)) {
            String msg = String.format("The types: %s and %s in value comparison are not comparable", leftHandSide.toString(), rightHandSide.toString());
            addError(ctx, msg);
        }
        return typeFactory.boolean_();
    }

    private XQueryType handleMultiplicativeExpr(final OrExprContext ctx) {
        final XQueryType number = typeFactory.number();
        var type = ctx.orExpr(0).accept(this);
        if (!type.isSubtypeOf(number)) {
            addError(ctx, "Multiplicative expression requires a number as its first operand");
        }
        final var orCount = ctx.multiplicativeOperator().size();
        for (int i = 1; i <= orCount; i++) {
            final var visitedType = ctx.orExpr(i).accept(this);
            if (!visitedType.isSubtypeOf(number)) {
                addError(ctx, "Multiplicative expression requires a number as its first operand");
            }
            i++;
        }
        return number;
    }

    private XQueryType handleUnionExpr(final OrExprContext ctx) {
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

    private XQueryType handleIntersectionExpr(final OrExprContext ctx) {
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


    private XQueryType handleSequenceSubtractionExpr(final OrExprContext ctx) {
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


    private XQueryType handleUnaryArithmeticExpr(final OrExprContext ctx) {
        final var type = ctx.orExpr(0).accept(this);
        final XQueryType number = typeFactory.number();
        if (!type.isSubtypeOf(typeFactory.number())) {
            addError(ctx, "Arithmetic unary expression requires a number");
        }
        return number;
    }

    @Override
    public XQueryType visitSwitchExpr(SwitchExprContext ctx) {
        Map<XQueryType, ParseTree> valueToExpression = ctx.switchCaseClause().stream()
                .flatMap(clause -> clause.switchCaseOperand()
                                            .stream().map(operand -> Map.entry(operand.accept(this), clause.exprSingle())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        XQueryType switchedValue = ctx.switchedExpr.accept(this);
        ParseTree toBeExecuted = valueToExpression.getOrDefault(switchedValue, ctx.defaultExpr);
        return toBeExecuted.accept(this);
    }


    @Override
    public XQueryType visitArgument(final ArgumentContext ctx) {
        final var value =  super.visitArgument(ctx);
        visitedArgumentTypesList.add(value);
        return value;
    }

    private List<XQueryType> saveVisitedArguments() {
        final var saved = visitedArgumentTypesList;
        visitedArgumentTypesList = new ArrayList<>();
        return saved;
    }

    private Stream<List<TupleElement>> saveVisitedTupleStream() {
        final Stream<List<TupleElement>> saved = visitedTupleStream;
        visitedTupleStream = Stream.of(List.of());
        return  saved;
    }


    // private XQuerySemanticContext saveContext() {
    //     final XQuerySemanticContext saved = semani;
    //     context = new XQueryVisitingContext();
    //     return saved;
    // }



    private Comparator<List<TupleElement>> ascendingEmptyGreatest(ParseTree expr) {
        return (tuple1, tuple2) -> {
            entypeVariables(tuple1);
            XQueryType value1 = expr.accept(this);
            entypeVariables(tuple2);
            XQueryType value2 = expr.accept(this);
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
            entypeVariables(tuple1);
            XQueryType value1 = expr.accept(this);
            entypeVariables(tuple2);
            XQueryType value2 = expr.accept(this);
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
            entypeVariables(tuple1);
            XQueryType value1 = expr.accept(this);
            entypeVariables(tuple2);
            XQueryType value2 = expr.accept(this);
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
            entypeVariables(tuple1);
            XQueryType value1 = expr.accept(this);
            entypeVariables(tuple2);
            XQueryType value2 = expr.accept(this);
            boolean value1IsEmptySequence = value1.isSequence() && value1.sequence().isEmpty();
            boolean value2IsEmptySequence = value2.isSequence() && value2.sequence().isEmpty();
            if (value1IsEmptySequence && !value2IsEmptySequence) {
                //  empty greatest
                return -1;
            }
            return -compareValues(value1, value2);
        };
    };


    private Comparator<List<TupleElement>> comparatorFromNthOrderSpec(List<OrderSpecContext> orderSpecs,
            int[] modifierMaskArray, int i) {
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


    boolean hasEffectiveBooleanValue(XQueryType type) {
        return !type.isFunction()
                && !type.isMap()
                && !type.isArray();
    }

    void addError(ParserRuleContext where, String message) {
        errors.add(String.format("[line:%i, column:%i] %s", where.getStart().getLine(),
                where.getStart().getCharPositionInLine(), message));
    }

    void addError(ParserRuleContext where, Function<ParserRuleContext, String> message) {
        errors.add(String.format("[line:%i, column:%i] %s", where.getStart().getLine(),
                where.getStart().getCharPositionInLine(), message.apply(where)));
    }

    @Override
    public XQueryType visitIfExpr(IfExprContext ctx) {
        var visitedType = ctx.condition.accept(this);
        if (!hasEffectiveBooleanValue(visitedType)) {
            var msg = String.format(
                    "If condition must have an effective boolean value and the type %s doesn't have one",
                    visitedType.toString());
            addError(ctx, msg);
        }
        var trueType = ctx.ifValue.accept(this);
        var falseType =  ctx.elseValue.accept(this);
        if (trueType.equals(falseType))
            return trueType;
        if (trueType.isSubtypeOf(falseType))
            return falseType;
        if (falseType.isSubtypeOf(trueType))
            return trueType;
        // Add union types
        return typeFactory.any();
    }

    @Override
    public XQueryType visitOrderByClause(OrderByClauseContext ctx) {
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

    private void entypeVariables(List<TupleElement> tuple) {
        for (var e : tuple) {
            contextManager.entypeVariable(e.name, e.type);
            if (e.positionalName != null)
                contextManager.entypeVariable(e.positionalName, typeFactory.number());
        }
    }


}
