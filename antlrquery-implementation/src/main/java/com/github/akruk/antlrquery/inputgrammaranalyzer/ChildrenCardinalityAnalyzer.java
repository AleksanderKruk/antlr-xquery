package com.github.akruk.antlrquery.inputgrammaranalyzer;

import java.util.*;

import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver.QualifiedName;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class ChildrenCardinalityAnalyzer {
    final Map<QualifiedName, List<RuleEdge>> graph;
    final Map<QualifiedName, Map<QualifiedName, Cardinality>> children;

    public ChildrenCardinalityAnalyzer(RuleGraph graph) {
        this.graph = graph.graph();
        this.children = new HashMap<>();
    }

    public Map<QualifiedName, Map<QualifiedName, Cardinality>> analyzeAll() {
        children.clear();
        // Step 1: Collect direct children cardinalities
        for (Map.Entry<QualifiedName, List<RuleEdge>> entry : graph.entrySet()) {
            QualifiedName from = entry.getKey();
            Map<QualifiedName, Cardinality> childrenOfFrom = new HashMap<>();
            for (RuleEdge edge : entry.getValue()) {
                QualifiedName to = edge.to;
                Cardinality existing = childrenOfFrom.get(to);
                if (existing == null) {
                    childrenOfFrom.put(to, edge.operator);
                } else {
                    // Union if multiple edges to the same child
                    childrenOfFrom.put(to, Cardinalities.union(existing, edge.operator));
                }
            }
            children.put(from, childrenOfFrom);
        }
        return children;
    }

    public Map<QualifiedName, Cardinality> getChildren(QualifiedName node) {
        return children.getOrDefault(node, Map.of());
    }

    public void printResults(java.io.PrintStream out) {
        for (QualifiedName rule : children.keySet()) {
            out.println("Children of " + rule + ":");
            children.get(rule).forEach((k, v) ->
                    out.println("  " + k + " -> " + Cardinalities.stringify(v)));
            out.println();
        }
    }
    static void main(String[] args) {
        QualifiedName x = new QualifiedName("", "x");
        QualifiedName a = new QualifiedName("", "a");
        QualifiedName b = new QualifiedName("", "b");
        QualifiedName c = new QualifiedName("", "c");

        Set<QualifiedName> allNames = Set.of(x, a, b, c);

        RuleGraph graph = new RuleGraph(new LinkedHashMap<>());
        graph.addRule(x, a, Cardinality.ONE);
        graph.addRule(x, b, Cardinality.ONE);
        graph.addRule(x, c, Cardinality.ONE);
        graph.addRule(b, x, Cardinality.ZERO_OR_ONE);

        DescendantCardinalityAnalyzer descendantAnalyzer = new DescendantCardinalityAnalyzer(graph, allNames);
        ChildrenCardinalityAnalyzer childrenAnalyzer = new ChildrenCardinalityAnalyzer(graph);

        Map<QualifiedName, Map<QualifiedName, Cardinality>> children = childrenAnalyzer.analyzeAll();
        Map<QualifiedName, Map<QualifiedName, Cardinality>> descendants = descendantAnalyzer.analyzeAll();

        System.out.println("=== Children Cardinalities ===");
        childrenAnalyzer.printResults(System.out);

        System.out.println("=== Descendant Cardinalities ===");
        descendantAnalyzer.printResults(System.out);
    }
}
