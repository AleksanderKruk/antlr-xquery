import java.util.*;

public class XQueryDescendantCardinalityTest {

    public enum XQueryCardinality {
        ONE,
        ZERO,
        ZERO_OR_ONE,
        ONE_OR_MORE,
        ZERO_OR_MORE;

        String occurenceSuffix() {
            return switch (this) {
                case ZERO -> "";
                case ONE -> "";
                case ZERO_OR_ONE -> "?";
                case ZERO_OR_MORE -> "*";
                case ONE_OR_MORE -> "+";
            };
        }
    }

    static class RuleEdge {
        String from;
        String to;
        XQueryCardinality operator;

        RuleEdge(String from, String to, XQueryCardinality operator) {
            this.from = from;
            this.to = to;
            this.operator = operator;
        }
    }

    static class Analyzer {
        Map<String, List<RuleEdge>> graph = new HashMap<>();
        Map<String, Map<String, XQueryCardinality>> result = new HashMap<>();

        void addRule(String from, String to, XQueryCardinality op) {
            graph.computeIfAbsent(from, _ -> new ArrayList<>()).add(new RuleEdge(from, to, op));
        }

        void analyzeAll() {
            for (String rule : graph.keySet()) {
                Map<String, XQueryCardinality> desc = new HashMap<>();
                analyze(rule, rule, new ArrayList<>(), desc);
                result.put(rule, desc);
            }
            propagateRecursion();
        }

        void analyze(String start, String current, List<String> path, Map<String, XQueryCardinality> desc) {
            path.add(current);
            for (RuleEdge edge : graph.getOrDefault(current, List.of())) {
                String target = edge.to;
                XQueryCardinality op = edge.operator;

                if (target.equals(start) && path.size() > 1) {
                    desc.put(target, mergeMax(desc.get(target), XQueryCardinality.ZERO_OR_MORE));
                    continue;
                }
                if (path.contains(target) && !target.equals(start)) {
                    desc.put(target, mergeMax(desc.get(target), XQueryCardinality.ONE_OR_MORE));
                    continue;
                }

                Map<String, XQueryCardinality> subDesc = new HashMap<>();
                analyze(start, target, new ArrayList<>(path), subDesc);

                for (Map.Entry<String, XQueryCardinality> entry : subDesc.entrySet()) {
                    String subTarget = entry.getKey();
                    XQueryCardinality adjusted = applyIncomingOperator(entry.getValue(), op);
                    desc.put(subTarget, mergeMax(desc.get(subTarget), adjusted));
                }

                desc.put(target, mergeMax(desc.get(target), op));
            }
        }

        void propagateRecursion() {
            for (String rule : result.keySet()) {
                Map<String, XQueryCardinality> desc = result.get(rule);
                XQueryCardinality selfCard = desc.get(rule);
                if (selfCard == XQueryCardinality.ZERO_OR_MORE || selfCard == XQueryCardinality.ONE_OR_MORE) {
                    for (String target : desc.keySet()) {
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
            if (incomingOp == XQueryCardinality.ZERO_OR_ONE || incomingOp == XQueryCardinality.ZERO_OR_MORE) {
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
            for (String rule : result.keySet()) {
                System.out.println("Descendants of " + rule + ":");
                result.get(rule).forEach((k, v) ->
                    System.out.println("  " + k + " -> " + v + " " + v.occurenceSuffix()));
                System.out.println();
            }
        }
    }

    public static void main(String[] args) {
        Analyzer analyzer = new Analyzer();

        // x: a b c;
        analyzer.addRule("x", "a", XQueryCardinality.ONE);
        analyzer.addRule("x", "b", XQueryCardinality.ONE);
        analyzer.addRule("x", "c", XQueryCardinality.ONE);

        // b: x?
        analyzer.addRule("b", "x", XQueryCardinality.ZERO_OR_ONE);

        analyzer.analyzeAll();
        analyzer.printResults();
    }
}
