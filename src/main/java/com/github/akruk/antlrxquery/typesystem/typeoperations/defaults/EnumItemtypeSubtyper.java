
package com.github.akruk.antlrxquery.typesystem.typeoperations.defaults;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import com.github.akruk.antlrxquery.typesystem.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.XQueryRecordField;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryEnumItemType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryEnumItemTypeArray;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryEnumItemTypeEnum;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryEnumItemTypeFunction;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryEnumItemTypeMap;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryEnumItemTypeRecord;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryEnumSequenceType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryTypes;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class EnumItemtypeSubtyper
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

    private final Predicate<XQueryItemType>[] itemtypeIsSubtypeOf;

    @SuppressWarnings("unchecked")
    public EnumItemtypeSubtyper(final XQueryEnumItemType x, final XQueryTypeFactory typeFactory)
    {
        final int typeOrdinal = x.getType().ordinal();
        final Predicate<XQueryItemType> choicesubtype = (y) -> {
            final XQueryEnumItemType y_ = (XQueryEnumItemType) y;
            final var items = y_.getItemTypes();
            final boolean anyIsSubtype = items.stream().anyMatch(i-> x.itemtypeIsSubtypeOf(i));
            return anyIsSubtype;
        };

        final Predicate<XQueryItemType> allchoicesSubtyped = (y) -> {
            final XQueryEnumItemType y_ = (XQueryEnumItemType) y;
            final var items = y_.getItemTypes();
            final boolean anyIsSubtype = items.stream().anyMatch(i-> x.itemtypeIsSubtypeOf(i));
            return anyIsSubtype;
        };
        itemtypeIsSubtypeOf = new Predicate[XQueryTypes.values().length];
        switch (XQueryTypes.values()[typeOrdinal]) {
            case ERROR:
                Arrays.fill(itemtypeIsSubtypeOf, alwaysFalse);
                itemtypeIsSubtypeOf[ERROR] = alwaysTrue;
                itemtypeIsSubtypeOf[ANY_ITEM] = alwaysTrue;
                break;

            case ANY_ITEM:
                Arrays.fill(itemtypeIsSubtypeOf, alwaysFalse);
                itemtypeIsSubtypeOf[ANY_ITEM] = alwaysTrue;
                break;

            case ANY_NODE:
                Arrays.fill(itemtypeIsSubtypeOf, alwaysFalse);
                itemtypeIsSubtypeOf[ANY_ITEM] = alwaysTrue;
                itemtypeIsSubtypeOf[ANY_NODE] = alwaysTrue;
                itemtypeIsSubtypeOf[CHOICE] = choicesubtype;
                break;

            case ELEMENT:
                Arrays.fill(itemtypeIsSubtypeOf, alwaysFalse);
                itemtypeIsSubtypeOf[ANY_ITEM] = alwaysTrue;
                itemtypeIsSubtypeOf[ANY_NODE] = alwaysTrue;
                itemtypeIsSubtypeOf[ELEMENT] = (y) -> {
                    final XQueryEnumItemType y_ = (XQueryEnumItemType) y;
                    return y_.getElementNames().containsAll(x.getElementNames());
                };
                itemtypeIsSubtypeOf[CHOICE] = choicesubtype;
                break;

            case ANY_MAP:
                Arrays.fill(itemtypeIsSubtypeOf, alwaysFalse);
                itemtypeIsSubtypeOf[ANY_ITEM] = alwaysTrue;
                itemtypeIsSubtypeOf[ANY_MAP] = alwaysTrue;
                // TODO: refine
                // itemtypeIsSubtypeOf[MAP] = alwaysFalse;
                itemtypeIsSubtypeOf[CHOICE] = choicesubtype;
                itemtypeIsSubtypeOf[ANY_FUNCTION] = alwaysTrue;
                // TODO: refine
                // itemtypeIsSubtypeOf[FUNCTION] = simpleChoice;
                break;

            case MAP:
                Arrays.fill(itemtypeIsSubtypeOf, alwaysFalse);

                itemtypeIsSubtypeOf[ANY_ITEM] = alwaysTrue;
                itemtypeIsSubtypeOf[ANY_MAP] = alwaysTrue;
                itemtypeIsSubtypeOf[ANY_ARRAY] = (_) -> {
                    final XQueryEnumItemType x_ = (XQueryEnumItemType) x;
                    // map must have a key that is a number
                    final var key = (XQueryEnumItemType) x_.getMapKeyType();
                    return key.getType() == XQueryTypes.NUMBER;
                };
                itemtypeIsSubtypeOf[MAP] = (y) -> {
                    final XQueryEnumItemType y_ = (XQueryEnumItemType) y;
                    return x.getMapKeyType().itemtypeIsSubtypeOf(y_.getMapKeyType())
                            && x.getMapValueType().isSubtypeOf(y_.getMapValueType());
                };
                itemtypeIsSubtypeOf[CHOICE] = choicesubtype;
                itemtypeIsSubtypeOf[ARRAY] = (_) -> {
                    // map must have a key that is a number
                    final var key = (XQueryEnumItemType) x.getMapKeyType();
                    return key.getType() == XQueryTypes.NUMBER;
                };
                itemtypeIsSubtypeOf[ANY_FUNCTION] = alwaysTrue;
                itemtypeIsSubtypeOf[FUNCTION] = (y) -> {
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
                break;

            case ANY_ARRAY:
                Arrays.fill(itemtypeIsSubtypeOf, alwaysFalse);


                itemtypeIsSubtypeOf[ANY_ITEM] = alwaysTrue;
                itemtypeIsSubtypeOf[ANY_ARRAY] = alwaysTrue;
                itemtypeIsSubtypeOf[CHOICE] = choicesubtype;
                itemtypeIsSubtypeOf[ARRAY] = (y) -> {
                    final XQuerySequenceType zeroOrMoreItems = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
                    return y.getArrayMemberType().equals(zeroOrMoreItems);
                };
                itemtypeIsSubtypeOf[ANY_FUNCTION] = alwaysTrue;
                itemtypeIsSubtypeOf[FUNCTION] = (y) -> {
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
                itemtypeIsSubtypeOf[ANY_MAP] = alwaysTrue;
                itemtypeIsSubtypeOf[MAP] = (y) -> {
                    final XQueryEnumItemTypeMap y_ = (XQueryEnumItemTypeMap) y;
                    final var mapKeyType = (XQueryEnumItemType) y_.getMapKeyType();
                    final boolean isNumber = mapKeyType.getType() == XQueryTypes.NUMBER;
                    return isNumber;
                };
                break;

            case ARRAY:
                Arrays.fill(itemtypeIsSubtypeOf, alwaysFalse);
                itemtypeIsSubtypeOf[ANY_ITEM] = alwaysTrue;
                itemtypeIsSubtypeOf[ANY_MAP] = alwaysTrue;
                itemtypeIsSubtypeOf[MAP] = (y) -> {
                    final XQueryEnumItemType x_ = (XQueryEnumItemType) x;
                    final XQueryEnumItemType y_ = (XQueryEnumItemType) y;
                    final var mapKeyType = (XQueryEnumItemType) y_.getMapKeyType();
                    final boolean isNumber = mapKeyType.getType() == XQueryTypes.NUMBER;
                    if (!isNumber)
                        return false;
                    return x_.getArrayMemberType().isSubtypeOf(y_.getMapValueType());
                };
                itemtypeIsSubtypeOf[CHOICE] = choicesubtype;
                itemtypeIsSubtypeOf[ANY_ARRAY] = alwaysTrue;
                itemtypeIsSubtypeOf[ARRAY] = (y) -> {
                    final XQueryEnumItemType x_ = (XQueryEnumItemType) x;
                    final XQueryEnumItemType y_ = (XQueryEnumItemType) y;
                    final boolean isSubtypeOfAnyArray = itemtypeIsSubtypeOf[ANY_ARRAY].test(y);
                    if (!isSubtypeOfAnyArray)
                        return false;
                    final XQuerySequenceType xArrayItemType = x_.getArrayMemberType();
                    final XQuerySequenceType yArrayItemType = y_.getArrayMemberType();
                    return xArrayItemType.isSubtypeOf(yArrayItemType);
                };
                itemtypeIsSubtypeOf[ANY_FUNCTION] = alwaysTrue;
                itemtypeIsSubtypeOf[FUNCTION] = (y) -> {
                    final XQueryEnumItemType x_ = (XQueryEnumItemType) x;
                    final XQueryEnumItemType y_ = (XQueryEnumItemType) y;
                    if (y_.getArgumentTypes().size() != 1)
                        return false;
                    final var onlyArg =  (XQueryEnumSequenceType) y_.getArgumentTypes().get(0);
                    final var onlyArgItem = (XQueryEnumItemType) onlyArg.getItemType();
                    if (onlyArgItem.getType() == XQueryTypes.NUMBER) {

                    }

                    return x_.getArrayMemberType().isSubtypeOf(y_.getReturnedType());
                };
                break;

            case ANY_FUNCTION:
                Arrays.fill(itemtypeIsSubtypeOf, alwaysFalse);
                itemtypeIsSubtypeOf[ANY_ITEM] = alwaysTrue;
                itemtypeIsSubtypeOf[CHOICE] = choicesubtype;
                itemtypeIsSubtypeOf[ANY_FUNCTION] = alwaysTrue;
                break;

            case FUNCTION:
            Arrays.fill(itemtypeIsSubtypeOf, alwaysFalse);
            final var canBeKey = booleanEnumArray(XQueryTypes.NUMBER, XQueryTypes.BOOLEAN, XQueryTypes.STRING, XQueryTypes.ENUM);

            itemtypeIsSubtypeOf[ANY_ITEM] = alwaysTrue;
            itemtypeIsSubtypeOf[ANY_MAP] = (_) -> {
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

            itemtypeIsSubtypeOf[MAP] = (y) -> {
                if (!itemtypeIsSubtypeOf[ANY_MAP].test(y))
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

            itemtypeIsSubtypeOf[CHOICE] = choicesubtype;
            itemtypeIsSubtypeOf[ANY_ARRAY] = (_) -> {
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

            itemtypeIsSubtypeOf[ARRAY] = (y) -> {
                if (!itemtypeIsSubtypeOf[ANY_ARRAY].test(y))
                    return false;
                final XQueryEnumItemTypeFunction x_ = (XQueryEnumItemTypeFunction) x;
                final XQueryEnumItemTypeArray y_ = (XQueryEnumItemTypeArray) y;
                final var returnedType = x_.getReturnedType();

                return returnedType.isSubtypeOf(y_.getArrayMemberType());
            };
            itemtypeIsSubtypeOf[ANY_FUNCTION] = alwaysTrue;
            itemtypeIsSubtypeOf[FUNCTION] = (y) -> {
                final XQueryEnumItemTypeFunction a = (XQueryEnumItemTypeFunction) x;
                final XQueryEnumItemTypeFunction b = (XQueryEnumItemTypeFunction) y;
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
            break;

            case ENUM:
            Arrays.fill(itemtypeIsSubtypeOf, alwaysFalse);
            itemtypeIsSubtypeOf[ANY_ITEM] = alwaysTrue;
            itemtypeIsSubtypeOf[ENUM] = (y) -> {
                final var x_ = (XQueryEnumItemTypeEnum) x;
                final var y_ = (XQueryEnumItemTypeEnum) y;
                return y_.getEnumMembers().containsAll(x_.getEnumMembers());
            };
            itemtypeIsSubtypeOf[STRING] = alwaysTrue;
            itemtypeIsSubtypeOf[CHOICE] = choicesubtype;

            break;

            case RECORD:
            Arrays.fill(itemtypeIsSubtypeOf, alwaysFalse);
            itemtypeIsSubtypeOf[ANY_FUNCTION] = alwaysTrue;
            itemtypeIsSubtypeOf[MAP] = (y) -> recordIsSubtypeOfMap(x, y);
            itemtypeIsSubtypeOf[ANY_MAP] = alwaysTrue;
            itemtypeIsSubtypeOf[ANY_FUNCTION] = alwaysTrue;
            itemtypeIsSubtypeOf[FUNCTION] = (y) -> recordIsSubtypeOfFunction(x, y);
            itemtypeIsSubtypeOf[ANY_ITEM] = alwaysTrue;
            itemtypeIsSubtypeOf[CHOICE] = choicesubtype;
            itemtypeIsSubtypeOf[RECORD] = (y) -> {
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
            itemtypeIsSubtypeOf[EXTENSIBLE_RECORD] = (y) -> {
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
            break;

            case EXTENSIBLE_RECORD:
                Arrays.fill(itemtypeIsSubtypeOf, alwaysFalse);
                itemtypeIsSubtypeOf[ANY_ITEM] = alwaysTrue;
                itemtypeIsSubtypeOf[ANY_MAP] = alwaysTrue;
                // itemtypeIsSubtypeOf[MAP] = simpleChoice;
                itemtypeIsSubtypeOf[CHOICE] = choicesubtype;
                // itemtypeIsSubtypeOf[ANY_ARRAY] = simpleChoice;
                // itemtypeIsSubtypeOf[ARRAY] = simpleChoice;
                itemtypeIsSubtypeOf[ANY_FUNCTION] = alwaysTrue;
                itemtypeIsSubtypeOf[FUNCTION] = y -> recordIsSubtypeOfFunction(x, y);
                // itemtypeIsSubtypeOf[RECORD] = extensibleRecordMerger;
                itemtypeIsSubtypeOf[EXTENSIBLE_RECORD] = y -> {
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

            break;

            case BOOLEAN:
            Arrays.fill(itemtypeIsSubtypeOf, alwaysFalse);
            itemtypeIsSubtypeOf[BOOLEAN] = alwaysTrue;
            itemtypeIsSubtypeOf[ANY_ITEM] = alwaysTrue;
            itemtypeIsSubtypeOf[CHOICE] = choicesubtype;
            break;

            case STRING:
            Arrays.fill(itemtypeIsSubtypeOf, alwaysFalse);
            itemtypeIsSubtypeOf[STRING] = alwaysTrue;
            itemtypeIsSubtypeOf[ANY_ITEM] = alwaysTrue;
            itemtypeIsSubtypeOf[CHOICE] = choicesubtype;
            break;

            case NUMBER:
            Arrays.fill(itemtypeIsSubtypeOf, alwaysFalse);
            itemtypeIsSubtypeOf[NUMBER] = alwaysTrue;
            itemtypeIsSubtypeOf[ANY_ITEM] = alwaysTrue;
            itemtypeIsSubtypeOf[CHOICE] = choicesubtype;
            break;

            case CHOICE:
            Arrays.fill(itemtypeIsSubtypeOf, alwaysFalse);
            final XQueryEnumItemType x_ = (XQueryEnumItemType) x;
            final Predicate<XQueryItemType> lchoice = (y) -> {
                final var items = x_.getItemTypes();
                final boolean anyIsSubtype = items.stream().allMatch(i-> i.itemtypeIsSubtypeOf(y));
                return anyIsSubtype;
            };


            itemtypeIsSubtypeOf[ANY_ITEM] = alwaysTrue;
            itemtypeIsSubtypeOf[ANY_NODE] = lchoice;
            itemtypeIsSubtypeOf[ELEMENT] = lchoice;
            itemtypeIsSubtypeOf[ENUM] = lchoice;
            itemtypeIsSubtypeOf[BOOLEAN] = lchoice;
            itemtypeIsSubtypeOf[NUMBER] = lchoice;
            itemtypeIsSubtypeOf[STRING] = lchoice;
            itemtypeIsSubtypeOf[ANY_MAP] = lchoice;
            itemtypeIsSubtypeOf[MAP] = lchoice;
            itemtypeIsSubtypeOf[CHOICE] = (y) -> {
                final XQueryEnumItemType y_ = (XQueryEnumItemType) y;
                final var xItems = x_.getItemTypes();
                final var yItems = y_.getItemTypes();
                final var allPresent = xItems.stream().allMatch(xItem->{
                    final boolean anyIsSubtype = yItems.stream().anyMatch(i-> xItem.itemtypeIsSubtypeOf(i));
                    return anyIsSubtype;
                });
                return allPresent;
            };
            itemtypeIsSubtypeOf[ANY_ARRAY] = lchoice;
            itemtypeIsSubtypeOf[ARRAY] = lchoice;
            itemtypeIsSubtypeOf[ANY_FUNCTION] = lchoice;
            itemtypeIsSubtypeOf[FUNCTION] = lchoice;
            itemtypeIsSubtypeOf[RECORD] = lchoice;
            itemtypeIsSubtypeOf[EXTENSIBLE_RECORD] = lchoice;
            break;

            default:
                throw new AssertionError("Unexpected type ordinal: " + typeOrdinal + ":" + XQueryTypes.values()[typeOrdinal]);
        }

    }

    private static final Predicate<XQueryItemType> alwaysTrue = (_) -> true;
    private static final Predicate<XQueryItemType> alwaysFalse = (_) -> false;



    public boolean itemtypeIsSubtypeOf(XQueryItemType type1, XQueryItemType type2) {
        final int otherOrdinal = ((XQueryEnumItemType) type2).getType().ordinal();
        return this.itemtypeIsSubtypeOf[otherOrdinal].test(type2);
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
        final XQueryEnumItemType yFieldItemType = (XQueryEnumItemType) yFieldType.getItemType();
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
        final XQueryEnumItemType keyItemType = (XQueryEnumItemType) y_.getMapKeyType();
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
