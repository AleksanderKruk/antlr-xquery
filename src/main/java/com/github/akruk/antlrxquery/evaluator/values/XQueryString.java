package com.github.akruk.antlrxquery.evaluator.values;

import java.util.List;
import java.util.Map;

import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;

public class XQueryString extends XQueryValueBase<String> {
    public XQueryString(String string, XQueryValueFactory valueFactory) {
        super(string, valueFactory);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof XQueryString)) {
            return false;
        }
        var str = (XQueryString) obj;
        return value.equals(str.value);
    }

    @Override
    public String stringValue() {
        return value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("<XQueryString:");
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
    public XQueryValue data() {
        var atomized = atomize();
        return valueFactory.sequence(atomized);
    }

    @Override
    public List<XQueryValue> arrayMembers() {
        return null;
    }

    @Override
    public Map<XQueryValue, XQueryValue> mapEntries() {
        return null;
    }
}
