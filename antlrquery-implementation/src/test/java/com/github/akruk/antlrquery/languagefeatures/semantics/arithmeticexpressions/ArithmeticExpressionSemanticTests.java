package com.github.akruk.antlrquery.languagefeatures.semantics.arithmeticexpressions;

import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import org.junit.jupiter.api.Test;

import com.github.akruk.antlrquery.languagefeatures.semantics.SemanticTestsBase;

public class ArithmeticExpressionSemanticTests extends SemanticTestsBase {

    @Test
    public void arithmeticExpressions() {
        assertType("1 + 1", typeFactory.number(NumericRange.of(2)));
        assertType("1 - 1", typeFactory.number(NumericRange.of(0)));
        assertType("1 * 1", typeFactory.number(NumericRange.of(1)));
        assertType("1 x 1", typeFactory.number(NumericRange.of(2)));
        assertType("1 ÷ 1", typeFactory.number(NumericRange.of(1)));
        assertType("1 div 1", typeFactory.number(NumericRange.of(1)));
        assertType("1 mod 1", typeFactory.number(NumericRange.of(0)));
        assertType("1 idiv 1", typeFactory.number(NumericRange.of(1)));
        assertErrors("() + 1");
        assertErrors("1 + ()");
        assertErrors("() * 1");
        assertErrors("1 * 'a'");
    }

}
