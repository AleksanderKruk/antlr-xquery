package com.github.akruk.antlrxquery.languagefeatures.semantics.semanticfunctiontests.thematic;


import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.semantics.SemanticTestsBase;


public class ProcessingTypesFunctionsTest extends SemanticTestsBase {
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
