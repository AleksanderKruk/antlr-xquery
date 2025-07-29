package com.github.akruk.antlrxquery.typesystem.typeoperations;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Token;
import com.github.akruk.antlrxquery.XQueryAxis;
import com.github.akruk.antlrxquery.inputgrammaranalyzer.InputGrammarAnalyzer.GrammarAnalysisResult;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class SequencetypePathOperator {
    private final XQueryTypeFactory typeFactory;
    private final XQuerySequenceType anyNodes;
    private final XQuerySequenceType emptySequence;
    private final Parser parser;

    public SequencetypePathOperator(XQueryTypeFactory typeFactory, Parser parser) {
        this.typeFactory = typeFactory;
        this.anyNodes = typeFactory.zeroOrMore(typeFactory.itemAnyNode());
        emptySequence = typeFactory.emptySequence();
        empty = new PathOperatorResult(emptySequence, true, Set.of(), Set.of());
        this.parser = parser;
        wildcard = new PathOperatorResult(anyNodes, false, Set.of(), Set.of());
    }

    public record PathOperatorResult(
        XQuerySequenceType result,
        boolean isEmptyTarget,
        Set<String> invalidNames,
        Set<String> duplicateNames) { }

    public PathOperatorResult pathOperator(
            XQuerySequenceType type,
            XQueryAxis axis,
            Set<String> names,
            GrammarAnalysisResult analysis)
    {
        boolean hasProvidedAnalysis = analysis == null;
        boolean usesWildcard = names == null;
        switch(axis) {
        case ANCESTOR:
        case CHILD:
        case DESCENDANT:
        case FOLLOWING:
        case FOLLOWING_SIBLING:
        case PRECEDING:
        case PRECEDING_SIBLING:
            return switch(type.occurence) {
                case ZERO -> empty;
                default   -> zeroOrMore(names, usesWildcard, hasProvidedAnalysis);
            };
        case ANCESTOR_OR_SELF:
        case DESCENDANT_OR_SELF:
        case FOLLOWING_OR_SELF:
        case FOLLOWING_SIBLING_OR_SELF:
        case PRECEDING_OR_SELF:
        case PRECEDING_SIBLING_OR_SELF:
            return switch(type.occurence) {
                case ZERO         -> empty;
                case ONE          -> oneOrMore(names, usesWildcard, hasProvidedAnalysis);
                case ONE_OR_MORE  -> oneOrMore(names, usesWildcard, hasProvidedAnalysis);
                case ZERO_OR_ONE  -> zeroOrMore(names, usesWildcard, hasProvidedAnalysis);
                case ZERO_OR_MORE -> zeroOrMore(names, usesWildcard, hasProvidedAnalysis);
            };
        case PARENT:
            return switch(type.occurence) {
                case ZERO         -> new PathOperatorResult(emptySequence, true, Set.of(), Set.of());
                case ONE          -> zeroOrOne(names, usesWildcard, hasProvidedAnalysis);
                case ZERO_OR_ONE  -> zeroOrOne(names, usesWildcard, hasProvidedAnalysis);
                case ZERO_OR_MORE -> zeroOrMore(names, usesWildcard, hasProvidedAnalysis);
                case ONE_OR_MORE  -> zeroOrMore(names, usesWildcard, hasProvidedAnalysis);
            };
        case SELF:
            return new PathOperatorResult(type, type.isZero, Set.of(), Set.of());
        }
        return null; // unreachable
    }

    private final Predicate<String> canBeTokenName = Pattern.compile("^[\\p{IsUppercase}].*").asPredicate();
    private final PathOperatorResult empty;
    private final PathOperatorResult wildcard;

    private PathOperatorResult zeroOrMore(Set<String> names, boolean usesWildcard, boolean hasProvidedAnalysis) {
        if (hasProvidedAnalysis)
            return zeroOrMoreAnalyzed(names, usesWildcard);
        else
            return zeroOrMoreSimple(names, usesWildcard);

    }

    private PathOperatorResult oneOrMore(Set<String> names, boolean usesWildcard, boolean hasProvidedAnalysis) {
        if (hasProvidedAnalysis)
            return oneOrMoreAnalyzed(names, usesWildcard);
        else
            return oneOrMoreSimple(names, usesWildcard);
    }

    private PathOperatorResult zeroOrOne(Set<String> names, boolean usesWildcard, boolean hasProvidedAnalysis) {
        if (hasProvidedAnalysis)
            return zeroOrOneAnalyzed(names, usesWildcard);
        else
            return zeroOrOneSimple(names, usesWildcard);
    }



    private PathOperatorResult zeroOrMoreSimple(Set<String> names, boolean usesWildcard) {
        if (usesWildcard) {
            return new PathOperatorResult(anyNodes, false, Set.of(), Set.of());
        } else {
            final Set<String> invalidNames = new HashSet<>(names.size());
            final Set<String> duplicateNames = new HashSet<>(names.size());
            validateNames(names, invalidNames, duplicateNames);
            final var elements = typeFactory.zeroOrMore(typeFactory.itemElement(names));
            return new PathOperatorResult(elements, false, invalidNames, duplicateNames);
        }
    }

    private PathOperatorResult oneOrMoreSimple(Set<String> names, boolean usesWildcard) {
        if (usesWildcard) {
            return new PathOperatorResult(anyNodes, false, Set.of(), Set.of());
        } else {
            final Set<String> invalidNames = new HashSet<>(names.size());
            final Set<String> duplicateNames = new HashSet<>(names.size());
            validateNames(names, invalidNames, duplicateNames);
            final var elements = typeFactory.oneOrMore(typeFactory.itemElement(names));
            return new PathOperatorResult(elements, false, invalidNames, duplicateNames);
        }
    }

    private PathOperatorResult zeroOrOneSimple(Set<String> names, boolean usesWildcard) {
        if (usesWildcard) {
            return new PathOperatorResult(anyNodes, false, Set.of(), Set.of());
        } else {
            final Set<String> invalidNames = new HashSet<>(names.size());
            final Set<String> duplicateNames = new HashSet<>(names.size());
            validateNames(names, invalidNames, duplicateNames);
            final var elements = typeFactory.zeroOrOne(typeFactory.itemElement(names));
            return new PathOperatorResult(elements, false, invalidNames, duplicateNames);
        }
    }



    private PathOperatorResult zeroOrMoreAnalyzed(Set<String> names, boolean usesWildcard) {
        // TODO :
        if (usesWildcard) {
            return new PathOperatorResult(anyNodes, false, Set.of(), Set.of());
        } else {
            final Set<String> invalidNames = new HashSet<>(names.size());
            final Set<String> duplicateNames = new HashSet<>(names.size());
            validateNames(names, invalidNames, duplicateNames);
            final var elements = typeFactory.zeroOrMore(typeFactory.itemElement(names));
            return new PathOperatorResult(elements, false, invalidNames, duplicateNames);
        }
    }

    private PathOperatorResult oneOrMoreAnalyzed(Set<String> names, boolean usesWildcard) {
        // TODO :
        if (usesWildcard) {
            return wildcard;
        } else {
            final Set<String> invalidNames = new HashSet<>(names.size());
            final Set<String> duplicateNames = new HashSet<>(names.size());
            validateNames(names, invalidNames, duplicateNames);
            final var elements = typeFactory.oneOrMore(typeFactory.itemElement(names));
            return new PathOperatorResult(elements, false, invalidNames, duplicateNames);
        }
    }

    private PathOperatorResult zeroOrOneAnalyzed(Set<String> names, boolean usesWildcard) {
        // TODO :
        if (usesWildcard) {
            return new PathOperatorResult(anyNodes, false, Set.of(), Set.of());
        } else {
            final Set<String> invalidNames = new HashSet<>(names.size());
            final Set<String> duplicateNames = new HashSet<>(names.size());
            validateNames(names, invalidNames, duplicateNames);
            final var elements = typeFactory.zeroOrOne(typeFactory.itemElement(names));
            return new PathOperatorResult(elements, false, invalidNames, duplicateNames);
        }
    }




    private void validateNames(Set<String> names, final Set<String> invalidNames, final Set<String> duplicateNames) {
        for (final String name : names) {
            if (canBeTokenName.test(name)) { // test for token type
                final int tokenType = parser.getTokenType(name);
                if (tokenType == Token.INVALID_TYPE) {
                    boolean hasAdded = invalidNames.add(name);
                    if (!hasAdded) {
                        duplicateNames.add(name);
                    }
                }
            } else { // test for rule
                final int ruleIndex = parser.getRuleIndex(name);
                if (ruleIndex == -1) {
                    boolean hasAdded = invalidNames.add(name);
                    if (!hasAdded) {
                        duplicateNames.add(name);
                    }
                }
            }
        }
    }

                    // final String msg = String.format("Token name: %s is not recognized by parser %s", name,
                    //     parser.toString());
                    // error(test.qname(), msg);
                    // final String msg = String.format("Rule name: %s is not recognized by parser %s", name,
                    //     parser.getClass().toString());
                    // error(test.qname(), msg);


}
