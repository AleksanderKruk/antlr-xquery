package com.github.akruk.antlrquery.languagefeatures.evaluation.functions.processingarrays;

import com.github.akruk.antlrquery.languagefeatures.evaluation.EvaluationTestsBase;
import org.junit.jupiter.api.Test;

public class ArrayFootEvaluationTests extends EvaluationTestsBase {

    @Test
    public void arrayFoot_singleMember() {
        assertResult(
                "array:foot([1])",
                valueFactory.number(1)
        );
    }

    @Test
    public void arrayFoot_lastMember() {
        assertResult(
                "array:foot([1, 2, 3])",
                valueFactory.number(3)
        );
    }

    @Test
    public void arrayFoot_sequenceMember() {
        assertResult(
                "array:foot([1, (2, 3)])",
                valueFactory.sequence(
                        valueFactory.number(2),
                        valueFactory.number(3)
                )
        );
    }

    @Test
    public void arrayFoot_emptyMember() {
        assertResult(
                "array:foot([1, ()])",
                valueFactory.emptySequence()
        );
    }

    @Test
    public void arrayFoot_nestedArray() {
        assertResult(
                "array:foot([1, [2, 3]])",
                valueFactory.array(
                        valueFactory.number(2),
                        valueFactory.number(3)
                )
        );
    }

    @Test
    public void arrayFoot_nestedArrayIsNotFlattened() {
        assertResult(
                "array:foot([1, [[2, 3]]])",
                valueFactory.array(
                        valueFactory.array(
                                valueFactory.number(2),
                                valueFactory.number(3)
                        )
                )
        );
    }

    @Test
    public void arrayFoot_ignoresFollowingMembers() {
        assertResult(
                "array:foot([1, 'x', 3])",
                valueFactory.number(3)
        );
    }

    @Test
    public void arrayFoot_preservesOrder() {
        assertResult(
                "array:foot([1, (3, 1, 2)])",
                valueFactory.sequence(
                        valueFactory.number(3),
                        valueFactory.number(1),
                        valueFactory.number(2)
                )
        );
    }

    @Test
    public void arrayFoot_stringSequence() {
        assertResult(
                "array:foot([1, ('a', 'b')])",
                valueFactory.sequence(
                        valueFactory.string("a"),
                        valueFactory.string("b")
                )
        );
    }
}
