package com.github.akruk.antlrxquery.semanticfunctiontests.thematic;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.semanticfunctiontests.FunctionsSemanticTest;

public class NumericOperatorsFunctionsTests extends FunctionsSemanticTest {

    // numeric operators helper
    public void assertNumericOp(String opName) {
        String call = "op:" + opName + "(2, 3)";
        assertType(call, typeFactory.number());
    }

    @Test
    public void numericAdd_valid() {
        assertNumericOp("numeric-add");
    }

    @Test
    public void numericAdd_wrong() {
        assertErrors("op:numeric-add(1,'x')");
    }

    @Test
    public void numericAdd_arity() {
        assertErrors("op:numeric-add(1)");
    }

    @Test
    public void numericSub_valid() {
        assertNumericOp("numeric-subtract");
    }

    @Test
    public void numericSub_wrong() {
        assertErrors("op:numeric-subtract('a',2)");
    }

    @Test
    public void numericSub_arity() {
        assertErrors("op:numeric-subtract()");
    }

    @Test
    public void numericMul_valid() {
        assertNumericOp("numeric-multiply");
    }

    @Test
    public void numericMul_wrong() {
        assertErrors("op:numeric-multiply(1,true())");
    }

    @Test
    public void numericDiv_valid() {
        assertNumericOp("numeric-divide");
    }

    @Test
    public void numericDiv_wrong() {
        assertErrors("op:numeric-divide(1,<x/>)");
    }

    @Test
    public void intDivide_valid() {
        assertType("op:numeric-integer-divide(5, 2)", typeFactory.number());
    }

    @Test
    public void intDivide_wrong() {
        assertErrors("op:numeric-integer-divide(1,'y')");
    }

    @Test
    public void mod_valid() {
        assertNumericOp("numeric-mod");
    }

    @Test
    public void mod_wrong() {
        assertErrors("op:numeric-mod('x',3)");
    }

    @Test
    public void unaryPlus_valid() {
        assertType("op:numeric-unary-plus(4)", typeFactory.number());
    }

    @Test
    public void unaryMinus_valid() {
        var r = analyze("op:numeric-unary-minus(4)");
        assertNoErrors(r);
    }

    @Test
    public void unary_wrongArity() {
        assertErrors("op:numeric-unary-plus()");
        assertErrors("op:numeric-unary-minus(1,2)");
    }

    @Test
    public void unary_badType() {
        assertErrors("op:numeric-unary-plus('x')");
    }

    // op:numeric-equal($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:boolean
    // @Test
    // public void numericEqual_valid() {
    //     assertType("op:numeric-equal(1, 2)", typeFactory.boolean_());
    // }

    @Test
    public void numericEqual_wrongType() {
        assertErrors("op:numeric-equal(1, 'x')");
    }

    @Test
    public void numericEqual_arity() {
        assertErrors("op:numeric-equal(1)");
        assertErrors("op:numeric-equal()");
        assertErrors("op:numeric-equal(1,2,3)");
    }

    // op:numeric-less-than($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:boolean
    @Test
    public void numericLessThan_valid() {
        assertType("op:numeric-less-than(1, 2)", typeFactory.boolean_());
    }

    @Test
    public void numericLessThan_wrongType() {
        assertErrors("op:numeric-less-than('a', 2)");
    }

    @Test
    public void numericLessThan_arity() {
        assertErrors("op:numeric-less-than(1)");
        assertErrors("op:numeric-less-than()");
    }

    // op:numeric-greater-than($arg1 as xs:numeric, $arg2 as xs:numeric) as
    // xs:boolean
    @Test
    public void numericGreaterThan_valid() {
        assertType("op:numeric-greater-than(1, 2)", typeFactory.boolean_());
    }

    @Test
    public void numericGreaterThan_wrongType() {
        assertErrors("op:numeric-greater-than(1, true())");
    }

    // op:numeric-less-than-or-equal($arg1 as xs:numeric, $arg2 as xs:numeric) as
    // xs:boolean
    @Test
    public void numericLessThanOrEq_valid() {
        assertType("op:numeric-less-than-or-equal(1, 2)", typeFactory.boolean_());
    }

    // op:numeric-greater-than-or-equal($arg1 as xs:numeric, $arg2 as xs:numeric) as
    // xs:boolean
    @Test
    public void numericGreaterThanOrEq_valid() {
        assertType("op:numeric-greater-than-or-equal(1, 2)", typeFactory.boolean_());
    }
}
