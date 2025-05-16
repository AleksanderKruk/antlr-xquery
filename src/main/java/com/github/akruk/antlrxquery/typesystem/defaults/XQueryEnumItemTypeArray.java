package com.github.akruk.antlrxquery.typesystem.defaults;

public class XQueryEnumItemTypeArray extends XQueryEnumItemType {

  public XQueryEnumItemTypeArray(XQueryEnumSequenceType containedType) {
    super(XQueryTypes.ARRAY, null, null, containedType, null, null, null);
  }

}
