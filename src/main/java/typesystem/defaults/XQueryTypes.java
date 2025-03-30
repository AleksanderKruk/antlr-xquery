package typesystem.defaults;

public enum XQueryTypes {
    ERROR,

    ANY_ITEM,
    ANY_NODE,

    ANY_ELEMENT,
    ELEMENT,

    ANY_MAP,
    MAP,

    ANY_ARRAY,
    ARRAY,

    ANY_FUNCTION,
    FUNCTION,

    EMPTY_SEQUENCE,
    SEQUENCE,

    BOOLEAN,
    NODE,
    STRING,
    NUMBER,
    INTEGER,
    ;

    boolean isAtomic() {
        return switch (this) {
            case BOOLEAN -> true;
            case NODE -> true;
            case STRING -> true;
            case NUMBER -> true;
            case INTEGER -> true;
            default -> false;
        };
    }

    boolean isContainer() {
        return switch (this) {
            case SEQUENCE -> true;
            case EMPTY_SEQUENCE -> true;
            case ARRAY -> true;
            case MAP -> true;
            default -> false;
        };
    }
}
