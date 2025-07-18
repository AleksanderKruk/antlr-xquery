package com.github.akruk.antlrxquery.languagefeatures.evaluation.functions;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;

public class AggregateFunctions extends EvaluationTestsBase {

    @Test
    public void count() {
        assertResult("fn:count(())", valueFactory.number(0));
        assertResult("fn:count((1, 2, 3))", valueFactory.number(3));
    }

    @Test
    public void avg() {
        assertResult("fn:avg(())", List.of());
        assertResult("fn:avg((1, 2, 3))", valueFactory.number(BigDecimal.valueOf(2)));
    }

    @Test
    public void max() {
        assertResult("fn:max(())", List.of());
        assertResult("fn:max((1, 2, 3))", valueFactory.number(BigDecimal.valueOf(3)));
    }

    @Test
    public void min() {
        assertResult("fn:min(())", List.of());
        assertResult("fn:min((1, 2, 3))", valueFactory.number(BigDecimal.valueOf(1)));
    }

    @Test
    public void allEqual() {
        assertResult("fn:all-equal(())", valueFactory.bool(true));
        assertResult("fn:all-equal((1, 2, 3))", valueFactory.bool(false));
        assertResult("fn:all-equal((1, 1, 1))", valueFactory.bool(true));
        assertResult("fn:all-equal(('a', 'a'))", valueFactory.bool(true));
    }

    @Test
    public void allDifferent() {
        assertResult("fn:all-different(())", valueFactory.bool(true));
        assertResult("fn:all-different((1, 2, 3))", valueFactory.bool(true));
        assertResult("fn:all-different((1, 1, 1))", valueFactory.bool(false));
        assertResult("fn:all-different((1, 2, 2))", valueFactory.bool(false));
        assertResult("fn:all-different(('a', 'a'))", valueFactory.bool(false));
    }

}
