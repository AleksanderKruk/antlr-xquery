
package com.github.akruk.antlrxquery.evaluationfunctiontests.thematic;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.evaluationfunctiontests.FunctionsEvaluationTests;

import static java.lang.Math.*;

public class TrigonometricAndExponentialFunctions extends FunctionsEvaluationTests  {

    @Test
    public void pi() {
        assertResult("math:pi()", BigDecimal.valueOf(Math.PI));
    }

    @Test
    public void sinFunction() {
        assertResult("math:sin(0)", BigDecimal.valueOf(Math.sin(0)));
        assertResult("math:sin(3.141592653589793 div 2)", BigDecimal.valueOf(sin(PI / 2)));
    }

    @Test
    public void cosFunction() {
        assertResult("math:cos(0)", BigDecimal.valueOf(cos(0)));
        assertResult("math:cos(3.141592653589793)", BigDecimal.valueOf(cos(PI)));
    }

    @Test
    public void tanFunction() {
        assertResult("math:tan(0)", BigDecimal.valueOf(tan(0)));
    }

    @Test
    public void asinFunction() {
        assertResult("math:asin(0)", BigDecimal.valueOf(asin(0)));
    }

    @Test
    public void acosFunction() {
        assertResult("math:acos(1)", BigDecimal.valueOf(acos(1)));
    }

    @Test
    public void atanFunction() {
        assertResult("math:atan(1)", BigDecimal.valueOf(atan(1)));
    }

    @Test
    public void piConstant() {
        assertResult("math:pi()", BigDecimal.valueOf(PI));
    }

    @Test
    public void expFunction() {
        assertResult("math:exp(1)", BigDecimal.valueOf(exp(1)));
    }

    @Test
    public void logFunction() {
        assertResult("math:log(1)", BigDecimal.valueOf(log(1))); // should be 0
    }

    @Test
    public void sqrtFunction() {
        assertResult("math:sqrt(4)", BigDecimal.valueOf(sqrt(4))); // should be 2
    }

}
