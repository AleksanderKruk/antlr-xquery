package com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;
import com.github.akruk.antlrxquery.values.XQueryError;
import com.github.akruk.antlrxquery.values.XQueryValue;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

public class FunctionsOnNumericValues {
    private final XQueryValueFactory valueFactory;

    public FunctionsOnNumericValues(final XQueryValueFactory valueFactory) {
        this.valueFactory = valueFactory;
    }

    /**
     * fn:abs($value as xs:numeric?) as xs:numeric?
     */
    public XQueryValue abs(
            final XQueryVisitingContext context,
            final List<XQueryValue> args) {

        final XQueryValue v = args.get(0);
        // empty‐sequence → empty‐sequence
        if (v.isEmptySequence()) {
            return valueFactory.emptySequence();
        }
        if (!v.isNumericValue()) {
            return XQueryError.InvalidArgumentType;
        }

        // decimal or integer
        final BigDecimal bd = v.numericValue();
        final BigDecimal abs = bd.abs();
        return valueFactory.number(abs);
    }

    /**
     * fn:ceiling($value as xs:numeric?) as xs:numeric?
     */
    public XQueryValue ceiling(
            final XQueryVisitingContext context,
            final List<XQueryValue> args) {

        final XQueryValue v = args.get(0);
        if (v.isEmptySequence()) {
            return valueFactory.emptySequence();
        }
        if (!v.isNumericValue()) {
            return XQueryError.InvalidArgumentType;
        }

        // decimal or integer
        final BigDecimal bd = v.numericValue();
        // scale to 0 fractional digits, rounding up
        final BigDecimal r = bd.setScale(0, RoundingMode.CEILING);
        return valueFactory.number(r);
    }

    /**
     * fn:floor($value as xs:numeric?) as xs:numeric?
     */
    public XQueryValue floor(
            final XQueryVisitingContext context,
            final List<XQueryValue> args) {

        final XQueryValue v = args.get(0);
        if (v.isEmptySequence()) {
            return valueFactory.emptySequence();
        }
        if (!v.isNumericValue()) {
            return XQueryError.InvalidArgumentType;
        }

        final BigDecimal bd = v.numericValue();
        final BigDecimal r = bd.setScale(0, RoundingMode.FLOOR);
        return valueFactory.number(r);
    }

    public XQueryValue round(
            final XQueryVisitingContext context,
            final List<XQueryValue> args) {

        final XQueryValue v = args.get(0);
        if (v.isEmptySequence()) {
            return valueFactory.emptySequence();
        }
        if (!v.isNumericValue()) {
            return XQueryError.InvalidArgumentType;
        }

        int precision = 0;
        if (args.size() >= 2) {
            final XQueryValue p = args.get(1);
            if (p.isEmptySequence()) {
                precision = 0;
            } else if (!p.isNumericValue()) {
                return XQueryError.InvalidArgumentType;
            } else {
                precision = p.numericValue().intValue();
            }
        }

        String mode = "half-to-ceiling";
        if (args.size() == 3) {
            final XQueryValue m = args.get(2);
            if (!m.isEmptySequence()) {
                mode = m.stringValue();
            }
        }

        final BigDecimal bd = v.numericValue();
        BigDecimal rounded;
        try {
            if ("half-to-ceiling".equals(mode)) {
                final RoundingMode rm = bd.signum() < 0 ? RoundingMode.HALF_DOWN : RoundingMode.HALF_UP;
                rounded = bd.setScale(precision, rm);
            } else if ("half-to-floor".equals(mode)) {
                final RoundingMode rm = bd.signum() < 0 ? RoundingMode.HALF_UP : RoundingMode.HALF_DOWN;
                rounded = bd.setScale(precision, rm);
            } else {
                final RoundingMode rm;
                switch (mode) {
                    case "floor": rm = RoundingMode.FLOOR; break;
                    case "ceiling": rm = RoundingMode.CEILING; break;
                    case "toward-zero": rm = RoundingMode.DOWN; break;
                    case "away-from-zero": rm = RoundingMode.UP; break;
                    case "half-toward-zero": rm = RoundingMode.HALF_DOWN; break;
                    case "half-away-from-zero": rm = RoundingMode.HALF_UP; break;
                    case "half-to-even": rm = RoundingMode.HALF_EVEN; break;
                    default: return XQueryError.InvalidArgumentType;
                }
                rounded = bd.setScale(precision, rm);
            }
            return valueFactory.number(rounded);
        } catch (final ArithmeticException ex) {
            return XQueryError.NumericOverflowUnderflow;
        }
    }

    /**
     * fn:round-half-to-even(
     *   $value     as xs:numeric?,
     *   $precision as xs:integer? := 0
     * ) as xs:numeric?
     */
    public XQueryValue roundHalfToEven(
            final XQueryVisitingContext context,
            final List<XQueryValue> args) {

        // arity check
        if (args.size() < 1 || args.size() > 2) {
            return XQueryError.WrongNumberOfArguments;
        }

        final XQueryValue v = args.get(0);
        // empty-sequence → empty-sequence
        if (v.isEmptySequence()) {
            return valueFactory.emptySequence();
        }
        if (!v.isNumericValue()) {
            return XQueryError.InvalidArgumentType;
        }

        // precision (default 0)
        int precision = 0;
        if (args.size() == 2) {
            final XQueryValue p = args.get(1);
            if (p.isEmptySequence()) {
                precision = 0;
            } else if (!p.isNumericValue()) {
                return XQueryError.InvalidArgumentType;
            } else {
                precision = p.numericValue().intValue();
            }
        }

        // perform half-even rounding via BigDecimal
        final BigDecimal bd = v.numericValue();
        try {
            final BigDecimal rd = bd.setScale(precision, RoundingMode.HALF_EVEN);
            return valueFactory.number(rd);
        } catch (final ArithmeticException ex) {
            return XQueryError.NumericOverflowUnderflow;
        }
    }

    /**
     * fn:divide-decimals(
     *   $value     as xs:decimal,
     *   $divisor   as xs:decimal,
     *   $precision as xs:integer? := 0
     * ) as record(quotient as xs:decimal, remainder as xs:decimal)
     */
    public XQueryValue divideDecimals(
            final XQueryVisitingContext context,
            final List<XQueryValue> args)
    {

        final XQueryValue v1 = args.get(0), v2 = args.get(1);
        // must be decimals
        if (v1.isEmptySequence() || v2.isEmptySequence()
                || !v1.isNumericValue() || !v2.isNumericValue()) {
            return XQueryError.InvalidArgumentType;
        }

        final BigDecimal dividend = v1.numericValue();
        final BigDecimal divisor  = v2.numericValue();

        // division by zero
        if (BigDecimal.ZERO.compareTo(divisor) == 0) {
            return XQueryError.DivisionByZero;
        }

        // precision (default 0)
        int precision = 0;
        if (args.size() == 3) {
            final XQueryValue p = args.get(2);
            if (p.isEmptySequence()) {
                precision = 0;
            } else if (!p.isNumericValue()) {
                return XQueryError.InvalidArgumentType;
            } else {
                precision = p.numericValue().intValue();
            }
        }

        // compute quotient: |q| = |dividend/divisor| rounded DOWN at given scale
        final BigDecimal absQuotient = dividend
            .abs()
            .divide(divisor.abs(), precision, RoundingMode.DOWN);
        // restore sign of q
        final BigDecimal quotient = absQuotient
            .multiply(
                BigDecimal.valueOf(dividend.signum() * divisor.signum())
            );

        // compute exact remainder
        final BigDecimal remainder = dividend.subtract(quotient.multiply(divisor));

        // build record { "quotient":…, "remainder":… }
        final Map<String,XQueryValue> fields = new HashMap<>();
        fields.put("quotient", valueFactory.number(quotient));
        fields.put("remainder", valueFactory.number(remainder));
        return valueFactory.record(fields);
    }

}
