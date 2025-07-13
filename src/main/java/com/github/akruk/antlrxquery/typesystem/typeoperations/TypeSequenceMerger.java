
package com.github.akruk.antlrxquery.typesystem.typeoperations;

import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;

public interface TypeSequenceMerger {
    XQuerySequenceType sequenceMerge(XQuerySequenceType type1, XQuerySequenceType type2);
}