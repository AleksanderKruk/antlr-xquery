package com.github.akruk.antlrxquery.inputgrammaranalyzer;

import com.github.akruk.antlrgrammar.ANTLRv4Parser;
import com.github.akruk.antlrgrammar.ANTLRv4ParserBaseVisitor;
import java.util.*;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.tree.ParseTree;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryCardinality;
import com.github.akruk.antlrxquery.typesystem.typeoperations.occurence.AlternativeCardinalityMerger;
import com.github.akruk.antlrxquery.typesystem.typeoperations.occurence.BlockCardinalityMerger;
import com.github.akruk.antlrxquery.typesystem.typeoperations.occurence.SequenceCardinalityMerger;

class SiblingCardinalityAnalyzer extends ANTLRv4ParserBaseVisitor<Map<String, Map<String, XQueryCardinality>>> {
    final Map<String, Map<String, XQueryCardinality>> childrenMapping;
    final Map<String, Map<String, XQueryCardinality>> followingSiblingMapping;
    final Map<String, Map<String, XQueryCardinality>> precedingSiblingMapping;
    final AlternativeCardinalityMerger alternativeCardinalityMerger;
    final SequenceCardinalityMerger sequenceCardinalityMerger;
    final BlockCardinalityMerger blockCardinalityMerger;
    final Set<String> nodeNames;
    Map<String, XQueryCardinality> currentSubMapping;
    List<String> currentSequence;

    public SiblingCardinalityAnalyzer(final Set<String> nodeNames, final ANTLRv4Parser antlrParser) {
        this.nodeNames = nodeNames;
        this.childrenMapping = getMapping(nodeNames);
        this.followingSiblingMapping = getMapping(nodeNames);
        this.precedingSiblingMapping = getMapping(nodeNames);
        alternativeCardinalityMerger = new AlternativeCardinalityMerger();
        sequenceCardinalityMerger = new SequenceCardinalityMerger();
        blockCardinalityMerger = new BlockCardinalityMerger();
        currentSequence = new ArrayList<>();
    }

    private Map<String, Map<String, XQueryCardinality>> getMapping(final Set<String> nodeNames) {
        final var map = new HashMap<String, Map<String, XQueryCardinality>>(nodeNames.size(), 1);
        for (final var nodeName : nodeNames) {
            final var subHashMap = new HashMap<String, XQueryCardinality>(nodeNames.size(), 1);
            for (final var sub : nodeNames) {
                subHashMap.put(sub, XQueryCardinality.ZERO);
            }
            map.put(nodeName, subHashMap);
        }
        return map;
    }

    private Map<String, XQueryCardinality> getSubMapping(final Set<String> elements) {
        final var subHashMap = new HashMap<String, XQueryCardinality>(nodeNames.size(), 1);
        for (final var sub : elements) {
            subHashMap.put(sub, XQueryCardinality.ZERO);
        }
        return subHashMap;
    }

    XQueryCardinality visitedCardinality;
    String visitedRef;

    @Override
    public Map<String, Map<String, XQueryCardinality>> visitEbnfSuffix(final ANTLRv4Parser.EbnfSuffixContext ctx) {
        if (ctx.STAR() != null)
            visitedCardinality = XQueryCardinality.ZERO_OR_MORE;
        else if (ctx.PLUS() != null)
            visitedCardinality = XQueryCardinality.ONE_OR_MORE;
        else if (ctx.QUESTION() != null)
            visitedCardinality = XQueryCardinality.ZERO_OR_ONE;
        else
            visitedCardinality = XQueryCardinality.ONE;
        return null;
    }

    @Override
    public Map<String, Map<String, XQueryCardinality>> visitLexerRuleSpec(ANTLRv4Parser.LexerRuleSpecContext ctx) {
        final String currentRuleRef = ctx.TOKEN_REF().getText();
        super.visitLexerRuleSpec(ctx);
        childrenMapping.put(currentRuleRef, currentSubMapping);
        return null;
    }

    @Override
    public Map<String, Map<String, XQueryCardinality>> visitParserRuleSpec(final ANTLRv4Parser.ParserRuleSpecContext ctx) {
        final String currentRuleRef = ctx.RULE_REF().getText();
        super.visitParserRuleSpec(ctx);
        childrenMapping.put(currentRuleRef, currentSubMapping);
        return null;
    }

    @Override
    public Map<String, Map<String, XQueryCardinality>> visitLexerElement(ANTLRv4Parser.LexerElementContext ctx) {
        if (ctx.actionBlock() != null)
            return null;
        XQueryCardinality declaredCardinality = ctx.ebnfSuffix() == null ? XQueryCardinality.ONE : visitedCardinality;

        if (ctx.lexerBlock() != null) {
            ctx.lexerBlock().accept(this);
            blockMergeSubmapping(declaredCardinality);
        } else {
            ctx.lexerAtom().accept(this);
            if (visitedRef != null) {
                updateSiblingMappings(visitedRef);
                XQueryCardinality current = currentSubMapping.get(visitedRef);
                byte mergedOrdinal = sequenceCardinalityMerger.merge(declaredCardinality.ordinal(), current.ordinal());
                XQueryCardinality merged = XQueryCardinality.values()[mergedOrdinal];
                currentSubMapping.put(visitedRef, merged);
            }
        }
        return null;
    }

    @Override
    public Map<String, Map<String, XQueryCardinality>> visitElement(ANTLRv4Parser.ElementContext ctx) {
        if (ctx.actionBlock() != null)
            return null;
        if (ctx.ebnf() != null)
            return ctx.ebnf().accept(this);

        XQueryCardinality declaredCardinality = ctx.ebnfSuffix() == null ? XQueryCardinality.ONE : visitedCardinality;

        if (ctx.labeledElement() != null) {
            if (ctx.labeledElement().block() != null) {
                ctx.labeledElement().block().accept(this);
                blockMergeSubmapping(declaredCardinality);
            } else {
                ctx.labeledElement().atom().accept(this);
            }
        } else {
            ctx.atom().accept(this);
        }

        if (visitedRef != null) {
            updateSiblingMappings(visitedRef);
            XQueryCardinality current = currentSubMapping.get(visitedRef);
            byte mergedOrdinal = sequenceCardinalityMerger.merge(declaredCardinality.ordinal(), current.ordinal());
            XQueryCardinality merged = XQueryCardinality.values()[mergedOrdinal];
            currentSubMapping.put(visitedRef, merged);
        }
        return null;
    }

    private void blockMergeSubmapping(XQueryCardinality declaredCardinality) {
        for (final var entry : currentSubMapping.entrySet()) {
            final String ruleName = entry.getKey();
            final XQueryCardinality currentCardinality = currentSubMapping.get(ruleName);
            final XQueryCardinality merged = blockCardinalityMerger.merge(declaredCardinality, currentCardinality);
            currentSubMapping.put(ruleName, merged);
        }
    }

    private void updateSiblingMappings(String currentRef) {
        for (String ref : currentSequence) {
            followingSiblingMapping.get(ref).put(currentRef, XQueryCardinality.ONE);
            precedingSiblingMapping.get(currentRef).put(ref, XQueryCardinality.ONE);
        }
        currentSequence.add(currentRef);
    }

    @Override
    public Map<String, Map<String, XQueryCardinality>> visitEbnf(ANTLRv4Parser.EbnfContext ctx) {
        if (ctx.blockSuffix() == null)
            visitedCardinality = XQueryCardinality.ONE;
        else
            ctx.blockSuffix().accept(this);
        ctx.block().accept(this);
        blockMergeSubmapping(visitedCardinality);
        return null;
    }

    @Override
    public Map<String, Map<String, XQueryCardinality>> visitLexerAltList(ANTLRv4Parser.LexerAltListContext ctx) {
        ctx.lexerAlt(0).accept(this);
        var previous = currentSubMapping;
        for (var alternative : ctx.lexerAlt().subList(1, ctx.lexerAlt().size())) {
            currentSequence.clear();
            alternative.accept(this);
            alternativeMergeSubmapping(previous, currentSubMapping);
            previous = currentSubMapping;
        }
        return null;
    }

    @Override
    public Map<String, Map<String, XQueryCardinality>> visitAltList(ANTLRv4Parser.AltListContext ctx) {
        ctx.alternative(0).accept(this);
        var previous = currentSubMapping;
        for (var alternative : ctx.alternative().subList(1, ctx.alternative().size())) {
            currentSequence.clear();
            alternative.accept(this);
            alternativeMergeSubmapping(previous, currentSubMapping);
            previous = currentSubMapping;
        }
        return null;
    }

    @Override
    public Map<String, Map<String, XQueryCardinality>> visitLexerAlt(ANTLRv4Parser.LexerAltContext ctx) {
        currentSubMapping = getSubMapping(nodeNames);
        currentSequence.clear();
        return super.visitLexerAlt(ctx);
    }

    @Override
    public Map<String, Map<String, XQueryCardinality>> visitAlternative(ANTLRv4Parser.AlternativeContext ctx) {
        currentSubMapping = getSubMapping(nodeNames);
        currentSequence.clear();
        return super.visitAlternative(ctx);
    }

    @Override
    public Map<String, Map<String, XQueryCardinality>> visitTerminalDef(ANTLRv4Parser.TerminalDefContext ctx) {
        visitedRef = ctx.TOKEN_REF() != null ? ctx.TOKEN_REF().getText() : ctx.STRING_LITERAL().getText();
        return null;
    }

    @Override
    public Map<String, Map<String, XQueryCardinality>> visitRuleref(ANTLRv4Parser.RulerefContext ctx) {
        visitedRef = ctx.RULE_REF().getText();
        return null;
    }

    @Override
    public Map<String, Map<String, XQueryCardinality>> visitNotSet(ANTLRv4Parser.NotSetContext ctx) {
        visitedRef = null;
        return null;
    }

    @Override
    public Map<String, Map<String, XQueryCardinality>> visitWildcard(ANTLRv4Parser.WildcardContext ctx) {
        visitedRef = null;
        return null;
    }

    Set<String> toSet(final Collection<ParseTree> els) {
        return els.stream()
            .map(ParseTree::getText)
            .collect(Collectors.toSet());
    }


    private Map<String, XQueryCardinality> alternativeMergeSubmapping(Map<String, XQueryCardinality> previous, Map<String, XQueryCardinality> currentSubMapping2) {
        for (final var entry : previous.entrySet()) {
            final String ruleName = entry.getKey();
            final XQueryCardinality cardinality = entry.getValue();
            final XQueryCardinality currentCardinality = currentSubMapping.get(ruleName);
            final XQueryCardinality merged = alternativeCardinalityMerger.merge(cardinality, currentCardinality);
            currentSubMapping.put(ruleName, merged);
        }
        return null;
    }

}
