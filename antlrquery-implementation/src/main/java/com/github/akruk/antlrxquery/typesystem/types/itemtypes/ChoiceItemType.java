package com.github.akruk.antlrxquery.typesystem.types.itemtypes;

/**
 * ChoiceItemType
 */
public final record ChoiceItemType(ConcreteItemType[] itemTypes)
    implements AntlrQueryItemType 
{}
