package com.github.akruk.antlrquery.typesystem.typeoperations.itemtype;


import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.typeoperations.Types;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.ArrayLikeType;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.FunctionType;
import com.github.akruk.visitorannotations.Visitor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@Visitor(name = "ArrayLikeToFunctionVisitor", classes = { ArrayLikeType.class, FunctionType.class})
@DefaultQualifier(NonNull.class)
public class ArrayLikeSubtypeOfFunction implements ArrayLikeToFunctionVisitor<Boolean> {
    private final AntlrQueryTypeFactory typeFactory;

    public ArrayLikeSubtypeOfFunction(AntlrQueryTypeFactory typeFactory) {
        this.typeFactory = typeFactory;
    }

    @Override
    public Boolean visit(ArrayLikeType.ArrayType arrayType, FunctionType.AnyFunction anyFunction) {
        return true;
    }

    @Override
    public Boolean visit(ArrayLikeType.ArrayType arrayType, FunctionType.ConstrainedFunction constrainedFunction) {
        if (constrainedFunction.argumentTypes().size() != 1) {
            return false;
        }
        var indexType = Types.getIndexType(typeFactory, arrayType);
        boolean argIsCovariant = Types.isSubtype(typeFactory, constrainedFunction.argumentTypes().getFirst(), indexType);
        return argIsCovariant
                && Types.isSubtype(typeFactory, arrayType.memberType(), constrainedFunction.returnType());
    }

    @Override
    public Boolean visit(ArrayLikeType.TupleType tupleType, FunctionType.AnyFunction anyFunction) {
        return true;
    }

    @Override
    public Boolean visit(ArrayLikeType.TupleType tupleType, FunctionType.ConstrainedFunction constrainedFunction) {
        if (constrainedFunction.argumentTypes().size() != 1) {
            return false;
        }
        var indexType = Types.getIndexType(typeFactory, tupleType);
        boolean argIsCovariant = Types.isSubtype(typeFactory, indexType, constrainedFunction.argumentTypes().getFirst());
        var memberType = Types.getMemberType(typeFactory, tupleType);
        return argIsCovariant && Types.isSubtype(typeFactory, memberType, constrainedFunction.returnType());
    }

}
