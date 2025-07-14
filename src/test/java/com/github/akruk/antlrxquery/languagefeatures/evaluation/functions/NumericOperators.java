package com.github.akruk.antlrxquery.languagefeatures.evaluation.functions;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;

public class NumericOperators extends EvaluationTestsBase {
    @Test
    public void numericAdd() {
        assertResult("op:numeric-add(3, 5)", BigDecimal.valueOf(8));
    }

    @Test
    public void numericSubtract() {
        assertResult("op:numeric-subtract(3, 5)", BigDecimal.valueOf(-2));
    }

    @Test
    public void numericMultiply() {
        assertResult("op:numeric-multiply(3, 5)", BigDecimal.valueOf(15));
    }

    @Test
    public void numericDivide() {
        assertResult("op:numeric-divide(5, 5)", BigDecimal.ONE);
    }

    @Test
    public void numericIntegerDivide() {
        assertResult("op:numeric-integer-divide(5, 2)", BigDecimal.valueOf(2));
    }

    @Test
    public void numericMod() {
        assertResult("op:numeric-mod(5, 2)", BigDecimal.ONE);
    }

    @Test
    public void integerDivide_10_3() {
        assertResult("op:numeric-integer-divide(10, 3)", baseFactory.number(3));
    }

    @Test
    public void integerDivide_3_neg2() {
        assertResult("op:numeric-integer-divide(3, -2)", baseFactory.number(-1));
    }

    @Test
    public void integerDivide_neg3_2() {
        assertResult("op:numeric-integer-divide(-3, 2)", baseFactory.number(-1));
    }

    @Test
    public void integerDivide_neg3_neg2() {
        assertResult("op:numeric-integer-divide(-3, -2)", baseFactory.number(1));
    }

    @Test
    public void integerDivide_9_3_decimal() {
        assertResult("op:numeric-integer-divide(9.0, 3)", baseFactory.number(3));
    }

    @Test
    public void integerDivide_neg35_3() {
        assertResult("op:numeric-integer-divide(-3.5, 3)", baseFactory.number(-1));
    }

    @Test
    public void integerDivide_3_4() {
        assertResult("op:numeric-integer-divide(3.0, 4)", baseFactory.number(0));
    }

    @Test
    public void integerDivide_scientific_31e1_6() {
        assertResult("op:numeric-integer-divide(3.1E1, 6)", baseFactory.number(5));
    }

    @Test
    public void integerDivide_scientific_31e1_7() {
        assertResult("op:numeric-integer-divide(3.1E1, 7)", baseFactory.number(4));
    }

    @Test
    public void numericEqual_shouldBeTrue() {
        assertResult("op:numeric-equal(5, 5)", baseFactory.bool(true));
    }

    @Test
    public void numericEqual_shouldBeFalse() {
        assertResult("op:numeric-equal(5, 6)", baseFactory.bool(false));
    }

    @Test
    public void numericLessThan_trueCase() {
        assertResult("op:numeric-less-than(4, 5)", baseFactory.bool(true));
    }

    @Test
    public void numericLessThan_falseCase() {
        assertResult("op:numeric-less-than(5, 4)", baseFactory.bool(false));
    }

    @Test
    public void numericLessThanOrEqual_trueWhenEqual() {
        assertResult("op:numeric-less-than-or-equal(5, 5)", baseFactory.bool(true));
    }

    @Test
    public void numericLessThanOrEqual_trueWhenLess() {
        assertResult("op:numeric-less-than-or-equal(4, 5)", baseFactory.bool(true));
    }

    @Test
    public void numericLessThanOrEqual_falseWhenGreater() {
        assertResult("op:numeric-less-than-or-equal(6, 5)", baseFactory.bool(false));
    }

    @Test
    public void numericGreaterThan_trueCase() {
        assertResult("op:numeric-greater-than(6, 5)", baseFactory.bool(true));
    }

    @Test
    public void numericGreaterThan_falseCase() {
        assertResult("op:numeric-greater-than(5, 6)", baseFactory.bool(false));
    }

    @Test
    public void numericGreaterThanOrEqual_trueWhenEqual() {
        assertResult("op:numeric-greater-than-or-equal(5, 5)", baseFactory.bool(true));
    }

    @Test
    public void numericGreaterThanOrEqual_trueWhenGreater() {
        assertResult("op:numeric-greater-than-or-equal(6, 5)", baseFactory.bool(true));
    }

    @Test
    public void numericGreaterThanOrEqual_falseWhenLess() {
        assertResult("op:numeric-greater-than-or-equal(4, 5)", baseFactory.bool(false));
    }


}
