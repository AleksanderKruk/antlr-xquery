package com.github.akruk.antlrxquery.evaluator.values.operations;

import java.math.BigDecimal;

import com.github.akruk.antlrxquery.evaluator.values.XQueryError;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;

public class EffectiveBooleanValue {

    private final XQueryValueFactory valueFactory;
    private final XQueryValue true_;
    private final XQueryValue false_;

    public EffectiveBooleanValue(final XQueryValueFactory valueFactory) {
        this.valueFactory = valueFactory;
        true_ = valueFactory.bool(true);
        false_ = valueFactory.bool(false);
    }

    public XQueryValue effectiveBooleanValue(final XQueryValue value) {
        if (value.isEmptySequence) {
            return false_;
        }
        if (value.sequence.get(0).isNode)
            return true_;
        if (value.size != 1)
            return valueFactory.error(
                XQueryError.InvalidArgumentType,
                "Sequence: " + value + " of type " + value.type + " does not have an effective boolean value");
        if (value.isBoolean)
            return value;
        if (value.isString)
            return valueFactory.bool(!value.stringValue.isEmpty());
        if (value.isNumeric) {
            final boolean ebf = value.numericValue.compareTo(BigDecimal.ZERO) != 0;
            return valueFactory.bool(ebf);
        }
        return valueFactory.error(
            XQueryError.InvalidArgumentType,
            "Value: " + value + " of type " + value.type + " does not have an effective boolean value");

    }

}
