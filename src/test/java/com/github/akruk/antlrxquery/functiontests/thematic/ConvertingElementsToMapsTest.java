package com.github.akruk.antlrxquery.functiontests.thematic;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.functiontests.FunctionsSemanticTest;

public class ConvertingElementsToMapsTest extends FunctionsSemanticTest {

    // fn:element-to-map-plan($input as (document-node()|element(*))*) as map(xs:string, record(*))
    @Test
    public void elementToMapPlan_noArgs() {
        assertType(
            "fn:element-to-map-plan()",
            typeFactory.one(typeFactory.itemAnyMap())
        );
    }

    @Test
    public void elementToMapPlan_withElements() {
        assertType(
            "fn:element-to-map-plan(<a/>, <b/>)",
            typeFactory.one(typeFactory.itemAnyMap())
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
    public void elementToMap_noArgs() {
        assertType(
            "fn:element-to-map()",
            typeFactory.zeroOrOne(typeFactory.itemAnyMap())
        );
    }

    @Test
    public void elementToMap_withElement() {
        assertType(
            "fn:element-to-map(<x/>)",
            typeFactory.zeroOrOne(typeFactory.itemAnyMap())
        );
    }

    @Test
    public void elementToMap_withElementAndOptions() {
        assertType(
            "fn:element-to-map(<x/>, map{'a':1})",
            typeFactory.zeroOrOne(typeFactory.itemAnyMap())
        );
    }

    @Test
    public void elementToMap_namedArgs() {
        assertType(
            "fn:element-to-map(element := <x/>, options := map{})",
            typeFactory.zeroOrOne(typeFactory.itemAnyMap())
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
