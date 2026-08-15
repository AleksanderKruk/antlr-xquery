package com.github.akruk.antlrquery.inputgrammaranalyzer;

import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;

public final class RuleEdge {
    NamespaceResolver.QualifiedName from;
    NamespaceResolver.QualifiedName to;
    Cardinality operator;

    RuleEdge(NamespaceResolver.QualifiedName from, NamespaceResolver.QualifiedName to, Cardinality operator) {
        this.from = from;
        this.to = to;
        this.operator = operator;
    }

    @Override
    public String toString() {
        return this.from + " -> " + this.to + " " + this.operator;
    }
}
