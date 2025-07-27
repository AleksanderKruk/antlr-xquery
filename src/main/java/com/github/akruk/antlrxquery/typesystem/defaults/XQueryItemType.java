package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;

import com.github.akruk.antlrxquery.typesystem.XQueryRecordField;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.typeoperations.itemtype.ItemtypeAlternativeMerger;
import com.github.akruk.antlrxquery.typesystem.typeoperations.itemtype.ItemtypeIntersectionMerger;
import com.github.akruk.antlrxquery.typesystem.typeoperations.itemtype.ItemtypeStringRepresentation;
import com.github.akruk.antlrxquery.typesystem.typeoperations.itemtype.ItemtypeSubtyper;
import com.github.akruk.antlrxquery.typesystem.typeoperations.itemtype.ItemtypeUnionMerger;

public class XQueryItemType {
    private static final int ANY_ITEM = XQueryTypes.ANY_ITEM.ordinal();
    private static final int STRING = XQueryTypes.STRING.ordinal();
    private static final int ENUM = XQueryTypes.ENUM.ordinal();
    private static final int BOOLEAN = XQueryTypes.BOOLEAN.ordinal();
    private static final int NUMBER = XQueryTypes.NUMBER.ordinal();

    public final XQueryTypes type;
    public final int typeOrdinal;


    public final List<XQuerySequenceType> argumentTypes;
    public final XQuerySequenceType returnedType;
    public final XQuerySequenceType arrayMemberType;
    public final XQueryItemType mapKeyType;
    public final XQuerySequenceType mapValueType;
    public final Set<String> elementNames;
    public final Map<String, XQueryRecordField> recordFields;
    public final Collection<XQueryItemType> itemTypes;
    public final boolean hasEffectiveBooleanValue;

    // private final XQueryTypeFactory typeFactory;
    private final ItemtypeUnionMerger unionMerger;
    private final ItemtypeSubtyper itemtypeSubtyper;
    private final ItemtypeAlternativeMerger alternativeMerger;
    private final ItemtypeIntersectionMerger intersectionMerger;
    private final Function<XQueryItemType, String> representationOp;

    public XQueryItemType(final XQueryTypes type,
                                final List<XQuerySequenceType> argumentTypes,
                                final XQuerySequenceType returnedType,
                                final XQuerySequenceType arrayType,
                                final XQueryItemType key,
                                final XQuerySequenceType mapValueType,
                                final Set<String> elementNames,
                                final XQueryTypeFactory typeFactory,
                                final Collection<XQueryItemType> itemTypes,
                                final Map<String, XQueryRecordField> recordFields,
                                final XQuerySequenceType arrayMemberType,
                                final Set<String> enumMembers)
    {
        this.type = type;
        this.arrayMemberType = arrayMemberType;
        this.recordFields = recordFields;
        this.typeOrdinal = type.ordinal();
        this.argumentTypes = argumentTypes;
        this.returnedType = returnedType;
        // this.arrayType = arrayType;
        this.mapKeyType = key;
        this.mapValueType = mapValueType;
        this.elementNames = elementNames;
        // this.typeFactory = typeFactory;
        this.itemTypes = itemTypes;
        this.alternativeMerger = new ItemtypeAlternativeMerger(typeOrdinal, typeFactory);
        this.unionMerger = new ItemtypeUnionMerger(typeOrdinal, typeFactory);
        this.intersectionMerger = new ItemtypeIntersectionMerger(typeOrdinal, typeFactory);
        this.itemtypeSubtyper = new ItemtypeSubtyper(this, typeFactory);
        this.representationOp = representationProvider.getOperation(type);
        this.enumMembers = enumMembers;
        this.hasEffectiveBooleanValue = hasEffectiveBooleanValue();
    }

    protected XQueryItemType()
    {
        this.type = null;
        this.typeOrdinal = 0;
        this.argumentTypes = null;
        this.returnedType = null;
        this.arrayMemberType = null;
        // this.arrayType = arrayType;
        this.mapKeyType = null;
        this.mapValueType = null;
        this.elementNames = null;
        this.recordFields = null;
        // this.typeFactory = null;
        this.itemTypes = null;
        this.alternativeMerger = null;
        this.unionMerger = null;
        this.intersectionMerger = null;
        this.itemtypeSubtyper = null;
        this.representationOp = null;
        this.enumMembers = null;
        this.hasEffectiveBooleanValue = false;
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
        if (type != other.type)
            return false;
        final var otherArgumentTypes = other.argumentTypes;
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
        final XQuerySequenceType otherReturnedType = other.returnedType;
        if (!isNullableEquals(this.returnedType, otherReturnedType))
            return false;

        final var names = other.elementNames;
        if (names != null &&elementNames != null && !this.elementNames.containsAll(names)) {
            return false;
        }

        if (!isNullableEquals(arrayMemberType, other.arrayMemberType))
            return false;

        if (!isNullableEquals(mapKeyType, other.mapKeyType))
            return false;

        if (!isNullableEquals(mapValueType, other.mapValueType))
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
    private boolean hasEffectiveBooleanValue() {
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
        return castableAs[typeOrdinal][typed.type.ordinal()];
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
        return isValueComparableWith[typeOrdinal][other_.type.ordinal()];
    }

    public XQueryItemType alternativeMerge(XQueryItemType other) {
        return  alternativeMerger.alternativeMerge(this, other);
    }

    public XQuerySequenceType lookup(XQuerySequenceType keySpecifierType) {
        return null;
    }
    private static final ItemtypeStringRepresentation representationProvider = new ItemtypeStringRepresentation();

    @Override
    public String toString() {
        return representationOp.apply(this);
    }



    public static XQueryItemType choice(XQueryTypeFactory factory, Collection<XQueryItemType> itemTypes)
    {
        return new XQueryItemType(XQueryTypes.CHOICE,
            null, null, null, null, null, null, factory, itemTypes, null, null, null);
    }

    public static XQueryItemType anyArray(XQueryTypeFactory factory)
    {
        return new XQueryItemType(XQueryTypes.ANY_ARRAY, null, null, null, null, null, null, factory, null, null, null, null);
    }

    public static XQueryItemType anyFunction(XQueryTypeFactory factory)
    {
        return new XQueryItemType(XQueryTypes.ANY_FUNCTION, null, null, null, null, null, null, factory, null, null,
            null, null);
    }

    public static XQueryItemType anyItem(XQueryTypeFactory factory) {
        return new XQueryItemType(XQueryTypes.ANY_ITEM, null, null, null, null, null, null, factory, null, null, null, null);
    }

    public static XQueryItemType anyMap(XQueryTypeFactory factory) {
        return new XQueryItemType(XQueryTypes.ANY_MAP, null, null, null, null, null, null, factory, null, null, null, null);
    }

    public static XQueryItemType anyNode(XQueryTypeFactory factory) {
        return new XQueryItemType(XQueryTypes.ANY_NODE, null, null, null, null, null, null, factory, null, null, null, null);
    }


    public static XQueryItemType error(XQueryTypeFactory factory)
    {
        return new XQueryItemType(XQueryTypes.ERROR, null, null, null, null, null, null, factory, null, null, null, null);
    }


    public static XQueryItemType string(XQueryTypeFactory factory)
    {
        return new XQueryItemType(XQueryTypes.STRING, null, null, null, null, null, null, factory, null, null, null, null);
    }

    public static XQueryItemType boolean_(XQueryTypeFactory factory)
    {
        return new XQueryItemType(XQueryTypes.BOOLEAN, null, null, null, null, null, null, factory, null, null, null, null);
    }


    public static XQueryItemType number(XQueryTypeFactory factory) {
        return new XQueryItemType(XQueryTypes.NUMBER, null, null,
            null, null, null,
            null, factory, null,
                     null, null, null);
    }

    public static XQueryItemType element(Set<String> elementName, XQueryTypeFactory factory) {
        return new XQueryItemType(XQueryTypes.ELEMENT, null, null, null, null, null, elementName, factory, null, null, null, null);
    }



    public static XQueryItemType function(XQuerySequenceType returnedType, List<XQuerySequenceType> argumentType,
        XQueryTypeFactory factory)
    {
        return new XQueryItemType(XQueryTypes.FUNCTION, argumentType, returnedType, null, null, null, null, factory,
            null, null, null, null);
    }





    public static XQueryItemType map(XQueryItemType key,
                                    XQuerySequenceType value,
                                    XQueryTypeFactory factory)
    {
        return new XQueryItemType(XQueryTypes.MAP, null, null, null, key, value, null, factory, null, null, null, null);
    }



    public static XQueryItemType array(XQuerySequenceType containedType, XQueryTypeFactory factory) {
        return new XQueryItemType(XQueryTypes.ARRAY, null, null, null, null,
            null, null, factory,
                null, null, containedType, null);
    }

    public static XQueryItemType extensibleRecord(Map<String, XQueryRecordField> keyValuePairs, XQueryTypeFactory factory ) {
        return new XQueryItemType(XQueryTypes.EXTENSIBLE_RECORD, null, null, null, null, null, null, factory, null, keyValuePairs, null, null);
    }

    public static XQueryItemType contrainedRecord(Map<String, XQueryRecordField> keyValuePairs, XQueryTypeFactory factory ) {
        return new XQueryItemType(XQueryTypes.RECORD, null, null, null, null, null, null, factory, null, keyValuePairs, null, null);
    }

    final public Set<String> enumMembers;

    public static XQueryItemType enum_(Set<String> enumMembers, XQueryTypeFactory factory) {
        return new XQueryItemType(XQueryTypes.ENUM, null, null, null, null, null, null, factory, null, null, null, enumMembers);
    }




}
