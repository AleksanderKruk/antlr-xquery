package com.github.akruk.antlrquery.evaluator.functionmanager.defaults.functions;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.Collator;
import java.util.List;
import java.util.Map;
import org.antlr.v4.runtime.Parser;
import com.github.akruk.antlrquery.evaluator.AntlrQueryVisitingContext;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryError;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;
import com.github.akruk.antlrquery.evaluator.values.factories.AntlrQueryValueFactory;
import com.github.akruk.antlrquery.evaluator.values.operations.ValueAtomizer;
import com.github.akruk.antlrquery.evaluator.values.operations.ValueComparisonOperator;

public class AggregateFunctions {

    private final AntlrQueryValueFactory valueFactory;
    private final Map<String, Collator> collationUriToCollator;;
    private final ValueAtomizer atomizer;
    private final ValueComparisonOperator comparisonOperator;

    public AggregateFunctions(final AntlrQueryValueFactory valueFactory,
                                final Parser targetParser,
                                final Map<String, Collator> collationUriToCollator,
                                final ValueAtomizer atomizer,
                                final ValueComparisonOperator comparisonOperator)
    {
        this.valueFactory = valueFactory;
        this.collationUriToCollator = collationUriToCollator;
        this.atomizer = atomizer;
        this.comparisonOperator = comparisonOperator;
    }


    public AntlrQueryValue count(
            final AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args)
    {
        final var input = args.getFirst();
        return valueFactory.number(input.size);
    }


    public AntlrQueryValue avg(
            final AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args)
    {
        final var values = args.getFirst();
        if (values.isEmptySequence)
            return values;
        final List<AntlrQueryValue> sequence = values.sequence;
        if (!sequence.stream().allMatch(v->v.isNumeric))
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        final BigDecimal size = BigDecimal.valueOf(sequence.size());
        final BigDecimal summed = sequence.stream()
            .map(v->v.numericValue)
            .map(number->number.divide(size, MathContext.DECIMAL128))
            .reduce(BigDecimal::add)
            .get();
        return valueFactory.number(summed);
    }


    public AntlrQueryValue max(
            final AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args)
    {
        final var values = args.getFirst();
        if (values.isEmptySequence)
            return values;
        final List<AntlrQueryValue> sequence = values.sequence;
        if (!sequence.stream().allMatch(v->v.isNumeric))
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        final BigDecimal max = sequence.stream().map(v->v.numericValue).max(BigDecimal::compareTo).get();
        return valueFactory.number(max);
    }


    public AntlrQueryValue min(
            final AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args)
    {
        final var values = args.get(0);
        if (values.isEmptySequence)
            return values;
        final List<AntlrQueryValue> sequence = values.sequence;
        if (!sequence.stream().allMatch(v->v.isNumeric))
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        final BigDecimal min = sequence.stream().map(v->v.numericValue).min(BigDecimal::compareTo).get();
        return valueFactory.number(min);
    }


    public AntlrQueryValue sum(
            final AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args)
    {
        final var values = args.get(0);
        if (values.isEmptySequence)
            return values;
        final List<AntlrQueryValue> sequence = values.sequence;
        if (!sequence.stream().allMatch(v->v.isNumeric))
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        final BigDecimal summed = sequence.stream().map(v->v.numericValue).reduce(BigDecimal::add).get();
        return valueFactory.number(summed);
    }

    public AntlrQueryValue allEqual(
            final AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args)
    {
        // TODO: take collation into account
        final var values = args.getFirst();
        if (values.isEmptySequence)
            return valueFactory.bool(true);
        final List<AntlrQueryValue> sequence = atomizer.atomize(values);
        // if (!sequence.stream().allMatch(XQueryValue::isNumericValue))
            // return XQueryError.InvalidArgumentType;
        int size = sequence.size();
        AntlrQueryValue previousValue = sequence.get(0);
        for (int i = 1; i < size; i++) {
            var value = sequence.get(i);
            if (comparisonOperator.valueUnequal(previousValue, value).booleanValue)
                return valueFactory.bool(false);
        }
        return valueFactory.bool(true);
    }


    public AntlrQueryValue allDifferent(
            AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args) {

        AntlrQueryValue valuesArg    = args.get(0);
        AntlrQueryValue collationArg = args.get(1);

        List<AntlrQueryValue> items = valuesArg.sequence;
        if (items.size() <= 1) {
            return valueFactory.bool(true);
        }

        Collator collator = collationUriToCollator.get(collationArg.stringValue);

        for (int i = 0, n = items.size(); i < n - 1; i++) {
            AntlrQueryValue a = items.get(i);
            for (int j = i + 1; j < n; j++) {
                AntlrQueryValue b = items.get(j);

                if (atomicEquals(a, b, collator)) {
                    return valueFactory.bool(false);
                }
            }
        }

        return valueFactory.bool(true);
    }


    private boolean atomicEquals(
        AntlrQueryValue a,
        AntlrQueryValue b,
        Collator collator)
    {
        // TODO: rewrite
        if (a.isNumeric && b.isNumeric) {
            return a.numericValue.compareTo(b.numericValue) == 0;
        }
        if (a.isBoolean && b.isBoolean) {
            return a.booleanValue == b.booleanValue;
        }
        return collator.compare(a.stringValue, b.stringValue) == 0;
    }


}
