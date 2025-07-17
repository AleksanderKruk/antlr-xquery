package com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions;

import java.util.List;
import java.util.function.Predicate;

import org.antlr.v4.runtime.Parser;

import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;
import com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.EvaluatingFunctionManager;
import com.github.akruk.antlrxquery.values.XQueryFunction;
import com.github.akruk.antlrxquery.values.XQueryValue;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

public class LogicalFunctions {

    private final XQueryValueFactory valueFactory;

    public LogicalFunctions(final XQueryValueFactory valueFactory, final Parser targetParser)
    {
        this.valueFactory = valueFactory;
    }

    public XQueryValue every(
            XQueryVisitingContext context,
            List<XQueryValue> args)
    {
        final var input = args.get(0);
        final var predicate = args.get(1).functionValue();
        final var items = input.sequence();
        final Predicate<XQueryValue> matchesPredicate = item -> predicate.call(context, List.of(item)).effectiveBooleanValue();
        final boolean allMatch = items.stream().allMatch(matchesPredicate);
        return valueFactory.bool(allMatch);
    }

    public XQueryValue some(
            XQueryVisitingContext context,
            List<XQueryValue> args)
    {
        final var input = args.get(0);
        final var predicate = args.get(1).functionValue();
        final var items = input.sequence();
        final Predicate<XQueryValue> matchesPredicate = item -> predicate.call(context, List.of(item)).effectiveBooleanValue();
        final boolean allMatch = items.stream().anyMatch(matchesPredicate);
        return valueFactory.bool(allMatch);
    }


}
