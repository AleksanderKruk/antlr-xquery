package com.github.akruk.antlrxquery.inputgrammaranalyzer;

import com.github.akruk.antlrgrammar.ANTLRv4Parser;
import com.github.akruk.antlrgrammar.ANTLRv4ParserBaseVisitor;
import java.util.*;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryCardinality;
import com.github.akruk.antlrxquery.typesystem.typeoperations.occurence.AlternativeCardinalityMerger;
import com.github.akruk.antlrxquery.typesystem.typeoperations.occurence.BlockCardinalityMerger;
import com.github.akruk.antlrxquery.typesystem.typeoperations.occurence.SequenceCardinalityMerger;

class ExtendedCardinalityAnalyzer extends ANTLRv4ParserBaseVisitor<Map<String, Map<String, XQueryCardinality>>> {
    final Map<String, Map<String, XQueryCardinality>> childrenMapping;
    final Map<String, Map<String, XQueryCardinality>> followingSiblingMapping;
    final Map<String, Map<String, XQueryCardinality>> precedingSiblingMapping;
    final Map<String, Map<String, XQueryCardinality>> followingMapping;
    final Map<String, Map<String, XQueryCardinality>> precedingMapping;
    final AlternativeCardinalityMerger alternativeCardinalityMerger;
    final SequenceCardinalityMerger sequenceCardinalityMerger;
    final BlockCardinalityMerger blockCardinalityMerger;
    final Set<String> nodeNames;
    Map<String, XQueryCardinality> currentSubMapping;
    List<String> currentSequence;
    Deque<List<String>> sequenceStack;

    public ExtendedCardinalityAnalyzer(final Set<String> nodeNames, final ANTLRv4Parser antlrParser) {
        this.nodeNames = nodeNames;
        this.childrenMapping = getMapping(nodeNames);
        this.followingSiblingMapping = getMapping(nodeNames);
        this.precedingSiblingMapping = getMapping(nodeNames);
        this.followingMapping = getMapping(nodeNames);
        this.precedingMapping = getMapping(nodeNames);
        this.antlrParser = antlrParser;
        this.alternativeCardinalityMerger = new AlternativeCardinalityMerger();
        this.sequenceCardinalityMerger = new SequenceCardinalityMerger();
        this.blockCardinalityMerger = new BlockCardinalityMerger();
        this.currentSequence = new ArrayList<>();
        this.sequenceStack = new ArrayDeque<>();
    }

    private Map<String, Map<String, XQueryCardinality>> getMapping(final Set<String> nodeNames) {
        final var map = new HashMap<String, Map<String, XQueryCardinality>>(nodeNames.size(), 1);
        for (final var nodeName : nodeNames) {
            final var subHashMap = getSubMapping(nodeNames);
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
            sequenceStack.push(currentSequence);
            currentSequence = new ArrayList<>();
            ctx.lexerBlock().accept(this);
            blockMergeSubmapping(declaredCardinality);
            currentSequence = sequenceStack.pop();
        } else {
            ctx.lexerAtom().accept(this);
            if (visitedRef != null) {
                updateMappings(visitedRef);
                XQueryCardinality current = currentSubMapping.get(visitedRef);
                XQueryCardinality merged = sequenceCardinalityMerger.merge(declaredCardinality, current);
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
                sequenceStack.push(currentSequence);
                currentSequence = new ArrayList<>();
                ctx.labeledElement().block().accept(this);
                blockMergeSubmapping(declaredCardinality);
                currentSequence = sequenceStack.pop();
            } else {
                ctx.labeledElement().atom().accept(this);
            }
        } else {
            ctx.atom().accept(this);
        }

        if (visitedRef != null) {
            updateMappings(visitedRef);
            XQueryCardinality current = currentSubMapping.get(visitedRef);
            XQueryCardinality merged = sequenceCardinalityMerger.merge(declaredCardinality, current);
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

    private void updateMappings(String currentRef) {
        for (String ref : currentSequence) {
            final Map<String, XQueryCardinality> followingMappingForRef = followingMapping.get(ref);
            final XQueryCardinality currentFollowingCardinality = followingMappingForRef.get(currentRef);
            followingMappingForRef.put(currentRef, sequenceCardinalityMerger.merge(currentFollowingCardinality, XQueryCardinality.ONE));

            final Map<String, XQueryCardinality> precedingMappingForRef = precedingMapping.get(currentRef);
            final XQueryCardinality currentPrecedingCardinality = precedingMappingForRef.get(ref);
            precedingMappingForRef.put(ref, sequenceCardinalityMerger.merge(currentPrecedingCardinality, XQueryCardinality.ONE));
        }
        currentSequence.add(currentRef);

        for (String ref : currentSequence) {
            if (!ref.equals(currentRef)) {
                followingSiblingMapping.get(ref).put(currentRef, sequenceCardinalityMerger.merge(followingSiblingMapping.get(ref).get(currentRef), XQueryCardinality.ONE));
                precedingSiblingMapping.get(currentRef).put(ref, sequenceCardinalityMerger.merge(precedingSiblingMapping.get(currentRef).get(ref), XQueryCardinality.ONE));
            }
        }
    }

    @Override
    public Map<String, Map<String, XQueryCardinality>> visitEbnf(ANTLRv4Parser.EbnfContext ctx) {
        if (ctx.blockSuffix() == null)
            visitedCardinality = XQueryCardinality.ONE;
        else
            ctx.blockSuffix().accept(this);
        sequenceStack.push(currentSequence);
        currentSequence = new ArrayList<>();
        ctx.block().accept(this);
        blockMergeSubmapping(visitedCardinality);
        currentSequence = sequenceStack.pop();
        return null;
    }

    @Override
    public Map<String, Map<String, XQueryCardinality>> visitLexerAltList(ANTLRv4Parser.LexerAltListContext ctx) {
        ctx.lexerAlt(0).accept(this);
        Map<String, XQueryCardinality> previous = currentSubMapping;
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
        Map<String, XQueryCardinality> previous = currentSubMapping;
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

    private void alternativeMergeSubmapping(Map<String, XQueryCardinality> previous, Map<String, XQueryCardinality> current) {
        for (final var entry : previous.entrySet()) {
            final String ruleName = entry.getKey();
            final XQueryCardinality cardinality = entry.getValue();
            final XQueryCardinality currentCardinality = current.get(ruleName);
            final XQueryCardinality merged = alternativeCardinalityMerger.merge(cardinality, currentCardinality);
            current.put(ruleName, merged);
        }
    }

    Set<String> toSet(final Collection<ParseTree> els) {
        return els.stream()
            .map(ParseTree::getText)
            .collect(Collectors.toSet());
    }
}
