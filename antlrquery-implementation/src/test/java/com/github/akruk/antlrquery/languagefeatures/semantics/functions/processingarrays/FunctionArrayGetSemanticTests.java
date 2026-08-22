package com.github.akruk.antlrquery.languagefeatures.semantics.functions.processingarrays;

import com.github.akruk.antlrquery.languagefeatures.semantics.SemanticTestsBase;
import com.github.akruk.antlrquery.semanticanalyzer.ErrorType;
import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import org.junit.jupiter.api.Test;

import java.util.List;

public class FunctionArrayGetSemanticTests extends SemanticTestsBase {


    @Test
    public void arrayGet_singleArray_indexOne() {
        assertType(
                "array:get([1, 2, 3], 2)",
                typeFactory.one(
                        typeFactory.itemNumber(
                                NumericRange.of(1, 2, 3)
                        )
                )
        );
    }

    @Test
    public void arrayGet_nestedArray_memberIsArray() {
        assertType(
                "array:get([[1, 2]], 1)",
                typeFactory.one(
                        typeFactory.itemTuple(
                                typeFactory.number(NumericRange.of(1)),
                                typeFactory.number(NumericRange.of(2))
                        )
                )
        );
    }

    @Test
    public void arrayGet_errors() {
        assertDiagnostics(
                "array:get()",
                List.of(ErrorType.FUNCTION__NO_MATCHING_FUNCTION),
                List.of()
        );

        assertDiagnostics(
                "array:get(1)",
                List.of(ErrorType.FUNCTION__NO_MATCHING_FUNCTION),
                List.of()
        );

        assertDiagnostics(
                "array:get([1,2], 'x')",
                List.of(ErrorType.FUNCTION__NO_MATCHING_FUNCTION),
                List.of()
        );

        assertDiagnostics(
                "array:get((1, 2), 1)",
                List.of(ErrorType.FUNCTION__NO_MATCHING_FUNCTION),
                List.of()
        );
    }
}
