package com.github.akruk.antlrxquery;

import org.antlr.v4.Tool;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.contextmanagement.semanticcontext.baseimplementation.XQueryBaseSemanticContextManager;
import com.github.akruk.antlrxquery.evaluator.XQueryEvaluatorVisitor;
import com.github.akruk.antlrxquery.semanticanalyzer.XQuerySemanticAnalyzer;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.defaults.XQueryBaseSemanticFunctionCaller;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryEnumTypeFactory;
import com.github.akruk.antlrxquery.values.XQueryValue;
import com.github.akruk.antlrxquery.values.factories.defaults.XQueryMemoizedValueFactory;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class XQueryRunner {
    public static void main(final String[] args) throws Exception {
        runXQueryTool(args);
    }

    static void runXQueryTool(final String[] args) throws Exception {
        runXQueryTool(args, System.in, System.out, System.err);
    }

    static void runXQueryTool(final String[] args, final InputStream inputStream,
                             final PrintStream outputStream, final PrintStream errorStream)
    throws Exception
    {
        final Map<String, List<String>> argMap = parseArgs(args);
        final ValidationResult validation = validateInput(argMap);
        if (validation.status != InputStatus.OK) {
            errorStream.println(validation.message);
            System.exit(validation.status.ordinal());
        }
        final ExtractionResult extractedArgs = extractInput(argMap);

        runXQueryTool(extractedArgs, inputStream, outputStream, errorStream);
    }

    static void runXQueryTool(final ExtractionResult config) throws Exception {
        runXQueryTool(config, System.in, System.out, System.err);
    }

    static void runXQueryTool(final ExtractionResult config, final InputStream inputStream,
                             final PrintStream outputStream, final PrintStream errorStream) throws Exception {

        final List<String> grammarFiles = config.grammars;
        final List<String> targetFiles = config.targetFiles;
        final String startingRule = config.startingRule;
        final String query = config.query;

        final Path tmpDir = Files.createTempDirectory("antlr-gen");
        final Path sourceDir = tmpDir.resolve("src");
        final Path outputDir = tmpDir.resolve("classes");
        Files.createDirectories(sourceDir);
        Files.createDirectories(outputDir);

        final String baseName = getBaseGrammarName(grammarFiles.get(0));
        final String grammarPackage = "generated." + baseName.toLowerCase();

        final List<String> antlrArgs = new ArrayList<>(grammarFiles);
        antlrArgs.add("-o");
        antlrArgs.add(sourceDir.toString());
        antlrArgs.add("-package");
        antlrArgs.add(grammarPackage);

        final Tool antlr = new Tool(antlrArgs.toArray(new String[0]));
        antlr.processGrammarsOnCommandLine();

        compileJavaSources(sourceDir, outputDir);

        try (URLClassLoader classLoader = new URLClassLoader(new URL[] { outputDir.toUri().toURL() })) {
            final String lexerName = getFirstNonEmptyOrDefault(config.lexerName, findClassName(outputDir, "Lexer.class"));
            final String parserName = getFirstNonEmptyOrDefault(config.parserName, findClassName(outputDir, "Parser.class"));

            final Class<?> lexerClass = Class.forName(lexerName, true, classLoader);
            final Class<?> parserClass = Class.forName(parserName, true, classLoader);

            // Compile XQuery query to tree once
            final CharStream xqueryCharStream = CharStreams.fromString(query);
            final AntlrXqueryLexer xqueryLexer = new AntlrXqueryLexer(xqueryCharStream);
            final CommonTokenStream xqueryTokens = new CommonTokenStream(xqueryLexer);
            final AntlrXqueryParser xqueryParser = new AntlrXqueryParser(xqueryTokens);
            final ParseTree xqueryTree = xqueryParser.xquery();

            final String targetFile = Files.readString(Path.of(targetFiles.get(0)));
            final ParserAndTree parserAndTree = parseTargetFile(targetFile, lexerClass, parserClass, startingRule);
            final XQuerySemanticAnalyzer analyzer = new XQuerySemanticAnalyzer(
                    parserAndTree.parser,
                    new XQueryBaseSemanticContextManager(),
                    new XQueryEnumTypeFactory(),
                    new XQueryMemoizedValueFactory(),
                    new XQueryBaseSemanticFunctionCaller());
            analyzer.visit(xqueryTree);
            final var querySemanticErrors = analyzer.getErrors();
            for (final var error : querySemanticErrors) {
                errorStream.println(error);
            }
            if (!querySemanticErrors.isEmpty()) {
                System.exit(InputStatus.INVALID_QUERY.ordinal());
            }

            for (final String file : targetFiles) {
                final String fileContent = Files.readString(Path.of(file));
                final XQueryValue results = executeQuery(xqueryTree, lexerClass, parserClass, startingRule, fileContent);
                outputStream.println("File: " + file);
                for (final var result : results.atomize()) {
                    final String printed = result.stringValue();
                    if (printed != null)
                        outputStream.println(printed);
                    else
                        outputStream.println(result.toString());
                }
            }
        }
    }

    static XQueryValue executeQuery(
            final ParseTree query,
            final Class<?> lexerClass,
            final Class<?> parserClass,
            final String startingRule,
            final String input)
    {
        try {
            final ParserAndTree parserAndTree = parseTargetFile(input, lexerClass, parserClass, startingRule);
            final XQueryEvaluatorVisitor evaluator = new XQueryEvaluatorVisitor(parserAndTree.tree, parserAndTree.parser);
            return evaluator.visit(query);
        } catch (final Exception e) {
            return null;
        }
    }

    record ParserAndTree(Parser parser, ParseTree tree) {
    }

    static ParserAndTree parseTargetFile(final String input, final Class<?> lexerClass, final Class<?> parserClass, final String startingRule) throws Exception {
        final CharStream charStream = CharStreams.fromString(input);
        final Lexer lexer = (Lexer) lexerClass.getConstructor(CharStream.class).newInstance(charStream);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final Parser parser = (Parser) parserClass.getConstructor(TokenStream.class).newInstance(tokens);

        final Method startRuleMethod = parserClass.getMethod(startingRule);
        final ParseTree tree = (ParseTree) startRuleMethod.invoke(parser);
        return new ParserAndTree(parser, tree);
    }

    // === ARGUMENT PARSING AND VALIDATION ===

    static Map<String, List<String>> parseArgs(final String[] args) {
        final Map<String, List<String>> map = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            final String key = args[i];
            if (key.startsWith("--")) {
                final List<String> values = new ArrayList<>();
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

    static ValidationResult validateInput(final Map<String, List<String>> args) {
        if (!args.containsKey("--grammars") || args.get("--grammars").isEmpty()) {
            return new ValidationResult(InputStatus.NO_GRAMMARS, "No grammars given (--grammars)");
        }
        if (!args.containsKey("--target-files") || args.get("--target-files").isEmpty()) {
            return new ValidationResult(InputStatus.NO_TARGET_FILES, "No target files given (--target-files)");
        }
        for (var file : args.get("--target-files")) {
            final File targetFile = Path.of(file).toFile();
            if (!targetFile.exists()) {
                return new ValidationResult(InputStatus.INVALID_TARGET_FILE, "Target file does not exist: " + file);
            }
            if (!targetFile.isFile()) {
                return new ValidationResult(InputStatus.INVALID_TARGET_FILE, "Target file is not a regular file: " + file);
            }
        }

        if (!args.containsKey("--starting-rule") || args.get("--starting-rule").isEmpty()) {
            return new ValidationResult(InputStatus.NO_STARTING_RULE, "No starting rule given (--starting-rule)");
        }
        if (!args.containsKey("--query") && !args.containsKey("--query-file")) {
            return new ValidationResult(InputStatus.NO_QUERY, "Missing query (--query or --query-file)");
        }
        if (args.containsKey("--query") && args.containsKey("--query-file")) {
            return new ValidationResult(InputStatus.QUERY_DUPLICATION,
                    "Both --query and --query-file provided, please use only one.");
        }
        if (args.containsKey("--query-file")) {
            final String querypath = String.join(" ", args.getOrDefault("--query-file", Collections.emptyList()));
            final File queryFile = Path.of(querypath).toFile();
            if (!queryFile.exists())
                return new ValidationResult(InputStatus.NO_QUERY, "Query file does not exist: " + querypath);
            if (queryFile.isDirectory())
                return new ValidationResult(InputStatus.NO_QUERY, "Query file is a directory: " + querypath);
        }

        return new ValidationResult(InputStatus.OK, null);
    }

    static ExtractionResult extractInput(final Map<String, List<String>> args) {
        final List<String> grammars = args.get("--grammars");
        final List<String> targetFiles = args.get("--target-files");
        // TODO: add default first starting rule
        final String startingRule = getFirstArg(args, "--starting-rule", "");
        final String lexerName = getFirstArg(args, "--lexer-name", "");
        final String parserName = getFirstArg(args, "--parser-name", "");
        String query = String.join(" ", args.getOrDefault("--query", Collections.emptyList()));
        final String queryFile = String.join(" ", args.getOrDefault("--query-file", Collections.emptyList()));
        if (!args.containsKey("--query")) {
            try {
                query = Files.readString(Path.of(queryFile));
            } catch (final IOException e) {
                query = null;
            }
        }
        return new ExtractionResult(grammars, targetFiles, startingRule, lexerName, parserName, query);
    }

    // === UTILITY METHODS ===

    static String getFirstArg(final Map<String, List<String>> args, final String key, final String fallback) {
        final List<String> list = args.get(key);
        return (list != null && !list.isEmpty()) ? list.get(0) : fallback;
    }

    static String getFirstNonEmptyOrDefault(final String value, final String defaultValue) {
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    static void compileJavaSources(final Path sourceDir, final Path outputDir) throws IOException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("Java compiler not found. Use JDK, not JRE.");
        }

        final List<File> javaFiles = new ArrayList<>();
        Files.walk(sourceDir)
                .filter(p -> p.toString().endsWith(".java"))
                .forEach(p -> javaFiles.add(p.toFile()));

        final List<String> options = List.of(
                "-d", outputDir.toString(),
                "-classpath", System.getProperty("java.class.path"));

        final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        final Iterable<? extends JavaFileObject> units = fileManager.getJavaFileObjectsFromFiles(javaFiles);

        final boolean success = compiler.getTask(null, fileManager, null, options, null, units).call();
        fileManager.close();

        if (!success) {
            throw new RuntimeException("Compilation failed.");
        }
    }

    static String getBaseGrammarName(final String grammarFile) {
        final Path path = Path.of(grammarFile);
        final String name = path.getFileName().toString();
        return name.endsWith(".g4") ? name.substring(0, name.length() - 3) : name;
    }

    static String findClassName(final Path classRoot, final String suffix) throws IOException {
        final Optional<Path> match = Files.walk(classRoot)
                .filter(p -> p.toString().endsWith(suffix))
                .findFirst();

        if (match.isEmpty()) {
            throw new FileNotFoundException("Could not find class with suffix: " + suffix);
        }

        final Path relative = classRoot.relativize(match.get());
        return relative.toString()
                .replace(File.separatorChar, '.')
                .replaceAll("\\.class$", "");
    }

    // === DATA STRUCTURES ===

    enum InputStatus {
        OK, ERROR, EOF, NO_GRAMMARS, NO_TARGET_FILES, NO_STARTING_RULE, NO_QUERY, INVALID_QUERY, QUERY_DUPLICATION, INVALID_TARGET_FILE
    }

    record ValidationResult(InputStatus status, String message) {
    }

    record ExtractionResult(List<String> grammars, List<String> targetFiles, String startingRule,
                                    String lexerName, String parserName, String query) {
    }
}
