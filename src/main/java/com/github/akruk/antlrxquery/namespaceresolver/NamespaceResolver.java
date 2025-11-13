package com.github.akruk.antlrxquery.namespaceresolver;

import java.util.regex.Pattern;

public class NamespaceResolver {
    public record QualifiedName(String namespace, String name) {
        @Override
        public final String toString() {
            if (namespace == null || namespace.isEmpty()) {
                return name;
            } else {
                return namespace+":"+name;
            }
        }
    }

    final private String defaultFunctionNamespace;
    final private String defaultTypeNamespace;
    final private String defaultElementNamespace;
    final private String defaultConstructionNamespace;
    final private String defaultAnnotationNamespace;
    final private Pattern splitter;

    public NamespaceResolver(
        final String defaultFunctionNamespace,
        final String defaultTypeNamespace,
        final String defaultElementNamespace,
        final String defaultConstructionNamespace,
        final String defaultAnnotationNamespace
        )
    {
        this.defaultFunctionNamespace = defaultFunctionNamespace;
        this.defaultTypeNamespace = defaultTypeNamespace;
        this.defaultElementNamespace = defaultElementNamespace;
        this.defaultConstructionNamespace = defaultConstructionNamespace;
        this.defaultAnnotationNamespace = defaultAnnotationNamespace;
        this.splitter = Pattern.compile(":");
    }

    public QualifiedName resolveElement(final String fullName) {
        return resolve(fullName, defaultElementNamespace);
    }

    public QualifiedName resolveType(final String fullName) {
        return resolve(fullName, defaultTypeNamespace);
    }


    public QualifiedName resolveConstruction(final String fullName) {
        return resolve(fullName, defaultConstructionNamespace);
    }

    public QualifiedName resolveAnnotation(final String fullName) {
        return resolve(fullName, defaultAnnotationNamespace);
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

    public NamespaceResolver withElementNamespace(final String elementNamespace) {
        return new NamespaceResolver(
            defaultFunctionNamespace,
            defaultTypeNamespace,
            elementNamespace,
            defaultConstructionNamespace,
            defaultAnnotationNamespace
        );
    }


}
