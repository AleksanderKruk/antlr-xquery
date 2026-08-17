package com.github.akruk.antlrquery.typesystem.typeoperations.itemtype;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class ItemTypeSubtract {

    private final AntlrQueryTypeFactory typeFactory;

    public ItemTypeSubtract(AntlrQueryTypeFactory typeFactory) {
        this.typeFactory = typeFactory;
    }

    public @Nullable AntlrQueryItemType subtract(AntlrQueryItemType from, AntlrQueryItemType... subtractedTypes) {
        if (subtractedTypes.length == 0) {
            return from;
        }

        Set<AntlrQueryItemType> flatSubtracted = new HashSet<>();
        for (AntlrQueryItemType sub : subtractedTypes) {
            flatSubtracted.addAll(flatten(sub));
        }
        Map<Class<?>, List<AntlrQueryItemType>> groupedSubtracted = groupByClass(flatSubtracted);

        var anyItems = groupedSubtracted.getOrDefault(AnyItemType.class, List.of());
        if (!anyItems.isEmpty()) {
            return null;
        }
        Set<ConcreteItemType> baseItems;
        switch (from) {
            case AnyItemType anyItemType -> { return anyItemType; }
            case ChoiceItemType(ConcreteItemType[] itemTypes) -> baseItems = Arrays.stream(itemTypes).collect(Collectors.toSet());
            case ConcreteItemType concreteItemType -> baseItems = Set.of(concreteItemType);
            case NeverType _ -> { return null; }
            case NothingType _ -> { return typeFactory.itemNothing(); }
            case NamedItemType(NamespaceResolver.QualifiedName reference) -> {
                return subtract(typeFactory.guaranteedItemNamedType(reference, new IllegalStateException()), subtractedTypes);
            }
        }
        List<AntlrQueryItemType> resultTypes = new ArrayList<>(baseItems.size());
        var fromTypesGroupedByClass = groupByClass(baseItems);
        List<AntlrQueryItemType> nodeTypes = subtractNodeTypes(fromTypesGroupedByClass, groupedSubtracted);
        resultTypes.addAll(nodeTypes);


        for (ConcreteItemType baseItem : baseItems) {
            @Nullable AntlrQueryItemType resultingItem = null;
            switch (baseItem) {
                case ArrayLikeType.ArrayType arrayType -> {
                    AntlrQuerySequenceType currentMember = arrayType.memberType();
                    Cardinality currentCard = arrayType.cardinality();
                    boolean covered = false;
                    boolean splitted = false;

                    var subtractedArrays = groupedSubtracted.getOrDefault(ArrayLikeType.ArrayType.class, List.of());
                    var subtractedTuples = groupedSubtracted.getOrDefault(ArrayLikeType.TupleType.class, List.of());
                    var candidates = java.util.stream.Stream.concat(subtractedArrays.stream(), subtractedTuples.stream()).toList();

                    for (AntlrQueryItemType sub : candidates) {
                        @Nullable AntlrQuerySequenceType subMember = Types.getMemberType(typeFactory, sub);
                        if (subMember == null) continue;

                        Cardinality subCard;
                        if (sub instanceof ArrayLikeType.ArrayType sa) {
                            subCard = sa.cardinality();
                        } else {
                            subCard = Cardinality.of(((ArrayLikeType.TupleType) sub).members().length);
                        }

                        @Nullable Cardinality diffCard = Cardinalities.subtract(currentCard, subCard);

                        @Nullable AntlrQuerySequenceType diffItem = Types.subtract(typeFactory, currentMember, subMember);

                        boolean cardCovered = (diffCard == null);
                        boolean typeCovered = (diffItem.itemType() instanceof NothingType);

                        if (cardCovered && typeCovered) {
                            covered = true;
                            break;
                        }

                        // Cardinality type divergence
                        if (cardCovered) {
                            // (string|number)[29] - number[29] = (string)[29]
                            currentMember = diffItem;
                            splitted = true;
                        } else if (typeCovered) {
                            // string^1..2[] - string^1[] = string^2[]
                            currentCard = diffCard;
                            splitted = true;
                        } else {
                            if (ItemTypes.isSubtype(typeFactory, currentMember.itemType(), subMember.itemType())) {
                                currentCard = diffCard;
                                splitted = true;
                            }
                        }
                    }

                    resultingItem = covered ? typeFactory.itemNothing()
                            : (splitted ? new ArrayLikeType.ArrayType(currentMember, currentCard) : arrayType);
                }

                case ArrayLikeType.TupleType tupleType -> {
                    AntlrQuerySequenceType[] currentMembers = tupleType.members().clone();
                    boolean covered = false;
                    boolean splitted = false;

                    var subtractedArrays = groupedSubtracted.getOrDefault(ArrayLikeType.ArrayType.class, List.of());
                    var subtractedTuples = groupedSubtracted.getOrDefault(ArrayLikeType.TupleType.class, List.of());
                    var candidates = java.util.stream.Stream.concat(subtractedTuples.stream(), subtractedArrays.stream()).toList();

                    for (AntlrQueryItemType sub : candidates) {
                        boolean fullySubtracted = true;
                        boolean localSplitted = false;

                        if (sub instanceof ArrayLikeType.TupleType(AntlrQuerySequenceType[] subMembers)) {
                            if (currentMembers.length == subMembers.length) {
                                // Tuple - Tuple = element po elemencie
                                for (int i = 0; i < currentMembers.length; i++) {
                                    @Nullable AntlrQueryItemType diff = subtract(currentMembers[i].itemType(), subMembers[i].itemType());

                                    if (!ItemTypes.isSubtype(typeFactory, currentMembers[i].itemType(), subMembers[i].itemType()) && diff != null) {
                                        currentMembers[i] = typeFactory.sequence(diff, currentMembers[i].cardinality());
                                        localSplitted = true;
                                    } else {
                                        fullySubtracted = false;
                                        break;
                                    }
                                }
                            } else {
                                fullySubtracted = false;
                            }
                        } else if (sub instanceof ArrayLikeType.ArrayType(
                                AntlrQuerySequenceType subMember, Cardinality cardinality
                        )) {
                            Cardinality tupleCard = Cardinality.of(currentMembers.length);
                            if (Cardinalities.subtract(tupleCard, cardinality) == null) {

                                for (int i = 0; i < currentMembers.length; i++) {
                                    @Nullable AntlrQueryItemType diff = subtract(currentMembers[i].itemType(), subMember.itemType());

                                    if (diff instanceof NothingType || diff == null) {
                                        continue;
                                    } else if (!ItemTypes.isSubtype(typeFactory, currentMembers[i].itemType(), subMember.itemType()) && diff != currentMembers[i].itemType()) {
                                        currentMembers[i] = typeFactory.sequence(diff, currentMembers[i].cardinality());
                                        localSplitted = true;
                                    } else {
                                        fullySubtracted = false;
                                        break;
                                    }
                                }
                            } else {
                                fullySubtracted = false;
                            }
                        }

                        if (fullySubtracted) {
                            if (!localSplitted) {
                                covered = true;
                                break;
                            } else {
                                splitted = true;
                            }
                        }
                    }

                    resultingItem = covered ? typeFactory.itemNothing()
                            : (splitted ? new ArrayLikeType.TupleType(currentMembers) : tupleType);
                }
                case BooleanType b -> {
                    var bls = groupedSubtracted.getOrDefault(BooleanType.Boolean.class, List.of());
                    var fls = groupedSubtracted.getOrDefault(BooleanType.False.class, List.of());
                    var trs = groupedSubtracted.getOrDefault(BooleanType.True.class, List.of());
                    resultingItem = switch (b) {
                        case BooleanType.Boolean _
                                when !bls.isEmpty() -> typeFactory.itemNothing();
                        case BooleanType.Boolean _
                                when !fls.isEmpty() && !trs.isEmpty() -> typeFactory.itemNothing();
                        case BooleanType.Boolean _
                                when !fls.isEmpty() -> typeFactory.itemTrue();
                        case BooleanType.Boolean _
                                when !trs.isEmpty() -> typeFactory.itemFalse();
                        case BooleanType.False _
                                when !fls.isEmpty() || !bls.isEmpty() -> typeFactory.itemNothing();
                        case BooleanType.True _
                                when !trs.isEmpty() || !bls.isEmpty() -> typeFactory.itemNothing();
                        default -> b;
                    };
                }
                case AtomicType.NumberType numberType -> {
                    var subtractedNumbers = groupedSubtracted.getOrDefault(AtomicType.NumberType.class, List.of());
                    @Nullable NumericRange result = numberType.range();
                    for (AntlrQueryItemType i : subtractedNumbers) {
                        NumericRange sn = ((AtomicType.NumberType) i).range();
                        if (result == null) break;
                        result = Ranges.subtract(result, sn);
                    }
                    resultingItem = result == null ? typeFactory.itemNothing() : typeFactory.itemNumber(result);
                }
                case AtomicType.RegexType regexType -> {
                    var subtractedRegexes = groupedSubtracted.getOrDefault(AtomicType.RegexType.class, List.of());
                    boolean matched = false;
                    for (var r : subtractedRegexes) {
                        if (regexType.equals(r)) {
                            matched = true;
                            break;
                        }
                    }
                    resultingItem = matched ? typeFactory.itemNothing() : regexType;
                }
                case StringType str -> {
                    var subtractedStrings = groupedSubtracted.getOrDefault(StringType.StringNonEnum.class, List.of());
                    var subtractedEnums = groupedSubtracted.getOrDefault(StringType.StringEnum.class, List.of());
                    if (subtractedEnums.isEmpty() && subtractedStrings.isEmpty()) {
                        resultingItem = str;
                    } else {
                        Cardinality[] allCardinalities = Stream.of(subtractedStrings, subtractedEnums)
                                .flatMap(List::stream)
                                .map(i -> ((StringType) i).cardinality())
                                .toArray(Cardinality[]::new);
                        var unionCardinality = Cardinalities.union(allCardinalities);
                        @Nullable Cardinality resultingCardinality = Cardinalities.subtract(str.cardinality(), unionCardinality);

                        if (resultingCardinality == null) {
                            resultingItem = typeFactory.itemNothing();
                        } else {
                            Set<String> subtractedEnumMembers = subtractedEnums.stream()
                                    .flatMap(i -> ((StringType.StringEnum) i).members().stream())
                                    .collect(Collectors.toSet());

                            resultingItem = switch (str) {
                                case StringType.StringEnum(Set<String> members, Cardinality cardinality) -> {
                                    Set<String> validMembers = members.stream()
                                            .filter(m -> !subtractedEnumMembers.contains(m)
                                                    && Cardinalities.contains(resultingCardinality, m.length()))
                                            .collect(Collectors.toSet());
                                    yield validMembers.isEmpty() ? typeFactory.itemNothing() : typeFactory.itemEnum(validMembers);
                                }
                                case StringType.StringNonEnum(Cardinality _) -> typeFactory.itemString(resultingCardinality);
                            };
                        }
                    }
                }
                case MapLikeType.MapType mapType -> {
                    AntlrQueryItemType currentKey = mapType.keyType();
                    AntlrQuerySequenceType currentVal = mapType.valueType();
                    boolean covered = false;

                    var subtractedMaps = groupedSubtracted.getOrDefault(MapLikeType.MapType.class, List.of());
                    var subtractedExtensibleRecords = groupedSubtracted.getOrDefault(MapLikeType.ExtensibleRecordType.class, List.of());

                    var candidates = java.util.stream.Stream.concat(subtractedMaps.stream(), subtractedExtensibleRecords.stream()).toList();

                    for (AntlrQueryItemType sub : candidates) {
                        @Nullable AntlrQueryItemType subKey = Types.getMapKey(typeFactory, sub);
                        @Nullable AntlrQuerySequenceType subVal = Types.getMapValue(typeFactory, sub);

                        if (subKey != null && subVal != null) {
                            boolean keyCovered = ItemTypes.isSubtype(typeFactory, currentKey, subKey)
                                    || subtract(currentKey, subKey) instanceof NothingType;

                            boolean valCovered = ItemTypes.isSubtype(typeFactory, currentVal.itemType(), subVal.itemType())
                                    || subtract(currentVal.itemType(), subVal.itemType()) instanceof NothingType;

                            boolean cardCovered = Cardinalities.subtract(currentVal.cardinality(), subVal.cardinality()) == null;

                            if (keyCovered && valCovered && cardCovered) {
                                covered = true;
                                break;
                            }
                        }
                    }
                    resultingItem = covered ? typeFactory.itemNothing() : mapType;
                }

                case MapLikeType.RecordType recordType -> {
                    LinkedHashMap<String, RecordField> currentFields = recordType.fields();
                    boolean covered = false;

                    var subtractedMaps = groupedSubtracted.getOrDefault(MapLikeType.MapType.class, List.of());
                    var subtractedRecords = groupedSubtracted.getOrDefault(MapLikeType.RecordType.class, List.of());
                    var subtractedExtensibleRecords = groupedSubtracted.getOrDefault(MapLikeType.ExtensibleRecordType.class, List.of());

                    for (AntlrQueryItemType sub : subtractedRecords) {
                        MapLikeType.RecordType sr = (MapLikeType.RecordType) sub;
                        boolean fullySubtracted = true;

                        for (Map.Entry<String, RecordField> entry : currentFields.entrySet()) {
                            RecordField cf = entry.getValue();
                            RecordField sf = sr.fields().get(entry.getKey());

                            if (sf == null) {
                                fullySubtracted = false; break;
                            }

                            AntlrQuerySequenceType cType = cf.resolveFieldType(typeFactory);
                            AntlrQuerySequenceType sType = sf.resolveFieldType(typeFactory);

                            if (!Types.isSubtype(typeFactory, cType, sType) && !(Types.subtract(typeFactory, cType, sType).itemType() instanceof NeverType)) {
                                fullySubtracted = false; break;
                            }
                            if (!sf.isRequired() && cf.isRequired()) {
                                fullySubtracted = false; break;
                            }
                        }
                        if (fullySubtracted) { covered = true; break; }
                    }

                    if (!covered) {
                        for (AntlrQueryItemType sub : subtractedExtensibleRecords) {
                            MapLikeType.ExtensibleRecordType ser = (MapLikeType.ExtensibleRecordType) sub;
                            boolean fullySubtracted = true;

                            for (Map.Entry<String, RecordField> entry : currentFields.entrySet()) {
                                RecordField cf = entry.getValue();
                                RecordField sf = ser.fields().get(entry.getKey());

                                AntlrQuerySequenceType sType;
                                boolean isSReq;

                                if (sf != null) {
                                    sType = sf.resolveFieldType(typeFactory);
                                    isSReq = sf.isRequired();
                                } else {
                                    sType = ser.additionalFieldType();
                                    isSReq = false;
                                }

                                AntlrQuerySequenceType cType = cf.resolveFieldType(typeFactory);
                                if (!Types.isSubtype(typeFactory, cType, sType) && !(Types.subtract(typeFactory, cType, sType).itemType() instanceof NothingType)) {
                                    fullySubtracted = false; break;
                                }
                                if (!isSReq && cf.isRequired()) {
                                    fullySubtracted = false; break;
                                }
                            }
                            if (fullySubtracted) { covered = true; break; }
                        }
                    }

                    if (!covered) {
                        for (AntlrQueryItemType sub : subtractedMaps) {
                            MapLikeType.MapType sm = (MapLikeType.MapType) sub;
                            @Nullable AntlrQuerySequenceType subVal = Types.getMapValue(typeFactory, sm);

                            if (subVal != null) {
                                boolean fullySubtracted = true;
                                for (RecordField cf : currentFields.values()) {
                                    AntlrQuerySequenceType cType = cf.resolveFieldType(typeFactory);
                                    if (!Types.isSubtype(typeFactory, cType, subVal) && !(Types.subtract(typeFactory, cType, subVal).itemType() instanceof NothingType)) {
                                        fullySubtracted = false; break;
                                    }
                                }
                                if (fullySubtracted) { covered = true; break; }
                            }
                        }
                    }

                    resultingItem = covered ? typeFactory.itemNothing() : recordType;
                }

                case MapLikeType.ExtensibleRecordType extType -> {
                    Map<String, RecordField> currentFields = extType.fields();
                    AntlrQuerySequenceType currentAdditional = extType.additionalFieldType();
                    boolean covered = false;

                    var subtractedMaps = groupedSubtracted.getOrDefault(MapLikeType.MapType.class, List.of());
                    var subtractedRecords = groupedSubtracted.getOrDefault(MapLikeType.RecordType.class, List.of());
                    var subtractedExtensibleRecords = groupedSubtracted.getOrDefault(MapLikeType.ExtensibleRecordType.class, List.of());

                    if (currentAdditional instanceof AntlrQuerySequenceType.EmptySequence) {
                        for (AntlrQueryItemType sub : subtractedRecords) {
                            MapLikeType.RecordType sr = (MapLikeType.RecordType) sub;
                            boolean fullySubtracted = true;

                            for (Map.Entry<String, RecordField> entry : currentFields.entrySet()) {
                                RecordField cf = entry.getValue();
                                RecordField sf = sr.fields().get(entry.getKey());

                                if (sf == null) { fullySubtracted = false; break; }

                                AntlrQuerySequenceType cType = cf.resolveFieldType(typeFactory);
                                AntlrQuerySequenceType sType = sf.resolveFieldType(typeFactory);

                                if (!Types.isSubtype(typeFactory, cType, sType) && !(Types.subtract(typeFactory, cType, sType).itemType() instanceof NothingType)) {
                                    fullySubtracted = false; break;
                                }
                                if (!sf.isRequired() && cf.isRequired()) {
                                    fullySubtracted = false; break;
                                }
                            }
                            if (fullySubtracted) { covered = true; break; }
                        }
                    }

                    if (!covered) {
                        for (AntlrQueryItemType sub : subtractedExtensibleRecords) {
                            MapLikeType.ExtensibleRecordType ser = (MapLikeType.ExtensibleRecordType) sub;
                            boolean fullySubtracted = true;

                            for (Map.Entry<String, RecordField> entry : currentFields.entrySet()) {
                                RecordField cf = entry.getValue();
                                RecordField sf = ser.fields().get(entry.getKey());

                                AntlrQuerySequenceType sType = sf != null ? sf.resolveFieldType(typeFactory) : ser.additionalFieldType();
                                boolean sReq = sf != null && sf.isRequired();
                                AntlrQuerySequenceType cType = cf.resolveFieldType(typeFactory);

                                if (!Types.isSubtype(typeFactory, cType, sType) && !(Types.subtract(typeFactory, cType, sType).itemType() instanceof NothingType)) {
                                    fullySubtracted = false; break;
                                }
                                if (!sReq && cf.isRequired()) {
                                    fullySubtracted = false; break;
                                }
                            }

                            if (fullySubtracted) {
                                boolean addCovered = ItemTypes.isSubtype(typeFactory, currentAdditional.itemType(), ser.additionalFieldType().itemType())
                                        || subtract(currentAdditional.itemType(), ser.additionalFieldType().itemType()) instanceof NothingType;
                                boolean cardCovered = Cardinalities.subtract(currentAdditional.cardinality(), ser.additionalFieldType().cardinality()) == null;

                                if (addCovered && cardCovered) {
                                    covered = true; break;
                                }
                            }
                        }
                    }

                    if (!covered) {
                        for (AntlrQueryItemType sub : subtractedMaps) {
                            MapLikeType.MapType sm = (MapLikeType.MapType) sub;
                            @Nullable AntlrQuerySequenceType subVal = Types.getMapValue(typeFactory, sm);

                            if (subVal != null) {
                                boolean fullySubtracted = true;
                                for (RecordField cf : currentFields.values()) {
                                    AntlrQuerySequenceType cType = cf.resolveFieldType(typeFactory);
                                    if (!Types.isSubtype(typeFactory, cType, subVal) && !(Types.subtract(typeFactory, cType, subVal).itemType() instanceof NothingType)) {
                                        fullySubtracted = false; break;
                                    }
                                }

                                if (fullySubtracted) {
                                    boolean addCovered = ItemTypes.isSubtype(typeFactory, currentAdditional.itemType(), subVal.itemType())
                                            || subtract(currentAdditional.itemType(), subVal.itemType()) instanceof NothingType;
                                    boolean cardCovered = Cardinalities.subtract(currentAdditional.cardinality(), subVal.cardinality()) == null;

                                    if (addCovered && cardCovered) {
                                        covered = true; break;
                                    }
                                }
                            }
                        }
                    }

                    resultingItem = covered ? typeFactory.itemNothing() : extType;
                }
                case FunctionType functionType -> resultingItem = flatSubtracted.contains(functionType) ? typeFactory.itemNothing() : functionType;
                case GrammarEntityType grammarEntityType -> resultingItem = flatSubtracted.contains(grammarEntityType) ? typeFactory.itemNothing() : grammarEntityType;
                case TreeLike treeLike -> {}
            }

            if (resultingItem != null && !(resultingItem instanceof NothingType) && !(resultingItem instanceof NeverType)) {
                resultTypes.add(resultingItem);
            }
        }

        if (resultTypes.isEmpty()) {
            return typeFactory.itemNothing();
        }
        if (resultTypes.size() == 1) {
            return resultTypes.getFirst();
        }
        return typeFactory.itemChoice(resultTypes.toArray(AntlrQueryItemType[]::new));
    }

    private static Set<AntlrQueryItemType> flatten(AntlrQueryItemType type) {
        if (type instanceof ChoiceItemType(ConcreteItemType[] itemTypes)) {
            return Arrays.stream(itemTypes).collect(Collectors.toSet());
        }
        return Set.of(type);
    }

    private static Map<Class<?>, List<AntlrQueryItemType>> groupByClass(Collection<? extends AntlrQueryItemType> types) {
        Map<Class<?>, List<AntlrQueryItemType>> map = new HashMap<>();
        for (AntlrQueryItemType type : types) {
            map.computeIfAbsent(type.getClass(), _ -> new ArrayList<>()).add(type);
        }
        return map;
    }


    private List<AntlrQueryItemType> subtractNodeTypes(Map<Class<?>, List<AntlrQueryItemType>> fromTypesGroupedByClass, Map<Class<?>, List<AntlrQueryItemType>> groupedSubtracted) {
        var fromAnyNodes = fromTypesGroupedByClass.getOrDefault(TreeNodeType.AnyNode.class, List.of());
        var fromNodesFromGrammar = fromTypesGroupedByClass.getOrDefault(TreeNodeType.NodeType.class, List.of());
        var fromAnyNodeFromGrammar = fromTypesGroupedByClass.getOrDefault(TreeNodeType.AnyNodeFromGrammar.class, List.of());
        var fromAnyTokens = fromTypesGroupedByClass.getOrDefault(TreeTokenType.AnyToken.class, List.of());
        var fromTokensFromGrammar = fromTypesGroupedByClass.getOrDefault(TreeTokenType.TokenType.class, List.of());
        var fromAnyTokenFromGrammar = fromTypesGroupedByClass.getOrDefault(TreeTokenType.AnyTokenFromGrammar.class, List.of());
        var fromAnyRules = fromTypesGroupedByClass.getOrDefault(TreeRuleType.AnyRule.class, List.of());
        var fromRulesFromGrammar = fromTypesGroupedByClass.getOrDefault(TreeRuleType.RuleType.class, List.of());
        var fromAnyRuleFromGrammar = fromTypesGroupedByClass.getOrDefault(TreeRuleType.AnyRuleFromGrammar.class, List.of());

        if (fromAnyNodes.isEmpty()
                && fromAnyNodeFromGrammar.isEmpty()
                && fromNodesFromGrammar.isEmpty()
                && fromAnyTokens.isEmpty()
                && fromAnyTokenFromGrammar.isEmpty()
                && fromTokensFromGrammar.isEmpty()
                && fromAnyRules.isEmpty()
                && fromAnyRuleFromGrammar.isEmpty()
                && fromRulesFromGrammar.isEmpty()
            )
        {
            return List.of();
        }

        if (!fromAnyTokens.isEmpty()
                || !fromAnyTokenFromGrammar.isEmpty()
                || !fromTokensFromGrammar.isEmpty())
        { // constrained by token
            return constrainedByNodeType(
                    fromAnyTokenFromGrammar,
                    fromTokensFromGrammar,
                    fromAnyTokens,
                    typeFactory::itemTokensFromGrammar,
                    typeFactory::grammarTokens,
                    groupedSubtracted.getOrDefault(TreeTokenType.AnyToken.class, List.of()),
                    groupedSubtracted.getOrDefault(TreeNodeType.AnyNode.class, List.of()),
                    groupedSubtracted.getOrDefault(TreeTokenType.AnyTokenFromGrammar.class, List.of()),
                    groupedSubtracted.getOrDefault(TreeNodeType.AnyNodeFromGrammar.class, List.of()),
                    groupedSubtracted.getOrDefault(TreeTokenType.TokenType.class, List.of()),
                    groupedSubtracted.getOrDefault(TreeNodeType.NodeType.class, List.of())
            );
        }
        if (!fromAnyRules.isEmpty()
                || !fromAnyRuleFromGrammar.isEmpty()
                || !fromRulesFromGrammar.isEmpty())
        { // constrained by rule
            return constrainedByNodeType(fromAnyRuleFromGrammar,
                    fromRulesFromGrammar,
                    fromAnyRules,
                    typeFactory::itemRulesFromGrammar,
                    typeFactory::grammarRules,
                    groupedSubtracted.getOrDefault(TreeRuleType.AnyRule.class, List.of()),
                    groupedSubtracted.getOrDefault(TreeNodeType.AnyNode.class, List.of()),
                    groupedSubtracted.getOrDefault(TreeRuleType.AnyRuleFromGrammar.class, List.of()),
                    groupedSubtracted.getOrDefault(TreeNodeType.AnyNodeFromGrammar.class, List.of()),
                    groupedSubtracted.getOrDefault(TreeRuleType.RuleType.class, List.of()),
                    groupedSubtracted.getOrDefault(TreeNodeType.NodeType.class, List.of()));
        }
        return constrainedByAnyNodeType(
                fromAnyNodeFromGrammar,
                fromNodesFromGrammar,
                fromAnyNodes,
                typeFactory::itemNodesFromGrammar,
                typeFactory::itemTokensFromGrammar,
                typeFactory::itemRulesFromGrammar,
                typeFactory::itemAnyNodeFromGrammar,
                typeFactory::itemAnyTokenFromGrammar,
                typeFactory::itemAnyRuleFromGrammar,
                typeFactory::grammarNodes,
                typeFactory::grammarTokens,
                typeFactory::grammarRules,
                groupedSubtracted.getOrDefault(TreeTokenType.AnyToken.class, List.of()),
                groupedSubtracted.getOrDefault(TreeRuleType.AnyRule.class, List.of()),
                groupedSubtracted.getOrDefault(TreeNodeType.AnyNode.class, List.of()),
                groupedSubtracted.getOrDefault(TreeTokenType.AnyTokenFromGrammar.class, List.of()),
                groupedSubtracted.getOrDefault(TreeRuleType.AnyRuleFromGrammar.class, List.of()),
                groupedSubtracted.getOrDefault(TreeNodeType.AnyNodeFromGrammar.class, List.of()),
                groupedSubtracted.getOrDefault(TreeTokenType.TokenType.class, List.of()),
                groupedSubtracted.getOrDefault(TreeRuleType.RuleType.class, List.of()),
                groupedSubtracted.getOrDefault(TreeNodeType.NodeType.class, List.of())
        );
    }

    private List<AntlrQueryItemType> constrainedByNodeType(
            List<AntlrQueryItemType> fromAnyTokenFromGrammar,
            List<AntlrQueryItemType> fromTokensFromGrammar,
            List<AntlrQueryItemType> fromAnyTokens,
            BiFunction<String, Set<NamespaceResolver.QualifiedName>, AntlrQueryItemType> getItemTokenFromGrammar,
            Function<String, Set<NamespaceResolver.QualifiedName>> getTokenTypeFromGrammar,
            List<AntlrQueryItemType> subtractedAnyTokens,
            List<AntlrQueryItemType> subtractedAnyNodes,
            List<AntlrQueryItemType> subtractedAnyTokenFromGrammar,
            List<AntlrQueryItemType> subtractedAnyNodeFromGrammar,
            List<AntlrQueryItemType> subtractedTokensFromGrammar,
            List<AntlrQueryItemType> subtractedNodesFromGrammar

    )
    {
        if (!subtractedAnyTokens.isEmpty() || !subtractedAnyNodes.isEmpty()) {
            return List.of();
        }

        List<AntlrQueryItemType> results = new ArrayList<>();
        var subtractedGrammars = Stream.of(subtractedAnyTokenFromGrammar, subtractedAnyNodeFromGrammar)
                .flatMap(Collection::stream)
                .map(GrammarConstrained.class::cast)
                .map(GrammarConstrained::grammar)
                .collect(Collectors.toSet());
        var remainingAnyGrammars = fromAnyTokenFromGrammar.stream()
                .map(GrammarConstrained.class::cast)
                .filter(s->!subtractedGrammars.contains(s.grammar()))
                .collect(Collectors.toSet());
        var remainingTokenFromGrammar = fromTokensFromGrammar.stream()
                .map(obj -> (GrammarConstrained&NamesConstrained) obj)
                .filter(s->!subtractedGrammars.contains(s.grammar()))
                .collect(Collectors.toSet());

        var subtractedTokenFromGrammarGroupedByGrammar = Stream.of(subtractedTokensFromGrammar, subtractedNodesFromGrammar)
                .flatMap(Collection::stream)
                .map(e->(GrammarConstrained&NamesConstrained) e)
                .collect(Collectors.groupingBy(GrammarConstrained::grammar));

        for (var remainingAnyGrammar : remainingAnyGrammars) {
            var grammarTypes = subtractedTokenFromGrammarGroupedByGrammar.get(remainingAnyGrammar.grammar());
            if (grammarTypes == null) {
                results.add((AntlrQueryItemType) remainingAnyGrammar);
            }
            HashSet<NamespaceResolver.QualifiedName> types = new HashSet<>(getTokenTypeFromGrammar.apply(remainingAnyGrammar.grammar()));
            assert grammarTypes != null;
            grammarTypes.stream().map(NamesConstrained::elementNames).flatMap(Collection::stream).toList().forEach(types::remove);
            if (!types.isEmpty()) {
                results.add(getItemTokenFromGrammar.apply(remainingAnyGrammar.grammar(), types));
            }
        }
        for (var remainingToken : remainingTokenFromGrammar) {
            var grammarTypes = subtractedTokenFromGrammarGroupedByGrammar.get(remainingToken.grammar());
            if (grammarTypes == null) {
                results.add((AntlrQueryItemType) remainingToken);
            }
            var types = new HashSet<>(remainingToken.elementNames());
            assert grammarTypes != null;
            grammarTypes.stream().map(NamesConstrained::elementNames).flatMap(Collection::stream).toList().forEach(types::remove);
            if (!types.isEmpty()) {
                results.add(getItemTokenFromGrammar.apply(remainingToken.grammar(), types));
            }
        }
        if (!results.isEmpty()) {
            return results;
        }
        return List.of(fromAnyTokens.getFirst());
    }

    private List<AntlrQueryItemType> constrainedByAnyNodeType(
            List<AntlrQueryItemType> fromAnyNodesFromGrammar,
            List<AntlrQueryItemType> fromNodesFromGrammar,
            List<AntlrQueryItemType> fromAnyNodes,
            BiFunction<String, Set<NamespaceResolver.QualifiedName>, AntlrQueryItemType> getItemNodeFromGrammar,
            BiFunction<String, Set<NamespaceResolver.QualifiedName>, AntlrQueryItemType> getItemTokenFromGrammar,
            BiFunction<String, Set<NamespaceResolver.QualifiedName>, AntlrQueryItemType> getItemRuleFromGrammar,
            Function<String, AntlrQueryItemType> getAllNodeTypeFromGrammar,
            Function<String, AntlrQueryItemType> getAllTokenTypeFromGrammar,
            Function<String, AntlrQueryItemType> getAllRuleTypeFromGrammar,
            Function<String, Set<NamespaceResolver.QualifiedName>> getNodeTypeFromGrammar,
            Function<String, Set<NamespaceResolver.QualifiedName>> getTokenTypeFromGrammar,
            Function<String, Set<NamespaceResolver.QualifiedName>> getRuleTypeFromGrammar,
            List<AntlrQueryItemType> subtractedAnyTokens,
            List<AntlrQueryItemType> subtractedAnyRules,
            List<AntlrQueryItemType> subtractedAnyNodes,
            List<AntlrQueryItemType> subtractedAnyTokenFromGrammar,
            List<AntlrQueryItemType> subtractedAnyRuleFromGrammar,
            List<AntlrQueryItemType> subtractedAnyNodeFromGrammar,
            List<AntlrQueryItemType> subtractedTokensFromGrammar,
            List<AntlrQueryItemType> subtractedRulesFromGrammar,
            List<AntlrQueryItemType> subtractedNodesFromGrammar) {

        if (!subtractedAnyNodes.isEmpty()) {
            return List.of();
        }

        final boolean stripAllTokens = !subtractedAnyTokens.isEmpty();
        final boolean stripAllRules = !subtractedAnyRules.isEmpty();

        if (stripAllTokens && stripAllRules) {
            return List.of();
        }

        var effectiveSubtractedAnyTokenFromGrammar =
                stripAllTokens ? List.of() : subtractedAnyTokenFromGrammar;
        var effectiveSubtractedAnyRuleFromGrammar =
                stripAllRules ? List.of() : subtractedAnyRuleFromGrammar;

        List<AntlrQueryItemType> results = new ArrayList<>();

        var grammarsWithoutNodes = subtractedAnyNodeFromGrammar.stream()
                .map(GrammarConstrained.class::cast)
                .map(GrammarConstrained::grammar)
                .collect(Collectors.toSet());

        var remainingAnyNodeGrammars = fromAnyNodesFromGrammar.stream()
                .map(GrammarConstrained.class::cast)
                .filter(s -> !grammarsWithoutNodes.contains(s.grammar()))
                .collect(Collectors.toSet());

        var remainingNodeFromGrammar = fromNodesFromGrammar.stream()
                .map(obj -> (GrammarConstrained & NamesConstrained) obj)
                .filter(s -> !grammarsWithoutNodes.contains(s.grammar()))
                .collect(Collectors.toSet());

        var subtractedNodeFromGrammarGroupedByGrammar = subtractedNodesFromGrammar.stream()
                .map(e -> (GrammarConstrained & NamesConstrained) e)
                .collect(Collectors.groupingBy(GrammarConstrained::grammar));

        var subtractedTokenFromGrammarGroupedByGrammar = subtractedTokensFromGrammar.stream()
                .map(e -> (GrammarConstrained & NamesConstrained) e)
                .collect(Collectors.groupingBy(GrammarConstrained::grammar));

        var subtractedRuleFromGrammarGroupedByGrammar = subtractedRulesFromGrammar.stream()
                .map(e -> (GrammarConstrained & NamesConstrained) e)
                .collect(Collectors.groupingBy(GrammarConstrained::grammar));

        if (stripAllRules) {

            for (var remainingAnyGrammar : remainingAnyNodeGrammars) {

                var subtractedGrammarNodes =
                        subtractedNodeFromGrammarGroupedByGrammar.get(remainingAnyGrammar.grammar());
                var subtractedGrammarTokens =
                        subtractedTokenFromGrammarGroupedByGrammar.get(remainingAnyGrammar.grammar());

                if (subtractedGrammarNodes == null && subtractedGrammarTokens == null) {
                    results.add(getAllTokenTypeFromGrammar.apply(remainingAnyGrammar.grammar()));
                    continue;
                }

                var effectiveNodes =
                        Objects.requireNonNullElse(subtractedGrammarNodes, List.of());
                var effectiveTokens =
                        Objects.requireNonNullElse(subtractedGrammarTokens, List.of());

                var nodes = getTokenTypeFromGrammar.apply(remainingAnyGrammar.grammar())
                        .stream()
                        .filter(n -> !effectiveNodes.contains(n))
                        .filter(n -> !effectiveTokens.contains(n))
                        .collect(Collectors.toSet());

                if (!nodes.isEmpty()) {
                    results.add(getItemTokenFromGrammar.apply(
                            remainingAnyGrammar.grammar(),
                            nodes));
                }
            }

            for (var remainingNode : remainingNodeFromGrammar) {

                var subtractedGrammarNodes =
                        subtractedNodeFromGrammarGroupedByGrammar.get(remainingNode.grammar());
                var subtractedGrammarTokens =
                        subtractedTokenFromGrammarGroupedByGrammar.get(remainingNode.grammar());

                if (subtractedGrammarNodes == null && subtractedGrammarTokens == null) {
                    results.add((AntlrQueryItemType) remainingNode);
                    continue;
                }

                var effectiveNodes =
                        Objects.requireNonNullElse(subtractedGrammarNodes, List.of());
                var effectiveTokens =
                        Objects.requireNonNullElse(subtractedGrammarTokens, List.of());

                var filtered = remainingNode.elementNames().stream()
                        .filter(n -> n.name().matches("^\\P{IsUpper}"))
                        .filter(n -> !effectiveNodes.contains(n))
                        .filter(n -> !effectiveTokens.contains(n))
                        .collect(Collectors.toCollection(HashSet::new));

                if (!filtered.isEmpty()) {
                    results.add(getItemTokenFromGrammar.apply(
                            remainingNode.grammar(),
                            filtered));
                }
            }
        }


        else if (stripAllTokens) {

            for (var remainingAnyGrammar : remainingAnyNodeGrammars) {

                var subtractedGrammarNodes =
                        subtractedNodeFromGrammarGroupedByGrammar.get(remainingAnyGrammar.grammar());
                var subtractedGrammarRules =
                        subtractedRuleFromGrammarGroupedByGrammar.get(remainingAnyGrammar.grammar());

                if (subtractedGrammarNodes == null && subtractedGrammarRules == null) {
                    results.add(getAllRuleTypeFromGrammar.apply(remainingAnyGrammar.grammar()));
                    continue;
                }

                var effectiveNodes =
                        Objects.requireNonNullElse(subtractedGrammarNodes, List.of());
                var effectiveRules =
                        Objects.requireNonNullElse(subtractedGrammarRules, List.of());

                var nodes = getRuleTypeFromGrammar.apply(remainingAnyGrammar.grammar())
                        .stream()
                        .filter(n -> !effectiveNodes.contains(n))
                        .filter(n -> !effectiveRules.contains(n))
                        .collect(Collectors.toSet());

                if (!nodes.isEmpty()) {
                    results.add(getItemRuleFromGrammar.apply(
                            remainingAnyGrammar.grammar(),
                            nodes));
                }
            }

            for (var remainingNode : remainingNodeFromGrammar) {

                var subtractedGrammarNodes =
                        subtractedNodeFromGrammarGroupedByGrammar.get(remainingNode.grammar());
                var subtractedGrammarRules =
                        subtractedRuleFromGrammarGroupedByGrammar.get(remainingNode.grammar());

                if (subtractedGrammarNodes == null && subtractedGrammarRules == null) {
                    results.add((AntlrQueryItemType) remainingNode);
                    continue;
                }

                var effectiveNodes =
                        Objects.requireNonNullElse(subtractedGrammarNodes, List.of());
                var effectiveRules =
                        Objects.requireNonNullElse(subtractedGrammarRules, List.of());

                var filtered = remainingNode.elementNames().stream()
                        .filter(n -> n.name().matches("^\\p{IsUpper}"))
                        .filter(n -> !effectiveNodes.contains(n))
                        .filter(n -> !effectiveRules.contains(n))
                        .collect(Collectors.toCollection(HashSet::new));

                if (!filtered.isEmpty()) {
                    results.add(getItemRuleFromGrammar.apply(
                            remainingNode.grammar(),
                            filtered));
                }
            }
        }

        else {

            for (var remainingAnyGrammar : remainingAnyNodeGrammars) {

                var effectiveNodes = Objects.requireNonNullElse(
                        subtractedNodeFromGrammarGroupedByGrammar.get(remainingAnyGrammar.grammar()),
                        List.of());

                var effectiveTokens = Objects.requireNonNullElse(
                        subtractedTokenFromGrammarGroupedByGrammar.get(remainingAnyGrammar.grammar()),
                        List.of());

                var effectiveRules = Objects.requireNonNullElse(
                        subtractedRuleFromGrammarGroupedByGrammar.get(remainingAnyGrammar.grammar()),
                        List.of());

                var nodes = getNodeTypeFromGrammar.apply(remainingAnyGrammar.grammar())
                        .stream()
                        .filter(n -> !effectiveNodes.contains(n))
                        .filter(n -> !effectiveTokens.contains(n))
                        .filter(n -> !effectiveRules.contains(n))
                        .collect(Collectors.toSet());

                if (!nodes.isEmpty()) {
                    results.add(getItemNodeFromGrammar.apply(
                            remainingAnyGrammar.grammar(),
                            nodes));
                }
            }

            for (var remainingNode : remainingNodeFromGrammar) {

                var effectiveNodes = Objects.requireNonNullElse(
                        subtractedNodeFromGrammarGroupedByGrammar.get(remainingNode.grammar()),
                        List.of());

                var effectiveTokens = Objects.requireNonNullElse(
                        subtractedTokenFromGrammarGroupedByGrammar.get(remainingNode.grammar()),
                        List.of());

                var effectiveRules = Objects.requireNonNullElse(
                        subtractedRuleFromGrammarGroupedByGrammar.get(remainingNode.grammar()),
                        List.of());

                var filtered = remainingNode.elementNames().stream()
                        .filter(n -> !effectiveNodes.contains(n))
                        .filter(n -> !effectiveTokens.contains(n))
                        .filter(n -> !effectiveRules.contains(n))
                        .collect(Collectors.toCollection(HashSet::new));

                if (!filtered.isEmpty()) {
                    results.add(getItemNodeFromGrammar.apply(
                            remainingNode.grammar(),
                            filtered));
                }
            }
        }

        if (!results.isEmpty()) {
            return results;
        }

        return List.of(fromAnyNodes.getFirst());
    }

}
