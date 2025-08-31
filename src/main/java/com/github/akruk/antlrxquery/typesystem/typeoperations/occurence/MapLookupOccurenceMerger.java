package com.github.akruk.antlrxquery.typesystem.typeoperations.occurence;

import com.github.akruk.antlrxquery.typesystem.defaults.XQueryCardinality;

public class MapLookupOccurenceMerger
{
    private static final int ZERO         = XQueryCardinality.ZERO.ordinal();
    private static final int ONE          = XQueryCardinality.ONE.ordinal();
    private static final int ZERO_OR_ONE  = XQueryCardinality.ZERO_OR_ONE.ordinal();
    private static final int ONE_OR_MORE  = XQueryCardinality.ONE_OR_MORE.ordinal();
    private static final int ZERO_OR_MORE = XQueryCardinality.ZERO_OR_MORE.ordinal();

    protected final XQueryCardinality[][] automaton;

    private final int occurenceCount = XQueryCardinality.values().length;

    public MapLookupOccurenceMerger()
    {
        this.automaton = getAutomaton();
    }


    private XQueryCardinality[][] getAutomaton() {
        final var automaton = new XQueryCardinality[occurenceCount][occurenceCount];

        automaton[ZERO][ZERO] = XQueryCardinality.ZERO;
        automaton[ZERO][ONE] = XQueryCardinality.ZERO;
        automaton[ZERO][ZERO_OR_ONE] = XQueryCardinality.ZERO;
        automaton[ZERO][ZERO_OR_MORE] = XQueryCardinality.ZERO;
        automaton[ZERO][ONE_OR_MORE] = XQueryCardinality.ZERO;

        automaton[ONE][ZERO] = XQueryCardinality.ZERO;
        automaton[ONE][ONE] = XQueryCardinality.ZERO_OR_ONE;
        automaton[ONE][ZERO_OR_ONE] = XQueryCardinality.ZERO_OR_ONE;
        automaton[ONE][ZERO_OR_MORE] = XQueryCardinality.ZERO_OR_MORE;
        automaton[ONE][ONE_OR_MORE] = XQueryCardinality.ZERO_OR_MORE;

        automaton[ZERO_OR_ONE][ZERO] = XQueryCardinality.ZERO;
        automaton[ZERO_OR_ONE][ONE] = XQueryCardinality.ZERO_OR_ONE;
        automaton[ZERO_OR_ONE][ZERO_OR_ONE] = XQueryCardinality.ZERO_OR_ONE;
        automaton[ZERO_OR_ONE][ZERO_OR_MORE] = XQueryCardinality.ZERO_OR_MORE;
        automaton[ZERO_OR_ONE][ONE_OR_MORE] = XQueryCardinality.ZERO_OR_MORE;

        automaton[ZERO_OR_MORE][ZERO] = XQueryCardinality.ZERO;
        automaton[ZERO_OR_MORE][ONE] = XQueryCardinality.ZERO_OR_MORE;
        automaton[ZERO_OR_MORE][ZERO_OR_ONE] = XQueryCardinality.ZERO_OR_MORE;
        automaton[ZERO_OR_MORE][ZERO_OR_MORE] = XQueryCardinality.ZERO_OR_MORE;
        automaton[ZERO_OR_MORE][ONE_OR_MORE] = XQueryCardinality.ZERO_OR_MORE;

        automaton[ONE_OR_MORE][ZERO] = XQueryCardinality.ZERO;
        automaton[ONE_OR_MORE][ONE] = XQueryCardinality.ZERO_OR_MORE;
        automaton[ONE_OR_MORE][ZERO_OR_ONE] = XQueryCardinality.ZERO_OR_MORE;
        automaton[ONE_OR_MORE][ZERO_OR_MORE] = XQueryCardinality.ZERO_OR_MORE;
        automaton[ONE_OR_MORE][ONE_OR_MORE] = XQueryCardinality.ZERO_OR_MORE;
        return automaton;
    }

    public XQueryCardinality merge(XQueryCardinality o1, XQueryCardinality  o2) {
        // if (o1)
        return automaton[o1.ordinal()][o2.ordinal()];
    }


}
