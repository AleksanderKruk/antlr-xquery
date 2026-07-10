package com.github.akruk.antlrxquery.typesystem.factories;

import java.math.BigDecimal;

import com.github.akruk.antlrxquery.typesystem.types.Cardinality;
import com.github.akruk.antlrxquery.typesystem.types.Cardinality.CardinalityInterval;

/** 
 *  Factory responsible for creating {@link Cardinality} instances
*/
public interface CardinalityFactory {

    /**
     * Returns the empty cardinality.
     */
    Cardinality empty();

    /**
     * Returns a cardinality containing exactly one value.
     */
    Cardinality singleNumber(BigDecimal value);

    /**
     * Returns a closed interval [lower, upper].
     */
    Cardinality closedRange(BigDecimal lower, BigDecimal upper);

    /**
     * Returns an open interval (lower, upper).
     */
    Cardinality inclusiveRange(BigDecimal lower, BigDecimal upper);

    /**
     * Returns a half-open interval (lower, upper].
     */
    Cardinality leftOpenRange(BigDecimal lower, BigDecimal upper);

    /**
     * Returns a half-open interval [lower, upper).
     */
    Cardinality rightOpenRange(BigDecimal lower, BigDecimal upper);

    /**
     * Returns the interval [lower, +∞).
     */
    Cardinality minimum(BigDecimal lower);

    /**
     * Returns the interval (lower, +∞).
     */
    Cardinality greaterThan(BigDecimal lower);

    /**
     * Returns the interval [lower, +∞). Alias for {@link #minimum(BigDecimal)}.
     */
    Cardinality greaterOrEqual(BigDecimal lower);

    /**
     * Returns the interval (-∞, upper].
     */
    Cardinality maximum(BigDecimal upper);

    /**
     * Returns the interval (-∞, upper).
     */
    Cardinality lessThan(BigDecimal upper);

    /**
     * Returns the interval (-∞, upper]. Alias for {@link #maximum(BigDecimal)}.
     */
    Cardinality lessOrEqual(BigDecimal upper);

    /**
     * Returns a cardinality representing exactly one occurrence.
     */
    Cardinality exactlyOne();

    /**
     * Returns a cardinality representing one or more occurrences.
     */
    Cardinality oneOrMore();

    /**
     * Returns a cardinality representing zero or one occurrence.
     */
    Cardinality zeroOrOne();

    /**
     * Returns a cardinality representing zero or more occurrences.
     */
    Cardinality zeroOrMore();

    /**
     * Creates a cardinality from the specified intervals.
     */
    Cardinality of(CardinalityInterval... intervals);
}