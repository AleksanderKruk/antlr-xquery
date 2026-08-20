package com.github.akruk.antlrquery.typesystem.types.itemtypes;

import com.github.akruk.antlrquery.typesystem.typeoperations.stringify.Stringify;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.regex.Pattern;

public record RegexType(Pattern pattern)
        implements AtomicType {
    @Override
    public @NonNull String toString() {
        return Stringify.stringify(this);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof com.github.akruk.antlrquery.typesystem.types.itemtypes.RegexType(Pattern pattern1)
                && pattern1.equals(this.pattern);
    }
}
