package com.github.akruk.antlrxquery.languagefeatures.evaluation.windowexpression;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.evaluator.XQuery;
import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;
import com.github.akruk.antlrxquery.values.XQueryValue;

public class WindowExpressionEvaluationTests extends EvaluationTestsBase {

    @Test
    public void tumblingWindowTest() {
        String xquery = """
                for tumbling window $w in (1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                    start at $s when true()
                    end at $e when $e - $s eq 2
                    return $w
                """;
        XQueryValue value = XQuery.evaluate(null, xquery, null);
        List<XQueryValue> expected = Arrays.asList(
                baseFactory.sequence(List.of(baseFactory.number(1), baseFactory.number(2), baseFactory.number(3))),
                baseFactory.sequence(List.of(baseFactory.number(4), baseFactory.number(5), baseFactory.number(6))),
                baseFactory.sequence(List.of(baseFactory.number(7), baseFactory.number(8), baseFactory.number(9))),
                baseFactory.sequence(List.of(baseFactory.number(10))));
        XQueryValue expectedSequence = baseFactory.sequence(expected);
        assertTrue(deepEquals(expectedSequence, value));
    }

    @Test
    public void slidingWindowTest() {
        String xquery = """
                for sliding window $w in (1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                    start at $s when true()
                    end at $e when $e - $s eq 2
                    return $w
                """;
        XQueryValue value = XQuery.evaluate(null, xquery, null);
        List<XQueryValue> expected = Arrays.asList(
                baseFactory.sequence(List.of(baseFactory.number(1), baseFactory.number(2), baseFactory.number(3))),
                baseFactory.sequence(List.of(baseFactory.number(2), baseFactory.number(3), baseFactory.number(4))),
                baseFactory.sequence(List.of(baseFactory.number(3), baseFactory.number(4), baseFactory.number(5))),
                baseFactory.sequence(List.of(baseFactory.number(4), baseFactory.number(5), baseFactory.number(6))),
                baseFactory.sequence(List.of(baseFactory.number(5), baseFactory.number(6), baseFactory.number(7))),
                baseFactory.sequence(List.of(baseFactory.number(6), baseFactory.number(7), baseFactory.number(8))),
                baseFactory.sequence(List.of(baseFactory.number(7), baseFactory.number(8), baseFactory.number(9))),
                baseFactory.sequence(List.of(baseFactory.number(8), baseFactory.number(9), baseFactory.number(10))),
                baseFactory.sequence(List.of(baseFactory.number(9), baseFactory.number(10))),
                baseFactory.sequence(List.of(baseFactory.number(10))));
        XQueryValue expectedSequence = baseFactory.sequence(expected);
        assertTrue(deepEquals(expectedSequence, value));
    }

    @Test
    public void tumblingWindowWithPositionalVariablesTest() {
        String xquery = """
                    for tumbling window $w in (1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                            start $s at $sPos end $e at $ePos when $ePos - $sPos eq 2
                        return ($s, $e)
                """;
        XQueryValue value = XQuery.evaluate(null, xquery, null);
        List<XQueryValue> expected = Arrays.asList(
                baseFactory.sequence(List.of(baseFactory.number(1), baseFactory.number(3))),
                baseFactory.sequence(List.of(baseFactory.number(4), baseFactory.number(6))),
                baseFactory.sequence(List.of(baseFactory.number(7), baseFactory.number(9))),
                baseFactory.sequence(List.of(baseFactory.number(10), baseFactory.number(10))));
        XQueryValue expectedSequence = baseFactory.sequence(expected);
        assertTrue(deepEquals(expectedSequence, value));
    }

    @Test
    public void slidingWindowWithPositionalVariablesTest() {
        String xquery = """
                    for sliding window $w in (1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                        start $s at $sPos
                        end $e at $ePos when $ePos - $sPos eq 2
                    return ($s, $e)
                """;
        XQueryValue value = XQuery.evaluate(null, xquery, null);
        List<XQueryValue> expected = Arrays.asList(
                baseFactory.sequence(List.of(baseFactory.number(1), baseFactory.number(3))),
                baseFactory.sequence(List.of(baseFactory.number(2), baseFactory.number(4))),
                baseFactory.sequence(List.of(baseFactory.number(3), baseFactory.number(5))),
                baseFactory.sequence(List.of(baseFactory.number(4), baseFactory.number(6))),
                baseFactory.sequence(List.of(baseFactory.number(5), baseFactory.number(7))),
                baseFactory.sequence(List.of(baseFactory.number(6), baseFactory.number(8))),
                baseFactory.sequence(List.of(baseFactory.number(7), baseFactory.number(9))),
                baseFactory.sequence(List.of(baseFactory.number(8), baseFactory.number(10))),
                baseFactory.sequence(List.of(baseFactory.number(9), baseFactory.number(10))),
                baseFactory.sequence(List.of(baseFactory.number(10), baseFactory.number(10))));
        XQueryValue expectedSequence = baseFactory.sequence(expected);
        assertTrue(deepEquals(expectedSequence, value));
    }

}
