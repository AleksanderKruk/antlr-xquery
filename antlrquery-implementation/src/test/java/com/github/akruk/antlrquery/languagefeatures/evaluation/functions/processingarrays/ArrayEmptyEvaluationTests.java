package com.github.akruk.antlrquery.languagefeatures.evaluation.functions.processingarrays;

import com.github.akruk.antlrquery.languagefeatures.evaluation.EvaluationTestsBase;
import org.junit.jupiter.api.Test;

public class ArrayEmptyEvaluationTests extends EvaluationTestsBase {

    @Test
    public void emptyArray() {
        assertResult(
                "array:empty(array{})",
                valueFactory.bool(true));
    }

    @Test
    public void nonEmptyArray() {
        assertResult(
                "array:empty(array{1})",
                valueFactory.bool(false));
    }

    @Test
    public void nonEmptyArrayWithMultipleMembers() {
        assertResult(
                "array:empty(array{1, 2, 3})",
                valueFactory.bool(false));
    }

    @Test
    public void arrayContainingEmptySequence() {
        assertResult(
                "array:empty([()])",
                valueFactory.bool(false));
        assertResult(
                "array:empty(array{()})",
                valueFactory.bool(true));
    }

    @Test
    public void arrayContainingEmptyArray() {
        assertResult(
                "array:empty(array{array{}})",
                valueFactory.bool(false));
    }

    @Test
    public void arrayContainingMultipleEmptyMembers() {
        assertResult(
                "array:empty([(), (), ()])",
                valueFactory.bool(false));
        assertResult(
                "array:empty(array{(), (), ()})",
                valueFactory.bool(true));
    }

    @Test
    public void arrayContainingSequence() {
        assertResult(
                "array:empty(array{(1, 2, 3)})",
                valueFactory.bool(false));
    }

    @Test
    public void nestedNonEmptyArray() {
        assertResult(
                "array:empty(array{array{1, 2}})",
                valueFactory.bool(false));
    }

    @Test
    public void nestedEmptyArray() {
        assertResult(
                "array:empty(array{array{}})",
                valueFactory.bool(false));
    }
}
