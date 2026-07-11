package com.github.akruk.antlrxquery.typesystem.factories.defaults;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.akruk.antlrxquery.typesystem.XQueryRecordField;
import com.github.akruk.antlrxquery.typesystem.XQueryRecordField.TypeOrReference;
import com.github.akruk.antlrxquery.typesystem.types.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.types.AntlrQuerySequenceType;

public final class XQueryNamedTypeSets {
    private Map<String, Map<String, XQueryItemType>> DEFAULT_ALL;
    public Map<String, Map<String, XQueryItemType>> all() {
        final XQueryMemoizedTypeFactory typeFactory = new XQueryMemoizedTypeFactory(Map.of());
        if (DEFAULT_ALL != null)
            return DEFAULT_ALL;
        DEFAULT_ALL = new HashMap<>(10);
        final AntlrQuerySequenceType zeroOrMoreItems = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
        final XQueryItemType keyValuePair = typeFactory.itemExtensibleRecord(Map.of(
            "key", new XQueryRecordField(new TypeOrReference.Type(typeFactory.anyItem()), true),
            "value", new XQueryRecordField(new TypeOrReference.Type(zeroOrMoreItems), true)
        ));
        DEFAULT_ALL.computeIfAbsent("fn", _->new HashMap<>()).put("key-value-pair", keyValuePair);

        final AntlrQuerySequenceType stringToAnyItems = typeFactory.map(typeFactory.itemString(), zeroOrMoreItems);
        final AntlrQuerySequenceType integerToAnyFunction = typeFactory.map(typeFactory.itemNumber(), typeFactory.anyFunction());
        final AntlrQuerySequenceType stringToIntegerToAnyFunction = typeFactory.map(typeFactory.itemString(), integerToAnyFunction);
        final XQueryItemType loadXQueryModuleRecord = typeFactory.itemRecord(Map.of(
            "variables", new XQueryRecordField(new TypeOrReference.Type(stringToAnyItems), true),
            "functions", new XQueryRecordField(new TypeOrReference.Type(stringToIntegerToAnyFunction), true)
        ));
        DEFAULT_ALL.computeIfAbsent("fn", _->new HashMap<>()).put("load-xquery-module-record", loadXQueryModuleRecord);

        // final XQueryItemType parsedCSVStructureRecord = typeFactory.itemRecord(Map.of(
        //     "columns", new XQueryRecordField(stringToAnyItems, true),
        //     "column-index", new XQueryRecordField(stringToIntegerToAnyFunction, true),
        //     "rows", new XQueryRecordField(stringToIntegerToAnyFunction, true),
        //     "get", new XQueryRecordField(stringToIntegerToAnyFunction, true),
        // ));
        // DEFAULT_ALL.put("fn:parsed-csv-structure-record", null);


        final Map<String, XQueryRecordField> fields = new LinkedHashMap<>();
        fields.put("number", new XQueryRecordField(new TypeOrReference.Type(typeFactory.number()), true));
        fields.put("permute", new XQueryRecordField(new TypeOrReference.Type(typeFactory.function(zeroOrMoreItems, List.of(zeroOrMoreItems))), true));
        final XQueryItemType randomNumberGeneratorRecord = typeFactory.itemExtensibleRecord(fields);
        final var oneRandomRef = typeFactory.one(randomNumberGeneratorRecord);
        fields.put("next", new XQueryRecordField(new TypeOrReference.Type(typeFactory.function(oneRandomRef, List.of())), true));

        DEFAULT_ALL.computeIfAbsent("fn", _->new HashMap<>()).put("random-number-generator-record", randomNumberGeneratorRecord);

        // DEFAULT_ALL.put("fn:schema-type-record", null);
        // DEFAULT_ALL.put("fn:uri-structure-record", null);
        return DEFAULT_ALL;
    }
}
