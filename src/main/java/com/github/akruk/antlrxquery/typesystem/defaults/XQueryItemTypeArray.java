package com.github.akruk.antlrxquery.typesystem.defaults;

import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQueryItemTypeArray extends XQueryItemType {

  public XQueryItemTypeArray(XQuerySequenceType containedType, XQueryTypeFactory factory) {
    super(XQueryTypes.ARRAY, null, null, containedType, null, null, null, factory, null);
  }

  @Override
  public String toString() {
      return "array(" + getArrayType() + ")";
  }
}
