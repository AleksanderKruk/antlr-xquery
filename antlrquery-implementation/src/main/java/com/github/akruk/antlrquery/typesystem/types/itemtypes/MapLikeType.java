package com.github.akruk.antlrquery.typesystem.types.itemtypes;

import com.github.akruk.antlrquery.typesystem.RecordField;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;

import java.util.LinkedHashMap;
import java.util.Map;

public sealed interface MapLikeType
        extends ConcreteItemType
        permits MapLikeType.MapType,
        MapLikeType.RecordType,
        MapLikeType.ExtensibleRecordType
{
    record MapType(
            AntlrQueryItemType keyType,
            AntlrQuerySequenceType valueType
    ) implements MapLikeType
    {}

    record RecordType(
            LinkedHashMap<String, RecordField> fields
    ) implements MapLikeType
    {}

    record ExtensibleRecordType(
            Map<String, RecordField> fields,
            AntlrQuerySequenceType additionalFieldType
    ) implements MapLikeType
    {}
}
