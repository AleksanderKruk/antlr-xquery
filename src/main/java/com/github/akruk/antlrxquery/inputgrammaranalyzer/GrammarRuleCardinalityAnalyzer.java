package com.github.akruk.antlrxquery.inputgrammaranalyzer;
import java.util.*;

import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver.QualifiedName;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryCardinality;
import com.github.akruk.antlrxquery.typesystem.typeoperations.occurence.SequenceCardinalityMerger;

public class GrammarRuleCardinalityAnalyzer {

    static class RuleEdge {
        QualifiedName from;
        QualifiedName to;
        XQueryCardinality operator;

        RuleEdge(QualifiedName from, QualifiedName to, XQueryCardinality operator) {
            this.from = from;
            this.to = to;
            this.operator = operator;
        }
    }

    final Map<QualifiedName, List<RuleEdge>> graph;
    final SequenceCardinalityMerger sequenceCardinalityMerger;

    private Map<QualifiedName, Map<QualifiedName, XQueryCardinality>> result;
    final private Set<QualifiedName> allNames;
    final private Map<QualifiedName, XQueryCardinality> emptyMapping;

    public GrammarRuleCardinalityAnalyzer(
        final Set<QualifiedName> allNames
    )
    {
        this(new HashMap<>(), new SequenceCardinalityMerger(), allNames);
    }

    public GrammarRuleCardinalityAnalyzer(
        final Map<QualifiedName, List<RuleEdge>> graph,
        final SequenceCardinalityMerger sequenceCardinalityMerger,
        final Set<QualifiedName> allNames
    )
    {
        this.graph = graph;
        this.allNames = allNames;
        this.emptyMapping = new HashMap<>(allNames.size(), 1);
        for (var n : allNames) {
            emptyMapping.put(n, XQueryCardinality.ZERO);
        }
        result = new HashMap<>();
        this.sequenceCardinalityMerger = sequenceCardinalityMerger;
    }

    void addRule(QualifiedName from, QualifiedName to, XQueryCardinality op) {
        var edges = graph.computeIfAbsent(from, _ -> new ArrayList<>());
        for (var edge : edges) {
            if (edge.to.equals(to)) {
                edge.operator = sequenceCardinalityMerger.merge(edge.operator, op);
                return;
            }
        }
        edges.add(new RuleEdge(from, to, op));
    }

    Map<QualifiedName, Map<QualifiedName, XQueryCardinality>> analyzeAll() {
        result = new HashMap<>();
        for (QualifiedName rule : graph.keySet()) {
            Map<QualifiedName, XQueryCardinality> desc = new HashMap<>();
            analyze(rule, rule, new ArrayList<>(), desc);
            result.put(rule, desc);
        }
        propagateRecursion();
        for (QualifiedName rule : result.keySet()) {
            Map<QualifiedName, XQueryCardinality> descendants = result.get(rule);
            for (var name : allNames) {
                descendants.putIfAbsent(name, XQueryCardinality.ZERO);
            }
        }
        for (var name : allNames) {
            result.putIfAbsent(name, emptyMapping);
        }

        return result;
    }

    void analyze(QualifiedName start, QualifiedName current, List<QualifiedName> path, Map<QualifiedName, XQueryCardinality> desc) {
        path.add(current);
        for (RuleEdge edge : graph.getOrDefault(current, List.of())) {
            QualifiedName target = edge.to;
            XQueryCardinality op = edge.operator;

            if (target.equals(start) && path.size() > 1) {
                desc.put(target, mergeMax(desc.get(target), XQueryCardinality.ZERO_OR_MORE));
                continue;
            }
            if (path.contains(target) && !target.equals(start)) {
                desc.put(target, mergeMax(desc.get(target), XQueryCardinality.ONE_OR_MORE));
                continue;
            }

            Map<QualifiedName, XQueryCardinality> subDesc = new HashMap<>();
            analyze(start, target, new ArrayList<>(path), subDesc);

            for (Map.Entry<QualifiedName, XQueryCardinality> entry : subDesc.entrySet()) {
                QualifiedName subTarget = entry.getKey();
                XQueryCardinality adjusted = applyIncomingOperator(entry.getValue(), op);
                desc.put(subTarget, mergeMax(desc.get(subTarget), adjusted));
            }

            desc.put(target, mergeMax(desc.get(target), op));
        }
    }

    void propagateRecursion() {
        for (QualifiedName rule : result.keySet()) {
            Map<QualifiedName, XQueryCardinality> desc = result.get(rule);
            XQueryCardinality selfCard = desc.get(rule);
            if (selfCard == XQueryCardinality.ZERO_OR_MORE || selfCard == XQueryCardinality.ONE_OR_MORE) {
                for (QualifiedName target : desc.keySet()) {
                    if (!target.equals(rule)) {
                        XQueryCardinality elevated = elevate(desc.get(target), selfCard);
                        desc.put(target, mergeMax(desc.get(target), elevated));
                    }
                }
            }
        }
    }

    XQueryCardinality elevate(XQueryCardinality original, XQueryCardinality recursion) {
        if (recursion == XQueryCardinality.ZERO_OR_MORE) {
            return switch (original) {
                case ONE -> XQueryCardinality.ONE_OR_MORE;
                case ZERO_OR_ONE -> XQueryCardinality.ZERO_OR_MORE;
                default -> original;
            };
        }
        if (recursion == XQueryCardinality.ONE_OR_MORE) {
            return switch (original) {
                case ONE -> XQueryCardinality.ONE_OR_MORE;
                default -> original;
            };
        }
        return original;
    }

    XQueryCardinality applyIncomingOperator(XQueryCardinality original, XQueryCardinality incomingOp) {
        if (incomingOp == XQueryCardinality.ZERO_OR_ONE
            || incomingOp == XQueryCardinality.ZERO_OR_MORE)
        {
            return switch (original) {
                case ONE -> XQueryCardinality.ZERO_OR_ONE;
                case ONE_OR_MORE -> XQueryCardinality.ZERO_OR_MORE;
                case ZERO_OR_ONE -> XQueryCardinality.ZERO_OR_ONE;
                default -> original;
            };
        }
        if (incomingOp == XQueryCardinality.ONE_OR_MORE) {
            return switch (original) {
                case ONE -> XQueryCardinality.ONE_OR_MORE;
                default -> original;
            };
        }
        return original;
    }

    XQueryCardinality mergeMax(XQueryCardinality a, XQueryCardinality b) {
        if (a == null) return b;
        if (a == XQueryCardinality.ZERO_OR_MORE || b == XQueryCardinality.ZERO_OR_MORE) return XQueryCardinality.ZERO_OR_MORE;
        if (a == XQueryCardinality.ONE_OR_MORE || b == XQueryCardinality.ONE_OR_MORE) return XQueryCardinality.ONE_OR_MORE;
        if (a == XQueryCardinality.ZERO_OR_ONE || b == XQueryCardinality.ZERO_OR_ONE) return XQueryCardinality.ZERO_OR_ONE;
        if (a == XQueryCardinality.ONE || b == XQueryCardinality.ONE) return XQueryCardinality.ONE;
        return XQueryCardinality.ZERO;
    }

    void printResults() {
        for (QualifiedName rule : result.keySet()) {
            System.out.println("Descendants of " + rule + ":");
            result.get(rule).forEach((k, v) ->
                System.out.println("  " + k + " -> " + v + " " + v.occurenceSuffix()));
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
        analyzer.addRule(x, a, XQueryCardinality.ONE);
        analyzer.addRule(x, b, XQueryCardinality.ONE);
        analyzer.addRule(x, c, XQueryCardinality.ONE);

        // b: x?
        analyzer.addRule(b, x, XQueryCardinality.ZERO_OR_ONE);

        analyzer.analyzeAll();
        analyzer.printResults();
    }
}
