package com.github.akruk.antlrquery.inputgrammaranalyzer;

import java.io.PrintStream;
import java.util.*;

import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver.QualifiedName;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.Cardinality.CardinalityValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class DescendantCardinalityAnalyzer {

    final Map<QualifiedName, List<RuleEdge>> graph;
    private Map<QualifiedName, Map<QualifiedName, Cardinality>> result;
    final private Set<QualifiedName> allNames;

    public DescendantCardinalityAnalyzer(RuleGraph graph, final Set<QualifiedName> allNames) {
        this.graph = graph.graph();
        this.allNames = allNames;
        result = new HashMap<>();
    }


    // Helper method to check if a cardinality is unbounded (contains POSITIVE_INFINITY)
    private boolean isUnbounded(Cardinality card) {
        return card.toIntervals().stream()
                .anyMatch(interval -> interval.upperBound().equals(CardinalityValue.POSITIVE_INFINITY));
    }

    Map<QualifiedName, Map<QualifiedName, Cardinality>> analyzeAll() {
        result = new HashMap<>();
        for (QualifiedName rule : graph.keySet()) {
            Map<QualifiedName, Cardinality> desc = new HashMap<>();
            analyze(rule, rule, new ArrayList<>(), desc);
            result.put(rule, desc);
        }
        propagateRecursion();
        for (QualifiedName rule : result.keySet()) {
            Map<QualifiedName, Cardinality> descendants = result.get(rule);
            for (var name : allNames) {
                descendants.putIfAbsent(name, Cardinality.ZERO);
            }
        }
        return result;
    }

    void analyze(
            QualifiedName start,
            QualifiedName current,
            List<QualifiedName> path,
            Map<QualifiedName, Cardinality> descendantToCardinality
    ) {
        path.add(current);

        for (RuleEdge edge : graph.getOrDefault(current, List.of())) {
            QualifiedName target = edge.to;
            Cardinality edgeOp = edge.operator;

            // Direct edge: current -> target
            descendantToCardinality.merge(target, edgeOp, Cardinalities::multiply);

            // Recursion: current -> ... -> start
            if (target.equals(start) && path.size() > 1) {
                Cardinality recursionCard = Objects.requireNonNull(Cardinalities.recursionMerge(edgeOp));
                descendantToCardinality.merge(start, recursionCard, Cardinalities::add);
                continue;
            }

            // Cycle: current -> ... -> target (already in path)
            if (path.contains(target)) {
                Cardinality cycleCard = Objects.requireNonNull(Cardinalities.recursionMerge(edgeOp));
                descendantToCardinality.merge(start, cycleCard, Cardinalities::add);
                continue;
            }

            // Recursive analysis
            Map<QualifiedName, Cardinality> subDesc = new HashMap<>();
            analyze(start, target, new ArrayList<>(path), subDesc);

            // Propagate cardinalities from subDesc to desc
            for (Map.Entry<QualifiedName, Cardinality> entry : subDesc.entrySet()) {
                QualifiedName subTarget = entry.getKey();
                Cardinality subCardinality = entry.getValue();
                Cardinality adjusted = Cardinalities.multiply(subCardinality, edgeOp);
                descendantToCardinality.merge(subTarget, adjusted, Cardinalities::multiply);
            }
        }
    }

    void propagateRecursion() {
        for (QualifiedName rule : result.keySet()) {
            Map<QualifiedName, Cardinality> desc = result.get(rule);
            Cardinality selfCard = desc.getOrDefault(rule, Cardinality.ZERO);

            // Check if selfCard is unbounded (contains POSITIVE_INFINITY)
            if (isUnbounded(selfCard)) {
                for (QualifiedName target : desc.keySet()) {
                    if (!target.equals(rule)) {
                        Cardinality current = desc.get(target);
                        Cardinality elevated = Cardinalities.add(current, selfCard);
                        desc.put(target, elevated);
                    }
                }
            }
        }
    }

    public void printResults(PrintStream out) {
        for (QualifiedName rule : result.keySet()) {
            out.println("Descendants of " + rule + ":");
            result.get(rule).forEach((k, v) ->
                    out.println("  " + k + " -> " + Cardinalities.stringify(v)));
            out.println();
        }
    }

    static void main(String[] args) {
        final QualifiedName x = new QualifiedName("", "x");
        final QualifiedName a = new QualifiedName("", "a");
        final QualifiedName b = new QualifiedName("", "b");
        final QualifiedName c = new QualifiedName("", "c");

        final RuleGraph graph = new RuleGraph(new LinkedHashMap<>());
        // x: a b c;
        graph.addRule(x, a, Cardinality.ONE);
        graph.addRule(x, b, Cardinality.ONE);
        graph.addRule(x, c, Cardinality.ONE);
        // b: x?
        graph.addRule(b, x, Cardinality.ZERO_OR_ONE);

        final DescendantCardinalityAnalyzer analyzer = new DescendantCardinalityAnalyzer(graph, Set.of(x, a, b, c));

        analyzer.analyzeAll();
        analyzer.printResults(System.out);
    }

}

