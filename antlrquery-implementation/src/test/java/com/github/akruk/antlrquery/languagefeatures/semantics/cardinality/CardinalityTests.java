package com.github.akruk.antlrquery.languagefeatures.semantics.cardinality;

import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CardinalityTests {

    @Test
    public void subtraction() {
        assertEquals(Cardinality.ONE_OR_MORE, Cardinalities.subtract(Cardinality.ZERO_OR_MORE, Cardinality.ZERO));
        assertEquals(Cardinality.of(4), Cardinalities.subtract(Cardinality.of(4), Cardinality.ZERO_OR_ONE));
    }

    @Test
    public void union() {
        assertEquals(Cardinality.ZERO_OR_ONE, Cardinalities.union(Cardinality.ONE, Cardinality.ZERO));
        assertEquals(Cardinality.ZERO_OR_ONE, Cardinalities.union(Cardinality.ZERO_OR_ONE, Cardinality.ZERO_OR_ONE));
        assertEquals(Cardinality.ZERO_OR_MORE, Cardinalities.union(Cardinality.ZERO_OR_MORE, Cardinality.ZERO_OR_MORE));
        assertEquals(Cardinality.ONE_OR_MORE, Cardinalities.union(Cardinality.ONE_OR_MORE, Cardinality.ONE_OR_MORE));
        assertEquals(Cardinality.ONE, Cardinalities.union(Cardinality.ONE, Cardinality.ONE));
    }

    @Test
    public void recursionMerge() {
        assertEquals(Cardinality.ONE_OR_MORE, Cardinalities.recursionMerge(Cardinality.ONE));
        assertEquals(Cardinality.ZERO_OR_MORE, Cardinalities.recursionMerge(Cardinality.ZERO_OR_ONE));
        assertEquals(Cardinality.ONE_OR_MORE, Cardinalities.recursionMerge(Cardinality.ONE_OR_MORE));
        assertEquals(Cardinality.ZERO_OR_MORE, Cardinalities.recursionMerge(Cardinality.ZERO_OR_MORE));

    }
}
