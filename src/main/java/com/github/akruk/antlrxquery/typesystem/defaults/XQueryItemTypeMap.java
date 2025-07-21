package com.github.akruk.antlrxquery.typesystem.defaults;

import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQueryItemTypeMap extends XQueryItemType {

    public XQueryItemTypeMap(XQueryItemType key,
                                    XQuerySequenceType value,
                                    XQueryTypeFactory factory)
    {
        super(XQueryTypes.MAP, null, null, null, key, value, null, factory, null, null);
    }


}
