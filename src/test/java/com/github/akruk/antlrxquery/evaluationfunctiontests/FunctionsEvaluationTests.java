package com.github.akruk.antlrxquery.evaluationfunctiontests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.evaluator.XQuery;
import com.github.akruk.antlrxquery.testgrammars.TestLexer;
import com.github.akruk.antlrxquery.testgrammars.TestParser;
import com.github.akruk.antlrxquery.values.XQueryError;
import com.github.akruk.antlrxquery.values.XQueryValue;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;
import com.github.akruk.antlrxquery.values.factories.defaults.XQueryMemoizedValueFactory;

public class FunctionsEvaluationTests {
    public XQueryValueFactory baseFactory = new XQueryMemoizedValueFactory();
    public void assertResult(String xquery, String result) {
        var value = XQuery.evaluate(null, xquery, null);
        assertNotNull(value);
        assertEquals(result, value.stringValue());
    }

    public void assertResult(String xquery, BigDecimal result) {
        var value = XQuery.evaluate(null, xquery, null);
        assertNotNull(value);
        assertTrue(result.compareTo(value.numericValue()) == 0);
    }

    public void assertResult(String xquery, List<XQueryValue> result) {
        XQueryValue value = XQuery.evaluate(null, xquery, null);
        assertNotNull(value);
        assertEquals(result.size(), value.sequence().size());
        for (int i = 0; i < result.size(); i++) {
            var expected = result.get(i);
            var received = value.sequence().get(i);
            assertTrue(expected.valueEqual(received).booleanValue());
        }
    }


    public void assertResult(String xquery, XQueryValue result) {
        XQueryValue value = XQuery.evaluate(null, xquery, null);
        assertNotNull(value);
        assertFalse(value instanceof XQueryError, () ->"Value is error: " + ((XQueryError) value).getDescription());
        assertTrue(result == value || result.valueEqual(value).booleanValue());
    }

    public void assertError(String xquery, XQueryValue result) {
        XQueryValue value = XQuery.evaluate(null, xquery, null);
        assertNotNull(value);
        assertTrue(result == value);
    }


    public void assertResult(String xquery, String textualTree, XQueryValue result) {
        TestParserAndTree parserAndTree = parseTestTree(textualTree);
        var value = XQuery.evaluate(parserAndTree.tree, xquery, parserAndTree.parser);
        assertNotNull(value);
        assertTrue(result.valueEqual(value).booleanValue());
    }

    record TestParserAndTree(TestParser parser, ParseTree tree) {}

    TestParserAndTree parseTestTree(String text) {

    CodePointCharStream stream = CharStreams.fromString(text);
        TestLexer lexer = new TestLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TestParser parser = new TestParser(tokens);
        ParseTree tree = parser.test();
        return new TestParserAndTree(parser, tree);
    }



}
