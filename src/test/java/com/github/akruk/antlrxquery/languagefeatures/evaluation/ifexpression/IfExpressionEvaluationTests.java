package com.github.akruk.antlrxquery.languagefeatures.evaluation.ifexpression;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.evaluator.XQuery;
import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IfExpressionEvaluationTests extends EvaluationTestsBase {


    @Test
    public void or() {
        String xquery = "false() or false() or true()";
        var value = XQuery.evaluateWithMockRoot(null, xquery, null);
        assertTrue(value.booleanValue);
        xquery = "false() or false() or false()";
        value = XQuery.evaluateWithMockRoot(null, xquery, null);
        assertFalse(value.booleanValue);
    }

    @Test
    public void and() {
        String xquery = "true() and true() and false()";
        var value = XQuery.evaluateWithMockRoot(null, xquery, null);
        assertFalse(value.booleanValue);
        xquery = "true() and true() and true()";
        value = XQuery.evaluateWithMockRoot(null, xquery, null);
        assertTrue(value.booleanValue);
    }

    @Test
    public void not() {
        assertResult("not(true())", valueFactory.bool(false));
        assertResult("not(false())", valueFactory.bool(true));
    }

}
