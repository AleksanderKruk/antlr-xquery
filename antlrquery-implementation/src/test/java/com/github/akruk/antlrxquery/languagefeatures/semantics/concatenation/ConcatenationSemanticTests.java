package com.github.akruk.antlrxquery.languagefeatures.semantics.concatenation;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.semantics.SemanticTestsBase;

public class ConcatenationSemanticTests extends SemanticTestsBase {

    @Test
    public void concatenation() {
        assertType("'a'|| 'b'", typeFactory.string());
        assertType("'a' || ()", typeFactory.string());
        assertType(" () || ()", typeFactory.string());
        assertType("() || 'con' || ('cat', 'enate')", typeFactory.string());
    }
}
