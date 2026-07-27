package com.github.akruk.antlrxquery.typesystem.types.itemtypes;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
sealed public interface ConcreteItemType
    extends
        AntlrQueryItemType
    permits
        AtomicType,
        ArrayLikeType,
        FunctionType,
        MapLikeType,
        GrammarEntityType,
        TreeLike
{
}

