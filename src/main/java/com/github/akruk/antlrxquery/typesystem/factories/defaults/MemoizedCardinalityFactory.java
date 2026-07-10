package com.github.akruk.antlrxquery.typesystem.factories.defaults;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.akruk.antlrxquery.typesystem.factories.CardinalityFactory;
import com.github.akruk.antlrxquery.typesystem.types.Cardinality;
import com.github.akruk.antlrxquery.typesystem.types.Cardinality.CardinalityInterval;
import com.github.akruk.antlrxquery.typesystem.types.Cardinality.CardinalityValue;
import com.github.akruk.antlrxquery.typesystem.types.Cardinality.FiniteBound;

/**
 * Memoized factory for canonical Cardinality instances.
 *
 * <p>Acts as an interning layer for frequently used type constraints.</p>
 *
 * <p>Only structurally stable and commonly reused cardinalities are cached.</p>
 */
public final class MemoizedCardinalityFactory implements CardinalityFactory {

    private final Map<BigDecimal, Cardinality> singleNumberCache = new ConcurrentHashMap<>();

    private final Cardinality EMPTY = Cardinality.of(new CardinalityInterval[0]);

    private final Cardinality ZERO_OR_ONE;
    private final Cardinality ONE_OR_MORE;
    private final Cardinality ZERO_OR_MORE;
    private final Cardinality EXACTLY_ONE;

    private final Map<String, Cardinality> rangeCache = new ConcurrentHashMap<>();

    public MemoizedCardinalityFactory() {

        this.ZERO_OR_ONE = Cardinality.of(new CardinalityInterval[]{
                new CardinalityInterval(
                        new FiniteBound(BigDecimal.ZERO),
                        true,
                        new FiniteBound(BigDecimal.ONE),
                        true
                )
        });

        this.ONE_OR_MORE = Cardinality.of(new CardinalityInterval[]{
                new CardinalityInterval(
                        new FiniteBound(BigDecimal.ONE),
                        true,
                        CardinalityValue.POSITIVE_INFINITY,
                        true
                )
        });

        this.ZERO_OR_MORE = Cardinality.of(new CardinalityInterval[]{
                new CardinalityInterval(
                        new FiniteBound(BigDecimal.ZERO),
                        true,
                        CardinalityValue.POSITIVE_INFINITY,
                        true
                )
        });

        this.EXACTLY_ONE = Cardinality.of(new CardinalityInterval[]{
                new CardinalityInterval(
                        new FiniteBound(BigDecimal.ONE),
                        true,
                        new FiniteBound(BigDecimal.ONE),
                        true
                )
        });
    }

    @Override
    public Cardinality empty() {
        return EMPTY;
    }

    @Override
    public Cardinality singleNumber(BigDecimal value) {
        return singleNumberCache.computeIfAbsent(value.stripTrailingZeros(), v ->
                Cardinality.of(new CardinalityInterval[]{
                        new CardinalityInterval(
                                new FiniteBound(v),
                                true,
                                new FiniteBound(v),
                                true
                        )
                })
        );
    }

    @Override
    public Cardinality closedRange(BigDecimal lower, BigDecimal upper) {
        return rangeKey("CC", lower, upper, () ->
                Cardinality.of(new CardinalityInterval[]{
                        new CardinalityInterval(
                                new FiniteBound(lower),
                                true,
                                new FiniteBound(upper),
                                true
                        )
                })
        );
    }

    @Override
    public Cardinality inclusiveRange(BigDecimal lower, BigDecimal upper) {
        return rangeKey("OO", lower, upper, () ->
                Cardinality.of(new CardinalityInterval[]{
                        new CardinalityInterval(
                                new FiniteBound(lower),
                                false,
                                new FiniteBound(upper),
                                false
                        )
                })
        );
    }

    @Override
    public Cardinality leftOpenRange(BigDecimal lower, BigDecimal upper) {
        return rangeKey("OC", lower, upper, () ->
                Cardinality.of(new CardinalityInterval[]{
                        new CardinalityInterval(
                                new FiniteBound(lower),
                                false,
                                new FiniteBound(upper),
                                true
                        )
                })
        );
    }

    @Override
    public Cardinality rightOpenRange(BigDecimal lower, BigDecimal upper) {
        return rangeKey("CO", lower, upper, () ->
                Cardinality.of(new CardinalityInterval[]{
                        new CardinalityInterval(
                                new FiniteBound(lower),
                                true,
                                new FiniteBound(upper),
                                false
                        )
                })
        );
    }

    @Override
    public Cardinality minimum(BigDecimal lower) {
        return rangeKey("MIN", lower, null, () ->
                Cardinality.of(new CardinalityInterval[]{
                        new CardinalityInterval(
                                new FiniteBound(lower),
                                true,
                                CardinalityValue.POSITIVE_INFINITY,
                                true
                        )
                })
        );
    }

    @Override
    public Cardinality greaterThan(BigDecimal lower) {
        return rangeKey("GT", lower, null, () ->
                Cardinality.of(new CardinalityInterval[]{
                        new CardinalityInterval(
                                new FiniteBound(lower),
                                false,
                                CardinalityValue.POSITIVE_INFINITY,
                                true
                        )
                })
        );
    }

    @Override
    public Cardinality maximum(BigDecimal upper) {
        return rangeKey("MAX", null, upper, () ->
                Cardinality.of(new CardinalityInterval[]{
                        new CardinalityInterval(
                                CardinalityValue.NEGATIVE_INFINITY,
                                true,
                                new FiniteBound(upper),
                                true
                        )
                })
        );
    }

    @Override
    public Cardinality lessThan(BigDecimal upper) {
        return rangeKey("LT", null, upper, () ->
                Cardinality.of(new CardinalityInterval[]{
                        new CardinalityInterval(
                                CardinalityValue.NEGATIVE_INFINITY,
                                true,
                                new FiniteBound(upper),
                                false
                        )
                })
        );
    }

    @Override
    public Cardinality exactlyOne() {
        return EXACTLY_ONE;
    }

    @Override
    public Cardinality oneOrMore() {
        return ONE_OR_MORE;
    }

    @Override
    public Cardinality zeroOrOne() {
        return ZERO_OR_ONE;
    }

    @Override
    public Cardinality zeroOrMore() {
        return ZERO_OR_MORE;
    }

    @Override
    public Cardinality of(CardinalityInterval... intervals) {
        // intentionally NOT cached (combinatorial explosion)
        return Cardinality.of(intervals);
    }

    // =========================================================
    // INTERNAL CACHE KEY
    // =========================================================

    private Cardinality rangeKey(
            String prefix,
            BigDecimal lower,
            BigDecimal upper,
            Supplier<Cardinality> creator
    ) {
        String key = prefix + "|"
                + (lower == null ? "∞" : lower.stripTrailingZeros())
                + "|"
                + (upper == null ? "∞" : upper.stripTrailingZeros());

        return rangeCache.computeIfAbsent(key, k -> creator.get());
    }

    private interface Supplier<T> {
        T get();
    }
}