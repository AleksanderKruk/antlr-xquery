package com.github.akruk.antlrquery.typesystem.types.itemtypes;

import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver;
import com.github.akruk.antlrquery.typesystem.types.ItemTypes;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Set;

public sealed interface TreeRuleType
        extends TreeLike
        permits TreeRuleType.AnyRule,
        TreeRuleType.AnyRuleFromGrammar,
        TreeRuleType.RuleType
{
    record AnyRule()
            implements TreeRuleType
    {
        @Override
        public @NonNull String toString() {
            return ItemTypes.stringify(this);
        }

    }

    record AnyRuleFromGrammar(String grammar)
            implements TreeRuleType, GrammarConstrained
    {
        @Override
        public @NonNull String toString() {
            return ItemTypes.stringify(this);
        }

    }

    record RuleType(
            String grammar,
            Set<NamespaceResolver.QualifiedName> elementNames
    ) implements TreeRuleType, GrammarConstrained, NamesConstrained
    {
        @Override
        public @NonNull String toString() {
            return ItemTypes.stringify(this);
        }

    }
}
