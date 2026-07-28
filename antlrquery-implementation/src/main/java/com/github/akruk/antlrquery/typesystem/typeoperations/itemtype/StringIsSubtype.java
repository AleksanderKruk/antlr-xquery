package com.github.akruk.antlrquery.typesystem.typeoperations.itemtype;


import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.StringType;
import com.github.akruk.visitorannotations.Visitor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@Visitor(name = "StringTypeVisitor", classes = {StringType.class, StringType.class})
@DefaultQualifier(NonNull.class)
public class StringIsSubtype implements StringTypeVisitor<Boolean> {

    @Override
    public Boolean visit(StringType.StringNonEnum stringNonEnum, StringType.StringNonEnum stringNonEnum2) {
        return Cardinalities.isSubtype(stringNonEnum.cardinality(), stringNonEnum2.cardinality());
    }

    @Override
    public Boolean visit(StringType.StringNonEnum stringNonEnum, StringType.StringEnum stringEnum) {
        return false;
    }

    @Override
    public Boolean visit(StringType.StringEnum stringEnum, StringType.StringNonEnum stringNonEnum) {
        return Cardinalities.isSubtype(stringEnum.cardinality(), stringNonEnum.cardinality());
    }

    @Override
    public Boolean visit(StringType.StringEnum stringEnum, StringType.StringEnum stringEnum2) {
        return stringEnum2.members().containsAll(stringEnum.members());
    }

}
