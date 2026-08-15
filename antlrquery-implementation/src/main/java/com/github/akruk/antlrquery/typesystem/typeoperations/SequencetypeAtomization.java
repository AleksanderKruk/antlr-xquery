package com.github.akruk.antlrquery.typesystem.typeoperations;

import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.*;

public class SequencetypeAtomization {
    private final AntlrQuerySequenceType anyItems;
    private final AntlrQueryTypeFactory typeFactory;

    public SequencetypeAtomization(AntlrQueryTypeFactory typeFactory) {
        this.typeFactory = typeFactory;
        this.anyItems = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
    }

    public AntlrQuerySequenceType atomize(AntlrQuerySequenceType type) {
        return switch(type) {
            case AntlrQuerySequenceType.EmptySequence() -> type;
            case AntlrQuerySequenceType.NonEmptySequence(AntlrQueryItemType itemType, Cardinality cardinality) -> atomizeItemType(type, itemType, cardinality);
        };
    }

    private AntlrQuerySequenceType atomizeItemType(AntlrQuerySequenceType input, AntlrQueryItemType itemType, Cardinality cardinality) {
        return switch(itemType) {
            case ConcreteItemType r -> switch(r) {
                case AtomicType a -> input;
                case ArrayLikeType.ArrayType(AntlrQuerySequenceType memberType, Cardinality length) -> {
                    final Cardinality memberCardinalityTimesArrayLength = Cardinalities.multiply(memberType.cardinality(), length);
                    yield typeFactory.sequence(memberType.itemType(), memberCardinalityTimesArrayLength);
                }
                case ArrayLikeType.TupleType(AntlrQuerySequenceType[] memberTypes) -> {
                    AntlrQuerySequenceType result = typeFactory.emptySequence();
                    for (AntlrQuerySequenceType memberType : memberTypes) {
                        AntlrQuerySequenceType atomized = atomize(memberType);
                        result = result == typeFactory.emptySequence() ? atomized : Types.union(typeFactory, result, atomized);
                    }
                    yield result;
                }
                case MapLikeType.MapType _ -> anyItems;
                case MapLikeType.RecordType _ -> anyItems;
                case MapLikeType.ExtensibleRecordType _ -> anyItems;
                case FunctionType _ -> anyItems;
                case GrammarEntityType _ -> anyItems;
                case TreeLike _ -> anyItems;
            };
            case ChoiceItemType c -> {
                AntlrQuerySequenceType result = typeFactory.emptySequence();
                for (AntlrQueryItemType memberItemType : c.itemTypes()) {
                    AntlrQuerySequenceType atomized = atomizeItemType(input, memberItemType, cardinality);
                    result = result == typeFactory.emptySequence() ? atomized : Types.union(typeFactory, result, atomized);
                }
                yield result;
            }
            case AnyItemType() -> anyItems;
            case NothingType() -> input;
            case NeverType() -> input;
            case NamedItemType namedItemType ->
                    typeFactory.sequence(typeFactory.guaranteedItemNamedType(namedItemType.reference(), new IllegalStateException()), cardinality);
        };
    }


}
