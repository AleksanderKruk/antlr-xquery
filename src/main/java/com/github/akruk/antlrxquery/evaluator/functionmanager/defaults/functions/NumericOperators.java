package com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;
import com.github.akruk.antlrxquery.values.XQueryError;
import com.github.akruk.antlrxquery.values.XQueryValue;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

public class NumericOperators {
    private final XQueryValueFactory valueFactory;
    public NumericOperators(final XQueryValueFactory valueFactory) {
        this.valueFactory = valueFactory;
    }

    /**
     * op:numeric-add($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:numeric
     */
    public XQueryValue numericAdd(
            XQueryVisitingContext context,
            List<XQueryValue> args,
            Map<String, XQueryValue> kwargs) {

        // Must have exactly 2 arguments
        if (args.size() != 2) {
            return XQueryError.WrongNumberOfArguments;
        }

        XQueryValue a = args.get(0), b = args.get(1);

        // Type validation
        if (!a.isNumericValue() || !b.isNumericValue()) {
            return XQueryError.InvalidArgumentType;
        }

        BigDecimal result = a.numericValue().add(b.numericValue());
        return valueFactory.number(result);
    }

    /**
     * op:numeric-subtract($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:numeric
     */
    public XQueryValue numericSubtract(
            XQueryVisitingContext context,
            List<XQueryValue> args,
            Map<String, XQueryValue> kwargs) {

        // Must have exactly 2 arguments
        if (args.size() != 2) {
            return XQueryError.WrongNumberOfArguments;
        }

        XQueryValue a = args.get(0), b = args.get(1);

        // Type validation
        if (!a.isNumericValue() || !b.isNumericValue()) {
            return XQueryError.InvalidArgumentType;
        }

        BigDecimal result = a.numericValue().subtract(b.numericValue());
        return valueFactory.number(result);
    }



    /**
     * op:numeric-multiply($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:numeric
     */
    public XQueryValue numericMultiply(
            XQueryVisitingContext context,
            List<XQueryValue> args,
            Map<String, XQueryValue> kwargs) {

        if (args.size() != 2) return XQueryError.WrongNumberOfArguments;
        XQueryValue a = args.get(0), b = args.get(1);
        if (!a.isNumericValue() || !b.isNumericValue()) {
            return XQueryError.InvalidArgumentType;
        }

        BigDecimal result = a.numericValue().multiply(b.numericValue());
        return valueFactory.number(result);
    }

    /**
     * op:numeric-divide($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:numeric
     */
    public XQueryValue numericDivide(
            XQueryVisitingContext context,
            List<XQueryValue> args,
            Map<String, XQueryValue> kwargs) {

        if (args.size() != 2) return XQueryError.WrongNumberOfArguments;
        XQueryValue a = args.get(0), b = args.get(1);
        if (!a.isNumericValue() || !b.isNumericValue()) {
            return XQueryError.InvalidArgumentType;
        }

        BigDecimal divisor = b.numericValue();
        if (divisor.signum() == 0) {
            return XQueryError.DivisionByZero; // divide-by-zero
        }

        try {
            BigDecimal result = a.numericValue().divide(divisor, MathContext.DECIMAL128);
            return valueFactory.number(result);
        } catch (ArithmeticException ex) {
            return XQueryError.NumericOverflowUnderflow;
        }
    }

    /**
     * op:numeric-integer-divide($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:integer
     */
    public XQueryValue numericIntegerDivide(
            XQueryVisitingContext context,
            List<XQueryValue> args,
            Map<String, XQueryValue> kwargs) {

        if (args.size() != 2) return XQueryError.WrongNumberOfArguments;
        XQueryValue a = args.get(0), b = args.get(1);
        if (!a.isNumericValue() || !b.isNumericValue()) {
            return XQueryError.InvalidArgumentType;
        }

        BigDecimal arg1 = a.numericValue();
        BigDecimal arg2 = b.numericValue();

        if (arg2.signum() == 0) {
            return XQueryError.DivisionByZero; // divide-by-zero
        }

        try {
            // round toward zero — truncate result
            BigDecimal result = arg1.divide(arg2, 0, RoundingMode.DOWN);
            return valueFactory.number(result);
        } catch (ArithmeticException ex) {
            return XQueryError.NumericOverflowUnderflow;
        }
    }


    /**
     * op:numeric-mod($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:numeric
     */
    public XQueryValue numericMod(
            XQueryVisitingContext context,
            List<XQueryValue> args,
            Map<String, XQueryValue> kwargs) {

        if (args.size() != 2) return XQueryError.WrongNumberOfArguments;
        XQueryValue dividend = args.get(0), divisor = args.get(1);

        if (!dividend.isNumericValue() || !divisor.isNumericValue()) {
            return XQueryError.InvalidArgumentType;
        }

        BigDecimal a = dividend.numericValue();
        BigDecimal b = divisor.numericValue();

        if (b.signum() == 0) {
            return XQueryError.DivisionByZero;
        }

        try {
            BigDecimal idiv = a.divide(b, 0, RoundingMode.DOWN);
            BigDecimal mod = a.subtract(idiv.multiply(b));
            return valueFactory.number(mod);
        } catch (ArithmeticException ex) {
            return XQueryError.NumericOverflowUnderflow;
        }
    }

    /**
     * op:numeric-unary-plus($arg as xs:numeric) as xs:numeric
     */
    public XQueryValue numericUnaryPlus(
            XQueryVisitingContext context,
            List<XQueryValue> args,
            Map<String, XQueryValue> kwargs) {

        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        XQueryValue v = args.get(0);

        if (!v.isNumericValue()) {
            return XQueryError.InvalidArgumentType;
        }

        return valueFactory.number(v.numericValue());
    }

    /**
     * op:numeric-unary-minus($arg as xs:numeric) as xs:numeric
     */
    public XQueryValue numericUnaryMinus(
            XQueryVisitingContext context,
            List<XQueryValue> args,
            Map<String, XQueryValue> kwargs) {

        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        XQueryValue v = args.get(0);

        if (!v.isNumericValue()) {
            return XQueryError.InvalidArgumentType;
        }

        BigDecimal negated = v.numericValue().negate();
        return valueFactory.number(negated);
    }


    /**
     * op:numeric-equal($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:boolean
     */
    public XQueryValue numericEqual(
            XQueryVisitingContext context,
            List<XQueryValue> args,
            Map<String, XQueryValue> kwargs) {

        if (args.size() != 2) return XQueryError.WrongNumberOfArguments;
        XQueryValue a = args.get(0), b = args.get(1);
        if (!a.isNumericValue() || !b.isNumericValue()) {
            return XQueryError.InvalidArgumentType;
        }

        boolean result = a.numericValue().compareTo(b.numericValue()) == 0;
        return valueFactory.bool(result);
    }

    /**
     * op:numeric-less-than($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:boolean
     */
    public XQueryValue numericLessThan(
            XQueryVisitingContext context,
            List<XQueryValue> args,
            Map<String, XQueryValue> kwargs) {

        if (args.size() != 2) return XQueryError.WrongNumberOfArguments;
        XQueryValue a = args.get(0), b = args.get(1);
        if (!a.isNumericValue() || !b.isNumericValue()) {
            return XQueryError.InvalidArgumentType;
        }

        boolean result = a.numericValue().compareTo(b.numericValue()) < 0;
        return valueFactory.bool(result);
    }

    /**
     * op:numeric-less-than-or-equal($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:boolean
     */
    public XQueryValue numericLessThanOrEqual(
            XQueryVisitingContext context,
            List<XQueryValue> args,
            Map<String, XQueryValue> kwargs) {

        if (args.size() != 2) return XQueryError.WrongNumberOfArguments;
        XQueryValue a = args.get(0), b = args.get(1);
        if (!a.isNumericValue() || !b.isNumericValue()) {
            return XQueryError.InvalidArgumentType;
        }

        boolean result = a.numericValue().compareTo(b.numericValue()) <= 0;
        return valueFactory.bool(result);
    }

    /**
     * op:numeric-greater-than($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:boolean
     */
    public XQueryValue numericGreaterThan(
            XQueryVisitingContext context,
            List<XQueryValue> args,
            Map<String, XQueryValue> kwargs) {

        if (args.size() != 2) return XQueryError.WrongNumberOfArguments;
        XQueryValue a = args.get(0), b = args.get(1);
        if (!a.isNumericValue() || !b.isNumericValue()) {
            return XQueryError.InvalidArgumentType;
        }

        boolean result = a.numericValue().compareTo(b.numericValue()) > 0;
        return valueFactory.bool(result);
    }

    /**
     * op:numeric-greater-than-or-equal($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:boolean
     */
    public XQueryValue numericGreaterThanOrEqual(
            XQueryVisitingContext context,
            List<XQueryValue> args,
            Map<String, XQueryValue> kwargs) {

        if (args.size() != 2) return XQueryError.WrongNumberOfArguments;
        XQueryValue a = args.get(0), b = args.get(1);
        if (!a.isNumericValue() || !b.isNumericValue()) {
            return XQueryError.InvalidArgumentType;
        }

        boolean result = a.numericValue().compareTo(b.numericValue()) >= 0;
        return valueFactory.bool(result);
    }

}
