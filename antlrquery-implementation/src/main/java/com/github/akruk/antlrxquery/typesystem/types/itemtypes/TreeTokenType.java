package com.github.akruk.antlrxquery.typesystem.types.itemtypes;

import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver;

import java.util.Set;

public sealed interface TreeTokenType
        extends TreeLike
        permits TreeTokenType.AnyToken,
        TreeTokenType.AnyTokenFromGrammar,
        TreeTokenType.TokenType
{
    record AnyToken()
            implements TreeTokenType
    {}

    record AnyTokenFromGrammar(String grammar)
            implements TreeTokenType, GrammarConstrained
    {}

    record TokenType(
            String grammar,
            Set<NamespaceResolver.QualifiedName> elementNames
    ) implements TreeTokenType, GrammarConstrained, NamesConstrained
    {}
}
