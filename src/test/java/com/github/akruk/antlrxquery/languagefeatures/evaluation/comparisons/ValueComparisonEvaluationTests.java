package com.github.akruk.antlrxquery.languagefeatures.evaluation.comparisons;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;

public class ValueComparisonEvaluationTests extends EvaluationTestsBase {

    @Test
    public void valueComparisonsEqual() {
        // A eq B numeric numeric op:numeric-equal(A, B) xs:boolean
        assertResult("1 eq 1", valueFactory.bool(true));
        // A eq B xs:boolean xs:boolean op:boolean-equal(A, B) xs:boolean
        assertResult("true() eq true()", valueFactory.bool(true));
        // A eq B xs:string xs:string op:numeric-equal(fn:compare(A, B), 0) xs:boolean
        assertResult("'abcd' eq 'abcd'", valueFactory.bool(true));
        // A le B xs:boolean xs:boolean fn:not(op:boolean-greater-than(A, B)) xs:boolean
    }

    @Test
    public void valueComparisonsNotEqual() {
        // A ne B numeric numeric fn:not(op:numeric-equal(A, B)) xs:boolean
        assertResult("1 ne 0", valueFactory.bool(true));
        // A ne B xs:boolean xs:boolean fn:not(op:boolean-equal(A, B)) xs:boolean
        assertResult("true() ne false()", valueFactory.bool(true));
        // A ne B xs:string xs:string fn:not(op:numeric-equal(fn:compare(A, B), 0))
        // xs:boolean
        assertResult("'abc' ne 'abcd'", valueFactory.bool(true));
    }

    @Test
    public void valueComparisonsGreaterThan() {
        // A gt B numeric numeric op:numeric-greater-than(A, B) xs:boolean
        assertResult("3 gt 1", valueFactory.bool(true));
        // A gt B xs:boolean xs:boolean op:boolean-greater-than(A, B) xs:boolean
        assertResult("true() gt false()", valueFactory.bool(true));
        assertResult("false() gt true()", valueFactory.bool(false));
        assertResult("true() gt true()", valueFactory.bool(false));
        assertResult("false() gt false()", valueFactory.bool(false));
        // A gt B xs:string xs:string op:numeric-greater-than(fn:compare(A, B), 0)
        // xs:boolean
        assertResult("'abed' gt 'abcd'", valueFactory.bool(true));
    }

    @Test
    public void valueComparisonsGreaterOrEqual() {
        // A ge B numeric numeric op:numeric-greater-than(A, B) or op:numeric-equal(A,
        // B) xs:boolean
        assertResult("3 ge 1", valueFactory.bool(true));
        assertResult("1 ge 1", valueFactory.bool(true));
        assertResult("0 ge 1", valueFactory.bool(false));
        // A ge B xs:boolean xs:boolean xs:boolean
        assertResult("true() ge false()", valueFactory.bool(true));
        assertResult("false() ge true()", valueFactory.bool(false));
        assertResult("true() ge true()", valueFactory.bool(true));
        assertResult("false() ge false()", valueFactory.bool(true));
        // A ge B xs:string xs:string xs:boolean
        assertResult("'abcd' ge 'abcd'", valueFactory.bool(true));
        assertResult("'abed' ge 'abcd'", valueFactory.bool(true));
    }

    @Test
    public void valueComparisonsLessOrEqual() {
        // A le B numeric numeric
        assertResult("1 le 3", valueFactory.bool(true));
        assertResult("1 le 1", valueFactory.bool(true));
        assertResult("1 le 0", valueFactory.bool(false));
        // A le B xs:boolean xs:boolean
        assertResult("true() le false()", valueFactory.bool(false));
        assertResult("false() le true()", valueFactory.bool(true));
        assertResult("true() le true()", valueFactory.bool(true));
        assertResult("false() le false()", valueFactory.bool(true));
        // A le B xs:string xs:string
        assertResult("'abed' le 'abcd'", valueFactory.bool(false));
        assertResult("'abcd' le 'abed'", valueFactory.bool(true));
        assertResult("'abcd' le 'abcd'", valueFactory.bool(true));
    }

    @Test
    public void valueComparisonsLessThan() {
        // A lt B numeric numeric op:numeric-less-than(A, B) xs:boolean
        assertResult("1 lt 3", valueFactory.bool(true));
        assertResult("1 lt 1", valueFactory.bool(false));
        assertResult("1 lt 0", valueFactory.bool(false));
        // A lt B xs:boolean xs:boolean op:boolean-less-than(A, B) xs:boolean
        assertResult("true() lt false()", valueFactory.bool(false));
        assertResult("false() lt true()", valueFactory.bool(true));
        assertResult("true() lt true()", valueFactory.bool(false));
        assertResult("false() lt false()", valueFactory.bool(false));
        // A lt B xs:string xs:string op:numeric-less-than(fn:compare(A, B), 0)
        // xs:boolean
        assertResult("'abed' lt 'abcd'", valueFactory.bool(false));
        assertResult("'abcd' lt 'abed'", valueFactory.bool(true));
        assertResult("'abcd' lt 'abcd'", valueFactory.bool(false));
    }


    @Test
    public void emptyOperandValueComparison() {
        assertResult("() eq ()", List.of());
        assertResult("1  eq ()", List.of());
        assertResult("() eq 1", List.of());
        assertResult("() ne ()", List.of());
        assertResult("1  ne ()", List.of());
        assertResult("() ne 1", List.of());
        assertResult("() lt ()", List.of());
        assertResult("1  lt ()", List.of());
        assertResult("() lt 1", List.of());
        assertResult("() gt ()", List.of());
        assertResult("1  gt ()", List.of());
        assertResult("() gt 1", List.of());
        assertResult("() le ()", List.of());
        assertResult("1  le ()", List.of());
        assertResult("() le 1", List.of());
        assertResult("() ge ()", List.of());
        assertResult("1  ge ()", List.of());
        assertResult("() ge 1", List.of());
    }

}
