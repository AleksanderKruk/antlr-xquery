package com.github.akruk.antlrquery.languagefeatures.evaluation.ifexpression;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrquery.evaluator.AntlrQuery;
import com.github.akruk.antlrquery.languagefeatures.evaluation.EvaluationTestsBase;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IfExpressionEvaluationTests extends EvaluationTestsBase {


    @Test
    public void or() {
        String xquery = "false() or false() or true()";
        var value = AntlrQuery.evaluateWithMockRoot(null, xquery, "", null);
        assertTrue(value.booleanValue);
        xquery = "false() or false() or false()";
        value = AntlrQuery.evaluateWithMockRoot(null, xquery, "", null);
        assertFalse(value.booleanValue);
    }

    @Test
    public void and() {
        String xquery = "true() and true() and false()";
        var value = AntlrQuery.evaluateWithMockRoot(null, xquery, "", null);
        assertFalse(value.booleanValue);
        xquery = "true() and true() and true()";
        value = AntlrQuery.evaluateWithMockRoot(null, xquery, "", null);
        assertTrue(value.booleanValue);
    }

    @Test
    public void not() {
        assertResult("not(true())", valueFactory.bool(false));
        assertResult("not(false())", valueFactory.bool(true));
    }

}
