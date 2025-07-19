package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.Collection;

import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQueryChoiceItemType extends XQueryItemType {
    public XQueryChoiceItemType(Collection<XQueryItemType> itemTypes, XQueryTypeFactory factory) {
        super(XQueryTypes.CHOICE, null, null, null, null, null, null, factory, itemTypes);
    }

    @Override
    public String toString() {
        return String.join(" | ", getItemTypes().stream().map(Object::toString).toArray(String[]::new));
    }


}
