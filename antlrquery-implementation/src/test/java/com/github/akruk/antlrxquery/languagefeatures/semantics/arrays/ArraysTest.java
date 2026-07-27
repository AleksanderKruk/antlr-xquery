package com.github.akruk.antlrxquery.languagefeatures.semantics.arrays;

import java.util.Set;

import com.github.akruk.antlrxquery.typesystem.typeoperations.cardinality.Ranges;
import com.github.akruk.antlrxquery.typesystem.types.Cardinality;
import com.github.akruk.antlrxquery.typesystem.types.NumericRange;
import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.semantics.SemanticTestsBase;

public class ArraysTest extends SemanticTestsBase {

    @Test
    public void emptyArrays() {
        assertType("[]", typeFactory.anyArray());
        assertType("array {}", typeFactory.anyArray());
    }

    @Test
    public void named_oneTypeNonEmptyArrays() {
        final var numToNum = typeFactory.array(typeFactory.number(NumericRange.FULL), Cardinality.ZERO_OR_MORE);
        final var strToNum = typeFactory.array(typeFactory.enum_(Set.of("a", "b", "c")), Cardinality.ZERO_OR_MORE);
        assertType("array { 1 }", numToNum);
        assertType("array { 1, 2, 3}", numToNum);
        assertType("array { 'a', 'b', 'c' }", strToNum);
    }

    @Test
    public void bracketed_oneTypeNonEmptyArrays() {
        final var numToNum = typeFactory.array(typeFactory.number(NumericRange.FULL), Cardinality.ZERO_OR_MORE);
        final var strToNum = typeFactory.array(typeFactory.enum_(Set.of("a", "b", "c")), Cardinality.ZERO_OR_MORE);
        assertType("[ 1 ]", numToNum);
        assertType("[ 1, 2, 3]", numToNum);
        assertType("[ 'a', 'b', 'c' ]", strToNum);
    }
}
