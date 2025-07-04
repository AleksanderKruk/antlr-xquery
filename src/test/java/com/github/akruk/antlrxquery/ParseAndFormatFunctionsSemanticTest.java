package com.github.akruk.antlrxquery;

import static org.junit.jupiter.api.Assertions.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.contextmanagement.semanticcontext.baseimplementation.XQueryBaseSemanticContextManager;
import com.github.akruk.antlrxquery.semanticanalyzer.XQuerySemanticAnalyzer;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.defaults.XQuerySemanticFunctionManager;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryEnumTypeFactory;
import com.github.akruk.antlrxquery.values.factories.defaults.XQueryMemoizedValueFactory;

public class ParseAndFormatFunctionsSemanticTest {
    private XQueryEnumTypeFactory typeFactory;
    private XQuerySemanticFunctionManager fnManager;
    private XQuerySemanticAnalyzer analyzer;

    record Result(XQuerySemanticAnalyzer analyzer, XQuerySequenceType type) {}

    @BeforeEach
    void init() {
        typeFactory = new XQueryEnumTypeFactory();
        fnManager   = new XQuerySemanticFunctionManager(typeFactory);
    }

    private Result analyze(String xq) {
        CharStream chars  = CharStreams.fromString(xq);
        var lexer         = new AntlrXqueryLexer(chars);
        var tokens        = new CommonTokenStream(lexer);
        var parser        = new AntlrXqueryParser(tokens);
        var tree          = parser.xquery();
        analyzer = new XQuerySemanticAnalyzer(
            parser,
            new XQueryBaseSemanticContextManager(),
            typeFactory,
            new XQueryMemoizedValueFactory(),
            fnManager);
        var t = analyzer.visit(tree);
        return new Result(analyzer, t);
    }

    private void assertNoErr(Result r) {
        assertTrue(r.analyzer.getErrors().isEmpty(),
            "Expected no errors, got: " + r.analyzer.getErrors());
    }

    private void assertErr(String xq) {
        var r = analyze(xq);
        assertFalse(r.analyzer.getErrors().isEmpty(),
            "Expected errors for: " + xq);
    }

    private void assertType(String xq, XQuerySequenceType expected) {
        var r = analyze(xq);
        assertNoErr(r);
        assertEquals(expected, r.type);
    }

    // fn:parse-integer($value as xs:string?, $radix as xs:integer? := 10) as xs:integer?
    @Test void parseInteger_validDefaultRadix() {
        assertType("fn:parse-integer('123')",
            typeFactory.zeroOrOne(typeFactory.itemNumber()));
    }
    @Test void parseInteger_validExplicitRadix() {
        assertType("fn:parse-integer('FF', 16)",
            typeFactory.zeroOrOne(typeFactory.itemNumber()));
    }
    @Test void parseInteger_missingValue() {
        assertErr("fn:parse-integer()");
    }
    @Test void parseInteger_badValueType() {
        assertErr("fn:parse-integer(123, 10)");
    }
    @Test void parseInteger_badRadixType() {
        assertErr("fn:parse-integer('10', '2')");
    }
    @Test void parseInteger_tooManyArgs() {
        assertErr("fn:parse-integer('10', 2, 3)");
    }

    // fn:format-integer($value as xs:integer?, $picture as xs:string, $language as xs:string? := ()) as xs:string
    @Test void formatInteger_minimal() {
        assertType("fn:format-integer(42, '000')",
            typeFactory.one(typeFactory.itemString()));
    }
    @Test void formatInteger_withLanguage() {
        assertType("fn:format-integer(7, '#', 'en')",
            typeFactory.one(typeFactory.itemString()));
    }
    @Test void formatInteger_missingValueOrPicture() {
        assertErr("fn:format-integer()");
        assertErr("fn:format-integer(1)");
    }
    @Test void formatInteger_badValueType() {
        assertErr("fn:format-integer('x', '0')");
    }
    @Test void formatInteger_badPictureType() {
        assertErr("fn:format-integer(1, 2)");
    }
    @Test void formatInteger_badLanguageType() {
        assertErr("fn:format-integer(1, '#', 123)");
    }

    // fn:format-number($value as xs:numeric?, $picture as xs:string, $options as (xs:string|map(*))? := ()) as xs:string
    @Test void formatNumber_minimal() {
        assertType("fn:format-number(123.45, '#0.00')",
            typeFactory.one(typeFactory.itemString()));
    }
    @Test void formatNumber_withOptionsAsString() {
        assertType("fn:format-number(1.2, '0.0', 'decimal-separator=.')",
            typeFactory.one(typeFactory.itemString()));
    }
    @Test void formatNumber_withOptionsAsMap() {
        assertType("fn:format-number(3.14, '#.##', map{ 'decimal-separator' : '.' })",
            typeFactory.one(typeFactory.itemString()));
    }
    @Test void formatNumber_missingArgs() {
        assertErr("fn:format-number()");
        assertErr("fn:format-number(1.0)");
    }
    @Test void formatNumber_badValueType() {
        assertErr("fn:format-number('x', '#')");
    }
    @Test void formatNumber_badPictureType() {
        assertErr("fn:format-number(1.0, 0)");
    }
    @Test void formatNumber_badOptionsType() {
        assertErr("fn:format-number(1.0, '#', 123)");
    }
}

