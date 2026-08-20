package com.github.akruk.antlrquery.typesystem.types.itemtypes;

import com.github.akruk.antlrquery.typesystem.typeoperations.stringify.Stringify;
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
            return Stringify.stringify(this);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof GrammarType;
        }
    }

    record GrammarRuleType()
            implements com.github.akruk.antlrquery.typesystem.types.itemtypes.GrammarEntityType {
        @Override
        public @NonNull String toString() {
            return Stringify.stringify(this);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof GrammarRuleType;
        }
    }

    record GrammarTokenType()
            implements com.github.akruk.antlrquery.typesystem.types.itemtypes.GrammarEntityType {
        @Override
        public @NonNull String toString() {
            return Stringify.stringify(this);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof GrammarTokenType;
        }
    }
}
