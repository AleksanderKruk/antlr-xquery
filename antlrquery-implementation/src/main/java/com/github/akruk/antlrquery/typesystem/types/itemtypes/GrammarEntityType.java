package com.github.akruk.antlrquery.typesystem.types.itemtypes;

sealed public interface GrammarEntityType
        extends ConcreteItemType
        permits GrammarEntityType.GrammarType,
        GrammarEntityType.GrammarRuleType,
        GrammarEntityType.GrammarTokenType {
    record GrammarType()
            implements com.github.akruk.antlrquery.typesystem.types.itemtypes.GrammarEntityType {
    }

    record GrammarRuleType()
            implements com.github.akruk.antlrquery.typesystem.types.itemtypes.GrammarEntityType {
    }

    record GrammarTokenType()
            implements com.github.akruk.antlrquery.typesystem.types.itemtypes.GrammarEntityType {
    }
}
