package com.github.akruk.antlrxquery.evaluator.values;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;

public class XQueryValue {
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

    @Override
    public int hashCode() {
        return hashCode;
    }

    private int hashCode_() {
        if (isError)
            return error.hashCode();
        if (isNode)
            return node.hashCode();
        if (isNumeric)
            return numericValue.hashCode();
        if (isString)
            return stringValue.hashCode();
        if (isFunction)
            return functionValue.hashCode();
        if (isBoolean)
            return booleanValue.hashCode();
        if (isArray)
            return arrayMembers.hashCode();
        if (isMap)
            return mapEntries.hashCode();
        return sequence.hashCode();
    }


    public static XQueryValue functionReference(XQueryFunction v, XQuerySequenceType type) {
        return new XQueryValue(
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

    public static XQueryValue array(List<XQueryValue> arrayMembers, XQuerySequenceType type) {
        return new XQueryValue(
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

    private XQueryValue(XQuerySequenceType type,
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
        this.type = type;
        this.node = node;
        this.numericValue = numericValue;
        this.stringValue = stringValue;
        this.booleanValue = booleanValue;
        this.functionValue = functionValue;
        this.error = error;
        this.errorMessage = errorMessage;
        this.sequence = sequence != null ? sequence : List.of(this);
        this.arrayMembers = arrayMembers;
        this.mapEntries = mapEntries;
        isNode = node != null;
        isNumeric = numericValue != null;
        isString = stringValue != null;
        isFunction = functionValue != null;
        isBoolean = booleanValue != null;
        final int size_ = this.sequence.size();
        isEmptySequence = size_ == 0;
        size = size_;
        isArray = arrayMembers != null;
        isMap = mapEntries != null;
        isError = error != null;
        hashCode = hashCode_();
    }

    @Override
    public String toString() {
        if (isError) {
            return "<Error:" + errorMessage + "/>";
        }
        if (isNode) {
            return "<Node:" + node.getText() + "/>";
        }
        if (isNumeric) {
            return "<Number:" + numericValue.toPlainString() + "/>";
        }
        if (isString) {
            return "<String:\"" + stringValue + "\"/>";
        }
        if (isBoolean) {
            return "<Boolean:" + booleanValue + "/>";
        }
        if (isFunction) {
            return "<Function:" + functionValue + "/>";
        }
        if (isArray) {
            return "<Array:" + arrayMembers + "/>";
        }
        if (isMap) {
            return "<Map:" + mapEntries + "/>";
        }
        if (sequence != null && sequence.size() > 1) {
            return "<Sequence:" + sequence + "/>";
        }
        if (isEmptySequence) {
            return "<EmptySequence/>";
        }

        return "<Unknown/>";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        XQueryValue other = (XQueryValue) obj;
        if (this.size != other.size)
            return false;

        if (this.isEmptySequence || other.isEmptySequence)
            return true;


        if (this.isError || other.isError)
            return false;

        if (this.isNode && other.isNode)
            return this.node.getText().equals(other.node.getText());

        if (this.isNumeric && other.isNumeric)
            return this.numericValue.compareTo(other.numericValue) == 0;

        if (this.isString && other.isString)
            return this.stringValue.equals(other.stringValue);

        if (this.isBoolean && other.isBoolean)
            return this.booleanValue.equals(other.booleanValue);

        if (this.isFunction && other.isFunction)
            return this.functionValue.equals(other.functionValue); // lub referencja, jak wolisz

        if (this.isArray && other.isArray) {
            if (this.arrayMembers.size() != other.arrayMembers.size()) return false;
            for (int i = 0; i < this.arrayMembers.size(); i++) {
                if (!this.arrayMembers.get(i).equals(other.arrayMembers.get(i))) {
                    return false;
                }
            }
            return true;
        }

        if (this.isMap && other.isMap) {
            if (this.mapEntries.size() != other.mapEntries.size()) return false;
            for (Map.Entry<XQueryValue, XQueryValue> entry : this.mapEntries.entrySet()) {
                XQueryValue otherValue = other.mapEntries.get(entry.getKey());
                if (otherValue == null || !entry.getValue().equals(otherValue)) {
                    return false;
                }
            }
            return true;
        }

        if (size == 1) {
            return false;
        }


        for (int i = 0; i < this.sequence.size(); i++) {
            XQueryValue v1 = this.sequence.get(i);
            XQueryValue v2 = other.sequence.get(i);
            if (!v1.equals(v2.sequence.get(i))) {
                return false;
            }
        }
        return true;
    }



}
