package com.github.akruk.antlrxquery.values;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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


    public XQuerySequence() {
        value = Collections.emptyList();
    }

    @Override
    public List<XQueryValue> atomize() {
        List<XQueryValue> result = new ArrayList<>();
        for (XQueryValue element : value) {
            if (element.isAtomic()) {
                result.add(element);
            }
            // If the result is not atomic we atomize it
            // and extend the result list
            var atomizedValues = element.atomize();
            result.addAll(atomizedValues);
        }
        return result;
    }


}
