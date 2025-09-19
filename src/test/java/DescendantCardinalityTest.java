import java.util.*;

public class DescendantCardinalityTest {

    enum Cardinality {
        ZERO, ONE, QUESTION, STAR, PLUS;

        static Cardinality mergeSequence(Cardinality a, Cardinality b) {
            if (a == ZERO || b == ZERO) return ZERO;
            if (a == ONE && b == ONE) return PLUS;
            if (a == ONE && b == QUESTION) return QUESTION;
            if (a == ONE && b == STAR) return PLUS;
            if (a == ONE && b == PLUS) return PLUS;
            if (a == QUESTION || b == QUESTION) return QUESTION;
            if (a == STAR || b == STAR) return STAR;
            if (a == PLUS || b == PLUS) return PLUS;
            return QUESTION;
        }

        static Cardinality mergeAlternative(List<Cardinality> list) {
            if (list.contains(STAR)) return STAR;
            if (list.contains(PLUS)) return PLUS;
            if (list.contains(QUESTION)) return QUESTION;
            if (list.contains(ONE)) return ONE;
            return ZERO;
        }

        static Cardinality elevate(Cardinality original, Cardinality recursion) {
            if (recursion == STAR) {
                switch (original) {
                    case ONE: return PLUS;
                    case QUESTION: return STAR;
                    default: return original;
                }
            }
            if (recursion == PLUS) {
                switch (original) {
                    case ONE: return PLUS;
                    default: return original;
                }
            }
            return original;
        }

        static Cardinality applyIncomingOperator(Cardinality original, Cardinality incomingOp) {
            if (incomingOp == QUESTION || incomingOp == STAR) {
                switch (original) {
                    case ONE: return QUESTION;
                    case PLUS: return STAR;
                    case QUESTION: return QUESTION;
                    default: return original;
                }
            }
            if (incomingOp == PLUS) {
                switch (original) {
                    case ONE: return PLUS;
                    default: return original;
                }
            }
            return original;
        }
    }

    static class RuleEdge {
        String from;
        String to;
        Cardinality operator;

        RuleEdge(String from, String to, Cardinality operator) {
            this.from = from;
            this.to = to;
            this.operator = operator;
        }
    }

    static class Analyzer {
        Map<String, List<RuleEdge>> graph = new HashMap<>();
        Map<String, Map<String, Cardinality>> result = new HashMap<>();

        void addRule(String from, String to, Cardinality op) {
            graph.computeIfAbsent(from, k -> new ArrayList<>()).add(new RuleEdge(from, to, op));
        }

        void analyzeAll() {
            for (String rule : graph.keySet()) {
                Map<String, Cardinality> desc = new HashMap<>();
                analyze(rule, rule, new ArrayList<>(), desc);
                result.put(rule, desc);
            }
            propagateRecursion();
        }

        void analyze(String start, String current, List<String> path, Map<String, Cardinality> desc) {
            path.add(current);
            for (RuleEdge edge : graph.getOrDefault(current, List.of())) {
                String target = edge.to;
                Cardinality op = edge.operator;
                if (target.equals(start) && path.size() > 1) {
                    desc.put(target, mergeMax(desc.get(target), Cardinality.STAR));
                    continue;
                }
                if (path.contains(target) && !target.equals(start)) {
                    desc.put(target, mergeMax(desc.get(target), Cardinality.PLUS));
                    continue;
                }

                Map<String, Cardinality> subDesc = new HashMap<>();
                analyze(start, target, new ArrayList<>(path), subDesc);

                for (Map.Entry<String, Cardinality> entry : subDesc.entrySet()) {
                    String subTarget = entry.getKey();
                    Cardinality adjusted = Cardinality.applyIncomingOperator(entry.getValue(), op);
                    desc.put(subTarget, mergeMax(desc.get(subTarget), adjusted));
                }

                desc.put(target, mergeMax(desc.get(target), op));
            }
        }

        void propagateRecursion() {
            for (String rule : result.keySet()) {
                Map<String, Cardinality> desc = result.get(rule);
                Cardinality selfCard = desc.get(rule);
                if (selfCard == Cardinality.STAR || selfCard == Cardinality.PLUS) {
                    for (String target : desc.keySet()) {
                        if (!target.equals(rule)) {
                            Cardinality elevated = Cardinality.elevate(desc.get(target), selfCard);
                            desc.put(target, mergeMax(desc.get(target), elevated));
                        }
                    }
                }
            }
        }

        Cardinality mergeMax(Cardinality a, Cardinality b) {
            if (a == null) return b;
            if (a == Cardinality.STAR || b == Cardinality.STAR) return Cardinality.STAR;
            if (a == Cardinality.PLUS || b == Cardinality.PLUS) return Cardinality.PLUS;
            if (a == Cardinality.QUESTION || b == Cardinality.QUESTION) return Cardinality.QUESTION;
            if (a == Cardinality.ONE || b == Cardinality.ONE) return Cardinality.ONE;
            return Cardinality.ZERO;
        }

        void printResults() {
            for (String rule : result.keySet()) {
                System.out.println("Descendants of " + rule + ":");
                result.get(rule).forEach((k, v) -> System.out.println("  " + k + " → " + v));
                System.out.println();
            }
        }
    }

    public static void main(String[] args) {
        Analyzer analyzer = new Analyzer();

        // x: a b c;
        analyzer.addRule("x", "a", Cardinality.ONE);
        analyzer.addRule("x", "b", Cardinality.ONE);
        analyzer.addRule("x", "c", Cardinality.ONE);

        // b: x?
        analyzer.addRule("b", "x", Cardinality.QUESTION);

        analyzer.analyzeAll();
        analyzer.printResults();
    }
}
