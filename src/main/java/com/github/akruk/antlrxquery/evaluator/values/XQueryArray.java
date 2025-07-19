package com.github.akruk.antlrxquery.evaluator.values;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;

public record XQueryArray(List<XQueryValue> value, XQueryValueFactory valueFactory)
        implements XQueryValue
{
    @Override
    public XQueryValue valueEqual(XQueryValue other) {
        if (!(other instanceof XQueryArray otherArray)) {
            return XQueryError.InvalidArgumentType;
        }
        if (this.value.size() != otherArray.value.size()) {
            return valueFactory.bool(false);
        }

        for (int i = 0; i < this.value.size(); i++) {
            XQueryValue thisValue = this.value.get(i);
            XQueryValue otherValue = otherArray.value.get(i);
            if (!thisValue.valueEqual(otherValue).effectiveBooleanValue()) {
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
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < value.size(); i++) {
            sb.append(value.get(i).stringValue());
            if (i < value.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public XQueryFunction functionValue() {
        return (_, args) -> {
            if (!args.get(0).isNumericValue())
                return XQueryError.InvalidArgumentType;
            return value.get(args.get(0).numericValue().intValue());
        };
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
        return value;
    }

    @Override
    public Map<XQueryValue, XQueryValue> mapEntries() {
        return null;
    }



}
