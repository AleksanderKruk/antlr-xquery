package com.github.akruk.antlrquery.languagefeatures.semantics.functions.processingarrays;

import com.github.akruk.antlrquery.languagefeatures.semantics.SemanticTestsBase;
import com.github.akruk.antlrquery.semanticanalyzer.ErrorType;
import com.github.akruk.antlrquery.typesystem.types.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

public class FunctionArrayItemsSemanticTests extends SemanticTestsBase {

    // array:items($array as array(*)) as item()*

    @Test
    public void arrayItems_emptyArray() {
        assertType(
                "array:items([])",
                typeFactory.emptySequence()
        );
    }

    @Test
    public void arrayItems_singleMember() {
        assertType(
                "array:items([1])",
                typeFactory.one(
                        typeFactory.itemNumber(
                                NumericRange.of(1)
                        )
                )
        );
    }

    @Test
    public void arrayItems_multipleMembers() {
        assertType(
                "array:items([1, 2, 3])",
                typeFactory.sequence(
                        typeFactory.itemNumber(
                                NumericRange.of(1, 2, 3)
                        ),
                        Cardinality.of(3)
                )
        );
    }

    @Test
    public void arrayItems_differentMemberTypes() {
        assertType(
                "array:items([1, 'x', true()])",
                typeFactory.sequence(
                        typeFactory.itemChoice(
                                typeFactory.itemNumber(
                                        NumericRange.of(1)
                                ),
                                typeFactory.itemEnum(
                                        Set.of("x")
                                ),
                                typeFactory.itemTrue()
                        ),
                        Cardinality.of(3)
                )
        );
    }

    @Test
    public void arrayItems_emptyMembers() {
        assertType(
                "array:items([(), (), ()])",
                typeFactory.emptySequence()
        );
    }

    @Test
    public void arrayItems_emptyAndNonEmptyMembers() {
        assertType(
                "array:items([(), 1, ()])",
                typeFactory.one(
                        typeFactory.itemNumber(
                                NumericRange.of(1)
                        )
                )
        );
    }

    @Test
    public void arrayItems_sequenceMembers() {
        assertType(
                "array:items([1, (2, 3), 4])",
                typeFactory.sequence(
                        typeFactory.itemNumber(
                                NumericRange.of(1, 2, 3, 4)
                        ),
                        Cardinality.of(4)
                )
        );
    }

    @Test
    public void arrayItems_mixedSequenceCardinalities() {
        assertType(
                "array:items([(), 1, (2, 3), (4, 5, 6)])",
                typeFactory.sequence(
                        typeFactory.itemNumber(
                                NumericRange.of(1, 2, 3, 4, 5, 6)
                        ),
                        Cardinality.of(6)
                )
        );
    }

    @Test
    public void arrayItems_preservesNestedArray() {
        assertType(
                "array:items([1, [2, 3]])",
                typeFactory.sequence(
                        typeFactory.itemChoice(
                                typeFactory.itemNumber(
                                        NumericRange.of(1)
                                ),
                                    typeFactory.itemTuple(
                                            List.of(
                                                    typeFactory.number(
                                                            NumericRange.of(2)
                                                    ),
                                                    typeFactory.number(
                                                            NumericRange.of(3)
                                                    )
                                            )
                                    )
                        ),
                        Cardinality.of(2)
                )
        );
    }

    @Test
    public void arrayItems_nestedArrayIsNotFlattened() {
        assertType(
                "array:items([[1, 2], [3, 4]])",
                typeFactory.sequence(
                        typeFactory.itemChoice(
                                typeFactory.itemTuple(
                                        List.of(
                                                typeFactory.number(
                                                        NumericRange.of(1, 3)
                                                ),
                                                typeFactory.number(
                                                        NumericRange.of(2, 4)
                                                )
                                        )
                                )
                        ),
                        Cardinality.of(2)
                )
        );
    }

    @Test
    public void arrayItems_preservesMemberCardinality() {
        assertType(
                "array:items([(1, 2), (3, 4, 5)])",
                typeFactory.sequence(
                        typeFactory.itemNumber(
                                NumericRange.of(1, 2, 3, 4, 5)
                        ),
                        Cardinality.of(5)
                )
        );
    }

    @Test
    public void arrayItems_errors() {
        assertDiagnostics(
                "array:items()",
                List.of(ErrorType.FUNCTION__NO_MATCHING_FUNCTION),
                List.of()
        );

        assertDiagnostics(
                "array:items(1)",
                List.of(ErrorType.FUNCTION__NO_MATCHING_FUNCTION),
                List.of()
        );

        assertDiagnostics(
                "array:items('x')",
                List.of(ErrorType.FUNCTION__NO_MATCHING_FUNCTION),
                List.of()
        );

        assertDiagnostics(
                "array:items((1, 2))",
                List.of(ErrorType.FUNCTION__NO_MATCHING_FUNCTION),
                List.of()
        );
    }
}
