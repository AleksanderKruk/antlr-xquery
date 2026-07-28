package com.github.akruk.antlrquery.evaluator.values.operations;

import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrquery.evaluator.AntlrQueryEvaluator;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;
import com.github.akruk.antlrquery.evaluator.values.factories.AntlrQueryValueFactory;

public class ValueBooleanOperator {

    private final AntlrQueryValueFactory valueFactory;
    private final EffectiveBooleanValue ebv;
    private final AntlrQueryEvaluator evaluator;

    public ValueBooleanOperator(final AntlrQueryEvaluator evaluator, final AntlrQueryValueFactory valueFactory, final EffectiveBooleanValue ebv) {
        this.evaluator = evaluator;
        this.valueFactory = valueFactory;
        this.ebv = ebv;
    }


    public AntlrQueryValue or(final List<? extends ParseTree> operands) {
        boolean result = false;
        for (final var operand : operands) {
            final var evaluatedOperand = operand.accept(evaluator);
            final var effectiveBooleanValue = ebv.effectiveBooleanValue(evaluatedOperand);
            if (effectiveBooleanValue.isError)
                return effectiveBooleanValue;
            result = result || effectiveBooleanValue.booleanValue;
        }
        return valueFactory.bool(result);
    }

    public AntlrQueryValue and(final List<? extends ParseTree> operands) {
        boolean result = true;
        for (final var operand : operands) {
            final var evaluatedOperand = operand.accept(evaluator);
            final var effectiveBooleanValue = ebv.effectiveBooleanValue(evaluatedOperand);
            if (effectiveBooleanValue.isError)
                return effectiveBooleanValue;
            result = result && effectiveBooleanValue.booleanValue;
        }
        return valueFactory.bool(result);
    }

}
