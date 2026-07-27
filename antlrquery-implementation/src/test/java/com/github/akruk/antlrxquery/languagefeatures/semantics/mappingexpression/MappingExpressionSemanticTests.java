package com.github.akruk.antlrxquery.languagefeatures.semantics.mappingexpression;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.semantics.SemanticTestsBase;

public class MappingExpressionSemanticTests extends SemanticTestsBase {
    @Test
    public void mappingExpressions() {
        assertType("(1, 2, 3) ! (. gt 5)", typeFactory.oneOrMore(typeFactory.itemBoolean()));
        assertType("() ! (. gt 5)", typeFactory.emptySequence());
        assertType("""
                let $x as number? := 5
                return $x ! .
                """, typeFactory.zeroOrOne(typeFactory.itemNumber()));
        assertType("""
                let $x as number* := ()
                return $x ! .
                """, typeFactory.zeroOrMore(typeFactory.itemNumber()));

    }

}
