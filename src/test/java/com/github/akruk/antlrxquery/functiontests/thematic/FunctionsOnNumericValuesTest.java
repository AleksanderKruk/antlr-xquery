package com.github.akruk.antlrxquery.functiontests.thematic;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.functiontests.FunctionsSemanticTest;

public class FunctionsOnNumericValuesTest extends FunctionsSemanticTest {

    @Test void abs_valid() {
        assertType("fn:abs( -3 )",
            typeFactory.zeroOrOne(typeFactory.itemNumber()));
    }
    @Test void abs_invalidType() {
        assertErrors("fn:abs('foo')");
    }
    @Test void abs_missing() {
        assertErrors("fn:abs()");
    }

    // fn:ceiling($value as xs:numeric?) as xs:numeric?
    @Test void ceiling_valid() {
        assertType("fn:ceiling(2.5)",
            typeFactory.zeroOrOne(typeFactory.itemNumber()));
    }
    @Test void ceiling_defaultNotAllowed() {
        assertErrors("fn:ceiling()");
    }
    @Test void ceiling_wrongType() {
        assertErrors("fn:ceiling(true())");
    }

    // fn:floor($value as xs:numeric?) as xs:numeric?
    @Test void floor_valid() {
        assertType("fn:floor(2.9)",
            typeFactory.zeroOrOne(typeFactory.itemNumber()));
    }
    @Test void floor_noArgs() {
        assertErrors("fn:floor()");
    }
    @Test void floor_badType() {
        assertErrors("fn:floor('a')");
    }

    // fn:round($value as xs:numeric?, $precision as xs:integer? := 0, $mode as enum(...)? := 'half-to-ceiling') as xs:numeric?
    @Test void round_onlyValueUsesDefaults() {
        assertType("fn:round(1.23)",
            typeFactory.zeroOrOne(typeFactory.itemNumber()));
    }
    @Test void round_withPrecision() {
        assertType("fn:round(1.234, 2)",
            typeFactory.zeroOrOne(typeFactory.itemNumber()));
    }
    @Test void round_withMode() {
        assertType("fn:round(1.234, mode := 'floor')",
            typeFactory.zeroOrOne(typeFactory.itemNumber()));
    }
    @Test void round_namedAll() {
        assertType("fn:round(value := -1.5, precision := 0, mode := 'away-from-zero')",
            typeFactory.zeroOrOne(typeFactory.itemNumber()));
    }
    @Test void round_missingValue() {
        assertErrors("fn:round()");
    }
    @Test void round_badPrecision() {
        assertErrors("fn:round(1.2, 'x')");
    }
    @Test void round_badMode() {
        assertErrors("fn:round(1.2, 0, 'invalid-mode')");
    }

    // fn:round-half-to-even($value as xs:numeric?, $precision as xs:integer? := 0) as xs:numeric?
    @Test void roundHalfToEven_defaultPrecision() {
        assertType("fn:round-half-to-even(2.5)",
            typeFactory.zeroOrOne(typeFactory.itemNumber()));
    }
    @Test void roundHalfToEven_withPrecision() {
        assertType("fn:round-half-to-even(2.512, 2)",
            typeFactory.zeroOrOne(typeFactory.itemNumber()));
    }
    @Test void roundHalfToEven_missingValue() {
        assertErrors("fn:round-half-to-even()");
    }
    @Test void roundHalfToEven_badPrecision() {
        assertErrors("fn:round-half-to-even(2.5, 1.1)");
    }

    // fn:divide-decimals($value as xs:decimal, $divisor as xs:decimal, $precision as xs:integer? := 0) as record(...)
    @Test void divideDecimals_positional() {
        var r = analyze("fn:divide-decimals(10.0, 3.0)");
        assertNoErrors(r);
    }
    @Test void divideDecimals_withPrecision() {
        var r = analyze("fn:divide-decimals(10.0, 3.0, 2)");
        assertNoErrors(r);
    }
    @Test void divideDecimals_missingArgs() {
        assertErrors("fn:divide-decimals(1.0)");
        assertErrors("fn:divide-decimals()");
    }
    @Test void divideDecimals_wrongTypes() {
        assertErrors("fn:divide-decimals('x', 2.0)");
        assertErrors("fn:divide-decimals(1.0, 'y')");
        assertErrors("fn:divide-decimals(1.0, 2.0, 'z')");
    }

    // fn:is-NaN($value as xs:anyAtomicType) as xs:boolean
    @Test void isNaN_withNumeric() {
        assertType("fn:is-NaN(0 div 0)",
            typeFactory.one(typeFactory.itemBoolean()));
    }
    @Test void isNaN_withString() {
        assertType("fn:is-NaN('foo')",
            typeFactory.one(typeFactory.itemBoolean()));
    }
    @Test void isNaN_missing() {
        assertErrors("fn:is-NaN()");
    }
    @Test void isNaN_tooMany() {
        assertErrors("fn:is-NaN(1,2)");
    }

}
