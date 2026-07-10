package com.github.akruk.antlrxquery.typesystem.typeoperations.occurence;

import com.github.akruk.antlrxquery.typesystem.types.Cardinality;

public class IsValueComparableWith
{
    public boolean isValueComparableWith(Cardinality o1, Cardinality o2) {
        final boolean validLeft = o1.isZero() || o1.isOne() || o1.isZeroOrOne();
        final boolean validRight = o2.isZero() || o2.isOne() || o2.isZeroOrOne();
        return validLeft && validRight;
    }


}
