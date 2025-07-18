package com.github.akruk.antlrxquery.typesystem.defaults;

public enum XQueryOccurence {
    ONE,
    ZERO,
    ZERO_OR_ONE,
    ONE_OR_MORE,
    ZERO_OR_MORE;

    String occurenceSuffix() {
        return switch (this) {
            case ZERO -> "";
            case ONE -> "";
            case ZERO_OR_ONE -> "?";
            case ZERO_OR_MORE -> "*";
            case ONE_OR_MORE -> "+";
        };
    }
}
