package com.github.akruk.antlrxquery.semanticanalyzer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.xpath.XPath;

import com.github.akruk.antlrgrammar.ANTLRv4Lexer;
import com.github.akruk.antlrgrammar.ANTLRv4Parser;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.GrammarTypeContext;

public class GrammarManager {
    private final Set<Path> grammarPaths;
    private final Map<Path, ParseTree> trees;
    private final Map<Path, ANTLRv4Parser> parsers;

    enum ImportStatus {
        OK,
        MANY_VALID_PATHS,
        NO_PATH_FOUND__NEITHER_FOUND,
        NO_PATH_FOUND__NO_LEXER,
        NO_PATH_FOUND__NO_PARSER,
    }
    enum ResolvingStatus {
        OK,
        FOUND_OTHER_THAN_FILE,
        UNREADABLE,
        FAILED_TO_PARSE
    }

    record ResolveTreeResult(
        ParseTree tree,
        ANTLRv4Parser parser
    ){}

    record GrammarFile(
        ParseTree tree,
        GrammarFileType type
    ){}

    public record GrammarImportResult(
        Map<Path, GrammarFile> validPaths,
        List<Path> resolvedPaths,
        List<ResolvingStatus> resolvingStatuses,
        ImportStatus status
    )
    {}

    public GrammarManager(final Set<Path> grammarPaths) {
        this.grammarPaths = grammarPaths;
        this.trees = new HashMap<>();
        this.parsers = new HashMap<>();
    }

    enum GrammarFileType {
        MIXED,
        PARSER,
        LEXER
        // TOKENS
    }

    record ResolvePathsResult(
        Map<Path, GrammarFile> validPaths,
        List<Path> resolvedPaths,
        List<ResolvingStatus> statuses,
        boolean foundParser,
        boolean foundLexer
    ){}
    private ResolvePathsResult resolvePaths(
        final String relativeGrammarPath,
        final Map<Path, GrammarFile> validPaths, /* out */
        final List<Path> resolvedPaths, /* out */
        final List<ResolvingStatus> statuses /* out */
        )
    {
        for (final Path path : grammarPaths) {
            final Path resolved = path.resolve(relativeGrammarPath).toAbsolutePath();
            final File file = resolved.toFile();
            if (!file.exists()) {
                continue;
            }
            resolvedPaths.add(resolved);
            if (!file.isFile()) {
                statuses.add(ResolvingStatus.FOUND_OTHER_THAN_FILE);
                continue;
            }
            if (!file.canRead()) {
                statuses.add(ResolvingStatus.UNREADABLE);
                continue;
            }
            ResolveTreeResult resolvedTreeResult;
            try {
                if (validPaths.containsKey(resolved)) {
                    continue;
                }
                resolvedTreeResult = resolveTree(resolved);
                ParseTree[] grammarTypes = XPath.findAll(
                    resolvedTreeResult.tree,
                    "grammarSpec/grammarDecl/grammarType",
                    resolvedTreeResult.parser
                    ).toArray(ParseTree[]::new);
                if (grammarTypes.length != 1) {
                    statuses.add(ResolvingStatus.FAILED_TO_PARSE);
                } else {
                    GrammarTypeContext grammarType = (GrammarTypeContext) grammarTypes[0];
                    GrammarFileType type = getGrammarType(grammarType);
                    validPaths.put(resolved, new GrammarFile(resolvedTreeResult.tree, type));
                    statuses.add(ResolvingStatus.OK);
                }
            } catch (IOException e) {
                statuses.add(ResolvingStatus.FAILED_TO_PARSE);
            }
        }
        return new ResolvePathsResult(
            validPaths,
            resolvedPaths,
            statuses,
            validPaths.values().stream().anyMatch(gf->gf.type == GrammarFileType.PARSER || gf.type == GrammarFileType.MIXED),
            validPaths.values().stream().anyMatch(gf->gf.type == GrammarFileType.LEXER || gf.type == GrammarFileType.MIXED)
        );
    }

    private GrammarFileType getGrammarType(GrammarTypeContext grammarType) {
        GrammarFileType type;
        if (grammarType.LEXER() != null) {
            type = GrammarFileType.LEXER;
        } else if (grammarType.PARSER() != null) {
            type = GrammarFileType.PARSER;
        } else {
            type = GrammarFileType.MIXED;
        }
        return type;
    }

    private ResolveTreeResult resolveTree(final Path file) throws IOException {
        final ParseTree cachedTree = trees.get(file);
        final ANTLRv4Parser cachedParser = parsers.get(file);
        if (cachedTree != null) {
            return new ResolveTreeResult(cachedTree, cachedParser);
        } else {
            CharStream stream = CharStreams.fromPath(file);
            ANTLRv4Lexer lexer = new ANTLRv4Lexer(stream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            ANTLRv4Parser parser = new ANTLRv4Parser(tokens);
            var parsedTree = parser.grammarSpec();
            trees.put(file, parsedTree);
            parsers.put(file, parser);
            return new ResolveTreeResult(parsedTree, parser);
        }

    }

    public GrammarImportResult namespaceGrammarImport(final List<String> grammarImportPaths) {
        final Map<Path, GrammarFile> validPaths = new HashMap<>();
        final List<Path> resolvedPaths = new ArrayList<>();
        final List<ResolvingStatus> statuses = new ArrayList<>();
        ResolvePathsResult resolvedPathsResult = null;
        for (var g : grammarImportPaths) {
            resolvedPathsResult = resolvePaths("./"+g, validPaths, resolvedPaths, statuses);
        }
        return resolveImportResult(validPaths, resolvedPaths, statuses, resolvedPathsResult);
    }

    public GrammarImportResult pathOnlyGrammarImport(final List<String> grammarImportPaths) {
        final Map<Path, GrammarFile> validPaths = new HashMap<>();
        final List<Path> resolvedPaths = new ArrayList<>();
        final List<ResolvingStatus> statuses = new ArrayList<>();
        ResolvePathsResult resolvedPathsResult = null;
        for (var g : grammarImportPaths) {
            resolvedPathsResult = resolvePaths(g, validPaths, resolvedPaths, statuses);
        }
        return resolveImportResult(validPaths, resolvedPaths, statuses, resolvedPathsResult);

    }


    public GrammarImportResult defaultPathGrammarImport(final String grammarImportQuery) {
        final String defaultPathRoot = getDefaultPathRoot(grammarImportQuery);
        final Map<Path, GrammarFile> validPaths = new HashMap<>();
        final List<Path> resolvedPaths = new ArrayList<>();
        final List<ResolvingStatus> statuses = new ArrayList<>();
        var resolvedPathsResult = resolvePaths("./"+defaultPathRoot+".g4", validPaths, resolvedPaths, statuses);
        switch(validPaths.size()) {
            case 0 -> {
                resolvedPathsResult = resolvePaths("./"+defaultPathRoot+"Parser.g4", validPaths, resolvedPaths, statuses);
                resolvedPathsResult = resolvePaths("./"+defaultPathRoot+"Lexer.g4", validPaths, resolvedPaths, statuses);
                return resolveImportResult(validPaths, resolvedPaths, statuses, resolvedPathsResult);
            }
            case 1 -> {
                return resolveImportResult(validPaths, resolvedPaths, statuses, resolvedPathsResult);
            }
            default -> {
                return new GrammarImportResult(
                    validPaths,
                    resolvedPaths,
                    statuses,
                    ImportStatus.MANY_VALID_PATHS
                );
            }
        }
    }

    private GrammarImportResult resolveImportResult(final Map<Path, GrammarFile> validPaths, final List<Path> resolvedPaths,
            final List<ResolvingStatus> statuses, ResolvePathsResult resolvedPathsResult) {
        if (resolvedPathsResult.foundLexer && resolvedPathsResult.foundParser) {
            return new GrammarImportResult(
                validPaths,
                resolvedPaths,
                statuses,
                ImportStatus.OK
            );
        } else if (!resolvedPathsResult.foundLexer && resolvedPathsResult.foundParser) {
            return new GrammarImportResult(
                validPaths,
                resolvedPaths,
                statuses,
                ImportStatus.NO_PATH_FOUND__NO_LEXER
            );
        } else if (resolvedPathsResult.foundLexer && !resolvedPathsResult.foundParser) {
            return new GrammarImportResult(
                validPaths,
                resolvedPaths,
                statuses,
                ImportStatus.NO_PATH_FOUND__NO_PARSER
            );
        } else {
            return new GrammarImportResult(
                validPaths,
                resolvedPaths,
                statuses,
                ImportStatus.NO_PATH_FOUND__NEITHER_FOUND
            );
        }
    }

    private String getDefaultPathRoot(final String moduleImportQuery) {
        String[] parts = moduleImportQuery.split(":");
        StringBuilder path = new StringBuilder();
        for (int i = 0; i < parts.length-1; i++) {
            var p = parts[i];
            var capitalized = p.substring(0, 1).toUpperCase() + p.substring(1);
            path.append(capitalized).append("/");
        }
        path.append(parts[parts.length-1]);
        return path.toString();
    }

}
