package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.Collection;
import java.util.List;

import com.github.akruk.antlrxquery.typesystem.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQueryChoiceItemType extends XQueryEnumItemType {
    public XQueryChoiceItemType(List<XQueryItemType> itemTypes, XQueryTypeFactory factory) {
        super(XQueryTypes.CHOICE, null, null, null, null, null, null, factory, itemTypes);
    }


}
