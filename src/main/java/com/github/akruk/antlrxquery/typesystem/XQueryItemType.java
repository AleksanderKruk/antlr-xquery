package com.github.akruk.antlrxquery.typesystem;

import java.util.List;

public interface XQueryItemType {
    boolean isAtomic();
    boolean isNode();
    boolean isElement();
    boolean isElement(String otherName);
    boolean isFunction();
    boolean isFunction(String otherName, XQuerySequenceType returnedType, List<XQuerySequenceType> argumentTypes);
    boolean isMap();
    boolean isArray();
    boolean hasEffectiveBooleanValue();
    // subtype-itemtype(A, B) judgement
    boolean itemtypeIsSubtypeOf(XQueryItemType obj);
}
