package com.github.akruk.antlrquery.evaluator;

import com.github.akruk.antlrquery.languageserver.DiagnosticMessageCreator;
import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver;
import com.github.akruk.antlrquery.semanticanalyzer.visitors.*;
import com.github.akruk.antlrquery.typesystem.factories.CardinalityFactory;
import com.github.akruk.antlrquery.typesystem.factories.defaults.MemoizedCardinalityFactory;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.CharStreams;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.CharStream;
import com.github.akruk.antlrquery.AntlrQueryLexer;
import com.github.akruk.antlrquery.AntlrQueryParser;
import com.github.akruk.antlrquery.AxisVisitor;
import com.github.akruk.antlrquery.AntlrQueryParser.XqueryContext;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;
import com.github.akruk.antlrquery.evaluator.values.factories.AntlrQueryValueFactory;
import com.github.akruk.antlrquery.evaluator.values.factories.defaults.AntlrQueryMemoizedValueFactory;
import com.github.akruk.antlrquery.semanticanalyzer.GrammarManager;
import com.github.akruk.antlrquery.semanticanalyzer.ModuleManager;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.AntlrQuerySemanticContextManager;
import com.github.akruk.antlrquery.semanticanalyzer.semanticfunctioncaller.SemanticFunctionSets;
import com.github.akruk.antlrquery.semanticanalyzer.semanticfunctioncaller.SemanticSymbolManager;
import com.github.akruk.antlrquery.typesystem.factories.defaults.MemoizedTypeFactory;
import com.github.akruk.antlrquery.typesystem.factories.defaults.AntlrQueryNamedTypeSets;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;

@DefaultQualifier(NonNull.class)
public final class AntlrQuery {
    public static AntlrQueryValue evaluateWithMockRoot(
        final ParseTree tree,
        final String xquery,
        final String uri,
        final Parser parser
        )
    {
        return evaluateWithMockRoot(tree, xquery, uri, parser, Map.of());
    }

    public static AntlrQueryValue evaluateWithMockRoot(
        final ParseTree tree,
        final String xquery,
        final String uri,
        final Parser parser,
        final Map<String, AntlrQueryValue> vars)
    {
        final ParserRuleContext root = new ParserRuleContext();
        if (tree != null) {
            root.children = List.of(tree);
            final var originalParent = tree.getParent();
            tree.setParent(root);
            root.setParent((RuleContext) originalParent);
        }
        final AntlrQueryValue evaluated = evaluateWithoutMockRoot(
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



    public static AntlrQueryValue evaluateWithoutMockRoot(
        final ParseTree tree,
        final String xquery,
        final String uri,
        final Parser parser,
        final Map<String, AntlrQueryValue> vars
        )
    {
        final var xqueryTree = parse(xquery);
        final MemoizedTypeFactory typeFactory = new MemoizedTypeFactory(new AntlrQueryNamedTypeSets().all(), Map.of());
        final AntlrQueryValueFactory valueFactory = new AntlrQueryMemoizedValueFactory(typeFactory);
        final ModuleManager moduleManager = new ModuleManager(Set.of());
        final GrammarManager grammarManager = new GrammarManager(Set.of());
        final AntlrQuerySemanticContextManager contextManager = new AntlrQuerySemanticContextManager(typeFactory);
        final Map<String, AntlrQuerySequenceType> varTypes = vars.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e->e.getValue().type));
        final CardinalityFactory cardinalityFactory = new MemoizedCardinalityFactory();
        final NumericRangeVisitor numericRangeVisitor = new NumericRangeVisitor();
        final CardinalityVisitor cardinalityVisitor = new CardinalityVisitor(cardinalityFactory);
        final ItemTypeVisitor itemTypeVisitor = new ItemTypeVisitor(cardinalityVisitor, numericRangeVisitor, typeFactory);
        final TypeVisitor typeVisitor = new TypeVisitor(typeFactory, cardinalityVisitor, itemTypeVisitor);
        final AntlrQuerySemanticAnalyzer analyzer = new AntlrQuerySemanticAnalyzer(
            parser,
            typeFactory,
            valueFactory,
            new SemanticSymbolManager(
                typeFactory,
                contextManager,
                new SemanticFunctionSets(typeFactory).ALL()
            ),
            null,
            moduleManager,
            grammarManager,
            typeFactory.anyNode(),
            uri,
            varTypes,
            new AxisVisitor(),
            cardinalityFactory,
            cardinalityVisitor,
                typeVisitor,
            itemTypeVisitor,
                new NamespaceResolver("fn", "", "", "", "")
            );
        analyzer.visit(xqueryTree);
        if (!analyzer.getErrors().isEmpty())
        {

            DiagnosticMessageCreator c = new DiagnosticMessageCreator();
            throw new IllegalStateException("Errors in semantic analysis: "
                    + analyzer.getErrors().stream().map(c::create).map(Objects::toString).collect(Collectors.joining("\n")));

        }

        final AntlrQueryEvaluator visitor = new AntlrQueryEvaluator(
            tree, parser, valueFactory, analyzer, typeFactory, moduleManager, vars, typeVisitor);

        return visitor.visit(xqueryTree);
    }

    public static AntlrQueryValue evaluate(
        final ParseTree tree,
        final String xquery,
        final String uri,
        final Parser parser
        )
    {
        return evaluate(tree, xquery, uri, parser, Map.of());
    }

    public static AntlrQueryValue evaluate(
        final ParseTree tree,
        final String xquery,
        final String baseUri,
        final Parser parser,
        final Map<String, AntlrQueryValue> vars
        )
    {
        return evaluateWithoutMockRoot(tree, xquery, baseUri, parser, vars);
    }

    public interface TreeEvaluator {
        AntlrQueryValue evaluate(final ParseTree tree, Map<String, AntlrQueryValue> vars);
        default AntlrQueryValue evaluate(final ParseTree tree) {
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
        final MemoizedTypeFactory typeFactory = new MemoizedTypeFactory(new AntlrQueryNamedTypeSets().all(), Map.of());
        final AntlrQueryValueFactory valueFactory = new AntlrQueryMemoizedValueFactory(typeFactory);
        final ModuleManager moduleManager = new ModuleManager(Set.of());
        final GrammarManager grammarManager = new GrammarManager(Set.of());
        final AntlrQuerySemanticContextManager contextManager = new AntlrQuerySemanticContextManager(typeFactory);
        final MemoizedCardinalityFactory cardinalityFactory = new MemoizedCardinalityFactory();
        final CardinalityVisitor cardinalityVisitor = new CardinalityVisitor(cardinalityFactory);
        final NumericRangeVisitor numericRangeVisitor = new NumericRangeVisitor();
        final ItemTypeVisitor itemTypeVisitor = new ItemTypeVisitor(
                cardinalityVisitor, numericRangeVisitor, typeFactory);
        final TypeVisitor typeVisitor = new TypeVisitor(typeFactory, cardinalityVisitor, itemTypeVisitor);
        final AntlrQuerySemanticAnalyzer analyzer = new AntlrQuerySemanticAnalyzer(
            parser,
            typeFactory,
            valueFactory,
            new SemanticSymbolManager(
                typeFactory,
                contextManager,
                new SemanticFunctionSets(typeFactory).ALL()
            ),
            null,
            moduleManager,
            grammarManager,
            typeFactory.anyNode(),
            uri,
            Map.of(),
            new AxisVisitor(),
            cardinalityFactory,
            cardinalityVisitor,
                typeVisitor,
            itemTypeVisitor,
            new NamespaceResolver("fn", "", "", "", "")
            );


        return (tree, variables) -> {
            final AntlrQueryEvaluator visitor = new AntlrQueryEvaluator(
                tree,
                parser,
                valueFactory,
                analyzer,
                typeFactory,
                moduleManager,
                variables,
                typeVisitor
                );
            return visitor.visit(xqueryTree);
        };
    }


    public static XqueryContext parse(final String xquery) {
        final CharStream characters = CharStreams.fromString(xquery);
        final var xqueryLexer = new AntlrQueryLexer(characters);
        final var xqueryTokens = new CommonTokenStream(xqueryLexer);
        final var xqueryParser = new AntlrQueryParser(xqueryTokens);
        return xqueryParser.xquery();
    }


}
