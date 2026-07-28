package com.github.akruk.antlrquery.evaluator.values.operations;

import java.util.List;

import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;
import com.github.akruk.antlrquery.evaluator.values.factories.AntlrQueryValueFactory;

public class GeneralComparisonOperator {

    private final AntlrQueryValueFactory valueFactory;
    private final ValueAtomizer atomizer;
    private final ValueComparisonOperator valueComparisonOperator;

    public GeneralComparisonOperator(
        final AntlrQueryValueFactory valueFactory,
        final ValueAtomizer atomizer,
        final ValueComparisonOperator valueComparisonOperator)
    {
        this.valueFactory = valueFactory;
        this.atomizer = atomizer;
        this.valueComparisonOperator = valueComparisonOperator;
    }

    public AntlrQueryValue generalEquals(
        final AntlrQueryValue o1,
        final AntlrQueryValue o2)
    {
        final List<AntlrQueryValue> atomized1 = atomizer.atomize(o1);
        final List<AntlrQueryValue> atomized2 = atomizer.atomize(o2);

        for (final var element1 : atomized1) {
            for (final var element2 : atomized2) {
                final var comparison = valueComparisonOperator.valueEquals(element1, element2);
                if (comparison.isError || comparison.booleanValue)
                    return comparison;
            }
        }
        return valueFactory.bool(atomized1.size() == 0 && atomized2.size() == 0);
    }

    public AntlrQueryValue generalUnequals(
        final AntlrQueryValue o1,
        final AntlrQueryValue o2)
    {
        final List<AntlrQueryValue> atomized1 = atomizer.atomize(o1);
        final List<AntlrQueryValue> atomized2 = atomizer.atomize(o2);

        for (final var element1 : atomized1) {
            for (final var element2 : atomized2) {
                final var comparison = valueComparisonOperator.valueUnequal(element1, element2);
                if (comparison.isError || comparison.booleanValue)
                    return comparison;
            }
        }
        return valueFactory.bool(atomized1.size() == 0 && atomized2.size() == 0);
    }



    public AntlrQueryValue generalLessThan(
        final AntlrQueryValue o1,
        final AntlrQueryValue o2)
    {
        final List<AntlrQueryValue> atomized1 = atomizer.atomize(o1);
        final List<AntlrQueryValue> atomized2 = atomizer.atomize(o2);

        for (final var element1 : atomized1) {
            for (final var element2 : atomized2) {
                final var comparison = valueComparisonOperator.valueLessThan(element1, element2);
                if (comparison.isError || comparison.booleanValue)
                    return comparison;
            }
        }
        return valueFactory.bool(atomized1.size() == 0 && atomized2.size() == 0);
    }


    public AntlrQueryValue generalGreaterThan(
        final AntlrQueryValue o1,
        final AntlrQueryValue o2)
    {
        final List<AntlrQueryValue> atomized1 = atomizer.atomize(o1);
        final List<AntlrQueryValue> atomized2 = atomizer.atomize(o2);

        for (final var element1 : atomized1) {
            for (final var element2 : atomized2) {
                final var comparison = valueComparisonOperator.valueGreaterThan(element1, element2);
                if (comparison.isError || comparison.booleanValue)
                    return comparison;
            }
        }
        return valueFactory.bool(atomized1.size() == 0 && atomized2.size() == 0);
    }


    public AntlrQueryValue generalLessEqual(
        final AntlrQueryValue o1,
        final AntlrQueryValue o2)
    {
        final List<AntlrQueryValue> atomized1 = atomizer.atomize(o1);
        final List<AntlrQueryValue> atomized2 = atomizer.atomize(o2);

        for (final var element1 : atomized1) {
            for (final var element2 : atomized2) {
                final var comparison = valueComparisonOperator.valueLessEqual(element1, element2);
                if (comparison.isError || comparison.booleanValue)
                    return comparison;
            }
        }
        return valueFactory.bool(atomized1.size() == 0 && atomized2.size() == 0);
    }


    public AntlrQueryValue generalGreaterEqual(
        final AntlrQueryValue o1,
        final AntlrQueryValue o2)
    {
        final List<AntlrQueryValue> atomized1 = atomizer.atomize(o1);
        final List<AntlrQueryValue> atomized2 = atomizer.atomize(o2);

        for (final var element1 : atomized1) {
            for (final var element2 : atomized2) {
                final var comparison = valueComparisonOperator.valueGreaterEqual(element1, element2);
                if (comparison.isError || comparison.booleanValue)
                    return comparison;
            }
        }
        return valueFactory.bool(atomized1.size() == 0 && atomized2.size() == 0);
    }
}

