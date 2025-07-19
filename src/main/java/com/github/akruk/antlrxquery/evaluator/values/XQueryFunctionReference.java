package com.github.akruk.antlrxquery.evaluator.values;

import java.util.List;
import java.util.Map;

import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;

public class XQueryFunctionReference extends XQueryValueBase<XQueryFunction> {

    public XQueryFunctionReference(XQueryFunction xQueryFunction, XQueryValueFactory valueFactory) {
        super(xQueryFunction, valueFactory);
    }

    @Override
    public XQueryValue valueEqual(XQueryValue other) {
        return valueFactory.bool(value == other.functionValue());
    }



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("<");
        sb.append(super.toString());
        sb.append(":");
        sb.append(value.toString());
        sb.append("/>");
        return sb.toString();
    }


    @Override
    public XQueryFunction functionValue() {
        return value;
    }

    @Override
    public boolean isFunction() {
        return true;
    }

    @Override
    public XQueryValue valueLessThan(XQueryValue other) {
        return null;
    }
    @Override
    public XQueryValue empty() {
        return valueFactory.bool(false);
    }

    @Override
    public boolean isEmptySequence() {
        return false;
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
