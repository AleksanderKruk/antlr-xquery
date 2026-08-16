package com.github.akruk.antlrquery.languagefeatures.semantics.functions;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrquery.languagefeatures.semantics.SemanticTestsBase;

import java.util.List;

public class DynamicEvaluationFunctionsTest extends SemanticTestsBase {

    // fn:load-xquery-module($module-uri as xs:string, $options as map(*)? := {}) as load-xquery-module-record

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
