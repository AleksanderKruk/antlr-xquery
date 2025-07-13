package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.Map;

import com.github.akruk.antlrxquery.typesystem.XQueryRecordField;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQueryEnumItemTypeExtensibleRecord extends XQueryEnumItemTypeRecord {
    public XQueryEnumItemTypeExtensibleRecord(Map<String, XQueryRecordField> fields, XQueryTypeFactory factory) {
        super(XQueryTypes.EXTENSIBLE_RECORD, fields, factory);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("record(");
        boolean first = true;
        for (Map.Entry<String, XQueryRecordField> entry : getRecordFields().entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(entry.getKey())
              .append(" as ")
              .append(entry.getValue()); // assuming getType() returns the type name
            first = false;
        }
        sb.append(")");
        return sb.toString();
    }

}
