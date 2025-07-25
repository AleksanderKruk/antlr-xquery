package com.github.akruk.antlrxquery.typesystem.typeoperations.itemtype;

import com.github.akruk.antlrxquery.typesystem.defaults.XQueryTypes;

public abstract class  ItemtypeBinaryDirectOperation<Returned>
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
    // private static final int typesCount = XQueryTypes.values().length;

    protected final Returned[][] automaton;

    public ItemtypeBinaryDirectOperation()
    {
        this.automaton = getAutomaton();
    }

    public abstract Returned[][] allocateArray();

    private Returned[][] getAutomaton() {
        final Returned[][] automaton = allocateArray();
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


    public abstract Returned errorErrorValue();
    public abstract Returned errorAnyItemValue();
    public abstract Returned errorAnyNodeValue();
    public abstract Returned errorElementValue();
    public abstract Returned errorEnumValue();
    public abstract Returned errorBooleanValue();
    public abstract Returned errorNumberValue();
    public abstract Returned errorStringValue();
    public abstract Returned errorAnyMapValue();
    public abstract Returned errorMapValue();
    public abstract Returned errorChoiceValue();
    public abstract Returned errorAnyArrayValue();
    public abstract Returned errorArrayValue();
    public abstract Returned errorAnyFunctionValue();
    public abstract Returned errorFunctionValue();
    public abstract Returned errorRecordValue();
    public abstract Returned errorExtensibleRecordValue();

    // ANY_ITEM Values
    public abstract Returned anyItemErrorValue();
    public abstract Returned anyItemAnyItemValue();
    public abstract Returned anyItemAnyNodeValue();
    public abstract Returned anyItemElementValue();
    public abstract Returned anyItemEnumValue();
    public abstract Returned anyItemBooleanValue();
    public abstract Returned anyItemNumberValue();
    public abstract Returned anyItemStringValue();
    public abstract Returned anyItemAnyMapValue();
    public abstract Returned anyItemMapValue();
    public abstract Returned anyItemChoiceValue();
    public abstract Returned anyItemAnyArrayValue();
    public abstract Returned anyItemArrayValue();
    public abstract Returned anyItemAnyFunctionValue();
    public abstract Returned anyItemFunctionValue();
    public abstract Returned anyItemRecordValue();
    public abstract Returned anyItemExtensibleRecordValue();

    // ANY_NODE Values
    public abstract Returned anyNodeErrorValue();
    public abstract Returned anyNodeAnyItemValue();
    public abstract Returned anyNodeAnyNodeValue();
    public abstract Returned anyNodeElementValue();
    public abstract Returned anyNodeEnumValue();
    public abstract Returned anyNodeBooleanValue();
    public abstract Returned anyNodeNumberValue();
    public abstract Returned anyNodeStringValue();
    public abstract Returned anyNodeAnyMapValue();
    public abstract Returned anyNodeMapValue();
    public abstract Returned anyNodeChoiceValue();
    public abstract Returned anyNodeAnyArrayValue();
    public abstract Returned anyNodeArrayValue();
    public abstract Returned anyNodeAnyFunctionValue();
    public abstract Returned anyNodeFunctionValue();
    public abstract Returned anyNodeRecordValue();
    public abstract Returned anyNodeExtensibleRecordValue();
    // ELEMENT Values
    public abstract Returned elementErrorValue();
    public abstract Returned elementAnyItemValue();
    public abstract Returned elementAnyNodeValue();
    public abstract Returned elementElementValue();
    public abstract Returned elementEnumValue();
    public abstract Returned elementBooleanValue();
    public abstract Returned elementNumberValue();
    public abstract Returned elementStringValue();
    public abstract Returned elementAnyMapValue();
    public abstract Returned elementMapValue();
    public abstract Returned elementChoiceValue();
    public abstract Returned elementAnyArrayValue();
    public abstract Returned elementArrayValue();
    public abstract Returned elementAnyFunctionValue();
    public abstract Returned elementFunctionValue();
    public abstract Returned elementRecordValue();
    public abstract Returned elementExtensibleRecordValue();



    // ANY_MAP Values
    public abstract Returned anyMapErrorValue();
    public abstract Returned anyMapAnyItemValue();
    public abstract Returned anyMapAnyNodeValue();
    public abstract Returned anyMapElementValue();
    public abstract Returned anyMapEnumValue();
    public abstract Returned anyMapBooleanValue();
    public abstract Returned anyMapNumberValue();
    public abstract Returned anyMapStringValue();
    public abstract Returned anyMapAnyMapValue();
    public abstract Returned anyMapMapValue();
    public abstract Returned anyMapChoiceValue();
    public abstract Returned anyMapAnyArrayValue();
    public abstract Returned anyMapArrayValue();
    public abstract Returned anyMapAnyFunctionValue();
    public abstract Returned anyMapFunctionValue();
    public abstract Returned anyMapRecordValue();
    public abstract Returned anyMapExtensibleRecordValue();

    // MAP Values
    public abstract Returned mapErrorValue();
    public abstract Returned mapAnyItemValue();
    public abstract Returned mapAnyNodeValue();
    public abstract Returned mapElementValue();
    public abstract Returned mapEnumValue();
    public abstract Returned mapBooleanValue();
    public abstract Returned mapNumberValue();
    public abstract Returned mapStringValue();
    public abstract Returned mapAnyMapValue();
    public abstract Returned mapMapValue();
    public abstract Returned mapChoiceValue();
    public abstract Returned mapAnyArrayValue();
    public abstract Returned mapArrayValue();
    public abstract Returned mapAnyFunctionValue();
    public abstract Returned mapFunctionValue();
    public abstract Returned mapRecordValue();
    public abstract Returned mapExtensibleRecordValue();

    // ANY_ARRAY Values
    public abstract Returned anyArrayErrorValue();
    public abstract Returned anyArrayAnyItemValue();
    public abstract Returned anyArrayAnyNodeValue();
    public abstract Returned anyArrayElementValue();
    public abstract Returned anyArrayEnumValue();
    public abstract Returned anyArrayBooleanValue();
    public abstract Returned anyArrayNumberValue();
    public abstract Returned anyArrayStringValue();
    public abstract Returned anyArrayAnyMapValue();
    public abstract Returned anyArrayMapValue();
    public abstract Returned anyArrayChoiceValue();
    public abstract Returned anyArrayAnyArrayValue();
    public abstract Returned anyArrayArrayValue();
    public abstract Returned anyArrayAnyFunctionValue();
    public abstract Returned anyArrayFunctionValue();
    public abstract Returned anyArrayRecordValue();
    public abstract Returned anyArrayExtensibleRecordValue();


// ARRAY Values
    public abstract Returned arrayErrorValue();
    public abstract Returned arrayAnyItemValue();
    public abstract Returned arrayAnyNodeValue();
    public abstract Returned arrayElementValue();
    public abstract Returned arrayEnumValue();
    public abstract Returned arrayBooleanValue();
    public abstract Returned arrayNumberValue();
    public abstract Returned arrayStringValue();
    public abstract Returned arrayAnyMapValue();
    public abstract Returned arrayMapValue();
    public abstract Returned arrayChoiceValue();
    public abstract Returned arrayAnyArrayValue();
    public abstract Returned arrayArrayValue();
    public abstract Returned arrayAnyFunctionValue();
    public abstract Returned arrayFunctionValue();
    public abstract Returned arrayRecordValue();
    public abstract Returned arrayExtensibleRecordValue();

    // ANY_FUNCTION Values
    public abstract Returned anyFunctionErrorValue();
    public abstract Returned anyFunctionAnyItemValue();
    public abstract Returned anyFunctionAnyNodeValue();
    public abstract Returned anyFunctionElementValue();
    public abstract Returned anyFunctionEnumValue();
    public abstract Returned anyFunctionBooleanValue();
    public abstract Returned anyFunctionNumberValue();
    public abstract Returned anyFunctionStringValue();
    public abstract Returned anyFunctionAnyMapValue();
    public abstract Returned anyFunctionMapValue();
    public abstract Returned anyFunctionChoiceValue();
    public abstract Returned anyFunctionAnyArrayValue();
    public abstract Returned anyFunctionArrayValue();
    public abstract Returned anyFunctionAnyFunctionValue();
    public abstract Returned anyFunctionFunctionValue();
    public abstract Returned anyFunctionRecordValue();
    public abstract Returned anyFunctionExtensibleRecordValue();

    // FUNCTION Values
    public abstract Returned functionErrorValue();
    public abstract Returned functionAnyItemValue();
    public abstract Returned functionAnyNodeValue();
    public abstract Returned functionElementValue();
    public abstract Returned functionEnumValue();
    public abstract Returned functionBooleanValue();
    public abstract Returned functionNumberValue();
    public abstract Returned functionStringValue();
    public abstract Returned functionAnyMapValue();
    public abstract Returned functionMapValue();
    public abstract Returned functionChoiceValue();
    public abstract Returned functionAnyArrayValue();
    public abstract Returned functionArrayValue();
    public abstract Returned functionAnyFunctionValue();
    public abstract Returned functionFunctionValue();
    public abstract Returned functionRecordValue();
    public abstract Returned functionExtensibleRecordValue();

    // ENUM Values
    public abstract Returned enumErrorValue();
    public abstract Returned enumAnyItemValue();
    public abstract Returned enumAnyNodeValue();
    public abstract Returned enumElementValue();
    public abstract Returned enumEnumValue();
    public abstract Returned enumBooleanValue();
    public abstract Returned enumNumberValue();
    public abstract Returned enumStringValue();
    public abstract Returned enumAnyMapValue();
    public abstract Returned enumMapValue();
    public abstract Returned enumChoiceValue();
    public abstract Returned enumAnyArrayValue();
    public abstract Returned enumArrayValue();
    public abstract Returned enumAnyFunctionValue();
    public abstract Returned enumFunctionValue();
    public abstract Returned enumRecordValue();
    public abstract Returned enumExtensibleRecordValue();


    // Returned Values
    public abstract Returned booleanErrorValue();
    public abstract Returned booleanAnyItemValue();
    public abstract Returned booleanAnyNodeValue();
    public abstract Returned booleanElementValue();
    public abstract Returned booleanEnumValue();
    public abstract Returned booleanBooleanValue();
    public abstract Returned booleanNumberValue();
    public abstract Returned booleanStringValue();
    public abstract Returned booleanAnyMapValue();
    public abstract Returned booleanMapValue();
    public abstract Returned booleanChoiceValue();
    public abstract Returned booleanAnyArrayValue();
    public abstract Returned booleanArrayValue();
    public abstract Returned booleanAnyFunctionValue();
    public abstract Returned booleanFunctionValue();
    public abstract Returned booleanRecordValue();
    public abstract Returned booleanExtensibleRecordValue();

    // STRING Values
    public abstract Returned stringErrorValue();
    public abstract Returned stringAnyItemValue();
    public abstract Returned stringAnyNodeValue();
    public abstract Returned stringElementValue();
    public abstract Returned stringEnumValue();
    public abstract Returned stringBooleanValue();
    public abstract Returned stringNumberValue();
    public abstract Returned stringStringValue();
    public abstract Returned stringAnyMapValue();
    public abstract Returned stringMapValue();
    public abstract Returned stringChoiceValue();
    public abstract Returned stringAnyArrayValue();
    public abstract Returned stringArrayValue();
    public abstract Returned stringAnyFunctionValue();
    public abstract Returned stringFunctionValue();
    public abstract Returned stringRecordValue();
    public abstract Returned stringExtensibleRecordValue();

    // NUMBER Values
    public abstract Returned numberErrorValue();
    public abstract Returned numberAnyItemValue();
    public abstract Returned numberAnyNodeValue();
    public abstract Returned numberElementValue();
    public abstract Returned numberEnumValue();
    public abstract Returned numberBooleanValue();
    public abstract Returned numberNumberValue();
    public abstract Returned numberStringValue();
    public abstract Returned numberAnyMapValue();
    public abstract Returned numberMapValue();
    public abstract Returned numberChoiceValue();
    public abstract Returned numberAnyArrayValue();
    public abstract Returned numberArrayValue();
    public abstract Returned numberAnyFunctionValue();
    public abstract Returned numberFunctionValue();
    public abstract Returned numberRecordValue();
    public abstract Returned numberExtensibleRecordValue();

    // CHOICE Values
    public abstract Returned choiceErrorValue();
    public abstract Returned choiceAnyItemValue();
    public abstract Returned choiceAnyNodeValue();
    public abstract Returned choiceElementValue();
    public abstract Returned choiceEnumValue();
    public abstract Returned choiceBooleanValue();
    public abstract Returned choiceNumberValue();
    public abstract Returned choiceStringValue();
    public abstract Returned choiceAnyMapValue();
    public abstract Returned choiceMapValue();
    public abstract Returned choiceChoiceValue();
    public abstract Returned choiceAnyArrayValue();
    public abstract Returned choiceArrayValue();
    public abstract Returned choiceAnyFunctionValue();
    public abstract Returned choiceFunctionValue();
    public abstract Returned choiceRecordValue();
    public abstract Returned choiceExtensibleRecordValue();

    // RECORD Values
    public abstract Returned recordErrorValue();
    public abstract Returned recordAnyItemValue();
    public abstract Returned recordAnyNodeValue();
    public abstract Returned recordElementValue();
    public abstract Returned recordEnumValue();
    public abstract Returned recordBooleanValue();
    public abstract Returned recordNumberValue();
    public abstract Returned recordStringValue();
    public abstract Returned recordAnyMapValue();
    public abstract Returned recordMapValue();
    public abstract Returned recordChoiceValue();
    public abstract Returned recordAnyArrayValue();
    public abstract Returned recordArrayValue();
    public abstract Returned recordAnyFunctionValue();
    public abstract Returned recordFunctionValue();
    public abstract Returned recordRecordValue();
    public abstract Returned recordExtensibleRecordValue();

    // EXTENSIBLE_RECORD Values
    public abstract Returned extensibleRecordErrorValue();
    public abstract Returned extensibleRecordAnyItemValue();
    public abstract Returned extensibleRecordAnyNodeValue();
    public abstract Returned extensibleRecordElementValue();
    public abstract Returned extensibleRecordEnumValue();
    public abstract Returned extensibleRecordBooleanValue();
    public abstract Returned extensibleRecordNumberValue();
    public abstract Returned extensibleRecordStringValue();
    public abstract Returned extensibleRecordAnyMapValue();
    public abstract Returned extensibleRecordMapValue();
    public abstract Returned extensibleRecordChoiceValue();
    public abstract Returned extensibleRecordAnyArrayValue();
    public abstract Returned extensibleRecordArrayValue();
    public abstract Returned extensibleRecordAnyFunctionValue();
    public abstract Returned extensibleRecordFunctionValue();
    public abstract Returned extensibleRecordRecordValue();
    public abstract Returned extensibleRecordExtensibleRecordValue();


}
