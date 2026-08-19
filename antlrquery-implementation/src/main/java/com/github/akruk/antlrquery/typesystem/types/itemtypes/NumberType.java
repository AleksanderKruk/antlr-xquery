package com.github.akruk.antlrquery.typesystem.types.itemtypes;

import com.github.akruk.antlrquery.typesystem.types.ItemTypes;
import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import org.checkerframework.checker.nullness.qual.NonNull;

public record NumberType(NumericRange range)
        implements AtomicType {
    @Override
    public @NonNull String toString() {
        return ItemTypes.stringify(this);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof com.github.akruk.antlrquery.typesystem.types.itemtypes.NumberType(NumericRange range1)
                && range1.equals(this.range);
    }
}
