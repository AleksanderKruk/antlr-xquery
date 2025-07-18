package com.github.akruk.antlrxquery.typesystem.defaults;

import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQueryItemTypeError extends XQueryItemType {

  public XQueryItemTypeError(XQueryTypeFactory factory) {
    super(XQueryTypes.ERROR, null, null, null, null, null, null, factory, null);
  }

}
