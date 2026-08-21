package com.github.akruk.antlrquery.languagefeatures.semantics.functions.processingarrays;

import com.github.akruk.antlrquery.languagefeatures.semantics.SemanticTestsBase;
import com.github.akruk.antlrquery.semanticanalyzer.ErrorType;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

public class FunctionArrayFlattenSemanticTests extends SemanticTestsBase {

    @Test
    public void arrayFlatten_singleMember() {
        assertType(
                "array:flatten([1])",
                typeFactory.one(
                        typeFactory.itemNumber(
                                NumericRange.of(1)
                        )
                )
        );
    }

    @Test
    public void arrayFlatten_multipleMembers() {
        assertType(
                "array:flatten([1, 2, 3])",
                typeFactory.sequence(
                        typeFactory.itemNumber(
                                NumericRange.of(1, 2, 3)
                        ),
                        Cardinality.of(3)
                )
        );
    }

    @Test
    public void arrayFlatten_sequenceMember() {
        assertType(
                "array:flatten([(1, 2), 3])",
                typeFactory.sequence(
                        typeFactory.itemNumber(
                                NumericRange.of(1, 2, 3)
                        ),
                        Cardinality.of(3)
                )
        );
    }

    @Test
    public void arrayFlatten_multipleSequenceMembers() {
        assertType(
                "array:flatten([(1, 2), (3, 4)])",
                typeFactory.sequence(
                        typeFactory.itemNumber(
                                NumericRange.of(1, 2, 3, 4)
                        ),
                        Cardinality.of(4)
                )
        );
    }

    @Test
    public void arrayFlatten_emptyMember() {
        assertType(
                "array:flatten([(), 1, 2])",
                typeFactory.sequence(
                        typeFactory.itemNumber(
                                NumericRange.of(1, 2)
                        ),
                        Cardinality.of(2)
                )
        );
    }

    @Test
    public void arrayFlatten_allMembersEmpty() {
        assertType(
                "array:flatten([(), ()])",
                typeFactory.emptySequence()
        );
    }

    @Test
    public void arrayFlatten_nestedArray() {
        assertType(
                "array:flatten([[1, 2], 3])",
                typeFactory.sequence(
                        typeFactory.itemNumber(
                                NumericRange.of(1, 2, 3)
                        ),
                        Cardinality.of(3)
                )
        );
    }

    @Test
    public void arrayFlatten_nestedArrayIsFlattened() {
        assertType(
                "array:flatten([[[1, 2]], 3])",
                typeFactory.sequence(
                        typeFactory.itemNumber(
                                NumericRange.of(1, 2, 3)
                        ),
                        Cardinality.of(3)
                )
        );
    }

    @Test
    public void arrayFlatten_tupleMember() {
        assertType(
                "array:flatten([(1, 2), 3])",
                typeFactory.sequence(
                        typeFactory.itemNumber(
                                NumericRange.of(1, 2, 3)
                        ),
                        Cardinality.of(3)
                )
        );
    }

    @Test
    public void arrayFlatten_emptyTuple() {
        assertType(
                "array:flatten([[], 1])",
                typeFactory.one(
                        typeFactory.itemNumber(
                                NumericRange.of(1)
                        )
                )
        );
    }

    @Test
    public void arrayFlatten_preservesMemberCardinality() {
        assertType(
                "array:flatten([(1, 2, 3)])",
                typeFactory.sequence(
                        typeFactory.itemNumber(
                                NumericRange.of(1, 2, 3)
                        ),
                        Cardinality.of(3)
                )
        );
    }

    @Test
    public void arrayFlatten_flattensTupleMembers() {
        assertType(
                "array:flatten([[1, 2], [3, 4]])",
                typeFactory.sequence(
                        typeFactory.itemNumber(
                                NumericRange.of(1, 2, 3, 4)
                        ),
                        Cardinality.of(4)
                )
        );
    }

    @Test
    public void arrayFlatten_differentMemberTypes() {
        assertType(
                "array:flatten([1, 'x', true()])",
                typeFactory.sequence(
                        typeFactory.itemChoice(
                                typeFactory.itemNumber(
                                        NumericRange.of(1)
                                ),
                                typeFactory.itemEnum(Set.of("x")),
                                typeFactory.itemTrue()
                        ),
                        Cardinality.of(3)
                )
        );
    }

    @Test
    public void arrayFlatten_nestedArraysAreRecursivelyFlattened() {
        assertType(
                "array:flatten([[[1]], [[2]]])",
                typeFactory.sequence(
                        typeFactory.itemNumber(
                                NumericRange.of(1, 2)
                        ),
                        Cardinality.of(2)
                )
        );
    }

    @Test
    public void arrayFlatten_errors() {
        assertDiagnostics(
                "array:flatten()",
                List.of(ErrorType.FUNCTION__NO_MATCHING_FUNCTION),
                List.of()
        );
    }
}
