package com.github.akruk.antlrquery.evaluator.values.operations;

import java.util.function.BiPredicate;

import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValues;

@SuppressWarnings("unchecked")
public abstract class ValueBinaryOperationPredicate
{
    private static final int STRING = AntlrQueryValues.STRING.ordinal();
    private static final int ELEMENT = AntlrQueryValues.ELEMENT.ordinal();
    private static final int BOOLEAN = AntlrQueryValues.BOOLEAN.ordinal();
    private static final int NUMBER = AntlrQueryValues.NUMBER.ordinal();
    private static final int ERROR = AntlrQueryValues.ERROR.ordinal();
    private static final int MAP = AntlrQueryValues.MAP.ordinal();
    private static final int ARRAY = AntlrQueryValues.ARRAY.ordinal();
    private static final int FUNCTION = AntlrQueryValues.FUNCTION.ordinal();
    private static final int EMPTY_SEQUENCE = AntlrQueryValues.EMPTY_SEQUENCE.ordinal();
    private static final int SEQUENCE = AntlrQueryValues.SEQUENCE.ordinal();
    private static final int typesCount = AntlrQueryValues.values().length;

    protected final BiPredicate<AntlrQueryValue, AntlrQueryValue>[][] automaton;

    public ValueBinaryOperationPredicate()
    {
        this.automaton = getAutomaton();
    }


    private BiPredicate<AntlrQueryValue, AntlrQueryValue>[][] getAutomaton() {
        final BiPredicate<AntlrQueryValue, AntlrQueryValue>[][] automaton = new BiPredicate[typesCount][typesCount];
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


    public abstract boolean errorErrorOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean errorElementOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean errorBooleanOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean errorNumberOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean errorStringOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean errorMapOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean errorArrayOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean errorFunctionOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean errorEmptySequenceOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean errorSequenceOperation(AntlrQueryValue x, AntlrQueryValue y);


    // ELEMENT operation
    public abstract boolean elementErrorOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean elementElementOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean elementBooleanOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean elementNumberOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean elementStringOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean elementMapOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean elementArrayOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean elementFunctionOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean elementEmptySequenceOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean elementSequenceOperation(AntlrQueryValue x, AntlrQueryValue y);



    // MAP operationboolean
    public abstract boolean mapErrorOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean mapElementOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean mapBooleanOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean mapNumberOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean mapStringOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean mapMapOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean mapArrayOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean mapFunctionOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean mapEmptySequenceOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean mapSequenceOperation(AntlrQueryValue x, AntlrQueryValue y);

// ARRAY operations
    public abstract boolean arrayErrorOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean arrayElementOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean arrayBooleanOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean arrayNumberOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean arrayStringOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean arrayMapOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean arrayArrayOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean arrayFunctionOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean arrayEmptySequenceOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean arraySequenceOperation(AntlrQueryValue x, AntlrQueryValue y);


    // FUNCTION operboolean
    public abstract boolean functionErrorOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean functionElementOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean functionBooleanOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean functionNumberOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean functionStringOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean functionMapOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean functionArrayOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean functionFunctionOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean functionEmptySequenceOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean functionSequenceOperation(AntlrQueryValue x, AntlrQueryValue y);

    // BOOLEAN operaboolean
    public abstract boolean booleanErrorOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean booleanElementOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean booleanBooleanOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean booleanNumberOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean booleanStringOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean booleanMapOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean booleanArrayOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean booleanFunctionOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean booleanEmptySequenceOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean booleanSequenceOperation(AntlrQueryValue x, AntlrQueryValue y);


    public abstract boolean stringErrorOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean stringElementOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean stringBooleanOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean stringNumberOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean stringStringOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean stringMapOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean stringArrayOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean stringFunctionOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean stringEmptySequenceOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean stringSequenceOperation(AntlrQueryValue x, AntlrQueryValue y);


    public abstract boolean numberErrorOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean numberElementOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean numberBooleanOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean numberNumberOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean numberStringOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean numberMapOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean numberArrayOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean numberFunctionOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean numberEmptySequenceOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean numberSequenceOperation(AntlrQueryValue x, AntlrQueryValue y);

    public abstract boolean recordErrorOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean recordElementOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean recordBooleanOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean recordNumberOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean recordStringOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean recordMapOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean recordArrayOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean recordFunctionOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean recordEmptySequenceOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean recordSequenceOperation(AntlrQueryValue x, AntlrQueryValue y);

    public abstract boolean emptySequenceErrorOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean emptySequenceElementOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean emptySequenceBooleanOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean emptySequenceNumberOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean emptySequenceStringOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean emptySequenceMapOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean emptySequenceArrayOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean emptySequenceFunctionOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean emptySequenceEmptySequenceOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean emptySequenceSequenceOperation(AntlrQueryValue x, AntlrQueryValue y);

    public abstract boolean sequenceErrorOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean sequenceElementOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean sequenceBooleanOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean sequenceNumberOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean sequenceStringOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean sequenceMapOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean sequenceArrayOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean sequenceFunctionOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean sequenceEmptySequenceOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract boolean sequenceSequenceOperation(AntlrQueryValue x, AntlrQueryValue y);

}
