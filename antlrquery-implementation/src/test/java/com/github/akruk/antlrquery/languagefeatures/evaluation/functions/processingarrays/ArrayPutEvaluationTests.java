package com.github.akruk.antlrquery.languagefeatures.evaluation.functions.processingarrays;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrquery.evaluator.values.AntlrQueryError;
import com.github.akruk.antlrquery.languagefeatures.evaluation.EvaluationTestsBase;

public class ArrayPutEvaluationTests extends EvaluationTestsBase {

    @Test
    public void replaceFirstMember() {
        assertResult(
                "array:put([1, 2, 3], 1, 10)",
                valueFactory.array(List.of(
                        valueFactory.number(10),
                        valueFactory.number(2),
                        valueFactory.number(3))));
    }

    @Test
    public void replaceMiddleMember() {
        assertResult(
                "array:put([1, 2, 3], 2, 10)",
                valueFactory.array(List.of(
                        valueFactory.number(1),
                        valueFactory.number(10),
                        valueFactory.number(3))));
    }

    @Test
    public void replaceLastMember() {
        assertResult(
                "array:put([1, 2, 3], 3, 10)",
                valueFactory.array(List.of(
                        valueFactory.number(1),
                        valueFactory.number(2),
                        valueFactory.number(10))));
    }

    @Test
    public void putSequenceMember() {
        assertResult(
                "array:put([1, 2, 3], 2, (10, 20))",
                valueFactory.array(List.of(
                        valueFactory.number(1),
                        valueFactory.sequence(List.of(
                                valueFactory.number(10),
                                valueFactory.number(20))),
                        valueFactory.number(3))));
    }

    @Test
    public void putEmptySequenceMember() {
        assertResult(
                "array:put([1, 2, 3], 2, ())",
                valueFactory.array(List.of(
                        valueFactory.number(1),
                        valueFactory.sequence(List.of()),
                        valueFactory.number(3))));
    }

    @Test
    public void putArrayAsMember() {
        assertResult(
                "array:put([1, 2, 3], 2, [10, 20])",
                valueFactory.array(List.of(
                        valueFactory.number(1),
                        valueFactory.array(List.of(
                                valueFactory.number(10),
                                valueFactory.number(20))),
                        valueFactory.number(3))));
    }

    @Test
    public void putDoesNotFlattenArrayMember() {
        assertResult(
                "array:put([1], 1, [2, 3])",
                valueFactory.array(List.of(
                        valueFactory.array(List.of(
                                valueFactory.number(2),
                                valueFactory.number(3))))));
    }

    @Test
    public void putNestedSequenceAndArray() {
        assertResult(
                "array:put([1], 1, ([2], 3))",
                valueFactory.array(List.of(
                        valueFactory.sequence(List.of(
                                valueFactory.array(List.of(
                                        valueFactory.number(2))),
                                valueFactory.number(3))))));
    }


    @Test
    public void positionBeyondEndRaisesError() {
        assertError(
                "array:put([1, 2, 3], 5, 10)",
                valueFactory.error(
                        AntlrQueryError.ArrayIndexOutOfBounds,
                        ""));
    }
}
