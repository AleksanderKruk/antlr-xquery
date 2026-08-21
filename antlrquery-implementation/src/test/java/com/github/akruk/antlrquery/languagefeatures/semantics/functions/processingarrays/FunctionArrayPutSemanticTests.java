package com.github.akruk.antlrquery.languagefeatures.semantics.functions.processingarrays;

import com.github.akruk.antlrquery.languagefeatures.semantics.SemanticTestsBase;
import com.github.akruk.antlrquery.semanticanalyzer.ErrorType;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

public class FunctionArrayPutSemanticTests extends SemanticTestsBase {

    // array:put(
    //     $array as array(*),
    //     $position as xs:integer,
    //     $member as item()*
    // ) as array(*)

    @Test
    public void arrayPut_replaceFirstMember() {
        assertType(
                "array:put([1, 2, 3], 1, 10)",
                typeFactory.one(
                        typeFactory.itemTuple(
                                List.of(
                                        typeFactory.number(NumericRange.of(10)),
                                        typeFactory.number(NumericRange.of(2)),
                                        typeFactory.number(NumericRange.of(3))
                                )
                        )
                )
        );
    }

    @Test
    public void arrayPut_replaceMiddleMember() {
        assertType(
                "array:put([1, 2, 3], 2, 10)",
                typeFactory.one(
                        typeFactory.itemTuple(
                                List.of(
                                        typeFactory.number(NumericRange.of(1)),
                                        typeFactory.number(NumericRange.of(10)),
                                        typeFactory.number(NumericRange.of(3))
                                )
                        )
                )
        );
    }

    @Test
    public void arrayPut_replaceLastMember() {
        assertType(
                "array:put([1, 2, 3], 3, 10)",
                typeFactory.one(
                        typeFactory.itemTuple(
                                List.of(
                                        typeFactory.number(NumericRange.of(1)),
                                        typeFactory.number(NumericRange.of(2)),
                                        typeFactory.number(NumericRange.of(10))
                                )
                        )
                )
        );
    }

    @Test
    public void arrayPut_replaceWithString() {
        assertType(
                "array:put([1, 2], 1, 'x')",
                typeFactory.one(
                        typeFactory.itemTuple(
                                List.of(
                                        typeFactory.enum_(Set.of("x")),
                                        typeFactory.number(NumericRange.of(2))
                                )
                        )
                )
        );
    }

    @Test
    public void arrayPut_replaceWithArray() {
        assertType(
                "array:put([1, 2], 1, [3, 4])",
                typeFactory.one(
                        typeFactory.itemTuple(
                                List.of(
                                        typeFactory.one(
                                                typeFactory.itemTuple(
                                                        List.of(
                                                                typeFactory.number(
                                                                        NumericRange.of(3)),
                                                                typeFactory.number(
                                                                        NumericRange.of(4))
                                                        )
                                                )
                                        ),
                                        typeFactory.number(NumericRange.of(2))
                                )
                        )
                )
        );
    }

    @Test
    public void arrayPut_sequenceMember() {
        assertType(
                "array:put([1, 2], 1, (10, 20))",
                typeFactory.one(
                        typeFactory.itemTuple(
                                List.of(
                                        typeFactory.sequence(
                                                typeFactory.itemNumber(
                                                        NumericRange.of(10, 20)),
                                                Cardinality.of(2)
                                        ),
                                        typeFactory.number(NumericRange.of(2))
                                )
                        )
                )
        );
    }

    @Test
    public void arrayPut_emptySequenceMember() {
        assertType(
                "array:put([1, 2], 1, ())",
                typeFactory.one(
                        typeFactory.itemTuple(
                                List.of(
                                        typeFactory.emptySequence(),
                                        typeFactory.number(NumericRange.of(2))
                                )
                        )
                )
        );
    }

    @Test
    public void arrayPut_preservesNestedArray() {
        assertType(
                "array:put([[1], [2]], 1, [3])",
                typeFactory.one(
                        typeFactory.itemTuple(
                                List.of(
                                        typeFactory.one(
                                                typeFactory.itemTuple(
                                                        typeFactory.number(NumericRange.of(3))
                                                )
                                        ),
                                        typeFactory.one(
                                                typeFactory.itemTuple(
                                                        typeFactory.number(NumericRange.of(2))
                                                )
                                        )
                                )
                        )
                )
        );
    }

    @Test
    public void arrayPut_preservesSequenceAsMember() {
        assertType(
                "array:put([1, 2], 2, (10, 20))",
                typeFactory.one(
                        typeFactory.itemTuple(
                                List.of(
                                        typeFactory.number(NumericRange.of(1)),
                                        typeFactory.sequence(
                                                typeFactory.itemNumber(
                                                        NumericRange.of(10, 20)),
                                                Cardinality.of(2)
                                        )
                                )
                        )
                )
        );
    }

    @Test
    public void arrayPut_doesNotFlattenNestedArray() {
        assertType(
                "array:put([1], 1, [[2, 3]])",
                typeFactory.one(
                        typeFactory.itemTuple(
                                List.of(
                                        typeFactory.one(
                                                typeFactory.itemTuple(List.of(
                                                        typeFactory.one(
                                                                typeFactory.itemTuple(
                                                                        List.of(
                                                                                typeFactory.number(
                                                                                        NumericRange.of(2)),
                                                                                typeFactory.number(
                                                                                        NumericRange.of(3))
                                                                        )
                                                                )
                                                        ))
                                                )
                                        )
                                )
                        )
                )
        );
    }

    @Test
    public void arrayPut_doesNotInsertAfterLastMember() {
        assertType(
                "array:put([1, 2, 3], 4, 10)",
                typeFactory.neverType()
        );
    }

    @Test
    public void arrayPut_doesNotInsertBeforeFirstMember() {
        assertDiagnostics(
                "array:put([1, 2, 3], 0, 10)",
                List.of(ErrorType.FUNCTION__NO_MATCHING_FUNCTION),
                List.of()
        );
    }

    @Test
    public void arrayPut_doesNotInsertIntoEmptyArray() {
        assertType(
                "array:put([], 1, 10)",
                typeFactory.neverType()
        );
    }

    @Test
    public void arrayPut_negativePositionIsInvalid() {
        assertDiagnostics(
                "array:put([1, 2, 3], -1, 10)",
                List.of(ErrorType.FUNCTION__NO_MATCHING_FUNCTION),
                List.of()
        );
    }


    @Test
    public void negativeNumber() {
        assertType(
                "-1",
                typeFactory.number(NumericRange.of(-1))
        );
    }

    @Test
    public void arrayPut_errors() {
        assertDiagnostics(
                "array:put()",
                List.of(ErrorType.FUNCTION__NO_MATCHING_FUNCTION),
                List.of()
        );

        assertDiagnostics(
                "array:put(1, 1, 10)",
                List.of(ErrorType.FUNCTION__NO_MATCHING_FUNCTION),
                List.of()
        );

        assertDiagnostics(
                "array:put([1, 2], 'x', 10)",
                List.of(ErrorType.FUNCTION__NO_MATCHING_FUNCTION),
                List.of()
        );

        assertDiagnostics(
                "array:put([1, 2], 1)",
                List.of(ErrorType.FUNCTION__NO_MATCHING_FUNCTION),
                List.of()
        );
    }
}
