package com.github.akruk.antlrxquery.inputgrammaranalyzer;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.xpath.XPath;
import com.github.akruk.antlrgrammar.ANTLRv4Lexer;
import com.github.akruk.antlrgrammar.ANTLRv4Parser;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.GrammarSpecContext;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.ParserRuleSpecContext;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.TerminalDefContext;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryCardinality;
import com.github.akruk.antlrxquery.typesystem.typeoperations.occurence.BlockCardinalityMerger;
import com.github.akruk.antlrxquery.typesystem.typeoperations.occurence.SequenceCardinalityMerger;

public class InputGrammarAnalyzer {
    public record GrammarAnalysisResult(Map<String, Map<String, XQueryCardinality>> children,
                                        Map<String, Map<String, XQueryCardinality>> descendants,
                                        Map<String, Map<String, XQueryCardinality>> descendantsOrSelf,
                                        // Map<String, Map<String, XQueryCardinality>> following,
                                        // Map<String, Map<String, XQueryCardinality>> followingOrSelf,
                                        // Map<String, Map<String, XQueryCardinality>> followingSibling,
                                        // Map<String, Map<String, XQueryCardinality>> followingSiblingOrSelf,
                                        Map<String, Map<String, XQueryCardinality>> ancestors,
                                        Map<String, Map<String, XQueryCardinality>> ancestorsOrSelf,
                                        Map<String, Map<String, XQueryCardinality>> parent,
                                        // Map<String, Map<String, XQueryCardinality>> preceding,
                                        // Map<String, Map<String, XQueryCardinality>> precedingOrSelf,
                                        // Map<String, Map<String, XQueryCardinality>> precedingSibling,
                                        // Map<String, Map<String, XQueryCardinality>> precedingSiblingOrSelf,
                                        Set<String> simpleTokens,
                                        Set<String> simpleRules)
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
        final var definedNodes_ = XPath.findAll(tree, "//lexerRuleSpec/RULE_REF", antlrParser);
        final var terminalTokens_ = XPath.findAll(tree, "//lexerRuleSpec//TOKEN_REF", antlrParser);
        final var terminalTokenLiterals_ = XPath.findAll(tree, "//lexerRuleSpec//STRING_LITERAL", antlrParser);
        final Set<String> allNodeNames = toSet(definedNodes);
        allNodeNames.addAll(toSet(terminalTokens));
        allNodeNames.addAll(toSet(terminalTokenLiterals));
        allNodeNames.addAll(toSet(definedNodes_));
        allNodeNames.addAll(toSet(terminalTokens_));
        allNodeNames.addAll(toSet(terminalTokenLiterals_));

        final var childrenMapping = getChildrenMapping(antlrParser, tree, allNodeNames);
        final CardinalityAnalyzer cardinalityAnalyzer = new CardinalityAnalyzer(allNodeNames, antlrParser);
        tree.accept(cardinalityAnalyzer);
        final Map<String, Map<String, XQueryCardinality>> parentCardinalityMapping
            = getParentCardinalityMapping(childrenMapping);
        final Map<String, Map<String, XQueryCardinality>> ancestorCardinalityMapping
            = getAncestorCardinalityMapping(parentCardinalityMapping);
        final Map<String, Map<String, XQueryCardinality>> ancestorOrSelfCardinalityMapping
            = addSelf(ancestorCardinalityMapping);
        final var descendantCardinalityMapping = getDescendantCardinalityMapping(cardinalityAnalyzer.childrenMapping);
        final var descendantOrSelfCardinalityMapping = addSelf(descendantCardinalityMapping);
        // final ElementSequenceAnalyzer analyzer = new ElementSequenceAnalyzer(allNodeNames);
        // tree.accept(analyzer);

        // final var followingSiblingMapping = analyzer.followingSiblingMapping;
        // final var followingSiblingOrSelfMapping = addSelf(followingSiblingMapping);
        // final var precedingSiblingMapping = analyzer.precedingSiblingMapping;
        // final var precedingSiblingOrSelfMapping = addSelf(precedingSiblingMapping);
        // final var followingMapping = getFollowing(ancestorOrSelfMapping,
        //         followingSiblingMapping,
        //         descendantOrSelfMapping);
        // final var followingOrSelfMapping = addSelf(followingMapping);
        // final var precedingMapping = getPreceding(ancestorOrSelfMapping,
        //         precedingSiblingMapping,
        //         descendantOrSelfMapping);
        // final var precedingOrSelfMapping = addSelf(precedingMapping);

        final Set<String> simpleTokens = getSimpleTokens(antlrParser, tree);
        final Set<String> simpleRules = getSimpleRules(tree, antlrParser, simpleTokens);

        final var gatheredData = new GrammarAnalysisResult(
                cardinalityAnalyzer.childrenMapping,
                descendantCardinalityMapping,
                descendantOrSelfCardinalityMapping,
                // followingCardinalityMapping,
                // followingOrSelfCardinalityMapping,
                // followingSiblingCardinalityMapping,
                // followingSiblingOrSelfCardinalityMapping,
                ancestorCardinalityMapping,
                ancestorOrSelfCardinalityMapping,
                parentCardinalityMapping,
                // precedingCardinalityMapping,
                // precedingOrSelfCardinalityMapping,
                // precedingSiblingCardinalityMapping,
                // precedingSiblingOrSelfCardinalityMapping,
                simpleTokens,
                simpleRules
        );
        return gatheredData;
    }

    private Map<String, Map<String, XQueryCardinality>> getAncestorCardinalityMapping(final Map<String, Map<String, XQueryCardinality>> parentMapping)
    {
        final Set<String> allNodes = parentMapping.keySet();
        final  Map<String, Map<String, XQueryCardinality>> ancestorMapping = new HashMap<>(parentMapping);
        for (final String node: allNodes) {
            final Map<String, XQueryCardinality> ancestors = ancestorMapping.get(node);
            final Map<String, XQueryCardinality> parents = parentMapping.get(node);
            final Set<String> presentKeys = getPresentKeys(parents);
            final Set<String> nodesToProcess = new HashSet<>(presentKeys);
            while (!nodesToProcess.isEmpty()) {
                final String currentNode = nodesToProcess.stream().findFirst().get();
                final Map<String, XQueryCardinality> currentNodeParents = parentMapping.get(currentNode);
                final var processedParents = getPresentKeys(currentNodeParents);
                for (var parent : processedParents) {
                    XQueryCardinality currentCardinality = ancestors.get(parent);
                    XQueryCardinality other = currentNodeParents.get(parent);
                    byte mergedOrdinal = sequenceCardinalityMerger.merge(currentCardinality.ordinal(), other.ordinal());
                    XQueryCardinality merged = XQueryCardinality.values()[mergedOrdinal];
                    ancestors.put(parent, merged);
                }
                nodesToProcess.addAll(processedParents);
                nodesToProcess.remove(currentNode);
            }
        }
        return ancestorMapping;
    }

    private Set<String> getPresentKeys(final Map<String, XQueryCardinality> x) {
        final Set<String> presentKeys = new HashSet<>(x.keySet());
        presentKeys.removeIf(k->x.get(k) == XQueryCardinality.ZERO);
        return presentKeys;
    }


    Set<String> getSimpleTokens(final ANTLRv4Parser antlrParser, final GrammarSpecContext tree) {
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


    private Map<String, Map<String, XQueryCardinality>> addSelf(final Map<String, Map<String, XQueryCardinality>> mapping) {
        final Map<String, Map<String, XQueryCardinality>> selfMapping = new HashMap<>(mapping.size(), 1);
        for (final var node : mapping.keySet()) {
            final Map<String, XQueryCardinality> mapped = mapping.get(node);
            final Map<String, XQueryCardinality> cloned = new HashMap<>(mapped);
            var currentCardinality = cloned.get(node);
            var merged = sequenceCardinalityMerger.merge(XQueryCardinality.ONE.ordinal(), currentCardinality.ordinal());
            cloned.put(node, XQueryCardinality.values()[merged]);
            selfMapping.put(node, cloned);
        }
        return selfMapping;
    }

    final SequenceCardinalityMerger sequenceCardinalityMerger = new SequenceCardinalityMerger();

    private Map<String, Map<String, XQueryCardinality>>
    getParentCardinalityMapping(final Map<String, Set<String>> childrenMapping)
    {
        final  Map<String, Map<String, XQueryCardinality>> parentMapping = getMapping(childrenMapping.keySet());
        for (String parentName : parentMapping.keySet()) {
            final Set<String> children = childrenMapping.get(parentName);
            for (final var child : children) {
                final var parents = parentMapping.get(child);
                parents.put(parentName, XQueryCardinality.ZERO_OR_ONE);
            }
        }
        return parentMapping;
    }


final BlockCardinalityMerger blockCardinalityMerger = new BlockCardinalityMerger();

private Map<String, Map<String, XQueryCardinality>>
    getDescendantCardinalityMapping(final Map<String, Map<String, XQueryCardinality>> childrenMapping)
{
    final var allNodes = childrenMapping.keySet();
    final Map<String, Map<String, XQueryCardinality>> descendantMapping = new HashMap<>(childrenMapping.size());
    for (var name : allNodes) {
        descendantMapping.put(name, new HashMap<>(childrenMapping.get(name)));
    }

    for (final String node : allNodes) {
        final Map<String, XQueryCardinality> children = childrenMapping.get(node);
        final Map<String, XQueryCardinality> descendants = descendantMapping.get(node);
        final Set<String> nodesToProcess = getPresentKeys(children);

        while (!nodesToProcess.isEmpty()) {
            final String processedNode = nodesToProcess.stream().findFirst().get();
            nodesToProcess.remove(processedNode);

            final Map<String, XQueryCardinality> processedNodeChildren = childrenMapping.get(processedNode);
            final Set<String> presentChildren = getPresentKeys(processedNodeChildren);
            for (final var childName : presentChildren) {
                final XQueryCardinality childCardinality = processedNodeChildren.get(childName);
                final XQueryCardinality parentCardinality = descendants.get(processedNode);
                final XQueryCardinality mergedCardinality = blockCardinalityMerger.merge(parentCardinality, childCardinality);

                final XQueryCardinality existingCardinality = descendants.get(childName);
                final XQueryCardinality updatedCardinality = sequenceCardinalityMerger.merge(existingCardinality, mergedCardinality);
                descendants.put(childName, updatedCardinality);
                nodesToProcess.add(childName);
            }
        }
    }
    return descendantMapping;
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


}
