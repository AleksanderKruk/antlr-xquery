package com.github.akruk.antlrquery.languagefeatures.semantics.filterexpression;

import java.util.Set;

import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import org.junit.jupiter.api.Test;

import com.github.akruk.antlrquery.languagefeatures.semantics.SemanticTestsBase;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.AntlrQueryItemType;

public class FilterExpressionSemanticTests extends SemanticTestsBase {


    @Test
    public void itemGetting() {
        assertType("""
                    ("a", "b", "c")[()]
                """, typeFactory.emptySequence());
        final AntlrQueryItemType abcEnum = typeFactory.itemEnum(Set.of("a", "b", "c"));
        assertType("""
                    ("a", "b", "c")[1]
                """, typeFactory.zeroOrOne(abcEnum));
        assertType("""
                    ("a", "b", "c")[1, 2]
                """, typeFactory.sequence(abcEnum, Cardinality.inclusiveRange(0, 2)));
        assertType("""
                    let $x as number? := 1
                    return ("a", "b", "c")[$x]
                """, typeFactory.zeroOrOne(abcEnum));
        assertType("""
                    let $x as number* := (1, 2)
                    return ("a", "b", "c")[$x]
                """, typeFactory.zeroOrMore(abcEnum));
        assertType("""
                    let $x as number+ := (1, 2)
                    return ("a", "b", "c")[$x]
                """, typeFactory.zeroOrMore(abcEnum));
    }
}
