package com.github.akruk.antlrquery.evaluator.functionmanager.functions;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

import com.github.akruk.antlrquery.evaluator.AntlrQueryVisitingContext;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryError;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;
import com.github.akruk.antlrquery.evaluator.values.factories.AntlrQueryValueFactory;

public class NumericOperators {
    private final AntlrQueryValueFactory valueFactory;
    public NumericOperators(final AntlrQueryValueFactory valueFactory) {
        this.valueFactory = valueFactory;
    }

    /**
     * op:numeric-add($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:numeric
     */
    public AntlrQueryValue numericAdd(
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args) {

        // Must have exactly 2 arguments
        if (args.size() != 2) {
            return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        }

        AntlrQueryValue a = args.get(0), b = args.get(1);

        // Type validation
        if (!a.isNumeric || !b.isNumeric) {
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        }

        BigDecimal result = a.numericValue.add(b.numericValue);
        return valueFactory.number(result);
    }

    /**
     * op:numeric-subtract($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:numeric
     */
    public AntlrQueryValue numericSubtract(
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args) {

        // Must have exactly 2 arguments
        if (args.size() != 2) {
            return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        }

        AntlrQueryValue a = args.get(0), b = args.get(1);

        // Type validation
        if (!a.isNumeric || !b.isNumeric) {
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        }

        BigDecimal result = a.numericValue.subtract(b.numericValue);
        return valueFactory.number(result);
    }



    /**
     * op:numeric-multiply($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:numeric
     */
    public AntlrQueryValue numericMultiply(
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args) {

        if (args.size() != 2) return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        AntlrQueryValue a = args.get(0), b = args.get(1);
        if (!a.isNumeric || !b.isNumeric) {
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        }

        BigDecimal result = a.numericValue.multiply(b.numericValue);
        return valueFactory.number(result);
    }

    /**
     * op:numeric-divide($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:numeric
     */
    public AntlrQueryValue numericDivide(
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args) {

        if (args.size() != 2) return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        AntlrQueryValue a = args.get(0), b = args.get(1);
        if (!a.isNumeric || !b.isNumeric) {
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        }

        BigDecimal divisor = b.numericValue;
        if (divisor.signum() == 0) {
            return valueFactory.error(AntlrQueryError.DivisionByZero, ""); // divide-by-zero
        }

        try {
            BigDecimal result = a.numericValue.divide(divisor, MathContext.DECIMAL128);
            return valueFactory.number(result);
        } catch (ArithmeticException ex) {
            return valueFactory.error(AntlrQueryError.NumericOverflowUnderflow, "");
        }
    }

    /**
     * op:numeric-integer-divide($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:integer
     */
    public AntlrQueryValue numericIntegerDivide(
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args) {

        if (args.size() != 2) return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        AntlrQueryValue a = args.get(0), b = args.get(1);
        if (!a.isNumeric || !b.isNumeric) {
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        }

        BigDecimal arg1 = a.numericValue;
        BigDecimal arg2 = b.numericValue;

        if (arg2.signum() == 0) {
            return valueFactory.error(AntlrQueryError.DivisionByZero, ""); // divide-by-zero
        }

        try {
            // round toward zero — truncate result
            BigDecimal result = arg1.divide(arg2, 0, RoundingMode.DOWN);
            return valueFactory.number(result);
        } catch (ArithmeticException ex) {
            return valueFactory.error(AntlrQueryError.NumericOverflowUnderflow, "");
        }
    }


    /**
     * op:numeric-mod($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:numeric
     */
    public AntlrQueryValue numericMod(
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args) {

        if (args.size() != 2) return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        AntlrQueryValue dividend = args.get(0), divisor = args.get(1);

        if (!dividend.isNumeric || !divisor.isNumeric) {
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        }

        BigDecimal a = dividend.numericValue;
        BigDecimal b = divisor.numericValue;

        if (b.signum() == 0) {
            return valueFactory.error(AntlrQueryError.DivisionByZero, "");
        }

        try {
            BigDecimal idiv = a.divide(b, 0, RoundingMode.DOWN);
            BigDecimal mod = a.subtract(idiv.multiply(b));
            return valueFactory.number(mod);
        } catch (ArithmeticException ex) {
            return valueFactory.error(AntlrQueryError.NumericOverflowUnderflow, "");
        }
    }

    /**
     * op:numeric-unary-plus($arg as xs:numeric) as xs:numeric
     */
    public AntlrQueryValue numericUnaryPlus(
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args) {

        if (args.size() != 1) return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        AntlrQueryValue v = args.get(0);

        if (!v.isNumeric) {
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        }

        return valueFactory.number(v.numericValue);
    }

    /**
     * op:numeric-unary-minus($arg as xs:numeric) as xs:numeric
     */
    public AntlrQueryValue numericUnaryMinus(
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args) {

        if (args.size() != 1) return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        AntlrQueryValue v = args.get(0);

        if (!v.isNumeric) {
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        }

        BigDecimal negated = v.numericValue.negate();
        return valueFactory.number(negated);
    }


    /**
     * op:numeric-equal($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:boolean
     */
    public AntlrQueryValue numericEqual(
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args) {

        if (args.size() != 2) return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        AntlrQueryValue a = args.get(0), b = args.get(1);
        if (!a.isNumeric || !b.isNumeric) {
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        }

        boolean result = a.numericValue.compareTo(b.numericValue) == 0;
        return valueFactory.bool(result);
    }

    /**
     * op:numeric-less-than($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:boolean
     */
    public AntlrQueryValue numericLessThan(
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args) {

        if (args.size() != 2) return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        AntlrQueryValue a = args.get(0), b = args.get(1);
        if (!a.isNumeric || !b.isNumeric) {
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        }

        boolean result = a.numericValue.compareTo(b.numericValue) < 0;
        return valueFactory.bool(result);
    }

    /**
     * op:numeric-less-than-or-equal($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:boolean
     */
    public AntlrQueryValue numericLessThanOrEqual(
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args) {

        if (args.size() != 2) return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        AntlrQueryValue a = args.get(0), b = args.get(1);
        if (!a.isNumeric || !b.isNumeric) {
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        }

        boolean result = a.numericValue.compareTo(b.numericValue) <= 0;
        return valueFactory.bool(result);
    }

    /**
     * op:numeric-greater-than($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:boolean
     */
    public AntlrQueryValue numericGreaterThan(
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args) {

        if (args.size() != 2) return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        AntlrQueryValue a = args.get(0), b = args.get(1);
        if (!a.isNumeric || !b.isNumeric) {
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        }

        boolean result = a.numericValue.compareTo(b.numericValue) > 0;
        return valueFactory.bool(result);
    }

    /**
     * op:numeric-greater-than-or-equal($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:boolean
     */
    public AntlrQueryValue numericGreaterThanOrEqual(
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args) {

        if (args.size() != 2) return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        AntlrQueryValue a = args.get(0), b = args.get(1);
        if (!a.isNumeric || !b.isNumeric) {
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        }

        boolean result = a.numericValue.compareTo(b.numericValue) >= 0;
        return valueFactory.bool(result);
    }

}
