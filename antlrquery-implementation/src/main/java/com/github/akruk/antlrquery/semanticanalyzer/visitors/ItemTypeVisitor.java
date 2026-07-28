package com.github.akruk.antlrquery.semanticanalyzer.visitors;

import com.github.akruk.antlrquery.AntlrXqueryParserBaseVisitor;
import com.github.akruk.antlrquery.AntlrXqueryParser.*;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.AntlrQueryItemType;


/**
 * CardinalityVisitor visits AntlrQuery parse tree to determine the cardinality of type
 */
public class ItemTypeVisitor 
    extends AntlrXqueryParserBaseVisitor<AntlrQueryItemType> 
{
    private AntlrQueryTypeFactory typeFactory;
    public ItemTypeVisitor(AntlrQueryTypeFactory typeFactory) {
        this.typeFactory = typeFactory;

    }
    @Override
    public AntlrQueryItemType visitAnyItem(AnyItemContext ctx) {
        return typeFactory.itemAnyItem();
    }
    
    @Override
    public AntlrQueryItemType visitStringType(StringTypeContext ctx) {
        return typeFactory.itemString();
    }

    @Override
    public AntlrQueryItemType visitBooleanType(BooleanTypeContext ctx) {
        return typeFactory.itemBoolean();
    }

}
