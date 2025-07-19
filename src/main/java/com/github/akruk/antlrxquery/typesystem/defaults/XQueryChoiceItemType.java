package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.Collection;

import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

<<<<<<< HEAD:src/main/java/com/github/akruk/antlrxquery/typesystem/defaults/XQueryChoiceItemType.java
public class XQueryChoiceItemType extends XQueryItemType {
    public XQueryChoiceItemType(Collection<XQueryItemType> itemTypes, XQueryTypeFactory factory) {
=======
public class XQueryEnumChoiceItemType extends XQueryItemType {
    public XQueryEnumChoiceItemType(Collection<XQueryItemType> itemTypes, XQueryTypeFactory factory) {
>>>>>>> language-features/lookup-expression:src/main/java/com/github/akruk/antlrxquery/typesystem/defaults/XQueryEnumChoiceItemType.java
        super(XQueryTypes.CHOICE, null, null, null, null, null, null, factory, itemTypes);
    }

    @Override
    public String toString() {
        return String.join(" | ", getItemTypes().stream().map(Object::toString).toArray(String[]::new));
    }


}
