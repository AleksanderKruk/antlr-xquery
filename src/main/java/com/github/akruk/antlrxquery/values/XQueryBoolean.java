package com.github.akruk.antlrxquery.values;

import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;

public class XQueryBoolean extends XQueryValueBase<Boolean> {
    public static final XQueryBoolean TRUE = new XQueryBoolean(true);
    public static final XQueryBoolean FALSE = new XQueryBoolean(false);

    public XQueryBoolean(boolean bool) {
        value = bool;
    }

    @Override
    public Boolean booleanValue() {
        return value;
    }

    @Override
    public XQueryValue not(XQueryValue other) throws XQueryUnsupportedOperation {
        return new XQueryBoolean(!booleanValue());
    }

    @Override
    public XQueryValue and(XQueryValue other) throws XQueryUnsupportedOperation {
        return new XQueryBoolean(booleanValue() && other.booleanValue());
    }

    @Override
    public XQueryValue or(XQueryValue other) throws XQueryUnsupportedOperation {
        return new XQueryBoolean(booleanValue() || other.booleanValue());
    }

    @Override
    public XQueryValue valueEqual(XQueryValue other) throws XQueryUnsupportedOperation {
        return new XQueryBoolean(booleanValue() == booleanValue());
    }
}
