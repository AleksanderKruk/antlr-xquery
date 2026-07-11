package com.github.akruk.antlrxquery.typesystem.typeoperations;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.github.akruk.antlrxquery.XQueryAxis;
import com.github.akruk.antlrxquery.inputgrammaranalyzer.InputGrammarAnalyzer.QualifiedGrammarAnalysisResult;
import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver;
import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver.QualifiedName;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.XQuerySemanticSymbolManager;
import com.github.akruk.antlrxquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrxquery.typesystem.typeoperations.occurence.BlockCardinalityMerger;
import com.github.akruk.antlrxquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.types.Cardinality;
import com.github.akruk.antlrxquery.typesystem.types.XQueryItemType;

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
        blockCardinalityMerger = new BlockCardinalityMerger();
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
     * @param targetGrammar
     * Identifier of the grammar that the element names should come from.
     * If equals null than no grammar semantic checks are performed
     * @param type
     * left hand side type targetted by operator
     * @param axis
     * operator axis
     * @param axisElementNames
     * names of the elements or null if wildcard is used
     * @param namespaceResolver
     * namespace resolver used to resolve qualified element names
     * @return PathOperatorResult
     *         - inputStatus - information on target {@link InputStatus} 
     *         - result - resulting type or placeholder
     *         - inputGrammars - list of detected grammars in the input type
     *         - elementGrammars - list of detected grammars in the elements
     *         - invalidNames - names that are absent from the grammar
     *         - duplicatedNames - names that have been duplicated
     */
    public PathOperatorResult pathOperator(
        final @NonNull AntlrQuerySequenceType type,
        final @NonNull XQueryAxis axis,
        final @Nullable List<String> axisElementNames,
        final @NonNull NamespaceResolver namespaceResolver
        )
    {
        final ValidateNamesResult validatedNames = resolveAndValidateNames(axisElementNames, namespaceResolver);
        final Set<QualifiedName> typeElementNames = type.itemType.elementNames!=null ? type.itemType.elementNames : Set.of();
        final Map<String, GrammarStatus> inputGrammars = typeElementNames
            .stream()
            .map(QualifiedName::namespace)
            .collect(Collectors.toMap(
                UnaryOperator.identity(),
                this::getGrammarStatus
            ));

        final boolean usesWildcard = axisElementNames == null;
        final boolean isSelf = axis == XQueryAxis.SELF;
        final boolean nodeLike = switch(type.itemType.type) {
            case ELEMENT, ANY_NODE -> true;
            case ANY_ARRAY, ANY_FUNCTION, ANY_ITEM, ANY_MAP,
                ARRAY, BOOLEAN, CHOICE, ENUM, ERROR, EXTENSIBLE_RECORD,
                FUNCTION, MAP, NUMBER, RECORD, STRING -> false;
        };
        if (!nodeLike) {
            return new PathOperatorResult(
                InputStatus.NON_NODES, 
                zeroOrMoreNodes, 
                inputGrammars, 
                validatedNames.grammars, 
                validatedNames.invalidNames, 
                validatedNames.duplicatedNames, 
                Set.of());
        }

        final ItemTypeResult resultingItemType = getResultingItemType(type, validatedNames, usesWildcard, isSelf);
        final CardinalityResult resultingCardinality = getResultingCardinality(axis, type, usesWildcard, isSelf);
        final AntlrQuerySequenceType resultingType = typeFactory.sequence(resultingItemType.itemType, resultingCardinality.cardinality);

        final boolean isSingleGrammarInput = validatedNames.grammars.size() == 1;
        if (isSingleGrammarInput) {
            final String inputGrammar = inputGrammars.keySet().stream().findFirst().get();
            final QualifiedGrammarAnalysisResult analysis = symbolManager.getGrammar(inputGrammar);
            if (analysis == null) {
                return new PathOperatorResult(
                    InputStatus.OK, resultingType, inputGrammars, validatedNames.grammars, validatedNames.invalidNames, 
                    validatedNames.duplicatedNames, resultingItemType.unreachableNames);
            } else {
                final AnalyzedAxisResult analysisResult = usesWildcard 
                    ? analyzeAxisWithWildcard(resultingType, axis, analysis)
                    : analyzeAxisPathElements(resultingType, axis, validatedNames, analysis);
                final AntlrQuerySequenceType analyzedType = getAnalyzedReturnedType(analysisResult, typeElementNames, type.cardinality);
                
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
        } else { // multigrammar analysis
            return new PathOperatorResult(
                InputStatus.MULTIGRAMMAR, resultingType, inputGrammars, validatedNames.grammars, validatedNames.invalidNames, 
                validatedNames.duplicatedNames, resultingItemType.unreachableNames);
        }

    }



    record CardinalityResult(Cardinality cardinality) {}

    private CardinalityResult getResultingCardinality(XQueryAxis axis, AntlrQuerySequenceType type, boolean usesWildcards, boolean isSelfAxis) {
        // No analysis cardinality algorithm
        // - each axis contains inherent cardinality that is usually 0..inf
        // - result equals to: type-cardinality x axis-factor + type-cardinality x axis-selfness(0|1)
        // - if element name filter was appllied (!usesWildcards) every result can be filtered out so
        //   cardinality is optionalized
        final Cardinality axisFactor = axisToCardinality.get(axis);
        final Cardinality axisCardinality = Cardinalities.multiply(type.cardinality, axisFactor);
        final Cardinality selfCardinalityMultipliedByFactor = Cardinalities.multiply(type.cardinality, axisToSelfFactor.get(axis));
        final Cardinality finalCardinality = Cardinalities.sequenceMerge(axisCardinality, selfCardinalityMultipliedByFactor);
        final Cardinality optionalityFactor = usesWildcards? finalCardinality : Cardinalities.optionalize(finalCardinality);

        return new CardinalityResult(optionalityFactor);
    }


    record ItemTypeResult(XQueryItemType itemType, Set<QualifiedName> ConstrainedNames, Set<QualifiedName> unreachableNames) {

    }
    
    private ItemTypeResult getResultingItemType(
        AntlrQuerySequenceType type, ValidateNamesResult validatedNames, boolean usesWildcard, boolean isSelfAxis
        ) 
    {
        // Unchecked analysis
        // itemtype constraints:
        //      - uses wildcard?
        //           - true -> resultingItemType = any node
        //           - false -> 
        //                  - axis == self
        //                      resultingItemType = intersection(inputType.itemType, axisElementNames)
        //                  - else
        //                      resultingItemType = axisElementNames
        if (usesWildcard) {
            if (isSelfAxis) {
                return new ItemTypeResult(type.itemType, Set.of(), Set.of());
            } else {
                return new ItemTypeResult(typeFactory.itemAnyNode(), Set.of(), Set.of());
            }
        } else { // specified names
            if (isSelfAxis) {
                final var unreachableNames = new HashSet<>(type.itemType.elementNames);
                unreachableNames.removeAll(validatedNames.validNames);
                final var constrainedNames = new HashSet<>(type.itemType.elementNames);
                constrainedNames.removeAll(unreachableNames);
                return new ItemTypeResult(typeFactory.itemElement(constrainedNames), constrainedNames, unreachableNames);
            } else {
                return new ItemTypeResult(typeFactory.itemElement(validatedNames.validNames), Set.of(), Set.of());
            }
        }
    }
        





	private AntlrQuerySequenceType getAnalyzedReturnedType(
            AnalyzedAxisResult analyzedAxis,
            Set<QualifiedName> names,
            Cardinality inputCardinality)
    {
        Cardinality result =
            Cardinalities.multiply(
                inputCardinality,
                analyzedAxis.resultingCardinality()
            );

        return typeFactory.sequence(typeFactory.itemElement(names), result);
    }

    final BlockCardinalityMerger blockCardinalityMerger;

    record AnalyzedAxisResult(
        Cardinality resultingCardinality,
        Set<QualifiedName> possibleNames,
        Set<QualifiedName> impossibleNames
        ){}

    private AnalyzedAxisResult analyzeAxisPathElements(
        final AntlrQuerySequenceType type,
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
        for (final QualifiedName element : type.itemType.elementNames) {
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
        final AntlrQuerySequenceType type,
        final XQueryAxis axis,
        final QualifiedGrammarAnalysisResult analysis
        )
    {
        Cardinality resultingCardinality = Cardinality.ZERO;
        final Set<QualifiedName> possibleNames = new HashSet<>();
        final Map<QualifiedName, Map<QualifiedName, Cardinality>> axisInfo
            = analysis.axes().getOrDefault(axis, Map.of());
        for (final QualifiedName element : type.itemType.elementNames) {
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
        GrammarStatus grammarStatus = null;
        if ("".equals(grammar)) {
            grammarStatus = GrammarStatus.UNCHECKED;
        }
        else if (!symbolManager.grammarExists(grammar)) {
            grammarStatus = GrammarStatus.UNREGISTERED;
        } else {
            grammarStatus = GrammarStatus.REGISTERED;
        }
        return grammarStatus;
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
                case UNCHECKED -> {
                    validNames.add(resolvedName);
                }
                case UNREGISTERED -> {
                    invalidNames.add(resolvedName);
                }
            }
        }
        return new ValidateNamesResult(qualifiedNames, validNames, invalidNames, duplicatedNames, usedGrammars);
    }
}
    // private final Predicate<String> canBeTokenName = Pattern.compile("^[\\p{IsUppercase}].*").asPredicate();