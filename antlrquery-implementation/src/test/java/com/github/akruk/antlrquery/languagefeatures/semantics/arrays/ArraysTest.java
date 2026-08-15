package com.github.akruk.antlrquery.languagefeatures.semantics.arrays;

import java.util.List;
import java.util.Set;

import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import org.junit.jupiter.api.Test;

import com.github.akruk.antlrquery.languagefeatures.semantics.SemanticTestsBase;

public class ArraysTest extends SemanticTestsBase {

    @Test
    public void emptyArrays() {
        assertType("[]", typeFactory.one(typeFactory.itemTuple(List.of())));
        assertType("array {}", typeFactory.one(typeFactory.itemTuple(List.of())));
    }

    @Test
    public void named_oneTypeNonEmptyArrays() {
        final var numToNum = typeFactory.array(typeFactory.number(NumericRange.FULL), Cardinality.ZERO_OR_MORE);
        final var strToNum = typeFactory.array(typeFactory.enum_(Set.of("a", "b", "c")), Cardinality.ZERO_OR_MORE);
        assertType("array { 1 }",
                typeFactory.one(
                        typeFactory.itemArray(
                                typeFactory.number(NumericRange.of(1)),
                                Cardinality.ONE
                        )
                )
        );
        assertType("array { 1, 2, 3}",
                typeFactory.one(
                        typeFactory.itemArray(
                                typeFactory.number(NumericRange.of(1, 2, 3)),
                                Cardinality.of(3)
                        )
                )
        );
        assertType(
                "array { 'a', 'b', 'c' }",
                typeFactory.one(
                        typeFactory.itemArray(
                                        typeFactory.enum_(Set.of("a", "b", "c")),
                                Cardinality.of(3)
                        )
                )
        );
    }

    @Test
    public void bracketed_oneTypeNonEmptyArrays() {
        assertType(
                "[ 1 ]",
                typeFactory.one(
                        typeFactory.itemTuple(
                                List.of(
                                        typeFactory.number(NumericRange.of(1))
                                )
                        )
                )
        );
        assertType(
                "[ 1, 2, 3]",
                typeFactory.one(
                        typeFactory.itemTuple(
                                List.of(
                                        typeFactory.number(NumericRange.of(1)),
                                        typeFactory.number(NumericRange.of(2)),
                                        typeFactory.number(NumericRange.of(3))
                                )
                        )
                )
        );
        assertType(
                "[ 'a', 'b', 'c' ]",
                typeFactory.one(
                        typeFactory.itemTuple(
                                List.of(
                                        typeFactory.enum_(Set.of("a")),
                                        typeFactory.enum_(Set.of("b")),
                                        typeFactory.enum_(Set.of("c"))
                                        )
                        )
                )
        );
    }
}
