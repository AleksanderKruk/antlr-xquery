package com.github.akruk.antlrxquery.typesystem.defaults;

import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQueryItemTypeArray extends XQueryItemType {

    private final XQuerySequenceType arrayType;
    public XQueryItemTypeArray(XQuerySequenceType containedType, XQueryTypeFactory factory) {
        super(XQueryTypes.ARRAY, null, null, null, null, null, null, factory, null);
        arrayType = containedType;
    }

    @Override
    public XQuerySequenceType getArrayMemberType() {
        return arrayType;
    }

    @Override
    public String toString() {
        return "array(" + getArrayMemberType() + ")";
    }
}
