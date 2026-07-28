package com.github.akruk.antlrquery.typesystem.types.itemtypes;

import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;

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
    {}

    record TupleType(
            AntlrQuerySequenceType[] members
    ) implements ArrayLikeType
    {}
}
