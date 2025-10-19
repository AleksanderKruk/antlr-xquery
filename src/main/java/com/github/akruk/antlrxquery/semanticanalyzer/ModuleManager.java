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
import java.util.stream.IntStream;

import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.evaluator.XQuery;

public class ModuleManager {
    private Set<Path> modulePaths;
    private Map<Path, ParseTree> trees;

    enum ImportStatus {
        OK, MANY_VALID_PATHS, NO_PATH_FOUND
    }
    enum ResolvingStatus {
        OK, FOUND_OTHER_THAN_FILE, UNREADABLE
    }
    record ImportResult(
        ParseTree tree,
        List<Path> validPaths,
        List<Path> resolvedPaths,
        List<ResolvingStatus> resolvingStatuses,
        ImportStatus status
    )
    {}

    public ModuleManager(Set<Path> modulePaths) {
        this.modulePaths = modulePaths;
        this.trees = new HashMap<>();
    }
    // error(ctx, "Module import path does not exist: " + target.toAbsolutePath());
    // error(ctx, "Module import path is not a file: " + target.toAbsolutePath());
    // error(ctx, "Module import path cannot be read: " + target.toAbsolutePath());

    public ImportResult pathModuleImport(String moduleImportQuery) {
        return resolveImport(moduleImportQuery);
    }

    private ImportResult resolveImport(String moduleImportQuery) {
        try {
            List<Path> resolvedPaths = new ArrayList<>(modulePaths.size());
            List<ResolvingStatus> statuses = new ArrayList<>(modulePaths.size());
            for (Path path : modulePaths) {
                var resolved = path.resolve(moduleImportQuery).toAbsolutePath();
                File file = resolved.toFile();
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

            List<Path> validFiles = IntStream.range(0, statuses.size())
                .filter(i->statuses.get(i) == ResolvingStatus.OK)
                .mapToObj(i->resolvedPaths.get(i))
                .toList();
            if (validFiles.size() == 1) {
                Path validFile = validFiles.get(0);
                ParseTree tree = resolveTree(validFile);
                return new ImportResult(tree, validFiles, resolvedPaths, statuses, ImportStatus.OK);
            }

            if (validFiles.isEmpty()) {
                return new ImportResult(
                    null, validFiles, resolvedPaths, statuses, ImportStatus.NO_PATH_FOUND);
            }

            return new ImportResult(
                resolveTree(validFiles.get(0)),
                validFiles,
                resolvedPaths,
                statuses,
                ImportStatus.MANY_VALID_PATHS);
        } catch (IOException e) {
            return null;
        }
    }

    private ParseTree resolveTree(Path file) throws IOException {
        ParseTree tree = trees.get(file);
        if (tree != null) {
            return tree;
        } else {
            String text = Files.readString(file);
            return XQuery.parse(text);
        }

    }

    public ImportResult namespaceModuleImport(String moduleImportQuery) {
        return resolveImport(moduleImportQuery);
    }

    public ImportResult defaultPathModuleImport(String moduleImportQuery) {
        return resolveImport("./"+moduleImportQuery+".antlrquery");
    }

}
