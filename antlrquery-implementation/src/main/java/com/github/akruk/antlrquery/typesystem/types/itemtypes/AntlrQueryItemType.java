package com.github.akruk.antlrquery.typesystem.types.itemtypes;


public sealed interface AntlrQueryItemType 
    permits 
        ConcreteItemType,
        ChoiceItemType,
        AnyItemType,
        NothingType,
        NeverType
{
    NothingType NOTHING = new NothingType();
    NeverType NEVER = new NeverType();
    AnyItemType ANY_TYPE = new AnyItemType();
}