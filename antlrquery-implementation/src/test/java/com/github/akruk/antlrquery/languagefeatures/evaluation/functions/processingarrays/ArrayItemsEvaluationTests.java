package com.github.akruk.antlrquery.languagefeatures.evaluation.functions.processingarrays;

import com.github.akruk.antlrquery.languagefeatures.evaluation.EvaluationTestsBase;
import org.junit.jupiter.api.Test;

public class ArrayItemsEvaluationTests extends EvaluationTestsBase {

    @Test
    public void arrayItems_emptyArray() {
        assertResult(
                "array:items([])",
                valueFactory.emptySequence()
        );
    }

    @Test
    public void arrayItems_singleMember() {
        assertResult(
                "array:items([1])",
                valueFactory.number(1)
        );
    }

    @Test
    public void arrayItems_multipleMembers() {
        assertResult(
                "array:items([1, 2, 3])",
                valueFactory.sequence(
                        valueFactory.number(1),
                        valueFactory.number(2),
                        valueFactory.number(3)
                )
        );
    }

    @Test
    public void arrayItems_sequenceMember() {
        assertResult(
                "array:items([1, (2, 3), 4])",
                valueFactory.sequence(
                        valueFactory.number(1),
                        valueFactory.number(2),
                        valueFactory.number(3),
                        valueFactory.number(4)
                )
        );
    }

    @Test
    public void arrayItems_emptyMembers() {
        assertResult(
                "array:items([(), 1, (), 2, ()])",
                valueFactory.sequence(
                        valueFactory.number(1),
                        valueFactory.number(2)
                )
        );
    }

    @Test
    public void arrayItems_multipleSequenceMembers() {
        assertResult(
                "array:items([(1, 2), (3, 4, 5), (6)])",
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

    @Test
    public void arrayItems_preservesOrder() {
        assertResult(
                "array:items([3, 1, 2])",
                valueFactory.sequence(
                        valueFactory.number(3),
                        valueFactory.number(1),
                        valueFactory.number(2)
                )
        );
    }

    @Test
    public void arrayItems_mixedTypes() {
        assertResult(
                "array:items([1, 'x', true()])",
                valueFactory.sequence(
                        valueFactory.number(1),
                        valueFactory.string("x"),
                        valueFactory.bool(true)
                )
        );
    }

    @Test
    public void arrayItems_doesNotFlattenNestedArray() {
        assertResult(
                "array:items([1, [2, 3]])",
                valueFactory.sequence(
                        valueFactory.number(1),
                        valueFactory.array(
                                valueFactory.number(2),
                                valueFactory.number(3)
                        )
                )
        );
    }

    @Test
    public void arrayItems_nestedArraysRemainMembers() {
        assertResult(
                "array:items([[1, 2], [3, 4]])",
                valueFactory.sequence(
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
    public void arrayItems_specExample() {
        assertResult(
                "array:items([(), 1, (2 to 4), [5]])",
                valueFactory.sequence(
                        valueFactory.number(1),
                        valueFactory.number(2),
                        valueFactory.number(3),
                        valueFactory.number(4),
                        valueFactory.array(
                                valueFactory.number(5)
                        )
                )
        );
    }
}
