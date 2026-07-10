package com.github.akruk.antlrxquery.typesystem.typeoperations;

import java.util.function.Function;

import com.github.akruk.antlrxquery.typesystem.types.XQueryCardinality;
import com.github.akruk.antlrxquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.types.XQueryTypes;

public abstract class SequencetypeUnaryOperation<Returned>
{
    private static final int ZERO         = XQueryCardinality.ZERO.ordinal();
    private static final int ONE          = XQueryCardinality.ONE.ordinal();
    private static final int ZERO_OR_ONE  = XQueryCardinality.ZERO_OR_ONE.ordinal();
    private static final int ONE_OR_MORE  = XQueryCardinality.ONE_OR_MORE.ordinal();
    private static final int ZERO_OR_MORE = XQueryCardinality.ZERO_OR_MORE.ordinal();

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

    protected final Function<AntlrQuerySequenceType, Returned>[][] automaton;

    // private final int occurenceCount = XQueryOccurence.values().length;

    public SequencetypeUnaryOperation()
    {
        this.automaton = getAutomaton();
    }

    private Function<AntlrQuerySequenceType, Returned>[][] getAutomaton() {
        final Function<AntlrQuerySequenceType, Returned>[][] automaton = allocateArray();

        automaton[ZERO][STRING] = this::zeroStringOperation;
        automaton[ZERO][ELEMENT] = this::zeroElementOperation;
        automaton[ZERO][ENUM] = this::zeroEnumOperation;
        automaton[ZERO][BOOLEAN] = this::zeroBooleanOperation;
        automaton[ZERO][NUMBER] = this::zeroNumberOperation;
        automaton[ZERO][ERROR] = this::zeroErrorOperation;
        automaton[ZERO][ANY_ITEM] = this::zeroAnyItemOperation;
        automaton[ZERO][ANY_NODE] = this::zeroAnyNodeOperation;
        automaton[ZERO][ANY_MAP] = this::zeroAnyMapOperation;
        automaton[ZERO][MAP] = this::zeroMapOperation;
        automaton[ZERO][CHOICE] = this::zeroChoiceOperation;
        automaton[ZERO][ANY_ARRAY] = this::zeroAnyArrayOperation;
        automaton[ZERO][ARRAY] = this::zeroArrayOperation;
        automaton[ZERO][ANY_FUNCTION] = this::zeroAnyFunctionOperation;
        automaton[ZERO][FUNCTION] = this::zeroFunctionOperation;
        automaton[ZERO][RECORD] = this::zeroRecordOperation;
        automaton[ZERO][EXTENSIBLE_RECORD] = this::zeroExtensibleRecordOperation;

        automaton[ONE][STRING] = this::oneStringOperation;
        automaton[ONE][ELEMENT] = this::oneElementOperation;
        automaton[ONE][ENUM] = this::oneEnumOperation;
        automaton[ONE][BOOLEAN] = this::oneBooleanOperation;
        automaton[ONE][NUMBER] = this::oneNumberOperation;
        automaton[ONE][ERROR] = this::oneErrorOperation;
        automaton[ONE][ANY_ITEM] = this::oneAnyItemOperation;
        automaton[ONE][ANY_NODE] = this::oneAnyNodeOperation;
        automaton[ONE][ANY_MAP] = this::oneAnyMapOperation;
        automaton[ONE][MAP] = this::oneMapOperation;
        automaton[ONE][CHOICE] = this::oneChoiceOperation;
        automaton[ONE][ANY_ARRAY] = this::oneAnyArrayOperation;
        automaton[ONE][ARRAY] = this::oneArrayOperation;
        automaton[ONE][ANY_FUNCTION] = this::oneAnyFunctionOperation;
        automaton[ONE][FUNCTION] = this::oneFunctionOperation;
        automaton[ONE][RECORD] = this::oneRecordOperation;
        automaton[ONE][EXTENSIBLE_RECORD] = this::oneExtensibleRecordOperation;

        automaton[ZERO_OR_ONE][STRING] = this::zeroOrOneStringOperation;
        automaton[ZERO_OR_ONE][ELEMENT] = this::zeroOrOneElementOperation;
        automaton[ZERO_OR_ONE][ENUM] = this::zeroOrOneEnumOperation;
        automaton[ZERO_OR_ONE][BOOLEAN] = this::zeroOrOneBooleanOperation;
        automaton[ZERO_OR_ONE][NUMBER] = this::zeroOrOneNumberOperation;
        automaton[ZERO_OR_ONE][ERROR] = this::zeroOrOneErrorOperation;
        automaton[ZERO_OR_ONE][ANY_ITEM] = this::zeroOrOneAnyItemOperation;
        automaton[ZERO_OR_ONE][ANY_NODE] = this::zeroOrOneAnyNodeOperation;
        automaton[ZERO_OR_ONE][ANY_MAP] = this::zeroOrOneAnyMapOperation;
        automaton[ZERO_OR_ONE][MAP] = this::zeroOrOneMapOperation;
        automaton[ZERO_OR_ONE][CHOICE] = this::zeroOrOneChoiceOperation;
        automaton[ZERO_OR_ONE][ANY_ARRAY] = this::zeroOrOneAnyArrayOperation;
        automaton[ZERO_OR_ONE][ARRAY] = this::zeroOrOneArrayOperation;
        automaton[ZERO_OR_ONE][ANY_FUNCTION] = this::zeroOrOneAnyFunctionOperation;
        automaton[ZERO_OR_ONE][FUNCTION] = this::zeroOrOneFunctionOperation;
        automaton[ZERO_OR_ONE][RECORD] = this::zeroOrOneRecordOperation;
        automaton[ZERO_OR_ONE][EXTENSIBLE_RECORD] = this::zeroOrOneExtensibleRecordOperation;

        automaton[ONE_OR_MORE][STRING] = this::oneOrMoreStringOperation;
        automaton[ONE_OR_MORE][ELEMENT] = this::oneOrMoreElementOperation;
        automaton[ONE_OR_MORE][ENUM] = this::oneOrMoreEnumOperation;
        automaton[ONE_OR_MORE][BOOLEAN] = this::oneOrMoreBooleanOperation;
        automaton[ONE_OR_MORE][NUMBER] = this::oneOrMoreNumberOperation;
        automaton[ONE_OR_MORE][ERROR] = this::oneOrMoreErrorOperation;
        automaton[ONE_OR_MORE][ANY_ITEM] = this::oneOrMoreAnyItemOperation;
        automaton[ONE_OR_MORE][ANY_NODE] = this::oneOrMoreAnyNodeOperation;
        automaton[ONE_OR_MORE][ANY_MAP] = this::oneOrMoreAnyMapOperation;
        automaton[ONE_OR_MORE][MAP] = this::oneOrMoreMapOperation;
        automaton[ONE_OR_MORE][CHOICE] = this::oneOrMoreChoiceOperation;
        automaton[ONE_OR_MORE][ANY_ARRAY] = this::oneOrMoreAnyArrayOperation;
        automaton[ONE_OR_MORE][ARRAY] = this::oneOrMoreArrayOperation;
        automaton[ONE_OR_MORE][ANY_FUNCTION] = this::oneOrMoreAnyFunctionOperation;
        automaton[ONE_OR_MORE][FUNCTION] = this::oneOrMoreFunctionOperation;
        automaton[ONE_OR_MORE][RECORD] = this::oneOrMoreRecordOperation;
        automaton[ONE_OR_MORE][EXTENSIBLE_RECORD] = this::oneOrMoreExtensibleRecordOperation;

        automaton[ZERO_OR_MORE][STRING] = this::zeroOrMoreStringOperation;
        automaton[ZERO_OR_MORE][ELEMENT] = this::zeroOrMoreElementOperation;
        automaton[ZERO_OR_MORE][ENUM] = this::zeroOrMoreEnumOperation;
        automaton[ZERO_OR_MORE][BOOLEAN] = this::zeroOrMoreBooleanOperation;
        automaton[ZERO_OR_MORE][NUMBER] = this::zeroOrMoreNumberOperation;
        automaton[ZERO_OR_MORE][ERROR] = this::zeroOrMoreErrorOperation;
        automaton[ZERO_OR_MORE][ANY_ITEM] = this::zeroOrMoreAnyItemOperation;
        automaton[ZERO_OR_MORE][ANY_NODE] = this::zeroOrMoreAnyNodeOperation;
        automaton[ZERO_OR_MORE][ANY_MAP] = this::zeroOrMoreAnyMapOperation;
        automaton[ZERO_OR_MORE][MAP] = this::zeroOrMoreMapOperation;
        automaton[ZERO_OR_MORE][CHOICE] = this::zeroOrMoreChoiceOperation;
        automaton[ZERO_OR_MORE][ANY_ARRAY] = this::zeroOrMoreAnyArrayOperation;
        automaton[ZERO_OR_MORE][ARRAY] = this::zeroOrMoreArrayOperation;
        automaton[ZERO_OR_MORE][ANY_FUNCTION] = this::zeroOrMoreAnyFunctionOperation;
        automaton[ZERO_OR_MORE][FUNCTION] = this::zeroOrMoreFunctionOperation;
        automaton[ZERO_OR_MORE][RECORD] = this::zeroOrMoreRecordOperation;
        automaton[ZERO_OR_MORE][EXTENSIBLE_RECORD] = this::zeroOrMoreExtensibleRecordOperation;






        return automaton;
    }


    protected abstract Function<AntlrQuerySequenceType, Returned>[][] allocateArray();
    public abstract Returned zeroStringOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroElementOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroEnumOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroBooleanOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroNumberOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroErrorOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroAnyItemOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroAnyNodeOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroAnyMapOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroMapOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroChoiceOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroAnyArrayOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroArrayOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroAnyFunctionOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroFunctionOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroRecordOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroExtensibleRecordOperation(AntlrQuerySequenceType x);

    public abstract Returned oneStringOperation(AntlrQuerySequenceType x);
    public abstract Returned oneElementOperation(AntlrQuerySequenceType x);
    public abstract Returned oneEnumOperation(AntlrQuerySequenceType x);
    public abstract Returned oneBooleanOperation(AntlrQuerySequenceType x);
    public abstract Returned oneNumberOperation(AntlrQuerySequenceType x);
    public abstract Returned oneErrorOperation(AntlrQuerySequenceType x);
    public abstract Returned oneAnyItemOperation(AntlrQuerySequenceType x);
    public abstract Returned oneAnyNodeOperation(AntlrQuerySequenceType x);
    public abstract Returned oneAnyMapOperation(AntlrQuerySequenceType x);
    public abstract Returned oneMapOperation(AntlrQuerySequenceType x);
    public abstract Returned oneChoiceOperation(AntlrQuerySequenceType x);
    public abstract Returned oneAnyArrayOperation(AntlrQuerySequenceType x);
    public abstract Returned oneArrayOperation(AntlrQuerySequenceType x);
    public abstract Returned oneAnyFunctionOperation(AntlrQuerySequenceType x);
    public abstract Returned oneFunctionOperation(AntlrQuerySequenceType x);
    public abstract Returned oneRecordOperation(AntlrQuerySequenceType x);
    public abstract Returned oneExtensibleRecordOperation(AntlrQuerySequenceType x);

    public abstract Returned zeroOrOneStringOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroOrOneElementOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroOrOneEnumOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroOrOneBooleanOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroOrOneNumberOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroOrOneErrorOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroOrOneAnyItemOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroOrOneAnyNodeOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroOrOneAnyMapOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroOrOneMapOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroOrOneChoiceOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroOrOneAnyArrayOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroOrOneArrayOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroOrOneAnyFunctionOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroOrOneFunctionOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroOrOneRecordOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroOrOneExtensibleRecordOperation(AntlrQuerySequenceType x);

    public abstract Returned oneOrMoreStringOperation(AntlrQuerySequenceType x);
    public abstract Returned oneOrMoreElementOperation(AntlrQuerySequenceType x);
    public abstract Returned oneOrMoreEnumOperation(AntlrQuerySequenceType x);
    public abstract Returned oneOrMoreBooleanOperation(AntlrQuerySequenceType x);
    public abstract Returned oneOrMoreNumberOperation(AntlrQuerySequenceType x);
    public abstract Returned oneOrMoreErrorOperation(AntlrQuerySequenceType x);
    public abstract Returned oneOrMoreAnyItemOperation(AntlrQuerySequenceType x);
    public abstract Returned oneOrMoreAnyNodeOperation(AntlrQuerySequenceType x);
    public abstract Returned oneOrMoreAnyMapOperation(AntlrQuerySequenceType x);
    public abstract Returned oneOrMoreMapOperation(AntlrQuerySequenceType x);
    public abstract Returned oneOrMoreChoiceOperation(AntlrQuerySequenceType x);
    public abstract Returned oneOrMoreAnyArrayOperation(AntlrQuerySequenceType x);
    public abstract Returned oneOrMoreArrayOperation(AntlrQuerySequenceType x);
    public abstract Returned oneOrMoreAnyFunctionOperation(AntlrQuerySequenceType x);
    public abstract Returned oneOrMoreFunctionOperation(AntlrQuerySequenceType x);
    public abstract Returned oneOrMoreRecordOperation(AntlrQuerySequenceType x);
    public abstract Returned oneOrMoreExtensibleRecordOperation(AntlrQuerySequenceType x);

    public abstract Returned zeroOrMoreStringOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroOrMoreElementOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroOrMoreEnumOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroOrMoreBooleanOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroOrMoreNumberOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroOrMoreErrorOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroOrMoreAnyItemOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroOrMoreAnyNodeOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroOrMoreAnyMapOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroOrMoreMapOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroOrMoreChoiceOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroOrMoreAnyArrayOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroOrMoreArrayOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroOrMoreAnyFunctionOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroOrMoreFunctionOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroOrMoreRecordOperation(AntlrQuerySequenceType x);
    public abstract Returned zeroOrMoreExtensibleRecordOperation(AntlrQuerySequenceType x);


}
