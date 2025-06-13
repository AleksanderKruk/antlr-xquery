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
        Map<String, List<String>> argMap = parseArgs(args);

        List<String> grammarFiles = argMap.get("--grammars");
        List<String> targetFiles = argMap.get("--target-files");
        List<String> startingRules = argMap.get("--starting-rule");

        if (grammarFiles == null || grammarFiles.isEmpty()) {
            System.err.println("No grammars given (--grammars)");
            System.exit(1);
        }

        if (startingRules == null || startingRules.isEmpty()) {
            System.err.println("No starting rule given (--starting-rule)");
            System.exit(1);
        }
        String startingRule = startingRules.get(0);

        if (targetFiles == null || targetFiles.isEmpty()) {
            System.err.println("No target files given (--target-files)");
            System.exit(1);
        }

        Path tmpDir = Files.createTempDirectory("antlr-gen");
        Path sourceDir = tmpDir.resolve("src");
        Path outputDir = tmpDir.resolve("classes");
        Files.createDirectories(sourceDir);
        Files.createDirectories(outputDir);

        String baseName = getBaseGrammarName(grammarFiles.get(0));
        String grammarPackage = "generated." + baseName.toLowerCase();

        List<String> antlrArgs = new ArrayList<>(grammarFiles);
        antlrArgs.add("-o");
        antlrArgs.add(sourceDir.toString());
        antlrArgs.add("-package");
        antlrArgs.add(grammarPackage);

        Tool antlr = new Tool(antlrArgs.toArray(new String[0]));
        antlr.processGrammarsOnCommandLine();

        compileJavaSources(sourceDir, outputDir);

        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{outputDir.toUri().toURL()})) {
            String lexerName = getFirstArg(argMap, "--lexer-name", findClassName(outputDir, "Lexer.class"));
            String parserName = getFirstArg(argMap, "--parser-name", findClassName(outputDir, "Parser.class"));

            Class<?> lexerClass = Class.forName(lexerName, true, classLoader);
            Class<?> parserClass = Class.forName(parserName, true, classLoader);

            List<String> xqueryInput = argMap.get("--query");
            List<String> xqueryFile = argMap.get("--query-file");
            String query = (xqueryInput != null && !xqueryInput.isEmpty()) ? String.join(" ", xqueryInput) :
                           (xqueryFile != null && !xqueryFile.isEmpty()) ? Files.readString(Path.of(xqueryFile.get(0))) : null;

            if (query == null) {
                System.err.println("Missing query (--query or --query-file)");
                System.exit(1);
            }

            // Parser rule method
            Method startRuleMethod = parserClass.getMethod(startingRule);

            // Compile XQuery query to tree once
            CharStream xqueryCharStream = CharStreams.fromString(query);
            AntlrXqueryLexer xqueryLexer = new AntlrXqueryLexer(xqueryCharStream);
            CommonTokenStream xqueryTokens = new CommonTokenStream(xqueryLexer);
            AntlrXqueryParser xqueryParser = new AntlrXqueryParser(xqueryTokens);
            ParseTree xqueryTree = xqueryParser.xquery();

            for (String file : targetFiles) {
                String input = Files.readString(Path.of(file));
                CharStream charStream = CharStreams.fromString(input);
                Lexer lexer = (Lexer) lexerClass.getConstructor(CharStream.class).newInstance(charStream);
                CommonTokenStream tokens = new CommonTokenStream(lexer);
                Parser parser = (Parser) parserClass.getConstructor(TokenStream.class).newInstance(tokens);
                ParseTree tree = (ParseTree) startRuleMethod.invoke(parser);

                XQuerySemanticAnalyzer analyzer = new XQuerySemanticAnalyzer(
                        parser,
                        new XQueryBaseSemanticContextManager(),
                        new XQueryEnumTypeFactory(),
                        new XQueryMemoizedValueFactory(),
                        new XQueryBaseSemanticFunctionCaller()
                );
                analyzer.visit(xqueryTree);

                XQueryEvaluatorVisitor evaluator = new XQueryEvaluatorVisitor(tree, parser);
                var results = evaluator.visit(xqueryTree);

                System.out.println("File: " + file);
                for (var result : results.atomize()) {
                    String printed = result.stringValue();
                    if (printed != null)
                        System.out.println(printed);
                    else
                        System.out.println(result.toString());
                }
            }
        }
    }

    static String getFirstArg(Map<String, List<String>> args, String key, String fallback) {
        List<String> list = args.get(key);
        return (list != null && !list.isEmpty()) ? list.get(0) : fallback;
    }

    static void compileJavaSources(Path sourceDir, Path outputDir) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("Java compiler not found. Use JDK, not JRE.");
        }

        List<File> javaFiles = new ArrayList<>();
        Files.walk(sourceDir)
                .filter(p -> p.toString().endsWith(".java"))
                .forEach(p -> javaFiles.add(p.toFile()));

        List<String> options = List.of(
                "-d", outputDir.toString(),
                "-classpath", System.getProperty("java.class.path")
        );

        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> units = fileManager.getJavaFileObjectsFromFiles(javaFiles);

        boolean success = compiler.getTask(null, fileManager, null, options, null, units).call();
        fileManager.close();

        if (!success) {
            throw new RuntimeException("Compilation failed.");
        }
    }

    static String getBaseGrammarName(String grammarFile) {
        Path path = Path.of(grammarFile);
        String name = path.getFileName().toString();
        return name.endsWith(".g4") ? name.substring(0, name.length() - 3) : name;
    }

    static String findClassName(Path classRoot, String suffix) throws IOException {
        Optional<Path> match = Files.walk(classRoot)
                .filter(p -> p.toString().endsWith(suffix))
                .findFirst();

        if (match.isEmpty()) {
            throw new FileNotFoundException("Could not find class with suffix: " + suffix);
        }

        Path relative = classRoot.relativize(match.get());
        return relative.toString()
                .replace(File.separatorChar, '.')
                .replaceAll("\\.class$", "");
    }

    static Map<String, List<String>> parseArgs(String[] args) {
        Map<String, List<String>> map = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            String key = args[i];
            if (key.startsWith("--")) {
                List<String> values = new ArrayList<>();
                i++;
                while (i < args.length && !args[i].startsWith("--")) {
                    values.add(args[i]);
                    i++;
                }
                i--; // go back one, loop will increment
                map.put(key, values);
            }
        }
        return map;
    }
}
