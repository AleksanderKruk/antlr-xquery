package com.github.akruk.antlrquery.typesystem.types.itemtypes;

import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver;

import java.util.Set;

public interface NamesConstrained {
    Set<NamespaceResolver.QualifiedName> elementNames();
}
