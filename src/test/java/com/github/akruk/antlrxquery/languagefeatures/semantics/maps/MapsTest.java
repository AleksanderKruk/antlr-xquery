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

    @Test
    public void nonEmptyNumber() {
        assertType("""
    let $x as number? := 1
        return if ($x) then $x
        else 1
        """, typeFactory.number());
    }

    @Test
    public void nonEmptyBoolean() {
        assertType("""
    let $x as boolean? := fn:true()
        return if ($x) then $x
        else fn:true()
        """, typeFactory.boolean_());
    }

    @Test
    public void nonEmptyString() {
        assertType("""
    let $x as string? := "abc"
        return if ($x) then $x
        else "a"
        """, typeFactory.string());
    }

    @Test
    public void nonEmptyNode() {
        assertType("""
            let $x as node()* := /*
                return if ($x) then $x
                else .
        """, typeFactory.oneOrMore(typeFactory.itemAnyNode()));
    }


    @Test
    public void andAssumptions() {
        assertType("""
            let $x as number? := 1
            let $y as number? := 1
            return
                if ($x and $y) then
                    ($x, $y)
                else
                    (1, 1)
        """, typeFactory.oneOrMore(typeFactory.itemNumber()));
    }



}
