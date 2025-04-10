package com.github.akruk.antlrxquery.typesystem;

import java.util.List;

public interface XQuerySequenceType {
    // subtype(A, B) judgement
    boolean isSubtypeOf(XQuerySequenceType obj);
    // subtype-itemtype(A, B) judgement
    boolean isSubtypeItemtypeOf(XQuerySequenceType obj);
    boolean isOne();
    boolean isOneOrMore();
    boolean isZeroOrMore();
    boolean isZeroOrOne();
    boolean isZero();
}
