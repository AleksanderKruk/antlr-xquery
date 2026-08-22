package com.github.akruk.antlrquery.languagefeatures.semantics.functions;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrquery.languagefeatures.semantics.SemanticTestsBase;

public class OtherFunctionsOnNodesTest extends SemanticTestsBase{

    @Test
    public void root_default() {
        assertType("fn:root()", typeFactory.zeroOrOne(typeFactory.itemAnyNode()));
    }

    @Test
    public void root_node() {
        assertNoErrors(analyze("let $x as node()? := () return fn:root($x)"));
    }

    @Test
    public void root_wrong() {
        assertErrors("fn:root('x')");
    }

    @Test
    public void path_default() {
        assertType("fn:path()", typeFactory.zeroOrOne(typeFactory.itemString()));
    }

    @Test
    public void path_withOptions() {
        var r = analyze("fn:path(., map{})");
        assertNoErrors(r);
    }

    @Test
    public void path_bad() {
        assertErrors("fn:path(1,2)");
    }

    @Test
    public void hasChildren_default() {
        assertType("fn:has-children()",typeFactory.boolean_());
    }

    @Test
    public void hasChildren_node() {
        assertNoErrors(analyze("let $x as node()? := () return fn:has-children($x)"));
    }

    @Test
    public void hasChildren_invalid() {
        assertErrors("fn:has-children(1)");
    }

    @Test
    public void siblings_default() {
        assertType("fn:siblings()",typeFactory.zeroOrMore(typeFactory.itemAnyNode()));
    }

    @Test
    public void siblings_node() {
        assertNoErrors(analyze("fn:siblings(())"));
    }

    @Test
    public void siblings_wrong() {
        assertErrors("fn:siblings('x')");
    }

}
