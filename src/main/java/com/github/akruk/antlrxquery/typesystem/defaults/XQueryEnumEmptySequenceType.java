package com.github.akruk.antlrxquery.typesystem.defaults;

import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQueryEnumEmptySequenceType extends XQueryEnumSequenceType {
    public XQueryEnumEmptySequenceType(XQueryTypeFactory typeFactory) {
        super(typeFactory, null, XQueryOccurence.ZERO);
    }

    @Override
    public String toString() {
        return "empty-sequence()";
    }

}
