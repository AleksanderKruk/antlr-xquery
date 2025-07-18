
package com.github.akruk.antlrxquery.typesystem.typeoperations.defaults;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import com.github.akruk.antlrxquery.typesystem.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.XQueryRecordField;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryEnumChoiceItemType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryEnumItemType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryEnumItemTypeArray;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryEnumItemTypeElement;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryEnumItemTypeEnum;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryEnumItemTypeMap;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryEnumItemTypeRecord;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryTypes;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.typeoperations.IItemtypeAlternativeMerger;

public class EnumItemtypeAlternativeMerger
  implements IItemtypeAlternativeMerger
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
    private static final int typesCount = XQueryTypes.values().length;

    private final BinaryOperator<XQueryItemType>[] alternativeItemMerger;

    @SuppressWarnings("unchecked")
    public EnumItemtypeAlternativeMerger(final int typeOrdinal, final XQueryTypeFactory typeFactory)
    {
        this.alternativeItemMerger = new BinaryOperator[typesCount];
        final BinaryOperator<XQueryItemType> error = (_, _) -> typeFactory.itemError();
        final BinaryOperator<XQueryItemType> anyItem = (_, _) -> typeFactory.itemAnyItem();
        final BinaryOperator<XQueryItemType> anyNode = (_, _) -> typeFactory.itemAnyNode();
        final BinaryOperator<XQueryItemType> anyMap = (_, _) -> typeFactory.itemAnyMap();
        final BinaryOperator<XQueryItemType> anyArray = (_, _) -> typeFactory.itemAnyArray();
        final BinaryOperator<XQueryItemType> itemString = (_, _) -> typeFactory.itemString();
        final BinaryOperator<XQueryItemType> simpleChoice = (x, y) -> typeFactory.itemChoice(Set.of(x, y));
        final BinaryOperator<XQueryItemType> leftMergeChoice = (x, y) -> leftChoiceMerge(typeFactory, x, y);
        final BinaryOperator<XQueryItemType> rightMergeChoice = (x, y) -> rightChoiceMerge(typeFactory, x, y);
        final BinaryOperator<XQueryItemType> choiceMerging = (x, y) -> mergeChoices(typeFactory, x, y);
        final BinaryOperator<XQueryItemType> mapMerging = (x, y) -> mergeMaps(typeFactory, x, y);
        final BinaryOperator<XQueryItemType> elementSequenceMerger = (x, y) -> mergeElements(typeFactory, x, y);
        final BinaryOperator<XQueryItemType> enumMerging = enumMerger(typeFactory);
        final BinaryOperator<XQueryItemType> extensibleRecordMerger = extensibleRecordMerger(typeFactory);

        // Fully expanded automaton for alternativeItemMerger initialization
        final BinaryOperator<XQueryItemType> arrayMerging = (x, y) -> mergeArrays(typeFactory, x, y);
        final BinaryOperator<XQueryItemType> recordMerging = recordMerger(typeFactory);
        switch (XQueryTypes.values()[typeOrdinal]) {
            case ERROR:
            alternativeItemMerger[ERROR] = error;
            alternativeItemMerger[ANY_ITEM] = error;
            alternativeItemMerger[ANY_NODE] = error;
            alternativeItemMerger[ELEMENT] = error;
            alternativeItemMerger[ENUM] = error;
            alternativeItemMerger[BOOLEAN] = error;
            alternativeItemMerger[NUMBER] = error;
            alternativeItemMerger[STRING] = error;
            alternativeItemMerger[ANY_MAP] = error;
            alternativeItemMerger[MAP] = error;
            alternativeItemMerger[CHOICE] = error;
            alternativeItemMerger[ANY_ARRAY] = error;
            alternativeItemMerger[ARRAY] = error;
            alternativeItemMerger[ANY_FUNCTION] = error;
            alternativeItemMerger[FUNCTION] = error;
            alternativeItemMerger[RECORD] = error;
            alternativeItemMerger[EXTENSIBLE_RECORD] = error;
            break;

            case ANY_ITEM:
            alternativeItemMerger[ERROR] = error;
            alternativeItemMerger[ANY_ITEM] = anyItem;
            alternativeItemMerger[ANY_NODE] = anyItem;
            alternativeItemMerger[ELEMENT] = anyItem;
            alternativeItemMerger[ENUM] = anyItem;
            alternativeItemMerger[BOOLEAN] = anyItem;
            alternativeItemMerger[NUMBER] = anyItem;
            alternativeItemMerger[STRING] = anyItem;
            alternativeItemMerger[ANY_MAP] = anyItem;
            alternativeItemMerger[MAP] = anyItem;
            alternativeItemMerger[CHOICE] = anyItem;
            alternativeItemMerger[ANY_ARRAY] = anyItem;
            alternativeItemMerger[ARRAY] = anyItem;
            alternativeItemMerger[ANY_FUNCTION] = anyItem;
            alternativeItemMerger[FUNCTION] = anyItem;
            alternativeItemMerger[RECORD] = anyItem;
            alternativeItemMerger[EXTENSIBLE_RECORD] = anyItem;
            break;

            case ANY_NODE:
            alternativeItemMerger[ERROR] = error;
            alternativeItemMerger[ANY_ITEM] = anyItem;
            alternativeItemMerger[ANY_NODE] = anyNode;
            alternativeItemMerger[ELEMENT] = anyNode;
            alternativeItemMerger[ENUM] = simpleChoice;
            alternativeItemMerger[BOOLEAN] = simpleChoice;
            alternativeItemMerger[NUMBER] = simpleChoice;
            alternativeItemMerger[STRING] = simpleChoice;
            alternativeItemMerger[ANY_MAP] = simpleChoice;
            alternativeItemMerger[MAP] = simpleChoice;
            alternativeItemMerger[CHOICE] = rightMergeChoice;
            alternativeItemMerger[ANY_ARRAY] = simpleChoice;
            alternativeItemMerger[ARRAY] = simpleChoice;
            alternativeItemMerger[ANY_FUNCTION] = simpleChoice;
            alternativeItemMerger[FUNCTION] = simpleChoice;
            alternativeItemMerger[RECORD] = simpleChoice;
            alternativeItemMerger[EXTENSIBLE_RECORD] = simpleChoice;
            break;

            case ELEMENT:
            alternativeItemMerger[ERROR] = error;
            alternativeItemMerger[ANY_ITEM] = anyItem;
            alternativeItemMerger[ANY_NODE] = anyNode;
            alternativeItemMerger[ELEMENT] = elementSequenceMerger;
            alternativeItemMerger[ENUM] = simpleChoice;
            alternativeItemMerger[BOOLEAN] = simpleChoice;
            alternativeItemMerger[NUMBER] = simpleChoice;
            alternativeItemMerger[STRING] = simpleChoice;
            alternativeItemMerger[ANY_MAP] = simpleChoice;
            alternativeItemMerger[MAP] = simpleChoice;
            alternativeItemMerger[CHOICE] = rightMergeChoice;
            alternativeItemMerger[ANY_ARRAY] = simpleChoice;
            alternativeItemMerger[ARRAY] = simpleChoice;
            alternativeItemMerger[ANY_FUNCTION] = simpleChoice;
            alternativeItemMerger[FUNCTION] = simpleChoice;
            alternativeItemMerger[RECORD] = simpleChoice;
            alternativeItemMerger[EXTENSIBLE_RECORD] = simpleChoice;
            break;

            case ANY_MAP:
            alternativeItemMerger[ERROR] = error;
            alternativeItemMerger[ANY_ITEM] = anyItem;
            alternativeItemMerger[ANY_NODE] = simpleChoice;
            alternativeItemMerger[ELEMENT] = simpleChoice;
            alternativeItemMerger[ENUM] = simpleChoice;
            alternativeItemMerger[BOOLEAN] = simpleChoice;
            alternativeItemMerger[NUMBER] = simpleChoice;
            alternativeItemMerger[STRING] = simpleChoice;
            alternativeItemMerger[ANY_MAP] = anyMap;
            alternativeItemMerger[MAP] = anyMap;
            alternativeItemMerger[CHOICE] = rightMergeChoice;
            alternativeItemMerger[ANY_ARRAY] = simpleChoice;
            alternativeItemMerger[ARRAY] = simpleChoice;
            alternativeItemMerger[ANY_FUNCTION] = simpleChoice;
            alternativeItemMerger[FUNCTION] = simpleChoice;
            alternativeItemMerger[RECORD] = simpleChoice;
            alternativeItemMerger[EXTENSIBLE_RECORD] = simpleChoice;
            break;

            case MAP:
            alternativeItemMerger[ERROR] = error;
            alternativeItemMerger[ANY_ITEM] = anyItem;
            alternativeItemMerger[ANY_NODE] = simpleChoice;
            alternativeItemMerger[ELEMENT] = simpleChoice;
            alternativeItemMerger[ENUM] = simpleChoice;
            alternativeItemMerger[BOOLEAN] = simpleChoice;
            alternativeItemMerger[NUMBER] = simpleChoice;
            alternativeItemMerger[STRING] = simpleChoice;
            alternativeItemMerger[ANY_MAP] = anyMap;
            alternativeItemMerger[MAP] = mapMerging;
            alternativeItemMerger[CHOICE] = rightMergeChoice;
            alternativeItemMerger[ANY_ARRAY] = simpleChoice;
            alternativeItemMerger[ARRAY] = simpleChoice;
            alternativeItemMerger[ANY_FUNCTION] = simpleChoice;
            alternativeItemMerger[FUNCTION] = simpleChoice;
            alternativeItemMerger[RECORD] = simpleChoice;
            alternativeItemMerger[EXTENSIBLE_RECORD] = simpleChoice;
            break;

            case ANY_ARRAY:
            alternativeItemMerger[ERROR] = error;
            alternativeItemMerger[ANY_ITEM] = anyItem;
            alternativeItemMerger[ANY_NODE] = simpleChoice;
            alternativeItemMerger[ELEMENT] = simpleChoice;
            alternativeItemMerger[ENUM] = simpleChoice;
            alternativeItemMerger[BOOLEAN] = simpleChoice;
            alternativeItemMerger[NUMBER] = simpleChoice;
            alternativeItemMerger[STRING] = simpleChoice;
            alternativeItemMerger[ANY_MAP] = simpleChoice;
            alternativeItemMerger[MAP] = simpleChoice;
            alternativeItemMerger[CHOICE] = rightMergeChoice;
            alternativeItemMerger[ANY_ARRAY] = anyArray;
            alternativeItemMerger[ARRAY] = anyArray;
            alternativeItemMerger[ANY_FUNCTION] = simpleChoice;
            alternativeItemMerger[FUNCTION] = simpleChoice;
            alternativeItemMerger[RECORD] = simpleChoice;
            alternativeItemMerger[EXTENSIBLE_RECORD] = simpleChoice;
            break;

            case ARRAY:
            alternativeItemMerger[ERROR] = error;
            alternativeItemMerger[ANY_ITEM] = anyItem;
            alternativeItemMerger[ANY_NODE] = simpleChoice;
            alternativeItemMerger[ELEMENT] = simpleChoice;
            alternativeItemMerger[ENUM] = simpleChoice;
            alternativeItemMerger[BOOLEAN] = simpleChoice;
            alternativeItemMerger[NUMBER] = simpleChoice;
            alternativeItemMerger[STRING] = simpleChoice;
            alternativeItemMerger[ANY_MAP] = simpleChoice;
            alternativeItemMerger[MAP] = simpleChoice;
            alternativeItemMerger[CHOICE] = rightMergeChoice;
            alternativeItemMerger[ANY_ARRAY] = anyArray;
            alternativeItemMerger[ARRAY] = arrayMerging;
            alternativeItemMerger[ANY_FUNCTION] = simpleChoice;
            alternativeItemMerger[FUNCTION] = simpleChoice;
            alternativeItemMerger[RECORD] = simpleChoice;
            alternativeItemMerger[EXTENSIBLE_RECORD] = simpleChoice;
            break;

            case ANY_FUNCTION:
            alternativeItemMerger[ERROR] = error;
            alternativeItemMerger[ANY_ITEM] = anyItem;
            alternativeItemMerger[ANY_NODE] = simpleChoice;
            alternativeItemMerger[ELEMENT] = simpleChoice;
            alternativeItemMerger[ENUM] = simpleChoice;
            alternativeItemMerger[BOOLEAN] = simpleChoice;
            alternativeItemMerger[NUMBER] = simpleChoice;
            alternativeItemMerger[STRING] = simpleChoice;
            alternativeItemMerger[ANY_MAP] = simpleChoice;
            alternativeItemMerger[MAP] = simpleChoice;
            alternativeItemMerger[CHOICE] = rightMergeChoice;
            alternativeItemMerger[ANY_ARRAY] = simpleChoice;
            alternativeItemMerger[ARRAY] = simpleChoice;
            alternativeItemMerger[ANY_FUNCTION] = (_, _) -> typeFactory.itemAnyFunction();
            alternativeItemMerger[FUNCTION] = simpleChoice;
            alternativeItemMerger[RECORD] = simpleChoice;
            alternativeItemMerger[EXTENSIBLE_RECORD] = simpleChoice;
            break;

            case FUNCTION:
            alternativeItemMerger[ERROR] = error;
            alternativeItemMerger[ANY_ITEM] = anyItem;
            alternativeItemMerger[ANY_NODE] = simpleChoice;
            alternativeItemMerger[ELEMENT] = simpleChoice;
            alternativeItemMerger[ENUM] = simpleChoice;
            alternativeItemMerger[BOOLEAN] = simpleChoice;
            alternativeItemMerger[NUMBER] = simpleChoice;
            alternativeItemMerger[STRING] = simpleChoice;
            alternativeItemMerger[ANY_MAP] = simpleChoice;
            alternativeItemMerger[MAP] = simpleChoice;
            alternativeItemMerger[CHOICE] = rightMergeChoice;
            alternativeItemMerger[ANY_ARRAY] = simpleChoice;
            alternativeItemMerger[ARRAY] = simpleChoice;
            alternativeItemMerger[ANY_FUNCTION] = simpleChoice;
            alternativeItemMerger[FUNCTION] = simpleChoice;
            alternativeItemMerger[RECORD] = simpleChoice;
            alternativeItemMerger[EXTENSIBLE_RECORD] = simpleChoice;
            break;

            case ENUM:
            alternativeItemMerger[ERROR] = error;
            alternativeItemMerger[ANY_ITEM] = anyItem;
            alternativeItemMerger[ANY_NODE] = simpleChoice;
            alternativeItemMerger[ELEMENT] = simpleChoice;
            alternativeItemMerger[ENUM] = enumMerging;
            alternativeItemMerger[BOOLEAN] = simpleChoice;
            alternativeItemMerger[NUMBER] = simpleChoice;
            alternativeItemMerger[STRING] = itemString;
            alternativeItemMerger[ANY_MAP] = simpleChoice;
            alternativeItemMerger[MAP] = simpleChoice;
            alternativeItemMerger[CHOICE] = rightMergeChoice;
            alternativeItemMerger[ANY_ARRAY] = simpleChoice;
            alternativeItemMerger[ARRAY] = simpleChoice;
            alternativeItemMerger[ANY_FUNCTION] = simpleChoice;
            alternativeItemMerger[FUNCTION] = simpleChoice;
            alternativeItemMerger[RECORD] = simpleChoice;
            alternativeItemMerger[EXTENSIBLE_RECORD] = simpleChoice;
            break;

            case RECORD:
            alternativeItemMerger[ERROR] = error;
            alternativeItemMerger[ANY_ITEM] = anyItem;
            alternativeItemMerger[ANY_NODE] = simpleChoice;
            alternativeItemMerger[ELEMENT] = simpleChoice;
            alternativeItemMerger[ENUM] = simpleChoice;
            alternativeItemMerger[BOOLEAN] = simpleChoice;
            alternativeItemMerger[NUMBER] = simpleChoice;
            alternativeItemMerger[STRING] = simpleChoice;
            alternativeItemMerger[ANY_MAP] = simpleChoice;
            alternativeItemMerger[MAP] = simpleChoice;
            alternativeItemMerger[CHOICE] = rightMergeChoice;
            alternativeItemMerger[ANY_ARRAY] = simpleChoice;
            alternativeItemMerger[ARRAY] = simpleChoice;
            alternativeItemMerger[ANY_FUNCTION] = simpleChoice;
            alternativeItemMerger[FUNCTION] = simpleChoice;
            alternativeItemMerger[RECORD] = recordMerging;
            alternativeItemMerger[EXTENSIBLE_RECORD] = extensibleRecordMerger;
            break;

            case EXTENSIBLE_RECORD:
            alternativeItemMerger[ERROR] = error;
            alternativeItemMerger[ANY_ITEM] = anyItem;
            alternativeItemMerger[ANY_NODE] = simpleChoice;
            alternativeItemMerger[ELEMENT] = simpleChoice;
            alternativeItemMerger[ENUM] = simpleChoice;
            alternativeItemMerger[BOOLEAN] = simpleChoice;
            alternativeItemMerger[NUMBER] = simpleChoice;
            alternativeItemMerger[STRING] = simpleChoice;
            alternativeItemMerger[ANY_MAP] = simpleChoice;
            alternativeItemMerger[MAP] = simpleChoice;
            alternativeItemMerger[CHOICE] = rightMergeChoice;
            alternativeItemMerger[ANY_ARRAY] = simpleChoice;
            alternativeItemMerger[ARRAY] = simpleChoice;
            alternativeItemMerger[ANY_FUNCTION] = simpleChoice;
            alternativeItemMerger[FUNCTION] = simpleChoice;
            alternativeItemMerger[RECORD] = extensibleRecordMerger;
            alternativeItemMerger[EXTENSIBLE_RECORD] = extensibleRecordMerger;
            break;

            case BOOLEAN:
            alternativeItemMerger[ERROR] = error;
            alternativeItemMerger[ANY_ITEM] = anyItem;
            alternativeItemMerger[ANY_NODE] = simpleChoice;
            alternativeItemMerger[ELEMENT] = simpleChoice;
            alternativeItemMerger[ENUM] = simpleChoice;
            alternativeItemMerger[BOOLEAN] = (_, _) -> typeFactory.itemBoolean();
            alternativeItemMerger[NUMBER] = simpleChoice;
            alternativeItemMerger[STRING] = simpleChoice;
            alternativeItemMerger[ANY_MAP] = simpleChoice;
            alternativeItemMerger[MAP] = simpleChoice;
            alternativeItemMerger[CHOICE] = rightMergeChoice;
            alternativeItemMerger[ANY_ARRAY] = simpleChoice;
            alternativeItemMerger[ARRAY] = simpleChoice;
            alternativeItemMerger[ANY_FUNCTION] = simpleChoice;
            alternativeItemMerger[FUNCTION] = simpleChoice;
            alternativeItemMerger[RECORD] = simpleChoice;
            alternativeItemMerger[EXTENSIBLE_RECORD] = simpleChoice;
            break;

            case STRING:
            alternativeItemMerger[ERROR] = error;
            alternativeItemMerger[ANY_ITEM] = anyItem;
            alternativeItemMerger[ANY_NODE] = simpleChoice;
            alternativeItemMerger[ELEMENT] = simpleChoice;
            alternativeItemMerger[ENUM] = itemString;
            alternativeItemMerger[BOOLEAN] = simpleChoice;
            alternativeItemMerger[NUMBER] = simpleChoice;
            alternativeItemMerger[STRING] = (_, _) -> typeFactory.itemString();
            alternativeItemMerger[ANY_MAP] = simpleChoice;
            alternativeItemMerger[MAP] = simpleChoice;
            alternativeItemMerger[CHOICE] = rightMergeChoice;
            alternativeItemMerger[ANY_ARRAY] = simpleChoice;
            alternativeItemMerger[ARRAY] = simpleChoice;
            alternativeItemMerger[ANY_FUNCTION] = simpleChoice;
            alternativeItemMerger[FUNCTION] = simpleChoice;
            alternativeItemMerger[RECORD] = simpleChoice;
            alternativeItemMerger[EXTENSIBLE_RECORD] = simpleChoice;
            break;

            case NUMBER:
            alternativeItemMerger[ERROR] = error;
            alternativeItemMerger[ANY_ITEM] = anyItem;
            alternativeItemMerger[ANY_NODE] = simpleChoice;
            alternativeItemMerger[ELEMENT] = simpleChoice;
            alternativeItemMerger[ENUM] = simpleChoice;
            alternativeItemMerger[BOOLEAN] = simpleChoice;
            alternativeItemMerger[NUMBER] = (_, _) -> typeFactory.itemNumber();
            alternativeItemMerger[STRING] = simpleChoice;
            alternativeItemMerger[ANY_MAP] = simpleChoice;
            alternativeItemMerger[MAP] = simpleChoice;
            alternativeItemMerger[CHOICE] = rightMergeChoice;
            alternativeItemMerger[ANY_ARRAY] = simpleChoice;
            alternativeItemMerger[ARRAY] = simpleChoice;
            alternativeItemMerger[ANY_FUNCTION] = simpleChoice;
            alternativeItemMerger[FUNCTION] = simpleChoice;
            alternativeItemMerger[RECORD] = simpleChoice;
            alternativeItemMerger[EXTENSIBLE_RECORD] = simpleChoice;
            break;

            case CHOICE:
            alternativeItemMerger[ERROR] = error;
            alternativeItemMerger[ANY_ITEM] = anyItem;
            alternativeItemMerger[ANY_NODE] = leftMergeChoice;
            alternativeItemMerger[ELEMENT] = leftMergeChoice;
            alternativeItemMerger[ENUM] = leftMergeChoice;
            alternativeItemMerger[BOOLEAN] = leftMergeChoice;
            alternativeItemMerger[NUMBER] = leftMergeChoice;
            alternativeItemMerger[STRING] = leftMergeChoice;
            alternativeItemMerger[ANY_MAP] = leftMergeChoice;
            alternativeItemMerger[MAP] = leftMergeChoice;
            alternativeItemMerger[CHOICE] = choiceMerging;
            alternativeItemMerger[ANY_ARRAY] = leftMergeChoice;
            alternativeItemMerger[ARRAY] = leftMergeChoice;
            alternativeItemMerger[ANY_FUNCTION] = leftMergeChoice;
            alternativeItemMerger[FUNCTION] = leftMergeChoice;
            alternativeItemMerger[RECORD] = leftMergeChoice;
            alternativeItemMerger[EXTENSIBLE_RECORD] = leftMergeChoice;
            break;

            default:
                throw new AssertionError("Unexpected type ordinal: " + typeOrdinal + ":" + XQueryTypes.values()[typeOrdinal]);
        }

    }


    private XQueryItemType mergeArrays(final XQueryTypeFactory typeFactory, XQueryItemType x, XQueryItemType y)
    {
        final var x_ = (XQueryEnumItemTypeArray) x;
        final var y_ = (XQueryEnumItemTypeArray) y;
        final var xArrayType = x_.getArrayMemberType();
        final var yArrayType = y_.getArrayMemberType();
        final var merged = xArrayType.sequenceMerge(yArrayType);
        return typeFactory.itemArray(merged);
    }


    private XQueryItemType mergeMaps(final XQueryTypeFactory typeFactory, XQueryItemType x, XQueryItemType y)
    {
        final var x_ = (XQueryEnumItemTypeMap) x;
        final var y_ = (XQueryEnumItemTypeMap) y;
        final var xKey = x_.getMapKeyType();
        final var yKey = y_.getMapKeyType();
        final var xValue = x_.getMapValueType();
        final var yValue = y_.getMapValueType();
        final var mergedKeyType = xKey.alternativeMerge(yKey);
        final var mergedValueType = xValue.sequenceMerge(yValue);
        return typeFactory.itemMap(mergedKeyType, mergedValueType);
    }


    private XQueryItemType rightChoiceMerge(final XQueryTypeFactory typeFactory, XQueryItemType x, XQueryItemType y)
    {
        final var rightItems = ((XQueryEnumChoiceItemType) y).getItemTypes();
        final Set<XQueryItemType> sum = new HashSet<>(rightItems.size()+1);
        sum.addAll(rightItems);
        sum.add(x);
        return typeFactory.itemChoice(sum);
    }

    private XQueryItemType leftChoiceMerge(final XQueryTypeFactory typeFactory, XQueryItemType x, XQueryItemType y)
    {
        final var leftItems = ((XQueryEnumChoiceItemType) x).getItemTypes();
        final Set<XQueryItemType> sum = new HashSet<>(leftItems.size()+1);
        sum.addAll(leftItems);
        sum.add(y);
        return typeFactory.itemChoice(sum);
    }


    private XQueryItemType mergeElements(final XQueryTypeFactory typeFactory, XQueryItemType x, XQueryItemType y)
    {
        final var els1 = ((XQueryEnumItemTypeElement) x).getElementNames();
        final var els2 = ((XQueryEnumItemTypeElement) y).getElementNames();
        final Set<String> merged = new HashSet<>(els1.size() + els2.size());
        merged.addAll(els1);
        merged.addAll(els2);
        return typeFactory.itemElement(merged);
    }


    private XQueryItemType mergeChoices(final XQueryTypeFactory typeFactory, XQueryItemType x, XQueryItemType y)
    {
        final var leftItems = ((XQueryEnumChoiceItemType) x).getItemTypes();
        final var rightItems = ((XQueryEnumChoiceItemType) y).getItemTypes();
        final Set<XQueryItemType> sum = new HashSet<>(rightItems.size()+leftItems.size());
        sum.addAll(leftItems);
        sum.addAll(rightItems);
        return typeFactory.itemChoice(sum);
    }



    private BinaryOperator<XQueryItemType> recordMerger(final XQueryTypeFactory typeFactory)
    {
        return (x, y) -> {
            final var x_ = (XQueryEnumItemTypeRecord) x;
            final var y_ = (XQueryEnumItemTypeRecord) y;
            final var xFields = x_.getRecordFields();
            final var yFields = y_.getRecordFields();

            final Set<String> commonKeys = new HashSet<>(xFields.keySet());
            commonKeys.retainAll(yFields.keySet());
            if (commonKeys.isEmpty()) {
                return typeFactory.itemChoice(Set.of(x, y));
            }

            final Set<String> allKeys = new HashSet<>(xFields.keySet());
            allKeys.addAll(yFields.keySet());
            final Map<String, XQueryRecordField> newFields = allKeys.stream()
            .collect(java.util.stream.Collectors.collectingAndThen(
                java.util.stream.Collectors.toMap(
                key_ -> key_,
                key_ -> {
                    final XQueryRecordField xField = xFields.get(key_);
                    final XQueryRecordField yField = yFields.get(key_);
                    if (xField != null && yField != null) {
                    final XQuerySequenceType mergedType = xField.type().alternativeMerge(yField.type());
                    final boolean required = xField.isRequired() && yField.isRequired();
                    return new XQueryRecordField(mergedType, required);
                    } else if (xField != null) {
                    return new XQueryRecordField(xField.type(), false);
                    } else {
                    return new XQueryRecordField(yField.type(), false);
                    }
                }
                ),
                java.util.Collections::unmodifiableMap
            ));
            return typeFactory.itemRecord(newFields);
        };
    }


    private BinaryOperator<XQueryItemType> extensibleRecordMerger(final XQueryTypeFactory typeFactory)
    {
        return (x, y) -> {
            final var x_ = (XQueryEnumItemTypeRecord) x;
            final var y_ = (XQueryEnumItemTypeRecord) y;
            final var xRecordFields = x_.getRecordFields();
            final var yRecordFields = y_.getRecordFields();
            final Set<String> allKeys = new HashSet<>(xRecordFields.keySet());
            allKeys.addAll(yRecordFields.keySet());
            final Map<String, XQueryRecordField> newFields = allKeys.stream()
            .collect(Collectors.collectingAndThen(
                Collectors.toMap(
                key_ -> key_,
                key_ -> {
                    final XQueryRecordField xField = xRecordFields.get(key_);
                    final XQueryRecordField yField = yRecordFields.get(key_);
                    if (xField != null && yField != null) {
                        final XQuerySequenceType xFieldType = xField.type();
                        final XQuerySequenceType yFieldType = yField.type();
                        final boolean required = xField.isRequired() && yField.isRequired();
                        return new XQueryRecordField(xFieldType.alternativeMerge(yFieldType), required);
                    } else if (xField != null) {
                        return new XQueryRecordField(xField.type(), false);
                    } else {
                        return new XQueryRecordField(yField.type(), false);
                    }
                }
                ),
                Collections::unmodifiableMap
            ));
            return typeFactory.itemExtensibleRecord(newFields);
        };
    }


    private BinaryOperator<XQueryItemType> enumMerger(final XQueryTypeFactory typeFactory)
    {
        return (x, y) -> {
            final var x_ = (XQueryEnumItemTypeEnum) x;
            final var y_ = (XQueryEnumItemTypeEnum) y;
            final var xMembers = x_.getEnumMembers();
            final var yMembers = y_.getEnumMembers();
            final var merged = new HashSet<String>(xMembers.size() + yMembers.size());
            merged.addAll(xMembers);
            merged.addAll(yMembers);
            return typeFactory.itemEnum(merged);
        };
    }


    @Override
    public XQueryItemType alternativeMerge(final XQueryItemType type1, final XQueryItemType type2)
    {
        final int otherOrdinal = ((XQueryEnumItemType) type2).getType().ordinal();
        return this.alternativeItemMerger[otherOrdinal].apply(type1, type2);
    }



}
