package com.github.akruk.antlrquery.inputgrammaranalyzer;

import com.github.akruk.antlrgrammar.ANTLRv4Lexer;
import com.github.akruk.antlrgrammar.ANTLRv4Parser;
import com.github.akruk.antlrquery.AntlrQueryAxis;
import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver;
import org.antlr.v4.runtime.*;

import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class GrammarAnalysisTests {


    private InputGrammarAnalyzer.QualifiedGrammarAnalysisResult analyzeGrammar(String grammar) {
        final InputGrammarAnalyzer analyzer = new InputGrammarAnalyzer();
        final CharStream stream = CharStreams.fromString(grammar);
        var lexer = new ANTLRv4Lexer(stream);
        var parser = new ANTLRv4Parser(new CommonTokenStream(lexer));
        parser.addErrorListener(new BaseErrorListener(){
            @Override
            public void syntaxError(
                    Recognizer<?, ?> recognizer,
                    Object offendingSymbol,
                    int line,
                    int charPositionInLine,
                    String msg,
                    RecognitionException e)
            {
                throw new IllegalStateException(msg);
            }
        });
        var tree = parser.grammarSpec();
        return analyzer.analyze("", Collections.singletonList(tree));
    }

    private InputGrammarAnalyzer.QualifiedGrammarAnalysisResult relationshipGrammar() {
        final String grammar = """
            grammar GrammarName;
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
        final var children = results.axes().get(AntlrQueryAxis.CHILD);

        assertEquals(Map.ofEntries(
            Map.entry(new NamespaceResolver.QualifiedName("", "x"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "a"), Cardinality.ONE),
            Map.entry(new NamespaceResolver.QualifiedName("", "b"), Cardinality.ONE),
            Map.entry(new NamespaceResolver.QualifiedName("", "c"), Cardinality.ONE),
            Map.entry(new NamespaceResolver.QualifiedName("", "'a'"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "B"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "'c'"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "'b'"), Cardinality.ZERO)
        ), children.get(new NamespaceResolver.QualifiedName("", "x")));

        assertEquals(Map.ofEntries(
            Map.entry(new NamespaceResolver.QualifiedName("", "x"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "a"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "b"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "c"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "'a'"), Cardinality.ONE),
            Map.entry(new NamespaceResolver.QualifiedName("", "B"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "'c'"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "'b'"), Cardinality.ZERO)
        ), children.get(new NamespaceResolver.QualifiedName("", "a")));

        assertEquals(Map.ofEntries(
            Map.entry(new NamespaceResolver.QualifiedName("", "x"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "a"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "b"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "c"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "'a'"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "B"), Cardinality.ONE),
            Map.entry(new NamespaceResolver.QualifiedName("", "'c'"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "'b'"), Cardinality.ZERO)
        ), children.get(new NamespaceResolver.QualifiedName("", "b")));

        assertEquals(Map.ofEntries(
            Map.entry(new NamespaceResolver.QualifiedName("", "x"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "a"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "b"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "c"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "'a'"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "B"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "'c'"), Cardinality.ONE),
            Map.entry(new NamespaceResolver.QualifiedName("", "'b'"), Cardinality.ZERO)
        ), children.get(new NamespaceResolver.QualifiedName("", "c")));

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
        final var children = results.axes().get(AntlrQueryAxis.CHILD);

        assertEquals(Map.ofEntries(
            Map.entry(new NamespaceResolver.QualifiedName("", "a"), Cardinality.ONE),
            Map.entry(new NamespaceResolver.QualifiedName("", "b"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "c"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "d"), Cardinality.ZERO)
        ), children.get(new NamespaceResolver.QualifiedName("", "a")));

        assertEquals(Map.ofEntries(
            Map.entry(new NamespaceResolver.QualifiedName("", "a"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "b"), Cardinality.ZERO_OR_ONE),
            Map.entry(new NamespaceResolver.QualifiedName("", "c"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "d"), Cardinality.ZERO)
        ), children.get(new NamespaceResolver.QualifiedName("", "b")));

        assertEquals(Map.ofEntries(
            Map.entry(new NamespaceResolver.QualifiedName("", "a"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "b"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "c"), Cardinality.ZERO_OR_MORE),
            Map.entry(new NamespaceResolver.QualifiedName("", "d"), Cardinality.ZERO)
        ), children.get(new NamespaceResolver.QualifiedName("", "c")));

        assertEquals(Map.ofEntries(
            Map.entry(new NamespaceResolver.QualifiedName("", "a"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "b"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "c"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "d"), Cardinality.ONE_OR_MORE)
        ), children.get(new NamespaceResolver.QualifiedName("", "d")));
    }





    @Test
    public void parents() {
        final var results = relationshipGrammar();
        final var parent = results.axes().get(AntlrQueryAxis.PARENT);

        assertEquals(Map.ofEntries(
            Map.entry(new NamespaceResolver.QualifiedName("", "x"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "a"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "b"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "c"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "'a'"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "B"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "'c'"), Cardinality.ZERO),
            Map.entry(new NamespaceResolver.QualifiedName("", "'b'"), Cardinality.ZERO)
            ), parent.get(new NamespaceResolver.QualifiedName("", "x")));

        assertEquals(parent.get(new NamespaceResolver.QualifiedName("", "a")), Map.ofEntries(
                Map.entry(new NamespaceResolver.QualifiedName("", "x"), Cardinality.ZERO_OR_ONE),
                Map.entry(new NamespaceResolver.QualifiedName("", "a"), Cardinality.ZERO),
                Map.entry(new NamespaceResolver.QualifiedName("", "b"), Cardinality.ZERO),
                Map.entry(new NamespaceResolver.QualifiedName("", "c"), Cardinality.ZERO),
                Map.entry(new NamespaceResolver.QualifiedName("", "'a'"), Cardinality.ZERO),
                Map.entry(new NamespaceResolver.QualifiedName("", "B"), Cardinality.ZERO),
                Map.entry(new NamespaceResolver.QualifiedName("", "'c'"), Cardinality.ZERO),
                Map.entry(new NamespaceResolver.QualifiedName("", "'b'"), Cardinality.ZERO)
        ));

        assertEquals(
                Map.ofEntries(
                    Map.entry(new NamespaceResolver.QualifiedName("", "x"), Cardinality.ZERO_OR_ONE),
                    Map.entry(new NamespaceResolver.QualifiedName("", "a"), Cardinality.ZERO),
                    Map.entry(new NamespaceResolver.QualifiedName("", "b"), Cardinality.ZERO),
                    Map.entry(new NamespaceResolver.QualifiedName("", "c"), Cardinality.ZERO),
                    Map.entry(new NamespaceResolver.QualifiedName("", "'a'"), Cardinality.ZERO),
                    Map.entry(new NamespaceResolver.QualifiedName("", "B"), Cardinality.ZERO),
                    Map.entry(new NamespaceResolver.QualifiedName("", "'c'"), Cardinality.ZERO),
                    Map.entry(new NamespaceResolver.QualifiedName("", "'b'"), Cardinality.ZERO)
                ),
                parent.get(new NamespaceResolver.QualifiedName("", "b"))
        );

        assertEquals(
                Map.ofEntries(
                        Map.entry(new NamespaceResolver.QualifiedName("", "x"), Cardinality.ZERO_OR_ONE),
                        Map.entry(new NamespaceResolver.QualifiedName("", "a"), Cardinality.ZERO),
                        Map.entry(new NamespaceResolver.QualifiedName("", "b"), Cardinality.ZERO),
                        Map.entry(new NamespaceResolver.QualifiedName("", "c"), Cardinality.ZERO),
                        Map.entry(new NamespaceResolver.QualifiedName("", "'a'"), Cardinality.ZERO),
                        Map.entry(new NamespaceResolver.QualifiedName("", "B"), Cardinality.ZERO),
                        Map.entry(new NamespaceResolver.QualifiedName("", "'c'"), Cardinality.ZERO),
                        Map.entry(new NamespaceResolver.QualifiedName("", "'b'"), Cardinality.ZERO)
                ),
                parent.get(new NamespaceResolver.QualifiedName("", "c"))
        );
    }


    @Test
    public void ancestors() {
        final var results = relationshipGrammar();
        final var parent = results.axes().get(AntlrQueryAxis.ANCESTOR);

        assertEquals(Map.ofEntries(
                Map.entry(new NamespaceResolver.QualifiedName("", "x"), Cardinality.ZERO),
                Map.entry(new NamespaceResolver.QualifiedName("", "a"), Cardinality.ZERO),
                Map.entry(new NamespaceResolver.QualifiedName("", "b"), Cardinality.ZERO),
                Map.entry(new NamespaceResolver.QualifiedName("", "c"), Cardinality.ZERO),
                Map.entry(new NamespaceResolver.QualifiedName("", "'a'"), Cardinality.ZERO),
                Map.entry(new NamespaceResolver.QualifiedName("", "B"), Cardinality.ZERO),
                Map.entry(new NamespaceResolver.QualifiedName("", "'c'"), Cardinality.ZERO),
                Map.entry(new NamespaceResolver.QualifiedName("", "'b'"), Cardinality.ZERO)
        ), parent.get(new NamespaceResolver.QualifiedName("", "x")));

        assertEquals(Map.ofEntries(
                Map.entry(new NamespaceResolver.QualifiedName("", "x"), Cardinality.ZERO_OR_ONE),
                Map.entry(new NamespaceResolver.QualifiedName("", "a"), Cardinality.ZERO),
                Map.entry(new NamespaceResolver.QualifiedName("", "b"), Cardinality.ZERO),
                Map.entry(new NamespaceResolver.QualifiedName("", "c"), Cardinality.ZERO),
                Map.entry(new NamespaceResolver.QualifiedName("", "'a'"), Cardinality.ZERO),
                Map.entry(new NamespaceResolver.QualifiedName("", "B"), Cardinality.ZERO),
                Map.entry(new NamespaceResolver.QualifiedName("", "'c'"), Cardinality.ZERO),
                Map.entry(new NamespaceResolver.QualifiedName("", "'b'"), Cardinality.ZERO)
        ), parent.get(new NamespaceResolver.QualifiedName("", "a")));

        assertEquals(
                Map.ofEntries(
                        Map.entry(new NamespaceResolver.QualifiedName("", "x"), Cardinality.ZERO_OR_ONE),
                        Map.entry(new NamespaceResolver.QualifiedName("", "a"), Cardinality.ZERO),
                        Map.entry(new NamespaceResolver.QualifiedName("", "b"), Cardinality.ZERO),
                        Map.entry(new NamespaceResolver.QualifiedName("", "c"), Cardinality.ZERO),
                        Map.entry(new NamespaceResolver.QualifiedName("", "'a'"), Cardinality.ZERO),
                        Map.entry(new NamespaceResolver.QualifiedName("", "B"), Cardinality.ZERO),
                        Map.entry(new NamespaceResolver.QualifiedName("", "'c'"), Cardinality.ZERO),
                        Map.entry(new NamespaceResolver.QualifiedName("", "'b'"), Cardinality.ZERO)
                ),
                parent.get(new NamespaceResolver.QualifiedName("", "b"))
        );

        assertEquals(
                Map.ofEntries(
                        Map.entry(new NamespaceResolver.QualifiedName("", "x"), Cardinality.ZERO_OR_ONE),
                        Map.entry(new NamespaceResolver.QualifiedName("", "a"), Cardinality.ZERO),
                        Map.entry(new NamespaceResolver.QualifiedName("", "b"), Cardinality.ZERO),
                        Map.entry(new NamespaceResolver.QualifiedName("", "c"), Cardinality.ZERO),
                        Map.entry(new NamespaceResolver.QualifiedName("", "'a'"), Cardinality.ZERO),
                        Map.entry(new NamespaceResolver.QualifiedName("", "B"), Cardinality.ZERO),
                        Map.entry(new NamespaceResolver.QualifiedName("", "'c'"), Cardinality.ZERO),
                        Map.entry(new NamespaceResolver.QualifiedName("", "'b'"), Cardinality.ZERO)
                ),
                parent.get(new NamespaceResolver.QualifiedName("", "c"))
        );

        assertEquals(
                Map.ofEntries(
                        Map.entry(new NamespaceResolver.QualifiedName("", "x"), Cardinality.ZERO_OR_ONE),
                        Map.entry(new NamespaceResolver.QualifiedName("", "a"), Cardinality.ZERO),
                        Map.entry(new NamespaceResolver.QualifiedName("", "b"), Cardinality.ZERO),
                        Map.entry(new NamespaceResolver.QualifiedName("", "c"), Cardinality.ZERO_OR_ONE),
                        Map.entry(new NamespaceResolver.QualifiedName("", "'a'"), Cardinality.ZERO),
                        Map.entry(new NamespaceResolver.QualifiedName("", "B"), Cardinality.ZERO),
                        Map.entry(new NamespaceResolver.QualifiedName("", "'c'"), Cardinality.ZERO),
                        Map.entry(new NamespaceResolver.QualifiedName("", "'b'"), Cardinality.ZERO)
                ),
                parent.get(new NamespaceResolver.QualifiedName("", "'c'"))
        );

        assertEquals(
                Map.ofEntries(
                        Map.entry(new NamespaceResolver.QualifiedName("", "x"), Cardinality.ZERO_OR_ONE),
                        Map.entry(new NamespaceResolver.QualifiedName("", "a"), Cardinality.ZERO),
                        Map.entry(new NamespaceResolver.QualifiedName("", "b"), Cardinality.ZERO_OR_ONE),
                        Map.entry(new NamespaceResolver.QualifiedName("", "c"), Cardinality.ZERO),
                        Map.entry(new NamespaceResolver.QualifiedName("", "'a'"), Cardinality.ZERO),
                        Map.entry(new NamespaceResolver.QualifiedName("", "B"), Cardinality.ZERO_OR_ONE),
                        Map.entry(new NamespaceResolver.QualifiedName("", "'c'"), Cardinality.ZERO),
                        Map.entry(new NamespaceResolver.QualifiedName("", "'b'"), Cardinality.ZERO)
                ),
                parent.get(new NamespaceResolver.QualifiedName("", "'b'"))
        );

        assertEquals(
                Map.ofEntries(
                        Map.entry(new NamespaceResolver.QualifiedName("", "x"), Cardinality.ZERO_OR_ONE),
                        Map.entry(new NamespaceResolver.QualifiedName("", "a"), Cardinality.ZERO_OR_ONE),
                        Map.entry(new NamespaceResolver.QualifiedName("", "b"), Cardinality.ZERO),
                        Map.entry(new NamespaceResolver.QualifiedName("", "c"), Cardinality.ZERO),
                        Map.entry(new NamespaceResolver.QualifiedName("", "'a'"), Cardinality.ZERO),
                        Map.entry(new NamespaceResolver.QualifiedName("", "B"), Cardinality.ZERO),
                        Map.entry(new NamespaceResolver.QualifiedName("", "'c'"), Cardinality.ZERO),
                        Map.entry(new NamespaceResolver.QualifiedName("", "'b'"), Cardinality.ZERO)
                ),
                parent.get(new NamespaceResolver.QualifiedName("", "'a'"))
        );
    }



}
