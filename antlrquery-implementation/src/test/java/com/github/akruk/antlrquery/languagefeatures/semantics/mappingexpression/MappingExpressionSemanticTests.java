package com.github.akruk.antlrquery.languagefeatures.semantics.mappingexpression;

import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import org.junit.jupiter.api.Test;

import com.github.akruk.antlrquery.languagefeatures.semantics.SemanticTestsBase;

public class MappingExpressionSemanticTests extends SemanticTestsBase {
    @Test
    public void mappingExpressions() {
        assertType("(1, 2, 3) ! (. gt 5)",
                    typeFactory.sequence(typeFactory.itemBoolean(), Cardinality.of(3)));
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
