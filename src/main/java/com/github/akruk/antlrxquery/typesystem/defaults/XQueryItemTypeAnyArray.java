package com.github.akruk.antlrxquery.typesystem.defaults;

import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQueryItemTypeAnyArray extends XQueryItemType {

  public XQueryItemTypeAnyArray(XQueryTypeFactory factory) {
    super(XQueryTypes.ANY_ARRAY, null, null, null, null, null, null, factory, null);
  }

  @Override
  public String toString() {
      return "array(*)";
  }

}
