package com.github.akruk.antlrxquery.languageserver;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensParams;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;

import com.github.akruk.antlrxquery.AntlrXqueryLexer;
import com.github.akruk.antlrxquery.AntlrXqueryParser;
import com.github.akruk.antlrxquery.evaluator.XQuery;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.factories.defaults.XQueryMemoizedValueFactory;
import com.github.akruk.antlrxquery.languageserver.ParseTreeTraverser.HoverKind;
import com.github.akruk.antlrxquery.semanticanalyzer.DiagnosticError;
import com.github.akruk.antlrxquery.semanticanalyzer.DiagnosticWarning;
import com.github.akruk.antlrxquery.semanticanalyzer.XQuerySemanticAnalyzer;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.XQuerySemanticContextManager;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.defaults.XQuerySemanticFunctionManager;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryMemoizedTypeFactory;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryNamedTypeSets;

public class BasicTextDocumentService implements TextDocumentService {
    private LanguageClient client;
    private final Map<String, List<Token>> tokenStore = new HashMap<>();
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
            final ListTokenStream tokenStream = new ListTokenStream(lexer);
            final AntlrXqueryParser parser = new AntlrXqueryParser(tokenStream);

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
            tokenStore.put(uri, tokenStream.tokens);

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

        class SemanticToken {
            int line;
            int charPos;
            int length;
            int typeIndex;
            int modifierBitmask;

            SemanticToken(final int line, final int charPos, final int length, final int typeIndex, final int modifierBitmask) {
                this.line = line;
                this.charPos = charPos;
                this.length = length;
                this.typeIndex = typeIndex;
                this.modifierBitmask = modifierBitmask;
            }
        }

        final List<SemanticToken> tokens = new ArrayList<>();

        final Map<String, Integer> tokenTypes = Map.of(
            "type", 3,
            "variable", 0,
            "number", 1,
            "string", 2,
            "function", 4
        );

        final Map<XQueryValue, String> sources = Map.ofEntries(
            Map.entry(XQuery.evaluate(tree, """
                //(typeName
                    | anyItemTest
                    | typeName
                    | kindTest
                    | functionType
                    | mapType
                    | arrayType
                    | recordType
                    | enumerationType
                    | choiceItemType
                    | emptySequence)
                """, _parser), "type"),
            Map.entry(XQuery.evaluate(tree, "//varRef", _parser), "variable"),
            Map.entry(XQuery.evaluate(tree, "//numericLiteral", _parser), "number"),
            Map.entry(XQuery.evaluate(tree, "//STRING", _parser), "string"),
            Map.entry(XQuery.evaluate(tree, "//functionName", _parser), "function")
        );

        for (final var entry : sources.entrySet()) {
            final XQueryValue valueSet = entry.getKey();
            final String typeName = entry.getValue();
            final int typeIndex = tokenTypes.get(typeName);

            for (final XQueryValue val : valueSet.sequence) {
                final ParseTree node = val.node;
                if (!(node instanceof final ParserRuleContext ctx)) continue;
                final Token start = ctx.getStart();
                final int line = start.getLine() - 1;
                final int charPos = start.getCharPositionInLine();
                final int length = start.getText().length();

                tokens.add(new SemanticToken(line, charPos, length, typeIndex, 0));
            }
        }

        tokens.sort(Comparator
            .comparingInt((final SemanticToken t) -> t.line)
            .thenComparingInt(t -> t.charPos));

        final List<Integer> data = new ArrayList<>();
        int lastLine = 0;
        int lastChar = 0;

        for (final SemanticToken token : tokens) {
            final int deltaLine = token.line - lastLine;
            final int deltaChar = (deltaLine == 0) ? (token.charPos - lastChar) : token.charPos;

            data.add(deltaLine);
            data.add(deltaChar);
            data.add(token.length);
            data.add(token.typeIndex);
            data.add(token.modifierBitmask);

            lastLine = token.line;
            lastChar = token.charPos;
        }

        return CompletableFuture.completedFuture(new SemanticTokens(data));
    }


    @Override
    public CompletableFuture<Hover> hover(HoverParams params) {
        String uri = params.getTextDocument().getUri();
        Position position = params.getPosition();

        ParseTree tree = parseTreeStore.get(uri);
        List<Token> tokens = tokenStore.get(uri);
        if (tree == null) {
            return CompletableFuture.completedFuture(null);
        }

        HoverKind wordAtPosition = ParseTreeTraverser.findWordAtPosition(tree, position.getLine(), position.getCharacter());
        // if (wordAtPosition != null) {
        //     // Generate hover content based on the word/symbol
        //     String hoverMessage = generateHoverMessage(wordAtPosition);

        //     // Create a range that highlights the word/symbol
        //     Range hoverRange = findRangeOfWord(text, wordAtPosition, position.getLine());

        //     // Construct the hover response
        //     Hover hover = new Hover();
        //     hover.setContents(new MarkupContent("markdown", hoverMessage));
        //     hover.setRange(hoverRange);
        //     return CompletableFuture.completedFuture(hover);
        // }

        return CompletableFuture.completedFuture(null);
    }

}
