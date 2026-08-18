package com.github.akruk.antlrquery.languagefeatures.evaluation.functions;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrquery.evaluator.values.AntlrQueryError;
import com.github.akruk.antlrquery.languagefeatures.evaluation.EvaluationTestsBase;

public class CardinalityFunctionsEvaluationTests extends EvaluationTestsBase {

    @Test
    public void exactlyOne() {
        assertError("exactly-one(())", valueFactory.error(AntlrQueryError.ExactlyOneWrongArity, ""));
        assertResult("exactly-one(1) ", valueFactory.number(1));
    }

    @Test
    public void zeroOrOne() {
        assertError("zero-or-one((1, 2))", valueFactory.error(AntlrQueryError.ZeroOrOneWrongArity, ""));
        assertResult("zero-or-one(())", List.of());
        assertResult("zero-or-one(1)", valueFactory.number(1));
    }

    @Test
    public void oneOrMore() {
        assertError("one-or-more(())", valueFactory.error(AntlrQueryError.OneOrMoreEmpty, ""));
        assertResult(" one-or-more((1, 2)) ", List.of(valueFactory.number(1), valueFactory.number(2)));
    }


}
