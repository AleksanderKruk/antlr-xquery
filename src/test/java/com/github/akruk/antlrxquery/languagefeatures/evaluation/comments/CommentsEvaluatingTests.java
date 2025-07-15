package com.github.akruk.antlrxquery.languagefeatures.evaluation.comments;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;

import java.math.BigDecimal;

public class CommentsEvaluatingTests extends EvaluationTestsBase {
    @Test
    public void comments() {
        assertResult("(:comment:) 1", BigDecimal.ONE);
    }


}
