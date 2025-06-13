package com.github.akruk.antlrxquery;

import org.antlr.v4.Tool;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.contextmanagement.semanticcontext.baseimplementation.XQueryBaseSemanticContextManager;
import com.github.akruk.antlrxquery.evaluator.XQueryEvaluatorVisitor;
import com.github.akruk.antlrxquery.semanticanalyzer.XQuerySemanticAnalyzer;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.defaults.XQueryBaseSemanticFunctionCaller;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryEnumTypeFactory;
import com.github.akruk.antlrxquery.values.factories.defaults.XQueryMemoizedValueFactory;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class XQueryRunner {
    public static void main(String[] args) throws Exception {
        Map<String, String> argMap = parseArgs(args);

        String grammars = argMap.get("--grammars");
        String query = argMap.get("--query");
        String queryFile = argMap.get("--query-file");
        String startingRule = argMap.getOrDefault("--starting-rule", "xquery");

        if (grammars == null) {
            System.err.println("No grammars given (--grammars)");
            System.exit(1);
        }

        Path tmpDir = Files.createTempDirectory("antlr-gen");

        String[] grammarFiles = grammars.split(",");
        List<String> antlrArgs = new ArrayList<>();
        antlrArgs.add("-o");
        antlrArgs.add(tmpDir.toAbsolutePath().toString());
        antlrArgs.addAll(Arrays.asList(grammarFiles));

        Tool antlr = new Tool(antlrArgs.toArray(new String[0]));
        antlr.processGrammarsOnCommandLine();

        compileJavaSources(tmpDir);

        // Lexer and parser loading
        try (URLClassLoader classLoader = new URLClassLoader(new URL[] { tmpDir.toUri().toURL() })) {
            String baseName = getBaseGrammarName(grammarFiles[0]);
            String parserName = argMap.getOrDefault("--parser-name", baseName + "Lexer");
            String lexerName = argMap.getOrDefault("--lexer-name", baseName + "Parser");

            Class<?> lexerClass = classLoader.loadClass(lexerName);
            Class<?> parserClass = classLoader.loadClass(parserName);

            String inputQuery = query;
            if (queryFile != null) {
                inputQuery = Files.readString(Path.of(queryFile));
            }
            if (inputQuery == null) {
                System.err.println("Missing query (--query or --query-file)");
                System.exit(1);
            }

            // Instantiate lexer and parser dynamically
            CharStream charStream = CharStreams.fromString(inputQuery);
            Constructor<?> lexerConstructor = lexerClass.getConstructor(CharStream.class);
            Lexer lexer = (Lexer) lexerConstructor.newInstance(charStream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            Constructor<?> parserConstructor = parserClass.getConstructor(TokenStream.class);
            Parser parser = (Parser) parserConstructor.newInstance(tokens);

            // Calling starting rule
            Method startRuleMethod = parserClass.getMethod(startingRule);
            ParseTree tree = (ParseTree) startRuleMethod.invoke(parser);
            XQuerySemanticAnalyzer analyzer = new XQuerySemanticAnalyzer(
                    parser,
                    new XQueryBaseSemanticContextManager(),
                    new XQueryEnumTypeFactory(),
                    new XQueryMemoizedValueFactory(),
                    new XQueryBaseSemanticFunctionCaller());
            analyzer.visit(tree);

            XQueryEvaluatorVisitor evaluator = new XQueryEvaluatorVisitor(tree, parser);
            evaluator.visit(tree);
        }
    }

    static void compileJavaSources(Path sourceDir) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("Java compiler not found. Make sure to run this with a JDK, not a JRE.");
        }
        List<File> javaFiles = new ArrayList<>();
        Files.walk(sourceDir)
            .filter(p -> p.toString().endsWith(".java"))
            .forEach(p -> javaFiles.add(p.toFile()));

        List<String> options = List.of("-classpath", System.getProperty("java.class.path"));

        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(javaFiles);
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, null, options, null, compilationUnits);

        boolean success = task.call();
        fileManager.close();
        if (!success) {
            throw new RuntimeException("Failed to compile generated Java sources in " + sourceDir);
        }
    }

    static String getBaseGrammarName(String grammarFile) {
        Path p = Path.of(grammarFile);
        String fileName = p.getFileName().toString();
        if (fileName.endsWith(".g4")) {
            return fileName.substring(0, fileName.length() - 3);
        }
        return fileName;
    }

    static Map<String, String> parseArgs(String[] args) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--")) {
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    map.put(args[i], args[i + 1]);
                    i++;
                } else {
                    map.put(args[i], null);
                }
            }
        }
        return map;
    }
}
