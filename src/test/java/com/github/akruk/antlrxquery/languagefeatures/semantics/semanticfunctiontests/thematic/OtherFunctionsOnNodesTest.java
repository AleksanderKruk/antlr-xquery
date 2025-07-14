package com.github.akruk.antlrxquery.languagefeatures.semantics.semanticfunctiontests.thematic;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.semantics.semanticfunctiontests.FunctionsSemanticTest;

public class OtherFunctionsOnNodesTest extends FunctionsSemanticTest{

    // @Test
    // public void name_default() {
    //     assertType("fn:name()",typeFactory.string());
    // }

    // @Test
    // public void name_node() {
    //     assertNoErrors(analyze("fn:name(<b/>)"));
    // }

    // @Test
    // public void name_invalid() {
    //     assertErrors("fn:name( true() )");
    // }

    // @Test
    // public void localName_default() {
    //     assertType("fn:local-name()",typeFactory.string());
    // }

    // @Test
    // public void localName_node() {
    //     assertNoErrors(analyze("fn:local-name(<ns:a xmlns:ns='u'/>)"));
    // }

    // @Test
    // public void localName_bad() {
    //     assertErrors("fn:local-name(123)");
    // }

    // @Test
    // public void namespaceUri_default() {
    //     assertType("fn:namespace-uri()",typeFactory.string());
    // }

    // @Test
    // public void namespaceUri_node() {
    //     assertNoErrors(analyze("fn:namespace-uri(<ns:a xmlns:ns='u'/>)"));
    // }

    // @Test
    // public void namespaceUri_bad() {
    //     assertErrors("fn:namespace-uri(1)");
    // }

    // @Test
    // public void lang_positional() {
    //     assertType("fn:lang('en')",typeFactory.boolean_());
    // }

    // @Test
    // public void lang_namedNode() {
    //     var r = analyze("fn:lang(language := 'pl', node := <a xml:lang='pl'/>)");
    //     assertNoErrors(r);
    // }

    // @Test
    // public void lang_missingLang() {
    //     assertErrors("fn:lang()");
    // }

    // @Test
    // public void lang_badLangType() {
    //     assertErrors("fn:lang(1)");
    // }

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
