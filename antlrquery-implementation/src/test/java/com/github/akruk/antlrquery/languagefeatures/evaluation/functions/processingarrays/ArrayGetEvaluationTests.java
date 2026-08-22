package com.github.akruk.antlrquery.languagefeatures.evaluation.functions.processingarrays;

import com.github.akruk.antlrquery.evaluator.values.AntlrQueryError;
import com.github.akruk.antlrquery.languagefeatures.evaluation.EvaluationTestsBase;
import org.junit.jupiter.api.Test;

public class ArrayGetEvaluationTests extends EvaluationTestsBase {

    @Test
    public void arrayGet_singleArray_firstMember() {
        assertResult(
                """
                array:get([1, 2, 3], 1)
                """,
                valueFactory.number(1)
        );
    }

    @Test
    public void arrayGet_singleArray_middleMember() {
        assertResult(
                """
                array:get([1, 2, 3], 2)
                """,
                valueFactory.number(2)
        );
    }

    @Test
    public void arrayGet_singleArray_lastMember() {
        assertResult(
                """
                array:get([1, 2, 3], 3)
                """,
                valueFactory.number(3)
        );
    }

    @Test
    public void arrayGet_singleArray_outOfBounds_low() {
        assertError(
                """
                array:get([1, 2, 3], 0)
                """,
                valueFactory.error(AntlrQueryError.ArrayIndexOutOfBounds, "")
        );
    }

    @Test
    public void arrayGet_singleArray_outOfBounds_high() {
        assertError(
                """
                array:get([1, 2, 3], 4)
                """,
                valueFactory.error(AntlrQueryError.ArrayIndexOutOfBounds, "")
        );
    }

    @Test
    public void arrayGet_nestedArray_memberIsArray() {
        assertResult(
                """
                array:get([[1, 2], [3, 4]], 1)
                """,
                valueFactory.array(
                        valueFactory.number(1),
                        valueFactory.number(2)
                )
        );
    }

    @Test
    public void arrayGet_nestedArray_notFlattened() {
        assertResult(
                """
                array:get([[[1, 2]]], 1)
                """,
                valueFactory.array(
                        valueFactory.array(
                                valueFactory.number(1),
                                valueFactory.number(2)
                        )
                )
        );
    }

    @Test
    public void arrayGet_sequenceMembers() {
        assertResult(
                """
                array:get([(1, 2), (3, 4)], 1)
                """,
                valueFactory.sequence(
                        valueFactory.number(1),
                        valueFactory.number(2)
                )
        );
    }

    @Test
    public void arrayGet_stringArray() {
        assertResult(
                """
                array:get(["a", "b"], 2)
                """,
                valueFactory.string("b")
        );
    }

    @Test
    public void arrayGet_mixedTypes() {
        assertResult(
                """
                array:get([1, "x", true(), 5], 2)
                """,
                valueFactory.string("x")
        );
    }


    @Test
    public void arrayGet_positionDefinitelyOutOfRange_semanticNeverMeansRuntimeEmpty() {
        assertError(
                """
                array:get([1,2,3], 100)
                """,
                valueFactory.error(AntlrQueryError.ArrayIndexOutOfBounds, "")
        );
    }
}
