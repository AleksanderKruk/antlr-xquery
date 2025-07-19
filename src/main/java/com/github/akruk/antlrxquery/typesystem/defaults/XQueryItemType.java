package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.IntStream;

import com.github.akruk.antlrxquery.typesystem.XQueryRecordField;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.typeoperations.defaults.ItemtypeAlternativeMerger;
import com.github.akruk.antlrxquery.typesystem.typeoperations.defaults.ItemtypeIntersectionMerger;
import com.github.akruk.antlrxquery.typesystem.typeoperations.defaults.ItemtypeUnionMerger;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class XQueryItemType {
    private static final int ANY_ITEM = XQueryTypes.ANY_ITEM.ordinal();
    private static final int ANY_ARRAY = XQueryTypes.ANY_ARRAY.ordinal();
    private static final int ARRAY = XQueryTypes.ARRAY.ordinal();
    private static final int FUNCTION = XQueryTypes.FUNCTION.ordinal();
    private static final int ANY_FUNCTION = XQueryTypes.ANY_FUNCTION.ordinal();
    private static final int ANY_NODE = XQueryTypes.ANY_NODE.ordinal();
    private static final int ELEMENT = XQueryTypes.ELEMENT.ordinal();
    private static final int STRING = XQueryTypes.STRING.ordinal();
    private static final int ENUM = XQueryTypes.ENUM.ordinal();
    private static final int BOOLEAN = XQueryTypes.BOOLEAN.ordinal();
    private static final int NUMBER = XQueryTypes.NUMBER.ordinal();
    private static final int MAP = XQueryTypes.MAP.ordinal();
    private static final int ANY_MAP = XQueryTypes.ANY_MAP.ordinal();
    private static final int RECORD = XQueryTypes.RECORD.ordinal();
    private static final int EXTENSIBLE_RECORD = XQueryTypes.EXTENSIBLE_RECORD.ordinal();
    private static final int choice = XQueryTypes.CHOICE.ordinal();
    private final XQueryTypes type;
    private final int typeOrdinal;


    private final ItemtypeAlternativeMerger alternativeMerger;
    private final ItemtypeIntersectionMerger intersectionMerger;
    private final XQueryTypeFactory typeFactory;
    private final Collection<XQueryItemType> itemTypes;
    private final ItemtypeUnionMerger unionMerger;

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
        this.returnedType_ = getReturnedType_();
        this.arrayType = arrayType;
        this.mapKeyType = key;
        this.mapValueType = mapValueType;
        this.elementNames = elementNames;
        this.typeFactory = typeFactory;
        this.itemTypes = itemTypes;
        this.alternativeMerger = new ItemtypeAlternativeMerger(typeOrdinal, typeFactory);
        this.unionMerger = new ItemtypeUnionMerger(typeOrdinal, typeFactory);
        this.intersectionMerger = new ItemtypeIntersectionMerger(typeOrdinal, typeFactory);
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
    private final XQuerySequenceType returnedType_;


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
        if (returnedType == null)
            return typeFactory.error();
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
        if (this.argumentTypes != null && otherArgumentTypes == null)
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
        return isNullableEquals(this.returnedType, otherReturnedType);
    }

    private static final int typesCount = XQueryTypes.values().length;
    private static final BiPredicate<XQueryItemType, XQueryItemType> alwaysTrue = (_, _) -> true;
    private static final BiPredicate<XQueryItemType, XQueryItemType> alwaysFalse = (_, _) -> false;

    private static final BiPredicate[][] itemtypeIsSubtypeOf;
    static {
        itemtypeIsSubtypeOf = new BiPredicate[typesCount][typesCount];
        for (int i = 0; i < typesCount; i++) {
            for (int j = 0; j < typesCount; j++) {
                itemtypeIsSubtypeOf[i][j] = i == j ? alwaysTrue : alwaysFalse;
            }
        }
        final int anyItem = ANY_ITEM;
        for (int i = 0; i < typesCount; i++) {
            itemtypeIsSubtypeOf[i][anyItem] = alwaysTrue;
        }

        final BiPredicate<XQueryItemType, XQueryItemType> choicesubtype = (x, y) -> {
            final XQueryItemType y_ = (XQueryItemType) y;
            final var items = y_.getItemTypes();
            final boolean anyIsSubtype = items.stream().anyMatch(i-> x.itemtypeIsSubtypeOf(i));
            return anyIsSubtype;
        };

        for (int i = 0; i < typesCount; i++) {
            itemtypeIsSubtypeOf[i][choice] = choicesubtype;
        }

        itemtypeIsSubtypeOf[choice][choice] = (x, y) -> {
            final XQueryItemType x_ = (XQueryItemType) x;
            final XQueryItemType y_ = (XQueryItemType) y;
            final var xItems = x_.getItemTypes();
            final var yItems = y_.getItemTypes();
            final var allPresent = xItems.stream().allMatch(xItem->{
                final boolean anyIsSubtype = yItems.stream().anyMatch(i-> xItem.itemtypeIsSubtypeOf(i));
                return anyIsSubtype;
            });
            return allPresent;
        }
        ;


        itemtypeIsSubtypeOf[ANY_ARRAY][ANY_MAP] = alwaysTrue;
        itemtypeIsSubtypeOf[ANY_ARRAY][MAP] = (_, y) -> {
            final XQueryItemTypeMap y_ = (XQueryItemTypeMap) y;
            final var mapKeyType = (XQueryItemType) y_.getMapKeyType();
            final boolean isNumber = mapKeyType.getType() == XQueryTypes.NUMBER;
            return isNumber;
        };

        itemtypeIsSubtypeOf[ANY_ARRAY][ANY_FUNCTION] = alwaysTrue;
        itemtypeIsSubtypeOf[ANY_ARRAY][FUNCTION] = (_, y) -> {
            final XQueryItemTypeFunction y_ = (XQueryItemTypeFunction) y;
            final var argumentTypes = y_.getArgumentTypes();
            if (argumentTypes.size() != 1)
                return false;
            final var onlyArg =  (XQuerySequenceType) argumentTypes.get(0);
            final var onlyArgItem =  (XQueryItemType) onlyArg.getItemType();
            final boolean correctOccurence = onlyArg.isOne() || onlyArg.isOneOrMore();
            return correctOccurence
                    && onlyArgItem.getType() == XQueryTypes.NUMBER;
        };

        itemtypeIsSubtypeOf[ARRAY][ANY_MAP] = alwaysTrue;
        itemtypeIsSubtypeOf[ARRAY][MAP] = (x, y) -> {
            final XQueryItemType x_ = (XQueryItemType) x;
            final XQueryItemType y_ = (XQueryItemType) y;
            if (!itemtypeIsSubtypeOf[ANY_ARRAY][MAP].test(x, y))
                return false;
            return x_.getArrayType().isSubtypeOf(y_.getMapValueType());
        };

        itemtypeIsSubtypeOf[ELEMENT][ANY_NODE] = alwaysTrue;
        itemtypeIsSubtypeOf[ELEMENT][ELEMENT] = (x, y) -> {
            final XQueryItemType x_ = (XQueryItemType) x;
            final XQueryItemType y_ = (XQueryItemType) y;
            return y_.elementNames.containsAll(x_.elementNames);
        };

        itemtypeIsSubtypeOf[FUNCTION][ANY_FUNCTION] = alwaysTrue;
        final var canBeKey = booleanEnumArray(XQueryTypes.NUMBER, XQueryTypes.BOOLEAN, XQueryTypes.STRING, XQueryTypes.ENUM);
        itemtypeIsSubtypeOf[FUNCTION][ANY_MAP] = (x, _) -> {
            final XQueryItemTypeFunction x_ = (XQueryItemTypeFunction) x;
            // function must have one argument
            if (x_.getArgumentTypes().size() != 1)
                return false;
            final var onlyArg =  (XQuerySequenceType) x_.getArgumentTypes().get(0);
            final var onlyArgItem =  (XQueryItemType) onlyArg.getItemType();
            final boolean correctOccurence = onlyArg.isOne();
            return correctOccurence
                    && canBeKey[onlyArgItem.getType().ordinal()];
        };

        itemtypeIsSubtypeOf[FUNCTION][MAP] = (x, y) -> {
            if (!itemtypeIsSubtypeOf[FUNCTION][ANY_MAP].test(x, y))
                return false;
            final XQueryItemTypeFunction x_ = (XQueryItemTypeFunction) x;
            final XQueryItemTypeMap y_ = (XQueryItemTypeMap) y;
            final var onlyArg =  (XQuerySequenceType) x_.getArgumentTypes().get(0);
            final var onlyArgItem =  (XQueryItemType) onlyArg.getItemType();
            final boolean argCanBeKey = onlyArgItem.itemtypeIsSubtypeOf(y_.getMapKeyType());
            final boolean returnedCanBeValue = x_.getReturnedType().isSubtypeOf(y_.getMapValueType());
            final boolean correctOccurence = onlyArg.isOne();
            return correctOccurence
                    && argCanBeKey
                    && returnedCanBeValue;
        };

        itemtypeIsSubtypeOf[FUNCTION][ANY_ARRAY] = (x, _) -> {
            final XQueryItemTypeFunction x_ = (XQueryItemTypeFunction) x;
            // function must have one argument
            if (x_.getArgumentTypes().size() != 1)
                return false;
            final var onlyArg =  (XQuerySequenceType) x_.getArgumentTypes().get(0);
            final var onlyArgItem =  (XQueryItemType) onlyArg.getItemType();
            // this one argument must be either number or number+
            final boolean correctOccurence = onlyArg.isOne() || onlyArg.isOneOrMore();
            return correctOccurence
                    && onlyArgItem.getType() == XQueryTypes.NUMBER;
        };

        itemtypeIsSubtypeOf[FUNCTION][ARRAY] = (x, y) -> {
            if (!itemtypeIsSubtypeOf[FUNCTION][ANY_ARRAY].test(x, y))
                return false;
            final XQueryItemTypeFunction x_ = (XQueryItemTypeFunction) x;
            final XQueryItemTypeArray y_ = (XQueryItemTypeArray) y;
            final var returnedType = x_.getReturnedType();

            return returnedType.isSubtypeOf(y_.getArrayType());
        };

        itemtypeIsSubtypeOf[FUNCTION][FUNCTION] = (x, y) -> {
            final XQueryItemTypeFunction a = (XQueryItemTypeFunction) x;
            final XQueryItemTypeFunction b = (XQueryItemTypeFunction) y;
            final List<XQuerySequenceType> aArgs = a.getArgumentTypes();
            final List<XQuerySequenceType> bArgs = b.getArgumentTypes();
            final int aArgCount = aArgs.size();
            // TODO: verify arity constraint
            if (aArgCount > bArgs.size())
                return false;
            for (int i = 0; i < aArgCount; i++) {
                final var aArgType = aArgs.get(i);
                final var bArgType = bArgs.get(i);
                if (!bArgType.isSubtypeOf(aArgType))
                    return false;
            }
            return a.getReturnedType().isSubtypeOf(b.getReturnedType());
        };


        itemtypeIsSubtypeOf[ARRAY][ANY_FUNCTION] = alwaysTrue;
        itemtypeIsSubtypeOf[ARRAY][FUNCTION] = (x, y) -> {
            final XQueryItemType x_ = (XQueryItemType) x;
            final XQueryItemType y_ = (XQueryItemType) y;
            if (y_.getArgumentTypes().size() != 1)
                return false;
            final var onlyArg =  (XQuerySequenceType) y_.getArgumentTypes().get(0);
            final var onlyArgItem = (XQueryItemType) onlyArg.getItemType();
            if (onlyArgItem.getType() == XQueryTypes.NUMBER) {

            }

            return x_.getArrayType().isSubtypeOf(y_.getReturnedType());
        };

        final XQuerySequenceType anyItems_ = new XQuerySequenceType(null, new XQueryItemTypeAnyItem(null), XQueryOccurence.ZERO_OR_MORE) ;
        final XQueryItemType anyArray_ = new XQueryItemTypeArray(anyItems_, null);
        itemtypeIsSubtypeOf[ANY_ARRAY][ARRAY] = (_, y) -> {
            final XQueryItemType y_ = (XQueryItemType) y;
            return anyArray_.itemtypeIsSubtypeOf(y_);
        };

        itemtypeIsSubtypeOf[ARRAY][ANY_ARRAY] = alwaysTrue;
        itemtypeIsSubtypeOf[ARRAY][ARRAY] = (x, y) -> {
            final XQueryItemType x_ = (XQueryItemType) x;
            final XQueryItemType y_ = (XQueryItemType) y;
            final boolean isSubtypeOfAnyArray = itemtypeIsSubtypeOf[ARRAY][ANY_ARRAY].test(x, y);
            if (!isSubtypeOfAnyArray)
                return false;
            final XQuerySequenceType xArrayItemType = x_.getArrayType();
            final XQuerySequenceType yArrayItemType = y_.getArrayType();
            return xArrayItemType.isSubtypeOf(yArrayItemType);
        };

        itemtypeIsSubtypeOf[EXTENSIBLE_RECORD][ANY_FUNCTION] = alwaysTrue;
        itemtypeIsSubtypeOf[RECORD][ANY_FUNCTION] = alwaysTrue;
        itemtypeIsSubtypeOf[ANY_MAP][ANY_FUNCTION] = alwaysTrue;

        itemtypeIsSubtypeOf[MAP][ANY_FUNCTION] = alwaysTrue;
        itemtypeIsSubtypeOf[MAP][FUNCTION] = (x, y) -> {
            final XQueryItemType x_ = (XQueryItemType) x;
            final XQueryItemType y_ = (XQueryItemType) y;
            if (y_.getArgumentTypes().size() != 1)
                return false;
            final var onlyArg =  (XQuerySequenceType) y_.getArgumentTypes().get(0);
            final var onlyArgItem =  (XQueryItemType) onlyArg.getItemType();
            final boolean correctOccurence = onlyArg.isOne();
            return correctOccurence
                    && x_.getMapKeyType().itemtypeIsSubtypeOf(onlyArgItem);
        };

        itemtypeIsSubtypeOf[MAP][ANY_MAP] = alwaysTrue;
        itemtypeIsSubtypeOf[MAP][MAP] = (x, y) -> {
            final XQueryItemType x_ = (XQueryItemType) x;
            final XQueryItemType y_ = (XQueryItemType) y;
            return x_.getMapKeyType().itemtypeIsSubtypeOf(y_.getMapKeyType())
                    && x_.getMapValueType().isSubtypeOf(y_.getMapValueType());
        };


        itemtypeIsSubtypeOf[MAP][ANY_ARRAY] = (x, _) -> {
            final XQueryItemType x_ = (XQueryItemType) x;
            // map must have a key that is a number
            final var key = (XQueryItemType) x_.getMapKeyType();
            return key.getType() == XQueryTypes.NUMBER;
        };


        itemtypeIsSubtypeOf[ENUM][STRING] = alwaysTrue;
        itemtypeIsSubtypeOf[ENUM][ENUM] = (x, y) -> {
            final var x_ = (XQueryItemTypeEnum) x;
            final var y_ = (XQueryItemTypeEnum) y;
            return y_.getEnumMembers().containsAll(x_.getEnumMembers());
        };

        itemtypeIsSubtypeOf[RECORD][ANY_MAP] = alwaysTrue;
        itemtypeIsSubtypeOf[EXTENSIBLE_RECORD][ANY_MAP] = alwaysTrue;
        itemtypeIsSubtypeOf[RECORD][MAP] = XQueryItemType::recordIsSubtypeOfMap;
        // itemtypeIsSubtypeOf[extensibleRecord][map] = XQueryEnumItemType::recordIsSubtypeOfMap;


        itemtypeIsSubtypeOf[RECORD][ANY_FUNCTION] = alwaysTrue;
        itemtypeIsSubtypeOf[EXTENSIBLE_RECORD][ANY_FUNCTION] = alwaysTrue;
        itemtypeIsSubtypeOf[RECORD][FUNCTION] = XQueryItemType::recordIsSubtypeOfFunction;
        itemtypeIsSubtypeOf[EXTENSIBLE_RECORD][FUNCTION] = XQueryItemType::recordIsSubtypeOfFunction;

        itemtypeIsSubtypeOf[RECORD][RECORD] = (x, y) -> {
            final var x_ = (XQueryItemTypeRecord) x;
            final var y_ = (XQueryItemTypeRecord) y;
            final boolean allFieldsPresent = y_.getRecordFields().keySet().containsAll(x_.getRecordFields().keySet());
            if (!allFieldsPresent)
                return false;
            for (final var key : x_.getRecordFields().keySet()) {
                final var xFieldType = x_.getRecordFields().get(key);
                final var yFieldType = y_.getRecordFields().get(key);
                if (!xFieldType.type().isSubtypeOf(yFieldType.type()))
                    return false;
            }
            return true;
        };
        itemtypeIsSubtypeOf[EXTENSIBLE_RECORD][EXTENSIBLE_RECORD] = (x, y) -> {
            // All of the following are true:
            // A is an extensible record type
            // B is an extensible record type
            final var x_ = (XQueryItemTypeRecord) x;
            final var y_ = (XQueryItemTypeRecord) y;
            // Every mandatory field in B is also declared as mandatory in A.
            if (!areAllMandatoryFieldsPresent(x_, y_)) {
                return false;
            }
            // For every field that is declared in both A and B, where the declared type in A is T
            // and the declared type in B is U, T ⊑ U .
            if  (!isEveryDeclaredFieldSubtype(x_, y_)) {
                return false;
            }
            // For every field that is declared in B but not in A, the declared type in B is item()*.
            return true;
        };

        itemtypeIsSubtypeOf[RECORD][EXTENSIBLE_RECORD] = (x, y) -> {
            // All of the following are true:
            // A is a non-extensible record type.
            // B is an extensible record type.
            final var x_ = (XQueryItemTypeRecord) x;
            final var y_ = (XQueryItemTypeRecord) y;
            // Every mandatory field in B is also declared as mandatory in A.
            if (!areAllMandatoryFieldsPresent(x_, y_)) {
                return false;
            }
            // For every field that is declared in both A and B, where the declared type in A is T
            // and the declared type in B is U, T ⊑ U .
            return isEveryDeclaredFieldSubtype(x_, y_);
        };


    }

    private static boolean isEveryDeclaredFieldSubtype(final XQueryItemTypeRecord x_, final XQueryItemTypeRecord y_) {
        final Map<String, XQueryRecordField> recordFieldsX = x_.getRecordFields();
        final var commonFields = new HashSet<String>(recordFieldsX.keySet());
        final Map<String, XQueryRecordField> recordFieldsY = y_.getRecordFields();
        commonFields.retainAll(recordFieldsY.keySet());
        for (final String commonField : commonFields) {
            final var xFieldType = recordFieldsX.get(commonField);
            final var yFieldType = recordFieldsY.get(commonField);
            if (!xFieldType.type().isSubtypeOf(yFieldType.type()))
                return false;
        }
        return true;
    }

    private static boolean areAllMandatoryFieldsPresent(final XQueryItemTypeRecord x_, final XQueryItemTypeRecord y_)
    {
        final var mandatoryFieldsX = new HashSet<String>();
        getMandatoryFields(x_, mandatoryFieldsX);
        final var mandatoryFieldsY = new HashSet<String>();
        getMandatoryFields(y_, mandatoryFieldsY);
        final boolean allMandatoryFieldsPresent = mandatoryFieldsX.containsAll(mandatoryFieldsY);
        return allMandatoryFieldsPresent;
    }

    private static void getMandatoryFields(final XQueryItemTypeRecord x_, final HashSet<String> mandatoryFieldsX) {
        for (var field : x_.getRecordFields().keySet()) {
            var recordInfo = x_.getRecordFields().get(field);
            if (recordInfo.isRequired()) {
                mandatoryFieldsX.add(field);
            }
        }
    }

    private static boolean recordIsSubtypeOfFunction(Object x, Object y) {
        final var x_ = (XQueryItemTypeRecord) x;
        final var y_ = (XQueryItemTypeFunction) y;
        final var yArgumentTypes = y_.getArgumentTypes();
        if (yArgumentTypes.size() != 1)
            return false;
        final var yFieldType = yArgumentTypes.get(0);
        final XQueryItemType yFieldItemType = yFieldType.getItemType();
        if (yFieldItemType.getType() != XQueryTypes.STRING
            && yFieldItemType.getType() != XQueryTypes.ANY_ITEM)
            return false;
        final var yReturnedType = y_.getReturnedType();
        for (final var key : x_.getRecordFields().keySet()) {
            final var xFieldType = x_.getRecordFields().get(key);
            if (!xFieldType.type().isSubtypeOf(yReturnedType))
                return false;
        }
        return true;
    }

    private static boolean recordIsSubtypeOfMap(Object x, Object y) {
        final var x_ = (XQueryItemTypeRecord) x;
        final var y_ = (XQueryItemTypeMap) y;
        final XQueryItemType keyItemType =  y_.getMapKeyType();
        if (keyItemType.getType() != XQueryTypes.STRING
            && keyItemType.getType() != XQueryTypes.ANY_ITEM)
            return false;
        final var yFieldType = y_.getMapValueType();
        for (final var key : x_.getRecordFields().keySet()) {
            final XQueryRecordField xFieldType = x_.getRecordFields().get(key);
            if (!xFieldType.type().isSubtypeOf(yFieldType))
                return false;
        }
        return true;
    }

    public boolean itemtypeIsSubtypeOf(final XQueryItemType obj) {
        if (!(obj instanceof final XQueryItemType typed))
            return false;
        return itemtypeIsSubtypeOf[typeOrdinal][typed.getType().ordinal()].test(this, obj);
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'lookup'");
    }
}
