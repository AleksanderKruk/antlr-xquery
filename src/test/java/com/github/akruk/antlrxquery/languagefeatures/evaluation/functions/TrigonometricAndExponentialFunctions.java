package com.github.akruk.antlrxquery.languagefeatures.evaluation.functions;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationBase;
import com.github.akruk.antlrxquery.values.XQueryError;

import static java.lang.Math.*;

public class TrigonometricAndExponentialFunctions extends EvaluationBase  {

    @Test
    public void pi() {
        assertResult("math:pi()", BigDecimal.valueOf(PI));
    }

    @Test
    public void sinFunction() {
        assertResult("math:sin(0)", BigDecimal.valueOf(sin(0)));
        assertResult("math:sin(math:pi() div 2)", BigDecimal.valueOf(sin(PI / 2)));
    }

    @Test
    public void cosFunction() {
        assertResult("math:cos(0)", BigDecimal.valueOf(cos(0)));
        assertResult("math:cos(math:pi())", BigDecimal.valueOf(cos(PI)));
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
    public void expFunction() {
        assertResult("math:exp(1)", BigDecimal.valueOf(exp(1)));
    }

    @Test
    public void logFunction() {
        assertResult("math:log(1)", BigDecimal.valueOf(log(1)));
    }

    @Test
    public void sqrtFunction() {
        assertResult("math:sqrt(4)", BigDecimal.valueOf(sqrt(4)));
    }

    // new tests for e, sinh, cosh, tanh

    @Test
    public void eFunction() {
        assertResult("math:e()", BigDecimal.valueOf(E));
    }

    @Test
    public void eWithArgsError() {
        assertError("math:e(1)", XQueryError.WrongNumberOfArguments);
    }

    @Test
    public void sinhFunction() {
        assertResult("math:sinh(0)", BigDecimal.valueOf(sinh(0)));
        assertResult("math:sinh(1)", BigDecimal.valueOf(sinh(1)));
    }

    @Test
    public void sinhArityErrors() {
        assertError("math:sinh()", XQueryError.WrongNumberOfArguments);
        assertError("math:sinh(1, 2)", XQueryError.WrongNumberOfArguments);
    }

    @Test
    public void sinhInvalidType() {
        assertError("math:sinh('x')", XQueryError.InvalidArgumentType);
    }

    @Test
    public void coshFunction() {
        assertResult("math:cosh(0)", BigDecimal.valueOf(cosh(0)));
        assertResult("math:cosh(1)", BigDecimal.valueOf(cosh(1)));
    }

    @Test
    public void coshArityErrors() {
        assertError("math:cosh()", XQueryError.WrongNumberOfArguments);
        assertError("math:cosh(1, 2)", XQueryError.WrongNumberOfArguments);
    }

    @Test
    public void coshInvalidType() {
        assertError("math:cosh('x')", XQueryError.InvalidArgumentType);
    }

    @Test
    public void tanhFunction() {
        assertResult("math:tanh(0)", BigDecimal.valueOf(tanh(0)));
        assertResult("math:tanh(1)", BigDecimal.valueOf(tanh(1)));
    }

    @Test
    public void tanhArityErrors() {
        assertError("math:tanh()", XQueryError.WrongNumberOfArguments);
        assertError("math:tanh(1, 2)", XQueryError.WrongNumberOfArguments);
    }

    @Test
    public void tanhInvalidType() {
        assertError("math:tanh('x')", XQueryError.InvalidArgumentType);
    }
}
