package com.github.akruk.antlrxquery.evaluator.functioncaller.defaults.functions;

import java.math.BigDecimal;
import java.util.List;

import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;
import com.github.akruk.antlrxquery.values.XQueryError;
import com.github.akruk.antlrxquery.values.XQueryValue;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

public class MathFunctions {
    private XQueryValueFactory valueFactory;
    public MathFunctions(XQueryValueFactory valueFactory) {
        this.valueFactory = valueFactory;
    }

    public XQueryValue pi(XQueryVisitingContext context, List<XQueryValue> args) {
        if (!args.isEmpty()) return XQueryError.WrongNumberOfArguments;
        return valueFactory.number(BigDecimal.valueOf(Math.PI));
    }

    public XQueryValue e(XQueryVisitingContext context, List<XQueryValue> args) {
        if (!args.isEmpty()) return XQueryError.WrongNumberOfArguments;
        return valueFactory.number(BigDecimal.valueOf(Math.PI));
    }


    public XQueryValue exp(XQueryVisitingContext context, List<XQueryValue> args) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        var arg = args.get(0);
        if (!arg.isNumericValue()) return XQueryError.InvalidArgumentType;
        return valueFactory.number(BigDecimal.valueOf(Math.exp(arg.numericValue().doubleValue())));
    }

    public XQueryValue exp10(XQueryVisitingContext context, List<XQueryValue> args) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        var arg = args.get(0);
        if (!arg.isNumericValue()) return XQueryError.InvalidArgumentType;
        return valueFactory.number(BigDecimal.valueOf(Math.pow(10, arg.numericValue().doubleValue())));
    }

    public XQueryValue log(XQueryVisitingContext context, List<XQueryValue> args) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        var arg = args.get(0);
        if (!arg.isNumericValue()) return XQueryError.InvalidArgumentType;
        double v = arg.numericValue().doubleValue();
        if (v <= 0 || Double.isNaN(v)) return XQueryError.InvalidArgumentType;
        return valueFactory.number(BigDecimal.valueOf(Math.log(v)));
    }

    public XQueryValue log10(XQueryVisitingContext context, List<XQueryValue> args) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        var arg = args.get(0);
        if (!arg.isNumericValue()) return XQueryError.InvalidArgumentType;
        double v = arg.numericValue().doubleValue();
        if (v <= 0 || Double.isNaN(v)) return XQueryError.InvalidArgumentType;
        return valueFactory.number(BigDecimal.valueOf(Math.log10(v)));
    }

    public XQueryValue pow(XQueryVisitingContext context, List<XQueryValue> args) {
        if (args.size() != 2) return XQueryError.WrongNumberOfArguments;
        var base = args.get(0);
        var exponent = args.get(1);
        if (!base.isNumericValue() || !exponent.isNumericValue()) return XQueryError.InvalidArgumentType;
        return valueFactory.number(BigDecimal.valueOf(Math.pow(base.numericValue().doubleValue(), exponent.numericValue().doubleValue())));
    }

    public XQueryValue sqrt(XQueryVisitingContext context, List<XQueryValue> args) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        var arg = args.get(0);
        if (!arg.isNumericValue()) return XQueryError.InvalidArgumentType;
        double v = arg.numericValue().doubleValue();
        if (v < 0) return XQueryError.InvalidArgumentType;
        return valueFactory.number(BigDecimal.valueOf(Math.sqrt(v)));
    }

    public XQueryValue sin(XQueryVisitingContext context, List<XQueryValue> args) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        var arg = args.get(0);
        if (!arg.isNumericValue()) return XQueryError.InvalidArgumentType;
        return valueFactory.number(BigDecimal.valueOf(Math.sin(arg.numericValue().doubleValue())));
    }

    public XQueryValue cos(XQueryVisitingContext context, List<XQueryValue> args) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        var arg = args.get(0);
        if (!arg.isNumericValue()) return XQueryError.InvalidArgumentType;
        return valueFactory.number(BigDecimal.valueOf(Math.cos(arg.numericValue().doubleValue())));
    }

    public XQueryValue tan(XQueryVisitingContext context, List<XQueryValue> args) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        var arg = args.get(0);
        if (!arg.isNumericValue()) return XQueryError.InvalidArgumentType;
        return valueFactory.number(BigDecimal.valueOf(Math.tan(arg.numericValue().doubleValue())));
    }

    public XQueryValue asin(XQueryVisitingContext context, List<XQueryValue> args) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        var arg = args.get(0);
        if (!arg.isNumericValue()) return XQueryError.InvalidArgumentType;
        double v = arg.numericValue().doubleValue();
        if (v < -1 || v > 1) return XQueryError.InvalidArgumentType;
        return valueFactory.number(BigDecimal.valueOf(Math.asin(v)));
    }

    public XQueryValue acos(XQueryVisitingContext context, List<XQueryValue> args) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        var arg = args.get(0);
        if (!arg.isNumericValue()) return XQueryError.InvalidArgumentType;
        double v = arg.numericValue().doubleValue();
        if (v < -1 || v > 1) return XQueryError.InvalidArgumentType;
        return valueFactory.number(BigDecimal.valueOf(Math.acos(v)));
    }

    public XQueryValue atan(XQueryVisitingContext context, List<XQueryValue> args) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        var arg = args.get(0);
        if (!arg.isNumericValue()) return XQueryError.InvalidArgumentType;
        return valueFactory.number(BigDecimal.valueOf(Math.atan(arg.numericValue().doubleValue())));
    }

    public XQueryValue atan2(XQueryVisitingContext context, List<XQueryValue> args) {
        if (args.size() != 2) return XQueryError.WrongNumberOfArguments;
        var y = args.get(0);
        var x = args.get(1);
        if (!y.isNumericValue() || !x.isNumericValue()) return XQueryError.InvalidArgumentType;
        return valueFactory.number(BigDecimal.valueOf(Math.atan2(y.numericValue().doubleValue(), x.numericValue().doubleValue())));
    }

    public XQueryValue sinh(XQueryVisitingContext context, List<XQueryValue> args) {
        return null;
    }

    public XQueryValue cosh(XQueryVisitingContext context, List<XQueryValue> args) {
        return null;
    }

    public XQueryValue tanh(XQueryVisitingContext context, List<XQueryValue> args) {
        return null;
    }





}
