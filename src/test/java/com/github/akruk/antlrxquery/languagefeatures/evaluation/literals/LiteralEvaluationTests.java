package com.github.akruk.antlrxquery.languagefeatures.evaluation.literals;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.evaluator.XQuery;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;
import java.math.BigDecimal;


public class LiteralEvaluationTests extends EvaluationTestsBase {
    @Test
    public void stringLiteralsDoubleQuote() {
        assertResult("\"string\"", "string");
    }

    @Test
    public void stringLiteralsSingleQuote() {
        assertResult("'string'", "string");
    }

    @Test
    public void stringLiteralsEscapePredefinedChars() {
        assertResult("'a&apos;b'", "a'b");
        assertResult("'a&lt;b'", "a<b");
        assertResult("'a&gt;b'", "a>b");
        assertResult("'a&amp;b'", "a&b");
        assertResult("'a&quot;b'", "a\"b");

        assertResult("\"a&apos;b\"", "a'b");
        assertResult("\"a&lt;b\"", "a<b");
        assertResult("\"a&gt;b\"", "a>b");
        assertResult("\"a&amp;b\"", "a&b");
        assertResult("\"a&quot;b\"", "a\"b");
    }

    @Test
    public void stringLiteralsEscapeCharRefs() {
        assertResult("'a&#5;b'", "a\u0005b");
        assertResult("'a&#x10;b'", "a\u0010b");
        assertResult("\"a&#5;b\"", "a\u0005b");
        assertResult("\"a&#x10;b\"", "a\u0010b");
    }

    @Test
    public void charRef_decimal() {
        assertResult("'Hello &#65;lex'", "Hello Alex");
        assertResult("'Euro: &#8364;'", "Euro: €");
        assertResult("'Quote: &#34;'", "Quote: \"");
    }

    @Test
    public void charRef_hexadecimal() {
        assertResult("'Hex A: &#x41;'", "Hex A: A");
        assertResult("'Omega: &#x3A9;'", "Omega: Ω");
        assertResult("'Nbsp: &#xA0;'", "Nbsp: \u00A0");
    }

    @Test
    public void charRef_mixedAndSpecial() {
        assertResult("'Mix: &#x48;&#101;&#x6C;&#108;&#111;'", "Mix: Hello");
        assertResult("'Symbols: &lt;tag&gt; &amp; &#x26;'", "Symbols: <tag> & &");
    }

    @Test
    public void stringLiteralsEscapeCharsSingle() {
        assertResult("'a''b'", "a'b");
    }

    @Test
    public void stringLiteralsEscapeCharsDouble() {
        String xquery = """
                    "a""b"
                """;
        var value = XQuery.evaluate(null, xquery, null);
        assertEquals("a\"b", value.stringValue());
    }

    @Test
    public void integerLiteral() {
        assertResult("12345", new BigDecimal("12345"));
    }

    @Test
    public void floatLiteral() {
        assertResult("1.2", new BigDecimal("1.2"));
    }

    @Test
    public void integerLiteral_withUnderscores() {
        assertResult("1_000_000", new BigDecimal("1000000"));
    }

    @Test
    public void hexLiteralLowercase() {
        assertResult("0x1a", new BigDecimal("26"));
    }

    @Test
    public void hexLiteralUppercase() {
        assertResult("0xFF", new BigDecimal("255"));
    }

    @Test
    public void hexLiteral_withUnderscores() {
        assertResult("0xDE_AD_BE_EF", new BigDecimal("3735928559"));
    }

    @Test
    public void binaryLiteral() {
        assertResult("0b1010", new BigDecimal("10"));
    }

    @Test
    public void binaryLiteral_withUnderscores() {
        assertResult("0b1100_0101", new BigDecimal("197"));
    }

    @Test
    public void decimalLiteral_leadingDot() {
        assertResult(".75", new BigDecimal("0.75"));
    }

    @Test
    public void decimalLiteral_trailingDot() {
        assertResult("42.", new BigDecimal("42.0"));
    }

    @Test
    public void decimalLiteral_fullForm() {
        assertResult("12.34", new BigDecimal("12.34"));
    }

    @Test
    public void decimalLiteral_withUnderscores() {
        assertResult("1_000.0_01", new BigDecimal("1000.001"));
    }

    @Test
    public void doubleLiteral_exponentUpperCase() {
        assertResult("1.2E3", new BigDecimal("1200"));
    }

    @Test
    public void doubleLiteral_exponentLowerCase() {
        assertResult("4.5e2", new BigDecimal("450"));
    }

    @Test
    public void doubleLiteral_negativeExponent() {
        assertResult("6.0e-1", new BigDecimal("0.6"));
    }

    @Test
    public void doubleLiteral_noIntegerBeforeDot() {
        assertResult(".8e+1", new BigDecimal("8.0"));
    }

    @Test
    public void doubleLiteral_noFractionAfterDot() {
        assertResult("5.e3", new BigDecimal("5000"));
    }

    @Test
    public void doubleLiteral_withUnderscores() {
        assertResult("1_2.3_4e+1_0", new BigDecimal("1.234e11")); // 123400000000.0
    }
}
