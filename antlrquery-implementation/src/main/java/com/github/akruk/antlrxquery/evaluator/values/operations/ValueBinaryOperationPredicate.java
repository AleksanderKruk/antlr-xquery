package com.github.akruk.antlrxquery.evaluator.values.operations;

import java.util.function.BiPredicate;

import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValues;

@SuppressWarnings("unchecked")
public abstract class ValueBinaryOperationPredicate
{
    private static final int STRING = XQueryValues.STRING.ordinal();
    private static final int ELEMENT = XQueryValues.ELEMENT.ordinal();
    private static final int BOOLEAN = XQueryValues.BOOLEAN.ordinal();
    private static final int NUMBER = XQueryValues.NUMBER.ordinal();
    private static final int ERROR = XQueryValues.ERROR.ordinal();
    private static final int MAP = XQueryValues.MAP.ordinal();
    private static final int ARRAY = XQueryValues.ARRAY.ordinal();
    private static final int FUNCTION = XQueryValues.FUNCTION.ordinal();
    private static final int EMPTY_SEQUENCE = XQueryValues.EMPTY_SEQUENCE.ordinal();
    private static final int SEQUENCE = XQueryValues.SEQUENCE.ordinal();
    private static final int typesCount = XQueryValues.values().length;

    protected final BiPredicate<XQueryValue, XQueryValue>[][] automaton;

    public ValueBinaryOperationPredicate()
    {
        this.automaton = getAutomaton();
    }


    private BiPredicate<XQueryValue, XQueryValue>[][] getAutomaton() {
        final BiPredicate<XQueryValue, XQueryValue>[][] automaton = new BiPredicate[typesCount][typesCount];
        automaton[ERROR][ERROR]          = this::errorErrorOperation;
        automaton[ERROR][ELEMENT]        = this::errorElementOperation;
        automaton[ERROR][BOOLEAN]        = this::errorBooleanOperation;
        automaton[ERROR][NUMBER]         = this::errorNumberOperation;
        automaton[ERROR][STRING]         = this::errorStringOperation;
        automaton[ERROR][MAP]            = this::errorMapOperation;
        automaton[ERROR][ARRAY]          = this::errorArrayOperation;
        automaton[ERROR][FUNCTION]       = this::errorFunctionOperation;
        automaton[ERROR][EMPTY_SEQUENCE] = this::errorEmptySequenceOperation;
        automaton[ERROR][SEQUENCE]       = this::errorSequenceOperation;

        automaton[ELEMENT][ERROR]          = this::elementErrorOperation;
        automaton[ELEMENT][ELEMENT]        = this::elementElementOperation;
        automaton[ELEMENT][BOOLEAN]        = this::elementBooleanOperation;
        automaton[ELEMENT][NUMBER]         = this::elementNumberOperation;
        automaton[ELEMENT][STRING]         = this::elementStringOperation;
        automaton[ELEMENT][MAP]            = this::elementMapOperation;
        automaton[ELEMENT][ARRAY]          = this::elementArrayOperation;
        automaton[ELEMENT][FUNCTION]       = this::elementFunctionOperation;
        automaton[ELEMENT][EMPTY_SEQUENCE] = this::elementEmptySequenceOperation;
        automaton[ELEMENT][SEQUENCE]       = this::elementSequenceOperation;

        automaton[MAP][ERROR]          = this::mapErrorOperation;
        automaton[MAP][ELEMENT]        = this::mapElementOperation;
        automaton[MAP][BOOLEAN]        = this::mapBooleanOperation;
        automaton[MAP][NUMBER]         = this::mapNumberOperation;
        automaton[MAP][STRING]         = this::mapStringOperation;
        automaton[MAP][MAP]            = this::mapMapOperation;
        automaton[MAP][ARRAY]          = this::mapArrayOperation;
        automaton[MAP][FUNCTION]       = this::mapFunctionOperation;
        automaton[MAP][EMPTY_SEQUENCE] = this::mapEmptySequenceOperation;
        automaton[MAP][SEQUENCE]       = this::mapSequenceOperation;

        automaton[ARRAY][ERROR]          = this::arrayErrorOperation;
        automaton[ARRAY][ELEMENT]        = this::arrayElementOperation;
        automaton[ARRAY][BOOLEAN]        = this::arrayBooleanOperation;
        automaton[ARRAY][NUMBER]         = this::arrayNumberOperation;
        automaton[ARRAY][STRING]         = this::arrayStringOperation;
        automaton[ARRAY][MAP]            = this::arrayMapOperation;
        automaton[ARRAY][ARRAY]          = this::arrayArrayOperation;
        automaton[ARRAY][FUNCTION]       = this::arrayFunctionOperation;
        automaton[ARRAY][EMPTY_SEQUENCE] = this::arrayEmptySequenceOperation;
        automaton[ARRAY][SEQUENCE]       = this::arraySequenceOperation;

        automaton[FUNCTION][ERROR]          = this::functionErrorOperation;
        automaton[FUNCTION][ELEMENT]        = this::functionElementOperation;
        automaton[FUNCTION][BOOLEAN]        = this::functionBooleanOperation;
        automaton[FUNCTION][NUMBER]         = this::functionNumberOperation;
        automaton[FUNCTION][STRING]         = this::functionStringOperation;
        automaton[FUNCTION][MAP]            = this::functionMapOperation;
        automaton[FUNCTION][ARRAY]          = this::functionArrayOperation;
        automaton[FUNCTION][FUNCTION]       = this::functionFunctionOperation;
        automaton[FUNCTION][EMPTY_SEQUENCE] = this::functionEmptySequenceOperation;
        automaton[FUNCTION][SEQUENCE]       = this::functionSequenceOperation;

        automaton[BOOLEAN][ERROR]          = this::booleanErrorOperation;
        automaton[BOOLEAN][ELEMENT]        = this::booleanElementOperation;
        automaton[BOOLEAN][BOOLEAN]        = this::booleanBooleanOperation;
        automaton[BOOLEAN][NUMBER]         = this::booleanNumberOperation;
        automaton[BOOLEAN][STRING]         = this::booleanStringOperation;
        automaton[BOOLEAN][MAP]            = this::booleanMapOperation;
        automaton[BOOLEAN][ARRAY]          = this::booleanArrayOperation;
        automaton[BOOLEAN][FUNCTION]       = this::booleanFunctionOperation;
        automaton[BOOLEAN][EMPTY_SEQUENCE] = this::booleanEmptySequenceOperation;
        automaton[BOOLEAN][SEQUENCE]       = this::booleanSequenceOperation;

        automaton[STRING][ERROR]          = this::stringErrorOperation;
        automaton[STRING][ELEMENT]        = this::stringElementOperation;
        automaton[STRING][BOOLEAN]        = this::stringBooleanOperation;
        automaton[STRING][NUMBER]         = this::stringNumberOperation;
        automaton[STRING][STRING]         = this::stringStringOperation;
        automaton[STRING][MAP]            = this::stringMapOperation;
        automaton[STRING][ARRAY]          = this::stringArrayOperation;
        automaton[STRING][FUNCTION]       = this::stringFunctionOperation;
        automaton[STRING][EMPTY_SEQUENCE] = this::stringEmptySequenceOperation;
        automaton[STRING][SEQUENCE]       = this::stringSequenceOperation;

        automaton[NUMBER][ERROR]          = this::numberErrorOperation;
        automaton[NUMBER][ELEMENT]        = this::numberElementOperation;
        automaton[NUMBER][BOOLEAN]        = this::numberBooleanOperation;
        automaton[NUMBER][NUMBER]         = this::numberNumberOperation;
        automaton[NUMBER][STRING]         = this::numberStringOperation;
        automaton[NUMBER][MAP]            = this::numberMapOperation;
        automaton[NUMBER][ARRAY]          = this::numberArrayOperation;
        automaton[NUMBER][FUNCTION]       = this::numberFunctionOperation;
        automaton[NUMBER][EMPTY_SEQUENCE] = this::numberEmptySequenceOperation;
        automaton[NUMBER][SEQUENCE]       = this::numberSequenceOperation;

        automaton[EMPTY_SEQUENCE][ERROR]          = this::emptySequenceErrorOperation;
        automaton[EMPTY_SEQUENCE][ELEMENT]        = this::emptySequenceElementOperation;
        automaton[EMPTY_SEQUENCE][BOOLEAN]        = this::emptySequenceBooleanOperation;
        automaton[EMPTY_SEQUENCE][NUMBER]         = this::emptySequenceNumberOperation;
        automaton[EMPTY_SEQUENCE][STRING]         = this::emptySequenceStringOperation;
        automaton[EMPTY_SEQUENCE][MAP]            = this::emptySequenceMapOperation;
        automaton[EMPTY_SEQUENCE][ARRAY]          = this::emptySequenceArrayOperation;
        automaton[EMPTY_SEQUENCE][FUNCTION]       = this::emptySequenceFunctionOperation;
        automaton[EMPTY_SEQUENCE][EMPTY_SEQUENCE] = this::emptySequenceEmptySequenceOperation;
        automaton[EMPTY_SEQUENCE][SEQUENCE]       = this::emptySequenceSequenceOperation;

        automaton[SEQUENCE][ERROR]          = this::sequenceErrorOperation;
        automaton[SEQUENCE][ELEMENT]        = this::sequenceElementOperation;
        automaton[SEQUENCE][BOOLEAN]        = this::sequenceBooleanOperation;
        automaton[SEQUENCE][NUMBER]         = this::sequenceNumberOperation;
        automaton[SEQUENCE][STRING]         = this::sequenceStringOperation;
        automaton[SEQUENCE][MAP]            = this::sequenceMapOperation;
        automaton[SEQUENCE][ARRAY]          = this::sequenceArrayOperation;
        automaton[SEQUENCE][FUNCTION]       = this::sequenceFunctionOperation;
        automaton[SEQUENCE][EMPTY_SEQUENCE] = this::sequenceEmptySequenceOperation;
        automaton[SEQUENCE][SEQUENCE]       = this::sequenceSequenceOperation;


        return automaton;
    }


    public abstract boolean errorErrorOperation(XQueryValue x, XQueryValue y);
    public abstract boolean errorElementOperation(XQueryValue x, XQueryValue y);
    public abstract boolean errorBooleanOperation(XQueryValue x, XQueryValue y);
    public abstract boolean errorNumberOperation(XQueryValue x, XQueryValue y);
    public abstract boolean errorStringOperation(XQueryValue x, XQueryValue y);
    public abstract boolean errorMapOperation(XQueryValue x, XQueryValue y);
    public abstract boolean errorArrayOperation(XQueryValue x, XQueryValue y);
    public abstract boolean errorFunctionOperation(XQueryValue x, XQueryValue y);
    public abstract boolean errorEmptySequenceOperation(XQueryValue x, XQueryValue y);
    public abstract boolean errorSequenceOperation(XQueryValue x, XQueryValue y);


    // ELEMENT operation
    public abstract boolean elementErrorOperation(XQueryValue x, XQueryValue y);
    public abstract boolean elementElementOperation(XQueryValue x, XQueryValue y);
    public abstract boolean elementBooleanOperation(XQueryValue x, XQueryValue y);
    public abstract boolean elementNumberOperation(XQueryValue x, XQueryValue y);
    public abstract boolean elementStringOperation(XQueryValue x, XQueryValue y);
    public abstract boolean elementMapOperation(XQueryValue x, XQueryValue y);
    public abstract boolean elementArrayOperation(XQueryValue x, XQueryValue y);
    public abstract boolean elementFunctionOperation(XQueryValue x, XQueryValue y);
    public abstract boolean elementEmptySequenceOperation(XQueryValue x, XQueryValue y);
    public abstract boolean elementSequenceOperation(XQueryValue x, XQueryValue y);



    // MAP operationboolean
    public abstract boolean mapErrorOperation(XQueryValue x, XQueryValue y);
    public abstract boolean mapElementOperation(XQueryValue x, XQueryValue y);
    public abstract boolean mapBooleanOperation(XQueryValue x, XQueryValue y);
    public abstract boolean mapNumberOperation(XQueryValue x, XQueryValue y);
    public abstract boolean mapStringOperation(XQueryValue x, XQueryValue y);
    public abstract boolean mapMapOperation(XQueryValue x, XQueryValue y);
    public abstract boolean mapArrayOperation(XQueryValue x, XQueryValue y);
    public abstract boolean mapFunctionOperation(XQueryValue x, XQueryValue y);
    public abstract boolean mapEmptySequenceOperation(XQueryValue x, XQueryValue y);
    public abstract boolean mapSequenceOperation(XQueryValue x, XQueryValue y);

// ARRAY operations
    public abstract boolean arrayErrorOperation(XQueryValue x, XQueryValue y);
    public abstract boolean arrayElementOperation(XQueryValue x, XQueryValue y);
    public abstract boolean arrayBooleanOperation(XQueryValue x, XQueryValue y);
    public abstract boolean arrayNumberOperation(XQueryValue x, XQueryValue y);
    public abstract boolean arrayStringOperation(XQueryValue x, XQueryValue y);
    public abstract boolean arrayMapOperation(XQueryValue x, XQueryValue y);
    public abstract boolean arrayArrayOperation(XQueryValue x, XQueryValue y);
    public abstract boolean arrayFunctionOperation(XQueryValue x, XQueryValue y);
    public abstract boolean arrayEmptySequenceOperation(XQueryValue x, XQueryValue y);
    public abstract boolean arraySequenceOperation(XQueryValue x, XQueryValue y);


    // FUNCTION operboolean
    public abstract boolean functionErrorOperation(XQueryValue x, XQueryValue y);
    public abstract boolean functionElementOperation(XQueryValue x, XQueryValue y);
    public abstract boolean functionBooleanOperation(XQueryValue x, XQueryValue y);
    public abstract boolean functionNumberOperation(XQueryValue x, XQueryValue y);
    public abstract boolean functionStringOperation(XQueryValue x, XQueryValue y);
    public abstract boolean functionMapOperation(XQueryValue x, XQueryValue y);
    public abstract boolean functionArrayOperation(XQueryValue x, XQueryValue y);
    public abstract boolean functionFunctionOperation(XQueryValue x, XQueryValue y);
    public abstract boolean functionEmptySequenceOperation(XQueryValue x, XQueryValue y);
    public abstract boolean functionSequenceOperation(XQueryValue x, XQueryValue y);

    // BOOLEAN operaboolean
    public abstract boolean booleanErrorOperation(XQueryValue x, XQueryValue y);
    public abstract boolean booleanElementOperation(XQueryValue x, XQueryValue y);
    public abstract boolean booleanBooleanOperation(XQueryValue x, XQueryValue y);
    public abstract boolean booleanNumberOperation(XQueryValue x, XQueryValue y);
    public abstract boolean booleanStringOperation(XQueryValue x, XQueryValue y);
    public abstract boolean booleanMapOperation(XQueryValue x, XQueryValue y);
    public abstract boolean booleanArrayOperation(XQueryValue x, XQueryValue y);
    public abstract boolean booleanFunctionOperation(XQueryValue x, XQueryValue y);
    public abstract boolean booleanEmptySequenceOperation(XQueryValue x, XQueryValue y);
    public abstract boolean booleanSequenceOperation(XQueryValue x, XQueryValue y);


    public abstract boolean stringErrorOperation(XQueryValue x, XQueryValue y);
    public abstract boolean stringElementOperation(XQueryValue x, XQueryValue y);
    public abstract boolean stringBooleanOperation(XQueryValue x, XQueryValue y);
    public abstract boolean stringNumberOperation(XQueryValue x, XQueryValue y);
    public abstract boolean stringStringOperation(XQueryValue x, XQueryValue y);
    public abstract boolean stringMapOperation(XQueryValue x, XQueryValue y);
    public abstract boolean stringArrayOperation(XQueryValue x, XQueryValue y);
    public abstract boolean stringFunctionOperation(XQueryValue x, XQueryValue y);
    public abstract boolean stringEmptySequenceOperation(XQueryValue x, XQueryValue y);
    public abstract boolean stringSequenceOperation(XQueryValue x, XQueryValue y);


    public abstract boolean numberErrorOperation(XQueryValue x, XQueryValue y);
    public abstract boolean numberElementOperation(XQueryValue x, XQueryValue y);
    public abstract boolean numberBooleanOperation(XQueryValue x, XQueryValue y);
    public abstract boolean numberNumberOperation(XQueryValue x, XQueryValue y);
    public abstract boolean numberStringOperation(XQueryValue x, XQueryValue y);
    public abstract boolean numberMapOperation(XQueryValue x, XQueryValue y);
    public abstract boolean numberArrayOperation(XQueryValue x, XQueryValue y);
    public abstract boolean numberFunctionOperation(XQueryValue x, XQueryValue y);
    public abstract boolean numberEmptySequenceOperation(XQueryValue x, XQueryValue y);
    public abstract boolean numberSequenceOperation(XQueryValue x, XQueryValue y);

    public abstract boolean recordErrorOperation(XQueryValue x, XQueryValue y);
    public abstract boolean recordElementOperation(XQueryValue x, XQueryValue y);
    public abstract boolean recordBooleanOperation(XQueryValue x, XQueryValue y);
    public abstract boolean recordNumberOperation(XQueryValue x, XQueryValue y);
    public abstract boolean recordStringOperation(XQueryValue x, XQueryValue y);
    public abstract boolean recordMapOperation(XQueryValue x, XQueryValue y);
    public abstract boolean recordArrayOperation(XQueryValue x, XQueryValue y);
    public abstract boolean recordFunctionOperation(XQueryValue x, XQueryValue y);
    public abstract boolean recordEmptySequenceOperation(XQueryValue x, XQueryValue y);
    public abstract boolean recordSequenceOperation(XQueryValue x, XQueryValue y);

    public abstract boolean emptySequenceErrorOperation(XQueryValue x, XQueryValue y);
    public abstract boolean emptySequenceElementOperation(XQueryValue x, XQueryValue y);
    public abstract boolean emptySequenceBooleanOperation(XQueryValue x, XQueryValue y);
    public abstract boolean emptySequenceNumberOperation(XQueryValue x, XQueryValue y);
    public abstract boolean emptySequenceStringOperation(XQueryValue x, XQueryValue y);
    public abstract boolean emptySequenceMapOperation(XQueryValue x, XQueryValue y);
    public abstract boolean emptySequenceArrayOperation(XQueryValue x, XQueryValue y);
    public abstract boolean emptySequenceFunctionOperation(XQueryValue x, XQueryValue y);
    public abstract boolean emptySequenceEmptySequenceOperation(XQueryValue x, XQueryValue y);
    public abstract boolean emptySequenceSequenceOperation(XQueryValue x, XQueryValue y);

    public abstract boolean sequenceErrorOperation(XQueryValue x, XQueryValue y);
    public abstract boolean sequenceElementOperation(XQueryValue x, XQueryValue y);
    public abstract boolean sequenceBooleanOperation(XQueryValue x, XQueryValue y);
    public abstract boolean sequenceNumberOperation(XQueryValue x, XQueryValue y);
    public abstract boolean sequenceStringOperation(XQueryValue x, XQueryValue y);
    public abstract boolean sequenceMapOperation(XQueryValue x, XQueryValue y);
    public abstract boolean sequenceArrayOperation(XQueryValue x, XQueryValue y);
    public abstract boolean sequenceFunctionOperation(XQueryValue x, XQueryValue y);
    public abstract boolean sequenceEmptySequenceOperation(XQueryValue x, XQueryValue y);
    public abstract boolean sequenceSequenceOperation(XQueryValue x, XQueryValue y);

}
