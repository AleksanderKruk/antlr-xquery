package com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
            XQueryVisitingContext context,
            List<XQueryValue> args,
            Map<String,XQueryValue> kwargs) {

        XQueryValue v = args.get(0);
        // empty‐sequence → empty‐sequence
        if (v.isEmptySequence()) {
            return valueFactory.emptySequence();
        }
        if (!v.isNumericValue()) {
            return XQueryError.InvalidArgumentType;
        }

        // decimal or integer
        BigDecimal bd = v.numericValue();
        BigDecimal abs = bd.abs();
        return valueFactory.number(abs);
    }

    /**
     * fn:ceiling($value as xs:numeric?) as xs:numeric?
     */
    public XQueryValue ceiling(
            XQueryVisitingContext context,
            List<XQueryValue> args,
            Map<String,XQueryValue> kwargs) {

        XQueryValue v = args.get(0);
        if (v.isEmptySequence()) {
            return valueFactory.emptySequence();
        }
        if (!v.isNumericValue()) {
            return XQueryError.InvalidArgumentType;
        }

        // decimal or integer
        BigDecimal bd = v.numericValue();
        // scale to 0 fractional digits, rounding up
        BigDecimal r = bd.setScale(0, RoundingMode.CEILING);
        return valueFactory.number(r);
    }

    /**
     * fn:floor($value as xs:numeric?) as xs:numeric?
     */
    public XQueryValue floor(
            XQueryVisitingContext context,
            List<XQueryValue> args,
            Map<String,XQueryValue> kwargs) {

        XQueryValue v = args.get(0);
        if (v.isEmptySequence()) {
            return valueFactory.emptySequence();
        }
        if (!v.isNumericValue()) {
            return XQueryError.InvalidArgumentType;
        }

        BigDecimal bd = v.numericValue();
        BigDecimal r = bd.setScale(0, RoundingMode.FLOOR);
        return valueFactory.number(r);
    }

    /**
     * fn:round(
     *   $value     as xs:numeric?,
     *   $precision as xs:integer?  := 0,
     *   $mode      as xs:string?   := "half-to-ceiling"
     * ) as xs:numeric?
     */
    public XQueryValue round(
            XQueryVisitingContext context,
            List<XQueryValue> args,
            Map<String, XQueryValue> kwargs) {

        XQueryValue v = args.get(0);
        // empty-sequence → empty-sequence
        if (v.isEmptySequence()) {
            return valueFactory.emptySequence();
        }
        if (!v.isNumericValue()) {
            return XQueryError.InvalidArgumentType;
        }

        // Determine precision (default 0)
        int precision = 0;
        if (args.size() >= 2) {
            XQueryValue p = args.get(1);
            if (p.isEmptySequence()) {
                precision = 0;
            } else if (!p.isNumericValue()) {
                return XQueryError.InvalidArgumentType;
            } else {
                precision = p.numericValue().intValue();
            }
        }

        // Determine rounding mode (default "half-to-ceiling")
        String mode = "half-to-ceiling";
        if (args.size() == 3) {
            XQueryValue m = args.get(2);
            if (!m.isEmptySequence()) {
                mode = m.stringValue();
            }
        }



        // Map mode string to java.math.RoundingMode
        final RoundingMode rm;
        switch (mode) {
            case "floor":              rm = RoundingMode.FLOOR;           break;
            case "ceiling":            rm = RoundingMode.CEILING;         break;
            case "toward-zero":        rm = RoundingMode.DOWN;            break;
            case "away-from-zero":     rm = RoundingMode.UP;              break;
            case "half-to-floor":      rm = RoundingMode.HALF_DOWN;       break;
            case "half-to-ceiling":    rm = RoundingMode.HALF_UP;         break;
            case "half-toward-zero":   rm = RoundingMode.HALF_DOWN;       break;
            case "half-away-from-zero":rm = RoundingMode.HALF_UP;         break;
            case "half-to-even":       rm = RoundingMode.HALF_EVEN;       break;
            default:
                return XQueryError.InvalidArgumentType;
        }

        BigDecimal bd = v.numericValue();
        try {
            BigDecimal rounded;
            if ("half-to-ceiling".equals(mode)) {
            if (bd.signum() < 0) {
                rounded = bd.setScale(precision, RoundingMode.HALF_DOWN);
            } else {
                rounded = bd.setScale(precision, RoundingMode.HALF_UP);
            }
            } else {
                rounded = bd.setScale(precision, rm);
            }
            return valueFactory.number(rounded);
        } catch (ArithmeticException ex) {
            return XQueryError.NumericOverflowUnderflow;
        }
    }

    public XQueryValue divideDecimals(XQueryVisitingContext context, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        return null;
    }

    public XQueryValue roundHalfToEven(XQueryVisitingContext context, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        return null;
    }

    public XQueryValue isNaN(XQueryVisitingContext context, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        return null;
    }




}
