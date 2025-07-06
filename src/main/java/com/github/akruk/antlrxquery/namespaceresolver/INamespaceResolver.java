package com.github.akruk.antlrxquery.namespaceresolver;

import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver.ResolvedName;

public interface INamespaceResolver {
    ResolvedName resolve(String fullName);
}
