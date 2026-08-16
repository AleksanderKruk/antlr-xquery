package com.github.akruk.antlrquery.languagefeatures.semantics.lookupexpressions;

import java.util.List;
import java.util.Set;

import com.github.akruk.antlrquery.semanticanalyzer.ErrorType;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import org.junit.jupiter.api.Test;

import com.github.akruk.antlrquery.languagefeatures.semantics.SemanticTestsBase;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.AntlrQueryItemType;

public class LookupExpressionSemanticTests extends SemanticTestsBase {
    @Test
    public void lookupOnEmptyRecord() {
        final AntlrQuerySequenceType emptySequence = typeFactory.emptySequence();
        assertDiagnostics("{} ? abc", List.of(ErrorType.LOOKUP__INVALID_RECORD_KEY_TYPE), List.of());
        assertDiagnostics("{} ? 'a b c'", List.of(ErrorType.LOOKUP__INVALID_RECORD_KEY_TYPE), List.of());
        assertType("{} ? *", emptySequence);
        assertDiagnostics("{} ? 1", List.of(ErrorType.LOOKUP__INVALID_RECORD_KEY_TYPE), List.of());
    }

    @Test
    public void lookupOnEmptyMap() {
        final AntlrQuerySequenceType emptySequence = typeFactory.emptySequence();
        assertType("map {} ? abc", emptySequence);
        assertType("map {} ? 'a b c'", emptySequence);
        assertType("map {} ? *", emptySequence);
        assertType("map {} ? 1", emptySequence);
    }

    @Test
    public void lookupOnEmptyArray() {
        assertDiagnostics("array {} ? abc", List.of(ErrorType.LOOKUP__INVALID_ARRAY_KEY__WRONG_TYPE), List.of());
        assertDiagnostics("array {} ? 'a b c'", List.of(ErrorType.LOOKUP__INVALID_ARRAY_KEY__WRONG_TYPE), List.of());
        assertDiagnostics("array {} ? 2", List.of(ErrorType.LOOKUP__INVALID_ARRAY_KEY__INDEX_OUTSIDE_OF_RANGE), List.of());
        assertDiagnostics("array {} ? (5)", List.of(ErrorType.LOOKUP__INVALID_ARRAY_KEY__INDEX_OUTSIDE_OF_RANGE), List.of());
        assertDiagnostics("array {} ? (5, 6, 7)", List.of(ErrorType.LOOKUP__INVALID_ARRAY_KEY__INDEX_OUTSIDE_OF_RANGE), List.of());
        assertType("array {} ? *", typeFactory.emptySequence());

        assertDiagnostics("[] ? abc", List.of(ErrorType.LOOKUP__INVALID_ARRAY_KEY__WRONG_TYPE), List.of());
        assertDiagnostics("[] ? 'a b c'", List.of(ErrorType.LOOKUP__INVALID_ARRAY_KEY__WRONG_TYPE), List.of());
        assertDiagnostics("[] ? 2", List.of(ErrorType.LOOKUP__INVALID_ARRAY_KEY__INDEX_OUTSIDE_OF_RANGE), List.of());
        assertDiagnostics("[] ? (5)", List.of(ErrorType.LOOKUP__INVALID_ARRAY_KEY__INDEX_OUTSIDE_OF_RANGE), List.of());
        assertDiagnostics("[] ? (5, 6, 7)", List.of(ErrorType.LOOKUP__INVALID_ARRAY_KEY__INDEX_OUTSIDE_OF_RANGE), List.of());
        assertType("[] ? *", typeFactory.emptySequence());

    }

    @Test
    public void lookupOnNonEmptyMaps() {
        final AntlrQueryItemType abEnum = typeFactory.itemEnum(Set.of("a", "b"));
        assertDiagnostics("map {1: 'a', 2: 'b'} ? abc", List.of(ErrorType.LOOKUP__MAP_INVALID_KEY__WRONG_TYPE), List.of());
        assertDiagnostics("map {1: 'a', 2: 'b'} ? 'a b c'", List.of(ErrorType.LOOKUP__MAP_INVALID_KEY__WRONG_TYPE), List.of());
        assertType("map {1: 'a', 2: 'b'} ? 1", typeFactory.one(abEnum));
        assertDiagnostics("map {1: 'a', 2: 'b'} ? 0", List.of(ErrorType.LOOKUP__MAP_INVALID_KEY__WRONG_TYPE), List.of());
        assertType("map {1: 'a', 2: 'b'} ? ()", typeFactory.emptySequence());
        assertType("map {1: 'a', 2: 'b'} ? (1, 2, 3)", typeFactory.sequence(abEnum, Cardinality.inclusiveRange(0, 3)));
        assertType("map {1: 'a', 2: 'b'} ? (1, 2, 1, 2)", typeFactory.sequence(abEnum, Cardinality.of(4)));
        assertType("map {1: 'a', 2: 'b'} ? *", typeFactory.sequence(abEnum, Cardinality.ONE_OR_MORE)); // TODO: currently no way to track non-record map literal key cardinality
    }

    @Test
    public void lookupOnRecords() {
        final AntlrQueryItemType aEnum = typeFactory.itemEnum(Set.of("a"));
        final AntlrQueryItemType bEnum = typeFactory.itemEnum(Set.of("b"));
        final AntlrQueryItemType abEnum = typeFactory.itemEnum(Set.of("a", "b"));
        assertType("map {'abc': 'a', 'a': 'b'} ? abc", typeFactory.one(aEnum));
        assertType("map {'abc': 'a', 'a': 'b'} ? 'a'", typeFactory.one(bEnum));
        assertType("map {'abc': 'a', 'a': 'b'} ? ('abc', 'a')", typeFactory.sequence(abEnum, Cardinality.of(2)));
        assertType("map {'abc': 'a', 'a': 'b'} ? ()", typeFactory.emptySequence());
        assertDiagnostics("map {'abc': 'a', 'a': 'b'} ? 'b'", List.of(ErrorType.LOOKUP__INVALID_RECORD_KEY_TYPE), List.of());
    }

    // [ { "John": 3, "Jill": 5}, {"Peter": 8, "Mary": 6} ]

}
