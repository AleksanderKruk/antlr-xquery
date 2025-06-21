package com.github.akruk.antlrxquery.values;

import java.util.regex.Pattern;

import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;
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
        sb.append(">");
        return sb.toString();
    }


    @Override
    public Boolean effectiveBooleanValue() {
        return !value.isEmpty();
    }
    @Override
    public XQueryValue concatenate(XQueryValue other) {
        if (other.isSequence()) {
            StringBuilder builder = new StringBuilder(value);
            for (var element : other.atomize()) {
                builder.append(element.stringValue());
            }
            return valueFactory.string(builder.toString());
        }
        return valueFactory.string(value + other.stringValue());
    }

    @Override
    public XQueryValue valueEqual(XQueryValue other) {
        if (!other.isStringValue()) {
            return valueFactory.bool(false);
        }
        return valueFactory.bool(value.compareTo(other.stringValue()) == 0);
    }

    @Override
    public XQueryValue valueLessThan(XQueryValue other) {
        if (!other.isStringValue()) {
            return valueFactory.bool(false);
        }
        return valueFactory.bool(value.compareTo(other.stringValue()) < 0);
    }

    @Override
    public XQueryValue valueGreaterThan(XQueryValue other) {
        if (!other.isStringValue()) {
            return valueFactory.bool(false);
        }
        return valueFactory.bool(value.compareTo(other.stringValue()) > 0);
    }

    @Override
    public XQueryValue valueGreaterEqual(XQueryValue other) {
        if (!other.isStringValue()) {
            return valueFactory.bool(false);
        }
        return valueFactory.bool(value.compareTo(other.stringValue()) >= 0);
    }

    @Override
    public XQueryValue valueLessEqual(XQueryValue other) {
        if (!other.isStringValue()) {
            return valueFactory.bool(false);
        }
        return valueFactory.bool(value.compareTo(other.stringValue()) <= 0);
    }

    @Override
    public XQueryValue copy() {
        return valueFactory.string(value);
    }

    @Override
    public XQueryValue empty() {
        return valueFactory.bool(false);
    }

    @Override
    public XQueryValue head() throws XQueryUnsupportedOperation {
        if (value.isEmpty())
            return valueFactory.emptySequence();
        return valueFactory.string(value.substring(0, 1));
    }

    @Override
    public XQueryValue tail() throws XQueryUnsupportedOperation {
        if (value.isEmpty())
            return valueFactory.emptySequence();
        return valueFactory.string(value.substring(1));
    }

    @Override
    public XQueryValue data() throws XQueryUnsupportedOperation {
        var atomized = atomize();
        return valueFactory.sequence(atomized);
    }

    @Override
    public XQueryValue contains(XQueryValue other) throws XQueryUnsupportedOperation {
        if (value.isEmpty())
            return valueFactory.bool(false);
        if (other.stringValue().isEmpty())
            return valueFactory.bool(true);
        return valueFactory.bool(value.contains(other.stringValue()));
    }

    @Override
    public XQueryValue startsWith(XQueryValue other) throws XQueryUnsupportedOperation {
        if (value.isEmpty())
            return valueFactory.bool(false);
        if (other.stringValue().isEmpty())
            return valueFactory.bool(true);
        return valueFactory.bool(value.startsWith(other.stringValue()));
    }


    @Override
    public XQueryValue endsWith(XQueryValue other) throws XQueryUnsupportedOperation {
        if (value.isEmpty())
            return valueFactory.bool(false);
        if (other.stringValue().isEmpty())
            return valueFactory.bool(true);
        return valueFactory.bool(value.endsWith(other.stringValue()));
    }

    @Override
    public XQueryValue substring(int startingLoc) throws XQueryUnsupportedOperation {
        return substring(startingLoc, value.length()-startingLoc+1);
    }

    @Override
    public XQueryValue substring(int startingLoc, int length) throws XQueryUnsupportedOperation {
        int currentLength = value.length();
        if (startingLoc > currentLength) {
            return valueFactory.emptySequence();
        }
        int startIndexIncluded = Math.max(startingLoc - 1, 0);
        int endIndexExcluded = Math.min(startingLoc + length - 1, currentLength);
        String newSequence = value.substring(startIndexIncluded, endIndexExcluded);
        return valueFactory.string(newSequence);
    }



    @Override
    public XQueryValue substringBefore(XQueryValue splitstring) throws XQueryUnsupportedOperation {
        if (splitstring.empty().booleanValue())
            return valueFactory.emptyString();
        var escapedSplitstring = Pattern.quote(splitstring.stringValue());
        String[] splitString = value.split(escapedSplitstring, 2);
        if (splitString.length == 1)
            return valueFactory.emptyString();
        return valueFactory.string(splitString[0]);
    }

    @Override
    public XQueryValue substringAfter(XQueryValue splitstring) throws XQueryUnsupportedOperation {
        if (splitstring.empty().booleanValue())
            return valueFactory.emptyString();
        var splitstringValue = splitstring.stringValue();
        var escapedSplitstring = Pattern.quote(splitstringValue);
        String[] splitString = value.split(escapedSplitstring, 2);
        if (splitString.length == 1)
            return valueFactory.emptyString();
        return valueFactory.string(splitString[1]);
    }

    @Override
    public XQueryValue uppercase() throws XQueryUnsupportedOperation {
        return valueFactory.string(value.toUpperCase());
    }

    @Override
    public XQueryValue lowercase() throws XQueryUnsupportedOperation {
        return valueFactory.string(value.toLowerCase());
    }


}
