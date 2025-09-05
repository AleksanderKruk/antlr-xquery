package com.github.akruk.antlrxquery.languageserver;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.eclipse.lsp4j.jsonrpc.messages.Either3;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;
import org.eclipse.lsp4j.services.*;

import com.github.akruk.antlrxquery.AntlrXqueryLexer;
import com.github.akruk.antlrxquery.AntlrXqueryParser;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ArrowTargetContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.FunctionCallContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.FunctionNameContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.KeywordArgumentsContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.NamedFunctionRefContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.PositionalArgumentsContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.VarNameAndTypeContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.VarRefContext;
import com.github.akruk.antlrxquery.evaluator.XQuery;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.factories.defaults.XQueryMemoizedValueFactory;
import com.github.akruk.antlrxquery.languageserver.VariableAnalyzer.TypedVariable;
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
    private final Map<String, VariableAnalyzer> semanticAnalyzers = new HashMap<>();
    private final Map<String, List<ParserRuleContext>> functionCalls = new HashMap<>();
    private final Map<String, List<ParserRuleContext>> types = new HashMap<>();
    private final Map<String, List<VarRefContext>> variables = new HashMap<>();
    private final Map<String, List<TypedVariable>> typedVariables = new HashMap<>();


    private final int variableIndex;
    private final int parameterIndex;
    private final int functionIndex;
    private final int typeIndex;

    BasicTextDocumentService(final List<String> tokenLegend)
    {
        variableIndex = tokenLegend.indexOf("variable");
        parameterIndex = tokenLegend.indexOf("parameter");
        functionIndex = tokenLegend.indexOf("function");
        typeIndex = tokenLegend.indexOf("type");
        resolver = new NamespaceResolver("fn");
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
            final VariableAnalyzer analyzer = new VariableAnalyzer(
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
    private final NamespaceResolver resolver;


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


        final List<XQueryValue> variableRefs = XQuery.evaluate(tree, "//(varRef)", _parser).sequence;
        final List<VarRefContext> variableRefContexts = variableRefs.stream().map(v->(VarRefContext)v.node).toList();
        for (final var ctx : variableRefContexts) {
            final Token start = ctx.DOLLAR().getSymbol();
            final Token stop = ctx.qname().getStop();
            final int line = start.getLine() - 1;
            final int charPos = start.getCharPositionInLine();
            final int length = stop.getStopIndex() - start.getStartIndex() + 1;
            SemanticToken semTok = new SemanticToken(line, charPos, length, variableIndex, 0);
            tokens.add(semTok);
        }
        variables.put(uri, variableRefContexts);

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
    public CompletableFuture<Hover> hover(final HoverParams params)
    {
        final String uri = params.getTextDocument().getUri();
        final Position position = params.getPosition();

        final ParseTree tree = parseTreeStore.get(uri);

        if (tree == null) {
            return CompletableFuture.completedFuture(null);
        }

        final var analyzer = semanticAnalyzers.get(uri);

        final List<ParserRuleContext> calls = functionCalls.get(uri);
        if (calls.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        ParserRuleContext foundCtx = findRuleUsingPosition(position, calls);
        if (foundCtx != null) {
            if (foundCtx instanceof final NamedFunctionRefContext ctx) {
                final ResolvedName qname = resolver.resolve(ctx.qname().getText());
                final int arity = Integer.parseInt(ctx.IntegerLiteral().getText());

                return getFunctionHover(foundCtx, analyzer, qname, arity);

            } else if (foundCtx instanceof final FunctionNameContext ctx) {
                FunctionCallContext functionCall = (FunctionCallContext) foundCtx.getParent();
                boolean isArrowCall = (functionCall.getParent() instanceof ArrowTargetContext);
                PositionalArgumentsContext positionalArguments = functionCall.argumentList().positionalArguments();
                int positionalArgCount = positionalArguments == null ? 0 : positionalArguments.argument().size();
                KeywordArgumentsContext keywordArguments = functionCall.argumentList().keywordArguments();
                int kewordArgCount = keywordArguments == null ? 0 : keywordArguments.keywordArgument().size();
                int arity = positionalArgCount + kewordArgCount + (isArrowCall ? 1 : 0);
                final ResolvedName qname = resolver.resolve(ctx.getText());

                return getFunctionHover(foundCtx, analyzer, qname, arity);
            }

        }

        final List<TypedVariable> variablesMappedToTypes = analyzer.variablesMappedToTypes;
        if (variablesMappedToTypes == null || variablesMappedToTypes.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        Comparator<TypedVariable> comparator = (v1, v2) -> {
            Position s1 = v1.range().getStart();
            Position s2 = v2.range().getStart();
            int cmpStart = (s1.getLine() != s2.getLine())
                ? Integer.compare(s1.getLine(), s2.getLine())
                : Integer.compare(s1.getCharacter(), s2.getCharacter());
            if (cmpStart != 0)
                return cmpStart;

            Position e1 = v1.range().getEnd();
            Position e2 = v2.range().getEnd();
            return (e1.getLine() != e2.getLine())
                ? Integer.compare(e1.getLine(), e2.getLine())
                : Integer.compare(e1.getCharacter(), e2.getCharacter());
        };

        variablesMappedToTypes.sort(comparator);

        // Find variable at position
        TypedVariable foundVar = variablesMappedToTypes.stream()
            .filter(v -> {
                Range r = v.range();
                Position start = r.getStart();
                Position end = r.getEnd();
                int line = position.getLine();
                int charPos = position.getCharacter();
                boolean afterStart = (line > start.getLine()) ||
                    (line == start.getLine() && charPos >= start.getCharacter());
                boolean beforeEnd = (line < end.getLine()) ||
                    (line == end.getLine() && charPos <= end.getCharacter());
                return afterStart && beforeEnd;
            })
            .findFirst()
            .orElse(null);

        if (foundVar != null) {
            String hoverText = "```antlrquery\n" + foundVar.type() + "\n```";
            MarkupContent content = new MarkupContent(MarkupKind.MARKDOWN, hoverText);
            Hover hover = new Hover(content);
            hover.setRange(foundVar.range());
            return CompletableFuture.completedFuture(hover);
        }

        final List<ParserRuleContext> ts = types.get(uri);
        if (ts.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        ParserRuleContext foundType = findRuleUsingPosition(position, ts);
        if (foundType != null) {
            String hoverText = "```antlrquery\n" + foundType.accept(analyzer) + "\n```";

            MarkupContent content = new MarkupContent(MarkupKind.MARKDOWN, hoverText);
            Hover hover = new Hover(content);
            hover.setRange(getContextRange(foundType));
            return CompletableFuture.completedFuture(hover);
        }


        return CompletableFuture.completedFuture(null);
    }


    private String varBeingRenamed = null;
    @Override
    public CompletableFuture<Either3<Range, PrepareRenameResult, PrepareRenameDefaultBehavior>>
        prepareRename(PrepareRenameParams params)
    {
        CompletableFuture<Either3<Range, PrepareRenameResult, PrepareRenameDefaultBehavior>> failedFuture = CompletableFuture.failedFuture(
            new ResponseErrorException(new ResponseError(
                ResponseErrorCode.InvalidRequest,
                "This element cannot be renamed",
                null
            ))
        );
        final String uri = params.getTextDocument().getUri();
        final var vars = variables.get(uri);
        if (vars.isEmpty()) {
            return failedFuture;
        }
        final var foundVar = findRuleUsingPosition(params.getPosition(), vars);
        if (foundVar == null) {
            return failedFuture;
        }
        var range = getContextRange(foundVar.qname());
        varBeingRenamed = foundVar.qname().getText();
        return CompletableFuture.completedFuture(Either3.forFirst(range));

    }

    @Override
    public CompletableFuture<WorkspaceEdit> rename(RenameParams params) {
        String uri = params.getTextDocument().getUri();
        String newName = params.getNewName();

        Map<String, List<TextEdit>> edits = new HashMap<>();

        List<TextEdit> fileEdits = new ArrayList<>();
        for (var variable : variables.getOrDefault(uri, List.of())) {
            if (variable.qname().getText().equals(varBeingRenamed)) {
                Range range = getContextRange(variable.qname());
                fileEdits.add(new TextEdit(range, newName));
            }
        }

        if (!fileEdits.isEmpty()) {
            edits.put(uri, fileEdits);
        }

        return CompletableFuture.completedFuture(new WorkspaceEdit(edits));
    }



    private CompletableFuture<Hover> getFunctionHover(ParserRuleContext foundCtx, final XQuerySemanticAnalyzer analyzer,
            final ResolvedName qname, final int arity) {
        final FunctionSpecification specification = analyzer.getFunctionManager().getNamedFunctionSpecification(
            foundCtx, qname.namespace(), qname.name(), arity);
        if (specification == null)
            return CompletableFuture.completedFuture(null);
        final String hoverText = createFunctionSpecificationMarkdown(qname.namespace(), qname.name(), arity, specification);
        final MarkupContent content = new MarkupContent(MarkupKind.MARKDOWN, hoverText);
        final Hover hover = new Hover(content);

        hover.setRange(getContextRange(foundCtx));
        return CompletableFuture.completedFuture(hover);
    }

    private <Rule extends ParserRuleContext>
        Rule findRuleUsingPosition(final Position position, final List<Rule> calls)
    {
        int low = 0;
        int high = calls.size() - 1;
        return findRuleUsingPosition(position, calls, low, high);
    }

    private <Rule extends ParserRuleContext>
        Rule findRuleUsingPosition(final Position position, final List<Rule> calls, int low, int high)
    {
        Rule foundCtx = null;

        while (low <= high) {
            final int mid = low + (high - low) / 2;
            final Rule candidateCtx = calls.get(mid);

            if (isPositionInsideContext(position, candidateCtx)) {
                foundCtx = candidateCtx;
                break;
            }

            if (position.getLine() + 1 < candidateCtx.getStart().getLine()
                || (position.getLine() + 1 == candidateCtx.getStart().getLine()
                && position.getCharacter() < candidateCtx.getStart().getCharPositionInLine()))
            {
                high = mid - 1;
            }
            else {
                low = mid + 1;
            }
        }
        return foundCtx;
    }

    private boolean isPositionInsideContext(final Position position, final ParserRuleContext ctx) {
        final Token startToken = ctx.getStart();
        final Token stopToken = ctx.getStop();

        final int cursorLine = position.getLine() + 1;
        final int cursorChar = position.getCharacter();

        if (cursorLine < startToken.getLine()) return false;
        if (cursorLine > stopToken.getLine()) return false;

        if (cursorLine == startToken.getLine() && cursorChar < startToken.getCharPositionInLine()) {
            return false;
        }

        final int stopTokenEndChar = stopToken.getCharPositionInLine() + stopToken.getText().length();
        if (cursorLine == stopToken.getLine() && cursorChar > stopTokenEndChar) {
            return false;
        }

        return true;
    }

    private Range getContextRange(final ParserRuleContext ctx) {
        final Token startToken = ctx.getStart();
        final Token stopToken = ctx.getStop();

        final Position start = new Position(startToken.getLine() - 1, startToken.getCharPositionInLine());
        final Position end = new Position(stopToken.getLine() - 1, stopToken.getCharPositionInLine() + stopToken.getText().length());

        return new Range(start, end);
    }


    private static String createFunctionSpecificationMarkdown(
        final String namespace, final String name, final int arity, final FunctionSpecification specification)
    {
        try {
            final StringBuilder sb = new StringBuilder();

            sb.append("```antlrquery\n")
                .append(namespace)
                .append(":")
                .append(name)
                .append("(\n");

            final List<ArgumentSpecification> args = specification.args().subList(0, arity);
            for (int i = 0; i < args.size(); i++) {
                final ArgumentSpecification arg = args.get(i);
                sb.append("    ")
                    .append("$")
                    .append(arg.name())
                    .append(" as ")
                    .append(arg.type());
                if (i < args.size() - 1) {
                    sb.append(",\n");
                }
            }
            sb.append("\n) as ")
                .append(specification.returnedType());
            return sb.toString();
        } catch (final Exception e) {
            return null;
        }
    }

}
