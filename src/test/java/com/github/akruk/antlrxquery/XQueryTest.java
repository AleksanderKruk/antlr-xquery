package com.github.akruk.antlrxquery;

import org.junit.Test;

import com.github.akruk.antlrxquery.evaluator.XQuery;
import com.github.akruk.antlrxquery.values.XQueryBoolean;
import com.github.akruk.antlrxquery.values.XQueryNumber;
import com.github.akruk.antlrxquery.values.XQueryValue;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class XQueryTest {

    @Test
    public void stringLiteralsDoubleQuote() {
        String xquery = """
            "string"
        """;
        var value = XQuery.evaluate(null, xquery, null);
        assertEquals("string", value.stringValue());
    }

    @Test
    public void stringLiteralsSingleQuote() {
        String xquery = """
                    'string'
                """;
        var value = XQuery.evaluate(null, xquery, null);
        assertEquals("string", value.stringValue());
    }

    @Test
    public void stringLiteralsEscapeCharsSingle() {
        String xquery = """
                    'a''b'
                """;
        var value = XQuery.evaluate(null, xquery, null);
        assertEquals("a'b", value.stringValue());
    }

    @Test
    public void stringLiteralsEscapeCharsDouble() {
        String xquery = """
                    "a""b"
                """;
        var value = XQuery.evaluate(null, xquery, null);
        assertEquals("a\"b", value.stringValue());
    }

    @Test
    public void integerLiteral() {
        String xquery = """
                    1
                """;
        var value = XQuery.evaluate(null, xquery, null);
        assertEquals(BigDecimal.ONE, value.numericValue());
    }

    @Test
    public void floatLiteral() {
        String xquery = """
                    1.2
                """;
        var value = XQuery.evaluate(null, xquery, null);
        assertEquals(new BigDecimal("1.2"), value.numericValue());
    }

    @Test
    public void sequenceLiteral() {
        String xquery = """
                    (1, 2, 3)
                """;
        var value = XQuery.evaluate(null, xquery, null);
        List<XQueryValue> expected = List.of(
            new XQueryNumber(BigDecimal.ONE),
            new XQueryNumber(BigDecimal.valueOf(2)),
            new XQueryNumber(BigDecimal.valueOf(3))
        );
        assertNotNull(value);
        assertNotNull(value.sequence());
        var sequence = value.sequence();
        assertEquals(expected.size(), sequence.size());
        assertTrue(expected.get(0).numericValue().equals(sequence.get(0).numericValue()));
        assertTrue(expected.get(0).numericValue().equals(sequence.get(0).numericValue()));
        assertTrue(expected.get(0).numericValue().equals(sequence.get(0).numericValue()));

    }


    @Test
    public void atomization() {
        String xquery = """
                    (1, (2,3,4), ((5, 6), 7))
                """;
        var value = XQuery.evaluate(null, xquery, null);
        List<XQueryValue> expected = List.of(
            new XQueryNumber(BigDecimal.ONE),
            new XQueryNumber(BigDecimal.valueOf(2)),
            new XQueryNumber(BigDecimal.valueOf(3)),
            new XQueryNumber(BigDecimal.valueOf(4)),
            new XQueryNumber(BigDecimal.valueOf(5)),
            new XQueryNumber(BigDecimal.valueOf(6)),
            new XQueryNumber(BigDecimal.valueOf(7))
        );
        assertArrayEquals(
            expected.stream().map(XQueryValue::numericValue).toArray(),
            value.sequence().stream().map(XQueryValue::numericValue).toArray()
        );
    }

    @Test
    public void trueConstant() {
        String xquery = """
            true()
        """;
        var value = XQuery.evaluate(null, xquery, null);
        assertTrue(value.booleanValue().equals(XQueryBoolean.TRUE.booleanValue()));
    }


    @Test
    public void falseConstant() {
        String xquery = """
            false()
        """;
        var value = XQuery.evaluate(null, xquery, null);
        assertTrue(value.booleanValue().equals(XQueryBoolean.FALSE.booleanValue()));
    }


    @Test
    public void or() {
        String xquery = """
            false() or false() or true()
        """;
        var value = XQuery.evaluate(null, xquery, null);
        assertTrue(value.booleanValue());
        xquery = """
            false() or false() or false()
        """;
        value = XQuery.evaluate(null, xquery, null);
        assertFalse(value.booleanValue());
    }


    @Test
    public void and() {
        String xquery = """
            true() and true() and false()
        """;
        var value = XQuery.evaluate(null, xquery, null);
        assertFalse(value.booleanValue());
        xquery = """
            true() and true() and true()
        """;
        value = XQuery.evaluate(null, xquery, null);
        assertTrue(value.booleanValue());
    }

    @Test
    public void not() {
        String xquery = """
            not(true())
        """;
        var value = XQuery.evaluate(null, xquery, null);
        assertFalse(value.booleanValue());
        xquery = """
            not(false())
        """;
        value = XQuery.evaluate(null, xquery, null);
        assertTrue(value.booleanValue());
    }



}
