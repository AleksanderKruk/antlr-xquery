package com.github.akruk.antlrxquery.evaluator.values.operations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import com.github.akruk.antlrxquery.evaluator.values.XQueryError;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValues;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;
import com.github.akruk.antlrxquery.typesystem.XQueryRecordField;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryTypes;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class Caster {
    @SuppressWarnings("unchecked")
    private final BiFunction<XQueryItemType, XQueryValue, XQueryValue>[][] cast = new BiFunction[XQueryTypes.values().length][XQueryValues.values().length];
    private final Stringifier stringifier;
    private final XQueryValueFactory valueFactory;

    public XQueryValue cast(XQuerySequenceType targetType, XQueryValue testedValue)
    {
        if (testedValue.type.isSubtypeOf(targetType))
            return valueFactory.bool(true);
        if (testedValue.isEmptySequence) {
            return testedValue;
        }
        final var castFunction = cast[targetType.itemType.typeOrdinal][testedValue.valueTypeOrdinal];
        final XQueryValue castingResult = castFunction.apply(targetType.itemType, testedValue);
        return valueFactory.bool(!castingResult.isError);
    }


    public Caster(XQueryTypeFactory typeFactory, XQueryValueFactory valueFactory, Stringifier stringifier, EffectiveBooleanValue ebv) {
        this.valueFactory = valueFactory;
        this.stringifier = stringifier;
        BiFunction<XQueryItemType, XQueryValue, XQueryValue> err = (t, v) -> valueFactory.error(XQueryError.InvalidCastValue, "Cannot cast value " + v + " to " + t);
        for (var row : cast) {
            Arrays.fill(row, err);
        }
        final int FUNCTION_V = XQueryValues.FUNCTION.ordinal();
        final int NUMBER_V = XQueryValues.NUMBER.ordinal();
        final int STRING_V = XQueryValues.STRING.ordinal();
        final int BOOLEAN_V = XQueryValues.BOOLEAN.ordinal();
        final int ELEMENT_V = XQueryValues.ELEMENT.ordinal();
        final int ERROR_V = XQueryValues.ERROR.ordinal();
        final int ARRAY_V = XQueryValues.ARRAY.ordinal();
        final int MAP_V = XQueryValues.MAP.ordinal();
        final int SEQUENCE_V = XQueryValues.SEQUENCE.ordinal();
        final int EMPTY_SEQUENCE_V = XQueryValues.EMPTY_SEQUENCE.ordinal();

        final int MAP_T = XQueryTypes.MAP.ordinal();
        final int ARRAY_T = XQueryTypes.ARRAY.ordinal();
        final int ENUM_T = XQueryTypes.ENUM.ordinal();
        final int RECORD_T = XQueryTypes.RECORD.ordinal();
        final int EXTENSIBLE_RECORD_T = XQueryTypes.EXTENSIBLE_RECORD.ordinal();
        final int BOOLEAN_T = XQueryTypes.BOOLEAN.ordinal();
        final int STRING_T = XQueryTypes.STRING.ordinal();
        final int NUMBER_T = XQueryTypes.NUMBER.ordinal();

        cast[MAP_T][ARRAY_V] = (t, v) -> {
            Map<XQueryValue, XQueryValue> map = new HashMap<>(v.arrayMembers.size(), 1.0f);
            int i = 0;
            for (var el : v.arrayMembers) {
                var keyCast = cast(typeFactory.one(t.mapKeyType), valueFactory.number(i));
                if (keyCast.isError)
                    return keyCast;
                var valueCast = cast(t.mapValueType, el);
                if (valueCast.isError)
                    return valueCast;
                map.put(keyCast, valueCast);
                i++;
            }
            return valueFactory.map(map);
        };
        cast[MAP_T][MAP_V] = (XQueryItemType t, XQueryValue v) -> {
            Map<XQueryValue, XQueryValue> map = new HashMap<>(v.arrayMembers.size(), 1.0f);
            for (var entry : v.mapEntries.entrySet()) {
                var keyCast = cast(typeFactory.one(t.mapKeyType), entry.getKey());
                if (keyCast.isError)
                    return keyCast;
                var valueCast = cast(t.mapValueType, entry.getValue());
                if (valueCast.isError)
                    return valueCast;
                map.put(keyCast, valueCast);
            }
            return valueFactory.map(map);
        };

        cast[ARRAY_T][ARRAY_T] = (XQueryItemType t, XQueryValue v) -> {
            List<XQueryValue> list = new ArrayList<>(v.arrayMembers.size());
            for (var member : v.arrayMembers) {
                var valueCast = cast(t.arrayMemberType, member);
                if (valueCast.isError)
                    return valueCast;
                list.add(valueCast);
            }
            return valueFactory.array(list);
        };

        cast[ARRAY_T][SEQUENCE_V] = (_, v) -> {
            return valueFactory.array(v.sequence);
        };
        cast[ARRAY_T][EMPTY_SEQUENCE_V] = (_, _) -> valueFactory.array(List.of());

        cast[EXTENSIBLE_RECORD_T][MAP_V] = (t, v) -> {
            // Same as constrained record but without new record creation
            var recordFields = t.recordFields.entrySet().stream()
                .collect(Collectors.partitioningBy(entry -> entry.getValue().isRequired()));
            var requiredRecordFields = recordFields.get(true);
            for (Entry<String, XQueryRecordField>
                    entry : requiredRecordFields)
            {
                String fieldname = entry.getKey();
                XQueryRecordField semanticRecordField = entry.getValue();
                XQueryValue mapEntry = v.mapEntries.get(valueFactory.string(fieldname));
                if (mapEntry == null) {
                    return valueFactory.error(XQueryError.InvalidCastValue,
                        "At casting value: " + v + " to type " + t + " -> missing required field: " + fieldname);
                }
                var result = cast(semanticRecordField.type(), mapEntry);
                if (result.isError)
                    return result;
            }

            var optionalRecordFields = recordFields.get(false);
            for (Entry<String, XQueryRecordField>
                    entry : optionalRecordFields)
            {
                String fieldname = entry.getKey();
                XQueryRecordField semanticRecordField = entry.getValue();
                XQueryValue mapEntry = v.mapEntries.get(valueFactory.string(fieldname));
                if (mapEntry == null) {
                    continue;
                }
                var result = cast(semanticRecordField.type(), mapEntry);
                if (result.isError)
                    return result;
            }
            return v;
        };
        cast[RECORD_T][MAP_V] = (t, v) -> {
            var recordFields = t.recordFields.entrySet().stream()
                .collect(Collectors.partitioningBy(entry -> entry.getValue().isRequired()));
            var requiredRecordFields = recordFields.get(true);
            Map<String, XQueryValue> record = new HashMap<>(recordFields.size());

            for (Entry<String, XQueryRecordField>
                    entry : requiredRecordFields)
            {
                String fieldname = entry.getKey();
                XQueryRecordField semanticRecordField = entry.getValue();
                XQueryValue mapEntry = v.mapEntries.get(valueFactory.string(fieldname));
                if (mapEntry == null) {
                    return valueFactory.error(XQueryError.InvalidCastValue,
                        "At casting value: " + v + " to type " + t + " -> missing required field: " + fieldname);
                }
                var result = cast(semanticRecordField.type(), mapEntry);
                if (result.isError)
                    return result;
                record.put(fieldname, result);
            }

            var optionalRecordFields = recordFields.get(false);
            for (Entry<String, XQueryRecordField>
                    entry : optionalRecordFields)
            {
                String fieldname = entry.getKey();
                XQueryRecordField semanticRecordField = entry.getValue();
                XQueryValue mapEntry = v.mapEntries.get(valueFactory.string(fieldname));
                if (mapEntry == null) {
                    continue;
                }
                var result = cast(semanticRecordField.type(), mapEntry);
                if (result.isError)
                    return result;
                record.put(fieldname, result);
            }
            return valueFactory.record(record);
        };

        BiFunction<XQueryItemType, XQueryValue, XQueryValue> identity = (_, v) -> v;
        cast[BOOLEAN_T][NUMBER_V] = (_, v) -> ebv.effectiveBooleanValue(v);
        cast[BOOLEAN_T][STRING_V] = (_, v) -> ebv.effectiveBooleanValue(v);
        cast[BOOLEAN_T][BOOLEAN_V] = identity;
        cast[BOOLEAN_T][ARRAY_V] = (_, v) -> ebv.effectiveBooleanValue(v);
        cast[BOOLEAN_T][MAP_V] = (_, v) -> ebv.effectiveBooleanValue(v);

        cast[ENUM_T][FUNCTION_V] = castToEnum();
        cast[ENUM_T][NUMBER_V] = castToEnum();
        cast[ENUM_T][STRING_V] = castToEnum();
        cast[ENUM_T][BOOLEAN_V] = castToEnum();
        cast[ENUM_T][ELEMENT_V] = castToEnum();
        cast[ENUM_T][ERROR_V] = castToEnum();
        cast[ENUM_T][ARRAY_V] = castToEnum();
        cast[ENUM_T][MAP_V] = castToEnum();

        BiFunction<XQueryItemType, XQueryValue, XQueryValue> stringify = (_, v) -> stringifier.stringify(v);
        cast[STRING_T][FUNCTION_V] = stringify;
        cast[STRING_T][NUMBER_V]   = stringify;
        cast[STRING_T][STRING_V]   = stringify;
        cast[STRING_T][BOOLEAN_V]  = stringify;
        cast[STRING_T][ELEMENT_V]  = stringify;
        cast[STRING_T][ERROR_V]    = stringify;
        cast[STRING_T][ARRAY_V]    = stringify;
        cast[STRING_T][MAP_V]      = stringify;

        cast[NUMBER_T][NUMBER_V] = identity;
        cast[NUMBER_T][STRING_V] = (_, v) -> {
            try {
                return valueFactory.number(new BigDecimal(v.stringValue));
            } catch (NumberFormatException e) {
                return valueFactory.error(XQueryError.InvalidCastValue, "Failed to cast string: " + v.stringValue + " to number");
            }
        };
        cast[NUMBER_T][BOOLEAN_V] = (_, v) -> valueFactory.number(v.booleanValue? 1 : 0);
        cast[NUMBER_T][ELEMENT_V] = (_, v) -> {
            try {
                return valueFactory.number(new BigDecimal(v.node.getText()));
            } catch (NumberFormatException e) {
                return valueFactory.error(XQueryError.InvalidArgumentType, "Failed to cast string: " + v.stringValue + " to number");
            }
        };


    }


    private BiFunction<XQueryItemType, XQueryValue, XQueryValue> castToEnum()
    {
        return (t, v) -> {
            final var str = stringifier.stringify(v);
            if (t.enumMembers.contains(str.stringValue))
                return valueFactory.string(str.stringValue);
            return valueFactory.error(XQueryError.InvalidCastValue, "Failed to cast string: " + v.stringValue + " to number");
        };
    }





}
