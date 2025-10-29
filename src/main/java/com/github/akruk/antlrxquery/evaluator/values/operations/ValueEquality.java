package com.github.akruk.antlrxquery.evaluator.values.operations;

import java.util.Map;

import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;

public class ValueEquality extends ValueBinaryOperationPredicate {

    public boolean valueEquals(final XQueryValue x, final XQueryValue y) {
        return automaton[x.valueTypeOrdinal][y.valueTypeOrdinal].test(x, y) ;
    }



    @Override
    public boolean errorErrorOperation(final XQueryValue x, final XQueryValue y) {
        return true;
    }

    @Override
    public boolean errorElementOperation(final XQueryValue x, final XQueryValue y) {
        return false;
    }

    @Override
    public boolean errorBooleanOperation(final XQueryValue x, final XQueryValue y) {
        return false;
    }

    @Override
    public boolean errorNumberOperation(final XQueryValue x, final XQueryValue y) {
        return false;
    }

    @Override
    public boolean errorStringOperation(final XQueryValue x, final XQueryValue y) {
        return false;
    }

    @Override
    public boolean errorMapOperation(final XQueryValue x, final XQueryValue y) {
        return false;
    }

    @Override
    public boolean errorArrayOperation(final XQueryValue x, final XQueryValue y) {
        return false;
    }

    @Override
    public boolean errorFunctionOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean elementErrorOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean elementElementOperation(final XQueryValue x, final XQueryValue y) {
        return x.node == y.node;
    }

    @Override
    public boolean elementBooleanOperation(final XQueryValue x, final XQueryValue y) {
        return false;
    }

    @Override
    public boolean elementNumberOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean elementStringOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean elementMapOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean elementArrayOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean elementFunctionOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean mapErrorOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean mapElementOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean mapBooleanOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean mapNumberOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean mapStringOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean mapMapOperation(final XQueryValue x, final XQueryValue y) {
        if (x.mapEntries.size() != y.mapEntries.size()) return false;
        for (final Map.Entry<XQueryValue, XQueryValue> entry : x.mapEntries.entrySet()) {
            final XQueryValue yValue = y.mapEntries.get(entry.getKey());
            if (yValue == null || !entry.getValue().equals(yValue)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean mapArrayOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean mapFunctionOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean arrayErrorOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean arrayElementOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean arrayBooleanOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean arrayNumberOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean arrayStringOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean arrayMapOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean arrayArrayOperation(final XQueryValue x, final XQueryValue y) {
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
    public boolean arrayFunctionOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean functionErrorOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean functionElementOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean functionBooleanOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean functionNumberOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean functionStringOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean functionMapOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean functionArrayOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean functionFunctionOperation(final XQueryValue x, final XQueryValue y) {
        return x.functionValue.equals(y.functionValue);
    }

    @Override
    public boolean booleanErrorOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean booleanElementOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean booleanBooleanOperation(final XQueryValue x, final XQueryValue y) {
        return x.booleanValue == y.booleanValue;
    }

    @Override
    public boolean booleanNumberOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean booleanStringOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean booleanMapOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean booleanArrayOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean booleanFunctionOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean stringErrorOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean stringElementOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean stringBooleanOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean stringNumberOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean stringStringOperation(final XQueryValue x, final XQueryValue y) {
        return x.stringValue.compareTo(y.stringValue) == 0;
    }

    @Override
    public boolean stringMapOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean stringArrayOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean stringFunctionOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean numberErrorOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean numberElementOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean numberBooleanOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean numberNumberOperation(final XQueryValue x, final XQueryValue y) {
        return x.numericValue.compareTo(y.numericValue) == 0;
    }

    @Override
    public boolean numberStringOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean numberMapOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean numberArrayOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean numberFunctionOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean recordErrorOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean recordElementOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean recordBooleanOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean recordNumberOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean recordStringOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean recordMapOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean recordArrayOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean recordFunctionOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean errorEmptySequenceOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean elementEmptySequenceOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean mapEmptySequenceOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean functionEmptySequenceOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean booleanEmptySequenceOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean stringEmptySequenceOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean numberEmptySequenceOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean recordEmptySequenceOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean emptySequenceErrorOperation(final XQueryValue x, final XQueryValue y) {
        return false;
    }

    @Override
    public boolean emptySequenceElementOperation(final XQueryValue x, final XQueryValue y) {
        return false;
    }

    @Override
    public boolean emptySequenceBooleanOperation(final XQueryValue x, final XQueryValue y) {
        return false;
    }

    @Override
    public boolean emptySequenceNumberOperation(final XQueryValue x, final XQueryValue y) {
        return false;
    }

    @Override
    public boolean emptySequenceStringOperation(final XQueryValue x, final XQueryValue y) {
        return false;
    }

    @Override
    public boolean emptySequenceMapOperation(final XQueryValue x, final XQueryValue y) {
        return false;
    }

    @Override
    public boolean emptySequenceArrayOperation(final XQueryValue x, final XQueryValue y) {
        return false;
    }

    @Override
    public boolean emptySequenceFunctionOperation(final XQueryValue x, final XQueryValue y) {
        return false;
    }

    @Override
    public boolean emptySequenceEmptySequenceOperation(final XQueryValue x, final XQueryValue y) {
        return true;
    }

    @Override
    public boolean errorSequenceOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean elementSequenceOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean mapSequenceOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean arrayEmptySequenceOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean arraySequenceOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean functionSequenceOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean booleanSequenceOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean stringSequenceOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean numberSequenceOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean recordSequenceOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean emptySequenceSequenceOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean sequenceErrorOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean sequenceElementOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean sequenceBooleanOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean sequenceNumberOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean sequenceStringOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean sequenceMapOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean sequenceArrayOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean sequenceFunctionOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean sequenceEmptySequenceOperation(final XQueryValue x, final XQueryValue y) {

        return false;
    }

    @Override
    public boolean sequenceSequenceOperation(final XQueryValue x, final XQueryValue y) {
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
