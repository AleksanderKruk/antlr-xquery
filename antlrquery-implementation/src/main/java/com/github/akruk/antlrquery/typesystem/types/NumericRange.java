package com.github.akruk.antlrquery.typesystem.types;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Ranges;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.value.qual.ArrayLenRange;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * Event-native canonical representation of a numeric domain.
 * All internal storage is a normalized sweep-line event sequence.
 * There is no interval representation in the canonical internal model,
 * however a derived Interval view is available for convenience.
 */
@DefaultQualifier(NonNull.class)
public final class NumericRange {

    public static final NumericRange FULL = NumericRange.of(
            new Event(BoundValue.NEGATIVE_INFINITY, Type.START),
            new Event(BoundValue.POSITIVE_INFINITY, Type.END)
    );

    public static final NumericRange NON_NEGATIVE = NumericRange.of(
            new Event(new FiniteBound(BigDecimal.valueOf(0), true), Type.START),
            new Event(BoundValue.POSITIVE_INFINITY, Type.END)
    );

    private final Event[] events;

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

    public static NumericRange of(final Event... input) {
        return new NumericRange(normalize(input));
    }

    public static NumericRange of() {
        return ZERO;
    }

    public static NumericRange of(int@ArrayLenRange(from = 1)... ints) {
        assert ints.length > 0;
        Event[] events = new Event[ints.length * 2];
        for (int i = 0, a = 0, b = 1; b < events.length; i++, a += 2, b += 2) {
            FiniteBound fb = new FiniteBound(BigDecimal.valueOf(ints[i]), true);
            events[a] = new Event(fb, Type.START);
            events[b] = new Event(fb, Type.END);
        }
        return new NumericRange(events);
    }

    public static NumericRange of(BigDecimal bigDecimal) {
        FiniteBound fb = new FiniteBound(bigDecimal, true);
        return new NumericRange(
                new Event(fb, Type.START),
                new Event(fb, Type.END)
        );
    }

    public static NumericRange of(long@ArrayLenRange(from = 1)... ints) {
        assert ints.length > 0;
        Event[] events = new Event[ints.length * 2];
        for (int i = 0, a = 0, b = 1; b < events.length; i++, a += 2, b += 2) {
            FiniteBound fb = new FiniteBound(BigDecimal.valueOf(ints[i]), true);
            events[a] = new Event(fb, Type.START);
            events[b] = new Event(fb, Type.END);
        }
        return new NumericRange(events);
    }

    public Event[] events() {
        return events.clone();
    }

    public enum Type { START, END }

    public record Event(
            BoundValue value,
            Type type
    ) {}

    public record Interval(
            BoundValue lowerBound,
            BoundValue upperBound
    ) {}

    private static Event[] normalize(final Interval[] input) {

        final List<Event> events = new ArrayList<>(input.length * 2);

        for (final Interval i : input) {
            events.add(new Event(i.lowerBound(), Type.START));
            events.add(new Event(i.upperBound(), Type.END));
        }

        events.sort((a, b) -> {
            final int cmp = a.value.compareTo(b.value);
            if (cmp != 0) return cmp;

            if (a.type != b.type) {
                return a.type == Type.START ? -1 : 1;
            }

            boolean ai = a.value.inclusive();
            boolean bi = b.value.inclusive();
            return ai == bi ? 0 : (ai ? -1 : 1);
        });

        final List<Event> canonical = createCanonical(events);

        return canonical.toArray(Event[]::new);
    }

    private static List<Event> createCanonical(List<Event> events) {
        final List<Event> canonical = new ArrayList<>();

        int active = 0;

        BoundValue segmentStart = null;

        for (final Event e : events) {

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
        return canonical;
    }

    private static Event[] normalize(final Event[] input) {
        if (input.length == 0) {
            return input;
        }

        final Event[] events = input.clone();

        Arrays.sort(events, (a, b) -> {
            final int cmp = a.value.compareTo(b.value);
            if (cmp != 0) return cmp;

            if (a.type != b.type) {
                return a.type == Type.START ? -1 : 1;
            }

            boolean ai = a.value.inclusive();
            boolean bi = b.value.inclusive();
            return ai == bi ? 0 : (ai ? -1 : 1);
        });

        final List<Event> canonical = new ArrayList<>();

        int active = 0;

        BoundValue segmentStart = null;

        for (final Event e : events) {
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

    public List<Interval> toIntervals() {

        final List<Interval> out = new ArrayList<>();

        int active = 0;

        @Nullable BoundValue start = null;

        for (final Event e : events) {

            if (e.type == Type.START) {

                if (active == 0) {
                    start = e.value;
                }

                active++;
            } else {

                active--;

                if (active == 0) {
                    assert start != null;
                    out.add(new Interval(start, e.value));
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

        boolean inclusive();
    }

    public record FiniteBound(BigDecimal value, boolean inclusive) implements BoundValue {
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

        @Override
        public boolean inclusive() {
            return false;
        }
    }

    public static final class PositiveInfinity implements BoundValue {
        private PositiveInfinity() {}

        @Override
        public int compareTo(final BoundValue o) {
            return (o == this) ? 0 : 1;
        }

        @Override
        public boolean inclusive() {
            return false;
        }
    }

    public boolean isZero() {
        return events.length == 0;
    }

    public boolean isOne() {
        return events.length == 2
                && events[0].value instanceof FiniteBound(BigDecimal value, boolean inclusive)
                && value.compareTo(BigDecimal.ONE) == 0
                && inclusive
                && events[1].value instanceof FiniteBound(BigDecimal value1, boolean inclusive1)
                && value1.compareTo(BigDecimal.ONE) == 0
                && inclusive1;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof final NumericRange nr
                && Arrays.deepEquals(nr.events, events);
    }

    @Override
    public String toString() {
        return Ranges.stringify(this);
    }
}
