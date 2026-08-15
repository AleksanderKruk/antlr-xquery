package com.github.akruk.antlrquery;

import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver;
import com.github.akruk.antlrquery.semanticanalyzer.visitors.*;
import org.antlr.v4.Tool;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.xpath.XPath;

import com.github.akruk.antlrgrammar.ANTLRv4Lexer;
import com.github.akruk.antlrgrammar.ANTLRv4Parser;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.ParserRuleSpecContext;
import com.github.akruk.antlrquery.evaluator.AntlrQueryEvaluator;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;
import com.github.akruk.antlrquery.evaluator.values.factories.AntlrQueryValueFactory;
import com.github.akruk.antlrquery.evaluator.values.factories.defaults.AntlrQueryMemoizedValueFactory;
import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver.QualifiedName;
import com.github.akruk.antlrquery.semanticanalyzer.GrammarManager;
import com.github.akruk.antlrquery.semanticanalyzer.ModuleManager;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.AntlrQuerySemanticContextManager;
import com.github.akruk.antlrquery.semanticanalyzer.semanticfunctioncaller.SemanticFunctionSets;
import com.github.akruk.antlrquery.semanticanalyzer.semanticfunctioncaller.SemanticSymbolManager;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.factories.defaults.MemoizedCardinalityFactory;
import com.github.akruk.antlrquery.typesystem.factories.defaults.MemoizedTypeFactory;
import com.github.akruk.antlrquery.typesystem.factories.defaults.AntlrQueryNamedTypeSets;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class AntlrQueryRunner {
    static void main(final String[] args) { runXQueryTool(args); }

    public static void runXQueryTool(final String[] args) {
        runXQueryTool(args, System.in, System.out, System.err);
    }

    public static void runXQueryTool(
        final String[] args,
        final InputStream inputStream,
        final PrintStream outputStream,
        final PrintStream errorStream
        )
    {
        try {
            final Map<String, List<String>> argMap = parseArgs(args);
            final ValidationResult result = validateAndExtractInput(argMap, inputStream, outputStream, errorStream);
            if (result.status != InputStatus.OK) {
                errorStream.println(result.message);
                System.exit(result.status.ordinal());
            }
            runXQueryTool(result.extractedArgs);
        } catch (final Exception e) {
            e.printStackTrace(errorStream);
        }
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
        final Set<Path> modulePaths = config.modulePaths;

        final Path tmpDir = Files.createTempDirectory("antlr-gen");
        final Path sourceDir = tmpDir.resolve("src");
        final Path outputDir = tmpDir.resolve("classes");
        Files.createDirectories(sourceDir);
        Files.createDirectories(outputDir);

        final String baseName = getBaseGrammarName(grammarFiles.getFirst());
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
            final AntlrQueryLexer xqueryLexer = new AntlrQueryLexer(xqueryCharStream);
            final CommonTokenStream xqueryTokens = new CommonTokenStream(xqueryLexer);
            final AntlrQueryParser xqueryParser = new AntlrQueryParser(xqueryTokens);
            final ParseTree xqueryTree = xqueryParser.xquery();

            final String targetFile = Files.readString(Path.of(targetFiles.getFirst()));
            final ParserAndTree parserAndTree = parseTargetFile(targetFile, lexerClass, parserClass, startingRule);
            final AntlrQueryTypeFactory typeFactory = new MemoizedTypeFactory(new AntlrQueryNamedTypeSets().all(), Map.of());
            final Path cwd = Path.of(System.getProperty("user.dir"));
            modulePaths.add(cwd);
            final var contextManager = new AntlrQuerySemanticContextManager(typeFactory);
            final MemoizedCardinalityFactory cardinalityFactory = new MemoizedCardinalityFactory();
            final NumericRangeVisitor numericRangeVisitor = new NumericRangeVisitor();
            final CardinalityVisitor cardinalityVisitor = new CardinalityVisitor(cardinalityFactory);
            final ItemTypeVisitor itemTypeVisitor = new ItemTypeVisitor(
                    cardinalityVisitor, numericRangeVisitor, typeFactory);
            final TypeVisitor typeVisitor = new TypeVisitor(typeFactory, cardinalityVisitor, itemTypeVisitor);
            final AntlrQuerySemanticAnalyzer analyzer = new AntlrQuerySemanticAnalyzer(
                    parserAndTree.parser,
                    typeFactory,
                    new AntlrQueryMemoizedValueFactory(typeFactory),
                    new SemanticSymbolManager(typeFactory, contextManager, SemanticFunctionSets.ALL(typeFactory)),
                    // // TODO:
                    Map.of(),
                    new ModuleManager(modulePaths),
                    new GrammarManager(modulePaths),
                    typeFactory.anyNode(),
                    config.queryUri,
                    Map.of(),
                    new AxisVisitor(),
                    cardinalityFactory,
                    cardinalityVisitor,
                    typeVisitor,
                    itemTypeVisitor,
            new NamespaceResolver("fn", "", "", "", "")
                    );
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
                final AntlrQueryValue results = executeQuery(
                    xqueryTree,
                    lexerClass,
                    parserClass,
                    startingRule,
                    new QualifiedName("", startingRule), // TODO: connect to grammar
                    fileContent,
                    modulePaths,
                    modulePaths
                    );
                outputStream.println("File: " + file);
                if (results == null) {
                    errorStream.print("<null>");
                    return;
                }
                if (results.sequence == null) {
                    errorStream.println(results);
                    return;
                }
                for (final var result : results.sequence) {
                    final String printed = result.stringValue;
                    if (printed != null)
                        outputStream.println(printed);
                    else
                        outputStream.println(result);
                }
            }
        }
    }

    static AntlrQueryValue executeQuery(
        final ParseTree query,
        final Class<?> lexerClass,
        final Class<?> parserClass,
        final String startingRule,
        final QualifiedName startingRuleQname,
        final String input,
        final Set<Path> modulePaths,
        final Set<Path> grammarPaths,
        final Map<String, AntlrQueryValue> vars,
        final String startingUri
        )
    {
        try {
            final ParserAndTree parserAndTree = parseTargetFile(input, lexerClass, parserClass, startingRule);
            final AntlrQueryTypeFactory typeFactory = new MemoizedTypeFactory(new AntlrQueryNamedTypeSets().all(), Map.of());
            final AntlrQueryValueFactory valueFactory = new AntlrQueryMemoizedValueFactory(typeFactory);
            final ModuleManager manager = new ModuleManager(modulePaths);
            final GrammarManager grammarManager = new GrammarManager(grammarPaths);
            final AntlrQuerySemanticContextManager contextManager = new AntlrQuerySemanticContextManager(typeFactory);
            final MemoizedCardinalityFactory cardinalityFactory = new MemoizedCardinalityFactory();
            final CardinalityVisitor cardinalityVisitor = new CardinalityVisitor(cardinalityFactory);
            final NumericRangeVisitor numericRangeVisitor = new NumericRangeVisitor();
            final ItemTypeVisitor itemTypeVisitor = new ItemTypeVisitor(
                    cardinalityVisitor, numericRangeVisitor, typeFactory);
            final TypeVisitor typeVisitor = new TypeVisitor(typeFactory, cardinalityVisitor, itemTypeVisitor);
            final AntlrQuerySemanticAnalyzer analyzer = new AntlrQuerySemanticAnalyzer(
                    parserAndTree.parser,
                    typeFactory,
                    valueFactory,
                    new SemanticSymbolManager(
                        typeFactory,
                        contextManager,
                        SemanticFunctionSets.ALL(typeFactory)
                    ),
                    Map.of(),
                    manager,
                    grammarManager,
                    typeFactory.element("", Set.of(startingRuleQname)),
                    startingUri,
                    Map.of(),
                    new AxisVisitor(),
                    cardinalityFactory,
                    cardinalityVisitor,
                    typeVisitor,
                    itemTypeVisitor,
            new NamespaceResolver("fn", "", "", "", "")
                );
            final AntlrQueryEvaluator evaluator = new AntlrQueryEvaluator(
                    parserAndTree.tree,
                    parserAndTree.parser,
                    valueFactory,
                    analyzer,
                    typeFactory,
                    manager,
                    vars,
                    typeVisitor
                );
            return evaluator.visit(query);
        } catch (final Exception e) {
            return null;
        }
    }

    static AntlrQueryValue executeQuery(
            final ParseTree query,
            final Class<?> lexerClass,
            final Class<?> parserClass,
            final String startingRule,
            final QualifiedName startingRuleQName,
            final String input,
            final Set<Path> modulePaths,
            final Set<Path> grammarPaths
            )
    {
        return executeQuery(
            query,
            lexerClass,
            parserClass,
            startingRule,
            startingRuleQName,
            input,
            modulePaths,
            grammarPaths,
            Map.of(), ""
            );
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

    static String getFirstArg(final Map<String, List<String>> args, final String key, final String fallback) {
        final List<String> list = args.get(key);
        return (list != null && !list.isEmpty()) ? list.getFirst() : fallback;
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
        try (var walk = Files.walk(sourceDir)) {
            walk.filter(p -> p.toString().endsWith(".java"))
                .forEach(p -> javaFiles.add(p.toFile()));
        }
        

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
        try (var walked = Files.walk(classRoot)) {
            final Optional<Path> match =
                walked.filter(p -> p.toString().endsWith(suffix))
                    .findFirst();

            if (match.isEmpty()) {
                throw new FileNotFoundException("Could not find class with suffix: " + suffix);
            }

            final Path relative = classRoot.relativize(match.get());
            return relative.toString()
                    .replace(File.separatorChar, '.')
                    .replaceAll("\\.class$", "");
        }

    }


    enum InputStatus {
        OK, ERROR, EOF, NO_GRAMMARS, NO_TARGET_FILES, NO_STARTING_RULE, NO_QUERY, INVALID_QUERY, QUERY_DUPLICATION, INVALID_TARGET_FILE
    }

    public record ExtractionResult(
        List<String> grammars,
        List<String> targetFiles,
        String startingRule,
        String lexerName,
        String parserName,
        String query,
        String queryUri,
        InputStream inputStream,
        PrintStream outputStream,
        PrintStream errorStream,
        Set<Path> modulePaths)
    {}

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
                    return first.RULE_REF().getText();
                }
            } catch (final IOException e) {
                System.err.println("Error reading grammar file: " + grammarFile + "\n" + e);
            }
        }
        return "";
    }


    private static final String GRAMMARS_ARG = "--grammars";
    private static final String STARTING_RULE_ARG = "--starting-rule";
    private static final String QUERY_ARG = "--query";
    private static final String QUERY_FILE_ARG = "--query-file";
    private static final String TARGET_FILES_ARG = "--target-files";
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
            final String queryPath = args.get(QUERY_FILE_ARG).getFirst();
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

    static ExtractionResult extractInput(final Map<String, List<String>> args, final InputStream defaultIn,
                                    final PrintStream defaultOut, final PrintStream defaultErr) throws IOException {
        final List<String> grammars = args.get(GRAMMARS_ARG).stream().map(g->new File(g).getAbsolutePath()).toList();
        final List<String> targetFiles = args.get(TARGET_FILES_ARG).stream().map(g->new File(g).getAbsolutePath()).toList();
        final String startingRule = getFirstArg(args, STARTING_RULE_ARG, getFirstRule(grammars));
        final String lexerName = getFirstArg(args, LEXER_NAME_ARG, "");
        final String parserName = getFirstArg(args, PARSER_NAME_ARG, "");

        // Handle query extraction
        final Set<Path> modulePaths = new HashSet<>();
        String query;
        String queryUri;
        if (args.containsKey(QUERY_ARG)) {
            query = String.join(" ", args.get(QUERY_ARG));
            queryUri = null;
        } else {
            final String queryFile = args.get(QUERY_FILE_ARG).getFirst();
            final Path queryFilePath = Path.of(queryFile);
            queryUri = queryFile;
            query = Files.readString(queryFilePath);
            final Path parent = queryFilePath.getParent();
            if (parent != null)
                modulePaths.add(parent);
        }

        // Handle stream arguments
        InputStream inputStream = defaultIn;
        if (args.containsKey(STDIN_ARG)) {
            inputStream = new FileInputStream(args.get(STDIN_ARG).getFirst());
        }

        PrintStream outputStream = defaultOut;
        if (args.containsKey(STDOUT_ARG)) {
            outputStream = new PrintStream(new FileOutputStream(args.get(STDOUT_ARG).getFirst()));
        }

        PrintStream errorStream = defaultErr;
        if (args.containsKey(STDERR_ARG)) {
            errorStream = new PrintStream(new FileOutputStream(args.get(STDERR_ARG).getFirst()));
        }

        return new ExtractionResult(
            grammars,
            targetFiles,
            startingRule,
            lexerName,
            parserName,
            query,
            queryUri,
            inputStream,
            outputStream,
            errorStream,
            modulePaths
            );
    }
    public record ValidationResult(InputStatus status, String message, ExtractionResult extractedArgs) {
        ValidationResult(final InputStatus status, final String message) {
            this(status, message, null);
        }
    }

}
