package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.IntStream;


import com.github.akruk.antlrxquery.typesystem.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;

public class XQueryEnumItemType implements XQueryItemType {
    private final XQueryTypes type;
    private final List<XQuerySequenceType> argumentTypes;
    private final XQuerySequenceType returnedType;
    private final String name;

    public List<XQuerySequenceType> getArgumentTypes() {
        return argumentTypes;
    }

    public XQuerySequenceType getReturnedType() {
        return returnedType;
    }

    public XQueryEnumItemType(XQueryTypes type, List<XQuerySequenceType> argumentTypes,
            XQueryEnumSequenceType returnedType, String name) {
        this.type = type;
        this.argumentTypes = argumentTypes;
        this.returnedType = returnedType;
        this.name = name;
            // // case MAP -> this.isMap();
            // // case ARRAY -> this.isArray();
            // default -> false;

    }

    public String getName() {
        return name;
    }

    public XQueryTypes getType() {
        return type;
    }

    private static boolean isNullableEquals(Object one, Object other) {
        if (one != null)
            return one.equals(other);
        return one == other;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof XQueryEnumItemType))
            return false;
        XQueryEnumItemType other = (XQueryEnumItemType) obj;
        if (type != other.getType())
            return false;
        List<XQuerySequenceType> otherArgumentTypes = other.getArgumentTypes();
        if (this.argumentTypes == null && otherArgumentTypes != null)
            return false;
        if (this.argumentTypes != null && otherArgumentTypes == null)
            return false;
        if (this.argumentTypes != null) {
            if (this.argumentTypes.size() != otherArgumentTypes.size())
                return false;
            if (IntStream.range(0, this.argumentTypes.size())
                    .allMatch(i -> this.argumentTypes.get(i).equals(otherArgumentTypes.get(i))))
                return false;
        }
        XQuerySequenceType otherReturnedType = other.getReturnedType();
        return isNullableEquals(this.returnedType, otherReturnedType);
    }

    private static final int occurenceCount = XQueryOccurence.values().length;
    private static final BiPredicate<XQueryEnumItemType, XQueryEnumItemType> alwaysTrue = (t1, t2) -> true;
    private static final BiPredicate<XQueryEnumItemType, XQueryEnumItemType> alwaysFalse = (t1, t2) -> false;

    @SuppressWarnings("rawtypes")
    private static final BiPredicate[] itemtypeIsSubtypeOf;
    static {
        itemtypeIsSubtypeOf = new BiPredicate[occurenceCount];
        for (int i = 0; i < occurenceCount; i++) {
            itemtypeIsSubtypeOf[i] = alwaysFalse;
        }
        itemtypeIsSubtypeOf[XQueryTypes.ERROR.ordinal()] = (x, y) -> ((XQueryEnumItemType) x).isAtomic();
        itemtypeIsSubtypeOf[XQueryTypes.ANY_ITEM.ordinal()] = alwaysTrue;
        itemtypeIsSubtypeOf[XQueryTypes.ANY_NODE.ordinal()] = (x, y) -> ((XQueryEnumItemType) x).isNode();
        itemtypeIsSubtypeOf[XQueryTypes.ANY_ELEMENT.ordinal()] = (x, y) -> ((XQueryEnumItemType) x).isElement();
        itemtypeIsSubtypeOf[XQueryTypes.ELEMENT.ordinal()] = (x, y) -> ((XQueryEnumItemType) x).isElement(((XQueryEnumItemType) y).getName());
        itemtypeIsSubtypeOf[XQueryTypes.ANY_FUNCTION.ordinal()] = (x, y) -> ((XQueryEnumItemType) x).isFunction();
        itemtypeIsSubtypeOf[XQueryTypes.FUNCTION.ordinal()] = (x, y) -> {
            XQueryEnumItemType i1 = (XQueryEnumItemType) x;
            XQueryEnumItemType i2 = (XQueryEnumItemType) y;
            return i1.isFunction(i2.getName(), i2.getReturnedType(), i2.getArgumentTypes());
        };
        itemtypeIsSubtypeOf[XQueryTypes.ANY_MAP.ordinal()] = (x, y) -> ((XQueryEnumItemType) x).isMap();
        itemtypeIsSubtypeOf[XQueryTypes.ANY_ARRAY.ordinal()] = (x, y) -> ((XQueryEnumItemType) x).isArray();


    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean itemtypeIsSubtypeOf(XQueryItemType obj) {
        if (!(obj instanceof XQueryEnumItemType))
            return false;
        return itemtypeIsSubtypeOf[type.ordinal()].test(this, obj);
    }


    private static boolean[] booleanEnumArray(XQueryTypes... values) {
        var array = new boolean[XQueryTypes.values().length];
        for (var v : values) {
            array[v.ordinal()] = true;
        }
        return array;
    }

    private static boolean[] booleanEnumArray(Predicate<XQueryTypes> predicateForTrueValues) {
        var array = new boolean[XQueryTypes.values().length];
        for (int j = 0; j < array.length; j++) {
            XQueryTypes tested = XQueryTypes.values()[j];
            array[j] = predicateForTrueValues.test(tested);
        }
        return array;
    }

    private static final boolean[] isNode = booleanEnumArray(XQueryTypes.ANY_NODE, XQueryTypes.NODE);

    @Override
    public boolean isNode() {
        return isNode[type.ordinal()];
    }

    private static final boolean[] isElement = booleanEnumArray(XQueryTypes.ANY_ELEMENT, XQueryTypes.ELEMENT);

    @Override
    public boolean isElement() {
        return isElement[type.ordinal()];
    }

    @Override
    public boolean isElement(String otherName) {
        return isElement() && name.equals(otherName);
    }

    private static final boolean[] isFunction = booleanEnumArray(XQueryTypes.ANY_FUNCTION, XQueryTypes.FUNCTION);

    @Override
    public boolean isFunction() {
        return isFunction[type.ordinal()];
    }

    @Override
    public boolean isFunction(String otherName, XQuerySequenceType otherReturnedType,
            List<XQuerySequenceType> otherArgumentTypes) {
        return isFunction[type.ordinal()]
                && name.equals(otherName)
                && this.returnedType.equals(otherReturnedType)
                && this.argumentTypes.size() == otherArgumentTypes.size()
                && IntStream.range(0, this.argumentTypes.size())
                        .allMatch(i -> this.argumentTypes.get(i).equals(otherArgumentTypes.get(i)));
    }

    private static final boolean[] isMap = booleanEnumArray(XQueryTypes.MAP, XQueryTypes.ANY_MAP);

    @Override
    public boolean isMap() {
        return isMap[type.ordinal()];
    }

    private static final boolean[] isArray = booleanEnumArray(XQueryTypes.ARRAY, XQueryTypes.ANY_ARRAY);

    @Override
    public boolean isArray() {
        return isArray[type.ordinal()];
    }

    @Override
    public boolean hasEffectiveBooleanValue() {
        return !this.isFunction()
                && !this.isMap()
                && !this.isArray();
    }

    private static final boolean[] isAtomic = booleanEnumArray(t -> t != XQueryTypes.NODE && t != XQueryTypes.ELEMENT
            && t != XQueryTypes.FUNCTION);
    @Override
    public boolean isAtomic() {
        return isAtomic[type.ordinal()];
    }
}
