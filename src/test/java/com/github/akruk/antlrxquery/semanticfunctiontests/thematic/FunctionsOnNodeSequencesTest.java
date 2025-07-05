package com.github.akruk.antlrxquery.semanticfunctiontests.thematic;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.semanticfunctiontests.FunctionsSemanticTest;

public class FunctionsOnNodeSequencesTest extends FunctionsSemanticTest {
    @Test
    public void distinctOrderedNodes_noArgs() {
        assertType("fn:distinct-ordered-nodes()", typeFactory.zeroOrMore(typeFactory.itemAnyNode()));
    }

    @Test
    public void distinctOrderedNodes_withNodes() {
        assertType("fn:distinct-ordered-nodes(<a/>, <b/>)", typeFactory.zeroOrMore(typeFactory.itemAnyNode()));
    }

    @Test
    public void distinctOrderedNodes_wrongType() {
        assertErrors("fn:distinct-ordered-nodes(1, 'x')");
    }

    // --- fn:innermost(node()*) as node()* ---

    @Test
    public void innermost_defaults() {
        assertType("fn:innermost()",typeFactory.zeroOrMore(typeFactory.itemAnyNode()));
    }

    @Test
    public void innermost_withNodes() {
        assertNoErrors(analyze("fn:innermost(<a/><a><b/></a>)"));
    }

    @Test
    public void innermost_bad() {
        assertErrors("fn:innermost( 'x' )");
    }

    // --- fn:outermost(node()*) as node()* ---

    @Test
    public void outermost_defaults() {
        assertType("fn:outermost()",typeFactory.zeroOrMore(typeFactory.itemAnyNode()));
    }

    @Test
    public void outermost_withNodes() {
        assertNoErrors(analyze("fn:outermost(<a/><a><b/></a>)"));
    }

    @Test
    public void outermost_bad() {
        assertErrors("fn:outermost(0)");
    }

}
