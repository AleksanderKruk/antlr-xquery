const vscode = require('vscode');
const { LanguageClient, TransportKind } = require('vscode-languageclient/node');

let client;

function activate(context) {
    console.log('Activating AntlrQuery extension...');

    const serverOptions = {
        command: 'java',
        args: ['-jar', context.asAbsolutePath('./server/antlrxquery-language-server.jar')],
        transport: TransportKind.stdout,
        options: { cwd: process.cwd() }
    };

    const clientOptions = {
        documentSelector: [{ scheme: 'file', language: 'antlrquery' }],
        synchronize: {
            fileEvents: vscode.workspace.createFileSystemWatcher('**/.antlrquery')
        },
        outputChannel: vscode.window.createOutputChannel('AntlrQuery Language Server')
    };

    client = new LanguageClient(
        'antlrQueryLanguageServer',
        'AntlrQuery Language Server',
        serverOptions,
        clientOptions
    );

    client.onDidChangeState(event => {
        console.log(`LSP server state: ${event.newState}`);
    });

    try {
        client.start();
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
    return client.stop();
}

module.exports = {
    activate,
    deactivate
};
