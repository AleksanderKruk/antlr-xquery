package com.github.akruk.antlrxquery.languagefeatures.evaluation.functions;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;
import com.github.akruk.antlrxquery.values.XQueryError;

public class ProcessingBooleansTests extends EvaluationTestsBase {
    @Test
    public void true_() {
        assertResult("true()", baseFactory.bool(true));
    }

    @Test
    public void false_() {
        assertResult("false()", baseFactory.bool(false));
    }

    @Test
    public void not() {
        assertResult("not(true())", baseFactory.bool(false));
        assertResult("not(false())", baseFactory.bool(true));

        assertResult("not(1)", baseFactory.bool(false));
        assertResult("not(0)", baseFactory.bool(true));
        assertResult("not(-1)", baseFactory.bool(false));

        assertResult("not(())", baseFactory.bool(true));

        assertResult("not('')", baseFactory.bool(true));
        assertResult("not('text')", baseFactory.bool(false));

        assertError("not()", XQueryError.WrongNumberOfArguments);

    }

    @Test
    public void boolean_() {
        assertResult("boolean(true())", baseFactory.bool(true));
        assertResult("boolean(false())", baseFactory.bool(false));

        assertResult("boolean(1)", baseFactory.bool(true));
        assertResult("boolean(0)", baseFactory.bool(false));
        assertResult("boolean(-1)", baseFactory.bool(true));
        assertResult("boolean(3.14)", baseFactory.bool(true));

        assertResult("boolean(())", baseFactory.bool(false));

        assertResult("boolean('')", baseFactory.bool(false));
        assertResult("boolean('text')", baseFactory.bool(true));
        assertResult("boolean('0')", baseFactory.bool(true));

        assertError("boolean()", XQueryError.WrongNumberOfArguments);
    }


    @Test
    public void booleanEqual() {
        assertResult("op:boolean-equal(boolean(true()), boolean(true()))", baseFactory.bool(true));
        assertResult("op:boolean-equal(boolean(false()), boolean(false()))", baseFactory.bool(true));
        assertResult("op:boolean-equal(boolean(true()), boolean(false()))", baseFactory.bool(false));
        assertResult("op:boolean-equal(boolean(false()), boolean(true()))", baseFactory.bool(false));
        assertResult("op:boolean-equal(boolean(1), boolean(2))", baseFactory.bool(true));
        assertResult("op:boolean-equal(boolean(0), boolean(()))", baseFactory.bool(true));
        assertResult("op:boolean-equal(boolean('text'), boolean(42))", baseFactory.bool(true));
        assertError("op:boolean-equal(boolean(), boolean())", XQueryError.InvalidArgumentType);
    }

    @Test
    public void booleanLessThan() {
        assertResult("op:boolean-less-than(false(), boolean(true()))", baseFactory.bool(true));
        assertResult("op:boolean-less-than(true(), false()))", baseFactory.bool(false));
        assertResult("op:boolean-less-than(false(), false()))", baseFactory.bool(false));
        assertResult("op:boolean-less-than(true(), true())", baseFactory.bool(false));
    }

    @Test
    public void booleanLessThanOrEqual() {
        assertResult("op:boolean-less-than-or-equal(boolean(false()), boolean(true()))", baseFactory.bool(true));
        assertResult("op:boolean-less-than-or-equal(boolean(true()), boolean(false()))", baseFactory.bool(false));
        assertResult("op:boolean-less-than-or-equal(boolean(false()), boolean(false()))", baseFactory.bool(true));
        assertResult("op:boolean-less-than-or-equal(boolean(true()), boolean(true()))", baseFactory.bool(true));
    }

    @Test
    public void booleanGreaterThan() {
        assertResult("op:boolean-greater-than(boolean(true()), boolean(false()))", baseFactory.bool(true));
        assertResult("op:boolean-greater-than(boolean(false()), boolean(true()))", baseFactory.bool(false));
        assertResult("op:boolean-greater-than(boolean(false()), boolean(false()))", baseFactory.bool(false));
        assertResult("op:boolean-greater-than(boolean(true()), boolean(true()))", baseFactory.bool(false));
    }

    @Test
    public void booleanGreaterThanOrEqual() {
        assertResult("op:boolean-greater-than-or-equal(boolean(true()), boolean(false()))", baseFactory.bool(true));
        assertResult("op:boolean-greater-than-or-equal(boolean(false()), boolean(true()))", baseFactory.bool(false));
        assertResult("op:boolean-greater-than-or-equal(boolean(false()), boolean(false()))", baseFactory.bool(true));
        assertResult("op:boolean-greater-than-or-equal(boolean(true()), boolean(true()))", baseFactory.bool(true));
    }
}
