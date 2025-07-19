package com.github.akruk.antlrxquery.typesystem.typeoperations;

import java.util.function.BiFunction;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryTypes;

public abstract class ItemtypeBinaryOperation<Returned, Operand1, Operand2>
{
    private static final int STRING = XQueryTypes.STRING.ordinal();
    private static final int ELEMENT = XQueryTypes.ELEMENT.ordinal();
    private static final int ENUM = XQueryTypes.ENUM.ordinal();
    private static final int BOOLEAN = XQueryTypes.BOOLEAN.ordinal();
    private static final int NUMBER = XQueryTypes.NUMBER.ordinal();
    private static final int ERROR = XQueryTypes.ERROR.ordinal();
    private static final int ANY_ITEM = XQueryTypes.ANY_ITEM.ordinal();
    private static final int ANY_NODE = XQueryTypes.ANY_NODE.ordinal();
    private static final int ANY_MAP = XQueryTypes.ANY_MAP.ordinal();
    private static final int MAP = XQueryTypes.MAP.ordinal();
    private static final int CHOICE = XQueryTypes.CHOICE.ordinal();
    private static final int ANY_ARRAY = XQueryTypes.ANY_ARRAY.ordinal();
    private static final int ARRAY = XQueryTypes.ARRAY.ordinal();
    private static final int ANY_FUNCTION = XQueryTypes.ANY_FUNCTION.ordinal();
    private static final int FUNCTION = XQueryTypes.FUNCTION.ordinal();
    private static final int RECORD = XQueryTypes.RECORD.ordinal();
    private static final int EXTENSIBLE_RECORD = XQueryTypes.EXTENSIBLE_RECORD.ordinal();
    private static final int typesCount = XQueryTypes.values().length;

    private final BiFunction<Operand1, Operand2, Returned>[][] operationAutomata;

    @SuppressWarnings("unchecked")
    public ItemtypeBinaryOperation()
    {
        this.operationAutomata = new BiFunction[typesCount][typesCount];
        operationAutomata[ERROR][ERROR] = this::errorErrorOperation;
        operationAutomata[ERROR][ANY_ITEM] = this::errorAnyItemOperation;
        operationAutomata[ERROR][ANY_NODE] = this::errorAnyNodeOperation;
        operationAutomata[ERROR][ELEMENT] = this::errorElementOperation;
        operationAutomata[ERROR][ENUM] = this::errorEnumOperation;
        operationAutomata[ERROR][BOOLEAN] = this::errorBooleanOperation;
        operationAutomata[ERROR][NUMBER] = this::errorNumberOperation;
        operationAutomata[ERROR][STRING] = this::errorStringOperation;
        operationAutomata[ERROR][ANY_MAP] = this::errorAnyMapOperation;
        operationAutomata[ERROR][MAP] = this::errorMapOperation;
        operationAutomata[ERROR][CHOICE] = this::errorChoiceOperation;
        operationAutomata[ERROR][ANY_ARRAY] = this::errorAnyArrayOperation;
        operationAutomata[ERROR][ARRAY] = this::errorArrayOperation;
        operationAutomata[ERROR][ANY_FUNCTION] = this::errorAnyFunctionOperation;
        operationAutomata[ERROR][FUNCTION] = this::errorFunctionOperation;
        operationAutomata[ERROR][RECORD] = this::errorRecordOperation;
        operationAutomata[ERROR][EXTENSIBLE_RECORD] = this::errorExtensibleRecordOperation;

        operationAutomata[ANY_ITEM][ERROR] = this::anyItemErrorOperation;
        operationAutomata[ANY_ITEM][ANY_ITEM] = this::anyItemAnyItemOperation;
        operationAutomata[ANY_ITEM][ANY_NODE] = this::anyItemAnyNodeOperation;
        operationAutomata[ANY_ITEM][ELEMENT] = this::anyItemElementOperation;
        operationAutomata[ANY_ITEM][ENUM] = this::anyItemEnumOperation;
        operationAutomata[ANY_ITEM][BOOLEAN] = this::anyItemBooleanOperation;
        operationAutomata[ANY_ITEM][NUMBER] = this::anyItemNumberOperation;
        operationAutomata[ANY_ITEM][STRING] = this::anyItemStringOperation;
        operationAutomata[ANY_ITEM][ANY_MAP] = this::anyItemAnyMapOperation;
        operationAutomata[ANY_ITEM][MAP] = this::anyItemMapOperation;
        operationAutomata[ANY_ITEM][CHOICE] = this::anyItemChoiceOperation;
        operationAutomata[ANY_ITEM][ANY_ARRAY] = this::anyItemAnyArrayOperation;
        operationAutomata[ANY_ITEM][ARRAY] = this::anyItemArrayOperation;
        operationAutomata[ANY_ITEM][ANY_FUNCTION] = this::anyItemAnyFunctionOperation;
        operationAutomata[ANY_ITEM][FUNCTION] = this::anyItemFunctionOperation;
        operationAutomata[ANY_ITEM][RECORD] = this::anyItemRecordOperation;
        operationAutomata[ANY_ITEM][EXTENSIBLE_RECORD] = this::anyItemExtensibleRecordOperation;

        operationAutomata[ANY_NODE][ERROR] = this::anyNodeErrorOperation;
        operationAutomata[ANY_NODE][ANY_ITEM] = this::anyNodeAnyItemOperation;
        operationAutomata[ANY_NODE][ANY_NODE] = this::anyNodeAnyNodeOperation;
        operationAutomata[ANY_NODE][ELEMENT] = this::anyNodeElementOperation;
        operationAutomata[ANY_NODE][ENUM] = this::anyNodeEnumOperation;
        operationAutomata[ANY_NODE][BOOLEAN] = this::anyNodeBooleanOperation;
        operationAutomata[ANY_NODE][NUMBER] = this::anyNodeNumberOperation;
        operationAutomata[ANY_NODE][STRING] = this::anyNodeStringOperation;
        operationAutomata[ANY_NODE][ANY_MAP] = this::anyNodeAnyMapOperation;
        operationAutomata[ANY_NODE][MAP] = this::anyNodeMapOperation;
        operationAutomata[ANY_NODE][CHOICE] = this::anyNodeChoiceOperation;
        operationAutomata[ANY_NODE][ANY_ARRAY] = this::anyNodeAnyArrayOperation;
        operationAutomata[ANY_NODE][ARRAY] = this::anyNodeArrayOperation;
        operationAutomata[ANY_NODE][ANY_FUNCTION] = this::anyNodeAnyFunctionOperation;
        operationAutomata[ANY_NODE][FUNCTION] = this::anyNodeFunctionOperation;
        operationAutomata[ANY_NODE][RECORD] = this::anyNodeRecordOperation;
        operationAutomata[ANY_NODE][EXTENSIBLE_RECORD] = this::anyNodeExtensibleRecordOperation;

        operationAutomata[ELEMENT][ERROR] = this::elementErrorOperation;
        operationAutomata[ELEMENT][ANY_ITEM] = this::elementAnyItemOperation;
        operationAutomata[ELEMENT][ANY_NODE] = this::elementAnyNodeOperation;
        operationAutomata[ELEMENT][ELEMENT] = this::elementElementOperation;
        operationAutomata[ELEMENT][ENUM] = this::elementEnumOperation;
        operationAutomata[ELEMENT][BOOLEAN] = this::elementBooleanOperation;
        operationAutomata[ELEMENT][NUMBER] = this::elementNumberOperation;
        operationAutomata[ELEMENT][STRING] = this::elementStringOperation;
        operationAutomata[ELEMENT][ANY_MAP] = this::elementAnyMapOperation;
        operationAutomata[ELEMENT][MAP] = this::elementMapOperation;
        operationAutomata[ELEMENT][CHOICE] = this::elementChoiceOperation;
        operationAutomata[ELEMENT][ANY_ARRAY] = this::elementAnyArrayOperation;
        operationAutomata[ELEMENT][ARRAY] = this::elementArrayOperation;
        operationAutomata[ELEMENT][ANY_FUNCTION] = this::elementAnyFunctionOperation;
        operationAutomata[ELEMENT][FUNCTION] = this::elementFunctionOperation;
        operationAutomata[ELEMENT][RECORD] = this::elementRecordOperation;
        operationAutomata[ELEMENT][EXTENSIBLE_RECORD] = this::elementExtensibleRecordOperation;

        operationAutomata[ANY_MAP][ERROR] = this::anyMapErrorOperation;
        operationAutomata[ANY_MAP][ANY_ITEM] = this::anyMapAnyItemOperation;
        operationAutomata[ANY_MAP][ANY_NODE] = this::anyMapAnyNodeOperation;
        operationAutomata[ANY_MAP][ELEMENT] = this::anyMapElementOperation;
        operationAutomata[ANY_MAP][ENUM] = this::anyMapEnumOperation;
        operationAutomata[ANY_MAP][BOOLEAN] = this::anyMapBooleanOperation;
        operationAutomata[ANY_MAP][NUMBER] = this::anyMapNumberOperation;
        operationAutomata[ANY_MAP][STRING] = this::anyMapStringOperation;
        operationAutomata[ANY_MAP][ANY_MAP] = this::anyMapAnyMapOperation;
        operationAutomata[ANY_MAP][MAP] = this::anyMapMapOperation;
        operationAutomata[ANY_MAP][CHOICE] = this::anyMapChoiceOperation;
        operationAutomata[ANY_MAP][ANY_ARRAY] = this::anyMapAnyArrayOperation;
        operationAutomata[ANY_MAP][ARRAY] = this::anyMapArrayOperation;
        operationAutomata[ANY_MAP][ANY_FUNCTION] = this::anyMapAnyFunctionOperation;
        operationAutomata[ANY_MAP][FUNCTION] = this::anyMapFunctionOperation;
        operationAutomata[ANY_MAP][RECORD] = this::anyMapRecordOperation;
        operationAutomata[ANY_MAP][EXTENSIBLE_RECORD] = this::anyMapExtensibleRecordOperation;

        operationAutomata[MAP][ERROR] = this::mapErrorOperation;
        operationAutomata[MAP][ANY_ITEM] = this::mapAnyItemOperation;
        operationAutomata[MAP][ANY_NODE] = this::mapAnyNodeOperation;
        operationAutomata[MAP][ELEMENT] = this::mapElementOperation;
        operationAutomata[MAP][ENUM] = this::mapEnumOperation;
        operationAutomata[MAP][BOOLEAN] = this::mapBooleanOperation;
        operationAutomata[MAP][NUMBER] = this::mapNumberOperation;
        operationAutomata[MAP][STRING] = this::mapStringOperation;
        operationAutomata[MAP][ANY_MAP] = this::mapAnyMapOperation;
        operationAutomata[MAP][MAP] = this::mapMapOperation;
        operationAutomata[MAP][CHOICE] = this::mapChoiceOperation;
        operationAutomata[MAP][ANY_ARRAY] = this::mapAnyArrayOperation;
        operationAutomata[MAP][ARRAY] = this::mapArrayOperation;
        operationAutomata[MAP][ANY_FUNCTION] = this::mapAnyFunctionOperation;
        operationAutomata[MAP][FUNCTION] = this::mapFunctionOperation;
        operationAutomata[MAP][RECORD] = this::mapRecordOperation;
        operationAutomata[MAP][EXTENSIBLE_RECORD] = this::mapExtensibleRecordOperation;

        operationAutomata[ANY_ARRAY][ERROR] = this::anyArrayErrorOperation;
        operationAutomata[ANY_ARRAY][ANY_ITEM] = this::anyArrayAnyItemOperation;
        operationAutomata[ANY_ARRAY][ANY_NODE] = this::anyArrayAnyNodeOperation;
        operationAutomata[ANY_ARRAY][ELEMENT] = this::anyArrayElementOperation;
        operationAutomata[ANY_ARRAY][ENUM] = this::anyArrayEnumOperation;
        operationAutomata[ANY_ARRAY][BOOLEAN] = this::anyArrayBooleanOperation;
        operationAutomata[ANY_ARRAY][NUMBER] = this::anyArrayNumberOperation;
        operationAutomata[ANY_ARRAY][STRING] = this::anyArrayStringOperation;
        operationAutomata[ANY_ARRAY][ANY_MAP] = this::anyArrayAnyMapOperation;
        operationAutomata[ANY_ARRAY][MAP] = this::anyArrayMapOperation;
        operationAutomata[ANY_ARRAY][CHOICE] = this::anyArrayChoiceOperation;
        operationAutomata[ANY_ARRAY][ANY_ARRAY] = this::anyArrayAnyArrayOperation;
        operationAutomata[ANY_ARRAY][ARRAY] = this::anyArrayArrayOperation;
        operationAutomata[ANY_ARRAY][ANY_FUNCTION] = this::anyArrayAnyFunctionOperation;
        operationAutomata[ANY_ARRAY][FUNCTION] = this::anyArrayFunctionOperation;
        operationAutomata[ANY_ARRAY][RECORD] = this::anyArrayRecordOperation;
        operationAutomata[ANY_ARRAY][EXTENSIBLE_RECORD] = this::anyArrayExtensibleRecordOperation;

        operationAutomata[ARRAY][ERROR] = this::arrayErrorOperation;
        operationAutomata[ARRAY][ANY_ITEM] = this::arrayAnyItemOperation;
        operationAutomata[ARRAY][ANY_NODE] = this::arrayAnyNodeOperation;
        operationAutomata[ARRAY][ELEMENT] = this::arrayElementOperation;
        operationAutomata[ARRAY][ENUM] = this::arrayEnumOperation;
        operationAutomata[ARRAY][BOOLEAN] = this::arrayBooleanOperation;
        operationAutomata[ARRAY][NUMBER] = this::arrayNumberOperation;
        operationAutomata[ARRAY][STRING] = this::arrayStringOperation;
        operationAutomata[ARRAY][ANY_MAP] = this::arrayAnyMapOperation;
        operationAutomata[ARRAY][MAP] = this::arrayMapOperation;
        operationAutomata[ARRAY][CHOICE] = this::arrayChoiceOperation;
        operationAutomata[ARRAY][ANY_ARRAY] = this::arrayAnyArrayOperation;
        operationAutomata[ARRAY][ARRAY] = this::arrayArrayOperation;
        operationAutomata[ARRAY][ANY_FUNCTION] = this::arrayAnyFunctionOperation;
        operationAutomata[ARRAY][FUNCTION] = this::arrayFunctionOperation;
        operationAutomata[ARRAY][RECORD] = this::arrayRecordOperation;
        operationAutomata[ARRAY][EXTENSIBLE_RECORD] = this::arrayExtensibleRecordOperation;

        operationAutomata[ANY_FUNCTION][ERROR] = this::anyFunctionErrorOperation;
        operationAutomata[ANY_FUNCTION][ANY_ITEM] = this::anyFunctionAnyItemOperation;
        operationAutomata[ANY_FUNCTION][ANY_NODE] = this::anyFunctionAnyNodeOperation;
        operationAutomata[ANY_FUNCTION][ELEMENT] = this::anyFunctionElementOperation;
        operationAutomata[ANY_FUNCTION][ENUM] = this::anyFunctionEnumOperation;
        operationAutomata[ANY_FUNCTION][BOOLEAN] = this::anyFunctionBooleanOperation;
        operationAutomata[ANY_FUNCTION][NUMBER] = this::anyFunctionNumberOperation;
        operationAutomata[ANY_FUNCTION][STRING] = this::anyFunctionStringOperation;
        operationAutomata[ANY_FUNCTION][ANY_MAP] = this::anyFunctionAnyMapOperation;
        operationAutomata[ANY_FUNCTION][MAP] = this::anyFunctionMapOperation;
        operationAutomata[ANY_FUNCTION][CHOICE] = this::anyFunctionChoiceOperation;
        operationAutomata[ANY_FUNCTION][ANY_ARRAY] = this::anyFunctionAnyArrayOperation;
        operationAutomata[ANY_FUNCTION][ARRAY] = this::anyFunctionArrayOperation;
        operationAutomata[ANY_FUNCTION][ANY_FUNCTION] = this::anyFunctionAnyFunctionOperation;
        operationAutomata[ANY_FUNCTION][FUNCTION] = this::anyFunctionFunctionOperation;
        operationAutomata[ANY_FUNCTION][RECORD] = this::anyFunctionRecordOperation;
        operationAutomata[ANY_FUNCTION][EXTENSIBLE_RECORD] = this::anyFunctionExtensibleRecordOperation;

        operationAutomata[FUNCTION][ERROR] = this::functionErrorOperation;
        operationAutomata[FUNCTION][ANY_ITEM] = this::functionAnyItemOperation;
        operationAutomata[FUNCTION][ANY_NODE] = this::functionAnyNodeOperation;
        operationAutomata[FUNCTION][ELEMENT] = this::functionElementOperation;
        operationAutomata[FUNCTION][ENUM] = this::functionEnumOperation;
        operationAutomata[FUNCTION][BOOLEAN] = this::functionBooleanOperation;
        operationAutomata[FUNCTION][NUMBER] = this::functionNumberOperation;
        operationAutomata[FUNCTION][STRING] = this::functionStringOperation;
        operationAutomata[FUNCTION][ANY_MAP] = this::functionAnyMapOperation;
        operationAutomata[FUNCTION][MAP] = this::functionMapOperation;
        operationAutomata[FUNCTION][CHOICE] = this::functionChoiceOperation;
        operationAutomata[FUNCTION][ANY_ARRAY] = this::functionAnyArrayOperation;
        operationAutomata[FUNCTION][ARRAY] = this::functionArrayOperation;
        operationAutomata[FUNCTION][ANY_FUNCTION] = this::functionAnyFunctionOperation;
        operationAutomata[FUNCTION][FUNCTION] = this::functionFunctionOperation;
        operationAutomata[FUNCTION][RECORD] = this::functionRecordOperation;
        operationAutomata[FUNCTION][EXTENSIBLE_RECORD] = this::functionExtensibleRecordOperation;

        operationAutomata[ENUM][ERROR] = this::enumErrorOperation;
        operationAutomata[ENUM][ANY_ITEM] = this::enumAnyItemOperation;
        operationAutomata[ENUM][ANY_NODE] = this::enumAnyNodeOperation;
        operationAutomata[ENUM][ELEMENT] = this::enumElementOperation;
        operationAutomata[ENUM][ENUM] = this::enumEnumOperation;
        operationAutomata[ENUM][BOOLEAN] = this::enumBooleanOperation;
        operationAutomata[ENUM][NUMBER] = this::enumNumberOperation;
        operationAutomata[ENUM][STRING] = this::enumStringOperation;
        operationAutomata[ENUM][ANY_MAP] = this::enumAnyMapOperation;
        operationAutomata[ENUM][MAP] = this::enumMapOperation;
        operationAutomata[ENUM][CHOICE] = this::enumChoiceOperation;
        operationAutomata[ENUM][ANY_ARRAY] = this::enumAnyArrayOperation;
        operationAutomata[ENUM][ARRAY] = this::enumArrayOperation;
        operationAutomata[ENUM][ANY_FUNCTION] = this::enumAnyFunctionOperation;
        operationAutomata[ENUM][FUNCTION] = this::enumFunctionOperation;
        operationAutomata[ENUM][RECORD] = this::enumRecordOperation;
        operationAutomata[ENUM][EXTENSIBLE_RECORD] = this::enumExtensibleRecordOperation;

        operationAutomata[RECORD][ERROR] = this::recordErrorOperation;
        operationAutomata[RECORD][ANY_ITEM] = this::recordAnyItemOperation;
        operationAutomata[RECORD][ANY_NODE] = this::recordAnyNodeOperation;
        operationAutomata[RECORD][ELEMENT] = this::recordElementOperation;
        operationAutomata[RECORD][ENUM] = this::recordEnumOperation;
        operationAutomata[RECORD][BOOLEAN] = this::recordBooleanOperation;
        operationAutomata[RECORD][NUMBER] = this::recordNumberOperation;
        operationAutomata[RECORD][STRING] = this::recordStringOperation;
        operationAutomata[RECORD][ANY_MAP] = this::recordAnyMapOperation;
        operationAutomata[RECORD][MAP] = this::recordMapOperation;
        operationAutomata[RECORD][CHOICE] = this::recordChoiceOperation;
        operationAutomata[RECORD][ANY_ARRAY] = this::recordAnyArrayOperation;
        operationAutomata[RECORD][ARRAY] = this::recordArrayOperation;
        operationAutomata[RECORD][ANY_FUNCTION] = this::recordAnyFunctionOperation;
        operationAutomata[RECORD][FUNCTION] = this::recordFunctionOperation;
        operationAutomata[RECORD][RECORD] = this::recordRecordOperation;
        operationAutomata[RECORD][EXTENSIBLE_RECORD] = this::recordExtensibleRecordOperation;

        operationAutomata[EXTENSIBLE_RECORD][ERROR] = this::extensibleRecordErrorOperation;
        operationAutomata[EXTENSIBLE_RECORD][ANY_ITEM] = this::extensibleRecordAnyItemOperation;
        operationAutomata[EXTENSIBLE_RECORD][ANY_NODE] = this::extensibleRecordAnyNodeOperation;
        operationAutomata[EXTENSIBLE_RECORD][ELEMENT] = this::extensibleRecordElementOperation;
        operationAutomata[EXTENSIBLE_RECORD][ENUM] = this::extensibleRecordEnumOperation;
        operationAutomata[EXTENSIBLE_RECORD][BOOLEAN] = this::extensibleRecordBooleanOperation;
        operationAutomata[EXTENSIBLE_RECORD][NUMBER] = this::extensibleRecordNumberOperation;
        operationAutomata[EXTENSIBLE_RECORD][STRING] = this::extensibleRecordStringOperation;
        operationAutomata[EXTENSIBLE_RECORD][ANY_MAP] = this::extensibleRecordAnyMapOperation;
        operationAutomata[EXTENSIBLE_RECORD][MAP] = this::extensibleRecordMapOperation;
        operationAutomata[EXTENSIBLE_RECORD][CHOICE] = this::extensibleRecordChoiceOperation;
        operationAutomata[EXTENSIBLE_RECORD][ANY_ARRAY] = this::extensibleRecordAnyArrayOperation;
        operationAutomata[EXTENSIBLE_RECORD][ARRAY] = this::extensibleRecordArrayOperation;
        operationAutomata[EXTENSIBLE_RECORD][ANY_FUNCTION] = this::extensibleRecordAnyFunctionOperation;
        operationAutomata[EXTENSIBLE_RECORD][FUNCTION] = this::extensibleRecordFunctionOperation;
        operationAutomata[EXTENSIBLE_RECORD][RECORD] = this::extensibleRecordRecordOperation;
        operationAutomata[EXTENSIBLE_RECORD][EXTENSIBLE_RECORD] = this::extensibleRecordExtensibleRecordOperation;

        operationAutomata[BOOLEAN][ERROR] = this::booleanErrorOperation;
        operationAutomata[BOOLEAN][ANY_ITEM] = this::booleanAnyItemOperation;
        operationAutomata[BOOLEAN][ANY_NODE] = this::booleanAnyNodeOperation;
        operationAutomata[BOOLEAN][ELEMENT] = this::booleanElementOperation;
        operationAutomata[BOOLEAN][ENUM] = this::booleanEnumOperation;
        operationAutomata[BOOLEAN][BOOLEAN] = this::booleanBooleanOperation;
        operationAutomata[BOOLEAN][NUMBER] = this::booleanNumberOperation;
        operationAutomata[BOOLEAN][STRING] = this::booleanStringOperation;
        operationAutomata[BOOLEAN][ANY_MAP] = this::booleanAnyMapOperation;
        operationAutomata[BOOLEAN][MAP] = this::booleanMapOperation;
        operationAutomata[BOOLEAN][CHOICE] = this::booleanChoiceOperation;
        operationAutomata[BOOLEAN][ANY_ARRAY] = this::booleanAnyArrayOperation;
        operationAutomata[BOOLEAN][ARRAY] = this::booleanArrayOperation;
        operationAutomata[BOOLEAN][ANY_FUNCTION] = this::booleanAnyFunctionOperation;
        operationAutomata[BOOLEAN][FUNCTION] = this::booleanFunctionOperation;
        operationAutomata[BOOLEAN][RECORD] = this::booleanRecordOperation;
        operationAutomata[BOOLEAN][EXTENSIBLE_RECORD] = this::booleanExtensibleRecordOperation;

        operationAutomata[STRING][ERROR] = this::stringErrorOperation;
        operationAutomata[STRING][ANY_ITEM] = this::stringAnyItemOperation;
        operationAutomata[STRING][ANY_NODE] = this::stringAnyNodeOperation;
        operationAutomata[STRING][ELEMENT] = this::stringElementOperation;
        operationAutomata[STRING][ENUM] = this::stringEnumOperation;
        operationAutomata[STRING][BOOLEAN] = this::stringBooleanOperation;
        operationAutomata[STRING][NUMBER] = this::stringNumberOperation;
        operationAutomata[STRING][STRING] = this::stringStringOperation;
        operationAutomata[STRING][ANY_MAP] = this::stringAnyMapOperation;
        operationAutomata[STRING][MAP] = this::stringMapOperation;
        operationAutomata[STRING][CHOICE] = this::stringChoiceOperation;
        operationAutomata[STRING][ANY_ARRAY] = this::stringAnyArrayOperation;
        operationAutomata[STRING][ARRAY] = this::stringArrayOperation;
        operationAutomata[STRING][ANY_FUNCTION] = this::stringAnyFunctionOperation;
        operationAutomata[STRING][FUNCTION] = this::stringFunctionOperation;
        operationAutomata[STRING][RECORD] = this::stringRecordOperation;
        operationAutomata[STRING][EXTENSIBLE_RECORD] = this::stringExtensibleRecordOperation;

        operationAutomata[NUMBER][ERROR] = this::numberErrorOperation;
        operationAutomata[NUMBER][ANY_ITEM] = this::numberAnyItemOperation;
        operationAutomata[NUMBER][ANY_NODE] = this::numberAnyNodeOperation;
        operationAutomata[NUMBER][ELEMENT] = this::numberElementOperation;
        operationAutomata[NUMBER][ENUM] = this::numberEnumOperation;
        operationAutomata[NUMBER][BOOLEAN] = this::numberBooleanOperation;
        operationAutomata[NUMBER][NUMBER] = this::numberNumberOperation;
        operationAutomata[NUMBER][STRING] = this::numberStringOperation;
        operationAutomata[NUMBER][ANY_MAP] = this::numberAnyMapOperation;
        operationAutomata[NUMBER][MAP] = this::numberMapOperation;
        operationAutomata[NUMBER][CHOICE] = this::numberChoiceOperation;
        operationAutomata[NUMBER][ANY_ARRAY] = this::numberAnyArrayOperation;
        operationAutomata[NUMBER][ARRAY] = this::numberArrayOperation;
        operationAutomata[NUMBER][ANY_FUNCTION] = this::numberAnyFunctionOperation;
        operationAutomata[NUMBER][FUNCTION] = this::numberFunctionOperation;
        operationAutomata[NUMBER][RECORD] = this::numberRecordOperation;
        operationAutomata[NUMBER][EXTENSIBLE_RECORD] = this::numberExtensibleRecordOperation;

        operationAutomata[CHOICE][ERROR] = this::choiceErrorOperation;
        operationAutomata[CHOICE][ANY_ITEM] = this::choiceAnyItemOperation;
        operationAutomata[CHOICE][ANY_NODE] = this::choiceAnyNodeOperation;
        operationAutomata[CHOICE][ELEMENT] = this::choiceElementOperation;
        operationAutomata[CHOICE][ENUM] = this::choiceEnumOperation;
        operationAutomata[CHOICE][BOOLEAN] = this::choiceBooleanOperation;
        operationAutomata[CHOICE][NUMBER] = this::choiceNumberOperation;
        operationAutomata[CHOICE][STRING] = this::choiceStringOperation;
        operationAutomata[CHOICE][ANY_MAP] = this::choiceAnyMapOperation;
        operationAutomata[CHOICE][MAP] = this::choiceMapOperation;
        operationAutomata[CHOICE][CHOICE] = this::choiceChoiceOperation;
        operationAutomata[CHOICE][ANY_ARRAY] = this::choiceAnyArrayOperation;
        operationAutomata[CHOICE][ARRAY] = this::choiceArrayOperation;
        operationAutomata[CHOICE][ANY_FUNCTION] = this::choiceAnyFunctionOperation;
        operationAutomata[CHOICE][FUNCTION] = this::choiceFunctionOperation;
        operationAutomata[CHOICE][RECORD] = this::choiceRecordOperation;
        operationAutomata[CHOICE][EXTENSIBLE_RECORD] = this::choiceExtensibleRecordOperation;
    }


    public abstract Returned errorErrorOperation(Operand1 x, Operand2 y);
    public abstract Returned errorAnyItemOperation(Operand1 x, Operand2 y);
    public abstract Returned errorAnyNodeOperation(Operand1 x, Operand2 y);
    public abstract Returned errorElementOperation(Operand1 x, Operand2 y);
    public abstract Returned errorEnumOperation(Operand1 x, Operand2 y);
    public abstract Returned errorBooleanOperation(Operand1 x, Operand2 y);
    public abstract Returned errorNumberOperation(Operand1 x, Operand2 y);
    public abstract Returned errorStringOperation(Operand1 x, Operand2 y);
    public abstract Returned errorAnyMapOperation(Operand1 x, Operand2 y);
    public abstract Returned errorMapOperation(Operand1 x, Operand2 y);
    public abstract Returned errorChoiceOperation(Operand1 x, Operand2 y);
    public abstract Returned errorAnyArrayOperation(Operand1 x, Operand2 y);
    public abstract Returned errorArrayOperation(Operand1 x, Operand2 y);
    public abstract Returned errorAnyFunctionOperation(Operand1 x, Operand2 y);
    public abstract Returned errorFunctionOperation(Operand1 x, Operand2 y);
    public abstract Returned errorRecordOperation(Operand1 x, Operand2 y);
    public abstract Returned errorExtensibleRecordOperation(Operand1 x, Operand2 y);

    // ANY_ITEM operations
    public abstract Returned anyItemErrorOperation(Operand1 x, Operand2 y);
    public abstract Returned anyItemAnyItemOperation(Operand1 x, Operand2 y);
    public abstract Returned anyItemAnyNodeOperation(Operand1 x, Operand2 y);
    public abstract Returned anyItemElementOperation(Operand1 x, Operand2 y);
    public abstract Returned anyItemEnumOperation(Operand1 x, Operand2 y);
    public abstract Returned anyItemBooleanOperation(Operand1 x, Operand2 y);
    public abstract Returned anyItemNumberOperation(Operand1 x, Operand2 y);
    public abstract Returned anyItemStringOperation(Operand1 x, Operand2 y);
    public abstract Returned anyItemAnyMapOperation(Operand1 x, Operand2 y);
    public abstract Returned anyItemMapOperation(Operand1 x, Operand2 y);
    public abstract Returned anyItemChoiceOperation(Operand1 x, Operand2 y);
    public abstract Returned anyItemAnyArrayOperation(Operand1 x, Operand2 y);
    public abstract Returned anyItemArrayOperation(Operand1 x, Operand2 y);
    public abstract Returned anyItemAnyFunctionOperation(Operand1 x, Operand2 y);
    public abstract Returned anyItemFunctionOperation(Operand1 x, Operand2 y);
    public abstract Returned anyItemRecordOperation(Operand1 x, Operand2 y);
    public abstract Returned anyItemExtensibleRecordOperation(Operand1 x, Operand2 y);

    // ANY_NODE operations
    public abstract Returned anyNodeErrorOperation(Operand1 x, Operand2 y);
    public abstract Returned anyNodeAnyItemOperation(Operand1 x, Operand2 y);
    public abstract Returned anyNodeAnyNodeOperation(Operand1 x, Operand2 y);
    public abstract Returned anyNodeElementOperation(Operand1 x, Operand2 y);
    public abstract Returned anyNodeEnumOperation(Operand1 x, Operand2 y);
    public abstract Returned anyNodeBooleanOperation(Operand1 x, Operand2 y);
    public abstract Returned anyNodeNumberOperation(Operand1 x, Operand2 y);
    public abstract Returned anyNodeStringOperation(Operand1 x, Operand2 y);
    public abstract Returned anyNodeAnyMapOperation(Operand1 x, Operand2 y);
    public abstract Returned anyNodeMapOperation(Operand1 x, Operand2 y);
    public abstract Returned anyNodeChoiceOperation(Operand1 x, Operand2 y);
    public abstract Returned anyNodeAnyArrayOperation(Operand1 x, Operand2 y);
    public abstract Returned anyNodeArrayOperation(Operand1 x, Operand2 y);
    public abstract Returned anyNodeAnyFunctionOperation(Operand1 x, Operand2 y);
    public abstract Returned anyNodeFunctionOperation(Operand1 x, Operand2 y);
    public abstract Returned anyNodeRecordOperation(Operand1 x, Operand2 y);
    public abstract Returned anyNodeExtensibleRecordOperation(Operand1 x, Operand2 y);
    // ELEMENT operations
    public abstract Returned elementErrorOperation(Operand1 x, Operand2 y);
    public abstract Returned elementAnyItemOperation(Operand1 x, Operand2 y);
    public abstract Returned elementAnyNodeOperation(Operand1 x, Operand2 y);
    public abstract Returned elementElementOperation(Operand1 x, Operand2 y);
    public abstract Returned elementEnumOperation(Operand1 x, Operand2 y);
    public abstract Returned elementBooleanOperation(Operand1 x, Operand2 y);
    public abstract Returned elementNumberOperation(Operand1 x, Operand2 y);
    public abstract Returned elementStringOperation(Operand1 x, Operand2 y);
    public abstract Returned elementAnyMapOperation(Operand1 x, Operand2 y);
    public abstract Returned elementMapOperation(Operand1 x, Operand2 y);
    public abstract Returned elementChoiceOperation(Operand1 x, Operand2 y);
    public abstract Returned elementAnyArrayOperation(Operand1 x, Operand2 y);
    public abstract Returned elementArrayOperation(Operand1 x, Operand2 y);
    public abstract Returned elementAnyFunctionOperation(Operand1 x, Operand2 y);
    public abstract Returned elementFunctionOperation(Operand1 x, Operand2 y);
    public abstract Returned elementRecordOperation(Operand1 x, Operand2 y);
    public abstract Returned elementExtensibleRecordOperation(Operand1 x, Operand2 y);



    // ANY_MAP operations
    public abstract Returned anyMapErrorOperation(Operand1 x, Operand2 y);
    public abstract Returned anyMapAnyItemOperation(Operand1 x, Operand2 y);
    public abstract Returned anyMapAnyNodeOperation(Operand1 x, Operand2 y);
    public abstract Returned anyMapElementOperation(Operand1 x, Operand2 y);
    public abstract Returned anyMapEnumOperation(Operand1 x, Operand2 y);
    public abstract Returned anyMapBooleanOperation(Operand1 x, Operand2 y);
    public abstract Returned anyMapNumberOperation(Operand1 x, Operand2 y);
    public abstract Returned anyMapStringOperation(Operand1 x, Operand2 y);
    public abstract Returned anyMapAnyMapOperation(Operand1 x, Operand2 y);
    public abstract Returned anyMapMapOperation(Operand1 x, Operand2 y);
    public abstract Returned anyMapChoiceOperation(Operand1 x, Operand2 y);
    public abstract Returned anyMapAnyArrayOperation(Operand1 x, Operand2 y);
    public abstract Returned anyMapArrayOperation(Operand1 x, Operand2 y);
    public abstract Returned anyMapAnyFunctionOperation(Operand1 x, Operand2 y);
    public abstract Returned anyMapFunctionOperation(Operand1 x, Operand2 y);
    public abstract Returned anyMapRecordOperation(Operand1 x, Operand2 y);
    public abstract Returned anyMapExtensibleRecordOperation(Operand1 x, Operand2 y);

    // MAP operations
    public abstract Returned mapErrorOperation(Operand1 x, Operand2 y);
    public abstract Returned mapAnyItemOperation(Operand1 x, Operand2 y);
    public abstract Returned mapAnyNodeOperation(Operand1 x, Operand2 y);
    public abstract Returned mapElementOperation(Operand1 x, Operand2 y);
    public abstract Returned mapEnumOperation(Operand1 x, Operand2 y);
    public abstract Returned mapBooleanOperation(Operand1 x, Operand2 y);
    public abstract Returned mapNumberOperation(Operand1 x, Operand2 y);
    public abstract Returned mapStringOperation(Operand1 x, Operand2 y);
    public abstract Returned mapAnyMapOperation(Operand1 x, Operand2 y);
    public abstract Returned mapMapOperation(Operand1 x, Operand2 y);
    public abstract Returned mapChoiceOperation(Operand1 x, Operand2 y);
    public abstract Returned mapAnyArrayOperation(Operand1 x, Operand2 y);
    public abstract Returned mapArrayOperation(Operand1 x, Operand2 y);
    public abstract Returned mapAnyFunctionOperation(Operand1 x, Operand2 y);
    public abstract Returned mapFunctionOperation(Operand1 x, Operand2 y);
    public abstract Returned mapRecordOperation(Operand1 x, Operand2 y);
    public abstract Returned mapExtensibleRecordOperation(Operand1 x, Operand2 y);

    // ANY_ARRAY operations
    public abstract Returned anyArrayErrorOperation(Operand1 x, Operand2 y);
    public abstract Returned anyArrayAnyItemOperation(Operand1 x, Operand2 y);
    public abstract Returned anyArrayAnyNodeOperation(Operand1 x, Operand2 y);
    public abstract Returned anyArrayElementOperation(Operand1 x, Operand2 y);
    public abstract Returned anyArrayEnumOperation(Operand1 x, Operand2 y);
    public abstract Returned anyArrayBooleanOperation(Operand1 x, Operand2 y);
    public abstract Returned anyArrayNumberOperation(Operand1 x, Operand2 y);
    public abstract Returned anyArrayStringOperation(Operand1 x, Operand2 y);
    public abstract Returned anyArrayAnyMapOperation(Operand1 x, Operand2 y);
    public abstract Returned anyArrayMapOperation(Operand1 x, Operand2 y);
    public abstract Returned anyArrayChoiceOperation(Operand1 x, Operand2 y);
    public abstract Returned anyArrayAnyArrayOperation(Operand1 x, Operand2 y);
    public abstract Returned anyArrayArrayOperation(Operand1 x, Operand2 y);
    public abstract Returned anyArrayAnyFunctionOperation(Operand1 x, Operand2 y);
    public abstract Returned anyArrayFunctionOperation(Operand1 x, Operand2 y);
    public abstract Returned anyArrayRecordOperation(Operand1 x, Operand2 y);
    public abstract Returned anyArrayExtensibleRecordOperation(Operand1 x, Operand2 y);


// ARRAY operations
    public abstract Returned arrayErrorOperation(Operand1 x, Operand2 y);
    public abstract Returned arrayAnyItemOperation(Operand1 x, Operand2 y);
    public abstract Returned arrayAnyNodeOperation(Operand1 x, Operand2 y);
    public abstract Returned arrayElementOperation(Operand1 x, Operand2 y);
    public abstract Returned arrayEnumOperation(Operand1 x, Operand2 y);
    public abstract Returned arrayBooleanOperation(Operand1 x, Operand2 y);
    public abstract Returned arrayNumberOperation(Operand1 x, Operand2 y);
    public abstract Returned arrayStringOperation(Operand1 x, Operand2 y);
    public abstract Returned arrayAnyMapOperation(Operand1 x, Operand2 y);
    public abstract Returned arrayMapOperation(Operand1 x, Operand2 y);
    public abstract Returned arrayChoiceOperation(Operand1 x, Operand2 y);
    public abstract Returned arrayAnyArrayOperation(Operand1 x, Operand2 y);
    public abstract Returned arrayArrayOperation(Operand1 x, Operand2 y);
    public abstract Returned arrayAnyFunctionOperation(Operand1 x, Operand2 y);
    public abstract Returned arrayFunctionOperation(Operand1 x, Operand2 y);
    public abstract Returned arrayRecordOperation(Operand1 x, Operand2 y);
    public abstract Returned arrayExtensibleRecordOperation(Operand1 x, Operand2 y);

    // ANY_FUNCTION operations
    public abstract Returned anyFunctionErrorOperation(Operand1 x, Operand2 y);
    public abstract Returned anyFunctionAnyItemOperation(Operand1 x, Operand2 y);
    public abstract Returned anyFunctionAnyNodeOperation(Operand1 x, Operand2 y);
    public abstract Returned anyFunctionElementOperation(Operand1 x, Operand2 y);
    public abstract Returned anyFunctionEnumOperation(Operand1 x, Operand2 y);
    public abstract Returned anyFunctionBooleanOperation(Operand1 x, Operand2 y);
    public abstract Returned anyFunctionNumberOperation(Operand1 x, Operand2 y);
    public abstract Returned anyFunctionStringOperation(Operand1 x, Operand2 y);
    public abstract Returned anyFunctionAnyMapOperation(Operand1 x, Operand2 y);
    public abstract Returned anyFunctionMapOperation(Operand1 x, Operand2 y);
    public abstract Returned anyFunctionChoiceOperation(Operand1 x, Operand2 y);
    public abstract Returned anyFunctionAnyArrayOperation(Operand1 x, Operand2 y);
    public abstract Returned anyFunctionArrayOperation(Operand1 x, Operand2 y);
    public abstract Returned anyFunctionAnyFunctionOperation(Operand1 x, Operand2 y);
    public abstract Returned anyFunctionFunctionOperation(Operand1 x, Operand2 y);
    public abstract Returned anyFunctionRecordOperation(Operand1 x, Operand2 y);
    public abstract Returned anyFunctionExtensibleRecordOperation(Operand1 x, Operand2 y);

    // FUNCTION operations
    public abstract Returned functionErrorOperation(Operand1 x, Operand2 y);
    public abstract Returned functionAnyItemOperation(Operand1 x, Operand2 y);
    public abstract Returned functionAnyNodeOperation(Operand1 x, Operand2 y);
    public abstract Returned functionElementOperation(Operand1 x, Operand2 y);
    public abstract Returned functionEnumOperation(Operand1 x, Operand2 y);
    public abstract Returned functionBooleanOperation(Operand1 x, Operand2 y);
    public abstract Returned functionNumberOperation(Operand1 x, Operand2 y);
    public abstract Returned functionStringOperation(Operand1 x, Operand2 y);
    public abstract Returned functionAnyMapOperation(Operand1 x, Operand2 y);
    public abstract Returned functionMapOperation(Operand1 x, Operand2 y);
    public abstract Returned functionChoiceOperation(Operand1 x, Operand2 y);
    public abstract Returned functionAnyArrayOperation(Operand1 x, Operand2 y);
    public abstract Returned functionArrayOperation(Operand1 x, Operand2 y);
    public abstract Returned functionAnyFunctionOperation(Operand1 x, Operand2 y);
    public abstract Returned functionFunctionOperation(Operand1 x, Operand2 y);
    public abstract Returned functionRecordOperation(Operand1 x, Operand2 y);
    public abstract Returned functionExtensibleRecordOperation(Operand1 x, Operand2 y);

    // ENUM operations
    public abstract Returned enumErrorOperation(Operand1 x, Operand2 y);
    public abstract Returned enumAnyItemOperation(Operand1 x, Operand2 y);
    public abstract Returned enumAnyNodeOperation(Operand1 x, Operand2 y);
    public abstract Returned enumElementOperation(Operand1 x, Operand2 y);
    public abstract Returned enumEnumOperation(Operand1 x, Operand2 y);
    public abstract Returned enumBooleanOperation(Operand1 x, Operand2 y);
    public abstract Returned enumNumberOperation(Operand1 x, Operand2 y);
    public abstract Returned enumStringOperation(Operand1 x, Operand2 y);
    public abstract Returned enumAnyMapOperation(Operand1 x, Operand2 y);
    public abstract Returned enumMapOperation(Operand1 x, Operand2 y);
    public abstract Returned enumChoiceOperation(Operand1 x, Operand2 y);
    public abstract Returned enumAnyArrayOperation(Operand1 x, Operand2 y);
    public abstract Returned enumArrayOperation(Operand1 x, Operand2 y);
    public abstract Returned enumAnyFunctionOperation(Operand1 x, Operand2 y);
    public abstract Returned enumFunctionOperation(Operand1 x, Operand2 y);
    public abstract Returned enumRecordOperation(Operand1 x, Operand2 y);
    public abstract Returned enumExtensibleRecordOperation(Operand1 x, Operand2 y);


    // BOOLEAN operations
    public abstract Returned booleanErrorOperation(Operand1 x, Operand2 y);
    public abstract Returned booleanAnyItemOperation(Operand1 x, Operand2 y);
    public abstract Returned booleanAnyNodeOperation(Operand1 x, Operand2 y);
    public abstract Returned booleanElementOperation(Operand1 x, Operand2 y);
    public abstract Returned booleanEnumOperation(Operand1 x, Operand2 y);
    public abstract Returned booleanBooleanOperation(Operand1 x, Operand2 y);
    public abstract Returned booleanNumberOperation(Operand1 x, Operand2 y);
    public abstract Returned booleanStringOperation(Operand1 x, Operand2 y);
    public abstract Returned booleanAnyMapOperation(Operand1 x, Operand2 y);
    public abstract Returned booleanMapOperation(Operand1 x, Operand2 y);
    public abstract Returned booleanChoiceOperation(Operand1 x, Operand2 y);
    public abstract Returned booleanAnyArrayOperation(Operand1 x, Operand2 y);
    public abstract Returned booleanArrayOperation(Operand1 x, Operand2 y);
    public abstract Returned booleanAnyFunctionOperation(Operand1 x, Operand2 y);
    public abstract Returned booleanFunctionOperation(Operand1 x, Operand2 y);
    public abstract Returned booleanRecordOperation(Operand1 x, Operand2 y);
    public abstract Returned booleanExtensibleRecordOperation(Operand1 x, Operand2 y);

    // STRING operations
    public abstract Returned stringErrorOperation(Operand1 x, Operand2 y);
    public abstract Returned stringAnyItemOperation(Operand1 x, Operand2 y);
    public abstract Returned stringAnyNodeOperation(Operand1 x, Operand2 y);
    public abstract Returned stringElementOperation(Operand1 x, Operand2 y);
    public abstract Returned stringEnumOperation(Operand1 x, Operand2 y);
    public abstract Returned stringBooleanOperation(Operand1 x, Operand2 y);
    public abstract Returned stringNumberOperation(Operand1 x, Operand2 y);
    public abstract Returned stringStringOperation(Operand1 x, Operand2 y);
    public abstract Returned stringAnyMapOperation(Operand1 x, Operand2 y);
    public abstract Returned stringMapOperation(Operand1 x, Operand2 y);
    public abstract Returned stringChoiceOperation(Operand1 x, Operand2 y);
    public abstract Returned stringAnyArrayOperation(Operand1 x, Operand2 y);
    public abstract Returned stringArrayOperation(Operand1 x, Operand2 y);
    public abstract Returned stringAnyFunctionOperation(Operand1 x, Operand2 y);
    public abstract Returned stringFunctionOperation(Operand1 x, Operand2 y);
    public abstract Returned stringRecordOperation(Operand1 x, Operand2 y);
    public abstract Returned stringExtensibleRecordOperation(Operand1 x, Operand2 y);

    // NUMBER operations
    public abstract Returned numberErrorOperation(Operand1 x, Operand2 y);
    public abstract Returned numberAnyItemOperation(Operand1 x, Operand2 y);
    public abstract Returned numberAnyNodeOperation(Operand1 x, Operand2 y);
    public abstract Returned numberElementOperation(Operand1 x, Operand2 y);
    public abstract Returned numberEnumOperation(Operand1 x, Operand2 y);
    public abstract Returned numberBooleanOperation(Operand1 x, Operand2 y);
    public abstract Returned numberNumberOperation(Operand1 x, Operand2 y);
    public abstract Returned numberStringOperation(Operand1 x, Operand2 y);
    public abstract Returned numberAnyMapOperation(Operand1 x, Operand2 y);
    public abstract Returned numberMapOperation(Operand1 x, Operand2 y);
    public abstract Returned numberChoiceOperation(Operand1 x, Operand2 y);
    public abstract Returned numberAnyArrayOperation(Operand1 x, Operand2 y);
    public abstract Returned numberArrayOperation(Operand1 x, Operand2 y);
    public abstract Returned numberAnyFunctionOperation(Operand1 x, Operand2 y);
    public abstract Returned numberFunctionOperation(Operand1 x, Operand2 y);
    public abstract Returned numberRecordOperation(Operand1 x, Operand2 y);
    public abstract Returned numberExtensibleRecordOperation(Operand1 x, Operand2 y);

    // CHOICE operations
    public abstract Returned choiceErrorOperation(Operand1 x, Operand2 y);
    public abstract Returned choiceAnyItemOperation(Operand1 x, Operand2 y);
    public abstract Returned choiceAnyNodeOperation(Operand1 x, Operand2 y);
    public abstract Returned choiceElementOperation(Operand1 x, Operand2 y);
    public abstract Returned choiceEnumOperation(Operand1 x, Operand2 y);
    public abstract Returned choiceBooleanOperation(Operand1 x, Operand2 y);
    public abstract Returned choiceNumberOperation(Operand1 x, Operand2 y);
    public abstract Returned choiceStringOperation(Operand1 x, Operand2 y);
    public abstract Returned choiceAnyMapOperation(Operand1 x, Operand2 y);
    public abstract Returned choiceMapOperation(Operand1 x, Operand2 y);
    public abstract Returned choiceChoiceOperation(Operand1 x, Operand2 y);
    public abstract Returned choiceAnyArrayOperation(Operand1 x, Operand2 y);
    public abstract Returned choiceArrayOperation(Operand1 x, Operand2 y);
    public abstract Returned choiceAnyFunctionOperation(Operand1 x, Operand2 y);
    public abstract Returned choiceFunctionOperation(Operand1 x, Operand2 y);
    public abstract Returned choiceRecordOperation(Operand1 x, Operand2 y);
    public abstract Returned choiceExtensibleRecordOperation(Operand1 x, Operand2 y);

    // RECORD operations
    public abstract Returned recordErrorOperation(Operand1 x, Operand2 y);
    public abstract Returned recordAnyItemOperation(Operand1 x, Operand2 y);
    public abstract Returned recordAnyNodeOperation(Operand1 x, Operand2 y);
    public abstract Returned recordElementOperation(Operand1 x, Operand2 y);
    public abstract Returned recordEnumOperation(Operand1 x, Operand2 y);
    public abstract Returned recordBooleanOperation(Operand1 x, Operand2 y);
    public abstract Returned recordNumberOperation(Operand1 x, Operand2 y);
    public abstract Returned recordStringOperation(Operand1 x, Operand2 y);
    public abstract Returned recordAnyMapOperation(Operand1 x, Operand2 y);
    public abstract Returned recordMapOperation(Operand1 x, Operand2 y);
    public abstract Returned recordChoiceOperation(Operand1 x, Operand2 y);
    public abstract Returned recordAnyArrayOperation(Operand1 x, Operand2 y);
    public abstract Returned recordArrayOperation(Operand1 x, Operand2 y);
    public abstract Returned recordAnyFunctionOperation(Operand1 x, Operand2 y);
    public abstract Returned recordFunctionOperation(Operand1 x, Operand2 y);
    public abstract Returned recordRecordOperation(Operand1 x, Operand2 y);
    public abstract Returned recordExtensibleRecordOperation(Operand1 x, Operand2 y);

    // EXTENSIBLE_RECORD operations
    public abstract Returned extensibleRecordErrorOperation(Operand1 x, Operand2 y);
    public abstract Returned extensibleRecordAnyItemOperation(Operand1 x, Operand2 y);
    public abstract Returned extensibleRecordAnyNodeOperation(Operand1 x, Operand2 y);
    public abstract Returned extensibleRecordElementOperation(Operand1 x, Operand2 y);
    public abstract Returned extensibleRecordEnumOperation(Operand1 x, Operand2 y);
    public abstract Returned extensibleRecordBooleanOperation(Operand1 x, Operand2 y);
    public abstract Returned extensibleRecordNumberOperation(Operand1 x, Operand2 y);
    public abstract Returned extensibleRecordStringOperation(Operand1 x, Operand2 y);
    public abstract Returned extensibleRecordAnyMapOperation(Operand1 x, Operand2 y);
    public abstract Returned extensibleRecordMapOperation(Operand1 x, Operand2 y);
    public abstract Returned extensibleRecordChoiceOperation(Operand1 x, Operand2 y);
    public abstract Returned extensibleRecordAnyArrayOperation(Operand1 x, Operand2 y);
    public abstract Returned extensibleRecordArrayOperation(Operand1 x, Operand2 y);
    public abstract Returned extensibleRecordAnyFunctionOperation(Operand1 x, Operand2 y);
    public abstract Returned extensibleRecordFunctionOperation(Operand1 x, Operand2 y);
    public abstract Returned extensibleRecordRecordOperation(Operand1 x, Operand2 y);
    public abstract Returned extensibleRecordExtensibleRecordOperation(Operand1 x, Operand2 y);


}
