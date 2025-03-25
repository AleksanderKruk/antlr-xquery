package com.github.akruk.antlrxquery.values;

import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

public class XQueryFunctionReference extends XQueryValueBase<XQueryFunction> {

    public XQueryFunctionReference(XQueryFunction xQueryFunction) {
        value = xQueryFunction;
    }

    @Override
    public XQueryValue valueEqual(XQueryValueFactory valueFactory, XQueryValue other) {
        return XQueryBoolean.of(value == other.functionValue());
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
    public XQueryValue valueLessThan(XQueryValueFactory valueFactory, XQueryValue other) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public XQueryValue copy(XQueryValueFactory valueFactory) {
        return valueFactory.functionReference(value);
    }

    @Override
    public XQueryValue empty(XQueryValueFactory valueFactory) {
        return valueFactory.bool(false);
    }
}
