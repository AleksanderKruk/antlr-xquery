package com.github.akruk.antlrquery.typesystem.types.itemtypes;

import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.ItemTypes;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Arrays;

@DefaultQualifier(NonNull.class)
public sealed interface ArrayLikeType
        extends ConcreteItemType
        permits ArrayLikeType.ArrayType,
        ArrayLikeType.TupleType
{
    Cardinality cardinality();

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
        public String toString() {
            return ItemTypes.stringify(this);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ArrayType(AntlrQuerySequenceType type, Cardinality cardinality1)
                    && type.equals(this.memberType)
                    && cardinality1.equals(this.cardinality);
        }
    }

    record TupleType(
            AntlrQuerySequenceType[] members
    ) implements ArrayLikeType
    {
        @Override
        public String toString() {
            return ItemTypes.stringify(this);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof TupleType(AntlrQuerySequenceType[] members1)
                    && Arrays.equals(this.members, members1);
        }

        @Override
        public Cardinality cardinality() {
            return Cardinality.of(members.length);
        }

    }
}
