package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import com.github.akruk.antlrxquery.typesystem.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;

@SuppressWarnings("unchecked")
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

    @SuppressWarnings("rawtypes")
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
        final int enum_ = XQueryTypes.ENUM.ordinal();
        final int string = XQueryTypes.STRING.ordinal();

        itemtypeIsSubtypeOf[anyArray][anyMap] = alwaysTrue;
        itemtypeIsSubtypeOf[anyArray][map] = (x, y) -> {
            XQueryEnumItemTypeMap y_ = (XQueryEnumItemTypeMap) y;
            var mapKeyType = (XQueryEnumItemType) y_.getMapKeyType();
            boolean isNumber = mapKeyType.getType() == XQueryTypes.NUMBER;
            return isNumber;
        };

        itemtypeIsSubtypeOf[anyArray][anyFunction] = alwaysTrue;
        itemtypeIsSubtypeOf[anyArray][function] = (x, y) -> {
            XQueryEnumItemTypeFunction y_ = (XQueryEnumItemTypeFunction) y;
            var argumentTypes = y_.getArgumentTypes();
            if (argumentTypes.size() != 1)
                return false;
            var onlyArg =  (XQueryEnumSequenceType) argumentTypes.get(0);
            var onlyArgItem =  (XQueryEnumItemType) onlyArg.getItemType();
            boolean correctOccurence = onlyArg.isOne() || onlyArg.isOneOrMore();
            return correctOccurence
                    && onlyArgItem.getType() == XQueryTypes.NUMBER;
        };

        itemtypeIsSubtypeOf[array][anyMap] = alwaysTrue;
        itemtypeIsSubtypeOf[array][map] = (x, y) -> {
            XQueryEnumItemType x_ = (XQueryEnumItemType) x;
            XQueryEnumItemType y_ = (XQueryEnumItemType) y;
            if (!itemtypeIsSubtypeOf[anyArray][map].test(x, y))
                return false;
            return x_.getArrayType().isSubtypeOf(y_.getMapValueType());
        };

        itemtypeIsSubtypeOf[element][anyNode] = alwaysTrue;
        itemtypeIsSubtypeOf[element][element] = (x, y) -> {
            XQueryEnumItemType x_ = (XQueryEnumItemType) x;
            XQueryEnumItemType y_ = (XQueryEnumItemType) y;
            return y_.elementNames.containsAll(x_.elementNames);
        };

        itemtypeIsSubtypeOf[function][anyFunction] = alwaysTrue;
        itemtypeIsSubtypeOf[function][function] = (x, y) -> {
            XQueryEnumItemType i1 = (XQueryEnumItemType) x;
            XQueryEnumItemType i2 = (XQueryEnumItemType) y;
            return i1.isFunction(i2.getReturnedType(), i2.getArgumentTypes());
        };

        final var canBeKey = booleanEnumArray(XQueryTypes.NUMBER, XQueryTypes.BOOLEAN, XQueryTypes.STRING, XQueryTypes.ENUM);
        itemtypeIsSubtypeOf[function][anyMap] = (x, y) -> {
            XQueryEnumItemTypeFunction x_ = (XQueryEnumItemTypeFunction) x;
            // function must have one argument
            if (x_.getArgumentTypes().size() != 1)
                return false;
            var onlyArg =  (XQueryEnumSequenceType) x_.getArgumentTypes().get(0);
            var onlyArgItem =  (XQueryEnumItemType) onlyArg.getItemType();
            boolean correctOccurence = onlyArg.isOne();
            return correctOccurence
                    && canBeKey[onlyArgItem.getType().ordinal()];
        };

        itemtypeIsSubtypeOf[function][map] = (x, y) -> {
            if (!itemtypeIsSubtypeOf[function][anyMap].test(x, y))
                return false;
            XQueryEnumItemTypeFunction x_ = (XQueryEnumItemTypeFunction) x;
            XQueryEnumItemTypeMap y_ = (XQueryEnumItemTypeMap) y;
            var onlyArg =  (XQueryEnumSequenceType) x_.getArgumentTypes().get(0);
            var onlyArgItem =  (XQueryEnumItemType) onlyArg.getItemType();
            boolean argCanBeKey = onlyArgItem.itemtypeIsSubtypeOf(y_.getMapKeyType());
            boolean returnedCanBeValue = x_.getReturnedType().isSubtypeOf(y_.getMapValueType());
            boolean correctOccurence = onlyArg.isOne();
            return correctOccurence
                    && argCanBeKey
                    && returnedCanBeValue;
        };

        itemtypeIsSubtypeOf[function][anyArray] = (x, y) -> {
            XQueryEnumItemTypeFunction x_ = (XQueryEnumItemTypeFunction) x;
            // function must have one argument
            if (x_.getArgumentTypes().size() != 1)
                return false;
            var onlyArg =  (XQueryEnumSequenceType) x_.getArgumentTypes().get(0);
            var onlyArgItem =  (XQueryEnumItemType) onlyArg.getItemType();
            // this one argument must be either number or number+
            boolean correctOccurence = onlyArg.isOne() || onlyArg.isOneOrMore();
            return correctOccurence
                    && onlyArgItem.getType() == XQueryTypes.NUMBER;
        };

        itemtypeIsSubtypeOf[function][array] = (x, y) -> {
            if (!itemtypeIsSubtypeOf[function][anyArray].test(x, y))
                return false;
            XQueryEnumItemTypeFunction x_ = (XQueryEnumItemTypeFunction) x;
            XQueryEnumItemTypeArray y_ = (XQueryEnumItemTypeArray) y;
            var returnedType = x_.getReturnedType();

            return returnedType.isSubtypeOf(y_.getArrayType());
        };

        itemtypeIsSubtypeOf[function][function] = (x, y) -> {
            XQueryEnumItemTypeFunction x_ = (XQueryEnumItemTypeFunction) x;
            XQueryEnumItemTypeFunction y_ = (XQueryEnumItemTypeFunction) y;
            if (x_.getArgumentTypes().size() != y_.getArgumentTypes().size())
                return false;
            for (int i = 0; i < x_.getArgumentTypes().size(); i++) {
                var xArgType = x_.getArgumentTypes().get(i);
                var yArgType = y_.getArgumentTypes().get(i);
                if (!xArgType.isSubtypeOf(yArgType))
                    return false;
            }
            return x_.getReturnedType().isSubtypeOf(y_.getReturnedType());
        };


        itemtypeIsSubtypeOf[array][anyFunction] = alwaysTrue;
        itemtypeIsSubtypeOf[array][function] = (x, y) -> {
            XQueryEnumItemType x_ = (XQueryEnumItemType) x;
            XQueryEnumItemType y_ = (XQueryEnumItemType) y;
            if (y_.getArgumentTypes().size() != 1)
                return false;
            var onlyArg =  (XQueryEnumSequenceType) y_.getArgumentTypes().get(0);
            var onlyArgItem = (XQueryEnumItemType) onlyArg.getItemType();
            if (onlyArgItem.getType() == XQueryTypes.NUMBER) {

            }

            return x_.getArrayType().isSubtypeOf(y_.getReturnedType());
        };

        itemtypeIsSubtypeOf[array][anyArray] = alwaysTrue;
        itemtypeIsSubtypeOf[array][array] = (x, y) -> {
            XQueryEnumItemType x_ = (XQueryEnumItemType) x;
            XQueryEnumItemType y_ = (XQueryEnumItemType) y;
            boolean isSubtypeOfAnyArray = itemtypeIsSubtypeOf[array][anyArray].test(x, y);
            if (!isSubtypeOfAnyArray)
                return false;
            XQuerySequenceType xArrayItemType = x_.getArrayType();
            XQuerySequenceType yArrayItemType = y_.getArrayType();
            return xArrayItemType.isSubtypeOf(yArrayItemType);
        };

        itemtypeIsSubtypeOf[record][anyFunction] = alwaysTrue;
        itemtypeIsSubtypeOf[anyMap][anyFunction] = alwaysTrue;

        itemtypeIsSubtypeOf[map][anyFunction] = alwaysTrue;
        itemtypeIsSubtypeOf[map][function] = (x, y) -> {
            XQueryEnumItemType x_ = (XQueryEnumItemType) x;
            XQueryEnumItemType y_ = (XQueryEnumItemType) y;
            if (y_.getArgumentTypes().size() != 1)
                return false;
            var onlyArg =  (XQueryEnumSequenceType) y_.getArgumentTypes().get(0);
            var onlyArgItem =  (XQueryEnumItemType) onlyArg.getItemType();
            boolean correctOccurence = onlyArg.isOne();
            return correctOccurence
                    && x_.getMapKeyType().itemtypeIsSubtypeOf(onlyArgItem);
        };

        itemtypeIsSubtypeOf[map][anyMap] = alwaysTrue;
        itemtypeIsSubtypeOf[map][map] = (x, y) -> {
            XQueryEnumItemType x_ = (XQueryEnumItemType) x;
            XQueryEnumItemType y_ = (XQueryEnumItemType) y;
            return x_.getMapKeyType().itemtypeIsSubtypeOf(y_.getMapKeyType())
                    && x_.getMapValueType().isSubtypeOf(y_.getMapValueType());
        };


        itemtypeIsSubtypeOf[map][anyArray] = (x, y) -> {
            XQueryEnumItemType x_ = (XQueryEnumItemType) x;
            // map must have a key that is a number
            var key = (XQueryEnumItemType) x_.getMapKeyType();
            return key.getType() == XQueryTypes.NUMBER;
        };


        itemtypeIsSubtypeOf[enum_][string] = alwaysTrue;
        itemtypeIsSubtypeOf[enum_][enum_] = (x, y) -> {
            var x_ = (XQueryEnumItemTypeEnum) x;
            var y_ = (XQueryEnumItemTypeEnum) y;
            return y_.getEnumMembers().containsAll(x_.getEnumMembers());
        };

        itemtypeIsSubtypeOf[record][anyMap] = alwaysTrue;
        itemtypeIsSubtypeOf[record][map] = (x, y) -> {
            var x_ = (XQueryEnumItemTypeRecord) x;
            var y_ = (XQueryEnumItemTypeMap) y;
            var keyItemType = (XQueryEnumItemType) y_.getMapKeyType();
            if (keyItemType.getType() != XQueryTypes.STRING
                && keyItemType.getType() != XQueryTypes.ANY_ITEM)
                return false;
            var yFieldType = y_.getMapValueType();
            for (var key : x_.getRecordFields().keySet()) {
                var xFieldType = x_.getRecordFields().get(key);
                if (!xFieldType.isSubtypeOf(yFieldType))
                    return false;
            }
            return true;
        };

        itemtypeIsSubtypeOf[record][anyFunction] = alwaysTrue;
        itemtypeIsSubtypeOf[record][function] = (x, y) -> {
            var x_ = (XQueryEnumItemTypeRecord) x;
            var y_ = (XQueryEnumItemTypeFunction) y;
            var yArgumentTypes = y_.getArgumentTypes();
            if (yArgumentTypes.size() != 1)
                return false;
            var yFieldType = (XQueryEnumSequenceType) yArgumentTypes.get(0);
            var yFieldItemType = (XQueryEnumItemType) yFieldType.getItemType();
            if (yFieldItemType.getType() != XQueryTypes.STRING
                && yFieldItemType.getType() != XQueryTypes.ANY_ITEM)
                return false;
            var yReturnedType = y_.getReturnedType();
            for (var key : x_.getRecordFields().keySet()) {
                var xFieldType = x_.getRecordFields().get(key);
                if (!xFieldType.isSubtypeOf(yReturnedType))
                    return false;
            }
            return true;
        };

        itemtypeIsSubtypeOf[record][record] = (x, y) -> {
            var x_ = (XQueryEnumItemTypeRecord) x;
            var y_ = (XQueryEnumItemTypeRecord) y;
            boolean allFieldsPresent = y_.getRecordFields().keySet().containsAll(x_.getRecordFields().keySet());
            if (!allFieldsPresent)
                return false;
            for (var key : x_.getRecordFields().keySet()) {
                var xFieldType = x_.getRecordFields().get(key);
                var yFieldType = y_.getRecordFields().get(key);
                if (!xFieldType.isSubtypeOf(yFieldType))
                    return false;
            }
            return true;
        };


    }

    @Override
    public boolean itemtypeIsSubtypeOf(XQueryItemType obj) {
        if (!(obj instanceof XQueryEnumItemType))
            return false;
        var typed = (XQueryEnumItemType) obj;
        return itemtypeIsSubtypeOf[type.ordinal()][typed.getType().ordinal()].test(this, obj);
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

    private static final boolean[] isNode = booleanEnumArray(XQueryTypes.ANY_NODE, XQueryTypes.ELEMENT);

    @Override
    public boolean isNode() {
        return isNode[type.ordinal()];
    }

    private static final boolean[] isElement = isNode;

    @Override
    public boolean isElement() {
        return isElement[type.ordinal()];
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

    private static final boolean[] isAtomic = booleanEnumArray(t -> t != XQueryTypes.ELEMENT
                                                                    && t != XQueryTypes.FUNCTION);
    @Override
    public boolean isAtomic() {
        return isAtomic[type.ordinal()];
    }


    private static final boolean[][] castableAs;
    static {
        castableAs = new boolean[typesCount][typesCount];
        for (int i = 0; i < typesCount; i++) {
            for (int j = 0; j < typesCount; j++) {
                castableAs[i][j] = i == j;
            }
        }
        final int number = XQueryTypes.NUMBER.ordinal();
        final int string = XQueryTypes.STRING.ordinal();
        final int anyItem = XQueryTypes.ANY_ITEM.ordinal();
        for (int i = 0; i < typesCount; i++) {
            castableAs[i][anyItem] = true;
            castableAs[i][string] = true;
        }
    }

    @Override
    public boolean castableAs(XQueryItemType itemType) {
        if (!(itemType instanceof XQueryEnumItemType))
            return false;
        var typed = (XQueryEnumItemType) itemType;
        return castableAs[type.ordinal()][typed.getType().ordinal()];
    }
}
