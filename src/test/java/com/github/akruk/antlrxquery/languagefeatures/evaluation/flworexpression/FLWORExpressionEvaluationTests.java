package com.github.akruk.antlrxquery.languagefeatures.evaluation.flworexpression;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.evaluator.values.XQueryNumber;
import com.github.akruk.antlrxquery.evaluator.values.XQueryString;
import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;

public class FLWORExpressionEvaluationTests extends EvaluationTestsBase {

    @Test
    public void variableBinding() {
        assertResult("let $x := 1 return $x", new XQueryNumber(1, valueFactory));
        assertResult("let $x := 'abc', $y := 1 return ($x, $y)",
                List.of(new XQueryString("abc", valueFactory), new XQueryNumber(1, valueFactory)));
    }

    @Test
    public void forClause() {
        assertResult("for $x in (1 to 5) return $x + 1",
                List.of(valueFactory.number(2),
                        valueFactory.number(3),
                        valueFactory.number(4),
                        valueFactory.number(5),
                        valueFactory.number(6)));
        assertResult("for $x in (1 to 5), $y in (1, 2) return $x * $y",
                List.of(valueFactory.number(1),
                        valueFactory.number(2),
                        valueFactory.number(2),
                        valueFactory.number(4),
                        valueFactory.number(3),
                        valueFactory.number(6),
                        valueFactory.number(4),
                        valueFactory.number(8),
                        valueFactory.number(5),
                        valueFactory.number(10)));
    }

    @Test
    public void forMemberClause() {
        assertResult("for member $x in [1, 2, 3, 4, 5] return $x + 1",
                List.of(valueFactory.number(2),
                        valueFactory.number(3),
                        valueFactory.number(4),
                        valueFactory.number(5),
                        valueFactory.number(6)));
        assertResult("for member $x in [1, 2, 3, 4, 5], $y in (1, 2) return $x * $y",
                List.of(valueFactory.number(1),
                        valueFactory.number(2),
                        valueFactory.number(2),
                        valueFactory.number(4),
                        valueFactory.number(3),
                        valueFactory.number(6),
                        valueFactory.number(4),
                        valueFactory.number(8),
                        valueFactory.number(5),
                        valueFactory.number(10)));
    }

    @Test
    public void forKeyClause() {
        assertResult("for key $x in map {1: 'a', 2: 'b', 3: 'c', 4: 'd', 5: 'e'} return $x + 1",
                List.of(valueFactory.number(2),
                        valueFactory.number(3),
                        valueFactory.number(4),
                        valueFactory.number(5),
                        valueFactory.number(6)));
    }

    @Test
    public void forValueClause() {
        assertResult("for value $x in {1: 'a', 2: 'b', 3: 'c', 4: 'd', 5: 'e'} return $x",
                List.of(valueFactory.string("a"),
                        valueFactory.string("b"),
                        valueFactory.string("c"),
                        valueFactory.string("d"),
                        valueFactory.string("e")));
    }

    @Test
    public void forEntryClause() {
        assertResult("for key $x value $y in {1: 'a', 2: 'b', 3: 'c', 4: 'd', 5: 'e'} return $x || $y",
                List.of(valueFactory.string("1a"),
                        valueFactory.string("2b"),
                        valueFactory.string("3c"),
                        valueFactory.string("4d"),
                        valueFactory.string("5e")));
    }

    @Test
    public void forClausePositionalVar() {
        assertResult("for $x at $i in (1 to 5) return $i",
                List.of(valueFactory.number(1),
                        valueFactory.number(2),
                        valueFactory.number(3),
                        valueFactory.number(4),
                        valueFactory.number(5)));
    }

    @Test
    public void countClause() {
        assertResult("for $x in (1 to 5) count $count return $count",
                List.of(valueFactory.number(1),
                        valueFactory.number(2),
                        valueFactory.number(3),
                        valueFactory.number(4),
                        valueFactory.number(5)));
    }

    @Test
    public void whereClause() {
        assertResult("for $x in (1 to 5) where ($x mod 2) eq 0 return $x",
                List.of(valueFactory.number(2),
                        valueFactory.number(4)));
    }

    @Test
    public void whileClause() {
        assertResult("for $x in (1 to 5) while $x < 4 return $x",
                List.of(valueFactory.number(1),
                        valueFactory.number(2),
                        valueFactory.number(3)));
    }

    @Test
    public void orderByAscending() {
        assertResult("for $x in (2, 4, 3, 1) order by $x return $x",
                List.of(valueFactory.number(1),
                        valueFactory.number(2),
                        valueFactory.number(3),
                        valueFactory.number(4)));
        assertResult("for $x in (2, 4, 3, 1) order by $x ascending return $x",
                List.of(valueFactory.number(1),
                        valueFactory.number(2),
                        valueFactory.number(3),
                        valueFactory.number(4)));
    }

    @Test
    public void orderByDescending() {
        assertResult("for $x in (2, 4, 3, 1) order by $x descending return $x",
                List.of(valueFactory.number(4),
                        valueFactory.number(3),
                        valueFactory.number(2),
                        valueFactory.number(1)));
    }
}
