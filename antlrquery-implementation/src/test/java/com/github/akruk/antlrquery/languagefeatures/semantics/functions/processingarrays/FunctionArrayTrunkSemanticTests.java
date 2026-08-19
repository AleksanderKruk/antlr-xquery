package com.github.akruk.antlrquery.languagefeatures.semantics.functions.processingarrays;

import com.github.akruk.antlrquery.languagefeatures.semantics.SemanticTestsBase;
import com.github.akruk.antlrquery.semanticanalyzer.ErrorType;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import org.junit.jupiter.api.Test;

import java.util.List;

public class FunctionArrayTrunkSemanticTests extends SemanticTestsBase {

    @Test
    public void arrayTrunk_removeLastMember() {
        assertType(
                "array:trunk([1, 2, 3])",
                typeFactory.one(
                        typeFactory.itemTuple(
                                List.of(
                                        typeFactory.number(
                                                NumericRange.of(1)),
                                        typeFactory.number(
                                                NumericRange.of(2))
                                )
                        )
                )
        );
    }

    @Test
    public void arrayTrunk_removeLastMemberFromTwo() {
        assertType(
                "array:trunk([1, 2])",
                typeFactory.one(
                        typeFactory.itemTuple(
                                List.of(
                                        typeFactory.number(
                                                NumericRange.of(1))
                                )
                        )
                )
        );
    }

    @Test
    public void arrayTrunk_singleMemberBecomesEmptyArray() {
        assertType(
                "array:trunk([1])",
                typeFactory.one(
                        typeFactory.itemTuple(
                                List.of()
                        )
                )
        );
    }

    @Test
    public void arrayTrunk_preservesSequenceMember() {
        assertType(
                "array:trunk([(1, 2), 3, 4])",
                typeFactory.one(
                        typeFactory.itemTuple(
                                List.of(
                                        typeFactory.sequence(
                                                typeFactory.itemNumber(
                                                        NumericRange.of(1, 2)),
                                                Cardinality.of(2)),
                                        typeFactory.number(
                                                NumericRange.of(3))
                                )
                        )
                )
        );
    }

    @Test
    public void arrayTrunk_preservesEmptySequenceMember() {
        assertType(
                "array:trunk([(), 1, 2])",
                typeFactory.one(
                        typeFactory.itemTuple(
                                List.of(
                                        typeFactory.emptySequence(),
                                        typeFactory.number(
                                                NumericRange.of(1))
                                )
                        )
                )
        );
    }

    @Test
    public void arrayTrunk_preservesNestedArrayMember() {
        assertType(
                "array:trunk([[1, 2], [3, 4], [5, 6]])",
                typeFactory.one(
                        typeFactory.itemTuple(
                                List.of(
                                        typeFactory.one(
                                                typeFactory.itemTuple(
                                                        List.of(
                                                                typeFactory.number(
                                                                        NumericRange.of(1)),
                                                                typeFactory.number(
                                                                        NumericRange.of(2))
                                                        )
                                                )
                                        ),
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
                                )
                        )
                )
        );
    }

    @Test
    public void arrayTrunk_preservesNestedArray() {
        assertType(
                "array:trunk([ [[1, 2]], [3, 4] ])",
                typeFactory.one(
                        typeFactory.itemTuple(
                            typeFactory.one(
                                    typeFactory.itemTuple(
                                            List.of(
                                                    typeFactory.one(
                                                            typeFactory.itemTuple(
                                                                    List.of(
                                                                            typeFactory.number(
                                                                                    NumericRange.of(1)),
                                                                            typeFactory.number(
                                                                                    NumericRange.of(2))
                                                                    )
                                                            )
                                                    )
                                            )
                                    )
                            )
                        )
                )
        );
    }

    @Test
    public void arrayTrunk_preservesCardinalityOfRemainingMembers() {
        assertType(
                "array:trunk([(1, 2, 3), (4, 5), 6])",
                typeFactory.one(
                        typeFactory.itemTuple(
                                List.of(
                                        typeFactory.sequence(
                                                typeFactory.itemNumber(
                                                        NumericRange.of(1, 2, 3)),
                                                Cardinality.of(3)),
                                        typeFactory.sequence(
                                                typeFactory.itemNumber(
                                                        NumericRange.of(4, 5)),
                                                Cardinality.of(2))
                                )
                        )
                )
        );
    }

    @Test
    public void arrayTrunk_errors() {
        assertDiagnostics(
                "array:trunk()",
                List.of(
                        ErrorType.FUNCTION__NO_MATCHING_FUNCTION
                ),
                List.of()
        );

        assertDiagnostics(
                "array:trunk(1)",
                List.of(
                        ErrorType.FUNCTION__NO_MATCHING_FUNCTION
                ),
                List.of()
        );

        assertDiagnostics(
                "array:trunk(())",
                List.of(
                        ErrorType.FUNCTION__NO_MATCHING_FUNCTION
                ),
                List.of()
        );
    }
}
