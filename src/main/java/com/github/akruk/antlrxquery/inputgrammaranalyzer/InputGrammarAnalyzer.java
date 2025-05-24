package com.github.akruk.antlrxquery.inputgrammaranalyzer;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.xpath.XPath;

import com.github.akruk.antlrgrammar.ANTLRv4Lexer;
import com.github.akruk.antlrgrammar.ANTLRv4Parser;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.AlternativeContext;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.EbnfContext;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.EbnfSuffixContext;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.GrammarSpecContext;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.ParserRuleSpecContext;

public class InputGrammarAnalyzer {
    record NodeData(Set<String> children,
                    Set<String> descendants,
                    Set<String> descendantsOrSelf,
                    Set<String> following,
                    Set<String> followingOrSelf,
                    Set<String> followingSibling,
                    Set<String> followingSiblingOrSelf,
                    Set<String> ancestors,
                    Set<String> ancestorsOrSelf,
                    Set<String> parent,
                    Set<String> preceding,
                    Set<String> precedingOrSelf,
                    Set<String> precedingSibling,
                    Set<String> precedingSiblingOrSelf) {}
    record GrammarAnalysisResult(Map<String, NodeData> nodeInfo) {}

    Set<String> toSet(final Collection<ParseTree> els) {
        return els.stream()
            .map(e->e.getText())
            .collect(Collectors.toSet());
    }

    public GrammarAnalysisResult analyze(final CharStream characterStream) {
        // + "child"
        // + "descendant"
        // + "descendant-or-self"
        // "following"
        // "following-or-self"
        // "following-sibling"
        // "following-sibling-or-self"
        // + "self"
        // + "ancestor"
        // + "ancestor-or-self"
        // + "parent"
        // "preceding"
        // "preceding-or-self"
        // "preceding-sibling"
        // "preceding-sibling-or-self"


        final Lexer xqueryLexer = new ANTLRv4Lexer(characterStream);
        final CommonTokenStream xqueryTokens = new CommonTokenStream(xqueryLexer);
        final ANTLRv4Parser antlrParser = new ANTLRv4Parser(xqueryTokens);
        final var tree = antlrParser.grammarSpec();
        final var definedNodes = XPath.findAll(tree, "//parserRuleSpec/RULE_REF", antlrParser);
        final Set<String> allNodeNames = toSet(definedNodes);

        final var childrenMapping = getChildrenMapping(antlrParser, tree, definedNodes, allNodeNames);
        final var parentMapping = getParentMapping(antlrParser, childrenMapping, tree);
        final var ancestorMapping = getAncestorMapping(parentMapping);
        final var ancestorOrSelfMapping = addSelf(ancestorMapping);
        final var descendantMapping = getDescendantMapping(childrenMapping);
        final var descendantOrSelfMapping = addSelf(descendantMapping);




        // final var parentMapping = getChildrenMapping(antlrParser, tree, definedNodes, allNodeNames);
        // final var precedingSiblingMapping = getPrecedingSiblingMapping(antlrParser, tree, definedNodes, allNodeNames);

            // final NodeData gatheredData = new NodeData(children, null, null, null, null, null, null, null, null, null,)
            // childrenMapping.put(ruleRef, gatheredData);

        return null;
    }



    private Map<String, Set<String>> addSelf(Map<String, Set<String>> mapping) {
        Map<String, Set<String>> selfMapping = new HashMap<>(mapping);
        for (var node : mapping.keySet()) {
            selfMapping.get(node).add(node);
        }
        return selfMapping;
    }

    private Map<String, Set<String>> getParentMapping(final ANTLRv4Parser antlrParser,
                                                        final Map<String, Set<String>> childrenMapping,
                                                        final GrammarSpecContext tree)
    {
        final var allNodes = childrenMapping.keySet();
        final  Map<String, Set<String>> parentMapping = new HashMap<>(allNodes.size());
        for (final var node: allNodes) {
            parentMapping.put(node, new HashSet<>());
        }
        final var ruleSpecs = XPath.findAll(tree, "//parserRuleSpec", antlrParser);
        for (final ParseTree spec :ruleSpecs) {
            final ParserRuleSpecContext spec_ = (ParserRuleSpecContext) spec;
            final String ruleRef = spec_.RULE_REF().getText();
            final Set<String> children = childrenMapping.get(ruleRef);
            for (final var child : children) {
                parentMapping.get(child).add(ruleRef);
            }
        }
        return parentMapping;
    }

    private Map<String, Set<String>> getAncestorMapping(final Map<String, Set<String>> parentMapping)
    {
        final var allNodes = parentMapping.keySet();
        final  Map<String, Set<String>> ancestorMapping = new HashMap<>(allNodes.size(), 1);
        for (final var node: allNodes) {
            final Set<String> parents = parentMapping.get(node);

            final Set<String> ancestors = new HashSet<>(parentMapping.size());
            ancestors.addAll(parents);
            final Set<String> nodesToProcess = new HashSet<>(parents);
            while (!nodesToProcess.isEmpty()) {
                final String processedNode = nodesToProcess.stream().findFirst().get();
                final var processedParents = new HashSet<>(parentMapping.get(processedNode));
                processedParents.removeAll(ancestors);
                nodesToProcess.addAll(processedParents);
                nodesToProcess.remove(processedNode);
            }
            ancestorMapping.put(node, ancestors);
        }
        return ancestorMapping;
    }


    private Map<String, Set<String>> getDescendantMapping(final Map<String, Set<String>> childrenMapping)
    {
        final var allNodes = childrenMapping.keySet();
        final  Map<String, Set<String>> ancestorMapping = new HashMap<>(childrenMapping.size(), 1);
        for (final var node: allNodes) {
            final Set<String> children = childrenMapping.get(node);

            final Set<String> descendants = new HashSet<>(childrenMapping.size());
            descendants.addAll(children);
            final Set<String> nodesToProcess = new HashSet<>(children);
            while (!nodesToProcess.isEmpty()) {
                final String processedNode = nodesToProcess.stream().findFirst().get();
                final var processedParents = new HashSet<>(childrenMapping.get(processedNode));
                processedParents.removeAll(descendants);
                nodesToProcess.addAll(processedParents);
                nodesToProcess.remove(processedNode);
            }
            ancestorMapping.put(node, descendants);
        }
        return ancestorMapping;
    }


    private Map<String, Set<String>> getChildrenMapping(final ANTLRv4Parser antlrParser,
                                                        final GrammarSpecContext tree,
                                                        final Collection<ParseTree> definedNodes,
                                                        final Set<String> allNodeNames)
    {
        final  Map<String, Set<String>> childrenMapping = new HashMap<>(definedNodes.size()*2);

        final var ruleSpecs = XPath.findAll(tree, "//parserRuleSpec", antlrParser);
        for (final ParseTree spec :ruleSpecs) {
            final ParserRuleSpecContext spec_ = (ParserRuleSpecContext) spec;
            final String ruleRef = spec_.RULE_REF().getText();

            final Set<String> children = new HashSet<>();
            final var rulerefs = XPath.findAll(spec, "//ruleref", antlrParser);
            final var terminalTokens = XPath.findAll(spec, "//TOKEN_REF", antlrParser);
            final var terminalTokenLiterals = XPath.findAll(spec, "//STRING_LITERAL", antlrParser);

            children.addAll(toSet(rulerefs));
            children.addAll(toSet(terminalTokens));
            children.addAll(toSet(terminalTokenLiterals));
            allNodeNames.addAll(children);
            childrenMapping.put(ruleRef, children);
        }
        return childrenMapping;
    }


    private Map<String, Set<String>> getFollowingSibling(final ANTLRv4Parser antlrParser,
                                                        final GrammarSpecContext tree,
                                                        final Set<String> allNodeNames)
    {
        final  Map<String, Set<String>> childrenMapping = new HashMap<>(allNodeNames.size());
        for (var node : childrenMapping.keySet()) {
            childrenMapping.put(node, new HashSet<>());
        }

        final var ruleSpecs = XPath.findAll(tree, "//parserRuleSpec", antlrParser);
        for (final ParseTree spec :ruleSpecs) {
            final ParserRuleSpecContext spec_ = (ParserRuleSpecContext) spec;
            final String ruleRef = spec_.RULE_REF().getText();
            // TODO: take labeled alts into account
            final var alternatives = XPath.findAll(spec_, "//alternative", antlrParser);
            for (var alternative_ : alternatives) {
                var alternative = (AlternativeContext) alternative_;
                for (var element: alternative.element()) {
                    for (var child: element.children) {
                        if (child instanceof EbnfContext) { }
                        if (child instanceof EbnfSuffixContext) { }
                        if (child instanceof EbnfSuffixContext) { }

                    }


                }

            }

            // final Set<String> children = new HashSet<>();
            final var rulerefs = XPath.findAll(spec, "//ruleref", antlrParser);
            final var terminalTokens = XPath.findAll(spec, "//TOKEN_REF", antlrParser);
            final var terminalTokenLiterals = XPath.findAll(spec, "//STRING_LITERAL", antlrParser);

        }
        return childrenMapping;
    }

}
