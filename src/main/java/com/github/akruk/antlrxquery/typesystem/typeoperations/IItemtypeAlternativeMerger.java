package com.github.akruk.antlrxquery.typesystem.typeoperations;

import com.github.akruk.antlrxquery.typesystem.XQueryItemType;

public interface IItemtypeAlternativeMerger {
  /**
   * Merges two item types into one alternative item type.
   * e.g.
   * number, string -> (number | xs:string)
   * element(a), element(b) -> element(a | b)
   * @param type1 the first item type
   * @param type2 the second item type
   * @return a new item type that is an alternative of the two input types
   */
  XQueryItemType alternativeMerge(XQueryItemType type1, XQueryItemType type2);

}
