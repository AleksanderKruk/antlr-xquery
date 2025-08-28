
package com.github.akruk.antlrxquery.inputgrammaranalyzer;

import com.github.akruk.antlrgrammar.ANTLRv4Parser;
import com.github.akruk.antlrgrammar.ANTLRv4ParserBaseVisitor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.tree.ParseTree;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.AltListContext;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.AlternativeContext;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.EbnfContext;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.EbnfSuffixContext;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.ElementContext;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.LexerAltContext;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.LexerAltListContext;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.LexerElementContext;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.LexerRuleSpecContext;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.NotSetContext;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.ParserRuleSpecContext;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.RulerefContext;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.TerminalDefContext;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.WildcardContext;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryCardinality;
import com.github.akruk.antlrxquery.typesystem.typeoperations.occurence.AlternativeCardinalityMerger;
import com.github.akruk.antlrxquery.typesystem.typeoperations.occurence.BlockCardinalityMerger;
import com.github.akruk.antlrxquery.typesystem.typeoperations.occurence.SequenceCardinalityMerger;



class CardinalityAnalyzer extends ANTLRv4ParserBaseVisitor<Map<String, Map<String, XQueryCardinality>>> {
    final Map<String, Map<String, XQueryCardinality>> childrenMapping;
    final AlternativeCardinalityMerger alternativeCardinalityMerger;
    final SequenceCardinalityMerger sequenceCardinalityMerger;
    final BlockCardinalityMerger blockCardinalityMerger;
    final Set<String> nodeNames;

    Map<String, Map<String, XQueryCardinality>> currentMapping;
    Map<String, XQueryCardinality> currentSubMapping;

    public CardinalityAnalyzer(final Set<String> nodeNames, final ANTLRv4Parser antlrParser) {
        this.nodeNames = nodeNames;
        this.childrenMapping = getMapping(nodeNames);
        alternativeCardinalityMerger = new AlternativeCardinalityMerger();
        sequenceCardinalityMerger = new SequenceCardinalityMerger();
        blockCardinalityMerger = new BlockCardinalityMerger();
    }


    private Map<String, Map<String, XQueryCardinality>> getMapping(final Set<String> nodeNames) {
        final var map = new HashMap<String, Map<String, XQueryCardinality>>(nodeNames.size(), 1);
        for (final var nodename : nodeNames) {
            final var subhashmap =  new HashMap<String, XQueryCardinality>(nodeNames.size(), 1);
            for (final var sub : nodeNames) {
                subhashmap.put(sub, XQueryCardinality.ZERO);
            }
            map.put(nodename, subhashmap);
        }
        return map;
    }

    private Map<String, XQueryCardinality> getSubMapping(final Set<String> elements) {
        final var subhashmap =  new HashMap<String, XQueryCardinality>(nodeNames.size(), 1);
        for (final var sub : elements) {
            subhashmap.put(sub, XQueryCardinality.ZERO);
        }
        return subhashmap;
    }


    XQueryCardinality visitedCardinality;
    @Override
    public Map<String, Map<String, XQueryCardinality>> visitEbnfSuffix(final EbnfSuffixContext ctx) {
        if (ctx.STAR() != null)
            visitedCardinality = XQueryCardinality.ZERO_OR_MORE;
        if (ctx.PLUS() != null)
            visitedCardinality = XQueryCardinality.ONE_OR_MORE;
        if (ctx.QUESTION().size() == 1)
            visitedCardinality = XQueryCardinality.ONE_OR_MORE;
        visitedCardinality = XQueryCardinality.ONE;
        return null;
    }


    @Override
    public Map<String, Map<String, XQueryCardinality>> visitLexerRuleSpec(LexerRuleSpecContext ctx) {
        final String currentRuleRef = ctx.TOKEN_REF().getText();
        super.visitLexerRuleSpec(ctx);
        childrenMapping.put(currentRuleRef, currentSubMapping);
        return null;
    }

    @Override
    public Map<String, Map<String, XQueryCardinality>> visitParserRuleSpec(final ParserRuleSpecContext ctx) {
        final String currentRuleRef = ctx.RULE_REF().getText();
        super.visitParserRuleSpec(ctx);
        childrenMapping.put(currentRuleRef, currentSubMapping);
        return null;
    }


    @Override
    public Map<String, Map<String, XQueryCardinality>> visitLexerElement(LexerElementContext ctx) {
        if (ctx.actionBlock() != null)
            return null;
        XQueryCardinality declaredCardinality = null;
        if (ctx.ebnfSuffix() == null) {
            declaredCardinality = XQueryCardinality.ONE;
        } else {
            ctx.ebnfSuffix().accept(this);
            declaredCardinality = visitedCardinality;
        }

        if (ctx.lexerBlock() != null) {
            ctx.lexerBlock().accept(this);
            blockMergeSubmapping(declaredCardinality);
            return null;
        } else {
            ctx.lexerAtom().accept(this);
        }
        // atom case

        // if no ref visited then skipping
        if (visitedRef == null)
            return null;
        XQueryCardinality current = currentSubMapping.get(visitedRef);
        byte mergedOrdinal = sequenceCardinalityMerger.merge(declaredCardinality.ordinal(), current.ordinal());
        XQueryCardinality merged = XQueryCardinality.values()[mergedOrdinal];
        currentSubMapping.put(visitedRef, merged);
        return null;
    }


    @Override
    public Map<String, Map<String, XQueryCardinality>> visitElement(final ElementContext ctx) {
        if (ctx.actionBlock() != null)
            return null;
        if (ctx.ebnf() != null)
            return ctx.ebnf().accept(this);
        XQueryCardinality declaredCardinality = null;
        if (ctx.ebnfSuffix() == null) {
            declaredCardinality = XQueryCardinality.ONE;
        } else {
            ctx.ebnfSuffix().accept(this);
            declaredCardinality = visitedCardinality;
        }

        if (ctx.labeledElement() != null) {
            if (ctx.labeledElement().block() != null) {
                ctx.labeledElement().block().accept(this);
                blockMergeSubmapping(declaredCardinality);
                return null;
            }
            ctx.labeledElement().atom().accept(this);
        } else {
            ctx.atom().accept(this);
        }
        // atom case

        // if no ref visited then skipping
        if (visitedRef == null)
            return null;
        XQueryCardinality current = currentSubMapping.get(visitedRef);
        byte mergedOrdinal = sequenceCardinalityMerger.merge(declaredCardinality.ordinal(), current.ordinal());
        XQueryCardinality merged = XQueryCardinality.values()[mergedOrdinal];
        currentSubMapping.put(visitedRef, merged);
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

    @Override
    public Map<java.lang.String, Map<java.lang.String, XQueryCardinality>> visitEbnf(EbnfContext ctx) {
        if (ctx.blockSuffix() == null)
            visitedCardinality = XQueryCardinality.ONE;
        else
            ctx.blockSuffix().accept(this);

        ctx.block().accept(this);
        blockMergeSubmapping(visitedCardinality);
        return null;
    }


    @Override
    public Map<String, Map<String, XQueryCardinality>> visitLexerAltList(LexerAltListContext ctx) {
        ctx.lexerAlt(0).accept(this);
        var previous = currentSubMapping;
        for (var alternative : ctx.lexerAlt().subList(1, ctx.lexerAlt().size())) {
            alternative.accept(this);
            alternativeMergeSubmapping(previous, currentSubMapping);
            previous = currentSubMapping;
        }
        return null;
    }

    @Override
    public Map<java.lang.String, Map<java.lang.String, XQueryCardinality>> visitAltList(AltListContext ctx) {
        ctx.alternative(0).accept(this);
        var previous = currentSubMapping;
        for (var alternative : ctx.alternative().subList(1, ctx.alternative().size())) {
            alternative.accept(this);
            alternativeMergeSubmapping(previous, currentSubMapping);
            previous = currentSubMapping;
        }
        return null;
    }

    @Override
    public Map<String, Map<String, XQueryCardinality>> visitLexerAlt(LexerAltContext ctx) {
        currentSubMapping = getSubMapping(nodeNames);
        return super.visitLexerAlt(ctx);
    }

    @Override
    public Map<java.lang.String, Map<java.lang.String, XQueryCardinality>> visitAlternative(AlternativeContext ctx) {
        currentSubMapping = getSubMapping(nodeNames);
        return super.visitAlternative(ctx);
    }


    String visitedRef;

    @Override
    public Map<String, Map<String, XQueryCardinality>> visitTerminalDef(final TerminalDefContext ctx) {
        if (ctx.TOKEN_REF() != null)
            visitedRef = ctx.TOKEN_REF().getText();
        else
            visitedRef = ctx.STRING_LITERAL().getText();
        return null;
    }


    @Override
    public Map<String, Map<String, XQueryCardinality>> visitRuleref(final RulerefContext ctx) {
        visitedRef = ctx.RULE_REF().getText();
        return null;
    }

    @Override
    public Map<String, Map<String, XQueryCardinality>> visitNotSet(final NotSetContext ctx) {
        visitedRef = null;
        return null;
    }

    @Override
    public Map<String, Map<String, XQueryCardinality>> visitWildcard(final WildcardContext ctx) {
        visitedRef = null;
        return null;
    }





    Set<String> toSet(final Collection<ParseTree> els) {
        return els.stream()
            .map(e->e.getText())
            .collect(Collectors.toSet());
    }



}
