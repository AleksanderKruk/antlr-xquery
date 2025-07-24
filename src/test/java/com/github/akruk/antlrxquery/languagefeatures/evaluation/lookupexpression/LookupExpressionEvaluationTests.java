package com.github.akruk.antlrxquery.languagefeatures.evaluation.lookupexpression;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.evaluator.XQuery;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

public class LookupExpressionEvaluationTests extends EvaluationTestsBase {
    @Test
    public void emptylookupMap() {
        assertResult("map {} ? 'a'", valueFactory.sequence(List.of()));
    }

    @Test
    public void emptylookupArray() {
        assertResult("array {} ? 'a'", valueFactory.sequence(List.of()));
    }

    @Test
    public void emptyLookup() {
        assertResult("array {'a', 'b', 'c'} ? 2", valueFactory.sequence(List.of()));
    }
}
