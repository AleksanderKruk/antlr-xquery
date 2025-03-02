package com.github.akruk.antlrxquery;

import org.junit.Test;

import com.github.akruk.antlrxquery.evaluator.XQuery;

import static org.junit.Assert.assertEquals;
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



}
