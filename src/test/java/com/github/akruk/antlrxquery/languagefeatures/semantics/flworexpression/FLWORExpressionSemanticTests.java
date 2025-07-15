package com.github.akruk.antlrxquery.languagefeatures.semantics.flworexpression;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.semantics.SemanticTestsBase;

public class FLWORExpressionSemanticTests extends SemanticTestsBase {
    @Test
    public void variableBinding() {
        assertType("let $x := 1 return $x", typeFactory.number());
        assertType("let $x as item() := 1 return $x", typeFactory.anyItem());
        // If casting should be done, then the type of $x should be number
        // assertType("let $x as boolean := 1 return $x", typeFactory.boolean_());
    }

    @Test
    public void forClauseBinding() {
        assertType("for $x in (1, 2, 3) return $x", typeFactory.oneOrMore(typeFactory.itemNumber()));
    }

    // @Test
    // public void forMembers() {
    //     assertType("for $x in (1, 2, 3) return $x", typeFactory.oneOrMore(typeFactory.itemNumber()));
    // }

    @Test
    public void indexVariableBinding() {
        assertType("for $x at $i in (1, 2, 3) return $i", typeFactory.oneOrMore(typeFactory.itemNumber()));
    }

    @Test
    public void countVariableClause() {
        assertType("""
                    for $x at $i in (1, 2, 3)
                    count $count
                    return $count
                """, typeFactory.oneOrMore(typeFactory.itemNumber()));
    }

    @Test
    public void whereClause() {
        assertType("""
                    for $x at $i in (1, 2, 3)
                    where $x > 3
                    return $x
                """, typeFactory.zeroOrMore(typeFactory.itemNumber()));
    }

    @Test
    public void whileClause() {
        assertType("""
                    for $x at $i in (1, 2, 3)
                    while $x > 3
                    return $x
                """, typeFactory.zeroOrMore(typeFactory.itemNumber()));
    }

}
