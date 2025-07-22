
package com.github.akruk.antlrxquery.typesystem.typeoperations;

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

public class ItemtypeStateSubtyper2 extends ItemtypeBinaryPredicateOperation
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
    private final XQueryTypeFactory typeFactory;

    @SuppressWarnings("unchecked")
    public ItemtypeStateSubtyper2(final XQueryTypeFactory typeFactory)
    {
        this.typeFactory = typeFactory;
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
            final var onlyArgItem =  (XQueryItemType) onlyArg.getItemType();
            final boolean correctOccurence = onlyArg.isOne();
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
            final var onlyArgItem =  (XQueryItemType) onlyArg.getItemType();
            final boolean correctOccurence = onlyArg.isOne() || onlyArg.isOneOrMore();
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
            final var onlyArgItem =  onlyArg.getItemType();
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
            final var onlyArgItem =  (XQueryItemType) onlyArg.getItemType();
            final boolean correctOccurence = onlyArg.isOne();
            return correctOccurence
                    && canBeKey[onlyArgItem.getType().ordinal()];
        };
        itemtypeIsSubtypeOf[FUNCTION][MAP] = (x, y) -> {
            if (!itemtypeIsSubtypeOf[FUNCTION][ANY_MAP].test(x, y))
                return false;
            final var onlyArg = x.getArgumentTypes().get(0);
            final var onlyArgItem = onlyArg.getItemType();
            final boolean argCanBeKey = onlyArgItem.itemtypeIsSubtypeOf(y.getMapKeyType());
            final boolean returnedCanBeValue = x.getReturnedType().isSubtypeOf(y.getMapValueType());
            final boolean correctOccurence = onlyArg.isOne();
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
        final XQueryItemType yFieldItemType = (XQueryItemType) yFieldType.getItemType();
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

    @Override
    public boolean errorErrorOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean errorAnyItemOperation(XQueryItemType x, XQueryItemType y) {
        return true;
    }

    @Override
    public boolean errorAnyNodeOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean errorElementOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean errorEnumOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean errorBooleanOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean errorNumberOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean errorStringOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean errorAnyMapOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean errorMapOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean errorChoiceOperation(XQueryItemType x, XQueryItemType y) {
        return rightChoice(x, y);
    }

    @Override
    public boolean errorAnyArrayOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean errorArrayOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean errorAnyFunctionOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean errorFunctionOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean errorRecordOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean errorExtensibleRecordOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }





    @Override
    public boolean anyItemErrorOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyItemAnyItemOperation(XQueryItemType x, XQueryItemType y) {
        return true; // As per your condition for ANY_ITEM and ANY_ITEM
    }

    @Override
    public boolean anyItemAnyNodeOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyItemElementOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyItemEnumOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyItemBooleanOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyItemNumberOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyItemStringOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyItemAnyMapOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyItemMapOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyItemChoiceOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyItemAnyArrayOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyItemArrayOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyItemAnyFunctionOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyItemFunctionOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyItemRecordOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyItemExtensibleRecordOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }






    @Override
    public boolean anyNodeErrorOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyNodeAnyItemOperation(XQueryItemType x, XQueryItemType y) {
        return true; // As per your condition for ANY_NODE and ANY_ITEM
    }

    @Override
    public boolean anyNodeAnyNodeOperation(XQueryItemType x, XQueryItemType y) {
        return true; // As per your condition for ANY_NODE and ANY_NODE
    }

    @Override
    public boolean anyNodeElementOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyNodeEnumOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyNodeBooleanOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyNodeNumberOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyNodeStringOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyNodeAnyMapOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyNodeMapOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyNodeChoiceOperation(XQueryItemType x, XQueryItemType y) {
        return rightChoice(x, y); // Assuming rightChoice is a method in the class
    }

    @Override
    public boolean anyNodeAnyArrayOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyNodeArrayOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyNodeAnyFunctionOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyNodeFunctionOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyNodeRecordOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyNodeExtensibleRecordOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean elementErrorOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean elementAnyItemOperation(XQueryItemType x, XQueryItemType y) {
        return true; // As per your condition for ELEMENT and ANY_ITEM
    }

    @Override
    public boolean elementAnyNodeOperation(XQueryItemType x, XQueryItemType y) {
        return true; // As per your condition for ELEMENT and ANY_NODE
    }

    @Override
    public boolean elementElementOperation(XQueryItemType x, XQueryItemType y) {
        return y.getElementNames().containsAll(x.getElementNames()); // As per your condition for ELEMENT and ELEMENT
    }

    @Override
    public boolean elementEnumOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean elementBooleanOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean elementNumberOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean elementStringOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean elementAnyMapOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean elementMapOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean elementChoiceOperation(XQueryItemType x, XQueryItemType y) {
        return rightChoice(x, y); // Assuming rightChoice is a method in the class
    }

    @Override
    public boolean elementAnyArrayOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean elementArrayOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean elementAnyFunctionOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean elementFunctionOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean elementRecordOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean elementExtensibleRecordOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }





    @Override
    public boolean anyMapErrorOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyMapAnyItemOperation(XQueryItemType x, XQueryItemType y) {
        return true; // As per your condition for ANY_MAP and ANY_ITEM
    }

    @Override
    public boolean anyMapAnyNodeOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyMapElementOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyMapEnumOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyMapBooleanOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyMapNumberOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyMapStringOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyMapAnyMapOperation(XQueryItemType x, XQueryItemType y) {
        return true; // As per your condition for ANY_MAP and ANY_MAP
    }

    @Override
    public boolean anyMapMapOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyMapChoiceOperation(XQueryItemType x, XQueryItemType y) {
        return rightChoice(x, y); // Assuming rightChoice is a method in the class
    }

    @Override
    public boolean anyMapAnyArrayOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyMapArrayOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyMapAnyFunctionOperation(XQueryItemType x, XQueryItemType y) {
        return true; // As per your condition for ANY_MAP and ANY_FUNCTION
    }

    @Override
    public boolean anyMapFunctionOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyMapRecordOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyMapExtensibleRecordOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean mapErrorOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean mapAnyItemOperation(XQueryItemType x, XQueryItemType y) {
        return true; // As per your condition for MAP and ANY_ITEM
    }

    @Override
    public boolean mapAnyNodeOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean mapElementOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean mapEnumOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean mapBooleanOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean mapNumberOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean mapStringOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean mapAnyMapOperation(XQueryItemType x, XQueryItemType y) {
        return true; // As per your condition for MAP and ANY_MAP
    }

    @Override
    public boolean mapMapOperation(XQueryItemType x, XQueryItemType y) {
        // Check if the map key type and map value type of x are subtypes of those in y
        return x.getMapKeyType().itemtypeIsSubtypeOf(y.getMapKeyType()) &&
            x.getMapValueType().isSubtypeOf(y.getMapValueType());
    }

    @Override
    public boolean mapChoiceOperation(XQueryItemType x, XQueryItemType y) {
        return rightChoice(x, y); // Assuming rightChoice is a method in the class
    }

    @Override
    public boolean mapAnyArrayOperation(XQueryItemType x, XQueryItemType y) {
        // Check if the map key is a number
        return x.getMapKeyType().getType() == XQueryTypes.NUMBER;
    }

    @Override
    public boolean mapArrayOperation(XQueryItemType x, XQueryItemType y) {
        // Check if the map key is a number
        return x.getMapKeyType().getType() == XQueryTypes.NUMBER;
    }

    @Override
    public boolean mapAnyFunctionOperation(XQueryItemType x, XQueryItemType y) {
        return true; // As per your condition for MAP and ANY_FUNCTION
    }

    @Override
    public boolean mapFunctionOperation(XQueryItemType x, XQueryItemType y) {
        // Check if y has exactly one argument type and if the map key type of x is a subtype of that argument type
        if (y.getArgumentTypes().size() != 1) {
            return false;
        }
        XQuerySequenceType onlyArg = (XQuerySequenceType) y.getArgumentTypes().get(0);
        XQueryItemType onlyArgItem = (XQueryItemType) onlyArg.getItemType();
        boolean correctOccurrence = onlyArg.isOne();
        return correctOccurrence && x.getMapKeyType().itemtypeIsSubtypeOf(onlyArgItem);
    }

    @Override
    public boolean mapRecordOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean mapExtensibleRecordOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }




    @Override
    public boolean anyArrayErrorOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyArrayAnyItemOperation(XQueryItemType x, XQueryItemType y) {
        return true; // As per your condition for ANY_ARRAY and ANY_ITEM
    }

    @Override
    public boolean anyArrayAnyNodeOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyArrayElementOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyArrayEnumOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyArrayBooleanOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyArrayNumberOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyArrayStringOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyArrayAnyMapOperation(XQueryItemType x, XQueryItemType y) {
        return true; // As per your condition for ANY_ARRAY and ANY_MAP
    }

    @Override
    public boolean anyArrayMapOperation(XQueryItemType x, XQueryItemType y) {
        XQueryItemTypeMap yMap = (XQueryItemTypeMap) y;
        XQueryItemType mapKeyType = yMap.getMapKeyType();
        return mapKeyType.getType() == XQueryTypes.NUMBER; // Check if the map key is a number
    }

    @Override
    public boolean anyArrayChoiceOperation(XQueryItemType x, XQueryItemType y) {
        return rightChoice(x, y); // Assuming rightChoice is a method in the class
    }

    @Override
    public boolean anyArrayAnyArrayOperation(XQueryItemType x, XQueryItemType y) {
        return true; // As per your condition for ANY_ARRAY and ANY_ARRAY
    }

    @Override
    public boolean anyArrayArrayOperation(XQueryItemType x, XQueryItemType y) {

        XQuerySequenceType zeroOrMoreItems = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
        return y.getArrayMemberType().equals(zeroOrMoreItems.getItemType()); // Check if y's array member type is zero or more items
    }

    @Override
    public boolean anyArrayAnyFunctionOperation(XQueryItemType x, XQueryItemType y) {
        return true; // As per your condition for ANY_ARRAY and ANY_FUNCTION
    }

    @Override
    public boolean anyArrayFunctionOperation(XQueryItemType x, XQueryItemType y) {
        XQueryItemTypeFunction yFunc = (XQueryItemTypeFunction) y;
        List<XQuerySequenceType> argumentTypes = yFunc.getArgumentTypes();
        if (argumentTypes.size() != 1) {
            return false;
        }
        XQuerySequenceType onlyArg = argumentTypes.get(0);
        XQueryItemType onlyArgItem = (XQueryItemType) onlyArg.getItemType();
        boolean correctOccurrence = onlyArg.isOne() || onlyArg.isOneOrMore();
        return correctOccurrence && onlyArgItem.getType() == XQueryTypes.NUMBER; // Check if the argument type is a number
    }

    @Override
    public boolean anyArrayRecordOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean anyArrayExtensibleRecordOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean arrayErrorOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean arrayAnyItemOperation(XQueryItemType x, XQueryItemType y) {
        return true; // As per your condition for ARRAY and ANY_ITEM
    }

    @Override
    public boolean arrayAnyNodeOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean arrayElementOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean arrayEnumOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean arrayBooleanOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean arrayNumberOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean arrayStringOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean arrayAnyMapOperation(XQueryItemType x, XQueryItemType y) {
        return true; // As per your condition for ARRAY and ANY_MAP
    }

    @Override
    public boolean arrayMapOperation(XQueryItemType x, XQueryItemType y) {
        XQueryItemTypeMap yMap = (XQueryItemTypeMap) y;
        XQueryItemType mapKeyType = yMap.getMapKeyType();
        boolean isNumber = mapKeyType.getType() == XQueryTypes.NUMBER;
        if (!isNumber) {
            return false;
        }
        return x.getArrayMemberType().isSubtypeOf(yMap.getMapValueType()); // Check if x's array member type is a subtype of y's map value type
    }

    @Override
    public boolean arrayChoiceOperation(XQueryItemType x, XQueryItemType y) {
        return rightChoice(x, y); // Assuming rightChoice is a method in the class
    }

    @Override
    public boolean arrayAnyArrayOperation(XQueryItemType x, XQueryItemType y) {
        return true; // As per your condition for ARRAY and ANY_ARRAY
    }

    @Override
    public boolean arrayArrayOperation(XQueryItemType x, XQueryItemType y) {
        boolean isSubtypeOfAnyArray = anyArrayArrayOperation(x, y); // Check if x is a subtype of any array
        if (!isSubtypeOfAnyArray) {
            return false;
        }
        XQuerySequenceType xArrayItemType = x.getArrayMemberType();
        XQuerySequenceType yArrayItemType = y.getArrayMemberType();
        return xArrayItemType.isSubtypeOf(yArrayItemType); // Check if x's array member type is a subtype of y's array member type
    }

    @Override
    public boolean arrayAnyFunctionOperation(XQueryItemType x, XQueryItemType y) {
        return true; // As per your condition for ARRAY and ANY_FUNCTION
    }

    @Override
    public boolean arrayFunctionOperation(XQueryItemType x, XQueryItemType y) {
        XQueryItemTypeFunction yFunc = (XQueryItemTypeFunction) y;
        if (yFunc.getArgumentTypes().size() != 1) {
            return false;
        }
        XQueryItemType onlyArgItem = yFunc.getArgumentTypes().get(0).getItemType();
        if (onlyArgItem.getType() == XQueryTypes.NUMBER) {
            // Additional logic can be added here if needed
        }
        return x.getArrayMemberType().isSubtypeOf(yFunc.getReturnedType()); // Check if x's array member type is a subtype of y's returned type
    }

    @Override
    public boolean arrayRecordOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }

    @Override
    public boolean arrayExtensibleRecordOperation(XQueryItemType x, XQueryItemType y) {
        return false;
    }



@Override
public boolean anyFunctionErrorOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean anyFunctionAnyItemOperation(XQueryItemType x, XQueryItemType y) {
    return true; // As per your condition for ANY_FUNCTION and ANY_ITEM
}

@Override
public boolean anyFunctionAnyNodeOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean anyFunctionElementOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean anyFunctionEnumOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean anyFunctionBooleanOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean anyFunctionNumberOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean anyFunctionStringOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean anyFunctionAnyMapOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean anyFunctionMapOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean anyFunctionChoiceOperation(XQueryItemType x, XQueryItemType y) {
    return rightChoice(x, y); // Assuming rightChoice is a method in the class
}

@Override
public boolean anyFunctionAnyArrayOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean anyFunctionArrayOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean anyFunctionAnyFunctionOperation(XQueryItemType x, XQueryItemType y) {
    return true; // As per your condition for ANY_FUNCTION and ANY_FUNCTION
}

@Override
public boolean anyFunctionFunctionOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean anyFunctionRecordOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean anyFunctionExtensibleRecordOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean functionErrorOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean functionAnyItemOperation(XQueryItemType x, XQueryItemType y) {
    return true; // As per your condition for FUNCTION and ANY_ITEM
}

@Override
public boolean functionAnyNodeOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean functionElementOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean functionEnumOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean functionBooleanOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean functionNumberOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean functionStringOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}


final boolean[] canBeKey = booleanEnumArray(XQueryTypes.NUMBER, XQueryTypes.BOOLEAN, XQueryTypes.STRING, XQueryTypes.ENUM);
@Override
public boolean functionAnyMapOperation(XQueryItemType x, XQueryItemType y) {
    XQueryItemTypeFunction xFunc = (XQueryItemTypeFunction) x;
    if (xFunc.getArgumentTypes().size() != 1) {
        return false;
    }
    XQuerySequenceType onlyArg = xFunc.getArgumentTypes().get(0);
    XQueryItemType onlyArgItem = (XQueryItemType) onlyArg.getItemType();
    boolean correctOccurrence = onlyArg.isOne();
    return correctOccurrence && canBeKey[onlyArgItem.getType().ordinal()]; // Check if the argument type can be a key
}

@Override
public boolean functionMapOperation(XQueryItemType x, XQueryItemType y) {
    if (!functionAnyMapOperation(x, y)) {
        return false;
    }
    XQueryItemTypeFunction xFunc = (XQueryItemTypeFunction) x;
    XQuerySequenceType onlyArg = xFunc.getArgumentTypes().get(0);
    XQueryItemType onlyArgItem = (XQueryItemType) onlyArg.getItemType();
    boolean argCanBeKey = onlyArgItem.itemtypeIsSubtypeOf(y.getMapKeyType());
    boolean returnedCanBeValue = xFunc.getReturnedType().isSubtypeOf(y.getMapValueType());
    boolean correctOccurrence = onlyArg.isOne();
    return correctOccurrence && argCanBeKey && returnedCanBeValue;
}

@Override
public boolean functionChoiceOperation(XQueryItemType x, XQueryItemType y) {
    return rightChoice(x, y); // Assuming rightChoice is a method in the class
}

@Override
public boolean functionAnyArrayOperation(XQueryItemType x, XQueryItemType y) {
    XQueryItemTypeFunction xFunc = (XQueryItemTypeFunction) x;
    if (xFunc.getArgumentTypes().size() != 1) {
        return false;
    }
    XQuerySequenceType onlyArg = xFunc.getArgumentTypes().get(0);
    XQueryItemType onlyArgItem = (XQueryItemType) onlyArg.getItemType();
    boolean correctOccurrence = onlyArg.isOne() || onlyArg.isOneOrMore();
    return correctOccurrence && onlyArgItem.getType() == XQueryTypes.NUMBER; // Check if the argument type is a number
}

@Override
public boolean functionArrayOperation(XQueryItemType x, XQueryItemType y) {
    if (!functionAnyArrayOperation(x, y)) {
        return false;
    }
    XQueryItemTypeFunction xFunc = (XQueryItemTypeFunction) x;
    XQueryItemTypeArray yArray = (XQueryItemTypeArray) y;
    return xFunc.getReturnedType().isSubtypeOf(yArray.getArrayMemberType()); // Check if the returned type is a subtype of the array member type
}

@Override
public boolean functionAnyFunctionOperation(XQueryItemType x, XQueryItemType y) {
    return true; // As per your condition for FUNCTION and ANY_FUNCTION
}

@Override
public boolean functionFunctionOperation(XQueryItemType x, XQueryItemType y) {
    XQueryItemTypeFunction xFunc = (XQueryItemTypeFunction) x;
    XQueryItemTypeFunction yFunc = (XQueryItemTypeFunction) y;
    List<XQuerySequenceType> xArgs = xFunc.getArgumentTypes();
    List<XQuerySequenceType> yArgs = yFunc.getArgumentTypes();
    int xArgCount = xArgs.size();
    if (xArgCount > yArgs.size()) {
        return false;
    }
    for (int i = 0; i < xArgCount; i++) {
        XQuerySequenceType xArgType = xArgs.get(i);
        XQuerySequenceType yArgType = yArgs.get(i);
        if (!yArgType.isSubtypeOf(xArgType)) {
            return false;
        }
    }
    return xFunc.getReturnedType().isSubtypeOf(yFunc.getReturnedType()); // Check if the returned type of x is a subtype of the returned type of y
}

@Override
public boolean functionRecordOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean functionExtensibleRecordOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}






@Override
public boolean enumErrorOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean enumAnyItemOperation(XQueryItemType x, XQueryItemType y) {
    return true; // As per your condition for ENUM and ANY_ITEM
}

@Override
public boolean enumAnyNodeOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean enumElementOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean enumEnumOperation(XQueryItemType x, XQueryItemType y) {
    XQueryItemTypeEnum xEnum = (XQueryItemTypeEnum) x;
    XQueryItemTypeEnum yEnum = (XQueryItemTypeEnum) y;
    return yEnum.getEnumMembers().containsAll(xEnum.getEnumMembers()); // Check if y's enum members contain all of x's
}

@Override
public boolean enumBooleanOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean enumNumberOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean enumStringOperation(XQueryItemType x, XQueryItemType y) {
    return true; // As per your condition for ENUM and STRING
}

@Override
public boolean enumAnyMapOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean enumMapOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean enumChoiceOperation(XQueryItemType x, XQueryItemType y) {
    return rightChoice(x, y); // Assuming rightChoice is a method in the class
}

@Override
public boolean enumAnyArrayOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean enumArrayOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean enumAnyFunctionOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean enumFunctionOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean enumRecordOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean enumExtensibleRecordOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean booleanErrorOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean booleanAnyItemOperation(XQueryItemType x, XQueryItemType y) {
    return true; // As per your condition for BOOLEAN and ANY_ITEM
}

@Override
public boolean booleanAnyNodeOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean booleanElementOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean booleanEnumOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean booleanBooleanOperation(XQueryItemType x, XQueryItemType y) {
    return true; // As per your condition for BOOLEAN and BOOLEAN
}

@Override
public boolean booleanNumberOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean booleanStringOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean booleanAnyMapOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean booleanMapOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean booleanChoiceOperation(XQueryItemType x, XQueryItemType y) {
    return rightChoice(x, y); // Assuming rightChoice is a method in the class
}

@Override
public boolean booleanAnyArrayOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean booleanArrayOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean booleanAnyFunctionOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean booleanFunctionOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean booleanRecordOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean booleanExtensibleRecordOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}



@Override
public boolean stringErrorOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean stringAnyItemOperation(XQueryItemType x, XQueryItemType y) {
    return true; // As per your condition for STRING and ANY_ITEM
}

@Override
public boolean stringAnyNodeOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean stringElementOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean stringEnumOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean stringBooleanOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean stringNumberOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean stringStringOperation(XQueryItemType x, XQueryItemType y) {
    return true; // As per your condition for STRING and STRING
}

@Override
public boolean stringAnyMapOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean stringMapOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean stringChoiceOperation(XQueryItemType x, XQueryItemType y) {
    return rightChoice(x, y); // Assuming rightChoice is a method in the class
}

@Override
public boolean stringAnyArrayOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean stringArrayOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean stringAnyFunctionOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean stringFunctionOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean stringRecordOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean stringExtensibleRecordOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean numberErrorOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean numberAnyItemOperation(XQueryItemType x, XQueryItemType y) {
    return true; // As per your condition for NUMBER and ANY_ITEM
}

@Override
public boolean numberAnyNodeOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean numberElementOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean numberEnumOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean numberBooleanOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean numberNumberOperation(XQueryItemType x, XQueryItemType y) {
    return true; // As per your condition for NUMBER and NUMBER
}

@Override
public boolean numberStringOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean numberAnyMapOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean numberMapOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean numberChoiceOperation(XQueryItemType x, XQueryItemType y) {
    return rightChoice(x, y); // Assuming rightChoice is a method in the class
}

@Override
public boolean numberAnyArrayOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean numberArrayOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean numberAnyFunctionOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean numberFunctionOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean numberRecordOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}

@Override
public boolean numberExtensibleRecordOperation(XQueryItemType x, XQueryItemType y) {
    return false;
}





    @Override
    public boolean choiceErrorOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'choiceErrorOperation'");
    }

    @Override
    public boolean choiceAnyItemOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'choiceAnyItemOperation'");
    }

    @Override
    public boolean choiceAnyNodeOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'choiceAnyNodeOperation'");
    }

    @Override
    public boolean choiceElementOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'choiceElementOperation'");
    }

    @Override
    public boolean choiceEnumOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'choiceEnumOperation'");
    }

    @Override
    public boolean choiceBooleanOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'choiceBooleanOperation'");
    }

    @Override
    public boolean choiceNumberOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'choiceNumberOperation'");
    }

    @Override
    public boolean choiceStringOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'choiceStringOperation'");
    }

    @Override
    public boolean choiceAnyMapOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'choiceAnyMapOperation'");
    }

    @Override
    public boolean choiceMapOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'choiceMapOperation'");
    }

    @Override
    public boolean choiceChoiceOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'choiceChoiceOperation'");
    }

    @Override
    public boolean choiceAnyArrayOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'choiceAnyArrayOperation'");
    }

    @Override
    public boolean choiceArrayOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'choiceArrayOperation'");
    }

    @Override
    public boolean choiceAnyFunctionOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'choiceAnyFunctionOperation'");
    }

    @Override
    public boolean choiceFunctionOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'choiceFunctionOperation'");
    }

    @Override
    public boolean choiceRecordOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'choiceRecordOperation'");
    }

    @Override
    public boolean choiceExtensibleRecordOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'choiceExtensibleRecordOperation'");
    }





    @Override
    public boolean recordErrorOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recordErrorOperation'");
    }

    @Override
    public boolean recordAnyItemOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recordAnyItemOperation'");
    }

    @Override
    public boolean recordAnyNodeOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recordAnyNodeOperation'");
    }

    @Override
    public boolean recordElementOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recordElementOperation'");
    }

    @Override
    public boolean recordEnumOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recordEnumOperation'");
    }

    @Override
    public boolean recordBooleanOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recordBooleanOperation'");
    }

    @Override
    public boolean recordNumberOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recordNumberOperation'");
    }

    @Override
    public boolean recordStringOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recordStringOperation'");
    }

    @Override
    public boolean recordAnyMapOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recordAnyMapOperation'");
    }

    @Override
    public boolean recordMapOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recordMapOperation'");
    }

    @Override
    public boolean recordChoiceOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recordChoiceOperation'");
    }

    @Override
    public boolean recordAnyArrayOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recordAnyArrayOperation'");
    }

    @Override
    public boolean recordArrayOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recordArrayOperation'");
    }

    @Override
    public boolean recordAnyFunctionOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recordAnyFunctionOperation'");
    }

    @Override
    public boolean recordFunctionOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recordFunctionOperation'");
    }

    @Override
    public boolean recordRecordOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recordRecordOperation'");
    }

    @Override
    public boolean recordExtensibleRecordOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recordExtensibleRecordOperation'");
    }

    @Override
    public boolean extensibleRecordErrorOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'extensibleRecordErrorOperation'");
    }

    @Override
    public boolean extensibleRecordAnyItemOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'extensibleRecordAnyItemOperation'");
    }

    @Override
    public boolean extensibleRecordAnyNodeOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'extensibleRecordAnyNodeOperation'");
    }

    @Override
    public boolean extensibleRecordElementOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'extensibleRecordElementOperation'");
    }

    @Override
    public boolean extensibleRecordEnumOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'extensibleRecordEnumOperation'");
    }

    @Override
    public boolean extensibleRecordBooleanOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'extensibleRecordBooleanOperation'");
    }

    @Override
    public boolean extensibleRecordNumberOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'extensibleRecordNumberOperation'");
    }

    @Override
    public boolean extensibleRecordStringOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'extensibleRecordStringOperation'");
    }

    @Override
    public boolean extensibleRecordAnyMapOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'extensibleRecordAnyMapOperation'");
    }

    @Override
    public boolean extensibleRecordMapOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'extensibleRecordMapOperation'");
    }

    @Override
    public boolean extensibleRecordChoiceOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'extensibleRecordChoiceOperation'");
    }

    @Override
    public boolean extensibleRecordAnyArrayOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'extensibleRecordAnyArrayOperation'");
    }

    @Override
    public boolean extensibleRecordArrayOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'extensibleRecordArrayOperation'");
    }

    @Override
    public boolean extensibleRecordAnyFunctionOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'extensibleRecordAnyFunctionOperation'");
    }

    @Override
    public boolean extensibleRecordFunctionOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'extensibleRecordFunctionOperation'");
    }

    @Override
    public boolean extensibleRecordRecordOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'extensibleRecordRecordOperation'");
    }

    @Override
    public boolean extensibleRecordExtensibleRecordOperation(XQueryItemType x, XQueryItemType y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'extensibleRecordExtensibleRecordOperation'");
    }





}
