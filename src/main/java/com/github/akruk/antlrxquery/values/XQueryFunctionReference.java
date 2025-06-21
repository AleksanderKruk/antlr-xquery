package com.github.akruk.antlrxquery.values;

import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

public class XQueryFunctionReference extends XQueryValueBase<XQueryFunction> {

    public XQueryFunctionReference(XQueryFunction xQueryFunction, XQueryValueFactory valueFactory) {
        super(xQueryFunction, valueFactory);
    }

    @Override
    public XQueryValue valueEqual(XQueryValue other) {
        return valueFactory.bool(value == other.functionValue());
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
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public XQueryValue copy() {
        return valueFactory.functionReference(value);
    }

    @Override
    public XQueryValue empty() {
        return valueFactory.bool(false);
    }
}
