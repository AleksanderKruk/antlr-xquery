
package com.github.akruk.antlrquery.typesystem.typeoperations.itemtype;

import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver;
import com.github.akruk.antlrquery.typesystem.RecordField;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.typeoperations.Types;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Ranges;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.ItemTypes;
import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.*;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@DefaultQualifier(NonNull.class)
public class ItemTypeIntersection
{


    /**
     * Creates itemtype that is intersection of given items
     * e.g.
     * number | string -> empty
     * element(a|b) | element(b|c) -> element(b)
     * @param typeFactory factory creating new item type
     * @param types item types to be intersected
     * @return a new item type that is an intersection of the input types
     */
    public static @Nullable AntlrQueryItemType intersection(AntlrQueryTypeFactory typeFactory, AntlrQueryItemType... types) {
        // Flatten all item types
        Map<Class<? extends AntlrQueryItemType>, List<AntlrQueryItemType>> itemTypeToInstances =
                Arrays.stream(types).parallel()
                        .flatMap(it -> {
                            if (it instanceof ChoiceItemType(ConcreteItemType[] itemTypes)) {
                                return Arrays.stream(itemTypes);
                            } else {
                                return Stream.of(it);
                            }
                        })
                        .collect(Collectors.groupingBy(AntlrQueryItemType::getClass));

        if (!itemTypeToInstances.getOrDefault(NeverType.class, List.of()).isEmpty()) {
            return null;
        }
        if (!itemTypeToInstances.getOrDefault(NothingType.class, List.of()).isEmpty()) {
            return typeFactory.itemNothing();
        }
        final int allNonChoiceItems = itemTypeToInstances.values().parallelStream().mapToInt(List::size).sum();
        List<AntlrQueryItemType> results = new ArrayList<>(allNonChoiceItems);

        // Intersect booleans
        List<AntlrQueryItemType> falses = itemTypeToInstances.getOrDefault(BooleanType.False.class, List.of());
        List<AntlrQueryItemType> trues = itemTypeToInstances.getOrDefault(BooleanType.True.class, List.of());
        List<AntlrQueryItemType> booleans = itemTypeToInstances.getOrDefault(BooleanType.Boolean.class, List.of());

        if (falses.isEmpty() || trues.isEmpty()) {
            if (!falses.isEmpty()) {
                results.add(typeFactory.itemFalse());
            } else if (!trues.isEmpty()) {
                results.add(typeFactory.itemTrue());
            } else if (!booleans.isEmpty()) {
                results.add(typeFactory.itemBoolean());
            }
        } // else False ^^ True = empty set

        // Intersect strings
        List<AntlrQueryItemType> strings = itemTypeToInstances.getOrDefault(StringType.StringNonEnum.class, List.of());
        List<AntlrQueryItemType> enums = itemTypeToInstances.getOrDefault(StringType.StringEnum.class, List.of());
        @Nullable AntlrQueryItemType stringResult = stringIntersectionType(typeFactory, strings, enums);
        if (stringResult != null) {
            results.add(stringResult);
        }

        // Intersect numbers
        List<AntlrQueryItemType> numbers = itemTypeToInstances.getOrDefault(AtomicType.NumberType.class, List.of());
        @Nullable AntlrQueryItemType numberResult = numberIntersectionType(typeFactory, numbers);
        if (numberResult != null) {
            results.add(numberResult);
        }

        // Intersect regexes
        List<AntlrQueryItemType> regexes = itemTypeToInstances.getOrDefault(AtomicType.RegexType.class, List.of());
        @Nullable AntlrQueryItemType regexResult = regexIntersectionType(regexes);
        if (regexResult != null) {
            results.add(regexResult);
        }

        // Intersect records and maps together
        List<AntlrQueryItemType> records = itemTypeToInstances.getOrDefault(MapLikeType.RecordType.class, List.of());
        List<AntlrQueryItemType> extRecords = itemTypeToInstances.getOrDefault(MapLikeType.ExtensibleRecordType.class, List.of());
        List<AntlrQueryItemType> maps = itemTypeToInstances.getOrDefault(MapLikeType.MapType.class, List.of());

        @Nullable AntlrQueryItemType recordOrMapResult = recordAndMapIntersectionType(typeFactory, records, extRecords, maps);
        if (recordOrMapResult != null) {
            results.add(recordOrMapResult);
        }

        // Intersect arrays and tuples together
        List<AntlrQueryItemType> arrays = itemTypeToInstances.getOrDefault(ArrayLikeType.ArrayType.class, List.of());
        List<AntlrQueryItemType> tuples = itemTypeToInstances.getOrDefault(ArrayLikeType.TupleType.class, List.of());
        @Nullable AntlrQueryItemType arrayResult = arrayAndTupleIntersectionType(typeFactory, arrays, tuples);
        if (arrayResult != null) {
            results.add(arrayResult);
        }

        // Intersect TreeNodes together
        List<AntlrQueryItemType> nodesFromGrammar = itemTypeToInstances.getOrDefault(TreeNodeType.NodeType.class, List.of());
        List<AntlrQueryItemType> anyNodes = itemTypeToInstances.getOrDefault(TreeNodeType.AnyNode.class, List.of());
        List<AntlrQueryItemType> anyNodesFromGrammar = itemTypeToInstances.getOrDefault(TreeNodeType.AnyNodeFromGrammar.class, List.of());

        List<AntlrQueryItemType> rulesFromGrammar = itemTypeToInstances.getOrDefault(TreeRuleType.RuleType.class, List.of());
        List<AntlrQueryItemType> anyRules = itemTypeToInstances.getOrDefault(TreeRuleType.AnyRule.class, List.of());
        List<AntlrQueryItemType> anyRulesFromGrammar = itemTypeToInstances.getOrDefault(TreeRuleType.AnyRuleFromGrammar.class, List.of());

        List<AntlrQueryItemType> tokensFromGrammar = itemTypeToInstances.getOrDefault(TreeTokenType.TokenType.class, List.of());
        List<AntlrQueryItemType> anyTokens = itemTypeToInstances.getOrDefault(TreeTokenType.AnyToken.class, List.of());
        List<AntlrQueryItemType> anyTokensFromGrammar = itemTypeToInstances.getOrDefault(TreeTokenType.AnyTokenFromGrammar.class, List.of());

        @Nullable AntlrQueryItemType treeNodeResult = treeNodesIntersectionType(
                typeFactory,
                nodesFromGrammar,
                anyNodes,
                anyNodesFromGrammar,
                rulesFromGrammar,
                anyRules,
                anyRulesFromGrammar,
                tokensFromGrammar,
                anyTokens,
                anyTokensFromGrammar
        );
        if (treeNodeResult != null) {
            results.add(treeNodeResult);
        }

        // Intersect Functions
        List<AntlrQueryItemType> anyFunctions = itemTypeToInstances.getOrDefault(FunctionType.AnyFunction.class, List.of());
        List<AntlrQueryItemType> functions = itemTypeToInstances.getOrDefault(FunctionType.ConstrainedFunction.class, List.of());
        @Nullable AntlrQueryItemType functionResult = functionIntersectionType(typeFactory, anyFunctions, functions);
        if (functionResult != null) {
            results.add(functionResult);
        }

        // Intersect Grammar Entities (Since they have no constraints, intersecting identical instances yields the same instance)
        if (!itemTypeToInstances.getOrDefault(GrammarEntityType.GrammarType.class, List.of()).isEmpty()) {
            results.add(new GrammarEntityType.GrammarType());
        }
        if (!itemTypeToInstances.getOrDefault(GrammarEntityType.GrammarRuleType.class, List.of()).isEmpty()) {
            results.add(new GrammarEntityType.GrammarRuleType());
        }
        if (!itemTypeToInstances.getOrDefault(GrammarEntityType.GrammarTokenType.class, List.of()).isEmpty()) {
            results.add(new GrammarEntityType.GrammarTokenType());
        }

        // Fallback: If no explicit constraints were collected, but AnyItemType was present, return AnyType.
        // AnyType intersected with another constraint simply vanishes since the other constraint is strictly narrower.
        if (results.isEmpty() && !itemTypeToInstances.getOrDefault(AnyItemType.class, List.of()).isEmpty()) {
            return AnyItemType.ANY_TYPE;
        }

        return switch (results.size()) {
            case 0 -> typeFactory.itemNothing();
            case 1 -> results.getFirst();
            default -> typeFactory.itemChoice(results.toArray(AntlrQueryItemType[]::new));
        };
    }


    private static @Nullable AntlrQueryItemType regexIntersectionType(List<AntlrQueryItemType> regexes) {
        if (regexes.isEmpty()) {
            return null;
        }

        Pattern firstPattern = ((AtomicType.RegexType) regexes.getFirst()).pattern();
        String patternStr = firstPattern.pattern();

        // Exact pattern match constraint. Differentiated Regex types yield an empty set.
        for (int i = 1; i < regexes.size(); i++) {
            if (!((AtomicType.RegexType) regexes.get(i)).pattern().pattern().equals(patternStr)) {
                return null;
            }
        }

        return new AtomicType.RegexType(firstPattern);
    }

    private static @Nullable AntlrQueryItemType treeNodesIntersectionType(
            AntlrQueryTypeFactory typeFactory,
            List<AntlrQueryItemType> nodesFromGrammar,
            List<AntlrQueryItemType> anyNodes,
            List<AntlrQueryItemType> anyNodesFromGrammar,
            List<AntlrQueryItemType> rulesFromGrammar,
            List<AntlrQueryItemType> anyRules,
            List<AntlrQueryItemType> anyRulesFromGrammar,
            List<AntlrQueryItemType> tokensFromGrammar,
            List<AntlrQueryItemType> anyTokens,
            List<AntlrQueryItemType> anyTokensFromGrammar)
    {
        if (nodesFromGrammar.isEmpty()
                && anyNodes.isEmpty()
                && anyNodesFromGrammar.isEmpty()
                && rulesFromGrammar.isEmpty()
                && anyRules.isEmpty()
                && anyRulesFromGrammar.isEmpty()
                && tokensFromGrammar.isEmpty()
                && anyTokens.isEmpty()
                && anyTokensFromGrammar.isEmpty()) {
            return null;
        }

        final boolean ruleRestricted =
                        !rulesFromGrammar.isEmpty()
                        || !anyRules.isEmpty()
                        || !anyRulesFromGrammar.isEmpty();

        final boolean tokenRestricted =
                        !tokensFromGrammar.isEmpty()
                        || !anyTokens.isEmpty()
                        || !anyTokensFromGrammar.isEmpty();

        if (ruleRestricted && tokenRestricted) {
            return null;
        }
        if (ruleRestricted) {
            return getNodeTypeIntersection(typeFactory,
                    rulesFromGrammar,
                    anyRules,
                    anyRulesFromGrammar,
                    typeFactory::itemRulesFromGrammar,
                    typeFactory::grammarRules
            );
        }
        if (tokenRestricted) {
            return getNodeTypeIntersection(typeFactory,
                    tokensFromGrammar,
                    anyTokens,
                    anyTokensFromGrammar,
                    typeFactory::itemTokensFromGrammar,
                    typeFactory::grammarTokens
            );
        }
        return getNodeTypeIntersection(typeFactory,
                nodesFromGrammar,
                anyNodes,
                anyNodesFromGrammar,
                typeFactory::itemNodesFromGrammar,
                typeFactory::grammarNodes
        );

    }


    private static
    @Nullable AntlrQueryItemType getNodeTypeIntersection(
            AntlrQueryTypeFactory typeFactory,
            List<AntlrQueryItemType> rulesFromGrammar,
            List<AntlrQueryItemType> anyRules,
            List<AntlrQueryItemType> anyRulesFromGrammar,
            BiFunction<String, Set<NamespaceResolver.QualifiedName>,AntlrQueryItemType> constrainedTypeFactory,
            Function<String, Set<NamespaceResolver.QualifiedName>> typesFromGrammarGetter
            )
    {
        if (rulesFromGrammar.size() == 1) {
            return rulesFromGrammar.getFirst();
        }
        var grammarToRules = rulesFromGrammar.stream()
                .map(obj -> (NamesConstrained&GrammarConstrained) obj)
                .collect(Collectors.groupingBy(GrammarConstrained::grammar))
            ;
        if (!grammarToRules.isEmpty())  {
            var grammar = grammarToRules.keySet().stream().findFirst().get();
            var els = grammarToRules.get(grammar)
                    .stream()
                    .map(NamesConstrained::elementNames)
                    .reduce((qualifiedNames, qualifiedNames2) -> {
                         var new_ = new HashSet<>(qualifiedNames);
                         new_.retainAll(qualifiedNames2);
                         return new_;
                    }).get();
            if (els.isEmpty()) {
                return null;
            }
            return constrainedTypeFactory.apply(grammar, els);
        }
        if (anyRulesFromGrammar.size() == 1) {
            return anyRulesFromGrammar.getFirst();
        }
        var grammarToAnyRules = anyRulesFromGrammar.stream()
                .map(obj -> (GrammarConstrained) obj)
                .collect(Collectors.groupingBy(GrammarConstrained::grammar))
                ;
        if (!grammarToAnyRules.isEmpty())  {
            var grammar = grammarToAnyRules.keySet().stream().findFirst().get();
            var els = grammarToAnyRules.get(grammar)
                    .stream()
                    .map(anyRuleFromGrammar -> typesFromGrammarGetter.apply(grammar) )
                    .reduce((qualifiedNames, qualifiedNames2) -> {
                        qualifiedNames.retainAll(qualifiedNames2);
                        return qualifiedNames;
                    }).get();
            if (els.isEmpty()) {
                return null;
            }
            return constrainedTypeFactory.apply(grammar, els);
        }
        if (anyRules.isEmpty()) {
            return null;
        }
        return anyRules.getFirst();
    }

    private static @Nullable AntlrQueryItemType functionIntersectionType(
            AntlrQueryTypeFactory typeFactory,
            List<AntlrQueryItemType> anyFunctions, List<AntlrQueryItemType> functions)
    {
        if (functions.isEmpty() && anyFunctions.isEmpty()) {
            return null;
        }
        if (functions.isEmpty()) {
            return typeFactory.itemAnyFunction();
        }

        // Functions must share exact arity to have an overlapping signature
        int arity = ((FunctionType.ConstrainedFunction) functions.getFirst()).argumentTypes().size();
        for (AntlrQueryItemType f : functions) {
            if (((FunctionType.ConstrainedFunction) f).argumentTypes().size() != arity) {
                return null;
            }
        }

        // An intersected function accepts the intersection of all arguments
        List<AntlrQuerySequenceType> mergedArgs = new ArrayList<>(arity);
        for (int i = 0; i < arity; i++) {
            List<AntlrQuerySequenceType> argCandidates = new ArrayList<>();
            for (AntlrQueryItemType f : functions) {
                argCandidates.add(((FunctionType.ConstrainedFunction) f).argumentTypes().get(i));
            }

            AntlrQuerySequenceType mergedArgSeq = Types.intersection(
                    typeFactory,
                    argCandidates.toArray(AntlrQuerySequenceType[]::new)
            );

            if (mergedArgSeq.itemType() instanceof NeverType) {
                return null;
            }
            mergedArgs.add(mergedArgSeq);
        }

        // An intersected function guarantees the intersection of all return types
        List<AntlrQuerySequenceType> retCandidates = new ArrayList<>();
        for (AntlrQueryItemType f : functions) {
            retCandidates.add(((FunctionType.ConstrainedFunction) f).returnType());
        }

        AntlrQuerySequenceType mergedRetSeq = Types.intersection(
                typeFactory,
                retCandidates.toArray(AntlrQuerySequenceType[]::new)
        );

        if (mergedRetSeq.itemType() instanceof NeverType) {
            return null;
        }

        return new FunctionType.ConstrainedFunction(mergedArgs, mergedRetSeq);
    }


    private static @Nullable AntlrQueryItemType stringIntersectionType(
            AntlrQueryTypeFactory typeFactory,
            List<AntlrQueryItemType> strings,
            List<AntlrQueryItemType> enums)
    {
        if (strings.isEmpty() && enums.isEmpty()) {
            return null;
        }

        Cardinality[] allCardinalities = Stream.of(strings, enums)
                .flatMap(List::stream)
                .map(i -> ((StringType) i).cardinality())
                .toArray(Cardinality[]::new);

        @Nullable Cardinality mergedStringCardinality = Cardinalities.intersection(allCardinalities);
        if (mergedStringCardinality == null) {
            return null;
        }
        if (enums.isEmpty()) {
            return typeFactory.itemString(mergedStringCardinality);
        }
        // Enums

        Set<String> validEnumMembers = enums.stream()
                .flatMap(i->((StringType.StringEnum) i).members().stream())
                .filter(enumMember->
                        (Cardinalities.isSubSet(Cardinality.of(enumMember.length()), mergedStringCardinality))
                )
                .collect(Collectors.toSet());

        Set<String> finalMembers = validEnumMembers.stream()
                .filter(enumMember -> Cardinalities.isSubSet(
                        Cardinality.of(enumMember.length()),
                        mergedStringCardinality))
                .collect(Collectors.toSet());

        return finalMembers.isEmpty() ? null : typeFactory.itemEnum(finalMembers);
    }

    private static @Nullable AntlrQueryItemType numberIntersectionType(
            AntlrQueryTypeFactory typeFactory,
            List<AntlrQueryItemType> numbers)
    {
        if (numbers.isEmpty()) {
            return null;
        }

        NumericRange[] allRanges = numbers.stream()
                .map(i -> ((AtomicType.NumberType) i).range())
                .toArray(NumericRange[]::new);

        @Nullable NumericRange mergedRange = Ranges.intersection(allRanges);
        if (mergedRange == null) { // empty set
            return null;
        }
        return typeFactory.itemNumber(mergedRange);
    }

    private static @Nullable AntlrQueryItemType recordAndMapIntersectionType(
            AntlrQueryTypeFactory typeFactory,
            List<AntlrQueryItemType> records,
            List<AntlrQueryItemType> extRecords,
            List<AntlrQueryItemType> maps)
    {
        if (records.isEmpty() && extRecords.isEmpty() && maps.isEmpty()) {
            return null;
        }

        // Case 1: Pure maps (no structural records provided)
        if (records.isEmpty() && extRecords.isEmpty()) {
            AntlrQueryItemType[] keyTypes = maps.stream()
                    .map(m -> ((MapLikeType.MapType) m).keyType())
                    .toArray(AntlrQueryItemType[]::new);

            @Nullable AntlrQueryItemType mergedKey = intersection(typeFactory, keyTypes);
            if (mergedKey == null) {
                return null;
            }

            AntlrQuerySequenceType[] valueTypes = maps.stream()
                    .map(m -> ((MapLikeType.MapType) m).valueType())
                    .toArray(AntlrQuerySequenceType[]::new);

            AntlrQuerySequenceType mergedValueSeq = Types.intersection(typeFactory, valueTypes);
            if (mergedValueSeq.itemType() instanceof NeverType) {
                return null;
            }

            return new MapLikeType.MapType(mergedKey, mergedValueSeq);
        }

        // Case 2: Record and Map unification
        // Collect all explicit field keys declared across all records
        Set<String> allKeys = new LinkedHashSet<>();
        for (AntlrQueryItemType rec : records) {
            allKeys.addAll(((MapLikeType.RecordType) rec).fields().keySet());
        }
        for (AntlrQueryItemType ext : extRecords) {
            allKeys.addAll(((MapLikeType.ExtensibleRecordType) ext).fields().keySet());
        }

        // Collect all map value constraints
        AntlrQuerySequenceType[] mapValueTypes = maps.stream()
                .map(m -> ((MapLikeType.MapType) m).valueType())
                .toArray(AntlrQuerySequenceType[]::new);

        // Validate all field keys against all map key constraints
        for (AntlrQueryItemType m : maps) {
            MapLikeType.MapType map = (MapLikeType.MapType) m;

            for (String key : allKeys) {
                @Nullable AntlrQueryItemType keyIntersection = ItemTypes.intersection(
                        typeFactory,
                        map.keyType(),
                        typeFactory.itemEnum(Set.of(key))
                );
                if (keyIntersection == null || keyIntersection instanceof NeverType) {
                    return null;
                }
            }
        }

        LinkedHashMap<String, RecordField> mergedFields = new LinkedHashMap<>();

        // Collect and intersect types for each field across all records and maps
        for (String key : allKeys) {
            List<AntlrQuerySequenceType> fieldSequenceCandidates = new ArrayList<>();
            boolean isRequired = false;

            // Gather candidate types from closed records
            for (AntlrQueryItemType rec : records) {
                RecordField field = ((MapLikeType.RecordType) rec).fields().get(key);
                if (field == null) {
                    return null;
                }
                fieldSequenceCandidates.add(field.resolveFieldType(typeFactory));
                isRequired |= field.isRequired();
            }

            // Gather candidate types from extensible records
            for (AntlrQueryItemType ext : extRecords) {
                MapLikeType.ExtensibleRecordType extRec = (MapLikeType.ExtensibleRecordType) ext;
                RecordField field = extRec.fields().get(key);

                if (field != null) {
                    fieldSequenceCandidates.add(field.resolveFieldType(typeFactory));
                    isRequired |= field.isRequired();
                } else {
                    fieldSequenceCandidates.add(extRec.additionalFieldType());
                }
            }

            // Append all map value constraints to the field candidates
            Collections.addAll(fieldSequenceCandidates, mapValueTypes);

            // Bulk intersect all collected field candidates using Types.intersection
            AntlrQuerySequenceType mergedFieldSeq = Types.intersection(
                    typeFactory,
                    fieldSequenceCandidates.toArray(AntlrQuerySequenceType[]::new)
            );

            if (mergedFieldSeq.itemType() instanceof NeverType) {
                return null;
            }

            if (mergedFieldSeq.itemType() instanceof NothingType) {
                mergedFieldSeq = typeFactory.emptySequence();
            }

            mergedFields.put(key, new RecordField(
                    key,
                    new RecordField.TypeOrReference.Type(mergedFieldSeq),
                    isRequired
            ));
        }

        // Closed records always result in a closed RecordType
        if (!records.isEmpty()) {
            return new MapLikeType.RecordType(mergedFields);
        }

        // Bulk intersect additional field types for extensible records along with map value constraints
        List<AntlrQuerySequenceType> additionalCandidates = new ArrayList<>();
        for (AntlrQueryItemType ext : extRecords) {
            additionalCandidates.add(((MapLikeType.ExtensibleRecordType) ext).additionalFieldType());
        }
        Collections.addAll(additionalCandidates, mapValueTypes);

        AntlrQuerySequenceType mergedAdditional = Types.intersection(
                typeFactory,
                additionalCandidates.toArray(AntlrQuerySequenceType[]::new)
        );

        if (mergedAdditional.itemType() instanceof NothingType || mergedAdditional.itemType() instanceof NeverType) {
            return new MapLikeType.RecordType(mergedFields);
        }

        return new MapLikeType.ExtensibleRecordType(mergedFields, mergedAdditional);
    }

    private static @Nullable AntlrQueryItemType arrayAndTupleIntersectionType(
            AntlrQueryTypeFactory typeFactory,
            List<AntlrQueryItemType> arrays,
            List<AntlrQueryItemType> tuples)
    {
        if (arrays.isEmpty() && tuples.isEmpty()) {
            return null;
        }

        // Pure arrays (no structural tuples provided)
        if (tuples.isEmpty()) {
            return arrays.stream()
                    .map(ArrayLikeType.ArrayType.class::cast)
                    .collect(Collectors.teeing(
                            Collectors.mapping(ArrayLikeType.ArrayType::memberType, Collectors.toList()),
                            Collectors.mapping(ArrayLikeType.ArrayType::cardinality, Collectors.toList()),
                            (antlrQuerySequenceTypes, cardinalities) -> {
                                var type = Types.intersection(typeFactory, antlrQuerySequenceTypes.toArray(AntlrQuerySequenceType[]::new));
                                if (type.itemType() instanceof  NeverType) {
                                    return type.itemType();
                                }
                                @Nullable Cardinality card = Cardinalities.intersection(cardinalities.toArray(Cardinality[]::new));
                                if (card == null) {
                                    return typeFactory.itemTuple(List.of());
                                }
                                return typeFactory.itemArray(type, card);
                            }
                    ));
        }

        // Tuple and Array unification
        // Tuples can only be intersected if they share the exact same length
        int expectedSize = ((ArrayLikeType.TupleType) tuples.getFirst()).members().length;
        for (AntlrQueryItemType t : tuples) {
            if (((ArrayLikeType.TupleType) t).members().length != expectedSize) {
                return null; // Size mismatch implies an empty intersection
            }
        }

        // Collect all array element constraints
        AntlrQuerySequenceType[] arrayElementTypes = arrays.stream()
                .map(a -> ((ArrayLikeType.ArrayType) a).memberType())
                .toArray(AntlrQuerySequenceType[]::new);

        List<AntlrQuerySequenceType> mergedElements = new ArrayList<>(expectedSize);

        for (int i = 0; i < expectedSize; i++) {
            List<AntlrQuerySequenceType> elementCandidates = new ArrayList<>();

            // Gather candidate types from tuples at index i
            for (AntlrQueryItemType t : tuples) {
                elementCandidates.add(((ArrayLikeType.TupleType) t).members()[i]);
            }

            // Append all array element constraints to the candidates
            Collections.addAll(elementCandidates, arrayElementTypes);

            // Bulk intersect all collected candidates for the current index
            AntlrQuerySequenceType mergedElementSeq = Types.intersection(
                    typeFactory,
                    elementCandidates.toArray(AntlrQuerySequenceType[]::new)
            );

            if (mergedElementSeq.itemType() instanceof NeverType) {
                return null;
            }

            if (mergedElementSeq.itemType() instanceof NothingType) {
                mergedElementSeq = typeFactory.emptySequence();
            }

            mergedElements.add(mergedElementSeq);
        }

        return typeFactory.itemTuple(mergedElements);
    }


}
