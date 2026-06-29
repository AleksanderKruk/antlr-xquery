package com.github.akruk.antlrxquery.typesystem.types;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Canonical representation of a set of numeric ranges used in type inference.
 *
 * <p>Cardinality models a union of disjoint intervals over an ordered domain
 * (including infinities). It is primarily used for semantic reasoning in the
 * type system (e.g. range inference, constraint propagation, validation of
 * numeric expressions).</p>
 *
 * <p>Internally, all interval sets are normalized into a canonical form using
 * a sweep-line algorithm. This guarantees that semantically equivalent inputs
 * always produce identical representations.</p>
 */
public record Cardinality(Interval[] intervals) {

    /**
     * Constructs a normalized cardinality.
     *
     * @param intervals raw interval set (may overlap or be unordered)
     * @implNote normalization is applied immediately using sweep-line evaluation
     */
    public Cardinality(Interval[] intervals) {
        this.intervals = normalize(intervals);
    }

    // =========================================================
    // BOUND MODEL
    // =========================================================

    /**
     * Inclusivity marker for interval boundaries.
     *
     * <ul>
     *   <li>INCLUSIVE: boundary value is part of the interval</li>
     *   <li>EXCLUSIVE: boundary value is excluded</li>
     * </ul>
     */
    public enum BoundExclusivity {
        INCLUSIVE,
        EXCLUSIVE
    }

    /**
     * Closed-form interval over an ordered domain.
     *
     * <p>Represents a contiguous range [lower, upper] with explicit boundary
     * inclusivity semantics. Used only as a *result artifact* of normalization,
     * not as the primary computation model.</p>
     */
    public record Interval(
        BoundValue lower,
        BoundExclusivity lowerInclusivity,
        BoundValue upper,
        BoundExclusivity upperInclusivity
    ) {}

    // =========================================================
    // BOUND VALUE HIERARCHY
    // =========================================================

    /**
     * Ordered domain value used as interval boundary.
     *
     * <p>Supports comparison across finite values and infinities.
     * Defines total ordering required for sweep-line evaluation.</p>
     */
    public sealed interface BoundValue
            extends Comparable<BoundValue>
            permits FiniteBound, NegativeInfinity, PositiveInfinity {

        int compareTo(BoundValue other);

        /**
         * Returns greater of two boundary values.
         */
        default BoundValue max(BoundValue other) {
            return compareTo(other) >= 0 ? this : other;
        }

        /**
         * Returns smaller of two boundary values.
         */
        default BoundValue min(BoundValue other) {
            return compareTo(other) <= 0 ? this : other;
        }
    }

    /**
     * Finite numeric boundary backed by BigDecimal.
     *
     * <p>Implements value-based ordering consistent with numeric semantics.
     * Trailing zeros are stripped for canonical equality.</p>
     */
    public record FiniteBound(BigDecimal value) implements BoundValue {

        public FiniteBound {
            value = value.stripTrailingZeros();
        }

        @Override
        public int compareTo(BoundValue other) {
            return switch (other) {
                case FiniteBound o -> value.compareTo(o.value);
                case NegativeInfinity _ -> 1;
                case PositiveInfinity _ -> -1;
            };
        }
    }

    /**
     * Negative infinity boundary (lowest possible value).
     *
     * <p>Singleton-like semantic instance representing -∞.</p>
     */
    public static final class NegativeInfinity implements BoundValue {
        private NegativeInfinity() {}

        @Override
        public int compareTo(BoundValue other) {
            return (other == this) ? 0 : -1;
        }
    }

    /**
     * Positive infinity boundary (highest possible value).
     *
     * <p>Singleton-like semantic instance representing +∞.</p>
     */
    public static final class PositiveInfinity implements BoundValue {
        private PositiveInfinity() {}

        @Override
        public int compareTo(BoundValue other) {
            return (other == this) ? 0 : 1;
        }
    }

    /** Global -∞ instance */
    public static final NegativeInfinity NEGATIVE_INFINITY = new NegativeInfinity();

    /** Global +∞ instance */
    public static final PositiveInfinity POSITIVE_INFINITY = new PositiveInfinity();

    // =========================================================
    // EVENT MODEL (SWEEP LINE)
    // =========================================================

    /**
     * Internal sweep-line event representing interval boundary.
     *
     * <p>Intervals are decomposed into START and END events and processed in
     * sorted order to reconstruct canonical disjoint intervals.</p>
     */
    private enum EventType {
        START,
        END
    }

    /**
     * Sweep-line event.
     *
     * @param value boundary value on number line
     * @param type start or end of interval
     * @param exclusivity boundary inclusion semantics
     */
    private record Event(
        BoundValue value,
        EventType type,
        BoundExclusivity exclusivity
    ) {}

    // =========================================================
    // NORMALIZATION (SWEEP-LINE ENGINE)
    // =========================================================

    /**
     * Normalizes interval set into canonical disjoint representation.
     *
     * <p>Algorithm:
     * <ol>
     *   <li>Decompose intervals into boundary events</li>
     *   <li>Sort events by value (START before END on ties)</li>
     *   <li>Perform sweep-line scan tracking active intervals</li>
     *   <li>Emit new interval whenever active region starts/ends</li>
     * </ol>
     *
     * <p>Guarantees:
     * <ul>
     *   <li>output intervals are disjoint</li>
     *   <li>fully ordered by lower bound</li>
     *   <li>minimal representation (no overlaps or adjacency redundancy)</li>
     * </ul>
     *
     * @param input raw intervals (may overlap, unordered)
     * @return canonical normalized interval array
     */
    private static Interval[] normalize(Interval[] input) {
        if (input.length <= 1) return input;

        List<Event> events = new ArrayList<>(input.length * 2);

        for (Interval i : input) {
            events.add(new Event(i.lower(), EventType.START, i.lowerInclusivity()));
            events.add(new Event(i.upper(), EventType.END, i.upperInclusivity()));
        }

        events.sort((a, b) -> {
            int cmp = a.value().compareTo(b.value());
            if (cmp != 0) return cmp;

            // START first ensures correct continuity semantics
            return a.type() == b.type() ? 0
                    : (a.type() == EventType.START ? -1 : 1);
        });

        List<Interval> result = new ArrayList<>();

        BoundValue start = null;
        BoundExclusivity startInc = null;
        int active = 0;

        for (Event e : events) {

            if (e.type() == EventType.START) {

                if (active == 0) {
                    start = e.value();
                    startInc = e.exclusivity();
                }

                active++;
            } else {

                active--;

                if (active == 0) {
                    result.add(new Interval(
                        start,
                        startInc,
                        e.value(),
                        e.exclusivity()
                    ));
                }
            }
        }

        return result.toArray(Interval[]::new);
    }
}