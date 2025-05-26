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

    private GrammarAnalysisResult testGrammar() {
        InputGrammarAnalyzer analyzer = new InputGrammarAnalyzer();
        CharStream stream = CharStreams.fromString(
            """
                grammar grammarname;
                x: a b c;
                a: 'a';
                b: B;
                c: 'c';
                B: 'b';
        """);
        final var results = analyzer.analyze(stream);
        return results;
    }

    @Test
    public void children() throws XQueryUnsupportedOperation {
        final var results = testGrammar();
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
        final var results = testGrammar();
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
        final var results = testGrammar();
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
        final var results = testGrammar();
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
        final var results = testGrammar();
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
        final var results = testGrammar();
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
        final var results = testGrammar();
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
        final var results = testGrammar();
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
        final var results = testGrammar();
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
        final var results = testGrammar();
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
        final var results = testGrammar();
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

}
