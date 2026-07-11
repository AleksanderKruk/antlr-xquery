package com.github.akruk.antlrxquery.languagefeatures.semantics.otherwiseexpression;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.semantics.SemanticTestsBase;
import com.github.akruk.antlrxquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrxquery.typesystem.types.Cardinality;

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
                """, typeFactory.sequence(typeFactory.itemNumber(), 
                Cardinalities.union(Cardinality.of(BigDecimal.valueOf(0)), Cardinality.of(BigDecimal.valueOf(3)))));
        assertType("""
                    (1, 2, 3) otherwise (1, 2, 3) otherwise (1, 2, 3)
                """, typeFactory.sequence(typeFactory.itemNumber(), Cardinality.of(BigDecimal.valueOf(3))));
    }

}
