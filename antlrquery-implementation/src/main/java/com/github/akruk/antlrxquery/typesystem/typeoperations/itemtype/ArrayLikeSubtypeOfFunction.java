package com.github.akruk.antlrxquery.typesystem.typeoperations.itemtype;


import com.github.akruk.antlrxquery.typesystem.types.itemtypes.ArrayLikeType;
import com.github.akruk.antlrxquery.typesystem.types.itemtypes.FunctionType;
import com.github.akruk.antlrxquery.typesystem.types.itemtypes.MapLikeType;
import com.github.akruk.visitorannotations.Visitor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@Visitor(name = "ArrayLikeToFunctionVisitor", classes = { ArrayLikeType.class, FunctionType.class})
@DefaultQualifier(NonNull.class)
public class ArrayLikeSubtypeOfFunction implements ArrayLikeToFunctionVisitor<Boolean> {

    @Override
    public Boolean visit(ArrayLikeType.ArrayType arrayType, FunctionType.AnyFunction anyFunction) {
        return null;
    }

    @Override
    public Boolean visit(ArrayLikeType.ArrayType arrayType, FunctionType.ConstrainedFunction constrainedFunction) {
        return null;
    }

    @Override
    public Boolean visit(ArrayLikeType.TupleType tupleType, FunctionType.AnyFunction anyFunction) {
        return null;
    }

    @Override
    public Boolean visit(ArrayLikeType.TupleType tupleType, FunctionType.ConstrainedFunction constrainedFunction) {
        return null;
    }
}
