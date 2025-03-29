package typesystem.defaults;

public enum XQueryTypes {
    BOOLEAN,
    NODE,
    STRING,
    NUMBER,
    INTEGER,
    FUNCTION,
    EMPTY_SEQUENCE,
    SEQUENCE,
    MAP,
    ARRAY;
    boolean isAtomic() {
        return !isContainer();
    }

    boolean isContainer() {
        return this == SEQUENCE
            || this == EMPTY_SEQUENCE
            || this == ARRAY
            || this == MAP;
    }
}
