package com.github.akruk.antlrxquery.languagefeatures.semantics.maps;

import java.util.LinkedHashMap;
import java.util.Map;

import com.github.akruk.antlrxquery.typesystem.typeoperations.cardinality.Ranges;
import com.github.akruk.antlrxquery.typesystem.types.NumericRange;
import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.semantics.SemanticTestsBase;
import com.github.akruk.antlrxquery.typesystem.RecordField;
import com.github.akruk.antlrxquery.typesystem.RecordField.TypeOrReference;

public class MapsTest extends SemanticTestsBase {
    @Test
    public void oneTypeNonEmptyMapsAndRecords()
    {
        final var numToNum = typeFactory.map(typeFactory.itemNumber(), typeFactory.number(NumericRange.FULL));
        final var recordType = typeFactory.record(
            new LinkedHashMap<>(Map.of("a", new RecordField("a", new TypeOrReference.Type(typeFactory.number(NumericRange.FULL)), true),
                "b", new RecordField("b", new TypeOrReference.Type(typeFactory.number(NumericRange.FULL)), true))));

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
        """, typeFactory.number(NumericRange.FULL));
    }

    @Test
    public void nonEmptyNumber() {
        assertType("""
    let $x as number? := 1
        return if ($x) then $x
        else 1
        """, typeFactory.number(NumericRange.FULL));
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
