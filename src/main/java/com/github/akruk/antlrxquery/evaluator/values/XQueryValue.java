package com.github.akruk.antlrxquery.evaluator.values;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.evaluator.values.operations.ValueEquality;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;

public class XQueryValue {
    public final XQueryValues valueType;
    public final int valueTypeOrdinal;
    public final XQuerySequenceType type;
    public final ParseTree node;
    public final BigDecimal numericValue;
    public final String stringValue;
    public final XQueryFunction functionValue;
    public final Boolean booleanValue;
    public final List<XQueryValue> sequence;
    public final List<XQueryValue> arrayMembers;
    public final Map<XQueryValue, XQueryValue> mapEntries;

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
    public final XQueryError error;
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
            default -> throw new IllegalArgumentException("Unexpected value: " + valueType);
        };
    }


    public static XQueryValue functionReference(XQueryFunction v, XQuerySequenceType type) {
        return new XQueryValue(
            XQueryValues.FUNCTION,
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


    public static XQueryValue boolean_(boolean v, XQuerySequenceType type) {
        return new XQueryValue(
            XQueryValues.BOOLEAN,
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

    public static XQueryValue string(String v, XQuerySequenceType type) {
        return new XQueryValue(
            XQueryValues.STRING,
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

    public static XQueryValue number(BigDecimal v, XQuerySequenceType type) {
        return new XQueryValue(
            XQueryValues.NUMBER,
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

    public static XQueryValue number(int v, XQuerySequenceType type) {
        return new XQueryValue(
            XQueryValues.NUMBER,
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

    public static XQueryValue node(ParseTree node, XQuerySequenceType type) {
        return new XQueryValue(
            XQueryValues.ELEMENT,
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

    public static XQueryValue sequence(List<XQueryValue> sequence, XQuerySequenceType type) {
        return new XQueryValue(
            XQueryValues.SEQUENCE,
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

    public static XQueryValue emptySequence(XQuerySequenceType type) {
        return new XQueryValue(
            XQueryValues.EMPTY_SEQUENCE,
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

    public static XQueryValue array(List<XQueryValue> arrayMembers, XQuerySequenceType type) {
        return new XQueryValue(
            XQueryValues.ARRAY,
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

    public static XQueryValue map(Map<XQueryValue, XQueryValue> mapEntries, XQuerySequenceType type) {
        return new XQueryValue(
            XQueryValues.MAP,
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

    public static XQueryValue error(XQueryError error, String message, XQuerySequenceType type) {
        return new XQueryValue(
            XQueryValues.ERROR,
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

    private XQueryValue(
        XQueryValues valueType,
        XQuerySequenceType type,
        ParseTree node,
        BigDecimal numericValue,
        String stringValue,
        Boolean booleanValue,
        XQueryFunction functionValue,
        List<XQueryValue> sequence,
        List<XQueryValue> arrayMembers,
        Map<XQueryValue, XQueryValue> mapEntries,
        XQueryError error,
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

        this.isEmptySequence = this.valueType == XQueryValues.EMPTY_SEQUENCE;
        this.isNode = this.valueType == XQueryValues.ELEMENT;
        this.isNumeric = this.valueType == XQueryValues.NUMBER;
        this.isString = this.valueType == XQueryValues.STRING;
        this.isFunction = this.valueType == XQueryValues.FUNCTION;
        this.isBoolean = this.valueType == XQueryValues.BOOLEAN;
        this.isArray = this.valueType == XQueryValues.ARRAY;
        this.isMap = this.valueType == XQueryValues.MAP;
        this.isError = this.valueType == XQueryValues.ERROR;
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
            case ELEMENT -> "<Node:" + node.getText() + "/>";
            case EMPTY_SEQUENCE -> "<Sequence:" + sequence + "/>";
            case FUNCTION -> "<Function:" + functionValue + "/>";
            case MAP -> "<Map:" + mapEntries + "/>";
            case NUMBER -> "<Number:" + numericValue.toPlainString() + "/>";
            case SEQUENCE -> "<EmptySequence/>";
            case STRING -> "<String:\"" + stringValue + "\"/>";
            default -> throw new IllegalArgumentException("Unexpected value: " + valueType);
        };
    }

    ValueEquality equality = new ValueEquality();

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || !(obj instanceof final XQueryValue other))
            return false;
        if (this.hashCode == other.hashCode)
            return true;
        if (this.valueType != other.valueType)
            return false;
        return equality.valueEquals(this, other);
    }



}
