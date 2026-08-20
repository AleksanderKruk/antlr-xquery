package com.github.akruk.antlrquery.typesystem.types.itemtypes;

import com.github.akruk.antlrquery.typesystem.typeoperations.stringify.Stringify;
import org.checkerframework.checker.nullness.qual.NonNull;

public record AnyItemType() implements AntlrQueryItemType {

    @Override
    public @NonNull String toString() {
        return Stringify.stringify(this);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AnyItemType;
    }
}