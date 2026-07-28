package com.github.akruk.antlrquery.evaluator.values.operations;

import java.math.BigDecimal;

import com.github.akruk.antlrquery.evaluator.values.AntlrQueryError;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;
import com.github.akruk.antlrquery.evaluator.values.factories.AntlrQueryValueFactory;

public class EffectiveBooleanValue {

    private final AntlrQueryValueFactory valueFactory;
    private final AntlrQueryValue true_;
    private final AntlrQueryValue false_;

    public EffectiveBooleanValue(final AntlrQueryValueFactory valueFactory) {
        this.valueFactory = valueFactory;
        true_ = valueFactory.bool(true);
        false_ = valueFactory.bool(false);
    }

    public AntlrQueryValue effectiveBooleanValue(final AntlrQueryValue value) {
        if (value.isEmptySequence) {
            return false_;
        }
        if (value.sequence.get(0).isNode)
            return true_;
        if (value.size != 1)
            return valueFactory.error(
                AntlrQueryError.InvalidArgumentType,
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
            AntlrQueryError.InvalidArgumentType,
            "Value: " + value + " of type " + value.type + " does not have an effective boolean value");

    }

}
