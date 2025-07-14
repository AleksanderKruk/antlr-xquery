package com.github.akruk.antlrxquery.languagefeatures.evaluation.simplemapexpression;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;
import com.github.akruk.antlrxquery.values.XQueryValue;

import java.util.List;

public class SimpleMapExpressionEvaluationTests extends EvaluationTestsBase {
    @Test
    public void simpleMapSingleValue() {
        // map on a single atomic value without let-binding
        assertResult(
                "1 ! (. + 1)",
                List.of(baseFactory.number(2)));
    }

    @Test
    public void simpleMapSingleValueNoOp() {
        // map on a single atomic value without let-binding
        assertResult(
                "1 ! .",
                List.of(baseFactory.number(1)));
    }

    @Test
    public void simpleMapOverSequence() {
        // add 1 to each item in a sequence directly
        String xquery = "(1, 2, 3) ! (. + 1)";
        List<XQueryValue> expected = List.of(
                baseFactory.number(2),
                baseFactory.number(3),
                baseFactory.number(4));
        assertResult(xquery, expected);
    }

    @Test
    public void chainedSimpleMapExpressions() {
        // multiply by 2 then add 1, chaining two map operators
        String xquery = "(1, 2) ! (. * 2) ! (. + 1)";
        List<XQueryValue> expected = List.of(
                baseFactory.number(3), // (1*2)+1
                baseFactory.number(5) // (2*2)+1
        );
        assertResult(xquery, expected);
    }

    @Test
    public void simpleMapWithStringFunctions() {
        // build strings, then measure their length via chained maps
        String xquery = "('a', 'bc') ! concat(., '-') ! string-length(.)";
        List<XQueryValue> expected = List.of(
                baseFactory.number(2),
                baseFactory.number(3));
        assertResult(xquery, expected);
    }

}
