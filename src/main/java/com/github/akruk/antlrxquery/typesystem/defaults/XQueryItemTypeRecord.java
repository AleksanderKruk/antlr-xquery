package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.Map;

import com.github.akruk.antlrxquery.typesystem.XQueryRecordField;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQueryItemTypeRecord extends XQueryItemType {

    public XQueryItemTypeRecord(Map<String, XQueryRecordField> keyValuePairs, XQueryTypeFactory factory ) {
        this(XQueryTypes.RECORD, keyValuePairs, factory);
    }

    XQueryItemTypeRecord(XQueryTypes recordType, Map<String, XQueryRecordField> keyValuePairs, XQueryTypeFactory factory ) {
        super(recordType, null, null, null, null, null, null, factory, null, keyValuePairs);
    }

}
