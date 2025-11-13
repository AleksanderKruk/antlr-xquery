package com.github.akruk.antlrxquery.inputgrammaranalyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver.QualifiedName;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryCardinality;

public class GrammarRuleCardinalityAnalyzerTest {

    QualifiedName x = new QualifiedName("", "x");
    QualifiedName a = new QualifiedName("", "a");
    QualifiedName b = new QualifiedName("", "b");
    QualifiedName c = new QualifiedName("", "c");

    @Test
    void a() {
        GrammarRuleCardinalityAnalyzer analyzer = new GrammarRuleCardinalityAnalyzer(
            Set.of(x, a, b, c)
        );
        // x: a b c;
        analyzer.addRule(x, a, XQueryCardinality.ONE);
        analyzer.addRule(x, b, XQueryCardinality.ONE);
        analyzer.addRule(x, c, XQueryCardinality.ONE);

        // b: x?
        analyzer.addRule(b, x, XQueryCardinality.ZERO_OR_ONE);

        var r = analyzer.analyzeAll();
        Map<QualifiedName, XQueryCardinality> xDescendants = r.get(x);
        XQueryCardinality xx = xDescendants.get(x);
        XQueryCardinality xa = xDescendants.get(a);
        XQueryCardinality xb = xDescendants.get(b);
        XQueryCardinality xc = xDescendants.get(c);
        assertEquals(XQueryCardinality.ZERO_OR_MORE, xx);
        assertEquals(XQueryCardinality.ONE_OR_MORE, xa);
        assertEquals(XQueryCardinality.ONE_OR_MORE, xb);
        assertEquals(XQueryCardinality.ONE_OR_MORE, xc);

        Map<QualifiedName, XQueryCardinality> bDescendants = r.get(b);
        XQueryCardinality bx = bDescendants.get(x);
        XQueryCardinality ba = bDescendants.get(a);
        XQueryCardinality bb = bDescendants.get(b);
        XQueryCardinality bc = bDescendants.get(c);
        assertEquals(XQueryCardinality.ZERO_OR_MORE, bx);
        assertEquals(XQueryCardinality.ZERO_OR_MORE, ba);
        assertEquals(XQueryCardinality.ZERO_OR_MORE, bb);
        assertEquals(XQueryCardinality.ZERO_OR_MORE, bc);
    }


    @Test
    void b() {
        GrammarRuleCardinalityAnalyzer analyzer = new GrammarRuleCardinalityAnalyzer(Set.of(x, a));
        // x: a a;
        analyzer.addRule(x, a, XQueryCardinality.ONE);
        analyzer.addRule(x, a, XQueryCardinality.ONE);
        var r = analyzer.analyzeAll();
        Map<QualifiedName, XQueryCardinality> xDescendants = r.get(x);
        XQueryCardinality xa = xDescendants.get(a);
        assertEquals(XQueryCardinality.ONE_OR_MORE, xa);
    }
}
