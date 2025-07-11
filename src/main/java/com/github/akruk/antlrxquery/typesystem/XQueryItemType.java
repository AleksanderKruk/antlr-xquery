package com.github.akruk.antlrxquery.typesystem;

import java.util.List;

public interface XQueryItemType {
    boolean isFunction(XQuerySequenceType returnedType, List<XQuerySequenceType> argumentTypes);
    boolean hasEffectiveBooleanValue();
    // subtype-itemtype(A, B) judgement
    boolean itemtypeIsSubtypeOf(XQueryItemType obj);
    boolean castableAs(XQueryItemType other);
    XQueryItemType alternativeMerge(XQueryItemType other);
    XQueryItemType unionMerge(XQueryItemType other);
    XQueryItemType intersectionMerge(XQueryItemType other);
    XQueryItemType exceptionMerge(XQueryItemType other);
    boolean isValueComparableWith(XQueryItemType other);
}
