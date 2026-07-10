
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
import com.github.akruk.antlrxquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrxquery.typesystem.typeoperations.occurence.AlternativeCardinalityMerger;
import com.github.akruk.antlrxquery.typesystem.typeoperations.occurence.BlockCardinalityMerger;
import com.github.akruk.antlrxquery.typesystem.types.Cardinality;



class CardinalityAnalyzer extends ANTLRv4ParserBaseVisitor<Map<String, Map<String, Cardinality>>> {
    final Map<String, Map<String, Cardinality>> childrenMapping;
    final AlternativeCardinalityMerger alternativeCardinalityMerger;
    final BlockCardinalityMerger blockCardinalityMerger;
    final Set<String> nodeNames;

    Map<String, Map<String, Cardinality>> currentMapping;
    Map<String, Cardinality> currentSubMapping;

    public CardinalityAnalyzer(final Set<String> nodeNames, final ANTLRv4Parser antlrParser) {
        this.nodeNames = nodeNames;
        this.childrenMapping = getMapping(nodeNames);
        alternativeCardinalityMerger = new AlternativeCardinalityMerger();
        blockCardinalityMerger = new BlockCardinalityMerger();
    }


    private Map<String, Map<String, Cardinality>> getMapping(final Set<String> nodeNames) {
        final var map = new HashMap<String, Map<String, Cardinality>>(nodeNames.size(), 1);
        for (final var nodename : nodeNames) {
            final var subhashmap =  new HashMap<String, Cardinality>(nodeNames.size(), 1);
            for (final var sub : nodeNames) {
                subhashmap.put(sub, Cardinality.ZERO);
            }
            map.put(nodename, subhashmap);
        }
        return map;
    }

    private Map<String, Cardinality> getSubMapping(final Set<String> elements) {
        final var subhashmap =  new HashMap<String, Cardinality>(nodeNames.size(), 1);
        for (final var sub : elements) {
            subhashmap.put(sub, Cardinality.ZERO);
        }
        return subhashmap;
    }


    Cardinality visitedCardinality;
    @Override
    public Map<String, Map<String, Cardinality>> visitEbnfSuffix(final EbnfSuffixContext ctx) {
        if (ctx.STAR() != null)
            visitedCardinality = Cardinality.ZERO_OR_MORE;
        else if (ctx.PLUS() != null)
            visitedCardinality = Cardinality.ONE_OR_MORE;
        else if (ctx.QUESTION().size() == 1)
            visitedCardinality = Cardinality.ZERO_OR_ONE;
        else
            visitedCardinality = Cardinality.ONE;
        return null;
    }


    String currentRuleRef;
    @Override
    public Map<String, Map<String, Cardinality>> visitLexerRuleSpec(LexerRuleSpecContext ctx) {
        currentRuleRef = ctx.TOKEN_REF().getText();
        super.visitLexerRuleSpec(ctx);
        childrenMapping.put(currentRuleRef, currentSubMapping);
        return null;
    }

    @Override
    public Map<String, Map<String, Cardinality>> visitParserRuleSpec(final ParserRuleSpecContext ctx) {
        currentRuleRef = ctx.RULE_REF().getText();
        super.visitParserRuleSpec(ctx);
        childrenMapping.put(currentRuleRef, currentSubMapping);
        return null;
    }


    @Override
    public Map<String, Map<String, Cardinality>> visitLexerElement(LexerElementContext ctx) {
        if (ctx.actionBlock() != null)
            return null;
        Cardinality declaredCardinality = null;
        if (ctx.ebnfSuffix() == null) {
            declaredCardinality = Cardinality.ONE;
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
        // if (visitedRef == currentRu)
        Cardinality current = currentSubMapping.get(visitedRef);
        Cardinality merged = Cardinalities.sequenceMerge(declaredCardinality, current);
        currentSubMapping.put(visitedRef, merged);
        return null;
    }


    @Override
    public Map<String, Map<String, Cardinality>> visitElement(final ElementContext ctx) {
        if (ctx.actionBlock() != null)
            return null;
        if (ctx.ebnf() != null)
            return ctx.ebnf().accept(this);
        Cardinality declaredCardinality = null;
        if (ctx.ebnfSuffix() == null) {
            declaredCardinality = Cardinality.ONE;
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
        if (visitedRef.equals(currentRuleRef)) {
            Cardinality current = currentSubMapping.get(visitedRef);
            // Cardinality blockMerged = sequenceCardinalityMerger.merge(currentSubMapping.get(visitedRef), declaredCardinality);
            Cardinality merged = Cardinalities.recursionMerge(declaredCardinality, current);
            currentSubMapping.put(visitedRef, merged);
        } else {
            Cardinality current = currentSubMapping.get(visitedRef);
            Cardinality merged = Cardinalities.sequenceMerge(declaredCardinality, current);
            currentSubMapping.put(visitedRef, merged);
        }
        return null;
    }


    private void blockMergeSubmapping(Cardinality declaredCardinality) {
        for (final var entry : currentSubMapping.entrySet()) {
            final String ruleName = entry.getKey();
            final Cardinality currentCardinality = currentSubMapping.get(ruleName);
            final Cardinality merged = Cardinalities.blockMerge(declaredCardinality, currentCardinality);
            currentSubMapping.put(ruleName, merged);
        }
    }

    private Map<String, Cardinality> alternativeMergeSubmapping(Map<String, Cardinality> previous, Map<String, Cardinality> currentSubMapping2) {
        for (final var entry : previous.entrySet()) {
            final String ruleName = entry.getKey();
            final Cardinality cardinality = entry.getValue();
            final Cardinality currentCardinality = currentSubMapping.get(ruleName);
            final Cardinality merged = Cardinalities.alternativeMerge(cardinality, currentCardinality);
            currentSubMapping.put(ruleName, merged);
        }
        return null;
    }

    @Override
    public Map<java.lang.String, Map<java.lang.String, Cardinality>> visitEbnf(EbnfContext ctx) {
        if (ctx.blockSuffix() == null)
            visitedCardinality = Cardinality.ONE;
        else
            ctx.blockSuffix().accept(this);

        ctx.block().accept(this);
        blockMergeSubmapping(visitedCardinality);
        return null;
    }


    @Override
    public Map<String, Map<String, Cardinality>> visitLexerAltList(LexerAltListContext ctx) {
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
    public Map<java.lang.String, Map<java.lang.String, Cardinality>> visitAltList(AltListContext ctx) {
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
    public Map<String, Map<String, Cardinality>> visitLexerAlt(LexerAltContext ctx) {
        currentSubMapping = getSubMapping(nodeNames);
        return super.visitLexerAlt(ctx);
    }

    @Override
    public Map<java.lang.String, Map<java.lang.String, Cardinality>> visitAlternative(AlternativeContext ctx) {
        currentSubMapping = getSubMapping(nodeNames);
        return super.visitAlternative(ctx);
    }


    String visitedRef;

    @Override
    public Map<String, Map<String, Cardinality>> visitTerminalDef(final TerminalDefContext ctx) {
        if (ctx.TOKEN_REF() != null)
            visitedRef = ctx.TOKEN_REF().getText();
        else
            visitedRef = ctx.STRING_LITERAL().getText();
        return null;
    }


    @Override
    public Map<String, Map<String, Cardinality>> visitRuleref(final RulerefContext ctx) {
        visitedRef = ctx.RULE_REF().getText();
        return null;
    }

    @Override
    public Map<String, Map<String, Cardinality>> visitNotSet(final NotSetContext ctx) {
        visitedRef = null;
        return null;
    }

    @Override
    public Map<String, Map<String, Cardinality>> visitWildcard(final WildcardContext ctx) {
        visitedRef = null;
        return null;
    }





    Set<String> toSet(final Collection<ParseTree> els) {
        return els.stream()
            .map(e->e.getText())
            .collect(Collectors.toSet());
    }



}
