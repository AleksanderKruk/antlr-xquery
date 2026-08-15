package com.github.akruk.antlrquery.inputgrammaranalyzer;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.xpath.XPath;
import com.github.akruk.antlrgrammar.ANTLRv4Parser;
import com.github.akruk.antlrgrammar.ANTLRv4Parser.TerminalDefContext;
import com.github.akruk.antlrquery.AntlrQueryAxis;
import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver.QualifiedName;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;

public class InputGrammarAnalyzer {

    public record QualifiedGrammarAnalysisResult(
        /* element <=> rule | token */
        Set<String> grammarNames,
        Set<QualifiedName> elementNames,
        /* axis -> ruleName -> relativeRuleName -> cardinality */
        EnumMap<AntlrQueryAxis, Map<QualifiedName, Map<QualifiedName, Cardinality>>> axes,
        Set<QualifiedName> simpleTokens,
        Set<QualifiedName> simpleRules
    ) {}

    Set<QualifiedName> toQualifiedSet(final Collection<ParseTree> els, String addedNamespace)
    {
        return els.stream()
            .map(e -> new QualifiedName(addedNamespace, e.getText()))
            .collect(Collectors.toSet());
    }

    Set<String> toSet(final Collection<ParseTree> els)
    {
        return els.stream()
            .map(ParseTree::getText)
            .collect(Collectors.toSet());
    }


    public QualifiedGrammarAnalysisResult analyze(String addedNamespace, final List<ParseTree> trees)
    {
        final var antlrParser = new ANTLRv4Parser(null);
        final Set<QualifiedName> allNodeNames = new HashSet<>(trees.size()*100);
        final Set<ParseTree> allLexerRules = new HashSet<>(trees.size()*100);
        final Set<ParseTree> allParserRules = new HashSet<>(trees.size()*100);
        final Set<String> grammarNames = new HashSet<>(trees.size());
        for (ParseTree tree : trees) {
            final Collection<ParseTree> name = XPath.findAll(tree, "//grammarSpec/grammarDecl/identifier", antlrParser);
            final Collection<ParseTree> definedNodes = XPath.findAll(tree, "//parserRuleSpec/RULE_REF", antlrParser);
            final Collection<ParseTree> terminalTokens = XPath.findAll(tree, "//parserRuleSpec//TOKEN_REF", antlrParser);
            final Collection<ParseTree> terminalTokenLiterals = XPath.findAll(tree, "//parserRuleSpec//STRING_LITERAL", antlrParser);
            final Collection<ParseTree> definedNodes_ = XPath.findAll(tree, "//lexerRuleSpec/RULE_REF", antlrParser);
            final Collection<ParseTree> terminalTokens_ = XPath.findAll(tree, "//lexerRuleSpec//TOKEN_REF", antlrParser);
            final Collection<ParseTree> terminalTokenLiterals_ = XPath.findAll(tree, "//lexerRuleSpec//STRING_LITERAL", antlrParser);
            name.stream().findFirst().ifPresent((n)->grammarNames.add(n.getText()));
            allNodeNames.addAll(toQualifiedSet(definedNodes, addedNamespace));
            allNodeNames.addAll(toQualifiedSet(terminalTokens, addedNamespace));
            allNodeNames.addAll(toQualifiedSet(terminalTokenLiterals, addedNamespace));
            allNodeNames.addAll(toQualifiedSet(definedNodes_, addedNamespace));
            allNodeNames.addAll(toQualifiedSet(terminalTokens_, addedNamespace));
            allNodeNames.addAll(toQualifiedSet(terminalTokenLiterals_, addedNamespace));

            final var lexerRules = XPath.findAll(tree, "//lexerRuleSpec", antlrParser);
            final var parserRules = XPath.findAll(tree, "//parserRuleSpec", antlrParser);
            allLexerRules.addAll(lexerRules);
            allParserRules.addAll(parserRules);
        }

        final QualifiedCardinalityAnalyzer childrenAnalyzer = new QualifiedCardinalityAnalyzer(allNodeNames, addedNamespace);
        for (var tree : trees) {
            tree.accept(childrenAnalyzer);
        }
        final Map<QualifiedName, Map<QualifiedName, Cardinality>> childrenMapping
            = childrenAnalyzer.childrenMapping;

        RuleGraph graph = new RuleGraph(new LinkedHashMap<>());

        for (QualifiedName node : childrenMapping.keySet()) {
            final Map<QualifiedName, Cardinality> children = childrenMapping.get(node);
            for (var child : children.keySet()) {
                graph.addRule(node, child, children.get(child));
            }
        }
        final DescendantCardinalityAnalyzer descendantAnalyzer
                = new DescendantCardinalityAnalyzer(graph, childrenMapping.keySet());

        final var descendants = descendantAnalyzer.analyzeAll();
        final var parents = getQualifiedParentCardinalityMapping(allNodeNames, childrenMapping);
        final Map<QualifiedName, Map<QualifiedName, Cardinality>> zeroOrMoreMapping
            = getQualifiedMapping(allNodeNames, Cardinality.ZERO_OR_MORE);

        final AncestorCardinalityAnalyzer ancestorAnalyzer
                = new AncestorCardinalityAnalyzer(graph, childrenMapping.keySet());
        final Map<QualifiedName, Map<QualifiedName, Cardinality>> ancestors = ancestorAnalyzer.analyzeAll();
        final Map<QualifiedName, Map<QualifiedName, Cardinality>> ancestorsOrSelf = addSelf(ancestors);

        final var simpleTokens = getSimpleTokens(antlrParser, allLexerRules);
        final var simpleRules = getSimpleRules(allParserRules, simpleTokens);
        final Set<QualifiedName> qualifiedSimpleTokens = simpleTokens.stream()
            .map(t->new QualifiedName(addedNamespace, t))
            .collect(Collectors.toSet());
        final Set<QualifiedName> qualifiedSimpleRules = simpleRules.stream()
            .map(t->new QualifiedName(addedNamespace, t))
            .collect(Collectors.toSet());


        EnumMap<AntlrQueryAxis, Map<QualifiedName, Map<QualifiedName, Cardinality>>> axes = new EnumMap<>(AntlrQueryAxis.class);
        axes.put(AntlrQueryAxis.CHILD, childrenMapping);
        axes.put(AntlrQueryAxis.DESCENDANT, descendants);
        axes.put(AntlrQueryAxis.DESCENDANT_OR_SELF, addSelf(descendants));
        axes.put(AntlrQueryAxis.PARENT, parents);
        axes.put(AntlrQueryAxis.ANCESTOR, ancestors);
        axes.put(AntlrQueryAxis.ANCESTOR_OR_SELF, ancestorsOrSelf);
        axes.put(AntlrQueryAxis.FOLLOWING, zeroOrMoreMapping);
        axes.put(AntlrQueryAxis.FOLLOWING_OR_SELF, zeroOrMoreMapping);
        axes.put(AntlrQueryAxis.FOLLOWING_SIBLING, zeroOrMoreMapping);
        axes.put(AntlrQueryAxis.FOLLOWING_SIBLING_OR_SELF, zeroOrMoreMapping);
        axes.put(AntlrQueryAxis.PRECEDING, zeroOrMoreMapping);
        axes.put(AntlrQueryAxis.PRECEDING_OR_SELF, zeroOrMoreMapping);
        axes.put(AntlrQueryAxis.PRECEDING_SIBLING, zeroOrMoreMapping);
        axes.put(AntlrQueryAxis.PRECEDING_SIBLING_OR_SELF, zeroOrMoreMapping);
        axes.put(AntlrQueryAxis.SELF, getSelfMapping(allNodeNames));
        for (var axis : AntlrQueryAxis.values()) {
            assert axes.containsKey(axis);
        }

        return new QualifiedGrammarAnalysisResult(
            grammarNames,
            childrenMapping.keySet(),
            axes,
            qualifiedSimpleTokens,
            qualifiedSimpleRules
        );
    }

    private Set<QualifiedName> getPresentKeys(final Map<QualifiedName, Cardinality> x) {
         final Set<QualifiedName> presentKeys = new HashSet<>(x.keySet());
         presentKeys.removeIf(k->x.get(k).equals(Cardinality.ZERO));
         return presentKeys;
     }

    Set<String> getSimpleTokens(final ANTLRv4Parser antlrParser, Collection<ParseTree> lexerRules)
    {
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
            final Set<String> previousFragmentRuleNames = previousSimpleFragments.stream().map(this::getLexerRuleName)
                .collect(Collectors.toSet());
            final Set<String> previousSimpleRuleNames = previousSimpleRules.stream().map(this::getLexerRuleName)
                .collect(Collectors.toSet());
            final Set<ParseTree> simpleRecursiveFragmentLexerRules = remainingFragments.stream()
                .filter(rule -> isSimpleFragmentedLexerRule(antlrParser, rule, previousSimpleRuleNames,
                    previousFragmentRuleNames))
                .collect(Collectors.toSet());
            remainingFragments.removeAll(simpleRecursiveFragmentLexerRules);
            previousSimpleFragments.addAll(simpleRecursiveFragmentLexerRules);

            final Set<String> simpleRecursiveFragmentLexerRuleNames = simpleRecursiveFragmentLexerRules.stream()
                .map(this::getLexerRuleName)
                .collect(Collectors.toSet());

            final var simpleRecursiveLexerRules = remainingRules.stream()
                .filter(rule -> isSimpleFragmentedLexerRule(antlrParser, rule, previousFragmentRuleNames,
                    simpleRecursiveFragmentLexerRuleNames))
                .collect(Collectors.toSet());
            remainingRules.removeAll(simpleRecursiveLexerRules);
            previousSimpleRules.addAll(simpleRecursiveLexerRules);

            currentSimpleRuleCount = previousSimpleFragments.size() + previousSimpleRules.size();
        } while (previousSimpleRuleCount != currentSimpleRuleCount);
        return previousSimpleRules.stream().map(this::getLexerRuleName).collect(Collectors.toSet());
    }

    public Set<String> getSimpleRules(
            Collection<ParseTree> allParserRules,
            Set<String> simpleTokens)
    {
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

        if (altList.labeledAlt().size() != 1)
            return false;

        ANTLRv4Parser.LabeledAltContext alt = altList.labeledAlt(0);
        return isSimpleAlternative(alt.alternative(), simpleTokens, simpleRules);
    }

    private boolean isSimpleAlternative(ANTLRv4Parser.AlternativeContext alt,
        Set<String> simpleTokens,
        Set<String> simpleRules)
    {
        for (ANTLRv4Parser.ElementContext element : alt.element()) {
            if (!isSimpleElement(element, simpleTokens, simpleRules)) {
                return false;
            }
        }
        return true;
    }

    private boolean isSimpleElement(ANTLRv4Parser.ElementContext element,
        Set<String> simpleTokens,
        Set<String> simpleRules)
    {
        // Reject elements with modifiers/quantifiers
        if (element.ebnfSuffix() != null)
            return false;

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
        Set<String> simpleRules)
    {
        // Must contain exactly one alternative
        if (block.altList().alternative().size() != 1)
            return false;
        return isSimpleAlternative(block.altList().alternative(0), simpleTokens, simpleRules);
    }

    private boolean isSimpleAtom(ANTLRv4Parser.AtomContext atom,
        Set<String> simpleTokens,
        Set<String> simpleRules)
    {
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

    private String getLexerRuleName(final ParseTree rule)
    {
        final var ruleSpec = (ANTLRv4Parser.LexerRuleSpecContext) rule;
        return ruleSpec.TOKEN_REF().getText();
    }

    private boolean isSimpleLexerRule(final ANTLRv4Parser antlrParser, final ParseTree rule)
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
        final var allAtoms = XPath.findAll(ruleBlock, "//lexerAtom", antlrParser);
        final var allLiterals = XPath.findAll(ruleBlock, "//STRING_LITERAL", antlrParser);
        if (allAtoms.size() != allLiterals.size())
            return false;
        final var suffixes = XPath.findAll(ruleBlock, "//ebnfSuffix", antlrParser);
        return suffixes.isEmpty();
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
        return suffixes.isEmpty();
    }

    private Map<QualifiedName, Map<QualifiedName, Cardinality>>
        addSelf(final Map<QualifiedName, Map<QualifiedName, Cardinality>> mapping)
    {
        final Map<QualifiedName, Map<QualifiedName, Cardinality>> selfMapping =
            new HashMap<>(mapping.size(), 1);
        for (final var node : mapping.keySet()) {
            final Map<QualifiedName, Cardinality> mapped = mapping.get(node);
            final Map<QualifiedName, Cardinality> cloned = new HashMap<>(mapped);
            var currentCardinality = cloned.get(node);
            var merged = Cardinalities.add(Cardinality.ONE, currentCardinality);
            cloned.put(node, merged);
            selfMapping.put(node, cloned);
        }
        return selfMapping;
    }

    private Map<QualifiedName, Map<QualifiedName, Cardinality>>
        getSelfMapping(final Set<QualifiedName> nodeNames)
    {
        final Map<QualifiedName, Map<QualifiedName, Cardinality>> selfMapping
            = getQualifiedMapping(nodeNames);
        for (final QualifiedName nodeName : nodeNames) {
            selfMapping.get(nodeName).put(nodeName, Cardinality.ONE);
        }
        return selfMapping;
    }



    private
    Map<QualifiedName, Map<QualifiedName, Cardinality>>
        getQualifiedParentCardinalityMapping(
            final Set<QualifiedName> allNodeNames,
            final Map<QualifiedName, Map<QualifiedName, Cardinality>> childrenMapping
        )
    {
        final Map<QualifiedName, Map<QualifiedName, Cardinality>> parentMapping = getQualifiedMapping(allNodeNames);
        for (QualifiedName parentName : parentMapping.keySet()) {
            final Set<QualifiedName> children = childrenMapping.get(parentName).keySet();
            for (final var child : children) {
                final var parentsForGivenChild = parentMapping.get(child);
                final Cardinality c = childrenMapping.get(parentName).get(child);
                if (!c.equals(Cardinality.ZERO)) {
                    parentsForGivenChild.put(parentName, Cardinality.ZERO_OR_ONE );
                }
            }
        }
        return parentMapping;
    }


    // private Map<String, Set<String>> getFollowing(final Map<String, Set<String>>
    // ancestorOrSelfMapping,
    // final Map<String, Set<String>> followingSiblingMapping,
    // final Map<String, Set<String>> descendantsOrSelfMapping)
    // {
    // final Map<String, Set<String>> followingMapping = new
    // HashMap<>(ancestorOrSelfMapping.size());
    // for (final var node : ancestorOrSelfMapping.keySet()) {
    // followingMapping.put(node, new HashSet<>());
    // }
    // for (final var node : followingMapping.keySet()) {
    // final var result = followingMapping.get(node);
    // final var ancestors = ancestorOrSelfMapping.get(node);
    // for (final var ancestor: ancestors) {
    // final var followingSibling = followingSiblingMapping.get(ancestor);
    // for (final var fs: followingSibling) {
    // final var descendantOrSelfs = descendantsOrSelfMapping.get(fs);
    // result.addAll(descendantOrSelfs);
    // }
    // }
    // }
    // return followingMapping;
    // }

    // private Map<String, Set<String>> getPreceding(final Map<String, Set<String>>
    // ancestorOrSelfMapping,
    // final Map<String, Set<String>> precedingSiblingMapping,
    // final Map<String, Set<String>> descendantsOrSelfMapping)
    // {
    // final Map<String, Set<String>> precedingMapping = new
    // HashMap<>(ancestorOrSelfMapping.size(), 1);
    // for (final var node : ancestorOrSelfMapping.keySet()) {
    // precedingMapping.put(node, new HashSet<>());
    // }
    // for (final var node : precedingMapping.keySet()) {
    // final var result = precedingMapping.get(node);
    // final var ancestors = ancestorOrSelfMapping.get(node);
    // for (final var ancestor: ancestors) {
    // final var precedingSibling = precedingSiblingMapping.get(ancestor);
    // for (final var ps: precedingSibling) {
    // final var descendantOrSelfs = descendantsOrSelfMapping.get(ps);
    // result.addAll(descendantOrSelfs);
    // }
    // }
    // }
    // return precedingMapping;
    // }

    private Map<String, Map<String, Cardinality>> getMapping(final Set<String> nodeNames)
    {
        final var map = new HashMap<String, Map<String, Cardinality>>(nodeNames.size(), 1);
        for (final var nodename : nodeNames) {
            final var subhashmap = new HashMap<String, Cardinality>(nodeNames.size(), 1);
            for (final var sub : nodeNames) {
                subhashmap.put(sub, Cardinality.ZERO);
            }
            map.put(nodename, subhashmap);
        }
        return map;
    }

    private Map<QualifiedName, Map<QualifiedName, Cardinality>>
        getQualifiedMapping(
            final Set<QualifiedName> nodeNames
        )
    {
        return getQualifiedMapping(nodeNames, Cardinality.ZERO);
    }

    private Map<QualifiedName, Map<QualifiedName, Cardinality>>
        getQualifiedMapping(
            final Set<QualifiedName> nodeNames,
            final Cardinality defaultCardinality
        )
    {
        final var map = new HashMap<QualifiedName, Map<QualifiedName, Cardinality>>(nodeNames.size(), 1);
        for (final var nodename : nodeNames) {
            final var subhashmap = new HashMap<QualifiedName, Cardinality>(nodeNames.size(), 1);
            for (final var sub : nodeNames) {
                subhashmap.put(sub, defaultCardinality);
            }
            map.put(nodename, subhashmap);
        }
        return map;
    }


}
