package com.github.akruk.antlrxquery.languagefeatures.evaluation.flworexpression;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;
import com.github.akruk.antlrxquery.values.XQueryNumber;
import com.github.akruk.antlrxquery.values.XQueryString;

public class FLWORExpressionEvaluationTests extends EvaluationTestsBase {

    @Test
    public void variableBinding() {
        assertResult("let $x := 1 return $x", new XQueryNumber(1, baseFactory));
        assertResult("let $x := 'abc', $y := 1 return ($x, $y)",
                List.of(new XQueryString("abc", baseFactory), new XQueryNumber(1, baseFactory)));
    }

    @Test
    public void forClause() {
        assertResult("for $x in (1 to 5) return $x + 1",
                List.of(baseFactory.number(2),
                        baseFactory.number(3),
                        baseFactory.number(4),
                        baseFactory.number(5),
                        baseFactory.number(6)));
        assertResult("for $x in (1 to 5), $y in (1, 2) return $x * $y",
                List.of(baseFactory.number(1),
                        baseFactory.number(2),
                        baseFactory.number(2),
                        baseFactory.number(4),
                        baseFactory.number(3),
                        baseFactory.number(6),
                        baseFactory.number(4),
                        baseFactory.number(8),
                        baseFactory.number(5),
                        baseFactory.number(10)));
    }

    @Test
    public void forMemberClause() {
        assertResult("for member $x in [1, 2, 3, 4, 5] return $x + 1",
                List.of(baseFactory.number(2),
                        baseFactory.number(3),
                        baseFactory.number(4),
                        baseFactory.number(5),
                        baseFactory.number(6)));
        assertResult("for member $x in [1, 2, 3, 4, 5], $y in (1, 2) return $x * $y",
                List.of(baseFactory.number(1),
                        baseFactory.number(2),
                        baseFactory.number(2),
                        baseFactory.number(4),
                        baseFactory.number(3),
                        baseFactory.number(6),
                        baseFactory.number(4),
                        baseFactory.number(8),
                        baseFactory.number(5),
                        baseFactory.number(10)));
    }

    @Test
    public void forClausePositionalVar() {
        assertResult("for $x at $i in (1 to 5) return $i",
                List.of(baseFactory.number(1),
                        baseFactory.number(2),
                        baseFactory.number(3),
                        baseFactory.number(4),
                        baseFactory.number(5)));
    }

    @Test
    public void countClause() {
        assertResult("for $x in (1 to 5) count $count return $count",
                List.of(baseFactory.number(1),
                        baseFactory.number(2),
                        baseFactory.number(3),
                        baseFactory.number(4),
                        baseFactory.number(5)));
    }

    @Test
    public void whereClause() {
        assertResult("for $x in (1 to 5) where ($x mod 2) eq 0 return $x",
                List.of(baseFactory.number(2),
                        baseFactory.number(4)));
    }

    @Test
    public void whileClause() {
        assertResult("for $x in (1 to 5) while $x < 4 return $x",
                List.of(baseFactory.number(1),
                        baseFactory.number(2),
                        baseFactory.number(3)));
    }

    @Test
    public void orderByAscending() {
        assertResult("for $x in (2, 4, 3, 1) order by $x return $x",
                List.of(baseFactory.number(1),
                        baseFactory.number(2),
                        baseFactory.number(3),
                        baseFactory.number(4)));
        assertResult("for $x in (2, 4, 3, 1) order by $x ascending return $x",
                List.of(baseFactory.number(1),
                        baseFactory.number(2),
                        baseFactory.number(3),
                        baseFactory.number(4)));
    }

    @Test
    public void orderByDescending() {
        assertResult("for $x in (2, 4, 3, 1) order by $x descending return $x",
                List.of(baseFactory.number(4),
                        baseFactory.number(3),
                        baseFactory.number(2),
                        baseFactory.number(1)));
    }
}
