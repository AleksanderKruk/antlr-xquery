package com.github.akruk.antlrxquery.languagefeatures.semantics.functions;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.semantics.SemanticTestsBase;

public class FunctionsOnStringValuesTest extends SemanticTestsBase {

    // fn:char($value as xs:string|xs:positiveInteger) as xs:string
    @Test
    public void char_withString() {
        assertType("fn:char('A')",
                typeFactory.string());
    }

    @Test
    public void char_withInteger() {
        assertType("fn:char(65)",
                typeFactory.string());
    }

    @Test
    public void char_wrongType() {
        assertErrors("fn:char(map {})");
    }

    @Test
    public void char_arity() {
        assertErrors("fn:char()");
        assertErrors("fn:char(65,66)");
    }

    // fn:characters($value as xs:string?) as xs:string*
    @Test
    public void characters_withText() {
        assertType("fn:characters('abc')",
                typeFactory.zeroOrMore(typeFactory.itemString()));
    }

    @Test
    public void characters_empty() {
        assertType("fn:characters(())",
                typeFactory.zeroOrMore(typeFactory.itemString()));
    }

    @Test
    public void characters_wrong() {
        assertErrors("fn:characters(1)");
    }

    // fn:graphemes($value as xs:string?) as xs:string*
    @Test
    public void graphemes_withText() {
        assertType("fn:graphemes('ąść')", typeFactory.zeroOrMore(typeFactory.itemString()));
    }

    @Test
    public void graphemes_null() {
        assertType("fn:graphemes(())",
                typeFactory.zeroOrMore(typeFactory.itemString()));
    }

    @Test
    public void graphemes_bad() {
        assertErrors("fn:graphemes(true())");
    }











    // fn:concat($values as xs:anyAtomicType* := ()) as xs:string
    @Test
    public void concat_noArgs() {
        assertType("fn:concat()",
                typeFactory.string());
    }

    @Test
    public void concat_mixed() {
        assertType("fn:concat(('x', 1, 'y'))",
                typeFactory.string());
    }

    // fn:string-join($values as xs:anyAtomicType*, $separator as xs:string? := '')
    // as xs:string
    @Test
    public void stringJoin_defaultSep() {
        assertType("fn:string-join(('a','b'))",
                typeFactory.string());
    }

    @Test
    public void stringJoin_namedSep() {
        assertType("fn:string-join(('x','y'), separator := ',')",
                typeFactory.string());
    }

    @Test
    public void stringJoin_bad() {
        assertErrors("fn:string-join(('a'), 5)");
    }

    // fn:substring($value as xs:string?, $start as xs:double, $length as xs:double?
    // := ()) as xs:string
    @Test
    public void substring_minimal() {
        assertType("fn:substring('hello', 2)",
                typeFactory.string());
    }

    @Test
    public void substring_withLength() {
        assertType("fn:substring('hello', 2, 3)",
                typeFactory.string());
    }

    @Test
    public void substring_wrong() {
        assertErrors("fn:substring(123,1)");
        assertErrors("fn:substring('x','y')");
    }

    // fn:string-length($value as xs:string? := fn:string(.)) as xs:integer
    @Test
    public void stringLength_arg() {
        assertType("fn:string-length('abc')",
                typeFactory.number());
    }

    @Test
    public void stringLength_default() {
        assertType("fn:string-length()", typeFactory.number());
    }

    @Test
    public void stringLength_bad() {
        assertErrors("fn:string-length(1)");
    }

    // fn:normalize-space($value as xs:string? := fn:string(.)) as xs:string
    @Test
    public void normalizeSpace_arg() {
        assertType("fn:normalize-space('  a  b  ')",
                typeFactory.string());
    }

    @Test
    public void normalizeSpace_default() {
        assertType("fn:normalize-space()",
                typeFactory.string());
    }

    @Test
    public void normalizeSpace_invalid() {
        assertErrors("fn:normalize-space(1)");
    }

    // fn:normalize-unicode($value as xs:string?, $form as xs:string? := 'NFC') as
    // xs:string
    @Test
    public void normalizeUnicode_minimal() {
        assertType("fn:normalize-unicode('zażółć')",
                typeFactory.string());
    }

    @Test
    public void normalizeUnicode_withForm() {
        assertType("fn:normalize-unicode('x','NFD')",
                typeFactory.string());
    }

    @Test
    public void normalizeUnicode_badForm() {
        assertErrors("fn:normalize-unicode('x', 5)");
    }

    // fn:upper-case($value as xs:string?) as xs:string
    @Test
    public void upperCase_arg() {
        assertType("fn:upper-case('abc')",
                typeFactory.string());
    }

    @Test
    public void upperCase_missing() {
        assertErrors("fn:upper-case()");
    }

    @Test
    public void upperCase_wrong() {
        assertErrors("fn:upper-case(1)");
    }

    // fn:lower-case($value as xs:string?) as xs:string
    @Test
    public void lowerCase_arg() {
        assertType("fn:lower-case('ABC')",
                typeFactory.string());
    }

    @Test
    public void lowerCase_invalid() {
        assertErrors("fn:lower-case(false())");
    }

    // fn:translate($value as xs:string?, $replace as xs:string, $with as xs:string)
    // as xs:string
    @Test
    public void translate_valid() {
        assertType("fn:translate('abc','a','A')",
                typeFactory.string());
    }

    @Test
    public void translate_missing() {
        assertErrors("fn:translate()");
        assertErrors("fn:translate('x','a')");
    }

    @Test
    public void translate_bad() {
        assertErrors("fn:translate(1,'a','b')");
        assertErrors("fn:translate('x',1,'b')");
        assertErrors("fn:translate('x','a',1)");
    }

    // fn:hash($value as xs:string|xs:hexBinary|xs:base64Binary?,
    // $algorithm as xs:string? := 'MD5',
    // $options as map(*)? := {}) as xs:hexBinary?
    // @Test
    // public void hash_defaultAll() {
    //     assertType("fn:hash()",
    //             typeFactory.zeroOrOne(typeFactory.itemHexBinary()));
    // }

    // @Test
    // public void hash_withValue() {
    //     assertType("fn:hash('data')",
    //             typeFactory.zeroOrOne(typeFactory.itemHexBinary()));
    // }

    // @Test
    // public void hash_allNamed() {
    //     assertType("fn:hash(value := 'x', algorithm := 'SHA-1', options := map{})",
    //             typeFactory.zeroOrOne(typeFactory.itemHexBinary()));
    // }

    @Test
    public void hash_badAlgorithm() {
        assertErrors("fn:hash('x', algorithm := 1)");
    }

    @Test
    public void hash_badOptions() {
        assertErrors("fn:hash('x', 'MD5', options := 'no-map')");
    }
}
