package com.github.akruk.antlrquery.typesystem.types.itemtypes;

import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver;

public record NamedItemType(NamespaceResolver.QualifiedName reference)
        implements AntlrQueryItemType
{}
