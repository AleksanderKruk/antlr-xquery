package com.github.akruk.antlrquery.typesystem.types;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Cardinalities;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * Event-native canonical representation of a numeric domain.
 * All internal storage is a normalized sweep-line event sequence.
 * There is no interval representation in the canonical internal model,
 * however a derived Interval view is available for convenience.
 * This guarantees:
 * - O(n log n) canonicalization
 * - pure state-machine semantics
 */
@DefaultQualifier(NonNull.class)
public final class Cardinality {

    private final Event[] events;

    /**
     * @param events The events need to be normalized before creation of cardinality e.g. {@link Cardinality#normalize(Event[])}
     */
    private Cardinality(Event[] events) {
        this.events = events;
    }

    public static Cardinality skipNormalization(Event[] events) {
        return new Cardinality(events);
    }

    public static final Cardinality ZERO = Cardinality.of(BigInteger.ZERO);
    public static final Cardinality ONE = Cardinality.of(BigInteger.ONE);
    public static final Cardinality ZERO_OR_ONE = new Cardinality(
        new Event[]{ new Event(new FiniteBound(BigInteger.ZERO), Type.START), 
                     new Event(new FiniteBound(BigInteger.ONE), Type.END) });
    public static final Cardinality ZERO_OR_MORE = new Cardinality(
        new Event[]{ new Event(new FiniteBound(BigInteger.ZERO), Type.START), 
                     new Event(CardinalityValue.POSITIVE_INFINITY, Type.END) });
    public static final Cardinality ONE_OR_MORE = new Cardinality(
        new Event[]{ new Event(new FiniteBound(BigInteger.ONE), Type.START), 
                     new Event(CardinalityValue.POSITIVE_INFINITY, Type.END) });

    public static Cardinality of(Event... input) {
        return new Cardinality(normalize(input));
    }

    public static Cardinality of(BigInteger point) {
        return skipNormalization(
            new Event[]{ new Event(new FiniteBound(point), Type.START), 
                        new Event(new FiniteBound(point), Type.END) }
        );
    }

    public static Cardinality of(@NonNegative int point) {
        return of(BigInteger.valueOf(point));
    }

    public static Cardinality of(@NonNegative long point) {
        return of(BigInteger.valueOf(point));
    }

    public static Cardinality inclusiveRange(@NonNegative int i, @NonNegative int i1) {
        assert i <= i1;
        return Cardinality.skipNormalization(new Event[]{ new Event(new FiniteBound(BigInteger.valueOf(i)), Type.START),
                new Event(new FiniteBound(BigInteger.valueOf(i1)), Type.END) });
    }

    public static Cardinality inclusiveRange(@NonNegative BigInteger i, @NonNegative BigInteger i1) {
        assert i.compareTo(i1) < 0;
        return Cardinality.skipNormalization(
                new Event[]{
                        new Event(new FiniteBound(i), Type.START),
                        new Event(new FiniteBound(i1), Type.END)
                }
        );
    }

    public static Cardinality greaterThan(@NonNegative int i) {
        return Cardinality.skipNormalization(new Event[]{ new Event(new FiniteBound(BigInteger.valueOf(i)), Type.START),
                new Event(CardinalityValue.POSITIVE_INFINITY, Type.END) });
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Cardinality other)) {
            return false;
        }
        return java.util.Arrays.equals(events, other.events);
    }

    @Override
    public int hashCode() {
        return java.util.Arrays.hashCode(events);
    }

    /**
     * Convert internal events to public events for external inspection.
     */
    public Event[] events() {
        return events.clone();
    }

    public enum Type { START, END }

    public record Event(
            CardinalityValue value,
            Type type
    ) { }

    /**
     * Helper record for easier inline Cardinality creation
     * @apiNote BoundValues cannot be null
     */
    public record CardinalityInterval(
            CardinalityValue lowerBound,
            CardinalityValue upperBound
    ) {

    }

    /**
     * Normalizes an already flattened event stream into canonical event sequence.
     */
    public static Event[] normalize(Event[] input) {
        if (input.length == 0) {
            return input;
        }

        Event[] events = input.clone();

        Arrays.sort(events, (a, b) -> {
            int cmp = a.value.compareTo(b.value);
            if (cmp != 0) {
                return cmp;
            }

            return a.type == b.type
                    ? 0
                    : (a.type == Type.START ? -1 : 1);
        });

        List<Event> canonical = new ArrayList<>();

        int active = 0;

        @Nullable CardinalityValue currentStart = null;
        @Nullable CardinalityValue pendingEnd = null;

        for (Event e : events) {
            if (e.type == Type.START) {
                if (active == 0) {
                    if (pendingEnd == null) {
                        currentStart = e.value;
                    } else if (!pendingEnd.isAdjacent(e.value)) {
                        assert currentStart != null;
                        canonical.add(new Event(currentStart, Type.START));
                        canonical.add(new Event(pendingEnd, Type.END));
                        currentStart = e.value;
                    }
                }
                active++;
            } else {
                active--;
                if (active == 0) {
                    pendingEnd = e.value;
                }
            }
        }

        if (pendingEnd != null) {
            assert currentStart != null;
            canonical.add(new Event(currentStart, Type.START));
            canonical.add(new Event(pendingEnd, Type.END));
        }

        return canonical.toArray(Event[]::new);
    }

    /**
     * Reconstructs interval view (derived, not canonical storage).
     */
    public List<CardinalityInterval> toIntervals() {

        List<CardinalityInterval> out = new ArrayList<>();

        int active = 0;

        @Nullable  CardinalityValue start = null;

        for (Event e : events) {

            if (e.type == Type.START) {

                if (active == 0) {
                    start = e.value;
                }

                active++;
            } else {

                active--;

                if (active == 0) {
                    assert start != null;
                    out.add(new CardinalityInterval(
                            start,
                            e.value
                    ));
                }
            }
        }

        return out;
    }

    public sealed interface CardinalityValue
            extends Comparable<CardinalityValue>
            permits FiniteBound, PositiveInfinity 
    {

        Cardinality.CardinalityValue POSITIVE_INFINITY = new Cardinality.PositiveInfinity();

        int compareTo(CardinalityValue other);
        int compareTo(BigInteger other);
        CardinalityValue multiply(CardinalityValue other);
        boolean isAdjacent(CardinalityValue other);
    }

    public record FiniteBound(BigInteger value) implements CardinalityValue {
        @Override
        public int compareTo(CardinalityValue other) {
            return switch (other) {
                case FiniteBound f -> value.compareTo(f.value);
                case PositiveInfinity _ -> -1;
            };
        }

        @Override
        public int compareTo(BigInteger other) {
            return value.compareTo(other);
        }

        @Override
        public CardinalityValue multiply(CardinalityValue other) {
            return switch (other) {
                case FiniteBound f -> new FiniteBound(value.multiply(f.value));
                case PositiveInfinity inf -> inf;
            };
        }

        @Override
        public boolean isAdjacent(CardinalityValue other) {
            return switch (other) {
                case FiniteBound f ->
                    f.value.compareTo(value.add(BigInteger.ONE)) == 0;
                case PositiveInfinity _ -> false;
            };
        }
    }

    public static final class PositiveInfinity implements CardinalityValue {
        private PositiveInfinity() {}
        @Override
        public int compareTo(CardinalityValue o) {
            return (o == this) ? 0 : 1;
        }
        @Override
        public int compareTo(BigInteger other) {
            return 1; // +inf > any finite value
        }
        @Override
        public CardinalityValue multiply(CardinalityValue other) {
            return this;
        }
        @Override
        public boolean isAdjacent(CardinalityValue other) {
            return false;
        }
    }
    
    public boolean isZero() {
        return this.equals(Cardinality.ZERO);
    }

    public boolean isOne() { // TODO: simplify
        return events.length == 2 
            && events[0].value instanceof FiniteBound(BigInteger value1) && value1.compareTo(BigInteger.ONE) == 0
            && events[1].value instanceof FiniteBound(BigInteger value) && value.compareTo(BigInteger.ONE) == 0
            ;
    }

    public boolean isZeroOrOne() {
        return events.length == 2 
            && events[0].value instanceof FiniteBound(BigInteger value1) && value1.compareTo(BigInteger.ZERO) == 0
            && events[1].value instanceof FiniteBound(BigInteger value) && value.compareTo(BigInteger.ONE) == 0
            ;
    }

    @Override
    public String toString() {
        return Cardinalities.stringify(this);
    }

}
