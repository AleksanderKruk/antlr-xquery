package com.github.akruk.antlrxquery.languagefeatures.evaluation;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


import java.util.ArrayList;
import java.util.HashMap;

import org.antlr.v4.Tool;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.xpath.XPath;

import com.github.akruk.antlrxquery.evaluator.XQuery;
import com.github.akruk.antlrxquery.values.XQueryError;
import com.github.akruk.antlrxquery.values.XQuerySequence;
import com.github.akruk.antlrxquery.values.XQueryValue;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;
import com.github.akruk.antlrxquery.values.factories.defaults.XQueryMemoizedValueFactory;

public class EvaluationTestsBase {
    public XQueryValueFactory baseFactory = new XQueryMemoizedValueFactory();

    public static boolean deepEquals(XQueryValue sequence1, XQueryValue sequence2) {
        if (sequence1 == sequence2) {
            return true;
        }

        if (sequence1 == null || sequence2 == null) {
            return false;
        }

        List<XQueryValue> seq1 = sequence1.atomize();
        List<XQueryValue> seq2 = sequence2.atomize();

        if (seq1.size() != seq2.size()) {
            return false;
        }

        for (int i = 0; i < seq1.size(); i++) {
            XQueryValue element1 = seq1.get(i);
            XQueryValue element2 = seq2.get(i);

            if (!element1.valueEqual(element2).booleanValue()) {
                return false;
            }
        }

        return true;
    }

    public void assertResult(String xquery, String result) {
        var value = XQuery.evaluate(null, xquery, null);
        assertNotNull(value);
        assertEquals(result, value.stringValue());
    }

    public void assertResult(String xquery, BigDecimal result) {
        var value = XQuery.evaluate(null, xquery, null);
        assertNotNull(value);
        assertTrue(result.compareTo(value.numericValue()) == 0);
    }

    public void assertResult(String xquery, List<XQueryValue> result) {
        XQueryValue value = XQuery.evaluate(null, xquery, null);
        assertNotNull(value);
        assertEquals(result.size(), value.sequence().size());
        for (int i = 0; i < result.size(); i++) {
            var expected = result.get(i);
            var received = value.sequence().get(i);
            assertTrue(expected.valueEqual(received).booleanValue());
        }
    }

    public void assertResult(String xquery, XQueryValue result) {
        XQueryValue value = XQuery.evaluate(null, xquery, null);
        assertNotNull(value);
        assertFalse(value instanceof XQueryError, () -> "Value is error: " + ((XQueryError) value).getDescription());
        if (result instanceof XQuerySequence)
            assertTrue(deepEquals(result, value));
        else
            assertTrue(result == value || result.valueEqual(value).booleanValue());
    }

    public void assertError(String xquery, XQueryValue result) {
        XQueryValue value = XQuery.evaluate(null, xquery, null);
        assertNotNull(value);
        assertTrue(result == value);
    }

    private static final java.util.Map<String, Parser> grammarParserCache = new HashMap<>();

    protected record ValueParserAndTree(XQueryValue value, Parser parser, ParseTree tree) {};

    /**
     * Generates grammar and parser/lexer classes in a dedicated directory structure.
     * Each grammar gets its own folder under a common temp directory.
     */
    public XQueryValue executeDynamicGrammarQuery(String grammarName,
                                                  String grammarString,
                                                  String startRuleName,
                                                  String textualTree,
                                                  String xquery) throws Exception
    {
        var valueParserAndTree = executeDynamicGrammarQueryWithTree(grammarName, grammarString, startRuleName, textualTree, xquery);
        return valueParserAndTree.value;
    }


    /**
     * Generates grammar and parser/lexer classes in a dedicated directory structure.
     * Each grammar gets its own folder under a common temp directory.
     */
    public ValueParserAndTree executeDynamicGrammarQueryWithTree(String grammarName,
                                                  String grammarString,
                                                  String startRuleName,
                                                  String textualTree,
                                                  String xquery) throws Exception
    {
        Parser parser = grammarParserCache.get(grammarString);

        if (parser == null) {
            // Create a dedicated temp directory for this grammar
            Path baseTmpDir = Files.createTempDirectory("antlr-dyn-grammars");
            Path grammarDir = baseTmpDir.resolve(grammarName);
            Files.createDirectories(grammarDir);

            // Save grammar file
            Path grammarFile = grammarDir.resolve(grammarName + ".g4");
            Files.writeString(grammarFile, grammarString);

            // Generate sources into grammarDir/src
            Path sourceDir = grammarDir.resolve("src");
            Files.createDirectories(sourceDir);

            Tool antlrTool = new Tool(new String[] {
                grammarFile.toString(), "-visitor", "-no-listener", "-o", sourceDir.toString()
            });
            antlrTool.processGrammarsOnCommandLine();

            // Compile generated Java sources into grammarDir/classes
            Path outputDir = grammarDir.resolve("classes");
            Files.createDirectories(outputDir);

            List<Path> javaFiles = Files.walk(sourceDir)
                .filter(p -> p.toString().endsWith(".java"))
                .toList();
            javax.tools.JavaCompiler compiler = javax.tools.ToolProvider.getSystemJavaCompiler();
            List<String> compileArgs = new ArrayList<>();
            compileArgs.add("-d");
            compileArgs.add(outputDir.toString());
            for (Path javaFile : javaFiles) {
                compileArgs.add(javaFile.toString());
            }
            compiler.run(null, null, null, compileArgs.toArray(new String[0]));

            // Load classes using URLClassLoader
            java.net.URLClassLoader classLoader = java.net.URLClassLoader
                .newInstance(new java.net.URL[] { outputDir.toUri().toURL() });

            Class<?> lexerClass = classLoader.loadClass(grammarName + "Lexer");
            Class<?> parserClass = classLoader.loadClass(grammarName + "Parser");

            CharStream input = CharStreams.fromString(textualTree);
            Lexer lexer = (Lexer) lexerClass.getConstructor(CharStream.class).newInstance(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            parser = (Parser) parserClass.getConstructor(TokenStream.class).newInstance(tokens);

            // Cache parser for future use
            grammarParserCache.put(grammarString, parser);
        }

        Method startRule = parser.getClass().getMethod(startRuleName);
        ParseTree tree = (ParseTree) startRule.invoke(parser);


        var value = XQuery.evaluate(tree, xquery, parser);
        assertNotNull(value);
        return new ValueParserAndTree(value, parser, tree);
    }




    public void assertDynamicGrammarQuery(String grammarName, String grammarString, String startRuleName, String textualTree, String xquery, XQueryValue expected) throws Exception {
        var value = executeDynamicGrammarQuery(grammarName, grammarString, startRuleName, textualTree, xquery);
        assertNotNull(value);;
    }


    public void assertSameResultsAsAntlrXPath(String grammarname,
                                                String grammar,
                                                String startingRule,
                                                String textualTree,
                                                String xquery)
        throws Exception
    {
        ValueParserAndTree results = executeDynamicGrammarQueryWithTree(grammarname, grammar, startingRule, textualTree, xquery);
        ParseTree[] nodes = XPath.findAll(results.tree(), xquery, results.parser())
                .toArray(ParseTree[]::new);
        ParseTree[] xqueryNodes = results.value().sequence().stream().map(val -> val.node())
                .toArray(ParseTree[]::new);
        assertArrayEquals(nodes, xqueryNodes);
    }



}
