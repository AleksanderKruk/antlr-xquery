package com.github.akruk.antlrxquery.typesystem.types;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Event-native canonical representation of a numeric domain.
 *
 * All internal storage is a normalized sweep-line event sequence.
 * There is no interval representation in the canonical internal model,
 * however a derived Interval view is available for convenience.
 *
 * This guarantees:
 * - O(n log n) canonicalization
 * - pure state-machine semantics
 */
public final class Cardinality {

    private final Event[] events;

    /**
     * @param events The events need to be normalized before creation of cardinality e.g. {@link Cardinality#normalize(Event[])}
     */
    private Cardinality(Event[] events) {
        this.events = events;
    }

    private Cardinality() {
        this.events = new Event[]{};
    }

    public static Cardinality skipNormalization(Event[] events) {
        return new Cardinality(events);
    }

    public static final Cardinality ZERO = new Cardinality();
    public static final Cardinality ONE = new Cardinality(new Event[]{ new Event(new FiniteBound(BigDecimal.ONE), Type.START), new Event(new FiniteBound(BigDecimal.ONE), Type.END) });
    public static final Cardinality ZERO_OR_ONE = new Cardinality(new Event[]{ new Event(new FiniteBound(BigDecimal.ZERO), Type.START), new Event(new FiniteBound(BigDecimal.ONE), Type.END) });
    public static final Cardinality ZERO_OR_MORE = new Cardinality(new Event[]{ new Event(new FiniteBound(BigDecimal.ZERO), Type.START) });
    public static final Cardinality ONE_OR_MORE = new Cardinality(new Event[]{ new Event(new FiniteBound(BigDecimal.ONE), Type.START), new Event(new FiniteBound(BigDecimal.ONE), Type.END) });

    public static Cardinality of(@NonNull CardinalityInterval... input) {
        if (input.length == 0) {
            return ZERO;
        } else {
            return new Cardinality(normalize(input));
        }
    }

    public static Cardinality of(@NonNull Event... input) {
        return new Cardinality(normalize(input));
    }

    public static Cardinality of() {
        return ZERO;
    }
    
    /**
     * Convert internal events to public events for external inspection.
     */
    public Event[] events() {
        return events.clone();
    }

    public static enum Type { START, END }

    public static record Event(
            CardinalityValue value,
            Type type
    ) {}

    /**
     * Helper record for easier inline Cardinality creation
     * @apiNote BoundValues cannot be null
     */
    public record CardinalityInterval(
            @NonNull CardinalityValue lowerBound,
            @NonNull CardinalityValue upperBound
    ) {

    }

    /**
     * Converts arbitrary intervals into canonical event stream.
     */
    private static Event[] normalize(CardinalityInterval[] input) {

        List<Event> events = new ArrayList<>(input.length * 2);

        for (CardinalityInterval i : input) {
            events.add(new Event(i.lowerBound(), Type.START));
            events.add(new Event(i.upperBound(), Type.END));
        }

        events.sort((a, b) -> {
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

        CardinalityValue segmentStart = null;

        for (Event e : events) {

            if (e.type == Type.START) {

                if (active == 0) {
                    segmentStart = e.value;
                }

                active++;
            } else {

                active--;

                if (active == 0) {
                    canonical.add(new Event(
                            segmentStart,
                            Type.START
                    ));

                    canonical.add(new Event(
                            e.value,
                            Type.END
                    ));
                }
            }
        }

        return canonical.toArray(Event[]::new);
    }

    /**
     * Normalizes an already flattened event stream into canonical event sequence.
     */
    public static Event[] normalize(Event[] input) {
        if (input.length == 0) {
            return input;
        }

        Event[] events = input.clone();

        java.util.Arrays.sort(events, (a, b) -> {
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

        CardinalityValue segmentStart = null;

        for (Event e : events) {
            if (e.type == Type.START) {
                if (active == 0) {
                    segmentStart = e.value;
                }
                active++;
            } else {
                active--;
                if (active == 0) {
                    canonical.add(new Event(segmentStart, Type.START));
                    canonical.add(new Event(e.value, Type.END));
                }
            }
        }

        return canonical.toArray(Event[]::new);
    }

    /**
     * Reconstructs interval view (derived, not canonical storage).
     */
    public List<CardinalityInterval> toIntervals() {

        List<CardinalityInterval> out = new ArrayList<>();

        int active = 0;

        CardinalityValue start = null;

        for (Event e : events) {

            if (e.type == Type.START) {

                if (active == 0) {
                    start = e.value;
                }

                active++;
            } else {

                active--;

                if (active == 0) {
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
            permits FiniteBound, PositiveInfinity {

        Cardinality.CardinalityValue POSITIVE_INFINITY = new Cardinality.PositiveInfinity();

        public int compareTo(CardinalityValue other);
        public int compareTo(BigDecimal other);
        public CardinalityValue multiply(CardinalityValue other);
    }

    public record FiniteBound(BigDecimal value) implements CardinalityValue {
        public FiniteBound {
            value = value.stripTrailingZeros();
        }

        @Override
        public int compareTo(CardinalityValue other) {
            return switch (other) {
                case FiniteBound f -> value.compareTo(f.value);
                case PositiveInfinity _ -> -1;
            };
        }

        @Override
        public int compareTo(BigDecimal other) {
            return value.compareTo(other);
        }

        @Override
        public CardinalityValue multiply(CardinalityValue other) {
            return switch (other) {
                case FiniteBound f -> new FiniteBound(value.multiply(f.value));
                case PositiveInfinity inf -> inf;
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
        public int compareTo(BigDecimal other) {
            return 1; // +inf > any finite value
        }
        @Override
        public CardinalityValue multiply(CardinalityValue other) {
            return this;
        }
    }
    
    public boolean isZero() {
        return events.length == 0;
    }

    public boolean isOne() { // TODO: simplify
        return events.length == 2 
            && events[0].value instanceof FiniteBound f && f.value().compareTo(BigDecimal.ONE) == 0 
            && events[1].value instanceof FiniteBound f2 && f2.value().compareTo(BigDecimal.ONE) == 0 
            ;
    }

    /** 
     * Returns true if the cardinality is a singular value, i.e. it has exactly one possible value. 
    */
    public boolean isSingular() {
        if (events.length != 2) {
            return false;
        }
        if (!(events[0].value instanceof FiniteBound f1)) {
            return false;
        }
        if (!(events[1].value instanceof FiniteBound f2)) {
            return false;
        }
        return f1.value().compareTo(f2.value()) == 0;
    }

    public boolean isZeroOrOne() {
        return events.length == 2 
            && events[0].value instanceof FiniteBound f && f.value().compareTo(BigDecimal.ZERO) == 0
            && events[1].value instanceof FiniteBound f2 && f2.value().compareTo(BigDecimal.ONE) == 0 
            ;
    }

}
