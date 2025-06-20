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
import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;
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

    public XQueryValue not(final XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        try {
            return args.get(0).not();
        } catch (final XQueryUnsupportedOperation e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    public XQueryValue exp(final XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        var arg = args.get(0);
        if (!arg.isNumericValue()) return null;
        return valueFactory.number(BigDecimal.valueOf(Math.exp(arg.numericValue().doubleValue())));
    }

    public XQueryValue exp10(final XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        var arg = args.get(0);
        if (!arg.isNumericValue()) return null;
        return valueFactory.number(BigDecimal.valueOf(Math.pow(10, arg.numericValue().doubleValue())));
    }

    public XQueryValue log(final XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        var arg = args.get(0);
        if (!arg.isNumericValue()) return null;
        return valueFactory.number(BigDecimal.valueOf(Math.log(arg.numericValue().doubleValue())));
    }

    public XQueryValue log10(final XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        var arg = args.get(0);
        if (!arg.isNumericValue()) return null;
        return valueFactory.number(BigDecimal.valueOf(Math.log10(arg.numericValue().doubleValue())));
    }

    public XQueryValue pow(final XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 2;
        var base = args.get(0);
        var exponent = args.get(1);
        if (!base.isNumericValue() || !exponent.isNumericValue()) return null;
        return valueFactory.number(BigDecimal.valueOf(Math.pow(base.numericValue().doubleValue(), exponent.numericValue().doubleValue())));
    }

    public XQueryValue sqrt(final XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        var arg = args.get(0);
        if (!arg.isNumericValue()) return null;
        return valueFactory.number(BigDecimal.valueOf(Math.sqrt(arg.numericValue().doubleValue())));
    }

    public XQueryValue sin(final XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        var arg = args.get(0);
        if (!arg.isNumericValue()) return null;
        return valueFactory.number(BigDecimal.valueOf(Math.sin(arg.numericValue().doubleValue())));
    }

    public XQueryValue cos(final XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        var arg = args.get(0);
        if (!arg.isNumericValue()) return null;
        return valueFactory.number(BigDecimal.valueOf(Math.cos(arg.numericValue().doubleValue())));
    }

    public XQueryValue tan(final XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        var arg = args.get(0);
        if (!arg.isNumericValue()) return null;
        return valueFactory.number(BigDecimal.valueOf(Math.tan(arg.numericValue().doubleValue())));
    }

    public XQueryValue asin(final XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        var arg = args.get(0);
        if (!arg.isNumericValue()) return null;
        return valueFactory.number(BigDecimal.valueOf(Math.asin(arg.numericValue().doubleValue())));
    }

    public XQueryValue acos(final XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        var arg = args.get(0);
        if (!arg.isNumericValue()) return null;
        return valueFactory.number(BigDecimal.valueOf(Math.acos(arg.numericValue().doubleValue())));
    }

    public XQueryValue atan(final XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        var arg = args.get(0);
        if (!arg.isNumericValue()) return null;
        return valueFactory.number(BigDecimal.valueOf(Math.atan(arg.numericValue().doubleValue())));
    }

    public XQueryValue atan2(final XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 2;
        var y = args.get(0);
        var x = args.get(1);
        if (!y.isNumericValue() || !x.isNumericValue()) return null;
        return valueFactory.number(BigDecimal.valueOf(Math.atan2(y.numericValue().doubleValue(), x.numericValue().doubleValue())));
    }



    // fn:abs($arg as xs:numeric?) as xs:numeric?
    public XQueryValue abs(final XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        final var arg = args.get(0);
        // TODO: Add type check failure
        if (!arg.isNumericValue())
            return null;
        return valueFactory.number(arg.numericValue().abs());
    }

    public XQueryValue ceiling(final XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        final var arg = args.get(0);
        // TODO: Add type check failure
        if (!arg.isNumericValue())
            return null;
        return valueFactory.number(arg.numericValue().setScale(0, RoundingMode.CEILING));
    }

    public XQueryValue floor(final XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        final var arg = args.get(0);
        // TODO: Add type check failure
        if (!arg.isNumericValue())
            return null;
        return valueFactory.number(arg.numericValue().setScale(0, RoundingMode.FLOOR));
    }

    public XQueryValue round(final XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1 || args.size() == 2;
        final var arg1 = args.get(0);
        final var number1 = arg1.numericValue();
        final var negativeNumber = number1.compareTo(BigDecimal.ZERO) == -1;
        final var oneArg = args.size() == 1;
        if (oneArg && negativeNumber) {
            return valueFactory.number(number1.setScale(0, RoundingMode.HALF_DOWN));
        }
        if (oneArg) {
            return valueFactory.number(number1.setScale(0, RoundingMode.HALF_UP));
        }
        final var number2 = args.get(1).numericValue();
        final int scale = number2.intValue();
        if (negativeNumber) {
            return valueFactory.number(arg1.numericValue().setScale(scale, RoundingMode.HALF_DOWN));
        }
        if (scale > 0) {
            final var roundedNumberNormalNotation = number1.setScale(scale, RoundingMode.HALF_UP);
            return valueFactory.number(roundedNumberNormalNotation);
        }
        final var roundedNumber = number1.setScale(scale, RoundingMode.HALF_UP);
        final var roundedNumberNormalNotation = roundedNumber.setScale(0, RoundingMode.HALF_UP);
        return valueFactory.number(roundedNumberNormalNotation);
    }





    public XQueryValue numericAdd(final XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 2;
        final var val1 = args.get(0);
        final var val2 = args.get(1);
        // TODO: Add type check failure
        if (!val1.isNumericValue() || !val2.isNumericValue())
            return null;
        try {
            return val1.add(val2);
        } catch (final XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue numericSubtract(final XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 2;
        final var val1 = args.get(0);
        final var val2 = args.get(1);
        // TODO: Add type check failure
        if (!val1.isNumericValue() || !val2.isNumericValue())
            return null;
        try {
            return val1.subtract(val2);
        } catch (final XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue numericMultiply(final XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 2;
        final var val1 = args.get(0);
        final var val2 = args.get(1);
        // TODO: Add type check failure
        if (!val1.isNumericValue() || !val2.isNumericValue())
            return null;
        try {
            return val1.multiply(val2);
        } catch (final XQueryUnsupportedOperation e) {
            return null;
        }
    }


    public XQueryValue numericDivide(XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 2;
        final var val1 = args.get(0);
        final var val2 = args.get(1);
        // TODO: Add type check failure
        if (!val1.isNumericValue() || !val2.isNumericValue())
            return null;
        try {
            return val1.divide(val2);
        } catch (final XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue numericIntegerDivide(XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 2;
        final var val1 = args.get(0);
        final var val2 = args.get(1);
        // TODO: Add type check failure
        if (!val1.isNumericValue() || !val2.isNumericValue())
            return null;
        try {
            return val1.integerDivide(val2);
        } catch (final XQueryUnsupportedOperation e) {
            return null;
        }
    }


    public XQueryValue numericMod(XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 2;
        final var val1 = args.get(0);
        final var val2 = args.get(1);
        // TODO: Add type check failure
        if (!val1.isNumericValue() || !val2.isNumericValue())
            return null;
        try {
            return val1.modulus(val2);
        } catch (final XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue numericUnaryPlus(XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        final var val1 = args.get(0);
        // TODO: Add type check failure
        if (!val1.isNumericValue())
            return null;
        return val1;
    }

    public XQueryValue numericUnaryMinus(XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        final var val1 = args.get(0);
        // TODO: Add type check failure
        if (!val1.isNumericValue())
            return null;
        return valueFactory.number(val1.numericValue().negate());
    }

    public XQueryValue true_(XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 0;
        return valueFactory.bool(true);
    }

    public XQueryValue false_(XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 0;
        return valueFactory.bool(false);
    }

    public XQueryValue pi(XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 0;
        return valueFactory.number(BigDecimal.valueOf(Math.PI));
    }

    public XQueryValue empty(XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        var arg = args.get(0);
        try {
            return arg.empty();
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue exists(XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        try {
            return empty(context, args).not();
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue head(XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        try {
            return args.get(0).head();
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue tail(XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        try {
            return args.get(0).tail();
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }


    public XQueryValue insertBefore(XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 3;
        try {
            var target = args.get(0);
            var position = args.get(1);
            var inserts = args.get(2);
            return target.insertBefore(position, inserts);
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue remove(XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 2;
        try {
            var target = args.get(0);
            var position = args.get(1);
            return target.remove(position);
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue reverse(XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        try {
            var target = args.get(0);
            if (target.isAtomic()) {
                return valueFactory.sequence(List.of(target));
            }
            return target.reverse();
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }


    public XQueryValue subsequence(XQueryVisitingContext context, final List<XQueryValue> args) {
        try {
            return switch (args.size()) {
                case 3 -> {
                    var target = args.get(0);
                    var position = args.get(1).numericValue().intValue();
                    var length = args.get(2).numericValue().intValue();
                    yield target.subsequence(position, length);
                }
                case 2 -> {
                    var target = args.get(0);
                    var position = args.get(1).numericValue().intValue();
                    yield target.subsequence(position);
                }
                default -> null;
            };
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue substring(XQueryVisitingContext context, final List<XQueryValue> args) {
        try {
            return switch (args.size()) {
                case 3 -> {
                    var target = args.get(0);
                    var position = args.get(1).numericValue().intValue();
                    var length = args.get(2).numericValue().intValue();
                    yield target.substring(position, length);
                }
                case 2 -> {
                    var target = args.get(0);
                    var position = args.get(1).numericValue().intValue();
                    yield target.substring(position);
                }
                default -> null;
            };
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue distinctValues(XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        try {
            var target = args.get(0);
            return target.distinctValues();
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue zeroOrOne(XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        try {
            var target = args.get(0);
            return target.zeroOrOne();
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue oneOrMore(XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        try {
            var target = args.get(0);
            return target.oneOrMore();
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue exactlyOne(XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        try {
            var target = args.get(0);
            return target.exactlyOne();
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue data(XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        try {
            var target = args.get(0);
            return target.data();
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue contains(XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 2;
        try {
            var target = args.get(0);
            var what = args.get(1);
            return target.contains(what);
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue startsWith(XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 2;
        try {
            var target = args.get(0);
            var what = args.get(1);
            return target.startsWith(what);
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue endsWith(XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 2;
        try {
            var target = args.get(0);
            var what = args.get(1);
            return target.endsWith(what);
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }


    public XQueryValue substringAfter(XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 2;
        try {
            var target = args.get(0);
            var what = args.get(1);
            return target.substringAfter(what);
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }


    public XQueryValue substringBefore(XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 2;
        try {
            var target = args.get(0);
            var what = args.get(1);
            return target.substringBefore(what);
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }


    public XQueryValue uppercase(XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        try {
            var target = args.get(0);
            return target.uppercase();
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }


    public XQueryValue lowercase(XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        try {
            var target = args.get(0);
            return target.lowercase();
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue string(XQueryVisitingContext context, final List<XQueryValue> args) {
        var target = switch (args.size()) {
            case 0 -> context.getItem();
            case 1 -> args.get(0);
            default -> null;
        };
        return valueFactory.string(target.stringValue());
    }

    public XQueryValue concat(XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() >= 2;
        String joined = args.stream().map(XQueryValue::stringValue).collect(Collectors.joining());
        return valueFactory.string(joined);
    }

    public XQueryValue stringJoin(XQueryVisitingContext context, final List<XQueryValue> args) {
        return switch (args.size()) {
            case 1 -> {
                var sequence = args.get(0).sequence();
                String joined = sequence.stream().map(XQueryValue::stringValue).collect(Collectors.joining());
                yield valueFactory.string(joined);
            }
            case 2 -> {
                var sequence = args.get(0).sequence();
                var delimiter = args.get(1).stringValue();
                String joined = sequence.stream().map(XQueryValue::stringValue).collect(Collectors.joining(delimiter));
                yield valueFactory.string(joined);
            }
            default -> null;
        };
    }

    public XQueryValue position(final XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 0;
        return valueFactory.number(context.getPosition());
    }

    public XQueryValue last(XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 0;
        return valueFactory.number(context.getSize());
    }

    public XQueryValue stringLength(XQueryVisitingContext context, final List<XQueryValue> args) {
        return switch (args.size()) {
            case 0 -> {
                var string = context.getItem().stringValue();
                yield valueFactory.number(string.length());
            }
            case 1 -> {
                var string = args.get(0).stringValue();
                yield valueFactory.number(string.length());
            }
            default -> null;
        };
    }

    public Pattern whitespace = Pattern.compile("\\s+");
    public UnaryOperator<String> normalize = (String s) -> {
        var trimmed = s.trim();
        return whitespace.matcher(trimmed).replaceAll(" ");
    };
    public XQueryValue normalizeSpace(XQueryVisitingContext context, final List<XQueryValue> args) {
        return switch (args.size()) {
            case 0 -> {
                String string = context.getItem().stringValue();
                String normalized = normalize.apply(string);
                yield valueFactory.string(normalized);
            }
            case 1 -> {
                String string = args.get(0).stringValue();
                String normalized = normalize.apply(string);
                yield valueFactory.string(normalized);
            }
            default -> null;
        };
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

    public XQueryValue replace(XQueryVisitingContext context, final List<XQueryValue> args) {
        return switch (args.size()) {
            case 3 -> {
                String input = args.get(0).stringValue();
                String pattern = args.get(1).stringValue();
                String replacement = args.get(2).stringValue();
                String result = input.replaceAll(pattern, replacement);
                yield valueFactory.string(result);
            }
            case 4 -> {
                String input = args.get(0).stringValue();
                String pattern = args.get(1).stringValue();
                String replacement = args.get(2).stringValue();
                String flags = args.get(3).stringValue();
                ParseFlagsResult parsedFlags = parseFlags(flags, pattern, replacement);
                var matcher = Pattern.compile(parsedFlags.newPattern(), parsedFlags.flags()).matcher(input);
                String result = matcher.replaceAll(parsedFlags.newReplacement());
                yield valueFactory.string(result);
            }
            default -> null;
        };
    }

    private void registerFunction(String namespace, String localName, XQueryFunction function) {
        namespaceFunctions.computeIfAbsent(namespace, k -> new HashMap<>()).put(localName, function);
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
