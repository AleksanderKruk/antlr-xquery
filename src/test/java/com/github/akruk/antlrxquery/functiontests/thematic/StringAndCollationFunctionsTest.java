package com.github.akruk.antlrxquery.functiontests.thematic;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.functiontests.FunctionsSemanticTest;

public class StringAndCollationFunctionsTest extends FunctionsSemanticTest {

    // fn:codepoints-to-string($values as xs:integer*) as xs:string
    @Test
    public void codepointsToString_noArgs() {
        assertType(
            "fn:codepoints-to-string()",
            typeFactory.one(typeFactory.itemString())
        );
    }

    @Test
    public void codepointsToString_withInts() {
        assertType(
            "fn:codepoints-to-string(65, 66, 67)",
            typeFactory.one(typeFactory.itemString())
        );
    }

    @Test
    public void codepointsToString_wrongType() {
        assertErrors("fn:codepoints-to-string('A')");
    }

    // fn:string-to-codepoints($value as xs:string?) as xs:integer*
    @Test
    public void stringToCodepoints_valid() {
        assertType(
            "fn:string-to-codepoints('Hi')",
            typeFactory.zeroOrMore(typeFactory.itemNumber())
        );
    }

    @Test
    public void stringToCodepoints_nullString() {
        assertType(
            "fn:string-to-codepoints(())",
            typeFactory.zeroOrMore(typeFactory.itemNumber())
        );
    }

    @Test
    public void stringToCodepoints_missingArg() {
        assertErrors("fn:string-to-codepoints()");
    }

    @Test
    public void stringToCodepoints_wrongType() {
        assertErrors("fn:string-to-codepoints(123)");
    }

    // fn:codepoint-equal($v1 as xs:string?, $v2 as xs:string?) as xs:boolean?
    @Test
    public void codepointEqual_valid() {
        assertType(
            "fn:codepoint-equal('a','A')",
            typeFactory.zeroOrOne(typeFactory.itemBoolean())
        );
    }

    @Test
    public void codepointEqual_nulls() {
        assertType(
            "fn:codepoint-equal((), ())",
            typeFactory.zeroOrOne(typeFactory.itemBoolean())
        );
    }

    @Test
    public void codepointEqual_missing() {
        assertErrors("fn:codepoint-equal()");
        assertErrors("fn:codepoint-equal('x')");
    }

    @Test
    public void codepointEqual_wrongType() {
        assertErrors("fn:codepoint-equal(1,'x')");
        assertErrors("fn:codepoint-equal('x',true())");
    }

    // fn:collation($options as map(*)) as xs:string
    @Test
    public void collation_requiredOptions() {
        assertType(
            "fn:collation(map{})",
            typeFactory.one(typeFactory.itemString())
        );
    }

    @Test
    public void collation_missing() {
        assertErrors("fn:collation()");
    }

    @Test
    public void collation_wrongType() {
        assertErrors("fn:collation('abc')");
    }

    // fn:collation-available($collation as xs:string, $usage as enum(...) * := ()) as xs:boolean
    @Test
    public void collationAvailable_defaultUsage() {
        assertType(
            "fn:collation-available('en')",
            typeFactory.one(typeFactory.itemBoolean())
        );
    }

    @Test
    public void collationAvailable_withUsage() {
        assertType(
            "fn:collation-available('en','compare','key')",
            typeFactory.one(typeFactory.itemBoolean())
        );
    }

    @Test
    public void collationAvailable_wrongUsage() {
        assertErrors("fn:collation-available('en','invalid')");
    }

    @Test
    public void collationAvailable_missingOrWrong() {
        assertErrors("fn:collation-available()");
        assertErrors("fn:collation-available(1)");
    }

    // fn:collation-key($value as xs:string, $collation as xs:string? := fn:default-collation()) as xs:base64Binary
    // @Test
    // public void collationKey_requiredValue() {
    //     assertType(
    //         "fn:collation-key('abc')",
    //         typeFactory.one(typeFactory.itemBase64Binary())
    //     );
    // }

    // @Test
    // public void collationKey_withCollation() {
    //     assertType(
    //         "fn:collation-key('x','uci')",
    //         typeFactory.one(typeFactory.itemBase64Binary())
    //     );
    // }

    @Test
    public void collationKey_missingValue() {
        assertErrors("fn:collation-key()");
    }

    @Test
    public void collationKey_wrongTypes() {
        assertErrors("fn:collation-key(1)");
        assertErrors("fn:collation-key('x', 5)");
    }

    // fn:contains-token($value as xs:string*, $token as xs:string, $collation as xs:string? := fn:default-collation()) as xs:boolean
    @Test
    public void containsToken_minimal() {
        assertType(
            "fn:contains-token('a','a')",
            typeFactory.one(typeFactory.itemBoolean())
        );
    }

    @Test
    public void containsToken_withMultipleAndCollation() {
        assertType(
            "fn:contains-token(('x','y'),'y','uci')",
            typeFactory.one(typeFactory.itemBoolean())
        );
    }

    @Test
    public void containsToken_missingArgs() {
        assertErrors("fn:contains-token()");
        assertErrors("fn:contains-token('a')");
    }

    @Test
    public void containsToken_wrongTypes() {
        assertErrors("fn:contains-token(1,'a')");
        assertErrors("fn:contains-token('a',1)");
        assertErrors("fn:contains-token('a','a',1)");
    }
}
