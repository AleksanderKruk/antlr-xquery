package com.github.akruk.antlrquery.typesystem.types.itemtypes;

/**
 * ChoiceItemType
 */
public final record ChoiceItemType(ConcreteItemType[] itemTypes)
    implements AntlrQueryItemType 
{}
