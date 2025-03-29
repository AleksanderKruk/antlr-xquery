package typesystem;

public interface XQueryType {
    @Override
    boolean equals(Object obj);
    // subtype(A, B) judgement
    boolean isSubtypeOf(Object obj);
    // subtype-itemtype(A, B) judgement
    boolean isSubtypeItemtypeOf(Object obj);
    // subtype-assertions(A, B) judgement
    // boolean isSubtypeItemtype(Object obj);
}
