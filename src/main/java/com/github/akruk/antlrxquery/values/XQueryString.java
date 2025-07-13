package com.github.akruk.antlrxquery.values;

import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

public class XQueryString extends XQueryValueBase<String> {
    public XQueryString(String string, XQueryValueFactory valueFactory) {
        super(string, valueFactory);
    }

    @Override
    public String stringValue() {
        return value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("<");
        sb.append(super.toString());
        sb.append(":");
        sb.append(value);
        sb.append("/>");
        return sb.toString();
    }


    @Override
    public Boolean effectiveBooleanValue() {
        return !value.isEmpty();
    }

    @Override
    public XQueryValue concatenate(XQueryValue other) {
        try {
            StringBuilder builder = new StringBuilder(value);
            for (var element : other.atomize()) {
                builder.append(element.stringValue());
            }
            return valueFactory.string(builder.toString());
        } catch (Exception e) {
            return XQueryError.InvalidArgumentType;
        }
    }

    @Override
    public XQueryValue valueEqual(XQueryValue other) {
        if (!other.isStringValue()) return XQueryError.InvalidArgumentType;
        return valueFactory.bool(value.equals(other.stringValue()));
    }

    @Override
    public XQueryValue valueLessThan(XQueryValue other) {
        if (!other.isStringValue()) return XQueryError.InvalidArgumentType;
        return valueFactory.bool(value.compareTo(other.stringValue()) < 0);
    }

    @Override
    public XQueryValue valueGreaterThan(XQueryValue other) {
        if (!other.isStringValue()) return XQueryError.InvalidArgumentType;
        return valueFactory.bool(value.compareTo(other.stringValue()) > 0);
    }

    @Override
    public XQueryValue valueGreaterEqual(XQueryValue other) {
        if (!other.isStringValue()) return XQueryError.InvalidArgumentType;
        return valueFactory.bool(value.compareTo(other.stringValue()) >= 0);
    }

    @Override
    public XQueryValue valueLessEqual(XQueryValue other) {
        if (!other.isStringValue()) return XQueryError.InvalidArgumentType;
        return valueFactory.bool(value.compareTo(other.stringValue()) <= 0);
    }

    @Override
    public XQueryValue empty() {
        return valueFactory.bool(false);
    }

    @Override
    public XQueryValue head() {
        if (value.isEmpty())
            return valueFactory.emptySequence();
        return valueFactory.string(value.substring(0, 1));
    }

    @Override
    public XQueryValue tail() {
        if (value.isEmpty())
            return valueFactory.emptySequence();
        return valueFactory.string(value.substring(1));
    }

    @Override
    public XQueryValue data() {
        var atomized = atomize();
        return valueFactory.sequence(atomized);
    }
}
