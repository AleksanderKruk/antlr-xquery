package com.github.akruk.antlrxquery.evaluator.values.operations;

import java.util.List;

import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;

public class GeneralComparisonOperator {

    private final XQueryValueFactory valueFactory;
    private final ValueAtomizer atomizer;
    private final ValueComparisonOperator valueComparisonOperator;

    public GeneralComparisonOperator(
        final XQueryValueFactory valueFactory,
        final ValueAtomizer atomizer,
        final ValueComparisonOperator valueComparisonOperator)
    {
        this.valueFactory = valueFactory;
        this.atomizer = atomizer;
        this.valueComparisonOperator = valueComparisonOperator;
    }

    public XQueryValue generalEquals(
        final XQueryValue o1,
        final XQueryValue o2)
    {
        final List<XQueryValue> atomized1 = atomizer.atomize(o1);
        final List<XQueryValue> atomized2 = atomizer.atomize(o2);

        for (final var element1 : atomized1) {
            for (final var element2 : atomized2) {
                final var comparison = valueComparisonOperator.valueEquals(element1, element2);
                if (comparison.isError || comparison.booleanValue)
                    return comparison;
            }
        }
        return valueFactory.bool(atomized1.size() == 0 && atomized2.size() == 0);
    }

    public XQueryValue generalUnequals(
        final XQueryValue o1,
        final XQueryValue o2)
    {
        final List<XQueryValue> atomized1 = atomizer.atomize(o1);
        final List<XQueryValue> atomized2 = atomizer.atomize(o2);

        for (final var element1 : atomized1) {
            for (final var element2 : atomized2) {
                final var comparison = valueComparisonOperator.valueUnequal(element1, element2);
                if (comparison.isError || comparison.booleanValue)
                    return comparison;
            }
        }
        return valueFactory.bool(atomized1.size() == 0 && atomized2.size() == 0);
    }



    public XQueryValue generalLessThan(
        final XQueryValue o1,
        final XQueryValue o2)
    {
        final List<XQueryValue> atomized1 = atomizer.atomize(o1);
        final List<XQueryValue> atomized2 = atomizer.atomize(o2);

        for (final var element1 : atomized1) {
            for (final var element2 : atomized2) {
                final var comparison = valueComparisonOperator.valueLessThan(element1, element2);
                if (comparison.isError || comparison.booleanValue)
                    return comparison;
            }
        }
        return valueFactory.bool(atomized1.size() == 0 && atomized2.size() == 0);
    }


    public XQueryValue generalGreaterThan(
        final XQueryValue o1,
        final XQueryValue o2)
    {
        final List<XQueryValue> atomized1 = atomizer.atomize(o1);
        final List<XQueryValue> atomized2 = atomizer.atomize(o2);

        for (final var element1 : atomized1) {
            for (final var element2 : atomized2) {
                final var comparison = valueComparisonOperator.valueGreaterThan(element1, element2);
                if (comparison.isError || comparison.booleanValue)
                    return comparison;
            }
        }
        return valueFactory.bool(atomized1.size() == 0 && atomized2.size() == 0);
    }


    public XQueryValue generalLessEqual(
        final XQueryValue o1,
        final XQueryValue o2)
    {
        final List<XQueryValue> atomized1 = atomizer.atomize(o1);
        final List<XQueryValue> atomized2 = atomizer.atomize(o2);

        for (final var element1 : atomized1) {
            for (final var element2 : atomized2) {
                final var comparison = valueComparisonOperator.valueLessEqual(element1, element2);
                if (comparison.isError || comparison.booleanValue)
                    return comparison;
            }
        }
        return valueFactory.bool(atomized1.size() == 0 && atomized2.size() == 0);
    }


    public XQueryValue generalGreaterEqual(
        final XQueryValue o1,
        final XQueryValue o2)
    {
        final List<XQueryValue> atomized1 = atomizer.atomize(o1);
        final List<XQueryValue> atomized2 = atomizer.atomize(o2);

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

