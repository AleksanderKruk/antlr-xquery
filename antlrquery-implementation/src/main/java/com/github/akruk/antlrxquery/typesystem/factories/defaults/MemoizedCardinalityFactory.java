package com.github.akruk.antlrxquery.typesystem.factories.defaults;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.github.akruk.antlrxquery.typesystem.factories.CardinalityFactory;
import com.github.akruk.antlrxquery.typesystem.types.Cardinality;
import com.github.akruk.antlrxquery.typesystem.types.Cardinality.CardinalityInterval;
import com.github.akruk.antlrxquery.typesystem.types.Cardinality.CardinalityValue;
import com.github.akruk.antlrxquery.typesystem.types.Cardinality.Event;
import com.github.akruk.antlrxquery.typesystem.types.Cardinality.FiniteBound;
import com.github.akruk.antlrxquery.typesystem.types.Cardinality.Type;


/**
 * Interning factory for canonical Cardinality instances.
 *
 * Cache levels:
 * - FiniteBound cache
 * - Event cache
 * - Cardinality cache
 */
public final class MemoizedCardinalityFactory implements CardinalityFactory {


    private final ConcurrentMap<BigInteger, FiniteBound> boundCache =
            new ConcurrentHashMap<>();


    private final ConcurrentMap<EventKey, Event> eventCache =
            new ConcurrentHashMap<>();


    private final ConcurrentMap<CardinalityKey, Cardinality> cardinalityCache =
            new ConcurrentHashMap<>();


    private final Cardinality EMPTY;


    private final Cardinality EXACTLY_ONE;
    private final Cardinality ZERO_OR_ONE;
    private final Cardinality ZERO_OR_MORE;
    private final Cardinality ONE_OR_MORE;


    public MemoizedCardinalityFactory() {

        EMPTY = intern();

        EXACTLY_ONE = intern(
                event(bound(BigInteger.ONE), Type.START),
                event(bound(BigInteger.ONE), Type.END)
        );


        ZERO_OR_ONE = intern(
                event(bound(BigInteger.ZERO), Type.START),
                event(bound(BigInteger.ONE), Type.END)
        );


        ZERO_OR_MORE = intern(
                event(bound(BigInteger.ZERO), Type.START),
                event(CardinalityValue.POSITIVE_INFINITY, Type.END)
        );


        ONE_OR_MORE = intern(
                event(bound(BigInteger.ONE), Type.START),
                event(CardinalityValue.POSITIVE_INFINITY, Type.END)
        );
    }


    private FiniteBound bound(BigInteger value) {
        return boundCache.computeIfAbsent(value, FiniteBound::new);
    }


    private Event event(
            CardinalityValue value,
            Type type
    ) {

        return eventCache.computeIfAbsent(
                new EventKey(value, type),
                k -> new Event(value, type)
        );
    }



    private Cardinality intern(Event... events) {

        Event[] normalized = Cardinality.normalize(events);

        CardinalityKey key =
                new CardinalityKey(List.of(normalized));


        return cardinalityCache.computeIfAbsent(
                key,
                k -> Cardinality.skipNormalization(normalized)
        );
    }



    @Override
    public Cardinality empty() {
        return EMPTY;
    }


    @Override
    public Cardinality singleNumber(BigInteger value) {

        FiniteBound bound = bound(value);

        return intern(
                event(bound, Type.START),
                event(bound, Type.END)
        );
    }


    @Override
    public Cardinality closedRange(
            BigInteger lower,
            BigInteger upper
    ) {

        return intern(
                event(bound(lower), Type.START),
                event(bound(upper), Type.END)
        );
    }


    @Override
    public Cardinality inclusiveRange(
            BigInteger lower,
            BigInteger upper
    ) {
        return closedRange(lower, upper);
    }


    @Override
    public Cardinality leftOpenRange(
            BigInteger lower,
            BigInteger upper
    ) {
        return closedRange(lower, upper);
    }


    @Override
    public Cardinality rightOpenRange(
            BigInteger lower,
            BigInteger upper
    ) {
        return closedRange(lower, upper);
    }


    @Override
    public Cardinality minimum(BigInteger lower) {

        return intern(
                event(bound(lower), Type.START),
                event(CardinalityValue.POSITIVE_INFINITY, Type.END)
        );
    }


    @Override
    public Cardinality greaterThan(BigInteger lower) {
        return minimum(lower);
    }


    @Override
    public Cardinality greaterOrEqual(BigInteger lower) {
        return minimum(lower);
    }


    @Override
    public Cardinality maximum(BigInteger upper) {

        return intern(
                event(bound(BigInteger.ZERO), Type.START),
                event(bound(upper), Type.END)
        );
    }


    @Override
    public Cardinality lessThan(BigInteger upper) {
        return maximum(upper);
    }


    @Override
    public Cardinality lessOrEqual(BigInteger upper) {
        return maximum(upper);
    }


    @Override
    public Cardinality exactlyOne() {
        return EXACTLY_ONE;
    }


    @Override
    public Cardinality oneOrMore() {
        return ONE_OR_MORE;
    }


    @Override
    public Cardinality zeroOrOne() {
        return ZERO_OR_ONE;
    }


    @Override
    public Cardinality zeroOrMore() {
        return ZERO_OR_MORE;
    }


    @Override
    public Cardinality of(CardinalityInterval... intervals) {

        Event[] events = new Event[intervals.length * 2];

        int index = 0;

        for (CardinalityInterval interval : intervals) {

            events[index++] =
                    event(interval.lowerBound(), Type.START);

            events[index++] =
                    event(interval.upperBound(), Type.END);
        }

        return intern(events);
    }



    private record EventKey(
            CardinalityValue value,
            Type type
    ) {}


    private record CardinalityKey(
            List<Event> events
    ) {

        CardinalityKey {
            events = List.copyOf(events);
        }

    }

}