package com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.stream.Collector;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;
import com.github.akruk.antlrxquery.values.XQueryError;
import com.github.akruk.antlrxquery.values.XQueryValue;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

public class AggregateFunctions {

    private final XQueryValueFactory valueFactory;
    private final Parser targetParser;;

    public AggregateFunctions(final XQueryValueFactory valueFactory, final Parser targetParser) {
        this.valueFactory = valueFactory;
        this.targetParser = targetParser;
    }


    public XQueryValue count(
            final XQueryVisitingContext context,
            final List<XQueryValue> args)
    {
        final var input = args.get(0);
        return valueFactory.number(input.sequence().size());
    }


    public XQueryValue avg(
            final XQueryVisitingContext context,
            final List<XQueryValue> args)
    {
        final var values = args.get(0);
        if (values.isEmptySequence())
            return values;
        final List<XQueryValue> sequence = values.sequence();
        if (!sequence.stream().allMatch(XQueryValue::isNumericValue))
            return XQueryError.InvalidArgumentType;
        final BigDecimal size = BigDecimal.valueOf(sequence.size());
        final BigDecimal summed = sequence.stream()
            .map(XQueryValue::numericValue)
            .map(number->number.divide(size, MathContext.UNLIMITED))
            .reduce(BigDecimal::add)
            .get();
        return valueFactory.number(summed);
    }


    public XQueryValue max(
            final XQueryVisitingContext context,
            final List<XQueryValue> args)
    {
        final var values = args.get(0);
        if (values.isEmptySequence())
            return values;
        final List<XQueryValue> sequence = values.sequence();
        if (!sequence.stream().allMatch(XQueryValue::isNumericValue))
            return XQueryError.InvalidArgumentType;
        final BigDecimal max = sequence.stream().map(XQueryValue::numericValue).max(BigDecimal::compareTo).get();
        return valueFactory.number(max);
    }


    public XQueryValue min(
            final XQueryVisitingContext context,
            final List<XQueryValue> args)
    {
        final var values = args.get(0);
        if (values.isEmptySequence())
            return values;
        final List<XQueryValue> sequence = values.sequence();
        if (!sequence.stream().allMatch(XQueryValue::isNumericValue))
            return XQueryError.InvalidArgumentType;
        final BigDecimal min = sequence.stream().map(XQueryValue::numericValue).min(BigDecimal::compareTo).get();
        return valueFactory.number(min);
    }


    public XQueryValue sum(
            final XQueryVisitingContext context,
            final List<XQueryValue> args)
    {
        final var values = args.get(0);
        if (values.isEmptySequence())
            return values;
        final List<XQueryValue> sequence = values.sequence();
        if (!sequence.stream().allMatch(XQueryValue::isNumericValue))
            return XQueryError.InvalidArgumentType;
        final BigDecimal summed = sequence.stream().map(XQueryValue::numericValue).reduce(BigDecimal::add).get();
        return valueFactory.number(summed);
    }

    public XQueryValue allEqual(
            final XQueryVisitingContext context,
            final List<XQueryValue> args)
    {
        // TODO: take collation into account
        final var values = args.get(0);
        if (values.isEmptySequence())
            return valueFactory.bool(true);
        final List<XQueryValue> sequence = values.atomize();
        // if (!sequence.stream().allMatch(XQueryValue::isNumericValue))
            // return XQueryError.InvalidArgumentType;
        int size = sequence.size();
        XQueryValue previousValue = sequence.get(0);
        for (int i = 1; i < size; i++) {
            var value = sequence.get(i);
            if (!previousValue.valueEqual(value).effectiveBooleanValue())
                return valueFactory.bool(false);
        }
        return valueFactory.bool(true);
    }


    public XQueryValue allDifferent(
            final XQueryVisitingContext context,
            final List<XQueryValue> args)
    {
        // TODO: take collation into account
        final var values = args.get(0);
        if (values.isEmptySequence())
            return valueFactory.bool(true);
        final List<XQueryValue> sequence = values.atomize();
        // if (!sequence.stream().allMatch(XQueryValue::isNumericValue))
            // return XQueryError.InvalidArgumentType;
        int size = sequence.size();
        XQueryValue previousValue = sequence.get(0);
        for (int i = 1; i < size; i++) {
            var value = sequence.get(i);
            if (previousValue.valueEqual(value).effectiveBooleanValue())
                return valueFactory.bool(false);
        }
        return valueFactory.bool(true);
    }

}
