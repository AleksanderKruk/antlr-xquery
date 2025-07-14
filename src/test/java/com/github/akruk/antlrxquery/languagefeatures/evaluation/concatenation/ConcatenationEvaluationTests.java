package com.github.akruk.antlrxquery.languagefeatures.evaluation.concatenation;

import org.junit.jupiter.api.Test;
import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;


public class ConcatenationEvaluationTests extends EvaluationTestsBase {


    @Test
    public void concatenationExpressions() {
        assertResult("'abc' || 'def' || 'ghi'", baseFactory.string("abcdefghi"));
        assertResult("""
                () || "con" || ("cat", "enate")
                    """, baseFactory.string("concatenate"));
    }

}
