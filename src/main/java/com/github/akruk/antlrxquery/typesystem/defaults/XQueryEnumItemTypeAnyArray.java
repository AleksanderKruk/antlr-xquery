package com.github.akruk.antlrxquery.typesystem.defaults;

import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQueryEnumItemTypeAnyArray extends XQueryEnumItemType {
    final private XQueryEnumSequenceType anyItems;

    public XQueryEnumItemTypeAnyArray(XQueryTypeFactory factory) {
        super(XQueryTypes.ANY_ARRAY, null, null, null, null, null, null, factory, null);
        anyItems = new XQueryEnumSequenceType(factory, new XQueryEnumItemTypeAnyItem(factory), XQueryOccurence.ZERO_OR_MORE);
    }

    @Override
    public String toString() {
        return "array(*)";
    }

    @Override
    public XQuerySequenceType getArrayMemberType() {
        return anyItems;
    }

}
