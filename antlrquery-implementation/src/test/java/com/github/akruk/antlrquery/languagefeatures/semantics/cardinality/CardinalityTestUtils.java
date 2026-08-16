package com.github.akruk.antlrquery.languagefeatures.semantics.cardinality;

import com.github.akruk.antlrquery.typesystem.types.Cardinality;

public class CardinalityTestUtils {
    protected static Cardinality range(int start, int end) {
        return Cardinality.inclusiveRange(start, end);
    }

    protected static Cardinality point(int point) {
        return Cardinality.of(point);
    }

}
