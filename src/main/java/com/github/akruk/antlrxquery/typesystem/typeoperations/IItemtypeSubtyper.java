package com.github.akruk.antlrxquery.typesystem.typeoperations;

import com.github.akruk.antlrxquery.typesystem.XQueryItemType;

public interface IItemtypeSubtyper {
  /**
   * Checks whether or not type1 is subtype of type2
   * @param type1 the first item type
   * @param type2 the second item type
   * @return boolean predicate
   */
  boolean itemtypeIsSubtypeOf(XQueryItemType type1, XQueryItemType type2);

}
