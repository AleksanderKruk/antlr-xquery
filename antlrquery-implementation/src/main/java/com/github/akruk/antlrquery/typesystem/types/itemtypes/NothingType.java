package com.github.akruk.antlrquery.typesystem.types.itemtypes;

import com.github.akruk.antlrquery.typesystem.types.ItemTypes;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public record NothingType() implements AntlrQueryItemType {
    @Override
    public String toString() {
        return ItemTypes.stringify(this);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NothingType;
    }
}