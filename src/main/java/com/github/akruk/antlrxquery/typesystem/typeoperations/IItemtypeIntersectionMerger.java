package com.github.akruk.antlrxquery.typesystem.typeoperations;

import com.github.akruk.antlrxquery.typesystem.XQueryItemType;

public interface IItemtypeIntersectionMerger {

    XQueryItemType intersectionMerge(XQueryItemType type1, XQueryItemType type2);

}