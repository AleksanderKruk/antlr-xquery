package com.github.akruk.antlrquery.inputgrammaranalyzer;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;

import com.github.akruk.antlrquery.inputgrammaranalyzer.InputGrammarAnalyzer.GrammarAnalysisResult;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class GrammarAnalysisTests {


    private GrammarAnalysisResult analyzeGrammar(String grammar) {
        final InputGrammarAnalyzer analyzer = new InputGrammarAnalyzer();
        final CharStream stream = CharStreams.fromString(grammar);
        return analyzer.analyze(stream);
    }

    private GrammarAnalysisResult relationshipGrammar() {
        final String grammar = """
            grammar grammarname;
            x: a b c;
            a: 'a';
            b: B;
            c: 'c';
            B: 'b';
        """;
        return analyzeGrammar(grammar);
    }

    // private GrammarAnalysisResult simpleTokenTestGrammar() {
    //     String grammar = """
    //         grammar grammarname;
    //         A: 'a';
    //         B: 'b' 'c' 'd';
    //         C: 'bcd';
    //         fragment D: 'd';
    //         fragment E: 'e';
    //         F: D E;
    //         FF: D 'k';
    //         FFF: 'h' E;
    //         G: 'a'+;
    //         H: 'a'*;
    //         I: 'e' 'a'?;
    //         J: 'e' | 'h';
    //         K: 'e' | [abcd];
    //         L: [abcd];
    //         M: ~'h';
    //     """;
    //     return analyzeGrammar(grammar);
    // }

    // private GrammarAnalysisResult simpleRuleTestGrammar() {
    //     String grammar = """
    //         grammar grammarname;
    //         a: 'a';
    //         b: 'a' | 'b';
    //         c: 'a'    # d
    //             | 'b' # e;
    //         f: k='c';
    //         g: z=('c');
    //         h: z=('c'|'b');
    //         i: A;
    //         j: A|;
    //         jj: a a a;
    //         jjj: a c c;
    //         jjjj: a a A B;

    //         k: 'a'+;
    //         l: 'a'*;
    //         m: 'e' 'a'?;
    //         n: 'e' | 'h';
    //         r: ~'h';
    //         A: 'A';
    //         B: 'B';
    //     """;
    //     return analyzeGrammar(grammar);
    // }


    @Test
    public void children() {
        final var results = relationshipGrammar();
        final var children = results.children();

        assertEquals(Map.ofEntries(
            Map.entry("x", Cardinality.ZERO),
            Map.entry("a", Cardinality.ONE),
            Map.entry("b", Cardinality.ONE),
            Map.entry("c", Cardinality.ONE),
            Map.entry("'a'", Cardinality.ZERO),
            Map.entry("B", Cardinality.ZERO),
            Map.entry("'c'", Cardinality.ZERO),
            Map.entry("'b'", Cardinality.ZERO)
        ), children.get("x"));

        assertEquals(Map.ofEntries(
            Map.entry("x", Cardinality.ZERO),
            Map.entry("a", Cardinality.ZERO),
            Map.entry("b", Cardinality.ZERO),
            Map.entry("c", Cardinality.ZERO),
            Map.entry("'a'", Cardinality.ONE),
            Map.entry("B", Cardinality.ZERO),
            Map.entry("'c'", Cardinality.ZERO),
            Map.entry("'b'", Cardinality.ZERO)
        ), children.get("a"));

        assertEquals(Map.ofEntries(
            Map.entry("x", Cardinality.ZERO),
            Map.entry("a", Cardinality.ZERO),
            Map.entry("b", Cardinality.ZERO),
            Map.entry("c", Cardinality.ZERO),
            Map.entry("'a'", Cardinality.ZERO),
            Map.entry("B", Cardinality.ONE),
            Map.entry("'c'", Cardinality.ZERO),
            Map.entry("'b'", Cardinality.ZERO)
        ), children.get("b"));

        assertEquals(Map.ofEntries(
            Map.entry("x", Cardinality.ZERO),
            Map.entry("a", Cardinality.ZERO),
            Map.entry("b", Cardinality.ZERO),
            Map.entry("c", Cardinality.ZERO),
            Map.entry("'a'", Cardinality.ZERO),
            Map.entry("B", Cardinality.ZERO),
            Map.entry("'c'", Cardinality.ONE),
            Map.entry("'b'", Cardinality.ZERO)
        ), children.get("c"));

    }

    @Test
    public void childrenRecursive() {
        final var results = analyzeGrammar("""
            grammar B;
            a: a;
            b: b?;
            c: c*;
            d: d+;
        """);
        final var children = results.children();

        assertEquals(Map.ofEntries(
            Map.entry("a", Cardinality.ONE_OR_MORE),
            Map.entry("b", Cardinality.ZERO),
            Map.entry("c", Cardinality.ZERO),
            Map.entry("d", Cardinality.ZERO)
        ), children.get("a"));

        assertEquals(Map.ofEntries(
            Map.entry("a", Cardinality.ZERO),
            Map.entry("b", Cardinality.ZERO_OR_MORE),
            Map.entry("c", Cardinality.ZERO),
            Map.entry("d", Cardinality.ZERO)
        ), children.get("b"));

        assertEquals(Map.ofEntries(
            Map.entry("a", Cardinality.ZERO),
            Map.entry("b", Cardinality.ZERO),
            Map.entry("c", Cardinality.ZERO_OR_MORE),
            Map.entry("d", Cardinality.ZERO)
        ), children.get("c"));

        assertEquals(Map.ofEntries(
            Map.entry("a", Cardinality.ZERO),
            Map.entry("b", Cardinality.ZERO),
            Map.entry("c", Cardinality.ZERO),
            Map.entry("d", Cardinality.ONE_OR_MORE)
        ), children.get("d"));
    }





    @Test
    public void parents() {
        final var results = relationshipGrammar();
        final var parent = results.parent();

        assertEquals(Map.ofEntries(
            Map.entry("x", Cardinality.ZERO),
            Map.entry("a", Cardinality.ZERO),
            Map.entry("b", Cardinality.ZERO),
            Map.entry("c", Cardinality.ZERO),
            Map.entry("'a'", Cardinality.ZERO),
            Map.entry("B", Cardinality.ZERO),
            Map.entry("'c'", Cardinality.ZERO),
            Map.entry("'b'", Cardinality.ZERO)
            ), parent.get("x"));

        assertEquals(parent.get("a"), Map.ofEntries(
                Map.entry("x", Cardinality.ZERO_OR_ONE),
                Map.entry("a", Cardinality.ZERO),
                Map.entry("b", Cardinality.ZERO),
                Map.entry("c", Cardinality.ZERO),
                Map.entry("'a'", Cardinality.ZERO),
                Map.entry("B", Cardinality.ZERO),
                Map.entry("'c'", Cardinality.ZERO),
                Map.entry("'b'", Cardinality.ZERO)
        ));

        assertEquals(parent.get("b"), Map.ofEntries(
                Map.entry("x", Cardinality.ZERO_OR_ONE),
                Map.entry("a", Cardinality.ZERO),
                Map.entry("b", Cardinality.ZERO),
                Map.entry("c", Cardinality.ZERO),
                Map.entry("'a'", Cardinality.ZERO),
                Map.entry("B", Cardinality.ZERO),
                Map.entry("'c'", Cardinality.ZERO),
                Map.entry("'b'", Cardinality.ZERO)
        ));

        assertEquals(parent.get("c"), Map.ofEntries(
                Map.entry("x", Cardinality.ZERO_OR_ONE),
                Map.entry("a", Cardinality.ZERO),
                Map.entry("b", Cardinality.ZERO),
                Map.entry("c", Cardinality.ZERO),
                Map.entry("'a'", Cardinality.ZERO),
                Map.entry("B", Cardinality.ZERO),
                Map.entry("'c'", Cardinality.ZERO),
                Map.entry("'b'", Cardinality.ZERO)
        ));
    }


}
