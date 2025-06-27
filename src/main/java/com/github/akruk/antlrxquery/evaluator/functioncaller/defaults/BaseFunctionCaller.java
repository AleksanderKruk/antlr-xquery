package com.github.akruk.antlrxquery.evaluator.functioncaller.defaults;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;
import com.github.akruk.antlrxquery.evaluator.functioncaller.XQueryFunctionCaller;
import com.github.akruk.antlrxquery.values.XQueryError;
import com.github.akruk.antlrxquery.values.XQueryFunction;
import com.github.akruk.antlrxquery.values.XQueryValue;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

public class BaseFunctionCaller implements XQueryFunctionCaller {

    private final Map<String, Map<String, XQueryFunction>> namespaceFunctions;
    private final XQueryValueFactory valueFactory;

    public BaseFunctionCaller(final XQueryValueFactory valueFactory) {
        this.valueFactory = valueFactory;
        this.namespaceFunctions = new HashMap<>(500);

        registerFunction("fn", "true", this::true_);
        registerFunction("fn", "false", this::false_);
        registerFunction("fn", "not", this::not);
        registerFunction("fn", "abs", this::abs);
        registerFunction("fn", "ceiling", this::ceiling);
        registerFunction("fn", "floor", this::floor);
        registerFunction("fn", "round", this::round);
        registerFunction("fn", "empty", this::empty);
        registerFunction("fn", "exists", this::exists);
        registerFunction("fn", "head", this::head);
        registerFunction("fn", "tail", this::tail);
        registerFunction("fn", "insert-before", this::insertBefore);
        registerFunction("fn", "remove", this::remove);
        registerFunction("fn", "reverse", this::reverse);
        registerFunction("fn", "subsequence", this::subsequence);
        registerFunction("fn", "substring", this::substring);
        registerFunction("fn", "distinct-values", this::distinctValues);
        registerFunction("fn", "zero-or-one", this::zeroOrOne);
        registerFunction("fn", "one-or-more", this::oneOrMore);
        registerFunction("fn", "exactly-one", this::exactlyOne);
        registerFunction("fn", "data", this::data);
        registerFunction("fn", "contains", this::contains);
        registerFunction("fn", "starts-with", this::startsWith);
        registerFunction("fn", "ends-with", this::endsWith);
        registerFunction("fn", "substring-after", this::substringAfter);
        registerFunction("fn", "substring-before", this::substringBefore);
        registerFunction("fn", "upper-case", this::uppercase);
        registerFunction("fn", "lower-case", this::lowercase);
        registerFunction("fn", "string", this::string);
        registerFunction("fn", "concat", this::concat);
        registerFunction("fn", "string-join", this::stringJoin);
        registerFunction("fn", "string-length", this::stringLength);
        registerFunction("fn", "normalize-space", this::normalizeSpace);
        registerFunction("fn", "replace", this::replace);
        registerFunction("fn", "position", this::position);
        registerFunction("fn", "last", this::last);

        registerFunction("math", "pi", this::pi);
        registerFunction("math", "exp", this::exp);
        registerFunction("math", "exp10", this::exp10);
        registerFunction("math", "log", this::log);
        registerFunction("math", "log10", this::log10);
        registerFunction("math", "pow", this::pow);
        registerFunction("math", "sqrt", this::sqrt);
        registerFunction("math", "sin", this::sin);
        registerFunction("math", "cos", this::cos);
        registerFunction("math", "tan", this::tan);
        registerFunction("math", "asin", this::asin);
        registerFunction("math", "acos", this::acos);
        registerFunction("math", "atan", this::atan);
        registerFunction("math", "atan2", this::atan2);

        registerFunction("op", "numeric-add", this::numericAdd);
        registerFunction("op", "numeric-subtract", this::numericSubtract);
        registerFunction("op", "numeric-multiply", this::numericMultiply);
        registerFunction("op", "numeric-divide", this::numericDivide);
        registerFunction("op", "numeric-integer-divide", this::numericIntegerDivide);
        registerFunction("op", "numeric-mod", this::numericMod);
        registerFunction("op", "numeric-unary-plus", this::numericUnaryPlus);
        registerFunction("op", "numeric-unary-minus", this::numericUnaryMinus);
    }

    public XQueryValue not(XQueryVisitingContext context, List<XQueryValue> args) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        return args.get(0).not();
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


    public XQueryValue abs(XQueryVisitingContext context, List<XQueryValue> args) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        var arg = args.get(0);
        if (!arg.isNumericValue()) return XQueryError.InvalidArgumentType;
        return valueFactory.number(arg.numericValue().abs());
    }

    public XQueryValue ceiling(XQueryVisitingContext context, List<XQueryValue> args) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        var arg = args.get(0);
        if (!arg.isNumericValue()) return XQueryError.InvalidArgumentType;
        return valueFactory.number(arg.numericValue().setScale(0, RoundingMode.CEILING));
    }

    public XQueryValue floor(XQueryVisitingContext context, List<XQueryValue> args) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        var arg = args.get(0);
        if (!arg.isNumericValue()) return XQueryError.InvalidArgumentType;
        return valueFactory.number(arg.numericValue().setScale(0, RoundingMode.FLOOR));
    }

    public XQueryValue round(XQueryVisitingContext context, List<XQueryValue> args) {
        if (args.size() < 1 || args.size() > 2) return XQueryError.WrongNumberOfArguments;
        var arg = args.get(0);
        if (!arg.isNumericValue()) return XQueryError.InvalidArgumentType;
        var number = arg.numericValue();
        boolean isNegative = number.signum() < 0;

        int scale = 0;
        if (args.size() == 2) {
            var scaleArg = args.get(1);
            if (!scaleArg.isNumericValue()) return XQueryError.InvalidArgumentType;
            scale = scaleArg.numericValue().intValue();
        }

        RoundingMode mode = isNegative ? RoundingMode.HALF_DOWN : RoundingMode.HALF_UP;
        try {
            return valueFactory.number(number.setScale(scale, mode));
        } catch (ArithmeticException e) {
            return XQueryError.NumericOverflowUnderflow;
        }
    }

    public XQueryValue numericAdd(XQueryVisitingContext context, List<XQueryValue> args) {
        if (args.size() != 2) return XQueryError.WrongNumberOfArguments;
        var a = args.get(0); var b = args.get(1);
        if (!a.isNumericValue() || !b.isNumericValue()) return XQueryError.InvalidArgumentType;
        return a.add(b);
    }

    public XQueryValue numericSubtract(XQueryVisitingContext context, List<XQueryValue> args) {
        if (args.size() != 2) return XQueryError.WrongNumberOfArguments;
        var a = args.get(0); var b = args.get(1);
        if (!a.isNumericValue() || !b.isNumericValue()) return XQueryError.InvalidArgumentType;
        return a.subtract(b);
    }

    public XQueryValue numericMultiply(XQueryVisitingContext context, List<XQueryValue> args) {
        if (args.size() != 2) return XQueryError.WrongNumberOfArguments;
        var a = args.get(0); var b = args.get(1);
        if (!a.isNumericValue() || !b.isNumericValue()) return XQueryError.InvalidArgumentType;
        return a.multiply(b);
    }

    public XQueryValue numericDivide(XQueryVisitingContext context, List<XQueryValue> args) {
        if (args.size() != 2) return XQueryError.WrongNumberOfArguments;
        var a = args.get(0); var b = args.get(1);
        if (!a.isNumericValue() || !b.isNumericValue()) return XQueryError.InvalidArgumentType;
        return a.divide(b);
    }

    public XQueryValue numericIntegerDivide(XQueryVisitingContext context, List<XQueryValue> args) {
        if (args.size() != 2) return XQueryError.WrongNumberOfArguments;
        var a = args.get(0); var b = args.get(1);
        if (!a.isNumericValue() || !b.isNumericValue()) return XQueryError.InvalidArgumentType;
        return a.integerDivide(b);
    }

    public XQueryValue numericMod(XQueryVisitingContext context, List<XQueryValue> args) {
        if (args.size() != 2) return XQueryError.WrongNumberOfArguments;
        var a = args.get(0); var b = args.get(1);
        if (!a.isNumericValue() || !b.isNumericValue()) return XQueryError.InvalidArgumentType;
        return a.modulus(b);
    }

    public XQueryValue numericUnaryPlus(XQueryVisitingContext context, List<XQueryValue> args) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        var a = args.get(0);
        if (!a.isNumericValue()) return XQueryError.InvalidArgumentType;
        return a;
    }

    public XQueryValue numericUnaryMinus(XQueryVisitingContext context, List<XQueryValue> args) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        var a = args.get(0);
        if (!a.isNumericValue()) return XQueryError.InvalidArgumentType;
        return valueFactory.number(a.numericValue().negate());
    }

    public XQueryValue true_(XQueryVisitingContext context, List<XQueryValue> args) {
        if (!args.isEmpty()) return XQueryError.WrongNumberOfArguments;
        return valueFactory.bool(true);
    }

    public XQueryValue false_(XQueryVisitingContext context, List<XQueryValue> args) {
        if (!args.isEmpty()) return XQueryError.WrongNumberOfArguments;
        return valueFactory.bool(false);
    }

    public XQueryValue pi(XQueryVisitingContext context, List<XQueryValue> args) {
        if (!args.isEmpty()) return XQueryError.WrongNumberOfArguments;
        return valueFactory.number(BigDecimal.valueOf(Math.PI));
    }

    public XQueryValue empty(XQueryVisitingContext ctx, List<XQueryValue> args) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        return args.get(0).empty();
    }

    public XQueryValue exists(XQueryVisitingContext ctx, List<XQueryValue> args) {
        var result = empty(ctx, args);
        if (result.isError()) return result;
        return result.not();
    }

    public XQueryValue head(XQueryVisitingContext ctx, List<XQueryValue> args) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        return args.get(0).head();
    }

    public XQueryValue tail(XQueryVisitingContext ctx, List<XQueryValue> args) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        return args.get(0).tail();
    }

    public XQueryValue insertBefore(XQueryVisitingContext ctx, List<XQueryValue> args) {
        if (args.size() != 3) return XQueryError.WrongNumberOfArguments;
        return args.get(0).insertBefore(args.get(1), args.get(2));
    }

    public XQueryValue remove(XQueryVisitingContext ctx, List<XQueryValue> args) {
        if (args.size() != 2) return XQueryError.WrongNumberOfArguments;
        return args.get(0).remove(args.get(1));
    }

    public XQueryValue reverse(XQueryVisitingContext ctx, List<XQueryValue> args) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        var target = args.get(0);
        return target.isAtomic()
            ? valueFactory.sequence(List.of(target))
            : target.reverse();
    }

    public XQueryValue subsequence(XQueryVisitingContext ctx, List<XQueryValue> args) {
        if (args.size() == 2 || args.size() == 3) {
            var target = args.get(0);
            if (!args.get(1).isNumericValue()) return XQueryError.InvalidArgumentType;
            int position = args.get(1).numericValue().intValue();
            if (args.size() == 2) {
                return target.subsequence(position);
            } else {
                if (!args.get(2).isNumericValue()) return XQueryError.InvalidArgumentType;
                int length = args.get(2).numericValue().intValue();
                return target.subsequence(position, length);
            }
        }
        return XQueryError.WrongNumberOfArguments;
    }

    public XQueryValue substring(XQueryVisitingContext ctx, List<XQueryValue> args) {
        if (args.size() == 2 || args.size() == 3) {
            var target = args.get(0);
            if (!args.get(1).isNumericValue()) return XQueryError.InvalidArgumentType;
            int position = args.get(1).numericValue().intValue();
            if (args.size() == 2) {
                return target.substring(position);
            } else {
                if (!args.get(2).isNumericValue()) return XQueryError.InvalidArgumentType;
                int length = args.get(2).numericValue().intValue();
                return target.substring(position, length);
            }
        }
        return XQueryError.WrongNumberOfArguments;
    }

    public XQueryValue distinctValues(XQueryVisitingContext ctx, List<XQueryValue> args) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        return args.get(0).distinctValues();
    }

    public XQueryValue zeroOrOne(XQueryVisitingContext ctx, List<XQueryValue> args) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        var target = args.get(0);
        return target.isSequence() ? target.zeroOrOne() : target;
    }

    public XQueryValue oneOrMore(XQueryVisitingContext ctx, List<XQueryValue> args) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        var target = args.get(0);
        return target.isSequence() ? target.oneOrMore() : target;
    }

    public XQueryValue exactlyOne(XQueryVisitingContext ctx, List<XQueryValue> args) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        var target = args.get(0);
        return target.isSequence() ? target.exactlyOne() : target;
    }

    public XQueryValue data(XQueryVisitingContext ctx, List<XQueryValue> args) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        return args.get(0).data();
    }

    public XQueryValue contains(XQueryVisitingContext ctx, List<XQueryValue> args) {
        if (args.size() != 2) return XQueryError.WrongNumberOfArguments;
        return args.get(0).contains(args.get(1));
    }

    public XQueryValue startsWith(XQueryVisitingContext ctx, List<XQueryValue> args) {
        if (args.size() != 2) return XQueryError.WrongNumberOfArguments;
        return args.get(0).startsWith(args.get(1));
    }

    public XQueryValue endsWith(XQueryVisitingContext ctx, List<XQueryValue> args) {
        if (args.size() != 2) return XQueryError.WrongNumberOfArguments;
        return args.get(0).endsWith(args.get(1));
    }

    public XQueryValue substringAfter(XQueryVisitingContext ctx, List<XQueryValue> args) {
        if (args.size() != 2) return XQueryError.WrongNumberOfArguments;
        return args.get(0).substringAfter(args.get(1));
    }

    public XQueryValue substringBefore(XQueryVisitingContext ctx, List<XQueryValue> args) {
        if (args.size() != 2) return XQueryError.WrongNumberOfArguments;
        return args.get(0).substringBefore(args.get(1));
    }

    public XQueryValue uppercase(XQueryVisitingContext context, List<XQueryValue> args) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        return args.get(0).uppercase();
    }

    public XQueryValue lowercase(XQueryVisitingContext context, List<XQueryValue> args) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        return args.get(0).lowercase();
    }

    public XQueryValue string(XQueryVisitingContext context, List<XQueryValue> args) {
        if (args.size() > 1) return XQueryError.WrongNumberOfArguments;
        var target = args.isEmpty() ? context.getItem() : args.get(0);
        return valueFactory.string(target.stringValue());
    }

    public XQueryValue concat(XQueryVisitingContext context, List<XQueryValue> args) {
        if (args.size() < 2) return XQueryError.WrongNumberOfArguments;
        String joined = args.stream().map(XQueryValue::stringValue).collect(Collectors.joining());
        return valueFactory.string(joined);
    }

    public XQueryValue stringJoin(XQueryVisitingContext context, List<XQueryValue> args) {
        if (args.size() == 1) {
            var sequence = args.get(0).sequence();
            String joined = sequence.stream().map(XQueryValue::stringValue).collect(Collectors.joining());
            return valueFactory.string(joined);
        } else if (args.size() == 2) {
            var sequence = args.get(0).sequence();
            var delimiter = args.get(1).stringValue();
            String joined = sequence.stream().map(XQueryValue::stringValue).collect(Collectors.joining(delimiter));
            return valueFactory.string(joined);
        } else {
            return XQueryError.WrongNumberOfArguments;
        }
    }

    public XQueryValue position(XQueryVisitingContext context, List<XQueryValue> args) {
        if (!args.isEmpty()) return XQueryError.WrongNumberOfArguments;
        return valueFactory.number(context.getPosition());
    }

    public XQueryValue last(XQueryVisitingContext context, List<XQueryValue> args) {
        if (!args.isEmpty()) return XQueryError.WrongNumberOfArguments;
        return valueFactory.number(context.getSize());
    }

    public XQueryValue stringLength(XQueryVisitingContext context, List<XQueryValue> args) {
        if (args.size() == 0) {
            var str = context.getItem().stringValue();
            return valueFactory.number(str.length());
        } else if (args.size() == 1) {
            return valueFactory.number(args.get(0).stringValue().length());
        } else {
            return XQueryError.WrongNumberOfArguments;
        }
    }

    public Pattern whitespace = Pattern.compile("\\s+");
    public UnaryOperator<String> normalize = (String s) -> {
        var trimmed = s.trim();
        return whitespace.matcher(trimmed).replaceAll(" ");
    };
    public XQueryValue normalizeSpace(XQueryVisitingContext context, List<XQueryValue> args) {
        if (args.size() == 0) {
            String s = context.getItem().stringValue();
            return valueFactory.string(normalize.apply(s));
        } else if (args.size() == 1) {
            return valueFactory.string(normalize.apply(args.get(0).stringValue()));
        } else {
            return XQueryError.WrongNumberOfArguments;
        }
    }
    record ParseFlagsResult(int flags, String newPattern, String newReplacement) {}

    public ParseFlagsResult parseFlags(String flags, String pattern, String replacement) {
        int flagBitMap = 0;
        Set<Character> uniqueFlags = flags.chars().mapToObj(i->(char) i).collect(Collectors.toSet());
        for (char c : uniqueFlags) {
            flagBitMap = switch (c) {
                case 'q' -> {
                    pattern = Pattern.quote(pattern);
                    // TODO: more direct
                    replacement = Pattern.quote(replacement);
                    yield flagBitMap;
                }
                case 's' -> flagBitMap & ~Pattern.MULTILINE;
                case 'm' -> flagBitMap | Pattern.MULTILINE;
                case 'i' -> flagBitMap | Pattern.UNICODE_CASE;
                case 'x' -> flagBitMap | Pattern.COMMENTS;
                // case '0' -> ;
                // case '1' -> ;
                // case '2' -> ;
                // case '3' -> ;
                // case '4' -> ;
                // case '5' -> ;
                // case '6' -> ;
                // case '7' -> ;
                // case '8' -> ;
                // case '9' -> ;
                default -> flagBitMap;
            };
        }
        return new ParseFlagsResult(flagBitMap, pattern, replacement);
    }

    public XQueryValue replace(XQueryVisitingContext context, List<XQueryValue> args) {
        if (args.size() == 3) {
            try {
                String input = args.get(0).stringValue();
                String pattern = args.get(1).stringValue();
                String replacement = args.get(2).stringValue();
                String result = input.replaceAll(pattern, replacement);
                return valueFactory.string(result);
            } catch (Exception e) {
                return XQueryError.InvalidRegex;
            }
        } else if (args.size() == 4) {
            try {
                String input = args.get(0).stringValue();
                String pattern = args.get(1).stringValue();
                String replacement = args.get(2).stringValue();
                String flags = args.get(3).stringValue();
                var parsed = parseFlags(flags, pattern, replacement);
                Pattern compiled = Pattern.compile(parsed.newPattern(), parsed.flags());
                String result = compiled.matcher(input).replaceAll(parsed.newReplacement());
                return valueFactory.string(result);
            } catch (Exception e) {
                return XQueryError.InvalidRegexFlags;
            }
        } else {
            return XQueryError.WrongNumberOfArguments;
        }
    }
 
    private void registerFunction(String namespace, String localName, XQueryFunction function) {
        namespaceFunctions.computeIfAbsent(namespace, _ -> new HashMap<>()).put(localName, function);
    }

    private XQueryFunction getFunction(String functionName) {
        final String[] parts = functionName.split(":", 2);
        String namespace = parts.length == 2 ? parts[0] : "fn";
        String localName = parts.length == 2 ? parts[1] : parts[0];

        Map<String, XQueryFunction> functionsInNs = namespaceFunctions.get(namespace);
        if (functionsInNs == null) {
            return null;
        }
        return functionsInNs.get(localName);
    }


    @Override
    public XQueryValue call(String functionName, XQueryVisitingContext context, List<XQueryValue> args) {
        XQueryFunction function = getFunction(functionName);
        if (function == null) {
            throw new IllegalArgumentException("Unknown function: " + functionName);
        }
        return function.call(context, args);
    }

    @Override
    public XQueryValue getFunctionReference(String functionName) {
        XQueryFunction function = getFunction(functionName);
        if (function == null) {
            throw new IllegalArgumentException("Unknown function reference: " + functionName);
        }
        return valueFactory.functionReference(function);
    }

}
