package com.github.akruk.antlrxquery.languagefeatures.evaluation.instanceof_;


import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;

public class InstanceOfEvaluationTests extends EvaluationTestsBase {
    @Test
    public void number() {
        assertResult("1 instance of number", valueFactory.bool(true));
    }
    @Test
    public void string() {
        assertResult("'a' instance of string", valueFactory.bool(true));
    }
    @Test
    public void emptysequence() {
        assertResult("() instance of string?", valueFactory.bool(true));
        assertResult("() instance of string", valueFactory.bool(false));
        assertResult("() instance of empty-sequence()", valueFactory.bool(true));
    }
}
