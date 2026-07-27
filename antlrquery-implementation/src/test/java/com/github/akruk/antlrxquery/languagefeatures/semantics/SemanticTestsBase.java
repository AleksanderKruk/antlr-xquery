package com.github.akruk.antlrxquery.languagefeatures.semantics;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.AntlrXqueryLexer;
import com.github.akruk.antlrxquery.AntlrXqueryParser;
import com.github.akruk.antlrxquery.AxisVisitor;
import com.github.akruk.antlrxquery.evaluator.values.factories.defaults.XQueryMemoizedValueFactory;
import com.github.akruk.antlrxquery.languageserver.DiagnosticMessageCreator;
import com.github.akruk.antlrxquery.semanticanalyzer.GrammarManager;
import com.github.akruk.antlrxquery.semanticanalyzer.ModuleManager;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.XQuerySemanticContextManager;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.SemanticFunctionSets;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.XQuerySemanticSymbolManager;
import com.github.akruk.antlrxquery.semanticanalyzer.visitors.AntlrQuerySemanticAnalyzer;
import com.github.akruk.antlrxquery.semanticanalyzer.visitors.CardinalityVisitor;
import com.github.akruk.antlrxquery.semanticanalyzer.visitors.ItemTypeVisitor;
import com.github.akruk.antlrxquery.semanticanalyzer.visitors.TypeVisitor;
import com.github.akruk.antlrxquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.MemoizedCardinalityFactory;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.MemoizedTypeFactory;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryNamedTypeSets;
import com.github.akruk.antlrxquery.typesystem.types.AntlrQuerySequenceType;

public class SemanticTestsBase {
    final protected AntlrQueryTypeFactory typeFactory = new MemoizedTypeFactory(new XQueryNamedTypeSets().all(), Map.of());

    record AnalysisResult(
        AntlrQuerySemanticAnalyzer analyzer,
        AntlrQuerySequenceType expressionType
        )
    {};

    protected AnalysisResult analyze(final String text) {
        final CharStream characters = CharStreams.fromString(text);
        final Lexer xqueryLexer = new AntlrXqueryLexer(characters);
        final CommonTokenStream xqueryTokens = new CommonTokenStream(xqueryLexer);
        final AntlrXqueryParser xqueryParser = new AntlrXqueryParser(xqueryTokens);
        xqueryParser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(
                Recognizer<?, ?> recognizer,
                Object offendingSymbol,
                int line,
                int charPositionInLine,
                String msg, RecognitionException e)
            {
                throw e;
            }
        });
        final ParseTree xqueryTree = xqueryParser.xquery();
        final var contextManager = new XQuerySemanticContextManager(typeFactory);
        final XQuerySemanticSymbolManager caller = new XQuerySemanticSymbolManager(typeFactory, contextManager, SemanticFunctionSets.ALL(typeFactory));
        final var memoizedFactory = new MemoizedCardinalityFactory();
        CardinalityVisitor cardinalityVisitor = new CardinalityVisitor(memoizedFactory);
        final AntlrQuerySemanticAnalyzer analyzer = new AntlrQuerySemanticAnalyzer(
                null,
                typeFactory,
                new XQueryMemoizedValueFactory(typeFactory),
                caller,
                null,
                new ModuleManager(Set.of()),
                new GrammarManager(Set.of()),
                typeFactory.anyNode(),
                "",
                Map.of(),
                new AxisVisitor(),
                memoizedFactory,
                cardinalityVisitor,
                new TypeVisitor(typeFactory, cardinalityVisitor, new ItemTypeVisitor(typeFactory))
                );
        final var lastVisitedType = analyzer.visit(xqueryTree);
        if (lastVisitedType == null) {
            return new AnalysisResult(analyzer, null);
        } else {
            return new AnalysisResult(analyzer, lastVisitedType.type);
        }
    }

    protected DiagnosticMessageCreator messageCreator = new DiagnosticMessageCreator();
    protected void assertNoErrors(final AnalysisResult analyzer) {
        boolean noErrors = analyzer.analyzer.getErrors().size() == 0;
        String concatenatedInNewlinesMessages = analyzer.analyzer.getErrors().stream()
            .map(e->messageCreator.create(e))
            .collect(Collectors.joining(System.lineSeparator()));
        assertTrue(noErrors, concatenatedInNewlinesMessages);
    }

    protected void assertErrors(final String xquery) {
        final var analysisResult = analyze(xquery);
        assertErrors(analysisResult);
    }

    protected void assertErrors(final AnalysisResult analyzer) {
        assertTrue(analyzer.analyzer.getErrors().size() != 0, "Found no erros");
    }

    protected void assertType(final AnalysisResult result, final AntlrQuerySequenceType expectedType) {
        assertNoErrors(result);
        assertEquals(result.expressionType, expectedType);
    }

    protected void assertType(final String xquery, final AntlrQuerySequenceType expectedType) {
        final var analysisResult = analyze(xquery);
        assertNoErrors(analysisResult);
        assertEquals(expectedType, analysisResult.expressionType);
    }

    protected void assertNoErrors(String query) {
        assertNoErrors(analyze(query));
    }

}
