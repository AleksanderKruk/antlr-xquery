package com.github.akruk.antlrxquery.languagefeatures.semantics.semanticfunctiontests.thematic;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.semantics.semanticfunctiontests.FunctionsSemanticTest;

public class ExternalInformationFunctionsTest extends FunctionsSemanticTest {

    // fn:doc($source as xs:string?, $options as map(*)? := {}) as document-node()?
    @Test
    public void doc_noArgs() {
        assertType(
                "fn:doc(())",
                typeFactory.zeroOrOne(typeFactory.itemAnyNode()));
    }

    @Test
    public void doc_withSource() {
        assertType(
                "fn:doc('http://example.com')",
                typeFactory.zeroOrOne(typeFactory.itemAnyNode()));
    }

    @Test
    public void doc_withOptions() {
        assertType(
                "fn:doc('u', map{})",
                typeFactory.zeroOrOne(typeFactory.itemAnyNode()));
    }

    @Test
    public void doc_namedArgs() {
        assertType(
                "fn:doc(source := 'u', options := map{})",
                typeFactory.zeroOrOne(typeFactory.itemAnyNode()));
    }

    @Test
    public void doc_wrongTypes() {
        assertErrors("fn:doc(<a/>)");
        assertErrors("fn:doc('u', 'notMap')");
    }

    @Test
    public void doc_tooManyArgs() {
        assertErrors("fn:doc('u', map{}, 1)");
    }

    // fn:doc-available($source as xs:string?, $options as map(*)? := {}) as
    // xs:boolean
    @Test
    public void docAvailable_noArgs() {
        assertType(
                "fn:doc-available(())",
                typeFactory.one(typeFactory.itemBoolean()));
    }

    @Test
    public void docAvailable_withArgs() {
        assertType(
                "fn:doc-available('u', map{})",
                typeFactory.one(typeFactory.itemBoolean()));
    }

    @Test
    public void docAvailable_wrong() {
        assertErrors("fn:doc-available(1)");
        assertErrors("fn:doc-available('u','x')");
    }

    // fn:collection($source as xs:string? := ()) as item()*
    @Test
    public void collection_noArgs() {
        assertType(
                "fn:collection()",
                typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }

    @Test
    public void collection_withSource() {
        assertType(
                "fn:collection('col')",
                typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }

    @Test
    public void collection_wrong() {
        assertErrors("fn:collection(1)");
    }

    // fn:uri-collection($source as xs:string? := ()) as xs:anyURI*
    // @Test public void uriCollection_noArgs() {
    // assertType(
    // "fn:uri-collection()",
    // typeFactory.zeroOrMore(typeFactory.anyURI_())
    // );
    // }
    // @Test public void uriCollection_withSource() {
    // assertType(
    // "fn:uri-collection('u')",
    // typeFactory.zeroOrMore(typeFactory.anyURI_())
    // );
    // }
    @Test
    public void uriCollection_wrong() {
        assertErrors("fn:uri-collection(1)");
    }

    // fn:unparsed-text($source as xs:string?, $options as (xs:string|map(*))? :=
    // ()) as xs:string?
    @Test
    public void unparsedText_noArgs() {
        assertType(
                "fn:unparsed-text(())",
                typeFactory.zeroOrOne(typeFactory.itemString()));
    }

    @Test
    public void unparsedText_withSourceAndStringOpts() {
        assertType(
                "fn:unparsed-text('u','encoding=UTF-8')",
                typeFactory.zeroOrOne(typeFactory.itemString()));
    }

    @Test
    public void unparsedText_withMapOpts() {
        assertType(
                "fn:unparsed-text('u', map{})",
                typeFactory.zeroOrOne(typeFactory.itemString()));
    }

    @Test
    public void unparsedText_wrong() {
        assertErrors("fn:unparsed-text(1)");
        assertErrors("fn:unparsed-text('u', 1)");
    }

    // fn:unparsed-text-lines($source as xs:string?, $options as (xs:string|map(*))?
    // := ()) as xs:string*
    @Test
    public void unparsedTextLines_noArgs() {
        assertType(
                "fn:unparsed-text-lines(())",
                typeFactory.zeroOrMore(typeFactory.itemString()));
    }

    @Test
    public void unparsedTextLines_withArgs() {
        assertType(
                "fn:unparsed-text-lines('u', 'encoding=UTF-8')",
                typeFactory.zeroOrMore(typeFactory.itemString()));
    }

    @Test
    public void unparsedTextLines_wrong() {
        assertErrors("fn:unparsed-text-lines(1)");
    }

    // fn:unparsed-text-available($source as xs:string?, $options as
    // (xs:string|map(*))? := ()) as xs:boolean
    @Test
    public void unparsedTextAvailable_minimal() {
        assertType(
                "fn:unparsed-text-available(())",
                typeFactory.one(typeFactory.itemBoolean()));
    }

    @Test
    public void unparsedTextAvailable_withArgs() {
        assertType(
                "fn:unparsed-text-available('u', map{})",
                typeFactory.one(typeFactory.itemBoolean()));
    }

    @Test
    public void unparsedTextAvailable_wrong() {
        assertErrors("fn:unparsed-text-available(1, 'opt')");
    }

    // fn:unparsed-binary($source as xs:string?) as xs:base64Binary?
    // @Test public void unparsedBinary_defaults() {
    // assertType(
    // "fn:unparsed-binary()",
    // typeFactory.zeroOrOne(typeFactory.itemBase64Binary())
    // );
    // }
    // @Test public void unparsedBinary_withSource() {
    // assertType(
    // "fn:unparsed-binary('u')",
    // typeFactory.zeroOrOne(typeFactory.itemBase64Binary())
    // );
    // }
    @Test
    public void unparsedBinary_wrong() {
        assertErrors("fn:unparsed-binary(1)");
        assertErrors("fn:unparsed-binary('u', map{})");
    }

    // fn:environment-variable($name as xs:string) as xs:string?
    @Test
    public void environmentVariable_valid() {
        assertType(
                "fn:environment-variable('PATH')",
                typeFactory.zeroOrOne(typeFactory.itemString()));
    }

    @Test
    public void environmentVariable_missing() {
        assertErrors("fn:environment-variable()");
    }

    @Test
    public void environmentVariable_wrongType() {
        assertErrors("fn:environment-variable(1)");
    }

    // fn:available-environment-variables() as xs:string*
    @Test
    public void availableEnvironmentVariables() {
        assertType(
                "fn:available-environment-variables()",
                typeFactory.zeroOrMore(typeFactory.itemString()));
    }

    @Test
    public void availableEnvironmentVariables_withArgs() {
        assertErrors("fn:available-environment-variables('x')");
    }
}
