package com.github.akruk.antlrxquery;

import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.tree.xpath.XPath;
import org.junit.Test;

import com.github.akruk.antlrxquery.evaluator.XQuery;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.*;

public class XQueryEvaluatorTest extends EvaluationTestsBase {


    // TODO: add no grammar errors requirement

    @Test
    public void atomization() {
        final String xquery = "(1, (2,3,4), ((5, 6), 7))";
        final var value = XQuery.evaluate(null, xquery, null);
        final List<XQueryValue> expected = List.of(
                valueFactory.number(1),
                valueFactory.number(2),
                valueFactory.number(3),
                valueFactory.number(4),
                valueFactory.number(5),
                valueFactory.number(6),
                valueFactory.number(7));
        assertArrayEquals(
                expected.stream().map(v->v.numericValue).toArray(),
                value.sequence.stream().map(v->v.numericValue).toArray());
    }

    // TODO: rewrite
    // @Test
    // public void sequenceUnion() {
    //     String xquery = """
    //                 (1, 2, 3) | (4, 5, 6)
    //             """;
    //     var value = XQuery.evaluate(null, xquery, null);
    //     final var expected = List.of(
    //             valueFactory.number(1),
    //             valueFactory.number(2),
    //             valueFactory.number(3),
    //             valueFactory.number(4),
    //             valueFactory.number(5),
    //             valueFactory.number(6));
    //     assertEquals(expected.size(), value.sequence.size());
    //     var sequence = value.sequence;
    //     for (int i = 0; i < expected.size(); i++) {
    //         final var element = expected.get(i);
    //         final var received = sequence.get(i);
    //         assertEquals(element.numericValue, received.numericValue);
    //     }
    //     xquery = """
    //                 (1, 2, 3) union (4, 5, 6)
    //             """;
    //     value = XQuery.evaluate(null, xquery, null);
    //     assertEquals(expected.size(), value.sequence.size());
    //     sequence = value.sequence;
    //     for (int i = 0; i < expected.size(); i++) {
    //         final var element = expected.get(i);
    //         final var received = sequence.get(i);
    //         assertEquals(element.numericValue, received.numericValue);
    //     }
    // }

    // // TODO: rewrite
    // @Test
    // public void sequenceIntersection() {
    //     final String xquery = """
    //                 (1, 2, 3, 4) intersect (0, 2, 4, 8)
    //             """;
    //     final var value = XQuery.evaluate(null, xquery, null);
    //     final BigDecimal[] expected = {
    //             (BigDecimal.valueOf(2)),
    //             (BigDecimal.valueOf(4)),
    //     };
    //     final BigDecimal[] numbersFromSequence = value.sequence
    //             .stream()
    //             .map(v->v.numericValue)
    //             .toArray(BigDecimal[]::new);
    //     assertArrayEquals(expected, numbersFromSequence);
    // }

    // // TODO: rewrite
    // @Test
    // public void sequenceSubtraction() {
    //     final String xquery = """
    //                 (1, 2, 3, 4) except (2, 4)
    //             """;
    //     final var value = XQuery.evaluate(null, xquery, null);
    //     final BigDecimal[] expected = {
    //             BigDecimal.valueOf(1),
    //             BigDecimal.valueOf(3),
    //     };
    //     final BigDecimal[] numbersFromSequence = value.sequence
    //             .stream()
    //             .map(v->v.numericValue)
    //             .toArray(BigDecimal[]::new);
    //     assertArrayEquals(expected, numbersFromSequence);
    // }

    @Test
    public void concatenationExpressions() {
        assertResult("'abc' || 'def' || 'ghi'", valueFactory.string("abcdefghi"));
        assertResult("""
                () || "con" || ("cat", "enate")
                    """, valueFactory.string("concatenate"));
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
        final String textualTree = "a bc a d";
        final String xquery = "//*";
        final ValueParserAndTree parserAndTree = executeDynamicGrammarQueryWithTree(
            TEST_GRAMMAR_NAME, TEST_GRAMMAR, "test", textualTree, xquery);
        final ParseTree[] nodes = XPath.findAll(parserAndTree.tree(), xquery, parserAndTree.parser())
                .toArray(ParseTree[]::new);
        final ParseTree[] xqueryNodes = parserAndTree.value().sequence.stream().map(val -> val.node)
                .toArray(ParseTree[]::new);
        assertEquals(nodes.length, xqueryNodes.length);
        for (int i = 1; i < xqueryNodes.length; i++) {
            assertEquals(nodes[i], xqueryNodes[i]);
        }
    }

    // @Test
    // public void distinctValues() {
    //     var i1 = valueFactory.string("1", baseFactory);
    //     var i2 = valueFactory.string("2", baseFactory);
    //     assertResult("""
    //                 distinct-values((1, "1", 1, "1", "2", false(), false(), true(), true()))
    //             """, List.of(baseFactory.number(1), i1, i2, baseFactory.bool(false), baseFactory.bool(true)));
    //     assertResult("""
    //                 distinct-values(())
    //             """, List.of());
    // }

    @Test
    public void rangeExpression() {
        final var i1 = valueFactory.number(1);
        final var i2 = valueFactory.number(2);
        final var i3 = valueFactory.number(3);
        final var i4 = valueFactory.number(4);
        final var i5 = valueFactory.number(5);
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
        // var i1 = valueFactory.number(1);
        // var i2 = valueFactory.number(2);
        // var i3 = valueFactory.number(3);
        final var i4 = valueFactory.number(4);
        final var i5 = valueFactory.number(5);
        assertResult("(1, 2, 3, 4, 5)[. gt 3]", List.of(i4, i5));
    }

    @Test
    public void booleanToString() {
        assertResult("string(true())",  valueFactory.string("true" ));
        assertResult("string(false())", valueFactory.string("false"));
    }

    @Test
    public void stringToString() {
        assertResult("string('abc')", valueFactory.string("abc"));
    }

    @Test
    public void numberToString() {
        assertResult("string(1.2)", valueFactory.string("1.2"));
    }

    @Test
    public void itemGetter() {
        assertResult("(1, 2, 3)[2]", valueFactory.number(2));
    }

    @Test
    public void itemGetterIndices() {
        assertResult("(1, 2, 3, 4, 5, 6)[()]", List.of());
        assertResult("(1, 2, 3, 4, 5, 6)[3 to 5]", List.of(
            valueFactory.number(3),
            valueFactory.number(4),
            valueFactory.number(5)));
    }

    @Test
    public void positionFunction() {
        assertResult("(1, 2, 3)[position() eq 2][1]", valueFactory.number(2));
        assertResult("(1, 2, 3)[position() eq 2]", List.of(valueFactory.number(2)));
    }

    @Test
    public void lastFunction() {
        assertResult("(1, 2, 3)[last()]", valueFactory.number(3));
    }

    @Test
    public void arrowExpression() {
        assertResult("'a' => string-length()", valueFactory.number(1));
        assertResult("'a' => string-length() => string()", valueFactory.string("1"));
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
        final List<XQueryValue> $123 = List.of(valueFactory.number(1), valueFactory.number(2),
                valueFactory.number(3));
        assertResult("""
                    () otherwise 1
                """, valueFactory.number(1));
        assertResult("""
                    1 otherwise 2
                """, valueFactory.number(1));
        assertResult("""
                    "napis" otherwise 2
                """, valueFactory.string("napis"));
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
