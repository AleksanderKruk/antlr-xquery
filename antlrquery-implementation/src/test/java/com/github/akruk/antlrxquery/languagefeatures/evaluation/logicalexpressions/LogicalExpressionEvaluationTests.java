package com.github.akruk.antlrxquery.languagefeatures.evaluation.logicalexpressions;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;
import java.util.List;

public class LogicalExpressionEvaluationTests extends EvaluationTestsBase {

    @Test
    public void ifExpression() {
        assertResult("if ('non-empty-string') then 1 else 2", valueFactory.number(1));
        assertResult("if ('') then 1 else 2", valueFactory.number(2));
    }

    @Test
    public void shortIfExpression() {
        assertResult("if ('non-empty-string') { 1 }", valueFactory.number(1));
        assertResult("if ('') { 1 }", List.of());
    }

}
