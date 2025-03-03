package com.github.akruk.antlrxquery;

import org.antlr.v4.runtime.CommonTokenStream;

import com.github.akruk.antlrxquery.values.XQueryValue;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;

public final class XQueryRunner {
    private XQueryRunner() {

    }

    public static void main(String[] args) {
        CharStream characters = CharStreams.fromString(args[0]);
        var lexer = new AntlrXqueryLexer(characters);
        var tokens = new CommonTokenStream(lexer);
        var parser = new AntlrXqueryParser(tokens);
        var evaluator = new AntlrXqueryParserBaseVisitor<XQueryValue>();
        evaluator.visit(null);
        System.out.println("Hello World!");
    }
}
