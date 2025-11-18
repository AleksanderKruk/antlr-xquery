package com.github.akruk.antlrxquery.languagefeatures.semantics.recordDeclarations;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.semantics.SemanticTestsBase;

public class RecordDeclarations extends SemanticTestsBase {
    @Test
    void unorderedDeclarations() {
        assertType("""
            module m;
            declare record A(b as B);
            declare record B(a as string);

                """, null);

    }

    // @Test
    // void many() {
    //     assertType("""
    // module lsp;
    // declare type lsp:LSPAny as number;

    // declare record lsp:Message(
    //     jsonrpc as string,
    //     *
    // );

    // declare record lsp:RequestMessage(
    //     (: lsp:Message :)
    //     jsonrpc as string,
    //     (: lsp:RequestMessage :)
    //     id as (number|string),
    //     method as string,
    //     params? as (array(*)|map(*))
    // );

    // declare record lsp:ResponseMessage(
    //     (: lsp:Message :)
    //     jsonrpc as string,
    //     (: lsp:ResponseMessage :)
    //     id as (number|string)?,
    //     result? as lsp:LSPAny?,
    //     error? as lsp:ResponseError
    // );

    // declare record lsp:ResponseError(
    //     code as number,
    //     message as string,
    //     data? as lsp:LSPAny?
    // );

    // declare record lsp:NotificationMessage(
    //     (: lsp:Message :)
    //     jsonrpc as string,
    //     (: lsp:NotificationMessage :)
    //     method as string,
    //     params? as (array(*)|map(*))
    // );

    // declare record lsp:CancelParams(
    //     id as (number|string)
    // );

    // declare record lsp:ProgressParams(
    //     token as lsp:ProgressToken,
    //     value as item()*
    // );

    // declare record lsp:HoverParams(
    //     textDocument as string,
    //     position as false
    // );

    // declare record lsp:HoverResult(
    //     value as string
    // );

    // declare record lsp:RegularExpressionsClientCapabilities(
    //     engine as string,
    //     version? as string
    // );

    // declare record lsp:Position(
    //     line as number,
    //     character as number
    // );

    // declare record lsp:Range(
    //     start as lsp:Position,
    //     end as lsp:Position
    // );

    // declare record lsp:TextDocumentItem(
    //     uri as lsp:DocumentUri,
    //     languageId as string,
    //     version as number,
    //     text as string
    // );

    // declare record lsp:TextDocumentIdentifier(
    //     uri as lsp:DocumentUri,
    //     *
    // );

    // declare record lsp:VersionedTextDocumentIdentifier(
    //     (: lsp:TextDocumentIdentifier :)
    //     uri as lsp:DocumentUri,
    //     (: lsp:VersionedTextDocumentIdentifier :)
    //     version as number
    // );

    // declare record lsp:WillSaveTextDocumentParams(
    //     textDocument as lsp:TextDocumentIdentifier,
    //     reason as lsp:TextDocumentSaveReason
    // );

    // declare record lsp:SaveOptions(
    //     includeText? as boolean
    // );

    //             """, null);

    // }


}
