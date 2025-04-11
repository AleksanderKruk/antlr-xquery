package com.github.akruk.antlrxquery.typesystem.defaults;

import java.util.function.BiPredicate;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;

public class XQueryEnumEmptySequenceType extends XQueryEnumSequenceType {
    public XQueryEnumEmptySequenceType() {
        super(null, XQueryOccurence.ZERO);
    }

}
