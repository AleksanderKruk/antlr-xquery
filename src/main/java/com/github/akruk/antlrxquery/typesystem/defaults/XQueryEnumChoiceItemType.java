package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.Collection;

import com.github.akruk.antlrxquery.typesystem.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQueryEnumChoiceItemType extends XQueryEnumItemType {
    public XQueryEnumChoiceItemType(Collection<XQueryItemType> itemTypes, XQueryTypeFactory factory) {
        super(XQueryTypes.CHOICE, null, null, null, null, null, null, factory, itemTypes);
    }


}
