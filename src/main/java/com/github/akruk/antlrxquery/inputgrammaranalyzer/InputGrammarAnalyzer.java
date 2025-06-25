package com.github.akruk.antlrxquery.inputgrammaranalyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
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
import com.github.akruk.antlrgrammar.ANTLRv4Parser.ParserRuleSpecContext;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.TerminalDefContext;

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
                                        Set<String> simpleTokens,
                                        Set<String> simpleRules,
                                        Map<String, List<String>> enumerationTokens)
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
        final Set<String> simpleRules = getSimpleRules(tree, antlrParser, simpleTokens);

        final Map<String, List<String>> enumerationTokens = getEnumTokens(tree, antlrParser, simpleTokens, simpleRules);


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
                simpleTokens,
                simpleRules,
                enumerationTokens);
        return gatheredData;
    }

    private Map<String, List<String>> getEnumTokens(GrammarSpecContext tree,
                                                ANTLRv4Parser antlrParser,
                                                Set<String> simpleTokens)
    {

        final Map<String, List<String>> enumTokens = new HashMap<>();
        final Collection<ParseTree> lexerRules = XPath.findAll(tree, "//lexerRuleSpec", antlrParser);

        for (ParseTree rule : lexerRules) {
            final ANTLRv4Parser.LexerRuleSpecContext ruleSpec = (ANTLRv4Parser.LexerRuleSpecContext) rule;

            // Pomijanie reguł fragmentów
            if (ruleSpec.FRAGMENT() != null) continue;

            final TerminalNode tokenRef = ruleSpec.TOKEN_REF();
            if (tokenRef == null) continue;

            final String tokenName = tokenRef.getText();
            final Collection<ParseTree> alternatives = XPath.findAll(ruleSpec, "//lexerAlt", antlrParser);

            final List<String> literalValues = new ArrayList<>();
            for (ParseTree alt : alternatives) {
                final var elements = XPath.findAll(alt, ".//lexerElement", antlrParser);

                // Sprawdzanie czy alternatywa jest pojedynczym literałem lub prostym tokenem
                if (elements.size() == 1) {
                    final ParseTree element = elements.iterator().next();

                    // Bezpośredni literał łańcuchowy
                    final var stringLiterals = XPath.findAll(element, "STRING_LITERAL", antlrParser);
                    if (!stringLiterals.isEmpty()) {
                        literalValues.add(stringLiterals.iterator().next().getText());
                    }
                    // Referencja do prostego tokenu
                    else {
                        final var tokenRefs = XPath.findAll(element, "TOKEN_REF", antlrParser);
                        if (!tokenRefs.isEmpty() && simpleTokens.contains(tokenRefs.get(0).getText())) {
                            literalValues.add(tokenRefs.iterator().next().getText());
                        }
                    }
                }
            }

            // Warunek: co najmniej 2 unikalne reprezentacje
            if (literalValues.size() >= 2) {
                enumTokens.put(tokenName, literalValues);
            }
        }

        return enumTokens;
    }



    Set<String> getSimpleTokens(final ANTLRv4Parser antlrParser, final GrammarSpecContext tree)
    {
        final var lexerRules = XPath.findAll(tree, "//lexerRuleSpec", antlrParser);
        final Predicate<ParseTree> isFragment = rule -> {
            final var ruleSpec = (ANTLRv4Parser.LexerRuleSpecContext) rule;
            return (ruleSpec.FRAGMENT() != null);
        };
        final var partitionedByIsFragment = lexerRules.stream().collect(Collectors.partitioningBy(isFragment));
        final var fragmentRules = partitionedByIsFragment.get(true);
        final var normalRules = partitionedByIsFragment.get(false);
        final Set<ParseTree> previousSimpleFragments = fragmentRules.stream()
            .filter(rule -> isSimpleLexerRule(antlrParser, rule))
            .collect(Collectors.toSet());
        final Set<ParseTree> previousSimpleRules = normalRules.stream()
            .filter(rule -> isSimpleLexerRule(antlrParser, rule))
            .collect(Collectors.toSet());
        final Set<ParseTree> remainingFragments = new HashSet<>(fragmentRules);
        final Set<ParseTree> remainingRules = new HashSet<>(normalRules);
        remainingRules.removeAll(previousSimpleRules);
        remainingFragments.removeAll(previousSimpleFragments);
        int previousSimpleRuleCount = 0;
        int currentSimpleRuleCount = 0;
        do {
            previousSimpleRuleCount = previousSimpleFragments.size() + previousSimpleRules.size();
            final Set<String> previousFragmentRuleNames = previousSimpleFragments.stream().map(this::getLexerRuleName).collect(Collectors.toSet());
            final Set<String> previousSimpleRuleNames = previousSimpleRules.stream().map(this::getLexerRuleName).collect(Collectors.toSet());
            final Set<ParseTree> simpleRecursiveFragmentLexerRules = remainingFragments.stream()
                .filter(rule-> isSimpleFragmentedLexerRule(antlrParser, rule, previousSimpleRuleNames, previousFragmentRuleNames))
                .collect(Collectors.toSet());
            remainingFragments.removeAll(simpleRecursiveFragmentLexerRules);
            previousSimpleFragments.addAll(simpleRecursiveFragmentLexerRules);

            final Set<String> simpleRecursiveFragmentLexerRuleNames = simpleRecursiveFragmentLexerRules.stream()
                .map(this::getLexerRuleName)
                .collect(Collectors.toSet());

            final var simpleRecursiveLexerRules = remainingRules.stream()
                .filter(rule-> isSimpleFragmentedLexerRule(antlrParser, rule, previousFragmentRuleNames, simpleRecursiveFragmentLexerRuleNames ))
                .collect(Collectors.toSet());
            remainingRules.removeAll(simpleRecursiveLexerRules);
            previousSimpleRules.addAll(simpleRecursiveLexerRules);

            currentSimpleRuleCount = previousSimpleFragments.size() + previousSimpleRules.size();
        } while (previousSimpleRuleCount != currentSimpleRuleCount);
        return previousSimpleRules.stream().map(this::getLexerRuleName).collect(Collectors.toSet());

    }




    public Set<String> getSimpleRules(GrammarSpecContext tree, ANTLRv4Parser antlrParser, Set<String> simpleTokens) {
        // Step 1: Collect all parser rules
        Collection<ParseTree> allParserRules = XPath.findAll(tree, "//parserRuleSpec", antlrParser);

        // Track simple rules and pending rules
        Set<String> simpleRules = new HashSet<>(allParserRules.size());
        Set<ParseTree> pendingRules = new HashSet<>(allParserRules);

        int previousCount;
        do {
            previousCount = simpleRules.size();
            Iterator<ParseTree> iterator = pendingRules.iterator();

            while (iterator.hasNext()) {
                ParseTree rule = iterator.next();
                ANTLRv4Parser.ParserRuleSpecContext ruleCtx = (ANTLRv4Parser.ParserRuleSpecContext) rule;
                String ruleName = ruleCtx.RULE_REF().getText();

                // Check if rule meets simplicity criteria
                if (isSimpleRule(ruleCtx, simpleTokens, simpleRules)) {
                    simpleRules.add(ruleName);
                    iterator.remove();
                }
            }
        } while (simpleRules.size() > previousCount); // Iterate until no new additions

        return simpleRules;
    }

    private boolean isSimpleRule(final ANTLRv4Parser.ParserRuleSpecContext rule,
                                 final Set<String> simpleTokens,
                                 final Set<String> simpleRules)
    {
        // Check for single alternative
        ANTLRv4Parser.RuleAltListContext altList = rule.ruleBlock().ruleAltList();
        for (final var alt : altList.labeledAlt()) {
            if (alt.POUND() != null) {
                final var id = alt.identifier().getText();
                if (isSimpleAlternative(alt.alternative(), simpleTokens, simpleRules)) {
                    simpleRules.add(id);
                }
            }
        }

        if (altList.labeledAlt().size() != 1) return false;

        ANTLRv4Parser.LabeledAltContext alt = altList.labeledAlt(0);
        return isSimpleAlternative(alt.alternative(), simpleTokens, simpleRules);
    }

    private boolean isSimpleAlternative(ANTLRv4Parser.AlternativeContext alt,
                                    Set<String> simpleTokens,
                                    Set<String> simpleRules) {
        for (ANTLRv4Parser.ElementContext element : alt.element()) {
            if (!isSimpleElement(element, simpleTokens, simpleRules)) {
                return false;
            }
        }
        return true;
    }

    private boolean isSimpleElement(ANTLRv4Parser.ElementContext element,
                                Set<String> simpleTokens,
                                Set<String> simpleRules) {
        // Reject elements with modifiers/quantifiers
        if (element.ebnfSuffix() != null) return false;

        // Handle labeled elements (e.g., k='c')
        if (element.labeledElement() != null) {
            ANTLRv4Parser.LabeledElementContext labeled = element.labeledElement();
            if (labeled.atom() != null)
                return isSimpleAtom(labeled.atom(), simpleTokens, simpleRules);
            return isSimpleBlock(labeled.block(), simpleTokens, simpleRules);
        }
        // Handle atomic elements
        else if (element.atom() != null) {
            return isSimpleAtom(element.atom(), simpleTokens, simpleRules);
        }
        // Handle parenthesized blocks
        else if (element.ebnf() != null && element.ebnf().block() != null) {
            return isSimpleBlock(element.ebnf().block(), simpleTokens, simpleRules);
        }
        return false;
    }

    private boolean isSimpleBlock(ANTLRv4Parser.BlockContext block,
                                Set<String> simpleTokens,
                                Set<String> simpleRules) {
        // Must contain exactly one alternative
        if (block.altList().alternative().size() != 1) return false;
        return isSimpleAlternative(block.altList().alternative(0), simpleTokens, simpleRules);
    }

    private boolean isSimpleAtom(ANTLRv4Parser.AtomContext atom,
                                Set<String> simpleTokens,
                                Set<String> simpleRules) {
        // Terminal tokens (strings or token references)
        if (atom.terminalDef() != null) {
            TerminalDefContext term = atom.terminalDef();
            if (term.TOKEN_REF() != null) {
                return simpleTokens.contains(term.TOKEN_REF().getText());
            }
            return true; // String literals are always simple
        }
        // Rule references
        else if (atom.ruleref() != null) {
            return simpleRules.contains(atom.ruleref().RULE_REF().getText());
        }
        return false;
    }

    private String getLexerRuleName(final ParseTree rule) {
        final var ruleSpec = (ANTLRv4Parser.LexerRuleSpecContext) rule;
        final var ruleName = ruleSpec.TOKEN_REF().getText();
        return ruleName;
    }

    private boolean isSimpleLexerRule(final ANTLRv4Parser antlrParser, final ParseTree rule) {
        final var ruleSpec = (ANTLRv4Parser.LexerRuleSpecContext) rule;
        final var ruleBlock = ruleSpec.lexerRuleBlock();
        if (ruleBlock.children.size() != 1)
            return false;
        final var allAlts = XPath.findAll(ruleBlock, "//lexerAlt", antlrParser);
        if (allAlts.size() != 1)
            return false;
        final var notSets = XPath.findAll(ruleBlock, "//notSet", antlrParser);
        if (!notSets.isEmpty())
            return false;
        final var charRange = XPath.findAll(ruleBlock, "//characterRange", antlrParser);
        if (!charRange.isEmpty())
            return false;
        final var wildcard = XPath.findAll(ruleBlock, "//wildcard", antlrParser);
        if (!wildcard.isEmpty())
            return false;
        final var allAtoms = XPath.findAll(ruleBlock, "//lexerAtom", antlrParser);
        final var allLiterals = XPath.findAll(ruleBlock, "//STRING_LITERAL", antlrParser);
        if (allAtoms.size() != allLiterals.size())
            return false;
        final var suffixes = XPath.findAll(ruleBlock, "//ebnfSuffix", antlrParser);
        return suffixes.size() == 0;
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
        final var allAlts = XPath.findAll(ruleBlock, "//lexerAlt", antlrParser);
        if (allAlts.size() != 1)
            return false;
        final var notSets = XPath.findAll(ruleBlock, "//notSet", antlrParser);
        if (!notSets.isEmpty())
            return false;
        final var charRange = XPath.findAll(ruleBlock, "//characterRange", antlrParser);
        if (!charRange.isEmpty())
            return false;
        final var wildcard = XPath.findAll(ruleBlock, "//wildcard", antlrParser);
        if (!wildcard.isEmpty())
            return false;
        final var refs = XPath.findAll(ruleSpec.lexerRuleBlock(), "//TOKEN_REF", antlrParser);
        final var allSimpleRefs = refs.stream().allMatch(ref -> {
            var name = ref.getText();
            return simpleRules.contains(name) || simpleFragments.contains(name);
        });
        if (!allSimpleRefs)
            return false;
        final var allAtoms = XPath.findAll(ruleBlock, "//lexerAtom", antlrParser);
        final var allLiterals = XPath.findAll(ruleBlock, "//STRING_LITERAL", antlrParser);
        if (allAtoms.size() != (allLiterals.size() + refs.size()))
            return false;
        final var suffixes = XPath.findAll(ruleBlock, "//ebnfSuffix", antlrParser);
        return suffixes.size() == 0;
    }


    private Map<String, Set<String>> addSelf(final Map<String, Set<String>> mapping) {
        final Map<String, Set<String>> selfMapping = new HashMap<>(mapping.size(), 1);
        for (final var node : mapping.keySet()) {
            final var mapped = mapping.get(node);
            final var cloned = new HashSet<>(mapped);
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
        for (final var nodename : allNodeNames) {
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
        for (final var node : ancestorOrSelfMapping.keySet()) {
            followingMapping.put(node, new HashSet<>());
        }
        for (final var node  : followingMapping.keySet()) {
            final var result = followingMapping.get(node);
            final var ancestors = ancestorOrSelfMapping.get(node);
            for (final var ancestor: ancestors) {
                final var followingSibling = followingSiblingMapping.get(ancestor);
                for (final var fs: followingSibling) {
                    final var descendantOrSelfs = descendantsOrSelfMapping.get(fs);
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
        for (final var node : ancestorOrSelfMapping.keySet()) {
            precedingMapping.put(node, new HashSet<>());
        }
        for (final var node  : precedingMapping.keySet()) {
            final var result = precedingMapping.get(node);
            final var ancestors = ancestorOrSelfMapping.get(node);
            for (final var ancestor: ancestors) {
                final var precedingSibling = precedingSiblingMapping.get(ancestor);
                for (final var ps: precedingSibling) {
                    final var descendantOrSelfs = descendantsOrSelfMapping.get(ps);
                    result.addAll(descendantOrSelfs);
                }
            }
        }
        return precedingMapping;
    }
}
