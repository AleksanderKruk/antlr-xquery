package com.github.akruk.antlrquery.languagefeatures.evaluation.functions;


import org.junit.jupiter.api.Test;

import com.github.akruk.antlrquery.languagefeatures.evaluation.EvaluationTestsBase;

public class TrigonometricAndExponentialFunctions extends EvaluationTestsBase  {

    @Test
    public void containsToken() {
        assertResult("fn:contains-token('red green blue', 'red')", valueFactory.bool(true));
        assertResult("fn:contains-token('red green blue', 'white')", valueFactory.bool(false));
        assertResult("fn:contains-token('red green blue', ' red ')", valueFactory.bool(true));
        assertResult("fn:contains-token('red, green, blue', 'red')", valueFactory.bool(false));
    }


}
