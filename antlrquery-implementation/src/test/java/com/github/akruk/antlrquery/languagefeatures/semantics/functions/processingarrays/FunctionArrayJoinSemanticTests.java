package com.github.akruk.antlrquery.languagefeatures.semantics.functions.processingarrays;

import com.github.akruk.antlrquery.languagefeatures.semantics.SemanticTestsBase;
import com.github.akruk.antlrquery.semanticanalyzer.ErrorType;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

public class FunctionArrayJoinSemanticTests extends SemanticTestsBase {
    @Test
    public void arrayJoin_emptySequence() {
        assertType(
                "array:join(())",
                typeFactory.one(
                        typeFactory.itemTuple()
                )
        );
    }

    @Test
    public void arrayJoin_singleArray() {
        assertType(
                "array:join([1, 2, 3])",
                typeFactory.one(
                        typeFactory.itemArray(
                                typeFactory.number(
                                        NumericRange.of(1, 2, 3)
                                ),
                                Cardinality.of(3)
                        )
                )
        );
    }

    @Test
    public void arrayJoin_multipleArrays() {
        assertType(
                "array:join(([1, 2], [3, 4]))",
                typeFactory.one(
                        typeFactory.itemArray(
                                typeFactory.number(
                                        NumericRange.of(1, 2, 3, 4)
                                ),
                                Cardinality.of(4)
                        )
                )
        );
    }

    @Test
    public void arrayJoin_differentArrayLengths() {
        assertType(
                "array:join(([1, 2], [3]))",
                typeFactory.one(
                        typeFactory.itemArray(
                                typeFactory.number(
                                        NumericRange.of(1, 2, 3)
                                ),
                                Cardinality.inclusiveRange(2, 4)
                        )
                )
        );
    }

    @Test
    public void arrayJoin_differentArrayLengths_areGeneralized() {
        assertType(
                "array:join(([1, 2], [3, 4, 5]))",
                typeFactory.one(
                        typeFactory.itemArray(
                                typeFactory.number(
                                        NumericRange.of(1, 2, 3, 4, 5)
                                ),
                                Cardinality.inclusiveRange(4, 6)
                        )
                )
        );
    }

    @Test
    public void arrayJoin_repeatedArrays() {
        assertType(
                "array:join(([1, 2], [3, 4], [5]))",
                typeFactory.one(
                        typeFactory.itemArray(
                                typeFactory.number(
                                        NumericRange.of(1, 2, 3, 4, 5)
                                ),
                                Cardinality.inclusiveRange(3, 6)
                        )
                )
        );
    }

    @Test
    public void arrayJoin_emptyArrayDoesNotAddMembers() {
        assertType(
                "array:join(([1, 2], []))",
                typeFactory.one(
                        typeFactory.itemArray(
                                typeFactory.number(
                                        NumericRange.of(1, 2)
                                ),
                                Cardinalities.union(Cardinality.of(0), Cardinality.of(4))
                        )
                )
        );
    }

    @Test
    public void arrayJoin_onlyEmptyArrays() {
        assertType(
                "array:join(([], []))",
                typeFactory.one(typeFactory.itemTuple())
        );
    }

    @Test
    public void arrayJoin_nestedArraysAreNotFlattened() {
        assertType(
                "array:join(([[1, 2]], [[3, 4]]))",
                typeFactory.one(
                        typeFactory.itemArray(
                                typeFactory.tuple(
                                    typeFactory.number(NumericRange.of(1, 3)),
                                    typeFactory.number(NumericRange.of(2, 4))
                                ),
                                Cardinality.of(2)
                        )
                )
        );
    }

    @Test
    public void arrayJoin_stringArrays() {
        assertType(
                "array:join(([\"a\", \"b\"], [\"c\"]))",
                typeFactory.one(
                        typeFactory.itemArray(
                                typeFactory.enum_(
                                        Set.of("a", "b", "c")
                                ),
                                Cardinality.inclusiveRange(2, 4)
                        )
                )
        );
    }

    @Test
    public void arrayJoin_mixedMemberTypes() {
        assertType(
                "array:join(([1, 2], [\"x\"]))",
                typeFactory.one(
                        typeFactory.itemArray(
                                typeFactory.choice(
                                        typeFactory.itemNumber(
                                                NumericRange.of(1, 2)
                                        ),
                                        typeFactory.itemEnum(
                                                Set.of("x")
                                        )
                                ),
                                Cardinality.inclusiveRange(2, 4)
                        )
                )
        );
    }

    @Test
    public void arrayJoin_arrayCardinalityIsSeparateFromMemberCardinality() {
        assertType(
                "array:join(([1, 2], [3, 4]))",
                typeFactory.one(
                        typeFactory.itemArray(
                                typeFactory.number(
                                        NumericRange.of(1, 2, 3, 4)
                                ),
                                Cardinality.of(4)
                        )
                )
        );
    }

    @Test
    public void arrayJoin_errors() {
        assertDiagnostics(
                "array:join()",
                List.of(ErrorType.FUNCTION__NO_MATCHING_FUNCTION),
                List.of()
        );

        assertDiagnostics(
                "array:join(1)",
                List.of(ErrorType.FUNCTION__NO_MATCHING_FUNCTION),
                List.of()
        );

        assertDiagnostics(
                "array:join('x')",
                List.of(ErrorType.FUNCTION__NO_MATCHING_FUNCTION),
                List.of()
        );

        assertDiagnostics(
                "array:join((1, 2))",
                List.of(ErrorType.FUNCTION__NO_MATCHING_FUNCTION),
                List.of()
        );
    }
}
