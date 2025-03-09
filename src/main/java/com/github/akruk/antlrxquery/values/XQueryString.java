package com.github.akruk.antlrxquery.values;

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
        return new XQueryString(stringValue() + other.stringValue());
    }

    @Override
    public XQueryValue valueEqual(XQueryValue other) {
        return XQueryBoolean.of(stringValue().equals(other.stringValue()));
    }

    @Override
    public XQueryValue valueLessThan(XQueryValue other) {
        return XQueryBoolean.of(stringValue().compareTo(other.stringValue()) == -1);
    }

    @Override
    public XQueryValue copy() {
        return new XQueryString(value);
    }


}
