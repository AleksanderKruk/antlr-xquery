package com.github.akruk.antlrquery.typesystem.typeoperations.itemtype;

import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.typeoperations.Types;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.ArrayLikeType;
import com.github.akruk.visitorannotations.Visitor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@Visitor(name = "ArrayVisitor", classes= {ArrayLikeType.class, ArrayLikeType.class})
@DefaultQualifier(NonNull.class)
public class ArrayTypeIsSubtype implements  ArrayVisitor<Boolean>{
    private final AntlrQueryTypeFactory typeFactory;

    public ArrayTypeIsSubtype(AntlrQueryTypeFactory typeFactory) {
        this.typeFactory = typeFactory;
    }

    @Override
    public Boolean visit(ArrayLikeType.ArrayType arrayType, ArrayLikeType.ArrayType arrayType2) {
        return Cardinalities.isSubSet(arrayType.cardinality(), arrayType2.cardinality())
                && Types.isSubtype(typeFactory, arrayType.memberType(), arrayType2.memberType());
    }

    @Override
    public Boolean visit(ArrayLikeType.ArrayType arrayType, ArrayLikeType.TupleType tupleType) {
        return false;
    }

    @Override
    public Boolean visit(ArrayLikeType.TupleType tupleType, ArrayLikeType.ArrayType arrayType) {
        AntlrQuerySequenceType[] members = tupleType.members();

        final Cardinality tupleLength = Cardinality.of(members.length);
        if (!Cardinalities.isSubSet(tupleLength, arrayType.cardinality())) {
            return false;
        }

        final AntlrQuerySequenceType targetMemberType = arrayType.memberType();
        for (AntlrQuerySequenceType member : members) {
            boolean isSub = Types.isSubtype(typeFactory, member, targetMemberType);
            if (!isSub) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Boolean visit(ArrayLikeType.TupleType tupleType, ArrayLikeType.TupleType tupleType2) {
        final AntlrQuerySequenceType[] sourceMembers = tupleType.members();
        final AntlrQuerySequenceType[] targetMembers = tupleType2.members();

        if (sourceMembers.length != targetMembers.length) {
            return false;
        }

        for (int i = 0; i < sourceMembers.length; i++) {
            boolean isSub = Types.isSubtype(typeFactory, sourceMembers[i], targetMembers[i]);
            if (!isSub) {
                return false;
            }
        }
        return true;
    }
}
