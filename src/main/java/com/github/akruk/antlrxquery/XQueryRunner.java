package com.github.akruk.antlrxquery;

import org.antlr.v4.runtime.CommonTokenStream;

import com.github.akruk.antlrxquery.contextmanagement.semanticcontext.baseimplementation.XQueryBaseSemanticContextManager;
import com.github.akruk.antlrxquery.evaluator.XQueryEvaluatorVisitor;
import com.github.akruk.antlrxquery.semanticanalyzer.XQuerySemanticAnalyzer;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.defaults.XQueryBaseSemanticFunctionCaller;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryEnumTypeFactory;
import com.github.akruk.antlrxquery.values.factories.defaults.XQueryMemoizedValueFactory;

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
        final var tree = parser.xquery();
        final var analyzer = new XQuerySemanticAnalyzer(
            parser,
            new XQueryBaseSemanticContextManager(),
            new XQueryEnumTypeFactory(),
            new XQueryMemoizedValueFactory(),
            new XQueryBaseSemanticFunctionCaller());
        final var evaluator = new XQueryEvaluatorVisitor(tree, parser);
        analyzer.visit(tree);
        evaluator.visit(tree);
    }
}
