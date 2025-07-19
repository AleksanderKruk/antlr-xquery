package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.typeoperations.ItemtypeAlternativeMerger;
import com.github.akruk.antlrxquery.typesystem.typeoperations.ItemtypeIntersectionMerger;
import com.github.akruk.antlrxquery.typesystem.typeoperations.ItemtypeSubtyper;
import com.github.akruk.antlrxquery.typesystem.typeoperations.ItemtypeUnionMerger;

public class XQueryItemType {
    private static final int ANY_ITEM = XQueryTypes.ANY_ITEM.ordinal();
    private static final int STRING = XQueryTypes.STRING.ordinal();
    private static final int ENUM = XQueryTypes.ENUM.ordinal();
    private static final int BOOLEAN = XQueryTypes.BOOLEAN.ordinal();
    private static final int NUMBER = XQueryTypes.NUMBER.ordinal();
    private final XQueryTypes type;
    private final int typeOrdinal;


    private final List<XQuerySequenceType> argumentTypes;
    private final XQuerySequenceType returnedType;
    private final XQueryItemType mapKeyType;
    private final XQuerySequenceType mapValueType;
    private final Set<String> elementNames;
    private final XQuerySequenceType returnedType_;

    private final XQueryTypeFactory typeFactory;
    private final Collection<XQueryItemType> itemTypes;
    private final ItemtypeUnionMerger unionMerger;
    private final ItemtypeSubtyper itemtypeSubtyper;
    private final ItemtypeAlternativeMerger alternativeMerger;
    private final ItemtypeIntersectionMerger intersectionMerger;

    public Collection<XQueryItemType> getItemTypes() {
        return itemTypes;
    }

    public XQueryItemType(final XQueryTypes type,
                                final List<XQuerySequenceType> argumentTypes,
                                final XQuerySequenceType returnedType,
                                final XQuerySequenceType arrayType,
                                final XQueryItemType key,
                                final XQuerySequenceType mapValueType,
                                final Set<String> elementNames,
                                final XQueryTypeFactory typeFactory,
                                final Collection<XQueryItemType> itemTypes)
    {
        this.type = type;
        this.typeOrdinal = type.ordinal();
        this.argumentTypes = argumentTypes;
        this.returnedType = returnedType;
        // this.arrayType = arrayType;
        this.mapKeyType = key;
        this.mapValueType = mapValueType;
        this.elementNames = elementNames;
        this.typeFactory = typeFactory;
        this.itemTypes = itemTypes;
        this.returnedType_ = getReturnedType_();
        this.alternativeMerger = new ItemtypeAlternativeMerger(typeOrdinal, typeFactory);
        this.unionMerger = new ItemtypeUnionMerger(typeOrdinal, typeFactory);
        this.intersectionMerger = new ItemtypeIntersectionMerger(typeOrdinal, typeFactory);
        this.itemtypeSubtyper = new ItemtypeSubtyper(this, typeFactory);
    }

    public XQueryItemType()
    {
        this.type = null;
        this.typeOrdinal = 0;
        this.argumentTypes = null;
        this.returnedType = null;
        // this.arrayType = arrayType;
        this.mapKeyType = null;
        this.mapValueType = null;
        this.elementNames = null;
        this.typeFactory = null;
        this.itemTypes = null;
        this.returnedType_ = null;
        this.alternativeMerger = null;
        this.unionMerger = null;
        this.intersectionMerger = null;
        this.itemtypeSubtyper = null;
    }

    public XQueryItemType getMapKeyType() {
        return mapKeyType;
    }
    public XQuerySequenceType getMapValueType() {
        return mapValueType;
    }
    public List<XQuerySequenceType> getArgumentTypes() {
        return argumentTypes;
    }

    public XQuerySequenceType getReturnedType() {
        return this.returnedType_;
    }

    private XQuerySequenceType getReturnedType_() {
        return returnedType;
    }

    public Set<String> getElementNames() {
        return elementNames;
    }

    public XQueryTypes getType() {
        return type;
    }

    private static boolean isNullableEquals(final Object one, final Object other) {
        if (one != null)
            return one.equals(other);
        return one == other;
    }

    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof XQueryItemType other))
            return false;
        if (type != other.getType())
            return false;
        final var otherArgumentTypes = other.getArgumentTypes();
        if (this.argumentTypes == null && otherArgumentTypes != null)
            return false;
        if (this.argumentTypes != null) {
            if (this.argumentTypes.size() != otherArgumentTypes.size())
                return false;
            for (int i = 0; i < this.argumentTypes.size(); i++) {
                final XQuerySequenceType thisArg = this.argumentTypes.get(i);
                final XQuerySequenceType otherArg = otherArgumentTypes.get(i);
                if (!thisArg.equals(otherArg)) {
                    return false;
                }
            }
        }
        final XQuerySequenceType otherReturnedType = other.getReturnedType();
        if (!isNullableEquals(this.returnedType, otherReturnedType))
            return false;

        final var names = other.getElementNames();
        if (names != null &&elementNames != null && !this.elementNames.containsAll(names)) {
            return false;
        }

        if (!isNullableEquals(getArrayMemberType(), other.getArrayMemberType()))
            return false;

        if (!isNullableEquals(mapKeyType, other.getMapKeyType()))
            return false;

        if (!isNullableEquals(mapValueType, other.getMapValueType()))
            return false;

        final var otherTypes = other.itemTypes;
        if (itemTypes != null && otherTypes != null && !this.itemTypes.containsAll(otherTypes))
            return false;
        return true;
    }
    private static final int typesCount = XQueryTypes.values().length;

    public boolean itemtypeIsSubtypeOf(final XQueryItemType obj) {
        if (!(obj instanceof XQueryItemType))
            return false;
        final XQueryItemType typed = (XQueryItemType) obj;
        return itemtypeSubtyper.itemtypeIsSubtypeOf(this, typed);
    }


    private static boolean[] booleanEnumArray(final XQueryTypes... values) {
        final var array = new boolean[XQueryTypes.values().length];
        for (final var v : values) {
            array[v.ordinal()] = true;
        }
        return array;
    }

    private static final boolean[] isFunction = booleanEnumArray(XQueryTypes.ANY_FUNCTION, XQueryTypes.FUNCTION);

    public boolean isFunction(final XQuerySequenceType otherReturnedType, final List<XQuerySequenceType> otherArgumentTypes) {
        return isFunction[typeOrdinal]
                && this.returnedType.equals(otherReturnedType)
                && this.argumentTypes.size() == otherArgumentTypes.size()
                && IntStream.range(0, this.argumentTypes.size())
                        .allMatch(i -> this.argumentTypes.get(i).equals(otherArgumentTypes.get(i)));
    }

    private static final boolean[] noEffectiveBooleanValue = booleanEnumArray(XQueryTypes.FUNCTION,
                                                                                XQueryTypes.ANY_ARRAY,
                                                                                XQueryTypes.MAP,
                                                                                XQueryTypes.ANY_MAP,
                                                                                XQueryTypes.ARRAY,
                                                                                XQueryTypes.ANY_ARRAY);
    public boolean hasEffectiveBooleanValue() {
        if (type == XQueryTypes.CHOICE) {
            return itemTypes.stream().allMatch(itemType->itemType.hasEffectiveBooleanValue());
        }
        return !noEffectiveBooleanValue[typeOrdinal];
    }

    private static final boolean[][] castableAs;
    static {
        castableAs = new boolean[typesCount][typesCount];
        for (int i = 0; i < typesCount; i++) {
            for (int j = 0; j < typesCount; j++) {
                castableAs[i][j] = i == j;
            }
        }
        // final int number = XQueryTypes.NUMBER.ordinal();
        final int string = STRING;
        final int anyItem = ANY_ITEM;
        for (int i = 0; i < typesCount; i++) {
            castableAs[i][anyItem] = true;
            castableAs[i][string] = true;
        }
    }

    public boolean castableAs(final XQueryItemType itemType) {
        if (!(itemType instanceof final XQueryItemType typed))
            return false;
        return castableAs[typeOrdinal][typed.getType().ordinal()];
    }

    public XQueryItemType unionMerge(final XQueryItemType other) {
        return unionMerger.unionMerge(this, other);
    }

    public XQueryItemType intersectionMerge(final XQueryItemType other) {
        return intersectionMerger.intersectionMerge(this, other);
    }

    public XQueryItemType exceptionMerge(final XQueryItemType other) {
        return this;
    }

    private static final boolean[][] isValueComparableWith;
    static {
        isValueComparableWith = new boolean[typesCount][typesCount];
        isValueComparableWith[STRING][STRING] = true;
        isValueComparableWith[NUMBER][NUMBER] = true;
        isValueComparableWith[BOOLEAN][BOOLEAN] = true;
        isValueComparableWith[ENUM][STRING] = true;
        isValueComparableWith[STRING][ENUM] = true;
        isValueComparableWith[ENUM][ENUM] = true;
    }


    public boolean isValueComparableWith(final XQueryItemType other) {
        final XQueryItemType other_ = (XQueryItemType) other;
        return isValueComparableWith[typeOrdinal][other_.getType().ordinal()];
    }

    public XQueryItemType alternativeMerge(XQueryItemType other) {
        return  alternativeMerger.alternativeMerge(this, other);
    }

    public XQuerySequenceType lookup(XQuerySequenceType keySpecifierType) {
        return null;
    }
    public XQuerySequenceType getArrayMemberType() {
        return null;
    }
}
