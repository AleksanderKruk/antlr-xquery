package com.github.akruk.antlrxquery;

import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.tree.xpath.XPath;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;

import com.github.akruk.antlrxquery.evaluator.XQuery;
import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;
import com.github.akruk.antlrxquery.testgrammars.TestLexer;
import com.github.akruk.antlrxquery.testgrammars.TestParser;
import com.github.akruk.antlrxquery.values.XQueryBoolean;
import com.github.akruk.antlrxquery.values.XQueryNumber;
import com.github.akruk.antlrxquery.values.XQuerySequence;
import com.github.akruk.antlrxquery.values.XQueryString;
import com.github.akruk.antlrxquery.values.XQueryValue;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collection;
import java.util.List;
import static org.junit.Assert.*;

public class XQueryTest {

    public void assertResult(String xquery, String result) {
        var value = XQuery.evaluate(null, xquery, null);
        assertNotNull(value);
        assertEquals(result, value.stringValue());
    }

    public void assertResult(String xquery, BigDecimal result) {
        var value = XQuery.evaluate(null, xquery, null);
        assertNotNull(value);
        assertEquals(result, value.numericValue());
    }

    public void assertResult(String xquery, List<XQueryValue> result) throws XQueryUnsupportedOperation {
        XQueryValue value = XQuery.evaluate(null, xquery, null);
        assertNotNull(value);
        assertEquals(result.size(), value.sequence().size());
        for (int i = 0; i < result.size(); i++) {
            var expected = result.get(i);
            var received = value.sequence().get(i);
            assertEquals(XQueryBoolean.TRUE, expected.valueEqual(received));
        }
    }


    public void assertResult(String xquery, XQueryValue result) throws XQueryUnsupportedOperation {
        XQueryValue value = XQuery.evaluate(null, xquery, null);
        assertNotNull(value);
        assertTrue(result.valueEqual(value).booleanValue());
    }


    @Test
    public void stringLiteralsDoubleQuote() {
        assertResult("\"string\"", "string");
    }

    @Test
    public void stringLiteralsSingleQuote() {
        assertResult("'string'", "string");
    }

    @Test
    public void stringLiteralsEscapeCharsSingle() {
        assertResult("'a''b'", "a'b");
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
        assertResult("1", BigDecimal.ONE);
    }

    @Test
    public void floatLiteral() {
        assertResult("1.2", new BigDecimal("1.2"));
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
                new XQueryNumber(BigDecimal.valueOf(3)));
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
                new XQueryNumber(BigDecimal.valueOf(7)));
        assertArrayEquals(
                expected.stream().map(XQueryValue::numericValue).toArray(),
                value.sequence().stream().map(XQueryValue::numericValue).toArray());
    }

    @Test
    public void trueConstant() throws XQueryUnsupportedOperation {
        assertResult("true()", XQueryBoolean.TRUE);
    }

    @Test
    public void falseConstant() throws XQueryUnsupportedOperation {
        assertResult("false()", XQueryBoolean.FALSE);
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

    @Test
    public void sequenceUnion() {
        String xquery = """
                    (1, 2, 3) | (4, 5, 6)
                """;
        var value = XQuery.evaluate(null, xquery, null);
        var expected = List.of(
                new XQueryNumber(BigDecimal.valueOf(1)),
                new XQueryNumber(BigDecimal.valueOf(2)),
                new XQueryNumber(BigDecimal.valueOf(3)),
                new XQueryNumber(BigDecimal.valueOf(4)),
                new XQueryNumber(BigDecimal.valueOf(5)),
                new XQueryNumber(BigDecimal.valueOf(6)));
        assertEquals(expected.size(), value.sequence().size());
        var sequence = value.sequence();
        for (int i = 0; i < expected.size(); i++) {
            var element = expected.get(i);
            var received = sequence.get(i);
            assertEquals(element.numericValue(), received.numericValue());
        }
        xquery = """
                    (1, 2, 3) union (4, 5, 6)
                """;
        value = XQuery.evaluate(null, xquery, null);
        assertEquals(expected.size(), value.sequence().size());
        sequence = value.sequence();
        for (int i = 0; i < expected.size(); i++) {
            var element = expected.get(i);
            var received = sequence.get(i);
            assertEquals(element.numericValue(), received.numericValue());
        }
    }

    @Test
    public void sequenceIntersection() {
        String xquery = """
                    (1, 2, 3, 4) intersect (0, 2, 4, 8)
                """;
        var value = XQuery.evaluate(null, xquery, null);
        BigDecimal[] expected = {
                (BigDecimal.valueOf(2)),
                (BigDecimal.valueOf(4)),
        };
        BigDecimal[] numbersFromSequence = value.sequence()
                .stream()
                .map(XQueryValue::numericValue)
                .toArray(BigDecimal[]::new);
        assertArrayEquals(expected, numbersFromSequence);
    }

    @Test
    public void sequenceSubtraction() {
        String xquery = """
                    (1, 2, 3, 4) except (2, 4)
                """;
        var value = XQuery.evaluate(null, xquery, null);
        BigDecimal[] expected = {
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(3),
        };
        BigDecimal[] numbersFromSequence = value.sequence()
                .stream()
                .map(XQueryValue::numericValue)
                .toArray(BigDecimal[]::new);
        assertArrayEquals(expected, numbersFromSequence);
    }

    @Test
    public void generalComparison() throws XQueryUnsupportedOperation {
        assertResult("(1, 2) = (2, 3)", XQueryBoolean.TRUE);
        assertResult("(1, 2) != (2, 3)", XQueryBoolean.TRUE);
        assertResult("(1, 2) < (2, 3)", XQueryBoolean.TRUE);
        assertResult("(1, 2) <= (2, 3)", XQueryBoolean.TRUE);
        assertResult("(1, 2) > (2, 3)", XQueryBoolean.FALSE);
        assertResult("(1, 2) >= (2, 3)", XQueryBoolean.TRUE);
    }

    @Test
    public void valueComparisonsEqual() throws XQueryUnsupportedOperation {
        // A eq B 	numeric 	numeric 	op:numeric-equal(A, B) 	xs:boolean
        assertResult("1 eq 1", XQueryBoolean.TRUE);
        // A eq B 	xs:boolean 	xs:boolean 	op:boolean-equal(A, B) 	xs:boolean
        assertResult("true() eq true()", XQueryBoolean.TRUE);
        // A eq B 	xs:string 	xs:string 	op:numeric-equal(fn:compare(A, B), 0) 	xs:boolean
        assertResult("'abcd' eq 'abcd'", XQueryBoolean.TRUE);
        // A le B 	xs:boolean 	xs:boolean 	fn:not(op:boolean-greater-than(A, B)) 	xs:boolean
    }

    @Test
    public void valueComparisonsNotEqual() throws XQueryUnsupportedOperation {
        // A ne B 	numeric 	numeric 	fn:not(op:numeric-equal(A, B)) 	xs:boolean
        assertResult("1 ne 0", XQueryBoolean.TRUE);
        // A ne B 	xs:boolean 	xs:boolean 	fn:not(op:boolean-equal(A, B)) 	xs:boolean
        assertResult("true() ne false()", XQueryBoolean.TRUE);
        // A ne B 	xs:string 	xs:string 	fn:not(op:numeric-equal(fn:compare(A, B), 0)) 	xs:boolean
        assertResult("'abc' ne 'abcd'", XQueryBoolean.TRUE);
    }

    @Test
    public void valueComparisonsGreaterThan() throws XQueryUnsupportedOperation {
        // A gt B 	numeric 	numeric 	op:numeric-greater-than(A, B) 	xs:boolean
        assertResult("3 gt 1", XQueryBoolean.TRUE);
        // A gt B 	xs:boolean 	xs:boolean 	op:boolean-greater-than(A, B) 	xs:boolean
        assertResult("true() gt false()", XQueryBoolean.TRUE);
        assertResult("false() gt true()", XQueryBoolean.FALSE);
        assertResult("true() gt true()", XQueryBoolean.FALSE);
        assertResult("false() gt false()", XQueryBoolean.FALSE);
        // A gt B 	xs:string 	xs:string 	op:numeric-greater-than(fn:compare(A, B), 0) 	xs:boolean
        assertResult("'abed' gt 'abcd'", XQueryBoolean.TRUE);
    }

    @Test
    public void valueComparisonsGreaterOrEqual() throws XQueryUnsupportedOperation {
        // A ge B 	numeric 	numeric 	op:numeric-greater-than(A, B) or op:numeric-equal(A, B) 	xs:boolean
        assertResult("3 ge 1", XQueryBoolean.TRUE);
        assertResult("1 ge 1", XQueryBoolean.TRUE);
        assertResult("0 ge 1", XQueryBoolean.FALSE);
        // A ge B 	xs:boolean 	xs:boolean 	xs:boolean
        assertResult("true() ge false()", XQueryBoolean.TRUE);
        assertResult("false() ge true()", XQueryBoolean.FALSE);
        assertResult("true() ge true()", XQueryBoolean.TRUE);
        assertResult("false() ge false()", XQueryBoolean.TRUE);
        // A ge B 	xs:string 	xs:string   xs:boolean
        assertResult("'abcd' ge 'abcd'", XQueryBoolean.TRUE);
        assertResult("'abed' ge 'abcd'", XQueryBoolean.TRUE);
    }

    @Test
    public void valueComparisonsLessOrEqual() throws XQueryUnsupportedOperation {
        // A le B 	numeric 	numeric
        assertResult("1 le 3", XQueryBoolean.TRUE);
        assertResult("1 le 1", XQueryBoolean.TRUE);
        assertResult("1 le 0", XQueryBoolean.FALSE);
        // A le B 	xs:boolean 	xs:boolean
        assertResult("true() le false()", XQueryBoolean.FALSE);
        assertResult("false() le true()", XQueryBoolean.TRUE);
        assertResult("true() le true()", XQueryBoolean.TRUE);
        assertResult("false() le false()", XQueryBoolean.TRUE);
        // A le B 	xs:string 	xs:string
        assertResult("'abed' le 'abcd'", XQueryBoolean.FALSE);
        assertResult("'abcd' le 'abed'", XQueryBoolean.TRUE);
        assertResult("'abcd' le 'abcd'", XQueryBoolean.TRUE);
    }

    @Test
    public void valueComparisonsLessThan() throws XQueryUnsupportedOperation {
        // A lt B 	numeric 	numeric 	op:numeric-less-than(A, B) 	xs:boolean
        assertResult("1 lt 3", XQueryBoolean.TRUE);
        assertResult("1 lt 1", XQueryBoolean.FALSE);
        assertResult("1 lt 0", XQueryBoolean.FALSE);
        // A lt B 	xs:boolean 	xs:boolean 	op:boolean-less-than(A, B) 	xs:boolean
        assertResult("true() lt false()", XQueryBoolean.FALSE);
        assertResult("false() lt true()", XQueryBoolean.TRUE);
        assertResult("true() lt true()", XQueryBoolean.FALSE);
        assertResult("false() lt false()", XQueryBoolean.FALSE);
        // A lt B 	xs:string 	xs:string 	op:numeric-less-than(fn:compare(A, B), 0) 	xs:boolean
        assertResult("'abed' lt 'abcd'", XQueryBoolean.FALSE);
        assertResult("'abcd' lt 'abed'", XQueryBoolean.TRUE);
        assertResult("'abcd' lt 'abcd'", XQueryBoolean.FALSE);
    }

    @Test
    public void concatenationExperssions() throws XQueryUnsupportedOperation {
        assertResult("'abc' || 'def' || 'ghi'", new XQueryString("abcdefghi"));
    }

    @Test
    public void abs() {
        assertResult("abs(3)", BigDecimal.valueOf(3));
        assertResult("abs(-3)", BigDecimal.valueOf(3));
    }

    @Test
    public void ceiling() {
        assertResult("ceiling(3.3)", BigDecimal.valueOf(4));
    }

    @Test
    public void floor() {
        assertResult("floor(3.3)", BigDecimal.valueOf(3));
    }

    @Test
    public void round() {
        // From https://www.w3.org/TR/xpath-functions-3/#func-round
        assertResult("round(3.3)", BigDecimal.valueOf(3));
        assertResult("round(3.5)", BigDecimal.valueOf(4));
        assertResult("round(-2.5)", BigDecimal.valueOf(-2));
        assertResult("round(1.125, 2)", new BigDecimal("1.13"));
        assertResult("round(8452, -2)", new BigDecimal("8500"));
    }

    @Test
    public void numericAdd() {
        assertResult("numeric-add(3, 5)", BigDecimal.valueOf(8));
    }

    @Test
    public void numericSubtract() {
        assertResult("numeric-subtract(3, 5)", BigDecimal.valueOf(-2));
    }

    @Test
    public void numericMultiply() {
        assertResult("numeric-multiply(3, 5)", BigDecimal.valueOf(15));
    }

    @Test
    public void numericDivide() {
        assertResult("numeric-divide(5, 5)", BigDecimal.ONE);
    }

    @Test
    public void numericIntegerDivide() {
        assertResult("numeric-integer-divide(5, 2)", BigDecimal.valueOf(2));
    }

    @Test
    public void numericMod() {
        assertResult("numeric-mod(5, 2)", BigDecimal.ONE);
    }

    @Test
    public void pi() {
        assertResult("pi()", new BigDecimal(Math.PI));
    }

    // @Test
    // public void exp() {
    //     assertResult("exp(5, 2)", BigDecimal.ONE);
    // }

    // @Test
    // public void exp10() {
    //     assertResult("exp10(5, 2)", BigDecimal.ONE);
    // }

    // @Test
    // public void log() {
    //     assertResult("log(5, 2)", BigDecimal.ONE);
    // }

    // @Test
    // public void log10() {
    //     assertResult("log10(5, 2)", BigDecimal.ONE);
    // }

    // @Test
    // public void pow() {
    //     assertResult("pow(5, 2)", BigDecimal.ONE);
    // }

    // @Test
    // public void sqrt() {
    //     assertResult("sqrt(5, 2)", BigDecimal.ONE);
    // }

    // @Test
    // public void sin() {
    //     assertResult("sin(5, 2)", BigDecimal.ONE);
    // }

    // @Test
    // public void cos() {
    //     assertResult("cos(5, 2)", BigDecimal.ONE);
    // }

    // @Test
    // public void tan() {
    //     assertResult("tan(5, 2)", BigDecimal.ONE);
    // }

    // @Test
    // public void asin() {
    //     assertResult("asin(5, 2)", BigDecimal.ONE);
    // }

    // @Test
    // public void acos() {
    //     assertResult("acos(5, 2)", BigDecimal.ONE);
    // }

    // @Test
    // public void atan() {
    //     assertResult("atan(5, 2)", BigDecimal.ONE);
    // }

    // @Test
    // public void atan2() {
    //     assertResult("atan2(5, 2)", BigDecimal.ONE);
    // }

    record TestParserAndTree(TestParser parser, ParseTree tree) {}

    TestParserAndTree parseTestTree(String text) {
        CodePointCharStream stream = CharStreams.fromString(text);
        TestLexer lexer = new TestLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TestParser parser = new TestParser(tokens);
        ParseTree tree = parser.test();
        return new TestParserAndTree(parser, tree);
    }


    public void assertSameResultsAsAntlrXPath(String textualTree, String xquery) {
        TestParserAndTree parserAndTree = parseTestTree(textualTree);
        ParseTree[] nodes = XPath.findAll(parserAndTree.tree, xquery, parserAndTree.parser)
            .toArray(ParseTree[]::new);
        var value = XQuery.evaluate(parserAndTree.tree, xquery, parserAndTree.parser);
        ParseTree[] xqueryNodes = value.sequence().stream().map(val->val.node())
            .toArray(ParseTree[]::new);
        assertArrayEquals(nodes, xqueryNodes);
    }

    @Test
    public void rootPath() {
        // assert false;
        assertSameResultsAsAntlrXPath("a bc a d",  "/test");
    }


    @Test
    public void rulePath() {
        // assert false;
        assertSameResultsAsAntlrXPath("a bc a d",  "/test/rule");
        assertSameResultsAsAntlrXPath("a bc a d",  "/test//rule");
    }


    @Test
    public void tokenPath() {
        // assert false;
        assertSameResultsAsAntlrXPath("a bc a d",  "//A");
        assertSameResultsAsAntlrXPath("a bc a d",  "//B");
        assertSameResultsAsAntlrXPath("a bc a d",  "//C");
        assertSameResultsAsAntlrXPath("a bc a d",  "//D");
    }

    @Test
    public void wildcards() {
        String textualTree = "a bc a d";
        String xquery = "//*";
        TestParserAndTree parserAndTree = parseTestTree(textualTree);
        ParseTree[] nodes = XPath.findAll(parserAndTree.tree, xquery, parserAndTree.parser)
                .toArray(ParseTree[]::new);
        var value = XQuery.evaluate(parserAndTree.tree, xquery, parserAndTree.parser);
        ParseTree[] xqueryNodes = value.sequence().stream().map(val -> val.node())
                .toArray(ParseTree[]::new);
        assertEquals(nodes.length, xqueryNodes.length);
        for (int i = 1; i < xqueryNodes.length; i++) {
            assertEquals(nodes[i], xqueryNodes[i]);
        }
    }


    @Test
    public void empty() throws XQueryUnsupportedOperation {
        assertResult("empty(())", XQueryBoolean.TRUE);
        assertResult("empty((1,2,3))", XQueryBoolean.FALSE);
        assertResult("empty(\"\")", XQueryBoolean.TRUE);
        assertResult("empty(\"abcd\")", XQueryBoolean.FALSE);
        // The expression fn:empty([]) returns false().
        // The expression fn:empty(map{}) returns false().
        // Assuming $in is an element with no children:
        //        let $break := <br/>
        //        return fn:empty($break)
        // The result is false().
    }


    @Test
    public void exists() throws XQueryUnsupportedOperation {
        assertResult("exists(())", XQueryBoolean.FALSE);
        assertResult("exists((1,2,3))", XQueryBoolean.TRUE);
        assertResult("exists(\"\")", XQueryBoolean.FALSE);
        assertResult("exists(\"abcd\")", XQueryBoolean.TRUE);
        // The expression fn:exists([]) returns true().
        // The expression fn:exists(map{}) returns true().
        // Assuming $in is an element with no children:
        //                let $break :=
        //                return fn:exists($break)
        // The result is true().
    }


    @Test
    public void head() throws XQueryUnsupportedOperation {
        assertResult("head(())", XQuerySequence.EMPTY.sequence());
        assertResult("head((1,2,3))", XQueryNumber.ONE);
        assertResult("head(\"\")", XQuerySequence.EMPTY.sequence());
        assertResult("head(\"abcd\")", new XQueryString("a"));
        // The expression fn:head(1 to 5) returns 1.
        // The expression fn:head(("a", "b", "c")) returns "a".
        // The expression fn:head(()) returns ().
        // The expression fn:head([1,2,3]) returns [1,2,3].
    }


    @Test
    public void tail() throws XQueryUnsupportedOperation {
        assertResult("tail(())", XQuerySequence.EMPTY.sequence());
        assertResult("tail((1,2,3))", List.of(new XQueryNumber(2), new XQueryNumber(3)));
        assertResult("tail(\"\")", XQuerySequence.EMPTY.sequence());
        assertResult("tail(\"abcd\")", new XQueryString("bcd"));
        // The expression fn:head(1 to 5) returns 1.
        // The expression fn:head(("a", "b", "c")) returns "a".
        // The expression fn:head(()) returns ().
        // The expression fn:head([1,2,3]) returns [1,2,3].
    }

// Wildcards

}
