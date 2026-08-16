package com.github.akruk.antlrquery.languagefeatures.semantics.otherwiseexpression;

import java.util.Set;

import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import org.junit.jupiter.api.Test;

import com.github.akruk.antlrquery.languagefeatures.semantics.SemanticTestsBase;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;

public class OtherwiseExpressionSemanticTests extends SemanticTestsBase {
    @Test
    public void otherwiseExpression() {
        assertType("""
                    () otherwise 1
                """, typeFactory.zeroOrOne(typeFactory.itemNumber(NumericRange.of(1))));
        assertType("""
                    1 otherwise 2
                """, typeFactory.number(NumericRange.of(1, 2)));
        assertType("""
                    "text" otherwise 2
                """, typeFactory.choice(typeFactory.itemEnum(Set.of("text")), typeFactory.itemNumber(NumericRange.of(2))));
        assertType("""
                    (1, 2, 3) otherwise () otherwise (1, 2, 3)
                """,
                typeFactory.sequence(
                        typeFactory.itemNumber(NumericRange.of(1, 2, 3)),
                        Cardinalities.union(Cardinality.ZERO, Cardinality.of(3))
                )
        );
        assertType("""
                    (1, 2, 3) otherwise (1, 2, 3) otherwise (1, 2, 3)
                """,
                typeFactory.sequence(
                        typeFactory.itemNumber(NumericRange.of(1, 2, 3)),
                        Cardinality.of(3)
                )
        );
    }

}
