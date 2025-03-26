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
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;
import com.github.akruk.antlrxquery.values.factories.defaults.XQueryBaseValueFactory;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import static org.junit.Assert.*;

public class XQueryTest {
    XQueryValueFactory baseFactory = new XQueryBaseValueFactory();
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
            assertEquals(XQueryBoolean.TRUE, expected.valueEqual(baseFactory, received));
        }
    }


    public void assertResult(String xquery, XQueryValue result) throws XQueryUnsupportedOperation {
        XQueryValue value = XQuery.evaluate(null, xquery, null);
        assertNotNull(value);
        assertTrue(result.valueEqual(baseFactory, value).booleanValue());
    }


    public void assertResult(String xquery, String textualTree, XQueryValue result) throws XQueryUnsupportedOperation {
        TestParserAndTree parserAndTree = parseTestTree(textualTree);
        var value = XQuery.evaluate(parserAndTree.tree, xquery, parserAndTree.parser);
        assertNotNull(value);
        assertTrue(result.valueEqual(baseFactory, value).booleanValue());
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
                baseFactory.number(1),
                baseFactory.number(2),
                baseFactory.number(3));
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
                baseFactory.number(1),
                baseFactory.number(2),
                baseFactory.number(3),
                baseFactory.number(4),
                baseFactory.number(5),
                baseFactory.number(6),
                baseFactory.number(7));
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
        String xquery = "false() or false() or true()";
        var value = XQuery.evaluate(null, xquery, null);
        assertTrue(value.booleanValue());
        xquery = "false() or false() or false()";
        value = XQuery.evaluate(null, xquery, null);
        assertFalse(value.booleanValue());
    }

    @Test
    public void and() {
        String xquery = "true() and true() and false()";
        var value = XQuery.evaluate(null, xquery, null);
        assertFalse(value.booleanValue());
        xquery = "true() and true() and true()";
        value = XQuery.evaluate(null, xquery, null);
        assertTrue(value.booleanValue());
    }

    @Test
    public void not() {
        String xquery = "not(true())";
        var value = XQuery.evaluate(null, xquery, null);
        assertFalse(value.booleanValue());
        xquery = "not(false())";
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
                baseFactory.number(1),
                baseFactory.number(2),
                baseFactory.number(3),
                baseFactory.number(4),
                baseFactory.number(5),
                baseFactory.number(6));
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
    public void identityNodeComparison() throws XQueryUnsupportedOperation {
        assertResult("/test is /test", "a bc a d", XQueryBoolean.TRUE);
    }

    @Test
    public void beforeNode() throws XQueryUnsupportedOperation {
        assertResult("/test << /test", "a bc a d", XQueryBoolean.FALSE);
        assertResult("/test << /test/A[1]", "a bc a d", XQueryBoolean.TRUE);
    }

    @Test
    public void afterNode() throws XQueryUnsupportedOperation {
        assertResult("/test >> /test", "a bc a d", XQueryBoolean.FALSE);
        assertResult("/test/A[1] >> /test", "a bc a d", XQueryBoolean.TRUE);
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
        assertResult("empty(1)", XQueryBoolean.FALSE);
        assertResult("empty((1,2,3))", XQueryBoolean.FALSE);
        assertResult("empty(\"\")", XQueryBoolean.FALSE);
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
        assertResult("exists(\"\")", XQueryBoolean.TRUE);
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

    @Test
    public void insertBefore() throws XQueryUnsupportedOperation {
        var a = new XQueryString("a");
        var b = new XQueryString("b");
        var c = new XQueryString("c");
        var z = new XQueryString("z");
        // The expression fn:insert-before(("a", "b", "c"), 0, "z") returns ("z", "a", "b", "c").
        assertResult("""
                insert-before(("a", "b", "c"), 0, "z")
                """, List.of(z, a, b, c));
        // The expression fn:insert-before(("a", "b", "c"), 1, "z") returns ("z", "a", "b", "c").
        assertResult("""
                insert-before(("a", "b", "c"), 1, "z")
                """, List.of(z, a, b, c));
        // The expression fn:insert-before(("a", "b", "c"), 2, "z") returns ("a", "z", "b", "c").
        assertResult("""
                insert-before(("a", "b", "c"), 2, "z")
                """, List.of(a, z, b, c));
        // The expression fn:insert-before(("a", "b", "c"), 3, "z") returns ("a", "b", "z", "c").
        assertResult("""
                insert-before(("a", "b", "c"), 3, "z")
                """, List.of(a, b, z, c));
        // The expression fn:insert-before(("a", "b", "c"), 4, "z") returns ("a", "b", "c", "z").
        assertResult("""
                insert-before(("a", "b", "c"), 4, "z")
                """, List.of(a, b, c, z));
    }

    @Test
    public void remove() throws XQueryUnsupportedOperation {
        var a = new XQueryString("a");
        var b = new XQueryString("b");
        var c = new XQueryString("c");
        // The expression fn:remove($abc, 0) returns ("a", "b", "c").
        assertResult("""
                remove(("a", "b", "c"), 0)
                """, List.of(a, b, c));
        // The expression fn:remove($abc, 1) returns ("b", "c").
        assertResult("""
                remove(("a", "b", "c"), 1)
                """, List.of(b, c));
        // The expression fn:remove($abc, 6) returns ("a", "b", "c").
        assertResult("""
                remove(("a", "b", "c"), 6)
                """, List.of(a, b, c));
        // The expression fn:remove((), 3) returns ().
        assertResult("remove((), 3)", List.of());
    }

    @Test
    public void reverse() throws XQueryUnsupportedOperation {
        var a = new XQueryString("a");
        var b = new XQueryString("b");
        var c = new XQueryString("c");
        // The expression fn:reverse($abc) returns ("c", "b", "a").
        assertResult("""
                reverse(("a", "b", "c"))
                """, List.of(c, b, a));
        // The expression fn:reverse(("hello")) returns ("hello").
        assertResult("reverse((\"Hello\"))", List.of(new XQueryString("Hello")));
        // The expression fn:reverse(()) returns ().
        assertResult("reverse(())", List.of());
        // The expression fn:reverse([1,2,3]) returns [1,2,3]. (The input is a sequence containing a single item (the array)).
        // The expression fn:reverse(([1,2,3],[4,5,6])) returns ([4,5,6],[1,2,3]).
    }

    @Test
    public void subsequence() throws XQueryUnsupportedOperation {
        // var i1 = new XQueryString("item1");
        // var i2 = new XQueryString("item2");
        var i3 = new XQueryString("item3");
        var i4 = new XQueryString("item4");
        var i5 = new XQueryString("item5");
        // The expression fn:subsequence($seq, 4) returns ("item4", "item5").
        // The expression fn:subsequence($seq, 3, 2) returns ("item3", "item4").
        assertResult("""
                subsequence(("item1", "item2", "item3", "item4", "item5"), 4)
            """, List.of(i4, i5));
        assertResult("""
                subsequence(("item1", "item2", "item3", "item4", "item5"), 3, 2)
            """, List.of(i3, i4));
    }

    @Test
    public void distinctValues() throws XQueryUnsupportedOperation {
        var i1 = new XQueryString("1");
        var i2 = new XQueryString("2");
        assertResult("""
                distinct-values((1, "1", 1, "1", "2", false(), false(), true(), true()))
            """, List.of(XQueryNumber.ONE, i1, i2, XQueryBoolean.FALSE, XQueryBoolean.TRUE));
        assertResult("""
                distinct-values(())
            """, List.of());
    }

    @Test
    public void zeroOrOne() throws XQueryUnsupportedOperation {
        var value = XQuery.evaluate(null, "zero-or-one((1, 2))", null);
        assertNull(value);
        assertResult("""
                zero-or-one(())
            """, List.of());
    }

    @Test
    public void oneOrMore() throws XQueryUnsupportedOperation {
        var value = XQuery.evaluate(null, "one-or-more(())", null);
        assertNull(value);
        assertResult("""
                    one-or-more((1, 2))
                """, List.of(XQueryNumber.ONE, new XQueryNumber(2)));
    }

    @Test
    public void data() throws XQueryUnsupportedOperation {
        assertResult("data(1)", List.of(XQueryNumber.ONE));
        assertResult("data('a')", List.of(new XQueryString("a")));
    }

    @Test
    public void contains() throws XQueryUnsupportedOperation {
        assertResult("contains('abc', 'bc')", XQueryBoolean.TRUE);
        assertResult("contains('', 'bc')", XQueryBoolean.FALSE);
        assertResult("contains('abc', '')", XQueryBoolean.TRUE);
    }

    @Test
    public void startsWith() throws XQueryUnsupportedOperation {
        assertResult("starts-with('tattoo', 'tat')", XQueryBoolean.TRUE);
        assertResult("starts-with('tattoo', 'att')", XQueryBoolean.FALSE);
    }

    @Test
    public void endsWith() throws XQueryUnsupportedOperation {
        assertResult("ends-with('tattoo', 'oo')", XQueryBoolean.TRUE);
        assertResult("ends-with('tattoo', 'tatt')", XQueryBoolean.FALSE);
    }


    @Test
    public void lowercase() throws XQueryUnsupportedOperation {
        assertResult("lower-case('AbCdE')", new XQueryString("abcde"));
    }

    @Test
    public void uppercase() throws XQueryUnsupportedOperation {
        assertResult("upper-case('AbCdE')", new XQueryString("ABCDE"));
    }

    @Test
    public void substring() throws XQueryUnsupportedOperation {
        assertResult("substring('abcde', 4)", new XQueryString("de"));
        assertResult("substring('abcde', 3, 2)", new XQueryString("cd"));
    }

    @Test
    public void substringBefore() throws XQueryUnsupportedOperation {
        assertResult("substring-before('tattoo', 'attoo')", new XQueryString("t"));
        assertResult("substring-before('tattoo', 'tatto')", new XQueryString(""));
        assertResult("substring-before('abcde', 'f')", new XQueryString(""));
    }

    @Test
    public void substringAfter() throws XQueryUnsupportedOperation {
        assertResult("substring-after('tattoo', 'tat')", new XQueryString("too"));
        assertResult("substring-after('tattoo', 'tattoo')", new XQueryString(""));
        assertResult("substring-after('abcde', 'f')", new XQueryString(""));
    }

    public void rangeExpression() throws XQueryUnsupportedOperation {
        var i1 = baseFactory.number(1);
        var i2 = baseFactory.number(2);
        var i3 = baseFactory.number(3);
        var i4 = baseFactory.number(4);
        var i5 = baseFactory.number(5);
        assertResult("1 to 5", List.of(i1, i2, i3, i4, i5));
        assertResult("4 to 3", List.of());
        assertResult("3 to 3", List.of(i3));
    }


    @Test
    public void predicateExpression() throws XQueryUnsupportedOperation {
        // var i1 = new XQueryNumber(1);
        // var i2 = new XQueryNumber(2);
        // var i3 = new XQueryNumber(3);
        var i4 = new XQueryNumber(4);
        var i5 = new XQueryNumber(5);
        assertResult("(1, 2, 3, 4, 5)[. gt 3]", List.of(i4, i5));
    }

    @Test
    public void booleanToString() throws XQueryUnsupportedOperation {
        assertResult("string(true())", new XQueryString("true"));
        assertResult("string(false())", new XQueryString("false"));
    }

    @Test
    public void stringToString() throws XQueryUnsupportedOperation {
        assertResult("string('abc')", new XQueryString("abc"));
    }

    @Test
    public void numberToString() throws XQueryUnsupportedOperation {
        assertResult("string(1.2)", new XQueryString("1.2"));
    }


    @Test
    public void concat() throws XQueryUnsupportedOperation {
        assertResult("concat('a', 'b', 'c')", new XQueryString("abc"));
    }

    @Test
    public void stringJoin() throws XQueryUnsupportedOperation {
        assertResult("string-join(('a', 'b', 'c'))", new XQueryString("abc"));
        assertResult("string-join(('a', 'b', 'c'), '-')", new XQueryString("a-b-c"));
    }

    @Test
    public void stringLength() throws XQueryUnsupportedOperation {
        assertResult("string-length('abcde')", new XQueryNumber(5));
        assertResult("string-length('')", new XQueryNumber(0));
    }

    @Test
    public void normalization() throws XQueryUnsupportedOperation {
        assertResult("normalize-space(' \t\n\r a    b \t \t c   \t')", new XQueryString("a b c"));
    }

    @Test
    public void itemGetter() throws XQueryUnsupportedOperation {
        assertResult("(1, 2, 3)[2]",  new XQueryNumber(2));
    }

    @Test
    public void positionFunction() throws XQueryUnsupportedOperation {
        assertResult("(1, 2, 3)[position() eq 2][1]", new XQueryNumber(2));
        assertResult("(1, 2, 3)[position() eq 2]", List.of(new XQueryNumber(2)));
    }

    @Test
    public void lastFunction() throws XQueryUnsupportedOperation {
        assertResult("(1, 2, 3)[last()]", new XQueryNumber(3));
    }

    @Test
    public void arrowExpression() throws XQueryUnsupportedOperation {
        assertResult("'a' => string-length()", new XQueryNumber(1));
        assertResult("'a' => string-length() => string()", new XQueryString("1"));
    }

    @Test
    public void variableBinding() throws XQueryUnsupportedOperation {
        assertResult("let $x := 1 return $x", new XQueryNumber(1));
        assertResult("let $x := 'abc', $y := 1 return ($x, $y)",
                        List.of(new XQueryString("abc"), new XQueryNumber(1)));
    }

    @Test
    public void quantifiedExpression() throws XQueryUnsupportedOperation {
        assertResult("some $v in (1, 2, 3, 4) satisfies $v eq 3", XQueryBoolean.TRUE);
        assertResult("some $v in (1, 2, 3, 4) satisfies $v eq -1", XQueryBoolean.FALSE);
        assertResult("every $v in (1, 2, 3, 4) satisfies $v gt 0", XQueryBoolean.TRUE);
        assertResult("every $v in (1, 2, 3, 4) satisfies $v lt 4", XQueryBoolean.FALSE);
    }


    @Test
    public void forClause() throws XQueryUnsupportedOperation {
        assertResult("for $x in (1 to 5) return $x + 1",
                List.of(baseFactory.number(2),
                        baseFactory.number(3),
                        baseFactory.number(4),
                        baseFactory.number(5),
                        baseFactory.number(6)));
        assertResult("for $x in (1 to 5), for $y in (1, 2) return $x * $y",
                List.of(baseFactory.number(1),
                        baseFactory.number(2),
                        baseFactory.number(2),
                        baseFactory.number(4),
                        baseFactory.number(3),
                        baseFactory.number(6),
                        baseFactory.number(4),
                        baseFactory.number(8),
                        baseFactory.number(5),
                        baseFactory.number(10))
            );
    }


    @Test
    public void forClausePositionalVar() throws XQueryUnsupportedOperation {
        assertResult("for $x at $i in (1 to 5) return $i",
                List.of(baseFactory.number(1),
                        baseFactory.number(2),
                        baseFactory.number(3),
                        baseFactory.number(4),
                        baseFactory.number(5)));
    }

    @Test
    public void whereClause() throws XQueryUnsupportedOperation {
        assertResult("for $x in (1 to 5) where ($x mod 2) eq 0 return $x",
                List.of(baseFactory.number(2),
                        baseFactory.number(4)));
    }


// Wildcards

}
