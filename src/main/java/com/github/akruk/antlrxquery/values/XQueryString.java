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

    @Override
    public XQueryValue contains(XQueryValue other) {
        if (!other.isStringValue()) return XQueryError.InvalidArgumentType;
        var otherVal = other.stringValue();
        if (otherVal.isEmpty()) return valueFactory.bool(true);
        return valueFactory.bool(value.contains(otherVal));
    }

    @Override
    public XQueryValue startsWith(XQueryValue other) {
        if (value.isEmpty())
            return valueFactory.bool(false);
        if (other.stringValue().isEmpty())
            return valueFactory.bool(true);
        return valueFactory.bool(value.startsWith(other.stringValue()));
    }


    @Override
    public XQueryValue endsWith(XQueryValue other) {
        if (value.isEmpty())
            return valueFactory.bool(false);
        if (other.stringValue().isEmpty())
            return valueFactory.bool(true);
        return valueFactory.bool(value.endsWith(other.stringValue()));
    }

    @Override
    public XQueryValue substring(int startingLoc) {
        return substring(startingLoc, value.length()-startingLoc+1);
    }

    @Override
    public XQueryValue substring(int startingLoc, int length) {
        int currentLength = value.length();
        if (startingLoc <= 0 || length < 0) return XQueryError.ArrayIndexOutOfBounds;

        int start = startingLoc - 1;
        int end = Math.min(start + length, currentLength);
        if (start >= currentLength) return valueFactory.emptyString();

        try {
            return valueFactory.string(value.substring(start, end));
        } catch (IndexOutOfBoundsException e) {
            return XQueryError.ArrayIndexOutOfBounds;
        }
    }

    @Override
    public XQueryValue substringBefore(XQueryValue splitstring) {
        if (!splitstring.isStringValue()) return XQueryError.InvalidArgumentType;
        var needle = splitstring.stringValue();
        int index = value.indexOf(needle);
        if (index == -1) return valueFactory.emptyString();
        return valueFactory.string(value.substring(0, index));
    }

    @Override
    public XQueryValue substringAfter(XQueryValue splitstring) {
        if (!splitstring.isStringValue()) return XQueryError.InvalidArgumentType;
        var needle = splitstring.stringValue();
        int index = value.indexOf(needle);
        if (index == -1) return valueFactory.emptyString();
        return valueFactory.string(value.substring(index + needle.length()));
    }

    @Override
    public XQueryValue uppercase() {
        return valueFactory.string(value.toUpperCase());
    }

    @Override
    public XQueryValue lowercase() {
        return valueFactory.string(value.toLowerCase());
    }
}
