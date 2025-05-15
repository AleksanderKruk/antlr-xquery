package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.List;

public class XQueryEnumItemTypeRecord extends XQueryEnumItemType {
  record KeyValuePair(XQueryEnumItemType key, XQueryEnumSequenceType value) {}

  List<KeyValuePair> keyValuePairs;
  public XQueryEnumItemTypeRecord (List<KeyValuePair> keyValuePairs) {
    super(XQueryTypes.RECORD, null, null, null, null, null, null);
    this.keyValuePairs = keyValuePairs;
  }

}
