package com.github.akruk.antlrquery.evaluator.functionmanager.defaults.functions;

import java.util.List;
import java.util.function.Predicate;

import org.antlr.v4.runtime.Parser;

import com.github.akruk.antlrquery.evaluator.AntlrQueryVisitingContext;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;
import com.github.akruk.antlrquery.evaluator.values.factories.AntlrQueryValueFactory;
import com.github.akruk.antlrquery.evaluator.values.operations.EffectiveBooleanValue;

public class LogicalFunctions {

    private final AntlrQueryValueFactory valueFactory;
    private final EffectiveBooleanValue ebv;

    public LogicalFunctions(final AntlrQueryValueFactory valueFactory,
                            final Parser targetParser,
                            final EffectiveBooleanValue ebv)
    {
        this.valueFactory = valueFactory;
        this.ebv = ebv;
    }

    public AntlrQueryValue every(
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args)
    {
        final var input = args.get(0);
        final var predicate = args.get(1).functionValue;
        final var items = input.sequence;
        final Predicate<AntlrQueryValue> matchesPredicate = item -> {
            AntlrQueryValue predicateResult = predicate.call(context, List.of(item));
            return ebv.effectiveBooleanValue(predicateResult).booleanValue;
        };
        final boolean allMatch = items.stream().allMatch(matchesPredicate);
        return valueFactory.bool(allMatch);
    }

    public AntlrQueryValue some(
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args)
    {
        final var input = args.get(0);
        final var predicate = args.get(1).functionValue;
        final var items = input.sequence;
        final Predicate<AntlrQueryValue> matchesPredicate = item -> {
            AntlrQueryValue call = predicate.call(context, List.of(item));
            return ebv.effectiveBooleanValue(call).booleanValue;
        };
        final boolean allMatch = items.stream().anyMatch(matchesPredicate);
        return valueFactory.bool(allMatch);
    }


}
