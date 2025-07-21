package com.github.akruk.antlrxquery.typesystem.defaults;

import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQueryItemTypeAnyItem extends XQueryItemType {

  public XQueryItemTypeAnyItem(XQueryTypeFactory factory) {
    super(XQueryTypes.ANY_ITEM, null, null, null, null, null, null, factory, null, null);
  }

}
