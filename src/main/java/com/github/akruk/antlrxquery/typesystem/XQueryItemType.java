package com.github.akruk.antlrxquery.typesystem;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface XQueryItemType {
    List<XQuerySequenceType> getArgumentTypes();
    Collection<XQueryItemType> getItemTypes();
    XQueryItemType getMapKeyType();
    XQuerySequenceType getMapValueType();
    XQuerySequenceType getArrayMemberType();
    XQuerySequenceType getReturnedType();
    Set<String> getElementNames();
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
    XQuerySequenceType lookup(XQuerySequenceType keySpecifierType);
}
