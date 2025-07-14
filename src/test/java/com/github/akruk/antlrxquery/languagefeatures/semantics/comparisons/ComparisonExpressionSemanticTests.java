package com.github.akruk.antlrxquery.languagefeatures.semantics.comparisons;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.semantics.SemanticTestsBase;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;

public class ComparisonExpressionSemanticTests extends SemanticTestsBase {
    @Test
    public void nodeComparisons() {
        assertType("""
                    let $x as node()? := (),
                        $y as node()? := ()
                    return $x is $y
                """, typeFactory.zeroOrOne(typeFactory.itemBoolean()));

        assertErrors("""
                    let $x as number+ := (1, 2, 3)
                    return $x is $x
                """);
        assertErrors("""
                    let $x as string? := "abc"
                    return $x is $x
                """);
    }

    @Test
    public void valueComparisons() {
        final XQuerySequenceType optionalBool = typeFactory.zeroOrOne(typeFactory.itemBoolean());
        final XQuerySequenceType bool = typeFactory.boolean_();
        assertType("1 eq 1", bool);
        assertType("1 ne 1", bool);
        assertType("1 lt 1", bool);
        assertType("1 gt 1", bool);
        assertType("1 le 1", bool);
        assertType("1 ge 1", bool);
        assertType("'a' eq 'b'", bool);
        assertType("'a' ne 'b'", bool);
        assertType("'a' lt 'b'", bool);
        assertType("'a' gt 'b'", bool);
        assertType("'a' le 'b'", bool);
        assertType("'a' ge 'b'", bool);
        assertType("'a' eq ()", optionalBool);
        assertType("'a' ne ()", optionalBool);
        assertType("'a' lt ()", optionalBool);
        assertType("'a' gt ()", optionalBool);
        assertType("'a' le ()", optionalBool);
        assertType("'a' ge ()", optionalBool);
        assertType("() eq 'b'", optionalBool);
        assertType("() ne 'b'", optionalBool);
        assertType("() lt 'b'", optionalBool);
        assertType("() gt 'b'", optionalBool);
        assertType("() le 'b'", optionalBool);
        assertType("() ge 'b'", optionalBool);
        assertType("() eq ()", optionalBool);
        assertType("() ne ()", optionalBool);
        assertType("() lt ()", optionalBool);
        assertType("() gt ()", optionalBool);
        assertType("() le ()", optionalBool);
        assertType("() ge ()", optionalBool);

        assertErrors("'1' eq 1");
        assertErrors("'1' ne 1");
        assertErrors("'1' lt 1");
        assertErrors("'1' gt 1");
        assertErrors("'1' le 1");
        assertErrors("'1' ge 1");

        assertErrors("'1' eq true()");
        assertErrors("'1' ne true()");
        assertErrors("'1' lt true()");
        assertErrors("'1' gt true()");
        assertErrors("'1' le true()");
        assertErrors("'1' ge true()");

        assertErrors("""
                    let $x as number+ := (1, 2, 3)
                    return $x eq $x
                """);
    }

}
