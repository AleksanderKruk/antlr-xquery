package com.github.akruk.antlrxquery.typesystem.defaults;

import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQueryEnumItemTypeMap extends XQueryEnumItemType {

  public XQueryEnumItemTypeMap(XQueryEnumItemType key, XQueryEnumSequenceType value, XQueryTypeFactory factory) {
    super(XQueryTypes.MAP, null, null, null, key, value, null, factory);
  }

}
