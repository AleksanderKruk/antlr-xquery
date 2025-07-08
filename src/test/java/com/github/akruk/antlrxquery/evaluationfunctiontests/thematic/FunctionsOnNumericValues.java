package com.github.akruk.antlrxquery.evaluationfunctiontests.thematic;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.evaluationfunctiontests.FunctionsEvaluationTests;

public class FunctionsOnNumericValues extends FunctionsEvaluationTests {
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
            baseFactory.number(new BigDecimal("3.0"))
        );
    }

    @Test
    public void roundDefaultHalfToCeiling_nonTie() {
        assertResult(
            "round(2.4999)",
            baseFactory.number(new BigDecimal("2.0"))
        );
    }

    @Test
    public void roundDefaultHalfToCeiling_negativeTie() {
        assertResult(
            "round(-2.5)",
            baseFactory.number(new BigDecimal("-2.0"))
        );
    }

    @Test
    public void roundWithPrecision_positive() {
        assertResult(
            "round(1.125, 2)",
            baseFactory.number(new BigDecimal("1.13"))
        );
    }

    @Test
    public void roundWithNegativePrecision() {
        assertResult(
            "round(8452, -2)",
            baseFactory.number(new BigDecimal("8500"))
        );
    }

    @Test
    public void roundScientificNotation() {
        assertResult(
            "round(3.1415e0, 2)",
            baseFactory.number(new BigDecimal("3.14"))
        );
    }

    @Test
    public void roundFloorMode_positive() {
        assertResult(
            "round(1.7, 0, \"floor\")",
            baseFactory.number(BigDecimal.ONE)
        );
    }

    @Test
    public void roundFloorMode_negative() {
        assertResult(
            "round(-1.7, 0, \"floor\")",
            baseFactory.number(new BigDecimal("-2"))
        );
    }

    @Test
    public void roundCeilingMode_positive() {
        assertResult(
            "round(1.7, 0, \"ceiling\")",
            baseFactory.number(new BigDecimal("2"))
        );
    }

    @Test
    public void roundCeilingMode_negative() {
        assertResult(
            "round(-1.7, 0, \"ceiling\")",
            baseFactory.number(new BigDecimal("-1"))
        );
    }

    @Test
    public void roundTowardZero_positive() {
        assertResult(
            "round(1.7, 0, \"toward-zero\")",
            baseFactory.number(BigDecimal.ONE)
        );
    }

    @Test
    public void roundTowardZero_negative() {
        assertResult(
            "round(-1.7, 0, \"toward-zero\")",
            baseFactory.number(new BigDecimal("-1"))
        );
    }

    @Test
    public void roundAwayFromZero_positive() {
        assertResult(
            "round(1.7, 0, \"away-from-zero\")",
            baseFactory.number(new BigDecimal("2"))
        );
    }

    @Test
    public void roundAwayFromZero_negative() {
        assertResult(
            "round(-1.7, 0, \"away-from-zero\")",
            baseFactory.number(new BigDecimal("-2"))
        );
    }

    @Test
    public void roundHalfToFloor_positive() {
        assertResult(
            "round(1.125, 2, \"half-to-floor\")",
            baseFactory.number(new BigDecimal("1.12"))
        );
    }

    @Test
    public void roundHalfToFloor_negative() {
        assertResult(
            "round(-1.125, 2, \"half-to-floor\")",
            baseFactory.number(new BigDecimal("-1.13"))
        );
    }

    @Test
    public void roundHalfToCeiling_positive() {
        assertResult(
            "round(1.125, 2, \"half-to-ceiling\")",
            baseFactory.number(new BigDecimal("1.13"))
        );
    }

    @Test
    public void roundHalfToCeiling_negative() {
        assertResult(
            "round(-1.125, 2, \"half-to-ceiling\")",
            baseFactory.number(new BigDecimal("-1.12"))
        );
    }

    @Test
    public void roundHalfTowardZero_positive() {
        assertResult(
            "round(1.125, 2, \"half-toward-zero\")",
            baseFactory.number(new BigDecimal("1.12"))
        );
    }

    @Test
    public void roundHalfTowardZero_negative() {
        assertResult(
            "round(-1.125, 2, \"half-toward-zero\")",
            baseFactory.number(new BigDecimal("-1.12"))
        );
    }

    @Test
    public void roundHalfAwayFromZero_positive() {
        assertResult(
            "round(1.125, 2, \"half-away-from-zero\")",
            baseFactory.number(new BigDecimal("1.13"))
        );
    }

    @Test
    public void roundHalfAwayFromZero_negative() {
        assertResult(
            "round(-1.125, 2, \"half-away-from-zero\")",
            baseFactory.number(new BigDecimal("-1.13"))
        );
    }

    @Test
    public void roundHalfToEven_positive() {
        assertResult(
            "round(1.125, 2, \"half-to-even\")",
            baseFactory.number(new BigDecimal("1.12"))
        );
    }

    @Test
    public void roundHalfToEven_negative() {
        assertResult(
            "round(-1.125, 2, \"half-to-even\")",
            baseFactory.number(new BigDecimal("-1.12"))
        );
    }


}
