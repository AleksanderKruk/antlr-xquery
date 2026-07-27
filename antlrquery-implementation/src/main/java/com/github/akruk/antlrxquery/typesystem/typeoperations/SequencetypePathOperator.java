package com.github.akruk.antlrxquery.typesystem.typeoperations;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.akruk.antlrxquery.typesystem.types.itemtypes.*;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.github.akruk.antlrxquery.XQueryAxis;
import com.github.akruk.antlrxquery.inputgrammaranalyzer.InputGrammarAnalyzer.QualifiedGrammarAnalysisResult;
import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver;
import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver.QualifiedName;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.XQuerySemanticSymbolManager;
import com.github.akruk.antlrxquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrxquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.types.Cardinality;


public class SequencetypePathOperator {
    private final AntlrQueryTypeFactory typeFactory;
	private final XQuerySemanticSymbolManager symbolManager;
    private final AntlrQuerySequenceType zeroOrMoreNodes;
    
    public SequencetypePathOperator(
        final AntlrQueryTypeFactory typeFactory,
        final XQuerySemanticSymbolManager symbolManager
        )
    {
        this.typeFactory = typeFactory;
		this.symbolManager = symbolManager;
        this.zeroOrMoreNodes = typeFactory.zeroOrMore(typeFactory.itemAnyNode());
    }

    public enum GrammarStatus {
        REGISTERED,
        UNREGISTERED,
        UNCHECKED
    }

    public enum InputStatus {
        OK,
        NON_NODES,
        EMPTY_SEQUENCE,
        MULTIGRAMMAR
    }

    public record PathOperatorResult(
        InputStatus inputStatus,
        AntlrQuerySequenceType result,
        Map<String, GrammarStatus> inputGrammars,
        Map<String, GrammarStatus> elementGrammars,
        Set<QualifiedName> invalidElementNames,
        Set<QualifiedName> duplicatedNames,
        Set<QualifiedName> impossibleToReachNames
        )
        {}



    /**
     * Performs semantic analysis of path operator.
     * <code>{type}/{axis}::(*|{elementNames})</code>
     * @param type
     * left hand side type targeted by operator
     * @param axis
     * operator axis
     * @param axisElementNames
     * names of the elements or null if wildcard is used
     * @param namespaceResolver
     * namespace resolver used to resolve qualified element names
     * @return {@link PathOperatorResult}
     */
    public PathOperatorResult pathOperator(
        final @NonNull AntlrQuerySequenceType type,
        final @NonNull XQueryAxis axis,
        final @Nullable List<String> axisElementNames,
        final @NonNull NamespaceResolver namespaceResolver
        )
    {
        final ValidateNamesResult validatedNames = resolveAndValidateNames(axisElementNames != null? axisElementNames: List.of(), namespaceResolver);

        if (!Types.isSubtype(typeFactory, type, zeroOrMoreNodes)) {
            @MonotonicNonNull AntlrQuerySequenceType returnedType;
            if (validatedNames.grammars.size() == 1) {
                String grammar = validatedNames.grammars.keySet().stream().findFirst().get();
                 returnedType = typeFactory.zeroOrMore(typeFactory.itemElement(grammar, validatedNames.validNames));
            } else {
                returnedType = typeFactory.zeroOrMore(typeFactory.itemAnyNode());
            }

            return new PathOperatorResult(
                    InputStatus.NON_NODES,
                    returnedType,
                    Map.of(),
                    validatedNames.grammars,
                    validatedNames.invalidNames,
                    validatedNames.duplicatedNames,
                    Set.of()
            );
        }
        if (type.itemType() instanceof final ChoiceItemType c) {
            return new PathOperatorResult(
                    InputStatus.MULTIGRAMMAR,
                    typeFactory.zeroOrMore(typeFactory.itemElement("", validatedNames.validNames)),
                    getGrammars(c),
                    validatedNames.grammars,
                    validatedNames.invalidNames,
                    validatedNames.duplicatedNames,
                    Set.of()
            );
        }
        if (!(type.itemType() instanceof final TreeLike nodeType)) {
            throw new IllegalStateException("Checks above should have prevented types other than TreeNodeType");
        }
        final GrammarAndElement grammarAndElementNames = getGrammarAndElementNames(typeFactory, nodeType);
        final String inputGrammar = grammarAndElementNames.grammar;
        final Map<String, GrammarStatus> inputGrammars = Map.of(inputGrammar, getGrammarStatus(inputGrammar));
        final Set<QualifiedName> elementNames = grammarAndElementNames.elementNames;

        final boolean usesWildcard = axisElementNames == null;
        final boolean isSelf = axis == XQueryAxis.SELF;

        final ItemTypeResult resultingItemType = getResultingItemType(type, inputGrammar, elementNames, validatedNames, usesWildcard, isSelf);
        final CardinalityResult resultingCardinality = getResultingCardinality(axis, type, usesWildcard);
        final AntlrQuerySequenceType resultingType = typeFactory.sequence(resultingItemType.itemType, resultingCardinality.cardinality);

        final QualifiedGrammarAnalysisResult analysis = symbolManager.getGrammar(inputGrammar);
        if (analysis == null) {
            return new PathOperatorResult(
                    InputStatus.OK, resultingType, inputGrammars,
                    validatedNames.grammars, validatedNames.invalidNames, validatedNames.duplicatedNames,
                    resultingItemType.unreachableNames);
        } else {
            final AnalyzedAxisResult analysisResult = usesWildcard
                ? analyzeAxisWithWildcard(elementNames, axis, analysis)
                : analyzeAxisPathElements(elementNames, axis, validatedNames, analysis);
            final AntlrQuerySequenceType analyzedType = getAnalyzedReturnedType(analysisResult, inputGrammar, elementNames, type.cardinality());

            return new PathOperatorResult(
                InputStatus.OK,
                analyzedType,
                inputGrammars,
                validatedNames.grammars,
                validatedNames.invalidNames,
                validatedNames.duplicatedNames,
                analysisResult.impossibleNames
            );
        }

    }

    private Map<String, GrammarStatus> getGrammars(ChoiceItemType c) {
        final Map<String, GrammarStatus> result = new HashMap<>(c.itemTypes().length);
        for (var it : c.itemTypes()) {
            if (!(it instanceof final TreeLike tn)) {
                continue;
            }
            var grammarAndElement = getGrammarAndElementNames(typeFactory, tn);
            if (grammarAndElement.grammar != null && grammarAndElement.elementNames != null)
                result.put(grammarAndElement.grammar, getGrammarStatus(grammarAndElement.grammar));
        }
        return result;
    }


    record GrammarAndElement(@Nullable String grammar, @Nullable Set<QualifiedName> elementNames ){}

    private static GrammarAndElement getGrammarAndElementNames(AntlrQueryTypeFactory typeFactory, TreeLike type) {
        return switch(type) {
            case TreeNodeType.NodeType(String grammar_, Set<QualifiedName> elementNames_) ->
                new GrammarAndElement(grammar_, elementNames_);
            case TreeRuleType.RuleType(String grammar_, Set<QualifiedName> elementNames_) ->
                new GrammarAndElement(grammar_, elementNames_);
            case TreeTokenType.TokenType(String grammar_, Set<QualifiedName> elementNames_) ->
                new GrammarAndElement(grammar_, elementNames_);
            case TreeNodeType.AnyNode() ->
                    new GrammarAndElement(null, null);
            case TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar ->
                    new GrammarAndElement(anyNodeFromGrammar.grammar(), typeFactory.grammarNodes(anyNodeFromGrammar.grammar()));
            case TreeRuleType.AnyRule anyRule ->
                    new GrammarAndElement(null, null);
            case TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar ->
                    new GrammarAndElement(anyRuleFromGrammar.grammar(), typeFactory.grammarNodes(anyRuleFromGrammar.grammar()));
            case TreeTokenType.AnyToken anyToken ->
                    new GrammarAndElement(null, null);
            case TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar ->
                    new GrammarAndElement(anyTokenFromGrammar.grammar(), typeFactory.grammarNodes(anyTokenFromGrammar.grammar()));
        };
    }



    record CardinalityResult(Cardinality cardinality) {}

    private CardinalityResult getResultingCardinality(XQueryAxis axis, AntlrQuerySequenceType type, boolean usesWildcards) {
        // No analysis cardinality algorithm
        // - each axis contains inherent cardinality that is usually 0..inf
        // - result equals to: type-cardinality x axis-factor + type-cardinality x axis-selfness(0|1)
        // - if element name filter was applied (!usesWildcards) every result can be filtered out so
        //   cardinality is optionalized
        final Cardinality axisFactor = axisToCardinality.get(axis);
        final Cardinality axisCardinality = Cardinalities.multiply(type.cardinality(), axisFactor);
        final Cardinality selfCardinalityMultipliedByFactor = Cardinalities.multiply(type.cardinality(), axisToSelfFactor.get(axis));
        final Cardinality finalCardinality = Cardinalities.sequenceMerge(axisCardinality, selfCardinalityMultipliedByFactor);
        final Cardinality optionalityFactor = usesWildcards? finalCardinality : Cardinalities.optionalize(finalCardinality);

        return new CardinalityResult(optionalityFactor);
    }


    record ItemTypeResult(AntlrQueryItemType itemType, Set<QualifiedName> ConstrainedNames, Set<QualifiedName> unreachableNames) {

    }
    
    private ItemTypeResult getResultingItemType(
        AntlrQuerySequenceType type, String grammar, Set<QualifiedName> elementNames, ValidateNamesResult validatedNames, boolean usesWildcard, boolean isSelfAxis
        ) 
    {
        // Unchecked analysis
        // itemType constraints:
        //      - uses wildcard?
        //           - true -> resultingItemType = any node
        //           - false -> 
        //                  - axis == self
        //                      resultingItemType = intersection(inputType.itemType, axisElementNames)
        //                  - else
        //                      resultingItemType = axisElementNames
        if (usesWildcard) {
            if (isSelfAxis) {
                return new ItemTypeResult(type.itemType(), Set.of(), Set.of());
            } else {
                return new ItemTypeResult(typeFactory.itemAnyNode(), Set.of(), Set.of());
            }
        } else { // specified names
            if (isSelfAxis) {
                final var unreachableNames = new HashSet<>(elementNames);
                unreachableNames.removeAll(validatedNames.validNames);
                final var constrainedNames = new HashSet<>(elementNames);
                constrainedNames.removeAll(unreachableNames);
                return new ItemTypeResult(typeFactory.itemElement(grammar, constrainedNames), constrainedNames, unreachableNames);
            } else {
                return new ItemTypeResult(typeFactory.itemElement(grammar, validatedNames.validNames), Set.of(), Set.of());
            }
        }
    }
        





	private AntlrQuerySequenceType getAnalyzedReturnedType(
            AnalyzedAxisResult analyzedAxis,
            String grammar,
            Set<QualifiedName> names,
            Cardinality inputCardinality)
    {
        Cardinality result =
            Cardinalities.multiply(
                inputCardinality,
                analyzedAxis.resultingCardinality()
            );

        return typeFactory.sequence(typeFactory.itemElement(grammar, names), result);
    }

    record AnalyzedAxisResult(
        Cardinality resultingCardinality,
        Set<QualifiedName> possibleNames,
        Set<QualifiedName> impossibleNames
        ){}

    private AnalyzedAxisResult analyzeAxisPathElements(
        final Set<QualifiedName> elementNames,
        final XQueryAxis axis,
        final ValidateNamesResult validateNamesResult,
        final QualifiedGrammarAnalysisResult analysis
        )
    {
        Cardinality resultingCardinality = Cardinality.ZERO;
        final Set<QualifiedName> possibleNames = new HashSet<>(validateNamesResult.validNames.size());
        final Set<QualifiedName> impossibleNames = new HashSet<>(validateNamesResult.validNames.size());
        final Map<QualifiedName, Map<QualifiedName, Cardinality>> axisInfo
            = analysis.axes().getOrDefault(axis, Map.of());
        for (final QualifiedName element : elementNames) {
            final Map<QualifiedName, Cardinality> elementInfo = axisInfo.getOrDefault(element, Map.of());

            for (final QualifiedName pathElementName : validateNamesResult.validNames) {
                final Cardinality pathElementCardinality
                    = elementInfo.getOrDefault(pathElementName, Cardinality.ZERO);
                if (pathElementCardinality == Cardinality.ZERO)
                {
                    impossibleNames.add(pathElementName);
                } else {
                    possibleNames.add(pathElementName);
                }
                resultingCardinality = Cardinalities.sequenceMerge(resultingCardinality, pathElementCardinality);
            }
        }
        return new AnalyzedAxisResult(resultingCardinality, possibleNames, impossibleNames);
    }

    private AnalyzedAxisResult analyzeAxisWithWildcard(
        final Set<QualifiedName> elementNames,
        final XQueryAxis axis,
        final QualifiedGrammarAnalysisResult analysis
        )
    {
        Cardinality resultingCardinality = Cardinality.ZERO;
        final Set<QualifiedName> possibleNames = new HashSet<>();
        final Map<QualifiedName, Map<QualifiedName, Cardinality>> axisInfo
            = analysis.axes().getOrDefault(axis, Map.of());
        for (final QualifiedName element : elementNames) {
            final Map<QualifiedName, Cardinality> elementInfo = axisInfo.getOrDefault(element, Map.of());
            for (final var pathElementName : elementInfo.keySet()) {
                final Cardinality pathElementCardinality
                    = elementInfo.getOrDefault(pathElementName, Cardinality.ZERO);
                if (pathElementCardinality != Cardinality.ZERO)
                {
                    possibleNames.add(pathElementName);
                }
                resultingCardinality = Cardinalities.sequenceMerge(resultingCardinality, pathElementCardinality);

            }
        }
        return new AnalyzedAxisResult(resultingCardinality, possibleNames, Set.of());
    }

    EnumMap<XQueryAxis, Cardinality> axisToCardinality = new EnumMap<>(XQueryAxis.class);
    {
        axisToCardinality.put(XQueryAxis.ANCESTOR, Cardinality.ZERO_OR_MORE);
        axisToCardinality.put(XQueryAxis.CHILD, Cardinality.ZERO_OR_MORE);
        axisToCardinality.put(XQueryAxis.DESCENDANT, Cardinality.ZERO_OR_MORE);
        axisToCardinality.put(XQueryAxis.FOLLOWING, Cardinality.ZERO_OR_MORE);
        axisToCardinality.put(XQueryAxis.FOLLOWING_SIBLING, Cardinality.ZERO_OR_MORE);
        axisToCardinality.put(XQueryAxis.PRECEDING, Cardinality.ZERO_OR_MORE);
        axisToCardinality.put(XQueryAxis.PRECEDING_SIBLING, Cardinality.ZERO_OR_MORE);
        
        axisToCardinality.put(XQueryAxis.ANCESTOR_OR_SELF, Cardinality.ZERO_OR_MORE);
        axisToCardinality.put(XQueryAxis.DESCENDANT_OR_SELF, Cardinality.ZERO_OR_MORE);
        axisToCardinality.put(XQueryAxis.FOLLOWING_OR_SELF, Cardinality.ZERO_OR_MORE);
        axisToCardinality.put(XQueryAxis.FOLLOWING_SIBLING_OR_SELF, Cardinality.ZERO_OR_MORE);
        axisToCardinality.put(XQueryAxis.PRECEDING_OR_SELF, Cardinality.ZERO_OR_MORE);
        axisToCardinality.put(XQueryAxis.PRECEDING_SIBLING_OR_SELF, Cardinality.ZERO_OR_MORE);
        
        axisToCardinality.put(XQueryAxis.PARENT, Cardinality.ZERO_OR_ONE);
        
        axisToCardinality.put(XQueryAxis.SELF, Cardinality.ONE); // SELF does not change cardinality, it returns the same as input

        for (XQueryAxis axis : XQueryAxis.values()) {
            if (!axisToCardinality.containsKey(axis)) {
                throw new IllegalStateException("Missing cardinality mapping for axis: " + axis);
            }
        }
    }
    
    EnumMap<XQueryAxis, Cardinality> axisToSelfFactor = new EnumMap<>(XQueryAxis.class);
    {
        axisToCardinality.put(XQueryAxis.ANCESTOR, Cardinality.ZERO);
        axisToCardinality.put(XQueryAxis.CHILD, Cardinality.ZERO);
        axisToCardinality.put(XQueryAxis.DESCENDANT, Cardinality.ZERO);
        axisToCardinality.put(XQueryAxis.FOLLOWING, Cardinality.ZERO);
        axisToCardinality.put(XQueryAxis.FOLLOWING_SIBLING, Cardinality.ZERO);
        axisToCardinality.put(XQueryAxis.PRECEDING, Cardinality.ZERO);
        axisToCardinality.put(XQueryAxis.PRECEDING_SIBLING, Cardinality.ZERO);
        
        axisToCardinality.put(XQueryAxis.ANCESTOR_OR_SELF, Cardinality.ONE);
        axisToCardinality.put(XQueryAxis.DESCENDANT_OR_SELF, Cardinality.ONE);
        axisToCardinality.put(XQueryAxis.FOLLOWING_OR_SELF, Cardinality.ONE);
        axisToCardinality.put(XQueryAxis.FOLLOWING_SIBLING_OR_SELF, Cardinality.ONE);
        axisToCardinality.put(XQueryAxis.PRECEDING_OR_SELF, Cardinality.ONE);
        axisToCardinality.put(XQueryAxis.PRECEDING_SIBLING_OR_SELF, Cardinality.ONE);
        
        axisToCardinality.put(XQueryAxis.PARENT, Cardinality.ZERO);
        
        axisToCardinality.put(XQueryAxis.SELF, Cardinality.ZERO); // SELF does not change cardinality, it returns the same as input

        for (XQueryAxis axis : XQueryAxis.values()) {
            if (!axisToCardinality.containsKey(axis)) {
                throw new IllegalStateException("Missing cardinality mapping for axis: " + axis);
            }
        }

    }
    
    private GrammarStatus getGrammarStatus(final String grammar)
    {
        if ("".equals(grammar)) {
            return GrammarStatus.UNCHECKED;
        } else if (!symbolManager.grammarExists(grammar)) {
            return GrammarStatus.UNREGISTERED;
        } else {
            return GrammarStatus.REGISTERED;
        }
    }


    public record ValidateNamesResult(
        Set<QualifiedName> qualifiedNames,
        Set<QualifiedName> validNames,
        Set<QualifiedName> invalidNames,
        Set<QualifiedName> duplicatedNames,
        Map<String, GrammarStatus> grammars
    ) {}

    private ValidateNamesResult resolveAndValidateNames(
        final @NonNull List<String> axisElementNames,
        final @NonNull NamespaceResolver namespaceResolver
    ) {
        final Set<QualifiedName> qualifiedNames = new HashSet<>(axisElementNames.size());
        final Set<QualifiedName> validNames = new HashSet<>(axisElementNames.size());
        final Set<QualifiedName> invalidNames = new HashSet<>(axisElementNames.size());
        final Set<QualifiedName> duplicatedNames = new HashSet<>(axisElementNames.size());
        final Map<String, GrammarStatus> usedGrammars = new HashMap<>(axisElementNames.size());
        for (final var name : axisElementNames) {
            final QualifiedName resolvedName = namespaceResolver.resolveElement(name);
            if (!qualifiedNames.add(resolvedName)) {
                duplicatedNames.add(resolvedName);
            }
            final GrammarStatus grammarStatus = usedGrammars.computeIfAbsent(resolvedName.namespace(), this::getGrammarStatus);
            switch(grammarStatus) {
                case REGISTERED -> {
                    final QualifiedGrammarAnalysisResult analysis = symbolManager.getGrammar(resolvedName.namespace());
                    if (!analysis.elementNames().contains(resolvedName)) {
                        invalidNames.add(resolvedName);
                    } else {
                        validNames.add(resolvedName);
                    }
                }
                case UNCHECKED -> validNames.add(resolvedName);
                case UNREGISTERED -> invalidNames.add(resolvedName);
            }
        }
        return new ValidateNamesResult(qualifiedNames, validNames, invalidNames, duplicatedNames, usedGrammars);
    }
}