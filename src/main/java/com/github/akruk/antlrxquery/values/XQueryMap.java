package com.github.akruk.antlrxquery.values;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

public record XQueryMap(Map<XQueryValue, XQueryValue> value, XQueryValueFactory valueFactory)
        implements XQueryValue
{
    @Override
    public XQueryValue valueEqual(XQueryValue other) {
        if (!(other instanceof XQueryMap otherMap)) {
            return XQueryError.InvalidArgumentType;
        }
        if (this.value.size() != otherMap.value.size()) {
            return valueFactory.bool(false);
        }
        for (Map.Entry<XQueryValue, XQueryValue> entry : this.value.entrySet()) {
            XQueryValue otherValue = otherMap.value.get(entry.getKey());
            if (otherValue == null || !entry.getValue().valueEqual(otherValue).effectiveBooleanValue()) {
                return valueFactory.bool(false);
            }
        }
        return valueFactory.bool(true);
    }

    @Override
    public XQueryValue valueLessThan(XQueryValue other) {
        return XQueryError.InvalidArgumentType;
    }

    @Override
    public XQueryValue empty() {
        return valueFactory.bool(false);
    }

    @Override
    public ParseTree node() {
        return null;
    }

    @Override
    public BigDecimal numericValue() {
        return null;
    }

    @Override
    public String stringValue() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (Map.Entry<XQueryValue, XQueryValue> entry : value.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            sb.append(String.valueOf(entry.getKey()))
              .append(": ")
              .append(entry.getValue() != null ? entry.getValue().stringValue() : "null");
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public XQueryFunction functionValue() {
        return null;
    }

    @Override
    public Boolean effectiveBooleanValue() {
        // TODO: verify
        return true;
    }

    @Override
    public List<XQueryValue> sequence() {
        return null;
    }

    @Override
    public boolean isNumericValue() {
        return false;
    }

    @Override
    public boolean isStringValue() {
        return false;
    }

    @Override
    public boolean isBooleanValue() {
        return false;
    }

    @Override
    public boolean isSequence() {
        return false;
    }

    @Override
    public boolean isAtomic() {
        return false;
    }
    @Override
    public boolean isNode() {
        return false;
    }

    @Override
    public boolean isFunction() {
        return false;
    }

    @Override
    public boolean isError() {
        return false;
    }

    @Override
    public List<XQueryValue> atomize() {
        return List.of(this);
    }

    @Override
    public XQueryValue not() {
        return valueFactory.bool(!this.effectiveBooleanValue());
    }

    @Override
    public XQueryValue and(XQueryValue other) {
        return valueFactory.bool(this.effectiveBooleanValue() && other.effectiveBooleanValue());
    }

    @Override
    public XQueryValue or(XQueryValue other) {
        return valueFactory.bool(this.effectiveBooleanValue() || other.effectiveBooleanValue());
    }

    @Override
    public XQueryValue add(XQueryValue other) {
        return XQueryError.InvalidArgumentType;
    }

    @Override
    public XQueryValue subtract(XQueryValue other) {
        return XQueryError.InvalidArgumentType;
    }

    @Override
    public XQueryValue multiply(XQueryValue other) {
        return XQueryError.InvalidArgumentType;
    }

    @Override
    public XQueryValue divide(XQueryValue other) {
        return XQueryError.InvalidArgumentType;
    }

    @Override
    public XQueryValue integerDivide(XQueryValue other) {
        return XQueryError.InvalidArgumentType;
    }

    @Override
    public XQueryValue modulus(XQueryValue other) {
        return XQueryError.InvalidArgumentType;
    }

    @Override
    public XQueryValue concatenate(XQueryValue other) {
        return XQueryError.InvalidArgumentType;
    }

    @Override
    public XQueryValue valueUnequal(XQueryValue other) {
        return XQueryError.InvalidArgumentType;
    }

    @Override
    public XQueryValue valueLessEqual(XQueryValue other) {
        return XQueryError.InvalidArgumentType;
    }

    @Override
    public XQueryValue valueGreaterThan(XQueryValue other) {
        return XQueryError.InvalidArgumentType;
    }

    @Override
    public XQueryValue valueGreaterEqual(XQueryValue other) {
        return XQueryError.InvalidArgumentType;
    }

    @Override
    public XQueryValue generalEqual(XQueryValue other) {
        return XQueryError.InvalidArgumentType;
    }

    @Override
    public XQueryValue generalUnequal(XQueryValue other) {
        return XQueryError.InvalidArgumentType;
    }

    @Override
    public XQueryValue generalLessThan(XQueryValue other) {
        return XQueryError.InvalidArgumentType;
    }

    @Override
    public XQueryValue generalLessEqual(XQueryValue other) {
        return XQueryError.InvalidArgumentType;
    }

    @Override
    public XQueryValue generalGreaterThan(XQueryValue other) {
        return XQueryError.InvalidArgumentType;
    }

    @Override
    public XQueryValue generalGreaterEqual(XQueryValue other) {
        return XQueryError.InvalidArgumentType;
    }

    @Override
    public XQueryValue union(XQueryValue otherSequence) {
        return XQueryError.InvalidArgumentType;
    }

    @Override
    public XQueryValue intersect(XQueryValue otherSequence) {
        return XQueryError.InvalidArgumentType;
    }

    @Override
    public XQueryValue except(XQueryValue other) {
        return XQueryError.InvalidArgumentType;
    }

    @Override
    public XQueryValue remove(XQueryValue position) {
        return XQueryError.InvalidArgumentType;
    }

    @Override
    public XQueryValue data() {
        return XQueryError.InvalidArgumentType;
    }

    @Override
    public boolean isEmptySequence() {
        return false;
    }

    @Override
    public List<XQueryValue> arrayMembers() {
        return null;
    }

    @Override
    public Map<XQueryValue, XQueryValue> mapEntries() {
        return value;
    }
}
