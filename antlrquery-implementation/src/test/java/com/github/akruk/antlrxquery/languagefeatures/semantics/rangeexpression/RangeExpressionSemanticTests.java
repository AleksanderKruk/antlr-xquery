package com.github.akruk.antlrxquery.languagefeatures.semantics.rangeexpression;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.semantics.SemanticTestsBase;

public class RangeExpressionSemanticTests extends SemanticTestsBase {

    @Test
    public void rangeExpression() {
        final var numbers = typeFactory.zeroOrMore(typeFactory.itemNumber());
        assertType("""
                    1 to 5
                """, numbers);
        assertType("""
                    let $x as number? := 5
                    return ($x to 5)
                """, numbers);
        assertType("""
                    let $x as number? := 5
                    return (5 to $x)
                """, numbers);
        assertType("""
                    let $x as number? := 5,
                        $y as number? := 6
                    return ($x to $y)
                """, numbers);
        assertErrors("""
                    let $x as string? := "a",
                        $y as number? := 6
                    return ($x to $y)
                """);
        assertErrors("""
                    let $x as number? := 4,
                        $y as string? := "a"
                    return ($x to $y)
                """);
        assertErrors("""
                    let $x := (1, 2, 3, 4),
                        $y := (4, 5, 6, 7)
                    return ($x to $y)
                """);
        assertErrors("""
                    let $x as number+ := (1, 2, 3, 4),
                        $y as number+ := (4, 5, 6, 7)
                    return ($x to $y)
                """);
        assertErrors("""
                    let $x as item()+ := (1, 2, 3, 4),
                        $y as item()+ := (4, 5, 6, 7)
                    return ($x to $y)
                """);
    }


}
