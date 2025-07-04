package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.Map;

import com.github.akruk.antlrxquery.typesystem.XQueryRecordField;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQueryEnumItemTypeRecord extends XQueryEnumItemType {
    final Map<String, XQueryRecordField> recordFields;

  public Map<String, XQueryRecordField> getRecordFields() {
    return recordFields;
  }

  public XQueryEnumItemTypeRecord(Map<String, XQueryRecordField> keyValuePairs, XQueryTypeFactory factory ) {
    this(XQueryTypes.RECORD, keyValuePairs, factory);
  }

  XQueryEnumItemTypeRecord(XQueryTypes recordType, Map<String, XQueryRecordField> keyValuePairs, XQueryTypeFactory factory ) {
    super(recordType, null, null, null, null, null, null, factory, null);
    this.recordFields = keyValuePairs;
  }

}
