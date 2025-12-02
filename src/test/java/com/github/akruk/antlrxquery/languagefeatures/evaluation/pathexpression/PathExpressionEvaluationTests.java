package com.github.akruk.antlrxquery.languagefeatures.evaluation.pathexpression;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;

import java.util.List;

public class PathExpressionEvaluationTests extends EvaluationTestsBase {
    @Test
    public void pathExpressionFunctionCall() throws Exception {
        assertDynamicGrammarQuery(
            "test",
            """
                grammar test;
                a: (A | B | C)*;
                A: 'A';
                B: 'B';
                C: 'C';
            """,
            "a",
            "ABC",
            "//B/string()",
            valueFactory.sequence(List.of(valueFactory.string("B")))
        );
    }

}
