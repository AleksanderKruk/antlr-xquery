package com.github.akruk.antlrxquery.values;

import java.util.List;
import java.util.Map;

import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

public class XQueryBoolean extends XQueryValueBase<Boolean> {

    public XQueryBoolean(final boolean bool, final XQueryValueFactory valueFactory) {
        super(bool, valueFactory);
    }

    @Override
    public Boolean effectiveBooleanValue() {
        return value;
    }

    @Override
    public Boolean booleanValue() {
        return value;
    }

    @Override
    public String stringValue() {
        return value ? "true" : "false";
    }

    @Override
    public String toString() {
        return "<XQueryBoolean:" + stringValue() + "/>";
    }

    @Override
    public XQueryValue not() {
        return valueFactory.bool(!value);
    }

    @Override
    public XQueryValue and(final XQueryValue other) {
        if (!other.isBooleanValue()) return XQueryError.InvalidArgumentType;
        return valueFactory.bool(value && other.booleanValue());
    }

    @Override
    public XQueryValue or(final XQueryValue other) {
        if (!other.isBooleanValue()) return XQueryError.InvalidArgumentType;
        return valueFactory.bool(value || other.booleanValue());
    }

    @Override
    public XQueryValue valueEqual(final XQueryValue other) {
        if (!other.isBooleanValue()) return valueFactory.bool(false);
        return valueFactory.bool(value.equals(other.booleanValue()));
    }

    @Override
    public XQueryValue valueLessThan(final XQueryValue other) {
        if (!other.isBooleanValue()) return XQueryError.InvalidArgumentType;
        return valueFactory.bool(!value && other.booleanValue());
    }

    @Override
    public XQueryValue data() {
        return valueFactory.sequence(atomize());
    }

    @Override
    public XQueryValue empty() {
        return valueFactory.bool(false);
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
        return null;
    }

}
