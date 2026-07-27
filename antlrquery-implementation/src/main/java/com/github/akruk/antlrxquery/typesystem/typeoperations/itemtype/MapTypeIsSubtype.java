package com.github.akruk.antlrxquery.typesystem.typeoperations.itemtype;

import com.github.akruk.antlrxquery.typesystem.RecordField;
import com.github.akruk.antlrxquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.typeoperations.Types;
import com.github.akruk.antlrxquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.types.ItemTypes;
import com.github.akruk.antlrxquery.typesystem.types.itemtypes.MapLikeType;
import com.github.akruk.visitorannotations.Visitor;

import java.util.Map;

@Visitor(name = "MapVisitor", classes= {MapLikeType.class, MapLikeType.class})
public class MapTypeIsSubtype implements MapVisitor<Boolean>  {
    private final AntlrQueryTypeFactory typeFactory;

    public MapTypeIsSubtype(AntlrQueryTypeFactory typeFactory) {
        this.typeFactory = typeFactory;
    }


    public boolean isSubtype(MapLikeType t1, MapLikeType t2) {
        return visit(t1, t2);
    }

    @Override
    public Boolean visit(MapLikeType.ExtensibleRecordType extensibleRecordType, MapLikeType.ExtensibleRecordType extensibleRecordType2) {
        Map<String, RecordField> sourceFields = extensibleRecordType.fields();
        Map<String, RecordField> targetFields = extensibleRecordType2.fields();

        for (Map.Entry<String, RecordField> targetEntry : targetFields.entrySet()) {
            String fieldName = targetEntry.getKey();
            RecordField sourceField = sourceFields.get(fieldName);

            AntlrQuerySequenceType targetFieldType = targetEntry.getValue().resolveFieldType(typeFactory);

            if (sourceField != null) {
                AntlrQuerySequenceType sourceFieldType = sourceField.resolveFieldType(typeFactory);
                boolean fieldSub = Types.isSubtype(typeFactory, sourceFieldType, targetFieldType);
                if (!fieldSub) {
                    return false;
                }
            } else {
                boolean addSub = Types.isSubtype(typeFactory, extensibleRecordType.additionalFieldType(), targetFieldType);
                if (!addSub) {
                    return false;
                }
            }
        }

        return Types.isSubtype(typeFactory, extensibleRecordType.additionalFieldType(), extensibleRecordType2.additionalFieldType());
    }

    @Override
    public Boolean visit(MapLikeType.ExtensibleRecordType extensibleRecordType, MapLikeType.RecordType recordType) {
        Map<String, RecordField> sourceFields = extensibleRecordType.fields();
        Map<String, RecordField> targetFields = recordType.fields();

        for (Map.Entry<String, RecordField> targetEntry : targetFields.entrySet()) {
            String fieldName = targetEntry.getKey();
            RecordField sourceField = sourceFields.get(fieldName);
            AntlrQuerySequenceType targetFieldType = targetEntry.getValue().resolveFieldType(typeFactory);

            if (sourceField != null) {
                AntlrQuerySequenceType sourceFieldType = sourceField.resolveFieldType(typeFactory);
                boolean fieldSub = Types.isSubtype(typeFactory, sourceFieldType, targetFieldType);
                if (!fieldSub) {
                    return false;
                }
            } else {
                boolean addSub = Types.isSubtype(typeFactory, extensibleRecordType.additionalFieldType(), targetFieldType);
                if (!addSub) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public Boolean visit(MapLikeType.ExtensibleRecordType extensibleRecordType, MapLikeType.MapType mapType) {
        for (RecordField field : extensibleRecordType.fields().values()) {
            AntlrQuerySequenceType fieldType = field.resolveFieldType(typeFactory);
            boolean valSub = Types.isSubtype(typeFactory, fieldType, mapType.valueType());
            if (!valSub) {
                return false;
            }
        }
        return Types.isSubtype(typeFactory, extensibleRecordType.additionalFieldType(), mapType.valueType());
    }

    @Override
    public Boolean visit(MapLikeType.RecordType recordType, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        var fieldIterator = recordType.fields().values().iterator();
        var extensibleFieldIterator = extensibleRecordType.fields().values().iterator();
        var rField = fieldIterator.next();
        var eField = extensibleFieldIterator.next();
        while (fieldIterator.hasNext()) {
            if (!rField.name().equals(eField.name())) {
//                         optional eField can be skipped
                if (eField.isRequired()) {
                    return false;
                }
                if (!extensibleFieldIterator.hasNext()) {
                    // if no optional eFields remain we assume the type of additionalField
                    do {
                        if (!Types.isSubtype(typeFactory, rField.resolveFieldType(typeFactory), extensibleRecordType.additionalFieldType())) {
                            return false;
                        }
                        rField = fieldIterator.next();
                    } while (fieldIterator.hasNext());
                    return true;
                }
                eField = extensibleFieldIterator.next();
            } else {
                if (!Types.isSubtype(typeFactory, rField.resolveFieldType(typeFactory), eField.resolveFieldType(typeFactory))) {
                    return false;
                }
                if (!extensibleFieldIterator.hasNext()) {
                    // if no optional eFields remain we assume the type of additionalField
                    do {
                        if (!Types.isSubtype(typeFactory, rField.resolveFieldType(typeFactory), extensibleRecordType.additionalFieldType())) {
                            return false;
                        }
                        rField = fieldIterator.next();
                    } while (fieldIterator.hasNext());
                    return true;
                }
                rField = fieldIterator.next();
            }
        }
        return true;
    }

    @Override
    public Boolean visit(MapLikeType.RecordType recordType, MapLikeType.RecordType recordType2) {
        Map<String, RecordField> sourceFields = recordType.fields();
        Map<String, RecordField> targetFields = recordType2.fields();

        for (Map.Entry<String, RecordField> targetEntry : targetFields.entrySet()) {
            String fieldName = targetEntry.getKey();
            RecordField sourceField = sourceFields.get(fieldName);

            if (sourceField == null) {
                return false;
            }

            boolean fieldSub = Types.isSubtype(typeFactory, sourceField.resolveFieldType(typeFactory), targetEntry.getValue().resolveFieldType(typeFactory));
            if (!fieldSub) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Boolean visit(MapLikeType.RecordType recordType, MapLikeType.MapType mapType) {
        for (Map.Entry<String, RecordField> entry : recordType.fields().entrySet()) {
            boolean valSub = Types.isSubtype(typeFactory, entry.getValue().resolveFieldType(typeFactory), mapType.valueType());
            if (!valSub) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Boolean visit(MapLikeType.MapType mapType, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return false;
    }

    @Override
    public Boolean visit(MapLikeType.MapType mapType, MapLikeType.RecordType recordType) {
        return false;
    }

    @Override
    public Boolean visit(MapLikeType.MapType mapType, MapLikeType.MapType mapType2) {
        boolean keysSub = ItemTypes.isSubtype(typeFactory, mapType2.keyType(), mapType.keyType());
        boolean valuesSub = Types.isSubtype(typeFactory, mapType.valueType(), mapType2.valueType());
        return keysSub && valuesSub;
    }
}
