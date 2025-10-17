package com.github.akruk.antlrxquery.languagefeatures.semantics.maps;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.semantics.SemanticTestsBase;
import com.github.akruk.antlrxquery.typesystem.XQueryRecordField;

public class MapsTest extends SemanticTestsBase {
    @Test
    public void oneTypeNonEmptyMapsAndRecords()
    {
        final var numToNum = typeFactory.map(typeFactory.itemNumber(), typeFactory.number());
        final var recordType = typeFactory.record(
            Map.of("a", new XQueryRecordField(typeFactory.number(), true),
                "b", new XQueryRecordField(typeFactory.number(), true)));

        assertType("map { 1: 2, 3: 4 }", numToNum); // numeric keys -> map
        assertType("map { 'a': 1, 'b': 2 }", recordType); // string literal keys -> record
    }

    @Test
    public void impliedType() {
        assertType("""

let $x as number? := 1
return
if ($x instance of number)then
    let $y := $x
    return 1
else
    let $z := $x
    return 1
        """, typeFactory.number());
    }
}
