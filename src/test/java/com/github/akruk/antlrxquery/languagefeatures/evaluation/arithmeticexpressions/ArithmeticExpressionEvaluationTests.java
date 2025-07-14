package com.github.akruk.antlrxquery.languagefeatures.evaluation.arithmeticexpressions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.MathContext;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.evaluator.XQuery;
import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;


public class ArithmeticExpressionEvaluationTests extends EvaluationTestsBase {
    @Test
    public void addition() {
        String xquery = """
                    5 + 3.10
                """;
        var value = XQuery.evaluate(null, xquery, null);
        assertEquals(new BigDecimal("8.10"), value.numericValue());
    }

    @Test
    public void subtraction() {
        String xquery = """
                    5 - 3.10
                """;
        var value = XQuery.evaluate(null, xquery, null);
        assertEquals(new BigDecimal("1.9").setScale(2), value.numericValue().setScale(2));
    }

    @Test
    public void multiplication() {
        String xquery = """
                    5 * 3.0
                """;
        var value = XQuery.evaluate(null, xquery, null);
        assertEquals(new BigDecimal(15).setScale(2), value.numericValue().setScale(2));
    }

    @Test
    public void division() {
        assertResult("5 div 2.0", new BigDecimal(2.5, MathContext.UNLIMITED));
    }

    @Test
    public void integerDivision() {
        String xquery = """
                    5 idiv 2
                """;
        var value = XQuery.evaluate(null, xquery, null);
        assertEquals(new BigDecimal(2).setScale(2), value.numericValue().setScale(2));
    }

    @Test
    public void modulus() {
        String xquery = """
                    4 mod 2
                """;
        var value = XQuery.evaluate(null, xquery, null);
        assertEquals(BigDecimal.ZERO.setScale(2), value.numericValue().setScale(2));
    }


}
