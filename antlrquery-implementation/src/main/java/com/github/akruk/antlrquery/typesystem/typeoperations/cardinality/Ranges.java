// File: com/github/akruk/AntlrQuery/typesystem/typeoperations/cardinality/NumericRangeAlgebra.java
package com.github.akruk.antlrquery.typesystem.typeoperations.cardinality;

import java.math.BigDecimal;
import java.util.*;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import com.github.akruk.antlrquery.typesystem.types.NumericRange.Event;
import com.github.akruk.antlrquery.typesystem.types.NumericRange.FiniteBound;
import com.github.akruk.antlrquery.typesystem.types.NumericRange.NegativeInfinity;
import com.github.akruk.antlrquery.typesystem.types.NumericRange.PositiveInfinity;
import com.github.akruk.antlrquery.typesystem.types.NumericRange.BoundValue;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.value.qual.ArrayLenRange;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * Provides algebraic operations on {@link NumericRange} objects using the
 * event (sweep-line) representation exposed by {@link NumericRange.Event}.
 * All operations return new immutable {@code NumericRange} instances and never
 * modify the input objects.
 */
@DefaultQualifier(NonNull.class)
public final class Ranges {

    private Ranges() {}

    private static final Comparator<NumericRange.Event> EVENT_COMPARATOR =
            Comparator.comparing(Event::value)
                    .thenComparing(e -> e.type() == NumericRange.Type.START ? 0 : 1)// START before END
                    .thenComparing(e -> e.inclusive() ? 0 : 1); // inclusive before exclusive

    /**
     * Merge multiple event arrays into a single sorted event list.
     */
    private static List<NumericRange.Event> mergeAllEvents(final NumericRange... inputs) {
        final List<NumericRange.Event> all = new ArrayList<>();
        for (final NumericRange c : inputs) {
            all.addAll(Arrays.asList(c.events()));
        }
        all.sort(EVENT_COMPARATOR);
        return all;
    }

    /**
     * Build a NumericRange that represents the union of all provided cardinalities
     * by performing a sweep over the merged event stream.
     */
    public static NumericRange union(final NumericRange... cardinalities) {
        final List<NumericRange.Event> events = mergeAllEvents(cardinalities);
        if (events.isEmpty()) return NumericRange.of();

        List<NumericRange.Event> out = new ArrayList<>();

        int active = 0;
        NumericRange.Event segStart = null;

        for (NumericRange.Event e : events) {
            if (e.type() == NumericRange.Type.START) {
                if (active == 0) {
                    segStart = e;
                }
                active++;
            } else {
                active--;
                if (active == 0 && segStart != null) {
                    // segment from segStart.value to e.value
                    out.add(new NumericRange.Event(segStart.value(), NumericRange.Type.START, segStart.inclusive()));
                    out.add(new NumericRange.Event(e.value(), NumericRange.Type.END, e.inclusive()));
                    segStart = null;
                }
            }
        }

        return NumericRange.of(out.toArray(NumericRange.Event[]::new));
    }

    /**
     * Compute n-ary intersection by sweeping all events and keeping track of
     * how many cardinalities currently cover the point. A point belongs to the
     * intersection iff coverage == number of non-null inputs.
     */
    public static @Nullable NumericRange intersection(final NumericRange@ArrayLenRange(from = 1) ... cardinalities) {

        if (cardinalities.length == 1) return cardinalities[0];

        final List<NumericRange.Event> events = mergeAllEvents(cardinalities);
        if (events.isEmpty()) return null;

        final long required =  Arrays.stream(cardinalities).count();
        List<NumericRange.Event> out = new ArrayList<>();

        int coverage = 0;
        NumericRange.@Nullable Event segStart = null;

        for (NumericRange.Event e : events) {
            if (e.type() == NumericRange.Type.START) {
                coverage++;
                if (coverage == required) {
                    segStart = e;
                }
            } else {
                if (coverage == required && segStart != null) {
                    out.add(new NumericRange.Event(segStart.value(), NumericRange.Type.START, segStart.inclusive()));
                    out.add(new NumericRange.Event(e.value(), NumericRange.Type.END, e.inclusive()));
                    segStart = null;
                }
                coverage--;
            }
        }

        return NumericRange.of(out.toArray(NumericRange.Event[]::new));
    }

    /**
     * Subtract right-hand union from left: left \ (right1 ∪ right2 ∪ ...)
     */
    public static @Nullable NumericRange subtract(
            final NumericRange left,
            final NumericRange... right)
    {
        final NumericRange rhs = union(right);

        final NumericRange.Event[] a = left.events();
        final NumericRange.Event[] b = rhs.events();

        if (a.length == 0)
            return null;

        if (b.length == 0)
            return left;

        final List<NumericRange.Event> out = new ArrayList<>();

        int ia = 0;
        int ib = 0;

        int leftActive = 0;
        int rightActive = 0;

        NumericRange.Event segmentStart = null;

        while (ia < a.length || ib < b.length) {

            final BoundValue value;
            if (ia == a.length)
                value = b[ib].value();
            else if (ib == b.length)
                value = a[ia].value();
            else {
                final int cmp = a[ia].value().compareTo(b[ib].value());
                value = (cmp <= 0) ? a[ia].value() : b[ib].value();
            }

            int leftStarts = 0;
            int leftEnds = 0;
            int rightStarts = 0;
            int rightEnds = 0;

            boolean leftInclusiveStart = false;
            boolean leftInclusiveEnd = false;
            boolean rightInclusiveStart = false;
            boolean rightInclusiveEnd = false;

            // Collect left events at this value
            while (ia < a.length && a[ia].value().compareTo(value) == 0) {
                NumericRange.Event e = a[ia];
                if (e.type() == NumericRange.Type.START) {
                    leftStarts++;
                    leftInclusiveStart |= e.inclusive();
                } else {
                    leftEnds++;
                    leftInclusiveEnd |= e.inclusive();
                }
                ia++;
            }

            // Collect right events at this value
            while (ib < b.length && b[ib].value().compareTo(value) == 0) {
                NumericRange.Event e = b[ib];
                if (e.type() == NumericRange.Type.START) {
                    rightStarts++;
                    rightInclusiveStart |= e.inclusive();
                } else {
                    rightEnds++;
                    rightInclusiveEnd |= e.inclusive();
                }
                ib++;
            }

            // Detect point-events: equal number of START and END on same side at this coordinate
            final boolean leftPoint = (leftStarts > 0 && leftEnds > 0 && leftStarts == leftEnds);
            final boolean rightPoint = (rightStarts > 0 && rightEnds > 0 && rightStarts == rightEnds);

            // State before applying deltas at this coordinate
            final boolean before = leftActive > 0 && rightActive == 0;

            // If left has a point at this value and right does NOT cover this point, preserve the point.
            // Right covers the point if rightActive > 0 (an interval covering it) or rightPoint == true.
            if (leftPoint && !rightPoint && rightActive == 0 && rightStarts == 0 && rightEnds == 0) {
                out.add(new NumericRange.Event(value, NumericRange.Type.START, leftInclusiveStart || leftInclusiveEnd));
                out.add(new NumericRange.Event(value, NumericRange.Type.END, leftInclusiveStart || leftInclusiveEnd));
                // Points do not change active counts; continue to next coordinate.
                continue;
            }

            // Apply END deltas first
            leftActive -= leftEnds;
            rightActive -= rightEnds;

            // State in the middle (after ends, before starts)
            final boolean middle = leftActive > 0 && rightActive == 0;

            // Apply START deltas
            leftActive += leftStarts;
            rightActive += rightStarts;

            // State after applying starts
            final boolean after = leftActive > 0 && rightActive == 0;

            // Opening a segment: was closed before, now open (use middle because ends at this coord may open)
            if (!before && middle && segmentStart == null) {
                // Choose inclusivity: if any left bound at this coordinate is inclusive prefer inclusive
                boolean inclusive = leftInclusiveStart || leftInclusiveEnd;
                segmentStart = new NumericRange.Event(value, NumericRange.Type.START, inclusive);
            }

            // Closing a segment when middle -> not after (segment ends at this coordinate)
            if (middle && !after) {
                assert segmentStart != null;
                // Use left inclusivity for the end if present
                boolean inclusive = leftInclusiveStart || leftInclusiveEnd;
                out.add(segmentStart);
                out.add(new NumericRange.Event(value, NumericRange.Type.END, inclusive));
                segmentStart = null;
            }

            // If segment was closed before and becomes open after applying starts (no middle), open at this coordinate
            if (!middle && after && segmentStart == null) {
                boolean inclusive = leftInclusiveStart || leftInclusiveEnd;
                segmentStart = new NumericRange.Event(value, NumericRange.Type.START, inclusive);
            }

            // If it was open before and becomes closed in middle (before -> !middle), close at this coordinate
            if (before && !middle) {
                assert segmentStart != null;
                boolean inclusive = leftInclusiveStart || leftInclusiveEnd;
                out.add(segmentStart);
                out.add(new NumericRange.Event(value, NumericRange.Type.END, inclusive));
                segmentStart = null;
            }
        }

        // Close segment at +∞ if still open
        if (segmentStart != null) {
            out.add(segmentStart);
            out.add(new NumericRange.Event(
                    BoundValue.POSITIVE_INFINITY,
                    NumericRange.Type.END,
                    true));
        }

        return out.isEmpty()
                ? null
                : NumericRange.skipNormalization(out.toArray(NumericRange.Event[]::new));
    }

    /**
     * Returns true if every value in subset is contained in superset.
     */
    public static boolean isSubSet(final NumericRange subset, final NumericRange superset) {
        final @Nullable NumericRange diff = subtract(subset, superset);
        return diff == null;
    }



    /**
     * Returns true if the two cardinalities share at least one common value.
     */
    public static boolean overlaps(final NumericRange a, final NumericRange b) {
        final NumericRange inter = intersection(a, b);
        return inter.events().length > 0;
    }

    /**
     * Build a NumericRange that represents the sequence merge (concatenation)
     * of two cardinalities.
     *
     * Algorithm:
     * 1. Get normalized event arrays from both cardinalities
     * 2. Events come in pairs (START, END) due to normalization
     * 3. For each pair from a and each pair from b:
     *    - Add bounds: [start_a + start_b, end_a + end_b]
     *    - Combine inclusivity with AND logic
     * 4. Collect result events and pass to NumericRange.of() for normalization
     *
     * Inclusivity rule:
     * - Lower bound inclusive ⟺ both lower bounds are inclusive
     * - Upper bound inclusive ⟺ both upper bounds are inclusive
     */
    public static NumericRange sequenceMerge(final @NonNull NumericRange a, final @NonNull NumericRange b) {
        final Event[] eventsA = a.events();
        final Event[] eventsB = b.events();

        if (eventsA.length == 0 || eventsB.length == 0) {
            return NumericRange.of();
        }

        final List<Event> resultEvents = new ArrayList<>();

        // Events come in pairs (START, END) due to normalization
        for (int i = 0; i < eventsA.length; i += 2) {
            Event startA = eventsA[i];
            Event endA = eventsA[i + 1];

            for (int j = 0; j < eventsB.length; j += 2) {
                Event startB = eventsB[j];
                Event endB = eventsB[j + 1];

                // Add bounds
                final BoundValue newLower = addBoundValues(startA.value(), startB.value());
                final BoundValue newUpper = addBoundValues(endA.value(), endB.value());

                // Combine inclusivity: both must be inclusive for result to be inclusive
                final boolean newLowerInclusive = startA.inclusive() && startB.inclusive();
                final boolean newUpperInclusive = endA.inclusive() && endB.inclusive();

                // Generate result events
                resultEvents.add(new Event(newLower, NumericRange.Type.START, newLowerInclusive));
                resultEvents.add(new Event(newUpper, NumericRange.Type.END, newUpperInclusive));
            }
        }

        return NumericRange.of(resultEvents.toArray(new Event[0]));
    }

    /**
     * Helper for adding two bound values (finite or infinite).
     */
    private static BoundValue addBoundValues(final BoundValue v1, final BoundValue v2) {
        return switch (v1) {
            case FiniteBound fb1 -> {
                yield switch (v2) {
                    case FiniteBound fb2 -> new FiniteBound(fb1.value().add(fb2.value()));
                    case PositiveInfinity _ -> BoundValue.POSITIVE_INFINITY;
                    case NegativeInfinity _ -> BoundValue.NEGATIVE_INFINITY;
                };
            }
            case PositiveInfinity _ -> BoundValue.POSITIVE_INFINITY;
            case NegativeInfinity _ -> BoundValue.NEGATIVE_INFINITY;
        };
    }

    /**
     * Returns true if the given numeric value belongs to the specified cardinality.
     */
    public static boolean contains(final NumericRange range, final BigDecimal number) {
        if (range == null) return false;

        // Sweep the events and check whether value falls into any active segment
        final NumericRange.Event[] events = range.events();
        if (events.length == 0) return false;

        // Find position by scanning events (events are canonical and sorted)
        int active = 0;
        for (NumericRange.Event e : events) {
            if (e.type() == NumericRange.Type.START) {
                // start event: if value < e.value -> not yet entered
                if (compareValueToBound(number, e.value()) < 0) {
                    // value is before this start -> not in this or subsequent starts
                    return false;
                }
                // if value == e.value -> check inclusivity
                if (compareValueToBound(number, e.value()) == 0) {
                    if (e.inclusive()) return true;
                    // exclusive start: value not included by this start; continue scanning
                }
                active++;
            } else {
                // end event: if value < e.value -> value is inside active segment
                if (compareValueToBound(number, e.value()) < 0) {
                    return active > 0;
                }
                if (compareValueToBound(number, e.value()) == 0) {
                    return e.inclusive() && active > 0;
                }
                active--;
            }
        }
        return false;
    }

    /**
     * Helper: compare BigDecimal value to a BoundValue.
     * Returns -1 if value < bound, 0 if equal, +1 if value > bound.
     */
    private static int compareValueToBound(final BigDecimal value, final BoundValue bound) {
        if (bound instanceof FiniteBound(BigDecimal value1)) {
            return value.compareTo(value1);
        } else if (bound instanceof NumericRange.NegativeInfinity) {
            return 1; // value > -inf
        } else { // PositiveInfinity
            return -1; // value < +inf
        }
    }

    public static String stringify(NumericRange range) {
        final List<NumericRange.Interval> intervals = range.toIntervals();
        if (intervals.size() == 1) {
            return stringifyInterval(intervals.getFirst());
        }
        final StringJoiner sj = new StringJoiner(" | ", "(", ")");
        for (final NumericRange.Interval interval : intervals) {
            sj.add(stringifyInterval(interval));
        }
        return sj.toString();
    }

    private static String stringifyInterval(final NumericRange.Interval interval) {
        final String lowerBound = switch (interval.lowerBound()) {
            case FiniteBound f -> String.valueOf(f.value().longValue());
            case NegativeInfinity _ -> "-∞";
            case PositiveInfinity _ ->
                    throw new IllegalStateException("Positive infinity lower bound should not be possible for cardinality");
        };
        final String rightSideBound = switch (interval.upperBound()) {
            case FiniteBound f -> String.valueOf(f.value().intValue());
            case PositiveInfinity _ -> "∞";
            case NegativeInfinity _ ->
                    throw new IllegalStateException("Positive infinity lower bound should not be possible for cardinality");
        };

        if (lowerBound.equals(rightSideBound)) {
            // If both bounds are equal, we can represent it as a single value (e.g., "3" instead of "3..3").
            return lowerBound;
        }
        return lowerBound + ".." + rightSideBound;

    }

    public static NumericRange indices(final NumericRange range) {
        if (range.isZero()) {
            return NumericRange.ZERO;
        }

        final List<Event> out = new ArrayList<>();

        final Event[] events = range.events();
        for (int i = 0; i < events.length; i += 2) {
            final Event start = events[i];
            final Event end = events[i + 1];

            // ----- first integer in interval -----
            final BoundValue firstBound = switch (start.value()) {
                case NegativeInfinity _ ->
                        new FiniteBound(BigDecimal.ZERO);

                case PositiveInfinity _ ->
                        throw new IllegalStateException();

                case FiniteBound(BigDecimal value) -> {
                    BigDecimal first = value.setScale(0, java.math.RoundingMode.CEILING);
                    if (!start.inclusive() && first.compareTo(value) == 0) {
                        first = first.add(BigDecimal.ONE);
                    }
                    if (first.signum() < 0) {
                        first = BigDecimal.ZERO;
                    }
                    yield new FiniteBound(first);
                }
            };

            // ----- last integer in interval -----
            final BoundValue lastBound = switch (end.value()) {
                case PositiveInfinity _ -> BoundValue.POSITIVE_INFINITY;

                case NegativeInfinity _ ->
                        throw new IllegalStateException();

                case FiniteBound(BigDecimal value) -> {
                    BigDecimal last = value.setScale(0, java.math.RoundingMode.FLOOR);
                    if (!end.inclusive() && last.compareTo(value) == 0) {
                        last = last.subtract(BigDecimal.ONE);
                    }
                    yield new FiniteBound(last);
                }
            };

            if (lastBound instanceof FiniteBound(BigDecimal last)) {
                FiniteBound finiteBound = (FiniteBound) firstBound;
                BigDecimal first = finiteBound.value();
                if (first.compareTo(last) > 0) {
                    continue;
                }
            }

            out.add(new Event(firstBound, NumericRange.Type.START, true));
            out.add(new Event(lastBound, NumericRange.Type.END, true));
        }

        return NumericRange.of(out.toArray(Event[]::new));
    }
    public static boolean contains(NumericRange range, int i) {
        return false;
    }

    public static boolean contains(NumericRange range, long i) {
        return false;
    }

    public static NumericRange indices(
            @NonNegative int start,
            @NonNegative int stop)
    {
        if (start >= stop) {
            return NumericRange.ZERO;
        }

        NumericRange.Interval[] intervals = new NumericRange.Interval[stop - start];
        for (int i = start; i < stop; i++) {
            BigDecimal val = BigDecimal.valueOf(i);
            intervals[i - start] = new NumericRange.Interval(
                    new NumericRange.FiniteBound(val),
                    true,
                    new NumericRange.FiniteBound(val),
                    true
            );
        }

        return NumericRange.of(intervals);
    }
}
