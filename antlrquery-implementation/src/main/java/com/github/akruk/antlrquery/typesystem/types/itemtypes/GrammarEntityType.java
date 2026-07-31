package com.github.akruk.antlrquery.typesystem.types.itemtypes;

import com.github.akruk.antlrquery.typesystem.types.ItemTypes;
import org.checkerframework.checker.nullness.qual.NonNull;

sealed public interface GrammarEntityType
        extends ConcreteItemType
        permits GrammarEntityType.GrammarType,
        GrammarEntityType.GrammarRuleType,
        GrammarEntityType.GrammarTokenType {
    record GrammarType()
            implements com.github.akruk.antlrquery.typesystem.types.itemtypes.GrammarEntityType {
        @Override
        public @NonNull String toString() {
            return ItemTypes.stringify(this);
        }
    }

    record GrammarRuleType()
            implements com.github.akruk.antlrquery.typesystem.types.itemtypes.GrammarEntityType {
        @Override
        public @NonNull String toString() {
            return ItemTypes.stringify(this);
        }
    }

    record GrammarTokenType()
            implements com.github.akruk.antlrquery.typesystem.types.itemtypes.GrammarEntityType {
        @Override
        public @NonNull String toString() {
            return ItemTypes.stringify(this);
        }
    }
}
