package com.github.akruk.antlrquery.typesystem.types.itemtypes;

import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.ItemTypes;
import org.checkerframework.checker.nullness.qual.NonNull;

public sealed interface ArrayLikeType
        extends ConcreteItemType
        permits ArrayLikeType.ArrayType,
        ArrayLikeType.TupleType
{
    /**
     * Any array = ArrayType
     * where memberType == AnyItemType
     * and cardinality == ZERO_OR_MORE
     */
    record ArrayType(
            AntlrQuerySequenceType memberType,
            Cardinality cardinality
    ) implements ArrayLikeType
    {
        @Override
        public @NonNull String toString() {
            return ItemTypes.stringify(this);
        }

    }

    record TupleType(
            AntlrQuerySequenceType[] members
    ) implements ArrayLikeType
    {
        @Override
        public @NonNull String toString() {
            return ItemTypes.stringify(this);
        }
    }
}
