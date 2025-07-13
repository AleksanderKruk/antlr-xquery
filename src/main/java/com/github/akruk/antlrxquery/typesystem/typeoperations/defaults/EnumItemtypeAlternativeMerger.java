
package com.github.akruk.antlrxquery.typesystem.typeoperations.defaults;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;

import com.github.akruk.antlrxquery.typesystem.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.XQueryRecordField;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryEnumChoiceItemType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryEnumItemType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryEnumItemTypeArray;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryEnumItemTypeElement;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryEnumItemTypeEnum;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryEnumItemTypeFunction;
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

    public EnumItemtypeAlternativeMerger(final int typeOrdinal, final XQueryTypeFactory typeFactory)
    {
        @SuppressWarnings("unchecked")
        final BinaryOperator<XQueryItemType>[][] alternativeItemMerger = new BinaryOperator[typesCount][typesCount];
        final BinaryOperator<XQueryItemType> error = (_, _) -> typeFactory.itemError();
        final BinaryOperator<XQueryItemType> anyItem = (_, _) -> typeFactory.itemAnyItem();
        final BinaryOperator<XQueryItemType> anyNode = (_, _) -> typeFactory.itemAnyNode();
        final BinaryOperator<XQueryItemType> simpleChoice = (x, y) -> typeFactory.itemChoice(Set.of(x, y));
        final BinaryOperator<XQueryItemType> leftMergeChoice = (x, y) -> {
            final var leftItems = ((XQueryEnumChoiceItemType) x).getItemTypes();
            final Set<XQueryItemType> sum = new HashSet<>(leftItems.size()+1);
            sum.addAll(leftItems);
            sum.add(y);
            return typeFactory.itemChoice(sum);
        };
        final BinaryOperator<XQueryItemType> rightMergeChoice = (x, y) -> {
            final var rightItems = ((XQueryEnumChoiceItemType) y).getItemTypes();
            final Set<XQueryItemType> sum = new HashSet<>(rightItems.size()+1);
            sum.addAll(rightItems);
            sum.add(x);
            return typeFactory.itemChoice(sum);
        };

        final BinaryOperator<XQueryItemType> choiceMerging = (x, y) -> {
            final var leftItems = ((XQueryEnumChoiceItemType) x).getItemTypes();
            final var rightItems = ((XQueryEnumChoiceItemType) y).getItemTypes();
            final Set<XQueryItemType> sum = new HashSet<>(rightItems.size()+leftItems.size());
            sum.addAll(leftItems);
            sum.addAll(rightItems);
            return typeFactory.itemChoice(sum);
        };

        for (int i = 0; i < typesCount; i++) {
            alternativeItemMerger[ERROR][i] = error;
            alternativeItemMerger[i][ERROR] = error;
        }

        for (int i = 0; i < typesCount; i++) {
            if (i == ERROR) continue;
            alternativeItemMerger[ANY_ITEM][i] = anyItem;
            alternativeItemMerger[i][ANY_ITEM] = anyItem;
        }


        final BinaryOperator<XQueryItemType> elementSequenceMerger = (x, y) -> {
            final var els1 = ((XQueryEnumItemTypeElement) x).getElementNames();
            final var els2 = ((XQueryEnumItemTypeElement) y).getElementNames();
            final Set<String> merged = new HashSet<>(els1.size() + els2.size());
            merged.addAll(els1);
            merged.addAll(els2);
            return typeFactory.itemElement(merged);
        };

        for (int i = 0; i < typesCount; i++) {
            if (i == ERROR) continue;
            if (i == ANY_NODE) continue;
            alternativeItemMerger[ANY_NODE][i] = simpleChoice;
            alternativeItemMerger[i][ANY_NODE] = simpleChoice;
        }
        alternativeItemMerger[ANY_NODE][ANY_NODE] = anyNode;
        alternativeItemMerger[ANY_NODE][ELEMENT] = anyNode;
        alternativeItemMerger[ELEMENT][ANY_NODE] = anyNode;
        alternativeItemMerger[ANY_NODE][CHOICE] = rightMergeChoice;
        alternativeItemMerger[CHOICE][ANY_NODE] = leftMergeChoice;

        for (int i = 0; i < typesCount; i++) {
            if (i == ERROR) continue;
            if (i == ANY_NODE) continue;
            alternativeItemMerger[ELEMENT][i] = simpleChoice;
            alternativeItemMerger[i][ELEMENT] = simpleChoice;
        }
        alternativeItemMerger[ELEMENT][ELEMENT] = elementSequenceMerger;
        alternativeItemMerger[ELEMENT][CHOICE] = rightMergeChoice;
        alternativeItemMerger[CHOICE][ELEMENT] = leftMergeChoice;

        for (int i = 0; i < typesCount; i++) {
            if (i == ERROR) continue;
            if (i == ANY_NODE) continue;
            if (i == ELEMENT) continue;
            alternativeItemMerger[ANY_MAP][i] = simpleChoice;
            alternativeItemMerger[i][ANY_MAP] = simpleChoice;
        }
        final BinaryOperator<XQueryItemType> anyMap = (_, _) -> typeFactory.itemAnyMap();
        alternativeItemMerger[ANY_MAP][ANY_MAP] = anyMap;
        alternativeItemMerger[ANY_MAP][MAP] = anyMap;
        alternativeItemMerger[MAP][ANY_MAP] = anyMap;
        alternativeItemMerger[ANY_MAP][CHOICE] = rightMergeChoice;
        alternativeItemMerger[CHOICE][ANY_MAP] = leftMergeChoice;

        for (int i = 0; i < typesCount; i++) {
            if (i == ERROR) continue;
            if (i == ANY_NODE) continue;
            if (i == ELEMENT) continue;
            if (i == ANY_MAP) continue;
            alternativeItemMerger[MAP][i] = simpleChoice;
            alternativeItemMerger[i][MAP] = simpleChoice;
        }

        final BinaryOperator<XQueryItemType> mapMerging = (x, y) -> {
            final var x_ = (XQueryEnumItemTypeMap) x;
            final var y_ = (XQueryEnumItemTypeMap) y;
            final var xKey = x_.getMapKeyType();
            final var yKey = y_.getMapKeyType();
            final var xValue = x_.getMapValueType();
            final var yValue = y_.getMapValueType();
            final var mergedKeyType = xKey.alternativeMerge(yKey);
            final var mergedValueType = xValue.sequenceMerge(yValue);
            return typeFactory.itemMap(mergedKeyType, mergedValueType);
        };

        alternativeItemMerger[MAP][MAP] = mapMerging;
        alternativeItemMerger[ANY_MAP][CHOICE] = rightMergeChoice;
        alternativeItemMerger[CHOICE][ANY_MAP] = leftMergeChoice;

        for (int i = 0; i < typesCount; i++) {
            if (i == ERROR) continue;
            if (i == ANY_NODE) continue;
            if (i == ELEMENT) continue;
            if (i == ANY_MAP) continue;
            if (i == MAP) continue;
            alternativeItemMerger[ANY_ARRAY][i] = simpleChoice;
            alternativeItemMerger[i][ANY_ARRAY] = simpleChoice;
        }
        final BinaryOperator<XQueryItemType> anyArray = (_, _) -> typeFactory.itemAnyArray();
        alternativeItemMerger[ANY_ARRAY][ANY_ARRAY] = anyArray;
        alternativeItemMerger[ARRAY][ANY_ARRAY] = anyArray;
        alternativeItemMerger[ANY_ARRAY][ARRAY] = anyArray;
        alternativeItemMerger[ANY_ARRAY][CHOICE] = rightMergeChoice;
        alternativeItemMerger[CHOICE][ANY_ARRAY] = leftMergeChoice;


        for (int i = 0; i < typesCount; i++) {
            if (i == ERROR) continue;
            if (i == ANY_NODE) continue;
            if (i == ELEMENT) continue;
            if (i == ANY_MAP) continue;
            if (i == MAP) continue;
            if (i == ANY_ARRAY) continue;
            alternativeItemMerger[ARRAY][i] = simpleChoice;
            alternativeItemMerger[i][ARRAY] = simpleChoice;
        }
        final BinaryOperator<XQueryItemType> arrayMerging = (x, y) -> {
            final var x_ = (XQueryEnumItemTypeArray) x;
            final var y_ = (XQueryEnumItemTypeArray) y;
            final var xArrayType = x_.getArrayType();
            final var yArrayType = y_.getArrayType();
            final var merged = xArrayType.sequenceMerge(yArrayType);
            return typeFactory.itemArray(merged);
        };
        alternativeItemMerger[ARRAY][ARRAY] = arrayMerging;
        alternativeItemMerger[ARRAY][CHOICE] = rightMergeChoice;
        alternativeItemMerger[CHOICE][ARRAY] = leftMergeChoice;

        for (int i = 0; i < typesCount; i++) {
            if (i == ERROR) continue;
            if (i == ANY_NODE) continue;
            if (i == ELEMENT) continue;
            if (i == ANY_MAP) continue;
            if (i == MAP) continue;
            if (i == ANY_ARRAY) continue;
            if (i == ARRAY) continue;
            alternativeItemMerger[ANY_FUNCTION][i] = simpleChoice;
            alternativeItemMerger[i][ANY_FUNCTION] = simpleChoice;
        }
        alternativeItemMerger[ANY_FUNCTION][ANY_FUNCTION] = arrayMerging;
        alternativeItemMerger[ANY_FUNCTION][CHOICE] = rightMergeChoice;
        alternativeItemMerger[CHOICE][ANY_FUNCTION] = leftMergeChoice;


        for (int i = 0; i < typesCount; i++) {
            if (i == ERROR) continue;
            if (i == ANY_NODE) continue;
            if (i == ELEMENT) continue;
            if (i == ANY_MAP) continue;
            if (i == MAP) continue;
            if (i == ANY_ARRAY) continue;
            if (i == ARRAY) continue;
            if (i == ANY_FUNCTION) continue;
            alternativeItemMerger[FUNCTION][i] = simpleChoice;
            alternativeItemMerger[i][FUNCTION] = simpleChoice;
        }
        final BinaryOperator<XQueryItemType> functionMerging = (x, y) -> {
            final var x_ = (XQueryEnumItemTypeFunction) x;
            final var y_ = (XQueryEnumItemTypeFunction) y;
            final var xReturnedType = x_.getReturnedType();
            final var yReturnedType = y_.getReturnedType();
            final var xArgs = x_.getArgumentTypes();
            final var yArgs = y_.getArgumentTypes();
            // final var merged = xArrayType.sequenceMerge(yArrayType);
            // return typeFactory.itemArray(merged);
            return null;
        };
        alternativeItemMerger[FUNCTION][FUNCTION] = functionMerging;
        alternativeItemMerger[FUNCTION][CHOICE] = rightMergeChoice;
        alternativeItemMerger[CHOICE][FUNCTION] = leftMergeChoice;


        for (int i = 0; i < typesCount; i++) {
            if (i == ERROR) continue;
            if (i == ANY_NODE) continue;
            if (i == ELEMENT) continue;
            if (i == ANY_MAP) continue;
            if (i == MAP) continue;
            if (i == ANY_ARRAY) continue;
            if (i == ARRAY) continue;
            if (i == ANY_FUNCTION) continue;
            if (i == FUNCTION) continue;
            alternativeItemMerger[ENUM][i] = simpleChoice;
            alternativeItemMerger[i][ENUM] = simpleChoice;
        }
        final BinaryOperator<XQueryItemType> enumMerging = (x, y) -> {
            final var x_ = (XQueryEnumItemTypeEnum) x;
            final var y_ = (XQueryEnumItemTypeEnum) y;
            final var xMembers = x_.getEnumMembers();
            final var yMembers = y_.getEnumMembers();
            final var merged = new HashSet<String>(xMembers.size() + yMembers.size());
            merged.addAll(xMembers);
            merged.addAll(yMembers);
            return typeFactory.itemEnum(merged);
        };
        final BinaryOperator<XQueryItemType> itemString = (_, _) -> typeFactory.itemString();
        alternativeItemMerger[ENUM][ENUM] = enumMerging;
        alternativeItemMerger[ENUM][STRING] = itemString;
        alternativeItemMerger[STRING][ENUM] = itemString;
        alternativeItemMerger[ENUM][CHOICE] = rightMergeChoice;
        alternativeItemMerger[CHOICE][ENUM] = leftMergeChoice;


        for (int i = 0; i < typesCount; i++) {
            if (i == ERROR) continue;
            if (i == ANY_NODE) continue;
            if (i == ELEMENT) continue;
            if (i == ANY_MAP) continue;
            if (i == MAP) continue;
            if (i == ANY_ARRAY) continue;
            if (i == ARRAY) continue;
            if (i == ANY_FUNCTION) continue;
            if (i == FUNCTION) continue;
            if (i == ENUM) continue;
            alternativeItemMerger[RECORD][i] = simpleChoice;
            alternativeItemMerger[i][RECORD] = simpleChoice;
        }

        final BinaryOperator<XQueryItemType> recordMerging = (x, y) -> {
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

        final BinaryOperator<XQueryItemType> extensibleRecordMerging = (x, y) -> {
            final var x_ = (XQueryEnumItemTypeRecord) x;
            final var y_ = (XQueryEnumItemTypeRecord) y;
            final var xRecordFields = x_.getRecordFields();
            final var yRecordFields = y_.getRecordFields();
            final Set<String> allKeys = new HashSet<>(xRecordFields.keySet());
            allKeys.addAll(yRecordFields.keySet());
            final Map<String, XQueryRecordField> newFields = allKeys.stream()
            .collect(java.util.stream.Collectors.collectingAndThen(
                java.util.stream.Collectors.toMap(
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
                java.util.Collections::unmodifiableMap
            ));
            return typeFactory.itemExtensibleRecord(newFields);
        };
        alternativeItemMerger[RECORD][RECORD] = recordMerging;
        // TODO:
        alternativeItemMerger[EXTENSIBLE_RECORD][RECORD] = recordMerging;
        // TODO:
        alternativeItemMerger[RECORD][EXTENSIBLE_RECORD] = recordMerging;
        alternativeItemMerger[RECORD][CHOICE] = rightMergeChoice;
        alternativeItemMerger[CHOICE][RECORD] = leftMergeChoice;

        for (int i = 0; i < typesCount; i++) {
            if (i == ERROR) continue;
            if (i == ANY_NODE) continue;
            if (i == ELEMENT) continue;
            if (i == ANY_MAP) continue;
            if (i == MAP) continue;
            if (i == ANY_ARRAY) continue;
            if (i == ARRAY) continue;
            if (i == ANY_FUNCTION) continue;
            if (i == FUNCTION) continue;
            if (i == ENUM) continue;
            if (i == RECORD) continue;
            alternativeItemMerger[EXTENSIBLE_RECORD][i] = simpleChoice;
            alternativeItemMerger[i][EXTENSIBLE_RECORD] = simpleChoice;
        }
        alternativeItemMerger[EXTENSIBLE_RECORD][EXTENSIBLE_RECORD] = recordMerging;
        alternativeItemMerger[EXTENSIBLE_RECORD][CHOICE] = rightMergeChoice;
        alternativeItemMerger[CHOICE][EXTENSIBLE_RECORD] = leftMergeChoice;


        for (int i = 0; i < typesCount; i++) {
            if (i == ERROR) continue;
            if (i == ANY_NODE) continue;
            if (i == ELEMENT) continue;
            if (i == ANY_MAP) continue;
            if (i == MAP) continue;
            if (i == ANY_ARRAY) continue;
            if (i == ARRAY) continue;
            if (i == ANY_FUNCTION) continue;
            if (i == FUNCTION) continue;
            if (i == ENUM) continue;
            if (i == RECORD) continue;
            if (i == EXTENSIBLE_RECORD) continue;
            alternativeItemMerger[BOOLEAN][i] = simpleChoice;
            alternativeItemMerger[i][BOOLEAN] = simpleChoice;
        }
        alternativeItemMerger[BOOLEAN][BOOLEAN] = (_, _) -> typeFactory.itemBoolean();
        alternativeItemMerger[EXTENSIBLE_RECORD][CHOICE] = rightMergeChoice;
        alternativeItemMerger[CHOICE][EXTENSIBLE_RECORD] = leftMergeChoice;


        for (int i = 0; i < typesCount; i++) {
            if (i == ERROR) continue;
            if (i == ANY_NODE) continue;
            if (i == ELEMENT) continue;
            if (i == ANY_MAP) continue;
            if (i == MAP) continue;
            if (i == ANY_ARRAY) continue;
            if (i == ARRAY) continue;
            if (i == ANY_FUNCTION) continue;
            if (i == FUNCTION) continue;
            if (i == ENUM) continue;
            if (i == RECORD) continue;
            if (i == EXTENSIBLE_RECORD) continue;
            if (i == BOOLEAN) continue;
            alternativeItemMerger[STRING][i] = simpleChoice;
            alternativeItemMerger[i][STRING] = simpleChoice;
        }
        alternativeItemMerger[STRING][STRING] = (_, _) -> typeFactory.itemString();
        alternativeItemMerger[EXTENSIBLE_RECORD][CHOICE] = rightMergeChoice;
        alternativeItemMerger[CHOICE][EXTENSIBLE_RECORD] = leftMergeChoice;


        alternativeItemMerger[NUMBER][NUMBER] = (_, _) -> typeFactory.itemNumber();
        alternativeItemMerger[NUMBER][CHOICE] = rightMergeChoice;
        alternativeItemMerger[CHOICE][NUMBER] = leftMergeChoice;

        alternativeItemMerger[CHOICE][CHOICE] = choiceMerging;


        // we save only the relevant merger for the typeOrdinal
        this.alternativeItemMerger = alternativeItemMerger[typeOrdinal];
    }


    @Override
    public XQueryItemType alternativeMerge(final XQueryItemType type1, final XQueryItemType type2)
    {
        final int otherOrdinal = ((XQueryEnumItemType) type2).getType().ordinal();
        return this.alternativeItemMerger[otherOrdinal].apply(type1, type2);
    }



}