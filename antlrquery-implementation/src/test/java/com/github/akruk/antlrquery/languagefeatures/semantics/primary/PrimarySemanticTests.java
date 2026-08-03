package com.github.akruk.antlrquery.languagefeatures.semantics.primary;

import java.math.BigDecimal;
import java.util.Set;

import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.AntlrQueryItemType;
import org.junit.jupiter.api.Test;

import com.github.akruk.antlrquery.languagefeatures.semantics.SemanticTestsBase;

public class PrimarySemanticTests extends SemanticTestsBase {

    @Test
    public void numericLiteralTypes() {

        // Integer literals
        assertType("123", typeFactory.number(NumericRange.of(123)));
        assertType("1_000_000", typeFactory.number(NumericRange.of(1_000_000)));

        // Hexadecimal literals
        assertType("0x1F", typeFactory.number(NumericRange.of(0x1F)));
        assertType("0xDE_AD_BE_EF", typeFactory.number(NumericRange.of(Long.parseLong("DEADBEEF", 16))));
        assertType("0x0", typeFactory.number(NumericRange.of(0x0)));

        // Binary literals
        assertType("0b1010", typeFactory.number(NumericRange.of(0b1010)));
        assertType("0b0001_0001", typeFactory.number(NumericRange.of(0b0001_0001)));

        // Decimal literals (use string constructor to preserve exact decimal representation)
        assertType(".75", typeFactory.number(NumericRange.of(new BigDecimal("0.75"))));
        assertType("42.", typeFactory.number(NumericRange.of(new BigDecimal("42"))));
        assertType("3.14", typeFactory.number(NumericRange.of(new BigDecimal("3.14"))));
        assertType("1_000.000_1", typeFactory.number(NumericRange.of(new BigDecimal("1000.0001"))));

        // Double / scientific literals
        assertType("1.23e3", typeFactory.number(NumericRange.of(new BigDecimal("1.23e3"))));
        assertType(".5e+2", typeFactory.number(NumericRange.of(new BigDecimal("0.5e+2"))));
        assertType("4.56E-1", typeFactory.number(NumericRange.of(new BigDecimal("4.56E-1"))));

        assertType("7e4", typeFactory.number(NumericRange.of(new BigDecimal("7e4"))));
        assertType("1_2.3_4e+1_0", typeFactory.number(NumericRange.of(new BigDecimal("12.34e+10"))));
    }

    @Test
    public void parenthesizedExpression() {
        assertType("()", typeFactory.emptySequence());
        AntlrQueryItemType oneItem = typeFactory.itemNumber(NumericRange.of(1));
        AntlrQuerySequenceType oneSeq = typeFactory.one(oneItem);
        assertType("(1)", oneSeq);
        assertType("(1, 'a')", typeFactory
                .sequence(
                        typeFactory.itemChoice(oneItem, typeFactory.itemEnum(Set.of("a"))),
                        Cardinality.of(2)
                )
        );
        assertType("(1, 2, 3)", typeFactory.sequence(typeFactory.itemNumber(NumericRange.of(1, 2, 3)), Cardinality.of(3)));
        assertType("((), (), (1))", oneSeq);
        assertType("((), (1), (1))", typeFactory.sequence(typeFactory.itemNumber(NumericRange.of(1)), Cardinality.of(2)));
    }

    @Test
    public void stringConstructor() {
        assertType("``[]``", typeFactory.string());
    }

}
