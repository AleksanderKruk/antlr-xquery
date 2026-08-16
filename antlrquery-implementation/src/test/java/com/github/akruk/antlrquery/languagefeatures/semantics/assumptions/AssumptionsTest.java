package com.github.akruk.antlrquery.languagefeatures.semantics.assumptions;

import com.github.akruk.antlrquery.languagefeatures.semantics.SemanticTestsBase;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import org.junit.jupiter.api.Test;

public class AssumptionsTest extends SemanticTestsBase {
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
        """, typeFactory.number(NumericRange.of(1)));
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
        """, typeFactory.sequence(typeFactory.itemNumber(), Cardinality.of(2)));
    }



}
