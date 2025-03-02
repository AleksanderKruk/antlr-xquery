package com.github.akruk.antlrxquery.result;

import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;

public class XQueryBoolean extends XQueryValueBase<Boolean> {
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
