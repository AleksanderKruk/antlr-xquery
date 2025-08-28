package com.github.akruk.antlrxquery.languageserver;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;

import com.github.akruk.antlrxquery.AntlrXqueryLexer;
import com.github.akruk.antlrxquery.AntlrXqueryParser;
import com.github.akruk.antlrxquery.evaluator.values.factories.defaults.XQueryMemoizedValueFactory;
import com.github.akruk.antlrxquery.semanticanalyzer.DiagnosticError;
import com.github.akruk.antlrxquery.semanticanalyzer.DiagnosticWarning;
import com.github.akruk.antlrxquery.semanticanalyzer.XQuerySemanticAnalyzer;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.XQuerySemanticContextManager;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.defaults.XQuerySemanticFunctionManager;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryMemoizedTypeFactory;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryNamedTypeSets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AntlrQueryLanguageServer {
    public static class AntlrQueryTextDocumentService implements TextDocumentService {
        private final LanguageClient client;
        private final Map<String, String> documentStore = new HashMap<>();
        private final Map<String, ParseTree> parseTreeStore = new HashMap<>();

        public AntlrQueryTextDocumentService(LanguageClient client) {
            this.client = client;
        }

        @Override
        public void didOpen(DidOpenTextDocumentParams params)
        {
            String uri = params.getTextDocument().getUri();
            String text = params.getTextDocument().getText();
            documentStore.put(uri, text);
            parseAndAnalyze(uri, text);
            log("Document opened: " + uri);
        }

        @Override
        public void didChange(DidChangeTextDocumentParams params)
        {
            String uri = params.getTextDocument().getUri();
            String text = params.getContentChanges().get(0).getText(); // Assuming full document update
            documentStore.put(uri, text);
            parseAndAnalyze(uri, text);
            log(String.format("Document changed: %s", uri));
        }

        @Override
        public void didClose(DidCloseTextDocumentParams params)
        {
            String uri = params.getTextDocument().getUri();
            documentStore.remove(uri);
            parseTreeStore.remove(uri);
            log("Document closed: " + uri);
        }

        @Override
        public void didSave(DidSaveTextDocumentParams params)
        {
            String uri = params.getTextDocument().getUri();
            String text = documentStore.get(uri);
            if (text != null) {
                parseAndAnalyze(uri, text);
            }
            log("Document saved: " + uri);
        }

        private void parseAndAnalyze(String uri, String text)
        {
            // Parse the document using ANTLR
            AntlrXqueryLexer lexer = new AntlrXqueryLexer(CharStreams.fromString(text));
            AntlrXqueryParser parser = new AntlrXqueryParser(new CommonTokenStream(lexer));
            ParseTree tree = parser.xquery(); // This depends on your grammar
            parseTreeStore.put(uri, tree);

            final XQueryTypeFactory typeFactory = new XQueryMemoizedTypeFactory(new XQueryNamedTypeSets().all());
            final XQuerySemanticAnalyzer analyzer = new XQuerySemanticAnalyzer(
                null,
                new XQuerySemanticContextManager(),
                typeFactory,
                new XQueryMemoizedValueFactory(typeFactory),
                new XQuerySemanticFunctionManager(typeFactory),
                null);
            analyzer.visit(tree);

            List<DiagnosticError> errors = analyzer.getErrors();
            List<DiagnosticWarning> warnings = analyzer.getWarnings();
            List<Diagnostic> diagnostics = new ArrayList<>(errors.size() + warnings.size());
            for (var error :errors) {
                Diagnostic diagnostic = new Diagnostic();
                diagnostic.setSeverity(DiagnosticSeverity.Error);
                diagnostic.setMessage(error.message());
                diagnostic.setRange(new Range(
                    new Position(error.startLine() - 1, error.charPositionInLine()),
                    new Position(error.endLine() - 1, error.endCharPositionInLine())
                ));
                diagnostics.add(diagnostic);
            }
            for  (var warning :warnings) {
                Diagnostic diagnostic = new Diagnostic();
                diagnostic.setSeverity(DiagnosticSeverity.Warning);
                diagnostic.setMessage(warning.message());
                diagnostic.setRange(new Range(
                    new Position(warning.startLine() - 1, warning.charPositionInLine()),
                    new Position(warning.endLine() - 1, warning.endCharPositionInLine())
                ));
                diagnostics.add(diagnostic);
            }

            client.publishDiagnostics(new PublishDiagnosticsParams(uri, diagnostics));
        }

        private void log(String message)
        {
            System.out.println(message); // Or use a proper logging framework
        }
    }

}
