package com.github.akruk.antlrquery.languagefeatures.semantics;

import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Ranges;
import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RangesTest {
    @Test
    void subraction() {
        assertEquals(NumericRange.of(0), Ranges.subtract(NumericRange.of(0), NumericRange.of(1, 2)));
    }
}
