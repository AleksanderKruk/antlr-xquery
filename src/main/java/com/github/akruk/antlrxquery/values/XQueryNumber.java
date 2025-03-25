package com.github.akruk.antlrxquery.values;

import java.math.BigDecimal;
import java.math.MathContext;

import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

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
    public String stringValue() {
        return value.toString();
    }


    @Override
    public Boolean effectiveBooleanValue() {
        return value.equals(BigDecimal.ZERO);
    }

    @Override
    public XQueryValue add(XQueryValueFactory valueFactory, XQueryValue other) throws XQueryUnsupportedOperation {
        return valueFactory.number(value.add(other.numericValue(), MathContext.UNLIMITED));
    }

    @Override
    public XQueryValue subtract(XQueryValueFactory valueFactory, XQueryValue other) throws XQueryUnsupportedOperation {
        return valueFactory.number(value.subtract(other.numericValue(), MathContext.UNLIMITED));
    }

    @Override
    public XQueryValue multiply(XQueryValueFactory valueFactory, XQueryValue other) throws XQueryUnsupportedOperation {
        return valueFactory.number(value.multiply(other.numericValue(), MathContext.UNLIMITED));
    }

    @Override
    public XQueryValue divide(XQueryValueFactory valueFactory, XQueryValue other) throws XQueryUnsupportedOperation {
        return valueFactory.number(value.divide(other.numericValue(), MathContext.UNLIMITED));
    }

    @Override
    public XQueryValue integerDivide(XQueryValueFactory valueFactory, XQueryValue other) throws XQueryUnsupportedOperation {
        return valueFactory.number(value.divideToIntegralValue(other.numericValue()));
    }

    @Override
    public XQueryValue modulus(XQueryValueFactory valueFactory, XQueryValue other) throws XQueryUnsupportedOperation {
        return valueFactory.number(value.remainder(other.numericValue()));
    }

    @Override
    public XQueryValue valueEqual(XQueryValueFactory valueFactory, XQueryValue other) {
        if (!other.isNumericValue())
            return XQueryBoolean.FALSE;
        return XQueryBoolean.of(value.compareTo(other.numericValue()) == 0);
    }

    @Override
    public XQueryValue valueLessThan(XQueryValueFactory valueFactory, XQueryValue other) {
        if (!other.isNumericValue())
            return XQueryBoolean.FALSE;
        return XQueryBoolean.of(value.compareTo(other.numericValue()) == -1);
    }

    @Override
    public XQueryValue copy(XQueryValueFactory valueFactory) {
        return valueFactory.number(value);
    }

    @Override
    public XQueryValue data(XQueryValueFactory valueFactory) throws XQueryUnsupportedOperation {
        var atomized = atomize();
        return valueFactory.sequence(atomized);
    }

    @Override
    public XQueryValue empty(XQueryValueFactory valueFactory) {
        return valueFactory.bool(false);
    }
}
