package com.github.akruk.antlrxquery.typesystem.typeoperations.defaults;

import com.github.akruk.antlrxquery.typesystem.XQueryItemType;

public interface IItemtypeUnionMerger {

    XQueryItemType unionMerge(XQueryItemType type1, XQueryItemType type2);

}