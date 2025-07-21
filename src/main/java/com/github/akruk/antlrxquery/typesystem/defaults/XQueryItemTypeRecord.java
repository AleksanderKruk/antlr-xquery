package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.Map;

import com.github.akruk.antlrxquery.typesystem.XQueryRecordField;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQueryItemTypeRecord extends XQueryItemType {
    final Map<String, XQueryRecordField> recordFields;

  public Map<String, XQueryRecordField> getRecordFields() {
    return recordFields;
  }

  public XQueryItemTypeRecord(Map<String, XQueryRecordField> keyValuePairs, XQueryTypeFactory factory ) {
    this(XQueryTypes.RECORD, keyValuePairs, factory);
  }

  XQueryItemTypeRecord(XQueryTypes recordType, Map<String, XQueryRecordField> keyValuePairs, XQueryTypeFactory factory ) {
    super(recordType, null, null, null, null, null, null, factory, null);
    this.recordFields = keyValuePairs;
  }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("record(");
        boolean first = true;
        for (Map.Entry<String, XQueryRecordField> entry : recordFields.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(entry.getKey());
            if (!entry.getValue().isRequired()) {
                sb.append("?");
            }
            sb.append(" as ").append(entry.getValue().type());
            first = false;
            }
            sb.append(")");
            return sb.toString();
    }

}
