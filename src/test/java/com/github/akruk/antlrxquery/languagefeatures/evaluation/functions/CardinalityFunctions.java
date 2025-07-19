package com.github.akruk.antlrxquery.languagefeatures.evaluation.functions;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.evaluator.values.XQueryError;
import com.github.akruk.antlrxquery.evaluator.values.XQueryNumber;
import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;

public class CardinalityFunctions extends EvaluationTestsBase {

    @Test
    public void exactlyOne() {
        assertError("exactly-one(())", XQueryError.ExactlyOneWrongArity);
        assertResult("exactly-one(1) ", valueFactory.number(1));
    }

    @Test
    public void zeroOrOne() {
        assertError("zero-or-one((1, 2))", XQueryError.ZeroOrOneWrongArity);
        assertResult("zero-or-one(())", List.of());
        assertResult("zero-or-one(1)", valueFactory.number(1));
    }

    @Test
    public void oneOrMore() {
        assertError("one-or-more(())", XQueryError.OneOrMoreEmpty);
        assertResult(" one-or-more((1, 2)) ", List.of(valueFactory.number(1), new XQueryNumber(2, valueFactory)));
    }


}
