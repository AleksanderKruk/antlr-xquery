package com.github.akruk.antlrquery.evaluator.values.operations;

import java.util.List;

import com.github.akruk.antlrquery.evaluator.values.AntlrQueryError;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;
import com.github.akruk.antlrquery.evaluator.values.factories.AntlrQueryValueFactory;

public class ValueComparisonOperator {

    private final AntlrQueryValueFactory valueFactory;
    private static final ValueAtomizer atomizer = new ValueAtomizer();

    public ValueComparisonOperator(final AntlrQueryValueFactory valueFactory) {
        this.valueFactory = valueFactory;
    }

    private AntlrQueryValue validateOperand(
            final AntlrQueryValue operand,
            final List<AntlrQueryValue> atomized) {

        // If an atomized operand is a sequence of cardinality greater than one,
        // a type error is raised [err:XPTY0004].
        if (atomized.size() > 1) {
            return valueFactory.error(
                    AntlrQueryError.InvalidArgumentType,
                    "Atomized operand " + operand
                            + " is a sequence of cardinality greater than one");
        }

        if (operand.isError)
            return operand;

        if (operand.isNode) {
            return valueFactory.error(
                    AntlrQueryError.InvalidArgumentType,
                    "Operand: " + operand
                            + " is a node, which cannot be compared using value comparison");
        }

        if (operand.isMap) {
            return valueFactory.error(
                    AntlrQueryError.InvalidArgumentType,
                    "Operand: " + operand
                            + " is a map, which cannot be compared using value comparison");
        }

        if (operand.isArray) {
            return valueFactory.error(
                    AntlrQueryError.InvalidArgumentType,
                    "Operand: " + operand
                            + " is an array, which cannot be compared using value comparison");
        }

        return null;
    }

    /**
     * Compares two values and returns:
     *
     * <ul>
     *     <li>{@code -1} if {@code o1 < o2}</li>
     *     <li>{@code  0} if {@code o1 == o2}</li>
     *     <li>{@code  1} if {@code o1 > o2}</li>
     * </ul>
     *
     * An empty sequence is returned as-is, as required by value comparison.
     * Errors are also returned as-is.
     */
    public AntlrQueryValue valueCompare(
            final AntlrQueryValue o1,
            final AntlrQueryValue o2) {

        final List<AntlrQueryValue> atomized1 = atomizer.atomize(o1);
        final List<AntlrQueryValue> atomized2 = atomizer.atomize(o2);

        if (atomized1.isEmpty())
            return o1;

        if (atomized2.isEmpty())
            return o2;

        final var err1 = validateOperand(o1, atomized1);
        if (err1 != null)
            return err1;

        final var err2 = validateOperand(o2, atomized2);
        if (err2 != null)
            return err2;

        final int comparison;

        if (o1.isBoolean && o2.isBoolean) {
            comparison = o1.booleanValue.compareTo(o2.booleanValue);
        } else if (o1.isNumeric && o2.isNumeric) {
            comparison = o1.numericValue.compareTo(o2.numericValue);
        } else {
            comparison = o1.stringValue.compareTo(o2.stringValue);
        }

        return valueFactory.number(comparison);
    }

    public AntlrQueryValue valueEquals(
            final AntlrQueryValue o1,
            final AntlrQueryValue o2) {

        final AntlrQueryValue comparison = valueCompare(o1, o2);

        if (comparison.isError || comparison.isEmptySequence)
            return comparison;

        return valueFactory.bool(comparison.numericValue.intValue() == 0);
    }

    public AntlrQueryValue valueUnequal(
            final AntlrQueryValue o1,
            final AntlrQueryValue o2) {

        final AntlrQueryValue comparison = valueCompare(o1, o2);

        if (comparison.isError || comparison.isEmptySequence)
            return comparison;

        return valueFactory.bool(comparison.numericValue.intValue() != 0);
    }

    public AntlrQueryValue valueLessThan(
            final AntlrQueryValue o1,
            final AntlrQueryValue o2) {

        final AntlrQueryValue comparison = valueCompare(o1, o2);

        if (comparison.isError || comparison.isEmptySequence)
            return comparison;

        return valueFactory.bool(comparison.numericValue.intValue() < 0);
    }

    public AntlrQueryValue valueGreaterThan(
            final AntlrQueryValue o1,
            final AntlrQueryValue o2) {

        final AntlrQueryValue comparison = valueCompare(o1, o2);

        if (comparison.isError || comparison.isEmptySequence)
            return comparison;

        return valueFactory.bool(comparison.numericValue.intValue() > 0);
    }

    public AntlrQueryValue valueLessEqual(
            final AntlrQueryValue o1,
            final AntlrQueryValue o2) {

        final AntlrQueryValue comparison = valueCompare(o1, o2);

        if (comparison.isError || comparison.isEmptySequence)
            return comparison;

        return valueFactory.bool(comparison.numericValue.intValue() <= 0);
    }

    public AntlrQueryValue valueGreaterEqual(
            final AntlrQueryValue o1,
            final AntlrQueryValue o2) {

        final AntlrQueryValue comparison = valueCompare(o1, o2);

        if (comparison.isError || comparison.isEmptySequence)
            return comparison;

        return valueFactory.bool(comparison.numericValue.intValue() >= 0);
    }
}
