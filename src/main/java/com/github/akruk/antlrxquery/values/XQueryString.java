package com.github.akruk.antlrxquery.values;

import java.util.regex.Pattern;

import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

public class XQueryString extends XQueryValueBase<String> {
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
    public XQueryValue concatenate(XQueryValueFactory valueFactory, XQueryValue other) {
        return valueFactory.string(value + other.stringValue());
    }

    @Override
    public XQueryValue valueEqual(XQueryValueFactory valueFactory, XQueryValue other) {
        if (!other.isStringValue()) {
            return XQueryBoolean.FALSE;
        }
        return XQueryBoolean.of(value.compareTo(other.stringValue()) == 0);
    }

    @Override
    public XQueryValue valueLessThan(XQueryValueFactory valueFactory, XQueryValue other) {
        if (!other.isStringValue()) {
            return XQueryBoolean.FALSE;
        }
        return XQueryBoolean.of(value.compareTo(other.stringValue()) < 0);
    }

    @Override
    public XQueryValue valueGreaterThan(XQueryValueFactory valueFactory, XQueryValue other) {
        if (!other.isStringValue()) {
            return XQueryBoolean.FALSE;
        }
        return XQueryBoolean.of(value.compareTo(other.stringValue()) > 0);
    }

    @Override
    public XQueryValue valueGreaterEqual(XQueryValueFactory valueFactory, XQueryValue other) {
        if (!other.isStringValue()) {
            return XQueryBoolean.FALSE;
        }
        return XQueryBoolean.of(value.compareTo(other.stringValue()) >= 0);
    }

    @Override
    public XQueryValue valueLessEqual(XQueryValueFactory valueFactory, XQueryValue other) {
        if (!other.isStringValue()) {
            return XQueryBoolean.FALSE;
        }
        return XQueryBoolean.of(value.compareTo(other.stringValue()) <= 0);
    }

    @Override
    public XQueryValue copy(XQueryValueFactory valueFactory) {
        return valueFactory.string(value);
    }

    @Override
    public XQueryValue empty(XQueryValueFactory valueFactory) {
        return XQueryBoolean.FALSE;
    }

    @Override
    public XQueryValue head(XQueryValueFactory valueFactory) throws XQueryUnsupportedOperation {
        if (value.isEmpty())
            return XQuerySequence.EMPTY;
        return valueFactory.string(value.substring(0, 1));
    }

    @Override
    public XQueryValue tail(XQueryValueFactory valueFactory) throws XQueryUnsupportedOperation {
        if (value.isEmpty())
            return XQuerySequence.EMPTY;
        return valueFactory.string(value.substring(1));
    }

    @Override
    public XQueryValue data(XQueryValueFactory valueFactory) throws XQueryUnsupportedOperation {
        var atomized = atomize();
        return valueFactory.sequence(atomized);
    }

    @Override
    public XQueryValue contains(XQueryValueFactory valueFactory, XQueryValue other) throws XQueryUnsupportedOperation {
        if (value.isEmpty())
            return XQueryBoolean.FALSE;
        if (other.stringValue().isEmpty())
            return XQueryBoolean.TRUE;
        return XQueryBoolean.of(value.contains(other.stringValue()));
    }

    @Override
    public XQueryValue startsWith(XQueryValueFactory valueFactory, XQueryValue other) throws XQueryUnsupportedOperation {
        if (value.isEmpty())
            return XQueryBoolean.FALSE;
        if (other.stringValue().isEmpty())
            return XQueryBoolean.TRUE;
        return XQueryBoolean.of(value.startsWith(other.stringValue()));
    }


    @Override
    public XQueryValue endsWith(XQueryValueFactory valueFactory, XQueryValue other) throws XQueryUnsupportedOperation {
        if (value.isEmpty())
            return XQueryBoolean.FALSE;
        if (other.stringValue().isEmpty())
            return XQueryBoolean.TRUE;
        return XQueryBoolean.of(value.endsWith(other.stringValue()));
    }

    @Override
    public XQueryValue substring(XQueryValueFactory valueFactory, int startingLoc) throws XQueryUnsupportedOperation {
        return substring(valueFactory, startingLoc, value.length()-startingLoc+1);
    }

    @Override
    public XQueryValue substring(XQueryValueFactory valueFactory, int startingLoc, int length) throws XQueryUnsupportedOperation {
        int currentLength = value.length();
        if (startingLoc > currentLength) {
            return XQuerySequence.EMPTY;
        }
        int startIndexIncluded = Math.max(startingLoc - 1, 0);
        int endIndexExcluded = Math.min(startingLoc + length - 1, currentLength);
        String newSequence = value.substring(startIndexIncluded, endIndexExcluded);
        return valueFactory.string(newSequence);
    }



    @Override
    public XQueryValue substringBefore(XQueryValueFactory valueFactory, XQueryValue splitstring) throws XQueryUnsupportedOperation {
        if (splitstring.empty(valueFactory).booleanValue())
            return valueFactory.string("");
        var escapedSplitstring = Pattern.quote(splitstring.stringValue());
        String[] splitString = value.split(escapedSplitstring, 2);
        if (splitString.length == 1)
            return valueFactory.string("");
        return valueFactory.string(splitString[0]);
    }

    @Override
    public XQueryValue substringAfter(XQueryValueFactory valueFactory, XQueryValue splitstring) throws XQueryUnsupportedOperation {
        if (splitstring.empty(valueFactory).booleanValue())
            return valueFactory.string("");
        var splitstringValue = splitstring.stringValue();
        var escapedSplitstring = Pattern.quote(splitstringValue);
        String[] splitString = value.split(escapedSplitstring, 2);
        if (splitString.length == 1)
            return valueFactory.string("");
        return valueFactory.string(splitString[1]);
    }

    @Override
    public XQueryValue uppercase(XQueryValueFactory valueFactory) throws XQueryUnsupportedOperation {
        return valueFactory.string(value.toUpperCase());
    }

    @Override
    public XQueryValue lowercase(XQueryValueFactory valueFactory) throws XQueryUnsupportedOperation {
        return valueFactory.string(value.toLowerCase());
    }


}
