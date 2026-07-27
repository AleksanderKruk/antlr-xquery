package com.github.akruk.antlrxquery.evaluator.values.operations;

import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.evaluator.XQueryEvaluatorVisitor;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;

public class ValueBooleanOperator {

    private final XQueryValueFactory valueFactory;
    private final EffectiveBooleanValue ebv;
    private final XQueryEvaluatorVisitor evaluator;

    public ValueBooleanOperator(final XQueryEvaluatorVisitor evaluator, final XQueryValueFactory valueFactory, final EffectiveBooleanValue ebv) {
        this.evaluator = evaluator;
        this.valueFactory = valueFactory;
        this.ebv = ebv;
    }


    public XQueryValue or(final List<? extends ParseTree> operands) {
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

    public XQueryValue and(final List<? extends ParseTree> operands) {
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
