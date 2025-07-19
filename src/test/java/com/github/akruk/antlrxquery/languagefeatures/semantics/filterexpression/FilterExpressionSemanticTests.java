package com.github.akruk.antlrxquery.languagefeatures.semantics.filterexpression;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.semantics.SemanticTestsBase;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;

public class FilterExpressionSemanticTests extends SemanticTestsBase {


    @Test
    public void itemGetting() {
        assertType("""
                    ("a", "b", "c")[()]
                """, typeFactory.emptySequence());
        final XQueryItemType abcEnum = typeFactory.itemEnum(Set.of("a", "b", "c"));
        final XQuerySequenceType zeroOrOneABC = typeFactory.zeroOrOne(abcEnum);
        final XQuerySequenceType zeroOrMoreABC = typeFactory.zeroOrMore(abcEnum);
        assertType("""
                    ("a", "b", "c")[1]
                """, zeroOrOneABC);
        assertType("""
                    ("a", "b", "c")[1, 2]
                """, zeroOrMoreABC);
        assertType("""
                    let $x as number? := 1
                    return ("a", "b", "c")[$x]
                """, zeroOrOneABC);
        assertType("""
                    let $x as number* := (1, 2)
                    return ("a", "b", "c")[$x]
                """, zeroOrMoreABC);
        assertType("""
                    let $x as number+ := (1, 2)
                    return ("a", "b", "c")[$x]
                """, zeroOrMoreABC);
    }
}
