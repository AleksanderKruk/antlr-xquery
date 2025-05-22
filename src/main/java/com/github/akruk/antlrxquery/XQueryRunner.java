package com.github.akruk.antlrxquery;

import org.antlr.v4.runtime.CommonTokenStream;

import com.github.akruk.antlrxquery.values.XQueryValue;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;

public final class XQueryRunner {
    private XQueryRunner() {

    }

    public static void main(String[] args) {
        final CharStream characters = CharStreams.fromString(args[0]);
        final var lexer = new AntlrXqueryLexer(characters);
        final var tokens = new CommonTokenStream(lexer);
        final var parser = new AntlrXqueryParser(tokens);
        final var evaluator = new AntlrXqueryParserBaseVisitor<XQueryValue>();
        evaluator.visit(null);
    }
}
