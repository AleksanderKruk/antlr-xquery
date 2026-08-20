package com.github.akruk.antlrquery.evaluator.values.operations;

import java.util.Map;

import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;

public class ValueEquality extends ValueBinaryOperationPredicate {

    public boolean valueEquals(final AntlrQueryValue x, final AntlrQueryValue y) {
        return automaton[x.valueTypeOrdinal][y.valueTypeOrdinal].test(x, y) ;
    }



    @Override
    public boolean errorErrorOperation(final AntlrQueryValue x, final AntlrQueryValue y) {
        return true;
    }

    @Override
    public boolean errorElementOperation(final AntlrQueryValue x, final AntlrQueryValue y) {
        return false;
    }

    @Override
    public boolean errorBooleanOperation(final AntlrQueryValue x, final AntlrQueryValue y) {
        return false;
    }

    @Override
    public boolean errorNumberOperation(final AntlrQueryValue x, final AntlrQueryValue y) {
        return false;
    }

    @Override
    public boolean errorStringOperation(final AntlrQueryValue x, final AntlrQueryValue y) {
        return false;
    }

    @Override
    public boolean errorMapOperation(final AntlrQueryValue x, final AntlrQueryValue y) {
        return false;
    }

    @Override
    public boolean errorArrayOperation(final AntlrQueryValue x, final AntlrQueryValue y) {
        return false;
    }

    @Override
    public boolean errorFunctionOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean elementErrorOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean elementElementOperation(final AntlrQueryValue x, final AntlrQueryValue y) {
        return x.node == y.node;
    }

    @Override
    public boolean elementBooleanOperation(final AntlrQueryValue x, final AntlrQueryValue y) {
        return false;
    }

    @Override
    public boolean elementNumberOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean elementStringOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean elementMapOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean elementArrayOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean elementFunctionOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean mapErrorOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean mapElementOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean mapBooleanOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean mapNumberOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean mapStringOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean mapMapOperation(final AntlrQueryValue x, final AntlrQueryValue y) {
        if (x.mapEntries.size() != y.mapEntries.size()) return false;
        for (final Map.Entry<AntlrQueryValue, AntlrQueryValue> entry : x.mapEntries.entrySet()) {
            final AntlrQueryValue yValue = y.mapEntries.get(entry.getKey());
            if (yValue == null || !entry.getValue().equals(yValue)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean mapArrayOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean mapFunctionOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean arrayErrorOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean arrayElementOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean arrayBooleanOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean arrayNumberOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean arrayStringOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean arrayMapOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean arrayArrayOperation(final AntlrQueryValue x, final AntlrQueryValue y) {
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
    public boolean arrayFunctionOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean functionErrorOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean functionElementOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean functionBooleanOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean functionNumberOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean functionStringOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean functionMapOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean functionArrayOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean functionFunctionOperation(final AntlrQueryValue x, final AntlrQueryValue y) {
        return x.functionValue.equals(y.functionValue);
    }

    @Override
    public boolean booleanErrorOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean booleanElementOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean booleanBooleanOperation(final AntlrQueryValue x, final AntlrQueryValue y) {
        return x.booleanValue == y.booleanValue;
    }

    @Override
    public boolean booleanNumberOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean booleanStringOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean booleanMapOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean booleanArrayOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean booleanFunctionOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean stringErrorOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean stringElementOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean stringBooleanOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean stringNumberOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean stringStringOperation(final AntlrQueryValue x, final AntlrQueryValue y) {
        return x.stringValue.compareTo(y.stringValue) == 0;
    }

    @Override
    public boolean stringMapOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean stringArrayOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean stringFunctionOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean numberErrorOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean numberElementOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean numberBooleanOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean numberNumberOperation(final AntlrQueryValue x, final AntlrQueryValue y) {
        return x.numericValue.compareTo(y.numericValue) == 0;
    }

    @Override
    public boolean numberStringOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean numberMapOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean numberArrayOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean numberFunctionOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean recordErrorOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean recordElementOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean recordBooleanOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean recordNumberOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean recordStringOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean recordMapOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean recordArrayOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean recordFunctionOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean errorEmptySequenceOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean elementEmptySequenceOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean mapEmptySequenceOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean functionEmptySequenceOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean booleanEmptySequenceOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean stringEmptySequenceOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean numberEmptySequenceOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean recordEmptySequenceOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean emptySequenceErrorOperation(final AntlrQueryValue x, final AntlrQueryValue y) {
        return false;
    }

    @Override
    public boolean emptySequenceElementOperation(final AntlrQueryValue x, final AntlrQueryValue y) {
        return false;
    }

    @Override
    public boolean emptySequenceBooleanOperation(final AntlrQueryValue x, final AntlrQueryValue y) {
        return false;
    }

    @Override
    public boolean emptySequenceNumberOperation(final AntlrQueryValue x, final AntlrQueryValue y) {
        return false;
    }

    @Override
    public boolean emptySequenceStringOperation(final AntlrQueryValue x, final AntlrQueryValue y) {
        return false;
    }

    @Override
    public boolean emptySequenceMapOperation(final AntlrQueryValue x, final AntlrQueryValue y) {
        return false;
    }

    @Override
    public boolean emptySequenceArrayOperation(final AntlrQueryValue x, final AntlrQueryValue y) {
        return false;
    }

    @Override
    public boolean emptySequenceFunctionOperation(final AntlrQueryValue x, final AntlrQueryValue y) {
        return false;
    }

    @Override
    public boolean emptySequenceEmptySequenceOperation(final AntlrQueryValue x, final AntlrQueryValue y) {
        return true;
    }

    @Override
    public boolean errorSequenceOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean elementSequenceOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean mapSequenceOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean arrayEmptySequenceOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean arraySequenceOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean functionSequenceOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean booleanSequenceOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean stringSequenceOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean numberSequenceOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean recordSequenceOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean emptySequenceSequenceOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean sequenceErrorOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean sequenceElementOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean sequenceBooleanOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean sequenceNumberOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean sequenceStringOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean sequenceMapOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean sequenceArrayOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean sequenceFunctionOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean sequenceEmptySequenceOperation(final AntlrQueryValue x, final AntlrQueryValue y) {

        return false;
    }

    @Override
    public boolean sequenceSequenceOperation(final AntlrQueryValue x, final AntlrQueryValue y) {
        if (x.size != y.size)
            return false;
        for (int i = 0; i < x.sequence.size(); i++) {
            final AntlrQueryValue v1 = x.sequence.get(i);
            final AntlrQueryValue v2 = y.sequence.get(i);
            if (!v1.equals(v2)) {
                return false;
            }
        }
        return true;
    }



}
