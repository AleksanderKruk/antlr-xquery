package com.github.akruk.antlrxquery.values;

import java.math.BigDecimal;
import java.math.MathContext;

import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

public class XQueryNumber extends XQueryValueBase<BigDecimal> {
    public XQueryNumber(final BigDecimal n, final XQueryValueFactory valueFactory) {
        super(n, valueFactory);
    }

    public XQueryNumber(final int i, final XQueryValueFactory valueFactory) {
        super(new BigDecimal(i), valueFactory);
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
    public XQueryValue add(final XQueryValue other) throws XQueryUnsupportedOperation {
        return valueFactory.number(value.add(other.numericValue(), MathContext.UNLIMITED));
    }

    @Override
    public XQueryValue subtract(final XQueryValue other) throws XQueryUnsupportedOperation {
        return valueFactory.number(value.subtract(other.numericValue(), MathContext.UNLIMITED));
    }

    @Override
    public XQueryValue multiply(final XQueryValue other) throws XQueryUnsupportedOperation {
        return valueFactory.number(value.multiply(other.numericValue(), MathContext.UNLIMITED));
    }

    @Override
    public XQueryValue divide(final XQueryValue other) throws XQueryUnsupportedOperation {
        return valueFactory.number(value.divide(other.numericValue(), MathContext.UNLIMITED));
    }

    @Override
    public XQueryValue integerDivide(final XQueryValue other) throws XQueryUnsupportedOperation {
        return valueFactory.number(value.divideToIntegralValue(other.numericValue()));
    }

    @Override
    public XQueryValue modulus(final XQueryValue other) throws XQueryUnsupportedOperation {
        return valueFactory.number(value.remainder(other.numericValue()));
    }

    @Override
    public XQueryValue valueEqual(final XQueryValue other) {
        if (!other.isNumericValue())
            return valueFactory.bool(false);
        return valueFactory.bool(value.compareTo(other.numericValue()) == 0);
    }

    @Override
    public XQueryValue valueLessThan(final XQueryValue other) {
        if (!other.isNumericValue())
            return valueFactory.bool(false);
        return valueFactory.bool(value.compareTo(other.numericValue()) == -1);
    }

    @Override
    public XQueryValue copy() {
        return valueFactory.number(value);
    }

    @Override
    public XQueryValue data() throws XQueryUnsupportedOperation {
        final var atomized = atomize();
        return valueFactory.sequence(atomized);
    }

    @Override
    public XQueryValue empty() {
        return valueFactory.bool(false);
    }
}
