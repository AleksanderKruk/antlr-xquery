package com.github.akruk.antlrxquery.languagefeatures.semantics.flworexpression;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.semantics.SemanticTestsBase;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;

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

    @Test
    public void forClausePositionalBinding() {
        assertType("for $x at $i in (1, 2, 3) return $i", typeFactory.oneOrMore(typeFactory.itemNumber()));
    }

    @Test
    public void forMembers() {
        assertType("for member $x in [1, 2, 3] return $x",
            typeFactory.zeroOrMore(typeFactory.itemNumber()));
    }

    @Test
    public void forMembersPositional() {
        assertType("for member $x at $i in [1, 2, 3] return $i", typeFactory.zeroOrMore(typeFactory.itemNumber()));
    }

    @Test
    public void forKey() {
        assertType("for key $x in {1: 'a', 2: 'b', 3: 'c'} return $x", typeFactory.zeroOrMore(typeFactory.itemNumber()));
    }

    @Test
    public void forValue() {
        assertType("for value $x in {1: 'a', 2: 'b', 3: 'c'} return $x", typeFactory.zeroOrMore(typeFactory.itemEnum(Set.of("a", "b", "c"))));
    }

    @Test
    public void forEntry() {
        assertType("for key $x value $y in {1: 'a', 2: 'b', 3: 'c'} return ($x, $y)", typeFactory.zeroOrMore(
                typeFactory.itemChoice(Set.of(typeFactory.itemNumber(), typeFactory.itemEnum(Set.of("a", "b", "c"))))));
    }



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

    @Test
    public void tumblingWindow() {
        XQuerySequenceType zeroOrMoreNumbers = typeFactory.zeroOrMore(typeFactory.itemNumber());
        assertType("""
                    for tumbling window $w in (1, 2, 3)
                        start $s at $si when $s = 2
                        end $e at $ei when $e = 2
                    return $w
                """, zeroOrMoreNumbers);
        assertType("""
                    for tumbling window $w in (1, 2, 3)
                        start $s at $si when $s = 2
                        end $e at $ei when $e = 2
                    return $s
                """, zeroOrMoreNumbers);
        assertType("""
                    for tumbling window $w in (1, 2, 3)
                        start $s at $si when $s = 2
                        end $e at $ei when $e = 2
                    return $si
                """, zeroOrMoreNumbers);
        assertType("""
                    for tumbling window $w in (1, 2, 3)
                        start $s at $si when $s = 2
                        end $e at $ei when $e = 2
                    return $e
                """, zeroOrMoreNumbers);
        assertType("""
                    for tumbling window $w in (1, 2, 3)
                        start $s at $si when $s = 2
                        end $e at $ei when $e = 2
                    return $ei
                """, zeroOrMoreNumbers);
    }

}
