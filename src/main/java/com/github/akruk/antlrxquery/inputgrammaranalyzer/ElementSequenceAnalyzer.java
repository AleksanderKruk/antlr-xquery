
package com.github.akruk.antlrxquery.inputgrammaranalyzer;

import com.github.akruk.antlrgrammar.ANTLRv4ParserBaseVisitor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.github.akruk.antlrgrammar.ANTLRv4Parser.AlternativeContext;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.LexerRuleSpecContext;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.ParserRuleSpecContext;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.RulerefContext;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.TerminalDefContext;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryCardinality;




class ElementSequenceAnalyzer extends ANTLRv4ParserBaseVisitor<Boolean> {
    final Map<String, Map<String, XQueryCardinality>> followingSiblingMapping;
    final Map<String, Map<String, XQueryCardinality>> precedingSiblingMapping;
    final CardinalityAnalyzer analyzer;

    public ElementSequenceAnalyzer(Set<String> nodeNames, CardinalityAnalyzer analyzer)
    {
        this.analyzer = analyzer;
        this.followingSiblingMapping = getMapping(nodeNames);
        this.precedingSiblingMapping = getMapping(nodeNames);
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



    private String currentRule;
    @Override
    public Boolean visitParserRuleSpec(ParserRuleSpecContext ctx) {
        // currentRule = ctx.RULE_REF().getText();
        var visited = super.visitParserRuleSpec(ctx);
        // currentRule = null;
        return visited;
    }

    @Override
    public Boolean visitLexerRuleSpec(LexerRuleSpecContext ctx) {
        if (ctx.FRAGMENT() != null)
            return null;
        currentRule = ctx.TOKEN_REF().getText();
        var visited = super.visitLexerRuleSpec(ctx);
        currentRule = null;
        return visited;
    }

    @Override
    public Boolean visitAlternative(AlternativeContext ctx) {
        final var size = ctx.element().size();
        final var allElementNames = ctx.element().stream()
            .map(e->{ e.accept(this); return visitedName; })
            .filter(s->s != null)
            .toList();
        var currentElementFollowingSiblings = followingSiblingMapping.get(currentRule);
        // var currentElementPrecedingSiblings = precedingSiblingMapping.get(currentRule);
        // analyzer;
        for (int i = 0; i < size; i++) {
            final String element = allElementNames.get(i);
            // final Map<String, XQueryCardinality> followingSiblings = followingSiblingMapping.get(element);
            // final Map<String, XQueryCardinality> precedingSiblings = precedingSiblingMapping.get(element);
            // final List<String> followingSiblingElements = allElementNames.subList(i+1, size);
            // final List<String> precedingSiblingElements = allElementNames.subList(0, i);
            // var currentCardinality = currentElementFollowingSiblings.get(element);
            // var foundCardinality = ;

            currentElementFollowingSiblings.put(element, null);

        }
        return null;
    }

    // private Map<String, XQueryCardinality> getSubMapping(final Set<String> elements) {
    //     final var subhashmap =  new HashMap<String, XQueryCardinality>(elements.size(), 1);
    //     for (final var sub : elements) {
    //         subhashmap.put(sub, XQueryCardinality.ZERO);
    //     }
    //     return subhashmap;
    // }


    String visitedName;
    @Override
    public Boolean visitTerminalDef(TerminalDefContext ctx) {
        if (ctx.TOKEN_REF() != null)
            visitedName = ctx.TOKEN_REF().getText();
        if (ctx.STRING_LITERAL() != null)
            visitedName = ctx.STRING_LITERAL().getText();
        return null;
    }

    @Override
    public Boolean visitRuleref(RulerefContext ctx) {
        visitedName = ctx.RULE_REF().getText();
        return null;
    }

    boolean visitedOptional = false;
    boolean saveOptional() {
        var saved = visitedOptional;
        visitedOptional = false;
        return saved;
    }


}
