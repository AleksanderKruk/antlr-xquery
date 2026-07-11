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
public final class RangeConstraint {

    private final Event[] events;

    /**
     * @param events The events need to be normalized before creation of cardinality e.g. {@link RangeConstraint#normalize(Event[])}
     */
    private RangeConstraint(Event[] events) {
        this.events = events;
    }

    private RangeConstraint() {
        this.events = new Event[]{};
    }

    public static RangeConstraint skipNormalization(Event[] events) {
        return new RangeConstraint(events);
    }

    public static final RangeConstraint ZERO = new RangeConstraint();

    public static RangeConstraint of(@NonNull Interval... input) {
        if (input.length == 0) {
            return ZERO;
        } else {
            return new RangeConstraint(normalize(input));
        }
    }

    public static RangeConstraint of(@NonNull Event... input) {
        return new RangeConstraint(normalize(input));
    }

    public static RangeConstraint of() {
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
            BoundValue value,
            Type type,
            boolean inclusive
    ) {}

    /**
     * Helper record for easier inline RangeConstraint creation
     * @apiNote BoundValues cannot be null
     */
    public record Interval(
            @NonNull BoundValue lowerBound,
            boolean lowerInclusive,
            @NonNull BoundValue upperBound,
            boolean upperInclusive
    ) {

    }

    /**
     * Converts arbitrary intervals into canonical event stream.
     */
    private static Event[] normalize(Interval[] input) {

        List<Event> events = new ArrayList<>(input.length * 2);

        for (Interval i : input) {
            events.add(new Event(i.lowerBound(), Type.START, i.lowerInclusive()));
            events.add(new Event(i.upperBound(), Type.END, i.upperInclusive()));
        }

        events.sort((a, b) -> {
            int cmp = a.value.compareTo(b.value);
            if (cmp != 0) return cmp;

            // Same value: prioritize by type (START before END)
            if (a.type != b.type) {
                return a.type == Type.START ? -1 : 1;
            }

            // Same value and type: inclusive bounds have priority over exclusive
            return a.inclusive == b.inclusive ? 0
                    : (a.inclusive ? -1 : 1);
        });

        List<Event> canonical = new ArrayList<>();

        int active = 0;

        BoundValue segmentStart = null;
        boolean segmentStartInclusive = false;

        for (Event e : events) {

            if (e.type == Type.START) {

                if (active == 0) {
                    segmentStart = e.value;
                    segmentStartInclusive = e.inclusive;
                }

                active++;
            } else {

                active--;

                if (active == 0) {
                    canonical.add(new Event(
                            segmentStart,
                            Type.START,
                            segmentStartInclusive
                    ));

                    canonical.add(new Event(
                            e.value,
                            Type.END,
                            e.inclusive
                    ));
                }
            }
        }

        return canonical.toArray(Event[]::new);
    }

    /**
     * Normalizes an already flattened event stream into canonical event sequence.
     */
    private static Event[] normalize(Event[] input) {
        if (input.length == 0) {
            return input;
        }

        Event[] events = input.clone();

        java.util.Arrays.sort(events, (a, b) -> {
            int cmp = a.value.compareTo(b.value);
            if (cmp != 0) return cmp;

            if (a.type != b.type) {
                return a.type == Type.START ? -1 : 1;
            }

            return a.inclusive == b.inclusive ? 0
                    : (a.inclusive ? -1 : 1);
        });

        List<Event> canonical = new ArrayList<>();

        int active = 0;

        BoundValue segmentStart = null;
        boolean segmentStartInclusive = false;

        for (Event e : events) {
            if (e.type == Type.START) {
                if (active == 0) {
                    segmentStart = e.value;
                    segmentStartInclusive = e.inclusive;
                }
                active++;
            } else {
                active--;
                if (active == 0) {
                    canonical.add(new Event(segmentStart, Type.START, segmentStartInclusive));
                    canonical.add(new Event(e.value, Type.END, e.inclusive));
                }
            }
        }

        return canonical.toArray(Event[]::new);
    }

    /**
     * Reconstructs interval view (derived, not canonical storage).
     */
    public List<Interval> toIntervals() {

        List<Interval> out = new ArrayList<>();

        int active = 0;

        BoundValue start = null;
        boolean startInc = false;

        for (Event e : events) {

            if (e.type == Type.START) {

                if (active == 0) {
                    start = e.value;
                    startInc = e.inclusive;
                }

                active++;
            } else {

                active--;

                if (active == 0) {
                    out.add(new Interval(
                            start,
                            startInc,
                            e.value,
                            e.inclusive
                    ));
                }
            }
        }

        return out;
    }

    public sealed interface BoundValue
            extends Comparable<BoundValue>
            permits FiniteBound, NegativeInfinity, PositiveInfinity {

        RangeConstraint.BoundValue NEGATIVE_INFINITY = new RangeConstraint.NegativeInfinity();
        RangeConstraint.BoundValue POSITIVE_INFINITY = new RangeConstraint.PositiveInfinity();

        int compareTo(BoundValue other);
    }

    // TODO: Switch to using sealed interface for inclusive/exclusive bounds instead of boolean flag in Event
    public record FiniteBound(BigDecimal value) implements BoundValue {
        public FiniteBound {
            value = value.stripTrailingZeros();
        }

        @Override
        public int compareTo(BoundValue other) {
            return switch (other) {
                case FiniteBound f -> value.compareTo(f.value);
                case NegativeInfinity _ -> 1;
                case PositiveInfinity _ -> -1;
            };
        }
    }

    public static final class NegativeInfinity implements BoundValue {
        private NegativeInfinity() {}
        @Override
        public int compareTo(BoundValue o) {
            return (o == this) ? 0 : -1;
        }
    }

    public static final class PositiveInfinity implements BoundValue {
        private PositiveInfinity() {}
        @Override
        public int compareTo(BoundValue o) {
            return (o == this) ? 0 : 1;
        }
    }
    
    public boolean isZero() {
        return events.length == 0;
    }

    public boolean isOne() { // TODO: simplify
        return events.length == 1 && events[0].value instanceof FiniteBound f && f.value().compareTo(BigDecimal.ONE) == 0 
            && events[0].inclusive == true && events[1].value instanceof FiniteBound f2 && f2.value().compareTo(BigDecimal.ONE) == 0 && events[1].inclusive == true;
    }

}
