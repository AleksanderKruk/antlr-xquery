package com.github.akruk.antlrxquery.evaluator;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.CharStreams;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.CharStream;
import com.github.akruk.antlrxquery.AntlrXqueryLexer;
import com.github.akruk.antlrxquery.AntlrXqueryParser;
import com.github.akruk.antlrxquery.AxisVisitor;
import com.github.akruk.antlrxquery.AntlrXqueryParser.XqueryContext;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;
import com.github.akruk.antlrxquery.evaluator.values.factories.defaults.XQueryMemoizedValueFactory;
import com.github.akruk.antlrxquery.semanticanalyzer.GrammarManager;
import com.github.akruk.antlrxquery.semanticanalyzer.ModuleManager;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.XQuerySemanticContextManager;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.SemanticFunctionSets;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.XQuerySemanticSymbolManager;
import com.github.akruk.antlrxquery.semanticanalyzer.visitors.AntlrQuerySemanticAnalyzer;
import com.github.akruk.antlrxquery.semanticanalyzer.visitors.CardinalityVisitor;
import com.github.akruk.antlrxquery.semanticanalyzer.visitors.TypeVisitor;
import com.github.akruk.antlrxquery.typesystem.factories.CardinalityFactory;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.BaseCardinalityFactory;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryMemoizedTypeFactory;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryNamedTypeSets;
import com.github.akruk.antlrxquery.typesystem.types.AntlrQuerySequenceType;

public final class XQuery {
    public static XQueryValue evaluateWithMockRoot(
        final ParseTree tree,
        final String xquery,
        final String uri,
        final Parser parser
        )
    {
        return evaluateWithMockRoot(tree, xquery, uri, parser, Map.of());
    }

    public static XQueryValue evaluateWithMockRoot(
        final ParseTree tree,
        final String xquery,
        final String uri,
        final Parser parser,
        final Map<String, XQueryValue> vars)
    {
        final ParserRuleContext root = new ParserRuleContext();
        if (tree != null) {
            root.children = List.of(tree);
            final var originalParent = tree.getParent();
            tree.setParent(root);
            root.setParent((RuleContext) originalParent);
        }
        final XQueryValue evaluated = evaluateWithoutMockRoot(
            root,
            xquery,
            uri,
            parser,
            vars);
        if (tree != null) {
            tree.setParent(null);
        }
        return evaluated;
    }



    public static XQueryValue evaluateWithoutMockRoot(
        final ParseTree tree,
        final String xquery,
        final String uri,
        final Parser parser,
        final Map<String,XQueryValue> vars
        )
    {
        final var xqueryTree = parse(xquery);
        final XQueryMemoizedTypeFactory typeFactory = new XQueryMemoizedTypeFactory(new XQueryNamedTypeSets().all());
        final XQueryValueFactory valueFactory = new XQueryMemoizedValueFactory(typeFactory);
        final ModuleManager moduleManager = new ModuleManager(Set.of());
        final GrammarManager grammarManager = new GrammarManager(Set.of());
        final XQuerySemanticContextManager contextManager = new XQuerySemanticContextManager(typeFactory);
        final Map<String, AntlrQuerySequenceType> varTypes = vars.entrySet().stream().collect(Collectors.toMap(e->e.getKey(), e->e.getValue().type));
        final BaseCardinalityFactory cardinalityFactory = new BaseCardinalityFactory();
        final AntlrQuerySemanticAnalyzer analyzer = new AntlrQuerySemanticAnalyzer(
            parser,
            typeFactory,
            valueFactory,
            new XQuerySemanticSymbolManager(
                typeFactory,
                contextManager,
                SemanticFunctionSets.ALL(typeFactory)
            ),
            null,
            moduleManager,
            grammarManager,
            typeFactory.anyNode(),
            uri,
            varTypes,
            new AxisVisitor(),
            cardinalityFactory,
            new TypeVisitor(typeFactory, new CardinalityVisitor(cardinalityFactory))
            );
        final XQueryEvaluatorVisitor visitor = new XQueryEvaluatorVisitor(
            tree, parser, valueFactory, analyzer, typeFactory, moduleManager, vars);

        final XQueryValue evaluated = visitor.visit(xqueryTree);
        return evaluated;
    }

    public static XQueryValue evaluate(
        final ParseTree tree,
        final String xquery,
        final String uri,
        final Parser parser
        )
    {
        return evaluate(tree, xquery, uri, parser, Map.of());
    }

    public static XQueryValue evaluate(
        final ParseTree tree,
        final String xquery,
        final String baseUri,
        final Parser parser,
        final Map<String, XQueryValue> vars
        )
    {
        return evaluateWithoutMockRoot(tree, xquery, baseUri, parser, vars);
    }

    public interface TreeEvaluator {
        XQueryValue evaluate(final ParseTree tree, Map<String, XQueryValue> vars);
        default XQueryValue evaluate(final ParseTree tree) {
            return evaluate(tree, Map.of());
        }
    }

    public static TreeEvaluator compile(
        final String xquery,
        final String uri,
        final Parser parser
        )
    {
        final var xqueryTree = parse(xquery);
        final XQueryMemoizedTypeFactory typeFactory = new XQueryMemoizedTypeFactory(new XQueryNamedTypeSets().all());
        final XQueryValueFactory valueFactory = new XQueryMemoizedValueFactory(typeFactory);
        final ModuleManager moduleManager = new ModuleManager(Set.of());
        final GrammarManager grammarManager = new GrammarManager(Set.of());
        final XQuerySemanticContextManager contextManager = new XQuerySemanticContextManager(typeFactory);
        final BaseCardinalityFactory cardinalityFactory = new BaseCardinalityFactory();
        final AntlrQuerySemanticAnalyzer analyzer = new AntlrQuerySemanticAnalyzer(
            parser,
            typeFactory,
            valueFactory,
            new XQuerySemanticSymbolManager(
                typeFactory,
                contextManager,
                SemanticFunctionSets.ALL(typeFactory)
            ),
            null,
            moduleManager,
            grammarManager,
            typeFactory.anyNode(),
            uri,
            Map.of(),
            new AxisVisitor(),
            cardinalityFactory,
            new TypeVisitor(typeFactory, new CardinalityVisitor(cardinalityFactory))
            );


        return (tree, variables) -> {
            final XQueryEvaluatorVisitor visitor = new XQueryEvaluatorVisitor(
                tree,
                parser,
                valueFactory,
                analyzer,
                typeFactory,
                moduleManager,
                variables
                );
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
