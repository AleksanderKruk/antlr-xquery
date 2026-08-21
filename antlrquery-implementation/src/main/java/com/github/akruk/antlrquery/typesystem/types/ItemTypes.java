package com.github.akruk.antlrquery.typesystem.types;

import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.typeoperations.itemtype.ItemTypeIntersection;
import com.github.akruk.antlrquery.typesystem.typeoperations.itemtype.ItemTypeIsSubtype;
import com.github.akruk.antlrquery.typesystem.typeoperations.itemtype.ItemTypeUnion;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.*;
import com.github.akruk.visitorannotations.Visitor;
import org.checkerframework.checker.nullness.qual.NonNull;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.value.qual.ArrayLenRange;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
@Visitor(name = "AreValueComparable", classes = {AtomicType.class, AtomicType.class})
public final class ItemTypes {
    private ItemTypes(){}

    public static AntlrQueryItemType union(AntlrQueryTypeFactory typeFactory, AntlrQueryItemType@ArrayLenRange(from = 1)... itemTypes) {
        assert itemTypes.length != 0;
        var merger = new ItemTypeUnion(typeFactory);
        return merger.union(itemTypes);
    }

    public static boolean isSubtype(AntlrQueryTypeFactory typeFactory, AntlrQueryItemType tested, AntlrQueryItemType itemAnyItem) {
        var merger = new ItemTypeIsSubtype(typeFactory);
        return merger.isSubtype(tested, itemAnyItem);
    }

    public static @Nullable AntlrQueryItemType intersect(AntlrQueryTypeFactory typeFactory, AntlrQueryItemType@ArrayLenRange(from = 1)... array) {
        return ItemTypeIntersection.intersection(typeFactory, array);
    }

    public static boolean areValueComparable(AntlrQueryItemType type, AntlrQueryItemType type2) {
        boolean lhsEmpty = type instanceof NothingType;
        boolean rhsEmpty = type2 instanceof NothingType;
        if (lhsEmpty && rhsEmpty) return true;
        if (lhsEmpty) return type2 instanceof AtomicType;
        if (rhsEmpty) return type instanceof AtomicType;

        if (!(type instanceof final AtomicType a1) || !(type2 instanceof AtomicType a2)) {
            return false;
        }
        return switch (a1) {
            case NumberType _ when a2 instanceof NumberType -> true;
            case StringType _ when a2 instanceof StringType -> true;
            case BooleanType _ when a2 instanceof BooleanType -> true;
            case RegexType _ when a2 instanceof RegexType -> true;
            default -> false;
        };
    }





}

