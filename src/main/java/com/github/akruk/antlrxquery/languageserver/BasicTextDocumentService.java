package com.github.akruk.antlrxquery.languageserver;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensParams;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;

import com.github.akruk.antlrxquery.AntlrXqueryLexer;
import com.github.akruk.antlrxquery.AntlrXqueryParser;
import com.github.akruk.antlrxquery.AntlrXqueryParser.NamedFunctionRefContext;
import com.github.akruk.antlrxquery.evaluator.XQuery;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.factories.defaults.XQueryMemoizedValueFactory;
import com.github.akruk.antlrxquery.languageserver.HoverLogic.HoverInfo;
import com.github.akruk.antlrxquery.languageserver.HoverLogic.HoverKind;
import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver;
import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver.ResolvedName;
import com.github.akruk.antlrxquery.semanticanalyzer.DiagnosticError;
import com.github.akruk.antlrxquery.semanticanalyzer.DiagnosticWarning;
import com.github.akruk.antlrxquery.semanticanalyzer.XQuerySemanticAnalyzer;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.XQuerySemanticContextManager;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.defaults.XQuerySemanticFunctionManager;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.defaults.XQuerySemanticFunctionManager.ArgumentSpecification;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.defaults.XQuerySemanticFunctionManager.FunctionSpecification;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryMemoizedTypeFactory;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryNamedTypeSets;

public class BasicTextDocumentService implements TextDocumentService {
    private LanguageClient client;
    private final Map<String, List<Token>> tokenStore = new HashMap<>();
    private final Map<String, ParseTree> parseTreeStore = new HashMap<>();
    private final Map<String, XQuerySemanticAnalyzer> semanticAnalyzers = new HashMap<>();
    private final Map<String, List<ParserRuleContext>> functionCalls = new HashMap<>();
    private final Map<String, List<ParserRuleContext>> types = new HashMap<>();
    private final Map<String, List<ParserRuleContext>> variables = new HashMap<>();


    private final int variableIndex;
    private final int parameterIndex;
    private final int functionIndex;
    private final int typeIndex;

    BasicTextDocumentService(List<String> tokenLegend)
    {
        variableIndex = tokenLegend.indexOf("variable");
        parameterIndex = tokenLegend.indexOf("parameter");
        functionIndex = tokenLegend.indexOf("function");
        typeIndex = tokenLegend.indexOf("type");
    }

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
            final AntlrXqueryLexerSavingTokens lexer = new AntlrXqueryLexerSavingTokens(CharStreams.fromString(text));
            final CommonTokenStream tokenStream = new CommonTokenStream(lexer);
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
            tokenStore.put(uri, lexer.tokens);

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
            semanticAnalyzers.put(uri, analyzer);

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
        types.put(uri, typeValues.stream().map(v->(ParserRuleContext) v.node).toList());


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
        variables.put(uri, variableValues.stream().map(v->(ParserRuleContext) v.node).toList());

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
        functionCalls.put(uri, functionValues.stream().map(v->(ParserRuleContext) v.node).toList());

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


    @Override
    public CompletableFuture<Hover> hover(HoverParams params) {
        String uri = params.getTextDocument().getUri();
        Position position = params.getPosition();

        ParseTree tree = parseTreeStore.get(uri);

        if (tree == null) {
            return CompletableFuture.completedFuture(null);
        }

        List<ParserRuleContext> calls = functionCalls.get(uri);
        if (calls.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        int low = 0;
        int high = calls.size() - 1;
        ParserRuleContext foundCtx = null;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            ParserRuleContext candidateCtx = calls.get(mid);

            if (isPositionInsideContext(position, candidateCtx)) {
                foundCtx = candidateCtx;
                break;
            }

            if (position.getLine() + 1 < candidateCtx.getStart().getLine() ||
               (position.getLine() + 1 == candidateCtx.getStart().getLine() &&
                position.getCharacter() < candidateCtx.getStart().getCharPositionInLine()))
            {
                high = mid - 1;
            }
            else {
                low = mid + 1;
            }
        }

        if (foundCtx != null) {
            String hoverText = "Znaleziono wywołanie: `" + foundCtx.getText() + "`";

            MarkupContent content = new MarkupContent(MarkupKind.MARKDOWN, hoverText);
            Hover hover = new Hover(content);

            hover.setRange(getContextRange(foundCtx));

            return CompletableFuture.completedFuture(hover);
        }

        return CompletableFuture.completedFuture(null);
    }

    private boolean isPositionInsideContext(Position position, ParserRuleContext ctx) {
        Token startToken = ctx.getStart();
        Token stopToken = ctx.getStop();

        int cursorLine = position.getLine() + 1;
        int cursorChar = position.getCharacter();

        if (cursorLine < startToken.getLine()) return false;
        if (cursorLine > stopToken.getLine()) return false;

        if (cursorLine == startToken.getLine() && cursorChar < startToken.getCharPositionInLine()) {
            return false;
        }

        int stopTokenEndChar = stopToken.getCharPositionInLine() + stopToken.getText().length();
        if (cursorLine == stopToken.getLine() && cursorChar > stopTokenEndChar) {
            return false;
        }

        return true;
    }

    private Range getContextRange(ParserRuleContext ctx) {
        Token startToken = ctx.getStart();
        Token stopToken = ctx.getStop();

        Position start = new Position(startToken.getLine() - 1, startToken.getCharPositionInLine());
        Position end = new Position(stopToken.getLine() - 1, stopToken.getCharPositionInLine() + stopToken.getText().length());

        return new Range(start, end);
    }




    // @Override
    // public CompletableFuture<Hover> hover(HoverParams params) {
    //     String uri = params.getTextDocument().getUri();
    //     Position position = params.getPosition();

    //     ParseTree tree = parseTreeStore.get(uri);
    //     List<Token> tokens = tokenStore.get(uri);
    //     if (tree == null) {
    //         return CompletableFuture.completedFuture(null);
    //     }

    //     var calls = functionCalls.get(uri);
    //     if (!calls.isEmpty()) {
    //         int middle = calls.size()/2;
    //         var middleCtx =  calls.get(middle);
    //         var start = middleCtx.getStart();
    //         var stop = middle
    //         if (middleCtx)

    //     }



    //     HoverInfo hoverInfo = HoverLogic.getHoverKind(tree, position.getLine(), position.getCharacter());
    //     XQuerySemanticAnalyzer analyzer = semanticAnalyzers.get(uri);
    //     // switch (hoverInfo.hoverKind()) {
    //     //     case VARIABLE:
    //     //         break;
    //     //     case FUNCTION_CALL:
    //     //     case FUNCTION_REF:
    //     //         NamespaceResolver resolver = new NamespaceResolver("fn");
    //     //         NamedFunctionRefContext ctx = (NamedFunctionRefContext) hoverInfo.rule();
    //     //         ResolvedName qname = resolver.resolve(ctx.qname().getText());
    //     //         int arity = Integer.parseInt(ctx.IntegerLiteral().getText());
    //     //         FunctionSpecification specification = analyzer.getFunctionManager().getNamedFunctionSpecification(
    //     //             hoverInfo.rule(), qname.namespace(), qname.name(), arity);
    //     //         if (specification == null)
    //     //             return CompletableFuture.completedFuture(null);
    //     //         var rt = specification.returnedType();
    //     //         var args = specification.args().subList(0, arity);
    //     //         StringBuilder sb = new StringBuilder();
    //     //         sb.append(qname.namespace());
    //     //         sb.append(":");
    //     //         sb.append(qname.name());
    //     //         sb.append("(");
    //     //         sb.append(")");

    //     //         break;
    //     //     case OTHER:
    //     //         break;
    //     //     case TYPE:
    //     //         break;
    //     // }
    //     // if (wordAtPosition != null) {
    //     //     // Generate hover content based on the word/symbol
    //     //     String hoverMessage = generateHoverMessage(wordAtPosition);

    //     //     // Create a range that highlights the word/symbol
    //     //     Range hoverRange = findRangeOfWord(text, wordAtPosition, position.getLine());

    //     //     // Construct the hover response
    //     //     Hover hover = new Hover();
    //     //     hover.setContents(new MarkupContent("markdown", hoverMessage));
    //     //     hover.setRange(hoverRange);
    //     //     return CompletableFuture.completedFuture(hover);
    //     // }

    //     // return CompletableFuture.completedFuture(null);
    // }


    private static String createFunctionSpecificationMarkdown(
        String namespace, String name, int arity, FunctionSpecification specification)
    {
        try {
            StringBuilder sb = new StringBuilder();

            // Add function signature with color
            sb.append("```xquery\n");
            sb.append("<span style=\"color:blue\">").append(namespace).append(":</span>");
            sb.append("<span style=\"color:purple\">").append(name).append("</span>(");

            // Add arguments with color
            List<ArgumentSpecification> args = specification.args().subList(0, arity);
            for (int i = 0; i < args.size(); i++) {
                ArgumentSpecification arg = args.get(i);
                sb.append("<span style=\"color:green\">").append(arg.name()).append("</span> as ");
                sb.append("<span style=\"color:red\">").append(arg.type()).append("</span>");
                if (i < args.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append(") as <span style=\"color:red\">").append(specification.returnedType()).append("</span>\n```");

            sb.append("\n\n");
            sb.append("- <span style=\"color:black\">Returned Type:</span> <span style=\"color:red\">")
              .append(specification.returnedType()).append("</span>\n");
            sb.append("- <span style=\"color:black\">Required Context Value Type:</span> <span style=\"color:red\">")
              .append(specification.requiredContextValueType()).append("</span>\n");
            sb.append("- <span style=\"color:black\">Requires Position:</span> <span style=\"color:red\">")
              .append(specification.requiresPosition()).append("</span>\n");
            sb.append("- <span style=\"color:black\">Requires Size:</span> <span style=\"color:red\">")
              .append(specification.requiresSize()).append("</span>\n");

            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

}
