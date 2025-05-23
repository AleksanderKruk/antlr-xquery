package com.github.akruk.antlrxquery.typesystem.defaults;

import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQueryEnumItemTypeArray extends XQueryEnumItemType {

  public XQueryEnumItemTypeArray(XQueryEnumSequenceType containedType, XQueryTypeFactory factory) {
    super(XQueryTypes.ARRAY, null, null, containedType, null, null, null, factory);
  }

}
