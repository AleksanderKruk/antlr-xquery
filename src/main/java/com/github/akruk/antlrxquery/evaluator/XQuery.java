package com.github.akruk.antlrxquery.evaluator;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.CharStreams;

import java.util.List;

import org.antlr.v4.runtime.CharStream;
import com.github.akruk.antlrxquery.AntlrXqueryLexer;
import com.github.akruk.antlrxquery.AntlrXqueryParser;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;
import com.github.akruk.antlrxquery.evaluator.values.factories.defaults.XQueryMemoizedValueFactory;
import com.github.akruk.antlrxquery.semanticanalyzer.XQuerySemanticAnalyzer;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.XQuerySemanticContextManager;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.defaults.XQuerySemanticFunctionManager;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryMemoizedTypeFactory;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryNamedTypeSets;

public final class XQuery {
  public static XQueryValue evaluate(final ParseTree tree, final String xquery, final Parser parser) {
    final CharStream characters = CharStreams.fromString(xquery);
    final var xqueryLexer = new AntlrXqueryLexer(characters);
    final var xqueryTokens = new CommonTokenStream(xqueryLexer);
    final var xqueryParser = new AntlrXqueryParser(xqueryTokens);
    final var xqueryTree = xqueryParser.xquery();
    final ParserRuleContext root = new ParserRuleContext();
    if (tree != null) {
        root.children = List.of(tree);
        tree.setParent(root);
    }
    final XQueryMemoizedTypeFactory typeFactory = new XQueryMemoizedTypeFactory(new XQueryNamedTypeSets().all());
    final XQueryValueFactory valueFactory = new XQueryMemoizedValueFactory(typeFactory);
    final XQuerySemanticAnalyzer analyzer = new XQuerySemanticAnalyzer(parser,
        new XQuerySemanticContextManager(),
        typeFactory,
        valueFactory,
        new XQuerySemanticFunctionManager(typeFactory),
        null
        );
    final XQueryEvaluatorVisitor visitor = new XQueryEvaluatorVisitor(root, parser, analyzer, typeFactory);
    final XQueryValue evaluated = visitor.visit(xqueryTree);
    if (tree != null) {
        tree.setParent(null);
    }
    return evaluated;
  }
}
