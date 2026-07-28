package com.github.akruk.antlrquery.typesystem.types.itemtypes.constraints.grammarexpressions;

import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver.QualifiedName;

public sealed interface GrammarExpression
    permits
        GrammarExpression.AnyGrammarSelector,
        GrammarExpression.RuleSelector,
        GrammarExpression.UnionGrammarSelector,
        GrammarExpression.DifferenceGrammarSelector 
{

    public record AnyGrammarSelector(
        @NonNull QualifiedName grammar
    ) implements GrammarExpression {}

    public record RuleSelector(
        @NonNull QualifiedName rule
    ) implements GrammarExpression {}

    public record UnionGrammarSelector(
        @NonNull List<@NonNull GrammarExpression> selectors
    ) implements GrammarExpression {}

    public record DifferenceGrammarSelector(
        @NonNull GrammarExpression left,
        @NonNull GrammarExpression right
    ) implements GrammarExpression {}
}