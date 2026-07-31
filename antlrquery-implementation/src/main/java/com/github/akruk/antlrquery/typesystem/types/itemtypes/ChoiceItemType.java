package com.github.akruk.antlrquery.typesystem.types.itemtypes;

import com.github.akruk.antlrquery.typesystem.types.ItemTypes;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * ChoiceItemType
 */
public final record ChoiceItemType(ConcreteItemType[] itemTypes)
    implements AntlrQueryItemType 
{
    @Override
    public @NonNull String toString() {
        return ItemTypes.stringify(this);
    }

}
