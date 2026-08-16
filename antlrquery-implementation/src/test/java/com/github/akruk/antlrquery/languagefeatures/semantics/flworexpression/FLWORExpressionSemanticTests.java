package com.github.akruk.antlrquery.languagefeatures.semantics.flworexpression;

import java.util.Set;

import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import org.junit.jupiter.api.Test;

import com.github.akruk.antlrquery.languagefeatures.semantics.SemanticTestsBase;

public class FLWORExpressionSemanticTests extends SemanticTestsBase {
    @Test
    public void variableBinding() {
        assertType("let $x := 1 return $x", typeFactory.number(NumericRange.of(1)));
        assertType("let $x as item() := 1 return $x", typeFactory.anyItem());
        // If casting should be done, then the type of $x should be a number
        // assertType("let $x as boolean := 1 return $x", typeFactory.boolean_());
    }

    @Test
    public void forClauseBinding() {
        assertType(
                "for $x in (1, 2, 3) return $x",
                typeFactory.sequence(
                        typeFactory.itemNumber(NumericRange.of(1, 2, 3)),
                        Cardinality.of(3)
                )
        );
    }

    @Test
    public void forClausePositionalBinding() {
        assertType(
                "for $x at $i in (1, 2, 3) return $i",
                typeFactory.sequence(
                        typeFactory.itemNumber(NumericRange.NON_NEGATIVE),
                        Cardinality.of(3)
                )
        );
    }

    @Test
    public void forMembers() {
        assertType("for member $x in [1, 2, 3] return $x",
            typeFactory.zeroOrMore(typeFactory.itemNumber(NumericRange.of(1, 2, 3))));
    }

    @Test
    public void forMembersAssignmentRef() {
        assertType( """
                for member $x in [1, 2, 3]
                    let $y as number := $x
                return $x
        """,
            typeFactory.zeroOrMore(typeFactory.itemNumber(NumericRange.of(1, 2, 3))));
    }

    @Test
    public void forMembersPositional() {
        assertType(
                "for member $x at $i in [1, 2, 3] return $i",
                typeFactory.zeroOrMore(
                        typeFactory.itemNumber(NumericRange.NON_NEGATIVE)
                )
        );
    }

    @Test
    public void forKey() {
        assertType(
                "for key $x in {1: 'a', 2: 'b', 3: 'c'} return $x",
                typeFactory.zeroOrMore(
                        typeFactory.itemNumber(
                                NumericRange.of(1, 2, 3)
                        )
                )
        );
    }

    @Test
    public void forValue() {
        assertType("for value $x in {1: 'a', 2: 'b', 3: 'c'} return $x", typeFactory.zeroOrMore(typeFactory.itemEnum(Set.of("a", "b", "c"))));
    }

    @Test
    public void forEntry() {
        assertType("for key $x value $y in {1: 'a', 2: 'b', 3: 'c'} return ($x, $y)", typeFactory.zeroOrMore(
                typeFactory.itemChoice(typeFactory.itemNumber(NumericRange.of(1, 2, 3)), typeFactory.itemEnum(Set.of("a", "b", "c")))));
    }



    @Test
    public void indexVariableBinding() {
        assertType(
                "for $x at $i in (1, 2, 3) return $i",
                typeFactory.sequence(typeFactory.itemNumber(NumericRange.NON_NEGATIVE), Cardinality.of(3)));
    }

    @Test
    public void countVariableClause() {
        assertType("""
                    for $x at $i in (1, 2, 3)
                    count $count
                    return $count
                """, typeFactory.sequence(typeFactory.itemNumber(NumericRange.NON_NEGATIVE), Cardinality.of(3)));
    }

    @Test
    public void whereClause() {
        assertType("""
                    for $x at $i in (1, 2, 3)
                    where $x > 3
                    return $x
                """, typeFactory.sequence(typeFactory.itemNumber(NumericRange.of(1, 2, 3)), Cardinality.inclusiveRange(0, 3))
        );
    }

    @Test
    public void whileClause() {
        assertType("""
                    for $x at $i in (1, 2, 3)
                    while $x > 3
                    return $x
                """, typeFactory.sequence(typeFactory.itemNumber(NumericRange.of(1, 2, 3)), Cardinality.inclusiveRange(0, 3)));
    }

    @Test
    public void tumblingWindow() {
        assertType("""
                    for tumbling window $w in (1, 2, 3)
                        start $s at $si when $s = 2
                        end $e at $ei when $e = 2
                    return $w
                """, typeFactory.zeroOrMore(typeFactory.itemNumber(NumericRange.of(1, 2, 3))));
        assertType("""
                    for tumbling window $w in (1, 2, 3)
                        start $s at $si when $s = 2
                        end $e at $ei when $e = 2
                    return $s
                """, typeFactory.zeroOrMore(typeFactory.itemNumber(NumericRange.of(1, 2, 3))));
        assertType("""
                    for tumbling window $w in (1, 2, 3)
                        start $s at $si when $s = 2
                        end $e at $ei when $e = 2
                    return $si
                """, typeFactory.zeroOrMore(typeFactory.itemNumber(NumericRange.NON_NEGATIVE)));
        assertType("""
                    for tumbling window $w in (1, 2, 3)
                        start $s at $si when $s = 2
                        end $e at $ei when $e = 2
                    return $e
                """, typeFactory.zeroOrMore(typeFactory.itemNumber(NumericRange.of(1, 2, 3))));
        assertType("""
                    for tumbling window $w in (1, 2, 3)
                        start $s at $si when $s = 2
                        end $e at $ei when $e = 2
                    return $ei
                """, typeFactory.zeroOrMore(typeFactory.itemNumber(NumericRange.NON_NEGATIVE)));
    }

}
