package com.github.akruk.antlrquery.languagefeatures.evaluation.functions.processingarrays;

import com.github.akruk.antlrquery.languagefeatures.evaluation.EvaluationTestsBase;
import org.junit.jupiter.api.Test;

public class ArrayTailEvaluationTests extends EvaluationTestsBase {

    @Test
    public void arrayTail_removesFirstMember() {
        assertResult(
                "array:tail([1, 2, 3])",
                valueFactory.array(
                        valueFactory.number(2),
                        valueFactory.number(3)
                )
        );
    }

    @Test
    public void arrayTail_twoMembers() {
        assertResult(
                "array:tail([1, 2])",
                valueFactory.array(
                        valueFactory.number(2)
                )
        );
    }

    @Test
    public void arrayTail_singleMember() {
        assertResult(
                "array:tail([1])",
                valueFactory.array()
        );
    }

    @Test
    public void arrayTail_preservesSequenceMember() {
        assertResult(
                "array:tail([1, (2, 3), 4])",
                valueFactory.array(
                        valueFactory.sequence(
                                valueFactory.number(2),
                                valueFactory.number(3)
                        ),
                        valueFactory.number(4)
                )
        );
    }

    @Test
    public void arrayTail_preservesEmptyMember() {
        assertResult(
                "array:tail([1, (), 3])",
                valueFactory.array(
                        valueFactory.emptySequence(),
                        valueFactory.number(3)
                )
        );
    }

    @Test
    public void arrayTail_preservesNestedArrayMember() {
        assertResult(
                "array:tail([[1, 2], [3, 4], [5, 6]])",
                valueFactory.array(
                        valueFactory.array(
                                valueFactory.number(3),
                                valueFactory.number(4)
                        ),
                        valueFactory.array(
                                valueFactory.number(5),
                                valueFactory.number(6)
                        )
                )
        );
    }

    @Test
    public void arrayTail_preservesDeeplyNestedArray() {
        assertResult(
                "array:tail([1, [[2, 3]], 4])",
                valueFactory.array(
                        valueFactory.array(
                                valueFactory.array(
                                        valueFactory.number(2),
                                        valueFactory.number(3)
                                )
                        ),
                        valueFactory.number(4)
                )
        );
    }

    @Test
    public void arrayTail_doesNotFlattenNestedArrays() {
        assertResult(
                "array:tail([1, [2, 3], [4, 5]])",
                valueFactory.array(
                        valueFactory.array(
                                valueFactory.number(2),
                                valueFactory.number(3)
                        ),
                        valueFactory.array(
                                valueFactory.number(4),
                                valueFactory.number(5)
                        )
                )
        );
    }

    @Test
    public void arrayTail_preservesMemberOrder() {
        assertResult(
                "array:tail([1, (3, 2, 4), 5])",
                valueFactory.array(
                        valueFactory.sequence(
                                valueFactory.number(3),
                                valueFactory.number(2),
                                valueFactory.number(4)
                        ),
                        valueFactory.number(5)
                )
        );
    }
}
