package com.github.akruk.antlrquery.languagefeatures.semantics;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver;
import com.github.akruk.antlrquery.semanticanalyzer.*;
import com.github.akruk.antlrquery.semanticanalyzer.visitors.*;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrquery.AntlrQueryLexer;
import com.github.akruk.antlrquery.AntlrQueryParser;
import com.github.akruk.antlrquery.AxisVisitor;
import com.github.akruk.antlrquery.evaluator.values.factories.defaults.AntlrQueryMemoizedValueFactory;
import com.github.akruk.antlrquery.languageserver.DiagnosticMessageCreator;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.AntlrQuerySemanticContextManager;
import com.github.akruk.antlrquery.semanticanalyzer.semanticfunctioncaller.SemanticFunctionSets;
import com.github.akruk.antlrquery.semanticanalyzer.semanticfunctioncaller.SemanticSymbolManager;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.factories.defaults.MemoizedCardinalityFactory;
import com.github.akruk.antlrquery.typesystem.factories.defaults.MemoizedTypeFactory;
import com.github.akruk.antlrquery.typesystem.factories.defaults.AntlrQueryNamedTypeSets;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;

public class SemanticTestsBase {
    final protected AntlrQueryTypeFactory typeFactory = new MemoizedTypeFactory(new AntlrQueryNamedTypeSets().all(), Map.of());

    protected record AnalysisResult(
        AntlrQuerySemanticAnalyzer analyzer,
        AntlrQuerySequenceType expressionType
        )
    {}

    protected AnalysisResult analyze(final String text) {
        final CharStream characters = CharStreams.fromString(text);
        final Lexer xqueryLexer = new AntlrQueryLexer(characters);
        final CommonTokenStream xqueryTokens = new CommonTokenStream(xqueryLexer);
        final AntlrQueryParser antlrQueryParser = new AntlrQueryParser(xqueryTokens);
        antlrQueryParser.addErrorListener(new BaseErrorListener() {
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
        final ParseTree xqueryTree = antlrQueryParser.xquery();
        final var contextManager = new AntlrQuerySemanticContextManager(typeFactory);
        final SemanticSymbolManager.ProtoSemanticSymbolManager caller
                = new SemanticSymbolManager.ProtoSemanticSymbolManager(
                        typeFactory, contextManager, new SemanticFunctionSets(typeFactory).ALL());
        final var memoizedFactory = new MemoizedCardinalityFactory();
        final NumericRangeVisitor numericRangeVisitor = new NumericRangeVisitor();
        final CardinalityVisitor cardinalityVisitor = new CardinalityVisitor(memoizedFactory);
        final ItemTypeVisitor itemTypeVisitor = new ItemTypeVisitor(cardinalityVisitor, numericRangeVisitor, typeFactory);
        final TypeVisitor typeVisitor = new TypeVisitor(typeFactory, cardinalityVisitor, itemTypeVisitor);
        final AntlrQuerySemanticAnalyzer analyzer = new AntlrQuerySemanticAnalyzer(
                antlrQueryParser,
                typeFactory,
                new AntlrQueryMemoizedValueFactory(typeFactory),
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
                typeVisitor,
                itemTypeVisitor,
        new NamespaceResolver("fn", "", "", "", "")
                );
        final var lastVisitedType = analyzer.visit(xqueryTree);
        if (lastVisitedType == null) {
            return new AnalysisResult(analyzer, null);
        } else {
            return new AnalysisResult(analyzer, lastVisitedType.type);
        }
    }

    protected final DiagnosticMessageCreator messageCreator = new DiagnosticMessageCreator();
    protected void assertNoErrors(final AnalysisResult analyzer) {
        boolean noErrors = analyzer.analyzer.getErrors().isEmpty();
        String concatenatedInNewlinesMessages = analyzer.analyzer.getErrors().stream()
            .map(messageCreator::create)
            .collect(Collectors.joining(System.lineSeparator()));
        assertTrue(noErrors, concatenatedInNewlinesMessages);
    }

    protected void assertErrors(final String xquery) {
        final var analysisResult = analyze(xquery);
        assertErrors(analysisResult);
    }

    protected void assertErrors(final AnalysisResult analyzer) {
        assertFalse(analyzer.analyzer.getErrors().isEmpty(), "Found no errors");
    }

    protected void assertDiagnostics(
            final String xquery,
            final List<ErrorType> errors,
            final List<WarningType> warnings)
    {
        final var analysisResult = analyze(xquery);
        assertEquals(errors, analysisResult.analyzer.getErrors().stream().map(DiagnosticError::type).toList());
        assertEquals(warnings, analysisResult.analyzer.getWarnings().stream().map(DiagnosticWarning::type).toList());
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
