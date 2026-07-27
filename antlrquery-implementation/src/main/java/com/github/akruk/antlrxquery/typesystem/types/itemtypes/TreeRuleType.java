package com.github.akruk.antlrxquery.typesystem.types.itemtypes;

import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver;

import java.util.Set;

public sealed interface TreeRuleType
        extends TreeLike
        permits TreeRuleType.AnyRule,
        TreeRuleType.AnyRuleFromGrammar,
        TreeRuleType.RuleType
{
    record AnyRule()
            implements TreeRuleType
    {}

    record AnyRuleFromGrammar(String grammar)
            implements TreeRuleType, GrammarConstrained
    {}

    record RuleType(
            String grammar,
            Set<NamespaceResolver.QualifiedName> elementNames
    ) implements TreeRuleType, GrammarConstrained, NamesConstrained
    {}
}
