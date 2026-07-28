package com.github.akruk.antlrquery.evaluator.functionmanager.defaults.functions;

import java.util.List;

import org.antlr.v4.runtime.Parser;

import com.github.akruk.antlrquery.evaluator.AntlrQueryVisitingContext;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryError;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;
import com.github.akruk.antlrquery.evaluator.values.factories.AntlrQueryValueFactory;
import com.github.akruk.antlrquery.evaluator.values.operations.EffectiveBooleanValue;

public class ProcessingBooleans {
    private final AntlrQueryValueFactory valueFactory;
    private final EffectiveBooleanValue ebv;

    public ProcessingBooleans(final AntlrQueryValueFactory valueFactory, final Parser targetParser, final EffectiveBooleanValue ebv) {
        this.valueFactory = valueFactory;
        this.ebv = ebv;
    }


    public AntlrQueryValue true_(final AntlrQueryVisitingContext context, final List<AntlrQueryValue> args) {
        return valueFactory.bool(true);
    }

    public AntlrQueryValue false_(final AntlrQueryVisitingContext context, final List<AntlrQueryValue> args) {
        return valueFactory.bool(false);
    }


    public AntlrQueryValue not(final AntlrQueryVisitingContext context, final List<AntlrQueryValue> args) {
        AntlrQueryValue effectiveBooleanValue = ebv.effectiveBooleanValue(args.get(0));
        if (effectiveBooleanValue == null)
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        return valueFactory.bool(!effectiveBooleanValue.booleanValue);
    }

    public AntlrQueryValue boolean_(final AntlrQueryVisitingContext context, final List<AntlrQueryValue> args) {
        AntlrQueryValue effectiveBooleanValue = ebv.effectiveBooleanValue(args.get(0));
        if (effectiveBooleanValue.isError)
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");

        return effectiveBooleanValue;
    }

    public AntlrQueryValue booleanEqual(final AntlrQueryVisitingContext context, final List<AntlrQueryValue> args) {
        AntlrQueryValue effectiveBooleanValue1 = ebv.effectiveBooleanValue(args.get(0));
        AntlrQueryValue effectiveBooleanValue2 = ebv.effectiveBooleanValue(args.get(1));

        if (effectiveBooleanValue1.isError || effectiveBooleanValue2.isError)
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");

        return valueFactory.bool(effectiveBooleanValue1 == effectiveBooleanValue2);
    }

    public AntlrQueryValue booleanLessThan(final AntlrQueryVisitingContext context, final List<AntlrQueryValue> args) {
        AntlrQueryValue effectiveBooleanValue1 = ebv.effectiveBooleanValue(args.get(0));
        AntlrQueryValue effectiveBooleanValue2 = ebv.effectiveBooleanValue(args.get(1));

        if (effectiveBooleanValue1.isError || effectiveBooleanValue2.isError)
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");

        return valueFactory.bool(!effectiveBooleanValue1.booleanValue && effectiveBooleanValue2.booleanValue);
    }

    public AntlrQueryValue booleanLessThanOrEqual(final AntlrQueryVisitingContext context, final List<AntlrQueryValue> args) {
        AntlrQueryValue effectiveBooleanValue1 = ebv.effectiveBooleanValue(args.get(0));
        AntlrQueryValue effectiveBooleanValue2 = ebv.effectiveBooleanValue(args.get(1));

        if (effectiveBooleanValue1.isError || effectiveBooleanValue2.isError)
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");

        return valueFactory.bool(!effectiveBooleanValue1.booleanValue || effectiveBooleanValue2.booleanValue);
    }

    public AntlrQueryValue booleanGreaterThan(final AntlrQueryVisitingContext context, final List<AntlrQueryValue> args) {
        AntlrQueryValue effectiveBooleanValue1 = ebv.effectiveBooleanValue(args.get(0));
        AntlrQueryValue effectiveBooleanValue2 = ebv.effectiveBooleanValue(args.get(1));

        if (effectiveBooleanValue1.isError || effectiveBooleanValue2.isError)
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");

        return valueFactory.bool(effectiveBooleanValue1.booleanValue && !effectiveBooleanValue2.booleanValue);
    }

    public AntlrQueryValue booleanGreaterThanOrEqual(final AntlrQueryVisitingContext context, final List<AntlrQueryValue> args) {
        AntlrQueryValue effectiveBooleanValue1 = ebv.effectiveBooleanValue(args.get(0));
        AntlrQueryValue effectiveBooleanValue2 = ebv.effectiveBooleanValue(args.get(1));

        if (effectiveBooleanValue1.isError || effectiveBooleanValue2.isError)
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");

        return valueFactory.bool(effectiveBooleanValue1.booleanValue
                                || !effectiveBooleanValue2.booleanValue);
    }


}
