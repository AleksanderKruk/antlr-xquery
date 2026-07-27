const vscode = require('vscode');
const { LanguageClient, TransportKind } = require('vscode-languageclient/node');

let client;
let previewDecorationType = vscode.window.createTextEditorDecorationType({
    after: {
        color: 'gray',
        margin: '0 1em 0 0'
    }
});

function activate(context) {
    console.log('Activating AntlrQuery extension...');

    const disposable = vscode.commands.registerCommand(
        'extension.selectExtractionTarget',
        async (ranges, indices, actionId) => {
            actionId = Number.parseInt(actionId);
            const editor = vscode.window.activeTextEditor;
            if (!editor) return;

            const document = editor.document;
            const contextStack = ranges.map(range => document.getText(new vscode.Range(range.start, range.end)));

            const selected = await vscode.window.showQuickPick(contextStack, {
                placeHolder: 'Select extraction target'
            });

            if (selected) {
                const selectionIndex = contextStack.indexOf(selected);
                const extractedContextIndex = indices[selectionIndex];
                const selectedRange = ranges[selectionIndex];

                const { ranges: rawRanges, texts } = await client.sendRequest('custom/extractVariableLocations', {
                    range: selectedRange,
                    selectedIndex: extractedContextIndex,
                    actionId: actionId,
                    variableText: selected,
                    variableName: 'extractedValue'
                });

                const quickPick = getPositionPicker(rawRanges, texts, actionId, selected, extractedContextIndex);
                quickPick.show();
            }
        }
    );
    context.subscriptions.push(disposable);

    // Konfiguracja LSP
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

function getPositionPicker(ranges, texts, actionId, variableText, extractedContextIndex) {
    const quickPick = vscode.window.createQuickPick();
    let i = 0;
    quickPick.items = ranges.map(r => ({
        label: `[line=${r.start.line}; character=${r.start.character} -> line=${r.end.line}; character=${r.end.character}]`,
        description: `[line=${r.start.line}; character=${r.start.character} -> line=${r.end.line}; character=${r.end.character}]`,
        text: texts[i],
        range: r,
        index: i++
    }));
    quickPick.placeholder = 'Select location of the extracted variable';

    quickPick.onDidChangeActive(([active]) => {
        if (active) {
            showPreviewDecoration(active.range, active.text);
        }
    });

    quickPick.onDidHide(() => {
        clearPreviewDecoration();
    });

    quickPick.onDidAccept(async () => {
        const selected = quickPick.selectedItems[0];
        clearPreviewDecoration();
        quickPick.dispose();

        const message = {
            chosenPositionIndex: selected.index,
            extractedContextIndex,
            actionId,
            variableName: 'extractedValue'
        };

        const { changes } = await client.sendRequest('custom/extractVariable', message);
        const workspaceEdit = new vscode.WorkspaceEdit();

        for (const fileUri in changes) {
            for (const edit of changes[fileUri]) {
                workspaceEdit.replace(vscode.Uri.parse(fileUri), edit.range, edit.newText);
            }
        }

        await vscode.workspace.applyEdit(workspaceEdit);
    });

    return quickPick;
}

function showPreviewDecoration(range, text) {
    const editor = vscode.window.activeTextEditor;
    if (!editor) return;

    const decoration = {
        range: new vscode.Range(range.start, range.start),
        renderOptions: {
            after: {
                contentText: `${text} → `,
            }
        },
    };

    editor.setDecorations(previewDecorationType, [decoration]);
}

function clearPreviewDecoration() {
    const editor = vscode.window.activeTextEditor;
    if (editor) {
        editor.setDecorations(previewDecorationType, []);
    }
}

function deactivate() {
    if (!client) return undefined;
    return client.stop();
}

module.exports = {
    activate,
    deactivate
};
