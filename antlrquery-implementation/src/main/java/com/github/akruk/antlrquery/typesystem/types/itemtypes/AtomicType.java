package com.github.akruk.antlrquery.typesystem.types.itemtypes;

import com.github.akruk.antlrquery.typesystem.types.ItemTypes;
import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.regex.Pattern;

sealed public interface AtomicType extends ConcreteItemType
        permits
        AtomicType.NumberType,
        StringType,
        BooleanType,
        AtomicType.RegexType
{

    record NumberType(NumericRange range)
            implements com.github.akruk.antlrquery.typesystem.types.itemtypes.AtomicType {
        @Override
        public @NonNull String toString() {
            return ItemTypes.stringify(this);
        }

    }


    record RegexType(Pattern pattern)
            implements com.github.akruk.antlrquery.typesystem.types.itemtypes.AtomicType {
        @Override
        public @NonNull String toString() {
            return ItemTypes.stringify(this);
        }

    }

}
