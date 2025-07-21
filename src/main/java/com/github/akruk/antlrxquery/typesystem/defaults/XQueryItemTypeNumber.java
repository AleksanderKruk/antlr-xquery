package com.github.akruk.antlrxquery.typesystem.defaults;

import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQueryItemTypeNumber extends XQueryItemType {

  public XQueryItemTypeNumber(XQueryTypeFactory factory) {
    super(XQueryTypes.NUMBER, null, null, null,null,null,null, factory, null, null);
  }

}
