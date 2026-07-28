package com.github.akruk.antlrquery.typesystem.typeoperations;

import com.github.akruk.antlrquery.typesystem.RecordField;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Ranges;
import com.github.akruk.antlrquery.typesystem.types.*;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.*;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Set;


@DefaultQualifier(NonNull.class)
public class LookupOperation {
    private final AntlrQueryTypeFactory typeFactory;
    private final AntlrQuerySequenceType emptySequence;
    private final AntlrQuerySequenceType zeroOrMoreItems;

    public LookupOperation(AntlrQueryTypeFactory typeFactory) {
        this.typeFactory = typeFactory;
        this.emptySequence = typeFactory.emptySequence();
        this.zeroOrMoreItems = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
    }


    public sealed interface LookupSemanticResult
            permits LookupSemanticResult.LookupError, LookupSemanticResult.Success {

        record Success(AntlrQuerySequenceType resultingType)
                implements LookupSemanticResult {}

        sealed interface LookupError
                extends LookupSemanticResult
                permits
                LookupError.EmptyTarget,
                LookupError.InvalidTarget,
                LookupError.KeyEmpty,
                LookupError.InvalidChoiceItem
        {
            AntlrQuerySequenceType resultingType();

            record KeyEmpty(AntlrQuerySequenceType resultingType) implements LookupError {}

            record InvalidTarget(AntlrQuerySequenceType resultingType, AntlrQuerySequenceType target) implements LookupError {}

            record EmptyTarget(AntlrQuerySequenceType resultingType, AntlrQuerySequenceType target) implements LookupError {}

            record InvalidChoiceItem(AntlrQuerySequenceType resultingType, LookupError innerError, AntlrQuerySequenceType target) implements LookupError {}
        }
    }

    //         (map | record | extensible-record) ?  *
    //         (tuple | array) ? *
    //         choice[...]
    public LookupSemanticResult lookupWildcard(
            AntlrQuerySequenceType mapLikeTypeSequence
    )
    {
        switch(mapLikeTypeSequence.itemType()) {
            case NothingType() -> { return new LookupSemanticResult.LookupError.EmptyTarget(zeroOrMoreItems, mapLikeTypeSequence); }
            case NeverType() -> { return new LookupSemanticResult.Success(mapLikeTypeSequence); }
            case ChoiceItemType choice -> {
                final List<AntlrQuerySequenceType> results = new java.util.ArrayList<>();
                for (final AntlrQueryItemType itemType : choice.itemTypes()) {
                    final AntlrQuerySequenceType singleSubSequence = typeFactory.sequence(itemType, mapLikeTypeSequence.cardinality());
                    final LookupSemanticResult subResult = lookupWildcard(singleSubSequence);

                    switch (subResult) {
                        case LookupSemanticResult.Success success -> results.add(success.resultingType());
                        case LookupSemanticResult.LookupError error -> {
                            if (error instanceof LookupSemanticResult.LookupError.InvalidTarget invalidTarget) {
                                return new LookupSemanticResult.LookupError.InvalidChoiceItem(
                                        invalidTarget.resultingType(),
                                        invalidTarget,
                                        mapLikeTypeSequence
                                );
                            } else {
                                if (error.resultingType() != null) {
                                    results.add(error.resultingType());
                                }
                            }
                        }
                    }
                }
                if (results.isEmpty()) {
                    return new LookupSemanticResult.Success(emptySequence);
                }
                final AntlrQuerySequenceType mergedResult = Types.union(
                        typeFactory,
                        results.toArray(new AntlrQuerySequenceType[0])
                );
                return new LookupSemanticResult.Success(mergedResult);
            }
            case ArrayLikeType.ArrayType a -> {
                final Cardinality arrayLength = a.cardinality();
                final NumericRange numericRange = Ranges.indices(Cardinalities.toNumericRange(arrayLength));
                final AntlrQuerySequenceType wildcardKeyType = typeFactory.sequence(typeFactory.itemNumber(numericRange), a.cardinality());
                return lookupNonWildcardArray(mapLikeTypeSequence.cardinality(), a, wildcardKeyType);
            }
            case ArrayLikeType.TupleType t -> {
                final var merged = Types.union(typeFactory, t.members());
                final var multiplied = typeFactory.sequence(merged.itemType(), Cardinalities.multiply(merged.cardinality(), mapLikeTypeSequence.cardinality()));
                return new LookupSemanticResult.Success(multiplied);
            }
            case MapLikeType.MapType m -> {
                final AntlrQuerySequenceType wildcardKeyType = typeFactory.oneOrMore(m.keyType());
                return lookupNonWildcardMapType(mapLikeTypeSequence.cardinality(), m, wildcardKeyType);
            }
            case MapLikeType.RecordType record -> {
                final AntlrQueryItemType keyEnumItemType = typeFactory.itemString();
                final AntlrQuerySequenceType wildcardKeyType = typeFactory.oneOrMore(keyEnumItemType);
                return lookupNonWildcardRecord(mapLikeTypeSequence.cardinality(), record, wildcardKeyType);
            }
            case MapLikeType.ExtensibleRecordType extensibleRecord -> {
                final Map<String, RecordField> recordFields = extensibleRecord.fields();
                final AntlrQueryItemType keyEnumItemType = typeFactory.itemEnum(recordFields.keySet());
                final AntlrQuerySequenceType wildcardKeyType = typeFactory.zeroOrMore(keyEnumItemType);
                return lookupNonWildcardExtensibleRecord(mapLikeTypeSequence.cardinality(), extensibleRecord, wildcardKeyType);
            }
            case AnyItemType _,
                 AtomicType _,
                 FunctionType _,
                 GrammarEntityType _,
                 TreeLike _
                    -> { return new LookupSemanticResult.LookupError.InvalidTarget(zeroOrMoreItems, mapLikeTypeSequence); }
        }
    }

    // (map | record | extensible-record) ?  KeyType
    // (tuple | array) ? KeyType
    // choice[...] ? KeyType
    public LookupSemanticResult lookupNonWildcard(
            AntlrQuerySequenceType mapLikeTypeSequence,
            AntlrQuerySequenceType keyTypeSequence
    )
    {
        if (keyTypeSequence.cardinality().equals(Cardinality.ZERO)) {
            return new LookupSemanticResult.LookupError.KeyEmpty(emptySequence);
        }
        switch(mapLikeTypeSequence.itemType()) {
            case NothingType() -> { return new LookupSemanticResult.LookupError.EmptyTarget(zeroOrMoreItems, mapLikeTypeSequence); }
            case NeverType() -> { return new LookupSemanticResult.Success(mapLikeTypeSequence); }
            case ChoiceItemType choice -> {
                final List<AntlrQuerySequenceType> results = new java.util.ArrayList<>();
                for (final AntlrQueryItemType itemType : choice.itemTypes()) {
                    final AntlrQuerySequenceType singleSubSequence = typeFactory.sequence(itemType, mapLikeTypeSequence.cardinality());
                    final LookupSemanticResult subResult = lookupNonWildcard(singleSubSequence, keyTypeSequence);

                    switch (subResult) {
                        case LookupSemanticResult.Success success -> results.add(success.resultingType());
                        case LookupSemanticResult.LookupError error -> {
                            if (error instanceof final LookupSemanticResult.LookupError.InvalidTarget invalidTarget) {
                                return new LookupSemanticResult.LookupError.InvalidChoiceItem(
                                        invalidTarget.resultingType(),
                                        invalidTarget,
                                        mapLikeTypeSequence
                                );
                            } else {
                                if (error.resultingType() != null) {
                                    results.add(error.resultingType());
                                }
                            }
                        }
                    }
                }
                if (results.isEmpty()) {
                    return new LookupSemanticResult.Success(emptySequence);
                }
                final AntlrQuerySequenceType mergedResult = Types.union(
                        typeFactory,
                        results.toArray(new AntlrQuerySequenceType[0])
                );
                return new LookupSemanticResult.Success(mergedResult);
            }
            case ArrayLikeType.ArrayType a -> {
                return lookupNonWildcardArray(mapLikeTypeSequence.cardinality(), a, keyTypeSequence);
            }
            case ArrayLikeType.TupleType t -> {
                return lookupNonWildcardTuple(mapLikeTypeSequence.cardinality(), t, keyTypeSequence);
            }
            case MapLikeType.MapType m -> {
                return lookupNonWildcardMapType(mapLikeTypeSequence.cardinality(), m, keyTypeSequence);
            }
            case MapLikeType.RecordType record -> {
                return lookupNonWildcardRecord(mapLikeTypeSequence.cardinality(), record, keyTypeSequence);
            }
            case MapLikeType.ExtensibleRecordType extensibleRecord -> {
                return lookupNonWildcardExtensibleRecord(mapLikeTypeSequence.cardinality(), extensibleRecord, keyTypeSequence);
            }
            case AnyItemType _,
                 AtomicType _,
                 FunctionType _,
                 GrammarEntityType _,
                 TreeLike _
                    -> { return new LookupSemanticResult.LookupError.InvalidTarget(zeroOrMoreItems, mapLikeTypeSequence); }
        }
    }


    private LookupSemanticResult lookupNonWildcardMapType(
            Cardinality inputSequenceCardinality,
            MapLikeType.MapType m,
            AntlrQuerySequenceType keyTypeSequence)
    {
        final AntlrQuerySequenceType expectedKeyItemType = typeFactory.zeroOrMore(m.keyType());

        if (!Types.isSubtype(typeFactory, keyTypeSequence, expectedKeyItemType)) {
            return new LookupSemanticResult.LookupError.InvalidTarget(zeroOrMoreItems, keyTypeSequence);
        }

        final AntlrQuerySequenceType valueType = m.valueType();
        final Cardinality keyCardinality = keyTypeSequence.cardinality();
        final Cardinality resultingCardinality = Cardinalities.optionalize(Cardinalities.multiply(inputSequenceCardinality, keyCardinality));
        final AntlrQuerySequenceType resultingType = typeFactory.sequence(valueType.itemType(), resultingCardinality);

        return new LookupSemanticResult.Success(resultingType);
    }



    /*
     * array(MemberType) ? KeyType
     */
    private LookupSemanticResult lookupNonWildcardArray(
            Cardinality inputSequenceCardinality, ArrayLikeType.ArrayType a, AntlrQuerySequenceType keyTypeSequence)
    {
        final Cardinality arrayLength = a.cardinality();
        final NumericRange numericRange = Ranges.indices(Cardinalities.toNumericRange(arrayLength));
        final AntlrQuerySequenceType expectedKeyItemType = typeFactory.zeroOrMore(typeFactory.itemNumber(numericRange));

        if (!Types.isSubtype(typeFactory, keyTypeSequence, expectedKeyItemType)) {
            return new LookupSemanticResult.LookupError.InvalidTarget(zeroOrMoreItems, keyTypeSequence);
        }

        final AntlrQuerySequenceType memberType = a.memberType();
        final Cardinality keyCardinality = keyTypeSequence.cardinality();
        final Cardinality resultingCardinality = Cardinalities.multiply(inputSequenceCardinality, keyCardinality);
        final AntlrQuerySequenceType resultingType = typeFactory.sequence(memberType.itemType(), resultingCardinality);

        return new LookupSemanticResult.Success(resultingType);
    }

    private LookupSemanticResult lookupNonWildcardTuple(
            Cardinality inputSequenceCardinality,
            ArrayLikeType.TupleType t,
            AntlrQuerySequenceType keyTypeSequence)
    {
        final int tupleSize = t.members().length;
        final NumericRange numericRange = Ranges.indices(0, tupleSize);
        final AntlrQuerySequenceType expectedKeyItemType = typeFactory.zeroOrMore(typeFactory.itemNumber(numericRange));

        if (!Types.isSubtype(typeFactory, keyTypeSequence, expectedKeyItemType)) {
            return new LookupSemanticResult.LookupError.InvalidTarget(zeroOrMoreItems, keyTypeSequence);
        }

        final AntlrQuerySequenceType[] members = t.members();
        if (members.length == 0) {
            return new LookupSemanticResult.Success(emptySequence);
        }

        final AntlrQuerySequenceType matchedType;
        final AntlrQueryItemType keyItemType = keyTypeSequence.itemType();

        if (keyItemType instanceof AtomicType.NumberType(NumericRange range)) {

            int lowest = -1;
            for (int i = tupleSize - 1; i >= 0; i--) {
                if (Ranges.contains(range, i)) {
                    lowest = i;
                    break;
                }
            }
            if (lowest == -1) {
                matchedType = emptySequence;
            } else {
                final AntlrQuerySequenceType[] matchedMembers = new AntlrQuerySequenceType[lowest+1];
                for (int i = lowest - 1; i >= 0; i--) {
                    if (Ranges.contains(range, i)) {
                        matchedMembers[i] = members[i];
                    }
                }
                matchedType = Types.union(typeFactory, matchedMembers);
            }
        } else {
            matchedType = Types.union(typeFactory, members);
        }

        final Cardinality keyCardinality = keyTypeSequence.cardinality();
        final Cardinality resultingCardinality = Cardinalities.multiply(inputSequenceCardinality, keyCardinality);
        final AntlrQuerySequenceType resultingType = typeFactory.sequence(matchedType.itemType(), resultingCardinality);

        return new LookupSemanticResult.Success(resultingType);
    }

    private LookupSemanticResult lookupNonWildcardExtensibleRecord(
            Cardinality inputSequenceCardinality,
            MapLikeType.ExtensibleRecordType extensibleRecord,
            AntlrQuerySequenceType keyTypeSequence)
    {
        final Map<String, RecordField> recordFields = extensibleRecord.fields();
        if (!(keyTypeSequence.itemType() instanceof final StringType stringType)) {
            return new LookupSemanticResult.LookupError.InvalidTarget(zeroOrMoreItems, keyTypeSequence);
        }

        final AntlrQuerySequenceType matchedType;
        switch (stringType) {
            case StringType.StringEnum stringEnum -> {
                final Set<String> members = stringEnum.members();
                final List<AntlrQuerySequenceType> matchedFields = new java.util.ArrayList<>();
                for (final String member : members) {
                    final RecordField field = recordFields.get(member);
                    if (field != null) {
                        AntlrQuerySequenceType fType = field.resolveFieldType(typeFactory);
                        matchedFields.add(field.isRequired() ? fType : Types.union(typeFactory, fType, emptySequence));
                    } else {
                        matchedFields.add(extensibleRecord.additionalFieldType());
                    }
                }
                matchedType = matchedFields.isEmpty()
                        ? extensibleRecord.additionalFieldType()
                        : Types.union(typeFactory, matchedFields.toArray(new AntlrQuerySequenceType[0]));
            }
            case StringType.StringNonEnum _ -> {
                final AntlrQuerySequenceType mergedFields = recordFields.isEmpty()
                        ? extensibleRecord.additionalFieldType()
                        : Types.union(
                        typeFactory,
                        recordFields.values().stream()
                                .map(x -> x.isRequired()
                                        ? x.resolveFieldType(typeFactory)
                                        : Types.union(typeFactory, x.resolveFieldType(typeFactory), emptySequence))
                                .toArray(AntlrQuerySequenceType[]::new)
                );
                matchedType = Types.union(typeFactory, mergedFields, extensibleRecord.additionalFieldType());
            }
        }

        final Cardinality keyCardinality = keyTypeSequence.cardinality();
        final Cardinality resultingCardinality = Cardinalities.multiply(inputSequenceCardinality, keyCardinality);
        final AntlrQuerySequenceType resultingType = typeFactory.sequence(matchedType.itemType(), resultingCardinality);

        return new LookupSemanticResult.Success(resultingType);
    }

    private LookupSemanticResult lookupNonWildcardRecord(
            Cardinality inputSequenceCardinality,
            MapLikeType.RecordType record,
            AntlrQuerySequenceType keyTypeSequence)
    {
        final Map<String, RecordField> recordFields = record.fields();
        if (!(keyTypeSequence.itemType() instanceof final StringType stringType)) {
            return new LookupSemanticResult.LookupError.InvalidTarget(zeroOrMoreItems, keyTypeSequence);
        }

        final AntlrQuerySequenceType matchedType;
        switch (stringType) {
            case StringType.StringEnum stringEnum -> {
                final Set<String> members = stringEnum.members();
                final List<AntlrQuerySequenceType> matchedFields = new java.util.ArrayList<>();
                for (final String member : members) {
                    final RecordField field = recordFields.get(member);
                    if (field != null) {
                        AntlrQuerySequenceType fType = field.resolveFieldType(typeFactory);
                        matchedFields.add(
                                field.isRequired()
                                    ? fType
                                    : Types.union(typeFactory, fType, emptySequence)
                        );
                    }
                }
                matchedType = matchedFields.isEmpty()
                        ? emptySequence
                        : Types.union(typeFactory, matchedFields.toArray(new AntlrQuerySequenceType[0]));
            }
            case StringType.StringNonEnum _ ->
                    matchedType = recordFields.isEmpty()
                        ? emptySequence
                        : Types.union(
                            typeFactory,
                            recordFields.values().stream()
                                    .map(x -> x.isRequired()
                                            ? x.resolveFieldType(typeFactory)
                                            : Types.union(typeFactory, x.resolveFieldType(typeFactory), emptySequence) )
                                    .toArray(AntlrQuerySequenceType[]::new)
            );
        }

        final Cardinality keyCardinality = keyTypeSequence.cardinality();
        final Cardinality resultingCardinality = Cardinalities.multiply(inputSequenceCardinality, keyCardinality);
        final AntlrQuerySequenceType resultingType = typeFactory.sequence(matchedType.itemType(), resultingCardinality);

        return new LookupSemanticResult.Success(resultingType);
    }

}
