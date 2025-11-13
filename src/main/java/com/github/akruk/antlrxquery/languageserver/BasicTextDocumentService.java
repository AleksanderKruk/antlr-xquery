package com.github.akruk.antlrxquery.languageserver;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4j.InlayHintKind;
import org.eclipse.lsp4j.InlayHintParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PrepareRenameDefaultBehavior;
import org.eclipse.lsp4j.PrepareRenameParams;
import org.eclipse.lsp4j.PrepareRenameResult;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.Either3;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;

import com.github.akruk.antlrxquery.AntlrXqueryLexer;
import com.github.akruk.antlrxquery.AntlrXqueryParser;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ArgumentListContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ArrowTargetContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.FunctionCallContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.FunctionDeclContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.FunctionNameContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.KeywordArgumentsContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.NamedFunctionRefContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.NamedRecordTypeDeclContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.PositionalArgumentsContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.TypeNameContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.VarNameAndTypeContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.VarNameContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.VarRefContext;
import com.github.akruk.antlrxquery.evaluator.XQuery;
import com.github.akruk.antlrxquery.evaluator.XQuery.TreeEvaluator;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.factories.defaults.XQueryMemoizedValueFactory;
import com.github.akruk.antlrxquery.languageserver.AntlrQueryLanguageServer.ExtractLocationInfo;
import com.github.akruk.antlrxquery.languageserver.AntlrQueryLanguageServer.ExtractVariableLocationsParams;
import com.github.akruk.antlrxquery.languageserver.AntlrQueryLanguageServer.ExtractVariableParams;
import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver;
import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver.QualifiedName;
import com.github.akruk.antlrxquery.semanticanalyzer.DiagnosticError;
import com.github.akruk.antlrxquery.semanticanalyzer.DiagnosticWarning;
import com.github.akruk.antlrxquery.semanticanalyzer.GrammarManager;
import com.github.akruk.antlrxquery.semanticanalyzer.ModuleManager;
import com.github.akruk.antlrxquery.semanticanalyzer.XQuerySemanticAnalyzer;
import com.github.akruk.antlrxquery.semanticanalyzer.XQuerySemanticAnalyzer.AnalysisListener;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.XQuerySemanticContextManager;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.XQuerySemanticSymbolManager;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.XQuerySemanticSymbolManager.ArgumentSpecification;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.XQuerySemanticSymbolManager.FunctionSpecification;
import com.github.akruk.antlrxquery.typesystem.defaults.TypeInContext;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryMemoizedTypeFactory;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryNamedTypeSets;
import com.google.gson.JsonPrimitive;

public class BasicTextDocumentService implements TextDocumentService {
    private LanguageClient client;
    private final Map<String, List<Token>> tokenStore = new HashMap<>();
    private final Map<String, ParseTree> parseTreeStore = new HashMap<>();
    private final Map<String, XQuerySemanticAnalyzer> semanticAnalyzers = new HashMap<>();
    private final Map<String, List<NamedFunctionRefContext>> namedFunctionRefs = new HashMap<>();
    private final Map<String, List<FunctionNameContext>> functionNames = new HashMap<>();
    private final Map<String, List<ParserRuleContext>> types = new HashMap<>();
    private final Map<String, List<TypeNameContext>> namedTypes = new HashMap<>();
    private final Map<String, List<VarRefContext>> variableReferences = new HashMap<>();
    private final Map<String, List<VarNameContext>> variableDeclarationsWithoutType = new HashMap<>();
    private final Map<String, List<FunctionDeclContext>> functionDecls = new HashMap<>();
    private final Map<String, List<NamedRecordTypeDeclContext>> recordDeclarations = new HashMap<>();
    private final Map<String, List<VarNameAndTypeContext>> variableDeclarations = new HashMap<>();
    private final Map<String, Map<VarRefContext, TypeInContext>> varRefsMappedToTypes = new HashMap<>();
    private final Map<String, Map<VarNameContext, TypeInContext>> varNamesMappedToTypes = new HashMap<>();
    private final DiagnosticMessageCreator diagnosticMessageCreator = new DiagnosticMessageCreator();

    private Set<Path> modulePaths = Set.of();

	private final int variableIndex;
    private final int functionIndex;
    private final int typeIndex;
    // private final int parameterIndex;
    private final int stringIndex;
    private final int propertyIndex;
    private final int decoratorIndex;

    BasicTextDocumentService(final List<String> tokenLegend)
    {
        variableIndex = tokenLegend.indexOf("variable");
        functionIndex = tokenLegend.indexOf("function");
        typeIndex = tokenLegend.indexOf("type");
        // parameterIndex = tokenLegend.indexOf("parameter");
        stringIndex = tokenLegend.indexOf("string");
        propertyIndex = tokenLegend.indexOf("property");
        decoratorIndex = tokenLegend.indexOf("decorator");
        resolver = new NamespaceResolver(
            "fn",
            "",
            "",
            "",
            "");
    }

    public void setModulePaths(final Set<Path> modulePaths) {
		this.modulePaths = modulePaths;
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

    public void parseAndAnalyze(final String uri, final String text)
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
            final Set<Path> paths = new HashSet<>(modulePaths.size());
            paths.addAll(modulePaths);
            final Path currentPath = Path.of(URI.create(uri));
            paths.add(currentPath.getParent());
            final XQuerySemanticAnalyzer analyzer = new XQuerySemanticAnalyzer(
                null,
                new XQuerySemanticContextManager(typeFactory),
                typeFactory,
                new XQueryMemoizedValueFactory(typeFactory),
                new XQuerySemanticSymbolManager(typeFactory),
                null,
                new ModuleManager(paths),
                new GrammarManager(paths),
                typeFactory.anyNode()
                );

            final Map<VarRefContext, TypeInContext> varRefsMappedToTypes_ = new HashMap<>();
            final Map<VarNameContext, TypeInContext> varNamesMappedToTypes_ = new HashMap<>();
            analyzer.addListener(new AnalysisListener() {
                @Override
                public void onVariableDeclaration(VarNameContext varName, TypeInContext type) {
                    varNamesMappedToTypes_.put(varName, type);
                }

                @Override
                public void onVariableReference(VarRefContext varRef, TypeInContext type) {
                    varRefsMappedToTypes_.put(varRef, type);
                }
            });
            try {
                analyzer.visit(tree);
            } catch(Exception e) {

            }
            semanticAnalyzers.put(uri, analyzer);
            varRefsMappedToTypes.put(uri, varRefsMappedToTypes_);
            varNamesMappedToTypes.put(uri, varNamesMappedToTypes_);


            final List<DiagnosticError> errors = analyzer.getErrors();
            final List<DiagnosticWarning> warnings = analyzer.getWarnings();
            System.err
                .println("[parseAndAnalyze] Semantic Errors: " + errors.size() + ", Warnings: " + warnings.size());

            for (final var error : errors) {
                diagnostics.add(new Diagnostic(
                    new Range(
                        new Position(error.startLine() - 1, error.charPositionInLine()),
                        new Position(error.endLine() - 1, error.endCharPositionInLine())),
                    diagnosticMessageCreator.create(error),
                    DiagnosticSeverity.Error,
                    "antlr-xquery"));
            }

            for (final var warning : warnings) {
                diagnostics.add(new Diagnostic(
                    new Range(
                        new Position(warning.startLine() - 1, warning.charPositionInLine()),
                        new Position(warning.endLine() - 1, warning.endCharPositionInLine())),
                    diagnosticMessageCreator.create(warning),
                    DiagnosticSeverity.Warning,
                    "antlr-xquery"));
            }

            if (client != null) {
                client.publishDiagnostics(new PublishDiagnosticsParams(uri, diagnostics));
            } else {
                System.err.println("[parseAndAnalyze] LanguageClient is null. Cannot publish diagnostics.");
            }

            {
            final var declarations = XQuery.evaluateWithMockRoot(tree, "//varNameAndType", _parser).sequence;
            final List<VarNameAndTypeContext> declarationContexts = declarations.stream()
                .map(x->(VarNameAndTypeContext) x.node)
                .toList();

            final Stream<VarNameContext> declarationWithoutTypeContexts = declarationContexts.stream()
                .filter(x->x.typeDeclaration() == null)
                .map(x->x.varName());
            variableDeclarations.put(uri, declarationContexts);

            final List<XQueryValue> windowVars = XQuery.evaluateWithMockRoot(tree, "//windowVars//varName", _parser).sequence;
            var windowVarVarRefs = windowVars.stream().map(t -> (VarNameContext) t.node);
            List<VarNameContext> combinedVarRefs = Stream
                .of(declarationWithoutTypeContexts, windowVarVarRefs)
                .flatMap((Stream<VarNameContext> x)->x)
                .toList();
            variableDeclarationsWithoutType.put(uri, combinedVarRefs);
            }

            {
            final var typeNames = XQuery.evaluateWithMockRoot(tree, "//typeName", _parser).sequence;
            final List<TypeNameContext> typeNameContexts = typeNames.stream()
                .map(x->(TypeNameContext) x.node)
                .toList();
            namedTypes.put(uri, typeNameContexts);
            }

            {
            final List<XQueryValue> recordDecls = XQuery.evaluateWithMockRoot(tree, "//namedRecordTypeDecl", _parser).sequence;
            final List<NamedRecordTypeDeclContext> rdecls = recordDecls.stream().map(v->(NamedRecordTypeDeclContext)v.node).toList();
            recordDeclarations.put(uri, rdecls);
            }

            {
            final List<XQueryValue> variableRefs = XQuery.evaluateWithMockRoot(tree, "//varRef", _parser).sequence;
            variableReferences.put(uri, variableRefs.stream().map(v->(VarRefContext)v.node).toList());
            }

            {
            final List<XQueryValue> fNames = XQuery.evaluateWithMockRoot(tree, "//functionName", _parser).sequence;
            functionNames.put(uri, fNames.stream().map(v -> (FunctionNameContext) v.node).toList());
            }

            {
            final List<XQueryValue> namedRefs = XQuery.evaluateWithMockRoot(tree, "//namedFunctionRef", _parser).sequence;
            namedFunctionRefs.put(uri, namedRefs.stream().map(v -> (NamedFunctionRefContext) v.node).toList());
            }

            {
            final List<XQueryValue> fDecls = XQuery.evaluateWithMockRoot(tree, "//functionDecl", _parser).sequence;
            functionDecls.put(uri, fDecls.stream().map(t -> (FunctionDeclContext)t.node).toList());
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
    final AntlrXqueryParser _parser = new AntlrXqueryParser(_tokens);
    private final NamespaceResolver resolver;

    private final TreeEvaluator constructors = XQuery.compile("//constructorChars", _parser);
    private final TreeEvaluator constructorBoundaries = XQuery.compile("//(STRING_CONSTRUCTION_START|STRING_CONSTRUCTION_END)", _parser);
    private final TreeEvaluator properties = XQuery.compile("//extendedFieldDeclaration//fieldDeclaration/fieldName", _parser);
    private final TreeEvaluator annotations = XQuery.compile("//annotation", _parser);

    record SemanticToken(int line, int charPos, int length, int typeIndex, int modifierBitmask) {}

    @Override
    public CompletableFuture<SemanticTokens> semanticTokensFull(final SemanticTokensParams params) {
        final String uri = params.getTextDocument().getUri();
        final ParseTree tree = parseTreeStore.get(uri);

        if (tree == null) {
            return CompletableFuture.completedFuture(new SemanticTokens(List.of()));
        }


        final List<SemanticToken> tokens = new ArrayList<>();

        final List<XQueryValue> typeValues = XQuery.evaluateWithMockRoot(
            tree, """
                    //(sequenceType|castTarget)
                    | //itemTypeDecl/(qname|itemType)
                    | //namedRecordTypeDecl/qname
            """, _parser).sequence;
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


        for (final var ctx : variableReferences.getOrDefault(uri, List.of())) {
            final Token start = ctx.getStart();
            final Token stop = ctx.qname().getStop();
            final int line = start.getLine() - 1;
            final int charPos = start.getCharPositionInLine();
            final int length = stop.getStopIndex() - start.getStartIndex() + 1;
            final SemanticToken semTok = new SemanticToken(line, charPos, length, variableIndex, 0);
            tokens.add(semTok);
        }

        for (final var val : functionNames.getOrDefault(uri, List.of())) {
            final SemanticToken functionToken = getFunctionSemanticToken(val);
            tokens.add(functionToken);
        }
        for (final var val : namedFunctionRefs.getOrDefault(uri, List.of())) {
            final SemanticToken functionToken = getFunctionSemanticToken(val);
            tokens.add(functionToken);
        }

        markTokens(tree, tokens, constructors, stringIndex);

        for (final XQueryValue val : constructorBoundaries.evaluate(tree).sequence) {
            final TerminalNode ctx = (TerminalNode) val.node;
            final Token start = ctx.getSymbol();
            final int line = start.getLine() - 1;
            final int charPos = start.getCharPositionInLine();
            final SemanticToken semTok = new SemanticToken(line, charPos, 3, stringIndex, 0);
            tokens.add(semTok);
        }

        markTokens(tree, tokens, properties, propertyIndex);
        markTokens(tree, tokens, annotations, decoratorIndex);


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

    private void markTokens(final ParseTree tree, final List<SemanticToken> tokens, final TreeEvaluator contextQuery, final int typeIndex)
    {
        for (final XQueryValue val : contextQuery.evaluate(tree).sequence) {
            final ParserRuleContext ctx = (ParserRuleContext) val.node;
            final Token start = ctx.getStart();
            final Token stop = ctx.getStop();
            final int line = start.getLine() - 1;
            final int charPos = start.getCharPositionInLine();
            final int length = stop.getStopIndex() - start.getStartIndex() + 1;
            final SemanticToken semTok = new SemanticToken(line, charPos, length, typeIndex, 0);
            tokens.add(semTok);
        }
    }

    private SemanticToken getFunctionSemanticToken(final ParserRuleContext ctx)
    {
        final Token start = ctx.getStart();
        final Token stop = ctx.getStop();
        final int line = start.getLine() - 1;
        final int charPos = start.getCharPositionInLine();
        final int length = stop.getStopIndex() - start.getStartIndex() + 1;
        final SemanticToken functionToken = new SemanticToken(line, charPos, length, functionIndex, 0);
        return functionToken;
    }


    @Override
    public CompletableFuture<Hover> hover(final HoverParams params)
    {
        final String uri = params.getTextDocument().getUri();
        final Position position = params.getPosition();

        final ParseTree tree = parseTreeStore.get(uri);
        final XQuerySemanticAnalyzer analyzer = semanticAnalyzers.get(uri);

        if (tree == null || analyzer == null) {
            return CompletableFuture.completedFuture(null);
        }


        final List<FunctionNameContext> names = functionNames.get(uri);
        final List<NamedFunctionRefContext> namedRefs = namedFunctionRefs.get(uri);
        if (names.isEmpty() && namedRefs.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        final FunctionNameContext foundName = findRuleUsingPosition(position, names);
        if (foundName != null) {
            final int arity = getArity(foundName);
            final QualifiedName qname = resolver.resolveFunction(foundName.getText());
            return getFunctionHover(foundName, analyzer, qname, arity);
        }
        final NamedFunctionRefContext foundNamedRef = findRuleUsingPosition(position, namedRefs);
        if (foundNamedRef != null) {
            final QualifiedName qname = resolver.resolveFunction(foundNamedRef.qname().getText());
            final int arity = getArity(foundNamedRef);

            return getFunctionHover(foundNamedRef, analyzer, qname, arity);
        }


        {VarRefContext foundVarRef = findRuleUsingPosition(position, variableReferences.get(uri));
        if (foundVarRef != null) {
            var type = varRefsMappedToTypes.get(uri).get(foundVarRef);
            String hoverText = "```antlrquery\n" + type + "\n```";
            MarkupContent content = new MarkupContent(MarkupKind.MARKDOWN, hoverText);
            Hover hover = new Hover(content);
            hover.setRange(getContextRange(foundVarRef));
            return CompletableFuture.completedFuture(hover);
        }}

        {final List<ParserRuleContext> ts = types.get(uri);
        if (ts.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        final ParserRuleContext foundType = findRuleUsingPosition(position, ts);
        if (foundType != null) {
            final String hoverText = "```antlrquery\n" + foundType.accept(analyzer) + "\n```";

            final MarkupContent content = new MarkupContent(MarkupKind.MARKDOWN, hoverText);
            final Hover hover = new Hover(content);
            hover.setRange(getContextRange(foundType));
            return CompletableFuture.completedFuture(hover);
        }}

        return CompletableFuture.completedFuture(null);
    }

    private int getArity(final ParserRuleContext foundCtx)
    {
        final FunctionCallContext functionCall = (FunctionCallContext) foundCtx.getParent();
        final boolean isArrowCall = isArrowCall(functionCall);
        final PositionalArgumentsContext positionalArguments = functionCall.argumentList().positionalArguments();
        final int positionalArgCount = positionalArguments == null ? 0 : positionalArguments.argument().size();
        final KeywordArgumentsContext keywordArguments = functionCall.argumentList().keywordArguments();
        final int kewordArgCount = keywordArguments == null ? 0 : keywordArguments.keywordArgument().size();
        final int arity = positionalArgCount + kewordArgCount + (isArrowCall ? 1 : 0);
        return arity;
    }

    private boolean isArrowCall(final FunctionCallContext functionCall)
    {
        return functionCall.getParent() instanceof ArrowTargetContext;
    }

    private int getArity(final NamedFunctionRefContext ctx)
    {
        return Integer.parseInt(ctx.IntegerLiteral().getText());
    }



    private String varBeingRenamed = null;
    private FunctionDeclData functionBeingRenamed = null;
    @Override
    public CompletableFuture<Either3<Range, PrepareRenameResult, PrepareRenameDefaultBehavior>>
        prepareRename(final PrepareRenameParams params)
    {
        final CompletableFuture<Either3<Range, PrepareRenameResult, PrepareRenameDefaultBehavior>> failedFuture = CompletableFuture.failedFuture(
            new ResponseErrorException(new ResponseError(
                ResponseErrorCode.InvalidRequest,
                "This element cannot be renamed",
                null
            ))
        );
        final String uri = params.getTextDocument().getUri();
        final var vars = variableReferences.get(uri);
        final Position position = params.getPosition();
        final var foundVar = findRuleUsingPosition(position, vars);
        if (foundVar != null) {
            // TODO: Add variable char validation
            final var range = getContextRange(foundVar.qname());
            varBeingRenamed = foundVar.qname().getText();
            functionBeingRenamed = null;
            return CompletableFuture.completedFuture(Either3.forFirst(range));
        }

        XQuerySemanticAnalyzer analyzer = semanticAnalyzers.get(uri);
        {
            final var foundFunctionName = findRuleUsingPosition(position, functionNames.get(uri));
            if (foundFunctionName != null) {
                final var fName = resolver.resolveFunction(foundFunctionName.qname().getText());
                final var functionDeclaration = getFunctionDeclaration(foundFunctionName, fName, analyzer);
                if (functionDeclaration != null) {
                    functionBeingRenamed = functionDeclaration;
                    varBeingRenamed = null;
                    final Range contextRange = getContextRange(foundFunctionName.qname());
                    return CompletableFuture.completedFuture(Either3.forFirst(contextRange));
                }
            }
        }
        {
            final var foundFunctionRef = findRuleUsingPosition(position, namedFunctionRefs.get(uri));
            if (foundFunctionRef != null) {
                final var fName = resolver.resolveFunction(foundFunctionRef.qname().getText());
                final var functionDeclaration = getFunctionDeclaration(foundFunctionRef, fName, analyzer);
                if (functionDeclaration != null) {
                    functionBeingRenamed = functionDeclaration;
                    varBeingRenamed = null;
                    final Range contextRange = getContextRange(foundFunctionRef.qname());
                    return CompletableFuture.completedFuture(Either3.forFirst(contextRange));
                }
            }
        }
        {
            for (final var fDecl : functionDecls.get(uri)) {
                if (isPositionInsideContext(position, fDecl.qname())) {
                    functionBeingRenamed = new FunctionDeclData(uri, fDecl);
                    final Range contextRange = getContextRange(fDecl.qname());
                    return CompletableFuture.completedFuture(Either3.forFirst(contextRange));
                }
            }
        }
        varBeingRenamed = null;
        functionBeingRenamed = null;
        return failedFuture;
    }

    @Override
    public CompletableFuture<WorkspaceEdit> rename(final RenameParams params) {
        final String uri = params.getTextDocument().getUri();
        final String newName = params.getNewName();

        final Map<String, List<TextEdit>> edits = new HashMap<>();

        if (varBeingRenamed != null) {
            handleVarRenamingFileEdits(uri, newName, edits);
        }
        if (functionBeingRenamed != null) {
            handleFunctionRenamingFileEdits(uri, newName, edits);
            final TextEdit fDeclNameEdit = new TextEdit(getContextRange(functionBeingRenamed.context.qname()), newName);
            edits.get(uri).add(fDeclNameEdit);
        }

        return CompletableFuture.completedFuture(new WorkspaceEdit(edits));
    }

    private void handleFunctionRenamingFileEdits(final String uri, final String newName, final Map<String, List<TextEdit>> edits)
    {
        for (final var functionUri : functionNames.keySet()) {
            final List<TextEdit> fileEdits = edits.computeIfAbsent(functionUri, (_)->new ArrayList<>());
            for (final var functionName : functionNames.get(functionUri)) {
                final var qname = resolver.resolveFunction(functionName.qname().getText());
                final FunctionDeclData functionDeclaration = getFunctionDeclaration(
                    functionName, qname, semanticAnalyzers.get(functionUri));
                if (functionDeclaration == null)
                    continue;
                if (functionDeclaration.context == functionBeingRenamed.context) {
                    final Range range = getContextRange(functionName.qname());
                    fileEdits.add(new TextEdit(range, newName));
                }
            }

        }
        for (final var namedUri : namedFunctionRefs.keySet()) {
            final List<TextEdit> fileEdits = edits.computeIfAbsent(namedUri, (_)->new ArrayList<>());
            for (final var namedRef : namedFunctionRefs.getOrDefault(namedUri, List.of())) {
                final var qname = resolver.resolveFunction(namedRef.qname().getText());
                final FunctionDeclData functionDeclaration = getFunctionDeclaration(
                    namedRef, qname, semanticAnalyzers.get(namedUri));
                if (functionDeclaration == null)
                    continue;
                if (functionDeclaration.context == functionBeingRenamed.context) {
                    final Range range = getContextRange(namedRef.qname());
                    fileEdits.add(new TextEdit(range, newName));
                }
            }
        }
    }

    private void handleVarRenamingFileEdits(final String uri, final String newName, final Map<String, List<TextEdit>> edits)
    {
        final List<TextEdit> fileEdits = edits.computeIfAbsent(uri, (_)->new ArrayList<>());
        for (final var variable : variableReferences.getOrDefault(uri, List.of())) {
            if (variable.qname().getText().equals(varBeingRenamed)) {
                final Range range = getContextRange(variable.qname());
                fileEdits.add(new TextEdit(range, newName));
            }
        }
    }




    private CompletableFuture<Hover> getFunctionHover(final ParserRuleContext foundCtx, final XQuerySemanticAnalyzer analyzer,
            final QualifiedName qname, final int arity) {
        final FunctionSpecification specification = analyzer.getSymbolManager().getNamedFunctionSpecification(
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
        Rule findRuleUsingPosition(final Position position, final List<Rule> elements)
    {
        if (position == null || elements == null)
            return null;
        final int low = 0;
        final int high = elements.size() - 1;
        return findRuleUsingPosition(position, elements, low, high);
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

    Range getContextRange(final ParserRuleContext ctx) {
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
                .append("(");

            final List<ArgumentSpecification> args = specification.args().subList(0, arity);
            if (!args.isEmpty()) {
                sb.append("\n");
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
                sb.append("\n");
            }
            sb.append(") as ")
                .append(specification.returnedType());
            return sb.toString();
        } catch (final Exception e) {
            return null;
        }
    }

    @Override
    public CompletableFuture<List<InlayHint>> inlayHint(final InlayHintParams params)
    {
        final String uri = params.getTextDocument().getUri();
        final List<InlayHint> hints = new ArrayList<>();
        final var analyzer = semanticAnalyzers.get(uri);
        if (analyzer == null) {
            return CompletableFuture.completedFuture(List.of());
        }

        List<VarNameContext> variableDeclarationsWithoutTypeForFile = variableDeclarationsWithoutType.getOrDefault(uri, List.of());
        Map<VarNameContext, TypeInContext> varNamesMappedToTypesForFile = varNamesMappedToTypes.getOrDefault(uri, Map.of());
        for (VarNameContext variable : variableDeclarationsWithoutTypeForFile) {
            TypeInContext type = varNamesMappedToTypesForFile.get(variable);
            Token stop = variable.getStop();
            Position hintPosition = new Position(stop.getLine() - 1,
                stop.getCharPositionInLine() + stop.getText().length());

            final InlayHint hint = new InlayHint();
            hint.setPosition(hintPosition);
            hint.setLabel(Either.forLeft(type.toString()));
            hint.setKind(InlayHintKind.Type);
            hint.setPaddingLeft(true);

            hints.add(hint);
        }

        for (final FunctionNameContext functionName : functionNames.getOrDefault(uri, List.of())) {
            final int arity = getArity(functionName);
            final QualifiedName resolvedName = resolver.resolveFunction(functionName.qname().getText());
            final FunctionSpecification spec = analyzer.getSymbolManager().getNamedFunctionSpecification(
                functionName, resolvedName.namespace(), resolvedName.name(), arity);
            if (spec == null)
                continue;
            final FunctionCallContext functionCall = (FunctionCallContext) functionName.getParent();
            final ArgumentListContext argumentList = functionCall.argumentList();
            if (argumentList == null) {
                continue;
            }
            final PositionalArgumentsContext positionalArguments = argumentList.positionalArguments();
            if (positionalArguments == null) {
                continue;
            }
            int argIndex = isArrowCall(functionCall) ? 1 : 0;
            for (final var positional : positionalArguments.argument()) {
                final Token start = positional.getStart();
                final Position hintPosition = new Position(start.getLine() - 1, start.getCharPositionInLine());
                if (argIndex >= spec.args().size()) {
                    continue;
                }
                final ArgumentSpecification argSpec = spec.args().get(argIndex);

                final InlayHint hint = new InlayHint();
                hint.setPosition(hintPosition);
                hint.setLabel(Either.forLeft(argSpec.name() + ":"));
                hint.setKind(InlayHintKind.Parameter);
                hint.setPaddingLeft(true);
                hints.add(hint);
                argIndex++;
            }

        }
        return CompletableFuture.completedFuture(hints);
    }


    record FunctionDeclData(String uri, FunctionDeclContext context) {
    }

    FunctionDeclData getFunctionDeclaration(final NamedFunctionRefContext namedFunctionRef, final QualifiedName qname,
        final XQuerySemanticAnalyzer analyzer)
    {
        final int arity = getArity(namedFunctionRef);
        final FunctionSpecification spec = analyzer
            .getSymbolManager()
            .getNamedFunctionSpecification(namedFunctionRef, qname.namespace(), qname.name(), arity);
        if (spec == null) {
            return null;
        }
        for (final var fDeclUri : functionDecls.keySet()) {
            for (final var fDecl : functionDecls.get(fDeclUri)) {
                final var qname2 = resolver.resolveFunction(fDecl.qname().getText());
                if (!qname.equals(qname2)) {
                    continue;
                }
                return new FunctionDeclData(fDeclUri, fDecl);
            }
        }
        return null;
    }

    FunctionDeclData getFunctionDeclaration(final FunctionNameContext namedFunctionRef, final QualifiedName qname,
        final XQuerySemanticAnalyzer analyzer)
    {
        final int arity = getArity(namedFunctionRef);
        final FunctionSpecification spec = analyzer
            .getSymbolManager()
            .getNamedFunctionSpecification(namedFunctionRef, qname.namespace(), qname.name(), arity);
        if (spec == null) {
            return null;
        }
        for (final var fDeclUri : functionDecls.keySet()) {
            for (final var fDecl : functionDecls.get(fDeclUri)) {
                final var qname2 = resolver.resolveFunction(fDecl.qname().getText());
                if (!qname.equals(qname2)) {
                    continue;
                }
                return new FunctionDeclData(fDeclUri, fDecl);
            }
        }
        return null;
    }

    record RecordDeclData(String uri, NamedRecordTypeDeclContext context) {
    }

    RecordDeclData getRecordDeclaration(final QualifiedName qname, final XQuerySemanticAnalyzer analyzer) {
        for (final var recordUrl : recordDeclarations.keySet()) {
            for (final var record : recordDeclarations.get(recordUrl)) {
                final var name = record.qname().getText();
                final var resolved = resolver.resolveType(name);
                if (resolved.equals(qname)) {
                    return new RecordDeclData(recordUrl, record);
                }
            }
        }
        return null;
    }



    CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>>
        handleFunctionCallDefinition(final FunctionNameContext functionName, final String document)
    {
        final int arity = getArity(functionName);
        final var analyzer = semanticAnalyzers.get(document);
        final var qname = resolver.resolveFunction(functionName.getText());
        final FunctionSpecification spec = analyzer.getSymbolManager().getNamedFunctionSpecification(
            functionName, qname.namespace(), qname.name(), arity);
        if (spec == null) {
            return CompletableFuture.completedFuture(Either.forLeft(List.of()));
        }
        final var functionDeclaration = getFunctionDeclaration(functionName, qname, analyzer);
        if (functionDeclaration != null) {
            return CompletableFuture.completedFuture(Either.forLeft(List.of(
                new Location(functionDeclaration.uri, getContextRange(functionDeclaration.context.qname())))));
        }

        final var recordDeclaration = getRecordDeclaration(qname, analyzer);
        if (recordDeclaration != null) {
            return CompletableFuture.completedFuture(Either.forLeft(List.of(
                new Location(recordDeclaration.uri, getContextRange(recordDeclaration.context.qname()))
            )));
        }

        return CompletableFuture.completedFuture(Either.forLeft(List.of()));

    }



    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>>
        definition(final DefinitionParams params)
    {
        final var position = params.getPosition();
        final var document = params.getTextDocument().getUri();
        final var varRefs = variableReferences.getOrDefault(document, List.of());
        final VarRefContext foundVarRef = findRuleUsingPosition(position, varRefs);
        if (foundVarRef != null) {
            return handleVariableReferenceDefinition(document, foundVarRef);
        }
        final var foundCall = findRuleUsingPosition(position, functionNames.get(document));
        if (foundCall != null) {
            return handleFunctionCallDefinition(foundCall, document);
        }
        final var foundNamedRef = findRuleUsingPosition(position, namedFunctionRefs.get(document));
        if (foundNamedRef != null) {
            return handleFunctionCallDefinition(foundCall, document);
        }
        final TypeNameContext foundNamedType = findRuleUsingPosition(position, namedTypes.get(document));
        if (foundNamedType != null) {
            return handleTypeName(foundNamedType);
        }
        return CompletableFuture.completedFuture(Either.forLeft(List.of()));
    }

    private CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>>
        handleTypeName(final TypeNameContext foundNamedType)
    {
        final var resolvedName = resolver.resolveType(foundNamedType.qname().getText());
        for (final var recordDeclUrl : recordDeclarations.keySet()) {
            for (final var recordDecl : recordDeclarations.get(recordDeclUrl)) {
                final var recordName = resolver.resolveType(recordDecl.qname().getText());
                if (recordName.equals(resolvedName)) {
                    return CompletableFuture.completedFuture(Either.forLeft(List.of(
                        new Location(recordDeclUrl, getContextRange(recordDecl.qname()))
                    )));
                }
            }

        }
        return CompletableFuture.completedFuture(Either.forLeft(List.of()));
    }

    private CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>>
        handleVariableReferenceDefinition(
            String document,
            VarRefContext foundVarRef)
    {
        final String varname = foundVarRef.getText();
        final int foundOffset = foundVarRef.getStart().getStartIndex();
        VarNameAndTypeContext previousDecl = null;

        for (VarNameAndTypeContext vdef : variableDeclarations.getOrDefault(document, List.of())) {
            int declOffset = vdef.varName().getStart().getStartIndex();
            if (declOffset > foundOffset) {
                break;
            }
            if (vdef.varName().getText().equals(varname)) {
                previousDecl = vdef;
            }
        }

        if (previousDecl == null) {
            return CompletableFuture.completedFuture(Either.forLeft(List.of()));
        }

        var declaringVarRef = previousDecl.varName();
        Location location = new Location(document, getContextRange(declaringVarRef));
        return CompletableFuture.completedFuture(Either.forLeft(List.of(location)));
    }

    @Override
    public CompletableFuture<CodeAction> resolveCodeAction(final CodeAction unresolved) {
        final int actionId = ((JsonPrimitive) unresolved.getData()).getAsInt();
        System.err.println("[resolveCodeAction] Resolution of id: " + actionId);
        final VarExtractionInfo extractionInfo = varExtractionData.get(actionId);
        if (extractionInfo == null) {
            return CompletableFuture.completedFuture(null);
        }

        final Set<Range> insertedRanges = new HashSet<>(extractionInfo.contextStack.size());
        final List<Range> ranges = new ArrayList<>(extractionInfo.contextStack.size());
        final List<Integer> indices = new ArrayList<>(extractionInfo.contextStack.size());
        int i = 0;
        for (final var ctx : extractionInfo.contextStack) {
            final var range = getContextRange(ctx);
            if (insertedRanges.add(range)) {
                ranges.add(range);
                indices.add(i);
            }
            i++;
        }
        resolvedVarExtractionData.put(actionId, new ResolvedVarExtractionInfo(ranges, indices));

        unresolved.setCommand(new Command(
            "AntlrQuery: Select value to extract",
            "extension.selectExtractionTarget",
            List.of(ranges, indices, actionId)
        ));
        return CompletableFuture.completedFuture(unresolved);
    }

    record VarExtractionInfo(
        String uri,
        Position position,
        List<ParserRuleContext> contextStack
    ) {}

    record ResolvedVarExtractionInfo(
        List<Range> ranges,
        List<Integer> indices
    ) {}


    private int codeActionId = 0;
    final Map<Integer, VarExtractionInfo> varExtractionData = new HashMap<>();
    final Map<Integer, ResolvedVarExtractionInfo> resolvedVarExtractionData = new HashMap<>();

    synchronized int codeActionId()
    {
        if (codeActionId == Integer.MAX_VALUE) {
            codeActionId = 0;
        } else {
            codeActionId++;
        }
        return codeActionId;
    }

    private final TreeEvaluator extractableExpressions = XQuery.compile("""
        /ancestor-or-self::(exprSingle
            | fLWORExpr
            | quantifiedExpr
            | ifExpr
            | switchExpr
            | tryCatchExpr
            | orExpr
            | comparisonExpr
            | otherwiseExpr
            | stringConcatExpr
            | rangeExpr
            | additiveExpr
            | multiplicativeExpr
            | unionExpr
            | intersectExpr
            | instanceofExpr
            | treatExpr
            | castableExpr
            | castExpr
            | pipelineExpr
            | arrowExpr
            | postfixExpr
            | axisStep
            | reverseStep
            | forwardStep
            | functionItemExpr
            | mapConstructor
            | arrayConstructor
            | primaryExpr
            | literal
            | varRef
            | parenthesizedExpr
            | contextValueRef
            | functionCall
            | functionItemExpr
            | mapConstructor
            | arrayConstructor
            | stringConstructor
            | unaryLookup)
    """, _parser);

    @Override
    public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(final CodeActionParams params)
    {
        final Position start = params.getRange().getStart();
        final PositionAnalyzer positionAnalyzer = new PositionAnalyzer(start);
        final String uri = params.getTextDocument().getUri();
        final ParseTree tree = parseTreeStore.get(uri);
        if (tree == null) {
            return CompletableFuture.completedFuture(List.of());
        }
        final PositionAnalysis analysis = positionAnalyzer.visit(tree);
        final List<ParserRuleContext> extractableExpressionNodes = extractableExpressions
            .evaluate(analysis.innerMostContext())
            .sequence.stream().map(v -> (ParserRuleContext) v.node)
            .toList();
        final CodeAction action = new CodeAction();
        action.setTitle("Extract variable");
        action.setKind(CodeActionKind.RefactorExtract);
        final VarExtractionInfo extractionInfo = new VarExtractionInfo(uri, start, extractableExpressionNodes);
        final int codeActionId_ = codeActionId();
        varExtractionData.put(codeActionId_, extractionInfo);
        action.setData(codeActionId_);
        return CompletableFuture.completedFuture(List.of(Either.forRight(action)));
    }

    final TreeEvaluator innermostFlworQuery = XQuery.compile("/ancestor::(initialClause|intermediateClause|returnClause)", _parser);
    final TreeEvaluator outermostExpr = XQuery.compile("/ancestor::expr", _parser);
    final String x = """
        let $expr := .
        let $dependentVariables := .//varRef except .//varNameAndType/varRef
        let $dependentVariableStrings := $dependentVariables =!> string()
        let $dependentDeclaration := ./ancestor::varNameAndType/varRef[string() = $dependentVariableStrings][last()]
        let $clauses := ./ancestor::(initialClause|intermediateClause|returnClause)[. follows $dependentDeclaration]
        let $exprs := ./ancestor::expr[. follows $dependentDeclaration]
        return (
            array { $clauses, $exprs },
            array {
                $clauses ! `let ${$variableText} := {$variableText}\n`,
                $exprs ! `let ${$variableText} := ({$variableText}) return\n`
            }
        )
    """;

    Map<ParserRuleContext, ExtractLocationInfo> extractVariableLocationsCache = new HashMap<>();

    public CompletableFuture<ExtractLocationInfo> extractVariableLocations(final ExtractVariableLocationsParams params) {
        final List<Range> ranges = new ArrayList<>();
        final List<String> texts = new ArrayList<>();
        final VarExtractionInfo info = varExtractionData.get(params.actionId());
        final ParserRuleContext selectedContext = info.contextStack.get(params.selectedIndex());
        final XQueryValue flwor = innermostFlworQuery.evaluate(selectedContext);
        final String letBindingWithoutReturn = getLetBindingWithoutReturn(params);
        for (final var location : flwor.sequence) {
            final ParserRuleContext ctx = (ParserRuleContext) location.node;
            final Range ctxRange = getContextRange(ctx);
            ranges.add(ctxRange);
            texts.add(letBindingWithoutReturn);
        }
        final XQueryValue expression = outermostExpr.evaluate(selectedContext);
        final String letBindingWithReturn = getLetBindingWithReturn(params);
        for (final var location: expression.sequence) {
            final ParserRuleContext ctx = (ParserRuleContext) location.node;
            final Range ctxRange = getContextRange(ctx);
            ranges.add(ctxRange);
            texts.add(letBindingWithReturn);
        }

        final ExtractLocationInfo value = new ExtractLocationInfo(ranges, texts);
        extractVariableLocationsCache.put(selectedContext, value);
        return CompletableFuture.completedFuture(value);
    }

    private String getLetBindingWithoutReturn(final ExtractVariableLocationsParams params) {
        final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("let $");
		stringBuilder.append(params.variableName());
		stringBuilder.append(" := ");
		stringBuilder.append(params.variableText());
		stringBuilder.append("\n");
		return stringBuilder.toString();
    }

    private String getLetBindingWithReturn(final ExtractVariableLocationsParams params) {
        final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("let $");
		stringBuilder.append(params.variableName());
		stringBuilder.append(" := (");
		stringBuilder.append(params.variableText());
		stringBuilder.append(") return ");
		return stringBuilder.toString();
    }


    public CompletableFuture<WorkspaceEdit> extractVariable(final ExtractVariableParams params) {
        final Map<String, List<TextEdit>> changes = new HashMap<>();
        final VarExtractionInfo info = varExtractionData.get(params.actionId());
        final ParserRuleContext selectedContext = info.contextStack.get(params.extractedContextIndex());
        final Range selectedContextRange = getContextRange(selectedContext);
        final ExtractLocationInfo varLocInfo = extractVariableLocationsCache.get(selectedContext);
        final var chosenPosition = varLocInfo.getRanges().get(params.chosenPositionIndex()).getStart();
        final var chosenBinding = varLocInfo.getTexts().get(params.chosenPositionIndex());

        final TextEdit varInserted = new TextEdit(selectedContextRange, "$" + params.variableName());
        final TextEdit valueExtracted = new TextEdit(
            new Range(chosenPosition, chosenPosition), chosenBinding
        );
        changes.put(info.uri(), List.of(varInserted, valueExtracted));
        return CompletableFuture.completedFuture(new WorkspaceEdit(changes));
    }
}
