package com.github.akruk.antlrxquery.values;

import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;
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
        return (value)? "true" : "false";
    }

    @Override
    public XQueryValue not() throws XQueryUnsupportedOperation {
        return valueFactory.bool(!value);
    }

    @Override
    public XQueryValue and(XQueryValue other) throws XQueryUnsupportedOperation {
        return valueFactory.bool(value && other.booleanValue());
    }

    @Override
    public XQueryValue or(XQueryValue other) throws XQueryUnsupportedOperation {
        return valueFactory.bool(value || other.booleanValue());
    }

    @Override
    public XQueryValue valueEqual(XQueryValue other) {
        // Identity comparison is used because
        // we maintain just 2 XQueryBoolean instances
        // TRUE and FALSE
        return valueFactory.bool(this == other ||
                (other.isBooleanValue() && value.equals(other.booleanValue())));
    }

    @Override
    public XQueryValue valueLessThan(XQueryValue other) {
        // Identity comparison is used because
        // we maintain just 2 XQueryBoolean instances
        // TRUE and FALSE
        var false_ = valueFactory.bool(false);
        return valueFactory.bool(this == false_ && other != false_);
    }

    @Override
    public XQueryValue copy() {
        return this;
    }

    @Override
    public XQueryValue data() throws XQueryUnsupportedOperation {
        var atomized = atomize();
        return valueFactory.sequence(atomized);
    }

    @Override
    public XQueryValue empty() throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }


}
