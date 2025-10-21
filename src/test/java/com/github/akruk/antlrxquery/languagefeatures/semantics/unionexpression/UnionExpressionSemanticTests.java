package com.github.akruk.antlrxquery.languagefeatures.semantics.unionexpression;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.semantics.SemanticTestsBase;

public class UnionExpressionSemanticTests extends SemanticTestsBase {


    @Test
    public void unionExpression() {
        assertType("""
                    let $x as node()* := (),
                        $y as node()* := (),
                        $z as node()* := ()
                    return $x | $y | $z
                """, typeFactory.zeroOrMore(typeFactory.itemAnyNode()));

        assertType("""
                    let $x as element(a)* := (),
                        $y as element(b)* := (),
                        $z as element(c)* := ()
                    return $x | $y | $z
                """, typeFactory.zeroOrMore(typeFactory.itemElement(Set.of("a", "b", "c"))));

        assertErrors("""
                    let $x as number+ := (1, 2, 3)
                    return $x | $x
                """);
    }


    @Test
    public void efb() {
        assertType("""
                    let $x as number? := 3
                    return if ($x)
                        then $x
                        else 1
                """, typeFactory.one(typeFactory.itemNumber()));
    }



}
