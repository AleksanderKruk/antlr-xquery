package com.github.akruk.antlrxquery;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.junit.Test;

import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;
import com.github.akruk.antlrxquery.inputgrammaranalyzer.InputGrammarAnalyzer;
import com.github.akruk.antlrxquery.typesystem.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryEnumTypeFactory;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class GrammarAnalysisTests {
    @Test
    public void x() throws XQueryUnsupportedOperation {
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
        final var descendant = results.descendants();
        final var descendantOrSelfs = results.descendantsOrSelf();
        final var ancestors = results.ancestors();
        final var ancestorsOrSelf = results.ancestorsOrSelf();
        final var followingSibling = results.followingSibling();
        final var followingSiblingOrSelf = results.followingSiblingOrSelf();

        final var allExpectedNodes = Set.of("x", "a", "b", "c", "'a'", "B", "'c'");

        final var children = results.children();
        assertTrue(children.keySet().equals(allExpectedNodes));
        assertTrue(children.get("x").equals(Set.of("a", "b", "c")));
        assertTrue(children.get("a").equals(Set.of("'a'")));
        assertTrue(children.get("b").equals(Set.of("B")));
        assertTrue(children.get("c").equals(Set.of("'c'")));

        final var parents = results.parent();
        assertTrue(parents.keySet().equals(allExpectedNodes));
        assertTrue(parents.get("x").equals(Set.of()));
        assertTrue(parents.get("a").equals(Set.of("x")));
        assertTrue(parents.get("'a'").equals(Set.of("a")));
        assertTrue(parents.get("b").equals(Set.of("x")));
        assertTrue(parents.get("B").equals(Set.of("b")));
        assertTrue(parents.get("c").equals(Set.of("x")));
        assertTrue(parents.get("'c'").equals(Set.of("c")));



    }
}
