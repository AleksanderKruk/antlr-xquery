package com.github.akruk.antlrxquery.semanticfunctiontests;
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
}
