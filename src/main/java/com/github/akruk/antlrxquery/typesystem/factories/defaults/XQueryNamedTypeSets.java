package com.github.akruk.antlrxquery.typesystem.factories.defaults;

import java.util.HashMap;
import java.util.Map;

import com.github.akruk.antlrxquery.typesystem.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.XQueryRecordField;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public final class XQueryNamedTypeSets {
    private Map<String, XQueryItemType> DEFAULT_ALL;
    public Map<String, XQueryItemType> getDefaultAll() {
        XQueryEnumTypeFactory typeFactory = new XQueryEnumTypeFactory(null);
        if (DEFAULT_ALL != null)
            return DEFAULT_ALL;
        DEFAULT_ALL = new HashMap<>(10);
        final XQuerySequenceType anyItems = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
        final XQueryItemType keyValuePair = typeFactory.itemExtensibleRecord(Map.of(
            "key", new XQueryRecordField(typeFactory.anyItem(), true),
            "value", new XQueryRecordField(anyItems, true)
        ));
        DEFAULT_ALL.put("key-value-pair", keyValuePair);

        final XQuerySequenceType stringToAnyItems = typeFactory.map(typeFactory.itemString(), anyItems);
        final XQuerySequenceType integerToAnyFunction = typeFactory.map(typeFactory.itemNumber(), typeFactory.anyFunction());
        final XQuerySequenceType stringToIntegerToAnyFunction = typeFactory.map(typeFactory.itemString(), integerToAnyFunction);
        final XQueryItemType loadXQueryModuleRecord = typeFactory.itemRecord(Map.of(
            "variables", new XQueryRecordField(stringToAnyItems, true),
            "functions", new XQueryRecordField(stringToIntegerToAnyFunction, true)
        ));
        DEFAULT_ALL.put("load-xquery-module-record", loadXQueryModuleRecord);

        DEFAULT_ALL.put("parsed-csv-structure-record", null);

        DEFAULT_ALL.put("random-number-generator-record", null);
        DEFAULT_ALL.put("schema-type-record", null);
        DEFAULT_ALL.put("uri-structure-record", null);
        return DEFAULT_ALL;
    }
}
