// File: com/github/akruk/antlrxquery/typesystem/typeoperations/cardinality/NumericRangeAlgebra.java
package com.github.akruk.antlrxquery.typesystem.typeoperations.cardinality;

import java.math.BigDecimal;
import java.util.*;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

import com.github.akruk.antlrxquery.typesystem.types.NumericRange;
import com.github.akruk.antlrxquery.typesystem.types.NumericRange.Event;
import com.github.akruk.antlrxquery.typesystem.types.NumericRange.FiniteBound;
import com.github.akruk.antlrxquery.typesystem.types.NumericRange.NegativeInfinity;
import com.github.akruk.antlrxquery.typesystem.types.NumericRange.PositiveInfinity;
import com.github.akruk.antlrxquery.typesystem.types.NumericRange.BoundValue;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.value.qual.ArrayLenRange;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * Provides algebraic operations on {@link NumericRange} objects using the
 * event (sweep-line) representation exposed by {@link NumericRange.Event}.
 *
 * All operations return new immutable {@code NumericRange} instances and never
 * modify the input objects.
 */
@DefaultQualifier(NonNull.class)
public final class Ranges {

    private Ranges() {}

    private static final Comparator<NumericRange.Event> EVENT_COMPARATOR =
            Comparator.<NumericRange.Event, BoundValue>comparing(e -> e.value())
                    .thenComparing(e -> e.type() == NumericRange.Type.START ? 0 : 1)// START before END
                    .thenComparing(e -> e.inclusive() ? 0 : 1); // inclusive before exclusive

    /**
     * Merge multiple event arrays into a single sorted event list.
     */
    private static List<NumericRange.Event> mergeAllEvents(final NumericRange... inputs) {
        final List<NumericRange.Event> all = new ArrayList<>();
        for (final NumericRange c : inputs) {
            if (c == null) continue;
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
     * Subtract right-hand union from left: left \\ (right1 ∪ right2 ∪ ...)
     */
    public static @Nullable NumericRange subtract(final NumericRange left, final NumericRange... right) {
        final NumericRange unionRight = union(right);
        
        // Collect events from both sides
        final Event[] leftEvents = left.events();
        final Event[] rightEvents = unionRight.events();

        // Map key -> [leftDeltaSum, rightDeltaSum] and representative event for ordering/inclusivity
        final java.util.Map<String, int[]> deltas = new java.util.HashMap<>();
        final java.util.Map<String, NumericRange.Event> representative = new java.util.HashMap<>();

        java.util.function.Function<NumericRange.Event, String> keyOf = e ->
                e.value().toString() + "|" + (e.type() == NumericRange.Type.START ? "S" : "E") + "|" + (e.inclusive() ? "I" : "X");

        // accumulate left deltas
        for (NumericRange.Event e : leftEvents) {
            final String k = keyOf.apply(e);
            deltas.computeIfAbsent(k, kk -> new int[2]);
            deltas.get(k)[0] += e.type() == NumericRange.Type.START ? 1 : -1;
            representative.putIfAbsent(k, e);
        }

        // accumulate right deltas
        for (NumericRange.Event e : rightEvents) {
            final String k = keyOf.apply(e);
            deltas.computeIfAbsent(k, kk -> new int[2]);
            deltas.get(k)[1] += e.type() == NumericRange.Type.START ? 1 : -1;
            representative.putIfAbsent(k, e);
        }

        // Build sorted list of unique representative events
        final List<NumericRange.Event> sorted = new ArrayList<>(representative.values());
        sorted.sort(EVENT_COMPARATOR);

        List<NumericRange.Event> out = new ArrayList<>();

        int leftActive = 0;
        int rightActive = 0;
        NumericRange.Event segStart = null;

        for (NumericRange.Event e : sorted) {
            final String k = keyOf.apply(e);
            final int[] dr = deltas.getOrDefault(k, new int[2]);
            final int leftDelta = dr[0];
            final int rightDelta = dr[1];

            // Apply left deltas first
            if (leftDelta != 0) {
                int prevLeft = leftActive;
                leftActive += leftDelta;
                // start a segment when left becomes >0 while right==0
                if (prevLeft == 0 && leftActive > 0 && rightActive == 0) {
                    segStart = e;
                }
                // end a segment when left drops to 0 while right==0
                if (prevLeft > 0 && leftActive == 0 && rightActive == 0 && segStart != null) {
                    out.add(new NumericRange.Event(segStart.value(), NumericRange.Type.START, segStart.inclusive()));
                    out.add(new NumericRange.Event(e.value(), NumericRange.Type.END, e.inclusive()));
                    segStart = null;
                }
            }

            // Then apply right deltas
            if (rightDelta != 0) {
                int prevRight = rightActive;
                rightActive += rightDelta;

                // If right becomes >0 while leftActive>0 -> close current segment (we remove overlap)
                if (prevRight == 0 && rightActive > 0 && leftActive > 0 && segStart != null) {
                    out.add(new NumericRange.Event(segStart.value(), NumericRange.Type.START, segStart.inclusive()));
                    out.add(new NumericRange.Event(e.value(), NumericRange.Type.END, e.inclusive()));
                    segStart = null;
                }

                // If right becomes 0 (i.e., gap in right) and leftActive>0 -> start new segment
                if (prevRight > 0 && rightActive == 0 && leftActive > 0) {
                    segStart = e;
                }
            }
        }

        // If sweep ended while a segment was open and rightActive == 0 and leftActive > 0, close it at +inf
        if (segStart != null && leftActive > 0 && rightActive == 0) {
            out.add(new NumericRange.Event(segStart.value(), NumericRange.Type.START, segStart.inclusive()));
            out.add(new NumericRange.Event(BoundValue.POSITIVE_INFINITY, NumericRange.Type.END, true));
        }

        return NumericRange.of(out.toArray(NumericRange.Event[]::new));
    }

    /**
     * Returns true if every value in subset is contained in superset.
     */
    public static boolean isSubtype(final NumericRange subset, final NumericRange superset) {
        if (subset == null) return true;
        if (superset == null) return false;

        final NumericRange diff = subtract(subset, superset);
        // if subset \ superset is empty then subset is contained
        return diff.events().length == 0;
    }



    /**
     * Returns true if the two cardinalities share at least one common value.
     */
    public static boolean overlaps(final NumericRange a, final NumericRange b) {
        if (a == null || b == null) return false;
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'stringify'");
    }


    public static NumericRange indices(NumericRange numericRange) {
        return null;
    }

    public static boolean contains(NumericRange range, int i) {
        return false;
    }

    public static boolean contains(NumericRange range, long i) {
        return false;
    }

    public static NumericRange indices(
            @NonNegative int start,
            int stop)
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
