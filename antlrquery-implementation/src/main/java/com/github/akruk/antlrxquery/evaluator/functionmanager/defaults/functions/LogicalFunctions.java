package com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions;

import java.util.List;
import java.util.function.Predicate;

import org.antlr.v4.runtime.Parser;

import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;
import com.github.akruk.antlrxquery.evaluator.values.operations.EffectiveBooleanValue;

public class LogicalFunctions {

    private final XQueryValueFactory valueFactory;
    private final EffectiveBooleanValue ebv;

    public LogicalFunctions(final XQueryValueFactory valueFactory,
                            final Parser targetParser,
                            final EffectiveBooleanValue ebv)
    {
        this.valueFactory = valueFactory;
        this.ebv = ebv;
    }

    public XQueryValue every(
            XQueryVisitingContext context,
            List<XQueryValue> args)
    {
        final var input = args.get(0);
        final var predicate = args.get(1).functionValue;
        final var items = input.sequence;
        final Predicate<XQueryValue> matchesPredicate = item -> {
            XQueryValue predicateResult = predicate.call(context, List.of(item));
            return ebv.effectiveBooleanValue(predicateResult).booleanValue;
        };
        final boolean allMatch = items.stream().allMatch(matchesPredicate);
        return valueFactory.bool(allMatch);
    }

    public XQueryValue some(
            XQueryVisitingContext context,
            List<XQueryValue> args)
    {
        final var input = args.get(0);
        final var predicate = args.get(1).functionValue;
        final var items = input.sequence;
        final Predicate<XQueryValue> matchesPredicate = item -> {
            XQueryValue call = predicate.call(context, List.of(item));
            return ebv.effectiveBooleanValue(call).booleanValue;
        };
        final boolean allMatch = items.stream().anyMatch(matchesPredicate);
        return valueFactory.bool(allMatch);
    }


}
