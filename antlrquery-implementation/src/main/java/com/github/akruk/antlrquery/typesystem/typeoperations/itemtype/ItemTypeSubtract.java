package com.github.akruk.antlrquery.typesystem.typeoperations.itemtype;

import java.util.*;
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
            case NothingType nothingType -> { return typeFactory.itemNothing(); }
        }

        List<AntlrQueryItemType> resultTypes = new ArrayList<>(baseItems.size());

        for (ConcreteItemType baseItem : baseItems) {
            AntlrQueryItemType resultingItem;

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
                        result = Ranges.subtract(result, sn);
                        if (result == null) break;
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
                case TreeLike treeLike -> {
                    if (!(treeLike instanceof final GrammarConstrained treeLikeGrammar)) {
                        continue;
                    }
                    if (!(treeLike instanceof final NamesConstrained treeLikeNames)) {
                        continue;
                    }

                    String currentGrammar = treeLikeGrammar.grammar();
                    Set<NamespaceResolver.QualifiedName> currentNames = new java.util.HashSet<>(treeLikeNames.elementNames());
                    boolean currentIsUniversal = currentNames.isEmpty();

                    boolean isElement = treeLike instanceof TreeNodeType;
                    boolean isRule = treeLike instanceof TreeRuleType;
                    boolean isToken = treeLike instanceof TreeTokenType;

                    boolean covered = false;

                    var subtractedTrees = groupedSubtracted.getOrDefault(TreeLike.class, List.of());
                    for (AntlrQueryItemType sub : subtractedTrees) {
                        TreeLike subTree = (TreeLike) sub;


                        if (subTree instanceof final GrammarConstrained gc && !currentGrammar.equals(gc.grammar())) {
                            continue;
                        }

                        boolean subIsElement = subTree instanceof TreeNodeType.NodeType;
                        boolean subIsRule = subTree instanceof TreeRuleType.RuleType;
                        boolean subIsToken = subTree instanceof TreeTokenType.TokenType;

                        if (!(subTree instanceof final NamesConstrained subTreeWithNames)) {
                            continue;
                        }
                        Set<NamespaceResolver.QualifiedName> subNames = subTreeWithNames.elementNames();
                        boolean subIsUniversal = subNames.isEmpty();

                        if (isRule && subIsToken) continue;
                        if (isToken && subIsRule) continue;

                        if (subIsElement) {
                            if (subIsUniversal) {
                                covered = true;
                                break;
                            } else if (!currentIsUniversal) {
                                currentNames.removeAll(subNames);
                            }
                        } else if (subIsRule) {
                            if (isElement && subIsUniversal) {
                                isElement = false;
                                isToken = true;
                            } else if (subIsUniversal) {
                                covered = true;
                                break;
                            } else if (!currentIsUniversal) {
                                currentNames.removeAll(subNames);
                            }
                        } else if (subIsToken) {
                            if (isElement && subIsUniversal) {
                                isElement = false;
                                isRule = true;
                            } else if (subIsUniversal) {
                                covered = true;
                                break;
                            } else if (!currentIsUniversal) {
                                currentNames.removeAll(subNames);
                            }
                        }

                        if (!currentIsUniversal && currentNames.isEmpty()) {
                            covered = true;
                            break;
                        }
                    }

                    if (covered) {
                        resultingItem = typeFactory.itemNothing();
                    } else {
                        if (isElement) {
                            resultingItem = typeFactory.itemElement(currentGrammar, currentNames);
                        } else if (isRule) {
                            resultingItem = typeFactory.itemRule(currentGrammar, currentNames);
                        } else {
                            resultingItem = typeFactory.itemToken(currentGrammar, currentNames);
                        }

                        if (resultingItem.equals(treeLike)) {
                            resultingItem = treeLike;
                        }
                    }
                }
            }

            if (!(resultingItem instanceof NothingType) && !(resultingItem instanceof NeverType)) {
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

    private static Map<Class<?>, List<AntlrQueryItemType>> groupByClass(Collection<AntlrQueryItemType> types) {
        Map<Class<?>, List<AntlrQueryItemType>> map = new HashMap<>();
        for (AntlrQueryItemType type : types) {
            map.computeIfAbsent(type.getClass(), _ -> new ArrayList<>()).add(type);
        }
        return map;
    }

}
