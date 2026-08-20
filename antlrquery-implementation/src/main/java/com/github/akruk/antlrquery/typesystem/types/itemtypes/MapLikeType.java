package com.github.akruk.antlrquery.typesystem.types.itemtypes;

import com.github.akruk.antlrquery.typesystem.RecordField;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.typeoperations.stringify.Stringify;
import org.checkerframework.checker.nullness.qual.NonNull;

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
    {
        @Override
        public @NonNull String toString() {
            return Stringify.stringify(this);
        }
    }

    record RecordType(
            LinkedHashMap<String, RecordField> fields
    ) implements MapLikeType
    {
        @Override
        public @NonNull String toString() {
            return Stringify.stringify(this);
        }
    }

    record ExtensibleRecordType(
            Map<String, RecordField> fields,
            AntlrQuerySequenceType additionalFieldType
    ) implements MapLikeType
    {
        @Override
        public @NonNull String toString() {
            return Stringify.stringify(this);
        }

    }
}
