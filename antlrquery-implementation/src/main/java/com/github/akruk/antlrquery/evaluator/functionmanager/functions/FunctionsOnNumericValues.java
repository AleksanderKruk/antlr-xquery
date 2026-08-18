package com.github.akruk.antlrquery.evaluator.functionmanager.functions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.akruk.antlrquery.evaluator.AntlrQueryVisitingContext;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryError;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;
import com.github.akruk.antlrquery.evaluator.values.factories.AntlrQueryValueFactory;

public class FunctionsOnNumericValues {
    private final AntlrQueryValueFactory valueFactory;

    public FunctionsOnNumericValues(final AntlrQueryValueFactory valueFactory) {
        this.valueFactory = valueFactory;
    }

    /**
     * fn:abs($value as xs:numeric?) as xs:numeric?
     */
    public AntlrQueryValue abs(
            final AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args) {

        final AntlrQueryValue v = args.getFirst();
        // empty‐sequence → empty‐sequence
        if (v.isEmptySequence) {
            return valueFactory.emptySequence();
        }
        if (!v.isNumeric) {
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        }

        // decimal or integer
        final BigDecimal bd = v.numericValue;
        final BigDecimal abs = bd.abs();
        return valueFactory.number(abs);
    }

    /**
     * fn:ceiling($value as xs:numeric?) as xs:numeric?
     */
    public AntlrQueryValue ceiling(
            final AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args) {

        final AntlrQueryValue v = args.getFirst();
        if (v.isEmptySequence) {
            return valueFactory.emptySequence();
        }
        if (!v.isNumeric) {
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        }

        // decimal or integer
        final BigDecimal bd = v.numericValue;
        // scale to 0 fractional digits, rounding up
        final BigDecimal r = bd.setScale(0, RoundingMode.CEILING);
        return valueFactory.number(r);
    }

    /**
     * fn:floor($value as xs:numeric?) as xs:numeric?
     */
    public AntlrQueryValue floor(
            final AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args) {

        final AntlrQueryValue v = args.getFirst();
        if (v.isEmptySequence) {
            return valueFactory.emptySequence();
        }
        if (!v.isNumeric) {
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        }

        final BigDecimal bd = v.numericValue;
        final BigDecimal r = bd.setScale(0, RoundingMode.FLOOR);
        return valueFactory.number(r);
    }

    public AntlrQueryValue round(
            final AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args) {

        final AntlrQueryValue v = args.getFirst();
        if (v.isEmptySequence) {
            return valueFactory.emptySequence();
        }
        if (!v.isNumeric) {
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        }

        int precision = 0;
        if (args.size() >= 2) {
            final AntlrQueryValue p = args.get(1);
            if (p.isEmptySequence) {
                precision = 0;
            } else if (!p.isNumeric) {
                return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
            } else {
                precision = p.numericValue.intValue();
            }
        }

        String mode = "half-to-ceiling";
        if (args.size() == 3) {
            final AntlrQueryValue m = args.get(2);
            if (!m.isEmptySequence) {
                mode = m.stringValue;
            }
        }

        final BigDecimal bd = v.numericValue;
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
                    default:
                        return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
                }
                rounded = bd.setScale(precision, rm);
            }
            return valueFactory.number(rounded);
        } catch (final ArithmeticException ex) {
            return valueFactory.error(AntlrQueryError.NumericOverflowUnderflow, "");
        }
    }

    /**
     * fn:round-half-to-even(
     *   $value     as xs:numeric?,
     *   $precision as xs:integer? := 0
     * ) as xs:numeric?
     */
    public AntlrQueryValue roundHalfToEven(
            final AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args) {

        // arity check
        if (args.isEmpty() || args.size() > 2) {
            return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        }

        final AntlrQueryValue v = args.getFirst();
        // empty-sequence → empty-sequence
        if (v.isEmptySequence) {
            return valueFactory.emptySequence();
        }
        if (!v.isNumeric) {
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        }

        // precision (default 0)
        int precision = 0;
        if (args.size() == 2) {
            final AntlrQueryValue p = args.get(1);
            if (p.isEmptySequence) {
                precision = 0;
            } else if (!p.isNumeric) {
                return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
            } else {
                precision = p.numericValue.intValue();
            }
        }

        // perform half-even rounding via BigDecimal
        final BigDecimal bd = v.numericValue;
        try {
            final BigDecimal rd = bd.setScale(precision, RoundingMode.HALF_EVEN);
            return valueFactory.number(rd);
        } catch (final ArithmeticException ex) {
            return valueFactory.error(AntlrQueryError.NumericOverflowUnderflow, "");
        }
    }

    /**
     * fn:divide-decimals(
     *   $value     as xs:decimal,
     *   $divisor   as xs:decimal,
     *   $precision as xs:integer? := 0
     * ) as record(quotient as xs:decimal, remainder as xs:decimal)
     */
    public AntlrQueryValue divideDecimals(
            final AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args)
    {

        final AntlrQueryValue v1 = args.get(0), v2 = args.get(1);
        // must be decimals
        if (v1.isEmptySequence || v2.isEmptySequence
                || !v1.isNumeric || !v2.isNumeric) {
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        }

        final BigDecimal dividend = v1.numericValue;
        final BigDecimal divisor  = v2.numericValue;

        // division by zero
        if (BigDecimal.ZERO.compareTo(divisor) == 0) {
            return valueFactory.error(AntlrQueryError.DivisionByZero, "");
        }

        // precision (default 0)
        int precision = 0;
        if (args.size() == 3) {
            final AntlrQueryValue p = args.get(2);
            if (p.isEmptySequence) {
                precision = 0;
            } else if (!p.isNumeric) {
                return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
            } else {
                precision = p.numericValue.intValue();
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
        final Map<String, AntlrQueryValue> fields = new HashMap<>();
        fields.put("quotient", valueFactory.number(quotient));
        fields.put("remainder", valueFactory.number(remainder));
        return valueFactory.record(fields);
    }

}
