package com.github.akruk.antlrquery.evaluator.functionmanager.defaults.functions;

import java.math.BigDecimal;
import java.util.List;

import com.github.akruk.antlrquery.evaluator.AntlrQueryVisitingContext;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryError;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;
import com.github.akruk.antlrquery.evaluator.values.factories.AntlrQueryValueFactory;

public class MathFunctions {
    private final AntlrQueryValueFactory valueFactory;
    public MathFunctions(final AntlrQueryValueFactory valueFactory) {
        this.valueFactory = valueFactory;
    }

    public AntlrQueryValue pi(final AntlrQueryVisitingContext context, final List<AntlrQueryValue> args) {
        if (!args.isEmpty()) return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        return valueFactory.number(BigDecimal.valueOf(Math.PI));
    }

    public AntlrQueryValue e(final AntlrQueryVisitingContext context, final List<AntlrQueryValue> args) {
        if (!args.isEmpty()) {
            return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        }
        return valueFactory.number(BigDecimal.valueOf(Math.E));
    }

    public AntlrQueryValue exp(final AntlrQueryVisitingContext context, final List<AntlrQueryValue> args) {
        if (args.size() != 1) return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        final var arg = args.get(0);
        if (!arg.isNumeric) return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        return valueFactory.number(BigDecimal.valueOf(Math.exp(arg.numericValue.doubleValue())));
    }

    public AntlrQueryValue exp10(final AntlrQueryVisitingContext context, final List<AntlrQueryValue> args) {
        if (args.size() != 1) return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        final var arg = args.get(0);
        if (!arg.isNumeric) return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        return valueFactory.number(BigDecimal.valueOf(Math.pow(10, arg.numericValue.doubleValue())));
    }

    public AntlrQueryValue log(final AntlrQueryVisitingContext context, final List<AntlrQueryValue> args) {
        if (args.size() != 1) return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        final var arg = args.get(0);
        if (!arg.isNumeric) return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        final double v = arg.numericValue.doubleValue();
        if (v <= 0 || Double.isNaN(v)) return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        return valueFactory.number(BigDecimal.valueOf(Math.log(v)));
    }

    public AntlrQueryValue log10(final AntlrQueryVisitingContext context, final List<AntlrQueryValue> args) {
        if (args.size() != 1) return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        final var arg = args.get(0);
        if (!arg.isNumeric) return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        final double v = arg.numericValue.doubleValue();
        if (v <= 0 || Double.isNaN(v)) return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        return valueFactory.number(BigDecimal.valueOf(Math.log10(v)));
    }

    public AntlrQueryValue pow(final AntlrQueryVisitingContext context, final List<AntlrQueryValue> args) {
        if (args.size() != 2) return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        final var base = args.get(0);
        final var exponent = args.get(1);
        if (!base.isNumeric || !exponent.isNumeric) return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        return valueFactory.number(BigDecimal.valueOf(Math.pow(base.numericValue.doubleValue(), exponent.numericValue.doubleValue())));
    }

    public AntlrQueryValue sqrt(final AntlrQueryVisitingContext context, final List<AntlrQueryValue> args) {
        if (args.size() != 1) return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        final var arg = args.get(0);
        if (!arg.isNumeric) return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        final double v = arg.numericValue.doubleValue();
        if (v < 0) return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        return valueFactory.number(BigDecimal.valueOf(Math.sqrt(v)));
    }

    public AntlrQueryValue sin(final AntlrQueryVisitingContext context, final List<AntlrQueryValue> args) {
        if (args.size() != 1) return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        final var arg = args.get(0);
        if (!arg.isNumeric) return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        return valueFactory.number(BigDecimal.valueOf(Math.sin(arg.numericValue.doubleValue())));
    }

    public AntlrQueryValue cos(final AntlrQueryVisitingContext context, final List<AntlrQueryValue> args) {
        if (args.size() != 1) return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        final var arg = args.get(0);
        if (!arg.isNumeric) return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        return valueFactory.number(BigDecimal.valueOf(Math.cos(arg.numericValue.doubleValue())));
    }

    public AntlrQueryValue tan(final AntlrQueryVisitingContext context, final List<AntlrQueryValue> args) {
        if (args.size() != 1) return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        final var arg = args.get(0);
        if (!arg.isNumeric) return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        return valueFactory.number(BigDecimal.valueOf(Math.tan(arg.numericValue.doubleValue())));
    }

    public AntlrQueryValue asin(final AntlrQueryVisitingContext context, final List<AntlrQueryValue> args) {
        if (args.size() != 1) return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        final var arg = args.get(0);
        if (!arg.isNumeric) return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        final double v = arg.numericValue.doubleValue();
        if (v < -1 || v > 1) return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        return valueFactory.number(BigDecimal.valueOf(Math.asin(v)));
    }

    public AntlrQueryValue acos(final AntlrQueryVisitingContext context, final List<AntlrQueryValue> args) {
        if (args.size() != 1) return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        final var arg = args.get(0);
        if (!arg.isNumeric) return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        final double v = arg.numericValue.doubleValue();
        if (v < -1 || v > 1) return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        return valueFactory.number(BigDecimal.valueOf(Math.acos(v)));
    }

    public AntlrQueryValue atan(final AntlrQueryVisitingContext context, final List<AntlrQueryValue> args) {
        if (args.size() != 1) return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        final var arg = args.get(0);
        if (!arg.isNumeric) return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        return valueFactory.number(BigDecimal.valueOf(Math.atan(arg.numericValue.doubleValue())));
    }

    public AntlrQueryValue atan2(final AntlrQueryVisitingContext context, final List<AntlrQueryValue> args) {
        if (args.size() != 2) return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        final var y = args.get(0);
        final var x = args.get(1);
        if (!y.isNumeric || !x.isNumeric) return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        return valueFactory.number(BigDecimal.valueOf(Math.atan2(y.numericValue.doubleValue(), x.numericValue.doubleValue())));
    }
    public AntlrQueryValue sinh(final AntlrQueryVisitingContext context, final List<AntlrQueryValue> args) {
        if (args.size() != 1) {
            return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        }
        final var arg = args.get(0);
        if (!arg.isNumeric) {
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        }
        final double v = arg.numericValue.doubleValue();
        return valueFactory.number(BigDecimal.valueOf(Math.sinh(v)));
    }

    public AntlrQueryValue cosh(final AntlrQueryVisitingContext context, final List<AntlrQueryValue> args) {
        if (args.size() != 1) {
            return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        }
        final var arg = args.get(0);
        if (!arg.isNumeric) {
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        }
        final double v = arg.numericValue.doubleValue();
        return valueFactory.number(BigDecimal.valueOf(Math.cosh(v)));
    }

    public AntlrQueryValue tanh(final AntlrQueryVisitingContext context, final List<AntlrQueryValue> args) {
        if (args.size() != 1) {
            return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        }
        final var arg = args.get(0);
        if (!arg.isNumeric) {
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        }
        final double v = arg.numericValue.doubleValue();
        return valueFactory.number(BigDecimal.valueOf(Math.tanh(v)));
    }






}
