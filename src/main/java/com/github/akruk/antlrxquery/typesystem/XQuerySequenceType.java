package com.github.akruk.antlrxquery.typesystem;

public interface XQuerySequenceType {
    XQueryItemType getItemType();
    XQueryItemType getMapKeyType();
    XQuerySequenceType getMapValueType();
    XQuerySequenceType getArrayMemberType();
    XQuerySequenceType sequenceMerge(XQuerySequenceType other);
    XQuerySequenceType unionMerge(XQuerySequenceType other);
    XQuerySequenceType intersectionMerge(XQuerySequenceType other);
    XQuerySequenceType exceptionMerge(XQuerySequenceType other);
    XQuerySequenceType alternativeMerge(XQuerySequenceType other);
    XQuerySequenceType addOptionality();
    XQuerySequenceType iteratedItem();
    XQuerySequenceType mapping(XQuerySequenceType mappingExpressionType);
    // subtype(A, B) judgement
    boolean isSubtypeOf(XQuerySequenceType obj);
    boolean itemtypeIsSubtypeOf(XQuerySequenceType obj);
    boolean isOne();
    boolean isOneOrMore();
    boolean isZeroOrMore();
    boolean isZeroOrOne();
    boolean isZero();
    boolean hasEffectiveBooleanValue();
    boolean castableAs(XQuerySequenceType other);
    boolean isValueComparableWith(XQuerySequenceType other);

    public enum RelativeCoercability {
        ALWAYS, POSSIBLE, NEVER
    }
    RelativeCoercability coerceableTo(XQuerySequenceType desiredType);
}
