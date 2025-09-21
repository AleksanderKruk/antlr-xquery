package com.github.akruk.antlrxquery.languageserver;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.*;

import java.util.List;
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

    private static final List<String> tokenLegend = List.of("variable", "parameter", "function", "type");

    private final BasicTextDocumentService textDocumentService = new BasicTextDocumentService(tokenLegend);
    private final com.github.akruk.antlrxquery.languageserver.BasicWorkspaceService workspaceService = new com.github.akruk.antlrxquery.languageserver.BasicWorkspaceService();

    private LanguageClient client;

    @Override
    public void initialized(InitializedParams params)
    {
        client.logMessage(new MessageParams(MessageType.Info, "AntlrQuery LSP initialized"));
        return;
    }


    @Override
    public void connect(final LanguageClient client)
    {
        this.client = client;
        textDocumentService.setClient(client);
        workspaceService.setClient(client);
        System.err.println("[connect] LanguageClient connected");
    }

    // record ExtractVariableParams(TextDocumentIdentifier textDocument, Range range, String variableName)
    // {}

    // @JsonRequest("custom/extractVariable")
    // public CompletableFuture<WorkspaceEdit> extractVariable(ExtractVariableParams params) {
    //     return null;
    // }



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

        capabilities.setHoverProvider(true);

        final RenameOptions renameOptions = new RenameOptions();
        renameOptions.setPrepareProvider(true);
        capabilities.setRenameProvider(renameOptions);

        final InlayHintRegistrationOptions inlayHintOptions = new InlayHintRegistrationOptions();
        inlayHintOptions.setResolveProvider(false);
        capabilities.setInlayHintProvider(inlayHintOptions);

        capabilities.setDefinitionProvider(true);

        // capabilities.setCompletionProvider(new CompletionOptions());

        capabilities.setCodeActionProvider(true);

        CodeActionOptions options = new CodeActionOptions();
        options.setResolveProvider(true);
        capabilities.setCodeActionProvider(options);



        return CompletableFuture.completedFuture(new InitializeResult(capabilities));
    }

    private boolean shutdownRequested = false;

    @Override
    public CompletableFuture<Object> shutdown() {
        shutdownRequested = true;
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void exit() {
        if (shutdownRequested) {
            System.err.println("[exit] Server exiting");
            System.exit(0);
        } else {
            System.err.println("[exit] Exit called before shutdown");
            System.exit(1);
        }
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
}
