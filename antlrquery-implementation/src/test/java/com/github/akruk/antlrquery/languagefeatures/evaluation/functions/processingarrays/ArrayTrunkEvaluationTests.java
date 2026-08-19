package com.github.akruk.antlrquery.languagefeatures.evaluation.functions.processingarrays;

import com.github.akruk.antlrquery.languagefeatures.evaluation.EvaluationTestsBase;
import org.junit.jupiter.api.Test;

public class ArrayTrunkEvaluationTests extends EvaluationTestsBase {

    @Test
    public void arrayTrunk_removesLastMember() {
        assertResult(
                "array:trunk([1, 2, 3])",
                valueFactory.array(
                        valueFactory.number(1),
                        valueFactory.number(2)
                )
        );
    }

    @Test
    public void arrayTrunk_twoMembers() {
        assertResult(
                "array:trunk([1, 2])",
                valueFactory.array(
                        valueFactory.number(1)
                )
        );
    }

    @Test
    public void arrayTrunk_singleMemberBecomesEmptyArray() {
        assertResult(
                "array:trunk([1])",
                valueFactory.array()
        );
    }

    @Test
    public void arrayTrunk_preservesSequenceMembers() {
        assertResult(
                "array:trunk([(1, 2), 3, 4])",
                valueFactory.array(
                        valueFactory.sequence(
                                valueFactory.number(1),
                                valueFactory.number(2)
                        ),
                        valueFactory.number(3)
                )
        );
    }

    @Test
    public void arrayTrunk_preservesEmptyMember() {
        assertResult(
                "array:trunk([(), 1, 2])",
                valueFactory.array(
                        valueFactory.emptySequence(),
                        valueFactory.number(1)
                )
        );
    }

    @Test
    public void arrayTrunk_preservesNestedArrays() {
        assertResult(
                "array:trunk([[1, 2], [3, 4], [5, 6]])",
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
    public void arrayTrunk_preservesDeeplyNestedArrays() {
        assertResult(
                "array:trunk([[[1, 2]], [3, 4]])",
                valueFactory.array(
                        valueFactory.array(
                                valueFactory.array(
                                        valueFactory.number(1),
                                        valueFactory.number(2)
                                )
                        )
                )
        );
    }

    @Test
    public void arrayTrunk_doesNotFlattenNestedArrays() {
        assertResult(
                "array:trunk([[1, 2], 3, [4, 5]])",
                valueFactory.array(
                        valueFactory.array(
                                valueFactory.number(1),
                                valueFactory.number(2)
                        ),
                        valueFactory.number(3)
                )
        );
    }

    @Test
    public void arrayTrunk_preservesMemberOrder() {
        assertResult(
                "array:trunk([(3, 1, 2), 4, 5])",
                valueFactory.array(
                        valueFactory.sequence(
                                valueFactory.number(3),
                                valueFactory.number(1),
                                valueFactory.number(2)
                        ),
                        valueFactory.number(4)
                )
        );
    }
}
