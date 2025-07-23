package com.github.akruk.antlrxquery.evaluator.values.operations;

import java.util.function.BiFunction;

import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValues;

@SuppressWarnings("unchecked")
public abstract class ValueBinaryOperation<Returned>
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

    protected final BiFunction<XQueryValue, XQueryValue, Returned>[][] automaton;

    public ValueBinaryOperation()
    {
        this.automaton = getAutomaton();
    }


    private BiFunction<XQueryValue, XQueryValue, Returned>[][] getAutomaton() {
        final BiFunction<XQueryValue, XQueryValue, Returned>[][] automaton = new BiFunction[typesCount][typesCount];
        automaton[ERROR][ERROR] = this::errorErrorOperation;
        automaton[ERROR][ELEMENT] = this::errorElementOperation;
        automaton[ERROR][BOOLEAN] = this::errorBooleanOperation;
        automaton[ERROR][NUMBER] = this::errorNumberOperation;
        automaton[ERROR][STRING] = this::errorStringOperation;
        automaton[ERROR][MAP] = this::errorMapOperation;
        automaton[ERROR][ARRAY] = this::errorArrayOperation;
        automaton[ERROR][FUNCTION] = this::errorFunctionOperation;

        automaton[ELEMENT][ERROR] = this::elementErrorOperation;
        automaton[ELEMENT][ELEMENT] = this::elementElementOperation;
        automaton[ELEMENT][BOOLEAN] = this::elementBooleanOperation;
        automaton[ELEMENT][NUMBER] = this::elementNumberOperation;
        automaton[ELEMENT][STRING] = this::elementStringOperation;
        automaton[ELEMENT][MAP] = this::elementMapOperation;
        automaton[ELEMENT][ARRAY] = this::elementArrayOperation;
        automaton[ELEMENT][FUNCTION] = this::elementFunctionOperation;

        automaton[MAP][ERROR] = this::mapErrorOperation;
        automaton[MAP][ELEMENT] = this::mapElementOperation;
        automaton[MAP][BOOLEAN] = this::mapBooleanOperation;
        automaton[MAP][NUMBER] = this::mapNumberOperation;
        automaton[MAP][STRING] = this::mapStringOperation;
        automaton[MAP][MAP] = this::mapMapOperation;
        automaton[MAP][ARRAY] = this::mapArrayOperation;
        automaton[MAP][FUNCTION] = this::mapFunctionOperation;

        automaton[ARRAY][ERROR] = this::arrayErrorOperation;
        automaton[ARRAY][ELEMENT] = this::arrayElementOperation;
        automaton[ARRAY][BOOLEAN] = this::arrayBooleanOperation;
        automaton[ARRAY][NUMBER] = this::arrayNumberOperation;
        automaton[ARRAY][STRING] = this::arrayStringOperation;
        automaton[ARRAY][MAP] = this::arrayMapOperation;
        automaton[ARRAY][ARRAY] = this::arrayArrayOperation;
        automaton[ARRAY][FUNCTION] = this::arrayFunctionOperation;

        automaton[FUNCTION][ERROR] = this::functionErrorOperation;
        automaton[FUNCTION][ELEMENT] = this::functionElementOperation;
        automaton[FUNCTION][BOOLEAN] = this::functionBooleanOperation;
        automaton[FUNCTION][NUMBER] = this::functionNumberOperation;
        automaton[FUNCTION][STRING] = this::functionStringOperation;
        automaton[FUNCTION][MAP] = this::functionMapOperation;
        automaton[FUNCTION][ARRAY] = this::functionArrayOperation;
        automaton[FUNCTION][FUNCTION] = this::functionFunctionOperation;

        automaton[BOOLEAN][ERROR] = this::booleanErrorOperation;
        automaton[BOOLEAN][ELEMENT] = this::booleanElementOperation;
        automaton[BOOLEAN][BOOLEAN] = this::booleanBooleanOperation;
        automaton[BOOLEAN][NUMBER] = this::booleanNumberOperation;
        automaton[BOOLEAN][STRING] = this::booleanStringOperation;
        automaton[BOOLEAN][MAP] = this::booleanMapOperation;
        automaton[BOOLEAN][ARRAY] = this::booleanArrayOperation;
        automaton[BOOLEAN][FUNCTION] = this::booleanFunctionOperation;

        automaton[STRING][ERROR] = this::stringErrorOperation;
        automaton[STRING][ELEMENT] = this::stringElementOperation;
        automaton[STRING][BOOLEAN] = this::stringBooleanOperation;
        automaton[STRING][NUMBER] = this::stringNumberOperation;
        automaton[STRING][STRING] = this::stringStringOperation;
        automaton[STRING][MAP] = this::stringMapOperation;
        automaton[STRING][ARRAY] = this::stringArrayOperation;
        automaton[STRING][FUNCTION] = this::stringFunctionOperation;

        automaton[NUMBER][ERROR] = this::numberErrorOperation;
        automaton[NUMBER][ELEMENT] = this::numberElementOperation;
        automaton[NUMBER][BOOLEAN] = this::numberBooleanOperation;
        automaton[NUMBER][NUMBER] = this::numberNumberOperation;
        automaton[NUMBER][STRING] = this::numberStringOperation;
        automaton[NUMBER][MAP] = this::numberMapOperation;
        automaton[NUMBER][ARRAY] = this::numberArrayOperation;
        automaton[NUMBER][FUNCTION] = this::numberFunctionOperation;

        return automaton;
    }


    public abstract Returned errorErrorOperation(XQueryValue x, XQueryValue y);
    public abstract Returned errorAnyItemOperation(XQueryValue x, XQueryValue y);
    public abstract Returned errorAnyNodeOperation(XQueryValue x, XQueryValue y);
    public abstract Returned errorElementOperation(XQueryValue x, XQueryValue y);
    public abstract Returned errorEnumOperation(XQueryValue x, XQueryValue y);
    public abstract Returned errorBooleanOperation(XQueryValue x, XQueryValue y);
    public abstract Returned errorNumberOperation(XQueryValue x, XQueryValue y);
    public abstract Returned errorStringOperation(XQueryValue x, XQueryValue y);
    public abstract Returned errorAnyMapOperation(XQueryValue x, XQueryValue y);
    public abstract Returned errorMapOperation(XQueryValue x, XQueryValue y);
    public abstract Returned errorChoiceOperation(XQueryValue x, XQueryValue y);
    public abstract Returned errorAnyArrayOperation(XQueryValue x, XQueryValue y);
    public abstract Returned errorArrayOperation(XQueryValue x, XQueryValue y);
    public abstract Returned errorAnyFunctionOperation(XQueryValue x, XQueryValue y);
    public abstract Returned errorFunctionOperation(XQueryValue x, XQueryValue y);
    public abstract Returned errorRecordOperation(XQueryValue x, XQueryValue y);
    public abstract Returned errorExtensibleRecordOperation(XQueryValue x, XQueryValue y);


    // ELEMENT operations
    public abstract Returned elementErrorOperation(XQueryValue x, XQueryValue y);
    public abstract Returned elementAnyItemOperation(XQueryValue x, XQueryValue y);
    public abstract Returned elementAnyNodeOperation(XQueryValue x, XQueryValue y);
    public abstract Returned elementElementOperation(XQueryValue x, XQueryValue y);
    public abstract Returned elementEnumOperation(XQueryValue x, XQueryValue y);
    public abstract Returned elementBooleanOperation(XQueryValue x, XQueryValue y);
    public abstract Returned elementNumberOperation(XQueryValue x, XQueryValue y);
    public abstract Returned elementStringOperation(XQueryValue x, XQueryValue y);
    public abstract Returned elementAnyMapOperation(XQueryValue x, XQueryValue y);
    public abstract Returned elementMapOperation(XQueryValue x, XQueryValue y);
    public abstract Returned elementChoiceOperation(XQueryValue x, XQueryValue y);
    public abstract Returned elementAnyArrayOperation(XQueryValue x, XQueryValue y);
    public abstract Returned elementArrayOperation(XQueryValue x, XQueryValue y);
    public abstract Returned elementAnyFunctionOperation(XQueryValue x, XQueryValue y);
    public abstract Returned elementFunctionOperation(XQueryValue x, XQueryValue y);
    public abstract Returned elementRecordOperation(XQueryValue x, XQueryValue y);
    public abstract Returned elementExtensibleRecordOperation(XQueryValue x, XQueryValue y);



    // MAP operations
    public abstract Returned mapErrorOperation(XQueryValue x, XQueryValue y);
    public abstract Returned mapAnyItemOperation(XQueryValue x, XQueryValue y);
    public abstract Returned mapAnyNodeOperation(XQueryValue x, XQueryValue y);
    public abstract Returned mapElementOperation(XQueryValue x, XQueryValue y);
    public abstract Returned mapEnumOperation(XQueryValue x, XQueryValue y);
    public abstract Returned mapBooleanOperation(XQueryValue x, XQueryValue y);
    public abstract Returned mapNumberOperation(XQueryValue x, XQueryValue y);
    public abstract Returned mapStringOperation(XQueryValue x, XQueryValue y);
    public abstract Returned mapAnyMapOperation(XQueryValue x, XQueryValue y);
    public abstract Returned mapMapOperation(XQueryValue x, XQueryValue y);
    public abstract Returned mapChoiceOperation(XQueryValue x, XQueryValue y);
    public abstract Returned mapAnyArrayOperation(XQueryValue x, XQueryValue y);
    public abstract Returned mapArrayOperation(XQueryValue x, XQueryValue y);
    public abstract Returned mapAnyFunctionOperation(XQueryValue x, XQueryValue y);
    public abstract Returned mapFunctionOperation(XQueryValue x, XQueryValue y);
    public abstract Returned mapRecordOperation(XQueryValue x, XQueryValue y);
    public abstract Returned mapExtensibleRecordOperation(XQueryValue x, XQueryValue y);

// ARRAY operations
    public abstract Returned arrayErrorOperation(XQueryValue x, XQueryValue y);
    public abstract Returned arrayAnyItemOperation(XQueryValue x, XQueryValue y);
    public abstract Returned arrayAnyNodeOperation(XQueryValue x, XQueryValue y);
    public abstract Returned arrayElementOperation(XQueryValue x, XQueryValue y);
    public abstract Returned arrayEnumOperation(XQueryValue x, XQueryValue y);
    public abstract Returned arrayBooleanOperation(XQueryValue x, XQueryValue y);
    public abstract Returned arrayNumberOperation(XQueryValue x, XQueryValue y);
    public abstract Returned arrayStringOperation(XQueryValue x, XQueryValue y);
    public abstract Returned arrayAnyMapOperation(XQueryValue x, XQueryValue y);
    public abstract Returned arrayMapOperation(XQueryValue x, XQueryValue y);
    public abstract Returned arrayChoiceOperation(XQueryValue x, XQueryValue y);
    public abstract Returned arrayAnyArrayOperation(XQueryValue x, XQueryValue y);
    public abstract Returned arrayArrayOperation(XQueryValue x, XQueryValue y);
    public abstract Returned arrayAnyFunctionOperation(XQueryValue x, XQueryValue y);
    public abstract Returned arrayFunctionOperation(XQueryValue x, XQueryValue y);
    public abstract Returned arrayRecordOperation(XQueryValue x, XQueryValue y);
    public abstract Returned arrayExtensibleRecordOperation(XQueryValue x, XQueryValue y);
    // FUNCTION operations
    public abstract Returned functionErrorOperation(XQueryValue x, XQueryValue y);
    public abstract Returned functionAnyItemOperation(XQueryValue x, XQueryValue y);
    public abstract Returned functionAnyNodeOperation(XQueryValue x, XQueryValue y);
    public abstract Returned functionElementOperation(XQueryValue x, XQueryValue y);
    public abstract Returned functionEnumOperation(XQueryValue x, XQueryValue y);
    public abstract Returned functionBooleanOperation(XQueryValue x, XQueryValue y);
    public abstract Returned functionNumberOperation(XQueryValue x, XQueryValue y);
    public abstract Returned functionStringOperation(XQueryValue x, XQueryValue y);
    public abstract Returned functionAnyMapOperation(XQueryValue x, XQueryValue y);
    public abstract Returned functionMapOperation(XQueryValue x, XQueryValue y);
    public abstract Returned functionChoiceOperation(XQueryValue x, XQueryValue y);
    public abstract Returned functionAnyArrayOperation(XQueryValue x, XQueryValue y);
    public abstract Returned functionArrayOperation(XQueryValue x, XQueryValue y);
    public abstract Returned functionAnyFunctionOperation(XQueryValue x, XQueryValue y);
    public abstract Returned functionFunctionOperation(XQueryValue x, XQueryValue y);
    public abstract Returned functionRecordOperation(XQueryValue x, XQueryValue y);
    public abstract Returned functionExtensibleRecordOperation(XQueryValue x, XQueryValue y);

    // BOOLEAN operations
    public abstract Returned booleanErrorOperation(XQueryValue x, XQueryValue y);
    public abstract Returned booleanAnyItemOperation(XQueryValue x, XQueryValue y);
    public abstract Returned booleanAnyNodeOperation(XQueryValue x, XQueryValue y);
    public abstract Returned booleanElementOperation(XQueryValue x, XQueryValue y);
    public abstract Returned booleanEnumOperation(XQueryValue x, XQueryValue y);
    public abstract Returned booleanBooleanOperation(XQueryValue x, XQueryValue y);
    public abstract Returned booleanNumberOperation(XQueryValue x, XQueryValue y);
    public abstract Returned booleanStringOperation(XQueryValue x, XQueryValue y);
    public abstract Returned booleanAnyMapOperation(XQueryValue x, XQueryValue y);
    public abstract Returned booleanMapOperation(XQueryValue x, XQueryValue y);
    public abstract Returned booleanChoiceOperation(XQueryValue x, XQueryValue y);
    public abstract Returned booleanAnyArrayOperation(XQueryValue x, XQueryValue y);
    public abstract Returned booleanArrayOperation(XQueryValue x, XQueryValue y);
    public abstract Returned booleanAnyFunctionOperation(XQueryValue x, XQueryValue y);
    public abstract Returned booleanFunctionOperation(XQueryValue x, XQueryValue y);
    public abstract Returned booleanRecordOperation(XQueryValue x, XQueryValue y);
    public abstract Returned booleanExtensibleRecordOperation(XQueryValue x, XQueryValue y);

    // STRING operations
    public abstract Returned stringErrorOperation(XQueryValue x, XQueryValue y);
    public abstract Returned stringAnyItemOperation(XQueryValue x, XQueryValue y);
    public abstract Returned stringAnyNodeOperation(XQueryValue x, XQueryValue y);
    public abstract Returned stringElementOperation(XQueryValue x, XQueryValue y);
    public abstract Returned stringEnumOperation(XQueryValue x, XQueryValue y);
    public abstract Returned stringBooleanOperation(XQueryValue x, XQueryValue y);
    public abstract Returned stringNumberOperation(XQueryValue x, XQueryValue y);
    public abstract Returned stringStringOperation(XQueryValue x, XQueryValue y);
    public abstract Returned stringAnyMapOperation(XQueryValue x, XQueryValue y);
    public abstract Returned stringMapOperation(XQueryValue x, XQueryValue y);
    public abstract Returned stringChoiceOperation(XQueryValue x, XQueryValue y);
    public abstract Returned stringAnyArrayOperation(XQueryValue x, XQueryValue y);
    public abstract Returned stringArrayOperation(XQueryValue x, XQueryValue y);
    public abstract Returned stringAnyFunctionOperation(XQueryValue x, XQueryValue y);
    public abstract Returned stringFunctionOperation(XQueryValue x, XQueryValue y);
    public abstract Returned stringRecordOperation(XQueryValue x, XQueryValue y);
    public abstract Returned stringExtensibleRecordOperation(XQueryValue x, XQueryValue y);

    // NUMBER operations
    public abstract Returned numberErrorOperation(XQueryValue x, XQueryValue y);
    public abstract Returned numberAnyItemOperation(XQueryValue x, XQueryValue y);
    public abstract Returned numberAnyNodeOperation(XQueryValue x, XQueryValue y);
    public abstract Returned numberElementOperation(XQueryValue x, XQueryValue y);
    public abstract Returned numberEnumOperation(XQueryValue x, XQueryValue y);
    public abstract Returned numberBooleanOperation(XQueryValue x, XQueryValue y);
    public abstract Returned numberNumberOperation(XQueryValue x, XQueryValue y);
    public abstract Returned numberStringOperation(XQueryValue x, XQueryValue y);
    public abstract Returned numberAnyMapOperation(XQueryValue x, XQueryValue y);
    public abstract Returned numberMapOperation(XQueryValue x, XQueryValue y);
    public abstract Returned numberChoiceOperation(XQueryValue x, XQueryValue y);
    public abstract Returned numberAnyArrayOperation(XQueryValue x, XQueryValue y);
    public abstract Returned numberArrayOperation(XQueryValue x, XQueryValue y);
    public abstract Returned numberAnyFunctionOperation(XQueryValue x, XQueryValue y);
    public abstract Returned numberFunctionOperation(XQueryValue x, XQueryValue y);
    public abstract Returned numberRecordOperation(XQueryValue x, XQueryValue y);
    public abstract Returned numberExtensibleRecordOperation(XQueryValue x, XQueryValue y);
    // RECORD operations
    public abstract Returned recordErrorOperation(XQueryValue x, XQueryValue y);
    public abstract Returned recordAnyItemOperation(XQueryValue x, XQueryValue y);
    public abstract Returned recordAnyNodeOperation(XQueryValue x, XQueryValue y);
    public abstract Returned recordElementOperation(XQueryValue x, XQueryValue y);
    public abstract Returned recordEnumOperation(XQueryValue x, XQueryValue y);
    public abstract Returned recordBooleanOperation(XQueryValue x, XQueryValue y);
    public abstract Returned recordNumberOperation(XQueryValue x, XQueryValue y);
    public abstract Returned recordStringOperation(XQueryValue x, XQueryValue y);
    public abstract Returned recordAnyMapOperation(XQueryValue x, XQueryValue y);
    public abstract Returned recordMapOperation(XQueryValue x, XQueryValue y);
    public abstract Returned recordChoiceOperation(XQueryValue x, XQueryValue y);
    public abstract Returned recordAnyArrayOperation(XQueryValue x, XQueryValue y);
    public abstract Returned recordArrayOperation(XQueryValue x, XQueryValue y);
    public abstract Returned recordAnyFunctionOperation(XQueryValue x, XQueryValue y);
    public abstract Returned recordFunctionOperation(XQueryValue x, XQueryValue y);
    public abstract Returned recordRecordOperation(XQueryValue x, XQueryValue y);
    public abstract Returned recordExtensibleRecordOperation(XQueryValue x, XQueryValue y);

}
