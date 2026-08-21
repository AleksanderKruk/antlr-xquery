package com.github.akruk.antlrquery.languagefeatures.evaluation.functions.processingarrays;

import com.github.akruk.antlrquery.languagefeatures.evaluation.EvaluationTestsBase;
import org.junit.jupiter.api.Test;

public class ArraySortEvaluationTests extends EvaluationTestsBase {

    @Test
    public void arraySort_sortsNumbers() {
        assertResult(
                "array:sort([1, 4, 6, 5, 3])",
                valueFactory.array(
                        valueFactory.number(1),
                        valueFactory.number(3),
                        valueFactory.number(4),
                        valueFactory.number(5),
                        valueFactory.number(6)
                )
        );
    }

    @Test
    public void arraySort_sortsNegativeNumbers() {
        assertResult(
                "array:sort([1, -2, 5, 10, -10, 10, 8])",
                valueFactory.array(
                        valueFactory.number(-10),
                        valueFactory.number(-2),
                        valueFactory.number(1),
                        valueFactory.number(5),
                        valueFactory.number(8),
                        valueFactory.number(10),
                        valueFactory.number(10)
                )
        );
    }

    @Test
    public void arraySort_sortsUsingKeyFunction() {
        assertResult(
                "array:sort([1, -2, 5, 10, -10, 10, 8], (), abs#1)",
                valueFactory.array(
                        valueFactory.number(1),
                        valueFactory.number(-2),
                        valueFactory.number(5),
                        valueFactory.number(8),
                        valueFactory.number(10),
                        valueFactory.number(-10),
                        valueFactory.number(10)
                )
        );
    }

    @Test
    public void arraySort_preservesNestedArrays() {
        assertResult(
                "array:sort([ [2, 'i'], [1, 'e'], [2, 'g'], [1, 'f'] ])",
                valueFactory.array(
                        valueFactory.array(
                                valueFactory.number(1),
                                valueFactory.string("e")
                        ),
                        valueFactory.array(
                                valueFactory.number(1),
                                valueFactory.string("f")
                        ),
                        valueFactory.array(
                                valueFactory.number(2),
                                valueFactory.string("g")
                        ),
                        valueFactory.array(
                                valueFactory.number(2),
                                valueFactory.string("i")
                        )
                )
        );
    }

    @Test
    public void arraySort_preservesSequenceMembers() {
        assertResult(
                "array:sort([(3, 1), (2, 4), (1, 5)])",
                valueFactory.array(
                        valueFactory.sequence(
                                valueFactory.number(1),
                                valueFactory.number(5)
                        ),
                        valueFactory.sequence(
                                valueFactory.number(2),
                                valueFactory.number(4)
                        ),
                        valueFactory.sequence(
                                valueFactory.number(3),
                                valueFactory.number(1)
                        )
                )
        );
    }

    @Test
    public void arraySort_preservesEmptyMembers() {
        assertResult(
                "array:sort([(), 3, 1, ()])",
                valueFactory.array(
                        valueFactory.emptySequence(),
                        valueFactory.emptySequence(),
                        valueFactory.number(1),
                        valueFactory.number(3)
                )
        );
    }

    @Test
    public void arraySort_preservesDuplicateMembers() {
        assertResult(
                "array:sort([3, 1, 3, 2, 1])",
                valueFactory.array(
                        valueFactory.number(1),
                        valueFactory.number(1),
                        valueFactory.number(2),
                        valueFactory.number(3),
                        valueFactory.number(3)
                )
        );
    }

    @Test
    public void arraySort_singleMember() {
        assertResult(
                "array:sort([42])",
                valueFactory.array(
                        valueFactory.number(42)
                )
        );
    }

    @Test
    public void arraySort_emptyArray() {
        assertResult(
                "array:sort([])",
                valueFactory.array()
        );
    }
}
