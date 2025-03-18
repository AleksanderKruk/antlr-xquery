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
        return XQueryBoolean.of(!value);
    }

    @Override
    public XQueryValue and(XQueryValue other) throws XQueryUnsupportedOperation {
        return XQueryBoolean.of(value && other.booleanValue());
    }

    @Override
    public XQueryValue or(XQueryValue other) throws XQueryUnsupportedOperation {
        return XQueryBoolean.of(value || other.booleanValue());
    }

    @Override
    public XQueryValue valueEqual(XQueryValue other) throws XQueryUnsupportedOperation {
        // Identity comparison is used because
        // we maintain just 2 XQueryBoolean instances
        // TRUE and FALSE
        return XQueryBoolean.of(this == other);
    }

    @Override
    public XQueryValue valueLessThan(XQueryValue other) throws XQueryUnsupportedOperation {
        // Identity comparison is used because
        // we maintain just 2 XQueryBoolean instances
        // TRUE and FALSE
        return XQueryBoolean.of(this == FALSE && other != FALSE);
    }

    @Override
    public XQueryValue copy() {
        return this;
    }
}
