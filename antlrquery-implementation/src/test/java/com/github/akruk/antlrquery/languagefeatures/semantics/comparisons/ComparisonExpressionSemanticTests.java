package com.github.akruk.antlrquery.languagefeatures.semantics.comparisons;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrquery.languagefeatures.semantics.SemanticTestsBase;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;

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
        final AntlrQuerySequenceType optionalBool = typeFactory.zeroOrOne(typeFactory.itemBoolean());
        final AntlrQuerySequenceType bool = typeFactory.boolean_();
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
        AntlrQuerySequenceType emptySequence=typeFactory.emptySequence();
        assertType("'a' eq ()", emptySequence);
        assertType("'a' ne ()", emptySequence);
        assertType("'a' lt ()", emptySequence);
        assertType("'a' gt ()", emptySequence);
        assertType("'a' le ()", emptySequence);
        assertType("'a' ge ()", emptySequence);
        assertType("() eq 'b'", emptySequence);
        assertType("() ne 'b'", emptySequence);
        assertType("() lt 'b'", emptySequence);
        assertType("() gt 'b'", emptySequence);
        assertType("() le 'b'", emptySequence);
        assertType("() ge 'b'", emptySequence);
        assertType("() eq ()", emptySequence);
        assertType("() ne ()", emptySequence);
        assertType("() lt ()", emptySequence);
        assertType("() gt ()", emptySequence);
        assertType("() le ()", emptySequence);
        assertType("() ge ()", emptySequence);

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
