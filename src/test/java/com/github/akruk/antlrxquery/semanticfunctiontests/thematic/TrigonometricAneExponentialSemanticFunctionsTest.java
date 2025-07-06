
package com.github.akruk.antlrxquery.semanticfunctiontests.thematic;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.semanticfunctiontests.FunctionsSemanticTest;

public class TrigonometricAneExponentialSemanticFunctionsTest extends FunctionsSemanticTest {
    @Test
    void pi_correctArity() {
        assertType("math:pi()", typeFactory.one(typeFactory.itemNumber()));
    }

    @Test
    void pi_tooManyArgs() {
        assertErrors("math:pi(1)");
        assertErrors("math:pi(1,2)");
    }

    @Test
    void e_correctArity() {
        assertType("math:e()", typeFactory.one(typeFactory.itemNumber()));
    }

    @Test
    void e_wrongArity() {
        assertErrors("math:e(0.1)");
    }

    @Test
    void exp_positional() {
        assertType("math:exp(2.5)",
                typeFactory.zeroOrOne(typeFactory.itemNumber()));
    }

    @Test
    void exp_named() {
        assertType("math:exp(value := 3.14)",
                typeFactory.zeroOrOne(typeFactory.itemNumber()));
    }

    @Test
    void exp_missingArg() {
        assertErrors("math:exp()");
    }

    @Test
    void exp_wrongType() {
        assertErrors("math:exp('foo')");
    }

    @Test
    void exp_unknownKeyword() {
        assertErrors("math:exp(v := 2.5)");
    }

    @Test
    void log_variants() {
        for (final String fn : List.of("log", "log10", "exp10", "sqrt")) {
            final String call = "math:" + fn + "(1.0)";
            assertType(call,
                    typeFactory.zeroOrOne(typeFactory.itemNumber()));
            assertErrors("math:" + fn + "()");
            assertErrors("math:" + fn + "(1,2)");
        }
    }

    @Test
    void trig_functions() {
        for (final String fn : List.of("sin", "cos", "tan", "asin", "acos")) {
            final String ok = "math:" + fn + "(0.0)";
            final String err1 = "math:" + fn + "()";
            final String err2 = "math:" + fn + "(0.0, 1.0)";
            final String err3 = "math:" + fn + "(val:=0.0)";
            assertType(ok,
                    typeFactory.zeroOrOne(typeFactory.itemNumber()));
            assertErrors(err1);
            assertErrors(err2);
            assertErrors(err3);
        }
    }

    @Test
    void pow_positional() {
        assertType("math:pow(2.0, 3)",
                typeFactory.zeroOrOne(typeFactory.itemNumber()));
    }

    @Test
    void pow_namedAll() {
        assertType("math:pow(x := 2.0, y := 3)",
                typeFactory.zeroOrOne(typeFactory.itemNumber()));
    }

    @Test
    void pow_mixed() {
        assertType("math:pow(2.0, y := 4)",
                typeFactory.zeroOrOne(typeFactory.itemNumber()));
    }

    @Test
    void pow_wrongArity() {
        assertErrors("math:pow(2.0)");
        assertErrors("math:pow(y := 4)");
        assertErrors("math:pow()");
        assertErrors("math:pow(1,2,3)");
    }

    @Test
    void pow_wrongNames() {
        assertErrors("math:pow(a:=2.0, b:=3)");
    }

    @Test
    void pow_wrongTypes() {
        assertErrors("math:pow('x', 5)");
        assertErrors("math:pow(2.0, 'y')");
    }

    // --- math:atan($value as xs:double?) as xs:double? ------------------------

    @Test
    void atan_positional() {
        assertType("math:atan(1.0)",
                typeFactory.zeroOrOne(typeFactory.itemNumber()));
    }

    @Test
    void atan_named() {
        assertType("math:atan(value := 0.0)",
                typeFactory.zeroOrOne(typeFactory.itemNumber()));
    }

    @Test
    void atan_missing() {
        assertErrors("math:atan()");
    }

    @Test
    void atan_tooMany() {
        assertErrors("math:atan(1.0, 2.0)");
    }

    @Test
    void atan_wrongType() {
        assertErrors("math:atan('foo')");
    }

    // --- math:atan2($y as xs:double, $x as xs:double) as xs:double ------------

    @Test
    void atan2_positional() {
        assertType("math:atan2(1.0, 1.0)",
                typeFactory.one(typeFactory.itemNumber()));
    }

    @Test
    void atan2_named() {
        assertType("math:atan2(y := 1.0, x := 2.0)",
                typeFactory.one(typeFactory.itemNumber()));
    }

    @Test
    void atan2_mixed() {
        assertType("math:atan2(3.0, x := 4.0)",
                typeFactory.one(typeFactory.itemNumber()));
    }

    @Test
    void atan2_missingArg() {
        assertErrors("math:atan2(1.0)");
        assertErrors("math:atan2()");
    }

    @Test
    void atan2_wrongNames() {
        assertErrors("math:atan2(a := 1.0, b := 2.0)");
    }

    @Test
    void atan2_wrongTypes() {
        assertErrors("math:atan2(1.0, 'abc')");
        assertErrors("math:atan2('y', 2.0)");
    }

    // --- math:sinh, math:cosh, math:tanh (identyczna sygnatura) ---------------

    @Test
    void hyperbolic_functions() {
        for (final String fn : List.of("sinh", "cosh", "tanh")) {
            assertType("math:" + fn + "(0.0)",
                    typeFactory.zeroOrOne(typeFactory.itemNumber()));
            assertType("math:" + fn + "(value := 1.1)",
                    typeFactory.zeroOrOne(typeFactory.itemNumber()));
            assertErrors("math:" + fn + "()");
            assertErrors("math:" + fn + "(1.0, 2.0)");
            assertErrors("math:" + fn + "('abc')");
            assertErrors("math:" + fn + "(val := 2.0)");
        }
    }

}
