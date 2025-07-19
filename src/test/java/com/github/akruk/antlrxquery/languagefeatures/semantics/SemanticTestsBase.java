package com.github.akruk.antlrxquery.languagefeatures.semantics;
import static org.junit.jupiter.api.Assertions.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.AntlrXqueryLexer;
import com.github.akruk.antlrxquery.AntlrXqueryParser;
import com.github.akruk.antlrxquery.semanticanalyzer.XQuerySemanticAnalyzer;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.XQuerySemanticContextManager;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.defaults.XQuerySemanticFunctionManager;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryEnumTypeFactory;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryNamedTypeSets;
import com.github.akruk.antlrxquery.values.factories.defaults.XQueryMemoizedValueFactory;

public class SemanticTestsBase {
    final protected XQueryTypeFactory typeFactory = new XQueryEnumTypeFactory(new XQueryNamedTypeSets().all());

    record AnalysisResult(XQuerySemanticAnalyzer analyzer, XQuerySequenceType expressionType) {
    };

    protected AnalysisResult analyze(final String text) {
        final CharStream characters = CharStreams.fromString(text);
        final Lexer xqueryLexer = new AntlrXqueryLexer(characters);
        final CommonTokenStream xqueryTokens = new CommonTokenStream(xqueryLexer);
        final AntlrXqueryParser xqueryParser = new AntlrXqueryParser(xqueryTokens);
        final ParseTree xqueryTree = xqueryParser.xquery();
        final XQuerySemanticFunctionManager caller = new XQuerySemanticFunctionManager(typeFactory);
        final XQuerySemanticAnalyzer analyzer = new XQuerySemanticAnalyzer(
                xqueryParser,
                new XQuerySemanticContextManager(),
                typeFactory,
                new XQueryMemoizedValueFactory(),
                caller);
        final var lastVisitedType = analyzer.visit(xqueryTree);
        return new AnalysisResult(analyzer, lastVisitedType);
    }

    protected void assertNoErrors(final AnalysisResult analyzer) {
        assertTrue(analyzer.analyzer.getErrors().size() == 0, String.join(System.lineSeparator(), analyzer.analyzer.getErrors()));
    }

    protected void assertErrors(final String xquery) {
        final var analysisResult = analyze(xquery);
        assertErrors(analysisResult);
    }

    protected void assertErrors(final AnalysisResult analyzer) {
        assertTrue(analyzer.analyzer.getErrors().size() != 0);
    }

    protected void assertType(final AnalysisResult result, final XQuerySequenceType expectedType) {
        assertNoErrors(result);
        assertTrue(result.expressionType.equals(expectedType));
    }

    protected void assertType(final String xquery, final XQuerySequenceType expectedType) {
        final var analysisResult = analyze(xquery);
        assertNoErrors(analysisResult);
        assertEquals(expectedType, analysisResult.expressionType);
    }



    protected void assertNoErrors(String query) {
        assertNoErrors(analyze(query));
    }

}
