package com.github.akruk.antlrxquery.typesystem.types.itemtypes;

import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver;

import java.util.Set;

public interface NamesConstrained {
    Set<NamespaceResolver.QualifiedName> elementNames();
}
