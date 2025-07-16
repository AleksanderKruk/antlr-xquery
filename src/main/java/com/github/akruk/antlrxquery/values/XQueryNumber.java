package com.github.akruk.antlrxquery.values;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

public class XQueryNumber extends XQueryValueBase<BigDecimal> {

    public XQueryNumber(final BigDecimal n, final XQueryValueFactory valueFactory) {
        super(n, valueFactory);
    }

    public XQueryNumber(final int i, final XQueryValueFactory valueFactory) {
        super(BigDecimal.valueOf(i), valueFactory);
    }

    @Override
    public BigDecimal numericValue() {
        return value;
    }

    @Override
    public String stringValue() {
        return value.toPlainString();
    }

    @Override
    public Boolean effectiveBooleanValue() {
        return !value.equals(BigDecimal.ZERO);
    }

    @Override
    public XQueryValue add(final XQueryValue other) {
        if (!other.isNumericValue()) return XQueryError.InvalidArgumentType;
        try {
            return valueFactory.number(value.add(other.numericValue(), MathContext.UNLIMITED));
        } catch (ArithmeticException e) {
            return XQueryError.NumericOverflowUnderflow;
        }
    }

    @Override
    public XQueryValue subtract(final XQueryValue other) {
        if (!other.isNumericValue()) return XQueryError.InvalidArgumentType;
        try {
            return valueFactory.number(value.subtract(other.numericValue(), MathContext.UNLIMITED));
        } catch (ArithmeticException e) {
            return XQueryError.NumericOverflowUnderflow;
        }
    }

    @Override
    public XQueryValue multiply(final XQueryValue other) {
        if (!other.isNumericValue()) return XQueryError.InvalidArgumentType;
        try {
            return valueFactory.number(value.multiply(other.numericValue(), MathContext.UNLIMITED));
        } catch (ArithmeticException e) {
            return XQueryError.NumericOverflowUnderflow;
        }
    }

    @Override
    public XQueryValue divide(final XQueryValue other) {
        if (!other.isNumericValue()) return XQueryError.InvalidArgumentType;
        BigDecimal divisor = other.numericValue();
        if (divisor.compareTo(BigDecimal.ZERO) == 0) return XQueryError.DivisionByZero;
        try {
            return valueFactory.number(value.divide(divisor, MathContext.UNLIMITED));
        } catch (ArithmeticException e) {
            return XQueryError.NumericOverflowUnderflow;
        }
    }

    @Override
    public XQueryValue integerDivide(final XQueryValue other) {
        if (!other.isNumericValue()) return XQueryError.InvalidArgumentType;
        BigDecimal divisor = other.numericValue();
        if (divisor.compareTo(BigDecimal.ZERO) == 0) return XQueryError.DivisionByZero;
        try {
            return valueFactory.number(value.divideToIntegralValue(divisor));
        } catch (ArithmeticException e) {
            return XQueryError.NumericOverflowUnderflow;
        }
    }

    @Override
    public XQueryValue modulus(final XQueryValue other) {
        if (!other.isNumericValue()) return XQueryError.InvalidArgumentType;
        try {
            return valueFactory.number(value.remainder(other.numericValue()));
        } catch (ArithmeticException e) {
            return XQueryError.NumericOverflowUnderflow;
        }
    }

    @Override
    public XQueryValue valueEqual(final XQueryValue other) {
        if (!other.isNumericValue()) return valueFactory.bool(false);
        return valueFactory.bool(value.compareTo(other.numericValue()) == 0);
    }

    @Override
    public XQueryValue valueLessThan(final XQueryValue other) {
        if (!other.isNumericValue()) return valueFactory.bool(false);
        return valueFactory.bool(value.compareTo(other.numericValue()) < 0);
    }
    @Override
    public XQueryValue data() {
        return valueFactory.sequence(List.of(this));
    }

    @Override
    public XQueryValue empty() {
        return valueFactory.bool(false);
    }

    @Override
    public String toString() {
        return "<XQueryNumber:" + value + "/>";
    }
}
