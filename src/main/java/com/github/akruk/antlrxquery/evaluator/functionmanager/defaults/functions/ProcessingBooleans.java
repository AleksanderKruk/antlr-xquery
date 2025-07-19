package com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions;

import java.util.List;

import org.antlr.v4.runtime.Parser;

import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;
import com.github.akruk.antlrxquery.evaluator.values.XQueryError;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;

public class ProcessingBooleans {
    private final XQueryValueFactory valueFactory;

    public ProcessingBooleans(final XQueryValueFactory valueFactory, final Parser targetParser) {
        this.valueFactory = valueFactory;
    }


    public XQueryValue true_(final XQueryVisitingContext context, final List<XQueryValue> args) {
        return valueFactory.bool(true);
    }

    public XQueryValue false_(final XQueryVisitingContext context, final List<XQueryValue> args) {
        return valueFactory.bool(false);
    }


    public XQueryValue not(final XQueryVisitingContext context, final List<XQueryValue> args) {
        Boolean effectiveBooleanValue = args.get(0).effectiveBooleanValue();
        if (effectiveBooleanValue == null)
            return XQueryError.InvalidArgumentType;
        return valueFactory.bool(!effectiveBooleanValue);
    }

    public XQueryValue boolean_(final XQueryVisitingContext context, final List<XQueryValue> args) {
        Boolean effectiveBooleanValue = args.get(0).effectiveBooleanValue();
        if (effectiveBooleanValue == null)
            return XQueryError.InvalidArgumentType;
        return valueFactory.bool(effectiveBooleanValue);
    }

    public XQueryValue booleanEqual(final XQueryVisitingContext context, final List<XQueryValue> args) {
        Boolean effectiveBooleanValue1 = args.get(0).effectiveBooleanValue();
        Boolean effectiveBooleanValue2 = args.get(1).effectiveBooleanValue();

        if (effectiveBooleanValue1 == null || effectiveBooleanValue2 == null)
            return XQueryError.InvalidArgumentType;

        return valueFactory.bool(effectiveBooleanValue1 == effectiveBooleanValue2);
    }

    public XQueryValue booleanLessThan(final XQueryVisitingContext context, final List<XQueryValue> args) {
        Boolean effectiveBooleanValue1 = args.get(0).effectiveBooleanValue();
        Boolean effectiveBooleanValue2 = args.get(1).effectiveBooleanValue();

        if (effectiveBooleanValue1 == null || effectiveBooleanValue2 == null)
            return XQueryError.InvalidArgumentType;

        return valueFactory.bool(!effectiveBooleanValue1 && effectiveBooleanValue2);
    }

    public XQueryValue booleanLessThanOrEqual(final XQueryVisitingContext context, final List<XQueryValue> args) {
        Boolean effectiveBooleanValue1 = args.get(0).effectiveBooleanValue();
        Boolean effectiveBooleanValue2 = args.get(1).effectiveBooleanValue();

        if (effectiveBooleanValue1 == null || effectiveBooleanValue2 == null)
            return XQueryError.InvalidArgumentType;

        return valueFactory.bool(!effectiveBooleanValue1 || effectiveBooleanValue2);
    }

    public XQueryValue booleanGreaterThan(final XQueryVisitingContext context, final List<XQueryValue> args) {
        Boolean effectiveBooleanValue1 = args.get(0).effectiveBooleanValue();
        Boolean effectiveBooleanValue2 = args.get(1).effectiveBooleanValue();

        if (effectiveBooleanValue1 == null || effectiveBooleanValue2 == null)
            return XQueryError.InvalidArgumentType;

        return valueFactory.bool(effectiveBooleanValue1 && !effectiveBooleanValue2);
    }

    public XQueryValue booleanGreaterThanOrEqual(final XQueryVisitingContext context, final List<XQueryValue> args) {
        Boolean effectiveBooleanValue1 = args.get(0).effectiveBooleanValue();
        Boolean effectiveBooleanValue2 = args.get(1).effectiveBooleanValue();

        if (effectiveBooleanValue1 == null || effectiveBooleanValue2 == null)
            return XQueryError.InvalidArgumentType;

        return valueFactory.bool(effectiveBooleanValue1 || !effectiveBooleanValue2);
    }


}
