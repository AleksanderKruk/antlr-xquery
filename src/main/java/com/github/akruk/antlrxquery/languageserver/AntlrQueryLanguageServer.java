package com.github.akruk.antlrxquery.languageserver;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.*;
import java.util.concurrent.CompletableFuture;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Language Server for AntlrQuery, implementing LSP (Language Server Protocol).
 * Logs all operations to a specified file.
 */
public class AntlrQueryLanguageServer implements LanguageServer, LanguageClientAware {
    private LanguageClient client;
    private static PrintWriter logWriter; // Writer for logging to file

    public static void main(String[] args) {
        InputStream in = System.in;
        OutputStream out = System.out;

        // Initialize file logging
        String logFilePath = "D:\\Programowanie\\antlr-xquery\\antlrqueryextension\\antlrquery\\log.txt";
        try {
            File logFile = new File(logFilePath);
            logFile.getParentFile().mkdirs(); // Create directory if it doesn't exist
            logWriter = new PrintWriter(new FileWriter(logFile, true), true); // Append mode
            log("=== LSP Server Started ===");
        } catch (IOException e) {
            System.err.println("Failed to open log file: " + e.getMessage());
            e.printStackTrace();
        }

        AntlrQueryLanguageServer server = new AntlrQueryLanguageServer();
        Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, in, out);
        server.connect(launcher.getRemoteProxy());
        launcher.startListening();
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        log("Initializing LSP server");
        ServerCapabilities capabilities = new ServerCapabilities();
        capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
        capabilities.setCompletionProvider(new CompletionOptions());
        return CompletableFuture.completedFuture(new InitializeResult(capabilities));
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        log("Shutdown requested");
        return CompletableFuture.completedFuture(new Object());
    }

    @Override
    public void exit() {
        log("Exit requested");
        if (logWriter != null) {
            logWriter.close(); // Close the log file
        }
        System.exit(0);
    }

    @Override
    public TextDocumentService getTextDocumentService() {
        return new AntlrQueryTextDocumentService();
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        return new AntlrQueryWorkspaceService();
    }

    @Override
    public void connect(LanguageClient client) {
        log("Connected to LSP client");
        this.client = client;
    }

    /**
     * Logs a message with timestamp to both console and file.
     * @param message The message to log.
     */
    private static void log(String message)
    {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logMessage = String.format("[%s] %s", timestamp, message);
        System.out.println(logMessage); // Log to console
        if (logWriter != null) {
            logWriter.println(logMessage); // Log to file
        }
    }


    // Custom TextDocumentService implementation
    private static class AntlrQueryTextDocumentService implements TextDocumentService {
        @Override
        public void didOpen(DidOpenTextDocumentParams params) {
            String uri = params.getTextDocument().getUri();
            log("Document opened: " + uri);
        }

        @Override
        public void didChange(DidChangeTextDocumentParams params) {
            String uri = params.getTextDocument().getUri();
            int version = params.getTextDocument().getVersion();
            String changes = params.getContentChanges().toString();
            // client.publishDiagnostics(null);
            log(String.format(
                "Document changed: %s (version: %d, changes: %s)",
                uri, version, changes
            ));
        }

        @Override
        public void didClose(DidCloseTextDocumentParams params) {
            String uri = params.getTextDocument().getUri();
            log("Document closed: " + uri);
        }

        @Override
        public void didSave(DidSaveTextDocumentParams params)
        {
            String uri = params.getTextDocument().getUri();
            log("Document saved: " + uri);
        }



    }

    // Custom WorkspaceService implementation
    private static class AntlrQueryWorkspaceService implements WorkspaceService {
        @Override
        public void didChangeConfiguration(DidChangeConfigurationParams params) {
            log("Configuration changed: " + params.getSettings());
        }

        @Override
        public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
            log("Watched files changed: " + params.getChanges());
        }
    }
}
