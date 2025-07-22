package com.github.akruk.antlrxquery.typesystem.typeoperations.occurence;

import com.github.akruk.antlrxquery.typesystem.defaults.XQueryOccurence;

public class UnionOccurenceMerger
{
    private static final int ZERO         = XQueryOccurence.ZERO.ordinal();
    private static final int ONE          = XQueryOccurence.ONE.ordinal();
    private static final int ZERO_OR_ONE  = XQueryOccurence.ZERO_OR_ONE.ordinal();
    private static final int ONE_OR_MORE  = XQueryOccurence.ONE_OR_MORE.ordinal();
    private static final int ZERO_OR_MORE = XQueryOccurence.ZERO_OR_MORE.ordinal();

    protected final XQueryOccurence[][] automaton;

    private final int occurenceCount = XQueryOccurence.values().length;

    public UnionOccurenceMerger()
    {
        this.automaton = getAutomaton();
    }


    private XQueryOccurence[][] getAutomaton() {
        final var automaton = new XQueryOccurence[occurenceCount][occurenceCount];

        automaton[ZERO][ZERO] = XQueryOccurence.ZERO;
        automaton[ZERO][ONE] = XQueryOccurence.ONE;
        automaton[ZERO][ZERO_OR_ONE] = XQueryOccurence.ZERO_OR_ONE;
        automaton[ZERO][ZERO_OR_MORE] = XQueryOccurence.ZERO_OR_MORE;
        automaton[ZERO][ONE_OR_MORE] = XQueryOccurence.ONE_OR_MORE;

        automaton[ONE][ZERO] = XQueryOccurence.ONE;
        automaton[ONE][ONE] = XQueryOccurence.ONE_OR_MORE;
        automaton[ONE][ZERO_OR_ONE] = XQueryOccurence.ONE_OR_MORE;
        automaton[ONE][ZERO_OR_MORE] = XQueryOccurence.ONE_OR_MORE;
        automaton[ONE][ONE_OR_MORE] = XQueryOccurence.ONE_OR_MORE;

        automaton[ZERO_OR_ONE][ZERO] = XQueryOccurence.ZERO_OR_ONE;
        automaton[ZERO_OR_ONE][ONE] = XQueryOccurence.ONE_OR_MORE;
        automaton[ZERO_OR_ONE][ZERO_OR_ONE] = XQueryOccurence.ZERO_OR_MORE;
        automaton[ZERO_OR_ONE][ZERO_OR_MORE] = XQueryOccurence.ZERO_OR_MORE;
        automaton[ZERO_OR_ONE][ONE_OR_MORE] = XQueryOccurence.ONE_OR_MORE;

        automaton[ZERO_OR_MORE][ZERO] = XQueryOccurence.ZERO_OR_MORE;
        automaton[ZERO_OR_MORE][ONE] = XQueryOccurence.ONE_OR_MORE;
        automaton[ZERO_OR_MORE][ZERO_OR_ONE] = XQueryOccurence.ZERO_OR_MORE;
        automaton[ZERO_OR_MORE][ZERO_OR_MORE] = XQueryOccurence.ZERO_OR_MORE;
        automaton[ZERO_OR_MORE][ONE_OR_MORE] = XQueryOccurence.ONE_OR_MORE;

        automaton[ONE_OR_MORE][ZERO] = XQueryOccurence.ONE_OR_MORE;
        automaton[ONE_OR_MORE][ONE] = XQueryOccurence.ONE_OR_MORE;
        automaton[ONE_OR_MORE][ZERO_OR_ONE] = XQueryOccurence.ONE_OR_MORE;
        automaton[ONE_OR_MORE][ZERO_OR_MORE] = XQueryOccurence.ONE_OR_MORE;
        automaton[ONE_OR_MORE][ONE_OR_MORE] = XQueryOccurence.ONE_OR_MORE;
        return automaton;
    }

    public XQueryOccurence merge(XQueryOccurence o1, XQueryOccurence  o2) {
        return automaton[o1.ordinal()][o2.ordinal()];
    }


}
