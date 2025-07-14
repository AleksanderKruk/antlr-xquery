package com.github.akruk.antlrxquery.languagefeatures.semantics.semanticfunctiontests.thematic;


import org.junit.jupiter.api.Test;


import com.github.akruk.antlrxquery.languagefeatures.semantics.SemanticTestsBase;
public class ContextFunctionsTest extends SemanticTestsBase {

    // fn:position() as xs:integer
    @Test
    public void position_withoutFocus() {
        assertErrors("fn:position()");
    }

    @Test
    public void position_withFocus() {
        assertNoErrors("(1, 2, 3) ! fn:position()");
    }


    @Test
    public void position_withArg_error() {
        assertErrors("(1, 2, 3) ! fn:position(1)");
    }

    // fn:last() as xs:integer
    @Test
    public void last_withoutFocus() {
        assertErrors( "fn:last()");
    }

    @Test
    public void last_withFocus() {
        assertNoErrors("(1, 2, 3) ! fn:last()");
    }

    @Test
    public void last_withArg_error() {
        assertErrors("(1, 2, 3) ! fn:last('x')");
    }


    // fn:current-dateTime() as xs:dateTimeStamp
    @Test
    public void currentDateTime_noArgs() {
        assertType(
                "fn:current-dateTime()",
                typeFactory.one(typeFactory.itemString()));
    }

    @Test
    public void currentDateTime_withArg_error() {
        assertErrors("fn:current-dateTime(1)");
    }

    // fn:current-date() as xs:date
    @Test
    public void currentDate_noArgs() {
        assertType(
                "fn:current-date()",
                typeFactory.one(typeFactory.itemString()));
    }

    @Test
    public void currentDate_withArg_error() {
        assertErrors("fn:current-date('x')");
    }

    // fn:current-time() as xs:time
    @Test
    public void currentTime_noArgs() {
        assertType(
                "fn:current-time()",
                typeFactory.one(typeFactory.itemString()));
    }

    @Test
    public void currentTime_withArg_error() {
        assertErrors("fn:current-time(())");
    }

    // fn:implicit-timezone() as xs:dayTimeDuration
    @Test
    public void implicitTimezone_noArgs() {
        assertType(
                "fn:implicit-timezone()",
                typeFactory.one(typeFactory.itemString()));
    }

    @Test
    public void implicitTimezone_withArg_error() {
        assertErrors("fn:implicit-timezone(true())");
    }

    // fn:default-collation() as xs:string
    @Test
    public void defaultCollation_noArgs() {
        assertType(
                "fn:default-collation()",
                typeFactory.string());
    }

    @Test
    public void defaultCollation_withArg_error() {
        assertErrors("fn:default-collation('x')");
    }

    // fn:default-language() as xs:language
    @Test
    public void defaultLanguage_noArgs() {
        assertType(
                "fn:default-language()",
                typeFactory.string());
    }

    @Test
    public void defaultLanguage_withArg_error() {
        assertErrors("fn:default-language(1)");
    }

}
