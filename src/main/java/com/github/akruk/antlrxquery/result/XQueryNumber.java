package com.github.akruk.antlrxquery.result;

import java.math.BigDecimal;

import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;

public class XQueryNumber extends XQueryValueBase<BigDecimal> {
    public XQueryNumber(BigDecimal n) {
        value = n;
    }

    @Override
    public BigDecimal numericValue() {
        return value;
    }

    @Override
    public XQueryValue add(XQueryValue other) throws XQueryUnsupportedOperation {
        return new XQueryNumber(value.add(other.numericValue()));
    }

    @Override
    public XQueryValue subtract(XQueryValue other) throws XQueryUnsupportedOperation {
        return new XQueryNumber(value.subtract(other.numericValue()));
    }

    @Override
    public XQueryValue multiply(XQueryValue other) throws XQueryUnsupportedOperation {
        return new XQueryNumber(value.multiply(other.numericValue()));
    }

    @Override
    public XQueryValue divide(XQueryValue other) throws XQueryUnsupportedOperation {
        return new XQueryNumber(value.divide(other.numericValue()));
    }

    @Override
    public XQueryValue valueEqual(XQueryValue other) throws XQueryUnsupportedOperation {
        return new XQueryBoolean(value.compareTo(other.numericValue()) == 0);
    }

    @Override
    public XQueryValue valueLessThan(XQueryValue other) throws XQueryUnsupportedOperation {
        return new XQueryBoolean(value.compareTo(other.numericValue()) == -1);
    }

}
