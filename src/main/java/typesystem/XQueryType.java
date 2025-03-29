package typesystem;

public interface XQueryType {
    @Override
    boolean equals(Object obj);
    // subtype(A, B) judgement
    boolean isSubtypeOf(XQueryType obj);
    // subtype-itemtype(A, B) judgement
    boolean isSubtypeItemtypeOf(XQueryType obj);
    // subtype-assertions(A, B) judgement
    // boolean isSubtypeAssertion(Object obj);
    boolean derivesFrom(XQueryType other);
    boolean isAtomicOrUnionTypes(XQueryType other);
    boolean isPureUnionType();
}
