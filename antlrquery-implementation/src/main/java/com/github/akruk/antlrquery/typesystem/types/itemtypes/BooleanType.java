package com.github.akruk.antlrquery.typesystem.types.itemtypes;

import com.github.akruk.antlrquery.typesystem.typeoperations.stringify.Stringify;
import org.checkerframework.checker.nullness.qual.NonNull;

sealed public interface BooleanType extends AtomicType
        permits BooleanType.True,
        BooleanType.False,
        BooleanType.Boolean {

    record True() implements com.github.akruk.antlrquery.typesystem.types.itemtypes.BooleanType {
        @Override
        public @NonNull String toString() {
            return Stringify.stringify(this);
        }
    }

    record False() implements com.github.akruk.antlrquery.typesystem.types.itemtypes.BooleanType {
        @Override
        public @NonNull String toString() {
            return Stringify.stringify(this);
        }
    }

    record Boolean() implements com.github.akruk.antlrquery.typesystem.types.itemtypes.BooleanType {
        @Override
        public @NonNull String toString() {
            return Stringify.stringify(this);
        }
    }
}
