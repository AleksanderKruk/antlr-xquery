package com.github.akruk.antlrxquery.values;

import java.util.regex.Pattern;

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
    public Boolean effectiveBooleanValue() {
        return value.isEmpty();
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
    public XQueryValue empty() {
        return XQueryBoolean.FALSE;
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

    @Override
    public XQueryValue contains(XQueryValue other) throws XQueryUnsupportedOperation {
        if (value.isEmpty())
            return XQueryBoolean.FALSE;
        if (other.stringValue().isEmpty())
            return XQueryBoolean.TRUE;
        return XQueryBoolean.of(value.contains(other.stringValue()));
    }

    @Override
    public XQueryValue startsWith(XQueryValue other) throws XQueryUnsupportedOperation {
        if (value.isEmpty())
            return XQueryBoolean.FALSE;
        if (other.stringValue().isEmpty())
            return XQueryBoolean.TRUE;
        return XQueryBoolean.of(value.startsWith(other.stringValue()));
    }


    @Override
    public XQueryValue endsWith(XQueryValue other) throws XQueryUnsupportedOperation {
        if (value.isEmpty())
            return XQueryBoolean.FALSE;
        if (other.stringValue().isEmpty())
            return XQueryBoolean.TRUE;
        return XQueryBoolean.of(value.endsWith(other.stringValue()));
    }

    @Override
    public XQueryValue substringBefore(XQueryValue splitstring) throws XQueryUnsupportedOperation {
        if (splitstring.empty().booleanValue())
            return new XQueryString("");
        var escapedSplitstring = Pattern.quote(splitstring.stringValue());
        String[] splitString = value.split(escapedSplitstring, 2);
        if (splitString.length == 1)
            return new XQueryString("");
        return new XQueryString(splitString[0]);
    }

    @Override
    public XQueryValue substringAfter(XQueryValue splitstring) throws XQueryUnsupportedOperation {
        if (splitstring.empty().booleanValue())
            return new XQueryString("");
        var splitstringValue = splitstring.stringValue();
        var escapedSplitstring = Pattern.quote(splitstringValue);
        String[] splitString = value.split(escapedSplitstring, 2);
        if (splitString.length == 1)
            return new XQueryString("");
        return new XQueryString(splitString[1]);
    }
}
