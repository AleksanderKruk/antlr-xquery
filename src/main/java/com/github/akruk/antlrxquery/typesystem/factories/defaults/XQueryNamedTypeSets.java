package com.github.akruk.antlrxquery.typesystem.factories.defaults;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.akruk.antlrxquery.typesystem.XQueryRecordField;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryItemTypeReference;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;

public final class XQueryNamedTypeSets {
    private Map<String, XQueryItemType> DEFAULT_ALL;
    public Map<String, XQueryItemType> all() {
        XQueryEnumTypeFactory typeFactory = new XQueryEnumTypeFactory(Map.of());
        if (DEFAULT_ALL != null)
            return DEFAULT_ALL;
        DEFAULT_ALL = new HashMap<>(10);
        final XQuerySequenceType anyItems = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
        final XQueryItemType keyValuePair = typeFactory.itemExtensibleRecord(Map.of(
            "key", new XQueryRecordField(typeFactory.anyItem(), true),
            "value", new XQueryRecordField(anyItems, true)
        ));
        DEFAULT_ALL.put("fn:key-value-pair", keyValuePair);

        final XQuerySequenceType stringToAnyItems = typeFactory.map(typeFactory.itemString(), anyItems);
        final XQuerySequenceType integerToAnyFunction = typeFactory.map(typeFactory.itemNumber(), typeFactory.anyFunction());
        final XQuerySequenceType stringToIntegerToAnyFunction = typeFactory.map(typeFactory.itemString(), integerToAnyFunction);
        final XQueryItemType loadXQueryModuleRecord = typeFactory.itemRecord(Map.of(
            "variables", new XQueryRecordField(stringToAnyItems, true),
            "functions", new XQueryRecordField(stringToIntegerToAnyFunction, true)
        ));
        DEFAULT_ALL.put("fn:load-xquery-module-record", loadXQueryModuleRecord);

        // final XQueryItemType parsedCSVStructureRecord = typeFactory.itemRecord(Map.of(
        //     "columns", new XQueryRecordField(stringToAnyItems, true),
        //     "column-index", new XQueryRecordField(stringToIntegerToAnyFunction, true),
        //     "rows", new XQueryRecordField(stringToIntegerToAnyFunction, true),
        //     "get", new XQueryRecordField(stringToIntegerToAnyFunction, true),
        // ));
        // DEFAULT_ALL.put("fn:parsed-csv-structure-record", null);


<<<<<<< HEAD
        final XQueryItemType randomRef = new XQueryItemTypeReference(()->typeFactory.itemNamedType("fn:random-number-generator-record"));
=======
        final XQueryItemTypeReference randomRef = new XQueryItemTypeReference(()->typeFactory.itemNamedType("fn:random-number-generator-record"));
>>>>>>> language-features/lookup-expression
        final var oneRandomRef = typeFactory.one(randomRef);
        final XQueryItemType randomNumberGeneratorRecord = typeFactory.itemExtensibleRecord(Map.of(
            "number", new XQueryRecordField(typeFactory.number(), true),
            "next", new XQueryRecordField(typeFactory.function(oneRandomRef, List.of()), true),
            "permute", new XQueryRecordField(typeFactory.function(anyItems, List.of(anyItems)), true)
        ));
        DEFAULT_ALL.put("fn:random-number-generator-record", randomNumberGeneratorRecord);

        // DEFAULT_ALL.put("fn:schema-type-record", null);
        // DEFAULT_ALL.put("fn:uri-structure-record", null);
        return DEFAULT_ALL;
    }
}
