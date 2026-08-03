package com.github.akruk.antlrquery.typesystem.types.itemtypes;

import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver;

import java.util.Set;

public sealed interface TreeTokenType
        extends TreeLike
        permits TreeTokenType.AnyToken,
        TreeTokenType.AnyTokenFromGrammar,
        TreeTokenType.TokenType
{
    record AnyToken()
            implements TreeTokenType
    {
        @Override
        public boolean equals(Object obj) {
            return obj instanceof AnyToken;
        }
    }

    record AnyTokenFromGrammar(String grammar)
            implements TreeTokenType, GrammarConstrained
    {
        @Override
        public boolean equals(Object obj) {
            return obj instanceof AnyTokenFromGrammar(String grammar1)
                    && grammar1.equals(this.grammar);
        }
    }

    record TokenType(
            String grammar,
            Set<NamespaceResolver.QualifiedName> elementNames
    ) implements TreeTokenType, GrammarConstrained, NamesConstrained
    {
        @Override
        public boolean equals(Object obj) {
            return obj instanceof TokenType(String grammar1, Set<NamespaceResolver.QualifiedName> names)
                    && grammar1.equals(this.grammar)
                    && names.equals(this.elementNames);
        }
    }
}
