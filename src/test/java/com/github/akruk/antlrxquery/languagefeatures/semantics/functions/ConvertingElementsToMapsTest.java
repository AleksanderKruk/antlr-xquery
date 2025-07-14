package com.github.akruk.antlrxquery.languagefeatures.semantics.functions;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.semantics.SemanticTestsBase;

public class ConvertingElementsToMapsTest extends SemanticTestsBase {

    // fn:element-to-map-plan($input as (document-node()|element(*))*) as map(xs:string, record(*))
    @Test
    public void elementToMapPlan_noElements() {
        assertType(
            "fn:element-to-map-plan(())",
            typeFactory.map(typeFactory.itemString(), typeFactory.anyMap())
        );
    }

    @Test
    public void elementToMapPlan_withElements() {
        assertType(
            "let $x as element(a)* := () return fn:element-to-map-plan($x)",
            typeFactory.map(typeFactory.itemString(), typeFactory.anyMap())
        );
    }

    @Test
    public void elementToMapPlan_wrongType() {
        assertErrors("fn:element-to-map-plan(1)");
    }

    @Test
    public void elementToMapPlan_namedArgNotAllowed() {
        assertErrors("fn:element-to-map-plan(input := <a/>)");
    }


    // fn:element-to-map($element as element()?, $options as map(*)? := {}) as map(xs:string, item()?)?
    @Test
    public void elementToMap_noElements() {
        assertType(
            "fn:element-to-map(())",
            typeFactory.zeroOrOne(typeFactory.itemMap(typeFactory.itemString(), typeFactory.zeroOrOne(typeFactory.itemAnyItem())))
        );
    }

    @Test
    public void elementToMap_withElement() {
        assertType(
            "let $x as element(x)? := () return fn:element-to-map($x)",
            typeFactory.zeroOrOne(typeFactory.itemMap(typeFactory.itemString(), typeFactory.zeroOrOne(typeFactory.itemAnyItem())))
        );
    }

    @Test
    public void elementToMap_withElementAndOptions() {
        assertType(
            "fn:element-to-map((), map{'a':1})",
            typeFactory.zeroOrOne(typeFactory.itemMap(typeFactory.itemString(), typeFactory.zeroOrOne(typeFactory.itemAnyItem())))
        );
    }

    @Test
    public void elementToMap_namedArgs() {
        assertType(
            "fn:element-to-map(element := (), options := map{})",
            typeFactory.zeroOrOne(typeFactory.itemMap(typeFactory.itemString(), typeFactory.zeroOrOne(typeFactory.itemAnyItem())))
        );
    }

    @Test
    public void elementToMap_wrongTypes() {
        assertErrors("fn:element-to-map(1)");
        assertErrors("fn:element-to-map(<x/>, 'notMap')");
    }

    @Test
    public void elementToMap_tooManyArgs() {
        assertErrors("fn:element-to-map(<x/>, map{}, <y/>)");
    }
}
