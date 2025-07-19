package com.github.akruk.antlrxquery.namespaceresolver;

import java.util.regex.Pattern;

public class NamespaceResolver implements INamespaceResolver {
    public record ResolvedName(String namespace, String name) {}

    private String defaultNamespace;
    private Pattern splitter;

    public NamespaceResolver(String defaultNamespace) {
        this.defaultNamespace = defaultNamespace;
        this.splitter = Pattern.compile(":");
    }

    @Override
    public ResolvedName resolve(String fullName) {
        String[] parts = splitter.split(fullName, 2);
        boolean hasNamespace = parts.length == 2;
        String namespace = hasNamespace ? parts[0] : defaultNamespace;
        String name = hasNamespace ? parts[1] : parts[0];

        return new ResolvedName(namespace, name);
    }
}
