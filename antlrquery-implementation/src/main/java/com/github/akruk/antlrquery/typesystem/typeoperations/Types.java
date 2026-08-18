package com.github.akruk.antlrquery.typesystem.typeoperations;

import java.util.*;

import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver;
import com.github.akruk.antlrquery.typesystem.RecordField;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Ranges;
import com.github.akruk.antlrquery.typesystem.typeoperations.itemtype.ItemTypeIsSubtype;
import com.github.akruk.antlrquery.typesystem.typeoperations.itemtype.ItemTypeSubtract;
import com.github.akruk.antlrquery.typesystem.typeoperations.itemtype.ItemTypeUnion;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.ItemTypes;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.*;
import org.checkerframework.checker.nullness.qual.NonNull;

import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType.EmptySequence;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType.NonEmptySequence;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.value.qual.ArrayLenRange;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class Types {
    private Types(){}

    public static @Nullable AntlrQuerySequenceType callResult(AntlrQueryTypeFactory typeFactory, AntlrQuerySequenceType type, List<AntlrQuerySequenceType> args) {
        return callResult(typeFactory, type.itemType(), args);
    }

    private static @Nullable AntlrQuerySequenceType callResult(AntlrQueryTypeFactory typeFactory, AntlrQueryItemType type, List<AntlrQuerySequenceType> args) {
        return switch(type) {
            case AnyItemType _ -> null;
            case ChoiceItemType choiceItemType -> {
                AntlrQuerySequenceType[] callResults = (AntlrQuerySequenceType[])
                        Arrays.stream(choiceItemType.itemTypes())
                            .map(i -> Types.callResult(typeFactory, i, args))
                            .filter(Objects::nonNull)
                            .toArray();
                yield Types.union(typeFactory, callResults);
            }
            case ConcreteItemType concreteItemType -> switch(concreteItemType) {
                case ArrayLikeType.ArrayType arrayType -> arrayType.memberType();
                case AtomicType _, MapLikeType.ExtensibleRecordType _ -> null;
                case FunctionType functionType -> functionType.returnType();
                case GrammarEntityType _ -> null;
                case MapLikeType.MapType mapType -> mapType.valueType();
                case MapLikeType.RecordType recordType -> {
                    AntlrQuerySequenceType[] merged =
                                recordType.fields().values().stream()
                                    .map(i -> Types.callResult(typeFactory, i.resolveFieldType(typeFactory), args))
                                    .filter(Objects::nonNull)
                                    .toArray(AntlrQuerySequenceType[]::new);
                    yield Types.union(typeFactory, merged);

                }
                case TreeLike treeLike -> null;
                case ArrayLikeType.TupleType tupleType -> null;
            };
            case NeverType _, NothingType _ -> null;
            case NamedItemType(NamespaceResolver.QualifiedName reference) ->
                    callResult(typeFactory, typeFactory.guaranteedItemNamedType(
                            reference,
                            new IllegalStateException(reference + " was not preregistered")
                    ), args);
        };
    }


    public static @Nullable AntlrQuerySequenceType getMemberType(AntlrQueryTypeFactory typeFactory, AntlrQueryItemType type) {
        return switch(type) {
            case ChoiceItemType choiceItemType -> {
                final AntlrQuerySequenceType[] memberTypes = Arrays.stream(choiceItemType.itemTypes())
                        .map(it -> Types.getMemberType(typeFactory, it))
                        .filter(Objects::nonNull)
                        .toArray(AntlrQuerySequenceType[]::new);
                yield Types.union(typeFactory, memberTypes);
            }
            case ArrayLikeType.ArrayType a -> a.memberType();
            case ArrayLikeType.TupleType t -> getMemberType(typeFactory, t);
            case AnyItemType anyItemType -> null;
            case AtomicType _, MapLikeType.ExtensibleRecordType _, FunctionType _,
                 GrammarEntityType _, MapLikeType.MapType _, MapLikeType.RecordType _,
                 TreeLike _, NeverType _, NothingType _ -> null;
            case NamedItemType(NamespaceResolver.QualifiedName reference) -> getMemberType(
                    typeFactory, typeFactory.guaranteedItemNamedType(reference, new IllegalStateException())
            );
        };
    }
    public static AntlrQuerySequenceType getMemberType(AntlrQueryTypeFactory typeFactory, ArrayLikeType.TupleType type) {
        return Types.union(typeFactory, type.members());
    }


    public static @Nullable AntlrQueryItemType getMapKey(AntlrQueryTypeFactory typeFactory, AntlrQueryItemType antlrQueryItemType)
    {
        return switch(antlrQueryItemType) {
            case ChoiceItemType choiceItemType -> {
                final AntlrQueryItemType[] memberTypes = Arrays.stream(choiceItemType.itemTypes())
                        .map(it -> Types.getMapKey(typeFactory, it))
                        .filter(Objects::nonNull)
                        .toArray(AntlrQueryItemType[]::new);
                yield ItemTypes.union(typeFactory, memberTypes);
            }
            case MapLikeType.MapType m -> getMapKey(typeFactory, m);
            case MapLikeType.ExtensibleRecordType extensibleRecordType -> getMapKey(typeFactory, extensibleRecordType);
            case AnyItemType _, ArrayLikeType.ArrayType _, AtomicType _, FunctionType _,
                 GrammarEntityType _, MapLikeType.RecordType _, TreeLike _,
                 ArrayLikeType.TupleType _, NeverType _, NothingType _ -> null;
            case NamedItemType(NamespaceResolver.QualifiedName reference) -> getMapKey(
                    typeFactory, typeFactory.guaranteedItemNamedType(reference, new IllegalStateException())
            );
        };

    }

    public static AntlrQueryItemType getMapKey(AntlrQueryTypeFactory typeFactory, MapLikeType.MapType antlrQueryItemType) {
        return antlrQueryItemType.keyType();
    }
    public static AntlrQueryItemType getMapKey(AntlrQueryTypeFactory typeFactory, MapLikeType.ExtensibleRecordType antlrQueryItemType) {
        return typeFactory.itemEnum(antlrQueryItemType.fields().keySet());
    }
    public static AntlrQueryItemType getMapKey(AntlrQueryTypeFactory typeFactory, MapLikeType.RecordType antlrQueryItemType) {
        return typeFactory.itemEnum(antlrQueryItemType.fields().keySet());
    }

    public static @Nullable AntlrQuerySequenceType getMapValue(
            AntlrQueryTypeFactory typeFactory,
            AntlrQueryItemType antlrQueryItemType)
    {
        return switch(antlrQueryItemType) {
            case ChoiceItemType choiceItemType -> {
                final AntlrQuerySequenceType[] memberTypes = Arrays.stream(choiceItemType.itemTypes())
                        .map(it -> Types.getMapValue(typeFactory, it))
                        .filter(Objects::nonNull)
                        .toArray(AntlrQuerySequenceType[]::new);
                yield Types.union(typeFactory, memberTypes);
            }
            case MapLikeType.MapType m -> m.valueType();
            case MapLikeType.RecordType r -> getMapValue(typeFactory, r);
            case MapLikeType.ExtensibleRecordType extensibleRecordType -> getMapValue(typeFactory, extensibleRecordType);
            case AnyItemType _, ArrayLikeType.ArrayType _, AtomicType _, FunctionType _,
                 GrammarEntityType _, TreeLike _,
                 ArrayLikeType.TupleType _, NeverType _, NothingType _ -> null;
            case NamedItemType(NamespaceResolver.QualifiedName reference) -> getMapValue(
                    typeFactory, typeFactory.guaranteedItemNamedType(reference, new IllegalStateException())
            );
        };

    }

    public static AntlrQuerySequenceType getMapValue(AntlrQueryTypeFactory typeFactory, MapLikeType.RecordType recordType) {
        final AntlrQuerySequenceType[] valueTypes = recordType.fields().values().stream()
                .map(r->r.resolveFieldType(typeFactory)).toArray(AntlrQuerySequenceType[]::new);
        return Types.union(typeFactory, valueTypes);
    }

    public static AntlrQuerySequenceType getMapValue(AntlrQueryTypeFactory typeFactory, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        final AntlrQuerySequenceType[] valueTypes = new AntlrQuerySequenceType[extensibleRecordType.fields().size() + 1];
        {
            int i = 0;
            for (RecordField recordField : extensibleRecordType.fields().values()) {
                var resolved = recordField.resolveFieldType(typeFactory);
                valueTypes[i] = resolved;
                i++;
            }
            valueTypes[i] = extensibleRecordType.additionalFieldType();
        }
        return Types.union(typeFactory, valueTypes);
    }

    public static AntlrQuerySequenceType getIndexType(AntlrQueryTypeFactory typeFactory, ArrayLikeType.TupleType tupleType) {
        var indices = Ranges.integers(0, tupleType.members().length);
        return typeFactory.zeroOrMore(typeFactory.itemNumber(indices));
    }

    public static AntlrQuerySequenceType getIndexType(AntlrQueryTypeFactory typeFactory, ArrayLikeType.ArrayType arrayType) {
        var indices = Ranges.integers(Cardinalities.toNumericRange(arrayType.cardinality()));
        return typeFactory.zeroOrMore(typeFactory.itemNumber(indices));
    }

    public enum EffectiveBooleanValueType {
        ALWAYS_FALSE__EMPTY_SEQUENCE,
        ALWAYS_TRUE__NUMBER_STRING_BOOLEAN,
        NUMBER_STRING_BOOLEAN,
        ALWAYS_TRUE__NODE,
        NODE,
        NO_EBV
    }

    public static EffectiveBooleanValueType effectiveBooleanValueType(AntlrQueryTypeFactory typeFactory, AntlrQuerySequenceType type) {
        return switch(type) {
            case final EmptySequence _ -> EffectiveBooleanValueType.ALWAYS_FALSE__EMPTY_SEQUENCE;
            case final NonEmptySequence _ -> {
                final AntlrQueryItemType itemChoice = typeFactory.itemChoice(
                    typeFactory.itemString(),
                    typeFactory.itemBoolean(),
                    typeFactory.itemNumber()
                );

                var variantSingleton = typeFactory.one(itemChoice);
                if (isSubtype(typeFactory, type, variantSingleton)) {
                    yield EffectiveBooleanValueType.ALWAYS_TRUE__NUMBER_STRING_BOOLEAN;
                }

                variantSingleton = typeFactory.zeroOrOne(itemChoice);
                if (isSubtype(typeFactory, type, variantSingleton)) {
                    yield EffectiveBooleanValueType.NUMBER_STRING_BOOLEAN;
                }
                var variantNodes = typeFactory.oneOrMore(typeFactory.itemAnyNode());
                if (isSubtype(typeFactory, type, variantNodes)) {
                    yield EffectiveBooleanValueType.ALWAYS_TRUE__NODE;
                }
                variantNodes = typeFactory.zeroOrMore(typeFactory.itemAnyNode());
                if (isSubtype(typeFactory, type, variantNodes)) {
                    yield EffectiveBooleanValueType.NODE;
                }
                yield EffectiveBooleanValueType.NO_EBV;
            }
        };
    }
    
    public static boolean isSubtype(AntlrQueryTypeFactory typeFactory, final AntlrQuerySequenceType subtype, final AntlrQuerySequenceType supertype) {
        if (!Cardinalities.isSubSet(subtype.cardinality(), supertype.cardinality())) {
            return false;
        }
        return Types.itemTypeIsSubtypeOf(typeFactory, subtype, supertype);
    }


    public static AntlrQuerySequenceType addition(final AntlrQueryTypeFactory typeFactory, final AntlrQuerySequenceType... sequences) {
        final Cardinality[] cardinalities = new Cardinality[sequences.length];
        final AntlrQueryItemType[] itemTypes = new AntlrQueryItemType[sequences.length];
        for (int i = 0; i < sequences.length; i++) {
            cardinalities[i] = sequences[i].cardinality();
            itemTypes[i] = sequences[i].itemType();
        }
        
        final Cardinality mergedCardinality = Cardinalities.add(cardinalities);
        final AntlrQueryItemType mergedItemType = ItemTypes.union(typeFactory, itemTypes);
        return typeFactory.sequence(mergedItemType, mergedCardinality);
    }


    public static boolean hasNoEffectiveBooleanValue(final AntlrQueryTypeFactory typeFactory, final AntlrQuerySequenceType sequence) {
        return effectiveBooleanValueType(typeFactory, sequence) == EffectiveBooleanValueType.NO_EBV;
    }

    public static AntlrQuerySequenceType optionalize(
        final AntlrQueryTypeFactory typeFactory, final AntlrQuerySequenceType sequence)
    {
        @Nullable Cardinality optionalizedCardinality = Cardinalities.optionalize(sequence.cardinality());
        if (optionalizedCardinality == null) {
            return typeFactory.neverType();
        }
        return typeFactory.sequence(sequence.itemType(), optionalizedCardinality);
    }

    public static boolean itemTypeIsSubtypeOf(
            AntlrQueryTypeFactory typeFactory,
            AntlrQuerySequenceType t1,
            AntlrQuerySequenceType t2)
    {
        return ItemTypes.isSubtype(typeFactory, t1.itemType(), t2.itemType());
    }

    public static boolean itemTypeIsSubtypeOf(AntlrQueryTypeFactory typeFactory, AntlrQuerySequenceType type, AntlrQueryItemType itemType) {
        var merger = new ItemTypeIsSubtype(typeFactory);
        return merger.isSubtype(type.itemType(), itemType);
    }



    public enum RelativeCoercibility {
        ALWAYS, POSSIBLE, NEVER
    }
    
    public static boolean isValueComparableWith(AntlrQuerySequenceType type, AntlrQuerySequenceType type2)
    {
        return Cardinalities.areValueComparable(type.cardinality(), type2.cardinality())
                && ItemTypes.areValueComparable(type.itemType(), type2.itemType());
    }

    public static AntlrQuerySequenceType iteratorType(AntlrQueryTypeFactory typeFactory, AntlrQuerySequenceType expr) {
        return typeFactory.one(expr.itemType());
    }

    public static RelativeCoercibility coercibility(
            AntlrQueryTypeFactory typeFactory,
            AntlrQuerySequenceType assignedType,
            AntlrQuerySequenceType desiredType)
    {
        if (Types.isSubtype(typeFactory, assignedType, desiredType)) {
            return RelativeCoercibility.ALWAYS;
        }
        final boolean emptySequenceRequired = Types.isSubtype(typeFactory, desiredType, typeFactory.emptySequence());
        if (emptySequenceRequired) {
            return RelativeCoercibility.NEVER;
        }
        return RelativeCoercibility.POSSIBLE;
    }

    public static String stringify(final AntlrQuerySequenceType type) {
        return switch(type) {
            case AntlrQuerySequenceType.EmptySequence() -> "empty-sequence()";
            case AntlrQuerySequenceType.NonEmptySequence(AntlrQueryItemType itemType, Cardinality cardinality) -> {
                String cardinalityRepr = Cardinalities.stringifyWithPrefix(cardinality);
                if (cardinalityRepr.isEmpty()) {
                    yield ItemTypes.stringifyWithoutParentheses(itemType);
                }
                if (itemType instanceof final FunctionType.ConstrainedFunction cf) {
                    if (!(cf.returnType().itemType() instanceof AnyItemType
                            && cf.returnType().cardinality().equals(Cardinality.ZERO_OR_MORE)))
                    {
                        yield "(" + ItemTypes.stringify(cf) + ")" + cardinalityRepr;
                    }
                }
                yield ItemTypes.stringify(itemType) + cardinalityRepr;
            }
        };
    }

    public static AntlrQuerySequenceType intersect(
            AntlrQueryTypeFactory typeFactory,
            AntlrQuerySequenceType@ArrayLenRange(from = 1)... types)
    {
        assert types.length > 0 : "There were no types given to intersect";
        if (types.length == 1) return types[0];

        // Collect and intersect cardinalities of all sequence types
        Cardinality[] cardinalities = Arrays.stream(types)
                .map(AntlrQuerySequenceType::cardinality)
                .toArray(Cardinality[]::new);

        final @Nullable Cardinality mergedCardinality = Cardinalities.intersection(cardinalities);
        // If the intersected cardinality allows no elements (e.g. range is empty / size 0)
        if (mergedCardinality == null) {
            return typeFactory.emptySequence();
        }
        // Each element of intersection could be optional
        final @Nullable Cardinality optionalized = Cardinalities.optionalize(mergedCardinality);

        // Intersect item types
        AntlrQueryItemType[] itemTypes = Arrays.stream(types)
                .map(AntlrQuerySequenceType::itemType)
                .toArray(AntlrQueryItemType[]::new);

        @Nullable AntlrQueryItemType mergedItemType = ItemTypes.intersect(typeFactory, itemTypes);

        // If item types are completely disjoint (NeverType), return empty sequence or never sequence based on type factory rules
        if (mergedItemType == null) {
            return typeFactory.neverType();
        }
        if (mergedItemType instanceof NothingType) {
            return typeFactory.emptySequence();
        }

        if (optionalized == null) {
            return typeFactory.neverType();
        }
        return typeFactory.sequence(mergedItemType, optionalized);
    }


    public static AntlrQuerySequenceType subtract(
            AntlrQueryTypeFactory typeFactory,
            AntlrQuerySequenceType@ArrayLenRange(from = 1)... types)
    {
        assert types.length > 0;
        if (types.length == 1)
            return types[0];
        var merger = new ItemTypeSubtract(typeFactory);
        AntlrQueryItemType it = Arrays.stream(types).map(AntlrQuerySequenceType::itemType).reduce(merger::subtract).get();
        Cardinality mergeOfSubtractedCardinalities = Arrays.stream(types)
                .skip(1)
                .map(AntlrQuerySequenceType::cardinality)
                .reduce(Cardinalities::union)
                .get();
        @Nullable Cardinality resultingCardinality = Cardinalities.subtract(types[0].cardinality(), mergeOfSubtractedCardinalities);
        if (resultingCardinality == null) {
            return typeFactory.neverType();
        }
        return typeFactory.sequence(it, resultingCardinality);
    }

    public static AntlrQuerySequenceType remove(
            AntlrQueryTypeFactory typeFactory,
            AntlrQuerySequenceType@ArrayLenRange(from = 1)... types)
    {
        assert types.length > 0;
        AntlrQuerySequenceType target = types[0];
        if (types.length == 1)
            return target;

        var removedTypes =
                Arrays.stream(types).skip(1).map(AntlrQuerySequenceType::itemType).toArray(AntlrQueryItemType[]::new);
        var removedTypeUnion = ItemTypes.union(typeFactory, removedTypes);
        @Nullable AntlrQueryItemType itemTypeIntersection = ItemTypes.intersect(typeFactory, target.itemType(), removedTypeUnion);
        if (itemTypeIntersection == null) {
            return target;
        }
        Cardinality mergeOfSubtractedCardinalities = Cardinalities.union(
                Arrays.stream(types).skip(1).map(AntlrQuerySequenceType::cardinality).toArray(Cardinality[]::new)
        );
        @Nullable Cardinality optionalizationOfSubtractedCardinalities = Cardinalities.optionalize(mergeOfSubtractedCardinalities);
        if (optionalizationOfSubtractedCardinalities == null) {
            return target;
        }
        @Nullable Cardinality resultingCardinality = Cardinalities.remove(target.cardinality(), optionalizationOfSubtractedCardinalities);
        if (resultingCardinality == null) {
            return typeFactory.neverType();
        }
        return typeFactory.sequence(target.itemType(), resultingCardinality);
    }


    public static AntlrQuerySequenceType union(
            AntlrQueryTypeFactory typeFactory,
            AntlrQuerySequenceType@ArrayLenRange(from = 1)... types)
    {
        assert types.length > 0;
        if (types.length == 1) return types[0];
        var merger = new ItemTypeUnion(typeFactory);
        Cardinality c = Arrays.stream(types).map(AntlrQuerySequenceType::cardinality).reduce(Cardinalities::union).get();
        AntlrQueryItemType it = Arrays.stream(types).map(AntlrQuerySequenceType::itemType).reduce(merger::union).get();
        return typeFactory.sequence(it, c);
    }




}
