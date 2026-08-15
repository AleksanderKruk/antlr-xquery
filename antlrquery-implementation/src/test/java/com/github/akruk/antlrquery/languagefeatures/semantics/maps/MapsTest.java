package com.github.akruk.antlrquery.languagefeatures.semantics.maps;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.github.akruk.Utils;
import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import org.junit.jupiter.api.Test;

import com.github.akruk.antlrquery.languagefeatures.semantics.SemanticTestsBase;
import com.github.akruk.antlrquery.typesystem.RecordField;
import com.github.akruk.antlrquery.typesystem.RecordField.TypeOrReference;

public class MapsTest extends SemanticTestsBase {
    @Test
    public void oneTypeNonEmptyMapsAndRecords()
    {
        final var numToNum = typeFactory.map(
                typeFactory.itemNumber(NumericRange.of(1, 3)),
                typeFactory.number(NumericRange.of(2, 4))
        );
        final var recordType = typeFactory.record(
            Utils.linkedHashMap(
                Map.entry("a", new RecordField("a", new TypeOrReference.Type(typeFactory.number(NumericRange.of(1))), true)),
                Map.entry("b", new RecordField("b", new TypeOrReference.Type(typeFactory.number(NumericRange.of(2))), true))
            )
        );

        assertType("map { 1: 2, 3: 4 }", numToNum); // numeric keys -> map
        assertType("map { 'a': 1, 'b': 2 }", recordType); // string literal keys -> record
        assertType("map { 'a': 1, 'b': 2, 3: 4 }", typeFactory.map(
                typeFactory.itemChoice(
                    typeFactory.itemEnum(Set.of("a", "b")),
                    typeFactory.itemNumber(NumericRange.of(3))
                ),
                typeFactory.number(NumericRange.of(1, 2, 4))
            )
        ); // mixed keys -> map
    }

}
