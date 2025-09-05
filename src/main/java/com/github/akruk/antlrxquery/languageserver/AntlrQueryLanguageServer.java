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



    @Override
    public void connect(final LanguageClient client)
    {
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

        capabilities.setHoverProvider(true);

        final RenameOptions renameOptions = new RenameOptions();
        renameOptions.setPrepareProvider(true);
        capabilities.setRenameProvider(renameOptions);

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
}
