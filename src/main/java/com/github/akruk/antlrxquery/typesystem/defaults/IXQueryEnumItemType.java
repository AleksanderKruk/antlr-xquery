package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.github.akruk.antlrxquery.typesystem.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;

public interface IXQueryEnumItemType extends XQueryItemType {
    Collection<XQueryItemType> getItemTypes();
    XQuerySequenceType getArrayType();
    XQueryItemType getMapKeyType();
    XQuerySequenceType getMapValueType();
    List<XQuerySequenceType> getArgumentTypes();
    XQuerySequenceType getReturnedType();
    Set<String> getElementNames();
    XQueryTypes getType();
}
