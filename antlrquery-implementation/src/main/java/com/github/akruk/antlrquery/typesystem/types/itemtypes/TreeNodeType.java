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

        @Override
        public boolean equals(Object obj) {
           return obj instanceof AnyNode;
        }
    }

    record AnyNodeFromGrammar(String grammar)
            implements TreeNodeType, GrammarConstrained
    {
        @Override
        public @NonNull String toString() {
            return ItemTypes.stringify(this);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof AnyNodeFromGrammar(String grammar1)
                    && grammar1.equals(this.grammar);
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

        @Override
        public boolean equals(Object obj) {
            return obj instanceof NodeType(String grammar1, Set<NamespaceResolver.QualifiedName> names)
                    && grammar1.equals(this.grammar)
                    && names.equals(this.elementNames);
        }
    }
}
