package com.github.akruk.antlrxquery.values;

import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;

public class XQueryString  extends XQueryValueBase<String> {
    public XQueryString(String string) {
        value = string;
    }

    @Override
    public String stringValue() {
        return value;
    }

    @Override
    public XQueryValue concatenate(XQueryValue other) {
        return new XQueryString(value + other.stringValue());
    }

    @Override
    public XQueryValue valueEqual(XQueryValue other) {
        return XQueryBoolean.of(value.compareTo(other.stringValue()) == 0);
    }

    @Override
    public XQueryValue valueLessThan(XQueryValue other) {
        return XQueryBoolean.of(value.compareTo(other.stringValue()) < 0);
    }

    @Override
    public XQueryValue valueGreaterThan(XQueryValue other) throws XQueryUnsupportedOperation {
        return XQueryBoolean.of(value.compareTo(other.stringValue()) > 0);
    }

    @Override
    public XQueryValue valueGreaterEqual(XQueryValue other) throws XQueryUnsupportedOperation {
        return XQueryBoolean.of(value.compareTo(other.stringValue()) >= 0);
    }

    @Override
    public XQueryValue valueLessEqual(XQueryValue other) throws XQueryUnsupportedOperation {
        return XQueryBoolean.of(value.compareTo(other.stringValue()) <= 0);
    }

    @Override
    public XQueryValue copy() {
        return new XQueryString(value);
    }


}
