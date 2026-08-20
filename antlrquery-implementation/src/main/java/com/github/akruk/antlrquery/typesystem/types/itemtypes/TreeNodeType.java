package com.github.akruk.antlrquery.typesystem.types.itemtypes;

import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver;
import com.github.akruk.antlrquery.typesystem.typeoperations.stringify.Stringify;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Objects;
import java.util.Set;

@DefaultQualifier(NonNull.class)
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
        public String toString() {
            return Stringify.stringify(this);
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
        public String toString() {
            return Stringify.stringify(this);
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
        public NodeType(
                String grammar,
                Set<NamespaceResolver.QualifiedName> elementNames
        ) {
            this.grammar = Objects.requireNonNull(grammar);
            this.elementNames = Objects.requireNonNull(elementNames);
        }
        @Override
        public String toString() {
            return Stringify.stringify(this);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof NodeType(String grammar1, Set<NamespaceResolver.QualifiedName> names)
                    && Objects.equals(grammar1, this.grammar)
                    && Objects.equals(names, this.elementNames);
        }
    }
}
