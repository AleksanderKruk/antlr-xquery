package com.github.akruk.antlrxquery.semanticfunctiontests.thematic;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.semanticfunctiontests.FunctionsSemanticTest;

import java.util.Set;

public class NodeIdentifierFunctionsTest extends FunctionsSemanticTest {

    // fn:id($values as xs:string*, $node as node() := .) as element()*
    @Test public void id_singleValueDefaultNode() {
        assertType(
            "fn:id('a')",
            typeFactory.zeroOrMore(typeFactory.itemElement(Set.of()))
        );
    }
    @Test public void id_multipleValuesAndExplicitNode() {
        assertType(
            "fn:id('x','y', <root/>)",
            typeFactory.zeroOrMore(typeFactory.itemElement(Set.of()))
        );
    }
    @Test public void id_namedArgs() {
        assertType(
            "fn:id(values := ('i1','i2'), node := <a/> )",
            typeFactory.zeroOrMore(typeFactory.itemElement(Set.of()))
        );
    }
    @Test public void id_missingValues() {
        assertErrors("fn:id()");
    }
    @Test public void id_wrongValueType() {
        assertErrors("fn:id(1)");
    }
    @Test public void id_wrongNodeType() {
        assertErrors("fn:id('i', 1)");
    }

    // fn:element-with-id($values as xs:string*, $node as node() := .) as element()*
    @Test public void elementWithId_singleValue() {
        assertType(
            "fn:element-with-id('eid')",
            typeFactory.zeroOrMore(typeFactory.itemElement(Set.of()))
        );
    }
    @Test public void elementWithId_namedNode() {
        assertType(
            "fn:element-with-id(values := ('a','b'), node := <doc/>)",
            typeFactory.zeroOrMore(typeFactory.itemElement(Set.of()))
        );
    }
    @Test public void elementWithId_errors() {
        assertErrors("fn:element-with-id()");
        assertErrors("fn:element-with-id(1, <a/>)");
        assertErrors("fn:element-with-id('x', 'y')");
    }

    // fn:idref($values as xs:string*, $node as node() := .) as node()*
    @Test public void idref_multiValues() {
        assertType(
            "fn:idref('i1','i2')",
            typeFactory.zeroOrMore(typeFactory.itemAnyNode())
        );
    }
    @Test public void idref_withNode() {
        assertType(
            "fn:idref('i', <root/> )",
            typeFactory.zeroOrMore(typeFactory.itemAnyNode())
        );
    }
    @Test public void idref_errors() {
        assertErrors("fn:idref()");
        assertErrors("fn:idref(1)");
        assertErrors("fn:idref('i', 2)");
    }

    // fn:generate-id($node as node()? := .) as xs:string
    @Test public void generateId_defaultNode() {
        assertType(
            "fn:generate-id()",
            typeFactory.one(typeFactory.itemString())
        );
    }
    @Test public void generateId_withNode() {
        assertType(
            "fn:generate-id(<a/>)",
            typeFactory.one(typeFactory.itemString())
        );
    }
    @Test public void generateId_wrongType() {
        assertErrors("fn:generate-id(1)");
    }
    @Test public void generateId_tooMany() {
        assertErrors("fn:generate-id(<a/>, <b/>)");
    }
}
