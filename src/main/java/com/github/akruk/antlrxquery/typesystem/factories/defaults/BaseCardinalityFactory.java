package com.github.akruk.antlrxquery.typesystem.factories.defaults;

import java.math.BigDecimal;

import com.github.akruk.antlrxquery.typesystem.factories.CardinalityFactory;
import com.github.akruk.antlrxquery.typesystem.types.Cardinality;
import com.github.akruk.antlrxquery.typesystem.types.Cardinality.CardinalityInterval;

public class BaseCardinalityFactory implements CardinalityFactory {

    @Override
    public Cardinality empty() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'empty'");
    }

    @Override
    public Cardinality singleNumber(BigDecimal value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'singleNumber'");
    }

    @Override
    public Cardinality closedRange(BigDecimal lower, BigDecimal upper) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'closedRange'");
    }

    @Override
    public Cardinality inclusiveRange(BigDecimal lower, BigDecimal upper) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'inclusiveRange'");
    }

    @Override
    public Cardinality leftOpenRange(BigDecimal lower, BigDecimal upper) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'leftOpenRange'");
    }

    @Override
    public Cardinality rightOpenRange(BigDecimal lower, BigDecimal upper) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'rightOpenRange'");
    }

    @Override
    public Cardinality minimum(BigDecimal lower) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'minimum'");
    }

    @Override
    public Cardinality greaterThan(BigDecimal lower) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'greaterThan'");
    }

    @Override
    public Cardinality greaterOrEqual(BigDecimal lower) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'greaterOrEqual'");
    }

    @Override
    public Cardinality maximum(BigDecimal upper) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'maximum'");
    }

    @Override
    public Cardinality lessThan(BigDecimal upper) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'lessThan'");
    }

    @Override
    public Cardinality lessOrEqual(BigDecimal upper) {
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
