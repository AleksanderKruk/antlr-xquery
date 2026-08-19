
package com.github.akruk.antlrquery.typesystem.typeoperations.itemtype;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.akruk.antlrquery.typesystem.RecordField;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Ranges;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.ItemTypes;
import com.github.akruk.antlrquery.typesystem.typeoperations.Types;
import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.*;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.com.google.common.collect.Streams;
import org.checkerframework.framework.qual.DefaultQualifier;


@DefaultQualifier(NonNull.class)
public class ItemTypeUnion
{

    private final AntlrQueryTypeFactory typeFactory;

    /**
     * Merges two item types into one union item type.
     * e.g.
     * number, string -> (number | string)
     * element(a), element(b) -> element(a | b)
     * @param types item types that need to be unionized
     * @return a new item type that is an alternative of the two input types
     */
    public AntlrQueryItemType union(final AntlrQueryItemType... types)
    {
        assert types.length != 0 : "Union of zero elements";
        if (types.length == 1) return types[0];

        var classToItems = Arrays.stream(types)
                .flatMap((AntlrQueryItemType antlrQueryItemType) -> {
                    if (antlrQueryItemType instanceof ChoiceItemType(ConcreteItemType[] itemTypes)) {
                        return Arrays.stream(itemTypes);
                    }
                    return Stream.of(antlrQueryItemType);
                })
                .collect(Collectors.groupingBy(Object::getClass, Collectors.toSet()));

        if (classToItems.get(AnyItemType.class) != null) {
            return typeFactory.itemAnyItem();
        }

        List<AntlrQueryItemType> itemTypes = new ArrayList<>();
        itemTypes.addAll(booleanUnion(classToItems));
        itemTypes.addAll(numberTypeUnion(classToItems));
        itemTypes.addAll(stringTypeUnion(classToItems));
        itemTypes.addAll(mapLikeTypeUnion(classToItems));
        itemTypes.addAll(arrayLikeTypeUnion(classToItems));
        itemTypes.addAll(functionTypeUnion(classToItems));
        itemTypes.addAll(treeNodeTypeUnion(classToItems));




        if (!itemTypes.isEmpty()) {
            return typeFactory.itemChoice(itemTypes.toArray(AntlrQueryItemType[]::new));
        }
        Set<AntlrQueryItemType> nothings = classToItems.getOrDefault(NothingType.class, Set.of());
        Set<AntlrQueryItemType> neverTypes = classToItems.getOrDefault(NeverType.class, Set.of());
        if (!nothings.isEmpty()) {
            return typeFactory.itemNothing();
        }
        assert !neverTypes.isEmpty() : "Missed type mapping  " + classToItems;
        return typeFactory.neverType().itemType();
    }

    private List<AntlrQueryItemType> treeNodeTypeUnion(
            Map<? extends Class<?>, Set<AntlrQueryItemType>> classToItems)
    {
        Set<AntlrQueryItemType> anyNodes = classToItems.getOrDefault(TreeNodeType.AnyNode.class, Set.of());
        if (!anyNodes.isEmpty()){
            return List.of(typeFactory.itemAnyNode());
        }

        Set<AntlrQueryItemType> anyTokens = classToItems.getOrDefault(TreeTokenType.AnyToken.class, Set.of());
        Set<AntlrQueryItemType> anyRules = classToItems.getOrDefault(TreeRuleType.AnyRule.class, Set.of());
        if (!anyTokens.isEmpty() && !anyRules.isEmpty()) {
            return List.of(typeFactory.itemAnyNode());
        }
        ArrayList<AntlrQueryItemType> results = new ArrayList<>();
        boolean  allTokens = !anyTokens.isEmpty();
        if (allTokens) {
            results.add(anyTokens.stream().findFirst().get());
        }
        boolean allRules = !anyRules.isEmpty();
        if (allRules) {
            results.add(anyRules.stream().findFirst().get());
        }
        Set<AntlrQueryItemType> anyNodesFromGrammar = classToItems.getOrDefault(TreeNodeType.AnyNodeFromGrammar.class, Set.of());
        Set<AntlrQueryItemType> nodesFromGrammar = classToItems.getOrDefault(TreeNodeType.NodeType.class, Set.of());
        Set<AntlrQueryItemType> anyTokensFromGrammar = classToItems.getOrDefault(TreeTokenType.AnyTokenFromGrammar.class, Set.of());
        Set<AntlrQueryItemType> tokensFromGrammar = classToItems.getOrDefault(TreeTokenType.TokenType.class, Set.of());
        Set<AntlrQueryItemType> anyRulesFromGrammar = classToItems.getOrDefault(TreeRuleType.AnyRuleFromGrammar.class, Set.of());
        Set<AntlrQueryItemType> rulesFromGrammar = classToItems.getOrDefault(TreeRuleType.RuleType.class, Set.of());

        var grammarToAnyNodes = anyNodesFromGrammar.stream()
                .map(TreeNodeType.AnyNodeFromGrammar.class::cast)
                .collect(Collectors.groupingBy(TreeNodeType.AnyNodeFromGrammar::grammar))
                ;
        var grammarToAnyRules = anyRulesFromGrammar.stream()
                .map(TreeRuleType.AnyRuleFromGrammar.class::cast)
                .collect(Collectors.groupingBy(TreeRuleType.AnyRuleFromGrammar::grammar))
                ;
        var grammarToAnyTokens = anyTokensFromGrammar.stream()
                .map(TreeTokenType.AnyTokenFromGrammar.class::cast)
                .collect(Collectors.groupingBy(TreeTokenType.AnyTokenFromGrammar::grammar))
                ;

        var grammarToNodes = nodesFromGrammar.stream()
                .map(TreeNodeType.NodeType.class::cast)
                .collect(Collectors.groupingBy(TreeNodeType.NodeType::grammar))
                ;
        var grammarToRules = rulesFromGrammar.stream()
                .map(TreeRuleType.RuleType.class::cast)
                .collect(Collectors.groupingBy(TreeRuleType.RuleType::grammar))
                ;
        var grammarToTokens = tokensFromGrammar.stream()
                .map(TreeTokenType.TokenType.class::cast)
                .collect(Collectors.groupingBy(TreeTokenType.TokenType::grammar))
                ;
        HashSet<String> allGrammars = new HashSet<>(
                grammarToAnyNodes.size()
                        + grammarToAnyRules.size()
                        + grammarToAnyTokens.size()
                        + grammarToNodes.size()
                        + grammarToRules.size()
                        + grammarToTokens.size());
        allGrammars.addAll(grammarToAnyNodes.keySet());
        allGrammars.addAll(grammarToAnyRules.keySet());
        allGrammars.addAll(grammarToAnyTokens.keySet());
        allGrammars.addAll(grammarToNodes.keySet());
        allGrammars.addAll(grammarToRules.keySet());
        allGrammars.addAll(grammarToTokens.keySet());
        for (final String grammar : allGrammars) {
            boolean allRulesFromGrammar = !grammarToAnyRules.getOrDefault(grammar, List.of()).isEmpty() || allRules;
            boolean allTokensFromGrammar = !grammarToAnyTokens.getOrDefault(grammar, List.of()).isEmpty() || allTokens;
            boolean allNodesFromGrammar = !grammarToAnyNodes.getOrDefault(grammar, List.of()).isEmpty()
                    || (allRulesFromGrammar && allTokensFromGrammar);
            if (allNodesFromGrammar) {
                results.add(typeFactory.itemAnyNodeFromGrammar(grammar));
                continue;
            }
            var nodesFromGivenGrammar = grammarToNodes.getOrDefault(grammar, List.of());
            var rulesFromGivenGrammar = grammarToRules.getOrDefault(grammar, List.of());
            var tokensFromGivenGrammar = grammarToTokens.getOrDefault(grammar, List.of());

            var effectiveNodes = Streams.concat(
                        nodesFromGivenGrammar.stream(),
                        rulesFromGivenGrammar.stream(),
                        tokensFromGivenGrammar.stream()
                    )
                    .map(NamesConstrained.class::cast)
                    .map(NamesConstrained::elementNames)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet())
                    ;
            if (allRulesFromGrammar) {
                effectiveNodes.addAll(typeFactory.grammarRules(grammar));
            }
            if (allTokensFromGrammar) {
                effectiveNodes.addAll(typeFactory.grammarTokens(grammar));
            }
            if (effectiveNodes.isEmpty()) {
                continue;
            }

            boolean containsToken = effectiveNodes.stream().anyMatch(qualifiedName -> qualifiedName.name().matches("^\\p{IsUpper}.*$"));
            boolean containsRule = effectiveNodes.stream().allMatch(qualifiedName -> qualifiedName.name().matches("^\\p{IsLower}.*$"));
            if (containsToken && containsRule) {
                results.add(typeFactory.itemNodesFromGrammar(grammar, effectiveNodes));
            } else if (containsToken) {
                results.add(typeFactory.itemTokensFromGrammar(grammar, effectiveNodes));
            } else {
                results.add(typeFactory.itemRulesFromGrammar(grammar, effectiveNodes));
            }
        }
        return results;
    }

    private Collection<? extends AntlrQueryItemType> arrayLikeTypeUnion(Map<? extends Class<?>, Set<AntlrQueryItemType>> classToItems) {
        Set<AntlrQueryItemType> tuples = classToItems.getOrDefault(ArrayLikeType.TupleType.class, Set.of());
        Set<AntlrQueryItemType> arrays = classToItems.getOrDefault(ArrayLikeType.ArrayType.class, Set.of());
        if(tuples.isEmpty() && arrays.isEmpty())
            return List.of();
        if (!tuples.isEmpty() && arrays.isEmpty()) {
            var lengthToTupleMembers = tuples.stream()
                .map(ArrayLikeType.TupleType.class::cast)
                .collect(Collectors.groupingBy(tuple->tuple.members().length))
                ;
            ArrayList<AntlrQueryItemType> result = new ArrayList<>(lengthToTupleMembers.size());
            for (List<ArrayLikeType.TupleType> sameLengthTuples : lengthToTupleMembers.values()) {
                var len = sameLengthTuples.getFirst().members().length;
                var mergedMembers = new AntlrQuerySequenceType[len];
                for (int i = 0; i < len; i++) {
                    final int finalI = i;
                    mergedMembers[i] = Types.union(
                            typeFactory,
                            sameLengthTuples.stream()
                                    .map(tupleType -> tupleType.members()[finalI])
                                    .toArray(AntlrQuerySequenceType[]::new)
                    );
                }
                result.add(new ArrayLikeType.TupleType(mergedMembers));
            }
            return result;
        }
        var typedArrays = arrays.stream().map(ArrayLikeType.ArrayType.class::cast).toList();
        var typedTuples = tuples.stream().map(ArrayLikeType.TupleType.class::cast).toList();
        AntlrQuerySequenceType mergedMemberType = Types.union(
            typeFactory,
            Streams.concat(typedArrays.stream(), typedTuples.stream())
                    .map(t -> Types.getMemberType(typeFactory, t))
                    .filter(Objects::nonNull)
                    .toArray(AntlrQuerySequenceType[]::new)
        );
        Cardinality mergedCardinality = Cardinalities.union(
            Streams.concat(typedArrays.stream(), typedTuples.stream())
                    .map(ArrayLikeType::cardinality)
                    .toArray(Cardinality[]::new)
        );
        return List.of(new ArrayLikeType.ArrayType(mergedMemberType, mergedCardinality));
    }

    private List<AntlrQueryItemType> functionTypeUnion(Map<? extends Class<?>, Set<AntlrQueryItemType>> classToItems) {
        Set<AntlrQueryItemType> anyFunctions = classToItems.getOrDefault(FunctionType.AnyFunction.class, Set.of());
        if (!anyFunctions.isEmpty())  {
            return List.of(typeFactory.itemAnyFunction());
        }
        var constrainedFunctions = classToItems.getOrDefault(FunctionType.ConstrainedFunction.class, Set.of());
        if (constrainedFunctions.isEmpty()) {
            return List.of();
        }
        Iterator<AntlrQueryItemType> i = constrainedFunctions.iterator();
        do  {
            var f1 = i.next();
            if (!constrainedFunctions.contains(f1)) {
                continue;
            }
            constrainedFunctions = constrainedFunctions.stream()
                    .filter(f2-> f1 == f2 || !ItemTypes.isSubtype(typeFactory, f2, f1))
                    .collect(Collectors.toUnmodifiableSet())
            ;
        } while(i.hasNext());
        return constrainedFunctions.stream().toList();
    }

    private List<AntlrQueryItemType> mapLikeTypeUnion(
            final Map<? extends Class<?>, Set<AntlrQueryItemType>> classToItems)
    {
        final Set<AntlrQueryItemType> maps =
                classToItems.getOrDefault(MapLikeType.MapType.class, Set.of());

        final Set<AntlrQueryItemType> records =
                classToItems.getOrDefault(MapLikeType.RecordType.class, Set.of());

        final Set<AntlrQueryItemType> extensibleRecords =
                classToItems.getOrDefault(MapLikeType.ExtensibleRecordType.class, Set.of());

        if (maps.isEmpty() && records.isEmpty() && extensibleRecords.isEmpty())
            return List.of();

        /*
         * A map absorbs all map-like types.
         *
         * The precise record structure cannot be represented by MapType,
         * therefore only the actual maps participate in the map union.
         */
        if (!maps.isEmpty()) {
            @Nullable AntlrQueryItemType keyType = null;
            @Nullable AntlrQuerySequenceType valueType = null;

            for (final AntlrQueryItemType item : maps) {
                final MapLikeType.MapType map = (MapLikeType.MapType) item;

                keyType = keyType == null
                        ? map.keyType()
                        : ItemTypes.union(typeFactory, keyType, map.keyType());

                valueType = valueType == null
                        ? map.valueType()
                        : Types.union(typeFactory, valueType, map.valueType());
            }

            return List.of(new MapLikeType.MapType(keyType, valueType));
        }

        /*
         * No maps: merge all records into one record.
         */
        final LinkedHashMap<String, RecordField> mergedFields = new LinkedHashMap<>();

        @Nullable AntlrQuerySequenceType additionalFieldType = null;
        boolean extensible = false;

        /*
         * We process normal records first and extensible records afterwards.
         * LinkedHashMap guarantees that the first occurrence of a key
         * determines its position.
         */
        for (final AntlrQueryItemType item : records) {
            final MapLikeType.RecordType record =
                    (MapLikeType.RecordType) item;

            mergeRecordFields(mergedFields, record.fields());
        }

        for (final AntlrQueryItemType item : extensibleRecords) {
            final MapLikeType.ExtensibleRecordType record =
                    (MapLikeType.ExtensibleRecordType) item;

            extensible = true;

            mergeRecordFields(mergedFields, record.fields());

            additionalFieldType = additionalFieldType == null
                    ? record.additionalFieldType()
                    : Types.union(typeFactory, additionalFieldType, record.additionalFieldType());
        }

        if (extensible) {
            return List.of(new MapLikeType.ExtensibleRecordType(
                    mergedFields,
                    additionalFieldType
            ));
        }

        return List.of(new MapLikeType.RecordType(mergedFields));
    }

    private void mergeRecordFields(
            final LinkedHashMap<String, RecordField> target,
            final Map<String, RecordField> source)
    {
        for (final var entry : source.entrySet()) {
            final String key = entry.getKey();
            final RecordField sourceField = entry.getValue();

            final RecordField targetField = target.get(key);

            if (targetField == null) {
                /*
                 * The field does not occur in the other record, therefore
                 * it must be optional in the union.
                 */
                target.put(key,
                        new RecordField(sourceField.name(), sourceField.typeOrReference(), sourceField.isRequired())
                        );
                continue;
            }

            target.put(key, unionFields(targetField, sourceField));
        }
    }

    private RecordField unionFields(
            final RecordField left,
            final RecordField right)
    {
        return new RecordField(
                left.name(),
                new RecordField.TypeOrReference.Type(Types.union(typeFactory, left.resolveFieldType(typeFactory), right.resolveFieldType(typeFactory))),
                left.isRequired() && right.isRequired()
        );
    }



    private List<AntlrQueryItemType> numberTypeUnion(Map<? extends Class<?>, Set<AntlrQueryItemType>> classToItems) {
        var numbers = classToItems.getOrDefault(NumberType.class, Set.of());
        if (!numbers.isEmpty()) {
            return List.of(typeFactory.itemNumber(
                    Ranges.union(
                            numbers.stream()
                                    .map(NumberType.class::cast)
                                    .map(NumberType::range)
                                    .toArray(NumericRange[]::new)
                    )
            ));
        }
        return List.of();
    }

    private List<AntlrQueryItemType> stringTypeUnion(Map<? extends Class<?>, Set<AntlrQueryItemType>> classToItems) {
        var enums = classToItems.getOrDefault(StringType.StringEnum.class, Set.of());
        var strings = classToItems.getOrDefault(StringType.StringNonEnum.class, Set.of());
        if (!strings.isEmpty() && !enums.isEmpty()) {
            var cardinalities = Streams.concat(strings.stream(), enums.stream())
                    .map(StringType.class::cast)
                    .map(StringType::cardinality)
                    .toArray(Cardinality[]::new);
            return List.of(typeFactory.itemString(Cardinalities.union(cardinalities)));
        } else if (!enums.isEmpty()) {
            var enumMembers =  enums.stream().map(StringType.StringEnum.class::cast)
                    .map(StringType.StringEnum::members)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toUnmodifiableSet());
            return List.of(typeFactory.itemEnum(enumMembers));
        }
        return List.of();
    }

    private List<AntlrQueryItemType> booleanUnion(Map<? extends Class<?>, Set<AntlrQueryItemType>> classToItems) {
        var booleans = classToItems.getOrDefault(BooleanType.Boolean.class, Set.of());
        var trues = classToItems.getOrDefault(BooleanType.False.class, Set.of());
        var falses = classToItems.getOrDefault(BooleanType.True.class, Set.of());
        if (!booleans.isEmpty()) {
            return List.of(typeFactory.itemBoolean());
        } else if (!trues.isEmpty() && !falses.isEmpty()) {
            return List.of(typeFactory.itemBoolean());
        } else if (!trues.isEmpty()) {
            return List.of(typeFactory.itemTrue());
        } else if (!falses.isEmpty()) {
            return List.of(typeFactory.itemFalse());
        }
        return List.of();
    }


    public ItemTypeUnion(final AntlrQueryTypeFactory typeFactory)
    {
        this.typeFactory = Objects.requireNonNull(typeFactory);
    }


}
