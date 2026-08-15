package com.github.akruk.antlrquery.inputgrammaranalyzer;

import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;

public record RuleGraph(LinkedHashMap<NamespaceResolver.QualifiedName, List<RuleEdge>> graph) {
    public void transformRule(NamespaceResolver.QualifiedName from, NamespaceResolver.QualifiedName to, Cardinality op, Function<Cardinality[], Cardinality> transformation) {
        var edges = graph.computeIfAbsent(from, _ -> new ArrayList<>());
        var edge = edges.stream()
                .filter(ruleEdge -> ruleEdge.to.equals(to))
                .findAny();
        edge.ifPresentOrElse(
                ruleEdge -> ruleEdge.operator = transformation.apply(new Cardinality[]{ruleEdge.operator, op}),
                () -> edges.add(new RuleEdge(from, to, op))
        );
    }
    public void addRule(NamespaceResolver.QualifiedName from, NamespaceResolver.QualifiedName to, Cardinality op) {
        transformRule(from, to, op, Cardinalities::add);
    }

}
