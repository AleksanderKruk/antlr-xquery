package com.github.akruk.antlrquery.typesystem.types.itemtypes;

sealed public interface AtomicType extends ConcreteItemType
        permits
        NumberType,
        StringType,
        BooleanType,
        RegexType
{


}
