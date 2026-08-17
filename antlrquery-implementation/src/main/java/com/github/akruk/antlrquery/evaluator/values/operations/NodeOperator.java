package com.github.akruk.antlrquery.evaluator.values.operations;

import java.util.ArrayList;
import java.util.List;

import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;
import com.github.akruk.antlrquery.evaluator.values.factories.AntlrQueryValueFactory;

public class NodeOperator {

    private final AntlrQueryValueFactory valueFactory;

    public NodeOperator(final AntlrQueryValueFactory valueFactory) {
        this.valueFactory = valueFactory;
    }


    public AntlrQueryValue union(final List<AntlrQueryValue> operands) {
        for (final var operand : operands) {
            if (operand.isError)
                return operand;
        }
        final var unionized = operands.stream().flatMap((final AntlrQueryValue op)->op.sequence.stream()).toList();
        return valueFactory.sequence(unionized);
    }

    public AntlrQueryValue intersect(final List<AntlrQueryValue> operands) {
        for (final var operand : operands) {
            if (operand.isError)
                return operand;
        }
        if (operands.isEmpty())
            return valueFactory.emptySequence();
        final var result = new ArrayList<>(operands.get(0).sequence);
        for (final var operand: operands.subList(1, operands.size())) {
            if (result.isEmpty())
                break;
            result.retainAll(operand.sequence);
        }
        return valueFactory.sequence(result);
    }

    public AntlrQueryValue except(final List<AntlrQueryValue> operands) {
        for (final var operand : operands) {
            if (operand.isError)
                return operand;
        }
        if (operands.isEmpty())
            return valueFactory.emptySequence();
        final var result = new ArrayList<>(operands.get(0).sequence);
        for (final var operand: operands.subList(1, operands.size())) {
            if (result.isEmpty())
                break;
            result.removeAll(operand.sequence);
        }
        return valueFactory.sequence(result);
    }

}
