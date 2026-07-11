package com.github.akruk.antlrxquery.semanticanalyzer.visitors;

import com.github.akruk.antlrxquery.AntlrXqueryParserBaseVisitor;
import com.github.akruk.antlrxquery.AntlrXqueryParser.*;
import com.github.akruk.antlrxquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.types.XQueryItemType;


/**
 * CardinalityVisitor visits AntlrQuery parse tree to determine the cardinality of type
 */
public class ItemTypeVisitor 
    extends AntlrXqueryParserBaseVisitor<XQueryItemType> 
{
    private AntlrQueryTypeFactory typeFactory;
    public ItemTypeVisitor(AntlrQueryTypeFactory typeFactory) {
        this.typeFactory = typeFactory;

    }
    @Override
    public XQueryItemType visitAnyItem(AnyItemContext ctx) {
        return typeFactory.itemAnyItem();
    }
    
    @Override
    public XQueryItemType visitStringType(StringTypeContext ctx) {
        return typeFactory.itemString();
    }

    @Override
    public XQueryItemType visitBooleanType(BooleanTypeContext ctx) {
        return typeFactory.itemBoolean();
    }

}
