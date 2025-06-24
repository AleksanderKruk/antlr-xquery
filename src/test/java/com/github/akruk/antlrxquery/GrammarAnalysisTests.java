package com.github.akruk.antlrxquery;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.junit.Test;

import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;
import com.github.akruk.antlrxquery.inputgrammaranalyzer.InputGrammarAnalyzer;
import com.github.akruk.antlrxquery.inputgrammaranalyzer.InputGrammarAnalyzer.GrammarAnalysisResult;
import java.util.Set;

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

    private GrammarAnalysisResult simpleTokenTestGrammar() {
        String grammar = """
            grammar grammarname;
            A: 'a';
            B: 'b' 'c' 'd';
            C: 'bcd';
            fragment D: 'd';
            fragment E: 'e';
            F: D E;
            FF: D 'k';
            FFF: 'h' E;
            G: 'a'+;
            H: 'a'*;
            I: 'e' 'a'?;
            J: 'e' | 'h';
            K: 'e' | [abcd];
            L: [abcd];
            M: ~'h';
        """;
        return analyzeGrammar(grammar);
    }

    private GrammarAnalysisResult simpleRuleTestGrammar() {
        String grammar = """
            grammar grammarname;
            a: 'a';
            b: 'a' | 'b';
            c: 'a'    # d
                | 'b' # e;
            f: k='c';
            g: z=('c');
            h: z=('c'|'b');
            i: A;
            j: A|;
            jj: a a a;
            jjj: a c c;
            jjjj: a a A B;

            k: 'a'+;
            l: 'a'*;
            m: 'e' 'a'?;
            n: 'e' | 'h';
            r: ~'h';
            A: 'A';
            B: 'B';
        """;
        return analyzeGrammar(grammar);
    }


    @Test
    public void children() throws XQueryUnsupportedOperation {
        final var results = relationshipGrammar();
        final var allExpectedNodes = Set.of("x", "a", "b", "c", "'a'", "B", "'c'");

        final var children = results.children();
        assertTrue(children.keySet().equals(allExpectedNodes));
        assertTrue(children.get("x").equals(Set.of("a", "b", "c")));
        assertTrue(children.get("a").equals(Set.of("'a'")));
        assertTrue(children.get("b").equals(Set.of("B")));
        assertTrue(children.get("c").equals(Set.of("'c'")));

    }

    @Test
    public void descendants() throws XQueryUnsupportedOperation {
        final var results = relationshipGrammar();
        final var allExpectedNodes = Set.of("x", "a", "b", "c", "'a'", "B", "'c'");
        final var descendantsOrSelf = results.descendantsOrSelf();
        assertTrue(descendantsOrSelf.keySet().equals(allExpectedNodes));
        assertTrue(descendantsOrSelf.get("x").equals(Set.of("x", "a", "b", "c", "B", "'a'", "'c'")));
        assertTrue(descendantsOrSelf.get("a").equals(Set.of("a", "'a'")));
        assertTrue(descendantsOrSelf.get("'a'").equals(Set.of("'a'")));
        assertTrue(descendantsOrSelf.get("b").equals(Set.of("b", "B")));
        assertTrue(descendantsOrSelf.get("B").equals(Set.of("B")));
        assertTrue(descendantsOrSelf.get("c").equals(Set.of("c", "'c'")));
        assertTrue(descendantsOrSelf.get("'c'").equals(Set.of("'c'")));
    }


    @Test
    public void descendantsOrSelf() throws XQueryUnsupportedOperation {
        final var results = relationshipGrammar();
        final var descendant = results.descendants();
        final var allExpectedNodes = Set.of("x", "a", "b", "c", "'a'", "B", "'c'");
        assertTrue(descendant.keySet().equals(allExpectedNodes));
        assertTrue(descendant.get("x").equals(Set.of("a", "b", "c", "B", "'a'", "'c'")));
        assertTrue(descendant.get("a").equals(Set.of("'a'")));
        assertTrue(descendant.get("'a'").equals(Set.of()));
        assertTrue(descendant.get("b").equals(Set.of("B")));
        assertTrue(descendant.get("B").equals(Set.of()));
        assertTrue(descendant.get("c").equals(Set.of("'c'")));
        assertTrue(descendant.get("'c'").equals(Set.of()));
    }

    @Test
    public void ancestors() throws XQueryUnsupportedOperation {
        final var results = relationshipGrammar();
        final var allExpectedNodes = Set.of("x", "a", "b", "c", "'a'", "B", "'c'");
        final var ancestors = results.ancestors();
        assertTrue(ancestors.keySet().equals(allExpectedNodes));
        assertTrue(ancestors.get("x").equals(Set.of()));
        assertTrue(ancestors.get("a").equals(Set.of("x")));
        assertTrue(ancestors.get("'a'").equals(Set.of("a", "x")));
        assertTrue(ancestors.get("b").equals(Set.of("x")));
        assertTrue(ancestors.get("B").equals(Set.of("b", "x")));
        assertTrue(ancestors.get("c").equals(Set.of("x")));
        assertTrue(ancestors.get("'c'").equals(Set.of("c", "x")));
    }

    @Test
    public void ancestorsOrSelf() throws XQueryUnsupportedOperation {
        final var results = relationshipGrammar();
        final var allExpectedNodes = Set.of("x", "a", "b", "c", "'a'", "B", "'c'");
        final var ancestorsOrSelf = results.ancestorsOrSelf();
        assertTrue(ancestorsOrSelf.keySet().equals(allExpectedNodes));
        assertTrue(ancestorsOrSelf.get("x").equals(Set.of("x")));
        assertTrue(ancestorsOrSelf.get("a").equals(Set.of("x", "a")));
        assertTrue(ancestorsOrSelf.get("'a'").equals(Set.of("a", "x", "'a'")));
        assertTrue(ancestorsOrSelf.get("b").equals(Set.of("x", "b")));
        assertTrue(ancestorsOrSelf.get("B").equals(Set.of("b", "x", "B")));
        assertTrue(ancestorsOrSelf.get("c").equals(Set.of("x", "c")));
        assertTrue(ancestorsOrSelf.get("'c'").equals(Set.of("c", "x", "'c'")));
    }

    @Test
    public void followingSibling() throws XQueryUnsupportedOperation {
        final var results = relationshipGrammar();
        final var allExpectedNodes = Set.of("x", "a", "b", "c", "'a'", "B", "'c'");
        final var followingSibling = results.followingSibling();
        assertTrue(followingSibling.keySet().equals(allExpectedNodes));
        assertTrue(followingSibling.get("x").equals(Set.of()));
        assertTrue(followingSibling.get("a").equals(Set.of("b", "c")));
        assertTrue(followingSibling.get("'a'").equals(Set.of()));
        assertTrue(followingSibling.get("b").equals(Set.of("c")));
        assertTrue(followingSibling.get("B").equals(Set.of()));
        assertTrue(followingSibling.get("c").equals(Set.of()));
        assertTrue(followingSibling.get("'c'").equals(Set.of()));
    }

    @Test
    public void followingSiblingOrSelf() throws XQueryUnsupportedOperation {
        final var results = relationshipGrammar();
        final var allExpectedNodes = Set.of("x", "a", "b", "c", "'a'", "B", "'c'");
        final var followingSiblingOrSelf = results.followingSiblingOrSelf();
        assertTrue(followingSiblingOrSelf.keySet().equals(allExpectedNodes));
        assertTrue(followingSiblingOrSelf.get("x").equals(Set.of("x")));
        assertTrue(followingSiblingOrSelf.get("a").equals(Set.of("b", "c", "a")));
        assertTrue(followingSiblingOrSelf.get("'a'").equals(Set.of("'a'")));
        assertTrue(followingSiblingOrSelf.get("b").equals(Set.of("c", "b")));
        assertTrue(followingSiblingOrSelf.get("B").equals(Set.of("B")));
        assertTrue(followingSiblingOrSelf.get("c").equals(Set.of("c")));
        assertTrue(followingSiblingOrSelf.get("'c'").equals(Set.of("'c'")));
    }

    @Test
    public void following() throws XQueryUnsupportedOperation {
        final var results = relationshipGrammar();
        final var allExpectedNodes = Set.of("x", "a", "b", "c", "'a'", "B", "'c'");
        final var  following = results.following();
        assertTrue(following.keySet().equals(allExpectedNodes));
        assertTrue(following.get("x").equals(Set.of()));
        assertTrue(following.get("a").equals(Set.of("b", "B", "c", "'c'")));
        assertTrue(following.get("'a'").equals(Set.of("b", "B", "c", "'c'")));
        assertTrue(following.get("b").equals(Set.of("c", "'c'")));
        assertTrue(following.get("B").equals(Set.of("c", "'c'")));
        assertTrue(following.get("c").equals(Set.of()));
        assertTrue(following.get("'c'").equals(Set.of()));
    }

    @Test
    public void followingOrSelf() throws XQueryUnsupportedOperation {
        final var results = relationshipGrammar();
        final var allExpectedNodes = Set.of("x", "a", "b", "c", "'a'", "B", "'c'");
        final var followingOrSelf = results.followingOrSelf();
        assertTrue(followingOrSelf.keySet().equals(allExpectedNodes));
        assertTrue(followingOrSelf.get("x").equals(Set.of("x")));
        assertTrue(followingOrSelf.get("a").equals(Set.of("a","b", "B", "c", "'c'")));
        assertTrue(followingOrSelf.get("'a'").equals(Set.of("'a'", "b", "B", "c", "'c'")));
        assertTrue(followingOrSelf.get("b").equals(Set.of("b", "c", "'c'")));
        assertTrue(followingOrSelf.get("B").equals(Set.of("B", "c", "'c'")));
        assertTrue(followingOrSelf.get("c").equals(Set.of("c")));
        assertTrue(followingOrSelf.get("'c'").equals(Set.of("'c'")));
    }


    @Test
    public void precedingSibling() throws XQueryUnsupportedOperation {
        final var results = relationshipGrammar();
        final var allExpectedNodes = Set.of("x", "a", "b", "c", "'a'", "B", "'c'");
        final var precedingSibling = results.precedingSibling();
        assertTrue(precedingSibling.keySet().equals(allExpectedNodes));
        assertTrue(precedingSibling.get("x").equals(Set.of()));
        assertTrue(precedingSibling.get("a").equals(Set.of()));
        assertTrue(precedingSibling.get("'a'").equals(Set.of()));
        assertTrue(precedingSibling.get("b").equals(Set.of("a")));
        assertTrue(precedingSibling.get("B").equals(Set.of()));
        assertTrue(precedingSibling.get("c").equals(Set.of("a", "b")));
        assertTrue(precedingSibling.get("'c'").equals(Set.of()));
    }

    @Test
    public void precedingSiblingOrSelf() throws XQueryUnsupportedOperation {
        final var results = relationshipGrammar();
        final var allExpectedNodes = Set.of("x", "a", "b", "c", "'a'", "B", "'c'");
        final var  precedingSiblingOrSelf = results.precedingSiblingOrSelf();
        assertTrue(precedingSiblingOrSelf.keySet().equals(allExpectedNodes));
        assertTrue(precedingSiblingOrSelf.get("x").equals(Set.of("x")));
        assertTrue(precedingSiblingOrSelf.get("a").equals(Set.of("a")));
        assertTrue(precedingSiblingOrSelf.get("'a'").equals(Set.of("'a'")));
        assertTrue(precedingSiblingOrSelf.get("b").equals(Set.of("a", "b")));
        assertTrue(precedingSiblingOrSelf.get("B").equals(Set.of("B")));
        assertTrue(precedingSiblingOrSelf.get("c").equals(Set.of("a", "b", "c")));
        assertTrue(precedingSiblingOrSelf.get("'c'").equals(Set.of("'c'")));
    }


    @Test
    public void preceding() throws XQueryUnsupportedOperation {
        final var results = relationshipGrammar();
        final var allExpectedNodes = Set.of("x", "a", "b", "c", "'a'", "B", "'c'");
        final var preceding = results.preceding();
        assertTrue(preceding.keySet().equals(allExpectedNodes));
        assertTrue(preceding.get("x").equals(Set.of()));
        assertTrue(preceding.get("a").equals(Set.of()));
        assertTrue(preceding.get("'a'").equals(Set.of()));
        assertTrue(preceding.get("b").equals(Set.of("a", "'a'")));
        assertTrue(preceding.get("B").equals(Set.of("a", "'a'")));
        assertTrue(preceding.get("c").equals(Set.of("a", "'a'", "b", "B")));
        assertTrue(preceding.get("'c'").equals(Set.of("a", "'a'", "b", "B")));
    }

    @Test
    public void precedingOrSelf() throws XQueryUnsupportedOperation {
        final var results = relationshipGrammar();
        final var allExpectedNodes = Set.of("x", "a", "b", "c", "'a'", "B", "'c'");
        final var  precedingOrSelf = results.precedingOrSelf();
        assertTrue(precedingOrSelf.keySet().equals(allExpectedNodes));
        assertTrue(precedingOrSelf.get("x").equals(Set.of("x")));
        assertTrue(precedingOrSelf.get("a").equals(Set.of("a")));
        assertTrue(precedingOrSelf.get("'a'").equals(Set.of("'a'")));
        assertTrue(precedingOrSelf.get("b").equals(Set.of("a", "'a'", "b")));
        assertTrue(precedingOrSelf.get("B").equals(Set.of("a", "'a'", "B")));
        assertTrue(precedingOrSelf.get("c").equals(Set.of("a", "'a'", "b", "B", "c")));
        assertTrue(precedingOrSelf.get("'c'").equals(Set.of("a", "'a'", "b", "B", "'c'")));
    }

    @Test
    public void simpleTokens() throws XQueryUnsupportedOperation {
        final var results = simpleTokenTestGrammar();
        final var simpleTokens = results.simpleTokens();
        // only strings
        assertTrue(simpleTokens.contains("A"));
        assertTrue(simpleTokens.contains("B"));
        assertTrue(simpleTokens.contains("C"));

        // no fragments
        assertFalse(simpleTokens.contains("D"));
        assertFalse(simpleTokens.contains("E"));

        // recursive simplicity
        assertTrue(simpleTokens.contains("F"));
        assertTrue(simpleTokens.contains("FF"));
        assertTrue(simpleTokens.contains("FFF"));

        // no modifiers
        assertFalse(simpleTokens.contains("G"));
        assertFalse(simpleTokens.contains("H"));
        assertFalse(simpleTokens.contains("I"));

        // no alternatives
        assertFalse(simpleTokens.contains("J"));
        assertFalse(simpleTokens.contains("K"));

        // no character classes
        assertFalse(simpleTokens.contains("L"));
        // no sets
        assertFalse(simpleTokens.contains("M"));
    }

    @Test
    public void simpleRules() throws XQueryUnsupportedOperation {
        final var results = simpleRuleTestGrammar();
        final var simpleRules = results.simpleRules();
        // only strings
        assertTrue(simpleRules.contains("a"));
        // no alternatives
        assertFalse(simpleRules.contains("b"));
        // named labels
        assertFalse(simpleRules.contains("c"));
        assertTrue(simpleRules.contains("d"));
        assertTrue(simpleRules.contains("e"));

        assertTrue(simpleRules.contains("f"));
        // parenthesis
        assertTrue(simpleRules.contains("g"));
        assertFalse(simpleRules.contains("h"));

        assertTrue(simpleRules.contains("i"));
        assertFalse(simpleRules.contains("j"));

        // recursive simplicity
        assertTrue(simpleRules.contains("jj"));
        assertFalse(simpleRules.contains("jjj"));
        assertTrue(simpleRules.contains("jjjj"));


        // no modifiers
        assertFalse(simpleRules.contains("k"));
        assertFalse(simpleRules.contains("l"));
        assertFalse(simpleRules.contains("m"));

        // no alternatives
        assertFalse(simpleRules.contains("n"));

        // no sets
        assertFalse(simpleRules.contains("r"));
    }


}
