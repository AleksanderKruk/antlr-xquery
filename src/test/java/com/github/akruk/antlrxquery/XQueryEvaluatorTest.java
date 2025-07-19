package com.github.akruk.antlrxquery;

import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.tree.xpath.XPath;
import org.junit.Test;

import com.github.akruk.antlrxquery.evaluator.XQuery;
import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;
import com.github.akruk.antlrxquery.values.XQueryNumber;
import com.github.akruk.antlrxquery.values.XQueryString;
import com.github.akruk.antlrxquery.values.XQueryValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.*;

public class XQueryEvaluatorTest extends EvaluationTestsBase {


    // TODO: add no grammar errors requirement

    @Test
    public void atomization() {
        String xquery = "(1, (2,3,4), ((5, 6), 7))";
        var value = XQuery.evaluate(null, xquery, null);
        List<XQueryValue> expected = List.of(
                valueFactory.number(1),
                valueFactory.number(2),
                valueFactory.number(3),
                valueFactory.number(4),
                valueFactory.number(5),
                valueFactory.number(6),
                valueFactory.number(7));
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
                valueFactory.number(1),
                valueFactory.number(2),
                valueFactory.number(3),
                valueFactory.number(4),
                valueFactory.number(5),
                valueFactory.number(6));
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
        assertResult("'abc' || 'def' || 'ghi'", new XQueryString("abcdefghi", valueFactory));
        assertResult("""
                () || "con" || ("cat", "enate")
                    """, new XQueryString("concatenate", valueFactory));
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
                                    valueFactory.bool(true));
    }

    @Test
    public void beforeNode() throws Exception {
        assertDynamicGrammarQuery(TEST_GRAMMAR_NAME, TEST_GRAMMAR, "test", "a bc a d", "/test << /test",
                                    valueFactory.bool(false));
        assertDynamicGrammarQuery(TEST_GRAMMAR_NAME, TEST_GRAMMAR, "test",
                    "a bc a d", "/test << /test/A[1]", valueFactory.bool(true));
    }

    @Test
    public void afterNode() throws Exception {
        assertDynamicGrammarQuery(TEST_GRAMMAR_NAME, TEST_GRAMMAR, "test", "a bc a d", "/test >> /test", valueFactory.bool(false));
        assertDynamicGrammarQuery(TEST_GRAMMAR_NAME, TEST_GRAMMAR, "test", "a bc a d", "/test/A[1] >> /test", valueFactory.bool(true));
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

    // @Test
    // public void distinctValues() {
    //     var i1 = new XQueryString("1", baseFactory);
    //     var i2 = new XQueryString("2", baseFactory);
    //     assertResult("""
    //                 distinct-values((1, "1", 1, "1", "2", false(), false(), true(), true()))
    //             """, List.of(baseFactory.number(1), i1, i2, baseFactory.bool(false), baseFactory.bool(true)));
    //     assertResult("""
    //                 distinct-values(())
    //             """, List.of());
    // }

    @Test
    public void rangeExpression() {
        var i1 = valueFactory.number(1);
        var i2 = valueFactory.number(2);
        var i3 = valueFactory.number(3);
        var i4 = valueFactory.number(4);
        var i5 = valueFactory.number(5);
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
        var i4 = new XQueryNumber(4, valueFactory);
        var i5 = new XQueryNumber(5, valueFactory);
        assertResult("(1, 2, 3, 4, 5)[. gt 3]", List.of(i4, i5));
    }

    @Test
    public void booleanToString() {
        assertResult("string(true())", new XQueryString("true", valueFactory));
        assertResult("string(false())", new XQueryString("false", valueFactory));
    }

    @Test
    public void stringToString() {
        assertResult("string('abc')", new XQueryString("abc", valueFactory));
    }

    @Test
    public void numberToString() {
        assertResult("string(1.2)", new XQueryString("1.2", valueFactory));
    }

    @Test
    public void itemGetter() {
        assertResult("(1, 2, 3)[2]", new XQueryNumber(2, valueFactory));
    }

    @Test
    public void itemGetterIndices() {
        assertResult("(1, 2, 3, 4, 5, 6)[()]", List.of());
        assertResult("(1, 2, 3, 4, 5, 6)[3 to 5]", List.of(new XQueryNumber(3, valueFactory),
                new XQueryNumber(4, valueFactory),
                new XQueryNumber(5, valueFactory)));
    }

    @Test
    public void positionFunction() {
        assertResult("(1, 2, 3)[position() eq 2][1]", new XQueryNumber(2, valueFactory));
        assertResult("(1, 2, 3)[position() eq 2]", List.of(new XQueryNumber(2, valueFactory)));
    }

    @Test
    public void lastFunction() {
        assertResult("(1, 2, 3)[last()]", new XQueryNumber(3, valueFactory));
    }

    @Test
    public void arrowExpression() {
        assertResult("'a' => string-length()", new XQueryNumber(1, valueFactory));
        assertResult("'a' => string-length() => string()", new XQueryString("1", valueFactory));
    }

    @Test
    public void quantifiedExpression() {
        assertResult("some $v in (1, 2, 3, 4) satisfies $v eq 3", valueFactory.bool(true));
        assertResult("some $v in (1, 2, 3, 4) satisfies $v eq -1", valueFactory.bool(false));
        assertResult("every $v in (1, 2, 3, 4) satisfies $v gt 0", valueFactory.bool(true));
        assertResult("every $v in (1, 2, 3, 4) satisfies $v lt 4", valueFactory.bool(false));
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
                """, valueFactory.bool(true));
        assertResult("""
                    switch (0)
                        case 3 return false()
                        case 1 return false()
                        case 5 return false()
                        case 4 return false()
                        default return true()
                """, valueFactory.bool(true));
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
                """, valueFactory.bool(true));
        assertResult("""
                    switch (0)
                        case 3 return false()
                        case 1 case 6 return false()
                        case 5 return false()
                        case 4 case 0 return true()
                        default return false()
                """, valueFactory.bool(true));
    }

    @Test
    public void arithmeticPrecedence() {
        assertResult("""
                    2 + 3 * -4
                """, valueFactory.number(-10));
    }

    @Test
    public void otherwiseExpression() {
        final List<XQueryValue> $123 = List.of(new XQueryNumber(1, valueFactory), new XQueryNumber(2, valueFactory),
                new XQueryNumber(3, valueFactory));
        assertResult("""
                    () otherwise 1
                """, new XQueryNumber(1, valueFactory));
        assertResult("""
                    1 otherwise 2
                """, new XQueryNumber(1, valueFactory));
        assertResult("""
                    "napis" otherwise 2
                """, new XQueryString("napis", valueFactory));
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
