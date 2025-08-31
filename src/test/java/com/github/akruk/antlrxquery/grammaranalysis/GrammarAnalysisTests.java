package com.github.akruk.antlrxquery.grammaranalysis;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.junit.Test;

import com.github.akruk.antlrxquery.inputgrammaranalyzer.InputGrammarAnalyzer;
import com.github.akruk.antlrxquery.inputgrammaranalyzer.InputGrammarAnalyzer.GrammarAnalysisResult;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryCardinality;

import java.util.Map;

import static org.junit.Assert.*;

public class GrammarAnalysisTests {


    private GrammarAnalysisResult analyzeGrammar(String grammar) {
        final InputGrammarAnalyzer analyzer = new InputGrammarAnalyzer();
        final CharStream stream = CharStreams.fromString(grammar);
        final var results = analyzer.analyze(stream);
        return results;
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
            Map.entry("x", XQueryCardinality.ZERO),
            Map.entry("a", XQueryCardinality.ONE),
            Map.entry("b", XQueryCardinality.ONE),
            Map.entry("c", XQueryCardinality.ONE),
            Map.entry("'a'", XQueryCardinality.ZERO),
            Map.entry("B", XQueryCardinality.ZERO),
            Map.entry("'c'", XQueryCardinality.ZERO),
            Map.entry("'b'", XQueryCardinality.ZERO)
        ), children.get("x"));

        assertEquals(Map.ofEntries(
            Map.entry("x", XQueryCardinality.ZERO),
            Map.entry("a", XQueryCardinality.ZERO),
            Map.entry("b", XQueryCardinality.ZERO),
            Map.entry("c", XQueryCardinality.ZERO),
            Map.entry("'a'", XQueryCardinality.ONE),
            Map.entry("B", XQueryCardinality.ZERO),
            Map.entry("'c'", XQueryCardinality.ZERO),
            Map.entry("'b'", XQueryCardinality.ZERO)
        ), children.get("a"));

        assertEquals(Map.ofEntries(
            Map.entry("x", XQueryCardinality.ZERO),
            Map.entry("a", XQueryCardinality.ZERO),
            Map.entry("b", XQueryCardinality.ZERO),
            Map.entry("c", XQueryCardinality.ZERO),
            Map.entry("'a'", XQueryCardinality.ZERO),
            Map.entry("B", XQueryCardinality.ONE),
            Map.entry("'c'", XQueryCardinality.ZERO),
            Map.entry("'b'", XQueryCardinality.ZERO)
        ), children.get("b"));

        assertEquals(Map.ofEntries(
            Map.entry("x", XQueryCardinality.ZERO),
            Map.entry("a", XQueryCardinality.ZERO),
            Map.entry("b", XQueryCardinality.ZERO),
            Map.entry("c", XQueryCardinality.ZERO),
            Map.entry("'a'", XQueryCardinality.ZERO),
            Map.entry("B", XQueryCardinality.ZERO),
            Map.entry("'c'", XQueryCardinality.ONE),
            Map.entry("'b'", XQueryCardinality.ZERO)
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
            Map.entry("a", XQueryCardinality.ONE_OR_MORE),
            Map.entry("b", XQueryCardinality.ZERO),
            Map.entry("c", XQueryCardinality.ZERO),
            Map.entry("d", XQueryCardinality.ZERO)
        ), children.get("a"));

        assertEquals(Map.ofEntries(
            Map.entry("a", XQueryCardinality.ZERO),
            Map.entry("b", XQueryCardinality.ZERO_OR_MORE),
            Map.entry("c", XQueryCardinality.ZERO),
            Map.entry("d", XQueryCardinality.ZERO)
        ), children.get("b"));

        assertEquals(Map.ofEntries(
            Map.entry("a", XQueryCardinality.ZERO),
            Map.entry("b", XQueryCardinality.ZERO),
            Map.entry("c", XQueryCardinality.ZERO_OR_MORE),
            Map.entry("d", XQueryCardinality.ZERO)
        ), children.get("c"));

        assertEquals(Map.ofEntries(
            Map.entry("a", XQueryCardinality.ZERO),
            Map.entry("b", XQueryCardinality.ZERO),
            Map.entry("c", XQueryCardinality.ZERO),
            Map.entry("d", XQueryCardinality.ONE_OR_MORE)
        ), children.get("d"));
    }





    @Test
    public void parents() {
        final var results = relationshipGrammar();
        final var parent = results.parent();

        assertEquals(Map.ofEntries(
            Map.entry("x", XQueryCardinality.ZERO),
            Map.entry("a", XQueryCardinality.ZERO),
            Map.entry("b", XQueryCardinality.ZERO),
            Map.entry("c", XQueryCardinality.ZERO),
            Map.entry("'a'", XQueryCardinality.ZERO),
            Map.entry("B", XQueryCardinality.ZERO),
            Map.entry("'c'", XQueryCardinality.ZERO),
            Map.entry("'b'", XQueryCardinality.ZERO)
            ), parent.get("x"));

        assertTrue(parent.get("a").equals(Map.ofEntries(
            Map.entry("x", XQueryCardinality.ZERO_OR_ONE),
            Map.entry("a", XQueryCardinality.ZERO),
            Map.entry("b", XQueryCardinality.ZERO),
            Map.entry("c", XQueryCardinality.ZERO),
            Map.entry("'a'", XQueryCardinality.ZERO),
            Map.entry("B", XQueryCardinality.ZERO),
            Map.entry("'c'", XQueryCardinality.ZERO),
            Map.entry("'b'", XQueryCardinality.ZERO)
        )));

        assertTrue(parent.get("b").equals(Map.ofEntries(
            Map.entry("x", XQueryCardinality.ZERO_OR_ONE),
            Map.entry("a", XQueryCardinality.ZERO),
            Map.entry("b", XQueryCardinality.ZERO),
            Map.entry("c", XQueryCardinality.ZERO),
            Map.entry("'a'", XQueryCardinality.ZERO),
            Map.entry("B", XQueryCardinality.ZERO),
            Map.entry("'c'", XQueryCardinality.ZERO),
            Map.entry("'b'", XQueryCardinality.ZERO)
        )));

        assertTrue(parent.get("c").equals(Map.ofEntries(
            Map.entry("x", XQueryCardinality.ZERO_OR_ONE),
            Map.entry("a", XQueryCardinality.ZERO),
            Map.entry("b", XQueryCardinality.ZERO),
            Map.entry("c", XQueryCardinality.ZERO),
            Map.entry("'a'", XQueryCardinality.ZERO),
            Map.entry("B", XQueryCardinality.ZERO),
            Map.entry("'c'", XQueryCardinality.ZERO),
            Map.entry("'b'", XQueryCardinality.ZERO)
        )));
    }

    // @Test
    // public void descendants() {
    //     final var results = relationshipGrammar();
    //     final var descendants = results.descendants();
    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ONE),
    //         Map.entry("b", XQueryCardinality.ONE),
    //         Map.entry("c", XQueryCardinality.ONE),
    //         Map.entry("'a'", XQueryCardinality.ONE),
    //         Map.entry("B", XQueryCardinality.ONE),
    //         Map.entry("'c'", XQueryCardinality.ONE),
    //         Map.entry("'b'", XQueryCardinality.ONE)
    //         ), descendants.get("x"));

    //     assertTrue(descendants.get("a").equals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ONE),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO)
    //     )));

    //     assertTrue(descendants.get("'a'").equals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO)
    //     )));


    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ONE),
    //         Map.entry("'c'", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ONE)
    //     ), descendants.get("b"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ONE)
    //     ), descendants.get("B"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ONE),
    //         Map.entry("'b'", XQueryCardinality.ZERO)
    //     ), descendants.get("c"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO)
    //     ), descendants.get("'c'"));

    // }


    // @Test
    // public void descendantsOrSelf() {
    //     final var results = relationshipGrammar();
    //     final var descendants = results.descendantsOrSelf();

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ONE),
    //         Map.entry("a", XQueryCardinality.ONE),
    //         Map.entry("b", XQueryCardinality.ONE),
    //         Map.entry("c", XQueryCardinality.ONE),
    //         Map.entry("'a'", XQueryCardinality.ONE),
    //         Map.entry("B", XQueryCardinality.ONE),
    //         Map.entry("'c'", XQueryCardinality.ONE),
    //         Map.entry("'b'", XQueryCardinality.ONE)
    //         ), descendants.get("x"));

    //     assertTrue(descendants.get("a").equals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ONE),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ONE),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO)
    //     )));

    //     assertTrue(descendants.get("'a'").equals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ONE),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO)
    //     )));


    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ONE),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ONE),
    //         Map.entry("'c'", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ONE)
    //     ), descendants.get("b"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ONE),
    //         Map.entry("'c'", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ONE)
    //     ), descendants.get("B"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ONE),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ONE),
    //         Map.entry("'b'", XQueryCardinality.ZERO)
    //     ), descendants.get("c"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ONE),
    //         Map.entry("'b'", XQueryCardinality.ZERO)
    //     ), descendants.get("'c'"));
    // }

    // @Test
    // public void ancestors() {
    //     final var results = relationshipGrammar();
    //     final var parent = results.ancestors();

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO)
    //         ), parent.get("x"));

    //     assertTrue(parent.get("a").equals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO_OR_ONE),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO)
    //     )));

    //     assertTrue(parent.get("'a'").equals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO_OR_ONE),
    //         Map.entry("a", XQueryCardinality.ZERO_OR_ONE),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO)
    //     )));

    //     assertTrue(parent.get("b").equals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO_OR_ONE),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO)
    //     )));

    //     assertTrue(parent.get("B").equals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO_OR_ONE),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO_OR_ONE),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO)
    //     )));

    //     assertTrue(parent.get("c").equals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO_OR_ONE),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO)
    //     )));

    //     assertTrue(parent.get("'c'").equals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO_OR_ONE),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO_OR_ONE),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO)
    //     )));
    // }

    // @Test
    // public void ancestorsOrSelf() {
    //     final var results = relationshipGrammar();
    //     final var ancestorsOrSelf = results.ancestorsOrSelf();

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ONE),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), ancestorsOrSelf.get("x"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO_OR_ONE),
    //         Map.entry("a", XQueryCardinality.ONE),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), ancestorsOrSelf.get("a"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO_OR_ONE),
    //         Map.entry("a", XQueryCardinality.ZERO_OR_ONE),
    //         Map.entry("'a'", XQueryCardinality.ONE),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), ancestorsOrSelf.get("'a'"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO_OR_ONE),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ONE),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), ancestorsOrSelf.get("b"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO_OR_ONE),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO_OR_ONE),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ONE),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), ancestorsOrSelf.get("B"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO_OR_ONE),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ONE),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), ancestorsOrSelf.get("c"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO_OR_ONE),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO_OR_ONE),
    //         Map.entry("'c'", XQueryCardinality.ONE)
    //     ), ancestorsOrSelf.get("'c'"));

    // }

    // @Test
    // public void followingSibling() {
    //     final var results = relationshipGrammar();
    //     final var followingSibling = results.followingSibling();

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), followingSibling.get("x"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ONE),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ONE),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), followingSibling.get("a"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), followingSibling.get("'a'"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ONE),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), followingSibling.get("b"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), followingSibling.get("B"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), followingSibling.get("c"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), followingSibling.get("'c'"));
    // }

    // @Test
    // public void followingSiblingOrSelf() {
    //     final var results = relationshipGrammar();
    //     final var followingSiblingOrSelf = results.followingSiblingOrSelf();

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ONE),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), followingSiblingOrSelf.get("x"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ONE),
    //         Map.entry("b", XQueryCardinality.ONE),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ONE),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), followingSiblingOrSelf.get("a"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ONE),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), followingSiblingOrSelf.get("'a'"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ONE),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ONE),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), followingSiblingOrSelf.get("b"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ONE),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), followingSiblingOrSelf.get("B"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ONE),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), followingSiblingOrSelf.get("c"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ONE)
    //     ), followingSiblingOrSelf.get("'c'"));
    // }


    // @Test
    // public void following() {
    //     final var results = relationshipGrammar();
    //     final var following = results.following();

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), following.get("x"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ONE),
    //         Map.entry("'b'", XQueryCardinality.ONE),
    //         Map.entry("c", XQueryCardinality.ONE),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ONE),
    //         Map.entry("'c'", XQueryCardinality.ONE)
    //     ), following.get("a"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ONE),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ONE),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ONE),
    //         Map.entry("'c'", XQueryCardinality.ONE)
    //     ), following.get("'a'"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ONE),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ONE)
    //     ), following.get("b"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ONE),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ONE)
    //     ), following.get("B"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), following.get("c"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), following.get("'c'"));
    // }


    // @Test
    // public void followingOrSelf() {
    //     final var results = relationshipGrammar();
    //     final var followingOrSelf = results.followingOrSelf();

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ONE),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), followingOrSelf.get("x"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ONE),
    //         Map.entry("b", XQueryCardinality.ONE),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ONE),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ONE),
    //         Map.entry("'c'", XQueryCardinality.ONE)
    //     ), followingOrSelf.get("a"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ONE),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ONE),
    //         Map.entry("'a'", XQueryCardinality.ONE),
    //         Map.entry("B", XQueryCardinality.ONE),
    //         Map.entry("'c'", XQueryCardinality.ONE)
    //     ), followingOrSelf.get("'a'"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ONE),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ONE),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ONE)
    //     ), followingOrSelf.get("b"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ONE),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ONE),
    //         Map.entry("'c'", XQueryCardinality.ONE)
    //     ), followingOrSelf.get("B"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ONE),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), followingOrSelf.get("c"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ONE)
    //     ), followingOrSelf.get("'c'"));
    // }


    // @Test
    // public void precedingSibling() {
    //     final var results = relationshipGrammar();
    //     final var precedingSibling = results.precedingSibling();

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), precedingSibling.get("x"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), precedingSibling.get("a"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), precedingSibling.get("'a'"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ONE),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), precedingSibling.get("b"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), precedingSibling.get("B"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ONE),
    //         Map.entry("b", XQueryCardinality.ONE),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), precedingSibling.get("c"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), precedingSibling.get("'c'"));
    // }

    // @Test
    // public void precedingSiblingOrSelf() {
    //     final var results = relationshipGrammar();
    //     final var precedingSiblingOrSelf = results.precedingSiblingOrSelf();

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ONE),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), precedingSiblingOrSelf.get("x"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ONE),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), precedingSiblingOrSelf.get("a"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ONE),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), precedingSiblingOrSelf.get("'a'"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ONE),
    //         Map.entry("b", XQueryCardinality.ONE),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), precedingSiblingOrSelf.get("b"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ONE),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), precedingSiblingOrSelf.get("B"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ONE),
    //         Map.entry("b", XQueryCardinality.ONE),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ONE),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), precedingSiblingOrSelf.get("c"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ONE)
    //     ), precedingSiblingOrSelf.get("'c'"));
    // }

    // @Test
    // public void preceding() {
    //     final var results = relationshipGrammar();
    //     final var preceding = results.preceding();

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), preceding.get("x"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), preceding.get("a"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), preceding.get("'a'"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ONE),
    //         Map.entry("'a'", XQueryCardinality.ONE),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), preceding.get("b"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ONE),
    //         Map.entry("'a'", XQueryCardinality.ONE),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), preceding.get("B"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ONE),
    //         Map.entry("'a'", XQueryCardinality.ONE),
    //         Map.entry("b", XQueryCardinality.ONE),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ONE),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), preceding.get("c"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ONE),
    //         Map.entry("'a'", XQueryCardinality.ONE),
    //         Map.entry("b", XQueryCardinality.ONE),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ONE),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), preceding.get("'c'"));
    // }

    // @Test
    // public void precedingOrSelf() {
    //     final var results = relationshipGrammar();
    //     final var precedingOrSelf = results.precedingOrSelf();

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ONE),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), precedingOrSelf.get("x"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ONE),
    //         Map.entry("'a'", XQueryCardinality.ZERO),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), precedingOrSelf.get("a"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ZERO),
    //         Map.entry("'a'", XQueryCardinality.ONE),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), precedingOrSelf.get("'a'"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ONE),
    //         Map.entry("'a'", XQueryCardinality.ONE),
    //         Map.entry("b", XQueryCardinality.ONE),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ZERO),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), precedingOrSelf.get("b"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ONE),
    //         Map.entry("'a'", XQueryCardinality.ONE),
    //         Map.entry("b", XQueryCardinality.ZERO),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ONE),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), precedingOrSelf.get("B"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ONE),
    //         Map.entry("'a'", XQueryCardinality.ONE),
    //         Map.entry("b", XQueryCardinality.ONE),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ONE),
    //         Map.entry("c", XQueryCardinality.ONE),
    //         Map.entry("'c'", XQueryCardinality.ZERO)
    //     ), precedingOrSelf.get("c"));

    //     assertEquals(Map.ofEntries(
    //         Map.entry("x", XQueryCardinality.ZERO),
    //         Map.entry("a", XQueryCardinality.ONE),
    //         Map.entry("'a'", XQueryCardinality.ONE),
    //         Map.entry("b", XQueryCardinality.ONE),
    //         Map.entry("'b'", XQueryCardinality.ZERO),
    //         Map.entry("B", XQueryCardinality.ONE),
    //         Map.entry("c", XQueryCardinality.ZERO),
    //         Map.entry("'c'", XQueryCardinality.ONE)
    //     ), precedingOrSelf.get("'c'"));
    // }


    // @Test
    // public void simpleTokens() {
    //     final var results = simpleTokenTestGrammar();
    //     final var simpleTokens = results.simpleTokens();
    //     // only strings
    //     assertTrue(simpleTokens.contains("A"));
    //     assertTrue(simpleTokens.contains("B"));
    //     assertTrue(simpleTokens.contains("C"));

    //     // no fragments
    //     assertFalse(simpleTokens.contains("D"));
    //     assertFalse(simpleTokens.contains("E"));

    //     // recursive simplicity
    //     assertTrue(simpleTokens.contains("F"));
    //     assertTrue(simpleTokens.contains("FF"));
    //     assertTrue(simpleTokens.contains("FFF"));

    //     // no modifiers
    //     assertFalse(simpleTokens.contains("G"));
    //     assertFalse(simpleTokens.contains("H"));
    //     assertFalse(simpleTokens.contains("I"));

    //     // no alternatives
    //     assertFalse(simpleTokens.contains("J"));
    //     assertFalse(simpleTokens.contains("K"));

    //     // no character classes
    //     assertFalse(simpleTokens.contains("L"));
    //     // no sets
    //     assertFalse(simpleTokens.contains("M"));
    // }

    // @Test
    // public void simpleRules() {
    //     final var results = simpleRuleTestGrammar();
    //     final var simpleRules = results.simpleRules();
    //     // only strings
    //     assertTrue(simpleRules.contains("a"));
    //     // no alternatives
    //     assertFalse(simpleRules.contains("b"));
    //     // named labels
    //     assertFalse(simpleRules.contains("c"));
    //     assertTrue(simpleRules.contains("d"));
    //     assertTrue(simpleRules.contains("e"));

    //     assertTrue(simpleRules.contains("f"));
    //     // parenthesis
    //     assertTrue(simpleRules.contains("g"));
    //     assertFalse(simpleRules.contains("h"));

    //     assertTrue(simpleRules.contains("i"));
    //     assertFalse(simpleRules.contains("j"));

    //     // recursive simplicity
    //     assertTrue(simpleRules.contains("jj"));
    //     assertFalse(simpleRules.contains("jjj"));
    //     assertTrue(simpleRules.contains("jjjj"));


    //     // no modifiers
    //     assertFalse(simpleRules.contains("k"));
    //     assertFalse(simpleRules.contains("l"));
    //     assertFalse(simpleRules.contains("m"));

    //     // no alternatives
    //     assertFalse(simpleRules.contains("n"));

    //     // no sets
    //     assertFalse(simpleRules.contains("r"));
    // }


}
