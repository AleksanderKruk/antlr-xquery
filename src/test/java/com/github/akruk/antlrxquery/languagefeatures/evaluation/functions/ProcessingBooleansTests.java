package com.github.akruk.antlrxquery.languagefeatures.evaluation.functions;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;
import com.github.akruk.antlrxquery.values.XQueryError;

public class ProcessingBooleansTests extends EvaluationTestsBase {
    @Test
    public void true_() {
        assertResult("true()", valueFactory.bool(true));
    }

    @Test
    public void false_() {
        assertResult("false()", valueFactory.bool(false));
    }

    @Test
    public void not() {
        assertResult("not(true())", valueFactory.bool(false));
        assertResult("not(false())", valueFactory.bool(true));

        assertResult("not(1)", valueFactory.bool(false));
        assertResult("not(0)", valueFactory.bool(true));
        assertResult("not(-1)", valueFactory.bool(false));

        assertResult("not(())", valueFactory.bool(true));

        assertResult("not('')", valueFactory.bool(true));
        assertResult("not('text')", valueFactory.bool(false));

        assertError("not()", XQueryError.WrongNumberOfArguments);

    }

    @Test
    public void boolean_() {
        assertResult("boolean(true())", valueFactory.bool(true));
        assertResult("boolean(false())", valueFactory.bool(false));

        assertResult("boolean(1)", valueFactory.bool(true));
        assertResult("boolean(0)", valueFactory.bool(false));
        assertResult("boolean(-1)", valueFactory.bool(true));
        assertResult("boolean(3.14)", valueFactory.bool(true));

        assertResult("boolean(())", valueFactory.bool(false));

        assertResult("boolean('')", valueFactory.bool(false));
        assertResult("boolean('text')", valueFactory.bool(true));
        assertResult("boolean('0')", valueFactory.bool(true));

        assertError("boolean()", XQueryError.WrongNumberOfArguments);
    }


    @Test
    public void booleanEqual() {
        assertResult("op:boolean-equal(boolean(true()), boolean(true()))", valueFactory.bool(true));
        assertResult("op:boolean-equal(boolean(false()), boolean(false()))", valueFactory.bool(true));
        assertResult("op:boolean-equal(boolean(true()), boolean(false()))", valueFactory.bool(false));
        assertResult("op:boolean-equal(boolean(false()), boolean(true()))", valueFactory.bool(false));
        assertResult("op:boolean-equal(boolean(1), boolean(2))", valueFactory.bool(true));
        assertResult("op:boolean-equal(boolean(0), boolean(()))", valueFactory.bool(true));
        assertResult("op:boolean-equal(boolean('text'), boolean(42))", valueFactory.bool(true));
        assertError("op:boolean-equal(boolean(), boolean())", XQueryError.InvalidArgumentType);
    }

    @Test
    public void booleanLessThan() {
        assertResult("op:boolean-less-than(false(), boolean(true()))", valueFactory.bool(true));
        assertResult("op:boolean-less-than(true(), false()))", valueFactory.bool(false));
        assertResult("op:boolean-less-than(false(), false()))", valueFactory.bool(false));
        assertResult("op:boolean-less-than(true(), true())", valueFactory.bool(false));
    }

    @Test
    public void booleanLessThanOrEqual() {
        assertResult("op:boolean-less-than-or-equal(boolean(false()), boolean(true()))", valueFactory.bool(true));
        assertResult("op:boolean-less-than-or-equal(boolean(true()), boolean(false()))", valueFactory.bool(false));
        assertResult("op:boolean-less-than-or-equal(boolean(false()), boolean(false()))", valueFactory.bool(true));
        assertResult("op:boolean-less-than-or-equal(boolean(true()), boolean(true()))", valueFactory.bool(true));
    }

    @Test
    public void booleanGreaterThan() {
        assertResult("op:boolean-greater-than(boolean(true()), boolean(false()))", valueFactory.bool(true));
        assertResult("op:boolean-greater-than(boolean(false()), boolean(true()))", valueFactory.bool(false));
        assertResult("op:boolean-greater-than(boolean(false()), boolean(false()))", valueFactory.bool(false));
        assertResult("op:boolean-greater-than(boolean(true()), boolean(true()))", valueFactory.bool(false));
    }

    @Test
    public void booleanGreaterThanOrEqual() {
        assertResult("op:boolean-greater-than-or-equal(boolean(true()), boolean(false()))", valueFactory.bool(true));
        assertResult("op:boolean-greater-than-or-equal(boolean(false()), boolean(true()))", valueFactory.bool(false));
        assertResult("op:boolean-greater-than-or-equal(boolean(false()), boolean(false()))", valueFactory.bool(true));
        assertResult("op:boolean-greater-than-or-equal(boolean(true()), boolean(true()))", valueFactory.bool(true));
    }
}
