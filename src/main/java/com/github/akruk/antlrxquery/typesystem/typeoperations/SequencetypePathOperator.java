package com.github.akruk.antlrxquery.typesystem.typeoperations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import com.github.akruk.antlrxquery.XQueryAxis;
import com.github.akruk.antlrxquery.inputgrammaranalyzer.InputGrammarAnalyzer.QualifiedGrammarAnalysisResult;
import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver;
import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver.QualifiedName;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.XQuerySemanticSymbolManager;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryCardinality;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.typeoperations.occurence.BlockCardinalityMerger;
import com.github.akruk.antlrxquery.typesystem.typeoperations.occurence.SequenceCardinalityMerger;

public class SequencetypePathOperator {
    private final XQueryTypeFactory typeFactory;
	private final XQuerySemanticSymbolManager symbolManager;
    private final XQuerySequenceType zeroOrOneNode;
    private final XQuerySequenceType zeroOrMoreNodes;
    private final XQuerySequenceType oneOrMoreNodes;
    private final XQuerySequenceType emptySequence;

    public SequencetypePathOperator(
        final XQueryTypeFactory typeFactory,
        final XQuerySemanticSymbolManager symbolManager
        )
    {
        this.typeFactory = typeFactory;
		this.symbolManager = symbolManager;
        this.zeroOrMoreNodes = typeFactory.zeroOrMore(typeFactory.itemAnyNode());
        this.oneOrMoreNodes = typeFactory.oneOrMore(typeFactory.itemAnyNode());
        this.emptySequence = typeFactory.emptySequence();
        this.zeroOrOneNode = typeFactory.zeroOrOne(typeFactory.itemAnyNode());
        zeroResult = new PathOperatorResult(
            InputStatus.EMPTY_SEQUENCE,
            emptySequence,
            Map.of(),
            Map.of(),
            Set.of(),
            Set.of(),
            Set.of());
        sequenceCardinalityMerger = new SequenceCardinalityMerger();
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
        XQuerySequenceType result,
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
     * <ul>
     *     <li><b>inputStatus</b> - information on target {@link InputStatus} </li>
     *     <li><b>result</b> - resulting type or placeholder</li>
     *     <li><b>inputGrammars</b> - list of detected grammars in the input type</li>
     *     <li><b>elementGrammars</b> - list of detected grammars in the elements</li>
     *     <li><b>invalidNames</b> - names that are absent from the grammar</li>
     *     <li><b>duplicatedNames</b> - names that have been duplicated</li>
     * </ul>
     */
    public PathOperatorResult pathOperator(
        final XQuerySequenceType type,
        final XQueryAxis axis,
        final List<String> axisElementNames,
        final NamespaceResolver namespaceResolver
        )
    {
        if (type.occurence == XQueryCardinality.ZERO)
            return zeroResult;
        final boolean usesWildcard = axisElementNames == null;

        switch (type.itemType.type) {
            case ELEMENT -> {
                final Set<QualifiedName> typeElementNames = type.itemType.elementNames;
                final Map<String, GrammarStatus> inputGrammars = typeElementNames
                    .stream()
                    .map(QualifiedName::namespace)
                    .collect(Collectors.toMap(
                        UnaryOperator.identity(),
                        this::getGrammarStatus
                    ));
                if (inputGrammars.size() == 1) { // single grammar input
                    final String inputGrammar = inputGrammars.keySet().stream().findFirst().get();
                    final QualifiedGrammarAnalysisResult analysis = symbolManager.getGrammar(inputGrammar);
                    final ValidateNamesResult validateNamesResult = resolveAndValidateNames(axisElementNames, namespaceResolver);
                    if (usesWildcard) {
                        if (analysis == null) {
                            return switch (type.occurence) {
                                case ONE          -> getResult_SingleGrammar_InputElements_One_NoAnalysis_Wildcard(InputStatus.OK, type, axis, validateNamesResult, inputGrammars);
                                case ONE_OR_MORE  -> getResult_SingleGrammar_InputElements_OneOrMore_NoAnalysis_Wildcard(InputStatus.OK, type, axis, validateNamesResult, inputGrammars);
                                case ZERO_OR_MORE -> getResult_SingleGrammar_InputElements_ZeroOrMore_NoAnalysis_Wildcard(InputStatus.OK, type, axis, validateNamesResult, inputGrammars);
                                case ZERO_OR_ONE  -> getResult_SingleGrammar_InputElements_ZeroOrOne_NoAnalysis_Wildcard(InputStatus.OK, type, axis, validateNamesResult, inputGrammars);
                                case ZERO         -> null; // already excluded
                            };
                        } else {
                            return getResult_SingleGrammar_InputElements_Analyzed_Wildcard(type, axis, validateNamesResult, analysis, inputGrammars);
                        }
                    } else {
                        if (analysis == null) {
                            return switch (type.occurence) {
                                case ONE          -> getResult_SingleGrammar_InputElements_One_NoAnalysis_Elements(InputStatus.OK, type, axis, validateNamesResult, inputGrammars);
                                case ONE_OR_MORE  -> getResult_SingleGrammar_InputElements_OneOrMore_NoAnalysis_Elements(InputStatus.OK, type, axis, validateNamesResult, inputGrammars);
                                case ZERO_OR_MORE -> getResult_SingleGrammar_InputElements_ZeroOrMore_NoAnalysis_Elements(InputStatus.OK, type, axis, validateNamesResult, inputGrammars);
                                case ZERO_OR_ONE  -> getResult_SingleGrammar_InputElements_ZeroOrOne_NoAnalysis_Elements(InputStatus.OK, type, axis, validateNamesResult, inputGrammars);
                                case ZERO         -> null; // already excluded
                            };
                        } else {
                            return getResult_SingleGrammar_InputElements_Analyzed_Elements(type, axis, validateNamesResult, analysis, inputGrammars);
                        }
                    }
                } else { // multigrammar input
                    final ValidateNamesResult validateNamesResult = resolveAndValidateNames(axisElementNames, namespaceResolver);
                    return switch (type.occurence) {
                        case ONE          -> getResult_SingleGrammar_InputElements_One_NoAnalysis_Elements(InputStatus.MULTIGRAMMAR, type, axis, validateNamesResult, inputGrammars);
                        case ONE_OR_MORE  -> getResult_SingleGrammar_InputElements_OneOrMore_NoAnalysis_Elements(InputStatus.MULTIGRAMMAR, type, axis, validateNamesResult, inputGrammars);
                        case ZERO_OR_MORE -> getResult_SingleGrammar_InputElements_ZeroOrMore_NoAnalysis_Elements(InputStatus.MULTIGRAMMAR, type, axis, validateNamesResult, inputGrammars);
                        case ZERO_OR_ONE  -> getResult_SingleGrammar_InputElements_ZeroOrOne_NoAnalysis_Elements(InputStatus.MULTIGRAMMAR, type, axis, validateNamesResult, inputGrammars);
                        case ZERO         -> null; // already excluded
                    };
                }

            }
            case ANY_NODE -> {
                final ValidateNamesResult validateNamesResult = resolveAndValidateNames(axisElementNames, namespaceResolver);
                final Map<String, GrammarStatus> inputGrammars = Map.of();
                return switch (type.occurence) {
                    case ONE          -> getResult_SingleGrammar_InputElements_One_NoAnalysis_Elements(InputStatus.MULTIGRAMMAR, type, axis, validateNamesResult, inputGrammars);
                    case ONE_OR_MORE  -> getResult_SingleGrammar_InputElements_OneOrMore_NoAnalysis_Elements(InputStatus.MULTIGRAMMAR, type, axis, validateNamesResult, inputGrammars);
                    case ZERO_OR_MORE -> getResult_SingleGrammar_InputElements_ZeroOrMore_NoAnalysis_Elements(InputStatus.MULTIGRAMMAR, type, axis, validateNamesResult, inputGrammars);
                    case ZERO_OR_ONE  -> getResult_SingleGrammar_InputElements_ZeroOrOne_NoAnalysis_Elements(InputStatus.MULTIGRAMMAR, type, axis, validateNamesResult, inputGrammars);
                    case ZERO         -> null; // already excluded
                };
            }
            case ANY_ARRAY, ANY_FUNCTION, ANY_ITEM, ANY_MAP,
                ARRAY, BOOLEAN, CHOICE, ENUM, ERROR, EXTENSIBLE_RECORD,
                FUNCTION, MAP, NUMBER, RECORD, STRING ->
            {
                return new PathOperatorResult(
                    InputStatus.NON_NODES,
                    zeroOrMoreNodes,
                    Map.of(),
                    Map.of(),
                    Set.of(),
                    Set.of(),
                    Set.of());

            }
        }
        return null; // unreachable
    }

    private PathOperatorResult getResult_SingleGrammar_InputElements_ZeroOrOne_NoAnalysis_Elements(
        final InputStatus inputStatus,
        final XQuerySequenceType type,
        final XQueryAxis axis,
        final ValidateNamesResult validateNamesResult,
        final Map<String,GrammarStatus> inputGrammars)
    {
        switch(axis) {
        case ANCESTOR, CHILD, DESCENDANT, FOLLOWING, FOLLOWING_SIBLING, PRECEDING, PRECEDING_SIBLING:
        case ANCESTOR_OR_SELF, DESCENDANT_OR_SELF, FOLLOWING_OR_SELF, FOLLOWING_SIBLING_OR_SELF, PRECEDING_OR_SELF, PRECEDING_SIBLING_OR_SELF:
            return new PathOperatorResult(
                inputStatus,
                typeFactory.zeroOrMore(typeFactory.itemElement(validateNamesResult.validNames)),
                inputGrammars,
                validateNamesResult.grammars,
                validateNamesResult.invalidNames,
                validateNamesResult.duplicatedNames,
                Set.of()
            );
        case PARENT:
            return new PathOperatorResult(
                inputStatus,
                typeFactory.oneOrMore(typeFactory.itemElement(validateNamesResult.validNames)),
                inputGrammars,
                validateNamesResult.grammars,
                validateNamesResult.invalidNames,
                validateNamesResult.duplicatedNames,
                Set.of()
            );
        case SELF:
            final var unreachableNames = new HashSet<>(type.itemType.elementNames);
            unreachableNames.removeAll(validateNamesResult.validNames);
            final var constrainedNames = new HashSet<>(type.itemType.elementNames);
            constrainedNames.removeAll(unreachableNames);
            final XQuerySequenceType constrainedType = constrainedNames.size() > 0
                ? typeFactory.zeroOrOne(typeFactory.itemElement(constrainedNames))
                : emptySequence;
            // XQuerySequenceType selfType = typeFactory.
            return new PathOperatorResult(
                inputStatus,
                constrainedType,
                inputGrammars,
                validateNamesResult.grammars,
                validateNamesResult.invalidNames,
                validateNamesResult.duplicatedNames,
                unreachableNames
            );
        }
        return null;//unreachable
    }
    private PathOperatorResult getResult_SingleGrammar_InputElements_ZeroOrMore_NoAnalysis_Elements(
        final InputStatus inputStatus,
        final XQuerySequenceType type,
        final XQueryAxis axis,
        final ValidateNamesResult validateNamesResult,
        final Map<String,GrammarStatus> inputGrammars
        )
    {
        switch(axis) {
        case ANCESTOR, CHILD, DESCENDANT, FOLLOWING, FOLLOWING_SIBLING, PRECEDING, PRECEDING_SIBLING:
        case ANCESTOR_OR_SELF, DESCENDANT_OR_SELF, FOLLOWING_OR_SELF, FOLLOWING_SIBLING_OR_SELF, PRECEDING_OR_SELF, PRECEDING_SIBLING_OR_SELF:
            return new PathOperatorResult(
                inputStatus,
                typeFactory.zeroOrMore(typeFactory.itemElement(validateNamesResult.validNames)),
                inputGrammars,
                validateNamesResult.grammars,
                validateNamesResult.invalidNames,
                validateNamesResult.duplicatedNames,
                Set.of()
            );
        case PARENT:
            return new PathOperatorResult(
                inputStatus,
                typeFactory.oneOrMore(typeFactory.itemElement(validateNamesResult.validNames)),
                inputGrammars,
                validateNamesResult.grammars,
                validateNamesResult.invalidNames,
                validateNamesResult.duplicatedNames,
                Set.of()
            );
        case SELF:
            final var unreachableNames = new HashSet<>(type.itemType.elementNames);
            unreachableNames.removeAll(validateNamesResult.validNames);
            final var constrainedNames = new HashSet<>(type.itemType.elementNames);
            constrainedNames.removeAll(unreachableNames);
            final XQuerySequenceType constrainedType = constrainedNames.size() > 0
                ? typeFactory.zeroOrMore(typeFactory.itemElement(constrainedNames))
                : emptySequence;
            return new PathOperatorResult(
                inputStatus,
                constrainedType,
                inputGrammars,
                validateNamesResult.grammars,
                validateNamesResult.invalidNames,
                validateNamesResult.duplicatedNames,
                unreachableNames
            );
        }
        return null;//unreachable
    }

    private PathOperatorResult getResult_SingleGrammar_InputElements_OneOrMore_NoAnalysis_Elements(
        final InputStatus inputStatus,
        final XQuerySequenceType type,
        final XQueryAxis axis,
        final ValidateNamesResult validateNamesResult,
        final Map<String,GrammarStatus> inputGrammars)
    {
        switch(axis) {
        case ANCESTOR, CHILD, DESCENDANT, FOLLOWING, FOLLOWING_SIBLING, PRECEDING, PRECEDING_SIBLING, PARENT:
            return new PathOperatorResult(
                inputStatus,
                typeFactory.zeroOrMore(typeFactory.itemElement(validateNamesResult.validNames)),
                inputGrammars,
                validateNamesResult.grammars,
                validateNamesResult.invalidNames,
                validateNamesResult.duplicatedNames,
                Set.of()
            );
        case ANCESTOR_OR_SELF, DESCENDANT_OR_SELF, FOLLOWING_OR_SELF, FOLLOWING_SIBLING_OR_SELF, PRECEDING_OR_SELF, PRECEDING_SIBLING_OR_SELF:
            return new PathOperatorResult(
                inputStatus,
                typeFactory.oneOrMore(typeFactory.itemElement(validateNamesResult.validNames)),
                inputGrammars,
                validateNamesResult.grammars,
                validateNamesResult.invalidNames,
                validateNamesResult.duplicatedNames,
                Set.of()
            );
        case SELF:
            final var unreachableNames = new HashSet<>(type.itemType.elementNames);
            unreachableNames.removeAll(validateNamesResult.validNames);
            final var constrainedNames = new HashSet<>(type.itemType.elementNames);
            constrainedNames.removeAll(unreachableNames);
            final XQuerySequenceType constrainedType = constrainedNames.size() > 0
                ? typeFactory.zeroOrMore(typeFactory.itemElement(constrainedNames))
                : emptySequence;
            return new PathOperatorResult(
                inputStatus,
                constrainedType,
                inputGrammars,
                validateNamesResult.grammars,
                validateNamesResult.invalidNames,
                validateNamesResult.duplicatedNames,
                unreachableNames
            );
        }
        return null;//unreachable
    }

    private PathOperatorResult getResult_SingleGrammar_InputElements_One_NoAnalysis_Elements(
        final InputStatus inputStatus,
        final XQuerySequenceType type,
        final XQueryAxis axis,
        final ValidateNamesResult validateNamesResult,
        final Map<String,GrammarStatus> inputGrammars
        )
    {
        final var elementGrammars = validateNamesResult.grammars;
        switch(axis) {
        case ANCESTOR, CHILD, DESCENDANT, FOLLOWING, FOLLOWING_SIBLING, PRECEDING, PRECEDING_SIBLING:
            return new PathOperatorResult(
                inputStatus,
                typeFactory.zeroOrMore(typeFactory.itemElement(validateNamesResult.validNames)),
                inputGrammars,
                elementGrammars,
                validateNamesResult.invalidNames,
                validateNamesResult.duplicatedNames,
                Set.of()
            );
        case ANCESTOR_OR_SELF, DESCENDANT_OR_SELF, FOLLOWING_OR_SELF, FOLLOWING_SIBLING_OR_SELF, PRECEDING_OR_SELF, PRECEDING_SIBLING_OR_SELF:
            return new PathOperatorResult(
                inputStatus,
                typeFactory.oneOrMore(typeFactory.itemElement(validateNamesResult.validNames)),
                inputGrammars,
                elementGrammars,
                validateNamesResult.invalidNames,
                validateNamesResult.duplicatedNames,
                Set.of()
            );
        case PARENT:
            return new PathOperatorResult(
                inputStatus,
                typeFactory.zeroOrOne(typeFactory.itemElement(validateNamesResult.validNames)),
                inputGrammars,
                elementGrammars,
                validateNamesResult.invalidNames,
                validateNamesResult.duplicatedNames,
                Set.of()
            );
        case SELF:
            final var unreachableNames = new HashSet<>(type.itemType.elementNames);
            unreachableNames.removeAll(validateNamesResult.validNames);
            final var constrainedNames = new HashSet<>(type.itemType.elementNames);
            constrainedNames.removeAll(unreachableNames);
            final XQuerySequenceType constrainedType = constrainedNames.size() > 0
                ? typeFactory.zeroOrOne(typeFactory.itemElement(constrainedNames))
                : emptySequence;
            return new PathOperatorResult(
                inputStatus,
                constrainedType,
                inputGrammars,
                validateNamesResult.grammars,
                validateNamesResult.invalidNames,
                validateNamesResult.duplicatedNames,
                unreachableNames
            );
        }
        return null; // unreachable
    }

    private PathOperatorResult getResult_SingleGrammar_InputElements_Analyzed_Elements(
        final XQuerySequenceType type,
        final XQueryAxis axis, final ValidateNamesResult validateNamesResult, final QualifiedGrammarAnalysisResult analysis,
        final Map<String,GrammarStatus> inputGrammars)
    {
        final Map<String, GrammarStatus> elementGrammars = validateNamesResult.grammars;
        final AnalyzedAxisResult analyzedAxis = analyzeAxisPathElements(type, axis, validateNamesResult, analysis);
        final var names = analyzedAxis.possibleNames;
        final var returnedType = getAnalyzedReturnedType(analyzedAxis, names, type.occurence);
        return new PathOperatorResult(
            InputStatus.OK,
            returnedType,
            inputGrammars,
            elementGrammars,
            validateNamesResult.invalidNames,
            validateNamesResult.duplicatedNames,
            analyzedAxis.impossibleNames
        );
    }

    private PathOperatorResult getResult_SingleGrammar_InputElements_Analyzed_Wildcard(
        final XQuerySequenceType type,
        final XQueryAxis axis,
        final ValidateNamesResult validateNamesResult,
        final QualifiedGrammarAnalysisResult analysis,
        final Map<String, GrammarStatus> inputGrammars
        )
    {
        final var elementGrammars = validateNamesResult.grammars;
        final var analyzedAxis = analyzeAxisWithWildcard(type, axis, analysis);
        final var names = analyzedAxis.possibleNames;
        final var returnedType = getAnalyzedReturnedType(analyzedAxis, names, type.occurence);
        return new PathOperatorResult(
            InputStatus.OK,
            returnedType,
            inputGrammars,
            elementGrammars,
            validateNamesResult.invalidNames,
            validateNamesResult.duplicatedNames,
            analyzedAxis.impossibleNames
        );
    }

    private XQuerySequenceType getAnalyzedReturnedType(
        final AnalyzedAxisResult analyzedAxis,
        final Set<QualifiedName> names,
        final XQueryCardinality inputTypeCardinality)
    {
        final XQueryCardinality blockMerged = blockCardinalityMerger.merge(
            analyzedAxis.resultingCardinality,
            inputTypeCardinality
            );
        return switch(blockMerged) {
            case ZERO         -> emptySequence;
            case ONE          -> typeFactory.one(typeFactory.itemElement(names));
            case ZERO_OR_ONE  -> typeFactory.zeroOrOne(typeFactory.itemElement(names));
            case ZERO_OR_MORE -> typeFactory.zeroOrMore(typeFactory.itemElement(names));
            case ONE_OR_MORE  -> typeFactory.oneOrMore(typeFactory.itemElement(names));
        };
    }


    final SequenceCardinalityMerger sequenceCardinalityMerger;
    final BlockCardinalityMerger blockCardinalityMerger;

    record AnalyzedAxisResult(
        XQueryCardinality resultingCardinality,
        Set<QualifiedName> possibleNames,
        Set<QualifiedName> impossibleNames
        ){}

    private AnalyzedAxisResult analyzeAxisPathElements(
        final XQuerySequenceType type,
        final XQueryAxis axis,
        final ValidateNamesResult validateNamesResult,
        final QualifiedGrammarAnalysisResult analysis
        )
    {
        XQueryCardinality resultingCardinality = XQueryCardinality.ZERO;
        final Set<QualifiedName> possibleNames = new HashSet<>(validateNamesResult.validNames.size());
        final Set<QualifiedName> impossibleNames = new HashSet<>(validateNamesResult.validNames.size());
        final Map<QualifiedName, Map<QualifiedName, XQueryCardinality>> axisInfo
            = analysis.axes().getOrDefault(axis, Map.of());
        for (final QualifiedName element : type.itemType.elementNames) {
            final Map<QualifiedName, XQueryCardinality> elementInfo = axisInfo.getOrDefault(element, Map.of());

            for (final QualifiedName pathElementName : validateNamesResult.validNames) {
                final XQueryCardinality pathElementCardinality
                    = elementInfo.getOrDefault(pathElementName, XQueryCardinality.ZERO);
                if (pathElementCardinality == XQueryCardinality.ZERO)
                {
                    impossibleNames.add(pathElementName);
                } else {
                    possibleNames.add(pathElementName);
                }
                resultingCardinality = sequenceCardinalityMerger.merge(resultingCardinality, pathElementCardinality);
            }
        }
        return new AnalyzedAxisResult(resultingCardinality, possibleNames, impossibleNames);
    }




    private AnalyzedAxisResult analyzeAxisWithWildcard(
        final XQuerySequenceType type,
        final XQueryAxis axis,
        final QualifiedGrammarAnalysisResult analysis
        )
    {
        XQueryCardinality resultingCardinality = XQueryCardinality.ZERO;
        final Set<QualifiedName> possibleNames = new HashSet<>();
        final Map<QualifiedName, Map<QualifiedName, XQueryCardinality>> axisInfo
            = analysis.axes().getOrDefault(axis, Map.of());
        for (final QualifiedName element : type.itemType.elementNames) {
            final Map<QualifiedName, XQueryCardinality> elementInfo = axisInfo.getOrDefault(element, Map.of());
            for (final var pathElementName : elementInfo.keySet()) {
                final XQueryCardinality pathElementCardinality
                    = elementInfo.getOrDefault(pathElementName, XQueryCardinality.ZERO);
                if (pathElementCardinality != XQueryCardinality.ZERO)
                {
                    possibleNames.add(pathElementName);
                }
                resultingCardinality = sequenceCardinalityMerger.merge(resultingCardinality, pathElementCardinality);

            }
        }
        return new AnalyzedAxisResult(resultingCardinality, possibleNames, Set.of());
    }

    private PathOperatorResult getResult_SingleGrammar_InputElements_ZeroOrOne_NoAnalysis_Wildcard(
        final InputStatus inputStatus,
        final XQuerySequenceType type,
        final XQueryAxis axis,
        final ValidateNamesResult validateNamesResult,
        final Map<String, GrammarStatus> inputGrammars
    )
    {
        switch(axis) {
        case ANCESTOR, CHILD, DESCENDANT, FOLLOWING, FOLLOWING_SIBLING, PRECEDING, PRECEDING_SIBLING:
        case ANCESTOR_OR_SELF, DESCENDANT_OR_SELF, FOLLOWING_OR_SELF, FOLLOWING_SIBLING_OR_SELF, PRECEDING_OR_SELF, PRECEDING_SIBLING_OR_SELF:
            return new PathOperatorResult(
                inputStatus,
                zeroOrMoreNodes,
                inputGrammars,
                validateNamesResult.grammars,
                validateNamesResult.invalidNames,
                validateNamesResult.duplicatedNames,
                Set.of()
            );
        case PARENT:
            return new PathOperatorResult(
                inputStatus,
                zeroOrOneNode,
                inputGrammars,
                validateNamesResult.grammars,
                validateNamesResult.invalidNames,
                validateNamesResult.duplicatedNames,
                Set.of()
            );
        case SELF:
            return new PathOperatorResult(
                inputStatus,
                type,
                inputGrammars,
                validateNamesResult.grammars,
                validateNamesResult.invalidNames,
                validateNamesResult.duplicatedNames,
                Set.of()
            );
        }
        return null;//unreachable
    }

    private PathOperatorResult getResult_SingleGrammar_InputElements_ZeroOrMore_NoAnalysis_Wildcard(
        final InputStatus inputStatus,
        final XQuerySequenceType type,
        final XQueryAxis axis,
        final ValidateNamesResult validateNamesResult,
        final Map<String, GrammarStatus> inputGrammars
    )
    {
        switch(axis) {
        case ANCESTOR, CHILD, DESCENDANT, FOLLOWING, FOLLOWING_SIBLING, PRECEDING, PRECEDING_SIBLING:
        case ANCESTOR_OR_SELF, DESCENDANT_OR_SELF, FOLLOWING_OR_SELF, FOLLOWING_SIBLING_OR_SELF, PRECEDING_OR_SELF, PRECEDING_SIBLING_OR_SELF:
            return new PathOperatorResult(
                inputStatus,
                zeroOrMoreNodes,
                inputGrammars,
                validateNamesResult.grammars,
                validateNamesResult.invalidNames,
                validateNamesResult.duplicatedNames,
                Set.of()
            );
        case PARENT:
            return new PathOperatorResult(
                inputStatus,
                zeroOrMoreNodes,
                inputGrammars,
                validateNamesResult.grammars,
                validateNamesResult.invalidNames,
                validateNamesResult.duplicatedNames,
                Set.of()
            );
        case SELF:
            return new PathOperatorResult(
                inputStatus,
                type,
                inputGrammars,
                validateNamesResult.grammars,
                validateNamesResult.invalidNames,
                validateNamesResult.duplicatedNames,
                Set.of()
            );
        }
        return null;//unreachable
    }

    private PathOperatorResult getResult_SingleGrammar_InputElements_OneOrMore_NoAnalysis_Wildcard(
        final InputStatus inputStatus,
        final XQuerySequenceType type,
        final XQueryAxis axis,
        final ValidateNamesResult validateNamesResult,
        final Map<String, GrammarStatus> inputGrammars
    )
    {
        switch(axis) {
        case ANCESTOR, CHILD, DESCENDANT, FOLLOWING, FOLLOWING_SIBLING, PRECEDING, PRECEDING_SIBLING, PARENT:
            return new PathOperatorResult(
                inputStatus,
                zeroOrMoreNodes,
                inputGrammars,
                validateNamesResult.grammars,
                validateNamesResult.invalidNames,
                validateNamesResult.duplicatedNames,
                Set.of()
            );
        case ANCESTOR_OR_SELF, DESCENDANT_OR_SELF, FOLLOWING_OR_SELF, FOLLOWING_SIBLING_OR_SELF, PRECEDING_OR_SELF, PRECEDING_SIBLING_OR_SELF:
            return new PathOperatorResult(
                inputStatus,
                oneOrMoreNodes,
                inputGrammars,
                validateNamesResult.grammars,
                validateNamesResult.invalidNames,
                validateNamesResult.duplicatedNames,
                Set.of()
            );
        case SELF:
            return new PathOperatorResult(
                inputStatus,
                type,
                inputGrammars,
                validateNamesResult.grammars,
                validateNamesResult.invalidNames,
                validateNamesResult.duplicatedNames,
                Set.of()
            );
        }
        return null;//unreachable
    }

    private PathOperatorResult getResult_SingleGrammar_InputElements_One_NoAnalysis_Wildcard(
        final InputStatus inputStatus,
        final XQuerySequenceType type,
        final XQueryAxis axis,
        final ValidateNamesResult validateNamesResult,
        final Map<String, GrammarStatus> inputGrammars
    )
    {
        final var elementGrammars = validateNamesResult.grammars;
        switch(axis) {
        case ANCESTOR, CHILD, DESCENDANT, FOLLOWING, FOLLOWING_SIBLING, PRECEDING, PRECEDING_SIBLING:
            return new PathOperatorResult(
                inputStatus,
                zeroOrMoreNodes,
                inputGrammars,
                elementGrammars,
                validateNamesResult.invalidNames,
                validateNamesResult.duplicatedNames,
                Set.of()
            );
        case ANCESTOR_OR_SELF, DESCENDANT_OR_SELF, FOLLOWING_OR_SELF, FOLLOWING_SIBLING_OR_SELF, PRECEDING_OR_SELF, PRECEDING_SIBLING_OR_SELF:
            return new PathOperatorResult(
                inputStatus,
                oneOrMoreNodes,
                inputGrammars,
                elementGrammars,
                validateNamesResult.invalidNames,
                validateNamesResult.duplicatedNames,
                Set.of()
            );
        case PARENT:
            return new PathOperatorResult(
                inputStatus,
                zeroOrOneNode,
                inputGrammars,
                elementGrammars,
                validateNamesResult.invalidNames,
                validateNamesResult.duplicatedNames,
                Set.of()
            );
        case SELF:
            return new PathOperatorResult(
                inputStatus,
                type,
                inputGrammars,
                elementGrammars,
                validateNamesResult.invalidNames,
                validateNamesResult.duplicatedNames,
                Set.of()
            );
        }
        return null; // unreachable
    }

    private GrammarStatus getGrammarStatus(final String grammar)
    {
        GrammarStatus grammarStatus = null;
        if (!("".equals(grammar))) {
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
        final List<String> axisElementNames,
        final NamespaceResolver namespaceResolver
    ) {
        if (axisElementNames == null) {
            return new ValidateNamesResult(
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of(),
                Map.of());
        }
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

    private final PathOperatorResult zeroResult;

}
    // private final Predicate<String> canBeTokenName = Pattern.compile("^[\\p{IsUppercase}].*").asPredicate();