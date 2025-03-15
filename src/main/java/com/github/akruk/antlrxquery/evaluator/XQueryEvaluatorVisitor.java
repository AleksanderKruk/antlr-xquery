package com.github.akruk.antlrxquery.evaluator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import com.github.akruk.antlrxquery.AntlrXqueryParserBaseVisitor;
import com.github.akruk.antlrxquery.AntlrXqueryParser.AbbrevReverseStepContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ArgumentContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.AxisStepContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ForwardAxisContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ForwardStepContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.FunctionCallContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.LiteralContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.NameTestContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.NodeTestContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.OrExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ParenthesizedExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.PathExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.RelativePathExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ReverseAxisContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ReverseStepContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.StepExprContext;
import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;
import com.github.akruk.antlrxquery.values.XQueryNumber;
import com.github.akruk.antlrxquery.values.XQuerySequence;
import com.github.akruk.antlrxquery.values.XQueryString;
import com.github.akruk.antlrxquery.values.XQueryTreeNode;
import com.github.akruk.antlrxquery.values.XQueryValue;
import com.github.akruk.antlrxquery.values.XQueryBoolean;
import com.github.akruk.antlrxquery.values.XQueryFunction;

class XQueryEvaluatorVisitor extends AntlrXqueryParserBaseVisitor<XQueryValue> {
    XQueryValue root;
    Parser parser;
    List<XQueryValue> visitedArgumentList;
    XQueryValue matchedNodes;
    XQueryAxis currentAxis;


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



    private final class Functions {
        private static final XQueryValue not(final List<XQueryValue> args) {
            assert args.size() == 1;
            try {
                return args.get(0).not();
            } catch (final XQueryUnsupportedOperation e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
        }

        // fn:abs($arg as xs:numeric?) as xs:numeric?
        private static final XQueryValue abs(final List<XQueryValue> args) {
            assert args.size() == 1;
            final var arg = args.get(0);
            // TODO: Add type check failure
            if (!arg.isNumericValue())
                return null;
            return new XQueryNumber(arg.numericValue().abs());
        }

        private static final XQueryValue ceiling(final List<XQueryValue> args) {
            assert args.size() == 1;
            final var arg = args.get(0);
            // TODO: Add type check failure
            if (!arg.isNumericValue())
                return null;
            return new XQueryNumber(arg.numericValue().setScale(0, RoundingMode.CEILING));
        }

        private static final XQueryValue floor(final List<XQueryValue> args) {
            assert args.size() == 1;
            final var arg = args.get(0);
            // TODO: Add type check failure
            if (!arg.isNumericValue())
                return null;
            return new XQueryNumber(arg.numericValue().setScale(0, RoundingMode.FLOOR));
        }

        private static final XQueryValue round(final List<XQueryValue> args) {
            assert args.size() == 1 || args.size() == 2;
            final var arg1 = args.get(0);
            final var number1 = arg1.numericValue();
            final var negativeNumber = number1.compareTo(BigDecimal.ZERO) == -1;
            final var oneArg = args.size() == 1;
            if (oneArg && negativeNumber) {
                return new XQueryNumber(number1.setScale(0, RoundingMode.HALF_DOWN));
            }
            if (oneArg) {
                return new XQueryNumber(number1.setScale(0, RoundingMode.HALF_UP));
            }
            final var number2 = args.get(1).numericValue();
            final int scale = number2.intValue();
            if (negativeNumber) {
                return new XQueryNumber(arg1.numericValue().setScale(scale, RoundingMode.HALF_DOWN));
            }
            if (scale > 0) {
                final var roundedNumberNormalNotation = number1.setScale(scale, RoundingMode.HALF_UP);
                return new XQueryNumber(roundedNumberNormalNotation);
            }
            final var roundedNumber = number1.setScale(scale, RoundingMode.HALF_UP);
            final var roundedNumberNormalNotation = roundedNumber.setScale(0, RoundingMode.HALF_UP);
            return new XQueryNumber(roundedNumberNormalNotation);
        }

        // private static final XQueryValue roundHaftToEven(final List<XQueryValue> args) {
        //     assert args.size() == 1 || args.size() == 2;
        //     final var arg1 = args.get(0);
        //     final var number1 = arg1.numericValue();
        //     final var negativeNumber = number1.compareTo(BigDecimal.ZERO) == -1;
        //     final var oneArg = args.size() == 1;
        //     if (oneArg && negativeNumber) {
        //         return new XQueryNumber(number1.setScale(0, RoundingMode.HALF_DOWN));
        //     }
        //     if (oneArg) {
        //         return new XQueryNumber(number1.setScale(0, RoundingMode.HALF_UP));
        //     }
        //     final var number2 = args.get(1).numericValue();
        //     final int scale = number2.intValue();
        //     if (negativeNumber) {
        //         return new XQueryNumber(arg1.numericValue().setScale(scale, RoundingMode.HALF_DOWN));
        //     }
        //     if (scale > 0) {
        //         final var roundedNumberNormalNotation = number1.setScale(scale, RoundingMode.HALF_UP);
        //         return new XQueryNumber(roundedNumberNormalNotation);
        //     }
        //     final var roundedNumber = number1.setScale(scale, RoundingMode.HALF_UP);
        //     final var roundedNumberNormalNotation = roundedNumber.setScale(0, RoundingMode.HALF_UP);
        //     return new XQueryNumber(roundedNumberNormalNotation);
        // }

        private static final XQueryValue numericAdd(final List<XQueryValue> args) {
            assert args.size() == 2;
            final var val1 = args.get(0);
            final var val2 = args.get(1);
            // TODO: Add type check failure
            if (!val1.isNumericValue() || !val2.isNumericValue())
                return null;
            try {
                return val1.add(val2);
            } catch (final XQueryUnsupportedOperation e) {
                return null;
            }
        }

        private static final XQueryValue numericSubtract(final List<XQueryValue> args) {
            assert args.size() == 2;
            final var val1 = args.get(0);
            final var val2 = args.get(1);
            // TODO: Add type check failure
            if (!val1.isNumericValue() || !val2.isNumericValue())
                return null;
            try {
                return val1.subtract(val2);
            } catch (final XQueryUnsupportedOperation e) {
                return null;
            }
        }

        private static final XQueryValue numericMultiply(final List<XQueryValue> args) {
            assert args.size() == 2;
            final var val1 = args.get(0);
            final var val2 = args.get(1);
            // TODO: Add type check failure
            if (!val1.isNumericValue() || !val2.isNumericValue())
                return null;
            try {
                return val1.multiply(val2);
            } catch (final XQueryUnsupportedOperation e) {
                return null;
            }
        }


        private static final XQueryValue numericDivide(final List<XQueryValue> args) {
            assert args.size() == 2;
            final var val1 = args.get(0);
            final var val2 = args.get(1);
            // TODO: Add type check failure
            if (!val1.isNumericValue() || !val2.isNumericValue())
                return null;
            try {
                return val1.divide(val2);
            } catch (final XQueryUnsupportedOperation e) {
                return null;
            }
        }

        private static final XQueryValue numericIntegerDivide(final List<XQueryValue> args) {
            assert args.size() == 2;
            final var val1 = args.get(0);
            final var val2 = args.get(1);
            // TODO: Add type check failure
            if (!val1.isNumericValue() || !val2.isNumericValue())
                return null;
            try {
                return val1.integerDivide(val2);
            } catch (final XQueryUnsupportedOperation e) {
                return null;
            }
        }


        private static final XQueryValue numericMod(final List<XQueryValue> args) {
            assert args.size() == 2;
            final var val1 = args.get(0);
            final var val2 = args.get(1);
            // TODO: Add type check failure
            if (!val1.isNumericValue() || !val2.isNumericValue())
                return null;
            try {
                return val1.modulus(val2);
            } catch (final XQueryUnsupportedOperation e) {
                return null;
            }
        }

        private static final XQueryValue numericUnaryPlus(final List<XQueryValue> args) {
            assert args.size() == 1;
            final var val1 = args.get(0);
            // TODO: Add type check failure
            if (!val1.isNumericValue())
                return null;
            return val1;
        }

        private static final XQueryValue numericUnaryMinus(final List<XQueryValue> args) {
            assert args.size() == 1;
            final var val1 = args.get(0);
            // TODO: Add type check failure
            if (!val1.isNumericValue())
                return null;
            return new XQueryNumber(val1.numericValue().negate());
        }

        private static XQueryValue true_(final List<XQueryValue> args) {
            assert args.size() == 0;
            return XQueryBoolean.TRUE;
        }

        private static XQueryValue false_(final List<XQueryValue> args) {
            assert args.size() == 0;
            return XQueryBoolean.FALSE;
        }

        private static XQueryValue pi(final List<XQueryValue> args) {
            assert args.size() == 0;
            return new XQueryNumber(new BigDecimal(Math.PI));
        }

    }

    private static final Map<String, XQueryFunction> functions;
    static {
        functions = new HashMap<>();
        functions.put("true", XQueryEvaluatorVisitor.Functions::true_);
        functions.put("false", XQueryEvaluatorVisitor.Functions::false_);
        functions.put("not", XQueryEvaluatorVisitor.Functions::not);
        functions.put("abs", XQueryEvaluatorVisitor.Functions::abs);
        functions.put("ceiling", XQueryEvaluatorVisitor.Functions::ceiling);
        functions.put("floor", XQueryEvaluatorVisitor.Functions::floor);
        functions.put("round", XQueryEvaluatorVisitor.Functions::round);
        functions.put("pi", XQueryEvaluatorVisitor.Functions::pi);
        functions.put("round", XQueryEvaluatorVisitor.Functions::round);
        functions.put("numeric-add", XQueryEvaluatorVisitor.Functions::numericAdd);
        functions.put("numeric-subtract", XQueryEvaluatorVisitor.Functions::numericSubtract);
        functions.put("numeric-multiply", XQueryEvaluatorVisitor.Functions::numericMultiply);
        functions.put("numeric-divide", XQueryEvaluatorVisitor.Functions::numericDivide);
        functions.put("numeric-integer-divide", XQueryEvaluatorVisitor.Functions::numericIntegerDivide);
        functions.put("numeric-mod", XQueryEvaluatorVisitor.Functions::numericMod);
        functions.put("numeric-unary-plus", XQueryEvaluatorVisitor.Functions::numericUnaryPlus);
        functions.put("numeric-unary-minus", XQueryEvaluatorVisitor.Functions::numericUnaryMinus);
    }

    public XQueryEvaluatorVisitor(final ParseTree tree, final Parser parser) {
        ParserRuleContext root = new ParserRuleContext();
		root.children = Collections.singletonList(tree); // don't set t's parent.
        this.root = new XQueryTreeNode(root);
        this.parser = parser;
    }

    @Override
    public XQueryValue visitLiteral(final LiteralContext ctx) {

        if (ctx.STRING() != null) {
            final String text = ctx.getText();
            final String removepars = ctx.getText().substring(1, text.length() - 1);
            final String string = unescapeString(removepars);
            return new XQueryString(string);
        }

        if (ctx.INTEGER() != null) {
            return new XQueryNumber(new BigDecimal(ctx.INTEGER().getText()));
        }

        return new XQueryNumber(new BigDecimal(ctx.DECIMAL().getText()));
    }

    @Override
    public XQueryValue visitParenthesizedExpr(final ParenthesizedExprContext ctx) {
        // Empty parentheses mean an empty sequence '()'
        if (ctx.expr() == null) {
            return new XQuerySequence();
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
        return new XQuerySequence(result);
    }


    // TODO: ESCAPE characters
    // &lt ...
    private String unescapeString(final String str) {
        return str.replace("\"\"", "\"").replace("''", "'");
    }

    @Override
    public XQueryValue visitFunctionCall(final FunctionCallContext ctx) {
        final var functionName = ctx.ID().getText();
        if (!functions.containsKey(functionName)) {
            // TODO: error handling missing function
            return null;
        }
        final var savedArgs = saveVisitedArguments();
        ctx.argumentList().accept(this);
        final XQueryFunction function = functions.get(functionName);
        final var value = function.call(visitedArgumentList);
        visitedArgumentList = savedArgs;
        return value;
    }

    @Override
    public XQueryValue visitOrExpr(final OrExprContext ctx) {
        try {
            XQueryValue value = null;
            if (ctx.orExpr().size() == 0) {
                value = ctx.pathExpr(0).accept(this);
            } else {
                // TODO path expr
            }
            if (!ctx.OR().isEmpty())
                return handleOrExpr(ctx);
            if (!ctx.AND().isEmpty())
                return handleAndExpr(ctx);
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


            return value;
        } catch (final XQueryUnsupportedOperation e) {
            // TODO: error handling
            return null;
        }
    }

    @Override
    public XQueryValue visitPathExpr(PathExprContext ctx) {
        boolean pathExpressionFromRoot = ctx.SLASH() != null;
        if (pathExpressionFromRoot) {
            final var savedNodes = saveMatchedModes();
            final var savedAxis = saveAxis();
            // TODO: Context nodes
            matchedNodes = nodeSequence(List.of(root.node()));
            var resultingNodeSequence = ctx.relativePathExpr().accept(this);
            matchedNodes = savedNodes;
            currentAxis = savedAxis;
            return resultingNodeSequence;
        }
        boolean useDescendantOrSelfAxis = ctx.SLASHES() != null;
        if (useDescendantOrSelfAxis) {
            final var savedNodes = saveMatchedModes();
            final var savedAxis = saveAxis();
            currentAxis = XQueryAxis.DESCENDANT_OR_SELF;
            List<ParseTree> matchedTreeNodes = getDescendantsOrSelf(root.node());
            matchedNodes = nodeSequence(matchedTreeNodes);
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
            matchedNodes = switch (ctx.pathOperator(i).getText()) {
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
            .map(XQueryTreeNode::new)
            .collect(Collectors.toList());
        return new XQuerySequence(nodeSequence);
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
        // TODO: add predicate list
        return stepResult;
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
            currentAxis = XQueryAxis.CHILD;
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


    private Predicate<String> canBeTokenName = Pattern.compile("^[[:upper:]]").asPredicate();
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
        for (ParseTree node : stepNodes) {
            String name = ctx.ID().toString();
            if (canBeTokenName.test(name)) {
                // test for tokens
                int tokenType = parser.getTokenType(name);
                // TODO: error values
                if (tokenType == Token.INVALID_TYPE) return null;
                TerminalNode tokenNode = (TerminalNode) node;
                Token token = tokenNode.getSymbol();
                if (token.getType() == tokenType) {
                    matchedTreeNodes.add(tokenNode);
                }
            }
            else { // test for rule
                int ruleIndex = parser.getRuleIndex(name);
                // TODO: error values
                if (ruleIndex == -1) return null;
                ParserRuleContext testedRule = (ParserRuleContext) node;
                if (testedRule.getRuleIndex() == ruleIndex) {
                    matchedTreeNodes.add(testedRule);
                }
            }
        }
        return nodeSequence(stepNodes);
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
        var followingSiblings = getFollowingSiblings(node);
        var following = getAllDescendantsOrSelf(followingSiblings);
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
        var precedingSiblings = getPrecedingSiblings(node);
        var preceding = getAllDescendantsOrSelf(precedingSiblings);
        return preceding;
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
        var parentsChildren = getChildren(parent);
        var nodeIndex = parentsChildren.indexOf(node);
        var followingSibling = parentsChildren.subList(nodeIndex, parentsChildren.size());
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
            descendants.forEach(descendant->allDescendants.addFirst(descendant));
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
            newMatched.add(root.node());
            var parent = valueNode.getParent();
            newMatched.add(parent);
            while (parent != root) {
                parent = valueNode.getParent();
                newMatched.add(parent);
            }
        }
        return newMatched;
    }

    private List<ParseTree> getAllParents(List<ParseTree> nodes) {
        List<ParseTree> newMatched = nodes.stream()
            .map(ParseTree::getParent)
            .toList();
        return newMatched;
    }

    private List<ParseTree> getAllAncestorsOrSelf(List<ParseTree> nodes) {
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
            value = value.concatenate(visitedExpression);
            i++;
        }

        return value;
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
            value = value.or(visitedExpression);
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
            value = value.and(visitedExpression);
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
                case "+" -> value.add(visitedExpression);
                case "-" -> value.subtract(visitedExpression);
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
            case "=" -> value.generalEqual(visitedExpression);
            case "!=" -> value.generalUnequal(visitedExpression);
            case ">" -> value.generalGreaterThan(visitedExpression);
            case "<" -> value.generalLessThan(visitedExpression);
            case "<=" -> value.generalLessEqual(visitedExpression);
            case ">=" -> value.generalGreaterEqual(visitedExpression);
            default -> null;
        };
    }

    private XQueryValue handleValueComparison(final OrExprContext ctx) throws XQueryUnsupportedOperation {
        final var value = ctx.orExpr(0).accept(this);
        final var visitedExpression = ctx.orExpr(1).accept(this);
        return switch(ctx.valueComp().getText()) {
            case "eq" -> value.valueEqual(visitedExpression);
            case "ne" -> value.valueUnequal(visitedExpression);
            case "lt" -> value.valueLessThan(visitedExpression);
            case "gt" -> value.valueGreaterThan(visitedExpression);
            case "le" -> value.valueLessEqual(visitedExpression);
            case "ge" -> value.valueGreaterEqual(visitedExpression);
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
                case "*" -> value.multiply(visitedExpression);
                case "div" -> value.divide(visitedExpression);
                case "idiv" -> value.integerDivide(visitedExpression);
                case "mod" -> value.modulus(visitedExpression);
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
            value = value.union(visitedExpression);
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
            value = value.intersect(visitedExpression);
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
            value = value.except(visitedExpression);
            i++;
        }
        return value;
    }


    private XQueryValue handleUnaryArithmeticExpr(final OrExprContext ctx) throws XQueryUnsupportedOperation {
        final var value = ctx.orExpr(0).accept(this);
        if (!value.isNumericValue()) {
            // TODO: type error
        }
        return value.multiply(new XQueryNumber(new BigDecimal(-1)));
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
        matchedNodes = new XQuerySequence();
        return saved;
    }

    private XQueryAxis saveAxis() {
        final var saved = currentAxis;
        currentAxis = null;
        return saved;
    }

}
