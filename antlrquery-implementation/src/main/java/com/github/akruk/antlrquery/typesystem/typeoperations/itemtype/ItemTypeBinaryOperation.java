package com.github.akruk.antlrquery.typesystem.typeoperations.itemtype;


import com.github.akruk.antlrquery.typesystem.types.itemtypes.AntlrQueryItemType;
import com.github.akruk.visitorannotations.Visitor;

@Visitor(name = "ItemTypeVisitor", classes = {AntlrQueryItemType.class, AntlrQueryItemType.class})
public interface ItemTypeBinaryOperation<ReturnedType>
        extends com.github.akruk.antlrquery.typesystem.typeoperations.itemtype.ItemTypeVisitor<ReturnedType>
{
}
