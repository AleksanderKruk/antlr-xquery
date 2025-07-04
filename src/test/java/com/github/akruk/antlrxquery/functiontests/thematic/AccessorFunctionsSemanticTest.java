package com.github.akruk.antlrxquery.functiontests.thematic;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.functiontests.FunctionsSemanticTest;

public class AccessorFunctionsSemanticTest extends FunctionsSemanticTest {
    @Test
    public void nodeName_defaultContext() {
        assertType("fn:node-name()", typeFactory.zeroOrOne(typeFactory.itemString()));
    }

    // @Test
    // public void nodeName_explicitNode() {
    //     assertNoErrors(analyze("fn:node-name(<a/>)"));
    //     assertTrue(typeFactory.zeroOrOne(typeFactory.itemString()).equals(analyze("fn:node-name(<a/>))").type()));
    // }

    @Test
    public void nodeName_wrongType() {
        assertErrors("fn:node-name(1)");
    }

    @Test
    public void nilled_default() {
        assertType("fn:nilled()", typeFactory.zeroOrOne(typeFactory.itemBoolean()));
    }

    // @Test
    // public void nilled_onNode() {
    //     assertNoErrors(analyze("fn:nilled(<a nilled='true'/>)"));
    // }

    @Test
    public void nilled_bad() {
        assertErrors("fn:nilled('x')");
    }

    @Test
    public void string_defaultContext() {
        assertType("fn:string()", typeFactory.one(typeFactory.itemString()));
    }

    @Test
    public void string_fromNumber() {
        assertType("fn:string(123)", typeFactory.string());
    }

    @Test
    public void string_extraArg() {
        assertErrors("fn:string(1,2)");
    }

    @Test
    public void data_default() {
        assertType("fn:data()", typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }

    @Test
    public void data_seq() {
        assertType("fn:data((1,'x'))",typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }

    @Test
    public void baseUri_default() {
        assertType("fn:base-uri()", typeFactory.zeroOrOne(typeFactory.itemString()));
    }

    // @Test
    // public void baseUri_node() {
    //     assertNoErrors(analyze("fn:base-uri(<a xml:base='u'/>)"));
    // }

    @Test
    public void baseUri_wrong() {
        assertErrors("fn:base-uri(1)");
    }

    @Test
    public void documentUri_default() {
        assertType("fn:document-uri()", typeFactory.zeroOrOne(typeFactory.itemString()));
    }

    // @Test
    // public void documentUri_node() {
    //     assertNoErrors(analyze("fn:document-uri(<a/> )"));
    // }

    @Test
    public void documentUri_bad() {
        assertErrors("fn:document-uri('x')");
    }


}
