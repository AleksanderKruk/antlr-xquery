package com.github.akruk.antlrxquery.evaluator.values.operations;

import java.math.BigDecimal;

import com.github.akruk.antlrxquery.evaluator.values.XQueryError;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;

public class EffectiveBooleanValue {

    private final XQueryValueFactory valueFactory;
    private final XQueryValue false_;

    public EffectiveBooleanValue(XQueryValueFactory valueFactory) {
        this.valueFactory = valueFactory;
        false_ = valueFactory.bool(false);
    }

    public XQueryValue effectiveBooleanValue(XQueryValue value) {
        if (value.isEmptySequence) {
            return false_;
        }
        if (value.size != 1)
            return valueFactory.error(XQueryError.InvalidArgumentType, "Sequence: " + value + " of type " + value.type + " does not have an effective boolean value");
        if (!(value.isString || value.isBoolean || value.isNumeric))
            return valueFactory.error(XQueryError.InvalidArgumentType,
                "Value: " + value + " of type " + value.type + " does not have an effective boolean value");

        return valueFactory.bool(effectiveBooleanValue_(value));

    }

    public boolean effectiveBooleanValue_(XQueryValue value) {
        if (value.isEmptySequence) {
            return false;
        }
        if (value.isString)
            return !value.stringValue.isEmpty();
        if (value.isBoolean)
            return value.booleanValue;
        if (value.isNumeric) {
            boolean ebf = value.numericValue.compareTo(BigDecimal.ZERO) != 0;
            return ebf;
        }
        return false;
    }
}
