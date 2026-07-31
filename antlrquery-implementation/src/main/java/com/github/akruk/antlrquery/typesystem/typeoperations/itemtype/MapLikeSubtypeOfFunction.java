package com.github.akruk.antlrquery.typesystem.typeoperations.itemtype;


import com.github.akruk.antlrquery.typesystem.RecordField;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.typeoperations.Types;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.FunctionType;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.MapLikeType;
import com.github.akruk.visitorannotations.Visitor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@Visitor(name = "MapLikeToFunctionVisitor", classes = { MapLikeType.class, FunctionType.class})
@DefaultQualifier(NonNull.class)
public class MapLikeSubtypeOfFunction implements  MapLikeToFunctionVisitor<Boolean> {
    private final AntlrQueryTypeFactory typeFactory;

    public MapLikeSubtypeOfFunction(AntlrQueryTypeFactory typeFactory) {
        this.typeFactory = typeFactory;
    }

    @Override
    public Boolean visit(MapLikeType.MapType mapType, FunctionType.ConstrainedFunction constrainedFunction) {
        if (constrainedFunction.argumentTypes().size() != 1) {
            return false;
        }
        if (!Types.isSubtype(typeFactory, typeFactory.one(mapType.keyType()), constrainedFunction.argumentTypes().getFirst()))
        {
            return false;
        }
        return Types.isSubtype(typeFactory, mapType.valueType(), constrainedFunction.returnType());
    }

    @Override
    public Boolean visit(MapLikeType.ExtensibleRecordType extensibleRecordType, FunctionType.ConstrainedFunction constrainedFunction) {
        if (constrainedFunction.argumentTypes().size() != 1) {
            return false;
        }
        if (!Types.isSubtype(typeFactory, extensibleRecordType.additionalFieldType(), constrainedFunction.returnType())) {
            return false;
        }
        for (RecordField field : extensibleRecordType.fields().values()) {
            final AntlrQuerySequenceType fieldType = field.resolveFieldType(typeFactory);
            if (!Types.isSubtype(typeFactory, fieldType, constrainedFunction.returnType())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Boolean visit(MapLikeType.RecordType recordType, FunctionType.ConstrainedFunction constrainedFunction) {
        if (constrainedFunction.argumentTypes().size() != 1) {
            return false;
        }
        for (RecordField field : recordType.fields().values()) {
            final AntlrQuerySequenceType fieldType = field.resolveFieldType(typeFactory);
            if (!Types.isSubtype(typeFactory, fieldType, constrainedFunction.returnType())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Boolean visit(MapLikeType.ExtensibleRecordType extensibleRecordType, FunctionType.AnyFunction anyFunction) {
        return true;
    }

    @Override
    public Boolean visit(MapLikeType.RecordType recordType, FunctionType.AnyFunction anyFunction) {
        return true;
    }
    @Override
    public Boolean visit(MapLikeType.MapType mapType, FunctionType.AnyFunction anyFunction) {
        return true;
    }

}
