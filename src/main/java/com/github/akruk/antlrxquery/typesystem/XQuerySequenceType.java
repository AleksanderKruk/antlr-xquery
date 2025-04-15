package com.github.akruk.antlrxquery.typesystem;

public interface XQuerySequenceType {
    // subtype(A, B) judgement
    XQueryItemType getItemType();
    XQuerySequenceType sequenceMerge(XQuerySequenceType other);
    boolean isSubtypeOf(XQuerySequenceType obj);
    boolean itemtypeIsSubtypeOf(XQuerySequenceType obj);
    boolean isOne();
    boolean isOneOrMore();
    boolean isZeroOrMore();
    boolean isZeroOrOne();
    boolean isZero();
    boolean hasEffectiveBooleanValue();
}
