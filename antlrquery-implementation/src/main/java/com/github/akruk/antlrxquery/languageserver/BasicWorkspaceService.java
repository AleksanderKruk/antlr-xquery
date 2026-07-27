package com.github.akruk.antlrxquery.languageserver;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.WorkspaceService;

public class BasicWorkspaceService implements WorkspaceService {
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
