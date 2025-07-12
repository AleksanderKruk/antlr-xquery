package com.github.akruk.antlrxquery.semanticfunctiontests.thematic;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.semanticfunctiontests.FunctionsSemanticTest;

public class ProcessingTypesFunctionsTest extends FunctionsSemanticTest {
    // fn:type-of($value as item()*) as xs:string
    @Test public void typeOf_noArgs() {
        assertType( "fn:type-of(())", typeFactory.string());
    }
    @Test public void typeOf_withValues() {
        assertType(
            "fn:type-of((1, map {}, 'text'))",
            typeFactory.string()
        );
    }
    @Test public void typeOf_tooManyArgs() {
        assertErrors("fn:type-of(1, 2)");
    }

}
