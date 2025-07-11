package com.languagefeaturessemantics;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.SemanticTests;

public class ArraysTest extends SemanticTests {

    @Test
    public void emptyArrays() {
        assertType("[]", typeFactory.anyArray());
        assertType("array {}", typeFactory.anyArray());
    }

    @Test
    public void named_oneTypeNonEmptyArrays() {
        final var numToNum = typeFactory.array(typeFactory.number());
        final var strToNum = typeFactory.array(typeFactory.enum_(Set.of("a", "b", "c")));
        assertType("array { 1 }", numToNum);
        assertType("array { 1, 2, 3}", numToNum);
        assertType("array { 'a', 'b', 'c' }", strToNum);
    }

    @Test
    public void bracketed_oneTypeNonEmptyArrays() {
        final var numToNum = typeFactory.array(typeFactory.number());
        final var strToNum = typeFactory.array(typeFactory.enum_(Set.of("a", "b", "c")));
        assertType("[ 1 ]", numToNum);
        assertType("[ 1, 2, 3]", numToNum);
        assertType("[ 'a', 'b', 'c' ]", strToNum);
    }
}
