package com.github.akruk.antlrquery.languagefeatures.semantics.functions.processingarrays;

import com.github.akruk.antlrquery.languagefeatures.semantics.SemanticTestsBase;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

public class FunctionArrayFootSemanticTests extends SemanticTestsBase {

    @Test
    public void arrayFoot_returnsLastMember() {
        assertType(
                "array:foot([1, 2, 3])",
                typeFactory.number(
                        NumericRange.of(3)
                )
        );
    }

    @Test
    public void arrayFoot_returnsLastStringMember() {
        assertType(
                "array:foot([1, 2, 'x'])",
                typeFactory.enum_(
                        Set.of("x")
                )
        );
    }

    @Test
    public void arrayFoot_preservesLastMemberSequence() {
        assertType(
                "array:foot([1, 2, (3, 4)])",
                typeFactory.sequence(
                        typeFactory.itemChoice(
                                typeFactory.itemNumber(NumericRange.of(3, 4))
                        ),
                        Cardinality.of(2)
                )
        );
    }

    @Test
    public void arrayFoot_preservesLastMemberEmptySequence() {
        assertType(
                "array:foot([1, 2, ()])",
                typeFactory.emptySequence()
        );
    }

    @Test
    public void arrayFoot_preservesNestedArray() {
        assertType(
                "array:foot([[1, 2], [3, 4]])",
                typeFactory.one(
                        typeFactory.itemTuple(
                                List.of(
                                        typeFactory.number(
                                                NumericRange.of(3)),
                                        typeFactory.number(
                                                NumericRange.of(4))
                                )
                        )
                )
        );
    }

    @Test
    public void arrayFoot_preservesDeeplyNestedArray() {
        assertType(
                "array:foot([1, [[2, 3]]])",
                typeFactory.one(
                        typeFactory.itemTuple(
                                List.of(
                                        typeFactory.one(
                                                typeFactory.itemTuple(
                                                        List.of(
                                                                typeFactory.number(
                                                                        NumericRange.of(2)),
                                                                typeFactory.number(
                                                                        NumericRange.of(3))
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }

    @Test
    public void arrayFoot_preservesNestedArrayMember() {
        assertType(
                "array:foot([[1, 2], [3, 4], [5, 6]])",
                typeFactory.one(
                        typeFactory.itemTuple(
                                List.of(
                                        typeFactory.number(
                                                NumericRange.of(5)),
                                        typeFactory.number(
                                                NumericRange.of(6))
                                )
                        )
                )
        );
    }

    @Test
    public void arrayFoot_preservesCardinalityOfLastMember() {
        assertType(
                "array:foot([1, (2, 3, 4)])",
                typeFactory.sequence(
                        typeFactory.itemChoice(
                                typeFactory.itemNumber(NumericRange.of(2, 3, 4))
                        ),
                        Cardinality.of(3)
                )
        );
    }

    @Test
    public void arrayFoot_doesNotFlattenNestedArray() {
        assertType(
                "array:foot([1, [2, 3]])",
                typeFactory.one(
                        typeFactory.itemTuple(
                                List.of(
                                        typeFactory.number(
                                                NumericRange.of(2)),
                                        typeFactory.number(
                                                NumericRange.of(3))
                                )
                        )
                )
        );
    }

    @Test
    public void arrayFoot_returnsOnlyLastMember() {
        assertType(
                "array:foot([1, 2, 3, 4])",
                typeFactory.number(
                        NumericRange.of(4)
                )
        );
    }
}
