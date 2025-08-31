// package com.github.akruk.antlrxquery;

// import org.eclipse.lsp4j.*;
// import org.eclipse.lsp4j.services.*;
// import org.eclipse.lsp4j.jsonrpc.Launcher;

// import java.io.InputStream;
// import java.io.OutputStream;
// import java.util.concurrent.CompletableFuture;

// public class MyLanguageServer implements LanguageServer, LanguageClientAware {

//     private LanguageClient client;
//     private final TextDocumentService textDocumentService = new MyTextDocumentService();
//     private final WorkspaceService workspaceService = new MyWorkspaceService();

//     @Override
//     public CompletableFuture<InitializeResult> initialize(InitializeParams params)
//     {
//         ServerCapabilities capabilities = new ServerCapabilities();
//         capabilities.setTextDocumentSync(TextDocumentSyncKind.Full); // lub Incremental
//         capabilities.setCompletionProvider(new CompletionOptions(true, List.of(".")));
//         capabilities.setHoverProvider(true);
//         return CompletableFuture.completedFuture(new InitializeResult(capabilities));
//     }



//     @Override
//     public CompletableFuture<Object> shutdown() {
//         return CompletableFuture.completedFuture(null);
//     }

//     @Override
//     public void exit() {
//         System.exit(0);
//     }

//     @Override
//     public TextDocumentService getTextDocumentService() {
//         return textDocumentService;
//     }

//     @Override
//     public WorkspaceService getWorkspaceService() {
//         return workspaceService;
//     }

//     @Override
//     public void connect(LanguageClient client) {
//         this.client = client;
//     }

//     public static void main(String[] args)
//     {
//         InputStream in = System.in;
//         OutputStream out = System.out;
//         MyLanguageServer server = new MyLanguageServer();
//         Launcher<LanguageClient> launcher = Launcher.createLauncher(server, LanguageClient.class, in, out);
//         server.connect(launcher.getRemoteProxy());
//         launcher.startListening();
//     }


// }
