package com.github.akruk.antlrxquery.languageserver;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.*;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    private static final List<String> tokenLegend = List.of(
        "variable", "parameter", "function", "type", "string", "property", "decorator");

    private final BasicTextDocumentService textDocumentService = new BasicTextDocumentService(tokenLegend);
    private final BasicWorkspaceService workspaceService = new BasicWorkspaceService();

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
        workspaceService.setClient(client);
        System.err.println("[connect] LanguageClient connected");
    }

    record ExtractVariableParams(
        Integer chosenPositionIndex,
        Integer extractedContextIndex,
        Integer actionId,
        String variableName)
    {}

    @JsonRequest("custom/extractVariable")
    public CompletableFuture<WorkspaceEdit> extractVariable(ExtractVariableParams params) {
        return textDocumentService.extractVariable(params);
    }

    record ExtractVariableLocationsParams(
        Range range,
        Integer selectedIndex,
        Integer actionId,
        String variableText,
        String variableName)
    {}

    public static class ExtractLocationInfo {
        private List<Range> ranges;
		public ExtractLocationInfo(List<Range> ranges, List<String> texts) {
			this.ranges = ranges;
			this.texts = texts;
		}
		public List<Range> getRanges() {
			return ranges;
		}
		public void setRanges(List<Range> ranges) {
			this.ranges = ranges;
		}
		private List<String> texts;
		public List<String> getTexts() {
			return texts;
		}
		public void setTexts(List<String> texts) {
			this.texts = texts;
		}
    }

    @JsonRequest("custom/extractVariableLocations")
    public CompletableFuture<ExtractLocationInfo> extractVariableLocations(ExtractVariableLocationsParams params) {
        return textDocumentService.extractVariableLocations(params);
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(final InitializeParams params)
    {
        System.err.println("[initialize] Server initialized");
        final ServerCapabilities capabilities = new ServerCapabilities();
        List<Path> workspacePaths = params.getWorkspaceFolders()
            .stream()
            .map(t->Paths.get(URI.create(t.getUri())))
            .toList();
        textDocumentService.setModulePaths(workspacePaths);
        System.err.println("Module paths from workspace: " + workspacePaths);

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

        workspacePaths.forEach(directoryPath->{
            try {
                Files.walk(directoryPath).forEach(p -> {
                    try {
                        if (p.toFile().isFile() && p.endsWith(".antlrquery"))
                        {
                            textDocumentService.parseAndAnalyze(p.toAbsolutePath().toString(), Files.readString(p));
                        }
                    } catch (IOException e) {
                        e.printStackTrace(System.err);
                    }

                });
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        });
        textDocumentService.setClient(client);


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
