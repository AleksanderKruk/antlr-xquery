const vscode = require('vscode');
const { LanguageClient, TransportKind } = require('vscode-languageclient/node');

let client; // Language client instance

function activate(context) {
    console.log('Activating AntlrQuery extension...');

    // Server configuration
    const serverOptions = {
        command: 'java',
        args: ['-jar', context.asAbsolutePath('./server/antlrxquery-language-server.jar')],
        transport: TransportKind.stdout,
        options: { cwd: process.cwd() } // Set working directory
    };

    // Client configuration
    const clientOptions = {
        documentSelector: [{ scheme: 'file', language: 'antlrquery' }], // File types to handle
        synchronize: {
            fileEvents: vscode.workspace.createFileSystemWatcher('**/.antlrquery') // Watch for file changes
        },
        outputChannel: vscode.window.createOutputChannel('AntlrQuery Language Server') // Log channel
    };

    // Create language client
    client = new LanguageClient(
        'antlrQueryLanguageServer',
        'AntlrQuery Language Server',
        serverOptions,
        clientOptions
    );

    // Handle state changes
    client.onDidChangeState(event => {
        console.log(`LSP server state: ${event.newState}`);
        if (event.newState === 2) { // 2 means "Stopped"
            vscode.window.showErrorMessage('LSP server stopped');
        }
    });

    try {
        client.start(); // Start the language client
        console.log('LSP server started');
    } catch (err) {
        console.error('Error starting LSP server:', err);
        vscode.window.showErrorMessage(`Failed to start LSP server: ${err.message}`);
    }

    console.log('AntlrQuery extension activated');
}

function deactivate() {
    if (!client) {
        return undefined;
    }
    return client.stop(); // Stop the language client on deactivation
}

module.exports = {
    activate,
    deactivate
};
