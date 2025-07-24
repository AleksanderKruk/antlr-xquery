package com.github.akruk.antlrxquery.typesystem.typeoperations.itemtype;

import java.util.function.BiFunction;

import com.github.akruk.antlrxquery.typesystem.defaults.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryTypes;

@SuppressWarnings("unchecked")
public abstract class ItemtypeBinaryOperation<Returned>
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

    protected final BiFunction<XQueryItemType, XQueryItemType, Returned>[][] automaton;

    public ItemtypeBinaryOperation()
    {
        this.automaton = getAutomaton();
    }


    private BiFunction<XQueryItemType, XQueryItemType, Returned>[][] getAutomaton() {
        final BiFunction<XQueryItemType, XQueryItemType, Returned>[][] automaton = new BiFunction[typesCount][typesCount];
        automaton[ERROR][ERROR] = this::errorErrorOperation;
        automaton[ERROR][ANY_ITEM] = this::errorAnyItemOperation;
        automaton[ERROR][ANY_NODE] = this::errorAnyNodeOperation;
        automaton[ERROR][ELEMENT] = this::errorElementOperation;
        automaton[ERROR][ENUM] = this::errorEnumOperation;
        automaton[ERROR][BOOLEAN] = this::errorBooleanOperation;
        automaton[ERROR][NUMBER] = this::errorNumberOperation;
        automaton[ERROR][STRING] = this::errorStringOperation;
        automaton[ERROR][ANY_MAP] = this::errorAnyMapOperation;
        automaton[ERROR][MAP] = this::errorMapOperation;
        automaton[ERROR][CHOICE] = this::errorChoiceOperation;
        automaton[ERROR][ANY_ARRAY] = this::errorAnyArrayOperation;
        automaton[ERROR][ARRAY] = this::errorArrayOperation;
        automaton[ERROR][ANY_FUNCTION] = this::errorAnyFunctionOperation;
        automaton[ERROR][FUNCTION] = this::errorFunctionOperation;
        automaton[ERROR][RECORD] = this::errorRecordOperation;
        automaton[ERROR][EXTENSIBLE_RECORD] = this::errorExtensibleRecordOperation;

        automaton[ANY_ITEM][ERROR] = this::anyItemErrorOperation;
        automaton[ANY_ITEM][ANY_ITEM] = this::anyItemAnyItemOperation;
        automaton[ANY_ITEM][ANY_NODE] = this::anyItemAnyNodeOperation;
        automaton[ANY_ITEM][ELEMENT] = this::anyItemElementOperation;
        automaton[ANY_ITEM][ENUM] = this::anyItemEnumOperation;
        automaton[ANY_ITEM][BOOLEAN] = this::anyItemBooleanOperation;
        automaton[ANY_ITEM][NUMBER] = this::anyItemNumberOperation;
        automaton[ANY_ITEM][STRING] = this::anyItemStringOperation;
        automaton[ANY_ITEM][ANY_MAP] = this::anyItemAnyMapOperation;
        automaton[ANY_ITEM][MAP] = this::anyItemMapOperation;
        automaton[ANY_ITEM][CHOICE] = this::anyItemChoiceOperation;
        automaton[ANY_ITEM][ANY_ARRAY] = this::anyItemAnyArrayOperation;
        automaton[ANY_ITEM][ARRAY] = this::anyItemArrayOperation;
        automaton[ANY_ITEM][ANY_FUNCTION] = this::anyItemAnyFunctionOperation;
        automaton[ANY_ITEM][FUNCTION] = this::anyItemFunctionOperation;
        automaton[ANY_ITEM][RECORD] = this::anyItemRecordOperation;
        automaton[ANY_ITEM][EXTENSIBLE_RECORD] = this::anyItemExtensibleRecordOperation;

        automaton[ANY_NODE][ERROR] = this::anyNodeErrorOperation;
        automaton[ANY_NODE][ANY_ITEM] = this::anyNodeAnyItemOperation;
        automaton[ANY_NODE][ANY_NODE] = this::anyNodeAnyNodeOperation;
        automaton[ANY_NODE][ELEMENT] = this::anyNodeElementOperation;
        automaton[ANY_NODE][ENUM] = this::anyNodeEnumOperation;
        automaton[ANY_NODE][BOOLEAN] = this::anyNodeBooleanOperation;
        automaton[ANY_NODE][NUMBER] = this::anyNodeNumberOperation;
        automaton[ANY_NODE][STRING] = this::anyNodeStringOperation;
        automaton[ANY_NODE][ANY_MAP] = this::anyNodeAnyMapOperation;
        automaton[ANY_NODE][MAP] = this::anyNodeMapOperation;
        automaton[ANY_NODE][CHOICE] = this::anyNodeChoiceOperation;
        automaton[ANY_NODE][ANY_ARRAY] = this::anyNodeAnyArrayOperation;
        automaton[ANY_NODE][ARRAY] = this::anyNodeArrayOperation;
        automaton[ANY_NODE][ANY_FUNCTION] = this::anyNodeAnyFunctionOperation;
        automaton[ANY_NODE][FUNCTION] = this::anyNodeFunctionOperation;
        automaton[ANY_NODE][RECORD] = this::anyNodeRecordOperation;
        automaton[ANY_NODE][EXTENSIBLE_RECORD] = this::anyNodeExtensibleRecordOperation;

        automaton[ELEMENT][ERROR] = this::elementErrorOperation;
        automaton[ELEMENT][ANY_ITEM] = this::elementAnyItemOperation;
        automaton[ELEMENT][ANY_NODE] = this::elementAnyNodeOperation;
        automaton[ELEMENT][ELEMENT] = this::elementElementOperation;
        automaton[ELEMENT][ENUM] = this::elementEnumOperation;
        automaton[ELEMENT][BOOLEAN] = this::elementBooleanOperation;
        automaton[ELEMENT][NUMBER] = this::elementNumberOperation;
        automaton[ELEMENT][STRING] = this::elementStringOperation;
        automaton[ELEMENT][ANY_MAP] = this::elementAnyMapOperation;
        automaton[ELEMENT][MAP] = this::elementMapOperation;
        automaton[ELEMENT][CHOICE] = this::elementChoiceOperation;
        automaton[ELEMENT][ANY_ARRAY] = this::elementAnyArrayOperation;
        automaton[ELEMENT][ARRAY] = this::elementArrayOperation;
        automaton[ELEMENT][ANY_FUNCTION] = this::elementAnyFunctionOperation;
        automaton[ELEMENT][FUNCTION] = this::elementFunctionOperation;
        automaton[ELEMENT][RECORD] = this::elementRecordOperation;
        automaton[ELEMENT][EXTENSIBLE_RECORD] = this::elementExtensibleRecordOperation;

        automaton[ANY_MAP][ERROR] = this::anyMapErrorOperation;
        automaton[ANY_MAP][ANY_ITEM] = this::anyMapAnyItemOperation;
        automaton[ANY_MAP][ANY_NODE] = this::anyMapAnyNodeOperation;
        automaton[ANY_MAP][ELEMENT] = this::anyMapElementOperation;
        automaton[ANY_MAP][ENUM] = this::anyMapEnumOperation;
        automaton[ANY_MAP][BOOLEAN] = this::anyMapBooleanOperation;
        automaton[ANY_MAP][NUMBER] = this::anyMapNumberOperation;
        automaton[ANY_MAP][STRING] = this::anyMapStringOperation;
        automaton[ANY_MAP][ANY_MAP] = this::anyMapAnyMapOperation;
        automaton[ANY_MAP][MAP] = this::anyMapMapOperation;
        automaton[ANY_MAP][CHOICE] = this::anyMapChoiceOperation;
        automaton[ANY_MAP][ANY_ARRAY] = this::anyMapAnyArrayOperation;
        automaton[ANY_MAP][ARRAY] = this::anyMapArrayOperation;
        automaton[ANY_MAP][ANY_FUNCTION] = this::anyMapAnyFunctionOperation;
        automaton[ANY_MAP][FUNCTION] = this::anyMapFunctionOperation;
        automaton[ANY_MAP][RECORD] = this::anyMapRecordOperation;
        automaton[ANY_MAP][EXTENSIBLE_RECORD] = this::anyMapExtensibleRecordOperation;

        automaton[MAP][ERROR] = this::mapErrorOperation;
        automaton[MAP][ANY_ITEM] = this::mapAnyItemOperation;
        automaton[MAP][ANY_NODE] = this::mapAnyNodeOperation;
        automaton[MAP][ELEMENT] = this::mapElementOperation;
        automaton[MAP][ENUM] = this::mapEnumOperation;
        automaton[MAP][BOOLEAN] = this::mapBooleanOperation;
        automaton[MAP][NUMBER] = this::mapNumberOperation;
        automaton[MAP][STRING] = this::mapStringOperation;
        automaton[MAP][ANY_MAP] = this::mapAnyMapOperation;
        automaton[MAP][MAP] = this::mapMapOperation;
        automaton[MAP][CHOICE] = this::mapChoiceOperation;
        automaton[MAP][ANY_ARRAY] = this::mapAnyArrayOperation;
        automaton[MAP][ARRAY] = this::mapArrayOperation;
        automaton[MAP][ANY_FUNCTION] = this::mapAnyFunctionOperation;
        automaton[MAP][FUNCTION] = this::mapFunctionOperation;
        automaton[MAP][RECORD] = this::mapRecordOperation;
        automaton[MAP][EXTENSIBLE_RECORD] = this::mapExtensibleRecordOperation;

        automaton[ANY_ARRAY][ERROR] = this::anyArrayErrorOperation;
        automaton[ANY_ARRAY][ANY_ITEM] = this::anyArrayAnyItemOperation;
        automaton[ANY_ARRAY][ANY_NODE] = this::anyArrayAnyNodeOperation;
        automaton[ANY_ARRAY][ELEMENT] = this::anyArrayElementOperation;
        automaton[ANY_ARRAY][ENUM] = this::anyArrayEnumOperation;
        automaton[ANY_ARRAY][BOOLEAN] = this::anyArrayBooleanOperation;
        automaton[ANY_ARRAY][NUMBER] = this::anyArrayNumberOperation;
        automaton[ANY_ARRAY][STRING] = this::anyArrayStringOperation;
        automaton[ANY_ARRAY][ANY_MAP] = this::anyArrayAnyMapOperation;
        automaton[ANY_ARRAY][MAP] = this::anyArrayMapOperation;
        automaton[ANY_ARRAY][CHOICE] = this::anyArrayChoiceOperation;
        automaton[ANY_ARRAY][ANY_ARRAY] = this::anyArrayAnyArrayOperation;
        automaton[ANY_ARRAY][ARRAY] = this::anyArrayArrayOperation;
        automaton[ANY_ARRAY][ANY_FUNCTION] = this::anyArrayAnyFunctionOperation;
        automaton[ANY_ARRAY][FUNCTION] = this::anyArrayFunctionOperation;
        automaton[ANY_ARRAY][RECORD] = this::anyArrayRecordOperation;
        automaton[ANY_ARRAY][EXTENSIBLE_RECORD] = this::anyArrayExtensibleRecordOperation;

        automaton[ARRAY][ERROR] = this::arrayErrorOperation;
        automaton[ARRAY][ANY_ITEM] = this::arrayAnyItemOperation;
        automaton[ARRAY][ANY_NODE] = this::arrayAnyNodeOperation;
        automaton[ARRAY][ELEMENT] = this::arrayElementOperation;
        automaton[ARRAY][ENUM] = this::arrayEnumOperation;
        automaton[ARRAY][BOOLEAN] = this::arrayBooleanOperation;
        automaton[ARRAY][NUMBER] = this::arrayNumberOperation;
        automaton[ARRAY][STRING] = this::arrayStringOperation;
        automaton[ARRAY][ANY_MAP] = this::arrayAnyMapOperation;
        automaton[ARRAY][MAP] = this::arrayMapOperation;
        automaton[ARRAY][CHOICE] = this::arrayChoiceOperation;
        automaton[ARRAY][ANY_ARRAY] = this::arrayAnyArrayOperation;
        automaton[ARRAY][ARRAY] = this::arrayArrayOperation;
        automaton[ARRAY][ANY_FUNCTION] = this::arrayAnyFunctionOperation;
        automaton[ARRAY][FUNCTION] = this::arrayFunctionOperation;
        automaton[ARRAY][RECORD] = this::arrayRecordOperation;
        automaton[ARRAY][EXTENSIBLE_RECORD] = this::arrayExtensibleRecordOperation;

        automaton[ANY_FUNCTION][ERROR] = this::anyFunctionErrorOperation;
        automaton[ANY_FUNCTION][ANY_ITEM] = this::anyFunctionAnyItemOperation;
        automaton[ANY_FUNCTION][ANY_NODE] = this::anyFunctionAnyNodeOperation;
        automaton[ANY_FUNCTION][ELEMENT] = this::anyFunctionElementOperation;
        automaton[ANY_FUNCTION][ENUM] = this::anyFunctionEnumOperation;
        automaton[ANY_FUNCTION][BOOLEAN] = this::anyFunctionBooleanOperation;
        automaton[ANY_FUNCTION][NUMBER] = this::anyFunctionNumberOperation;
        automaton[ANY_FUNCTION][STRING] = this::anyFunctionStringOperation;
        automaton[ANY_FUNCTION][ANY_MAP] = this::anyFunctionAnyMapOperation;
        automaton[ANY_FUNCTION][MAP] = this::anyFunctionMapOperation;
        automaton[ANY_FUNCTION][CHOICE] = this::anyFunctionChoiceOperation;
        automaton[ANY_FUNCTION][ANY_ARRAY] = this::anyFunctionAnyArrayOperation;
        automaton[ANY_FUNCTION][ARRAY] = this::anyFunctionArrayOperation;
        automaton[ANY_FUNCTION][ANY_FUNCTION] = this::anyFunctionAnyFunctionOperation;
        automaton[ANY_FUNCTION][FUNCTION] = this::anyFunctionFunctionOperation;
        automaton[ANY_FUNCTION][RECORD] = this::anyFunctionRecordOperation;
        automaton[ANY_FUNCTION][EXTENSIBLE_RECORD] = this::anyFunctionExtensibleRecordOperation;

        automaton[FUNCTION][ERROR] = this::functionErrorOperation;
        automaton[FUNCTION][ANY_ITEM] = this::functionAnyItemOperation;
        automaton[FUNCTION][ANY_NODE] = this::functionAnyNodeOperation;
        automaton[FUNCTION][ELEMENT] = this::functionElementOperation;
        automaton[FUNCTION][ENUM] = this::functionEnumOperation;
        automaton[FUNCTION][BOOLEAN] = this::functionBooleanOperation;
        automaton[FUNCTION][NUMBER] = this::functionNumberOperation;
        automaton[FUNCTION][STRING] = this::functionStringOperation;
        automaton[FUNCTION][ANY_MAP] = this::functionAnyMapOperation;
        automaton[FUNCTION][MAP] = this::functionMapOperation;
        automaton[FUNCTION][CHOICE] = this::functionChoiceOperation;
        automaton[FUNCTION][ANY_ARRAY] = this::functionAnyArrayOperation;
        automaton[FUNCTION][ARRAY] = this::functionArrayOperation;
        automaton[FUNCTION][ANY_FUNCTION] = this::functionAnyFunctionOperation;
        automaton[FUNCTION][FUNCTION] = this::functionFunctionOperation;
        automaton[FUNCTION][RECORD] = this::functionRecordOperation;
        automaton[FUNCTION][EXTENSIBLE_RECORD] = this::functionExtensibleRecordOperation;

        automaton[ENUM][ERROR] = this::enumErrorOperation;
        automaton[ENUM][ANY_ITEM] = this::enumAnyItemOperation;
        automaton[ENUM][ANY_NODE] = this::enumAnyNodeOperation;
        automaton[ENUM][ELEMENT] = this::enumElementOperation;
        automaton[ENUM][ENUM] = this::enumEnumOperation;
        automaton[ENUM][BOOLEAN] = this::enumBooleanOperation;
        automaton[ENUM][NUMBER] = this::enumNumberOperation;
        automaton[ENUM][STRING] = this::enumStringOperation;
        automaton[ENUM][ANY_MAP] = this::enumAnyMapOperation;
        automaton[ENUM][MAP] = this::enumMapOperation;
        automaton[ENUM][CHOICE] = this::enumChoiceOperation;
        automaton[ENUM][ANY_ARRAY] = this::enumAnyArrayOperation;
        automaton[ENUM][ARRAY] = this::enumArrayOperation;
        automaton[ENUM][ANY_FUNCTION] = this::enumAnyFunctionOperation;
        automaton[ENUM][FUNCTION] = this::enumFunctionOperation;
        automaton[ENUM][RECORD] = this::enumRecordOperation;
        automaton[ENUM][EXTENSIBLE_RECORD] = this::enumExtensibleRecordOperation;

        automaton[RECORD][ERROR] = this::recordErrorOperation;
        automaton[RECORD][ANY_ITEM] = this::recordAnyItemOperation;
        automaton[RECORD][ANY_NODE] = this::recordAnyNodeOperation;
        automaton[RECORD][ELEMENT] = this::recordElementOperation;
        automaton[RECORD][ENUM] = this::recordEnumOperation;
        automaton[RECORD][BOOLEAN] = this::recordBooleanOperation;
        automaton[RECORD][NUMBER] = this::recordNumberOperation;
        automaton[RECORD][STRING] = this::recordStringOperation;
        automaton[RECORD][ANY_MAP] = this::recordAnyMapOperation;
        automaton[RECORD][MAP] = this::recordMapOperation;
        automaton[RECORD][CHOICE] = this::recordChoiceOperation;
        automaton[RECORD][ANY_ARRAY] = this::recordAnyArrayOperation;
        automaton[RECORD][ARRAY] = this::recordArrayOperation;
        automaton[RECORD][ANY_FUNCTION] = this::recordAnyFunctionOperation;
        automaton[RECORD][FUNCTION] = this::recordFunctionOperation;
        automaton[RECORD][RECORD] = this::recordRecordOperation;
        automaton[RECORD][EXTENSIBLE_RECORD] = this::recordExtensibleRecordOperation;

        automaton[EXTENSIBLE_RECORD][ERROR] = this::extensibleRecordErrorOperation;
        automaton[EXTENSIBLE_RECORD][ANY_ITEM] = this::extensibleRecordAnyItemOperation;
        automaton[EXTENSIBLE_RECORD][ANY_NODE] = this::extensibleRecordAnyNodeOperation;
        automaton[EXTENSIBLE_RECORD][ELEMENT] = this::extensibleRecordElementOperation;
        automaton[EXTENSIBLE_RECORD][ENUM] = this::extensibleRecordEnumOperation;
        automaton[EXTENSIBLE_RECORD][BOOLEAN] = this::extensibleRecordBooleanOperation;
        automaton[EXTENSIBLE_RECORD][NUMBER] = this::extensibleRecordNumberOperation;
        automaton[EXTENSIBLE_RECORD][STRING] = this::extensibleRecordStringOperation;
        automaton[EXTENSIBLE_RECORD][ANY_MAP] = this::extensibleRecordAnyMapOperation;
        automaton[EXTENSIBLE_RECORD][MAP] = this::extensibleRecordMapOperation;
        automaton[EXTENSIBLE_RECORD][CHOICE] = this::extensibleRecordChoiceOperation;
        automaton[EXTENSIBLE_RECORD][ANY_ARRAY] = this::extensibleRecordAnyArrayOperation;
        automaton[EXTENSIBLE_RECORD][ARRAY] = this::extensibleRecordArrayOperation;
        automaton[EXTENSIBLE_RECORD][ANY_FUNCTION] = this::extensibleRecordAnyFunctionOperation;
        automaton[EXTENSIBLE_RECORD][FUNCTION] = this::extensibleRecordFunctionOperation;
        automaton[EXTENSIBLE_RECORD][RECORD] = this::extensibleRecordRecordOperation;
        automaton[EXTENSIBLE_RECORD][EXTENSIBLE_RECORD] = this::extensibleRecordExtensibleRecordOperation;

        automaton[BOOLEAN][ERROR] = this::booleanErrorOperation;
        automaton[BOOLEAN][ANY_ITEM] = this::booleanAnyItemOperation;
        automaton[BOOLEAN][ANY_NODE] = this::booleanAnyNodeOperation;
        automaton[BOOLEAN][ELEMENT] = this::booleanElementOperation;
        automaton[BOOLEAN][ENUM] = this::booleanEnumOperation;
        automaton[BOOLEAN][BOOLEAN] = this::booleanBooleanOperation;
        automaton[BOOLEAN][NUMBER] = this::booleanNumberOperation;
        automaton[BOOLEAN][STRING] = this::booleanStringOperation;
        automaton[BOOLEAN][ANY_MAP] = this::booleanAnyMapOperation;
        automaton[BOOLEAN][MAP] = this::booleanMapOperation;
        automaton[BOOLEAN][CHOICE] = this::booleanChoiceOperation;
        automaton[BOOLEAN][ANY_ARRAY] = this::booleanAnyArrayOperation;
        automaton[BOOLEAN][ARRAY] = this::booleanArrayOperation;
        automaton[BOOLEAN][ANY_FUNCTION] = this::booleanAnyFunctionOperation;
        automaton[BOOLEAN][FUNCTION] = this::booleanFunctionOperation;
        automaton[BOOLEAN][RECORD] = this::booleanRecordOperation;
        automaton[BOOLEAN][EXTENSIBLE_RECORD] = this::booleanExtensibleRecordOperation;

        automaton[STRING][ERROR] = this::stringErrorOperation;
        automaton[STRING][ANY_ITEM] = this::stringAnyItemOperation;
        automaton[STRING][ANY_NODE] = this::stringAnyNodeOperation;
        automaton[STRING][ELEMENT] = this::stringElementOperation;
        automaton[STRING][ENUM] = this::stringEnumOperation;
        automaton[STRING][BOOLEAN] = this::stringBooleanOperation;
        automaton[STRING][NUMBER] = this::stringNumberOperation;
        automaton[STRING][STRING] = this::stringStringOperation;
        automaton[STRING][ANY_MAP] = this::stringAnyMapOperation;
        automaton[STRING][MAP] = this::stringMapOperation;
        automaton[STRING][CHOICE] = this::stringChoiceOperation;
        automaton[STRING][ANY_ARRAY] = this::stringAnyArrayOperation;
        automaton[STRING][ARRAY] = this::stringArrayOperation;
        automaton[STRING][ANY_FUNCTION] = this::stringAnyFunctionOperation;
        automaton[STRING][FUNCTION] = this::stringFunctionOperation;
        automaton[STRING][RECORD] = this::stringRecordOperation;
        automaton[STRING][EXTENSIBLE_RECORD] = this::stringExtensibleRecordOperation;

        automaton[NUMBER][ERROR] = this::numberErrorOperation;
        automaton[NUMBER][ANY_ITEM] = this::numberAnyItemOperation;
        automaton[NUMBER][ANY_NODE] = this::numberAnyNodeOperation;
        automaton[NUMBER][ELEMENT] = this::numberElementOperation;
        automaton[NUMBER][ENUM] = this::numberEnumOperation;
        automaton[NUMBER][BOOLEAN] = this::numberBooleanOperation;
        automaton[NUMBER][NUMBER] = this::numberNumberOperation;
        automaton[NUMBER][STRING] = this::numberStringOperation;
        automaton[NUMBER][ANY_MAP] = this::numberAnyMapOperation;
        automaton[NUMBER][MAP] = this::numberMapOperation;
        automaton[NUMBER][CHOICE] = this::numberChoiceOperation;
        automaton[NUMBER][ANY_ARRAY] = this::numberAnyArrayOperation;
        automaton[NUMBER][ARRAY] = this::numberArrayOperation;
        automaton[NUMBER][ANY_FUNCTION] = this::numberAnyFunctionOperation;
        automaton[NUMBER][FUNCTION] = this::numberFunctionOperation;
        automaton[NUMBER][RECORD] = this::numberRecordOperation;
        automaton[NUMBER][EXTENSIBLE_RECORD] = this::numberExtensibleRecordOperation;

        automaton[CHOICE][ERROR] = this::choiceErrorOperation;
        automaton[CHOICE][ANY_ITEM] = this::choiceAnyItemOperation;
        automaton[CHOICE][ANY_NODE] = this::choiceAnyNodeOperation;
        automaton[CHOICE][ELEMENT] = this::choiceElementOperation;
        automaton[CHOICE][ENUM] = this::choiceEnumOperation;
        automaton[CHOICE][BOOLEAN] = this::choiceBooleanOperation;
        automaton[CHOICE][NUMBER] = this::choiceNumberOperation;
        automaton[CHOICE][STRING] = this::choiceStringOperation;
        automaton[CHOICE][ANY_MAP] = this::choiceAnyMapOperation;
        automaton[CHOICE][MAP] = this::choiceMapOperation;
        automaton[CHOICE][CHOICE] = this::choiceChoiceOperation;
        automaton[CHOICE][ANY_ARRAY] = this::choiceAnyArrayOperation;
        automaton[CHOICE][ARRAY] = this::choiceArrayOperation;
        automaton[CHOICE][ANY_FUNCTION] = this::choiceAnyFunctionOperation;
        automaton[CHOICE][FUNCTION] = this::choiceFunctionOperation;
        automaton[CHOICE][RECORD] = this::choiceRecordOperation;
        automaton[CHOICE][EXTENSIBLE_RECORD] = this::choiceExtensibleRecordOperation;
        return automaton;
    }


    public abstract Returned errorErrorOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned errorAnyItemOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned errorAnyNodeOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned errorElementOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned errorEnumOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned errorBooleanOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned errorNumberOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned errorStringOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned errorAnyMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned errorMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned errorChoiceOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned errorAnyArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned errorArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned errorAnyFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned errorFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned errorRecordOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned errorExtensibleRecordOperation(XQueryItemType x, XQueryItemType y);

    // ANY_ITEM operations
    public abstract Returned anyItemErrorOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyItemAnyItemOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyItemAnyNodeOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyItemElementOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyItemEnumOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyItemBooleanOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyItemNumberOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyItemStringOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyItemAnyMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyItemMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyItemChoiceOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyItemAnyArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyItemArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyItemAnyFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyItemFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyItemRecordOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyItemExtensibleRecordOperation(XQueryItemType x, XQueryItemType y);

    // ANY_NODE operations
    public abstract Returned anyNodeErrorOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyNodeAnyItemOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyNodeAnyNodeOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyNodeElementOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyNodeEnumOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyNodeBooleanOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyNodeNumberOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyNodeStringOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyNodeAnyMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyNodeMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyNodeChoiceOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyNodeAnyArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyNodeArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyNodeAnyFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyNodeFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyNodeRecordOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyNodeExtensibleRecordOperation(XQueryItemType x, XQueryItemType y);
    // ELEMENT operations
    public abstract Returned elementErrorOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned elementAnyItemOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned elementAnyNodeOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned elementElementOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned elementEnumOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned elementBooleanOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned elementNumberOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned elementStringOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned elementAnyMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned elementMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned elementChoiceOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned elementAnyArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned elementArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned elementAnyFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned elementFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned elementRecordOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned elementExtensibleRecordOperation(XQueryItemType x, XQueryItemType y);



    // ANY_MAP operations
    public abstract Returned anyMapErrorOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyMapAnyItemOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyMapAnyNodeOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyMapElementOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyMapEnumOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyMapBooleanOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyMapNumberOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyMapStringOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyMapAnyMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyMapMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyMapChoiceOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyMapAnyArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyMapArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyMapAnyFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyMapFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyMapRecordOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyMapExtensibleRecordOperation(XQueryItemType x, XQueryItemType y);

    // MAP operations
    public abstract Returned mapErrorOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned mapAnyItemOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned mapAnyNodeOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned mapElementOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned mapEnumOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned mapBooleanOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned mapNumberOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned mapStringOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned mapAnyMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned mapMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned mapChoiceOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned mapAnyArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned mapArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned mapAnyFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned mapFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned mapRecordOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned mapExtensibleRecordOperation(XQueryItemType x, XQueryItemType y);

    // ANY_ARRAY operations
    public abstract Returned anyArrayErrorOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyArrayAnyItemOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyArrayAnyNodeOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyArrayElementOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyArrayEnumOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyArrayBooleanOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyArrayNumberOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyArrayStringOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyArrayAnyMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyArrayMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyArrayChoiceOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyArrayAnyArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyArrayArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyArrayAnyFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyArrayFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyArrayRecordOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyArrayExtensibleRecordOperation(XQueryItemType x, XQueryItemType y);


// ARRAY operations
    public abstract Returned arrayErrorOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned arrayAnyItemOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned arrayAnyNodeOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned arrayElementOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned arrayEnumOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned arrayBooleanOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned arrayNumberOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned arrayStringOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned arrayAnyMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned arrayMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned arrayChoiceOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned arrayAnyArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned arrayArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned arrayAnyFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned arrayFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned arrayRecordOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned arrayExtensibleRecordOperation(XQueryItemType x, XQueryItemType y);

    // ANY_FUNCTION operations
    public abstract Returned anyFunctionErrorOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyFunctionAnyItemOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyFunctionAnyNodeOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyFunctionElementOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyFunctionEnumOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyFunctionBooleanOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyFunctionNumberOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyFunctionStringOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyFunctionAnyMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyFunctionMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyFunctionChoiceOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyFunctionAnyArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyFunctionArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyFunctionAnyFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyFunctionFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyFunctionRecordOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned anyFunctionExtensibleRecordOperation(XQueryItemType x, XQueryItemType y);

    // FUNCTION operations
    public abstract Returned functionErrorOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned functionAnyItemOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned functionAnyNodeOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned functionElementOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned functionEnumOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned functionBooleanOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned functionNumberOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned functionStringOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned functionAnyMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned functionMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned functionChoiceOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned functionAnyArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned functionArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned functionAnyFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned functionFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned functionRecordOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned functionExtensibleRecordOperation(XQueryItemType x, XQueryItemType y);

    // ENUM operations
    public abstract Returned enumErrorOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned enumAnyItemOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned enumAnyNodeOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned enumElementOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned enumEnumOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned enumBooleanOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned enumNumberOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned enumStringOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned enumAnyMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned enumMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned enumChoiceOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned enumAnyArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned enumArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned enumAnyFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned enumFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned enumRecordOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned enumExtensibleRecordOperation(XQueryItemType x, XQueryItemType y);


    // BOOLEAN operations
    public abstract Returned booleanErrorOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned booleanAnyItemOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned booleanAnyNodeOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned booleanElementOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned booleanEnumOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned booleanBooleanOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned booleanNumberOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned booleanStringOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned booleanAnyMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned booleanMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned booleanChoiceOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned booleanAnyArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned booleanArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned booleanAnyFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned booleanFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned booleanRecordOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned booleanExtensibleRecordOperation(XQueryItemType x, XQueryItemType y);

    // STRING operations
    public abstract Returned stringErrorOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned stringAnyItemOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned stringAnyNodeOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned stringElementOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned stringEnumOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned stringBooleanOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned stringNumberOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned stringStringOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned stringAnyMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned stringMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned stringChoiceOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned stringAnyArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned stringArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned stringAnyFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned stringFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned stringRecordOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned stringExtensibleRecordOperation(XQueryItemType x, XQueryItemType y);

    // NUMBER operations
    public abstract Returned numberErrorOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned numberAnyItemOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned numberAnyNodeOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned numberElementOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned numberEnumOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned numberBooleanOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned numberNumberOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned numberStringOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned numberAnyMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned numberMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned numberChoiceOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned numberAnyArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned numberArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned numberAnyFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned numberFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned numberRecordOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned numberExtensibleRecordOperation(XQueryItemType x, XQueryItemType y);

    // CHOICE operations
    public abstract Returned choiceErrorOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned choiceAnyItemOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned choiceAnyNodeOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned choiceElementOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned choiceEnumOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned choiceBooleanOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned choiceNumberOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned choiceStringOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned choiceAnyMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned choiceMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned choiceChoiceOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned choiceAnyArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned choiceArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned choiceAnyFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned choiceFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned choiceRecordOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned choiceExtensibleRecordOperation(XQueryItemType x, XQueryItemType y);

    // RECORD operations
    public abstract Returned recordErrorOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned recordAnyItemOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned recordAnyNodeOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned recordElementOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned recordEnumOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned recordBooleanOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned recordNumberOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned recordStringOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned recordAnyMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned recordMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned recordChoiceOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned recordAnyArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned recordArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned recordAnyFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned recordFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned recordRecordOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned recordExtensibleRecordOperation(XQueryItemType x, XQueryItemType y);

    // EXTENSIBLE_RECORD operations
    public abstract Returned extensibleRecordErrorOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned extensibleRecordAnyItemOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned extensibleRecordAnyNodeOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned extensibleRecordElementOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned extensibleRecordEnumOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned extensibleRecordBooleanOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned extensibleRecordNumberOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned extensibleRecordStringOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned extensibleRecordAnyMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned extensibleRecordMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned extensibleRecordChoiceOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned extensibleRecordAnyArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned extensibleRecordArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned extensibleRecordAnyFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned extensibleRecordFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned extensibleRecordRecordOperation(XQueryItemType x, XQueryItemType y);
    public abstract Returned extensibleRecordExtensibleRecordOperation(XQueryItemType x, XQueryItemType y);


}
