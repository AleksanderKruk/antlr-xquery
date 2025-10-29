package com.github.akruk.antlrxquery.semanticanalyzer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.evaluator.XQuery;

public class ModuleManager {
    private final Set<Path> modulePaths;
    private final Map<Path, ParseTree> trees;

    enum ImportStatus {
        OK, MANY_VALID_PATHS, NO_PATH_FOUND
    }
    enum ResolvingStatus {
        OK, FOUND_OTHER_THAN_FILE, UNREADABLE
    }
    public record ImportResult(
        ParseTree tree,
        Set<Path> validPaths,
        List<Path> resolvedPaths,
        List<ResolvingStatus> resolvingStatuses,
        ImportStatus status
    )
    {}

    public ModuleManager(final Set<Path> modulePaths) {
        this.modulePaths = modulePaths;
        this.trees = new HashMap<>();
    }

    public ImportResult pathModuleImport(final String moduleImportQuery) {
        return resolveImport(moduleImportQuery);
    }

    private ImportResult resolveImport(final String moduleImportQuery) {
        try {
            final List<Path> resolvedPaths = new ArrayList<>(modulePaths.size());
            final List<ResolvingStatus> statuses = new ArrayList<>(modulePaths.size());
            for (final Path path : modulePaths) {
                final var resolved = path.resolve(moduleImportQuery).toAbsolutePath();
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
                statuses.add(ResolvingStatus.OK);
            }

            final Set<Path> validFiles = IntStream.range(0, statuses.size())
                .filter(i->statuses.get(i) == ResolvingStatus.OK)
                .mapToObj(i->resolvedPaths.get(i))
                .collect(Collectors.toSet());
            if (validFiles.size() == 1) {
                final Path validFile = validFiles.stream().findFirst().orElse(null);
                final ParseTree tree = resolveTree(validFile);
                return new ImportResult(tree, validFiles, resolvedPaths, statuses, ImportStatus.OK);
            }

            if (validFiles.isEmpty()) {
                return new ImportResult(
                    null, validFiles, resolvedPaths, statuses, ImportStatus.NO_PATH_FOUND);
            }

            return new ImportResult(
                resolveTree(validFiles.stream().findFirst().orElse(null)),
                validFiles,
                resolvedPaths,
                statuses,
                ImportStatus.MANY_VALID_PATHS);
        } catch (final IOException e) {
            return null;
        }
    }

    private ParseTree resolveTree(final Path file) throws IOException {
        final ParseTree tree = trees.get(file);
        if (tree != null) {
            return tree;
        } else {
            final String text = Files.readString(file);
            return XQuery.parse(text);
        }

    }

    public ImportResult namespaceModuleImport(final String moduleImportQuery) {
        return resolveImport(moduleImportQuery);
    }

    public ImportResult defaultPathModuleImport(final String moduleImportQuery) {
        return resolveImport("./"+moduleImportQuery+".antlrquery");
    }

}
