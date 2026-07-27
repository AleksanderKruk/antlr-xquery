package com.github.akruk.antlrxquery.evaluator.values.operations;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.akruk.antlrxquery.evaluator.values.XQueryValues;
import com.github.akruk.antlrxquery.typesystem.types.itemtypes.*;
import org.checkerframework.checker.nullness.qual.NonNull;

import com.github.akruk.antlrxquery.evaluator.values.XQueryError;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;
import com.github.akruk.antlrxquery.typesystem.RecordField;
import com.github.akruk.antlrxquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrxquery.typesystem.typeoperations.cardinality.Ranges;
import com.github.akruk.antlrxquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.types.Cardinality;
import com.github.akruk.antlrxquery.typesystem.types.NumericRange;
import com.github.akruk.antlrxquery.typesystem.typeoperations.Types;
import com.github.akruk.antlrxquery.typesystem.types.itemtypes.AtomicType.*;
import com.github.akruk.antlrxquery.typesystem.types.itemtypes.StringType.StringEnum;
import com.github.akruk.antlrxquery.typesystem.types.itemtypes.StringType.StringNonEnum;
import org.checkerframework.framework.qual.DefaultQualifier;


@DefaultQualifier(NonNull.class)
public class Caster {
    private final Stringifier stringifier;
    private final XQueryValueFactory valueFactory;
    private final AntlrQueryTypeFactory typeFactory;
    private final EffectiveBooleanValue ebv;

    public Caster(Stringifier stringifier, XQueryValueFactory valueFactory, AntlrQueryTypeFactory typeFactory, EffectiveBooleanValue ebv) {
        this.stringifier = stringifier;
        this.valueFactory = valueFactory;
        this.typeFactory = typeFactory;
        this.ebv = ebv;
    }

    public XQueryValue cast(
        final  AntlrQuerySequenceType targetType,
        final  XQueryValue testedValue)
    {
        if (testedValue.isEmptySequence || testedValue.isError) {
            return testedValue;
        }
        if (Types.isSubtype(typeFactory, testedValue.type, targetType)) {
            return testedValue;
        }
        return switch(targetType.itemType()) {
            case ConcreteItemType r -> switch(r) {
                case AtomicType atomic -> 
                    handleAtomicType(testedValue, atomic);
                case ArrayLikeType.ArrayType array ->
                    handleArrayType(testedValue, array);
                case MapLikeType.MapType map ->
                    handleMapType(testedValue, map);
                case MapLikeType.RecordType record ->
                    handleRecordType(testedValue, record);
                case MapLikeType.ExtensibleRecordType extensibleRecordType ->
                    handleExtensibleRecordType(testedValue, extensibleRecordType);
                case FunctionType _ -> 
                    valueFactory.error(XQueryError.InvalidCastValue, ""); // TODO: message
                case ArrayLikeType.TupleType tuple ->
                    handleTupleType(testedValue, tuple);
                case GrammarEntityType _ -> valueFactory.error(XQueryError.InvalidCastValue, ""); // TODO: message
                case TreeLike _ -> valueFactory.error(XQueryError.InvalidCastValue, "");
            };
            case AnyItemType(), NothingType(), NeverType(), ChoiceItemType _ -> throw new IllegalStateException("Unreachable");
        };
    }

    private XQueryValue handleTupleType(XQueryValue testedValue, ArrayLikeType.TupleType tuple) {
        if (testedValue.isError) {
            return testedValue;
        }

        List<XQueryValue> elements;
        if (testedValue.isArray) {
            elements = testedValue.arrayMembers;
        } else if (testedValue.valueType == XQueryValues.SEQUENCE || testedValue.valueType == XQueryValues.EMPTY_SEQUENCE) {
            elements = testedValue.sequence;
        } else {
            elements = List.of(testedValue);
        }

        AntlrQuerySequenceType[] expectedTypes = tuple.members();

        if (elements.size() != expectedTypes.length) {
            return valueFactory.error(
                    XQueryError.InvalidCastValue,
                    "Cannot cast sequence of size " + elements.size() + " to TupleType with " + expectedTypes.length + " elements."
            );
        }

        List<XQueryValue> castedElements = new java.util.ArrayList<>();
        for (int i = 0; i < elements.size(); i++) {
            XQueryValue currentElement = elements.get(i);
            AntlrQuerySequenceType expectedType = expectedTypes[i];

            if (currentElement.type != null && Types.isSubtype(typeFactory, currentElement.type, expectedType)) {
                castedElements.add(currentElement);
            } else {
                return valueFactory.error(
                        XQueryError.InvalidCastValue,
                        "Element at index " + i + " does not match expected tuple field type."
                );
            }
        }

        return XQueryValue.sequence(castedElements, typeFactory.one(tuple));
    }

    private XQueryValue handleRecordType(
        final XQueryValue testedValue,
        final MapLikeType.RecordType recordType)
    {
        final var recordFields = recordType.fields().entrySet().stream()
            .collect(Collectors.partitioningBy(entry -> entry.getValue().isRequired()));
        final var requiredRecordFields = recordFields.get(true);
        final Map<String, XQueryValue> record = new HashMap<>(recordFields.size());

        for (final Entry<String, RecordField> entry : requiredRecordFields)
        {
            final String fieldname = entry.getKey();
            final RecordField semanticRecordField = entry.getValue();
            final XQueryValue mapEntry = testedValue.mapEntries.get(valueFactory.string(fieldname));
            if (mapEntry == null) {
                return valueFactory.error(XQueryError.InvalidCastValue,
                    "At casting value: " + testedValue + " to type " + recordType + " -> missing required field: " + fieldname);
            }
            final var result = cast(semanticRecordField.resolveFieldType(typeFactory), mapEntry);
            if (result.isError)
                return result;
            record.put(fieldname, result);
        }

        final List<Entry<String, RecordField>> optionalRecordFields = recordFields.get(false);
        for (final Entry<String, RecordField>
                entry : optionalRecordFields)
        {
            final String fieldname = entry.getKey();
            final RecordField semanticRecordField = entry.getValue();
            final XQueryValue mapEntry = testedValue.mapEntries.get(valueFactory.string(fieldname));
            if (mapEntry == null) {
                continue;
            }
            final var result = cast(semanticRecordField.resolveFieldType(typeFactory), mapEntry);
            if (result.isError)
                return result;
            record.put(fieldname, result);
        }
        return valueFactory.record(record);
    }



    private XQueryValue handleExtensibleRecordType( XQueryValue testedValue,  MapLikeType.ExtensibleRecordType extensibleRecordType) {
        // Same as constrained record but without new record creation
        final var recordFields = extensibleRecordType.fields().entrySet().stream()
            .collect(Collectors.partitioningBy(entry -> entry.getValue().isRequired()));
        final var requiredRecordFields = recordFields.get(true);
        for (final Entry<String, RecordField> entry : requiredRecordFields)
        {
            final String fieldname = entry.getKey();
            final RecordField semanticRecordField = entry.getValue();
            final XQueryValue mapEntry = testedValue.mapEntries.get(valueFactory.string(fieldname));
            if (mapEntry == null) {
                return valueFactory.error(XQueryError.InvalidCastValue,
                    "At casting value: " + testedValue + " to type " + extensibleRecordType + " -> missing required field: " + fieldname);
            }

            var type = semanticRecordField.resolveFieldType(typeFactory);
            final var result = cast(type, mapEntry);
            if (result.isError) {
                return result;
            }
        }
        final var optionalRecordFields = recordFields.get(false);
        for (final Entry<String, RecordField> entry : optionalRecordFields)
        {
            final String fieldname = entry.getKey();
            final RecordField semanticRecordField = entry.getValue();
            final XQueryValue mapEntry = testedValue.mapEntries.get(valueFactory.string(fieldname));
            if (mapEntry == null) {
                continue;
            }

            final var result = cast(semanticRecordField.resolveFieldType(typeFactory), mapEntry);
            if (result.isError)
                return result;
        }
        return testedValue;

    }

    private XQueryValue handleArrayType(
        final  XQueryValue testedValue,
        final ArrayLikeType.ArrayType array)
    {
        final var errorValue = valueFactory.error(XQueryError.InvalidCastValue, "Invalid"); // TODO: expand message
        return switch(testedValue.valueType) {
            case ARRAY -> {
                final List<XQueryValue> list = new ArrayList<>(testedValue.arrayMembers.size());
                for (final var member : testedValue.arrayMembers) {
                    final var valueCast = cast(array.memberType(), member);
                    if (valueCast.isError)
                        yield valueCast;
                    list.add(valueCast);
                }
                yield valueFactory.array(list);
            }
            case EMPTY_SEQUENCE -> valueFactory.array(List.of());
            case SEQUENCE -> valueFactory.array(testedValue.sequence);
            case MAP, STRING, BOOLEAN, ELEMENT -> errorValue;
            case NUMBER -> errorValue;
            case ERROR -> errorValue;
            case FUNCTION->errorValue;
        };

    }



    private XQueryValue handleMapType(
        final  XQueryValue testedValue,
        final MapLikeType.MapType map
        ) 
    {
        switch(testedValue.valueType) {
            case ARRAY -> {
                final Map<XQueryValue, XQueryValue> castResult = new HashMap<>(testedValue.arrayMembers.size(), 1.0f);
                int i = 0;
                for (final var el : testedValue.arrayMembers) {
                    final var keyCast = cast(typeFactory.one(map.keyType()), valueFactory.number(i));
                    if (keyCast.isError)
                        return keyCast;
                    final XQueryValue valueCast = cast(map.valueType(), el);
                    if (valueCast.isError)
                        return valueCast;
                    castResult.put(keyCast, valueCast);
                    i++;
                }
                return valueFactory.map(castResult);
            }
            case MAP -> {
                final Map<XQueryValue, XQueryValue> mapping = new HashMap<>(testedValue.arrayMembers.size(), 1.0f);
                for (final var entry : testedValue.mapEntries.entrySet()) {
                    final var keyCast = cast(typeFactory.one(map.keyType()), entry.getKey());
                    if (keyCast.isError)
                        return keyCast;
                    final var valueCast = cast(map.valueType(), entry.getValue());
                    if (valueCast.isError)
                        return valueCast;
                    mapping.put(keyCast, valueCast);
                }
                return valueFactory.map(mapping);
            }

            case BOOLEAN -> {}  case ELEMENT -> {}  case EMPTY_SEQUENCE -> {}
            case ERROR -> {}    case FUNCTION -> {} case NUMBER -> {}
            case SEQUENCE -> {} case STRING -> {}
        }
        return valueFactory.error(XQueryError.InvalidCastValue, ""); // TODO: error messages
    }


    private XQueryValue handleAtomicType(
        final  XQueryValue testedValue,
        final  AtomicType atomic
        ) 
    {
        return switch(atomic) {
            case NumberType(NumericRange range) -> 
                handleNumericType(testedValue, range);
            case StringType s -> 
                handleStringType(testedValue, s);
            case BooleanType booleanType -> 
                handleBooleanType(testedValue, booleanType);
            case RegexType regexType -> 
                handleRegexType(testedValue, regexType);
        };
    }


    private XQueryValue handleRegexType(XQueryValue testedValue, RegexType regexType) {
        throw new IllegalStateException("Not implemented"); // TODO: REGEX implement
    }


    private XQueryValue handleBooleanType(XQueryValue testedValue, BooleanType booleanType) {
        final XQueryValue testedEbv = ebv.effectiveBooleanValue(testedValue);
        if (testedEbv.isError) {
            return testedEbv;
        }
        switch(booleanType) {
            case BooleanType.True() -> {
                if (testedEbv.booleanValue) {
                    return testedEbv;
                } else {
                    return valueFactory.error(XQueryError.InvalidCastValue, "Tested effective boolean value is false"); // TODO: expand
                }
            }
            case BooleanType.False() -> {
                if (!testedEbv.booleanValue) {
                    return testedEbv;
                } else {
                    return valueFactory.error(XQueryError.InvalidCastValue, "Tested effective boolean value is false"); // TODO: expand
                }
            }
            case BooleanType.Boolean() -> { return testedEbv; }
        }
    }


    private XQueryValue handleStringType(XQueryValue testedValue, StringType s) {
        switch(s) {
            case StringEnum(Set<String> enumValues, Cardinality _) -> {
                final XQueryValue string = stringifier.stringify(testedValue);
                if (enumValues.contains(string.stringValue))
                    return string;
                return valueFactory.error(
                    XQueryError.InvalidCastValue,
                    String.format(
                        "Failed to cast value: %s \nstringified as: \n%s\nValue is not part of target enum\npossible enum values: %s",
                        testedValue,
                        string.stringValue,
                        enumValues
                    )
                );
            }
            case StringNonEnum(Cardinality length) -> {
                final XQueryValue string = stringifier.stringify(testedValue);
                final BigInteger targetLength = BigInteger.valueOf(string.stringValue.length());
                if (Cardinalities.contains(length, targetLength)) {
                    return string;
                } else {
                    return valueFactory.error(
                        XQueryError.InvalidCastValue,
                        String.format(
                            "Failed to cast value: %s \nstringified as: \n%s\nMismatching cardinality, expected: \n%s\nreceived:\n%s\n",
                            testedValue,
                            string.stringValue,
                            length,
                            targetLength
                        )
                    );
                }
            }
        }
    }


    private  XQueryValue handleNumericType(
        final  XQueryValue testedValue,
        final  NumericRange range
        ) 
    {
        final XQueryValue number = getNumericValue(testedValue);
        if (number.isError) {
            return number;
        }
        if (!Ranges.contains(range, number.numericValue)) {
            return valueFactory.error(
                XQueryError.InvalidCastValue, 
                "Failed cast; Number: " + number + " is not within range: " + range  
                );
        }

        return number;
    }

    private  XQueryValue getNumericValue(
        final  XQueryValue testedValue
        )
    {
        switch (testedValue.valueType) {
            case NUMBER:
                return testedValue;
            case BOOLEAN:
                return valueFactory.number(testedValue.booleanValue ? 1 : 0);
            case STRING:
                try {
                    return valueFactory.number(new BigDecimal(testedValue.stringValue));
                } catch (final NumberFormatException e) {
                    return valueFactory.error(XQueryError.InvalidCastValue, "Failed to cast string: " + testedValue.stringValue + " to number");
                }
            case ELEMENT:
                try {
                    return valueFactory.number(new BigDecimal(testedValue.node.getText()));
                } catch (final NumberFormatException e) {
                    return valueFactory.error(XQueryError.InvalidArgumentType, "Failed to cast string: " + testedValue.stringValue + " to number");
                }
            case ERROR: case FUNCTION: case MAP: case EMPTY_SEQUENCE: case ARRAY: case SEQUENCE:
            default:
                return valueFactory.error(XQueryError.InvalidCastValue, "Failed to cast string: " + testedValue.stringValue + " to number");
        }
    }


    public Caster(
        final  AntlrQueryTypeFactory typeFactory,
        final  XQueryValueFactory valueFactory,
        final  Stringifier stringifier,
        final  EffectiveBooleanValue ebv
        )
    {
        this.typeFactory = typeFactory;
        this.valueFactory = valueFactory;
        this.stringifier = stringifier;
        this.ebv = new EffectiveBooleanValue(valueFactory);
    }
}
