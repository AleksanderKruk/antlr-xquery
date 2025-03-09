package com.github.akruk.antlrxquery;

import org.junit.Test;

import com.github.akruk.antlrxquery.evaluator.XQuery;
import com.github.akruk.antlrxquery.values.XQueryBoolean;
import com.github.akruk.antlrxquery.values.XQueryNumber;
import com.github.akruk.antlrxquery.values.XQueryValue;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

import static org.junit.Assert.*;

public class XQueryTest {

    public void assertResult(String xquery, BigDecimal result) {
        var value = XQuery.evaluate(null, xquery, null);
        assertNotNull(value);
        assertEquals(result, value.numericValue());
    }

    @Test
    public void stringLiteralsDoubleQuote() {
        String xquery = """
            "string"
        """;
        var value = XQuery.evaluate(null, xquery, null);
        assertEquals("string", value.stringValue());
    }

    @Test
    public void stringLiteralsSingleQuote() {
        String xquery = """
                    'string'
                """;
        var value = XQuery.evaluate(null, xquery, null);
        assertEquals("string", value.stringValue());
    }

    @Test
    public void stringLiteralsEscapeCharsSingle() {
        String xquery = """
                    'a''b'
                """;
        var value = XQuery.evaluate(null, xquery, null);
        assertEquals("a'b", value.stringValue());
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
        assertResult("1", BigDecimal.ONE);
    }

    @Test
    public void floatLiteral() {
        assertResult("1.2", new BigDecimal("1.2"));
    }

    @Test
    public void sequenceLiteral() {
        String xquery = """
                    (1, 2, 3)
                """;
        var value = XQuery.evaluate(null, xquery, null);
        List<XQueryValue> expected = List.of(
            new XQueryNumber(BigDecimal.ONE),
            new XQueryNumber(BigDecimal.valueOf(2)),
            new XQueryNumber(BigDecimal.valueOf(3))
        );
        assertNotNull(value);
        assertNotNull(value.sequence());
        var sequence = value.sequence();
        assertEquals(expected.size(), sequence.size());
        assertTrue(expected.get(0).numericValue().equals(sequence.get(0).numericValue()));
        assertTrue(expected.get(0).numericValue().equals(sequence.get(0).numericValue()));
        assertTrue(expected.get(0).numericValue().equals(sequence.get(0).numericValue()));

    }


    @Test
    public void atomization() {
        String xquery = """
                    (1, (2,3,4), ((5, 6), 7))
                """;
        var value = XQuery.evaluate(null, xquery, null);
        List<XQueryValue> expected = List.of(
            new XQueryNumber(BigDecimal.ONE),
            new XQueryNumber(BigDecimal.valueOf(2)),
            new XQueryNumber(BigDecimal.valueOf(3)),
            new XQueryNumber(BigDecimal.valueOf(4)),
            new XQueryNumber(BigDecimal.valueOf(5)),
            new XQueryNumber(BigDecimal.valueOf(6)),
            new XQueryNumber(BigDecimal.valueOf(7))
        );
        assertArrayEquals(
            expected.stream().map(XQueryValue::numericValue).toArray(),
            value.sequence().stream().map(XQueryValue::numericValue).toArray()
        );
    }

    @Test
    public void trueConstant() {
        String xquery = """
            true()
        """;
        var value = XQuery.evaluate(null, xquery, null);
        assertTrue(value.booleanValue().equals(XQueryBoolean.TRUE.booleanValue()));
    }


    @Test
    public void falseConstant() {
        String xquery = """
            false()
        """;
        var value = XQuery.evaluate(null, xquery, null);
        assertTrue(value.booleanValue().equals(XQueryBoolean.FALSE.booleanValue()));
    }


    @Test
    public void or() {
        String xquery = """
            false() or false() or true()
        """;
        var value = XQuery.evaluate(null, xquery, null);
        assertTrue(value.booleanValue());
        xquery = """
            false() or false() or false()
        """;
        value = XQuery.evaluate(null, xquery, null);
        assertFalse(value.booleanValue());
    }


    @Test
    public void and() {
        String xquery = """
            true() and true() and false()
        """;
        var value = XQuery.evaluate(null, xquery, null);
        assertFalse(value.booleanValue());
        xquery = """
            true() and true() and true()
        """;
        value = XQuery.evaluate(null, xquery, null);
        assertTrue(value.booleanValue());
    }

    @Test
    public void not() {
        String xquery = """
            not(true())
        """;
        var value = XQuery.evaluate(null, xquery, null);
        assertFalse(value.booleanValue());
        xquery = """
            not(false())
        """;
        value = XQuery.evaluate(null, xquery, null);
        assertTrue(value.booleanValue());
    }


    @Test
    public void addition() {
        String xquery = """
            5 + 3.10
        """;
        var value = XQuery.evaluate(null, xquery, null);
        assertEquals(new BigDecimal("8.10"), value.numericValue());
    }


    @Test
    public void subtraction() {
        String xquery = """
            5 - 3.10
        """;
        var value = XQuery.evaluate(null, xquery, null);
        assertEquals(new BigDecimal("1.9").setScale(2), value.numericValue().setScale(2));
    }


    @Test
    public void multiplication() {
        String xquery = """
            5 * 3.0
        """;
        var value = XQuery.evaluate(null, xquery, null);
        assertEquals(new BigDecimal(15).setScale(2), value.numericValue().setScale(2));
    }


    @Test
    public void division() {
        String xquery = """
            5 div 2.0
        """;
        var value = XQuery.evaluate(null, xquery, null);
        assertEquals(new BigDecimal(2.5, MathContext.UNLIMITED), value.numericValue());
    }

    @Test
    public void integerDivision() {
        String xquery = """
                    5 idiv 2
                """;
        var value = XQuery.evaluate(null, xquery, null);
        assertEquals(new BigDecimal(2).setScale(2), value.numericValue().setScale(2));
    }

    @Test
    public void modulus() {
        String xquery = """
            4 mod 2
        """;
        var value = XQuery.evaluate(null, xquery, null);
        assertEquals(BigDecimal.ZERO.setScale(2), value.numericValue().setScale(2));
    }


    @Test
    public void sequenceUnion() {
        String xquery = """
            (1, 2, 3) | (4, 5, 6)
        """;
        var value = XQuery.evaluate(null, xquery, null);
        var expected = List.of(
                new XQueryNumber(BigDecimal.valueOf(1)),
                new XQueryNumber(BigDecimal.valueOf(2)),
                new XQueryNumber(BigDecimal.valueOf(3)),
                new XQueryNumber(BigDecimal.valueOf(4)),
                new XQueryNumber(BigDecimal.valueOf(5)),
                new XQueryNumber(BigDecimal.valueOf(6))
        );
        assertEquals(expected.size(), value.sequence().size());
        var sequence = value.sequence();
        for (int i = 0; i< expected.size(); i++) {
            var element = expected.get(i);
            var received = sequence.get(i);
            assertEquals(element.numericValue(), received.numericValue());
        }
        xquery = """
            (1, 2, 3) union (4, 5, 6)
        """;
        value = XQuery.evaluate(null, xquery, null);
        assertEquals(expected.size(), value.sequence().size());
        sequence = value.sequence();
        for (int i = 0; i< expected.size(); i++) {
            var element = expected.get(i);
            var received = sequence.get(i);
            assertEquals(element.numericValue(), received.numericValue());
        }
    }

    @Test
    public void sequenceIntersection() {
        String xquery = """
            (1, 2, 3, 4) intersect (0, 2, 4, 8)
        """;
        var value = XQuery.evaluate(null, xquery, null);
        BigDecimal[] expected = {
                (BigDecimal.valueOf(2)),
                (BigDecimal.valueOf(4)),
        };
        assertEquals(expected.length, value.sequence().size());
        BigDecimal[] numbersFromSequence = value.sequence()
            .stream()
            .map(XQueryValue::numericValue)
            .toArray(BigDecimal[]::new);
        assertArrayEquals(expected, numbersFromSequence);
    }



    @Test
    public void abs() {
        String xquery = """
            abs(3)
        """;
        var value = XQuery.evaluate(null, xquery, null);
        assertNotNull(value);
        assertEquals(BigDecimal.valueOf(3), value.numericValue());
        xquery = """
            abs(-3)
        """;
        value = XQuery.evaluate(null, xquery, null);
        assertNotNull(value);
        assertEquals(BigDecimal.valueOf(3), value.numericValue());
    }


    @Test
    public void ceiling() {
        String xquery = """
            ceiling(3.3)
        """;
        var value = XQuery.evaluate(null, xquery, null);
        assertNotNull(value);
        assertEquals(BigDecimal.valueOf(4), value.numericValue());
    }

    @Test
    public void floor() {
        String xquery = """
            floor(3.3)
        """;
        var value = XQuery.evaluate(null, xquery, null);
        assertNotNull(value);
        assertEquals(BigDecimal.valueOf(3), value.numericValue());
    }

    @Test
    public void round() {
        // From https://www.w3.org/TR/xpath-functions-3/#func-round
        assertResult("round(3.3)", BigDecimal.valueOf(3));
        assertResult("round(3.5)", BigDecimal.valueOf(4));
        assertResult("round(-2.5)", BigDecimal.valueOf(-2));
        assertResult("round(1.125, 2)", new BigDecimal("1.13"));
        assertResult("round(8452, -2)", new BigDecimal("8500"));
    }

    @Test
    public void numericAdd() {
        assertResult("numeric-add(3, 5)", BigDecimal.valueOf(8));
    }

    @Test
    public void numericSubtract() {
        assertResult("numeric-subtract(3, 5)", BigDecimal.valueOf(-2));
    }

    @Test
    public void numericMultiply() {
        assertResult("numeric-multiply(3, 5)", BigDecimal.valueOf(15));
    }

    @Test
    public void numericDivide() {
        assertResult("numeric-divide(5, 5)", BigDecimal.ONE);
    }

    @Test
    public void numericIntegerDivide() {
        assertResult("numeric-integer-divide(5, 2)", BigDecimal.valueOf(2));
    }

    @Test
    public void numericMod() {
        assertResult("numeric-mod(5, 2)", BigDecimal.ONE);
    }

    @Test
    public void pi() {
        assertResult("pi()", new BigDecimal(Math.PI));
    }

    @Test
    public void exp() {
        assertResult("exp(5, 2)", BigDecimal.ONE);
    }

    @Test
    public void exp10() {
        assertResult("exp10(5, 2)", BigDecimal.ONE);
    }

    @Test
    public void log() {
        assertResult("log(5, 2)", BigDecimal.ONE);
    }

    @Test
    public void log10() {
        assertResult("log10(5, 2)", BigDecimal.ONE);
    }

    @Test
    public void pow() {
        assertResult("pow(5, 2)", BigDecimal.ONE);
    }

    @Test
    public void sqrt() {
        assertResult("sqrt(5, 2)", BigDecimal.ONE);
    }

    @Test
    public void sin() {
        assertResult("sin(5, 2)", BigDecimal.ONE);
    }

    @Test
    public void cos() {
        assertResult("sin(5, 2)", BigDecimal.ONE);
    }

    @Test
    public void tan() {
        assertResult("tan(5, 2)", BigDecimal.ONE);
    }

    @Test
    public void asin() {
        assertResult("asin(5, 2)", BigDecimal.ONE);
    }

    @Test
    public void acos() {
        assertResult("acos(5, 2)", BigDecimal.ONE);
    }

    @Test
    public void atan() {
        assertResult("atan(5, 2)", BigDecimal.ONE);
    }

    @Test
    public void atan2() {
        assertResult("atan2(5, 2)", BigDecimal.ONE);
    }

}
