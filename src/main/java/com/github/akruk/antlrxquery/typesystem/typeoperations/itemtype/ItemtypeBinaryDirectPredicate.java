package com.github.akruk.antlrxquery.typesystem.typeoperations.itemtype;

import com.github.akruk.antlrxquery.typesystem.defaults.XQueryTypes;

public abstract class ItemtypeBinaryDirectPredicate
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

    protected final boolean[][] automaton;

    public ItemtypeBinaryDirectPredicate()
    {
        this.automaton = getAutomaton();
    }


    private boolean[][] getAutomaton() {
        final boolean[][] automaton = new boolean[typesCount][typesCount];
        automaton[ERROR][ERROR] = errorErrorValue();
        automaton[ERROR][ANY_ITEM] = errorAnyItemValue();
        automaton[ERROR][ANY_NODE] = errorAnyNodeValue();
        automaton[ERROR][ELEMENT] = errorElementValue();
        automaton[ERROR][ENUM] = errorEnumValue();
        automaton[ERROR][BOOLEAN] = errorBooleanValue();
        automaton[ERROR][NUMBER] = errorNumberValue();
        automaton[ERROR][STRING] = errorStringValue();
        automaton[ERROR][ANY_MAP] = errorAnyMapValue();
        automaton[ERROR][MAP] = errorMapValue();
        automaton[ERROR][CHOICE] = errorChoiceValue();
        automaton[ERROR][ANY_ARRAY] = errorAnyArrayValue();
        automaton[ERROR][ARRAY] = errorArrayValue();
        automaton[ERROR][ANY_FUNCTION] = errorAnyFunctionValue();
        automaton[ERROR][FUNCTION] = errorFunctionValue();
        automaton[ERROR][RECORD] = errorRecordValue();
        automaton[ERROR][EXTENSIBLE_RECORD] = errorExtensibleRecordValue();

        automaton[ANY_ITEM][ERROR] = anyItemErrorValue();
        automaton[ANY_ITEM][ANY_ITEM] = anyItemAnyItemValue();
        automaton[ANY_ITEM][ANY_NODE] = anyItemAnyNodeValue();
        automaton[ANY_ITEM][ELEMENT] = anyItemElementValue();
        automaton[ANY_ITEM][ENUM] = anyItemEnumValue();
        automaton[ANY_ITEM][BOOLEAN] = anyItemBooleanValue();
        automaton[ANY_ITEM][NUMBER] = anyItemNumberValue();
        automaton[ANY_ITEM][STRING] = anyItemStringValue();
        automaton[ANY_ITEM][ANY_MAP] = anyItemAnyMapValue();
        automaton[ANY_ITEM][MAP] = anyItemMapValue();
        automaton[ANY_ITEM][CHOICE] = anyItemChoiceValue();
        automaton[ANY_ITEM][ANY_ARRAY] = anyItemAnyArrayValue();
        automaton[ANY_ITEM][ARRAY] = anyItemArrayValue();
        automaton[ANY_ITEM][ANY_FUNCTION] = anyItemAnyFunctionValue();
        automaton[ANY_ITEM][FUNCTION] = anyItemFunctionValue();
        automaton[ANY_ITEM][RECORD] = anyItemRecordValue();
        automaton[ANY_ITEM][EXTENSIBLE_RECORD] = anyItemExtensibleRecordValue();

        automaton[ANY_NODE][ERROR] = anyNodeErrorValue();
        automaton[ANY_NODE][ANY_ITEM] = anyNodeAnyItemValue();
        automaton[ANY_NODE][ANY_NODE] = anyNodeAnyNodeValue();
        automaton[ANY_NODE][ELEMENT] = anyNodeElementValue();
        automaton[ANY_NODE][ENUM] = anyNodeEnumValue();
        automaton[ANY_NODE][BOOLEAN] = anyNodeBooleanValue();
        automaton[ANY_NODE][NUMBER] = anyNodeNumberValue();
        automaton[ANY_NODE][STRING] = anyNodeStringValue();
        automaton[ANY_NODE][ANY_MAP] = anyNodeAnyMapValue();
        automaton[ANY_NODE][MAP] = anyNodeMapValue();
        automaton[ANY_NODE][CHOICE] = anyNodeChoiceValue();
        automaton[ANY_NODE][ANY_ARRAY] = anyNodeAnyArrayValue();
        automaton[ANY_NODE][ARRAY] = anyNodeArrayValue();
        automaton[ANY_NODE][ANY_FUNCTION] = anyNodeAnyFunctionValue();
        automaton[ANY_NODE][FUNCTION] = anyNodeFunctionValue();
        automaton[ANY_NODE][RECORD] = anyNodeRecordValue();
        automaton[ANY_NODE][EXTENSIBLE_RECORD] = anyNodeExtensibleRecordValue();

        automaton[ELEMENT][ERROR] = elementErrorValue();
        automaton[ELEMENT][ANY_ITEM] = elementAnyItemValue();
        automaton[ELEMENT][ANY_NODE] = elementAnyNodeValue();
        automaton[ELEMENT][ELEMENT] = elementElementValue();
        automaton[ELEMENT][ENUM] = elementEnumValue();
        automaton[ELEMENT][BOOLEAN] = elementBooleanValue();
        automaton[ELEMENT][NUMBER] = elementNumberValue();
        automaton[ELEMENT][STRING] = elementStringValue();
        automaton[ELEMENT][ANY_MAP] = elementAnyMapValue();
        automaton[ELEMENT][MAP] = elementMapValue();
        automaton[ELEMENT][CHOICE] = elementChoiceValue();
        automaton[ELEMENT][ANY_ARRAY] = elementAnyArrayValue();
        automaton[ELEMENT][ARRAY] = elementArrayValue();
        automaton[ELEMENT][ANY_FUNCTION] = elementAnyFunctionValue();
        automaton[ELEMENT][FUNCTION] = elementFunctionValue();
        automaton[ELEMENT][RECORD] = elementRecordValue();
        automaton[ELEMENT][EXTENSIBLE_RECORD] = elementExtensibleRecordValue();

        automaton[ANY_MAP][ERROR] = anyMapErrorValue();
        automaton[ANY_MAP][ANY_ITEM] = anyMapAnyItemValue();
        automaton[ANY_MAP][ANY_NODE] = anyMapAnyNodeValue();
        automaton[ANY_MAP][ELEMENT] = anyMapElementValue();
        automaton[ANY_MAP][ENUM] = anyMapEnumValue();
        automaton[ANY_MAP][BOOLEAN] = anyMapBooleanValue();
        automaton[ANY_MAP][NUMBER] = anyMapNumberValue();
        automaton[ANY_MAP][STRING] = anyMapStringValue();
        automaton[ANY_MAP][ANY_MAP] = anyMapAnyMapValue();
        automaton[ANY_MAP][MAP] = anyMapMapValue();
        automaton[ANY_MAP][CHOICE] = anyMapChoiceValue();
        automaton[ANY_MAP][ANY_ARRAY] = anyMapAnyArrayValue();
        automaton[ANY_MAP][ARRAY] = anyMapArrayValue();
        automaton[ANY_MAP][ANY_FUNCTION] = anyMapAnyFunctionValue();
        automaton[ANY_MAP][FUNCTION] = anyMapFunctionValue();
        automaton[ANY_MAP][RECORD] = anyMapRecordValue();
        automaton[ANY_MAP][EXTENSIBLE_RECORD] = anyMapExtensibleRecordValue();

        automaton[MAP][ERROR] = mapErrorValue();
        automaton[MAP][ANY_ITEM] = mapAnyItemValue();
        automaton[MAP][ANY_NODE] = mapAnyNodeValue();
        automaton[MAP][ELEMENT] = mapElementValue();
        automaton[MAP][ENUM] = mapEnumValue();
        automaton[MAP][BOOLEAN] = mapBooleanValue();
        automaton[MAP][NUMBER] = mapNumberValue();
        automaton[MAP][STRING] = mapStringValue();
        automaton[MAP][ANY_MAP] = mapAnyMapValue();
        automaton[MAP][MAP] = mapMapValue();
        automaton[MAP][CHOICE] = mapChoiceValue();
        automaton[MAP][ANY_ARRAY] = mapAnyArrayValue();
        automaton[MAP][ARRAY] = mapArrayValue();
        automaton[MAP][ANY_FUNCTION] = mapAnyFunctionValue();
        automaton[MAP][FUNCTION] = mapFunctionValue();
        automaton[MAP][RECORD] = mapRecordValue();
        automaton[MAP][EXTENSIBLE_RECORD] = mapExtensibleRecordValue();

        automaton[ANY_ARRAY][ERROR] = anyArrayErrorValue();
        automaton[ANY_ARRAY][ANY_ITEM] = anyArrayAnyItemValue();
        automaton[ANY_ARRAY][ANY_NODE] = anyArrayAnyNodeValue();
        automaton[ANY_ARRAY][ELEMENT] = anyArrayElementValue();
        automaton[ANY_ARRAY][ENUM] = anyArrayEnumValue();
        automaton[ANY_ARRAY][BOOLEAN] = anyArrayBooleanValue();
        automaton[ANY_ARRAY][NUMBER] = anyArrayNumberValue();
        automaton[ANY_ARRAY][STRING] = anyArrayStringValue();
        automaton[ANY_ARRAY][ANY_MAP] = anyArrayAnyMapValue();
        automaton[ANY_ARRAY][MAP] = anyArrayMapValue();
        automaton[ANY_ARRAY][CHOICE] = anyArrayChoiceValue();
        automaton[ANY_ARRAY][ANY_ARRAY] = anyArrayAnyArrayValue();
        automaton[ANY_ARRAY][ARRAY] = anyArrayArrayValue();
        automaton[ANY_ARRAY][ANY_FUNCTION] = anyArrayAnyFunctionValue();
        automaton[ANY_ARRAY][FUNCTION] = anyArrayFunctionValue();
        automaton[ANY_ARRAY][RECORD] = anyArrayRecordValue();
        automaton[ANY_ARRAY][EXTENSIBLE_RECORD] = anyArrayExtensibleRecordValue();

        automaton[ARRAY][ERROR] = arrayErrorValue();
        automaton[ARRAY][ANY_ITEM] = arrayAnyItemValue();
        automaton[ARRAY][ANY_NODE] = arrayAnyNodeValue();
        automaton[ARRAY][ELEMENT] = arrayElementValue();
        automaton[ARRAY][ENUM] = arrayEnumValue();
        automaton[ARRAY][BOOLEAN] = arrayBooleanValue();
        automaton[ARRAY][NUMBER] = arrayNumberValue();
        automaton[ARRAY][STRING] = arrayStringValue();
        automaton[ARRAY][ANY_MAP] = arrayAnyMapValue();
        automaton[ARRAY][MAP] = arrayMapValue();
        automaton[ARRAY][CHOICE] = arrayChoiceValue();
        automaton[ARRAY][ANY_ARRAY] = arrayAnyArrayValue();
        automaton[ARRAY][ARRAY] = arrayArrayValue();
        automaton[ARRAY][ANY_FUNCTION] = arrayAnyFunctionValue();
        automaton[ARRAY][FUNCTION] = arrayFunctionValue();
        automaton[ARRAY][RECORD] = arrayRecordValue();
        automaton[ARRAY][EXTENSIBLE_RECORD] = arrayExtensibleRecordValue();

        automaton[ANY_FUNCTION][ERROR] = anyFunctionErrorValue();
        automaton[ANY_FUNCTION][ANY_ITEM] = anyFunctionAnyItemValue();
        automaton[ANY_FUNCTION][ANY_NODE] = anyFunctionAnyNodeValue();
        automaton[ANY_FUNCTION][ELEMENT] = anyFunctionElementValue();
        automaton[ANY_FUNCTION][ENUM] = anyFunctionEnumValue();
        automaton[ANY_FUNCTION][BOOLEAN] = anyFunctionBooleanValue();
        automaton[ANY_FUNCTION][NUMBER] = anyFunctionNumberValue();
        automaton[ANY_FUNCTION][STRING] = anyFunctionStringValue();
        automaton[ANY_FUNCTION][ANY_MAP] = anyFunctionAnyMapValue();
        automaton[ANY_FUNCTION][MAP] = anyFunctionMapValue();
        automaton[ANY_FUNCTION][CHOICE] = anyFunctionChoiceValue();
        automaton[ANY_FUNCTION][ANY_ARRAY] = anyFunctionAnyArrayValue();
        automaton[ANY_FUNCTION][ARRAY] = anyFunctionArrayValue();
        automaton[ANY_FUNCTION][ANY_FUNCTION] = anyFunctionAnyFunctionValue();
        automaton[ANY_FUNCTION][FUNCTION] = anyFunctionFunctionValue();
        automaton[ANY_FUNCTION][RECORD] = anyFunctionRecordValue();
        automaton[ANY_FUNCTION][EXTENSIBLE_RECORD] = anyFunctionExtensibleRecordValue();

        automaton[FUNCTION][ERROR] = functionErrorValue();
        automaton[FUNCTION][ANY_ITEM] = functionAnyItemValue();
        automaton[FUNCTION][ANY_NODE] = functionAnyNodeValue();
        automaton[FUNCTION][ELEMENT] = functionElementValue();
        automaton[FUNCTION][ENUM] = functionEnumValue();
        automaton[FUNCTION][BOOLEAN] = functionBooleanValue();
        automaton[FUNCTION][NUMBER] = functionNumberValue();
        automaton[FUNCTION][STRING] = functionStringValue();
        automaton[FUNCTION][ANY_MAP] = functionAnyMapValue();
        automaton[FUNCTION][MAP] = functionMapValue();
        automaton[FUNCTION][CHOICE] = functionChoiceValue();
        automaton[FUNCTION][ANY_ARRAY] = functionAnyArrayValue();
        automaton[FUNCTION][ARRAY] = functionArrayValue();
        automaton[FUNCTION][ANY_FUNCTION] = functionAnyFunctionValue();
        automaton[FUNCTION][FUNCTION] = functionFunctionValue();
        automaton[FUNCTION][RECORD] = functionRecordValue();
        automaton[FUNCTION][EXTENSIBLE_RECORD] = functionExtensibleRecordValue();

        automaton[ENUM][ERROR] = enumErrorValue();
        automaton[ENUM][ANY_ITEM] = enumAnyItemValue();
        automaton[ENUM][ANY_NODE] = enumAnyNodeValue();
        automaton[ENUM][ELEMENT] = enumElementValue();
        automaton[ENUM][ENUM] = enumEnumValue();
        automaton[ENUM][BOOLEAN] = enumBooleanValue();
        automaton[ENUM][NUMBER] = enumNumberValue();
        automaton[ENUM][STRING] = enumStringValue();
        automaton[ENUM][ANY_MAP] = enumAnyMapValue();
        automaton[ENUM][MAP] = enumMapValue();
        automaton[ENUM][CHOICE] = enumChoiceValue();
        automaton[ENUM][ANY_ARRAY] = enumAnyArrayValue();
        automaton[ENUM][ARRAY] = enumArrayValue();
        automaton[ENUM][ANY_FUNCTION] = enumAnyFunctionValue();
        automaton[ENUM][FUNCTION] = enumFunctionValue();
        automaton[ENUM][RECORD] = enumRecordValue();
        automaton[ENUM][EXTENSIBLE_RECORD] = enumExtensibleRecordValue();

        automaton[RECORD][ERROR] = recordErrorValue();
        automaton[RECORD][ANY_ITEM] = recordAnyItemValue();
        automaton[RECORD][ANY_NODE] = recordAnyNodeValue();
        automaton[RECORD][ELEMENT] = recordElementValue();
        automaton[RECORD][ENUM] = recordEnumValue();
        automaton[RECORD][BOOLEAN] = recordBooleanValue();
        automaton[RECORD][NUMBER] = recordNumberValue();
        automaton[RECORD][STRING] = recordStringValue();
        automaton[RECORD][ANY_MAP] = recordAnyMapValue();
        automaton[RECORD][MAP] = recordMapValue();
        automaton[RECORD][CHOICE] = recordChoiceValue();
        automaton[RECORD][ANY_ARRAY] = recordAnyArrayValue();
        automaton[RECORD][ARRAY] = recordArrayValue();
        automaton[RECORD][ANY_FUNCTION] = recordAnyFunctionValue();
        automaton[RECORD][FUNCTION] = recordFunctionValue();
        automaton[RECORD][RECORD] = recordRecordValue();
        automaton[RECORD][EXTENSIBLE_RECORD] = recordExtensibleRecordValue();

        automaton[EXTENSIBLE_RECORD][ERROR] = extensibleRecordErrorValue();
        automaton[EXTENSIBLE_RECORD][ANY_ITEM] = extensibleRecordAnyItemValue();
        automaton[EXTENSIBLE_RECORD][ANY_NODE] = extensibleRecordAnyNodeValue();
        automaton[EXTENSIBLE_RECORD][ELEMENT] = extensibleRecordElementValue();
        automaton[EXTENSIBLE_RECORD][ENUM] = extensibleRecordEnumValue();
        automaton[EXTENSIBLE_RECORD][BOOLEAN] = extensibleRecordBooleanValue();
        automaton[EXTENSIBLE_RECORD][NUMBER] = extensibleRecordNumberValue();
        automaton[EXTENSIBLE_RECORD][STRING] = extensibleRecordStringValue();
        automaton[EXTENSIBLE_RECORD][ANY_MAP] = extensibleRecordAnyMapValue();
        automaton[EXTENSIBLE_RECORD][MAP] = extensibleRecordMapValue();
        automaton[EXTENSIBLE_RECORD][CHOICE] = extensibleRecordChoiceValue();
        automaton[EXTENSIBLE_RECORD][ANY_ARRAY] = extensibleRecordAnyArrayValue();
        automaton[EXTENSIBLE_RECORD][ARRAY] = extensibleRecordArrayValue();
        automaton[EXTENSIBLE_RECORD][ANY_FUNCTION] = extensibleRecordAnyFunctionValue();
        automaton[EXTENSIBLE_RECORD][FUNCTION] = extensibleRecordFunctionValue();
        automaton[EXTENSIBLE_RECORD][RECORD] = extensibleRecordRecordValue();
        automaton[EXTENSIBLE_RECORD][EXTENSIBLE_RECORD] = extensibleRecordExtensibleRecordValue();

        automaton[BOOLEAN][ERROR] = booleanErrorValue();
        automaton[BOOLEAN][ANY_ITEM] = booleanAnyItemValue();
        automaton[BOOLEAN][ANY_NODE] = booleanAnyNodeValue();
        automaton[BOOLEAN][ELEMENT] = booleanElementValue();
        automaton[BOOLEAN][ENUM] = booleanEnumValue();
        automaton[BOOLEAN][BOOLEAN] = booleanBooleanValue();
        automaton[BOOLEAN][NUMBER] = booleanNumberValue();
        automaton[BOOLEAN][STRING] = booleanStringValue();
        automaton[BOOLEAN][ANY_MAP] = booleanAnyMapValue();
        automaton[BOOLEAN][MAP] = booleanMapValue();
        automaton[BOOLEAN][CHOICE] = booleanChoiceValue();
        automaton[BOOLEAN][ANY_ARRAY] = booleanAnyArrayValue();
        automaton[BOOLEAN][ARRAY] = booleanArrayValue();
        automaton[BOOLEAN][ANY_FUNCTION] = booleanAnyFunctionValue();
        automaton[BOOLEAN][FUNCTION] = booleanFunctionValue();
        automaton[BOOLEAN][RECORD] = booleanRecordValue();
        automaton[BOOLEAN][EXTENSIBLE_RECORD] = booleanExtensibleRecordValue();

        automaton[STRING][ERROR] = stringErrorValue();
        automaton[STRING][ANY_ITEM] = stringAnyItemValue();
        automaton[STRING][ANY_NODE] = stringAnyNodeValue();
        automaton[STRING][ELEMENT] = stringElementValue();
        automaton[STRING][ENUM] = stringEnumValue();
        automaton[STRING][BOOLEAN] = stringBooleanValue();
        automaton[STRING][NUMBER] = stringNumberValue();
        automaton[STRING][STRING] = stringStringValue();
        automaton[STRING][ANY_MAP] = stringAnyMapValue();
        automaton[STRING][MAP] = stringMapValue();
        automaton[STRING][CHOICE] = stringChoiceValue();
        automaton[STRING][ANY_ARRAY] = stringAnyArrayValue();
        automaton[STRING][ARRAY] = stringArrayValue();
        automaton[STRING][ANY_FUNCTION] = stringAnyFunctionValue();
        automaton[STRING][FUNCTION] = stringFunctionValue();
        automaton[STRING][RECORD] = stringRecordValue();
        automaton[STRING][EXTENSIBLE_RECORD] = stringExtensibleRecordValue();

        automaton[NUMBER][ERROR] = numberErrorValue();
        automaton[NUMBER][ANY_ITEM] = numberAnyItemValue();
        automaton[NUMBER][ANY_NODE] = numberAnyNodeValue();
        automaton[NUMBER][ELEMENT] = numberElementValue();
        automaton[NUMBER][ENUM] = numberEnumValue();
        automaton[NUMBER][BOOLEAN] = numberBooleanValue();
        automaton[NUMBER][NUMBER] = numberNumberValue();
        automaton[NUMBER][STRING] = numberStringValue();
        automaton[NUMBER][ANY_MAP] = numberAnyMapValue();
        automaton[NUMBER][MAP] = numberMapValue();
        automaton[NUMBER][CHOICE] = numberChoiceValue();
        automaton[NUMBER][ANY_ARRAY] = numberAnyArrayValue();
        automaton[NUMBER][ARRAY] = numberArrayValue();
        automaton[NUMBER][ANY_FUNCTION] = numberAnyFunctionValue();
        automaton[NUMBER][FUNCTION] = numberFunctionValue();
        automaton[NUMBER][RECORD] = numberRecordValue();
        automaton[NUMBER][EXTENSIBLE_RECORD] = numberExtensibleRecordValue();

        automaton[CHOICE][ERROR] = choiceErrorValue();
        automaton[CHOICE][ANY_ITEM] = choiceAnyItemValue();
        automaton[CHOICE][ANY_NODE] = choiceAnyNodeValue();
        automaton[CHOICE][ELEMENT] = choiceElementValue();
        automaton[CHOICE][ENUM] = choiceEnumValue();
        automaton[CHOICE][BOOLEAN] = choiceBooleanValue();
        automaton[CHOICE][NUMBER] = choiceNumberValue();
        automaton[CHOICE][STRING] = choiceStringValue();
        automaton[CHOICE][ANY_MAP] = choiceAnyMapValue();
        automaton[CHOICE][MAP] = choiceMapValue();
        automaton[CHOICE][CHOICE] = choiceChoiceValue();
        automaton[CHOICE][ANY_ARRAY] = choiceAnyArrayValue();
        automaton[CHOICE][ARRAY] = choiceArrayValue();
        automaton[CHOICE][ANY_FUNCTION] = choiceAnyFunctionValue();
        automaton[CHOICE][FUNCTION] = choiceFunctionValue();
        automaton[CHOICE][RECORD] = choiceRecordValue();
        automaton[CHOICE][EXTENSIBLE_RECORD] = choiceExtensibleRecordValue();
        return automaton;
    }


    public abstract boolean errorErrorValue();
    public abstract boolean errorAnyItemValue();
    public abstract boolean errorAnyNodeValue();
    public abstract boolean errorElementValue();
    public abstract boolean errorEnumValue();
    public abstract boolean errorBooleanValue();
    public abstract boolean errorNumberValue();
    public abstract boolean errorStringValue();
    public abstract boolean errorAnyMapValue();
    public abstract boolean errorMapValue();
    public abstract boolean errorChoiceValue();
    public abstract boolean errorAnyArrayValue();
    public abstract boolean errorArrayValue();
    public abstract boolean errorAnyFunctionValue();
    public abstract boolean errorFunctionValue();
    public abstract boolean errorRecordValue();
    public abstract boolean errorExtensibleRecordValue();

    // ANY_ITEM Values
    public abstract boolean anyItemErrorValue();
    public abstract boolean anyItemAnyItemValue();
    public abstract boolean anyItemAnyNodeValue();
    public abstract boolean anyItemElementValue();
    public abstract boolean anyItemEnumValue();
    public abstract boolean anyItemBooleanValue();
    public abstract boolean anyItemNumberValue();
    public abstract boolean anyItemStringValue();
    public abstract boolean anyItemAnyMapValue();
    public abstract boolean anyItemMapValue();
    public abstract boolean anyItemChoiceValue();
    public abstract boolean anyItemAnyArrayValue();
    public abstract boolean anyItemArrayValue();
    public abstract boolean anyItemAnyFunctionValue();
    public abstract boolean anyItemFunctionValue();
    public abstract boolean anyItemRecordValue();
    public abstract boolean anyItemExtensibleRecordValue();

    // ANY_NODE Values
    public abstract boolean anyNodeErrorValue();
    public abstract boolean anyNodeAnyItemValue();
    public abstract boolean anyNodeAnyNodeValue();
    public abstract boolean anyNodeElementValue();
    public abstract boolean anyNodeEnumValue();
    public abstract boolean anyNodeBooleanValue();
    public abstract boolean anyNodeNumberValue();
    public abstract boolean anyNodeStringValue();
    public abstract boolean anyNodeAnyMapValue();
    public abstract boolean anyNodeMapValue();
    public abstract boolean anyNodeChoiceValue();
    public abstract boolean anyNodeAnyArrayValue();
    public abstract boolean anyNodeArrayValue();
    public abstract boolean anyNodeAnyFunctionValue();
    public abstract boolean anyNodeFunctionValue();
    public abstract boolean anyNodeRecordValue();
    public abstract boolean anyNodeExtensibleRecordValue();
    // ELEMENT Values
    public abstract boolean elementErrorValue();
    public abstract boolean elementAnyItemValue();
    public abstract boolean elementAnyNodeValue();
    public abstract boolean elementElementValue();
    public abstract boolean elementEnumValue();
    public abstract boolean elementBooleanValue();
    public abstract boolean elementNumberValue();
    public abstract boolean elementStringValue();
    public abstract boolean elementAnyMapValue();
    public abstract boolean elementMapValue();
    public abstract boolean elementChoiceValue();
    public abstract boolean elementAnyArrayValue();
    public abstract boolean elementArrayValue();
    public abstract boolean elementAnyFunctionValue();
    public abstract boolean elementFunctionValue();
    public abstract boolean elementRecordValue();
    public abstract boolean elementExtensibleRecordValue();



    // ANY_MAP Values
    public abstract boolean anyMapErrorValue();
    public abstract boolean anyMapAnyItemValue();
    public abstract boolean anyMapAnyNodeValue();
    public abstract boolean anyMapElementValue();
    public abstract boolean anyMapEnumValue();
    public abstract boolean anyMapBooleanValue();
    public abstract boolean anyMapNumberValue();
    public abstract boolean anyMapStringValue();
    public abstract boolean anyMapAnyMapValue();
    public abstract boolean anyMapMapValue();
    public abstract boolean anyMapChoiceValue();
    public abstract boolean anyMapAnyArrayValue();
    public abstract boolean anyMapArrayValue();
    public abstract boolean anyMapAnyFunctionValue();
    public abstract boolean anyMapFunctionValue();
    public abstract boolean anyMapRecordValue();
    public abstract boolean anyMapExtensibleRecordValue();

    // MAP Values
    public abstract boolean mapErrorValue();
    public abstract boolean mapAnyItemValue();
    public abstract boolean mapAnyNodeValue();
    public abstract boolean mapElementValue();
    public abstract boolean mapEnumValue();
    public abstract boolean mapBooleanValue();
    public abstract boolean mapNumberValue();
    public abstract boolean mapStringValue();
    public abstract boolean mapAnyMapValue();
    public abstract boolean mapMapValue();
    public abstract boolean mapChoiceValue();
    public abstract boolean mapAnyArrayValue();
    public abstract boolean mapArrayValue();
    public abstract boolean mapAnyFunctionValue();
    public abstract boolean mapFunctionValue();
    public abstract boolean mapRecordValue();
    public abstract boolean mapExtensibleRecordValue();

    // ANY_ARRAY Values
    public abstract boolean anyArrayErrorValue();
    public abstract boolean anyArrayAnyItemValue();
    public abstract boolean anyArrayAnyNodeValue();
    public abstract boolean anyArrayElementValue();
    public abstract boolean anyArrayEnumValue();
    public abstract boolean anyArrayBooleanValue();
    public abstract boolean anyArrayNumberValue();
    public abstract boolean anyArrayStringValue();
    public abstract boolean anyArrayAnyMapValue();
    public abstract boolean anyArrayMapValue();
    public abstract boolean anyArrayChoiceValue();
    public abstract boolean anyArrayAnyArrayValue();
    public abstract boolean anyArrayArrayValue();
    public abstract boolean anyArrayAnyFunctionValue();
    public abstract boolean anyArrayFunctionValue();
    public abstract boolean anyArrayRecordValue();
    public abstract boolean anyArrayExtensibleRecordValue();


// ARRAY Values
    public abstract boolean arrayErrorValue();
    public abstract boolean arrayAnyItemValue();
    public abstract boolean arrayAnyNodeValue();
    public abstract boolean arrayElementValue();
    public abstract boolean arrayEnumValue();
    public abstract boolean arrayBooleanValue();
    public abstract boolean arrayNumberValue();
    public abstract boolean arrayStringValue();
    public abstract boolean arrayAnyMapValue();
    public abstract boolean arrayMapValue();
    public abstract boolean arrayChoiceValue();
    public abstract boolean arrayAnyArrayValue();
    public abstract boolean arrayArrayValue();
    public abstract boolean arrayAnyFunctionValue();
    public abstract boolean arrayFunctionValue();
    public abstract boolean arrayRecordValue();
    public abstract boolean arrayExtensibleRecordValue();

    // ANY_FUNCTION Values
    public abstract boolean anyFunctionErrorValue();
    public abstract boolean anyFunctionAnyItemValue();
    public abstract boolean anyFunctionAnyNodeValue();
    public abstract boolean anyFunctionElementValue();
    public abstract boolean anyFunctionEnumValue();
    public abstract boolean anyFunctionBooleanValue();
    public abstract boolean anyFunctionNumberValue();
    public abstract boolean anyFunctionStringValue();
    public abstract boolean anyFunctionAnyMapValue();
    public abstract boolean anyFunctionMapValue();
    public abstract boolean anyFunctionChoiceValue();
    public abstract boolean anyFunctionAnyArrayValue();
    public abstract boolean anyFunctionArrayValue();
    public abstract boolean anyFunctionAnyFunctionValue();
    public abstract boolean anyFunctionFunctionValue();
    public abstract boolean anyFunctionRecordValue();
    public abstract boolean anyFunctionExtensibleRecordValue();

    // FUNCTION Values
    public abstract boolean functionErrorValue();
    public abstract boolean functionAnyItemValue();
    public abstract boolean functionAnyNodeValue();
    public abstract boolean functionElementValue();
    public abstract boolean functionEnumValue();
    public abstract boolean functionBooleanValue();
    public abstract boolean functionNumberValue();
    public abstract boolean functionStringValue();
    public abstract boolean functionAnyMapValue();
    public abstract boolean functionMapValue();
    public abstract boolean functionChoiceValue();
    public abstract boolean functionAnyArrayValue();
    public abstract boolean functionArrayValue();
    public abstract boolean functionAnyFunctionValue();
    public abstract boolean functionFunctionValue();
    public abstract boolean functionRecordValue();
    public abstract boolean functionExtensibleRecordValue();

    // ENUM Values
    public abstract boolean enumErrorValue();
    public abstract boolean enumAnyItemValue();
    public abstract boolean enumAnyNodeValue();
    public abstract boolean enumElementValue();
    public abstract boolean enumEnumValue();
    public abstract boolean enumBooleanValue();
    public abstract boolean enumNumberValue();
    public abstract boolean enumStringValue();
    public abstract boolean enumAnyMapValue();
    public abstract boolean enumMapValue();
    public abstract boolean enumChoiceValue();
    public abstract boolean enumAnyArrayValue();
    public abstract boolean enumArrayValue();
    public abstract boolean enumAnyFunctionValue();
    public abstract boolean enumFunctionValue();
    public abstract boolean enumRecordValue();
    public abstract boolean enumExtensibleRecordValue();


    // BOOLEAN Values
    public abstract boolean booleanErrorValue();
    public abstract boolean booleanAnyItemValue();
    public abstract boolean booleanAnyNodeValue();
    public abstract boolean booleanElementValue();
    public abstract boolean booleanEnumValue();
    public abstract boolean booleanBooleanValue();
    public abstract boolean booleanNumberValue();
    public abstract boolean booleanStringValue();
    public abstract boolean booleanAnyMapValue();
    public abstract boolean booleanMapValue();
    public abstract boolean booleanChoiceValue();
    public abstract boolean booleanAnyArrayValue();
    public abstract boolean booleanArrayValue();
    public abstract boolean booleanAnyFunctionValue();
    public abstract boolean booleanFunctionValue();
    public abstract boolean booleanRecordValue();
    public abstract boolean booleanExtensibleRecordValue();

    // STRING Values
    public abstract boolean stringErrorValue();
    public abstract boolean stringAnyItemValue();
    public abstract boolean stringAnyNodeValue();
    public abstract boolean stringElementValue();
    public abstract boolean stringEnumValue();
    public abstract boolean stringBooleanValue();
    public abstract boolean stringNumberValue();
    public abstract boolean stringStringValue();
    public abstract boolean stringAnyMapValue();
    public abstract boolean stringMapValue();
    public abstract boolean stringChoiceValue();
    public abstract boolean stringAnyArrayValue();
    public abstract boolean stringArrayValue();
    public abstract boolean stringAnyFunctionValue();
    public abstract boolean stringFunctionValue();
    public abstract boolean stringRecordValue();
    public abstract boolean stringExtensibleRecordValue();

    // NUMBER Values
    public abstract boolean numberErrorValue();
    public abstract boolean numberAnyItemValue();
    public abstract boolean numberAnyNodeValue();
    public abstract boolean numberElementValue();
    public abstract boolean numberEnumValue();
    public abstract boolean numberBooleanValue();
    public abstract boolean numberNumberValue();
    public abstract boolean numberStringValue();
    public abstract boolean numberAnyMapValue();
    public abstract boolean numberMapValue();
    public abstract boolean numberChoiceValue();
    public abstract boolean numberAnyArrayValue();
    public abstract boolean numberArrayValue();
    public abstract boolean numberAnyFunctionValue();
    public abstract boolean numberFunctionValue();
    public abstract boolean numberRecordValue();
    public abstract boolean numberExtensibleRecordValue();

    // CHOICE Values
    public abstract boolean choiceErrorValue();
    public abstract boolean choiceAnyItemValue();
    public abstract boolean choiceAnyNodeValue();
    public abstract boolean choiceElementValue();
    public abstract boolean choiceEnumValue();
    public abstract boolean choiceBooleanValue();
    public abstract boolean choiceNumberValue();
    public abstract boolean choiceStringValue();
    public abstract boolean choiceAnyMapValue();
    public abstract boolean choiceMapValue();
    public abstract boolean choiceChoiceValue();
    public abstract boolean choiceAnyArrayValue();
    public abstract boolean choiceArrayValue();
    public abstract boolean choiceAnyFunctionValue();
    public abstract boolean choiceFunctionValue();
    public abstract boolean choiceRecordValue();
    public abstract boolean choiceExtensibleRecordValue();

    // RECORD Values
    public abstract boolean recordErrorValue();
    public abstract boolean recordAnyItemValue();
    public abstract boolean recordAnyNodeValue();
    public abstract boolean recordElementValue();
    public abstract boolean recordEnumValue();
    public abstract boolean recordBooleanValue();
    public abstract boolean recordNumberValue();
    public abstract boolean recordStringValue();
    public abstract boolean recordAnyMapValue();
    public abstract boolean recordMapValue();
    public abstract boolean recordChoiceValue();
    public abstract boolean recordAnyArrayValue();
    public abstract boolean recordArrayValue();
    public abstract boolean recordAnyFunctionValue();
    public abstract boolean recordFunctionValue();
    public abstract boolean recordRecordValue();
    public abstract boolean recordExtensibleRecordValue();

    // EXTENSIBLE_RECORD Values
    public abstract boolean extensibleRecordErrorValue();
    public abstract boolean extensibleRecordAnyItemValue();
    public abstract boolean extensibleRecordAnyNodeValue();
    public abstract boolean extensibleRecordElementValue();
    public abstract boolean extensibleRecordEnumValue();
    public abstract boolean extensibleRecordBooleanValue();
    public abstract boolean extensibleRecordNumberValue();
    public abstract boolean extensibleRecordStringValue();
    public abstract boolean extensibleRecordAnyMapValue();
    public abstract boolean extensibleRecordMapValue();
    public abstract boolean extensibleRecordChoiceValue();
    public abstract boolean extensibleRecordAnyArrayValue();
    public abstract boolean extensibleRecordArrayValue();
    public abstract boolean extensibleRecordAnyFunctionValue();
    public abstract boolean extensibleRecordFunctionValue();
    public abstract boolean extensibleRecordRecordValue();
    public abstract boolean extensibleRecordExtensibleRecordValue();


}
