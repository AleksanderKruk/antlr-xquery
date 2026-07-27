package com.github.akruk.antlrxquery.languagefeatures.evaluation.comparisons;


import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;

public class GeneralComparisonEvaluationTests extends EvaluationTestsBase {

    @Test
    public void generalComparison() {
        assertResult("(1, 2) = (2, 3)", valueFactory.bool(true));
        assertResult("(1, 2) != (2, 3)", valueFactory.bool(true));
        assertResult("(1, 2) < (2, 3)", valueFactory.bool(true));
        assertResult("(1, 2) <= (2, 3)", valueFactory.bool(true));
        assertResult("(1, 2) > (2, 3)", valueFactory.bool(false));
        assertResult("(1, 2) >= (2, 3)", valueFactory.bool(true));
    }

}
