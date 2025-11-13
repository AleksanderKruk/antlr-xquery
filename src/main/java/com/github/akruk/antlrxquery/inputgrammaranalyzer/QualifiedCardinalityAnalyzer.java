
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
import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver.QualifiedName;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryCardinality;
import com.github.akruk.antlrxquery.typesystem.typeoperations.occurence.AlternativeCardinalityMerger;
import com.github.akruk.antlrxquery.typesystem.typeoperations.occurence.BlockCardinalityMerger;
import com.github.akruk.antlrxquery.typesystem.typeoperations.occurence.RecursiveCardinalityMerger;
import com.github.akruk.antlrxquery.typesystem.typeoperations.occurence.SequenceCardinalityMerger;



class QualifiedCardinalityAnalyzer
    extends ANTLRv4ParserBaseVisitor<Map<QualifiedName, Map<QualifiedName, XQueryCardinality>>>
{
    final Map<QualifiedName, Map<QualifiedName, XQueryCardinality>> childrenMapping;
    final AlternativeCardinalityMerger alternativeCardinalityMerger;
    final SequenceCardinalityMerger sequenceCardinalityMerger;
    final BlockCardinalityMerger blockCardinalityMerger;
    final RecursiveCardinalityMerger recursiveCardinalityMerger;
    final Set<QualifiedName> nodeNames;

    Map<QualifiedName, Map<QualifiedName, XQueryCardinality>> currentMapping;
    Map<QualifiedName, XQueryCardinality> currentSubMapping;
    final String addedNamespace;

    public QualifiedCardinalityAnalyzer(final Set<QualifiedName> nodeNames, final ANTLRv4Parser antlrParser, String addedNamespace) {
        this.nodeNames = nodeNames;
        this.addedNamespace = addedNamespace;
        this.childrenMapping = getMapping(nodeNames);
        alternativeCardinalityMerger = new AlternativeCardinalityMerger();
        sequenceCardinalityMerger = new SequenceCardinalityMerger();
        blockCardinalityMerger = new BlockCardinalityMerger();
        recursiveCardinalityMerger = new RecursiveCardinalityMerger();
    }


    private Map<QualifiedName, Map<QualifiedName, XQueryCardinality>> getMapping(final Set<QualifiedName> nodeNames) {
        final var map = new HashMap<QualifiedName, Map<QualifiedName, XQueryCardinality>>(nodeNames.size(), 1);
        for (final var nodename : nodeNames) {
            final var subhashmap =  new HashMap<QualifiedName, XQueryCardinality>(nodeNames.size(), 1);
            for (final var sub : nodeNames) {
                subhashmap.put(sub, XQueryCardinality.ZERO);
            }
            map.put(nodename, subhashmap);
        }
        return map;
    }

    private Map<QualifiedName, XQueryCardinality> getSubMapping(final Set<QualifiedName> elements) {
        final var subhashmap =  new HashMap<QualifiedName, XQueryCardinality>(nodeNames.size(), 1);
        for (final var sub : elements) {
            subhashmap.put(sub, XQueryCardinality.ZERO);
        }
        return subhashmap;
    }


    XQueryCardinality visitedCardinality;
    @Override
    public Map<QualifiedName, Map<QualifiedName, XQueryCardinality>> visitEbnfSuffix(final EbnfSuffixContext ctx) {
        if (ctx.STAR() != null)
            visitedCardinality = XQueryCardinality.ZERO_OR_MORE;
        else if (ctx.PLUS() != null)
            visitedCardinality = XQueryCardinality.ONE_OR_MORE;
        else if (ctx.QUESTION().size() == 1)
            visitedCardinality = XQueryCardinality.ZERO_OR_ONE;
        else
            visitedCardinality = XQueryCardinality.ONE;
        return null;
    }


    QualifiedName currentRuleRef;
    @Override
    public Map<QualifiedName, Map<QualifiedName, XQueryCardinality>> visitLexerRuleSpec(LexerRuleSpecContext ctx) {
        currentRuleRef = new QualifiedName(addedNamespace, ctx.TOKEN_REF().getText());
        super.visitLexerRuleSpec(ctx);
        childrenMapping.put(currentRuleRef, currentSubMapping);
        return null;
    }

    @Override
    public Map<QualifiedName, Map<QualifiedName, XQueryCardinality>> visitParserRuleSpec(final ParserRuleSpecContext ctx) {
        currentRuleRef = new QualifiedName("", ctx.RULE_REF().getText());
        super.visitParserRuleSpec(ctx);
        childrenMapping.put(currentRuleRef, currentSubMapping);
        return null;
    }


    @Override
    public Map<QualifiedName, Map<QualifiedName, XQueryCardinality>> visitLexerElement(LexerElementContext ctx) {
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
        // if (visitedRef == currentRu)
        XQueryCardinality current = currentSubMapping.get(visitedRef);
        byte mergedOrdinal = sequenceCardinalityMerger.merge(declaredCardinality.ordinal(), current.ordinal());
        XQueryCardinality merged = XQueryCardinality.values()[mergedOrdinal];
        currentSubMapping.put(visitedRef, merged);
        return null;
    }


    @Override
    public Map<QualifiedName, Map<QualifiedName, XQueryCardinality>> visitElement(final ElementContext ctx) {
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
        if (visitedRef.equals(currentRuleRef)) {
            XQueryCardinality current = currentSubMapping.get(visitedRef);
            // XQueryCardinality blockMerged = sequenceCardinalityMerger.merge(currentSubMapping.get(visitedRef), declaredCardinality);
            XQueryCardinality merged = recursiveCardinalityMerger.merge(current, declaredCardinality);
            currentSubMapping.put(visitedRef, merged);
        } else {
            XQueryCardinality current = currentSubMapping.get(visitedRef);
            byte mergedOrdinal = sequenceCardinalityMerger.merge(declaredCardinality.ordinal(), current.ordinal());
            XQueryCardinality merged = XQueryCardinality.values()[mergedOrdinal];
            currentSubMapping.put(visitedRef, merged);
        }
        return null;
    }


    private void blockMergeSubmapping(XQueryCardinality declaredCardinality) {
        for (final var entry : currentSubMapping.entrySet()) {
            final QualifiedName ruleName = entry.getKey();
            final XQueryCardinality currentCardinality = currentSubMapping.get(ruleName);
            final XQueryCardinality merged = blockCardinalityMerger.merge(declaredCardinality, currentCardinality);
            currentSubMapping.put(ruleName, merged);
        }
    }

    private Map<QualifiedName, XQueryCardinality> alternativeMergeSubmapping(Map<QualifiedName, XQueryCardinality> previous, Map<QualifiedName, XQueryCardinality> currentSubMapping2) {
        for (final var entry : previous.entrySet()) {
            final QualifiedName ruleName = entry.getKey();
            final XQueryCardinality cardinality = entry.getValue();
            final XQueryCardinality currentCardinality = currentSubMapping.get(ruleName);
            final XQueryCardinality merged = alternativeCardinalityMerger.merge(cardinality, currentCardinality);
            currentSubMapping.put(ruleName, merged);
        }
        return null;
    }

    @Override
    public Map<QualifiedName, Map<QualifiedName, XQueryCardinality>> visitEbnf(EbnfContext ctx) {
        if (ctx.blockSuffix() == null)
            visitedCardinality = XQueryCardinality.ONE;
        else
            ctx.blockSuffix().accept(this);

        ctx.block().accept(this);
        blockMergeSubmapping(visitedCardinality);
        return null;
    }


    @Override
    public Map<QualifiedName, Map<QualifiedName, XQueryCardinality>> visitLexerAltList(LexerAltListContext ctx) {
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
    public Map<QualifiedName, Map<QualifiedName, XQueryCardinality>> visitAltList(AltListContext ctx) {
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
    public Map<QualifiedName, Map<QualifiedName, XQueryCardinality>> visitLexerAlt(LexerAltContext ctx) {
        currentSubMapping = getSubMapping(nodeNames);
        return super.visitLexerAlt(ctx);
    }

    @Override
    public Map<QualifiedName, Map<QualifiedName, XQueryCardinality>> visitAlternative(AlternativeContext ctx) {
        currentSubMapping = getSubMapping(nodeNames);
        return super.visitAlternative(ctx);
    }


    QualifiedName visitedRef;

    @Override
    public Map<QualifiedName, Map<QualifiedName, XQueryCardinality>> visitTerminalDef(final TerminalDefContext ctx) {
        if (ctx.TOKEN_REF() != null)
            visitedRef = new QualifiedName(addedNamespace, ctx.TOKEN_REF().getText());
        else
            visitedRef = new QualifiedName(addedNamespace, ctx.STRING_LITERAL().getText());
        return null;
    }


    @Override
    public Map<QualifiedName, Map<QualifiedName, XQueryCardinality>> visitRuleref(final RulerefContext ctx) {
        visitedRef = new QualifiedName(addedNamespace, ctx.RULE_REF().getText());
        return null;
    }

    @Override
    public Map<QualifiedName, Map<QualifiedName, XQueryCardinality>> visitNotSet(final NotSetContext ctx) {
        visitedRef = null;
        return null;
    }

    @Override
    public Map<QualifiedName, Map<QualifiedName, XQueryCardinality>> visitWildcard(final WildcardContext ctx) {
        visitedRef = null;
        return null;
    }





    Set<QualifiedName> toSet(final Collection<ParseTree> els) {
        return els.stream()
            .map(e->new QualifiedName(addedNamespace, e.getText()))
            .collect(Collectors.toSet());
    }



}
