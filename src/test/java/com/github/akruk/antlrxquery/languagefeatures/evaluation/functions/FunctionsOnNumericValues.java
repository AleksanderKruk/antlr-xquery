package com.github.akruk.antlrxquery.languagefeatures.evaluation.functions;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.evaluator.values.XQueryError;
import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;

public class FunctionsOnNumericValues extends EvaluationTestsBase {
    @Test
    public void abs() {
        assertResult("abs(3)", BigDecimal.valueOf(3));
        assertResult("abs(-3)", BigDecimal.valueOf(3));
    }

    @Test
    public void ceiling() {
        assertResult("ceiling(3.3)", BigDecimal.valueOf(4));
    }

    @Test
    public void floor() {
        assertResult("floor(3.3)", BigDecimal.valueOf(3));
    }

    @Test
    public void round() {
        // From https://www.w3.org/TR/xpath-functions-3/#func-round
        assertResult("round(3.3)", BigDecimal.valueOf(3));
        assertResult("round(3.5)", BigDecimal.valueOf(4));
        assertResult("round(-2.5)", BigDecimal.valueOf(-2));
        assertResult("round(1.125, 2)", new BigDecimal("1.13"));
        assertResult("round(8452, -2)", new BigDecimal("8500"));
    }

        @Test
    public void roundDefaultHalfToCeiling_positiveTie() {
        assertResult(
            "round(2.5)",
            valueFactory.number(new BigDecimal("3.0"))
        );
    }

    @Test
    public void roundDefaultHalfToCeiling_nonTie() {
        assertResult(
            "round(2.4999)",
            valueFactory.number(new BigDecimal("2.0"))
        );
    }

    @Test
    public void roundDefaultHalfToCeiling_negativeTie() {
        assertResult(
            "round(-2.5)",
            valueFactory.number(new BigDecimal("-2.0"))
        );
    }

    @Test
    public void roundWithPrecision_positive() {
        assertResult(
            "round(1.125, 2)",
            valueFactory.number(new BigDecimal("1.13"))
        );
    }

    @Test
    public void roundWithNegativePrecision() {
        assertResult(
            "round(8452, -2)",
            valueFactory.number(new BigDecimal("8500"))
        );
    }

    @Test
    public void roundScientificNotation() {
        assertResult(
            "round(3.1415e0, 2)",
            valueFactory.number(new BigDecimal("3.14"))
        );
    }

    @Test
    public void roundFloorMode_positive() {
        assertResult(
            "round(1.7, 0, \"floor\")",
            valueFactory.number(BigDecimal.ONE)
        );
    }

    @Test
    public void roundFloorMode_negative() {
        assertResult(
            "round(-1.7, 0, \"floor\")",
            valueFactory.number(new BigDecimal("-2"))
        );
    }

    @Test
    public void roundCeilingMode_positive() {
        assertResult(
            "round(1.7, 0, \"ceiling\")",
            valueFactory.number(new BigDecimal("2"))
        );
    }

    @Test
    public void roundCeilingMode_negative() {
        assertResult(
            "round(-1.7, 0, \"ceiling\")",
            valueFactory.number(new BigDecimal("-1"))
        );
    }

    @Test
    public void roundTowardZero_positive() {
        assertResult(
            "round(1.7, 0, \"toward-zero\")",
            valueFactory.number(BigDecimal.ONE)
        );
    }

    @Test
    public void roundTowardZero_negative() {
        assertResult(
            "round(-1.7, 0, \"toward-zero\")",
            valueFactory.number(new BigDecimal("-1"))
        );
    }

    @Test
    public void roundAwayFromZero_positive() {
        assertResult(
            "round(1.7, 0, \"away-from-zero\")",
            valueFactory.number(new BigDecimal("2"))
        );
    }

    @Test
    public void roundAwayFromZero_negative() {
        assertResult(
            "round(-1.7, 0, \"away-from-zero\")",
            valueFactory.number(new BigDecimal("-2"))
        );
    }

    @Test
    public void roundHalfToFloor_positive() {
        assertResult(
            "round(1.125, 2, \"half-to-floor\")",
            valueFactory.number(new BigDecimal("1.12"))
        );
    }

    @Test
    public void roundHalfToFloor_negative() {
        assertResult(
            "round(-1.125, 2, \"half-to-floor\")",
            valueFactory.number(new BigDecimal("-1.13"))
        );
    }

    @Test
    public void roundHalfToCeiling_positive() {
        assertResult(
            "round(1.125, 2, \"half-to-ceiling\")",
            valueFactory.number(new BigDecimal("1.13"))
        );
    }

    @Test
    public void roundHalfToCeiling_negative() {
        assertResult(
            "round(-1.125, 2, \"half-to-ceiling\")",
            valueFactory.number(new BigDecimal("-1.12"))
        );
    }

    @Test
    public void roundHalfTowardZero_positive() {
        assertResult(
            "round(1.125, 2, \"half-toward-zero\")",
            valueFactory.number(new BigDecimal("1.12"))
        );
    }

    @Test
    public void roundHalfTowardZero_negative() {
        assertResult(
            "round(-1.125, 2, \"half-toward-zero\")",
            valueFactory.number(new BigDecimal("-1.12"))
        );
    }

    @Test
    public void roundHalfAwayFromZero_positive() {
        assertResult(
            "round(1.125, 2, \"half-away-from-zero\")",
            valueFactory.number(new BigDecimal("1.13"))
        );
    }

    @Test
    public void roundHalfAwayFromZero_negative() {
        assertResult(
            "round(-1.125, 2, \"half-away-from-zero\")",
            valueFactory.number(new BigDecimal("-1.13"))
        );
    }

    @Test
    public void roundHalfToEven_positive() {
        assertResult(
            "round(1.125, 2, \"half-to-even\")",
            valueFactory.number(new BigDecimal("1.12"))
        );
    }

    @Test
    public void roundHalfToEven_negative() {
        assertResult(
            "round(-1.125, 2, \"half-to-even\")",
            valueFactory.number(new BigDecimal("-1.12"))
        );
    }

    @Test
    public void dividesExact() {
        assertResult(
            "divide-decimals(120.6, 60.3, 4)",
            valueFactory.record(Map.of(
                "quotient", valueFactory.number(2),
                "remainder", valueFactory.number(0)
            ))
        );
    }

    @Test
    public void dividesPositive() {
        assertResult(
            "divide-decimals(10, 3)",
            valueFactory.record(Map.of(
                "quotient", valueFactory.number(3),
                "remainder", valueFactory.number(1)
            ))
        );
    }

    @Test
    public void dividesPositiveNegativeDivisor() {
        assertResult(
            "divide-decimals(10, -3)",
            valueFactory.record(Map.of(
                "quotient", valueFactory.number(-3),
                "remainder", valueFactory.number(1)
            ))
        );
    }

    @Test
    public void dividesNegativePositiveDivisor() {
        assertResult(
            "divide-decimals(-10, 3)",
            valueFactory.record(Map.of(
                "quotient", valueFactory.number(-3),
                "remainder", valueFactory.number(-1)
            ))
        );
    }

    @Test
    public void dividesBothNegative() {
        assertResult(
            "divide-decimals(-10, -3)",
            valueFactory.record(Map.of(
                "quotient", valueFactory.number(3),
                "remainder", valueFactory.number(-1)
            ))
        );
    }

    @Test
    public void dividesWithPrecisionSix() {
        assertResult(
            "divide-decimals(10, 3, 6)",
            valueFactory.record(Map.of(
                "quotient", valueFactory.number(new BigDecimal("3.333333")),
                "remainder", valueFactory.number(new BigDecimal("0.000001"))
            ))
        );
    }

    @Test
    public void dividesHundredThirty() {
        assertResult(
            "divide-decimals(100, 30)",
            valueFactory.record(Map.of(
                "quotient", valueFactory.number(3),
                "remainder", valueFactory.number(10)
            ))
        );
    }

    @Test
    public void dividesWithNegativePrecision() {
        assertResult(
            "divide-decimals(150862, 7, -3)",
            valueFactory.record(Map.of(
                "quotient", valueFactory.number(21000),
                "remainder", valueFactory.number(3862)
            ))
        );
    }

    @Test
    public void divideByZeroRaisesError() {
        assertError("divide-decimals(1, 0)", valueFactory.error(XQueryError.DivisionByZero, ""));
    }



}
