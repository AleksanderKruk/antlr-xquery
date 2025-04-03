package com.github.akruk.antlrxquery.typesystem;

import java.util.List;

public interface XQueryType {
    @Override
    boolean equals(Object obj);
    // subtype(A, B) judgement
    boolean isSubtypeOf(XQueryType obj);
    // subtype-itemtype(A, B) judgement
    boolean isSubtypeItemtypeOf(XQueryType obj);
    boolean isAtomic();
    boolean isNode();
    boolean isElement();
    boolean isElement(String otherName);
    boolean isFunction();
    boolean isFunction(String otherName, XQueryType returnedType, List<XQueryType> argumentTypes);
    boolean isMap();
    boolean isArray();
}
