
package com.github.akruk.antlrxquery.inputgrammaranalyzer;

import com.github.akruk.antlrgrammar.ANTLRv4ParserBaseVisitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.AlternativeContext;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.ParserRuleSpecContext;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.RulerefContext;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.TerminalDefContext;



public class ElementSequenceAnalyzer extends ANTLRv4ParserBaseVisitor<Boolean> {
    final Map<String, Set<String>> followingSiblingMapping = new HashMap<>();
    final Map<String, Set<String>> precedingSiblingMapping = new HashMap<>();

    ElementSequenceAnalyzer()
    { }


    String currentRule;
    @Override
    public Boolean visitParserRuleSpec(ParserRuleSpecContext ctx) {
        currentRule = ctx.RULE_REF().getText();
        followingSiblingMapping.put(currentRule, new HashSet<>());
        precedingSiblingMapping.put(currentRule, new HashSet<>());
        var visited = super.visitParserRuleSpec(ctx);
        currentRule = null;
        return visited;
    }

    @Override
    public Boolean visitAlternative(AlternativeContext ctx) {
        final var size = ctx.element().size();
        final var end = size - 1;
        final var allElementNames = ctx.element().stream()
            .map(e->{ e.accept(this); return visitedName; })
            .filter(s->s != null)
            .toList();
        for (int i = 0; i < end; i++) {
            final var element = allElementNames.get(i);
            final var followingSiblings = followingSiblingMapping
                .computeIfAbsent(element, (_) -> new HashSet<String>());
            final var precedingSiblings = followingSiblingMapping
                .computeIfAbsent(element, (_) -> new HashSet<String>());
            followingSiblings.addAll(allElementNames.subList(i+1, size));
            precedingSiblings.addAll(allElementNames.subList(0, i));
        }
        return null;
    }



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
