package com.github.akruk.languagefeatures.evaluation.evaluationfunctiontests.thematic;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.values.XQueryError;
import com.github.akruk.antlrxquery.values.XQueryNumber;
import com.github.akruk.languagefeatures.evaluation.evaluationfunctiontests.FunctionsEvaluationTests;

public class CardinalityFunctions extends FunctionsEvaluationTests {

    @Test
    public void exactlyOne() {
        assertError("exactly-one(())", XQueryError.ExactlyOneWrongArity);
        assertResult("exactly-one(1) ", baseFactory.number(1));
    }

    @Test
    public void zeroOrOne() {
        assertError("zero-or-one((1, 2))", XQueryError.ZeroOrOneWrongArity);
        assertResult("zero-or-one(())", List.of());
        assertResult("zero-or-one(1)", baseFactory.number(1));
    }

    @Test
    public void oneOrMore() {
        assertError("one-or-more(())", XQueryError.OneOrMoreEmpty);
        assertResult(" one-or-more((1, 2)) ", List.of(baseFactory.number(1), new XQueryNumber(2, baseFactory)));
    }


}
