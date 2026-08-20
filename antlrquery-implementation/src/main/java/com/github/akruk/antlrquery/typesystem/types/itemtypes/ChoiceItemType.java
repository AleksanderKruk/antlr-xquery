package com.github.akruk.antlrquery.typesystem.types.itemtypes;

import com.github.akruk.antlrquery.typesystem.typeoperations.stringify.Stringify;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * ChoiceItemType
 */
public record ChoiceItemType(ConcreteItemType[] itemTypes)
    implements AntlrQueryItemType 
{
    @Override
    public @NonNull String toString() {
        return Stringify.stringify(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ChoiceItemType that = (ChoiceItemType) o;
        return itemTypes.length == that.itemTypes.length
                && Stream.of(itemTypes).allMatch(
                        concreteItemType -> Arrays.asList(that.itemTypes).contains(concreteItemType)
                );
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(itemTypes);
    }
}
