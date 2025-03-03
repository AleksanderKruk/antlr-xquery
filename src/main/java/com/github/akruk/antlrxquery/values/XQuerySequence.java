package com.github.akruk.antlrxquery.values;

import java.util.Arrays;
import java.util.List;

public class XQuerySequence extends XQueryValueBase<List<XQueryValue>> {
    @Override
    public List<XQueryValue> sequence() {
        return value;
    }

    public XQuerySequence(List<XQueryValue> list) {
        value = list;
    }

    public XQuerySequence(XQueryValue... values) {
        value = Arrays.asList(values);
    }


}
