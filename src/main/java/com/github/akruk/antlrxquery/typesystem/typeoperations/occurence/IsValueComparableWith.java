package com.github.akruk.antlrxquery.typesystem.typeoperations.occurence;

import com.github.akruk.antlrxquery.typesystem.defaults.XQueryCardinality;

public class IsValueComparableWith
{
    private static final int ZERO         = XQueryCardinality.ZERO.ordinal();
    private static final int ONE          = XQueryCardinality.ONE.ordinal();
    private static final int ZERO_OR_ONE  = XQueryCardinality.ZERO_OR_ONE.ordinal();

    protected final boolean[][] automaton;

    private final int occurenceCount = XQueryCardinality.values().length;

    public IsValueComparableWith()
    {
        this.automaton = getAutomaton();
    }


    private boolean[][] getAutomaton() {
        final var isValueComparableWith = new boolean[occurenceCount][occurenceCount];
        for (int i = 0; i < isValueComparableWith.length; i++) {
            for (int j = 0; j < isValueComparableWith.length; j++) {
                isValueComparableWith[i][j] = false;
            }
        }
        isValueComparableWith[ZERO][ZERO] = true;
        isValueComparableWith[ZERO][ONE] = true;
        isValueComparableWith[ZERO][ZERO_OR_ONE] = true;

        isValueComparableWith[ONE][ZERO] = true;
        isValueComparableWith[ONE][ONE] = true;
        isValueComparableWith[ONE][ZERO_OR_ONE] = true;

        isValueComparableWith[ZERO_OR_ONE][ONE] = true;
        isValueComparableWith[ZERO_OR_ONE][ZERO] = true;
        isValueComparableWith[ZERO_OR_ONE][ZERO_OR_ONE] = true;
        return isValueComparableWith;
    }

    public boolean isValueComparableWith(XQueryCardinality o1, XQueryCardinality  o2) {
        return automaton[o1.ordinal()][o2.ordinal()];
    }


}
