
package com.github.akruk.antlrxquery.inputgrammaranalyzer;

import com.github.akruk.antlrgrammar.ANTLRv4ParserBaseVisitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.AlternativeContext;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.ParserRuleSpecContext;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.RulerefContext;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.TerminalDefContext;



public class ElementSequenceAnalyzer extends ANTLRv4ParserBaseVisitor<Boolean> {

    final Map<String, Set<String>> followingSiblingMapping;
    final Map<String, Set<String>> precedingSiblingMapping;

    public ElementSequenceAnalyzer(Set<String> nodeNames) {
        followingSiblingMapping = new HashMap<>(nodeNames.size(), 1);
        precedingSiblingMapping = new HashMap<>(nodeNames.size(), 1);
        for (var nodename : nodeNames) {
            followingSiblingMapping.put(nodename, new HashSet<>());
            precedingSiblingMapping.put(nodename, new HashSet<>());
        }
    }

    public Map<String, Set<String>> getFollowingSiblingMapping() {
        return followingSiblingMapping;
    }



    public Map<String, Set<String>> getPrecedingSiblingMapping() {
        return precedingSiblingMapping;
    }


    String currentRule;
    @Override
    public Boolean visitParserRuleSpec(ParserRuleSpecContext ctx) {
        currentRule = ctx.RULE_REF().getText();
        var visited = super.visitParserRuleSpec(ctx);
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
        for (int i = 0; i < size; i++) {
            final String element = allElementNames.get(i);
            final Set<String> followingSiblings = followingSiblingMapping.get(element);
            final Set<String> precedingSiblings = precedingSiblingMapping.get(element);
            final List<String> followingSiblingElements = allElementNames.subList(i+1, size);
            final List<String> precedingSiblingElements = allElementNames.subList(0, i);
            followingSiblings.addAll(followingSiblingElements);
            precedingSiblings.addAll(precedingSiblingElements);
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
