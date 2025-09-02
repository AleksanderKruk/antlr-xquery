package com.github.akruk.antlrxquery.languageserver;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.*;

import com.github.akruk.antlrxquery.AntlrXqueryLexer;
import com.github.akruk.antlrxquery.AntlrXqueryParser;
import com.github.akruk.antlrxquery.evaluator.XQuery;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AntlrQueryLanguageServer implements LanguageServer, LanguageClientAware {
    public static void main(final String[] args)
    {
        System.err.println("[main] Starting BasicLanguageServer...");

        final AntlrQueryLanguageServer server = new AntlrQueryLanguageServer();

        final Launcher<LanguageClient> launcher = Launcher.createLauncher(
            server,
            LanguageClient.class,
            System.in,
            System.out);

        final LanguageClient client = launcher.getRemoteProxy();
        server.connect(client);

        System.err.println("[main] Launcher created. Listening...");
        launcher.startListening();
    }

    private LanguageClient client;
    private final BasicTextDocumentService textDocumentService = new BasicTextDocumentService();
    private final com.github.akruk.antlrxquery.languageserver.AntlrQueryLanguageServer.BasicTextDocumentService.BasicWorkspaceService workspaceService = new com.github.akruk.antlrxquery.languageserver.AntlrQueryLanguageServer.BasicTextDocumentService.BasicWorkspaceService();

    private static final List<String> tokenLegend = List.of("variable", "parameter", "function", "type");
    private static final int variableIndex = tokenLegend.indexOf("variable");
    private static final int parameterIndex = tokenLegend.indexOf("parameter");
    private static final int functionIndex = tokenLegend.indexOf("function");
    private static final int typeIndex = tokenLegend.indexOf("type");

    @Override
    public void connect(final LanguageClient client)
    {
        this.client = client;
        textDocumentService.setClient(client);
        workspaceService.setClient(client);
        System.err.println("[connect] LanguageClient connected");
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(final InitializeParams params)
    {
        System.err.println("[initialize] Server initialized");
        final ServerCapabilities capabilities = new ServerCapabilities();

        final TextDocumentSyncOptions syncOptions = new TextDocumentSyncOptions();
        syncOptions.setSave(new SaveOptions(true));
        syncOptions.setChange(TextDocumentSyncKind.Full);
        syncOptions.setOpenClose(true);
        capabilities.setTextDocumentSync(syncOptions);

        final SemanticTokensWithRegistrationOptions semanticTokensOptions = new SemanticTokensWithRegistrationOptions();
        final SemanticTokensLegend legend = new SemanticTokensLegend();
        legend.setTokenTypes(tokenLegend);
        legend.setTokenModifiers(List.of("declaration", "readonly", "static", "deprecated"));
        semanticTokensOptions.setLegend(legend);
        semanticTokensOptions.setFull(true);
        semanticTokensOptions.setRange(false);
        capabilities.setSemanticTokensProvider(semanticTokensOptions);

        return CompletableFuture.completedFuture(new InitializeResult(capabilities));
    }

    @Override
    public CompletableFuture<Object> shutdown()
    {
        System.err.println("[shutdown] Server shutting down");
        return CompletableFuture.completedFuture(new Object());
    }

    @Override
    public void exit()
    {
        System.err.println("[exit] Server exiting");
        System.exit(0);
    }

    @Override
    public TextDocumentService getTextDocumentService()
    {
        return textDocumentService;
    }

    @Override
    public WorkspaceService getWorkspaceService()
    {
        return workspaceService;
    }

    public static class BasicTextDocumentService implements TextDocumentService {
        private LanguageClient client;
        private final Map<String, ParseTree> parseTreeStore = new HashMap<>();

        public void setClient(final LanguageClient client)
        {
            this.client = client;
        }

        @Override
        public void didOpen(final DidOpenTextDocumentParams params)
        {
            final String uri = params.getTextDocument().getUri();
            final String text = params.getTextDocument().getText();
            System.err.println("[didOpen] " + uri);
            if (parseTreeStore.containsKey(uri)) {
                return;
            }
            parseAndAnalyze(uri, text);
        }

        @Override
        public void didChange(final DidChangeTextDocumentParams params)
        {
            final String uri = params.getTextDocument().getUri();
            if (params.getContentChanges().isEmpty()) {
                System.err.println("[didChange] No content changes for: " + uri);
                return;
            }
            final String text = params.getContentChanges().get(0).getText();
            System.err.println("[didChange] Document changed: " + uri + ", new length: " + text.length());
            parseAndAnalyze(uri, text);
        }

        @Override
        public void didClose(final DidCloseTextDocumentParams params)
        {
            final String uri = params.getTextDocument().getUri();
            parseTreeStore.remove(uri);
            System.err.println("[didClose] Document closed: " + uri);
        }

        @Override
        public void didSave(final DidSaveTextDocumentParams params)
        {
            final String uri = params.getTextDocument().getUri();
            System.err.println("[didSave] Document saved: " + uri);
            parseAndAnalyze(uri, params.getText());
        }

        private void parseAndAnalyze(final String uri, final String text)
        {
            System.err.println("[parseAndAnalyze] Starting for: " + uri);
            System.err.println("[parseAndAnalyze] Text length: " + text.length());

            try {
                final AntlrXqueryLexer lexer = new AntlrXqueryLexer(CharStreams.fromString(text));
                final CommonTokenStream tokens = new CommonTokenStream(lexer);
                final AntlrXqueryParser parser = new AntlrXqueryParser(tokens);

                // Custom syntax error listener
                final List<Diagnostic> diagnostics = new ArrayList<>();
                parser.removeErrorListeners();
                parser.addErrorListener(new BaseErrorListener() {
                    @Override
                    public void syntaxError(
                        final Recognizer<?, ?> recognizer,
                        final Object offendingSymbol,
                        final int line,
                        final int charPositionInLine,
                        final String msg,
                        final RecognitionException e)
                    {
                        System.err.println("[syntaxError] Line " + line + ":" + charPositionInLine + " " + msg);
                        final Diagnostic diagnostic = new Diagnostic(
                            new Range(
                                new Position(line - 1, charPositionInLine),
                                new Position(line - 1, charPositionInLine + 1)),
                            "Syntax error: " + msg,
                            DiagnosticSeverity.Error,
                            "antlr-xquery");
                        diagnostics.add(diagnostic);
                    }
                });

                final ParseTree tree = parser.xquery();
                parseTreeStore.put(uri, tree);

                // Skip semantic analysis if syntax errors exist
                if (!diagnostics.isEmpty()) {
                    System.err.println("[parseAndAnalyze] Skipping semantic analysis due to syntax errors");
                    if (client != null) {
                        client.publishDiagnostics(new PublishDiagnosticsParams(uri, diagnostics));
                    }
                    return;
                }

                final XQueryTypeFactory typeFactory = new XQueryMemoizedTypeFactory(new XQueryNamedTypeSets().all());
                final XQuerySemanticAnalyzer analyzer = new XQuerySemanticAnalyzer(
                    null,
                    new XQuerySemanticContextManager(),
                    typeFactory,
                    new XQueryMemoizedValueFactory(typeFactory),
                    new XQuerySemanticFunctionManager(typeFactory),
                    null);
                analyzer.visit(tree);

                final List<DiagnosticError> errors = analyzer.getErrors();
                final List<DiagnosticWarning> warnings = analyzer.getWarnings();
                System.err
                    .println("[parseAndAnalyze] Semantic Errors: " + errors.size() + ", Warnings: " + warnings.size());

                for (final var error : errors) {
                    diagnostics.add(new Diagnostic(
                        new Range(
                            new Position(error.startLine() - 1, error.charPositionInLine()),
                            new Position(error.endLine() - 1, error.endCharPositionInLine())),
                        error.message(),
                        DiagnosticSeverity.Error,
                        "antlr-xquery"));
                }

                for (final var warning : warnings) {
                    diagnostics.add(new Diagnostic(
                        new Range(
                            new Position(warning.startLine() - 1, warning.charPositionInLine()),
                            new Position(warning.endLine() - 1, warning.endCharPositionInLine())),
                        warning.message(),
                        DiagnosticSeverity.Warning,
                        "antlr-xquery"));
                }

                if (client != null) {
                    client.publishDiagnostics(new PublishDiagnosticsParams(uri, diagnostics));
                } else {
                    System.err.println("[parseAndAnalyze] LanguageClient is null. Cannot publish diagnostics.");
                }

            } catch (final Exception e) {
                System.err.println("[parseAndAnalyze] Exception: " + e.getClass().getName() + " - " + e.getMessage());
                for (final StackTraceElement el : e.getStackTrace()) {
                    System.err.println("    at " + el.toString());
                }
            }
        }

        private final AntlrXqueryLexer _lexer = new AntlrXqueryLexer(CharStreams.fromString(""));
        private final CommonTokenStream _tokens = new CommonTokenStream(_lexer);
        private final AntlrXqueryParser _parser = new AntlrXqueryParser(_tokens);

        @Override
        public CompletableFuture<SemanticTokens> semanticTokensFull(final SemanticTokensParams params) {
            final String uri = params.getTextDocument().getUri();
            final ParseTree tree = parseTreeStore.get(uri);

            if (tree == null) {
                System.err.println("[semanticTokensFull] No parse tree for URI: " + uri);
                return CompletableFuture.completedFuture(new SemanticTokens(List.of()));
            }

            record SemanticToken(int line, int charPos, int length, int typeIndex, int modifierBitmask) {}

            final List<SemanticToken> tokens = new ArrayList<>();

            final List<XQueryValue> typeValues = XQuery.evaluate(tree, "//(sequenceType|castTarget)", _parser).sequence;
            for (final XQueryValue val : typeValues) {
                final ParseTree node = val.node;
                if (!(node instanceof final ParserRuleContext ctx)) continue;
                final Token start = ctx.getStart();
                final Token stop = ctx.getStop();
                final int line = start.getLine() - 1;
                final int charPos = start.getCharPositionInLine();
                final int length = stop.getStopIndex() - start.getStartIndex() + 1;
                tokens.add(new SemanticToken(line, charPos, length, typeIndex, 0));
            }

            final List<XQueryValue> variableValues = XQuery.evaluate(tree, "//varRef", _parser).sequence;
            for (final XQueryValue val : variableValues) {
                final ParseTree node = val.node;
                if (!(node instanceof final ParserRuleContext ctx)) continue;
                final Token start = ctx.getStart();
                final Token stop = ctx.getStop();
                final int line = start.getLine() - 1;
                final int charPos = start.getCharPositionInLine();
                final int length = stop.getStopIndex() - start.getStartIndex() + 1;
                tokens.add(new SemanticToken(line, charPos, length, variableIndex, 0));
            }

            final List<XQueryValue> functionValues = XQuery.evaluate(tree, "//(functionName|namedFunctionRef)", _parser).sequence;
            for (final XQueryValue val : functionValues) {
                final ParseTree node = val.node;
                if (!(node instanceof final ParserRuleContext ctx)) continue;
                final Token start = ctx.getStart();
                final Token stop = ctx.getStop();
                final int line = start.getLine() - 1;
                final int charPos = start.getCharPositionInLine();
                final int length = stop.getStopIndex() - start.getStartIndex() + 1;
                tokens.add(new SemanticToken(line, charPos, length, functionIndex, 0));
            }

            tokens.sort(Comparator
                .comparingInt(SemanticToken::line)
                .thenComparingInt(SemanticToken::charPos));

            final List<Integer> data = new ArrayList<>();
            int lastLine = 0;
            int lastChar = 0;

            for (final SemanticToken token : tokens) {
                final int deltaLine = token.line() - lastLine;
                final int deltaChar = (deltaLine == 0) ? (token.charPos() - lastChar) : token.charPos();

                data.add(deltaLine);
                data.add(deltaChar);
                data.add(token.length());
                data.add(token.typeIndex());
                data.add(token.modifierBitmask());

                lastLine = token.line();
                lastChar = token.charPos();
            }

            return CompletableFuture.completedFuture(new SemanticTokens(data));
        }






        public static class BasicWorkspaceService implements WorkspaceService {
            private LanguageClient client;

            public void setClient(final LanguageClient client)
            {
                this.client = client;
            }

            @Override
            public void didChangeConfiguration(final DidChangeConfigurationParams params)
            {
                System.err.println("[didChangeConfiguration]");
                showMessage("Configuration changed");
            }

            @Override
            public void didChangeWatchedFiles(final DidChangeWatchedFilesParams params)
            {
                System.err.println("[didChangeWatchedFiles]");
                showMessage("Watched files changed");
            }

            private void showMessage(final String message)
            {
                if (client != null) {
                    final MessageParams msg = new MessageParams(MessageType.Log, message);
                    client.showMessage(msg);
                }
            }
        }
    }
}
