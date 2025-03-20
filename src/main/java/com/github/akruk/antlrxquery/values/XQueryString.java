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
        if (!other.isStringValue()) {
            return XQueryBoolean.FALSE;
        }
        return XQueryBoolean.of(value.compareTo(other.stringValue()) == 0);
    }

    @Override
    public XQueryValue valueLessThan(XQueryValue other) {
        if (!other.isStringValue()) {
            return XQueryBoolean.FALSE;
        }
        return XQueryBoolean.of(value.compareTo(other.stringValue()) < 0);
    }

    @Override
    public XQueryValue valueGreaterThan(XQueryValue other) {
        if (!other.isStringValue()) {
            return XQueryBoolean.FALSE;
        }
        return XQueryBoolean.of(value.compareTo(other.stringValue()) > 0);
    }

    @Override
    public XQueryValue valueGreaterEqual(XQueryValue other) {
        if (!other.isStringValue()) {
            return XQueryBoolean.FALSE;
        }
        return XQueryBoolean.of(value.compareTo(other.stringValue()) >= 0);
    }

    @Override
    public XQueryValue valueLessEqual(XQueryValue other) {
        if (!other.isStringValue()) {
            return XQueryBoolean.FALSE;
        }
        return XQueryBoolean.of(value.compareTo(other.stringValue()) <= 0);
    }

    @Override
    public XQueryValue copy() {
        return new XQueryString(value);
    }

    @Override
    public XQueryValue empty() throws UnsupportedOperationException {
        return XQueryBoolean.of(value.isEmpty());
    }

    @Override
    public XQueryValue head() throws XQueryUnsupportedOperation {
        if (value.isEmpty())
            return XQuerySequence.EMPTY;
        return new XQueryString(value.substring(0, 1));
    }

    @Override
    public XQueryValue tail() throws XQueryUnsupportedOperation {
        if (value.isEmpty())
            return XQuerySequence.EMPTY;
        return new XQueryString(value.substring(1));
    }

    @Override
    public XQueryValue data() throws XQueryUnsupportedOperation {
        var atomized = atomize();
        return new XQuerySequence(atomized);
    }
}
