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
                valueFactory.sequence(List.of(valueFactory.number(1), valueFactory.number(2), valueFactory.number(3))),
                valueFactory.sequence(List.of(valueFactory.number(4), valueFactory.number(5), valueFactory.number(6))),
                valueFactory.sequence(List.of(valueFactory.number(7), valueFactory.number(8), valueFactory.number(9))),
                valueFactory.sequence(List.of(valueFactory.number(10))));
        XQueryValue expectedSequence = valueFactory.sequence(expected);
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
                valueFactory.sequence(List.of(valueFactory.number(1), valueFactory.number(2), valueFactory.number(3))),
                valueFactory.sequence(List.of(valueFactory.number(2), valueFactory.number(3), valueFactory.number(4))),
                valueFactory.sequence(List.of(valueFactory.number(3), valueFactory.number(4), valueFactory.number(5))),
                valueFactory.sequence(List.of(valueFactory.number(4), valueFactory.number(5), valueFactory.number(6))),
                valueFactory.sequence(List.of(valueFactory.number(5), valueFactory.number(6), valueFactory.number(7))),
                valueFactory.sequence(List.of(valueFactory.number(6), valueFactory.number(7), valueFactory.number(8))),
                valueFactory.sequence(List.of(valueFactory.number(7), valueFactory.number(8), valueFactory.number(9))),
                valueFactory.sequence(List.of(valueFactory.number(8), valueFactory.number(9), valueFactory.number(10))),
                valueFactory.sequence(List.of(valueFactory.number(9), valueFactory.number(10))),
                valueFactory.sequence(List.of(valueFactory.number(10))));
        XQueryValue expectedSequence = valueFactory.sequence(expected);
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
                valueFactory.sequence(List.of(valueFactory.number(1), valueFactory.number(3))),
                valueFactory.sequence(List.of(valueFactory.number(4), valueFactory.number(6))),
                valueFactory.sequence(List.of(valueFactory.number(7), valueFactory.number(9))),
                valueFactory.sequence(List.of(valueFactory.number(10), valueFactory.number(10))));
        XQueryValue expectedSequence = valueFactory.sequence(expected);
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
                valueFactory.sequence(List.of(valueFactory.number(1), valueFactory.number(3))),
                valueFactory.sequence(List.of(valueFactory.number(2), valueFactory.number(4))),
                valueFactory.sequence(List.of(valueFactory.number(3), valueFactory.number(5))),
                valueFactory.sequence(List.of(valueFactory.number(4), valueFactory.number(6))),
                valueFactory.sequence(List.of(valueFactory.number(5), valueFactory.number(7))),
                valueFactory.sequence(List.of(valueFactory.number(6), valueFactory.number(8))),
                valueFactory.sequence(List.of(valueFactory.number(7), valueFactory.number(9))),
                valueFactory.sequence(List.of(valueFactory.number(8), valueFactory.number(10))),
                valueFactory.sequence(List.of(valueFactory.number(9), valueFactory.number(10))),
                valueFactory.sequence(List.of(valueFactory.number(10), valueFactory.number(10))));
        XQueryValue expectedSequence = valueFactory.sequence(expected);
        assertTrue(deepEquals(expectedSequence, value));
    }

}
