package com.github.akruk.antlrxquery.typesystem.defaults;

import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQueryItemTypeString extends XQueryItemType {
  public XQueryItemTypeString(XQueryTypeFactory factory) {
    super(XQueryTypes.STRING, null, null, null, null, null, null, factory, null);
  }

  @Override
  public String toString() {
      return "string";
  }
}
