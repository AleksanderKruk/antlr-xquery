package com.github.akruk.antlrxquery.languagefeatures.semantics.otherwiseexpression;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.semantics.SemanticTestsBase;

public class OtherwiseExpressionSemanticTests extends SemanticTestsBase {
    @Test
    public void otherwiseExpression() {
        final var number = typeFactory.number();
        final var optionalNumber = typeFactory.zeroOrOne(typeFactory.itemNumber());
        assertType("""
                    () otherwise 1
                """, optionalNumber);
        assertType("""
                    1 otherwise 2
                """, number);
        assertType("""
                    "napis" otherwise 2
                """, typeFactory.choice(Set.of(typeFactory.itemEnum(Set.of("napis")), typeFactory.itemNumber())));
        assertType("""
                    (1, 2, 3) otherwise () otherwise (1, 2, 3)
                """, typeFactory.zeroOrMore(typeFactory.itemNumber()));
        assertType("""
                    (1, 2, 3) otherwise (1, 2, 3) otherwise (1, 2, 3)
                """, typeFactory.oneOrMore(typeFactory.itemNumber()));
    }

}
