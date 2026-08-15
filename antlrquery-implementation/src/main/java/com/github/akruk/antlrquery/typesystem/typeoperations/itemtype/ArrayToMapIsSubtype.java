package com.github.akruk.antlrquery.typesystem.typeoperations.itemtype;

import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.typeoperations.Types;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Ranges;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.ItemTypes;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.ArrayLikeType;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.MapLikeType;
import com.github.akruk.visitorannotations.Visitor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@Visitor(name = "ArrayToMapVisitor", classes = {ArrayLikeType.class, MapLikeType.class})
@DefaultQualifier(NonNull.class)
public class ArrayToMapIsSubtype implements ArrayToMapVisitor<Boolean> {
    private final AntlrQueryTypeFactory typeFactory;

    public ArrayToMapIsSubtype(AntlrQueryTypeFactory typeFactory) {
        this.typeFactory = typeFactory;
    }

    public boolean isSubtype(ArrayLikeType t1, MapLikeType t2) {
        return visit(t1, t2);
    }

    @Override
    public Boolean visit(ArrayLikeType.TupleType tupleType, MapLikeType.MapType mapType) {
        int tupleLen = tupleType.members().length;
        if (tupleLen == 0) {
            return true;
        }

        if (!ItemTypes.isSubtype(typeFactory, typeFactory.itemNumber(Ranges.integers(1, tupleLen)), mapType.keyType())) {
            return false;
        }

        for (AntlrQuerySequenceType member : tupleType.members()) {
            if (!Types.isSubtype(typeFactory, member, mapType.valueType())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Boolean visit(ArrayLikeType.ArrayType arrayType, MapLikeType.MapType mapType) {
        AntlrQuerySequenceType indexType = Types.getIndexType(typeFactory, arrayType);
        if (!ItemTypes.isSubtype(typeFactory, indexType.itemType(), mapType.keyType()))
        {
            return false;
        }
        return Types.isSubtype(typeFactory, arrayType.memberType(), mapType.valueType());
    }


    @Override
    public Boolean visit(ArrayLikeType.TupleType tupleType, MapLikeType.RecordType recordType) {
        return false;
    }

    @Override
    public Boolean visit(ArrayLikeType.TupleType tupleType, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return false;
    }

    @Override
    public Boolean visit(ArrayLikeType.ArrayType arrayType, MapLikeType.RecordType recordType) {
        return false;
    }

    @Override
    public Boolean visit(ArrayLikeType.ArrayType arrayType, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return false;
    }

}
