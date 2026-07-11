// File: com/github/akruk/antlrxquery/typesystem/typeoperations/cardinality/CardinalityAlgebra.java
package com.github.akruk.antlrxquery.typesystem.typeoperations.cardinality;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.github.akruk.antlrxquery.typesystem.types.Cardinality;
import com.github.akruk.antlrxquery.typesystem.types.Cardinality.CardinalityValue;
import com.github.akruk.antlrxquery.typesystem.types.Cardinality.CardinalityInterval;
import com.github.akruk.antlrxquery.typesystem.types.Cardinality.Event;
import com.github.akruk.antlrxquery.typesystem.types.Cardinality.FiniteBound;
import com.github.akruk.antlrxquery.typesystem.types.Cardinality.PositiveInfinity;
import com.github.akruk.antlrxquery.typesystem.types.Cardinality.Type;

/**
 * Provides algebraic operations on {@link Cardinality} objects using the
 * event (sweep-line) representation exposed by {@link Cardinality.Event}.
 *
 * All operations return new immutable {@code Cardinality} instances and never
 * modify the input objects.
 */
public final class Cardinalities {

    private Cardinalities() {}

    private static final Comparator<Cardinality.Event> EVENT_COMPARATOR =
            Comparator.<Cardinality.Event, CardinalityValue>comparing(e -> e.value())
                    .thenComparing(e -> e.type() == Cardinality.Type.START ? 0 : 1)// START before END
                    ; 

    /**
     * Merge multiple event arrays into a single sorted event list.
     */
    private static List<Cardinality.Event> mergeAllEvents(final Cardinality... inputs) {
        final List<Cardinality.Event> all = new ArrayList<>();
        for (final Cardinality c : inputs) {
            if (c == null) continue;
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
        final List<Cardinality.Event> events = mergeAllEvents(cardinalities);
        if (events.isEmpty()) return Cardinality.of();

        List<Cardinality.Event> out = new ArrayList<>();

        int active = 0;
        Cardinality.Event segStart = null;

        for (Cardinality.Event e : events) {
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
    public static Cardinality intersection(final @NonNull Cardinality... cardinalities) {
        if (cardinalities.length == 0) return Cardinality.of();

        final List<Cardinality.Event> events = mergeAllEvents(cardinalities);
        if (events.isEmpty()) return Cardinality.of();

        final int required = (int) Arrays.stream(cardinalities).filter(c -> c != null).count();
        List<Cardinality.Event> out = new ArrayList<>();

        int coverage = 0;
        Cardinality.Event segStart = null;

        for (Cardinality.Event e : events) {
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

        return Cardinality.of(out.toArray(Cardinality.Event[]::new));
    }

    /**
     * Subtract right-hand union from left: left \\ (right1 ∪ right2 ∪ ...)
     */
    public static Cardinality subtract(@NonNull final Cardinality left, final @NonNull Cardinality... right) {
        final Cardinality unionRight = union(right);
        
        // Collect events from both sides
        final List<Cardinality.Event> leftEvents = Arrays.asList(left.events());
        final List<Cardinality.Event> rightEvents = Arrays.asList(unionRight.events());

        // Map key -> [leftDeltaSum, rightDeltaSum] and representative event for ordering/inclusivity
        final java.util.Map<String, int[]> deltas = new java.util.HashMap<>();
        final java.util.Map<String, Cardinality.Event> representative = new java.util.HashMap<>();

        java.util.function.Function<Cardinality.Event, String> keyOf = e ->
                e.value().toString() + "|" + (e.type() == Cardinality.Type.START ? "S" : "E");

        // accumulate left deltas
        for (Cardinality.Event e : leftEvents) {
            final String k = keyOf.apply(e);
            deltas.computeIfAbsent(k, kk -> new int[2]);
            deltas.get(k)[0] += e.type() == Cardinality.Type.START ? 1 : -1;
            representative.putIfAbsent(k, e);
        }

        // accumulate right deltas
        for (Cardinality.Event e : rightEvents) {
            final String k = keyOf.apply(e);
            deltas.computeIfAbsent(k, kk -> new int[2]);
            deltas.get(k)[1] += e.type() == Cardinality.Type.START ? 1 : -1;
            representative.putIfAbsent(k, e);
        }

        // Build sorted list of unique representative events
        final List<Cardinality.Event> sorted = new ArrayList<>(representative.values());
        sorted.sort(EVENT_COMPARATOR);

        List<Cardinality.Event> out = new ArrayList<>();

        int leftActive = 0;
        int rightActive = 0;
        Cardinality.Event segStart = null;

        for (Cardinality.Event e : sorted) {
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
                    out.add(new Cardinality.Event(segStart.value(), Cardinality.Type.START));
                    out.add(new Cardinality.Event(e.value(), Cardinality.Type.END));
                    segStart = null;
                }
            }

            // Then apply right deltas
            if (rightDelta != 0) {
                int prevRight = rightActive;
                rightActive += rightDelta;

                // If right becomes >0 while leftActive>0 -> close current segment (we remove overlap)
                if (prevRight == 0 && rightActive > 0 && leftActive > 0 && segStart != null) {
                    out.add(new Cardinality.Event(segStart.value(), Cardinality.Type.START));
                    out.add(new Cardinality.Event(e.value(), Cardinality.Type.END));
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
            out.add(new Cardinality.Event(segStart.value(), Cardinality.Type.START));
            out.add(new Cardinality.Event(CardinalityValue.POSITIVE_INFINITY, Cardinality.Type.END));
        }

        return Cardinality.of(out.toArray(Cardinality.Event[]::new));
    }

    /**
     * Returns true if every value in subset is contained in superset.
     */
    public static boolean isSubset(final Cardinality subset, final Cardinality superset) {
        if (subset == null) return true;
        if (superset == null) return false;

        final Cardinality diff = subtract(subset, superset);
        // if subset \ superset is empty then subset is contained
        return diff.events().length == 0;
    }



    /**
     * Returns true if the two cardinalities share at least one common value.
     */
    public static boolean overlaps(final Cardinality a, final Cardinality b) {
        if (a == null || b == null) return false;
        final Cardinality inter = intersection(a, b);
        return inter.events().length > 0;
    }

    /**
     * Build a Cardinality that represents the sequence merge (concatenation) 
     * of two cardinalities.
     * 
     * Algorithm:
     * 1. Get normalized event arrays from both cardinalities
     * 2. Events come in pairs (START, END) due to normalization
     * 3. For each pair from a and each pair from b:
     *    - Add bounds: [start_a + start_b, end_a + end_b]
     *    - Combine inclusivity with AND logic
     * 4. Collect result events and pass to Cardinality.of() for normalization
     * 
     * Inclusivity rule:
     * - Lower bound inclusive ⟺ both lower bounds are inclusive
     * - Upper bound inclusive ⟺ both upper bounds are inclusive
     */
    public static Cardinality sequenceMerge(final @NonNull Cardinality a, final @NonNull Cardinality b) {
        final Event[] eventsA = a.events();
        final Event[] eventsB = b.events();
        
        if (eventsA.length == 0 || eventsB.length == 0) {
            return Cardinality.of();
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
                final CardinalityValue newLower = addBoundValues(startA.value(), startB.value());
                final CardinalityValue newUpper = addBoundValues(endA.value(), endB.value());
                
                // Generate result events
                resultEvents.add(new Event(newLower, Cardinality.Type.START));
                resultEvents.add(new Event(newUpper, Cardinality.Type.END));
            }
        }
        
        return Cardinality.of(resultEvents.toArray(new Event[0]));
    }

    /**
     * Helper for adding two bound values (finite or infinite).
     */
    private static CardinalityValue addBoundValues(final CardinalityValue v1, final CardinalityValue v2) {
        return switch (v1) {
            case FiniteBound fb1 -> {
                yield switch (v2) {
                    case FiniteBound fb2 -> new FiniteBound(fb1.value().add(fb2.value()));
                    case PositiveInfinity _ -> CardinalityValue.POSITIVE_INFINITY;
                };
            }
            case PositiveInfinity _ -> CardinalityValue.POSITIVE_INFINITY;
        };
    }
        /**
         * Returns true if the given numeric value belongs to the specified cardinality.
         */
        public static boolean contains(final Cardinality cardinality, final BigDecimal value) {
            if (cardinality == null) return false;

            // Sweep the events and check whether value falls into any active segment
            final Cardinality.Event[] events = cardinality.events();
            if (events.length == 0) return false;

            // Find position by scanning events (events are canonical and sorted)
            int active = 0;
            for (Cardinality.Event e : events) {
                int cmp = -e.value().compareTo(value);
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

        public static String stringify(final @NonNull Cardinality cardinality) {
            final List<CardinalityInterval> intervals = cardinality.toIntervals();
            if (intervals.size() == 1) {
                return stringifyCardinalityInterval(intervals.get(0));
            }
            final StringJoiner sj = new StringJoiner(" | ", "(", ")");
            for (final CardinalityInterval interval : intervals) {
                sj.add(stringifyCardinalityInterval(interval));
            }
            return sj.toString();
        }


        private static String stringifyCardinalityInterval(final @NonNull CardinalityInterval interval) {
            final String leftSideBound = switch (interval.lowerBound()) {
                case final PositiveInfinity _ -> 
                    throw new IllegalStateException("Positive infinity lower bound should not be possible for cardinality");
                case final FiniteBound f -> {
                    long value = f.value().longValue();
                    yield value == 0 ? "" : String.valueOf(value);
                }
            };
            final String rightSideBound = switch (interval.upperBound()) {
                case final PositiveInfinity _ -> 
                    "";
                case final FiniteBound f -> 
                    String.valueOf(f.value().intValue());
            };

            if (leftSideBound.equals(rightSideBound)) {
                // If both bounds are equal, we can represent it as a single value (e.g., "3" instead of "3..3").
                return leftSideBound;
            }
            return leftSideBound + ".." + rightSideBound;

        }

        public static Cardinality blockMerge(Cardinality declaredCardinality, Cardinality currentCardinality) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'blockMerge'");
        }

        public static Cardinality alternativeMerge(Cardinality cardinality, Cardinality currentCardinality) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'alternativeMerge'");
        }

        public static Cardinality recursionMerge(Cardinality a, Cardinality b) {
            Cardinality result = a;
            Cardinality current = a;

            while (true) {
                Cardinality next = sequenceMerge(current, b);

                Cardinality merged = union(result, next);

                if (merged.equals(result)) {
                    return result;
                }

                result = merged;
                current = next;
            }
        }

    public static Cardinality multiply(
            final @NonNull Cardinality a,
            final @NonNull Cardinality b) {

        final List<Event> events = new ArrayList<>();

        CardinalityValue minLower = null;
        boolean hasPositiveInfinity = false;

        for (CardinalityInterval ia : a.toIntervals()) {
            for (CardinalityInterval ib : b.toIntervals()) {
                if (ia.upperBound() == CardinalityValue.POSITIVE_INFINITY
                        || ib.upperBound() == CardinalityValue.POSITIVE_INFINITY) {

                    hasPositiveInfinity = true;
                    continue;
                }

                CardinalityValue lower = ia.lowerBound().multiply(ib.lowerBound());

                if (minLower == null || lower.compareTo(minLower) < 0) {
                    minLower = lower;
                }

                // Oba końce skończone
                FiniteBound ua = (FiniteBound) ia.upperBound();
                FiniteBound ub = (FiniteBound) ib.upperBound();

                events.add(new Event(lower, Type.START));
                events.add(new Event(
                        new FiniteBound(ua.value().multiply(ub.value())),
                        Type.END));
            }
        }

        if (hasPositiveInfinity) {
            return Cardinality.of(new Event[] {
                    new Event(minLower, Type.START),
                    new Event(CardinalityValue.POSITIVE_INFINITY, Type.END)
            });
        }

        return Cardinality.of(events.toArray(Event[]::new));
    }

    public static Cardinality optionalize(Cardinality cardinality) {
        // TODO: switch to 0..max(...)
        return Cardinalities.alternativeMerge(Cardinality.ZERO, cardinality);
    }

    public static boolean isValueComparableWith(Cardinality o1, Cardinality o2) {
        final boolean validLeft = o1.isZero() || o1.isOne() || o1.isZeroOrOne();
        final boolean validRight = o2.isZero() || o2.isOne() || o2.isZeroOrOne();
        return validLeft && validRight;
    }
}
