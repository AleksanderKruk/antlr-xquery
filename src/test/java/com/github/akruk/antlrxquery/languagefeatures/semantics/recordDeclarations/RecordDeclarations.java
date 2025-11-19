package com.github.akruk.antlrxquery.languagefeatures.semantics.recordDeclarations;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.semantics.SemanticTestsBase;

public class RecordDeclarations extends SemanticTestsBase {
    @Test
    void unorderedDeclarations() {
        assertType("""
            module m;
            declare record m:A(b as m:B);
            declare record m:B(a as string);
                """, null);

    }

    @Test
    void selfRecursion() {
        assertType("""
            module m;
            declare record m:A(a as m:A);
                """, null);

    }

    @Test
    void indirectRecursion() {
        assertType("""
            module m;
            declare record m:A(a as m:B);
            declare record m:B(a as m:A?);
                """, null);
        assertType("""
            module m;
            declare record m:A(a as m:B);
            declare record m:B(a as m:A*);
                """, null);
        // assertErrors("""
        //     module m;
        //     declare record m:A(a as m:B);
        //     declare record m:B(a as m:A);
        //         """);
        // assertErrors("""
        //     module m;
        //     declare record m:A(a as m:B);
        //     declare record m:B(a as m:A+);
        //         """);

    }

    @Test
    void A() {
        assertNoErrors("""
module lsp;

declare record lsp:Position(
	line as number,
	character as number
);

declare type lsp:PositionEncodingKind as enum('utf-8', 'utf-16', 'utf-32');

declare record lsp:Range(
	start as lsp:Position,
	end as lsp:Position
);

declare function lsp:start-position($node as node()?) as lsp:Position? {
    $node ! lsp:Position(
        line := antlr:line() treat as number - 1, (:TODO: remove treatment after grained call return type analysis :)
        character := antlr:pos() treat as number
    )
};

declare function lsp:end-position($node as node()?) as lsp:Position? {
    if ($node => empty()) {
        (:TODO: remove treatment after grained call return type analysis :)
        let $start-line := antlr:line($node) treat as number - 1
        let $start-pos := antlr:pos($node) treat as number
        let $string-node := $node=>string()
        let $new-lines := $string-node=>characters()=>index-of("\n")
        let $additional-lines := $new-lines=>count()
        let $last-line-index := $new-lines[$additional-lines]
        let $last-line-length := string-length(
                if ($last-line-index=>exists())
                    then substring($string-node, $new-lines[$additional-lines] treat as number)
                    else $string-node
            )
        return lsp:Position(
            line := $start-line + $additional-lines,
            character := $start-pos + $last-line-length
        )
    }
};

declare function lsp:range($node as node()?) as lsp:Range? {
    if ($node) {
        lsp:Range(
            start := lsp:start-position($node) treat as lsp:Position,
            end := lsp:end-position($node) treat as lsp:Position
        )
    }
};

                """);
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
