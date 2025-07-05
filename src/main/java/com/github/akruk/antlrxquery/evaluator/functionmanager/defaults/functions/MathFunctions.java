package com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;
import com.github.akruk.antlrxquery.values.XQueryError;
import com.github.akruk.antlrxquery.values.XQueryValue;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

public class MathFunctions {
    private final XQueryValueFactory valueFactory;
    public MathFunctions(final XQueryValueFactory valueFactory) {
        this.valueFactory = valueFactory;
    }

    public XQueryValue pi(final XQueryVisitingContext context, final List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (!args.isEmpty()) return XQueryError.WrongNumberOfArguments;
        return valueFactory.number(BigDecimal.valueOf(Math.PI));
    }

    public XQueryValue e(final XQueryVisitingContext context, final List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (!args.isEmpty()) {
            return XQueryError.WrongNumberOfArguments;
        }
        return valueFactory.number(BigDecimal.valueOf(Math.E));
    }

    public XQueryValue exp(final XQueryVisitingContext context, final List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        final var arg = args.get(0);
        if (!arg.isNumericValue()) return XQueryError.InvalidArgumentType;
        return valueFactory.number(BigDecimal.valueOf(Math.exp(arg.numericValue().doubleValue())));
    }

    public XQueryValue exp10(final XQueryVisitingContext context, final List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        final var arg = args.get(0);
        if (!arg.isNumericValue()) return XQueryError.InvalidArgumentType;
        return valueFactory.number(BigDecimal.valueOf(Math.pow(10, arg.numericValue().doubleValue())));
    }

    public XQueryValue log(final XQueryVisitingContext context, final List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        final var arg = args.get(0);
        if (!arg.isNumericValue()) return XQueryError.InvalidArgumentType;
        final double v = arg.numericValue().doubleValue();
        if (v <= 0 || Double.isNaN(v)) return XQueryError.InvalidArgumentType;
        return valueFactory.number(BigDecimal.valueOf(Math.log(v)));
    }

    public XQueryValue log10(final XQueryVisitingContext context, final List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        final var arg = args.get(0);
        if (!arg.isNumericValue()) return XQueryError.InvalidArgumentType;
        final double v = arg.numericValue().doubleValue();
        if (v <= 0 || Double.isNaN(v)) return XQueryError.InvalidArgumentType;
        return valueFactory.number(BigDecimal.valueOf(Math.log10(v)));
    }

    public XQueryValue pow(final XQueryVisitingContext context, final List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() != 2) return XQueryError.WrongNumberOfArguments;
        final var base = args.get(0);
        final var exponent = args.get(1);
        if (!base.isNumericValue() || !exponent.isNumericValue()) return XQueryError.InvalidArgumentType;
        return valueFactory.number(BigDecimal.valueOf(Math.pow(base.numericValue().doubleValue(), exponent.numericValue().doubleValue())));
    }

    public XQueryValue sqrt(final XQueryVisitingContext context, final List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        final var arg = args.get(0);
        if (!arg.isNumericValue()) return XQueryError.InvalidArgumentType;
        final double v = arg.numericValue().doubleValue();
        if (v < 0) return XQueryError.InvalidArgumentType;
        return valueFactory.number(BigDecimal.valueOf(Math.sqrt(v)));
    }

    public XQueryValue sin(final XQueryVisitingContext context, final List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        final var arg = args.get(0);
        if (!arg.isNumericValue()) return XQueryError.InvalidArgumentType;
        return valueFactory.number(BigDecimal.valueOf(Math.sin(arg.numericValue().doubleValue())));
    }

    public XQueryValue cos(final XQueryVisitingContext context, final List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        final var arg = args.get(0);
        if (!arg.isNumericValue()) return XQueryError.InvalidArgumentType;
        return valueFactory.number(BigDecimal.valueOf(Math.cos(arg.numericValue().doubleValue())));
    }

    public XQueryValue tan(final XQueryVisitingContext context, final List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        final var arg = args.get(0);
        if (!arg.isNumericValue()) return XQueryError.InvalidArgumentType;
        return valueFactory.number(BigDecimal.valueOf(Math.tan(arg.numericValue().doubleValue())));
    }

    public XQueryValue asin(final XQueryVisitingContext context, final List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        final var arg = args.get(0);
        if (!arg.isNumericValue()) return XQueryError.InvalidArgumentType;
        final double v = arg.numericValue().doubleValue();
        if (v < -1 || v > 1) return XQueryError.InvalidArgumentType;
        return valueFactory.number(BigDecimal.valueOf(Math.asin(v)));
    }

    public XQueryValue acos(final XQueryVisitingContext context, final List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        final var arg = args.get(0);
        if (!arg.isNumericValue()) return XQueryError.InvalidArgumentType;
        final double v = arg.numericValue().doubleValue();
        if (v < -1 || v > 1) return XQueryError.InvalidArgumentType;
        return valueFactory.number(BigDecimal.valueOf(Math.acos(v)));
    }

    public XQueryValue atan(final XQueryVisitingContext context, final List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        final var arg = args.get(0);
        if (!arg.isNumericValue()) return XQueryError.InvalidArgumentType;
        return valueFactory.number(BigDecimal.valueOf(Math.atan(arg.numericValue().doubleValue())));
    }

    public XQueryValue atan2(final XQueryVisitingContext context, final List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() != 2) return XQueryError.WrongNumberOfArguments;
        final var y = args.get(0);
        final var x = args.get(1);
        if (!y.isNumericValue() || !x.isNumericValue()) return XQueryError.InvalidArgumentType;
        return valueFactory.number(BigDecimal.valueOf(Math.atan2(y.numericValue().doubleValue(), x.numericValue().doubleValue())));
    }
    public XQueryValue sinh(final XQueryVisitingContext context, final List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() != 1) {
            return XQueryError.WrongNumberOfArguments;
        }
        final var arg = args.get(0);
        if (!arg.isNumericValue()) {
            return XQueryError.InvalidArgumentType;
        }
        final double v = arg.numericValue().doubleValue();
        return valueFactory.number(BigDecimal.valueOf(Math.sinh(v)));
    }

    public XQueryValue cosh(final XQueryVisitingContext context, final List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() != 1) {
            return XQueryError.WrongNumberOfArguments;
        }
        final var arg = args.get(0);
        if (!arg.isNumericValue()) {
            return XQueryError.InvalidArgumentType;
        }
        final double v = arg.numericValue().doubleValue();
        return valueFactory.number(BigDecimal.valueOf(Math.cosh(v)));
    }

    public XQueryValue tanh(final XQueryVisitingContext context, final List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() != 1) {
            return XQueryError.WrongNumberOfArguments;
        }
        final var arg = args.get(0);
        if (!arg.isNumericValue()) {
            return XQueryError.InvalidArgumentType;
        }
        final double v = arg.numericValue().doubleValue();
        return valueFactory.number(BigDecimal.valueOf(Math.tanh(v)));
    }






}
