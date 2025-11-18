package com.github.akruk.antlrxquery.languagefeatures.semantics.pathexpression;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.semantics.SemanticTestsBase;
import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver.QualifiedName;;

public class PathExpressionSemanticTests extends SemanticTestsBase {
    private final Set<QualifiedName> xSet = Set.of(new QualifiedName("", "x"));
    private final Set<QualifiedName> xySet = Set.of(new QualifiedName("", "x"), new QualifiedName("", "y"));

    @Test
    public void pathNamedDescendantsOrSelf() {
        final var xs = typeFactory.zeroOrMore(typeFactory.itemElement(xSet));
        final var xys = typeFactory.zeroOrMore(typeFactory.itemElement(xySet));
        final var oneOrMoreNodes = typeFactory.oneOrMore(typeFactory.itemAnyNode());
        assertType("//x", xs);
        assertType("//(x|y)", xys);
        assertType("//*", oneOrMoreNodes);
    }

    @Test
    public void pathChild() {
        final var xs = typeFactory.zeroOrMore(typeFactory.itemElement(xSet));
        final var xys = typeFactory.zeroOrMore(typeFactory.itemElement(xySet));
        final var zeroOrMoreNodes = typeFactory.zeroOrMore(typeFactory.itemAnyNode());
        assertType("/x", xs);
        assertType("/(x|y)", xys);
        assertType("/*", zeroOrMoreNodes);
    }

    @Test
    public void predicates() {
        final var xs = typeFactory.zeroOrMore(typeFactory.itemElement(xSet));
        assertType("/x[true()]", xs);
    }

}
