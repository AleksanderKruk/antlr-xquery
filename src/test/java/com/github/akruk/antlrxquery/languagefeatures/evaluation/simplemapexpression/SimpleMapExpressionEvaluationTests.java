package com.github.akruk.antlrxquery.languagefeatures.evaluation.simplemapexpression;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;

import java.util.List;

public class SimpleMapExpressionEvaluationTests extends EvaluationTestsBase {
    @Test
    public void simpleMapSingleValue() {
        // map on a single atomic value without let-binding
        assertResult(
                "1 ! (. + 1)",
                List.of(valueFactory.number(2)));
    }

    @Test
    public void simpleMapSingleValueNoOp() {
        // map on a single atomic value without let-binding
        assertResult(
                "1 ! .",
                List.of(valueFactory.number(1)));
    }

    @Test
    public void simpleMapOverSequence() {
        // add 1 to each item in a sequence directly
        String xquery = "(1, 2, 3) ! (. + 1)";
        List<XQueryValue> expected = List.of(
                valueFactory.number(2),
                valueFactory.number(3),
                valueFactory.number(4));
        assertResult(xquery, expected);
    }

    @Test
    public void chainedSimpleMapExpressions() {
        // multiply by 2 then add 1, chaining two map operators
        String xquery = "(1, 2) ! (. * 2) ! (. + 1)";
        List<XQueryValue> expected = List.of(
                valueFactory.number(3), // (1*2)+1
                valueFactory.number(5) // (2*2)+1
        );
        assertResult(xquery, expected);
    }

    @Test
    public void simpleMapWithStringFunctions() {
        // build strings, then measure their length via chained maps
        String xquery = "('a', 'bc') ! concat((., '-')) ! string-length(.)";
        List<XQueryValue> expected = List.of(
                valueFactory.number(2),
                valueFactory.number(3));
        assertResult(xquery, expected);
    }

}
