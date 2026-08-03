package com.github.akruk.antlrquery.typesystem.types.itemtypes;

import com.github.akruk.antlrquery.typesystem.types.ItemTypes;
import org.checkerframework.checker.nullness.qual.NonNull;

public record NeverType() implements AntlrQueryItemType {
    @Override
    public @NonNull String toString() {
        return ItemTypes.stringify(this);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NeverType;
    }
}