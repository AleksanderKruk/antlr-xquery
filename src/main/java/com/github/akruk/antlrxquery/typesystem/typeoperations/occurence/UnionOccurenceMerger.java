package com.github.akruk.antlrxquery.typesystem.typeoperations.occurence;

import com.github.akruk.antlrxquery.typesystem.defaults.XQueryCardinality;

public class UnionOccurenceMerger
{
    private static final int ZERO         = XQueryCardinality.ZERO.ordinal();
    private static final int ONE          = XQueryCardinality.ONE.ordinal();
    private static final int ZERO_OR_ONE  = XQueryCardinality.ZERO_OR_ONE.ordinal();
    private static final int ONE_OR_MORE  = XQueryCardinality.ONE_OR_MORE.ordinal();
    private static final int ZERO_OR_MORE = XQueryCardinality.ZERO_OR_MORE.ordinal();

    protected final XQueryCardinality[][] automaton;

    private final int occurenceCount = XQueryCardinality.values().length;

    public UnionOccurenceMerger()
    {
        this.automaton = getAutomaton();
    }


    private XQueryCardinality[][] getAutomaton() {
        final var automaton = new XQueryCardinality[occurenceCount][occurenceCount];

        automaton[ZERO][ZERO] = XQueryCardinality.ZERO;
        automaton[ZERO][ONE] = XQueryCardinality.ONE;
        automaton[ZERO][ZERO_OR_ONE] = XQueryCardinality.ZERO_OR_ONE;
        automaton[ZERO][ZERO_OR_MORE] = XQueryCardinality.ZERO_OR_MORE;
        automaton[ZERO][ONE_OR_MORE] = XQueryCardinality.ONE_OR_MORE;

        automaton[ONE][ZERO] = XQueryCardinality.ONE;
        automaton[ONE][ONE] = XQueryCardinality.ONE_OR_MORE;
        automaton[ONE][ZERO_OR_ONE] = XQueryCardinality.ONE_OR_MORE;
        automaton[ONE][ZERO_OR_MORE] = XQueryCardinality.ONE_OR_MORE;
        automaton[ONE][ONE_OR_MORE] = XQueryCardinality.ONE_OR_MORE;

        automaton[ZERO_OR_ONE][ZERO] = XQueryCardinality.ZERO_OR_ONE;
        automaton[ZERO_OR_ONE][ONE] = XQueryCardinality.ONE_OR_MORE;
        automaton[ZERO_OR_ONE][ZERO_OR_ONE] = XQueryCardinality.ZERO_OR_MORE;
        automaton[ZERO_OR_ONE][ZERO_OR_MORE] = XQueryCardinality.ZERO_OR_MORE;
        automaton[ZERO_OR_ONE][ONE_OR_MORE] = XQueryCardinality.ONE_OR_MORE;

        automaton[ZERO_OR_MORE][ZERO] = XQueryCardinality.ZERO_OR_MORE;
        automaton[ZERO_OR_MORE][ONE] = XQueryCardinality.ONE_OR_MORE;
        automaton[ZERO_OR_MORE][ZERO_OR_ONE] = XQueryCardinality.ZERO_OR_MORE;
        automaton[ZERO_OR_MORE][ZERO_OR_MORE] = XQueryCardinality.ZERO_OR_MORE;
        automaton[ZERO_OR_MORE][ONE_OR_MORE] = XQueryCardinality.ONE_OR_MORE;

        automaton[ONE_OR_MORE][ZERO] = XQueryCardinality.ONE_OR_MORE;
        automaton[ONE_OR_MORE][ONE] = XQueryCardinality.ONE_OR_MORE;
        automaton[ONE_OR_MORE][ZERO_OR_ONE] = XQueryCardinality.ONE_OR_MORE;
        automaton[ONE_OR_MORE][ZERO_OR_MORE] = XQueryCardinality.ONE_OR_MORE;
        automaton[ONE_OR_MORE][ONE_OR_MORE] = XQueryCardinality.ONE_OR_MORE;
        return automaton;
    }

    public XQueryCardinality merge(XQueryCardinality o1, XQueryCardinality  o2) {
        return automaton[o1.ordinal()][o2.ordinal()];
    }


}
