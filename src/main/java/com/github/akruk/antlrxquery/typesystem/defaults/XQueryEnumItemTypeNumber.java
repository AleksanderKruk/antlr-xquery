package com.github.akruk.antlrxquery.typesystem.defaults;

import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQueryEnumItemTypeNumber extends XQueryEnumItemType {

  public XQueryEnumItemTypeNumber(XQueryTypeFactory factory) {
    super(XQueryTypes.NUMBER, null, null, null,null,null,null, factory, null);
  }

  @Override
  public String toString() {
    return "number";
  }

}
