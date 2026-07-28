package com.github.akruk.antlrquery.typesystem.factories.defaults;

import java.math.BigInteger;

import com.github.akruk.antlrquery.typesystem.factories.CardinalityFactory;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.Cardinality.CardinalityInterval;

public class BaseCardinalityFactory implements CardinalityFactory {

    @Override
    public Cardinality empty() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'empty'");
    }

    @Override
    public Cardinality singleNumber(BigInteger value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'singleNumber'");
    }

    @Override
    public Cardinality closedRange(BigInteger lower, BigInteger upper) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'closedRange'");
    }

    @Override
    public Cardinality inclusiveRange(BigInteger lower, BigInteger upper) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'inclusiveRange'");
    }

    @Override
    public Cardinality leftOpenRange(BigInteger lower, BigInteger upper) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'leftOpenRange'");
    }

    @Override
    public Cardinality rightOpenRange(BigInteger lower, BigInteger upper) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'rightOpenRange'");
    }

    @Override
    public Cardinality minimum(BigInteger lower) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'minimum'");
    }

    @Override
    public Cardinality greaterThan(BigInteger lower) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'greaterThan'");
    }

    @Override
    public Cardinality greaterOrEqual(BigInteger lower) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'greaterOrEqual'");
    }

    @Override
    public Cardinality maximum(BigInteger upper) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'maximum'");
    }

    @Override
    public Cardinality lessThan(BigInteger upper) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'lessThan'");
    }

    @Override
    public Cardinality lessOrEqual(BigInteger upper) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'lessOrEqual'");
    }

    @Override
    public Cardinality exactlyOne() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'exactlyOne'");
    }

    @Override
    public Cardinality oneOrMore() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'oneOrMore'");
    }

    @Override
    public Cardinality zeroOrOne() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'zeroOrOne'");
    }

    @Override
    public Cardinality zeroOrMore() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'zeroOrMore'");
    }

    @Override
    public Cardinality of(CardinalityInterval... intervals) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'of'");
    }
    
}
