package com.github.akruk.antlrxquery.evaluator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;
import com.github.akruk.antlrxquery.AntlrXqueryParserBaseVisitor;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ArgumentContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.FunctionCallContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.LiteralContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.OrExprContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ParenthesizedExprContext;
import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;
import com.github.akruk.antlrxquery.values.XQueryNumber;
import com.github.akruk.antlrxquery.values.XQuerySequence;
import com.github.akruk.antlrxquery.values.XQueryString;
import com.github.akruk.antlrxquery.values.XQueryValue;
import com.github.akruk.antlrxquery.values.XQueryBoolean;
import com.github.akruk.antlrxquery.values.XQueryFunction;

class XQueryEvaluatorVisitor extends AntlrXqueryParserBaseVisitor<XQueryValue> {
    ParseTree tree;
    Parser parser;

    List<XQueryValue> visitedArgumentList;
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
            return new XQueryNumber(arg.numericValue().negate());
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

        private static final XQueryValue roundHaftToEven(final List<XQueryValue> args) {
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
        this.tree = tree;
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
            if (ctx.MINUS() != null)
                return handleUnaryArithmeticExpr(ctx);


            return value;
        } catch (final XQueryUnsupportedOperation e) {
            // TODO: error handling
            return null;
        }
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

}
