package com.github.akruk.antlrquery.languagefeatures.evaluation.functions.processingarrays;

import com.github.akruk.antlrquery.languagefeatures.evaluation.EvaluationTestsBase;
import org.junit.jupiter.api.Test;

public class ArrayFlattenEvaluationTests extends EvaluationTestsBase {

    @Test
    public void arrayFlatten_singleMember() {
        assertResult(
                "array:flatten([1])",
                valueFactory.sequence(
                        valueFactory.number(1)
                )
        );
    }

    @Test
    public void arrayFlatten_multipleMembers() {
        assertResult(
                "array:flatten([1, 2, 3])",
                valueFactory.sequence(
                        valueFactory.number(1),
                        valueFactory.number(2),
                        valueFactory.number(3)
                )
        );
    }

    @Test
    public void arrayFlatten_sequenceMember() {
        assertResult(
                "array:flatten([1, (2, 3)])",
                valueFactory.sequence(
                        valueFactory.number(1),
                        valueFactory.number(2),
                        valueFactory.number(3)
                )
        );
    }

    @Test
    public void arrayFlatten_multipleSequenceMembers() {
        assertResult(
                "array:flatten([(1, 2), (3, 4)])",
                valueFactory.sequence(
                        valueFactory.number(1),
                        valueFactory.number(2),
                        valueFactory.number(3),
                        valueFactory.number(4)
                )
        );
    }

    @Test
    public void arrayFlatten_emptyMember() {
        assertResult(
                "array:flatten([1, (), 2])",
                valueFactory.sequence(
                        valueFactory.number(1),
                        valueFactory.number(2)
                )
        );
    }

    @Test
    public void arrayFlatten_allMembersEmpty() {
        assertResult(
                "array:flatten([(), ()])",
                valueFactory.emptySequence()
        );
    }

    @Test
    public void arrayFlatten_nestedArray() {
        assertResult(
                "array:flatten([1, [2, 3]])",
                valueFactory.sequence(
                        valueFactory.number(1),
                        valueFactory.number(2),
                        valueFactory.number(3)
                )
        );
    }

    @Test
    public void arrayFlatten_nestedArrays() {
        assertResult(
                "array:flatten([[1, 2], [3, 4]])",
                valueFactory.sequence(
                        valueFactory.number(1),
                        valueFactory.number(2),
                        valueFactory.number(3),
                        valueFactory.number(4)
                )
        );
    }

    @Test
    public void arrayFlatten_nestedArrayIsRecursivelyFlattened() {
        assertResult(
                "array:flatten([[[1, 2]], 3])",
                valueFactory.sequence(
                        valueFactory.number(1),
                        valueFactory.number(2),
                        valueFactory.number(3)
                )
        );
    }

    @Test
    public void arrayFlatten_nestedArraysAreRecursivelyFlattened() {
        assertResult(
                "array:flatten([[[1]], [[2]]])",
                valueFactory.sequence(
                        valueFactory.number(1),
                        valueFactory.number(2)
                )
        );
    }

    @Test
    public void arrayFlatten_tupleMember() {
        assertResult(
                "array:flatten([(1, 2), 3])",
                valueFactory.sequence(
                        valueFactory.number(1),
                        valueFactory.number(2),
                        valueFactory.number(3)
                )
        );
    }

    @Test
    public void arrayFlatten_emptyTuple() {
        assertResult(
                "array:flatten([[], 1])",
                valueFactory.number(1)
        );
    }

    @Test
    public void arrayFlatten_nestedTuple() {
        assertResult(
                "array:flatten([[(1, 2), 3], 4])",
                valueFactory.sequence(
                        valueFactory.number(1),
                        valueFactory.number(2),
                        valueFactory.number(3),
                        valueFactory.number(4)
                )
        );
    }

    @Test
    public void arrayFlatten_preservesOrder() {
        assertResult(
                "array:flatten([(3, 1), [4, 2], 5])",
                valueFactory.sequence(
                        valueFactory.number(3),
                        valueFactory.number(1),
                        valueFactory.number(4),
                        valueFactory.number(2),
                        valueFactory.number(5)
                )
        );
    }

    @Test
    public void arrayFlatten_differentTypes() {
        assertResult(
                "array:flatten([1, 'x', true()])",
                valueFactory.sequence(
                        valueFactory.number(1),
                        valueFactory.string("x"),
                        valueFactory.bool(true)
                )
        );
    }

    @Test
    public void arrayFlatten_stringSequence() {
        assertResult(
                "array:flatten([1, ('a', 'b')])",
                valueFactory.sequence(
                        valueFactory.number(1),
                        valueFactory.string("a"),
                        valueFactory.string("b")
                )
        );
    }

    @Test
    public void arrayFlatten_mixedNestedContent() {
        assertResult(
                "array:flatten([[1, (2, 3)], 4, [[5, 6]]])",
                valueFactory.sequence(
                        valueFactory.number(1),
                        valueFactory.number(2),
                        valueFactory.number(3),
                        valueFactory.number(4),
                        valueFactory.number(5),
                        valueFactory.number(6)
                )
        );
    }
}
