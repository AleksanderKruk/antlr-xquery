package com.github.akruk.antlrquery.languagefeatures.semantics.cardinality;

import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CardinalityRecursionTests extends CardinalityTestUtils {

    @Test
    public void recursionMerge()  {
        assertEquals(Cardinality.ONE_OR_MORE, Cardinalities.recursionMerge(Cardinality.ONE));
        assertEquals(Cardinality.ZERO_OR_MORE, Cardinalities.recursionMerge(Cardinality.ZERO_OR_ONE));
        assertEquals(Cardinality.ONE_OR_MORE, Cardinalities.recursionMerge(Cardinality.ONE_OR_MORE));
        assertEquals(Cardinality.ZERO_OR_MORE, Cardinalities.recursionMerge(Cardinality.ZERO_OR_MORE));

    }
}
