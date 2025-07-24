package com.github.akruk.antlrxquery.typesystem.typeoperations.itemtype;

import java.util.function.Function;

import com.github.akruk.antlrxquery.typesystem.defaults.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryTypes;

@SuppressWarnings("unchecked")
public abstract class ItemtypeUnaryOperation<Returned>
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

    protected final Function<XQueryItemType, Returned>[] automaton;

    public ItemtypeUnaryOperation()
    {
        this.automaton = getAutomaton();
    }


    private Function<XQueryItemType, Returned>[] getAutomaton() {
        final Function<XQueryItemType, Returned>[] automaton = new Function[typesCount];
        automaton[ERROR] = this::errorOperation;
        automaton[ANY_ITEM] = this::anyItemOperation;
        automaton[ANY_NODE] = this::anyNodeOperation;
        automaton[ELEMENT] = this::elementOperation;
        automaton[ANY_MAP] = this::anyMapOperation;
        automaton[MAP] = this::mapOperation;
        automaton[ANY_ARRAY] = this::anyArrayOperation;
        automaton[ARRAY] = this::arrayOperation;
        automaton[ANY_FUNCTION] = this::anyFunctionOperation;
        automaton[FUNCTION] = this::functionOperation;
        automaton[ENUM] = this::enumOperation;
        automaton[RECORD] = this::recordOperation;
        automaton[EXTENSIBLE_RECORD] = this::extensibleRecordOperation;
        automaton[BOOLEAN] = this::booleanOperation;
        automaton[STRING] = this::stringOperation;
        automaton[NUMBER] = this::numberOperation;
        automaton[CHOICE] = this::choiceOperation;
        return automaton;
    }

    public Function<XQueryItemType, Returned> getOperation(XQueryTypes type) {
        return automaton[type.ordinal()];
    }


    public abstract Returned errorOperation(XQueryItemType x);
    public abstract Returned anyItemOperation(XQueryItemType x);
    public abstract Returned anyNodeOperation(XQueryItemType x);
    public abstract Returned elementOperation(XQueryItemType x);
    public abstract Returned anyMapOperation(XQueryItemType x);
    public abstract Returned mapOperation(XQueryItemType x);
    public abstract Returned anyArrayOperation(XQueryItemType x);
    public abstract Returned arrayOperation(XQueryItemType x);
    public abstract Returned anyFunctionOperation(XQueryItemType x);
    public abstract Returned functionOperation(XQueryItemType x);
    public abstract Returned enumOperation(XQueryItemType x);
    public abstract Returned stringOperation(XQueryItemType x);
    public abstract Returned numberOperation(XQueryItemType x);
    public abstract Returned choiceOperation(XQueryItemType x);
    public abstract Returned booleanOperation(XQueryItemType x);
    public abstract Returned recordOperation(XQueryItemType x);
    public abstract Returned extensibleRecordOperation(XQueryItemType x);
}
