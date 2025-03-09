package com.github.akruk.antlrxquery.values;

import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;

public class XQueryBoolean extends XQueryValueBase<Boolean> {
    public static final XQueryBoolean TRUE = new XQueryBoolean(true);
    public static final XQueryBoolean FALSE = new XQueryBoolean(false);

    private XQueryBoolean(boolean bool) {
        value = bool;
    }

    public static XQueryBoolean of(boolean bool) {
        if (bool)
            return TRUE;
        else
            return FALSE;
    }


    @Override
    public Boolean booleanValue() {
        return value;
    }

    @Override
    public XQueryValue not() throws XQueryUnsupportedOperation {
        return new XQueryBoolean(!value);
    }

    @Override
    public XQueryValue and(XQueryValue other) throws XQueryUnsupportedOperation {
        return new XQueryBoolean(value && other.booleanValue());
    }

    @Override
    public XQueryValue or(XQueryValue other) throws XQueryUnsupportedOperation {
        return new XQueryBoolean(value || other.booleanValue());
    }

    @Override
    public XQueryValue valueEqual(XQueryValue other) throws XQueryUnsupportedOperation {
        return new XQueryBoolean(value == booleanValue());
    }


    @Override
    public XQueryValue copy() {
        return this;
    }
}
