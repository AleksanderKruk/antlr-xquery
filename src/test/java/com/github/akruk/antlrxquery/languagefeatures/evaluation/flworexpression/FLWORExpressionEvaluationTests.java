package com.github.akruk.antlrxquery.languagefeatures.evaluation.flworexpression;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;

public class FLWORExpressionEvaluationTests extends EvaluationTestsBase {
    @Test
    public void generalComparison() {
        assertResult("(1, 2) = (2, 3)", baseFactory.bool(true));
        assertResult("(1, 2) != (2, 3)", baseFactory.bool(true));
        assertResult("(1, 2) < (2, 3)", baseFactory.bool(true));
        assertResult("(1, 2) <= (2, 3)", baseFactory.bool(true));
        assertResult("(1, 2) > (2, 3)", baseFactory.bool(false));
        assertResult("(1, 2) >= (2, 3)", baseFactory.bool(true));
    }

}
