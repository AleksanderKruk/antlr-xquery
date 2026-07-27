package com.github.akruk.antlrxquery.evaluator.values.operations;

import com.github.akruk.antlrxquery.evaluator.values.XQueryValues;

public abstract class ValueBinaryDirectPredicate
{
    private static final int STRING = XQueryValues.STRING.ordinal();
    private static final int ELEMENT = XQueryValues.ELEMENT.ordinal();
    private static final int BOOLEAN = XQueryValues.BOOLEAN.ordinal();
    private static final int NUMBER = XQueryValues.NUMBER.ordinal();
    private static final int ERROR = XQueryValues.ERROR.ordinal();
    private static final int MAP = XQueryValues.MAP.ordinal();
    private static final int ARRAY = XQueryValues.ARRAY.ordinal();
    private static final int FUNCTION = XQueryValues.FUNCTION.ordinal();
    private static final int typesCount = XQueryValues.values().length;

    protected final boolean[][] automaton;

    public ValueBinaryDirectPredicate()
    {
        this.automaton = getAutomaton();
    }


    private boolean[][] getAutomaton() {
        final boolean[][] automaton = new boolean[typesCount][typesCount];
        automaton[ERROR][ERROR] = errorErrorValue();
        automaton[ERROR][ELEMENT] = errorElementValue();
        automaton[ERROR][BOOLEAN] = errorBooleanValue();
        automaton[ERROR][NUMBER] = errorNumberValue();
        automaton[ERROR][STRING] = errorStringValue();
        automaton[ERROR][MAP] = errorMapValue();
        automaton[ERROR][ARRAY] = errorArrayValue();
        automaton[ERROR][FUNCTION] = errorFunctionValue();

        automaton[ELEMENT][ERROR] = elementErrorValue();
        automaton[ELEMENT][ELEMENT] = elementElementValue();
        automaton[ELEMENT][BOOLEAN] = elementBooleanValue();
        automaton[ELEMENT][NUMBER] = elementNumberValue();
        automaton[ELEMENT][STRING] = elementStringValue();
        automaton[ELEMENT][MAP] = elementMapValue();
        automaton[ELEMENT][ARRAY] = elementArrayValue();
        automaton[ELEMENT][FUNCTION] = elementFunctionValue();

        automaton[MAP][ERROR] = mapErrorValue();
        automaton[MAP][ELEMENT] = mapElementValue();
        automaton[MAP][BOOLEAN] = mapBooleanValue();
        automaton[MAP][NUMBER] = mapNumberValue();
        automaton[MAP][STRING] = mapStringValue();
        automaton[MAP][MAP] = mapMapValue();
        automaton[MAP][ARRAY] = mapArrayValue();
        automaton[MAP][FUNCTION] = mapFunctionValue();

        automaton[ARRAY][ERROR] = arrayErrorValue();
        automaton[ARRAY][ELEMENT] = arrayElementValue();
        automaton[ARRAY][BOOLEAN] = arrayBooleanValue();
        automaton[ARRAY][NUMBER] = arrayNumberValue();
        automaton[ARRAY][STRING] = arrayStringValue();
        automaton[ARRAY][MAP] = arrayMapValue();
        automaton[ARRAY][ARRAY] = arrayArrayValue();
        automaton[ARRAY][FUNCTION] = arrayFunctionValue();

        automaton[FUNCTION][ERROR] = functionErrorValue();
        automaton[FUNCTION][ELEMENT] = functionElementValue();
        automaton[FUNCTION][BOOLEAN] = functionBooleanValue();
        automaton[FUNCTION][NUMBER] = functionNumberValue();
        automaton[FUNCTION][STRING] = functionStringValue();
        automaton[FUNCTION][MAP] = functionMapValue();
        automaton[FUNCTION][ARRAY] = functionArrayValue();
        automaton[FUNCTION][FUNCTION] = functionFunctionValue();

        automaton[BOOLEAN][ERROR] = booleanErrorValue();
        automaton[BOOLEAN][ELEMENT] = booleanElementValue();
        automaton[BOOLEAN][BOOLEAN] = booleanBooleanValue();
        automaton[BOOLEAN][NUMBER] = booleanNumberValue();
        automaton[BOOLEAN][STRING] = booleanStringValue();
        automaton[BOOLEAN][MAP] = booleanMapValue();
        automaton[BOOLEAN][ARRAY] = booleanArrayValue();
        automaton[BOOLEAN][FUNCTION] = booleanFunctionValue();

        automaton[STRING][ERROR] = stringErrorValue();
        automaton[STRING][ELEMENT] = stringElementValue();
        automaton[STRING][BOOLEAN] = stringBooleanValue();
        automaton[STRING][NUMBER] = stringNumberValue();
        automaton[STRING][STRING] = stringStringValue();
        automaton[STRING][MAP] = stringMapValue();
        automaton[STRING][ARRAY] = stringArrayValue();
        automaton[STRING][FUNCTION] = stringFunctionValue();

        automaton[NUMBER][ERROR] = numberErrorValue();
        automaton[NUMBER][ELEMENT] = numberElementValue();
        automaton[NUMBER][BOOLEAN] = numberBooleanValue();
        automaton[NUMBER][NUMBER] = numberNumberValue();
        automaton[NUMBER][STRING] = numberStringValue();
        automaton[NUMBER][MAP] = numberMapValue();
        automaton[NUMBER][ARRAY] = numberArrayValue();
        automaton[NUMBER][FUNCTION] = numberFunctionValue();

        return automaton;
    }


    public abstract boolean errorErrorValue();
    public abstract boolean errorElementValue();
    public abstract boolean errorBooleanValue();
    public abstract boolean errorNumberValue();
    public abstract boolean errorStringValue();
    public abstract boolean errorMapValue();
    public abstract boolean errorArrayValue();
    public abstract boolean errorFunctionValue();


    // ELEMENT Valueboolean
    public abstract boolean elementErrorValue();
    public abstract boolean elementElementValue();
    public abstract boolean elementBooleanValue();
    public abstract boolean elementNumberValue();
    public abstract boolean elementStringValue();
    public abstract boolean elementMapValue();
    public abstract boolean elementArrayValue();
    public abstract boolean elementFunctionValue();



    // MAP Values
    public abstract boolean mapErrorValue();
    public abstract boolean mapElementValue();
    public abstract boolean mapBooleanValue();
    public abstract boolean mapNumberValue();
    public abstract boolean mapStringValue();
    public abstract boolean mapMapValue();
    public abstract boolean mapArrayValue();
    public abstract boolean mapFunctionValue();

// ARRAY Values
    public abstract boolean arrayErrorValue();
    public abstract boolean arrayElementValue();
    public abstract boolean arrayBooleanValue();
    public abstract boolean arrayNumberValue();
    public abstract boolean arrayStringValue();
    public abstract boolean arrayMapValue();
    public abstract boolean arrayArrayValue();
    public abstract boolean arrayFunctionValue();

    // FUNCTION Valuboolean
    public abstract boolean functionErrorValue();
    public abstract boolean functionElementValue();
    public abstract boolean functionBooleanValue();
    public abstract boolean functionNumberValue();
    public abstract boolean functionStringValue();
    public abstract boolean functionMapValue();
    public abstract boolean functionArrayValue();
    public abstract boolean functionFunctionValue();

    // BOOLEAN Valueboolean
    public abstract boolean booleanErrorValue();
    public abstract boolean booleanElementValue();
    public abstract boolean booleanBooleanValue();
    public abstract boolean booleanNumberValue();
    public abstract boolean booleanStringValue();
    public abstract boolean booleanMapValue();
    public abstract boolean booleanArrayValue();
    public abstract boolean booleanFunctionValue();

    // STRING Values
    public abstract boolean stringErrorValue();
    public abstract boolean stringElementValue();
    public abstract boolean stringBooleanValue();
    public abstract boolean stringNumberValue();
    public abstract boolean stringStringValue();
    public abstract boolean stringMapValue();
    public abstract boolean stringArrayValue();
    public abstract boolean stringFunctionValue();

    // NUMBER Values
    public abstract boolean numberErrorValue();
    public abstract boolean numberElementValue();
    public abstract boolean numberBooleanValue();
    public abstract boolean numberNumberValue();
    public abstract boolean numberStringValue();
    public abstract boolean numberMapValue();
    public abstract boolean numberArrayValue();
    public abstract boolean numberFunctionValue();

    // RECORD Values
    public abstract boolean recordErrorValue();
    public abstract boolean recordElementValue();
    public abstract boolean recordBooleanValue();
    public abstract boolean recordNumberValue();
    public abstract boolean recordStringValue();
    public abstract boolean recordMapValue();
    public abstract boolean recordArrayValue();
    public abstract boolean recordFunctionValue();
}
