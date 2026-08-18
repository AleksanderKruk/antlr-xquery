package com.github.akruk.antlrquery.evaluator.values;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrquery.evaluator.values.operations.ValueEquality;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;

public class AntlrQueryValue {
    public final AntlrQueryValues valueType;
    public final int valueTypeOrdinal;
    public final AntlrQuerySequenceType type;
    public final ParseTree node;
    public final BigDecimal numericValue;
    public final String stringValue;
    public final AntlrQueryFunction functionValue;
    public final Boolean booleanValue;
    public final List<AntlrQueryValue> sequence;
    public final List<AntlrQueryValue> arrayMembers;
    public final Map<AntlrQueryValue, AntlrQueryValue> mapEntries;

    public final boolean isNode;
    public final boolean isNumeric;
    public final boolean isString;
    public final boolean isFunction;
    public final boolean isBoolean;
    public final boolean isArray;
    public final boolean isMap;
    public final boolean isError;

    public final boolean isEmptySequence;
    public final int size;
    public final AntlrQueryError error;
    public final String errorMessage;

    private final int hashCode;
    private final String toString;

    @Override
    public int hashCode() {
        return hashCode;
    }

    public int hashCode_() {
        return switch (valueType) {
            case ERROR -> error.hashCode();
            case ELEMENT -> node.hashCode();
            case BOOLEAN -> booleanValue.hashCode();
            case NUMBER -> numericValue.hashCode();
            case STRING -> stringValue.hashCode();
            case FUNCTION -> functionValue.hashCode();
            case ARRAY -> arrayMembers.hashCode();
            case MAP -> mapEntries.hashCode();
            case EMPTY_SEQUENCE, SEQUENCE -> sequence.hashCode();
        };
    }


    public static AntlrQueryValue functionReference(AntlrQueryFunction v, AntlrQuerySequenceType type) {
        return new AntlrQueryValue(
            AntlrQueryValues.FUNCTION,
            type,
            null,
            null,
            null,
            null,
            v,
            null,
            null,
            null,
            null,
            null
        );
    }


    public static AntlrQueryValue boolean_(boolean v, AntlrQuerySequenceType type) {
        return new AntlrQueryValue(
            AntlrQueryValues.BOOLEAN,
            type,
            null,
            null,
            null,
            v,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }

    public static AntlrQueryValue string(String v, AntlrQuerySequenceType type) {
        return new AntlrQueryValue(
            AntlrQueryValues.STRING,
            type,
            null,
            null,
            v,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }

    public static AntlrQueryValue number(BigDecimal v, AntlrQuerySequenceType type) {
        return new AntlrQueryValue(
            AntlrQueryValues.NUMBER,
            type,
            null,
            v,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }

    public static AntlrQueryValue number(int v, AntlrQuerySequenceType type) {
        return new AntlrQueryValue(
            AntlrQueryValues.NUMBER,
            type,
            null,
            BigDecimal.valueOf(v),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }

    public static AntlrQueryValue node(ParseTree node, AntlrQuerySequenceType type) {
        return new AntlrQueryValue(
            AntlrQueryValues.ELEMENT,
            type,
            node,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }

    public static AntlrQueryValue sequence(List<AntlrQueryValue> sequence, AntlrQuerySequenceType type) {
        if (sequence.isEmpty()) {
            return emptySequence(type);
        }
        if (sequence.size() == 1) {
            return sequence.getFirst();
        }
        return new AntlrQueryValue(
            AntlrQueryValues.SEQUENCE,
            type,
            null,
            null,
            null,
            null,
            null,
            sequence,
            null,
            null,
            null,
            null
        );
    }

    public static AntlrQueryValue emptySequence(AntlrQuerySequenceType type) {
        return new AntlrQueryValue(
            AntlrQueryValues.EMPTY_SEQUENCE,
            type,
            null,
            null,
            null,
            null,
            null,
            List.of(),
            null,
            null,
            null,
            null
        );
    }

    public static AntlrQueryValue array(List<AntlrQueryValue> arrayMembers, AntlrQuerySequenceType type) {
        return new AntlrQueryValue(
            AntlrQueryValues.ARRAY,
            type,
            null,
            null,
            null,
            null,
            null,
            null,
            arrayMembers,
            null,
            null,
            null
        );
    }

    public static AntlrQueryValue map(Map<AntlrQueryValue, AntlrQueryValue> mapEntries, AntlrQuerySequenceType type) {
        return new AntlrQueryValue(
            AntlrQueryValues.MAP,
            type,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            mapEntries,
            null,
            null
        );
    }

    public static AntlrQueryValue error(AntlrQueryError error, String message, AntlrQuerySequenceType type) {
        return new AntlrQueryValue(
            AntlrQueryValues.ERROR,
            type,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            error,
            message
        );
    }

    private AntlrQueryValue(
        AntlrQueryValues valueType,
        AntlrQuerySequenceType type,
        ParseTree node,
        BigDecimal numericValue,
        String stringValue,
        Boolean booleanValue,
        AntlrQueryFunction functionValue,
        List<AntlrQueryValue> sequence,
        List<AntlrQueryValue> arrayMembers,
        Map<AntlrQueryValue, AntlrQueryValue> mapEntries,
        AntlrQueryError error,
        String errorMessage)
    {
        this.valueType = valueType;
        this.valueTypeOrdinal = valueType.ordinal();
        this.type = type;
        this.node = node;
        this.numericValue = numericValue;
        this.stringValue = stringValue;
        this.booleanValue = booleanValue;
        this.functionValue = functionValue;
        this.error = error;
        this.errorMessage = errorMessage;
        this.arrayMembers = arrayMembers;
        this.mapEntries = mapEntries;

        this.sequence = sequence != null ? sequence : List.of(this);
        this.size = this.sequence.size();

        this.isEmptySequence = this.valueType == AntlrQueryValues.EMPTY_SEQUENCE;
        this.isNode = this.valueType == AntlrQueryValues.ELEMENT;
        this.isNumeric = this.valueType == AntlrQueryValues.NUMBER;
        this.isString = this.valueType == AntlrQueryValues.STRING;
        this.isFunction = this.valueType == AntlrQueryValues.FUNCTION;
        this.isBoolean = this.valueType == AntlrQueryValues.BOOLEAN;
        this.isArray = this.valueType == AntlrQueryValues.ARRAY;
        this.isMap = this.valueType == AntlrQueryValues.MAP;
        this.isError = this.valueType == AntlrQueryValues.ERROR;
        this.hashCode = hashCode_();
        this.toString = toString_();
    }

    @Override
    public String toString() {
        return this.toString;
    }

    public String toString_() {
        return switch(valueType) {
            case ERROR -> "<Error:" + errorMessage + "/>";
            case ARRAY -> "<Array:" + arrayMembers + "/>";
            case BOOLEAN -> "<Boolean:" + booleanValue + "/>";
            case ELEMENT -> {
                final Interval sourceInterval = node.getSourceInterval();
                yield "<Node:" +
                        node.getClass().getSimpleName() +
                        ":" +
                        sourceInterval.a +
                        "," +
                        sourceInterval.b +
                        ":" +
                        node.getText() +
                        "/>";
            }
            case SEQUENCE -> "<Sequence:" + sequence + "/>";
            case EMPTY_SEQUENCE -> "<EmptySequence/>";
            case FUNCTION -> "<Function:" + functionValue + "/>";
            case MAP -> "<Map:" + mapEntries + "/>";
            case NUMBER -> "<Number:" + numericValue.toPlainString() + "/>";
            case STRING -> "<String:\"" + stringValue + "\"/>";
        };
    }

    final ValueEquality equality = new ValueEquality();

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || !(obj instanceof final AntlrQueryValue other))
            return false;
        if (this.hashCode == other.hashCode)
            return true;
        if (this.valueType != other.valueType)
            return false;
        return equality.valueEquals(this, other);
    }



}
