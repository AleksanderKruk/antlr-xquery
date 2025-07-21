package com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions;

import java.util.List;

import org.antlr.v4.runtime.Parser;

import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;
import com.github.akruk.antlrxquery.evaluator.values.XQueryError;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;
import com.github.akruk.antlrxquery.evaluator.values.operations.EffectiveBooleanValue;

public class ProcessingBooleans {
    private final XQueryValueFactory valueFactory;
    private final EffectiveBooleanValue ebv;

    public ProcessingBooleans(final XQueryValueFactory valueFactory, final Parser targetParser, final EffectiveBooleanValue ebv) {
        this.valueFactory = valueFactory;
        this.ebv = ebv;
    }


    public XQueryValue true_(final XQueryVisitingContext context, final List<XQueryValue> args) {
        return valueFactory.bool(true);
    }

    public XQueryValue false_(final XQueryVisitingContext context, final List<XQueryValue> args) {
        return valueFactory.bool(false);
    }


    public XQueryValue not(final XQueryVisitingContext context, final List<XQueryValue> args) {
        XQueryValue effectiveBooleanValue = ebv.effectiveBooleanValue(args.get(0));
        if (effectiveBooleanValue == null)
            return valueFactory.error(XQueryError.InvalidArgumentType, "");
        return valueFactory.bool(!effectiveBooleanValue.booleanValue);
    }

    public XQueryValue boolean_(final XQueryVisitingContext context, final List<XQueryValue> args) {
        XQueryValue effectiveBooleanValue = ebv.effectiveBooleanValue(args.get(0));
        if (effectiveBooleanValue.isError)
            return valueFactory.error(XQueryError.InvalidArgumentType, "");

        return effectiveBooleanValue;
    }

    public XQueryValue booleanEqual(final XQueryVisitingContext context, final List<XQueryValue> args) {
        XQueryValue effectiveBooleanValue1 = ebv.effectiveBooleanValue(args.get(0));
        XQueryValue effectiveBooleanValue2 = ebv.effectiveBooleanValue(args.get(1));

        if (effectiveBooleanValue1.isError || effectiveBooleanValue2.isError)
            return valueFactory.error(XQueryError.InvalidArgumentType, "");

        return valueFactory.bool(effectiveBooleanValue1 == effectiveBooleanValue2);
    }

    public XQueryValue booleanLessThan(final XQueryVisitingContext context, final List<XQueryValue> args) {
        XQueryValue effectiveBooleanValue1 = ebv.effectiveBooleanValue(args.get(0));
        XQueryValue effectiveBooleanValue2 = ebv.effectiveBooleanValue(args.get(1));

        if (effectiveBooleanValue1.isError || effectiveBooleanValue2.isError)
            return valueFactory.error(XQueryError.InvalidArgumentType, "");

        return valueFactory.bool(!effectiveBooleanValue1.booleanValue && effectiveBooleanValue2.booleanValue);
    }

    public XQueryValue booleanLessThanOrEqual(final XQueryVisitingContext context, final List<XQueryValue> args) {
        XQueryValue effectiveBooleanValue1 = ebv.effectiveBooleanValue(args.get(0));
        XQueryValue effectiveBooleanValue2 = ebv.effectiveBooleanValue(args.get(1));

        if (effectiveBooleanValue1.isError || effectiveBooleanValue2.isError)
            return valueFactory.error(XQueryError.InvalidArgumentType, "");

        return valueFactory.bool(!effectiveBooleanValue1.booleanValue || effectiveBooleanValue2.booleanValue);
    }

    public XQueryValue booleanGreaterThan(final XQueryVisitingContext context, final List<XQueryValue> args) {
        XQueryValue effectiveBooleanValue1 = ebv.effectiveBooleanValue(args.get(0));
        XQueryValue effectiveBooleanValue2 = ebv.effectiveBooleanValue(args.get(1));

        if (effectiveBooleanValue1.isError || effectiveBooleanValue2.isError)
            return valueFactory.error(XQueryError.InvalidArgumentType, "");

        return valueFactory.bool(effectiveBooleanValue1.booleanValue && !effectiveBooleanValue2.booleanValue);
    }

    public XQueryValue booleanGreaterThanOrEqual(final XQueryVisitingContext context, final List<XQueryValue> args) {
        XQueryValue effectiveBooleanValue1 = ebv.effectiveBooleanValue(args.get(0));
        XQueryValue effectiveBooleanValue2 = ebv.effectiveBooleanValue(args.get(1));

        if (effectiveBooleanValue1.isError || effectiveBooleanValue2.isError)
            return valueFactory.error(XQueryError.InvalidArgumentType, "");

        return valueFactory.bool(effectiveBooleanValue1.booleanValue
                                || !effectiveBooleanValue2.booleanValue);
    }


}
