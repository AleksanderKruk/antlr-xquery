package com.github.akruk.antlrquery.languagefeatures.evaluation.functions.processingarrays;

import com.github.akruk.antlrquery.languagefeatures.evaluation.EvaluationTestsBase;
import org.junit.jupiter.api.Test;

public class ArrayJoinEvaluationTests extends EvaluationTestsBase {

    @Test
    public void arrayJoin_emptySequence() {
        assertResult(
                "array:join(())",
                valueFactory.array()
        );
    }

    @Test
    public void arrayJoin_singleArray() {
        assertResult(
                "array:join([1, 2, 3])",
                valueFactory.array(
                        valueFactory.number(1),
                        valueFactory.number(2),
                        valueFactory.number(3)
                )
        );
    }

    @Test
    public void arrayJoin_multipleArrays() {
        assertResult(
                "array:join(([1, 2], [3, 4]))",
                valueFactory.array(
                        valueFactory.number(1),
                        valueFactory.number(2),
                        valueFactory.number(3),
                        valueFactory.number(4)
                )
        );
    }

    @Test
    public void arrayJoin_differentArrayLengths() {
        assertResult(
                "array:join(([1, 2], [3]))",
                valueFactory.array(
                        valueFactory.number(1),
                        valueFactory.number(2),
                        valueFactory.number(3)
                )
        );
    }

    @Test
    public void arrayJoin_emptyArrayDoesNotAddMembers() {
        assertResult(
                "array:join(([1, 2], []))",
                valueFactory.array(
                        valueFactory.number(1),
                        valueFactory.number(2)
                )
        );
    }

    @Test
    public void arrayJoin_onlyEmptyArrays() {
        assertResult(
                "array:join(([], []))",
                valueFactory.array()
        );
    }

    @Test
    public void arrayJoin_nestedArraysAreNotFlattened() {
        assertResult(
                "array:join(([[1, 2]], [[3, 4]]))",
                valueFactory.array(
                        valueFactory.array(
                                valueFactory.number(1),
                                valueFactory.number(2)
                        ),
                        valueFactory.array(
                                valueFactory.number(3),
                                valueFactory.number(4)
                        )
                )
        );
    }

    @Test
    public void arrayJoin_stringArrays() {
        assertResult(
                "array:join(([\"a\", \"b\"], [\"c\"]))",
                valueFactory.array(
                        valueFactory.string("a"),
                        valueFactory.string("b"),
                        valueFactory.string("c")
                )
        );
    }

    @Test
    public void arrayJoin_mixedMemberTypes() {
        assertResult(
                "array:join(([1, 2], [\"x\"]))",
                valueFactory.array(
                        valueFactory.number(1),
                        valueFactory.number(2),
                        valueFactory.string("x")
                )
        );
    }

    @Test
    public void arrayJoin_sequenceMembersArePreserved() {
        assertResult(
                "array:join(([(1, 2)], [(3)]))",
                valueFactory.array(
                        valueFactory.sequence(
                                valueFactory.number(1),
                                valueFactory.number(2)
                        ),
                        valueFactory.sequence(
                                valueFactory.number(3)
                        )
                )
        );
    }
}
