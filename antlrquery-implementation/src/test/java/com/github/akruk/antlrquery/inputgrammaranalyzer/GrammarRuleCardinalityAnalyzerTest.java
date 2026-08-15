package com.github.akruk.antlrquery.inputgrammaranalyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver.QualifiedName;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;

public class GrammarRuleCardinalityAnalyzerTest {

    QualifiedName x = new QualifiedName("", "x");
    QualifiedName a = new QualifiedName("", "a");
    QualifiedName b = new QualifiedName("", "b");
    QualifiedName c = new QualifiedName("", "c");

    @Test
    void a() {
        var graph = new RuleGraph(new LinkedHashMap<>());
        graph.addRule(x, a, Cardinality.ONE);
        graph.addRule(x, b, Cardinality.ONE);
        graph.addRule(x, c, Cardinality.ONE);

        // b: x?
        graph.addRule(b, x, Cardinality.ZERO_OR_ONE);
        DescendantCardinalityAnalyzer analyzer = new DescendantCardinalityAnalyzer(graph,
                Set.of(x, a, b, c));
        // x: a b c;

        var r = analyzer.analyzeAll();
        Map<QualifiedName, Cardinality> xDescendants = r.get(x);
        Cardinality xx = xDescendants.get(x);
        Cardinality xa = xDescendants.get(a);
        Cardinality xb = xDescendants.get(b);
        Cardinality xc = xDescendants.get(c);
        assertEquals(Cardinality.ZERO_OR_MORE, xx);
        assertEquals(Cardinality.ONE_OR_MORE, xa);
        assertEquals(Cardinality.ONE_OR_MORE, xb);
        assertEquals(Cardinality.ONE_OR_MORE, xc);

        Map<QualifiedName, Cardinality> bDescendants = r.get(b);
        Cardinality bx = bDescendants.get(x);
        Cardinality ba = bDescendants.get(a);
        Cardinality bb = bDescendants.get(b);
        Cardinality bc = bDescendants.get(c);
        assertEquals(Cardinality.ZERO_OR_MORE, bx);
        assertEquals(Cardinality.ZERO_OR_MORE, ba);
        assertEquals(Cardinality.ZERO_OR_MORE, bb);
        assertEquals(Cardinality.ZERO_OR_MORE, bc);
    }


    @Test
    void b() {
        var graph = new RuleGraph(new LinkedHashMap<>());
        DescendantCardinalityAnalyzer analyzer = new DescendantCardinalityAnalyzer(graph, Set.of(x, a));
        // x: a a;
        graph.addRule(x, a, Cardinality.ONE);
        graph.addRule(x, a, Cardinality.ONE);
        var r = analyzer.analyzeAll();
        Map<QualifiedName, Cardinality> xDescendants = r.get(x);
        Cardinality xa = xDescendants.get(a);
        assertEquals(Cardinality.of(2), xa);
    }
}
