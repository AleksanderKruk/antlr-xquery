package com.github.akruk.antlrxquery.typesystem.defaults;

import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQueryItemTypeAnyFunction extends XQueryItemType {

  public XQueryItemTypeAnyFunction(XQueryTypeFactory factory) {
    super(XQueryTypes.ANY_FUNCTION, null, null, null, null, null, null, factory, null);
  }

  @Override
  public String toString() {
    return "function(*)";
  }
}
