package com.github.akruk.antlrxquery.values;

import java.math.BigDecimal;
import java.math.MathContext;

import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;

public class XQueryNumber extends XQueryValueBase<BigDecimal> {
    public static final XQueryNumber ZERO = new XQueryNumber(BigDecimal.ZERO);
    public static final XQueryNumber ONE = new XQueryNumber(BigDecimal.ONE);


    public XQueryNumber(BigDecimal n) {
        value = n;
    }

    public XQueryNumber(int i) {
        value = new BigDecimal(i);
    }

    @Override
    public BigDecimal numericValue() {
        return value;
    }

    @Override
    public XQueryValue add(XQueryValue other) throws XQueryUnsupportedOperation {
        return new XQueryNumber(value.add(other.numericValue(), MathContext.UNLIMITED));
    }

    @Override
    public XQueryValue subtract(XQueryValue other) throws XQueryUnsupportedOperation {
        return new XQueryNumber(value.subtract(other.numericValue(), MathContext.UNLIMITED));
    }

    @Override
    public XQueryValue multiply(XQueryValue other) throws XQueryUnsupportedOperation {
        return new XQueryNumber(value.multiply(other.numericValue(), MathContext.UNLIMITED));
    }

    @Override
    public XQueryValue divide(XQueryValue other) throws XQueryUnsupportedOperation {
        return new XQueryNumber(value.divide(other.numericValue(), MathContext.UNLIMITED));
    }

    @Override
    public XQueryValue integerDivide(XQueryValue other) throws XQueryUnsupportedOperation {
        return new XQueryNumber(value.divideToIntegralValue(other.numericValue()));
    }

    @Override
    public XQueryValue modulus(XQueryValue other) throws XQueryUnsupportedOperation {
        return new XQueryNumber(value.remainder(other.numericValue()));
    }

    @Override
    public XQueryValue valueEqual(XQueryValue other) throws XQueryUnsupportedOperation {
        return XQueryBoolean.of(value.compareTo(other.numericValue()) == 0);
    }

    @Override
    public XQueryValue valueLessThan(XQueryValue other) throws XQueryUnsupportedOperation {
        return XQueryBoolean.of(value.compareTo(other.numericValue()) == -1);
    }


    @Override
    public XQueryValue copy() {
        return new XQueryNumber(value.plus());
    }

}
