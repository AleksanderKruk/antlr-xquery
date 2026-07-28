package com.github.akruk.antlrquery.evaluator.values.operations;

import java.util.function.BiFunction;

import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValues;

@SuppressWarnings("unchecked")
public abstract class ValueBinaryOperation<Returned>
{
    private static final int STRING = AntlrQueryValues.STRING.ordinal();
    private static final int ELEMENT = AntlrQueryValues.ELEMENT.ordinal();
    private static final int BOOLEAN = AntlrQueryValues.BOOLEAN.ordinal();
    private static final int NUMBER = AntlrQueryValues.NUMBER.ordinal();
    private static final int ERROR = AntlrQueryValues.ERROR.ordinal();
    private static final int MAP = AntlrQueryValues.MAP.ordinal();
    private static final int ARRAY = AntlrQueryValues.ARRAY.ordinal();
    private static final int FUNCTION = AntlrQueryValues.FUNCTION.ordinal();
    private static final int typesCount = AntlrQueryValues.values().length;

    protected final BiFunction<AntlrQueryValue, AntlrQueryValue, Returned>[][] automaton;

    public ValueBinaryOperation()
    {
        this.automaton = getAutomaton();
    }


    private BiFunction<AntlrQueryValue, AntlrQueryValue, Returned>[][] getAutomaton() {
        final BiFunction<AntlrQueryValue, AntlrQueryValue, Returned>[][] automaton = new BiFunction[typesCount][typesCount];
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


    public abstract Returned errorErrorOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned errorAnyItemOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned errorAnyNodeOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned errorElementOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned errorEnumOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned errorBooleanOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned errorNumberOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned errorStringOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned errorAnyMapOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned errorMapOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned errorChoiceOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned errorAnyArrayOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned errorArrayOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned errorAnyFunctionOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned errorFunctionOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned errorRecordOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned errorExtensibleRecordOperation(AntlrQueryValue x, AntlrQueryValue y);


    // ELEMENT operations
    public abstract Returned elementErrorOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned elementAnyItemOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned elementAnyNodeOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned elementElementOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned elementEnumOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned elementBooleanOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned elementNumberOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned elementStringOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned elementAnyMapOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned elementMapOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned elementChoiceOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned elementAnyArrayOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned elementArrayOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned elementAnyFunctionOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned elementFunctionOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned elementRecordOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned elementExtensibleRecordOperation(AntlrQueryValue x, AntlrQueryValue y);



    // MAP operations
    public abstract Returned mapErrorOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned mapAnyItemOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned mapAnyNodeOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned mapElementOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned mapEnumOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned mapBooleanOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned mapNumberOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned mapStringOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned mapAnyMapOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned mapMapOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned mapChoiceOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned mapAnyArrayOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned mapArrayOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned mapAnyFunctionOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned mapFunctionOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned mapRecordOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned mapExtensibleRecordOperation(AntlrQueryValue x, AntlrQueryValue y);

// ARRAY operations
    public abstract Returned arrayErrorOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned arrayAnyItemOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned arrayAnyNodeOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned arrayElementOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned arrayEnumOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned arrayBooleanOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned arrayNumberOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned arrayStringOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned arrayAnyMapOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned arrayMapOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned arrayChoiceOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned arrayAnyArrayOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned arrayArrayOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned arrayAnyFunctionOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned arrayFunctionOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned arrayRecordOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned arrayExtensibleRecordOperation(AntlrQueryValue x, AntlrQueryValue y);
    // FUNCTION operations
    public abstract Returned functionErrorOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned functionAnyItemOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned functionAnyNodeOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned functionElementOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned functionEnumOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned functionBooleanOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned functionNumberOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned functionStringOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned functionAnyMapOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned functionMapOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned functionChoiceOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned functionAnyArrayOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned functionArrayOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned functionAnyFunctionOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned functionFunctionOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned functionRecordOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned functionExtensibleRecordOperation(AntlrQueryValue x, AntlrQueryValue y);

    // BOOLEAN operations
    public abstract Returned booleanErrorOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned booleanAnyItemOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned booleanAnyNodeOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned booleanElementOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned booleanEnumOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned booleanBooleanOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned booleanNumberOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned booleanStringOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned booleanAnyMapOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned booleanMapOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned booleanChoiceOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned booleanAnyArrayOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned booleanArrayOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned booleanAnyFunctionOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned booleanFunctionOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned booleanRecordOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned booleanExtensibleRecordOperation(AntlrQueryValue x, AntlrQueryValue y);

    // STRING operations
    public abstract Returned stringErrorOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned stringAnyItemOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned stringAnyNodeOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned stringElementOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned stringEnumOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned stringBooleanOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned stringNumberOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned stringStringOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned stringAnyMapOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned stringMapOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned stringChoiceOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned stringAnyArrayOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned stringArrayOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned stringAnyFunctionOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned stringFunctionOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned stringRecordOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned stringExtensibleRecordOperation(AntlrQueryValue x, AntlrQueryValue y);

    // NUMBER operations
    public abstract Returned numberErrorOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned numberAnyItemOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned numberAnyNodeOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned numberElementOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned numberEnumOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned numberBooleanOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned numberNumberOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned numberStringOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned numberAnyMapOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned numberMapOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned numberChoiceOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned numberAnyArrayOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned numberArrayOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned numberAnyFunctionOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned numberFunctionOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned numberRecordOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned numberExtensibleRecordOperation(AntlrQueryValue x, AntlrQueryValue y);
    // RECORD operations
    public abstract Returned recordErrorOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned recordAnyItemOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned recordAnyNodeOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned recordElementOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned recordEnumOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned recordBooleanOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned recordNumberOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned recordStringOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned recordAnyMapOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned recordMapOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned recordChoiceOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned recordAnyArrayOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned recordArrayOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned recordAnyFunctionOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned recordFunctionOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned recordRecordOperation(AntlrQueryValue x, AntlrQueryValue y);
    public abstract Returned recordExtensibleRecordOperation(AntlrQueryValue x, AntlrQueryValue y);

}
