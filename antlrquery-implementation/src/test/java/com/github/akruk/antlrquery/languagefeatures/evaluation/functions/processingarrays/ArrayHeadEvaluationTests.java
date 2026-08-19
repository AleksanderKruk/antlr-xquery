package com.github.akruk.antlrquery.languagefeatures.evaluation.functions.processingarrays;

import com.github.akruk.antlrquery.languagefeatures.evaluation.EvaluationTestsBase;
import org.junit.jupiter.api.Test;

public class ArrayHeadEvaluationTests extends EvaluationTestsBase {

    @Test
    public void arrayHead_singleMember() {
        assertResult(
                "array:head([1])",
                valueFactory.number(1)
        );
    }

    @Test
    public void arrayHead_firstMember() {
        assertResult(
                "array:head([1, 2, 3])",
                valueFactory.number(1)
        );
    }

    @Test
    public void arrayHead_sequenceMember() {
        assertResult(
                "array:head([(1, 2), 3])",
                valueFactory.sequence(
                        valueFactory.number(1),
                        valueFactory.number(2)
                )
        );
    }

    @Test
    public void arrayHead_emptyFirstMember() {
        assertResult(
                "array:head([(), 1, 2])",
                valueFactory.emptySequence()
        );
    }

    @Test
    public void arrayHead_nestedArray() {
        assertResult(
                "array:head([[1, 2], 3])",
                valueFactory.array(
                        valueFactory.number(1),
                        valueFactory.number(2)
                )
        );
    }

    @Test
    public void arrayHead_nestedArrayIsNotFlattened() {
        assertResult(
                "array:head([[[1, 2]], 3])",
                valueFactory.array(
                        valueFactory.array(
                                valueFactory.number(1),
                                valueFactory.number(2)
                        )
                )
        );
    }

    @Test
    public void arrayHead_ignoresFollowingMembers() {
        assertResult(
                "array:head([1, 'x', true()])",
                valueFactory.number(1)
        );
    }

    @Test
    public void arrayHead_preservesOrder() {
        assertResult(
                "array:head([(3, 1, 2), 4, 5])",
                valueFactory.sequence(
                        valueFactory.number(3),
                        valueFactory.number(1),
                        valueFactory.number(2)
                )
        );
    }

    @Test
    public void arrayHead_stringSequence() {
        assertResult(
                "array:head([('a', 'b'), 'c'])",
                valueFactory.sequence(
                        valueFactory.string("a"),
                        valueFactory.string("b")
                )
        );
    }
}
