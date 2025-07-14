package com.github.akruk.antlrxquery.languagefeatures.semantics.functions;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.semantics.SemanticTestsBase;

public class ParseAndFormatFunctionsSemanticTest extends SemanticTestsBase {
    // ++++ fn:number


    // fn:parse-integer($value as xs:string?, $radix as xs:integer? := 10) as xs:integer?
    @Test void parseInteger_validDefaultRadix() {
        assertType("fn:parse-integer('123')",
            typeFactory.zeroOrOne(typeFactory.itemNumber()));
    }
    @Test void parseInteger_validExplicitRadix() {
        assertType("fn:parse-integer('FF', 16)",
            typeFactory.zeroOrOne(typeFactory.itemNumber()));
    }
    @Test void parseInteger_missingValue() {
        assertErrors("fn:parse-integer()");
    }
    @Test void parseInteger_badValueType() {
        assertErrors("fn:parse-integer(123, 10)");
    }
    @Test void parseInteger_badRadixType() {
        assertErrors("fn:parse-integer('10', '2')");
    }
    @Test void parseInteger_tooManyArgs() {
        assertErrors("fn:parse-integer('10', 2, 3)");
    }

    // fn:format-integer($value as xs:integer?, $picture as xs:string, $language as xs:string? := ()) as xs:string
    @Test void formatInteger_minimal() {
        assertType("fn:format-integer(42, '000')",
            typeFactory.string());
    }
    @Test void formatInteger_withLanguage() {
        assertType("fn:format-integer(7, '#', 'en')",
            typeFactory.string());
    }
    @Test void formatInteger_missingValueOrPicture() {
        assertErrors("fn:format-integer()");
        assertErrors("fn:format-integer(1)");
    }
    @Test void formatInteger_badValueType() {
        assertErrors("fn:format-integer('x', '0')");
    }
    @Test void formatInteger_badPictureType() {
        assertErrors("fn:format-integer(1, 2)");
    }
    @Test void formatInteger_badLanguageType() {
        assertErrors("fn:format-integer(1, '#', 123)");
    }

    // fn:format-number($value as xs:numeric?, $picture as xs:string, $options as (xs:string|map(*))? := ()) as xs:string
    @Test void formatNumber_minimal() {
        assertType("fn:format-number(123.45, '#0.00')",
            typeFactory.string());
    }
    @Test void formatNumber_withOptionsAsString() {
        assertType("fn:format-number(1.2, '0.0', 'decimal-separator=.')",
            typeFactory.string());
    }
    @Test void formatNumber_withOptionsAsMap() {
        assertType("fn:format-number(3.14, '#.##', map{ 'decimal-separator' : '.' })",
            typeFactory.string());
    }
    @Test void formatNumber_missingArgs() {
        assertErrors("fn:format-number()");
        assertErrors("fn:format-number(1.0)");
    }
    @Test void formatNumber_badValueType() {
        assertErrors("fn:format-number('x', '#')");
    }
    @Test void formatNumber_badPictureType() {
        assertErrors("fn:format-number(1.0, 0)");
    }
    @Test void formatNumber_badOptionsType() {
        assertErrors("fn:format-number(1.0, '#', 123)");
    }
}

