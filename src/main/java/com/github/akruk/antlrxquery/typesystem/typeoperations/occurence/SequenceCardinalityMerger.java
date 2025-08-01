package com.github.akruk.antlrxquery.typesystem.typeoperations.occurence;

import com.github.akruk.antlrxquery.typesystem.defaults.XQueryCardinality;

public class SequenceCardinalityMerger
{
    private static final byte ZERO         = (byte) XQueryCardinality.ZERO.ordinal();
    private static final byte ONE          = (byte) XQueryCardinality.ONE.ordinal();
    private static final byte ZERO_OR_ONE  = (byte) XQueryCardinality.ZERO_OR_ONE.ordinal();
    private static final byte ONE_OR_MORE  = (byte) XQueryCardinality.ONE_OR_MORE.ordinal();
    private static final byte ZERO_OR_MORE = (byte) XQueryCardinality.ZERO_OR_MORE.ordinal();

    protected final byte[][] automaton;

    private final int occurenceCount = XQueryCardinality.values().length;

    public SequenceCardinalityMerger()
    {
        this.automaton = getAutomaton();
    }


    private byte[][] getAutomaton() {
        final var automaton = new byte[occurenceCount][occurenceCount];

        automaton[ZERO][ZERO] = ZERO;
        automaton[ZERO][ONE] = ONE;
        automaton[ZERO][ZERO_OR_ONE] = ZERO_OR_ONE;
        automaton[ZERO][ZERO_OR_MORE] = ZERO_OR_MORE;
        automaton[ZERO][ONE_OR_MORE] = ONE_OR_MORE;

        automaton[ONE][ZERO]         = ONE;
        automaton[ONE][ONE]          = ONE_OR_MORE;
        automaton[ONE][ZERO_OR_ONE]  = ONE_OR_MORE;
        automaton[ONE][ZERO_OR_MORE] = ONE_OR_MORE;
        automaton[ONE][ONE_OR_MORE]  = ONE_OR_MORE;

        automaton[ZERO_OR_ONE][ZERO] = ZERO_OR_ONE;
        automaton[ZERO_OR_ONE][ONE] = ONE_OR_MORE;
        automaton[ZERO_OR_ONE][ZERO_OR_ONE] = ZERO_OR_MORE;
        automaton[ZERO_OR_ONE][ZERO_OR_MORE] = ZERO_OR_MORE;
        automaton[ZERO_OR_ONE][ONE_OR_MORE] = ONE_OR_MORE;

        automaton[ZERO_OR_MORE][ZERO] = ZERO_OR_MORE;
        automaton[ZERO_OR_MORE][ONE] = ONE_OR_MORE;
        automaton[ZERO_OR_MORE][ZERO_OR_ONE] = ZERO_OR_MORE;
        automaton[ZERO_OR_MORE][ZERO_OR_MORE] = ZERO_OR_MORE;
        automaton[ZERO_OR_MORE][ONE_OR_MORE] = ONE_OR_MORE;

        automaton[ONE_OR_MORE][ZERO] = ONE_OR_MORE;
        automaton[ONE_OR_MORE][ONE] = ONE_OR_MORE;
        automaton[ONE_OR_MORE][ZERO_OR_ONE] = ONE_OR_MORE;
        automaton[ONE_OR_MORE][ZERO_OR_MORE] = ONE_OR_MORE;
        automaton[ONE_OR_MORE][ONE_OR_MORE] = ONE_OR_MORE;
        return automaton;
    }

    public byte merge(int o1, int o2)
    {
        return automaton[o1][o2];
    }

    private final XQueryCardinality[] values = XQueryCardinality.values();
    public XQueryCardinality merge(XQueryCardinality o1, XQueryCardinality  o2) {
        return values[automaton[o1.ordinal()][o2.ordinal()]];
    }

}
