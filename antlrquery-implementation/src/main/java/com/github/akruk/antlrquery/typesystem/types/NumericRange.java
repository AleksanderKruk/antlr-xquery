package com.github.akruk.antlrquery.typesystem.types;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;
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
public final class NumericRange {

    public static final NumericRange FULL = NumericRange.of(new Event(BoundValue.NEGATIVE_INFINITY, Type.START, false), new Event(BoundValue.POSITIVE_INFINITY, Type.END, false));
    private final Event[] events;

    /**
     * @param events The events need to be normalized before creation of cardinality e.g. {@link NumericRange#normalize(Event[])}
     */
    private NumericRange(final Event... events) {
        this.events = events;
    }

    private NumericRange() {
        this.events = new Event[]{};
    }

    public static NumericRange skipNormalization(final Event[] events) {
        return new NumericRange(events);
    }

    public static final NumericRange ZERO = new NumericRange();

    public static NumericRange of(final Interval... input) {
        if (input.length == 0) {
            return ZERO;
        } else {
            return new NumericRange(normalize(input));
        }
    }

    public static NumericRange of( final Event... input) {
        return new NumericRange(normalize(input));
    }

    public static NumericRange of() {
        return ZERO;
    }

    public static NumericRange of(int i) {
        return new NumericRange(
            new Event(new FiniteBound(BigDecimal.valueOf(i)), Type.START, true),
            new Event(new FiniteBound(BigDecimal.valueOf(i)), Type.END, true)
        );
    }

    public static NumericRange of(BigDecimal bigDecimal) {
        return new NumericRange(
                new Event(new FiniteBound(bigDecimal), Type.START, true),
                new Event(new FiniteBound(bigDecimal), Type.END, true)
        );
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
            BoundValue lowerBound,
            boolean lowerInclusive,
            BoundValue upperBound,
            boolean upperInclusive
    ) {

    }

    /**
     * Converts arbitrary intervals into canonical event stream.
     */
    private static Event[] normalize(final Interval[] input) {

        final List<Event> events = new ArrayList<>(input.length * 2);

        for (final Interval i : input) {
            events.add(new Event(i.lowerBound(), Type.START, i.lowerInclusive()));
            events.add(new Event(i.upperBound(), Type.END, i.upperInclusive()));
        }

        events.sort((a, b) -> {
            final int cmp = a.value.compareTo(b.value);
            if (cmp != 0) return cmp;

            // Same value: prioritize by type (START before END)
            if (a.type != b.type) {
                return a.type == Type.START ? -1 : 1;
            }

            // Same value and type: inclusive bounds have priority over exclusive
            return a.inclusive == b.inclusive ? 0
                    : (a.inclusive ? -1 : 1);
        });

        final List<Event> canonical = createCanonical(events);

        return canonical.toArray(Event[]::new);
    }

    private static List<Event> createCanonical(List<Event> events) {
        final List<Event> canonical = new ArrayList<>();

        int active = 0;

        BoundValue segmentStart = null;
        boolean segmentStartInclusive = false;

        for (final Event e : events) {

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
        return canonical;
    }

    /**
     * Normalizes an already flattened event stream into canonical event sequence.
     */
    private static Event[] normalize(final Event[] input) {
        if (input.length == 0) {
            return input;
        }

        final Event[] events = input.clone();

        java.util.Arrays.sort(events, (a, b) -> {
            final int cmp = a.value.compareTo(b.value);
            if (cmp != 0) return cmp;

            if (a.type != b.type) {
                return a.type == Type.START ? -1 : 1;
            }

            return a.inclusive == b.inclusive ? 0
                    : (a.inclusive ? -1 : 1);
        });

        final List<Event> canonical = new ArrayList<>();

        int active = 0;

        BoundValue segmentStart = null;
        boolean segmentStartInclusive = false;

        for (final Event e : events) {
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

        final List<Interval> out = new ArrayList<>();

        int active = 0;

        @Nullable BoundValue start = null;
        boolean startInc = false;

        for (final Event e : events) {

            if (e.type == Type.START) {

                if (active == 0) {
                    start = e.value;
                    startInc = e.inclusive;
                }

                active++;
            } else {

                active--;

                if (active == 0) {
                    assert start != null;
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
            permits FiniteBound, NegativeInfinity, PositiveInfinity 
    {

        NumericRange.BoundValue NEGATIVE_INFINITY = new NumericRange.NegativeInfinity();
        NumericRange.BoundValue POSITIVE_INFINITY = new NumericRange.PositiveInfinity();

        int compareTo(BoundValue other);
    }

    // TODO: Switch to using sealed interface for inclusive/exclusive bounds instead of boolean flag in Event
    public record FiniteBound(BigDecimal value) implements BoundValue {
        public FiniteBound {
            value = value.stripTrailingZeros();
        }

        @Override
        public int compareTo(final BoundValue other) {
            return switch (other) {
                case final FiniteBound f -> value.compareTo(f.value);
                case final NegativeInfinity _ -> 1;
                case final PositiveInfinity _ -> -1;
            };
        }
    }

    public static final class NegativeInfinity implements BoundValue {
        private NegativeInfinity() {}
        @Override
        public int compareTo(final BoundValue o) {
            return (o == this) ? 0 : -1;
        }
    }

    public static final class PositiveInfinity implements BoundValue {
        private PositiveInfinity() {}
        @Override
        public int compareTo(final BoundValue o) {
            return (o == this) ? 0 : 1;
        }
    }
    
    public boolean isZero() {
        return events.length == 0;
    }

    public boolean isOne() { // TODO: simplify
        return events.length == 2
                && events[0].value instanceof FiniteBound(BigDecimal value)
                && value.compareTo(BigDecimal.ONE) == 0
                && events[0].inclusive
                && events[1].value instanceof FiniteBound(BigDecimal value1)
                && value1.compareTo(BigDecimal.ONE) == 0
                && events[1].inclusive;
    }

}
