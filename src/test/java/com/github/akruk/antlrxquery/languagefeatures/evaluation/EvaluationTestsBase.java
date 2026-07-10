package com.github.akruk.antlrxquery.languagefeatures.evaluation;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


import java.util.ArrayList;

import org.antlr.v4.Tool;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.xpath.XPath;

import com.github.akruk.antlrxquery.evaluator.XQuery;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;
import com.github.akruk.antlrxquery.evaluator.values.factories.defaults.XQueryMemoizedValueFactory;
import com.github.akruk.antlrxquery.evaluator.values.operations.ValueAtomizer;
import com.github.akruk.antlrxquery.evaluator.values.operations.ValueComparisonOperator;
import com.github.akruk.antlrxquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryMemoizedTypeFactory;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryNamedTypeSets;

public class EvaluationTestsBase {
    public final AntlrQueryTypeFactory typeFactory;
    public final XQueryValueFactory valueFactory;
    public final ValueAtomizer atomizer;
    public final ValueComparisonOperator valueOperator;

    public EvaluationTestsBase() {
        typeFactory = new XQueryMemoizedTypeFactory(new XQueryNamedTypeSets().all());
        valueFactory = new XQueryMemoizedValueFactory(typeFactory);
        atomizer = new ValueAtomizer();
        valueOperator = new ValueComparisonOperator(valueFactory);

    }

    public boolean deepEquals(final XQueryValue sequence1, final XQueryValue sequence2) {
        if (sequence1 == sequence2) {
            return true;
        }

        if (sequence1 == null || sequence2 == null) {
            return false;
        }

        final List<XQueryValue> seq1 = atomizer.atomize(sequence1);
        final List<XQueryValue> seq2 = atomizer.atomize(sequence2);

        if (seq1.size() != seq2.size()) {
            return false;
        }

        for (int i = 0; i < seq1.size(); i++) {
            final XQueryValue element1 = seq1.get(i);
            final XQueryValue element2 = seq2.get(i);

            if (!valueOperator.valueEquals(element1, element2).booleanValue) {
                return false;
            }
        }

        return true;
    }

    public void assertResult(final String xquery, final String result) {
        final var value = XQuery.evaluateWithMockRoot(null, xquery, null, null);
        assertNotNull(value);
        assertEquals(result, value.stringValue);
    }

    public void assertResult(final String xquery, final BigDecimal result) {
        final var value = XQuery.evaluateWithMockRoot(null, xquery, null, null);
        assertNotNull(value);
        assertTrue(result.compareTo(value.numericValue) == 0);
    }

    public void assertResult(final String xquery, final List<XQueryValue> result) {
        final XQueryValue value = XQuery.evaluateWithMockRoot(null, xquery, null, null);
        assertNotNull(value);
        assertEquals(result.size(), value.size);
        for (int i = 0; i < result.size(); i++) {
            final var expected = result.get(i);
            final var received = value.sequence.get(i);
            assertTrue(valueOperator.valueEquals(expected, received).booleanValue);
        }
    }

    public void assertResult(final XQueryValue value, final XQueryValue result) {
        assertNotNull(value);
        assertFalse(value.isError, () -> "Value is error: " + value.error.getDescription());
        if (result.size != 1)
            assertTrue(deepEquals(result, value));
        else
            if (result == value)
                return;
            assertEquals(result, value);
    }

    public void assertResult(final String xquery, final XQueryValue result) {
        final XQueryValue value = XQuery.evaluateWithMockRoot(null, xquery, null, null);
        assertResult(value, result);
    }

    public void assertError(final String xquery, final XQueryValue result) {
        final XQueryValue value = XQuery.evaluateWithMockRoot(null, xquery, null, null);
        assertNotNull(value);
        assertTrue(result.error == value.error);
    }

    protected record ValueParserAndTree(XQueryValue value, Parser parser, ParseTree tree) {};

    /**
     * Generates grammar and parser/lexer classes in a dedicated directory structure.
     * Each grammar gets its own folder under a common temp directory.
     */
    public XQueryValue executeDynamicGrammarQuery(
        final String grammarName,
        final String grammarString,
        final String startRuleName,
        final String textualTree,
        final String xquery,
        final String uri
        )
        throws Exception
    {
        final var valueParserAndTree = executeDynamicGrammarQueryWithTree(
            grammarName,
            grammarString,
            startRuleName,
            textualTree,
            xquery,
            uri
            );
        return valueParserAndTree.value;
    }


    /**
     * Generates grammar and parser/lexer classes in a dedicated directory structure.
     * Each grammar gets its own folder under a common temp directory.
     */
    public ValueParserAndTree executeDynamicGrammarQueryWithTree(
        final String grammarName,
        final String grammarString,
        final String startingRuleName,
        final String textualTree,
        final String xquery,
        final String uri
        )
        throws Exception
    {
        // Create a dedicated temp directory for this grammar
        final Path baseTmpDir = Files.createTempDirectory("antlr-dyn-grammars");
        final Path grammarDir = baseTmpDir.resolve(grammarName);
        Files.createDirectories(grammarDir);

        // Save grammar file
        final Path grammarFile = grammarDir.resolve(grammarName + ".g4");
        Files.writeString(grammarFile, grammarString);

        // Generate sources into grammarDir/src
        final Path sourceDir = grammarDir.resolve("src");
        Files.createDirectories(sourceDir);

        final Tool antlrTool = new Tool(new String[] {
            grammarFile.toString(), "-visitor", "-no-listener", "-o", sourceDir.toString()
        });
        antlrTool.processGrammarsOnCommandLine();

        // Compile generated Java sources into grammarDir/classes
        final Path outputDir = grammarDir.resolve("classes");
        Files.createDirectories(outputDir);

        final List<Path> javaFiles = Files.walk(sourceDir)
            .filter(p -> p.toString().endsWith(".java"))
            .toList();
        final javax.tools.JavaCompiler compiler = javax.tools.ToolProvider.getSystemJavaCompiler();
        final List<String> compileArgs = new ArrayList<>();
        compileArgs.add("-d");
        compileArgs.add(outputDir.toString());
        for (final Path javaFile : javaFiles) {
            compileArgs.add(javaFile.toString());
        }
        compiler.run(null, null, null, compileArgs.toArray(new String[0]));

        // Load classes using URLClassLoader
        final java.net.URLClassLoader classLoader = java.net.URLClassLoader
            .newInstance(new java.net.URL[] { outputDir.toUri().toURL() });

        final Class<?> lexerClass = classLoader.loadClass(grammarName + "Lexer");
        final Class<?> parserClass = classLoader.loadClass(grammarName + "Parser");

        final CharStream input = CharStreams.fromString(textualTree);
        final Lexer lexer = (Lexer) lexerClass.getConstructor(CharStream.class).newInstance(input);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final Parser parser = (Parser) parserClass.getConstructor(TokenStream.class).newInstance(tokens);

        final Method startRule = parser.getClass().getMethod(startingRuleName);
        final ParseTree tree = (ParseTree) startRule.invoke(parser);


        final var value = XQuery.evaluateWithMockRoot(tree, xquery, uri, parser);
        assertNotNull(value);
        return new ValueParserAndTree(value, parser, tree);
    }




    public void assertDynamicGrammarQuery(
        final String grammarName,
        final String grammarString,
        final String startRuleName,
        final String textualTree,
        final String xquery,
        final String uri,
        final XQueryValue expected
        )
        throws Exception
    {
        final var value = executeDynamicGrammarQuery(
            grammarName,
            grammarString,
            startRuleName,
            textualTree,
            xquery,
            uri
            );
        assertNotNull(value);
        assertResult(value, expected);
    }

    public void assertDynamicGrammarQuery(
        final String grammarName,
        final Path grammar,
        final String startRuleName,
        final String textualTree,
        final String xquery,
        final String uri,
        final XQueryValue expected
        )
            throws Exception
    {
        final var value = executeDynamicGrammarQuery(
            grammarName,
            Files.readString(grammar),
            startRuleName,
            textualTree,
            xquery,
            uri
            );
        assertNotNull(value);;
        assertResult(value, expected);
    }


    public void assertSameResultsAsAntlrXPath(
        final String grammarname,
        final String grammar,
        final String startingRuleName,
        final String textualTree,
        final String xquery,
        final String uri
        )
        throws Exception
    {
        final ValueParserAndTree results = executeDynamicGrammarQueryWithTree(
            grammarname,
            grammar,
            startingRuleName,
            textualTree,
            xquery,
            uri
            );
        final ParseTree[] nodes = XPath.findAll(results.tree(), xquery, results.parser())
                .toArray(ParseTree[]::new);
        final ParseTree[] xqueryNodes = results.value().sequence.stream().map(val -> val.node)
                .toArray(ParseTree[]::new);
        assertArrayEquals(nodes, xqueryNodes);
    }



}
