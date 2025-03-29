package typesystem.defaults;

public enum XQueryTypes {
    EMPTY_SEQUENCE,
    BOOLEAN,
    NODE,
    STRING,
    NUMBER,
    INTEGER,
    FUNCTION,
    SEQUENCE,
    MAP,
    ARRAY;
    boolean isAtomic() {
        return !isContainer();
    }

    boolean isContainer() {
        return this == SEQUENCE
            || this == ARRAY
            || this == MAP;
    }
}
