package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.IntStream;

import com.github.akruk.antlrxquery.typesystem.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.XQueryRecordField;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.typeoperations.IItemtypeIntersectionMerger;
import com.github.akruk.antlrxquery.typesystem.typeoperations.defaults.EnumItemtypeAlternativeMerger;
import com.github.akruk.antlrxquery.typesystem.typeoperations.defaults.EnumItemtypeIntersectionMerger;
import com.github.akruk.antlrxquery.typesystem.typeoperations.defaults.EnumItemtypeUnionMerger;
import com.github.akruk.antlrxquery.typesystem.typeoperations.defaults.IItemtypeUnionMerger;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class XQueryEnumItemType implements IXQueryEnumItemType {
    private static final int STRING = XQueryTypes.STRING.ordinal();
    private static final int ENUM = XQueryTypes.ENUM.ordinal();
    private static final int BOOLEAN = XQueryTypes.BOOLEAN.ordinal();
    private static final int NUMBER = XQueryTypes.NUMBER.ordinal();
    private final XQueryTypes type;
    private final int typeOrdinal;


    private final EnumItemtypeAlternativeMerger alternativeMerger;
    private final IItemtypeIntersectionMerger intersectionMerger;
    private final XQueryTypeFactory typeFactory;
    private final Collection<XQueryItemType> itemTypes;
    private final IItemtypeUnionMerger unionMerger;

    @Override
    public Collection<XQueryItemType> getItemTypes() {
        return itemTypes;
    }
    public XQueryEnumItemType(final XQueryTypes type,
                                final List<XQuerySequenceType> argumentTypes,
                                final XQuerySequenceType returnedType,
                                final XQuerySequenceType arrayType,
                                final XQueryEnumItemType key,
                                final XQuerySequenceType mapValueType,
                                final Set<String> elementNames,
                                final XQueryTypeFactory typeFactory,
                                final Collection<XQueryItemType> itemTypes)
    {
        this.type = type;
        this.typeOrdinal = type.ordinal();
        this.argumentTypes = argumentTypes;
        this.returnedType = returnedType;
        this.arrayType = arrayType;
        this.mapKeyType = key;
        this.mapValueType = mapValueType;
        this.elementNames = elementNames;
        this.typeFactory = typeFactory;
        this.itemTypes = itemTypes;
        this.alternativeMerger = new EnumItemtypeAlternativeMerger(typeOrdinal, typeFactory);
        this.unionMerger = new EnumItemtypeUnionMerger(typeOrdinal, typeFactory);
        this.intersectionMerger = new EnumItemtypeIntersectionMerger(typeOrdinal, typeFactory);
    }


    private final List<XQuerySequenceType> argumentTypes;
    private final XQuerySequenceType returnedType;
    private final XQuerySequenceType arrayType;

    @Override
    public XQuerySequenceType getArrayType() {
        return arrayType;
    }

    private final XQueryItemType mapKeyType;

    private final XQuerySequenceType mapValueType;

    private final Set<String> elementNames;


    @Override
    public XQueryItemType getMapKeyType() {
        return mapKeyType;
    }
    @Override
    public XQuerySequenceType getMapValueType() {
        return mapValueType;
    }
    @Override
    public List<XQuerySequenceType> getArgumentTypes() {
        return argumentTypes;
    }

    @Override
    public XQuerySequenceType getReturnedType() {
        return returnedType;
    }

    @Override
    public Set<String> getElementNames() {
        return elementNames;
    }

    @Override
    public XQueryTypes getType() {
        return type;
    }

    private static boolean isNullableEquals(final Object one, final Object other) {
        if (one != null)
            return one.equals(other);
        return one == other;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof XQueryEnumItemType))
            return false;
        final IXQueryEnumItemType other = (IXQueryEnumItemType) obj;
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
    private static final BiPredicate<XQueryEnumItemType, XQueryEnumItemType> alwaysTrue = (_, _) -> true;
    private static final BiPredicate<XQueryEnumItemType, XQueryEnumItemType> alwaysFalse = (_, _) -> false;

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
        final int extensibleRecord = XQueryTypes.EXTENSIBLE_RECORD.ordinal();
        final int anyMap = XQueryTypes.ANY_MAP.ordinal();
        final int anyArray = XQueryTypes.ANY_ARRAY.ordinal();
        final int enum_ = ENUM;
        final int string = STRING;
        final int choice = XQueryTypes.CHOICE.ordinal();

        final BiPredicate<XQueryItemType, XQueryItemType> choicesubtype = (x, y) -> {
            final XQueryEnumItemType y_ = (XQueryEnumItemType) y;
            final var items = y_.getItemTypes();
            final boolean anyIsSubtype = items.stream().anyMatch(i-> x.itemtypeIsSubtypeOf(i));
            return anyIsSubtype;
        };

        for (int i = 0; i < typesCount; i++) {
            itemtypeIsSubtypeOf[i][choice] = choicesubtype;
        }

        itemtypeIsSubtypeOf[choice][choice] = (x, y) -> {
            final XQueryEnumItemType x_ = (XQueryEnumItemType) x;
            final XQueryEnumItemType y_ = (XQueryEnumItemType) y;
            final var xItems = x_.getItemTypes();
            final var yItems = y_.getItemTypes();
            final var allPresent = xItems.stream().allMatch(xItem->{
                final boolean anyIsSubtype = yItems.stream().anyMatch(i-> xItem.itemtypeIsSubtypeOf(i));
                return anyIsSubtype;
            });
            return allPresent;
        }
        ;


        itemtypeIsSubtypeOf[anyArray][anyMap] = alwaysTrue;
        itemtypeIsSubtypeOf[anyArray][map] = (_, y) -> {
            final XQueryEnumItemTypeMap y_ = (XQueryEnumItemTypeMap) y;
            final var mapKeyType = (XQueryEnumItemType) y_.getMapKeyType();
            final boolean isNumber = mapKeyType.getType() == XQueryTypes.NUMBER;
            return isNumber;
        };

        itemtypeIsSubtypeOf[anyArray][anyFunction] = alwaysTrue;
        itemtypeIsSubtypeOf[anyArray][function] = (_, y) -> {
            final XQueryEnumItemTypeFunction y_ = (XQueryEnumItemTypeFunction) y;
            final var argumentTypes = y_.getArgumentTypes();
            if (argumentTypes.size() != 1)
                return false;
            final var onlyArg =  (XQueryEnumSequenceType) argumentTypes.get(0);
            final var onlyArgItem =  (XQueryEnumItemType) onlyArg.getItemType();
            final boolean correctOccurence = onlyArg.isOne() || onlyArg.isOneOrMore();
            return correctOccurence
                    && onlyArgItem.getType() == XQueryTypes.NUMBER;
        };

        itemtypeIsSubtypeOf[array][anyMap] = alwaysTrue;
        itemtypeIsSubtypeOf[array][map] = (x, y) -> {
            final XQueryEnumItemType x_ = (XQueryEnumItemType) x;
            final XQueryEnumItemType y_ = (XQueryEnumItemType) y;
            if (!itemtypeIsSubtypeOf[anyArray][map].test(x, y))
                return false;
            return x_.getArrayType().isSubtypeOf(y_.getMapValueType());
        };

        itemtypeIsSubtypeOf[element][anyNode] = alwaysTrue;
        itemtypeIsSubtypeOf[element][element] = (x, y) -> {
            final XQueryEnumItemType x_ = (XQueryEnumItemType) x;
            final XQueryEnumItemType y_ = (XQueryEnumItemType) y;
            return y_.elementNames.containsAll(x_.elementNames);
        };

        itemtypeIsSubtypeOf[function][anyFunction] = alwaysTrue;
        final var canBeKey = booleanEnumArray(XQueryTypes.NUMBER, XQueryTypes.BOOLEAN, XQueryTypes.STRING, XQueryTypes.ENUM);
        itemtypeIsSubtypeOf[function][anyMap] = (x, _) -> {
            final XQueryEnumItemTypeFunction x_ = (XQueryEnumItemTypeFunction) x;
            // function must have one argument
            if (x_.getArgumentTypes().size() != 1)
                return false;
            final var onlyArg =  (XQueryEnumSequenceType) x_.getArgumentTypes().get(0);
            final var onlyArgItem =  (XQueryEnumItemType) onlyArg.getItemType();
            final boolean correctOccurence = onlyArg.isOne();
            return correctOccurence
                    && canBeKey[onlyArgItem.getType().ordinal()];
        };

        itemtypeIsSubtypeOf[function][map] = (x, y) -> {
            if (!itemtypeIsSubtypeOf[function][anyMap].test(x, y))
                return false;
            final XQueryEnumItemTypeFunction x_ = (XQueryEnumItemTypeFunction) x;
            final XQueryEnumItemTypeMap y_ = (XQueryEnumItemTypeMap) y;
            final var onlyArg =  (XQueryEnumSequenceType) x_.getArgumentTypes().get(0);
            final var onlyArgItem =  (XQueryEnumItemType) onlyArg.getItemType();
            final boolean argCanBeKey = onlyArgItem.itemtypeIsSubtypeOf(y_.getMapKeyType());
            final boolean returnedCanBeValue = x_.getReturnedType().isSubtypeOf(y_.getMapValueType());
            final boolean correctOccurence = onlyArg.isOne();
            return correctOccurence
                    && argCanBeKey
                    && returnedCanBeValue;
        };

        itemtypeIsSubtypeOf[function][anyArray] = (x, _) -> {
            final XQueryEnumItemTypeFunction x_ = (XQueryEnumItemTypeFunction) x;
            // function must have one argument
            if (x_.getArgumentTypes().size() != 1)
                return false;
            final var onlyArg =  (XQueryEnumSequenceType) x_.getArgumentTypes().get(0);
            final var onlyArgItem =  (XQueryEnumItemType) onlyArg.getItemType();
            // this one argument must be either number or number+
            final boolean correctOccurence = onlyArg.isOne() || onlyArg.isOneOrMore();
            return correctOccurence
                    && onlyArgItem.getType() == XQueryTypes.NUMBER;
        };

        itemtypeIsSubtypeOf[function][array] = (x, y) -> {
            if (!itemtypeIsSubtypeOf[function][anyArray].test(x, y))
                return false;
            final XQueryEnumItemTypeFunction x_ = (XQueryEnumItemTypeFunction) x;
            final XQueryEnumItemTypeArray y_ = (XQueryEnumItemTypeArray) y;
            final var returnedType = x_.getReturnedType();

            return returnedType.isSubtypeOf(y_.getArrayType());
        };

        itemtypeIsSubtypeOf[function][function] = (x, y) -> {
            final XQueryEnumItemTypeFunction a = (XQueryEnumItemTypeFunction) x;
            final XQueryEnumItemTypeFunction b = (XQueryEnumItemTypeFunction) y;
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


        itemtypeIsSubtypeOf[array][anyFunction] = alwaysTrue;
        itemtypeIsSubtypeOf[array][function] = (x, y) -> {
            final XQueryEnumItemType x_ = (XQueryEnumItemType) x;
            final XQueryEnumItemType y_ = (XQueryEnumItemType) y;
            if (y_.getArgumentTypes().size() != 1)
                return false;
            final var onlyArg =  (XQueryEnumSequenceType) y_.getArgumentTypes().get(0);
            final var onlyArgItem = (XQueryEnumItemType) onlyArg.getItemType();
            if (onlyArgItem.getType() == XQueryTypes.NUMBER) {

            }

            return x_.getArrayType().isSubtypeOf(y_.getReturnedType());
        };

        final XQueryEnumSequenceType anyItems_ = new XQueryEnumSequenceType(null, new XQueryEnumItemTypeAnyItem(null), XQueryOccurence.ZERO_OR_MORE) ;
        final XQueryItemType anyArray_ = new XQueryEnumItemTypeArray(anyItems_, null);
        itemtypeIsSubtypeOf[anyArray][array] = (_, y) -> {
            final XQueryEnumItemType y_ = (XQueryEnumItemType) y;
            return anyArray_.itemtypeIsSubtypeOf(y_);
        };

        itemtypeIsSubtypeOf[array][anyArray] = alwaysTrue;
        itemtypeIsSubtypeOf[array][array] = (x, y) -> {
            final XQueryEnumItemType x_ = (XQueryEnumItemType) x;
            final XQueryEnumItemType y_ = (XQueryEnumItemType) y;
            final boolean isSubtypeOfAnyArray = itemtypeIsSubtypeOf[array][anyArray].test(x, y);
            if (!isSubtypeOfAnyArray)
                return false;
            final XQuerySequenceType xArrayItemType = x_.getArrayType();
            final XQuerySequenceType yArrayItemType = y_.getArrayType();
            return xArrayItemType.isSubtypeOf(yArrayItemType);
        };

        itemtypeIsSubtypeOf[extensibleRecord][anyFunction] = alwaysTrue;
        itemtypeIsSubtypeOf[record][anyFunction] = alwaysTrue;
        itemtypeIsSubtypeOf[anyMap][anyFunction] = alwaysTrue;

        itemtypeIsSubtypeOf[map][anyFunction] = alwaysTrue;
        itemtypeIsSubtypeOf[map][function] = (x, y) -> {
            final XQueryEnumItemType x_ = (XQueryEnumItemType) x;
            final XQueryEnumItemType y_ = (XQueryEnumItemType) y;
            if (y_.getArgumentTypes().size() != 1)
                return false;
            final var onlyArg =  (XQueryEnumSequenceType) y_.getArgumentTypes().get(0);
            final var onlyArgItem =  (XQueryEnumItemType) onlyArg.getItemType();
            final boolean correctOccurence = onlyArg.isOne();
            return correctOccurence
                    && x_.getMapKeyType().itemtypeIsSubtypeOf(onlyArgItem);
        };

        itemtypeIsSubtypeOf[map][anyMap] = alwaysTrue;
        itemtypeIsSubtypeOf[map][map] = (x, y) -> {
            final XQueryEnumItemType x_ = (XQueryEnumItemType) x;
            final XQueryEnumItemType y_ = (XQueryEnumItemType) y;
            return x_.getMapKeyType().itemtypeIsSubtypeOf(y_.getMapKeyType())
                    && x_.getMapValueType().isSubtypeOf(y_.getMapValueType());
        };


        itemtypeIsSubtypeOf[map][anyArray] = (x, _) -> {
            final XQueryEnumItemType x_ = (XQueryEnumItemType) x;
            // map must have a key that is a number
            final var key = (XQueryEnumItemType) x_.getMapKeyType();
            return key.getType() == XQueryTypes.NUMBER;
        };


        itemtypeIsSubtypeOf[enum_][string] = alwaysTrue;
        itemtypeIsSubtypeOf[enum_][enum_] = (x, y) -> {
            final var x_ = (XQueryEnumItemTypeEnum) x;
            final var y_ = (XQueryEnumItemTypeEnum) y;
            return y_.getEnumMembers().containsAll(x_.getEnumMembers());
        };

        itemtypeIsSubtypeOf[record][anyMap] = alwaysTrue;
        itemtypeIsSubtypeOf[extensibleRecord][anyMap] = alwaysTrue;
        itemtypeIsSubtypeOf[record][map] = XQueryEnumItemType::recordIsSubtypeOfMap;
        // itemtypeIsSubtypeOf[extensibleRecord][map] = XQueryEnumItemType::recordIsSubtypeOfMap;


        itemtypeIsSubtypeOf[record][anyFunction] = alwaysTrue;
        itemtypeIsSubtypeOf[extensibleRecord][anyFunction] = alwaysTrue;
        itemtypeIsSubtypeOf[record][function] = XQueryEnumItemType::recordIsSubtypeOfFunction;
        itemtypeIsSubtypeOf[extensibleRecord][function] = XQueryEnumItemType::recordIsSubtypeOfFunction;

        itemtypeIsSubtypeOf[record][record] = (x, y) -> {
            final var x_ = (XQueryEnumItemTypeRecord) x;
            final var y_ = (XQueryEnumItemTypeRecord) y;
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
        itemtypeIsSubtypeOf[extensibleRecord][extensibleRecord] = (x, y) -> {
            // All of the following are true:
            // A is an extensible record type
            // B is an extensible record type
            final var x_ = (XQueryEnumItemTypeRecord) x;
            final var y_ = (XQueryEnumItemTypeRecord) y;
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

        itemtypeIsSubtypeOf[record][extensibleRecord] = (x, y) -> {
            // All of the following are true:
            // A is a non-extensible record type.
            // B is an extensible record type.
            final var x_ = (XQueryEnumItemTypeRecord) x;
            final var y_ = (XQueryEnumItemTypeRecord) y;
            // Every mandatory field in B is also declared as mandatory in A.
            if (!areAllMandatoryFieldsPresent(x_, y_)) {
                return false;
            }
            // For every field that is declared in both A and B, where the declared type in A is T
            // and the declared type in B is U, T ⊑ U .
            return isEveryDeclaredFieldSubtype(x_, y_);
        };


    }

    private static boolean isEveryDeclaredFieldSubtype(final XQueryEnumItemTypeRecord x_, final XQueryEnumItemTypeRecord y_) {
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

    private static boolean areAllMandatoryFieldsPresent(final XQueryEnumItemTypeRecord x_, final XQueryEnumItemTypeRecord y_)
    {
        final var mandatoryFieldsX = new HashSet<String>();
        getMandatoryFields(x_, mandatoryFieldsX);
        final var mandatoryFieldsY = new HashSet<String>();
        getMandatoryFields(y_, mandatoryFieldsY);
        final boolean allMandatoryFieldsPresent = mandatoryFieldsX.containsAll(mandatoryFieldsY);
        return allMandatoryFieldsPresent;
    }

    private static void getMandatoryFields(final XQueryEnumItemTypeRecord x_, final HashSet<String> mandatoryFieldsX) {
        for (var field : x_.getRecordFields().keySet()) {
            var recordInfo = x_.getRecordFields().get(field);
            if (recordInfo.isRequired()) {
                mandatoryFieldsX.add(field);
            }
        }
    }

    private static boolean recordIsSubtypeOfFunction(Object x, Object y) {
        final var x_ = (XQueryEnumItemTypeRecord) x;
        final var y_ = (XQueryEnumItemTypeFunction) y;
        final var yArgumentTypes = y_.getArgumentTypes();
        if (yArgumentTypes.size() != 1)
            return false;
        final var yFieldType = (XQueryEnumSequenceType) yArgumentTypes.get(0);
        final IXQueryEnumItemType yFieldItemType = (IXQueryEnumItemType) yFieldType.getItemType();
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
        final var x_ = (XQueryEnumItemTypeRecord) x;
        final var y_ = (XQueryEnumItemTypeMap) y;
        final IXQueryEnumItemType keyItemType = (IXQueryEnumItemType) y_.getMapKeyType();
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

    @Override
    public boolean itemtypeIsSubtypeOf(final XQueryItemType obj) {
        if (!(obj instanceof XQueryEnumItemType))
            return false;
        final IXQueryEnumItemType typed = (IXQueryEnumItemType) obj;
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

    @Override
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
    @Override
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
        final int anyItem = XQueryTypes.ANY_ITEM.ordinal();
        for (int i = 0; i < typesCount; i++) {
            castableAs[i][anyItem] = true;
            castableAs[i][string] = true;
        }
    }

    @Override
    public boolean castableAs(final XQueryItemType itemType) {
        if (!(itemType instanceof XQueryEnumItemType))
            return false;
        final IXQueryEnumItemType typed = (IXQueryEnumItemType) itemType;
        return castableAs[typeOrdinal][typed.getType().ordinal()];
    }

    @Override
    public XQueryItemType unionMerge(final XQueryItemType other) {
        return unionMerger.unionMerge(this, other);
    }

    @Override
    public XQueryItemType intersectionMerge(final XQueryItemType other) {
        return intersectionMerger.intersectionMerge(this, other);
    }

    @Override
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


    @Override
    public boolean isValueComparableWith(final XQueryItemType other) {
        final IXQueryEnumItemType other_ = (IXQueryEnumItemType) other;
        return isValueComparableWith[typeOrdinal][other_.getType().ordinal()];
    }

    @Override
    public XQueryItemType alternativeMerge(XQueryItemType other) {
        return  alternativeMerger.alternativeMerge(this, other);
    }
}
