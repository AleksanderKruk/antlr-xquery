package com.github.akruk.antlrxquery.functiontests.thematic;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.functiontests.FunctionsSemanticTest;

import java.util.List;

public class DynamicEvaluationFunctionsTest extends FunctionsSemanticTest {

    // fn:load-xquery-module($module-uri as xs:string, $options as map(*)? := {}) as load-xquery-module-record
    // @Test
    // public void loadXQueryModule_requiredUri() {
    //     assertType(
    //         "fn:load-xquery-module('http://mod')",
    //         typeFactory.one(typeFactory.item("load-xquery-module-record"))
    //     );
    // }
    // @Test
    // public void loadXQueryModule_withOptions() {
    //     assertType(
    //         "fn:load-xquery-module('mod', map{})",
    //         typeFactory.one(typeFactory.item("load-xquery-module-record"))
    //     );
    // }
    // @Test
    // public void loadXQueryModule_namedArgs() {
    //     assertType(
    //         "fn:load-xquery-module(module-uri := 'mod', options := map{})",
    //         typeFactory.one(typeFactory.item("load-xquery-module-record"))
    //     );
    // }
    // @Test
    // public void loadXQueryModule_wrongTypesAndArity() {
    //     assertErrors("fn:load-xquery-module(1)");
    //     assertErrors("fn:load-xquery-module('m','x')");
    //     assertErrors("fn:load-xquery-module('m', map{}, 1)");
    // }

    // // fn:transform($options as map(*)) as map(*)
    // @Test
    // public void transform_requiredOptions() {
    //     assertType(
    //         "fn:transform(map{'a':1})",
    //         typeFactory.one(typeFactory.itemAnyMap())
    //     );
    // }
    // @Test
    // public void transform_missingArg() {
    //     assertErrors("fn:transform()");
    // }
    // @Test
    // public void transform_wrongType() {
    //     assertErrors("fn:transform('x')");
    // }
    // @Test
    // public void transform_tooManyArgs() {
    //     assertErrors("fn:transform(map{}, map{})");
    // }

    // fn:op($operator as xs:string) as fn(item()*, item()*) as item()*
    @Test
    public void op_validOperator() {
        assertType(
            "fn:op('+')",
            typeFactory.one(
                typeFactory.itemFunction(
                    typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                    List.of(
                        typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                        typeFactory.zeroOrMore(typeFactory.itemAnyItem())
                    )
                )
            )
        );
    }
    @Test
    public void op_missingOrExtra() {
        assertErrors("fn:op()");
        assertErrors("fn:op('+','-')");
    }
    @Test
    public void op_wrongType() {
        assertErrors("fn:op(1)");
    }
}
