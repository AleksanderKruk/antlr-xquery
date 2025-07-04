package com.github.akruk.antlrxquery.functiontests.thematic;
import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.functiontests.FunctionsSemanticTest;

public class ProcessingBooleanFunctionsTest extends FunctionsSemanticTest {

    // fn:true() as xs:boolean
    @Test public void true_noArgs_returnsBoolean() {
        assertType(
            "fn:true()",
            typeFactory.one(typeFactory.itemBoolean())
        );
    }
    @Test public void true_withArgs_error() {
        assertErrors("fn:true(1)");
    }

    // fn:false() as xs:boolean
    @Test public void false_noArgs_returnsBoolean() {
        assertType(
            "fn:false()",
            typeFactory.one(typeFactory.itemBoolean())
        );
    }
    @Test public void false_withArgs_error() {
        assertErrors("fn:false('x')");
    }

    // op:boolean-equal($value1 as xs:boolean, $value2 as xs:boolean) as xs:boolean
    @Test public void booleanEqual_valid() {
        assertType(
            "op:boolean-equal(true(), false())",
            typeFactory.one(typeFactory.itemBoolean())
        );
    }
    @Test public void booleanEqual_wrongTypes() {
        assertErrors("op:boolean-equal(1, true())");
        assertErrors("op:boolean-equal(true(), 'x')");
    }
    @Test public void booleanEqual_arityErrors() {
        assertErrors("op:boolean-equal(true())");
        assertErrors("op:boolean-equal()");
        assertErrors("op:boolean-equal(true(), false(), true())");
    }

    // op:boolean-less-than($arg1 as xs:boolean, $arg2 as xs:boolean) as xs:boolean
    @Test public void booleanLessThan_valid() {
        assertType(
            "op:boolean-less-than(false(), true())",
            typeFactory.one(typeFactory.itemBoolean())
        );
    }
    @Test public void booleanLessThan_wrongTypes() {
        assertErrors("op:boolean-less-than((), true())");
        assertErrors("op:boolean-less-than(true(), 0)");
    }
    @Test public void booleanLessThan_arityErrors() {
        assertErrors("op:boolean-less-than(true())");
        assertErrors("op:boolean-less-than()");
        assertErrors("op:boolean-less-than(false(),true(),false())");
    }

    // fn:boolean($input as item()*) as xs:boolean
    @Test public void boolean_noArgs_returnsBoolean() {
        assertType(
            "fn:boolean()",
            typeFactory.one(typeFactory.itemBoolean())
        );
    }
    @Test public void boolean_withVariousItems() {
        assertType(
            "fn:boolean(0, '', <a/>, false())",
            typeFactory.one(typeFactory.itemBoolean())
        );
    }

    // fn:not($input as item()*) as xs:boolean
    @Test public void not_noArgs_returnsBoolean() {
        assertType(
            "fn:not()",
            typeFactory.one(typeFactory.itemBoolean())
        );
    }
    @Test public void not_withBoolean() {
        assertType(
            "fn:not(true())",
            typeFactory.one(typeFactory.itemBoolean())
        );
    }
    @Test public void not_withOtherItems() {
        assertType(
            "fn:not(0, 'x', <a/>)",
            typeFactory.one(typeFactory.itemBoolean())
        );
    }
}
