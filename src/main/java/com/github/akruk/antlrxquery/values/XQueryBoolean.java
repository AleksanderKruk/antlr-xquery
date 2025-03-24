package com.github.akruk.antlrxquery.values;

import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

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
    public Boolean effectiveBooleanValue() {
        return value;
    }

    @Override
    public Boolean booleanValue() {
        return value;
    }

    @Override
    public String stringValue() {
        return (value)? "true" : "false";
    }

    @Override
    public XQueryValue not(XQueryValueFactory valueFactory) throws XQueryUnsupportedOperation {
        return valueFactory.bool(!value);
    }

    @Override
    public XQueryValue and(XQueryValueFactory valueFactory, XQueryValue other) throws XQueryUnsupportedOperation {
        return valueFactory.bool(value && other.booleanValue());
    }

    @Override
    public XQueryValue or(XQueryValueFactory valueFactory, XQueryValue other) throws XQueryUnsupportedOperation {
        return valueFactory.bool(value || other.booleanValue());
    }

    @Override
    public XQueryValue valueEqual(XQueryValueFactory valueFactory, XQueryValue other) {
        // Identity comparison is used because
        // we maintain just 2 XQueryBoolean instances
        // TRUE and FALSE
        return XQueryBoolean.of(this == other);
    }

    @Override
    public XQueryValue valueLessThan(XQueryValueFactory valueFactory, XQueryValue other) {
        // Identity comparison is used because
        // we maintain just 2 XQueryBoolean instances
        // TRUE and FALSE
        return XQueryBoolean.of(this == FALSE && other != FALSE);
    }

    @Override
    public XQueryValue copy(XQueryValueFactory valueFactory) {
        return this;
    }

    @Override
    public XQueryValue data(XQueryValueFactory valueFactory) throws XQueryUnsupportedOperation {
        var atomized = atomize();
        return valueFactory.sequence(atomized);
    }

    @Override
    public XQueryValue empty(XQueryValueFactory valueFactory) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }


}
