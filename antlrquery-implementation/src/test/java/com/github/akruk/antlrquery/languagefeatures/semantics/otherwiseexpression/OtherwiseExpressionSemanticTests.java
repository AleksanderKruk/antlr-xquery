package com.github.akruk.antlrquery.languagefeatures.semantics.otherwiseexpression;

import java.math.BigInteger;
import java.util.Set;

import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import org.junit.jupiter.api.Test;

import com.github.akruk.antlrquery.languagefeatures.semantics.SemanticTestsBase;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;

public class OtherwiseExpressionSemanticTests extends SemanticTestsBase {
    @Test
    public void otherwiseExpression() {
        final var number = typeFactory.number(NumericRange.FULL);
        final var optionalNumber = typeFactory.zeroOrOne(typeFactory.itemNumber());
        assertType("""
                    () otherwise 1
                """, optionalNumber);
        assertType("""
                    1 otherwise 2
                """, number);
        assertType("""
                    "napis" otherwise 2
                """, typeFactory.choice(typeFactory.itemEnum(Set.of("napis")), typeFactory.itemNumber()));
        assertType("""
                    (1, 2, 3) otherwise () otherwise (1, 2, 3)
                """, typeFactory.sequence(typeFactory.itemNumber(), 
                Cardinalities.union(Cardinality.of(BigInteger.valueOf(0)), Cardinality.of(BigInteger.valueOf(3)))));
        assertType("""
                    (1, 2, 3) otherwise (1, 2, 3) otherwise (1, 2, 3)
                """, typeFactory.sequence(typeFactory.itemNumber(), Cardinality.of(BigInteger.valueOf(3))));
    }

}
