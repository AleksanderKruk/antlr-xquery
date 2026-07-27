package com.github.akruk.antlrxquery.typesystem.types.itemtypes;

import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver;

import java.util.Set;

sealed public interface TreeNodeType
    extends TreeLike
    permits
        TreeNodeType.AnyNode,
        TreeNodeType.AnyNodeFromGrammar,
        TreeNodeType.NodeType
{
    record AnyNode()
            implements TreeNodeType
    {}

    record AnyNodeFromGrammar(String grammar)
            implements TreeNodeType, GrammarConstrained
    {}

    record NodeType(
            String grammar,
            Set<NamespaceResolver.QualifiedName> elementNames
    ) implements TreeNodeType, GrammarConstrained, NamesConstrained
    {}
}
