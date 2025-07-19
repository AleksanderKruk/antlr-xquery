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

public final class XQuery {
  public static XQueryValue evaluate(ParseTree tree, String xquery, Parser parser) {
    CharStream characters = CharStreams.fromString(xquery);
    var xqueryLexer = new AntlrXqueryLexer(characters);
    var xqueryTokens = new CommonTokenStream(xqueryLexer);
    var xqueryParser = new AntlrXqueryParser(xqueryTokens);
    var xqueryTree = xqueryParser.xquery();
    ParserRuleContext root = new ParserRuleContext();
    if (tree != null) {
        root.children = List.of(tree);
        tree.setParent(root);
    }
    XQueryEvaluatorVisitor visitor = new XQueryEvaluatorVisitor(root, parser);
    XQueryValue evaluated = visitor.visit(xqueryTree);
    if (tree != null) {
        tree.setParent(null);
    }
    return evaluated;
  }
}
