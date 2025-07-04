package com.github.akruk.antlrxquery.functiontests.thematic;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.functiontests.FunctionsSemanticTest;

public class ContextFunctionsTest extends FunctionsSemanticTest {

    // fn:position() as xs:integer
    @Test public void position_noArgs() {
        assertType(
            "fn:position()",
            typeFactory.one(typeFactory.itemNumber())
        );
    }
    @Test public void position_withArg_error() {
        assertErrors("fn:position(1)");
    }

    // fn:last() as xs:integer
    @Test public void last_noArgs() {
        assertType(
            "fn:last()",
            typeFactory.one(typeFactory.itemNumber())
        );
    }
    @Test public void last_withArg_error() {
        assertErrors("fn:last('x')");
    }

    // fn:current-dateTime() as xs:dateTimeStamp
    // @Test public void currentDateTime_noArgs() {
    //     assertType(
    //         "fn:current-dateTime()",
    //         typeFactory.one(typeFactory.itemDateTimeStamp())
    //     );
    // }

    @Test public void currentDateTime_withArg_error() {
        assertErrors("fn:current-dateTime(1)");
    }

    // fn:current-date() as xs:date
    // @Test public void currentDate_noArgs() {
    //     assertType(
    //         "fn:current-date()",
    //         typeFactory.one(typeFactory.itemDate())
    //     );
    // }
    @Test public void currentDate_withArg_error() {
        assertErrors("fn:current-date('x')");
    }

    // fn:current-time() as xs:time
    // @Test public void currentTime_noArgs() {
    //     assertType(
    //         "fn:current-time()",
    //         typeFactory.one(typeFactory.itemTime())
    //     );
    // }

    @Test public void currentTime_withArg_error() {
        assertErrors("fn:current-time(())");
    }

    // fn:implicit-timezone() as xs:dayTimeDuration
    // @Test public void implicitTimezone_noArgs() {
    //     assertType(
    //         "fn:implicit-timezone()",
    //         typeFactory.one(typeFactory.itemDayTimeDuration())
    //     );
    // }
    @Test public void implicitTimezone_withArg_error() {
        assertErrors("fn:implicit-timezone(true())");
    }

    // fn:default-collation() as xs:string
    @Test public void defaultCollation_noArgs() {
        assertType(
            "fn:default-collation()",
            typeFactory.one(typeFactory.itemString())
        );
    }
    @Test public void defaultCollation_withArg_error() {
        assertErrors("fn:default-collation('x')");
    }

    // fn:default-language() as xs:language
    @Test public void defaultLanguage_noArgs() {
        assertType(
            "fn:default-language()",
            typeFactory.one(typeFactory.itemString())
        );
    }
    @Test public void defaultLanguage_withArg_error() {
        assertErrors("fn:default-language(1)");
    }

    // fn:static-base-uri() as xs:anyURI?
    // @Test public void staticBaseUri_noArgs() {
    //     assertType(
    //         "fn:static-base-uri()",
    //         typeFactory.zeroOrOne(typeFactory.anyURI_())
    //     );
    // }
    @Test public void staticBaseUri_withArg_error() {
        assertErrors("fn:static-base-uri(<a/>)");
    }
}
