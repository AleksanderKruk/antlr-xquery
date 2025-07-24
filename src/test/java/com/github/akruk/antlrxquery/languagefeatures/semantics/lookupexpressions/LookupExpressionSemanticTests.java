package com.github.akruk.antlrxquery.languagefeatures.semantics.lookupexpressions;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.semantics.SemanticTestsBase;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;

public class LookupExpressionSemanticTests extends SemanticTestsBase {
    @Test
    public void lookupOnEmptyMap() {
        final XQuerySequenceType anyItems = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
        assertType("map {} ? abc", anyItems);
        assertType("map {} ? 'a b c'", anyItems);
        assertType("map {} ? 2", anyItems);
        assertType("let $var := 1 return map {} ? $var", anyItems);
        assertType("map {} ? (5)", anyItems);
        assertType("map {} ? (5, 6, 7)", anyItems);
        assertType("map {} ? *", anyItems);
    }

    @Test
    public void lookupOnEmptyArray() {
        final XQuerySequenceType zeroOrMore = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
        assertErrors("array {} ? abc");
        assertErrors("array {} ? 'a b c'");
        assertType("array {} ? 2", zeroOrMore);
        assertType("let $var := 1 return array {} ? $var", zeroOrMore);
        assertType("array {} ? (5)", zeroOrMore);
        assertType("array {} ? (5, 6, 7)", zeroOrMore);
        assertType("array {} ? *", zeroOrMore);
    }

    @Test
    public void lookupOnNonEmptyMaps() {
        final XQueryItemType abEnum = typeFactory.itemEnum(Set.of("a", "b"));
        assertErrors("map {1: 'a', 2: 'b'} ? abc");
        assertErrors("map {1: 'a', 2: 'b'} ? 'a b c'");
        assertType("map {1: 'a', 2: 'b'} ? 1", typeFactory.zeroOrOne(abEnum));
        assertType("map {1: 'a', 2: 'b'} ? 0", typeFactory.zeroOrOne(abEnum));
        assertType("map {1: 'a', 2: 'b'} ? ()", typeFactory.emptySequence());
        assertType("map {1: 'a', 2: 'b'} ? (1, 2, 3)", typeFactory.zeroOrMore(abEnum));
        assertType("map {1: 'a', 2: 'b'} ? *", typeFactory.zeroOrMore(abEnum));
    }

    @Test
    public void lookupOnRecords() {
        final XQueryItemType aEnum = typeFactory.itemEnum(Set.of("a"));
        final XQueryItemType bEnum = typeFactory.itemEnum(Set.of("b"));
        final XQueryItemType abEnum = typeFactory.itemEnum(Set.of("a", "b"));
        assertType("map {'abc': 'a', 'a': 'b'} ? abc", typeFactory.one(aEnum));
        assertType("map {'abc': 'a', 'a': 'b'} ? 'a'", typeFactory.one(bEnum));
        assertType("map {'abc': 'a', 'a': 'b'} ? ('abc', 'a')", typeFactory.oneOrMore(abEnum));
        assertType("map {'abc': 'a', 'a': 'b'} ? ()", typeFactory.emptySequence());
        assertErrors("map {'abc': 'a', 'a': 'b'} ? 'b'");
    }

    // [ { "John": 3, "Jill": 5}, {"Peter": 8, "Mary": 6} ]

}
