package com.github.akruk.antlrxquery.languagefeatures.semantics.filterexpression;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.semantics.SemanticTestsBase;
import com.github.akruk.antlrxquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.types.itemtypes.AntlrQueryItemType;

public class FilterExpressionSemanticTests extends SemanticTestsBase {


    @Test
    public void itemGetting() {
        assertType("""
                    ("a", "b", "c")[()]
                """, typeFactory.emptySequence());
        final AntlrQueryItemType abcEnum = typeFactory.itemEnum(Set.of("a", "b", "c"));
        final AntlrQuerySequenceType zeroOrOneABC = typeFactory.zeroOrOne(abcEnum);
        final AntlrQuerySequenceType zeroOrMoreABC = typeFactory.zeroOrMore(abcEnum);
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
