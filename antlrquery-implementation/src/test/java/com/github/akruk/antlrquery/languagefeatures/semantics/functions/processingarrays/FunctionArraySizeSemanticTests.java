package com.github.akruk.antlrquery.languagefeatures.semantics.functions.processingarrays;

import com.github.akruk.antlrquery.languagefeatures.semantics.SemanticTestsBase;
import com.github.akruk.antlrquery.semanticanalyzer.ErrorType;
import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import org.junit.jupiter.api.Test;

import java.util.List;

public class FunctionArraySizeSemanticTests extends SemanticTestsBase {

    // array:size($array as array(*)) as xs:integer
    @Test
    public void arraySize_emptyArray() {
        assertType(
                "array:size([])",
                typeFactory.one(
                        typeFactory.itemNumber(
                                NumericRange.of(0)
                        )
                )
        );
    }

    @Test
    public void arraySize_singleMember() {
        assertType(
                "array:size([1])",
                typeFactory.one(
                        typeFactory.itemNumber(
                                NumericRange.of(1)
                        )
                )
        );
    }

    @Test
    public void arraySize_multipleMembers() {
        assertType(
                "array:size([1, 2, 3])",
                typeFactory.one(
                        typeFactory.itemNumber(
                                NumericRange.of(3)
                        )
                )
        );
    }

    @Test
    public void arraySize_nestedArray() {
        assertType(
                "array:size([[1], [2], [3]])",
                typeFactory.one(
                        typeFactory.itemNumber(
                                NumericRange.of(3)
                        )
                )
        );
    }

    @Test
    public void arraySize_arrayMembersMayBeSequences() {
        assertType(
                "array:size([(), (1, 2), (3, 4, 5)])",
                typeFactory.one(
                        typeFactory.itemNumber(
                                NumericRange.of(3)
                        )
                )
        );
    }

    @Test
    public void arraySize_errors() {
        assertDiagnostics(
                "array:size()",
                List.of(ErrorType.FUNCTION__NO_MATCHING_FUNCTION),
                List.of()
        );

        assertDiagnostics(
                "array:size(1)",
                List.of(ErrorType.FUNCTION__NO_MATCHING_FUNCTION),
                List.of()
        );

        assertDiagnostics(
                "array:size('x')",
                List.of(ErrorType.FUNCTION__NO_MATCHING_FUNCTION),
                List.of()
        );

        assertDiagnostics(
                "array:size((1, 2))",
                List.of(ErrorType.FUNCTION__NO_MATCHING_FUNCTION),
                List.of()
        );
    }
}
