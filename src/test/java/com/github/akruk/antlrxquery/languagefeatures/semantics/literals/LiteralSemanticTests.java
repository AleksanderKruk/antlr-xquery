package com.github.akruk.antlrxquery.languagefeatures.semantics.literals;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.semantics.SemanticTestsBase;

public class LiteralSemanticTests extends SemanticTestsBase {

    @Test
    public void numericLiteralTypes() {
        final var number = typeFactory.number();

        // Integer literals
        assertType("123", number);
        assertType("1_000_000", number);

        // Hexadecimal literals
        assertType("0x1F", number);
        assertType("0xDE_AD_BE_EF", number);
        assertType("0x0", number);

        // Binary literals
        assertType("0b1010", number);
        assertType("0b0001_0001", number);

        // Decimal literals
        assertType(".75", number);
        assertType("42.", number);
        assertType("3.14", number);
        assertType("1_000.000_1", number);

        // Double literals
        assertType("1.23e3", number);
        assertType(".5e+2", number);
        assertType("4.56E-1", number);
        assertType("7e4", number);
        assertType("1_2.3_4e+1_0", number); // z podkreśleniami
    }

    @Test
    public void parenthesizedExpression() {
        assertType("()", typeFactory.emptySequence());
        assertType("(1)", typeFactory.number());
        assertType("(1, 'a')", typeFactory
                .oneOrMore(typeFactory.itemChoice(List.of(typeFactory.itemNumber(), typeFactory.itemString()))));
        assertType("(1, 2, 3)", typeFactory.oneOrMore(typeFactory.itemNumber()));
        assertType("((), (), (1))", typeFactory.number());
        assertType("((), (1), (1))", typeFactory.oneOrMore(typeFactory.itemNumber()));
    }


    @Test
    public void stringConstructor() {
        assertType("``[]``", typeFactory.string());
    }


    @Test
    public void stringLiteral() {
        assertType("''", typeFactory.enum_(Set.of("")));
        assertType("\"\"", typeFactory.enum_(Set.of("")));
    }

}
