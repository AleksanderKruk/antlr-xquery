package com.github.akruk.antlrquery.typesystem.types.itemtypes;

sealed public interface BooleanType extends AtomicType
        permits BooleanType.True,
        BooleanType.False,
        BooleanType.Boolean {

    public record True() implements com.github.akruk.antlrquery.typesystem.types.itemtypes.BooleanType {
    }

    public record False() implements com.github.akruk.antlrquery.typesystem.types.itemtypes.BooleanType {
    }

    public record Boolean() implements com.github.akruk.antlrquery.typesystem.types.itemtypes.BooleanType {
    }
}
