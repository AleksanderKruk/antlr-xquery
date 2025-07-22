package com.github.akruk.antlrxquery.typesystem.typeoperations;

import java.util.function.BiPredicate;

import com.github.akruk.antlrxquery.typesystem.defaults.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryTypes;

@SuppressWarnings("unchecked")
public abstract class ItemtypeBinaryPredicateOperation
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

    protected final BiPredicate<XQueryItemType, XQueryItemType>[][] automaton;

    public ItemtypeBinaryPredicateOperation()
    {
        this.automaton = getAutomaton();
    }


    private BiPredicate<XQueryItemType, XQueryItemType>[][] getAutomaton() {
        final BiPredicate<XQueryItemType, XQueryItemType>[][] automaton = new BiPredicate[typesCount][typesCount];
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


    public abstract boolean errorErrorOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean errorAnyItemOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean errorAnyNodeOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean errorElementOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean errorEnumOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean errorBooleanOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean errorNumberOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean errorStringOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean errorAnyMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean errorMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean errorChoiceOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean errorAnyArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean errorArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean errorAnyFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean errorFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean errorRecordOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean errorExtensibleRecordOperation(XQueryItemType x, XQueryItemType y);

    // ANY_ITEM operations
    public abstract boolean anyItemErrorOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyItemAnyItemOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyItemAnyNodeOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyItemElementOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyItemEnumOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyItemBooleanOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyItemNumberOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyItemStringOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyItemAnyMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyItemMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyItemChoiceOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyItemAnyArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyItemArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyItemAnyFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyItemFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyItemRecordOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyItemExtensibleRecordOperation(XQueryItemType x, XQueryItemType y);

    // ANY_NODE operations
    public abstract boolean anyNodeErrorOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyNodeAnyItemOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyNodeAnyNodeOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyNodeElementOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyNodeEnumOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyNodeBooleanOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyNodeNumberOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyNodeStringOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyNodeAnyMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyNodeMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyNodeChoiceOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyNodeAnyArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyNodeArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyNodeAnyFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyNodeFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyNodeRecordOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyNodeExtensibleRecordOperation(XQueryItemType x, XQueryItemType y);
    // ELEMENT operations
    public abstract boolean elementErrorOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean elementAnyItemOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean elementAnyNodeOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean elementElementOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean elementEnumOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean elementBooleanOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean elementNumberOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean elementStringOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean elementAnyMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean elementMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean elementChoiceOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean elementAnyArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean elementArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean elementAnyFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean elementFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean elementRecordOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean elementExtensibleRecordOperation(XQueryItemType x, XQueryItemType y);



    // ANY_MAP operations
    public abstract boolean anyMapErrorOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyMapAnyItemOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyMapAnyNodeOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyMapElementOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyMapEnumOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyMapBooleanOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyMapNumberOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyMapStringOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyMapAnyMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyMapMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyMapChoiceOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyMapAnyArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyMapArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyMapAnyFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyMapFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyMapRecordOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyMapExtensibleRecordOperation(XQueryItemType x, XQueryItemType y);

    // MAP operations
    public abstract boolean mapErrorOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean mapAnyItemOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean mapAnyNodeOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean mapElementOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean mapEnumOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean mapBooleanOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean mapNumberOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean mapStringOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean mapAnyMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean mapMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean mapChoiceOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean mapAnyArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean mapArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean mapAnyFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean mapFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean mapRecordOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean mapExtensibleRecordOperation(XQueryItemType x, XQueryItemType y);

    // ANY_ARRAY operations
    public abstract boolean anyArrayErrorOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyArrayAnyItemOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyArrayAnyNodeOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyArrayElementOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyArrayEnumOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyArrayBooleanOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyArrayNumberOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyArrayStringOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyArrayAnyMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyArrayMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyArrayChoiceOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyArrayAnyArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyArrayArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyArrayAnyFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyArrayFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyArrayRecordOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyArrayExtensibleRecordOperation(XQueryItemType x, XQueryItemType y);


// ARRAY operations
    public abstract boolean arrayErrorOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean arrayAnyItemOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean arrayAnyNodeOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean arrayElementOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean arrayEnumOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean arrayBooleanOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean arrayNumberOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean arrayStringOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean arrayAnyMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean arrayMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean arrayChoiceOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean arrayAnyArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean arrayArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean arrayAnyFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean arrayFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean arrayRecordOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean arrayExtensibleRecordOperation(XQueryItemType x, XQueryItemType y);

    // ANY_FUNCTION operations
    public abstract boolean anyFunctionErrorOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyFunctionAnyItemOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyFunctionAnyNodeOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyFunctionElementOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyFunctionEnumOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyFunctionBooleanOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyFunctionNumberOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyFunctionStringOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyFunctionAnyMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyFunctionMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyFunctionChoiceOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyFunctionAnyArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyFunctionArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyFunctionAnyFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyFunctionFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyFunctionRecordOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean anyFunctionExtensibleRecordOperation(XQueryItemType x, XQueryItemType y);

    // FUNCTION operations
    public abstract boolean functionErrorOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean functionAnyItemOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean functionAnyNodeOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean functionElementOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean functionEnumOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean functionBooleanOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean functionNumberOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean functionStringOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean functionAnyMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean functionMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean functionChoiceOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean functionAnyArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean functionArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean functionAnyFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean functionFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean functionRecordOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean functionExtensibleRecordOperation(XQueryItemType x, XQueryItemType y);

    // ENUM operations
    public abstract boolean enumErrorOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean enumAnyItemOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean enumAnyNodeOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean enumElementOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean enumEnumOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean enumBooleanOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean enumNumberOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean enumStringOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean enumAnyMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean enumMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean enumChoiceOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean enumAnyArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean enumArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean enumAnyFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean enumFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean enumRecordOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean enumExtensibleRecordOperation(XQueryItemType x, XQueryItemType y);


    // BOOLEAN operations
    public abstract boolean booleanErrorOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean booleanAnyItemOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean booleanAnyNodeOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean booleanElementOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean booleanEnumOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean booleanBooleanOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean booleanNumberOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean booleanStringOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean booleanAnyMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean booleanMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean booleanChoiceOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean booleanAnyArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean booleanArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean booleanAnyFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean booleanFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean booleanRecordOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean booleanExtensibleRecordOperation(XQueryItemType x, XQueryItemType y);

    // STRING operations
    public abstract boolean stringErrorOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean stringAnyItemOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean stringAnyNodeOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean stringElementOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean stringEnumOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean stringBooleanOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean stringNumberOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean stringStringOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean stringAnyMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean stringMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean stringChoiceOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean stringAnyArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean stringArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean stringAnyFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean stringFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean stringRecordOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean stringExtensibleRecordOperation(XQueryItemType x, XQueryItemType y);

    // NUMBER operations
    public abstract boolean numberErrorOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean numberAnyItemOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean numberAnyNodeOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean numberElementOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean numberEnumOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean numberBooleanOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean numberNumberOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean numberStringOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean numberAnyMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean numberMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean numberChoiceOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean numberAnyArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean numberArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean numberAnyFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean numberFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean numberRecordOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean numberExtensibleRecordOperation(XQueryItemType x, XQueryItemType y);

    // CHOICE operations
    public abstract boolean choiceErrorOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean choiceAnyItemOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean choiceAnyNodeOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean choiceElementOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean choiceEnumOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean choiceBooleanOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean choiceNumberOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean choiceStringOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean choiceAnyMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean choiceMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean choiceChoiceOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean choiceAnyArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean choiceArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean choiceAnyFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean choiceFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean choiceRecordOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean choiceExtensibleRecordOperation(XQueryItemType x, XQueryItemType y);

    // RECORD operations
    public abstract boolean recordErrorOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean recordAnyItemOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean recordAnyNodeOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean recordElementOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean recordEnumOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean recordBooleanOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean recordNumberOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean recordStringOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean recordAnyMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean recordMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean recordChoiceOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean recordAnyArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean recordArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean recordAnyFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean recordFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean recordRecordOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean recordExtensibleRecordOperation(XQueryItemType x, XQueryItemType y);

    // EXTENSIBLE_RECORD operations
    public abstract boolean extensibleRecordErrorOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean extensibleRecordAnyItemOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean extensibleRecordAnyNodeOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean extensibleRecordElementOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean extensibleRecordEnumOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean extensibleRecordBooleanOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean extensibleRecordNumberOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean extensibleRecordStringOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean extensibleRecordAnyMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean extensibleRecordMapOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean extensibleRecordChoiceOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean extensibleRecordAnyArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean extensibleRecordArrayOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean extensibleRecordAnyFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean extensibleRecordFunctionOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean extensibleRecordRecordOperation(XQueryItemType x, XQueryItemType y);
    public abstract boolean extensibleRecordExtensibleRecordOperation(XQueryItemType x, XQueryItemType y);


}
