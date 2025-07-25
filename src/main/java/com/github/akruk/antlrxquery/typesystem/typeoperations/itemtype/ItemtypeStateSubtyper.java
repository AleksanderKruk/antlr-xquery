
package com.github.akruk.antlrxquery.typesystem.typeoperations.itemtype;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import com.github.akruk.antlrxquery.typesystem.XQueryRecordField;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryItemTypeArray;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryItemTypeEnum;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryItemTypeFunction;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryItemTypeMap;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryItemTypeRecord;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryTypes;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class ItemtypeStateSubtyper
{
    private static final int STRING = XQueryTypes.STRING.ordinal();
    private static final int ELEMENT = XQueryTypes.ELEMENT.ordinal();
    private static final int ENUM = XQueryTypes.ENUM.ordinal();
    private static final int BOOLEAN = XQueryTypes.BOOLEAN.ordinal();
    private static final int NUMBER = XQueryTypes.NUMBER.ordinal();
    private static final int ERROR = XQueryTypes.ERROR.ordinal();
    private static final int ANY_ITEM = XQueryTypes.ANY_ITEM.ordinal();
    private static final int ANY_NODE = XQueryTypes.ANY_NODE.ordinal();
    private static final int ANY_MAP = XQueryTypes.ANY_MAP.ordinal();
    private static final int MAP = XQueryTypes.MAP.ordinal();
    private static final int CHOICE = XQueryTypes.CHOICE.ordinal();
    private static final int ANY_ARRAY = XQueryTypes.ANY_ARRAY.ordinal();
    private static final int ARRAY = XQueryTypes.ARRAY.ordinal();
    private static final int ANY_FUNCTION = XQueryTypes.ANY_FUNCTION.ordinal();
    private static final int FUNCTION = XQueryTypes.FUNCTION.ordinal();
    private static final int RECORD = XQueryTypes.RECORD.ordinal();
    private static final int EXTENSIBLE_RECORD = XQueryTypes.EXTENSIBLE_RECORD.ordinal();

    private final BiPredicate<XQueryItemType, XQueryItemType>[][] itemtypeIsSubtypeOf;

    @SuppressWarnings("unchecked")
    public ItemtypeStateSubtyper(final XQueryTypeFactory typeFactory)
    {
        final var len = XQueryTypes.values().length;
        itemtypeIsSubtypeOf = new BiPredicate[len][len];


        // final Predicate<XQueryItemType> allchoicesSubtyped = (y) -> {
        //     final XQueryEnumItemType y_ = (XQueryEnumItemType) y;
        //     final var items = y_.getItemTypes();
        //     final boolean anyIsSubtype = items.stream().anyMatch(i-> x.itemtypeIsSubtypeOf(i));
        //     return anyIsSubtype;
        // };

        final BiPredicate<XQueryItemType, XQueryItemType>[] allFalse = new BiPredicate[len];
        Arrays.fill(allFalse, alwaysFalse);

        // ERROR
        Arrays.fill(itemtypeIsSubtypeOf[ERROR], allFalse);
        itemtypeIsSubtypeOf[ERROR][ERROR] = alwaysTrue;
        itemtypeIsSubtypeOf[ERROR][ANY_ITEM] = alwaysTrue;
        itemtypeIsSubtypeOf[ERROR][CHOICE] = this::rightChoice;

        // ANY_ITEM
        Arrays.fill(itemtypeIsSubtypeOf[ANY_ITEM], allFalse);
        itemtypeIsSubtypeOf[ANY_ITEM][ANY_ITEM] = alwaysTrue;

        // ANY_NODE
        Arrays.fill(itemtypeIsSubtypeOf[ANY_NODE], alwaysFalse);
        itemtypeIsSubtypeOf[ANY_NODE][ANY_ITEM] = alwaysTrue;
        itemtypeIsSubtypeOf[ANY_NODE][ANY_NODE] = alwaysTrue;
        itemtypeIsSubtypeOf[ANY_NODE][CHOICE] = this::rightChoice;

        // ELEMENT
        Arrays.fill(itemtypeIsSubtypeOf[ELEMENT], alwaysFalse);
        itemtypeIsSubtypeOf[ELEMENT][ANY_ITEM] = alwaysTrue;
        itemtypeIsSubtypeOf[ELEMENT][ANY_NODE] = alwaysTrue;
        itemtypeIsSubtypeOf[ELEMENT][ELEMENT] = (x, y) -> {
            return y.getElementNames().containsAll(x.getElementNames());
        };
        itemtypeIsSubtypeOf[ELEMENT][CHOICE] = this::rightChoice;

        // ANY_MAP
        Arrays.fill(itemtypeIsSubtypeOf[ANY_MAP], alwaysFalse);
        itemtypeIsSubtypeOf[ANY_MAP][ANY_ITEM] = alwaysTrue;
        itemtypeIsSubtypeOf[ANY_MAP][ANY_MAP] = alwaysTrue;
        // TODO: refine
        // itemtypeIsSubtypeOf[MAP] = alwaysFalse;
        itemtypeIsSubtypeOf[ANY_MAP][CHOICE] = this::rightChoice;
        itemtypeIsSubtypeOf[ANY_MAP][ANY_FUNCTION] = alwaysTrue;
        // TODO: refine
        // itemtypeIsSubtypeOf[FUNCTION] = simpleChoice;

        // MAP
        Arrays.fill(itemtypeIsSubtypeOf[MAP], alwaysFalse);
        itemtypeIsSubtypeOf[MAP][ANY_ITEM] = alwaysTrue;
        itemtypeIsSubtypeOf[MAP][ANY_MAP] = alwaysTrue;
        itemtypeIsSubtypeOf[MAP][ANY_ARRAY] = (x, _) -> {
            // map must have a key that is a number
            final var key = x.getMapKeyType();
            return key.getType() == XQueryTypes.NUMBER;
        };
        itemtypeIsSubtypeOf[MAP][MAP] = (x, y) -> {
            return x.getMapKeyType().itemtypeIsSubtypeOf(y.getMapKeyType())
                    && x.getMapValueType().isSubtypeOf(y.getMapValueType());
        };
        itemtypeIsSubtypeOf[MAP][CHOICE] = this::rightChoice;
        itemtypeIsSubtypeOf[MAP][ARRAY] = (x, _) -> {
            // map must have a key that is a number
            final var key =  x.getMapKeyType();
            return key.getType() == XQueryTypes.NUMBER;
        };
        itemtypeIsSubtypeOf[MAP][ANY_FUNCTION] = alwaysTrue;
        itemtypeIsSubtypeOf[MAP][FUNCTION] = (x, y) -> {
            if (y.getArgumentTypes().size() != 1)
                return false;
            final var onlyArg =  (XQuerySequenceType) y.getArgumentTypes().get(0);
            final var onlyArgItem =  (XQueryItemType) onlyArg.itemType;
            final boolean correctOccurence = onlyArg.isOne;
            return correctOccurence
                    && x.getMapKeyType().itemtypeIsSubtypeOf(onlyArgItem);
        };

        // ANY_ARRAY
        Arrays.fill(itemtypeIsSubtypeOf[ANY_ARRAY], alwaysFalse);
        itemtypeIsSubtypeOf[ANY_ARRAY][ANY_ITEM] = alwaysTrue;
        itemtypeIsSubtypeOf[ANY_ARRAY][ANY_ARRAY] = alwaysTrue;
        itemtypeIsSubtypeOf[ANY_ARRAY][CHOICE] = this::rightChoice;
        itemtypeIsSubtypeOf[ANY_ARRAY][ARRAY] = (_, y) -> {
            final XQuerySequenceType zeroOrMoreItems = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
            return y.getArrayMemberType().equals(zeroOrMoreItems);
        };
        itemtypeIsSubtypeOf[ANY_ARRAY][ANY_FUNCTION] = alwaysTrue;
        itemtypeIsSubtypeOf[ANY_ARRAY][FUNCTION] = (_, y) -> {
            final XQueryItemTypeFunction y_ = (XQueryItemTypeFunction) y;
            final var argumentTypes = y_.getArgumentTypes();
            if (argumentTypes.size() != 1)
                return false;
            final var onlyArg =  (XQuerySequenceType) argumentTypes.get(0);
            final var onlyArgItem =  (XQueryItemType) onlyArg.itemType;
            final boolean correctOccurence = onlyArg.isOne || onlyArg.isOneOrMore;
            return correctOccurence
                    && onlyArgItem.getType() == XQueryTypes.NUMBER;
        };
        itemtypeIsSubtypeOf[ANY_ARRAY][ANY_MAP] = alwaysTrue;
        itemtypeIsSubtypeOf[ANY_ARRAY][MAP] = (_, y) -> {
            final XQueryItemTypeMap y_ = (XQueryItemTypeMap) y;
            final var mapKeyType = (XQueryItemType) y_.getMapKeyType();
            final boolean isNumber = mapKeyType.getType() == XQueryTypes.NUMBER;
            return isNumber;
        };

        // ARRAY
        Arrays.fill(itemtypeIsSubtypeOf[ARRAY], alwaysFalse);
        itemtypeIsSubtypeOf[ARRAY][ANY_ITEM] = alwaysTrue;
        itemtypeIsSubtypeOf[ARRAY][ANY_MAP] = alwaysTrue;
        itemtypeIsSubtypeOf[ARRAY][MAP] = (x, y) -> {
            final var mapKeyType =  y.getMapKeyType();
            final boolean isNumber = mapKeyType.getType() == XQueryTypes.NUMBER;
            if (!isNumber)
                return false;
            return x.getArrayMemberType().isSubtypeOf(y.getMapValueType());
        };
        itemtypeIsSubtypeOf[ARRAY][CHOICE] = this::rightChoice;
        itemtypeIsSubtypeOf[ARRAY][ANY_ARRAY] = alwaysTrue;
        itemtypeIsSubtypeOf[ARRAY][ARRAY] = (x, y) -> {
            final boolean isSubtypeOfAnyArray = itemtypeIsSubtypeOf[ANY_ARRAY][ARRAY].test(x, y);
            if (!isSubtypeOfAnyArray)
                return false;
            final XQuerySequenceType xArrayItemType = x.getArrayMemberType();
            final XQuerySequenceType yArrayItemType = y.getArrayMemberType();
            return xArrayItemType.isSubtypeOf(yArrayItemType);
        };
        itemtypeIsSubtypeOf[ARRAY][ANY_FUNCTION] = alwaysTrue;
        itemtypeIsSubtypeOf[ARRAY][FUNCTION] = (x, y) -> {
            if (y.getArgumentTypes().size() != 1)
                return false;
            final var onlyArg =   y.getArgumentTypes().get(0);
            final var onlyArgItem =  onlyArg.itemType;
            if (onlyArgItem.getType() == XQueryTypes.NUMBER) {
                // TODO

            }

            return x.getArrayMemberType().isSubtypeOf(y.getReturnedType());
        };

        // ANY_FUNCTION
        Arrays.fill(itemtypeIsSubtypeOf[ANY_FUNCTION], alwaysFalse);
        itemtypeIsSubtypeOf[ANY_FUNCTION][ANY_ITEM] = alwaysTrue;
        itemtypeIsSubtypeOf[ANY_FUNCTION][CHOICE] = this::rightChoice;
        itemtypeIsSubtypeOf[ANY_FUNCTION][ANY_FUNCTION] = alwaysTrue;

        // FUNCTION
        Arrays.fill(itemtypeIsSubtypeOf[FUNCTION], alwaysFalse);
        final var canBeKey = booleanEnumArray(XQueryTypes.NUMBER, XQueryTypes.BOOLEAN, XQueryTypes.STRING, XQueryTypes.ENUM);
        itemtypeIsSubtypeOf[FUNCTION][ANY_ITEM] = alwaysTrue;
        itemtypeIsSubtypeOf[FUNCTION][ANY_MAP] = (x, _) -> {
            final XQueryItemTypeFunction x_ = (XQueryItemTypeFunction) x;
            // function must have one argument
            if (x_.getArgumentTypes().size() != 1)
                return false;
            final var onlyArg =  (XQuerySequenceType) x_.getArgumentTypes().get(0);
            final var onlyArgItem =  (XQueryItemType) onlyArg.itemType;
            final boolean correctOccurence = onlyArg.isOne;
            return correctOccurence
                    && canBeKey[onlyArgItem.getType().ordinal()];
        };
        itemtypeIsSubtypeOf[FUNCTION][MAP] = (x, y) -> {
            if (!itemtypeIsSubtypeOf[FUNCTION][ANY_MAP].test(x, y))
                return false;
            final var onlyArg = x.getArgumentTypes().get(0);
            final var onlyArgItem = onlyArg.itemType;
            final boolean argCanBeKey = onlyArgItem.itemtypeIsSubtypeOf(y.getMapKeyType());
            final boolean returnedCanBeValue = x.getReturnedType().isSubtypeOf(y.getMapValueType());
            final boolean correctOccurence = onlyArg.isOne;
            return correctOccurence
                    && argCanBeKey
                    && returnedCanBeValue;
        };

        itemtypeIsSubtypeOf[FUNCTION][CHOICE] = this::rightChoice;
        itemtypeIsSubtypeOf[FUNCTION][ANY_ARRAY] = (x, _) -> {
            final XQueryItemTypeFunction x_ = (XQueryItemTypeFunction) x;
            // function must have one argument
            if (x_.getArgumentTypes().size() != 1)
                return false;
            final var onlyArg =  (XQuerySequenceType) x_.getArgumentTypes().get(0);
            final var onlyArgItem =  (XQueryItemType) onlyArg.itemType;
            // this one argument must be either number or number+
            final boolean correctOccurence = onlyArg.isOne || onlyArg.isOneOrMore;
            return correctOccurence
                    && onlyArgItem.getType() == XQueryTypes.NUMBER;
        };

        itemtypeIsSubtypeOf[FUNCTION][ARRAY] = (x, y) -> {
            if (!itemtypeIsSubtypeOf[FUNCTION][ANY_ARRAY].test(x, y))
                return false;
            final XQueryItemTypeFunction x_ = (XQueryItemTypeFunction) x;
            final XQueryItemTypeArray y_ = (XQueryItemTypeArray) y;
            final var returnedType = x_.getReturnedType();

            return returnedType.isSubtypeOf(y_.getArrayMemberType());
        };

        itemtypeIsSubtypeOf[FUNCTION][ANY_FUNCTION] = alwaysTrue;
        itemtypeIsSubtypeOf[FUNCTION][FUNCTION] = (x, y) -> {
            final XQueryItemTypeFunction a = (XQueryItemTypeFunction) x;
            final XQueryItemTypeFunction b = (XQueryItemTypeFunction) y;
            final List<XQuerySequenceType> aArgs = a.getArgumentTypes();
            final List<XQuerySequenceType> bArgs = b.getArgumentTypes();
            final int aArgCount = aArgs.size();

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


        // ENUM
        Arrays.fill(itemtypeIsSubtypeOf[ENUM], alwaysFalse);
        itemtypeIsSubtypeOf[ENUM][ANY_ITEM] = alwaysTrue;
        itemtypeIsSubtypeOf[ENUM][ENUM] = (x, y) -> {
            final var x_ = (XQueryItemTypeEnum) x;
            final var y_ = (XQueryItemTypeEnum) y;
            return y_.getEnumMembers().containsAll(x_.getEnumMembers());
        };
        itemtypeIsSubtypeOf[ENUM][STRING] = alwaysTrue;
        itemtypeIsSubtypeOf[ENUM][CHOICE] = this::rightChoice;


        Arrays.fill(itemtypeIsSubtypeOf[RECORD], alwaysFalse);
        itemtypeIsSubtypeOf[RECORD][ANY_FUNCTION] = alwaysTrue;
        itemtypeIsSubtypeOf[RECORD][MAP] = (x, y) -> recordIsSubtypeOfMap(x, y);
        itemtypeIsSubtypeOf[RECORD][ANY_MAP] = alwaysTrue;
        itemtypeIsSubtypeOf[RECORD][ANY_FUNCTION] = alwaysTrue;
        itemtypeIsSubtypeOf[RECORD][FUNCTION] = (x, y) -> recordIsSubtypeOfFunction(x, y);
        itemtypeIsSubtypeOf[RECORD][ANY_ITEM] = alwaysTrue;
        itemtypeIsSubtypeOf[RECORD][CHOICE] = this::rightChoice;
        itemtypeIsSubtypeOf[RECORD][RECORD] = (x, y) -> {
            final boolean allFieldsPresent = y.getRecordFields().keySet().containsAll(x.getRecordFields().keySet());
            if (!allFieldsPresent)
                return false;
            for (final var key : x.getRecordFields().keySet()) {
                final var xFieldType = x.getRecordFields().get(key);
                final var yFieldType = y.getRecordFields().get(key);
                if (!xFieldType.type().isSubtypeOf(yFieldType.type()))
                    return false;
            }
            return true;
        };
        itemtypeIsSubtypeOf[RECORD][EXTENSIBLE_RECORD] = (x, y) -> {
            // All of the following are true:
            // A is a non-extensible record type.
            // B is an extensible record type.
            // Every mandatory field in B is also declared as mandatory in A.
            if (!areAllMandatoryFieldsPresent(x, y)) {
                return false;
            }
            // For every field that is declared in both A and B, where the declared type in A is T
            // and the declared type in B is U, T ⊑ U .
            return isEveryDeclaredFieldSubtype(x, y);
        };

        // EXTENSIBLE_RECORD
        Arrays.fill(itemtypeIsSubtypeOf[EXTENSIBLE_RECORD], alwaysFalse);
        itemtypeIsSubtypeOf[EXTENSIBLE_RECORD][ANY_ITEM] = alwaysTrue;
        itemtypeIsSubtypeOf[EXTENSIBLE_RECORD][ANY_MAP] = alwaysTrue;
        // itemtypeIsSubtypeOf[EXTENSIBLE_RECORD][MAP] = simpleChoice;
        itemtypeIsSubtypeOf[EXTENSIBLE_RECORD][CHOICE] = this::rightChoice;
        // itemtypeIsSubtypeOf[ANY_ARRAY] = simpleChoice;
        // itemtypeIsSubtypeOf[ARRAY] = simpleChoice;
        itemtypeIsSubtypeOf[EXTENSIBLE_RECORD][ANY_FUNCTION] = alwaysTrue;
        itemtypeIsSubtypeOf[EXTENSIBLE_RECORD][FUNCTION] = (x_, y) -> recordIsSubtypeOfFunction(x_, y);
        // itemtypeIsSubtypeOf[EXTENSIBLE_RECORD][RECORD] = extensibleRecordMerger;
        itemtypeIsSubtypeOf[EXTENSIBLE_RECORD][EXTENSIBLE_RECORD] = (a, y) -> {
            // All of the following are true:
            // A is an extensible record type
            // B is an extensible record type
            final var x_ = (XQueryItemTypeRecord) a;
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

        // BOOLEAN
        Arrays.fill(itemtypeIsSubtypeOf[BOOLEAN], alwaysFalse);
        itemtypeIsSubtypeOf[BOOLEAN][BOOLEAN] = alwaysTrue;
        itemtypeIsSubtypeOf[BOOLEAN][ANY_ITEM] = alwaysTrue;
        itemtypeIsSubtypeOf[BOOLEAN][CHOICE] = this::rightChoice;

        // STRING
        Arrays.fill(itemtypeIsSubtypeOf[STRING], alwaysFalse);
        itemtypeIsSubtypeOf[STRING][STRING] = alwaysTrue;
        itemtypeIsSubtypeOf[STRING][ANY_ITEM] = alwaysTrue;
        itemtypeIsSubtypeOf[STRING][CHOICE] = this::rightChoice;

        // NUMBER
        Arrays.fill(itemtypeIsSubtypeOf[NUMBER], alwaysFalse);
        itemtypeIsSubtypeOf[NUMBER][NUMBER] = alwaysTrue;
        itemtypeIsSubtypeOf[NUMBER][ANY_ITEM] = alwaysTrue;
        itemtypeIsSubtypeOf[NUMBER][CHOICE] = this::rightChoice;

        // CHOICE
        final BiPredicate<XQueryItemType, XQueryItemType> lchoice = (x, y) -> {
            final var items = x.getItemTypes();
            final boolean anyIsSubtype = items.stream().allMatch(i-> i.itemtypeIsSubtypeOf(y));
            return anyIsSubtype;
        };

        Arrays.fill(itemtypeIsSubtypeOf[CHOICE], lchoice);
        itemtypeIsSubtypeOf[CHOICE][ANY_ITEM] = alwaysTrue;
        itemtypeIsSubtypeOf[CHOICE][CHOICE] = (x, y) -> {
            final var xItems = x.getItemTypes();
            final var yItems = y.getItemTypes();
            final var allPresent = xItems.stream().allMatch(xItem->{
                final boolean anyIsSubtype = yItems.stream().anyMatch(i-> xItem.itemtypeIsSubtypeOf(i));
                return anyIsSubtype;
            });
            return allPresent;
        };
    }

    private boolean rightChoice(XQueryItemType x, XQueryItemType y) {
        final var items = y.getItemTypes();
        final boolean anyIsSubtype = items.stream().anyMatch(i-> x.itemtypeIsSubtypeOf(i));
        return anyIsSubtype;
    }

    private static final BiPredicate<XQueryItemType, XQueryItemType> alwaysTrue = (_, _) -> true;
    private static final BiPredicate<XQueryItemType, XQueryItemType> alwaysFalse = (_, _) -> false;



    /**
     * Checks whether or not type1 is subtype of type2
     * @param type1 the first item type
     * @param type2 the second item type
     * @return boolean predicate
     */
    public boolean itemtypeIsSubtypeOf(XQueryItemType type1, XQueryItemType type2) {
        final int ordinal1 = type2.getType().ordinal();
        final int ordinal2 = type2.getType().ordinal();
        return this.itemtypeIsSubtypeOf[ordinal1][ordinal2].test(type1, type2);
    }

    private static boolean isEveryDeclaredFieldSubtype(final XQueryItemType x_, final XQueryItemType y_) {
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

    private static boolean areAllMandatoryFieldsPresent(final XQueryItemType x_, final XQueryItemType y_)
    {
        final var mandatoryFieldsX = new HashSet<String>();
        getMandatoryFields(x_, mandatoryFieldsX);
        final var mandatoryFieldsY = new HashSet<String>();
        getMandatoryFields(y_, mandatoryFieldsY);
        final boolean allMandatoryFieldsPresent = mandatoryFieldsX.containsAll(mandatoryFieldsY);
        return allMandatoryFieldsPresent;
    }

    private static void getMandatoryFields(final XQueryItemType x_, final HashSet<String> mandatoryFieldsX) {
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
        final var yFieldType = (XQuerySequenceType) yArgumentTypes.get(0);
        final XQueryItemType yFieldItemType = (XQueryItemType) yFieldType.itemType;
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
        final XQueryItemType keyItemType = (XQueryItemType) y_.getMapKeyType();
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



    private static boolean[] booleanEnumArray(final XQueryTypes... values) {
        final var array = new boolean[XQueryTypes.values().length];
        for (final var v : values) {
            array[v.ordinal()] = true;
        }
        return array;
    }


}
