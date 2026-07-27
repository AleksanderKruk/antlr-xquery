package com.github.akruk.antlrxquery.typesystem.typeoperations.itemtype;


import com.github.akruk.antlrxquery.typesystem.types.itemtypes.AntlrQueryItemType;
import com.github.akruk.visitorannotations.Visitor;

@Visitor(name = "ItemTypeVisitor", classes = {AntlrQueryItemType.class, AntlrQueryItemType.class})
public interface ItemTypeBinaryOperation<ReturnedType>
        extends com.github.akruk.antlrxquery.typesystem.typeoperations.itemtype.ItemTypeVisitor<ReturnedType>
{
}
