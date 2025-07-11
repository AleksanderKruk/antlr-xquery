package com.languagefeaturessemantics;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.XQuerySemanticAnalyzerTest;

public class NodeConstructorTests extends XQuerySemanticAnalyzerTest  {
    @Test
    public void directElementConstructor() {
        assertType("<a></a>", typeFactory.element(Set.of("a")));
    }

    @Test
    public void shortenedElementConstructor() {
        assertType("<a/>", typeFactory.element(Set.of("a")));
    }

}
