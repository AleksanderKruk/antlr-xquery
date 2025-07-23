package com.github.akruk.antlrxquery.evaluator.values.operations;

import java.util.Map;

import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;

public class ValueEquality extends ValueBinaryOperationPredicate {

    public boolean valueEquals(XQueryValue x, XQueryValue y) {
        return automaton[x.valueTypeOrdinal][y.valueTypeOrdinal].test(x, y) ;
    }



    @Override
    public boolean errorErrorOperation(XQueryValue x, XQueryValue y) {
        return true;
    }

    @Override
    public boolean errorElementOperation(XQueryValue x, XQueryValue y) {
        return false;
    }

    @Override
    public boolean errorBooleanOperation(XQueryValue x, XQueryValue y) {
        return false;
    }

    @Override
    public boolean errorNumberOperation(XQueryValue x, XQueryValue y) {
        return false;
    }

    @Override
    public boolean errorStringOperation(XQueryValue x, XQueryValue y) {
        return false;
    }

    @Override
    public boolean errorMapOperation(XQueryValue x, XQueryValue y) {
        return false;
    }

    @Override
    public boolean errorArrayOperation(XQueryValue x, XQueryValue y) {
        return false;
    }

    @Override
    public boolean errorFunctionOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean elementErrorOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean elementElementOperation(XQueryValue x, XQueryValue y) {
        return x.node == y.node;
    }

    @Override
    public boolean elementBooleanOperation(XQueryValue x, XQueryValue y) {
        return false;
    }

    @Override
    public boolean elementNumberOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean elementStringOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean elementMapOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean elementArrayOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean elementFunctionOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean mapErrorOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean mapElementOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean mapBooleanOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean mapNumberOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean mapStringOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean mapMapOperation(XQueryValue x, XQueryValue y) {
        if (x.mapEntries.size() != y.mapEntries.size()) return false;
        for (Map.Entry<XQueryValue, XQueryValue> entry : x.mapEntries.entrySet()) {
            XQueryValue yValue = y.mapEntries.get(entry.getKey());
            if (yValue == null || !entry.getValue().equals(yValue)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean mapArrayOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean mapFunctionOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean arrayErrorOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean arrayElementOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean arrayBooleanOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean arrayNumberOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean arrayStringOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean arrayMapOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean arrayArrayOperation(XQueryValue x, XQueryValue y) {
        if (x.arrayMembers.size() != y.arrayMembers.size())
            return false;
        for (int i = 0; i < x.arrayMembers.size(); i++) {
            if (!x.arrayMembers.get(i).equals(y.arrayMembers.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean arrayFunctionOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean functionErrorOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean functionElementOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean functionBooleanOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean functionNumberOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean functionStringOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean functionMapOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean functionArrayOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean functionFunctionOperation(XQueryValue x, XQueryValue y) {
        return x.functionValue.equals(y.functionValue);
    }

    @Override
    public boolean booleanErrorOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean booleanElementOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean booleanBooleanOperation(XQueryValue x, XQueryValue y) {
        return x.booleanValue == y.booleanValue;
    }

    @Override
    public boolean booleanNumberOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean booleanStringOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean booleanMapOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean booleanArrayOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean booleanFunctionOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean stringErrorOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean stringElementOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean stringBooleanOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean stringNumberOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean stringStringOperation(XQueryValue x, XQueryValue y) {
        return x.stringValue.compareTo(y.stringValue) == 0;
    }

    @Override
    public boolean stringMapOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean stringArrayOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean stringFunctionOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean numberErrorOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean numberElementOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean numberBooleanOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean numberNumberOperation(XQueryValue x, XQueryValue y) {
        return x.numericValue.compareTo(y.numericValue) == 0;
    }

    @Override
    public boolean numberStringOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean numberMapOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean numberArrayOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean numberFunctionOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean recordErrorOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean recordElementOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean recordBooleanOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean recordNumberOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean recordStringOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean recordMapOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean recordArrayOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean recordFunctionOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean errorEmptySequenceOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean elementEmptySequenceOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean mapEmptySequenceOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean functionEmptySequenceOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean booleanEmptySequenceOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean stringEmptySequenceOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean numberEmptySequenceOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean recordEmptySequenceOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean emptySequenceErrorOperation(XQueryValue x, XQueryValue y) {
        return false;
    }

    @Override
    public boolean emptySequenceElementOperation(XQueryValue x, XQueryValue y) {
        return false;
    }

    @Override
    public boolean emptySequenceBooleanOperation(XQueryValue x, XQueryValue y) {
        return false;
    }

    @Override
    public boolean emptySequenceNumberOperation(XQueryValue x, XQueryValue y) {
        return false;
    }

    @Override
    public boolean emptySequenceStringOperation(XQueryValue x, XQueryValue y) {
        return false;
    }

    @Override
    public boolean emptySequenceMapOperation(XQueryValue x, XQueryValue y) {
        return false;
    }

    @Override
    public boolean emptySequenceArrayOperation(XQueryValue x, XQueryValue y) {
        return false;
    }

    @Override
    public boolean emptySequenceFunctionOperation(XQueryValue x, XQueryValue y) {
        return false;
    }

    @Override
    public boolean emptySequenceEmptySequenceOperation(XQueryValue x, XQueryValue y) {
        return true;
    }

    @Override
    public boolean errorSequenceOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean elementSequenceOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean mapSequenceOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean arrayEmptySequenceOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean arraySequenceOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean functionSequenceOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean booleanSequenceOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean stringSequenceOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean numberSequenceOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean recordSequenceOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean emptySequenceSequenceOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean sequenceErrorOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean sequenceElementOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean sequenceBooleanOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean sequenceNumberOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean sequenceStringOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean sequenceMapOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean sequenceArrayOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean sequenceFunctionOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean sequenceEmptySequenceOperation(XQueryValue x, XQueryValue y) {

        return false;
    }

    @Override
    public boolean sequenceSequenceOperation(XQueryValue x, XQueryValue y) {
        if (x.size != y.size)
            return false;
        for (int i = 0; i < x.sequence.size(); i++) {
            final XQueryValue v1 = x.sequence.get(i);
            final XQueryValue v2 = y.sequence.get(i);
            if (!v1.equals(v2.sequence.get(i))) {
                return false;
            }
        }
        return true;
    }



}
