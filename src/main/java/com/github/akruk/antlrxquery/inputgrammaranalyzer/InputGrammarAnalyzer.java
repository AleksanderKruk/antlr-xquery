package com.github.akruk.antlrxquery.inputgrammaranalyzer;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.xpath.XPath;
import com.github.akruk.antlrgrammar.ANTLRv4Lexer;
import com.github.akruk.antlrgrammar.ANTLRv4Parser;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.GrammarSpecContext;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.LexerRuleSpecContext;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.ParserRuleSpecContext;

public class InputGrammarAnalyzer {
    public record GrammarAnalysisResult(Map<String, Set<String>> children,
                                        Map<String, Set<String>> descendants,
                                        Map<String, Set<String>> descendantsOrSelf,
                                        Map<String, Set<String>> following,
                                        Map<String, Set<String>> followingOrSelf,
                                        Map<String, Set<String>> followingSibling,
                                        Map<String, Set<String>> followingSiblingOrSelf,
                                        Map<String, Set<String>> ancestors,
                                        Map<String, Set<String>> ancestorsOrSelf,
                                        Map<String, Set<String>> parent,
                                        Map<String, Set<String>> preceding,
                                        Map<String, Set<String>> precedingOrSelf,
                                        Map<String, Set<String>> precedingSibling,
                                        Map<String, Set<String>> precedingSiblingOrSelf,
                                        Set<String> simpleTokens)
    {}

    Set<String> toSet(final Collection<ParseTree> els) {
        return els.stream()
            .map(e->e.getText())
            .collect(Collectors.toSet());
    }

    public GrammarAnalysisResult analyze(final CharStream characterStream) {
        // + "child"
        // + "descendant"
        // + "descendant-or-self"
        // + "following"
        // + "following-or-self"
        // + "following-sibling"
        // + "following-sibling-or-self"
        // + "self"
        // + "ancestor"
        // + "ancestor-or-self"
        // + "parent"
        // + "preceding"
        // + "preceding-or-self"
        // + "preceding-sibling"
        // + "preceding-sibling-or-self"

        final Lexer xqueryLexer = new ANTLRv4Lexer(characterStream);
        final CommonTokenStream xqueryTokens = new CommonTokenStream(xqueryLexer);
        final ANTLRv4Parser antlrParser = new ANTLRv4Parser(xqueryTokens);
        final var tree = antlrParser.grammarSpec();
        final var definedNodes = XPath.findAll(tree, "//parserRuleSpec/RULE_REF", antlrParser);
        final var terminalTokens = XPath.findAll(tree, "//parserRuleSpec//TOKEN_REF", antlrParser);
        final var terminalTokenLiterals = XPath.findAll(tree, "//parserRuleSpec//STRING_LITERAL", antlrParser);
        final Set<String> allNodeNames = toSet(definedNodes);
        allNodeNames.addAll(toSet(terminalTokens));
        allNodeNames.addAll(toSet(terminalTokenLiterals));

        final var childrenMapping = getChildrenMapping(antlrParser, tree, allNodeNames);
        final var parentMapping = getParentMapping(antlrParser, childrenMapping, tree);
        final var ancestorMapping = getAncestorMapping(parentMapping);
        final var ancestorOrSelfMapping = addSelf(ancestorMapping);
        final var descendantMapping = getDescendantMapping(childrenMapping);
        final var descendantOrSelfMapping = addSelf(descendantMapping);
        final ElementSequenceAnalyzer analyzer = new ElementSequenceAnalyzer(allNodeNames);
        tree.accept(analyzer);

        final var followingSiblingMapping = analyzer.getFollowingSiblingMapping();
        final var followingSiblingOrSelfMapping = addSelf(followingSiblingMapping);
        final var precedingSiblingMapping = analyzer.getPrecedingSiblingMapping();
        final var precedingSiblingOrSelfMapping = addSelf(precedingSiblingMapping);
        final var followingMapping = getFollowing(ancestorOrSelfMapping,
                followingSiblingMapping,
                descendantOrSelfMapping);
        final var followingOrSelfMapping = addSelf(followingMapping);
        final var precedingMapping = getPreceding(ancestorOrSelfMapping,
                precedingSiblingMapping,
                descendantOrSelfMapping);
        final var precedingOrSelfMapping = addSelf(precedingMapping);

        final Set<String> simpleTokens = getSimpleTokens(antlrParser, tree);

        final var gatheredData = new GrammarAnalysisResult(childrenMapping,
                descendantMapping,
                descendantOrSelfMapping,
                followingMapping,
                followingOrSelfMapping,
                followingSiblingMapping,
                followingSiblingOrSelfMapping,
                ancestorMapping,
                ancestorOrSelfMapping,
                parentMapping,
                precedingMapping,
                precedingOrSelfMapping,
                precedingSiblingMapping,
                precedingSiblingOrSelfMapping,
                simpleTokens);
        return gatheredData;
    }


    Set<String> getSimpleTokens(final ANTLRv4Parser antlrParser, final GrammarSpecContext tree) {
        // final var matcher = antlrParser.compileParseTreePattern("<STRING_LITERAL>", ANTLRv4Parser.RULE_ruleBlock);
        // final var lexerMatcher = antlrParser.compileParseTreePattern("<STRING_LITERAL>", ANTLRv4Parser.RULE_lexerRuleBlock);
        final var lexerRules = XPath.findAll(tree, "//lexerRuleSpec", antlrParser);
        final Predicate<ParseTree> isFragment = rule -> {
            final var ruleSpec = (ANTLRv4Parser.LexerRuleSpecContext) rule;
            return (ruleSpec.FRAGMENT() != null);
        };
        final var partitionedByIsFragment = lexerRules.stream().collect(Collectors.partitioningBy(isFragment));
        final var simpleFragments = partitionedByIsFragment.get(true).stream()
            .filter(rule -> isSimpleLexerRule(antlrParser, rule))
            .map(this::getLexerRuleName)
            .collect(Collectors.toSet());

        final var simpleLexerRules = partitionedByIsFragment.get(false).stream()
            .filter(rule-> isSimpleLexerRule(antlrParser, rule))
            .map(this::getLexerRuleName)
            .collect(Collectors.toSet());

        final var simpleRecursiveLexerRules = partitionedByIsFragment.get(false).stream()
            .filter(rule-> isSimpleFragmentedLexerRule(antlrParser, rule, simpleLexerRules, simpleFragments))
            .map(this::getLexerRuleName)
            .collect(Collectors.toSet());


        final var parserRules = XPath.findAll(tree, "//parserRuleSpec", antlrParser);
        final var simpleParserRules = parserRules.stream()
            .filter(rule-> {
                final var ruleSpec = (ANTLRv4Parser.ParserRuleSpecContext) rule;
                final var ruleBlock = ruleSpec.ruleBlock();
                final var allAtoms = XPath.findAll(ruleBlock, "//atom", antlrParser);
                final var allLiterals = XPath.findAll(ruleBlock, "//STRING_LITERAL", antlrParser);
                final var elementOptions = XPath.findAll(ruleBlock, "//elementOptions", antlrParser);
                return ruleBlock.children.size() == 1
                    && elementOptions.size() == 0
                    && allAtoms.size() == allLiterals.size();
                }).map(rule -> {
                    final var ruleSpec = (ANTLRv4Parser.ParserRuleSpecContext) rule;
                    var ruleName = ruleSpec.RULE_REF().getText();
                    return ruleName;
                }).collect(Collectors.toSet());
        simpleLexerRules.addAll(simpleParserRules);
        return simpleLexerRules;
    }

    private String getLexerRuleName(ParseTree rule) {
        final var ruleSpec = (ANTLRv4Parser.LexerRuleSpecContext) rule;
        var ruleName = ruleSpec.TOKEN_REF().getText();
        return ruleName;
    }

    private boolean isSimpleLexerRule(final ANTLRv4Parser antlrParser, final ParseTree rule) {
        final var ruleSpec = (ANTLRv4Parser.LexerRuleSpecContext) rule;
        final var ruleBlock = ruleSpec.lexerRuleBlock();
        if (ruleBlock.children.size() != 1)
            return false;
        final var allAtoms = XPath.findAll(ruleBlock, "//lexerAtom", antlrParser);
        final var allLiterals = XPath.findAll(ruleBlock, "//STRING_LITERAL", antlrParser);
        if (allAtoms.size() != allLiterals.size())
            return false;
        final var elementOptions = XPath.findAll(ruleBlock, "//elementOptions", antlrParser);
        return elementOptions.size() == 0;
    }

    private boolean isSimpleFragmentedLexerRule(final ANTLRv4Parser antlrParser,
                                                final ParseTree rule,
                                                final Set<String> simpleRules,
                                                final Set<String> simpleFragments)
    {
        final var ruleSpec = (ANTLRv4Parser.LexerRuleSpecContext) rule;
        final var ruleBlock = ruleSpec.lexerRuleBlock();
        if (ruleBlock.children.size() != 1)
            return false;
        final var allAtoms = XPath.findAll(ruleBlock, "//lexerAtom", antlrParser);
        final var allLiterals = XPath.findAll(ruleBlock, "//STRING_LITERAL", antlrParser);
        final var allRefs = XPath.findAll(ruleBlock, "//STRING_LITERAL", antlrParser);
        if (allAtoms.size() != allLiterals.size())
            return false;
        final var elementOptions = XPath.findAll(ruleBlock, "//elementOptions", antlrParser);
        return elementOptions.size() == 0;
    }


    private Map<String, Set<String>> addSelf(Map<String, Set<String>> mapping) {
        final Map<String, Set<String>> selfMapping = new HashMap<>(mapping.size(), 1);
        for (var node : mapping.keySet()) {
            var mapped = mapping.get(node);
            var cloned = new HashSet<>(mapped);
            cloned.add(node);
            selfMapping.put(node, cloned);
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
                ancestors.addAll(processedParents);
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
        final  Map<String, Set<String>> ancestorMapping = new HashMap<>(childrenMapping.size());
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
                descendants.addAll(processedParents);
                nodesToProcess.remove(processedNode);
            }
            ancestorMapping.put(node, descendants);
        }
        return ancestorMapping;
    }


    private Map<String, Set<String>> getChildrenMapping(final ANTLRv4Parser antlrParser,
                                                        final GrammarSpecContext tree,
                                                        final Set<String> allNodeNames)
    {
        final  Map<String, Set<String>> childrenMapping = new HashMap<>(allNodeNames.size(), 1);
        for (var nodename : allNodeNames) {
            childrenMapping.put(nodename, new HashSet<>());
        }

        final var ruleSpecs = XPath.findAll(tree, "//parserRuleSpec", antlrParser);
        for (final ParseTree spec :ruleSpecs) {
            final ParserRuleSpecContext spec_ = (ParserRuleSpecContext) spec;
            final String ruleRef = spec_.RULE_REF().getText();

            final Set<String> children = new HashSet<>();
            final var rulerefs = XPath.findAll(spec, "/parserRuleSpec//ruleref", antlrParser);
            final var terminalTokens = XPath.findAll(spec, "/parserRuleSpec//TOKEN_REF", antlrParser);
            final var terminalTokenLiterals = XPath.findAll(spec, "/parserRuleSpec//STRING_LITERAL", antlrParser);

            children.addAll(toSet(rulerefs));
            children.addAll(toSet(terminalTokens));
            children.addAll(toSet(terminalTokenLiterals));
            allNodeNames.addAll(children);
            childrenMapping.put(ruleRef, children);
        }
        return childrenMapping;
    }


    private Map<String, Set<String>> getFollowing(final Map<String, Set<String>> ancestorOrSelfMapping,
                                                    final Map<String, Set<String>> followingSiblingMapping,
                                                    final Map<String, Set<String>> descendantsOrSelfMapping)
    {
        final  Map<String, Set<String>> followingMapping = new HashMap<>(ancestorOrSelfMapping.size());
        for (var node : ancestorOrSelfMapping.keySet()) {
            followingMapping.put(node, new HashSet<>());
        }
        for (var node  : followingMapping.keySet()) {
            var result = followingMapping.get(node);
            var ancestors = ancestorOrSelfMapping.get(node);
            for (var ancestor: ancestors) {
                var followingSibling = followingSiblingMapping.get(ancestor);
                for (var fs: followingSibling) {
                    var descendantOrSelfs = descendantsOrSelfMapping.get(fs);
                    result.addAll(descendantOrSelfs);
                }
            }
        }
        return followingMapping;
    }

    private Map<String, Set<String>> getPreceding(final Map<String, Set<String>> ancestorOrSelfMapping,
                                                    final Map<String, Set<String>> precedingSiblingMapping,
                                                    final Map<String, Set<String>> descendantsOrSelfMapping)
    {
        final  Map<String, Set<String>> precedingMapping = new HashMap<>(ancestorOrSelfMapping.size(), 1);
        for (var node : ancestorOrSelfMapping.keySet()) {
            precedingMapping.put(node, new HashSet<>());
        }
        for (var node  : precedingMapping.keySet()) {
            var result = precedingMapping.get(node);
            var ancestors = ancestorOrSelfMapping.get(node);
            for (var ancestor: ancestors) {
                var precedingSibling = precedingSiblingMapping.get(ancestor);
                for (var ps: precedingSibling) {
                    var descendantOrSelfs = descendantsOrSelfMapping.get(ps);
                    result.addAll(descendantOrSelfs);
                }
            }
        }
        return precedingMapping;
    }
}
