package com.github.akruk.antlrquery.typesystem.typeoperations.cardinality;

import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.Cardinality.*;
import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.value.qual.MinLen;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Provides algebraic operations on {@link Cardinality} objects using the
 * event (sweep-line) representation exposed by {@link Cardinality.Event}.
 * All operations return new immutable {@code Cardinality} instances and never
 * modify the input objects.
 */
@DefaultQualifier(NonNull.class)
public final class Cardinalities {

    private Cardinalities() {}

    private static final Comparator<Cardinality.Event> EVENT_COMPARATOR =
            Comparator.comparing(Event::value)
                    .thenComparing(e -> e.type() == Cardinality.Type.START ? 0 : 1)// START before END
                    ; 

    /**
     * Merge multiple event arrays into a single sorted event list.
     */
    private static List<Cardinality.Event> mergeAllEvents(final Cardinality... inputs) {
        final List<Cardinality.Event> all = new ArrayList<>();
        for (final Cardinality c : inputs) {
            all.addAll(Arrays.asList(c.events()));
        }
        all.sort(EVENT_COMPARATOR);
        return all;
    }

    /**
     * Build a Cardinality that represents the union of all provided cardinalities
     * by performing a sweep over the merged event stream.
     */
    public static Cardinality union(final Cardinality... cardinalities) {
        if (cardinalities.length == 1) return cardinalities[0];
        final List<Cardinality.Event> events = mergeAllEvents(cardinalities);

        final List<Cardinality.Event> out = new ArrayList<>();

        int active = 0;
        Cardinality.Event segStart = null;

        for (final Cardinality.Event e : events) {
            if (e.type() == Cardinality.Type.START) {
                if (active == 0) {
                    segStart = e;
                }
                active++;
            } else {
                active--;
                if (active == 0 && segStart != null) {
                    // segment from segStart.value to e.value
                    out.add(new Cardinality.Event(segStart.value(), Cardinality.Type.START));
                    out.add(new Cardinality.Event(e.value(), Cardinality.Type.END));
                    segStart = null;
                }
            }
        }

        return Cardinality.of(out.toArray(Cardinality.Event[]::new));
    }

    /**
     * Compute n-ary intersection by sweeping all events and keeping track of
     * how many cardinalities currently cover the point. A point belongs to the
     * intersection iff coverage == number of non-null inputs.
     */
    public static @Nullable Cardinality intersection(final @MinLen(1) Cardinality... cardinalities) {
        assert cardinalities.length != 0 : "There are no cardinalities to intersect";
        if (cardinalities.length == 1) return cardinalities[0];

        final List<Cardinality.Event> events = mergeAllEvents(cardinalities);

        final long required =  Arrays.stream(cardinalities).count();
        final List<Cardinality.Event> out = new ArrayList<>();

        int coverage = 0;
        Cardinality.Event segStart = null;

        for (final Cardinality.Event e : events) {
            if (e.type() == Cardinality.Type.START) {
                coverage++;
                if (coverage == required) {
                    segStart = e;
                }
            } else {
                if (coverage == required && segStart != null) {
                    out.add(new Cardinality.Event(segStart.value(), Cardinality.Type.START));
                    out.add(new Cardinality.Event(e.value(), Cardinality.Type.END));
                    segStart = null;
                }
                coverage--;
            }
        }
        if (out.isEmpty()) return null;

        return Cardinality.of(out.toArray(Cardinality.Event[]::new));
    }

    /**
     * Subtract right-hand union from left: left \\ (right1 ∪ right2 ∪ ...)
     */
    public static @Nullable Cardinality subtract(
            final Cardinality left,
            final Cardinality... right) {

        final Cardinality rhs = union(right);

        final Event[] a = left.events();
        final Event[] b = rhs.events();

        if (a.length == 0)
            return null;

        if (b.length == 0)
            return left;

        final List<Event> out = new ArrayList<>();

        int ia = 0;
        int ib = 0;

        int leftActive = 0;
        int rightActive = 0;

        Event segmentStart = null;

        while (ia < a.length || ib < b.length) {

            final CardinalityValue value;

            if (ia == a.length) {
                value = b[ib].value();
            } else if (ib == b.length) {
                value = a[ia].value();
            } else {
                value = a[ia].value().compareTo(b[ib].value()) <= 0
                        ? a[ia].value()
                        : b[ib].value();
            }

            int leftStarts = 0;
            int leftEnds = 0;
            int rightStarts = 0;
            int rightEnds = 0;

            while (ia < a.length && a[ia].value().compareTo(value) == 0) {
                if (a[ia].type() == Cardinality.Type.START)
                    leftStarts++;
                else
                    leftEnds++;
                ia++;
            }

            while (ib < b.length && b[ib].value().compareTo(value) == 0) {
                if (b[ib].type() == Cardinality.Type.START)
                    rightStarts++;
                else
                    rightEnds++;
                ib++;
            }

            /*
             * State before 'value'.
             */
            final boolean beforeResult =
                    leftActive > 0 && rightActive == 0;

            /*
             * START(value) is already active at 'value'.
             *
             * END(value), however, is also still active at 'value'
             * because intervals are inclusive.
             */
            final int leftAt = leftActive + leftStarts;
            final int rightAt = rightActive + rightStarts;

            final boolean atResult =
                    leftAt > 0 && rightAt == 0;

            /*
             * END(value) becomes inactive only AFTER 'value'.
             */
            final int leftAfter = leftAt - leftEnds;
            final int rightAfter = rightAt - rightEnds;

            final boolean afterResult =
                    leftAfter > 0 && rightAfter == 0;

            /*
             * Transition:
             *
             * OUT -> IN at 'value'
             */
            if (!beforeResult && atResult) {
                segmentStart = new Event(value, Cardinality.Type.START);
            }

            /*
             * Transition:
             *
             * IN -> OUT at 'value'
             *
             * The current value is NOT part of the result, so the
             * previous value is the END of the resulting interval.
             *
             * This happens e.g.:
             *
             * [1,10] - [4,10] = [1,3]
             */
            if (beforeResult && !atResult) {
                if (segmentStart != null) {
                    if (value instanceof FiniteBound(BigInteger value1)) {
                        out.add(segmentStart);
                        out.add(new Event(
                                new Cardinality.FiniteBound(
                                        value1.subtract(BigInteger.ONE)),
                                Cardinality.Type.END));
                        segmentStart = null;
                    }
                }
            }

            /*
             * Transition:
             *
             * IN at 'value' -> OUT after 'value'
             *
             * This is the important case for inclusive END:
             *
             * [1,10] - [1,4]
             *
             * At 4:
             *     atResult   = false
             *     afterResult = true
             *
             * therefore handled below by OUT -> IN after 'value'.
             *
             * For:
             *
             * [1,4] - [10,...]
             *
             * atResult = true
             * afterResult = false
             *
             * so 4 belongs to the result and must be the END.
             */
            if (atResult && !afterResult) {
                if (segmentStart == null) {
                    segmentStart = new Event(value, Cardinality.Type.START);
                }

                out.add(segmentStart);
                out.add(new Event(value, Cardinality.Type.END));
                segmentStart = null;
            }

            /*
             * Transition:
             *
             * OUT at 'value' -> IN after 'value'
             *
             * Since 'value' itself is excluded, the new segment starts
             * at value + 1.
             *
             * This is the case:
             *
             * [1,10] - [1,4]
             *
             * at 4:
             *     atResult    = false
             *     afterResult = true
             *
             * => result starts at 5.
             */
            if (!atResult && afterResult) {
                if (value instanceof FiniteBound(BigInteger value1)) {
                    segmentStart = new Event(
                            new Cardinality.FiniteBound(
                                    value1.add(BigInteger.ONE)),
                            Cardinality.Type.START);
                }
            }

            leftActive = leftAfter;
            rightActive = rightAfter;
        }

        if (segmentStart != null) {
            out.add(segmentStart);
            out.add(new Event(
                    Cardinality.CardinalityValue.POSITIVE_INFINITY,
                    Cardinality.Type.END));
        }

        return out.isEmpty()
                ? null
                : Cardinality.skipNormalization(
                out.toArray(Event[]::new));
    }

    public static CardinalityValue next(FiniteBound f) {
        return new Cardinality.FiniteBound(f.value().add(BigInteger.ONE));
    }



    /**
     * Equivalent to removing union(right) from left that is:
     * 10 remove 7 = 'from 10 elements remove exactly 7' = 3 elements remaining
     * 10 remove 0..7 = 'from 10 elements remove at most 7 elements' = 3..10 elements remaining
     */
    public static @Nullable Cardinality remove(
            final Cardinality left,
            final Cardinality... right)
    {
        final Cardinality rhs = union(right);

        if (left.events().length == 0) {
            return null;
        }

        if (rhs.events().length == 0) {
            return left;
        }

        final List<Cardinality.CardinalityInterval> leftIntervals = left.toIntervals();
        final List<Cardinality.CardinalityInterval> rightIntervals = rhs.toIntervals();

        final List<Cardinality.Event> out = new ArrayList<>();

        for (Cardinality.CardinalityInterval l : leftIntervals) {
            for (Cardinality.CardinalityInterval r : rightIntervals) {

                Cardinality.CardinalityValue lower =
                        subtractFloorZero(l.lowerBound(), r.upperBound());

                Cardinality.CardinalityValue upper =
                        subtractFloorZero(l.upperBound(), r.lowerBound());

                out.add(new Cardinality.Event(lower, Cardinality.Type.START));
                out.add(new Cardinality.Event(upper, Cardinality.Type.END));
            }
        }

        return out.isEmpty()
                ? null
                : Cardinality.of(out.toArray(Cardinality.Event[]::new));
    }

    private static Cardinality.CardinalityValue subtractFloorZero(
            Cardinality.CardinalityValue left,
            Cardinality.CardinalityValue right)
    {
        return switch (left) {
            case Cardinality.PositiveInfinity _ -> Cardinality.CardinalityValue.POSITIVE_INFINITY;

            case Cardinality.FiniteBound(BigInteger a) -> switch (right) {
                case Cardinality.PositiveInfinity _ ->
                        new Cardinality.FiniteBound(BigInteger.ZERO);

                case Cardinality.FiniteBound(BigInteger b) -> {
                    BigInteger result = a.subtract(b);
                    if (result.signum() < 0) {
                        result = BigInteger.ZERO;
                    }
                    yield new Cardinality.FiniteBound(result);
                }
            };
        };
    }

    /**
     * Returns true if every value in subset is contained in superset.
     */
    public static boolean isSubSet(final Cardinality subset, final Cardinality superset) {
        final @Nullable Cardinality diff = subtract(subset, superset);
        // if subset \ superset is empty then subset is contained
        return diff == null;
    }


    /**
     * Build a Cardinality that represents the sequence merge (concatenation) 
     * of two cardinalities.
     * Algorithm:
     * 1. Get normalized event arrays from both cardinalities
     * 2. Events come in pairs (START, END) due to normalization
     * 3. For each pair from a and each pair from b:
     *    - Add bounds: [start_a + start_b, end_a + end_b]
     *    - Combine inclusivity with AND logic
     * 4. Collect result events and pass to Cardinality.of() for normalization
     * Inclusivity rule:
     * - Lower bound inclusive ⟺ both lower bounds are inclusive
     * - Upper bound inclusive ⟺ both upper bounds are inclusive
     */
    public static Cardinality add(
            final @MinLen(1) Cardinality... cardinalities) {

        final List<Event> result = new ArrayList<>();

        buildSums(
                cardinalities,
                0,
                null,
                null,
                result);

        return Cardinality.of(result.toArray(Event[]::new));
    }

    private static void buildSums(
            final Cardinality[] cardinalities,
            final int index,
            final @Nullable CardinalityValue currentLower,
            final @Nullable CardinalityValue currentUpper,
            final List<Event> result) {

        if (index == cardinalities.length) {
            result.add(new Event(currentLower, Type.START));
            result.add(new Event(currentUpper, Type.END));
            return;
        }

        final Event[] events = cardinalities[index].events();

        for (int i = 0; i < events.length; i += 2) {

            final CardinalityValue lower =
                    currentLower == null
                            ? events[i].value()
                            : addBoundValues(currentLower, events[i].value());

            final CardinalityValue upper =
                    currentUpper == null
                            ? events[i + 1].value()
                            : addBoundValues(currentUpper, events[i + 1].value());

            buildSums(
                    cardinalities,
                    index + 1,
                    lower,
                    upper,
                    result);
        }
    }

    /**
     * Helper for adding two bound values (finite or infinite).
     */
    private static CardinalityValue addBoundValues(final CardinalityValue v1, final CardinalityValue v2) {
        return switch (v1) {
            case final FiniteBound fb1 -> switch (v2) {
                case final FiniteBound fb2 -> new FiniteBound(fb1.value().add(fb2.value()));
                case final PositiveInfinity _ -> CardinalityValue.POSITIVE_INFINITY;
            };
            case final PositiveInfinity _ -> CardinalityValue.POSITIVE_INFINITY;
        };
    }
    public static boolean contains(final Cardinality cardinality, final long value) {
        return contains(cardinality, BigInteger.valueOf(value));
    }
        /**
         * Returns true if the given numeric value belongs to the specified cardinality.
         */
        public static boolean contains(final Cardinality cardinality, final BigInteger value) {
            // Sweep the events and check whether value falls into any active segment
            final Cardinality.Event[] events = cardinality.events();
            if (events.length == 0) return false;

            // Find position by scanning events (events are canonical and sorted)
            int active = 0;
            for (final Cardinality.Event e : events) {
                final int cmp = -e.value().compareTo(value);
                if (e.type() == Cardinality.Type.START) {
                    // start event: if value < e.value -> not yet entered
                    if (cmp < 0) {
                        // value is before this start -> not in this or subsequent starts
                        return false;
                    }
                    // if value == e.value -> value is inside active segment
                    if (cmp == 0) {
                        return true;
                    }
                    active++;
                } else {
                    // end event: if value < e.value -> value is inside active segment
                    if (cmp <= 0) {
                        return active > 0;
                    }
                    active--;
                }
            }
            return false;
        }

        public static String stringify(final Cardinality cardinality) {
            final List<CardinalityInterval> intervals = cardinality.toIntervals();
            if (intervals.size() == 1) {
                return stringifyCardinalityInterval(intervals.getFirst());
            }
            final StringJoiner sj = new StringJoiner(" | ", "(", ")");
            for (final CardinalityInterval interval : intervals) {
                sj.add(stringifyCardinalityInterval(interval));
            }
            return sj.toString();
        }
        
        public static String stringifyWithoutParentheses(final Cardinality cardinality) {
            final List<CardinalityInterval> intervals = cardinality.toIntervals();
            if (intervals.size() == 1) {
                return stringifyCardinalityInterval(intervals.getFirst());
            }
            final StringJoiner sj = new StringJoiner(" | ");
            for (final CardinalityInterval interval : intervals) {
                sj.add(stringifyCardinalityInterval(interval));
            }
            return sj.toString();
        }
        
        /**
         * @return Stringifies cardinality with prefix "^" unless cardinality == 1
         */
        public static String stringifyWithPrefix(final Cardinality cardinality) {
            if (cardinality.isOne()) {
                return "";
            } else if (cardinality.isZeroOrOne()) {
                return "?";
            } else if (cardinality.equals(Cardinality.ZERO_OR_MORE)) {
                return "*";
            } else if (cardinality.equals(Cardinality.ONE_OR_MORE)) {
                return "+";
            } else {
                return "^" + stringify(cardinality);
            }
        }

        private static String stringifyCardinalityInterval(final CardinalityInterval interval) {
            final String leftSideBound = switch (interval.lowerBound()) {
                case final PositiveInfinity _ -> 
                    throw new IllegalStateException("Positive infinity lower bound should not be possible for cardinality");
                case final FiniteBound f -> {
                    final long value = f.value().longValue();
                    yield String.valueOf(value);
                }
            };
            final String rightSideBound = switch (interval.upperBound()) {
                case final PositiveInfinity _ -> 
                    "∞";
                case final FiniteBound f -> 
                    String.valueOf(f.value().intValue());
            };

            if (leftSideBound.equals(rightSideBound)) {
                // If both bounds are equal, we can represent it as a single value (e.g., "3" instead of "3..3").
                return leftSideBound;
            }
            return leftSideBound + ".." + rightSideBound;

        }

        public static @Nullable Cardinality recursionMerge(final Cardinality a) {
            @Nullable CardinalityValue min = Cardinalities.min(a);
            if (min==null) return null;
            return Cardinality.of(
                    new Event(min, Type.START),
                    new Event(CardinalityValue.POSITIVE_INFINITY, Type.END)
            );

        }

    public static Cardinality multiply(
            final Cardinality a,
            final Cardinality b)
    {
        final List<Event> events = new ArrayList<>();

        @MonotonicNonNull  CardinalityValue minLower = null;
        boolean hasPositiveInfinity = false;

        for (final CardinalityInterval ia : a.toIntervals()) {
            for (final CardinalityInterval ib : b.toIntervals()) {
                final CardinalityValue lower = ia.lowerBound().multiply(ib.lowerBound());

                if (minLower == null || lower.compareTo(minLower) < 0) {
                    minLower = lower;
                }

                if (ia.upperBound() == CardinalityValue.POSITIVE_INFINITY
                        || ib.upperBound() == CardinalityValue.POSITIVE_INFINITY) {

                    hasPositiveInfinity = true;
                    continue;
                }

                final FiniteBound ua = (FiniteBound) ia.upperBound();
                final FiniteBound ub = (FiniteBound) ib.upperBound();

                events.add(new Event(lower, Type.START));
                events.add(new Event(
                        new FiniteBound(ua.value().multiply(ub.value())),
                        Type.END));
            }
        }

        if (hasPositiveInfinity) {
            return Cardinality.of(new Event(minLower, Type.START), new Event(CardinalityValue.POSITIVE_INFINITY, Type.END));
        }

        return Cardinality.of(events.toArray(Event[]::new));
    }

    public static @Nullable CardinalityValue max(Cardinality cardinality) {
        Event[] events = cardinality.events();

        if (events.length == 0) {
            return null;
        }

        return events[events.length - 1].value();
    }

    public static @Nullable CardinalityValue min(Cardinality cardinality) {
        Event[] events = cardinality.events();

        if (events.length == 0) {
            return null;
        }
        return events[0].value();
    }

    public static @Nullable Cardinality optionalize(Cardinality cardinality) {
        @Nullable CardinalityValue max = max(cardinality);

        if (max == null) {
            return null;
        }

        return Cardinality.skipNormalization(new Event[] {
                new Event(new FiniteBound(BigInteger.ZERO), Cardinality.Type.START),
                new Event(max, Cardinality.Type.END)
        });
    }

    public static boolean areValueComparable(final Cardinality o1, final Cardinality o2) {
        final boolean validLeft = o1.isZero() || o1.isOne() || o1.isZeroOrOne();
        final boolean validRight = o2.isZero() || o2.isOne() || o2.isZeroOrOne();
        return validLeft && validRight;
    }

    public static NumericRange toNumericRange(Cardinality arrayLength) {
        Cardinality.Event[] cardEvents = arrayLength.events();
        NumericRange.Event[] numEvents = new NumericRange.Event[cardEvents.length];

        for (int i = 0; i < cardEvents.length; i++) {
            Cardinality.Event event = cardEvents[i];

            NumericRange.BoundValue numValue = switch (event.value()) {
                case Cardinality.FiniteBound fb -> new NumericRange.FiniteBound(new BigDecimal(fb.value()), true);
                case Cardinality.PositiveInfinity _ -> NumericRange.BoundValue.POSITIVE_INFINITY;
            };

            NumericRange.Type numType = event.type() == Cardinality.Type.START
                    ? NumericRange.Type.START
                    : NumericRange.Type.END;

            numEvents[i] = new NumericRange.Event(numValue, numType);
        }

        return NumericRange.skipNormalization(numEvents);
    }
}
