package com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions;

// import java.text.Collator;

import org.antlr.v4.runtime.Parser;

// import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;
import com.github.akruk.antlrxquery.evaluator.values.operations.EffectiveBooleanValue;

public class ComparisonFunctions {
    // private final XQueryValueFactory valueFactory;
    // private final EffectiveBooleanValue ebv;

    public ComparisonFunctions(final XQueryValueFactory valueFactory, final Parser targetParser, final EffectiveBooleanValue ebv) {
        // this.valueFactory = valueFactory;
        // this.ebv = ebv;
    }

    // private boolean deepEquals(
    //     XQueryValue a,
    //     XQueryValue b,
    //     Collator collator)
    // {
    //     if (a.type != b.type)
    //         return false;
    //     if (a.size != b.size)
    //         return false;
    //     switch(a.valueType) {
    //         case BOOLEAN: {
    //             return a.booleanValue == b.booleanValue;
    //         }
    //         case NUMBER: {
    //             return a.numericValue.compareTo(b.numericValue) == 0;
    //         }
    //         case STRING: {
    //             return collator.compare(a.stringValue, b.stringValue) == 0;
    //         }
    //         case ARRAY: {
    //             var aMembers = a.arrayMembers;
    //             var bMembers = b.arrayMembers;
    //             if (aMembers.size() != bMembers.size()) {
    //                 return false;
    //             }

    //             for (int i = 0; i < aMembers.size(); i++) {
    //                 var aValue = aMembers.get(i);
    //                 var bValue = bMembers.get(i);
    //                 if (!deepEquals(aValue, bValue, collator)) {
    //                     return false;
    //                 }
    //             }
    //             return true;
    //         }
    //         case MAP: {
    //             var aEntries = a.mapEntries;
    //             var bEntries = b.mapEntries;
    //             if (a.mapEntries.size() != b.mapEntries.size()) {
    //                 return false;
    //             }
    //             if (!aEntries.keySet().containsAll(bEntries.keySet()))
    //                 return false;
    //             for (var akey : aEntries.keySet()) {
    //                 var aValue = aEntries.get(akey);
    //                 var bValue = bEntries.get(akey);
    //                 if (!deepEquals(aValue, bValue, collator)) {
    //                     return false;
    //                 }
    //             }
    //             return true;
    //         }
    //         case ELEMENT: {

    //         }
    //         case EMPTY_SEQUENCE: {

    //         }
    //         case FUNCTION: {

    //         }
    //         case ERROR: {

    //         }
    //         case SEQUENCE: {

    //         }
    //     }
    //     return false;
    // }




}
