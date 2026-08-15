package com.github.akruk.antlrquery.inputgrammaranalyzer;

import java.util.*;

import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver.QualifiedName;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class AncestorCardinalityAnalyzer {

    final Map<QualifiedName, List<RuleEdge>> reversedGraph;
    private Map<QualifiedName, Map<QualifiedName, Cardinality>> result;
    final private Set<QualifiedName> allNames;

    public AncestorCardinalityAnalyzer(RuleGraph graph, Set<QualifiedName> allNames) {
        this.allNames = allNames;
        this.reversedGraph = new HashMap<>();

        for (Map.Entry<QualifiedName, List<RuleEdge>> entry : graph.graph().entrySet()) {
            QualifiedName from = entry.getKey();
            for (RuleEdge edge : entry.getValue()) {
                Cardinality optionalizedOp = Objects.requireNonNull(Cardinalities.optionalize(edge.operator));
                reversedGraph.computeIfAbsent(edge.to, _ -> new ArrayList<>())
                        .add(new RuleEdge(edge.to, from, optionalizedOp));
            }
        }
        result = new HashMap<>();
    }

    Map<QualifiedName, Map<QualifiedName, Cardinality>> analyzeAll() {
        result = new HashMap<>();
        for (QualifiedName node : allNames) {
            Map<QualifiedName, Cardinality> ancestors = new HashMap<>();
            analyze(node, ancestors);
            for (QualifiedName name : allNames) {
                ancestors.putIfAbsent(name, Cardinality.ZERO);
            }
            result.put(node, ancestors);
        }
        return result;
    }

    void analyze(QualifiedName start, Map<QualifiedName, Cardinality> ancestorToCardinality) {
        Queue<Pair<QualifiedName, Cardinality>> queue = new LinkedList<>();
        Set<QualifiedName> visited = new HashSet<>();

        queue.add(new Pair<>(start, Cardinality.ONE));

        while (!queue.isEmpty()) {
            Pair<QualifiedName, Cardinality> current = queue.poll();
            QualifiedName currentNode = current.first;
            Cardinality currentCard = current.second;

            if (visited.contains(currentNode)) {
                continue;
            }
            visited.add(currentNode);

            for (RuleEdge edge : reversedGraph.getOrDefault(currentNode, List.of())) {
                QualifiedName target = edge.to;
                Cardinality edgeOp = edge.operator;

                Cardinality newCard = Cardinalities.multiply(currentCard, edgeOp);
                if (newCard.equals(Cardinality.ZERO)) {
                    continue;
                }

                if (target.equals(start) && !currentNode.equals(start)) {
                    Cardinality recursionCard = Objects.requireNonNull(Cardinalities.recursionMerge(newCard));
                    ancestorToCardinality.merge(start, recursionCard, Cardinalities::union);
                }
                else if (visited.contains(target) && !target.equals(start)) {
                    Cardinality cycleCard = Objects.requireNonNull(Cardinalities.recursionMerge(newCard));
                    ancestorToCardinality.merge(target, cycleCard, Cardinalities::union);
                }
                else {
                    ancestorToCardinality.merge(target, newCard, Cardinalities::union);
                }

                if (!visited.contains(target)) {
                    queue.add(new Pair<>(target, newCard));
                }
            }
        }
    }

    private record Pair<A, B>(A first, B second) {
    }
}
