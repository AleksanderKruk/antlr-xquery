package com.github.akruk.antlrquery.languagefeatures.semantics.functions.processingarrays;

import com.github.akruk.antlrquery.languagefeatures.semantics.SemanticTestsBase;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

public class FunctionArraySortSemanticTests extends SemanticTestsBase {

    @Test
    public void arraySort_tupleBecomesArray() {
        assertType(
                "array:sort([1, 2, 3])",
                typeFactory.one(
                        typeFactory.itemArray(
                                typeFactory.number(
                                        NumericRange.of(1, 2, 3)),
                                Cardinality.of(3)
                        )
                )
        );
    }

    @Test
    public void arraySort_tupleLosesMemberOrder() {
        assertType(
                "array:sort([1, 2, 3, 4])",
                typeFactory.one(
                        typeFactory.itemArray(
                                typeFactory.number(
                                        NumericRange.of(1, 2, 3, 4)),
                                Cardinality.of(4)
                        )
                )
        );
    }

    @Test
    public void arraySort_mixedMemberTypesBecomeUnion() {
        assertType(
                "array:sort([1, 'x', 2])",
                typeFactory.one(
                        typeFactory.itemArray(
                                typeFactory.choice(
                                        typeFactory.itemNumber(
                                                NumericRange.of(1, 2)),
                                        typeFactory.itemEnum(Set.of("x"))
                                ),
                                Cardinality.of(3)
                        )
                )
        );
    }

    @Test
    public void arraySort_preservesNestedArrays() {
        assertType(
                "array:sort([[1, 2], [3, 4], [5, 6]])",
                typeFactory.one(
                        typeFactory.itemArray(
                                typeFactory.one(typeFactory.itemTuple(
                                        List.of(
                                                typeFactory.number(
                                                        NumericRange.of(1, 3, 5)),
                                                typeFactory.number(
                                                        NumericRange.of(2, 4, 6))
                                        )
                                )),
                                Cardinality.of(3)
                        )
                )
        );
    }

    @Test
    public void arraySort_preservesNestedArraysWithDifferentSizes() {
        assertType(
                "array:sort([[1], [2, 3], [4, 5, 6]])",
                    typeFactory.array(
                            typeFactory.choice(
                                typeFactory.itemTuple(
                                        typeFactory.number(NumericRange.of(1))
                                ),
                                typeFactory.itemTuple(
                                        typeFactory.number(NumericRange.of(2)),
                                        typeFactory.number(NumericRange.of(3))
                                ),
                                typeFactory.itemTuple(
                                    typeFactory.number(NumericRange.of(4)),
                                    typeFactory.number(NumericRange.of(5)),
                                    typeFactory.number(NumericRange.of(6))
                                )
                            ),
                            Cardinality.of(3)
                    )
        );
    }

    @Test
    public void arraySort_preservesSequenceMembers() {
        assertType(
                "array:sort([(1, 2), 3])",
                typeFactory.array(
                        typeFactory.sequence(
                                typeFactory.itemNumber(
                                        NumericRange.of(1, 2, 3)),
                                Cardinality.inclusiveRange(1, 2)

                        ),
                        Cardinality.of(2)
                )
        );
    }

    @Test
    public void arraySort_preservesEmptyMembers() {
        assertType(
                "array:sort([(), 1, 2])",
                typeFactory.one(
                        typeFactory.itemArray(
                                typeFactory.zeroOrOne(typeFactory.itemNumber(NumericRange.of(1, 2))),
                                Cardinality.of(3)
                        )
                )
        );
    }

    @Test
    public void arraySort_singleMemberTupleBecomesArray() {
        assertType(
                "array:sort([1])",
                typeFactory.one(
                        typeFactory.itemArray(
                                typeFactory.number(
                                        NumericRange.of(1)),
                                Cardinality.ONE
                        )
                )
        );
    }
}
