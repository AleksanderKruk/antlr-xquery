package com.github.akruk.antlrxquery.typesystem.types.itemtypes;

import java.util.Set;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.github.akruk.antlrxquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrxquery.typesystem.types.Cardinality;

public sealed interface StringType 
    extends AtomicType
    permits StringType.StringEnum, StringType.StringNonEnum 
{

    record StringEnum(@NonNull Set<@NonNull String> members, @NonNull Cardinality cardinality)
        implements StringType 
    {
        public StringEnum(@NonNull Set<@NonNull String> enumValues) {
            this(enumValues, Cardinalities.union(
                enumValues.stream()
                .map(String::length)
                .map(Cardinality::of)
                .toArray(Cardinality[]::new))
            );
        }

        public Set<String> members() {
            return members;
        }
    }

    record StringNonEnum(Cardinality cardinality) implements StringType{
    }

    Cardinality cardinality();

}