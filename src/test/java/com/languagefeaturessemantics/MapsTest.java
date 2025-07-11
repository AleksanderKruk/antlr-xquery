package com.languagefeaturessemantics;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.SemanticTests;

public class MapsTest extends SemanticTests {
    @Test
    public void oneTypeNonEmptyMaps() {
        final var numToNum = typeFactory.map(typeFactory.itemNumber(), typeFactory.number());
        final var strToNum = typeFactory.map(typeFactory.itemString(), typeFactory.number());
        assertType("map { 1: 2, 3: 4 }", numToNum);
        assertType("map { 'a': 1, 'b': 2 }", strToNum);
    }
}
