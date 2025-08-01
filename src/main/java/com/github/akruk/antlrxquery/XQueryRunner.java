package com.github.akruk.antlrxquery;

import org.antlr.v4.Tool;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.xpath.XPath;

import com.github.akruk.antlrgrammar.ANTLRv4Lexer;
import com.github.akruk.antlrgrammar.ANTLRv4Parser;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.ParserRuleSpecContext;
import com.github.akruk.antlrxquery.evaluator.XQueryEvaluatorVisitor;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;
import com.github.akruk.antlrxquery.evaluator.values.factories.defaults.XQueryMemoizedValueFactory;
import com.github.akruk.antlrxquery.semanticanalyzer.XQuerySemanticAnalyzer;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.XQuerySemanticContextManager;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.defaults.XQuerySemanticFunctionManager;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryMemoizedTypeFactory;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryNamedTypeSets;

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

    public static void runXQueryTool(final String[] args) throws Exception {
        runXQueryTool(args, System.in, System.out, System.err);
    }

    public static void runXQueryTool(final String[] args,
                                     final InputStream inputStream,
                                     final PrintStream outputStream,
                                     final PrintStream errorStream)
    throws Exception
    {
        final Map<String, List<String>> argMap = parseArgs(args);
        final ValidationResult result = validateAndExtractInput(argMap, inputStream, outputStream, errorStream);
        if (result.status != InputStatus.OK) {
            errorStream.println(result.message);
            System.exit(result.status.ordinal());
        }

        runXQueryTool(result.extractedArgs);
    }


    public static void runXQueryTool(final ExtractionResult config)
    throws Exception
    {

        final List<String> grammarFiles = config.grammars;
        final List<String> targetFiles = config.targetFiles;
        final String startingRule = config.startingRule;
        final String query = config.query;
        final PrintStream outputStream = config.outputStream;
        final PrintStream errorStream = config.errorStream;

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

        final Tool antlr = new Tool(antlrArgs.toArray(String[]::new));
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
            final XQueryTypeFactory typeFactory = new XQueryMemoizedTypeFactory(new XQueryNamedTypeSets().all());
            final XQuerySemanticAnalyzer analyzer = new XQuerySemanticAnalyzer(
                    parserAndTree.parser,
                    new XQuerySemanticContextManager(),
                    typeFactory,
                    new XQueryMemoizedValueFactory(typeFactory),
                    new XQuerySemanticFunctionManager(typeFactory),
                    // TODO:
                    null);
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
                // TODO: atomize
                // TODO:
                for (final var result : results.sequence) {
                    final String printed = result.stringValue;
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
            final XQueryTypeFactory typeFactory = new XQueryMemoizedTypeFactory(new XQueryNamedTypeSets().all());
            final XQueryValueFactory valueFactory = new XQueryMemoizedValueFactory(typeFactory);
            final XQuerySemanticAnalyzer analyzer = new XQuerySemanticAnalyzer(parserAndTree.parser,
                new XQuerySemanticContextManager(),
                typeFactory,
                valueFactory,
                new XQuerySemanticFunctionManager(typeFactory), null);
            final XQueryEvaluatorVisitor evaluator = new XQueryEvaluatorVisitor(
                parserAndTree.tree,
                parserAndTree.parser,
                analyzer,
                typeFactory);
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

    public static ValidationResult validateInput(final Map<String, List<String>> args) {
        if (!args.containsKey(GRAMMARS_ARG) || args.get(GRAMMARS_ARG).isEmpty()) {
            return new ValidationResult(InputStatus.NO_GRAMMARS, "No grammars given (--grammars)");
        }
        if (!args.containsKey(TARGET_FILES_ARG) || args.get(TARGET_FILES_ARG).isEmpty()) {
            return new ValidationResult(InputStatus.NO_TARGET_FILES, "No target files given (--target-files)");
        }
        for (final var file : args.get(TARGET_FILES_ARG)) {
            final File targetFile = Path.of(file).toFile();
            if (!targetFile.exists()) {
                return new ValidationResult(InputStatus.INVALID_TARGET_FILE, "Target file does not exist: " + file);
            }
            if (!targetFile.isFile()) {
                return new ValidationResult(InputStatus.INVALID_TARGET_FILE, "Target file is not a regular file: " + file);
            }
        }

        if (!args.containsKey(STARTING_RULE_ARG) || args.get(STARTING_RULE_ARG).isEmpty()) {
            return new ValidationResult(InputStatus.NO_STARTING_RULE, "No starting rule given (--starting-rule)");
        }
        if (!args.containsKey(QUERY_ARG) && !args.containsKey(QUERY_FILE_ARG)) {
            return new ValidationResult(InputStatus.NO_QUERY, "Missing query (--query or --query-file)");
        }
        if (args.containsKey(QUERY_ARG) && args.containsKey(QUERY_FILE_ARG)) {
            return new ValidationResult(InputStatus.QUERY_DUPLICATION,
                    "Both --query and --query-file provided, please use only one.");
        }
        if (args.containsKey(QUERY_FILE_ARG)) {
            final String querypath = String.join(" ", args.getOrDefault(QUERY_FILE_ARG, Collections.emptyList()));
            final File queryFile = Path.of(querypath).toFile();
            if (!queryFile.exists())
                return new ValidationResult(InputStatus.NO_QUERY, "Query file does not exist: " + querypath);
            if (queryFile.isDirectory())
                return new ValidationResult(InputStatus.NO_QUERY, "Query file is a directory: " + querypath);
        }

        return new ValidationResult(InputStatus.OK, null);
    }

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


    enum InputStatus {
        OK, ERROR, EOF, NO_GRAMMARS, NO_TARGET_FILES, NO_STARTING_RULE, NO_QUERY, INVALID_QUERY, QUERY_DUPLICATION, INVALID_TARGET_FILE
    }

    record ExtractionResult(List<String> grammars, List<String> targetFiles, String startingRule,
            String lexerName, String parserName, String query,
            InputStream inputStream, PrintStream outputStream, PrintStream errorStream) {
    }

    private static ValidationResult validateStreamFiles(final Map<String, List<String>> args) {
    if (args.containsKey(STDIN_ARG)) {
        final String stdinPath = String.join(" ", args.get(STDIN_ARG));
        final Path stdinFilePath = Path.of(stdinPath);
        if (!Files.exists(stdinFilePath)) {
            return new ValidationResult(InputStatus.INVALID_TARGET_FILE, "STDIN file does not exist: " + stdinPath);
        }
        if (Files.isDirectory(stdinFilePath)) {
            return new ValidationResult(InputStatus.INVALID_TARGET_FILE, "STDIN file is a directory: " + stdinPath);
        }
    }

    if (args.containsKey(STDOUT_ARG)) {
        final String stdoutPath = String.join(" ", args.get(STDOUT_ARG));
        final Path stdoutFilePath = Path.of(stdoutPath);
        if (Files.exists(stdoutFilePath) && Files.isDirectory(stdoutFilePath)) {
            return new ValidationResult(InputStatus.INVALID_TARGET_FILE, "STDOUT path is a directory: " + stdoutPath);
        }
    }

    if (args.containsKey(STDERR_ARG)) {
        final String stderrPath = String.join(" ", args.get(STDERR_ARG));
        final Path stderrFilePath = Path.of(stderrPath);
        if (Files.exists(stderrFilePath) && Files.isDirectory(stderrFilePath)) {
            return new ValidationResult(InputStatus.INVALID_TARGET_FILE, "STDERR path is a directory: " + stderrPath);
        }
    }

    return new ValidationResult(InputStatus.OK, null);
}



    static String getFirstRule(final List<String> grammars) {
        if (grammars == null || grammars.isEmpty())
            return "";

        for (final String grammarFile : grammars) {
            try {
                // Read grammar file content
                final String content = Files.readString(Path.of(grammarFile));
                final CharStream input = CharStreams.fromString(content);

                // Setup lexer and parser
                final ANTLRv4Lexer lexer = new ANTLRv4Lexer(input);
                final CommonTokenStream tokens = new CommonTokenStream(lexer);
                final ANTLRv4Parser parser = new ANTLRv4Parser(tokens);

                // Parse grammar specification
                final ParseTree tree = parser.grammarSpec();

                // Find first parser rule
                final var found = XPath.findAll(tree, "//parserRuleSpec", parser);
                if (!found.isEmpty()) {
                    final var first = (ParserRuleSpecContext) found.iterator().next();
                    final var firstRuleName = first.RULE_REF().getText();
                    return firstRuleName;
                }
            } catch (final IOException e) {
                System.err.println("Error reading grammar file: " + grammarFile);
                e.printStackTrace();
            }
        }
        return "";
    }


    private static final String GRAMMARS_ARG = "--grammars";
    private static final String TARGET_FILES_ARG = "--target-files";
    private static final String STARTING_RULE_ARG = "--starting-rule";
    private static final String QUERY_ARG = "--query";
    private static final String QUERY_FILE_ARG = "--query-file";
    private static final String LEXER_NAME_ARG = "--lexer-name";
    private static final String PARSER_NAME_ARG = "--parser-name";
    private static final String STDIN_ARG = "--stdin";
    private static final String STDOUT_ARG = "--stdout";
    private static final String STDERR_ARG = "--stderr";


    // Update the validateAndExtractInput method to include stream validation
    static ValidationResult validateAndExtractInput(
        final Map<String, List<String>> args,
        final InputStream defaultIn,
        final PrintStream defaultOut,
        final PrintStream defaultErr)
    throws IOException
    {
        ValidationResult validation = validateGrammars(args);
        if (validation.status != InputStatus.OK)
            return validation;

        validation = validateTargetFiles(args);
        if (validation.status != InputStatus.OK)
            return validation;

        validation = validateStartingRule(args);
        if (validation.status != InputStatus.OK)
            return validation;

        validation = validateQuery(args);
        if (validation.status != InputStatus.OK)
            return validation;

        validation = validateStreamFiles(args);
        if (validation.status != InputStatus.OK)
            return validation;

        final ExtractionResult extracted = extractInput(args, defaultIn, defaultOut, defaultErr);
        return new ValidationResult(InputStatus.OK, null, extracted);
    }


    private static ValidationResult validateGrammars(final Map<String, List<String>> args) {
        if (!args.containsKey(GRAMMARS_ARG)) {
            return new ValidationResult(InputStatus.NO_GRAMMARS, "No grammars given (" + GRAMMARS_ARG + ")");
        }
        final List<String> grammars = args.get(GRAMMARS_ARG);
        if (grammars.isEmpty()) {
            return new ValidationResult(InputStatus.NO_GRAMMARS, "Grammar list is empty (" + GRAMMARS_ARG + ")");
        }
        return new ValidationResult(InputStatus.OK, null);
    }

private static ValidationResult validateTargetFiles(final Map<String, List<String>> args) {
    if (!args.containsKey(TARGET_FILES_ARG)) {
        return new ValidationResult(InputStatus.NO_TARGET_FILES, "No target files given (" + TARGET_FILES_ARG + ")");
    }

    final List<String> targetFiles = args.get(TARGET_FILES_ARG);
    if (targetFiles.isEmpty()) {
        return new ValidationResult(InputStatus.NO_TARGET_FILES, "Target files list is empty (" + TARGET_FILES_ARG + ")");
    }

    for (final String file : targetFiles) {
        final Path targetPath = Path.of(file);
        if (!Files.exists(targetPath)) {
            return new ValidationResult(InputStatus.INVALID_TARGET_FILE, "Target file does not exist: " + file);
        }
        if (!Files.isRegularFile(targetPath)) {
            return new ValidationResult(InputStatus.INVALID_TARGET_FILE, "Target file is not a regular file: " + file);
        }
    }

    return new ValidationResult(InputStatus.OK, null);
}

private static ValidationResult validateStartingRule(final Map<String, List<String>> args) {
    if (!args.containsKey(STARTING_RULE_ARG)) {
        return new ValidationResult(InputStatus.NO_STARTING_RULE, "No starting rule given (" + STARTING_RULE_ARG + ")");
    }
    final List<String> startingRules = args.get(STARTING_RULE_ARG);
    if (startingRules.isEmpty()) {
        return new ValidationResult(InputStatus.NO_STARTING_RULE, "Starting rule is empty (" + STARTING_RULE_ARG + ")");
    }
    return new ValidationResult(InputStatus.OK, null);
}

    private static ValidationResult validateQuery(final Map<String, List<String>> args) {
        final boolean hasQuery = args.containsKey(QUERY_ARG);
        final boolean hasQueryFile = args.containsKey(QUERY_FILE_ARG);

        if (!hasQuery && !hasQueryFile) {
            return new ValidationResult(InputStatus.NO_QUERY,
                    "Missing query (" + QUERY_ARG + " or " + QUERY_FILE_ARG + ")");
        }

        if (hasQuery && hasQueryFile) {
            return new ValidationResult(InputStatus.QUERY_DUPLICATION,
                    "Both " + QUERY_ARG + " and " + QUERY_FILE_ARG + " provided, please use only one.");
        }

        if (hasQueryFile) {
            final String queryPath = args.get(QUERY_FILE_ARG).get(0);
            final Path queryFilePath = Path.of(queryPath);
            if (!Files.exists(queryFilePath)) {
                return new ValidationResult(InputStatus.NO_QUERY, "Query file does not exist: " + queryPath);
            }
            if (Files.isDirectory(queryFilePath)) {
                return new ValidationResult(InputStatus.NO_QUERY, "Query file is a directory: " + queryPath);
            }
        }

        return new ValidationResult(InputStatus.OK, null);
    }

    static ExtractionResult extractInput(final Map<String, List<String>> args) throws IOException {
        return extractInput(args, System.in, System.out, System.err);
    }

    static ExtractionResult extractInput(final Map<String, List<String>> args, final InputStream defaultIn,
                                    final PrintStream defaultOut, final PrintStream defaultErr) throws IOException {
        final List<String> grammars = args.get(GRAMMARS_ARG);
        final List<String> targetFiles = args.get(TARGET_FILES_ARG);
        final String startingRule = getFirstArg(args, STARTING_RULE_ARG, getFirstRule(grammars));
        final String lexerName = getFirstArg(args, LEXER_NAME_ARG, "");
        final String parserName = getFirstArg(args, PARSER_NAME_ARG, "");

        // Handle query extraction
        String query;
        if (args.containsKey(QUERY_ARG)) {
            query = String.join(" ", args.get(QUERY_ARG));
        } else {
            final String queryFile = args.get(QUERY_FILE_ARG).get(0);
            query = Files.readString(Path.of(queryFile));
        }

        // Handle stream arguments
        InputStream inputStream = defaultIn;
        if (args.containsKey(STDIN_ARG)) {
            inputStream = new FileInputStream(args.get(STDIN_ARG).get(0));
        }

        PrintStream outputStream = defaultOut;
        if (args.containsKey(STDOUT_ARG)) {
            outputStream = new PrintStream(new FileOutputStream(args.get(STDOUT_ARG).get(0)));
        }

        PrintStream errorStream = defaultErr;
        if (args.containsKey(STDERR_ARG)) {
            errorStream = new PrintStream(new FileOutputStream(args.get(STDERR_ARG).get(0)));
        }

        return new ExtractionResult(grammars, targetFiles, startingRule, lexerName, parserName, query,
                                inputStream, outputStream, errorStream);
    }
    record ValidationResult(InputStatus status, String message, ExtractionResult extractedArgs) {
        ValidationResult(final InputStatus status, final String message) {
            this(status, message, null);
        }
    }

}
