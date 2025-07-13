package com.github.akruk.antlrxquery.typesystem.defaults;

import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQueryEnumItemTypeAnyNode extends XQueryEnumItemType {

  public XQueryEnumItemTypeAnyNode(XQueryTypeFactory factory) {
    super(XQueryTypes.ANY_NODE, null, null, null, null, null, null, factory, null);
  }

  @Override
  public String toString() {
      return "node()";
  }

}
