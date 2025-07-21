package com.github.akruk.antlrxquery.typesystem.defaults;

import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQueryItemTypeBoolean extends XQueryItemType {

  public XQueryItemTypeBoolean(XQueryTypeFactory factory) {
    super(XQueryTypes.BOOLEAN, null, null, null, null, null, null, factory, null);
  }

  @Override
  public String toString() {
      return "boolean";
  }
}
