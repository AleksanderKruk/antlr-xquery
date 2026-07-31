package com.github.akruk.antlrquery.typesystem.types.itemtypes;

import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver;
import com.github.akruk.antlrquery.typesystem.types.ItemTypes;
import org.checkerframework.checker.nullness.qual.NonNull;

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
    {
        @Override
        public @NonNull String toString() {
            return ItemTypes.stringify(this);
        }

    }

    record AnyNodeFromGrammar(String grammar)
            implements TreeNodeType, GrammarConstrained
    {
        @Override
        public @NonNull String toString() {
            return ItemTypes.stringify(this);
        }

    }

    record NodeType(
            String grammar,
            Set<NamespaceResolver.QualifiedName> elementNames
    ) implements TreeNodeType, GrammarConstrained, NamesConstrained
    {
        @Override
        public @NonNull String toString() {
            return ItemTypes.stringify(this);
        }

    }
}
