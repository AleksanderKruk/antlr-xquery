package com.github.akruk.antlrxquery.typesystem.defaults;

import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQueryEnumItemTypeArray extends XQueryEnumItemType {

    private final XQuerySequenceType arrayType;
    public XQueryEnumItemTypeArray(XQueryEnumSequenceType containedType, XQueryTypeFactory factory) {
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
