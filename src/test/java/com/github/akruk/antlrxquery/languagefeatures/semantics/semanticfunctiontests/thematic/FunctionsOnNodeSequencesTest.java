package com.github.akruk.antlrxquery.languagefeatures.semantics.semanticfunctiontests.thematic;


import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.semantics.SemanticTestsBase;


public class FunctionsOnNodeSequencesTest extends SemanticTestsBase {
    @Test
    public void distinctOrderedNodes_noArgs() {
        assertType("fn:distinct-ordered-nodes(())", typeFactory.zeroOrMore(typeFactory.itemAnyNode()));
    }

    @Test
    public void distinctOrderedNodes_withNodes() {
        assertType("let $x as node()? := () return fn:distinct-ordered-nodes($x)", typeFactory.zeroOrMore(typeFactory.itemAnyNode()));
    }

    @Test
    public void distinctOrderedNodes_wrongType() {
        assertErrors("fn:distinct-ordered-nodes(1, 'x')");
    }

    // --- fn:innermost(node()*) as node()* ---

    @Test
    public void innermost_defaults() {
        assertType("fn:innermost(())",typeFactory.zeroOrMore(typeFactory.itemAnyNode()));
    }

    @Test
    public void innermost_withNodes() {
        assertType("let $x as node()? := () return fn:innermost($x)", typeFactory.zeroOrMore(typeFactory.itemAnyNode()));
    }

    @Test
    public void innermost_bad() {
        assertErrors("fn:innermost( 'x' )");
    }

    // --- fn:outermost(node()*) as node()* ---

    @Test
    public void outermost_defaults() {
        assertType("fn:outermost(())",typeFactory.zeroOrMore(typeFactory.itemAnyNode()));
    }

    @Test
    public void outermost_withNodes() {
        assertType("let $x as node()? := () return fn:outermost($x)", typeFactory.zeroOrMore(typeFactory.itemAnyNode()));
    }

    @Test
    public void outermost_bad() {
        assertErrors("fn:outermost(0)");
    }

}
