package com.github.akruk.antlrxquery.semanticfunctiontests.thematic;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.semanticfunctiontests.FunctionsSemanticTest;

public class ProcessingTypesFunctionsTest extends FunctionsSemanticTest {

    // fn:schema-type($name as xs:QName) as schema-type-record?
    // @Test public void schemaType_validQName() {
    //     assertType(
    //         "fn:schema-type(xs:QName('', 'myType'))",
    //         typeFactory.zeroOrOne(typeFactory.item("schema-type-record"))
    //     );
    // }
    @Test public void schemaType_missingArg() {
        assertErrors("fn:schema-type()");
    }
    @Test public void schemaType_wrongType() {
        assertErrors("fn:schema-type('notQName')");
    }

    // fn:type-of($value as item()*) as xs:string
    @Test public void typeOf_noArgs() {
        assertType(
            "fn:type-of()",
            typeFactory.string()
        );
    }
    @Test public void typeOf_withValues() {
        assertType(
            "fn:type-of(1, <a/>, 'text')",
            typeFactory.string()
        );
    }
    @Test public void typeOf_tooManyArgs() {
        assertErrors("fn:type-of(1, 2)");
    }

    // fn:atomic-type-annotation($value as xs:anyAtomicType) as schema-type-record
    // @Test public void atomicTypeAnnotation_withString() {
    //     assertType(
    //         "fn:atomic-type-annotation('hello')",
    //         typeFactory.one(typeFactory.item("schema-type-record"))
    //     );
    // }
    // @Test public void atomicTypeAnnotation_withNumber() {
    //     assertType(
    //         "fn:atomic-type-annotation(123)",
    //         typeFactory.one(typeFactory.item("schema-type-record"))
    //     );
    // }
    @Test public void atomicTypeAnnotation_missingOrTooMany() {
        assertErrors("fn:atomic-type-annotation()");
        assertErrors("fn:atomic-type-annotation('x', 'y')");
    }
    @Test public void atomicTypeAnnotation_wrongType() {
        assertErrors("fn:atomic-type-annotation(<a/>)");
    }

    // fn:node-type-annotation($node as element()|attribute()) as schema-type-record
    // @Test public void nodeTypeAnnotation_element() {
    //     assertType(
    //         "fn:node-type-annotation(<x/>)",
    //         typeFactory.one(typeFactory.item("schema-type-record"))
    //     );
    // }
    // @Test public void nodeTypeAnnotation_attribute() {
    //     assertType(
    //         "fn:node-type-annotation(attribute::a{'v'})",
    //         typeFactory.one(typeFactory.item("schema-type-record"))
    //     );
    // }
    @Test public void nodeTypeAnnotation_missingOrExtra() {
        assertErrors("fn:node-type-annotation()");
        assertErrors("fn:node-type-annotation(<a/>, <b/>)");
    }
    @Test public void nodeTypeAnnotation_wrongType() {
        assertErrors("fn:node-type-annotation('x')");
    }
}
