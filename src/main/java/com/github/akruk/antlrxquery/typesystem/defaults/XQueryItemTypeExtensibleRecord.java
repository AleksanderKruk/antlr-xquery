package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.Map;

import com.github.akruk.antlrxquery.typesystem.XQueryRecordField;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQueryItemTypeExtensibleRecord extends XQueryItemTypeRecord {
    public XQueryItemTypeExtensibleRecord(Map<String, XQueryRecordField> fields, XQueryTypeFactory factory) {
        super(XQueryTypes.EXTENSIBLE_RECORD, fields, factory);
    }

}
