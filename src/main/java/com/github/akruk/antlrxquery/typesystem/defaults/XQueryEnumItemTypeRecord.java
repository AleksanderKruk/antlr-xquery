package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.Map;

import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQueryEnumItemTypeRecord extends XQueryEnumItemType {
  final Map<String, XQuerySequenceType> recordFields;

  public Map<String, XQuerySequenceType> getRecordFields() {
    return recordFields;
  }

  public XQueryEnumItemTypeRecord(Map<String, XQuerySequenceType> keyValuePairs, XQueryTypeFactory factory ) {
    super(XQueryTypes.RECORD, null, null, null, null, null, null, factory);
    this.recordFields = keyValuePairs;
  }

}
