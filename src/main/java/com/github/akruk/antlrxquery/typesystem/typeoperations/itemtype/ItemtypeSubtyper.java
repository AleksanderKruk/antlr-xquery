
package com.github.akruk.antlrxquery.typesystem.typeoperations.itemtype;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import com.github.akruk.antlrxquery.typesystem.XQueryRecordField;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryTypes;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class ItemtypeSubtyper
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
    public ItemtypeSubtyper(final XQueryItemType x, final XQueryTypeFactory typeFactory)
    {
        final int typeOrdinal = x.type.ordinal();
        final Predicate<XQueryItemType> choicesubtype = (y) -> {
            final XQueryItemType y_ = (XQueryItemType) y;
            final var items = y_.getItemTypes();
            final boolean anyIsSubtype = items.stream().anyMatch(i-> x.itemtypeIsSubtypeOf(i));
            return anyIsSubtype;
        };

        // final Predicate<XQueryItemType> allchoicesSubtyped = (y) -> {
        //     final XQueryEnumItemType y_ = (XQueryEnumItemType) y;
        //     final var items = y_.getItemTypes();
        //     final boolean anyIsSubtype = items.stream().anyMatch(i-> x.itemtypeIsSubtypeOf(i));
        //     return anyIsSubtype;
        // };
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
                    final XQueryItemType y_ = (XQueryItemType) y;
                    return y_.elementNames.containsAll(x.elementNames);
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
                // itemtypeIsSubtypeOf[ANY_ARRAY] = (_) -> {
                //     final XQueryItemType x_ = (XQueryItemType) x;
                //     // map must have a key that is a number
                //     final var key = (XQueryItemType) x_.mapKeyType;
                //     return key.type == XQueryTypes.NUMBER;
                // };
                itemtypeIsSubtypeOf[MAP] = (y) -> {
                    final XQueryItemType y_ = (XQueryItemType) y;
                    return x.mapKeyType.itemtypeIsSubtypeOf(y_.mapKeyType)
                            && x.mapValueType.isSubtypeOf(y_.mapValueType);
                };
                itemtypeIsSubtypeOf[CHOICE] = choicesubtype;
                // itemtypeIsSubtypeOf[ARRAY] = (_) -> {
                //     // map must have a key that is a number
                //     final var key = (XQueryItemType) x.mapKeyType;
                //     return key.type == XQueryTypes.NUMBER;
                // };
                itemtypeIsSubtypeOf[ANY_FUNCTION] = alwaysTrue;
                itemtypeIsSubtypeOf[FUNCTION] = (y) -> {
                    final XQueryItemType x_ = (XQueryItemType) x;
                    final XQueryItemType y_ = (XQueryItemType) y;
                    if (y_.argumentTypes.size() != 1)
                        return false;
                    final var onlyArg =  (XQuerySequenceType) y_.argumentTypes.get(0);
                    final var onlyArgItem =  (XQueryItemType) onlyArg.itemType;
                    final boolean correctOccurence = onlyArg.isOne;
                    return correctOccurence
                            && x_.mapKeyType.itemtypeIsSubtypeOf(onlyArgItem);
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
                    final var argumentTypes = y.argumentTypes;
                    if (argumentTypes.size() != 1)
                        return false;
                    final var onlyArg =  (XQuerySequenceType) argumentTypes.get(0);
                    final var onlyArgItem =  (XQueryItemType) onlyArg.itemType;
                    final boolean correctOccurence = onlyArg.isOne || onlyArg.isOneOrMore;
                    return correctOccurence
                            && onlyArgItem.type == XQueryTypes.NUMBER;
                };
                itemtypeIsSubtypeOf[ANY_MAP] = alwaysTrue;
                itemtypeIsSubtypeOf[MAP] = (y) -> {
                    final boolean isNumber = y.mapKeyType.type == XQueryTypes.NUMBER;
                    return isNumber;
                };
                break;

            case ARRAY:
                Arrays.fill(itemtypeIsSubtypeOf, alwaysFalse);
                itemtypeIsSubtypeOf[ANY_ITEM] = alwaysTrue;
                itemtypeIsSubtypeOf[ANY_MAP] = alwaysTrue;
                itemtypeIsSubtypeOf[MAP] = (y) -> {
                    final XQueryItemType x_ = (XQueryItemType) x;
                    final XQueryItemType y_ = (XQueryItemType) y;
                    final var mapKeyType = (XQueryItemType) y_.mapKeyType;
                    final boolean isNumber = mapKeyType.type == XQueryTypes.NUMBER;
                    if (!isNumber)
                        return false;
                    return x_.getArrayMemberType().isSubtypeOf(y_.mapValueType);
                };
                itemtypeIsSubtypeOf[CHOICE] = choicesubtype;
                itemtypeIsSubtypeOf[ANY_ARRAY] = alwaysTrue;
                itemtypeIsSubtypeOf[ARRAY] = (y) -> {
                    final XQueryItemType x_ = (XQueryItemType) x;
                    final XQueryItemType y_ = (XQueryItemType) y;
                    final boolean isSubtypeOfAnyArray = itemtypeIsSubtypeOf[ANY_ARRAY].test(y);
                    if (!isSubtypeOfAnyArray)
                        return false;
                    final XQuerySequenceType xArrayItemType = x_.getArrayMemberType();
                    final XQuerySequenceType yArrayItemType = y_.getArrayMemberType();
                    return xArrayItemType.isSubtypeOf(yArrayItemType);
                };
                itemtypeIsSubtypeOf[ANY_FUNCTION] = alwaysTrue;
                itemtypeIsSubtypeOf[FUNCTION] = (y) -> {
                    final XQueryItemType x_ = (XQueryItemType) x;
                    final XQueryItemType y_ = (XQueryItemType) y;
                    if (y_.argumentTypes.size() != 1)
                        return false;
                    final var onlyArg =  (XQuerySequenceType) y_.argumentTypes.get(0);
                    final var onlyArgItem = (XQueryItemType) onlyArg.itemType;
                    if (onlyArgItem.type == XQueryTypes.NUMBER) {

                    }

                    return x_.getArrayMemberType().isSubtypeOf(y_.returnedType);
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
                // function must have one argument
                if (x_.argumentTypes.size() != 1)
                    return false;
                final var onlyArg = x.argumentTypes.get(0);
                final var onlyArgItem =  onlyArg.itemType;
                final boolean correctOccurence = onlyArg.isOne;
                return correctOccurence
                        && canBeKey[onlyArgItem.type.ordinal()];
            };

            itemtypeIsSubtypeOf[MAP] = (y) -> {
                if (!itemtypeIsSubtypeOf[ANY_MAP].test(y))
                    return false;
                final var onlyArg =  (XQuerySequenceType) x.argumentTypes.get(0);
                final var onlyArgItem =  (XQueryItemType) onlyArg.itemType;
                final boolean argCanBeKey = onlyArgItem.itemtypeIsSubtypeOf(y.mapKeyType);
                final boolean returnedCanBeValue = x.returnedType.isSubtypeOf(y.mapValueType);
                final boolean correctOccurence = onlyArg.isOne;
                return correctOccurence
                        && argCanBeKey
                        && returnedCanBeValue;
            };

            itemtypeIsSubtypeOf[CHOICE] = choicesubtype;
            itemtypeIsSubtypeOf[ANY_ARRAY] = (_) -> {
                // function must have one argument
                if (x_.argumentTypes.size() != 1)
                    return false;
                final var onlyArg =  (XQuerySequenceType) x.argumentTypes.get(0);
                final var onlyArgItem =  (XQueryItemType) onlyArg.itemType;
                // this one argument must be either number or number+
                final boolean correctOccurence = onlyArg.isOne || onlyArg.isOneOrMore;
                return correctOccurence
                        && onlyArgItem.type == XQueryTypes.NUMBER;
            };

            itemtypeIsSubtypeOf[ARRAY] = (y) -> {
                if (!itemtypeIsSubtypeOf[ANY_ARRAY].test(y))
                    return false;
                final var returnedType = x_.returnedType;

                return returnedType.isSubtypeOf(y_.getArrayMemberType());
            };
            itemtypeIsSubtypeOf[ANY_FUNCTION] = alwaysTrue;
            itemtypeIsSubtypeOf[FUNCTION] = (y) -> {
                final List<XQuerySequenceType> aArgs = x.argumentTypes;
                final List<XQuerySequenceType> bArgs = y.argumentTypes;
                final int aArgCount = aArgs.size();

                if (aArgCount > bArgs.size())
                    return false;
                for (int i = 0; i < aArgCount; i++) {
                    final var aArgType = aArgs.get(i);
                    final var bArgType = bArgs.get(i);
                    if (!bArgType.isSubtypeOf(aArgType))
                        return false;
                }
                return x.returnedType.isSubtypeOf(y.returnedType);
            };
            break;

            case ENUM:
            Arrays.fill(itemtypeIsSubtypeOf, alwaysFalse);
            itemtypeIsSubtypeOf[ANY_ITEM] = alwaysTrue;
            itemtypeIsSubtypeOf[ENUM] = (y) -> {
                return y.enumMembers.containsAll(x.enumMembers);
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
            itemtypeIsSubtypeOf[EXTENSIBLE_RECORD] = (y) -> {
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
                    // Every mandatory field in B is also declared as mandatory in A.
                    if (!areAllMandatoryFieldsPresent(x, y)) {
                        return false;
                    }
                    // For every field that is declared in both A and B, where the declared type in A is T
                    // and the declared type in B is U, T ⊑ U .
                    if  (!isEveryDeclaredFieldSubtype(x, y)) {
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
            final XQueryItemType x_ = (XQueryItemType) x;
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
                final XQueryItemType y_ = (XQueryItemType) y;
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



    /**
     * Checks whether or not type1 is subtype of type2
     * @param type1 the first item type
     * @param type2 the second item type
     * @return boolean predicate
     */
    public boolean itemtypeIsSubtypeOf(XQueryItemType type1, XQueryItemType type2) {
        return this.itemtypeIsSubtypeOf[type2.typeOrdinal].test(type2);
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
        final var x_ = (XQueryItemType) x;
        final var y_ = (XQueryItemType) y;
        final var yArgumentTypes = y_.argumentTypes;
        if (yArgumentTypes.size() != 1)
            return false;
        final var yFieldType = (XQuerySequenceType) yArgumentTypes.get(0);
        final XQueryItemType yFieldItemType = (XQueryItemType) yFieldType.itemType;
        if (yFieldItemType.type != XQueryTypes.STRING
            && yFieldItemType.type != XQueryTypes.ANY_ITEM)
            return false;
        final var yReturnedType = y_.returnedType;
        for (final var key : x_.getRecordFields().keySet()) {
            final var xFieldType = x_.getRecordFields().get(key);
            if (!xFieldType.type().isSubtypeOf(yReturnedType))
                return false;
        }
        return true;
    }

    private static boolean recordIsSubtypeOfMap(Object x, Object y) {
        final var x_ = (XQueryItemType) x;
        final var y_ = (XQueryItemType) y;
        final XQueryItemType keyItemType = (XQueryItemType) y_.mapKeyType;
        if (keyItemType.type != XQueryTypes.STRING
            && keyItemType.type != XQueryTypes.ANY_ITEM)
            return false;
        final var yFieldType = y_.mapValueType;
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
