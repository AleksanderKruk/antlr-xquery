package com.github.akruk.antlrxquery;

import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.tree.xpath.XPath;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;

import com.github.akruk.antlrxquery.evaluator.XQuery;
import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;
import com.github.akruk.antlrxquery.values.XQueryNumber;
import com.github.akruk.antlrxquery.values.XQueryString;
import com.github.akruk.antlrxquery.values.XQueryValue;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;
import com.github.akruk.antlrxquery.values.factories.defaults.XQueryMemoizedValueFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

import static org.junit.Assert.*;

public class XQueryEvaluatorTest extends EvaluationTestsBase {



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

    // TODO: rewrite
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

    // TODO: rewrite
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

    // TODO: rewrite
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
    public void concatenationExpressions() {
        assertResult("'abc' || 'def' || 'ghi'", new XQueryString("abcdefghi", baseFactory));
        assertResult("""
                () || "con" || ("cat", "enate")
                    """, new XQueryString("concatenate", baseFactory));
    }


    private static final String TEST_GRAMMAR_NAME = "Test";
    private static final String TEST_GRAMMAR = """
        grammar Test;
        test: (A | rule)+;
        rule: B C | D;
        A: 'a';
        B: 'b';
        C: 'c';
        D: 'd';
        WS: [\\p{White_Space}]+ -> skip;
            """;



    @Test
    public void rootPath() throws Exception {
        // assert false;
        assertSameResultsAsAntlrXPath(TEST_GRAMMAR_NAME, TEST_GRAMMAR, "test", "a bc a d", "/test");
    }

    @Test
    public void rulePath() throws Exception {
        // assert false;
        assertSameResultsAsAntlrXPath(TEST_GRAMMAR_NAME, TEST_GRAMMAR, "test", "a bc a d", "/test/rule");
        assertSameResultsAsAntlrXPath(TEST_GRAMMAR_NAME, TEST_GRAMMAR, "test", "a bc a d", "/test//rule");
    }

    @Test
    public void tokenPath() throws Exception {
        // assert false;
        assertSameResultsAsAntlrXPath(TEST_GRAMMAR_NAME, TEST_GRAMMAR, "test", "a bc a d", "//A");
        assertSameResultsAsAntlrXPath(TEST_GRAMMAR_NAME, TEST_GRAMMAR, "test", "a bc a d", "//B");
        assertSameResultsAsAntlrXPath(TEST_GRAMMAR_NAME, TEST_GRAMMAR, "test", "a bc a d", "//C");
        assertSameResultsAsAntlrXPath(TEST_GRAMMAR_NAME, TEST_GRAMMAR, "test", "a bc a d", "//D");
    }

    @Test
    public void identityNodeComparison() throws Exception {
        assertDynamicGrammarQuery(TEST_GRAMMAR_NAME, TEST_GRAMMAR, "test", "a bc a d", "/test is /test",
                                    baseFactory.bool(true));
    }

    @Test
    public void beforeNode() throws Exception {
        assertDynamicGrammarQuery(TEST_GRAMMAR_NAME, TEST_GRAMMAR, "test", "a bc a d", "/test << /test",
                                    baseFactory.bool(false));
        assertDynamicGrammarQuery(TEST_GRAMMAR_NAME, TEST_GRAMMAR, "test",
                    "a bc a d", "/test << /test/A[1]", baseFactory.bool(true));
    }

    @Test
    public void afterNode() throws Exception {
        assertDynamicGrammarQuery(TEST_GRAMMAR_NAME, TEST_GRAMMAR, "test", "a bc a d", "/test >> /test", baseFactory.bool(false));
        assertDynamicGrammarQuery(TEST_GRAMMAR_NAME, TEST_GRAMMAR, "test", "a bc a d", "/test/A[1] >> /test", baseFactory.bool(true));
    }

    @Test
    public void wildcards() throws Exception {
        String textualTree = "a bc a d";
        String xquery = "//*";
        ValueParserAndTree parserAndTree = executeDynamicGrammarQueryWithTree(
            TEST_GRAMMAR_NAME, TEST_GRAMMAR, "test", textualTree, xquery);
        ParseTree[] nodes = XPath.findAll(parserAndTree.tree(), xquery, parserAndTree.parser())
                .toArray(ParseTree[]::new);
        ParseTree[] xqueryNodes = parserAndTree.value().sequence().stream().map(val -> val.node())
                .toArray(ParseTree[]::new);
        assertEquals(nodes.length, xqueryNodes.length);
        for (int i = 1; i < xqueryNodes.length; i++) {
            assertEquals(nodes[i], xqueryNodes[i]);
        }
    }

    @Test
    public void empty() {
        assertResult("empty(())", baseFactory.bool(true));
        assertResult("empty(1)", baseFactory.bool(false));
        assertResult("empty((1,2,3))", baseFactory.bool(false));
        assertResult("empty(\"\")", baseFactory.bool(false));
        assertResult("empty(\"abcd\")", baseFactory.bool(false));
        // The expression fn:empty([]) returns false().
        // assertResult("empty([])", baseFactory.bool(false));
        // The expression fn:empty({}) returns false().
        // assertResult("empty({})", baseFactory.bool(false));
        // Assuming $in is an element with no children:
        // let $break := <br/>
        // return fn:empty($break)
        // The result is false().
    }

    @Test
    public void exists() {
        assertResult("exists(())", baseFactory.bool(false));
        assertResult("exists((1,2,3))", baseFactory.bool(true));
        assertResult("exists(\"\")", baseFactory.bool(true));
        assertResult("exists(\"abcd\")", baseFactory.bool(true));
        // The expression fn:exists([]) returns true().
        // The expression fn:exists(map{}) returns true().
        // Assuming $in is an element with no children:
        // let $break :=
        // return fn:exists($break)
        // The result is true().
    }

    @Test
    public void head() {
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
    public void tail() {
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
    public void insertBefore() {
        var a = new XQueryString("a", baseFactory);
        var b = new XQueryString("b", baseFactory);
        var c = new XQueryString("c", baseFactory);
        var z = new XQueryString("z", baseFactory);
        // The expression fn:insert-before(("a", "b", "c"), 0, "z") returns ("z", "a",
        // "b", "c").
        assertResult("""
                insert-before(("a", "b", "c"), 0, "z")
                """, List.of(z, a, b, c));
        // The expression fn:insert-before(("a", "b", "c"), 1, "z") returns ("z", "a",
        // "b", "c").
        assertResult("""
                insert-before(("a", "b", "c"), 1, "z")
                """, List.of(z, a, b, c));
        // The expression fn:insert-before(("a", "b", "c"), 2, "z") returns ("a", "z",
        // "b", "c").
        assertResult("""
                insert-before(("a", "b", "c"), 2, "z")
                """, List.of(a, z, b, c));
        // The expression fn:insert-before(("a", "b", "c"), 3, "z") returns ("a", "b",
        // "z", "c").
        assertResult("""
                insert-before(("a", "b", "c"), 3, "z")
                """, List.of(a, b, z, c));
        // The expression fn:insert-before(("a", "b", "c"), 4, "z") returns ("a", "b",
        // "c", "z").
        assertResult("""
                insert-before(("a", "b", "c"), 4, "z")
                """, List.of(a, b, c, z));
    }

    @Test
    public void remove() {
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
    public void reverse() {
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
        // The expression fn:reverse([1,2,3]) returns [1,2,3]. (The input is a sequence
        // containing a single item (the array)).
        // The expression fn:reverse(([1,2,3],[4,5,6])) returns ([4,5,6],[1,2,3]).
    }

    @Test
    public void subsequence() {
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
    public void distinctValues() {
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
    public void rangeExpression() {
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
    public void predicateExpression() {
        // var i1 = new XQueryNumber(1);
        // var i2 = new XQueryNumber(2);
        // var i3 = new XQueryNumber(3);
        var i4 = new XQueryNumber(4, baseFactory);
        var i5 = new XQueryNumber(5, baseFactory);
        assertResult("(1, 2, 3, 4, 5)[. gt 3]", List.of(i4, i5));
    }

    @Test
    public void booleanToString() {
        assertResult("string(true())", new XQueryString("true", baseFactory));
        assertResult("string(false())", new XQueryString("false", baseFactory));
    }

    @Test
    public void stringToString() {
        assertResult("string('abc')", new XQueryString("abc", baseFactory));
    }

    @Test
    public void numberToString() {
        assertResult("string(1.2)", new XQueryString("1.2", baseFactory));
    }

    @Test
    public void itemGetter() {
        assertResult("(1, 2, 3)[2]", new XQueryNumber(2, baseFactory));
    }

    @Test
    public void itemGetterIndices() {
        assertResult("(1, 2, 3, 4, 5, 6)[()]", List.of());
        assertResult("(1, 2, 3, 4, 5, 6)[3 to 5]", List.of(new XQueryNumber(3, baseFactory),
                new XQueryNumber(4, baseFactory),
                new XQueryNumber(5, baseFactory)));
    }

    @Test
    public void positionFunction() {
        assertResult("(1, 2, 3)[position() eq 2][1]", new XQueryNumber(2, baseFactory));
        assertResult("(1, 2, 3)[position() eq 2]", List.of(new XQueryNumber(2, baseFactory)));
    }

    @Test
    public void lastFunction() {
        assertResult("(1, 2, 3)[last()]", new XQueryNumber(3, baseFactory));
    }

    @Test
    public void arrowExpression() {
        assertResult("'a' => string-length()", new XQueryNumber(1, baseFactory));
        assertResult("'a' => string-length() => string()", new XQueryString("1", baseFactory));
    }

    @Test
    public void quantifiedExpression() {
        assertResult("some $v in (1, 2, 3, 4) satisfies $v eq 3", baseFactory.bool(true));
        assertResult("some $v in (1, 2, 3, 4) satisfies $v eq -1", baseFactory.bool(false));
        assertResult("every $v in (1, 2, 3, 4) satisfies $v gt 0", baseFactory.bool(true));
        assertResult("every $v in (1, 2, 3, 4) satisfies $v lt 4", baseFactory.bool(false));
    }


    @Test
    public void switchExpression() {
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
    public void switchMulticaseExpression() {
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
    public void arithmeticPrecedence() {
        assertResult("""
                    2 + 3 * -4
                """, baseFactory.number(-10));
    }

    @Test
    public void otherwiseExpression() {
        final List<XQueryValue> $123 = List.of(new XQueryNumber(1, baseFactory), new XQueryNumber(2, baseFactory),
                new XQueryNumber(3, baseFactory));
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

    // Wildcards
    // All effective boolean values

}
