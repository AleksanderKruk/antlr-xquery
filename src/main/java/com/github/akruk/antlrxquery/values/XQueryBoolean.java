package com.github.akruk.antlrxquery.values;

import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

public class XQueryBoolean extends XQueryValueBase<Boolean> {

    public XQueryBoolean(boolean bool, XQueryValueFactory valueFactory) {
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
    public XQueryValue and(XQueryValue other) {
        if (!other.isBooleanValue()) return XQueryError.InvalidArgumentType;
        return valueFactory.bool(value && other.booleanValue());
    }

    @Override
    public XQueryValue or(XQueryValue other) {
        if (!other.isBooleanValue()) return XQueryError.InvalidArgumentType;
        return valueFactory.bool(value || other.booleanValue());
    }

    @Override
    public XQueryValue valueEqual(XQueryValue other) {
        if (!other.isBooleanValue()) return valueFactory.bool(false);
        return valueFactory.bool(value.equals(other.booleanValue()));
    }

    @Override
    public XQueryValue valueLessThan(XQueryValue other) {
        if (!other.isBooleanValue()) return XQueryError.InvalidArgumentType;
        return valueFactory.bool(!value && other.booleanValue());
    }

    @Override
    public XQueryValue copy() {
        return this; // ponieważ jest niemutowalna
    }

    @Override
    public XQueryValue data() {
        return valueFactory.sequence(atomize());
    }

    @Override
    public XQueryValue empty() {
        return valueFactory.bool(false);
    }
}
