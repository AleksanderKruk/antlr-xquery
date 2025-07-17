package com.github.akruk.antlrxquery.languagefeatures.evaluation.functions;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;

public class ProcessingStrings extends EvaluationTestsBase {

    @Test public void testEmptyOutOfBoundsFilter() {
        assertResult("empty((1, 2, 3)[10])", baseFactory.bool(true));
    }


}
