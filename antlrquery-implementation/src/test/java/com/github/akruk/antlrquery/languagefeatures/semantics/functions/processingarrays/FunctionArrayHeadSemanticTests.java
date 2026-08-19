package com.github.akruk.antlrquery.languagefeatures.semantics.functions.processingarrays;

import com.github.akruk.antlrquery.languagefeatures.semantics.SemanticTestsBase;
import com.github.akruk.antlrquery.semanticanalyzer.ErrorType;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import org.junit.jupiter.api.Test;

import java.util.List;

public class FunctionArrayHeadSemanticTests extends SemanticTestsBase {

    // array:head($array as array(*[1..inf])) as item()*

    @Test
    public void arrayHead_singleMember() {
        assertType(
                "array:head([1])",
                typeFactory.one(
                        typeFactory.itemNumber(
                                NumericRange.of(1)
                        )
                )
        );
    }

    @Test
    public void arrayHead_firstMember() {
        assertType(
                "array:head([1, 2, 3])",
                typeFactory.one(
                        typeFactory.itemNumber(
                                NumericRange.of(1)
                        )
                )
        );
    }

    @Test
    public void arrayHead_sequenceMember() {
        assertType(
                "array:head([(1, 2), 3])",
                typeFactory.sequence(
                        typeFactory.itemNumber(
                                NumericRange.of(1, 2)
                        ),
                        Cardinality.of(2)
                )
        );
    }

    @Test
    public void arrayHead_emptyFirstMember() {
        assertType(
                "array:head([(), 1, 2])",
                typeFactory.emptySequence()
        );
    }

    @Test
    public void arrayHead_nestedArray() {
        assertType(
                "array:head([[1, 2], 3])",
                typeFactory.one(
                        typeFactory.itemTuple(
                                List.of(
                                        typeFactory.number(
                                                NumericRange.of(1)
                                        ),
                                        typeFactory.number(
                                                NumericRange.of(2)
                                        )
                                )
                        )
                )
        );
    }

    @Test
    public void arrayHead_nestedArrayIsNotFlattened() {
        assertType(
                "array:head([[[1, 2]], 3])",
                typeFactory.one(
                        typeFactory.itemTuple(
                                List.of(
                                        typeFactory.one(
                                                typeFactory.itemTuple(
                                                        List.of(
                                                                typeFactory.number(
                                                                        NumericRange.of(1)
                                                                ),
                                                                typeFactory.number(
                                                                        NumericRange.of(2)
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
    public void arrayHead_preservesFirstMemberCardinality() {
        assertType(
                "array:head([(1, 2, 3), 4, 5])",
                typeFactory.sequence(
                        typeFactory.itemNumber(
                                NumericRange.of(1, 2, 3)
                        ),
                        Cardinality.of(3)
                )
        );
    }

    @Test
    public void arrayHead_differentFollowingMembersDoNotAffectResult() {
        assertType(
                "array:head([1, 'x', true()])",
                typeFactory.one(
                        typeFactory.itemNumber(
                                NumericRange.of(1)
                        )
                )
        );
    }

    @Test
    public void arrayHead_emptyArrayIsRejected() {
        assertDiagnostics(
                "array:head([])",
                List.of(ErrorType.FUNCTION__NO_MATCHING_FUNCTION),
                List.of()
        );
    }

    @Test
    public void arrayHead_errors() {
        assertDiagnostics(
                "array:head()",
                List.of(ErrorType.FUNCTION__NO_MATCHING_FUNCTION),
                List.of()
        );

        assertDiagnostics(
                "array:head(1)",
                List.of(ErrorType.FUNCTION__NO_MATCHING_FUNCTION),
                List.of()
        );

        assertDiagnostics(
                "array:head('x')",
                List.of(ErrorType.FUNCTION__NO_MATCHING_FUNCTION),
                List.of()
        );

        assertDiagnostics(
                "array:head((1, 2))",
                List.of(ErrorType.FUNCTION__NO_MATCHING_FUNCTION),
                List.of()
        );
    }
}
