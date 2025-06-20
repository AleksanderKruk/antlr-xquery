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
import com.github.akruk.antlrxquery.values.XQueryNumber;
import com.github.akruk.antlrxquery.values.XQueryString;
import com.github.akruk.antlrxquery.values.XQueryValue;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;
import com.github.akruk.antlrxquery.values.factories.defaults.XQueryMemoizedValueFactory;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import static org.junit.Assert.*;

public class XQueryEvaluatorTest {
    XQueryValueFactory baseFactory = new XQueryMemoizedValueFactory();
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
            assertTrue(expected.valueEqual(received).booleanValue());
        }
    }


    public void assertResult(String xquery, XQueryValue result) throws XQueryUnsupportedOperation {
        XQueryValue value = XQuery.evaluate(null, xquery, null);
        assertNotNull(value);
        assertTrue(result.valueEqual(value).booleanValue());
    }


    public void assertResult(String xquery, String textualTree, XQueryValue result) throws XQueryUnsupportedOperation {
        TestParserAndTree parserAndTree = parseTestTree(textualTree);
        var value = XQuery.evaluate(parserAndTree.tree, xquery, parserAndTree.parser);
        assertNotNull(value);
        assertTrue(result.valueEqual(value).booleanValue());
    }


    @Test
    public void comments() {
        assertResult("(:comment:) 1", BigDecimal.ONE);
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
        String xquery = "(1, 2, 3)";
        var value = XQuery.evaluate(null, xquery, null);
        final List<XQueryValue> expected = List.of(
                baseFactory.number(1),
                baseFactory.number(2),
                baseFactory.number(3));
        assertNotNull(value);
        assertNotNull(value.sequence());
        var sequence = value.sequence();
        assertEquals(expected.size(), sequence.size());
        assertTrue(expected.get(0).numericValue().equals(sequence.get(0).numericValue()));
        assertTrue(expected.get(1).numericValue().equals(sequence.get(1).numericValue()));
        assertTrue(expected.get(2).numericValue().equals(sequence.get(2).numericValue()));
    }

    @Test
    public void atomization() {
        String xquery = "(1, (2,3,4), ((5, 6), 7))";
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
        assertResult("true()", baseFactory.bool(true));
    }

    @Test
    public void falseConstant() throws XQueryUnsupportedOperation {
        assertResult("false()", baseFactory.bool(false));
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
        assertResult("(1, 2) = (2, 3)", baseFactory.bool(true));
        assertResult("(1, 2) != (2, 3)", baseFactory.bool(true));
        assertResult("(1, 2) < (2, 3)", baseFactory.bool(true));
        assertResult("(1, 2) <= (2, 3)", baseFactory.bool(true));
        assertResult("(1, 2) > (2, 3)", baseFactory.bool(false));
        assertResult("(1, 2) >= (2, 3)", baseFactory.bool(true));
    }

    @Test
    public void emptyOperandValueComparison() throws XQueryUnsupportedOperation {
        assertResult("() eq ()", List.of());
        assertResult("1  eq ()", List.of());
        assertResult("() eq 1", List.of());
        assertResult("() ne ()", List.of());
        assertResult("1  ne ()", List.of());
        assertResult("() ne 1", List.of());
        assertResult("() lt ()", List.of());
        assertResult("1  lt ()", List.of());
        assertResult("() lt 1", List.of());
        assertResult("() gt ()", List.of());
        assertResult("1  gt ()", List.of());
        assertResult("() gt 1", List.of());
        assertResult("() le ()", List.of());
        assertResult("1  le ()", List.of());
        assertResult("() le 1", List.of());
        assertResult("() ge ()", List.of());
        assertResult("1  ge ()", List.of());
        assertResult("() ge 1", List.of());
    }

    @Test
    public void valueComparisonsEqual() throws XQueryUnsupportedOperation {
        // A eq B 	numeric 	numeric 	op:numeric-equal(A, B) 	xs:boolean
        assertResult("1 eq 1", baseFactory.bool(true));
        // A eq B 	xs:boolean 	xs:boolean 	op:boolean-equal(A, B) 	xs:boolean
        assertResult("true() eq true()", baseFactory.bool(true));
        // A eq B 	xs:string 	xs:string 	op:numeric-equal(fn:compare(A, B), 0) 	xs:boolean
        assertResult("'abcd' eq 'abcd'", baseFactory.bool(true));
        // A le B 	xs:boolean 	xs:boolean 	fn:not(op:boolean-greater-than(A, B)) 	xs:boolean
    }

    @Test
    public void valueComparisonsNotEqual() throws XQueryUnsupportedOperation {
        // A ne B 	numeric 	numeric 	fn:not(op:numeric-equal(A, B)) 	xs:boolean
        assertResult("1 ne 0", baseFactory.bool(true));
        // A ne B 	xs:boolean 	xs:boolean 	fn:not(op:boolean-equal(A, B)) 	xs:boolean
        assertResult("true() ne false()", baseFactory.bool(true));
        // A ne B 	xs:string 	xs:string 	fn:not(op:numeric-equal(fn:compare(A, B), 0)) 	xs:boolean
        assertResult("'abc' ne 'abcd'", baseFactory.bool(true));
    }

    @Test
    public void valueComparisonsGreaterThan() throws XQueryUnsupportedOperation {
        // A gt B 	numeric 	numeric 	op:numeric-greater-than(A, B) 	xs:boolean
        assertResult("3 gt 1", baseFactory.bool(true));
        // A gt B 	xs:boolean 	xs:boolean 	op:boolean-greater-than(A, B) 	xs:boolean
        assertResult("true() gt false()", baseFactory.bool(true));
        assertResult("false() gt true()", baseFactory.bool(false));
        assertResult("true() gt true()", baseFactory.bool(false));
        assertResult("false() gt false()", baseFactory.bool(false));
        // A gt B 	xs:string 	xs:string 	op:numeric-greater-than(fn:compare(A, B), 0) 	xs:boolean
        assertResult("'abed' gt 'abcd'", baseFactory.bool(true));
    }

    @Test
    public void valueComparisonsGreaterOrEqual() throws XQueryUnsupportedOperation {
        // A ge B 	numeric 	numeric 	op:numeric-greater-than(A, B) or op:numeric-equal(A, B) 	xs:boolean
        assertResult("3 ge 1", baseFactory.bool(true));
        assertResult("1 ge 1", baseFactory.bool(true));
        assertResult("0 ge 1", baseFactory.bool(false));
        // A ge B 	xs:boolean 	xs:boolean 	xs:boolean
        assertResult("true() ge false()", baseFactory.bool(true));
        assertResult("false() ge true()", baseFactory.bool(false));
        assertResult("true() ge true()", baseFactory.bool(true));
        assertResult("false() ge false()", baseFactory.bool(true));
        // A ge B 	xs:string 	xs:string   xs:boolean
        assertResult("'abcd' ge 'abcd'", baseFactory.bool(true));
        assertResult("'abed' ge 'abcd'", baseFactory.bool(true));
    }

    @Test
    public void valueComparisonsLessOrEqual() throws XQueryUnsupportedOperation {
        // A le B 	numeric 	numeric
        assertResult("1 le 3", baseFactory.bool(true));
        assertResult("1 le 1", baseFactory.bool(true));
        assertResult("1 le 0", baseFactory.bool(false));
        // A le B 	xs:boolean 	xs:boolean
        assertResult("true() le false()", baseFactory.bool(false));
        assertResult("false() le true()", baseFactory.bool(true));
        assertResult("true() le true()", baseFactory.bool(true));
        assertResult("false() le false()", baseFactory.bool(true));
        // A le B 	xs:string 	xs:string
        assertResult("'abed' le 'abcd'", baseFactory.bool(false));
        assertResult("'abcd' le 'abed'", baseFactory.bool(true));
        assertResult("'abcd' le 'abcd'", baseFactory.bool(true));
    }

    @Test
    public void valueComparisonsLessThan() throws XQueryUnsupportedOperation {
        // A lt B 	numeric 	numeric 	op:numeric-less-than(A, B) 	xs:boolean
        assertResult("1 lt 3", baseFactory.bool(true));
        assertResult("1 lt 1", baseFactory.bool(false));
        assertResult("1 lt 0", baseFactory.bool(false));
        // A lt B 	xs:boolean 	xs:boolean 	op:boolean-less-than(A, B) 	xs:boolean
        assertResult("true() lt false()", baseFactory.bool(false));
        assertResult("false() lt true()", baseFactory.bool(true));
        assertResult("true() lt true()", baseFactory.bool(false));
        assertResult("false() lt false()", baseFactory.bool(false));
        // A lt B 	xs:string 	xs:string 	op:numeric-less-than(fn:compare(A, B), 0) 	xs:boolean
        assertResult("'abed' lt 'abcd'", baseFactory.bool(false));
        assertResult("'abcd' lt 'abed'", baseFactory.bool(true));
        assertResult("'abcd' lt 'abcd'", baseFactory.bool(false));
    }

    @Test
    public void concatenationExpressions() throws XQueryUnsupportedOperation {
        assertResult("'abc' || 'def' || 'ghi'", new XQueryString("abcdefghi", baseFactory));
        assertResult("""
            () || "con" || ("cat", "enate")
                """, new XQueryString("concatenate", baseFactory));
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

    @Test
    public void namespaces() {
        assertResult("math:pi()", new BigDecimal(Math.PI));
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
        assertResult("/test is /test", "a bc a d", baseFactory.bool(true));
    }

    @Test
    public void beforeNode() throws XQueryUnsupportedOperation {
        assertResult("/test << /test", "a bc a d", baseFactory.bool(false));
        assertResult("/test << /test/A[1]", "a bc a d", baseFactory.bool(true));
    }

    @Test
    public void afterNode() throws XQueryUnsupportedOperation {
        assertResult("/test >> /test", "a bc a d", baseFactory.bool(false));
        assertResult("/test/A[1] >> /test", "a bc a d", baseFactory.bool(true));
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
        assertResult("empty(())", baseFactory.bool(true));
        assertResult("empty(1)", baseFactory.bool(false));
        assertResult("empty((1,2,3))", baseFactory.bool(false));
        assertResult("empty(\"\")", baseFactory.bool(false));
        assertResult("empty(\"abcd\")", baseFactory.bool(false));
        // The expression fn:empty([]) returns false().
        // The expression fn:empty(map{}) returns false().
        // Assuming $in is an element with no children:
        //        let $break := <br/>
        //        return fn:empty($break)
        // The result is false().
    }


    @Test
    public void exists() throws XQueryUnsupportedOperation {
        assertResult("exists(())", baseFactory.bool(false));
        assertResult("exists((1,2,3))", baseFactory.bool(true));
        assertResult("exists(\"\")", baseFactory.bool(true));
        assertResult("exists(\"abcd\")", baseFactory.bool(true));
        // The expression fn:exists([]) returns true().
        // The expression fn:exists(map{}) returns true().
        // Assuming $in is an element with no children:
        //                let $break :=
        //                return fn:exists($break)
        // The result is true().
    }


    @Test
    public void head() throws XQueryUnsupportedOperation {
        assertResult("head(())", baseFactory.emptySequence().sequence());
        assertResult("head((1,2,3))", baseFactory.number(1));
        assertResult("head(\"\")", baseFactory.emptySequence().sequence());
        assertResult("head(\"abcd\")", baseFactory.string("a"));
        // The expression fn:head(1 to 5) returns 1.
        // The expression fn:head(("a", "b", "c")) returns "a".
        // The expression fn:head(()) returns ().
        // The expression fn:head([1,2,3]) returns [1,2,3].
    }


    @Test
    public void tail() throws XQueryUnsupportedOperation {
        assertResult("tail(())", baseFactory.emptySequence().sequence());
        assertResult("tail((1,2,3))", List.of(baseFactory.number(2), baseFactory.number(3)));
        assertResult("tail(\"\")", baseFactory.emptySequence().sequence());
        assertResult("tail(\"abcd\")", baseFactory.string("bcd"));
        // The expression fn:head(1 to 5) returns 1.
        // The expression fn:head(("a", "b", "c")) returns "a".
        // The expression fn:head(()) returns ().
        // The expression fn:head([1,2,3]) returns [1,2,3].
    }

    @Test
    public void insertBefore() throws XQueryUnsupportedOperation {
        var a = new XQueryString("a", baseFactory);
        var b = new XQueryString("b", baseFactory);
        var c = new XQueryString("c", baseFactory);
        var z = new XQueryString("z", baseFactory);
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
        var a = new XQueryString("a", baseFactory);
        var b = new XQueryString("b", baseFactory);
        var c = new XQueryString("c", baseFactory);
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
        var a = new XQueryString("a", baseFactory);
        var b = new XQueryString("b", baseFactory);
        var c = new XQueryString("c", baseFactory);
        // The expression fn:reverse($abc) returns ("c", "b", "a").
        assertResult("""
                reverse(("a", "b", "c"))
                """, List.of(c, b, a));
        // The expression fn:reverse(("hello")) returns ("hello").
        assertResult("reverse((\"Hello\"))", List.of(new XQueryString("Hello", baseFactory)));
        // The expression fn:reverse(()) returns ().
        assertResult("reverse(())", List.of());
        // The expression fn:reverse([1,2,3]) returns [1,2,3]. (The input is a sequence containing a single item (the array)).
        // The expression fn:reverse(([1,2,3],[4,5,6])) returns ([4,5,6],[1,2,3]).
    }

    @Test
    public void subsequence() throws XQueryUnsupportedOperation {
        // var i1 = new XQueryString("item1");
        // var i2 = new XQueryString("item2");
        var i3 = new XQueryString("item3", baseFactory);
        var i4 = new XQueryString("item4", baseFactory);
        var i5 = new XQueryString("item5", baseFactory);
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
        var i1 = new XQueryString("1", baseFactory);
        var i2 = new XQueryString("2", baseFactory);
        assertResult("""
                distinct-values((1, "1", 1, "1", "2", false(), false(), true(), true()))
            """, List.of(baseFactory.number(1), i1, i2, baseFactory.bool(false), baseFactory.bool(true)));
        assertResult("""
                distinct-values(())
            """, List.of());
    }

    @Test
    public void zeroOrOne() throws XQueryUnsupportedOperation {
        var value = XQuery.evaluate(null, "zero-or-one((1, 2))", null);
        assertNull(value);
        assertResult("zero-or-one(())", List.of());
    }

    @Test
    public void oneOrMore() throws XQueryUnsupportedOperation {
        var value = XQuery.evaluate(null, "one-or-more(())", null);
        assertNull(value);
        assertResult(" one-or-more((1, 2)) ", List.of(baseFactory.number(1), new XQueryNumber(2, baseFactory)));
    }

    @Test
    public void data() throws XQueryUnsupportedOperation {
        assertResult("data(1)", List.of(baseFactory.number(1)));
        assertResult("data('a')", List.of(new XQueryString("a", baseFactory)));
    }

    @Test
    public void contains() throws XQueryUnsupportedOperation {
        assertResult("contains('abc', 'bc')", baseFactory.bool(true));
        assertResult("contains('', 'bc')", baseFactory.bool(false));
        assertResult("contains('abc', '')", baseFactory.bool(true));
    }

    @Test
    public void startsWith() throws XQueryUnsupportedOperation {
        assertResult("starts-with('tattoo', 'tat')", baseFactory.bool(true));
        assertResult("starts-with('tattoo', 'att')", baseFactory.bool(false));
    }

    @Test
    public void endsWith() throws XQueryUnsupportedOperation {
        assertResult("ends-with('tattoo', 'oo')", baseFactory.bool(true));
        assertResult("ends-with('tattoo', 'tatt')", baseFactory.bool(false));
    }


    @Test
    public void lowercase() throws XQueryUnsupportedOperation {
        assertResult("lower-case('AbCdE')", new XQueryString("abcde", baseFactory));
    }

    @Test
    public void uppercase() throws XQueryUnsupportedOperation {
        assertResult("upper-case('AbCdE')", new XQueryString("ABCDE", baseFactory));
    }

    @Test
    public void substring() throws XQueryUnsupportedOperation {
        assertResult("substring('abcde', 4)", new XQueryString("de", baseFactory));
        assertResult("substring('abcde', 3, 2)", new XQueryString("cd", baseFactory));
    }

    @Test
    public void substringBefore() throws XQueryUnsupportedOperation {
        assertResult("substring-before('tattoo', 'attoo')", new XQueryString("t", baseFactory));
        assertResult("substring-before('tattoo', 'tatto')", new XQueryString("", baseFactory));
        assertResult("substring-before('abcde', 'f')", new XQueryString("", baseFactory));
    }

    @Test
    public void substringAfter() throws XQueryUnsupportedOperation {
        assertResult("substring-after('tattoo', 'tat')", new XQueryString("too", baseFactory));
        assertResult("substring-after('tattoo', 'tattoo')", new XQueryString("", baseFactory));
        assertResult("substring-after('abcde', 'f')", new XQueryString("", baseFactory));
    }

    @Test
    public void rangeExpression() throws XQueryUnsupportedOperation {
        var i1 = baseFactory.number(1);
        var i2 = baseFactory.number(2);
        var i3 = baseFactory.number(3);
        var i4 = baseFactory.number(4);
        var i5 = baseFactory.number(5);
        assertResult("1 to 5", List.of(i1, i2, i3, i4, i5));
        assertResult("4 to 3", List.of());
        assertResult("3 to 3", List.of(i3));
        assertResult("4 to 3", List.of());
        assertResult("() to ()", List.of());
        assertResult("1 to ()", List.of());
        assertResult("() to 3", List.of());
    }


    @Test
    public void predicateExpression() throws XQueryUnsupportedOperation {
        // var i1 = new XQueryNumber(1);
        // var i2 = new XQueryNumber(2);
        // var i3 = new XQueryNumber(3);
        var i4 = new XQueryNumber(4, baseFactory);
        var i5 = new XQueryNumber(5, baseFactory);
        assertResult("(1, 2, 3, 4, 5)[. gt 3]", List.of(i4, i5));
    }

    @Test
    public void booleanToString() throws XQueryUnsupportedOperation {
        assertResult("string(true())", new XQueryString("true", baseFactory));
        assertResult("string(false())", new XQueryString("false", baseFactory));
    }

    @Test
    public void stringToString() throws XQueryUnsupportedOperation {
        assertResult("string('abc')", new XQueryString("abc", baseFactory));
    }

    @Test
    public void numberToString() throws XQueryUnsupportedOperation {
        assertResult("string(1.2)", new XQueryString("1.2", baseFactory));
    }


    @Test
    public void concat() throws XQueryUnsupportedOperation {
        assertResult("concat('a', 'b', 'c')", new XQueryString("abc", baseFactory));
    }

    @Test
    public void stringJoin() throws XQueryUnsupportedOperation {
        assertResult("string-join(('a', 'b', 'c'))", new XQueryString("abc", baseFactory));
        assertResult("string-join(('a', 'b', 'c'), '-')", new XQueryString("a-b-c", baseFactory));
    }

    @Test
    public void stringLength() throws XQueryUnsupportedOperation {
        assertResult("string-length('abcde')", new XQueryNumber(5, baseFactory));
        assertResult("string-length('')", new XQueryNumber(0, baseFactory));
    }

    @Test
    public void normalization() throws XQueryUnsupportedOperation {
        assertResult("normalize-space(' \t\n\r a    b \t \t c   \t')", new XQueryString("a b c", baseFactory));
    }

    @Test
    public void itemGetter() throws XQueryUnsupportedOperation {
        assertResult("(1, 2, 3)[2]",  new XQueryNumber(2, baseFactory));
    }

    @Test
    public void itemGetterIndices() throws XQueryUnsupportedOperation {
        assertResult("(1, 2, 3, 4, 5, 6)[()]", List.of());
        assertResult("(1, 2, 3, 4, 5, 6)[3 to 5]", List.of( new XQueryNumber(3, baseFactory),
                                                                   new XQueryNumber(4, baseFactory),
                                                                   new XQueryNumber(5, baseFactory)));
    }

    @Test
    public void positionFunction() throws XQueryUnsupportedOperation {
        assertResult("(1, 2, 3)[position() eq 2][1]", new XQueryNumber(2, baseFactory));
        assertResult("(1, 2, 3)[position() eq 2]", List.of(new XQueryNumber(2, baseFactory)));
    }

    @Test
    public void lastFunction() throws XQueryUnsupportedOperation {
        assertResult("(1, 2, 3)[last()]", new XQueryNumber(3, baseFactory));
    }

    @Test
    public void arrowExpression() throws XQueryUnsupportedOperation {
        assertResult("'a' => string-length()", new XQueryNumber(1, baseFactory));
        assertResult("'a' => string-length() => string()", new XQueryString("1", baseFactory));
    }

    @Test
    public void variableBinding() throws XQueryUnsupportedOperation {
        assertResult("let $x := 1 return $x", new XQueryNumber(1, baseFactory));
        assertResult("let $x := 'abc', $y := 1 return ($x, $y)",
                        List.of(new XQueryString("abc", baseFactory), new XQueryNumber(1, baseFactory)));
    }

    @Test
    public void quantifiedExpression() throws XQueryUnsupportedOperation {
        assertResult("some $v in (1, 2, 3, 4) satisfies $v eq 3", baseFactory.bool(true));
        assertResult("some $v in (1, 2, 3, 4) satisfies $v eq -1", baseFactory.bool(false));
        assertResult("every $v in (1, 2, 3, 4) satisfies $v gt 0", baseFactory.bool(true));
        assertResult("every $v in (1, 2, 3, 4) satisfies $v lt 4", baseFactory.bool(false));
    }


    @Test
    public void forClause() throws XQueryUnsupportedOperation {
        assertResult("for $x in (1 to 5) return $x + 1",
                List.of(baseFactory.number(2),
                        baseFactory.number(3),
                        baseFactory.number(4),
                        baseFactory.number(5),
                        baseFactory.number(6)));
        assertResult("for $x in (1 to 5), $y in (1, 2) return $x * $y",
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
    public void countClause() throws XQueryUnsupportedOperation {
        assertResult("for $x in (1 to 5) count $count return $count",
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

    @Test
    public void whileClause() throws XQueryUnsupportedOperation {
        assertResult("for $x in (1 to 5) while $x < 4 return $x",
                List.of(baseFactory.number(1),
                        baseFactory.number(2),
                        baseFactory.number(3)));
    }


    @Test
    public void orderByAscending() throws XQueryUnsupportedOperation {
        assertResult("for $x in (2, 4, 3, 1) order by $x return $x",
                List.of(baseFactory.number(1),
                        baseFactory.number(2),
                        baseFactory.number(3),
                        baseFactory.number(4)));
        assertResult("for $x in (2, 4, 3, 1) order by $x ascending return $x",
                List.of(baseFactory.number(1),
                        baseFactory.number(2),
                        baseFactory.number(3),
                        baseFactory.number(4)));
    }

    @Test
    public void orderByDescending() throws XQueryUnsupportedOperation {
        assertResult("for $x in (2, 4, 3, 1) order by $x descending return $x",
                List.of(baseFactory.number(4),
                        baseFactory.number(3),
                        baseFactory.number(2),
                        baseFactory.number(1)));
    }


    @Test
    public void ifExpression() throws XQueryUnsupportedOperation {
        assertResult("if ('non-empty-string') then 1 else 2", baseFactory.number(1));
        assertResult("if ('') then 1 else 2", baseFactory.number(2));
    }

    @Test
    public void shortIfExpression() throws XQueryUnsupportedOperation {
        assertResult("if ('non-empty-string') { 1 }", baseFactory.number(1));
        assertResult("if ('') { 1 }", List.of());
    }


    @Test
    public void switchExpression() throws XQueryUnsupportedOperation {
        assertResult("""
            switch (4)
                case 3 return false()
                case 1 return false()
                case 5 return false()
                case 4 return true()
                default return false()
        """, baseFactory.bool(true));
        assertResult("""
            switch (0)
                case 3 return false()
                case 1 return false()
                case 5 return false()
                case 4 return false()
                default return true()
        """, baseFactory.bool(true));
    }


    @Test
    public void switchMulticaseExpression() throws XQueryUnsupportedOperation {
        assertResult("""
            switch (4)
                case 3 return false()
                case 1 return false()
                case 5 return false()
                case 4 case 0 return true()
                default return false()
        """, baseFactory.bool(true));
        assertResult("""
            switch (0)
                case 3 return false()
                case 1 case 6 return false()
                case 5 return false()
                case 4 case 0 return true()
                default return false()
        """, baseFactory.bool(true));
    }


    @Test
    public void arithmeticPrecedence() throws XQueryUnsupportedOperation {
        assertResult("""
            2 + 3 * -4
        """, baseFactory.number(-10));
    }

    @Test
    public void otherwiseExpression() throws XQueryUnsupportedOperation {
        final List<XQueryValue> $123 = List.of(new XQueryNumber(1, baseFactory), new XQueryNumber(2, baseFactory), new XQueryNumber(3, baseFactory));
        assertResult("""
                    () otherwise 1
                """, new XQueryNumber(1, baseFactory));
        assertResult("""
                    1 otherwise 2
                """, new XQueryNumber(1, baseFactory));
        assertResult("""
                    "napis" otherwise 2
                """, new XQueryString("napis", baseFactory));
        assertResult("""
                    () otherwise () otherwise (1, 2, 3)
                """, $123);
        assertResult("""
                    (1, 2, 3) otherwise (4, 5, 6) otherwise (7, 8, 9)
                """, $123);
    }

    @Test
    public void simpleMapSingleValue() throws XQueryUnsupportedOperation {
        // map on a single atomic value without let-binding
        assertResult(
            "1 ! (. + 1)",
            List.of(baseFactory.number(2))
        );
    }


    @Test
    public void simpleMapSingleValueNoOp() throws XQueryUnsupportedOperation {
        // map on a single atomic value without let-binding
        assertResult(
                "1 ! .",
                List.of(baseFactory.number(1)));
    }


    @Test
    public void simpleMapOverSequence() throws XQueryUnsupportedOperation {
        // add 1 to each item in a sequence directly
        String xquery = "(1, 2, 3) ! (. + 1)";
        List<XQueryValue> expected = List.of(
            baseFactory.number(2),
            baseFactory.number(3),
            baseFactory.number(4)
        );
        assertResult(xquery, expected);
    }

    @Test
    public void chainedSimpleMapExpressions() throws XQueryUnsupportedOperation {
        // multiply by 2 then add 1, chaining two map operators
        String xquery = "(1, 2) ! (. * 2) ! (. + 1)";
        List<XQueryValue> expected = List.of(
            baseFactory.number(3),  // (1*2)+1
            baseFactory.number(5)   // (2*2)+1
        );
        assertResult(xquery, expected);
    }

    @Test
    public void simpleMapWithStringFunctions() throws XQueryUnsupportedOperation {
        // build strings, then measure their length via chained maps
        String xquery = "('a', 'bc') ! concat(., '-') ! string-length(.)";
        List<XQueryValue> expected = List.of(
            baseFactory.number(2),
            baseFactory.number(3)
        );
        assertResult(xquery, expected);
    }

    // Wildcards
    // All effective boolean values

}
