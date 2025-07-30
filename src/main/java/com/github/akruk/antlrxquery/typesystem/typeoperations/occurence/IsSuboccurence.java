package com.github.akruk.antlrxquery.typesystem.typeoperations.occurence;

import com.github.akruk.antlrxquery.typesystem.defaults.XQueryCardinality;

public class IsSuboccurence
{
    private static final int ZERO         =  XQueryCardinality.ZERO.ordinal();
    private static final int ONE          =  XQueryCardinality.ONE.ordinal();
    private static final int ZERO_OR_ONE  =  XQueryCardinality.ZERO_OR_ONE.ordinal();
    private static final int ONE_OR_MORE  =  XQueryCardinality.ONE_OR_MORE.ordinal();
    private static final int ZERO_OR_MORE =  XQueryCardinality.ZERO_OR_MORE.ordinal();

    protected final boolean[][] automaton;

    private final int occurenceCount = XQueryCardinality.values().length;

    public IsSuboccurence()
    {
        this.automaton = getAutomaton();
    }


    private boolean[][] getAutomaton() {
        final var isSubtypeOf = new boolean[occurenceCount][occurenceCount];
        for (int i = 0; i < occurenceCount; i++) {
            for (int j = 0; j < occurenceCount; j++) {
                isSubtypeOf[i][j] = false;
            }
        }
        isSubtypeOf[ZERO][ZERO] = true;
        isSubtypeOf[ZERO][ZERO_OR_ONE] = true;
        isSubtypeOf[ZERO][ZERO_OR_MORE] = true;

        isSubtypeOf[ZERO_OR_ONE][ZERO_OR_ONE] = true;
        isSubtypeOf[ZERO_OR_ONE][ZERO_OR_MORE] = true;
        ;

        isSubtypeOf[ZERO_OR_MORE][ZERO_OR_MORE] = true;

        isSubtypeOf[ONE][ONE] = true;
        isSubtypeOf[ONE][ONE_OR_MORE] = true;
        isSubtypeOf[ONE][ZERO_OR_MORE] = true;
        isSubtypeOf[ONE][ZERO_OR_ONE] = true;

        isSubtypeOf[ONE_OR_MORE][ZERO_OR_MORE] = true;
        isSubtypeOf[ONE_OR_MORE][ONE_OR_MORE] = true;
        return isSubtypeOf;
    }

    public boolean test(int o1, int  o2) {
        return automaton[o1][o2];
    }


}
