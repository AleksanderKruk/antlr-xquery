package com.github.akruk.antlrxquery.evaluator.values.operations;

import java.util.ArrayList;
import java.util.List;

import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;

public class NodeOperator {

    private final XQueryValueFactory valueFactory;

    public NodeOperator(final XQueryValueFactory valueFactory) {
        this.valueFactory = valueFactory;
    }


    public XQueryValue union(final List<XQueryValue> operands) {
        for (final var operand : operands) {
            if (operand.isError)
                return operand;
        }
        final var unionized = operands.stream().flatMap((final XQueryValue op)->op.sequence.stream()).toList();
        return valueFactory.sequence(unionized);
    }

    public XQueryValue intersect(final List<XQueryValue> operands) {
        for (final var operand : operands) {
            if (operand.isError)
                return operand;
        }
        if (operands.size() == 0)
            return valueFactory.emptySequence();
        final var result = new ArrayList<>(operands.get(0).sequence);
        for (final var operand: operands.subList(1, operands.size())) {
            if (result.size() == 0)
                break;
            result.retainAll(operand.sequence);
        }
        return valueFactory.sequence(result);
    }

    public XQueryValue except(final List<XQueryValue> operands) {
        for (final var operand : operands) {
            if (operand.isError)
                return operand;
        }
        if (operands.size() == 0)
            return valueFactory.emptySequence();
        final var result = new ArrayList<>(operands.get(0).sequence);
        for (final var operand: operands.subList(1, operands.size())) {
            if (result.size() == 0)
                break;
            result.removeAll(operand.sequence);
        }
        return valueFactory.sequence(result);
    }

}
