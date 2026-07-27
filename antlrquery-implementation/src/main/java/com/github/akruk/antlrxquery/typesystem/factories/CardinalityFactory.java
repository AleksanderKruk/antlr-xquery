package com.github.akruk.antlrxquery.typesystem.factories;

import java.math.BigInteger;

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
    Cardinality singleNumber(BigInteger value);

    /**
     * Returns a closed interval [lower, upper].
     */
    Cardinality closedRange(BigInteger lower, BigInteger upper);

    /**
     * Returns an open interval (lower, upper).
     */
    Cardinality inclusiveRange(BigInteger lower, BigInteger upper);

    /**
     * Returns a half-open interval (lower, upper].
     */
    Cardinality leftOpenRange(BigInteger lower, BigInteger upper);

    /**
     * Returns a half-open interval [lower, upper).
     */
    Cardinality rightOpenRange(BigInteger lower, BigInteger upper);

    /**
     * Returns the interval [lower, +∞).
     */
    Cardinality minimum(BigInteger lower);

    /**
     * Returns the interval (lower, +∞).
     */
    Cardinality greaterThan(BigInteger lower);

    /**
     * Returns the interval [lower, +∞). Alias for {@link #minimum(BigInteger)}.
     */
    Cardinality greaterOrEqual(BigInteger lower);

    /**
     * Returns the interval (-∞, upper].
     */
    Cardinality maximum(BigInteger upper);

    /**
     * Returns the interval (-∞, upper).
     */
    Cardinality lessThan(BigInteger upper);

    /**
     * Returns the interval (-∞, upper]. Alias for {@link #maximum(BigInteger)}.
     */
    Cardinality lessOrEqual(BigInteger upper);

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