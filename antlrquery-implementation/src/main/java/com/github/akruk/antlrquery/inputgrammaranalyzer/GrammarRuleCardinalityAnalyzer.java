package com.github.akruk.antlrquery.inputgrammaranalyzer;
import java.util.*;

import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver.QualifiedName;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;

public class GrammarRuleCardinalityAnalyzer {

    static class RuleEdge {
        QualifiedName from;
        QualifiedName to;
        Cardinality operator;

        RuleEdge(QualifiedName from, QualifiedName to, Cardinality operator) {
            this.from = from;
            this.to = to;
            this.operator = operator;
        }
    }

    final Map<QualifiedName, List<RuleEdge>> graph;

    private Map<QualifiedName, Map<QualifiedName, Cardinality>> result;
    final private Set<QualifiedName> allNames;
    final private Map<QualifiedName, Cardinality> emptyMapping;

    public GrammarRuleCardinalityAnalyzer(
        final Set<QualifiedName> allNames
    )
    {
        this(new HashMap<>(), allNames);
    }

    public GrammarRuleCardinalityAnalyzer(
        final Map<QualifiedName, List<RuleEdge>> graph,
        final Set<QualifiedName> allNames
    )
    {
        this.graph = graph;
        this.allNames = allNames;
        this.emptyMapping = new HashMap<>(allNames.size(), 1);
        for (var n : allNames) {
            emptyMapping.put(n, Cardinality.ZERO);
        }
        result = new HashMap<>();
    }

    void addRule(QualifiedName from, QualifiedName to, Cardinality op) {
        var edges = graph.computeIfAbsent(from, _ -> new ArrayList<>());
        for (var edge : edges) {
            if (edge.to.equals(to)) {
                edge.operator = Cardinalities.sequenceMerge(edge.operator, op);
                return;
            }
        }
        edges.add(new RuleEdge(from, to, op));
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
        for (var name : allNames) {
            result.putIfAbsent(name, emptyMapping);
        }

        return result;
    }

    void analyze(QualifiedName start, QualifiedName current, List<QualifiedName> path, Map<QualifiedName, Cardinality> desc) {
        path.add(current);
        for (RuleEdge edge : graph.getOrDefault(current, List.of())) {
            QualifiedName target = edge.to;
            Cardinality op = edge.operator;

            if (target.equals(start) && path.size() > 1) {
                desc.put(target, mergeMax(desc.get(target), Cardinality.ZERO_OR_MORE));
                continue;
            }
            if (path.contains(target) && !target.equals(start)) {
                desc.put(target, mergeMax(desc.get(target), Cardinality.ONE_OR_MORE));
                continue;
            }

            Map<QualifiedName, Cardinality> subDesc = new HashMap<>();
            analyze(start, target, new ArrayList<>(path), subDesc);

            for (Map.Entry<QualifiedName, Cardinality> entry : subDesc.entrySet()) {
                QualifiedName subTarget = entry.getKey();
                Cardinality adjusted = applyIncomingOperator(entry.getValue(), op);
                desc.put(subTarget, mergeMax(desc.get(subTarget), adjusted));
            }

            desc.put(target, mergeMax(desc.get(target), op));
        }
    }

    void propagateRecursion() {
        for (QualifiedName rule : result.keySet()) {
            Map<QualifiedName, Cardinality> desc = result.get(rule);
            Cardinality selfCard = desc.get(rule);
            if (selfCard == Cardinality.ZERO_OR_MORE || selfCard == Cardinality.ONE_OR_MORE) {
                for (QualifiedName target : desc.keySet()) {
                    if (!target.equals(rule)) {
                        Cardinality elevated = elevate(desc.get(target), selfCard);
                        desc.put(target, mergeMax(desc.get(target), elevated));
                    }
                }
            }
        }
    }

    Cardinality elevate(Cardinality original, Cardinality recursion) {
        if (recursion == Cardinality.ZERO_OR_MORE) {
            if (original == Cardinality.ONE) {
                return Cardinality.ONE_OR_MORE;
            } else if (original == Cardinality.ZERO_OR_ONE) {
                return Cardinality.ZERO_OR_MORE;
            } else {
                return original;
            }
        }
        if (recursion == Cardinality.ONE_OR_MORE) {
            if (original == Cardinality.ONE) {
                return Cardinality.ONE_OR_MORE;
            } else {
                return original;
            }
        }
        return original;
    }

    Cardinality applyIncomingOperator(Cardinality original, Cardinality incomingOp) {
        if (incomingOp == Cardinality.ZERO_OR_ONE
            || incomingOp == Cardinality.ZERO_OR_MORE)
        {
            if (original == Cardinality.ONE) {
                return Cardinality.ZERO_OR_ONE;
            } else if (original == Cardinality.ONE_OR_MORE) {
                return Cardinality.ZERO_OR_MORE;
            } else if (original == Cardinality.ZERO_OR_ONE) {
                return Cardinality.ZERO_OR_ONE;
            } else {
                return original;
            }
        }
        if (incomingOp == Cardinality.ONE_OR_MORE) {
            if (original == Cardinality.ONE) {
                return Cardinality.ONE_OR_MORE;
            } else {
                return original;
            }
        }
        return original;
    }

    Cardinality mergeMax(Cardinality a, Cardinality b) {
        if (a == null) return b;
        if (a == Cardinality.ZERO_OR_MORE || b == Cardinality.ZERO_OR_MORE) return Cardinality.ZERO_OR_MORE;
        if (a == Cardinality.ONE_OR_MORE || b == Cardinality.ONE_OR_MORE) return Cardinality.ONE_OR_MORE;
        if (a == Cardinality.ZERO_OR_ONE || b == Cardinality.ZERO_OR_ONE) return Cardinality.ZERO_OR_ONE;
        if (a == Cardinality.ONE || b == Cardinality.ONE) return Cardinality.ONE;
        return Cardinality.ZERO;
    }

    void printResults() {
        for (QualifiedName rule : result.keySet()) {
            System.out.println("Descendants of " + rule + ":");
            result.get(rule).forEach((k, v) ->
                System.out.println("  " + k + " -> " + v + " " + Cardinalities.stringify(v)));
            System.out.println();
        }
    }

    public static void main(String[] args) {

        QualifiedName x = new QualifiedName("", "x");
        QualifiedName a = new QualifiedName("", "a");
        QualifiedName b = new QualifiedName("", "b");
        QualifiedName c = new QualifiedName("", "c");

        GrammarRuleCardinalityAnalyzer analyzer = new GrammarRuleCardinalityAnalyzer(
            Set.of(x, a, b, c)
        );

        // x: a b c;
        analyzer.addRule(x, a, Cardinality.ONE);
        analyzer.addRule(x, b, Cardinality.ONE);
        analyzer.addRule(x, c, Cardinality.ONE);

        // b: x?
        analyzer.addRule(b, x, Cardinality.ZERO_OR_ONE);

        analyzer.analyzeAll();
        analyzer.printResults();
    }
}
