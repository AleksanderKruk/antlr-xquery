package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.Map;

import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;

public class XQueryEnumItemTypeRecord extends XQueryEnumItemType {
  final Map<String, XQuerySequenceType> recordFields;

  public Map<String, XQuerySequenceType> getRecordFields() {
    return recordFields;
  }

  public XQueryEnumItemTypeRecord(Map<String, XQuerySequenceType> keyValuePairs) {
    super(XQueryTypes.RECORD, null, null, null, null, null, null);
    this.recordFields = keyValuePairs;
  }

}
