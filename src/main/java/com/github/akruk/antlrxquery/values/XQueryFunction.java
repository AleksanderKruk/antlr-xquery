package com.github.akruk.antlrxquery.values;

import java.util.List;

public interface XQueryFunction {
    public XQueryValue call(List<XQueryValue> values);
}