package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import com.github.akruk.antlrxquery.typesystem.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;

public class XQueryEnumItemType implements XQueryItemType {
    private final XQueryTypes type;

    public XQueryEnumItemType(XQueryTypes type,
                                List<XQuerySequenceType> argumentTypes,
                                XQuerySequenceType returnedType,
                                XQuerySequenceType arrayType,
                                XQueryEnumItemType key,
                                XQuerySequenceType mapValueType,
                                Set<String> elementNames)
    {
        this.type = type;
        this.argumentTypes = argumentTypes;
        this.returnedType = returnedType;
        this.arrayType = arrayType;
        this.mapKeyType = key;
        this.mapValueType = mapValueType;
        this.elementNames = elementNames;
    }

    private final List<XQuerySequenceType> argumentTypes;
    private final XQuerySequenceType returnedType;
    private final XQuerySequenceType arrayType;
    public XQuerySequenceType getArrayType() {
        return arrayType;
    }

    private final XQueryItemType mapKeyType;

    private final XQuerySequenceType mapValueType;

    private final Set<String> elementNames;


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
        return returnedType;
    }

    public Set<String> getElementNames() {
        return elementNames;
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
        final var otherArgumentTypes = other.getArgumentTypes();
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

    private static final int typesCount = XQueryTypes.values().length;
    private static final BiPredicate<XQueryEnumItemType, XQueryEnumItemType> alwaysTrue = (t1, t2) -> true;
    private static final BiPredicate<XQueryEnumItemType, XQueryEnumItemType> alwaysFalse = (t1, t2) -> false;
    private static final BiPredicate<XQueryEnumItemType, XQueryEnumItemType> equalType = (t1, t2) -> t1.type == t2.type;

    private static final BiPredicate[][] itemtypeIsSubtypeOf;
    static {
        itemtypeIsSubtypeOf = new BiPredicate[typesCount][typesCount];
        for (int i = 0; i < typesCount; i++) {
            for (int j = 0; j < typesCount; j++) {
                itemtypeIsSubtypeOf[i][j] = i == j ? alwaysTrue : alwaysFalse;
            }
        }
        final int anyItem = XQueryTypes.ANY_ITEM.ordinal();
        for (int i = 0; i < typesCount; i++) {
            itemtypeIsSubtypeOf[i][anyItem] = alwaysTrue;
        }

        final int element = XQueryTypes.ELEMENT.ordinal();
        final int anyNode = XQueryTypes.ANY_NODE.ordinal();
        final int anyFunction = XQueryTypes.ANY_FUNCTION.ordinal();
        final int function = XQueryTypes.FUNCTION.ordinal();
        final int array = XQueryTypes.ARRAY.ordinal();
        final int map = XQueryTypes.MAP.ordinal();
        final int record = XQueryTypes.RECORD.ordinal();
        final int anyMap = XQueryTypes.ANY_MAP.ordinal();
        final int anyArray = XQueryTypes.ANY_ARRAY.ordinal();


        itemtypeIsSubtypeOf[element][anyNode] = alwaysTrue;
        itemtypeIsSubtypeOf[element][element] = (x, y) -> {
            XQueryEnumItemType x_ = (XQueryEnumItemType) x;
            XQueryEnumItemType y_ = (XQueryEnumItemType) y;
            return y_.elementNames.containsAll(x_.elementNames);
        };

        itemtypeIsSubtypeOf[function][anyFunction] = alwaysTrue;
        itemtypeIsSubtypeOf[array][anyFunction] = alwaysTrue;
        itemtypeIsSubtypeOf[map][anyFunction] = alwaysTrue;
        itemtypeIsSubtypeOf[record][anyFunction] = alwaysTrue;
        itemtypeIsSubtypeOf[anyArray][anyFunction] = alwaysTrue;
        itemtypeIsSubtypeOf[anyMap][anyFunction] = alwaysTrue;

        itemtypeIsSubtypeOf[function][function] = (x, y) -> {
            XQueryEnumItemType i1 = (XQueryEnumItemType) x;
            XQueryEnumItemType i2 = (XQueryEnumItemType) y;
            return i1.isFunction(i2.getReturnedType(), i2.getArgumentTypes());
        };

        itemtypeIsSubtypeOf[map][anyMap] = alwaysTrue;
        itemtypeIsSubtypeOf[map][map] = (x, y) -> {
            XQueryEnumItemType x_ = (XQueryEnumItemType) x;
            XQueryEnumItemType y_ = (XQueryEnumItemType) y;
            return x_.getMapKeyType().itemtypeIsSubtypeOf(y_.getMapKeyType())
                    && x_.getMapValueType().isSubtypeOf(y_.getMapValueType());
        };

        itemtypeIsSubtypeOf[array][anyArray] = alwaysTrue;
        itemtypeIsSubtypeOf[function][anyArray] = (x, y) -> {
            XQueryEnumItemType x_ = (XQueryEnumItemType) x;
            XQueryEnumItemType y_ = (XQueryEnumItemType) y;
            // function must have one argument
            if (x_.getArgumentTypes().size() != 1)
                return false;
            var onlyArg =  (XQueryEnumSequenceType) x_.getArgumentTypes().get(0);
            var onlyArgItem =  (XQueryEnumItemType) onlyArg.getItemType();
            // this one argument must be either
            // number or number+
            boolean correctOccurence = onlyArg.isOne() || onlyArg.isOneOrMore();
            return correctOccurence
                    && onlyArgItem.getType() == XQueryTypes.NUMBER;
        };

        itemtypeIsSubtypeOf[map][anyArray] = (x, y) -> {
            XQueryEnumItemType x_ = (XQueryEnumItemType) x;
            XQueryEnumItemType y_ = (XQueryEnumItemType) y;
            // map must have a key that is a number
            var key = (XQueryEnumItemType) x_.getMapKeyType();
            return key.getType() == XQueryTypes.NUMBER;
        };
        // itemtypeIsSubtypeOf[record][anyArray]

        itemtypeIsSubtypeOf[array][anyArray] = (x, y) -> {
            XQueryEnumItemType x_ = (XQueryEnumItemType) x;
            XQueryEnumItemType y_ = (XQueryEnumItemType) y;
            boolean isSubtypeOfAnyArray = itemtypeIsSubtypeOf[array][anyArray].test(x, y);
            if (!isSubtypeOfAnyArray)
                return false;
            XQuerySequenceType xArrayItemType = x_.getArrayType();
            XQuerySequenceType yArrayItemType = y_.getArrayType();
            return xArrayItemType.isSubtypeOf(yArrayItemType);

            x_.getArrayType().isSubtypeOf(y_.getArrayType());
        };
        // // case MAP -> this.isMap();
        // default -> false;

    }

    public final int typeOrdinal = type.ordinal();
    @SuppressWarnings("unchecked")
    @Override
    public boolean itemtypeIsSubtypeOf(XQueryItemType obj) {
        if (!(obj instanceof XQueryEnumItemType))
            return false;
        var typed = (XQueryEnumItemType) obj;
        return itemtypeIsSubtypeOf[typeOrdinal][typed.typeOrdinal].test(this, obj);
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

    private static final boolean[] isNode = booleanEnumArray(XQueryTypes.ANY_NODE, XQueryTypes.ANY_ELEMENT, XQueryTypes.ELEMENT, XQueryTypes.NODE);

    @Override
    public boolean isNode() {
        return isNode[type.ordinal()];
    }

    private static final boolean[] isElement = isNode;

    @Override
    public boolean isElement() {
        return isElement[type.ordinal()];
    }

    @Override
    public boolean isElement(String otherName) {
        return isElement() && elementNames != null && elementNames.equals(otherName);
    }

    private static final boolean[] isFunction = booleanEnumArray(XQueryTypes.ANY_FUNCTION, XQueryTypes.FUNCTION);

    @Override
    public boolean isFunction() {
        return isFunction[type.ordinal()];
    }

    @Override
    public boolean isFunction(XQuerySequenceType otherReturnedType, List<XQuerySequenceType> otherArgumentTypes) {
        return isFunction[type.ordinal()]
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
