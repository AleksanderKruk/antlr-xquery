package com.github.akruk.antlrquery.languagefeatures.semantics.cardinality;

import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CardinalityUnionTests extends CardinalityTestUtils {
    @Test
    public void union() {
        assertEquals(Cardinality.ZERO_OR_ONE, Cardinalities.union(Cardinality.ONE, Cardinality.ZERO));
        assertEquals(Cardinality.ZERO_OR_ONE, Cardinalities.union(Cardinality.ZERO_OR_ONE, Cardinality.ZERO_OR_ONE));
        assertEquals(Cardinality.ZERO_OR_MORE, Cardinalities.union(Cardinality.ZERO_OR_MORE, Cardinality.ZERO_OR_MORE));
        assertEquals(Cardinality.ONE_OR_MORE, Cardinalities.union(Cardinality.ONE_OR_MORE, Cardinality.ONE_OR_MORE));
        assertEquals(Cardinality.ONE, Cardinalities.union(Cardinality.ONE, Cardinality.ONE));
    }

}
