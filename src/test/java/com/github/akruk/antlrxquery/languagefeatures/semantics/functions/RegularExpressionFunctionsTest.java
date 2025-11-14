package com.github.akruk.antlrxquery.languagefeatures.semantics.functions;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.semantics.SemanticTestsBase;
import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver.QualifiedName;

import java.util.Set;

public class RegularExpressionFunctionsTest extends SemanticTestsBase {

    // fn:matches($value as xs:string?, $pattern as xs:string, $flags as xs:string? := "") as xs:boolean

    @Test public void matches_minimal() {
        assertType(
            "fn:matches('abc','b.*')",
            typeFactory.one(typeFactory.itemBoolean())
        );
    }
    @Test public void matches_withFlags() {
        assertType(
            "fn:matches('AbC','b','i')",
            typeFactory.one(typeFactory.itemBoolean())
        );
    }
    @Test public void matches_nullValue() {
        assertType(
            "fn:matches((), '.*')",
            typeFactory.one(typeFactory.itemBoolean())
        );
    }
    @Test public void matches_missingPattern() {
        assertErrors("fn:matches('x')");
        assertErrors("fn:matches()");
    }
    @Test public void matches_wrongTypes() {
        assertErrors("fn:matches(1,'p')");
        assertErrors("fn:matches('x',1)");
        assertErrors("fn:matches('x','p',1)");
    }


    // fn:replace(
    //   $value as xs:string?,
    //   $pattern as xs:string,
    //   $replacement as (xs:string | fn(untypedAtomic, untypedAtomic*) as item()?)? := (),
    //   $flags as xs:string? := ''
    // ) as xs:string

    @Test public void replace_basic() {
        assertType(
            "fn:replace('abracadabra','a','A')",
            typeFactory.string()
        );
    }
    @Test public void replace_emptyReplacement() {
        assertType(
            "fn:replace('abc','b', '')",
            typeFactory.string()
        );
    }
    @Test public void replace_withFlags() {
        assertType(
            "fn:replace('abc','a','X','g')",
            typeFactory.string()
        );
    }
    @Test public void replace_missingArgs() {
        assertErrors("fn:replace('x')");
        assertErrors("fn:replace()");
    }
    @Test public void replace_wrongTypes() {
        assertErrors("fn:replace(1,'p','r')");
        assertErrors("fn:replace('x',1,'r')");
        assertErrors("fn:replace('x','p',1)");
        assertErrors("fn:replace('x','p','r', 1)");
    }


    // fn:tokenize(
    //   $value as xs:string?,
    //   $pattern as xs:string? := (),
    //   $flags as xs:string? := ""
    // ) as xs:string*

    @Test public void tokenize_minimal() {
        assertType(
            "fn:tokenize('a b')",
            typeFactory.zeroOrMore(typeFactory.itemString())
        );
    }
    @Test public void tokenize_withPattern() {
        assertType(
            "fn:tokenize('a,b,c', ',')",
            typeFactory.zeroOrMore(typeFactory.itemString())
        );
    }
    @Test public void tokenize_withFlags() {
        assertType(
            "fn:tokenize('A|B|C','\\|','i')",
            typeFactory.zeroOrMore(typeFactory.itemString())
        );
    }
    @Test public void tokenize_wrongTypes() {
        assertErrors("fn:tokenize(1)");
        assertErrors("fn:tokenize('x', 1)");
        assertErrors("fn:tokenize('x','p',1)");
    }


    // fn:analyze-string(
    //   $value as xs:string?,
    //   $pattern as xs:string,
    //   $flags as xs:string? := ""
    // ) as element(fn:analyze-string-result)
    // fn:analyze-string
    @Test public void analyzeString_minimal() {
        assertType(
            "fn:analyze-string('abc','b')",
            typeFactory.one(typeFactory.itemElement(
                Set.of(new QualifiedName("fn", "analyze-string-result"))))
        );
    }
    @Test public void analyzeString_withFlags() {
        assertType(
            "fn:analyze-string('AbC','b','i')",
            typeFactory.one(typeFactory.itemElement(
                Set.of(new QualifiedName("fn", "analyze-string-result"))))
        );
    }
    @Test public void analyzeString_nullValue() {
        assertType(
            "fn:analyze-string((),'.*')",
            typeFactory.one(typeFactory.itemElement(
                Set.of(new QualifiedName("fn", "analyze-string-result"))))
        );
    }
    @Test public void analyzeString_missingPattern() {
        assertErrors("fn:analyze-string('x')");
        assertErrors("fn:analyze-string()");
    }
    @Test public void analyzeString_wrongTypes() {
        assertErrors("fn:analyze-string(1,'p')");
        assertErrors("fn:analyze-string('x',1)");
        assertErrors("fn:analyze-string('x','p',1)");
    }
}
