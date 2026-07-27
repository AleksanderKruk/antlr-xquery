package com.github.akruk.antlrxquery.typesystem.factories.defaults;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.akruk.antlrxquery.typesystem.RecordField;
import com.github.akruk.antlrxquery.typesystem.RecordField.TypeOrReference;
import com.github.akruk.antlrxquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.types.NumericRange;
import com.github.akruk.antlrxquery.typesystem.types.itemtypes.AntlrQueryItemType;

public final class XQueryNamedTypeSets {
    private Map<String, Map<String, AntlrQueryItemType>> DEFAULT_ALL;
    public Map<String, Map<String, AntlrQueryItemType>> all() {
        final MemoizedTypeFactory typeFactory = new MemoizedTypeFactory(Map.of(), Map.of());
        if (DEFAULT_ALL != null)
            return DEFAULT_ALL;
        DEFAULT_ALL = new HashMap<>(10);
        final AntlrQuerySequenceType zeroOrMoreItems = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
        final AntlrQueryItemType keyValuePair = typeFactory.itemExtensibleRecord(
                new LinkedHashMap<>(
                        Map.of(
                                "key", new RecordField("key", new TypeOrReference.Type(typeFactory.anyItem()), true),
                                "value", new RecordField("value", new TypeOrReference.Type(zeroOrMoreItems), true)
                        )
                    ),
                typeFactory.zeroOrMore(typeFactory.itemAnyItem())
            );
        DEFAULT_ALL.computeIfAbsent("fn", _->new HashMap<>()).put("key-value-pair", keyValuePair);

        final AntlrQuerySequenceType stringToAnyItems = typeFactory.map(typeFactory.itemString(), zeroOrMoreItems);
        final AntlrQuerySequenceType integerToAnyFunction = typeFactory.map(typeFactory.itemNumber(), typeFactory.anyFunction());
        final AntlrQuerySequenceType stringToIntegerToAnyFunction = typeFactory.map(typeFactory.itemString(), integerToAnyFunction);
        final AntlrQueryItemType loadXQueryModuleRecord = typeFactory.itemRecord(new LinkedHashMap<>(Map.of(
            "variables", new RecordField("variables", new TypeOrReference.Type(stringToAnyItems), true),
            "functions", new RecordField("functions", new TypeOrReference.Type(stringToIntegerToAnyFunction), true)
        )));
        DEFAULT_ALL.computeIfAbsent("fn", _->new HashMap<>()).put("load-xquery-module-record", loadXQueryModuleRecord);

        // final XQueryItemType parsedCSVStructureRecord = typeFactory.itemRecord(Map.of(
        //     "columns", new XQueryRecordField(stringToAnyItems, true),
        //     "column-index", new XQueryRecordField(stringToIntegerToAnyFunction, true),
        //     "rows", new XQueryRecordField(stringToIntegerToAnyFunction, true),
        //     "get", new XQueryRecordField(stringToIntegerToAnyFunction, true),
        // ));
        // DEFAULT_ALL.put("fn:parsed-csv-structure-record", null);


        final LinkedHashMap<String, RecordField> fields = new LinkedHashMap<>();
        fields.put("number", new RecordField("number", new TypeOrReference.Type(typeFactory.number(NumericRange.FULL)), true));
        fields.put("permute", new RecordField("permute", new TypeOrReference.Type(typeFactory.function(zeroOrMoreItems, List.of(zeroOrMoreItems))), true));
        final AntlrQueryItemType randomNumberGeneratorRecord = typeFactory.itemExtensibleRecord(fields, typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        final var oneRandomRef = typeFactory.one(randomNumberGeneratorRecord);
        fields.put("next", new RecordField("next", new TypeOrReference.Type(typeFactory.function(oneRandomRef, List.of())), true));

        DEFAULT_ALL.computeIfAbsent("fn", _->new HashMap<>()).put("random-number-generator-record", randomNumberGeneratorRecord);

        // DEFAULT_ALL.put("fn:schema-type-record", null);
        // DEFAULT_ALL.put("fn:uri-structure-record", null);
        return DEFAULT_ALL;
    }
}
