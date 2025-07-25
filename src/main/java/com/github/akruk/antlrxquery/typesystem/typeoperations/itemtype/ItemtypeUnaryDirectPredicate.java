package com.github.akruk.antlrxquery.typesystem.typeoperations.itemtype;

import com.github.akruk.antlrxquery.typesystem.defaults.XQueryTypes;

public abstract class ItemtypeUnaryDirectPredicate
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

    protected final boolean[] automaton;

    public ItemtypeUnaryDirectPredicate()
    {
        this.automaton = getAutomaton();
    }


    private boolean[] getAutomaton() {
        final boolean[] automaton = new boolean[typesCount];
        automaton[ERROR] = errorValue();
        automaton[ANY_ITEM] = anyItemValue();
        automaton[ANY_NODE] = anyNodeValue();
        automaton[ELEMENT] = elementValue();
        automaton[ENUM] = enumValue();
        automaton[BOOLEAN] = booleanValue();
        automaton[NUMBER] = numberValue();
        automaton[STRING] = stringValue();
        automaton[ANY_MAP] = anyMapValue();
        automaton[MAP] = mapValue();
        automaton[CHOICE] = choiceValue();
        automaton[ANY_ARRAY] = anyArrayValue();
        automaton[ARRAY] = arrayValue();
        automaton[ANY_FUNCTION] = anyFunctionValue();
        automaton[FUNCTION] = functionValue();
        automaton[RECORD] = recordValue();
        automaton[EXTENSIBLE_RECORD] = extensibleRecordValue();
        return automaton;
    }


    public abstract boolean errorValue();
    public abstract boolean anyItemValue();
    public abstract boolean anyNodeValue();
    public abstract boolean elementValue();
    public abstract boolean enumValue();
    public abstract boolean booleanValue();
    public abstract boolean numberValue();
    public abstract boolean stringValue();
    public abstract boolean anyMapValue();
    public abstract boolean mapValue();
    public abstract boolean choiceValue();
    public abstract boolean anyArrayValue();
    public abstract boolean arrayValue();
    public abstract boolean anyFunctionValue();
    public abstract boolean functionValue();
    public abstract boolean recordValue();
    public abstract boolean extensibleRecordValue();
}
