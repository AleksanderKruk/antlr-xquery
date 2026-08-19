package com.github.akruk.antlrquery.languagefeatures.evaluation.functions.processingarrays;

import com.github.akruk.antlrquery.languagefeatures.evaluation.EvaluationTestsBase;
import org.junit.jupiter.api.Test;

public class ArraySizeEvaluationTests extends EvaluationTestsBase {

    // array:size($array as array(*)) as xs:integer

    @Test
    public void arraySize_emptyArray() {
        assertResult(
                "array:size([])",
                valueFactory.number(0)
        );
    }

    @Test
    public void arraySize_singleMember() {
        assertResult(
                "array:size([1])",
                valueFactory.number(1)
        );
    }

    @Test
    public void arraySize_multipleMembers() {
        assertResult(
                "array:size([1, 2, 3])",
                valueFactory.number(3)
        );
    }

    @Test
    public void arraySize_nestedArray() {
        assertResult(
                "array:size([[1], [2], [3]])",
                valueFactory.number(3)
        );
    }

    @Test
    public void arraySize_emptyMembers() {
        assertResult(
                "array:size([(), (), ()])",
                valueFactory.number(3)
        );
    }

    @Test
    public void arraySize_sequenceMembers() {
        assertResult(
                "array:size([(1, 2), (3, 4, 5)])",
                valueFactory.number(2)
        );
    }

    @Test
    public void arraySize_mixedMembers() {
        assertResult(
                "array:size([1, 'x', [2, 3], ()])",
                valueFactory.number(4)
        );
    }

    @Test
    public void arraySize_doesNotFlattenMembers() {
        assertResult(
                "array:size([[1, 2], [3, 4], [5, 6]])",
                valueFactory.number(3)
        );
    }
}
