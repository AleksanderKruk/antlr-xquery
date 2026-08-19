package com.github.akruk.antlrquery.languagefeatures.semantics.functions.processingarrays;

import com.github.akruk.antlrquery.languagefeatures.semantics.SemanticTestsBase;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

public class FunctionArrayTailSemanticTests extends SemanticTestsBase {

    @Test
    public void arrayTail_removesFirstMember() {
        assertType(
                "array:tail([1, 2, 3])",
                typeFactory.one(
                        typeFactory.itemTuple(
                                List.of(
                                        typeFactory.number(NumericRange.of(2)),
                                        typeFactory.number(NumericRange.of(3))
                                )
                        )
                )
        );
    }

    @Test
    public void arrayTail_twoMembers() {
        assertType(
                "array:tail([1, 2])",
                typeFactory.one(
                        typeFactory.itemTuple(
                                List.of(
                                        typeFactory.number(NumericRange.of(2))
                                )
                        )
                )
        );
    }

    @Test
    public void arrayTail_singleMember() {
        assertType(
                "array:tail([1])",
                typeFactory.one(
                        typeFactory.itemTuple(
                                List.of()
                        )
                )
        );
    }

    @Test
    public void arrayTail_preservesMemberTypes() {
        assertType(
                "array:tail([1, 'x', 3])",
                typeFactory.one(
                        typeFactory.itemTuple(
                                List.of(
                                        typeFactory.enum_(Set.of("x")),
                                        typeFactory.number(NumericRange.of(3))
                                )
                        )
                )
        );
    }

    @Test
    public void arrayTail_preservesNestedArrayMember() {
        assertType(
                "array:tail([[1, 2], [3, 4], [5, 6]])",
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
                                )
                        )
                )
        );
    }

    @Test
    public void arrayTail_preservesDeeplyNestedArray() {
        assertType(
                "array:tail([1, [[2, 3]], 4])",
                typeFactory.one(
                        typeFactory.itemTuple(
                                List.of(
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
                                        ),
                                        typeFactory.number(
                                                NumericRange.of(4))
                                )
                        )
                )
        );
    }

    @Test
    public void arrayTail_preservesSequenceMember() {
        assertType(
                "array:tail([1, (2, 3), 4])",
                typeFactory.one(
                        typeFactory.itemTuple(
                                List.of(
                                        typeFactory.sequence(
                                                typeFactory.itemNumber(
                                                        NumericRange.of(2, 3)),
                                                Cardinality.of(2)
                                        ),
                                        typeFactory.number(
                                                NumericRange.of(4))
                                )
                        )
                )
        );
    }

    @Test
    public void arrayTail_preservesEmptyMember() {
        assertType(
                "array:tail([1, (), 3])",
                typeFactory.one(
                        typeFactory.itemTuple(
                                List.of(
                                        typeFactory.emptySequence(),
                                        typeFactory.number(
                                                NumericRange.of(3))
                                )
                        )
                )
        );
    }
}
