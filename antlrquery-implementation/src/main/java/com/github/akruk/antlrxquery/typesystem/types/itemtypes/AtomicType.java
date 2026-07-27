package com.github.akruk.antlrxquery.typesystem.types.itemtypes;

import com.github.akruk.antlrxquery.typesystem.types.NumericRange;

import java.util.regex.Pattern;

sealed public interface AtomicType extends ConcreteItemType
        permits
        AtomicType.NumberType,
        StringType,
        BooleanType,
        AtomicType.RegexType
{

    record NumberType(NumericRange range)
            implements com.github.akruk.antlrxquery.typesystem.types.itemtypes.AtomicType {
    }


    record RegexType(Pattern pattern)
            implements com.github.akruk.antlrxquery.typesystem.types.itemtypes.AtomicType {
    }

}
