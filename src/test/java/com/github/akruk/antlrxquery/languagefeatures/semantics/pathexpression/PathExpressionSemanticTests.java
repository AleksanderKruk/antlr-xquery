package com.github.akruk.antlrxquery.languagefeatures.semantics.pathexpression;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.semantics.SemanticTestsBase;

public class PathExpressionSemanticTests extends SemanticTestsBase {
    @Test
    public void pathNamedDescendantsOrSelf() {
        final var xs = typeFactory.zeroOrMore(typeFactory.itemElement(Set.of("x")));
        final var xys = typeFactory.zeroOrMore(typeFactory.itemElement(Set.of("x", "y")));
        final var oneOrMoreNodes = typeFactory.oneOrMore(typeFactory.itemAnyNode());
        assertType("//x", xs);
        assertType("//(x|y)", xys);
        assertType("//*", oneOrMoreNodes);
    }

    @Test
    public void pathChild() {
        final var xs = typeFactory.zeroOrMore(typeFactory.itemElement(Set.of("x")));
        final var xys = typeFactory.zeroOrMore(typeFactory.itemElement(Set.of("x", "y")));
        final var zeroOrMoreNodes = typeFactory.zeroOrMore(typeFactory.itemAnyNode());
        assertType("/x", xs);
        assertType("/(x|y)", xys);
        assertType("/*", zeroOrMoreNodes);
    }

}
