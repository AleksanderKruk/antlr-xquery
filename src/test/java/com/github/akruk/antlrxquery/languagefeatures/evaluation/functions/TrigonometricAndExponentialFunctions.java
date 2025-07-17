package com.github.akruk.antlrxquery.languagefeatures.evaluation.functions;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;
import com.github.akruk.antlrxquery.values.XQueryError;

import static java.lang.Math.*;

public class TrigonometricAndExponentialFunctions extends EvaluationTestsBase  {

    @Test
    public void containsToken() {
        assertResult("fn:contains-token('red green blue', 'red')", baseFactory.bool(true));
        assertResult("fn:contains-token('red green blue', 'white')", baseFactory.bool(false));
        assertResult("fn:contains-token('red green blue', ' red ')", baseFactory.bool(true));
        assertResult("fn:contains-token('red, green, blue', 'red')", baseFactory.bool(false));
    }


}
