package com.github.akruk.antlrxquery.namespaceresolver;

import java.util.regex.Pattern;

public class NamespaceResolver {
    public record QualifiedName(String namespace, String name) {
        @Override
        public final String toString() {
            return namespace+":"+name;
        }
    }

    final private String defaultFunctionNamespace;
    final private String defaultTypeNamespace;
    final private Pattern splitter;

    public NamespaceResolver(final String defaultFunctionNamespace, final String defaultTypeNamespace) {
        this.defaultFunctionNamespace = defaultFunctionNamespace;
        this.defaultTypeNamespace = defaultTypeNamespace;
        this.splitter = Pattern.compile(":");
    }

    public QualifiedName resolveType(final String fullName) {
        return resolve(fullName, defaultTypeNamespace);
    }

    private QualifiedName resolve(final String fullName, final String substituteNamespace) {
        final String[] parts = splitter.split(fullName, 2);
        final boolean hasNamespace = parts.length == 2;
        if (hasNamespace) {
            final String namespace = parts[0];
            final String name = parts[1];
            return new QualifiedName(namespace, name);
        } else {
            final String namespace = substituteNamespace;
            final String name = parts[0];
            return new QualifiedName(namespace, name);
        }
    }

    public QualifiedName resolveFunction(final String fullName) {
        return resolve(fullName, defaultFunctionNamespace);
    }
}
