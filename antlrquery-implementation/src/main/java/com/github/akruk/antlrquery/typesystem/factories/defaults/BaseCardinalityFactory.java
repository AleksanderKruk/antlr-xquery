package com.github.akruk.antlrquery.typesystem.factories.defaults;

import com.github.akruk.antlrquery.typesystem.factories.CardinalityFactory;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;

import java.math.BigInteger;


// TODO: implement
public class BaseCardinalityFactory implements CardinalityFactory {

    @Override
    public Cardinality empty() {
        return null;
    }

    @Override
    public Cardinality singleNumber(BigInteger value) {
        return null;
    }

    @Override
    public Cardinality closedRange(BigInteger lower, BigInteger upper) {
        return null;
    }

    @Override
    public Cardinality inclusiveRange(BigInteger lower, BigInteger upper) {
        return null;
    }

    @Override
    public Cardinality leftOpenRange(BigInteger lower, BigInteger upper) {
        return null;
    }

    @Override
    public Cardinality rightOpenRange(BigInteger lower, BigInteger upper) {
        return null;
    }

    @Override
    public Cardinality minimum(BigInteger lower) {
        return null;
    }

    @Override
    public Cardinality greaterThan(BigInteger lower) {
        return null;
    }

    @Override
    public Cardinality greaterOrEqual(BigInteger lower) {
        return null;
    }

    @Override
    public Cardinality maximum(BigInteger upper) {
        return null;
    }

    @Override
    public Cardinality lessThan(BigInteger upper) {
        return null;
    }

    @Override
    public Cardinality lessOrEqual(BigInteger upper) {
        return null;
    }

    @Override
    public Cardinality exactlyOne() {
        return null;
    }

    @Override
    public Cardinality oneOrMore() {
        return null;
    }

    @Override
    public Cardinality zeroOrOne() {
        return null;
    }

    @Override
    public Cardinality zeroOrMore() {
        return null;
    }

    @Override
    public Cardinality of(Cardinality.CardinalityInterval... intervals) {
        return null;
    }
}
