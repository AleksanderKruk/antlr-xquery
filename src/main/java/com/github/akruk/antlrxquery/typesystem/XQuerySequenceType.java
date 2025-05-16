package com.github.akruk.antlrxquery.typesystem;

public interface XQuerySequenceType {
    XQueryItemType getItemType();
    XQuerySequenceType sequenceMerge(XQuerySequenceType other);
    XQuerySequenceType unionMerge(XQuerySequenceType other);
    XQuerySequenceType intersectionMerge(XQuerySequenceType other);
    XQuerySequenceType exceptionMerge(XQuerySequenceType other);
    // subtype(A, B) judgement
    boolean isSubtypeOf(XQuerySequenceType obj);
    boolean itemtypeIsSubtypeOf(XQuerySequenceType obj);
    boolean isOne();
    boolean isOneOrMore();
    boolean isZeroOrMore();
    boolean isZeroOrOne();
    boolean isZero();
    boolean hasEffectiveBooleanValue();
}
