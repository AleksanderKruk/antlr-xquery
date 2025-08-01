// package com.github.akruk.antlrxquery.typesystem.typeoperations;

// import java.util.function.BiFunction;

// import com.github.akruk.antlrxquery.typesystem.defaults.XQueryItemType;
// import com.github.akruk.antlrxquery.typesystem.defaults.XQueryOccurence;
// import com.github.akruk.antlrxquery.typesystem.defaults.XQueryTypes;

// @SuppressWarnings("unchecked")
// public abstract class SequencetypeBinaryOperation<Returned>
// {
//     private static final int ZERO         = XQueryOccurence.ZERO.ordinal();
//     private static final int ONE          = XQueryOccurence.ONE.ordinal();
//     private static final int ZERO_OR_ONE  = XQueryOccurence.ZERO_OR_ONE.ordinal();
//     private static final int ONE_OR_MORE  = XQueryOccurence.ONE_OR_MORE.ordinal();
//     private static final int ZERO_OR_MORE = XQueryOccurence.ZERO_OR_MORE.ordinal();

//     protected final Returned[][] automaton;

//     private final int occurenceCount = XQueryOccurence.values().length;

//     public SequencetypeBinaryOperation()
//     {
//         this.automaton = getAutomaton();
//     }


//     private Returned[][] getAutomaton() {
//         final var automaton = new Returned[occurenceCount][occurenceCount];

//         automaton[ZERO][ZERO] = XQueryOccurence.ZERO;
//         automaton[ZERO][ONE] = XQueryOccurence.ZERO;
//         automaton[ZERO][ZERO_OR_ONE] = XQueryOccurence.ZERO;
//         automaton[ZERO][ZERO_OR_MORE] = XQueryOccurence.ZERO;
//         automaton[ZERO][ONE_OR_MORE] = XQueryOccurence.ZERO;

//         automaton[ONE][ZERO] = XQueryOccurence.ONE;
//         automaton[ONE][ONE] = XQueryOccurence.ZERO_OR_ONE;
//         automaton[ONE][ZERO_OR_ONE] = XQueryOccurence.ZERO_OR_ONE;
//         automaton[ONE][ZERO_OR_MORE] = XQueryOccurence.ZERO_OR_ONE;
//         automaton[ONE][ONE_OR_MORE] = XQueryOccurence.ZERO_OR_ONE;

//         automaton[ZERO_OR_ONE][ZERO] = XQueryOccurence.ZERO_OR_ONE;
//         automaton[ZERO_OR_ONE][ONE] = XQueryOccurence.ZERO_OR_ONE;
//         automaton[ZERO_OR_ONE][ZERO_OR_ONE] = XQueryOccurence.ZERO_OR_ONE;
//         automaton[ZERO_OR_ONE][ZERO_OR_MORE] = XQueryOccurence.ZERO_OR_ONE;
//         automaton[ZERO_OR_ONE][ONE_OR_MORE] = XQueryOccurence.ZERO_OR_ONE;

//         automaton[ZERO_OR_MORE][ZERO] = XQueryOccurence.ZERO_OR_MORE;
//         automaton[ZERO_OR_MORE][ONE] = XQueryOccurence.ZERO_OR_MORE;
//         automaton[ZERO_OR_MORE][ZERO_OR_ONE] = XQueryOccurence.ZERO_OR_MORE;
//         automaton[ZERO_OR_MORE][ZERO_OR_MORE] = XQueryOccurence.ZERO_OR_MORE;
//         automaton[ZERO_OR_MORE][ONE_OR_MORE] = XQueryOccurence.ZERO_OR_MORE;

//         automaton[ONE_OR_MORE][ZERO] = XQueryOccurence.ONE_OR_MORE;
//         automaton[ONE_OR_MORE][ONE] = XQueryOccurence.ZERO_OR_MORE;
//         automaton[ONE_OR_MORE][ZERO_OR_ONE] = XQueryOccurence.ZERO_OR_MORE;
//         automaton[ONE_OR_MORE][ZERO_OR_MORE] = XQueryOccurence.ZERO_OR_MORE;
//         automaton[ONE_OR_MORE][ONE_OR_MORE] = XQueryOccurence.ZERO_OR_MORE;

//         return automaton;
//     }


// }
