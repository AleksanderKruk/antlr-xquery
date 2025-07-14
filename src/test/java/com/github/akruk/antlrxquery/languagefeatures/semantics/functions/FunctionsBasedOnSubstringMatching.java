package com.github.akruk.antlrxquery.languagefeatures.semantics.functions;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.semantics.SemanticTestsBase;

public class FunctionsBasedOnSubstringMatching extends SemanticTestsBase {

    // fn:contains($value as xs:string?, $substring as xs:string?, $collation as
    // xs:string? := default) as xs:boolean
    @Test
    public void contains_positional() {
        assertType("fn:contains('abracadabra','cad')",
                typeFactory.boolean_());
    }

    @Test
    public void contains_namedArgs() {
        assertType("fn:contains(substring := 'bra', value := 'abracadabra')", typeFactory.boolean_());
    }

    @Test
    public void contains_withCollation() {
        assertType("fn:contains('a','A','uci')", typeFactory.boolean_());
    }

    @Test
    public void contains_defaultCollation() {
        assertType("fn:contains('','')", typeFactory.boolean_());
    }

    @Test
    public void contains_tooFewArguments() {
        assertErrors("fn:contains('a')");
        assertErrors("fn:contains()");
    }

    @Test
    public void contains_tooManyArguments() {
        assertErrors("fn:contains('a','b','c','d')");
    }

    @Test
    public void contains_wrongTypes() {
        assertErrors("fn:contains(1,'x')");
        assertErrors("fn:contains('x',1)");
        assertErrors("fn:contains('x','y',1)");
    }

    // fn:starts-with($value as xs:string?, $substring as xs:string?, $collation as
    // xs:string? := default) as xs:boolean
    @Test
    public void startsWith_positional() {
        assertType("fn:starts-with('hello','he')",
                typeFactory.boolean_());
    }

    @Test
    public void startsWith_namedArgs() {
        assertType("fn:starts-with(substring := 'lo', value := 'hello')",
                typeFactory.boolean_());
    }

    @Test
    public void startsWith_withCollation() {
        assertType("fn:starts-with('X','x','uci')",
                typeFactory.boolean_());
    }

    @Test
    public void startsWith_missingArguments() {
        assertErrors("fn:starts-with('x')");
        assertErrors("fn:starts-with()");
    }

    @Test
    public void startsWith_wrongTypes() {
        assertErrors("fn:starts-with(1,'h')");
        assertErrors("fn:starts-with('h',1)");
        assertErrors("fn:starts-with('h','h',true())");
    }

    // fn:ends-with($value as xs:string?, $substring as xs:string?, $collation as
    // xs:string? := default) as xs:boolean
    @Test
    public void endsWith_positional() {
        assertType("fn:ends-with('testing','ing')", typeFactory.boolean_());
    }

    @Test
    public void endsWith_namedArgs() {
        assertType("fn:ends-with(value := 'abc', substring := 'bc')", typeFactory.boolean_());
    }

    @Test
    public void endsWith_withCollation() {
        assertType("fn:ends-with('A','a','uci')",
                typeFactory.boolean_());
    }

    @Test
    public void endsWith_arityErrors() {
        assertErrors("fn:ends-with('a')");
        assertErrors("fn:ends-with()");
    }

    @Test
    public void endsWith_wrongTypes() {
        assertErrors("fn:ends-with(1,'x')");
        assertErrors("fn:ends-with('x',1)");
    }

    // fn:substring-before($value as xs:string?, $substring as xs:string?,
    // $collation as xs:string? := default) as xs:string
    @Test
    public void substringBefore_positional() {
        assertType("fn:substring-before('abcXYZ','XYZ')",
                typeFactory.string());
    }

    @Test
    public void substringBefore_namedArgs() {
        assertType("fn:substring-before(substring := 'lo', value := 'hello')",
                typeFactory.string());
    }

    @Test
    public void substringBefore_withCollation() {
        assertType("fn:substring-before('FoO','o','uci')",
                typeFactory.string());
    }

    @Test
    public void substringBefore_arityErrors() {
        assertErrors("fn:substring-before('x')");
        assertErrors("fn:substring-before()");
        assertErrors("fn:substring-before('x','y','z','w')");
    }

    @Test
    public void substringBefore_wrongTypes() {
        assertErrors("fn:substring-before(1,'a')");
        assertErrors("fn:substring-before('a',true())");
        assertErrors("fn:substring-before('a','b',0)");
    }

    // fn:substring-after($value as xs:string?, $substring as xs:string?, $collation
    // as xs:string? := default) as xs:string
    @Test
    public void substringAfter_positional() {
        assertType("fn:substring-after('abcXYZ','abc')",
                typeFactory.string());
    }

    @Test
    public void substringAfter_namedArgs() {
        assertType("fn:substring-after(value := 'abracadabra', substring := 'cad')",
                typeFactory.string());
    }

    @Test
    public void substringAfter_withCollation() {
        assertType("fn:substring-after('ABC','b','uci')",
                typeFactory.string());
    }

    @Test
    public void substringAfter_arityErrors() {
        assertErrors("fn:substring-after('x')");
        assertErrors("fn:substring-after()");
        assertErrors("fn:substring-after('x','y',1)");
    }

    @Test
    public void substringAfter_wrongTypes() {
        assertErrors("fn:substring-after(1,'a')");
        assertErrors("fn:substring-after('a',1)");
    }
}
