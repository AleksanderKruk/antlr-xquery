package com.github.akruk.antlrxquery.evaluator;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.CharStreams;

import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.CharStream;
import com.github.akruk.antlrxquery.AntlrXqueryLexer;
import com.github.akruk.antlrxquery.AntlrXqueryParser;
import com.github.akruk.antlrxquery.AntlrXqueryParser.XqueryContext;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;
import com.github.akruk.antlrxquery.evaluator.values.factories.defaults.XQueryMemoizedValueFactory;
import com.github.akruk.antlrxquery.semanticanalyzer.GrammarManager;
import com.github.akruk.antlrxquery.semanticanalyzer.ModuleManager;
import com.github.akruk.antlrxquery.semanticanalyzer.XQuerySemanticAnalyzer;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.XQuerySemanticContextManager;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.SemanticFunctionSets;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.XQuerySemanticSymbolManager;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryMemoizedTypeFactory;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryNamedTypeSets;

public final class XQuery {
    public static XQueryValue evaluateWithMockRoot(
        final ParseTree tree, final String xquery, final Parser parser)
    {
        final var xqueryTree = parse(xquery);
        final ParserRuleContext root = new ParserRuleContext();
        if (tree != null) {
            root.children = List.of(tree);
            final var originalParent = tree.getParent();
            tree.setParent(root);
            root.setParent((RuleContext) originalParent);
        }
        final XQueryMemoizedTypeFactory typeFactory = new XQueryMemoizedTypeFactory(new XQueryNamedTypeSets().all());
        final XQueryValueFactory valueFactory = new XQueryMemoizedValueFactory(typeFactory);
        final ModuleManager moduleManager = new ModuleManager(Set.of());
        final GrammarManager grammarManager = new GrammarManager(Set.of());
        final XQuerySemanticAnalyzer analyzer = new XQuerySemanticAnalyzer(
            parser,
            new XQuerySemanticContextManager(typeFactory),
            typeFactory,
            valueFactory,
            new XQuerySemanticSymbolManager(
                typeFactory,
                SemanticFunctionSets.ALL(typeFactory)),
            null,
            moduleManager,
            grammarManager,
            typeFactory.anyNode()
            );
        final XQueryEvaluatorVisitor visitor = new XQueryEvaluatorVisitor(root, parser, analyzer, typeFactory, moduleManager);
        final XQueryValue evaluated = visitor.visit(xqueryTree);
        if (tree != null) {
            tree.setParent(null);
        }
        return evaluated;
    }


    public static XQueryValue evaluateWithoutMockRoot(
        final ParseTree tree, final String xquery, final Parser parser)
    {
        final var xqueryTree = parse(xquery);
        final XQueryMemoizedTypeFactory typeFactory = new XQueryMemoizedTypeFactory(new XQueryNamedTypeSets().all());
        final XQueryValueFactory valueFactory = new XQueryMemoizedValueFactory(typeFactory);
        final ModuleManager moduleManager = new ModuleManager(Set.of());
        final GrammarManager grammarManager = new GrammarManager(Set.of());
        final XQuerySemanticAnalyzer analyzer = new XQuerySemanticAnalyzer(
            parser,
            new XQuerySemanticContextManager(typeFactory),
            typeFactory,
            valueFactory,
            new XQuerySemanticSymbolManager(typeFactory, SemanticFunctionSets.ALL(typeFactory)),
            null,
            moduleManager,
            grammarManager,
            typeFactory.anyNode()
            );
        final XQueryEvaluatorVisitor visitor = new XQueryEvaluatorVisitor(
            tree, parser, analyzer, typeFactory, moduleManager);
        final XQueryValue evaluated = visitor.visit(xqueryTree);
        return evaluated;
    }

    public static XQueryValue evaluate(final ParseTree tree, final String xquery, final Parser parser)
    {
        return evaluateWithoutMockRoot(tree, xquery, parser);
    }

    public interface TreeEvaluator {
        XQueryValue evaluate(final ParseTree tree);
    }

    public static TreeEvaluator compile(final String xquery, final Parser parser)
    {
        final var xqueryTree = parse(xquery);
        final XQueryMemoizedTypeFactory typeFactory = new XQueryMemoizedTypeFactory(new XQueryNamedTypeSets().all());
        final XQueryValueFactory valueFactory = new XQueryMemoizedValueFactory(typeFactory);
        final ModuleManager moduleManager = new ModuleManager(Set.of());
        final GrammarManager grammarManager = new GrammarManager(Set.of());
        final XQuerySemanticAnalyzer analyzer = new XQuerySemanticAnalyzer(
            parser,
            new XQuerySemanticContextManager(typeFactory),
            typeFactory,
            valueFactory,
            new XQuerySemanticSymbolManager(typeFactory, SemanticFunctionSets.ALL(typeFactory)),
            null,
            moduleManager,
            grammarManager,
            typeFactory.anyNode()
            );


        return tree -> {
            final XQueryEvaluatorVisitor visitor = new XQueryEvaluatorVisitor(
                tree, parser, valueFactory, analyzer, typeFactory, moduleManager);
            final XQueryValue evaluated = visitor.visit(xqueryTree);
			return evaluated;
        };
    }


    public static XqueryContext parse(final String xquery) {
        final CharStream characters = CharStreams.fromString(xquery);
        final var xqueryLexer = new AntlrXqueryLexer(characters);
        final var xqueryTokens = new CommonTokenStream(xqueryLexer);
        final var xqueryParser = new AntlrXqueryParser(xqueryTokens);
        final var xqueryTree = xqueryParser.xquery();
        return xqueryTree;
    }


}
