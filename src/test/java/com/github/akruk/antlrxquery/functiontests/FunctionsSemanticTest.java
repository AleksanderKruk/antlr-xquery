package com.github.akruk.antlrxquery.functiontests;
import static org.junit.jupiter.api.Assertions.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.AntlrXqueryLexer;
import com.github.akruk.antlrxquery.AntlrXqueryParser;
import com.github.akruk.antlrxquery.contextmanagement.semanticcontext.baseimplementation.XQueryBaseSemanticContextManager;
import com.github.akruk.antlrxquery.semanticanalyzer.XQuerySemanticAnalyzer;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.defaults.XQuerySemanticFunctionManager;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryEnumTypeFactory;
import com.github.akruk.antlrxquery.values.factories.defaults.XQueryMemoizedValueFactory;

public class FunctionsSemanticTest {
    protected XQueryEnumTypeFactory typeFactory;
    protected XQuerySemanticFunctionManager fnManager;
    protected XQuerySemanticAnalyzer analyzer;

    public record AnalysisResult(XQuerySemanticAnalyzer analyzer, XQuerySequenceType type) {}

    @BeforeEach
    protected void init() {
        typeFactory = new XQueryEnumTypeFactory();
        fnManager   = new XQuerySemanticFunctionManager(typeFactory);
    }

    protected AnalysisResult analyze(String xquery) {
        CharStream chars = CharStreams.fromString(xquery);
        var lexer = new AntlrXqueryLexer(chars);
        var tokens = new CommonTokenStream(lexer);
        var parser = new AntlrXqueryParser(tokens);
        var tree = parser.xquery();
        analyzer = new XQuerySemanticAnalyzer(
            parser,
            new XQueryBaseSemanticContextManager(),
            typeFactory,
            new XQueryMemoizedValueFactory(),
            fnManager);
        var resultType = analyzer.visit(tree);
        return new AnalysisResult(analyzer, resultType);
    }

    protected void assertNoErrors(String query) {
        assertNoErrors(analyze(query));
    }

    protected void assertNoErrors(AnalysisResult r) {
        assertTrue(r.analyzer.getErrors().isEmpty(),
            "Expected no errors, got: " + r.analyzer.getErrors());
    }

    protected void assertErrors(String xq) {
        var r = analyze(xq);
        assertFalse(r.analyzer.getErrors().isEmpty(),
            "Expected errors for [" + xq + "]");
    }

    protected void assertType(String xq, XQuerySequenceType expected) {
        var r = analyze(xq);
        assertNoErrors(r);
        assertEquals(expected, r.type);
    }


    // fn:abs($value as xs:numeric?) as xs:numeric?
    private void assertBoolean(String xq) {
        var r = analyze(xq);
        assertNoErrors(r);
        assertEquals(typeFactory.one(typeFactory.itemBoolean()), r.type);
    }

    private void assertString(String xq) {
        var r = analyze(xq);
        assertNoErrors(r);
        assertEquals(typeFactory.one(typeFactory.itemString()), r.type);
    }

    // fn:contains($value as xs:string?, $substring as xs:string?, $collation as xs:string? := default) as xs:boolean

    @Test void contains_validPositional() {
        assertBoolean("fn:contains('abc','b')");
    }
    @Test void contains_namedArgs() {
        assertBoolean("fn:contains(value := 'x', substring := 'y')");
    }
    @Test void contains_withCollationPos() {
        assertBoolean("fn:contains('a','b','uci')");
    }
    @Test void contains_defaultCollation() {
        assertBoolean("fn:contains('','')");
    }
    @Test void contains_tooFew() {
        assertErrors("fn:contains('a')");
        assertErrors("fn:contains()");
    }
    @Test void contains_tooMany() {
        assertErrors("fn:contains('a','b','c','d')");
    }
    @Test void contains_badTypes() {
        assertErrors("fn:contains(1,'b')");
        assertErrors("fn:contains('a',true())");
        assertErrors("fn:contains('a','b', 5)");
    }

    // fn:starts-with(... same signature ...) as xs:boolean

    @Test void startsWith_validPositional() {
        assertBoolean("fn:starts-with('hello','he')");
    }
    @Test void startsWith_namedAndDefault() {
        assertBoolean("fn:starts-with(substring := 'lo', value := 'hello')");
    }
    @Test void startsWith_missing() {
        assertErrors("fn:starts-with('x')");
        assertErrors("fn:starts-with()");
    }
    @Test void startsWith_badTypes() {
        assertErrors("fn:starts-with(1,'x')");
        assertErrors("fn:starts-with('x',2)");
        assertErrors("fn:starts-with('x','y',true())");
    }

    // fn:ends-with(... same signature ...) as xs:boolean

    @Test void endsWith_valid() {
        assertBoolean("fn:ends-with('test','st')");
    }
    @Test void endsWith_named() {
        assertBoolean("fn:ends-with(value := 'abc', substring := 'bc')");
    }
    @Test void endsWith_missingOrExtra() {
        assertErrors("fn:ends-with('a')");
        assertErrors("fn:ends-with()");
        assertErrors("fn:ends-with('a','b','c')");
    }
    @Test void endsWith_invalidTypes() {
        assertErrors("fn:ends-with(1, 'x')");
        assertErrors("fn:ends-with('x', 1)");
    }

    // fn:substring-before($value as xs:string?, $substring as xs:string?, $collation as xs:string? := default) as xs:string

    @Test void substringBefore_valid() {
        assertString("fn:substring-before('abcd','bc')");
    }
    @Test void substringBefore_named() {
        assertString("fn:substring-before(substring := 'z', value := 'xyz')");
    }
    @Test void substringBefore_withDefault() {
        assertString("fn:substring-before('aaa','a')");
    }
    @Test void substringBefore_argCounts() {
        assertErrors("fn:substring-before('x')");
        assertErrors("fn:substring-before()");
        assertErrors("fn:substring-before('x','y','z','w')");
    }
    @Test void substringBefore_badTypes() {
        assertErrors("fn:substring-before(1,'a')");
        assertErrors("fn:substring-before('a',true())");
        assertErrors("fn:substring-before('a','b', 0)");
    }

    // fn:substring-after(... same signature ...) as xs:string

    @Test void substringAfter_valid() {
        assertString("fn:substring-after('abcd','cd')");
    }
    @Test void substringAfter_named() {
        assertString("fn:substring-after(value := 'hello', substring := 'll')");
    }
    @Test void substringAfter_defaultCollation() {
        assertString("fn:substring-after('foo','o')");
    }
    @Test void substringAfter_errors() {
        assertErrors("fn:substring-after('x')");
        assertErrors("fn:substring-after()");
        assertErrors("fn:substring-after('x','y',1)");
        assertErrors("fn:substring-after('x', 1)");
    }

}
