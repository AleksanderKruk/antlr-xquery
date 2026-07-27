package com.github.akruk.antlrxquery.languagefeatures.evaluation.lookupexpression;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;

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
    public void arraySingleLookup() {
        assertResult("array {'a', 'b', 'c'} ? 2", "b");
    }
}
