package com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.defaults;

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
import com.github.akruk.antlrxquery.values.XQueryBoolean;
import com.github.akruk.antlrxquery.values.XQueryFunction;
import com.github.akruk.antlrxquery.values.XQueryValue;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

public class BaseSemanticFunctionCaller implements XQueryFunctionCaller {

    private final Map<String, XQueryFunction> functions;
    public BaseSemanticFunctionCaller() {
        functions = new HashMap<>(200);
        functions.put("true", this::true_);
        functions.put("false", this::false_);
        functions.put("not", this::not);
        functions.put("abs", this::abs);
        functions.put("ceiling", this::ceiling);
        functions.put("floor", this::floor);
        functions.put("round", this::round);
        functions.put("empty", this::empty);
        functions.put("exists", this::exists);
        functions.put("head", this::head);
        functions.put("tail", this::tail);
        functions.put("insert-before", this::insertBefore);
        functions.put("remove", this::remove);
        functions.put("reverse", this::reverse);
        functions.put("subsequence", this::subsequence);
        functions.put("substring", this::substring);
        functions.put("distinct-values", this::distinctValues);
        functions.put("zero-or-one", this::zeroOrOne);
        functions.put("one-or-more", this::oneOrMore);
        functions.put("exactly-one", this::exactlyOne);
        functions.put("data", this::data);
        functions.put("contains", this::contains);
        functions.put("starts-with", this::startsWith);
        functions.put("ends-with", this::endsWith);
        functions.put("substring-after", this::substringAfter);
        functions.put("substring-before", this::substringBefore);
        functions.put("upper-case", this::uppercase);
        functions.put("lower-case", this::lowercase);
        functions.put("string", this::string);
        functions.put("concat", this::concat);
        functions.put("string-join", this::stringJoin);
        functions.put("string-length", this::stringLength);
        functions.put("normalize-space", this::normalizeSpace);
        functions.put("replace", this::replace);
        functions.put("position", this::position);
        functions.put("last", this::last);
        functions.put("fn:true", this::true_);
        functions.put("fn:false", this::false_);
        functions.put("fn:not", this::not);
        functions.put("fn:abs", this::abs);
        functions.put("fn:ceiling", this::ceiling);
        functions.put("fn:floor", this::floor);
        functions.put("fn:round", this::round);
        functions.put("fn:empty", this::empty);
        functions.put("fn:exists", this::exists);
        functions.put("fn:head", this::head);
        functions.put("fn:tail", this::tail);
        functions.put("fn:insert-before", this::insertBefore);
        functions.put("fn:remove", this::remove);
        functions.put("fn:reverse", this::reverse);
        functions.put("fn:subsequence", this::subsequence);
        functions.put("fn:substring", this::substring);
        functions.put("fn:distinct-values", this::distinctValues);
        functions.put("fn:zero-or-one", this::zeroOrOne);
        functions.put("fn:one-or-more", this::oneOrMore);
        functions.put("fn:exactly-one", this::exactlyOne);
        functions.put("fn:data", this::data);
        functions.put("fn:contains", this::contains);
        functions.put("fn:starts-with", this::startsWith);
        functions.put("fn:ends-with", this::endsWith);
        functions.put("fn:substring-after", this::substringAfter);
        functions.put("fn:substring-before", this::substringBefore);
        functions.put("fn:upper-case", this::uppercase);
        functions.put("fn:lower-case", this::lowercase);
        functions.put("fn:string", this::string);
        functions.put("fn:concat", this::concat);
        functions.put("fn:string-join", this::stringJoin);
        functions.put("fn:string-length", this::stringLength);
        functions.put("fn:normalize-space", this::normalizeSpace);
        functions.put("fn:replace", this::replace);
        functions.put("fn:position", this::position);
        functions.put("fn:last", this::last);

        functions.put("pi", this::pi);
        functions.put("math:pi", this::pi);
        // functions.put("exp", this::exp);
        // functions.put("math:exp", this::exp);
        // functions.put("exp10", this::exp10);
        // functions.put("math:exp10", this::exp10);
        // functions.put("log", this::log);
        // functions.put("math:log", this::log);
        // functions.put("log10", this::log10);
        // functions.put("math:log10", this::log10);
        // functions.put("pow", this::pow);
        // functions.put("math:pow", this::pow);
        // functions.put("sqrt", this::sqrt);
        // functions.put("math:sqrt", this::sqrt);
        // functions.put("sin", this::sin);
        // functions.put("math:sin", this::sin);
        // functions.put("cos", this::cos);
        // functions.put("math:cos", this::cos);
        // functions.put("tan", this::tan);
        // functions.put("math:tan", this::tan);
        // functions.put("asin", this::asin);
        // functions.put("math:asin", this::asin);
        // functions.put("acos", this::acos);
        // functions.put("math:acos", this::acos);
        // functions.put("atan", this::atan);
        // functions.put("math:atan", this::atan);
        // functions.put("atan2", this::atan2);
        // functions.put("math:atan2", this::atan2);

        functions.put("numeric-add", this::numericAdd);
        functions.put("numeric-subtract", this::numericSubtract);
        functions.put("numeric-multiply", this::numericMultiply);
        functions.put("numeric-divide", this::numericDivide);
        functions.put("numeric-integer-divide", this::numericIntegerDivide);
        functions.put("numeric-mod", this::numericMod);
        functions.put("numeric-unary-plus", this::numericUnaryPlus);
        functions.put("numeric-unary-minus", this::numericUnaryMinus);
        functions.put("op:numeric-add", this::numericAdd);
        functions.put("op:numeric-subtract", this::numericSubtract);
        functions.put("op:numeric-multiply", this::numericMultiply);
        functions.put("op:numeric-divide", this::numericDivide);
        functions.put("op:numeric-integer-divide", this::numericIntegerDivide);
        functions.put("op:numeric-mod", this::numericMod);
        functions.put("op:numeric-unary-plus", this::numericUnaryPlus);
        functions.put("op:numeric-unary-minus", this::numericUnaryMinus);

    }

    public XQueryValue not(final XQueryValueFactory valueFactory, final XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        try {
            return args.get(0).not(valueFactory);
        } catch (final XQueryUnsupportedOperation e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    // fn:abs($arg as xs:numeric?) as xs:numeric?
    public XQueryValue abs(final XQueryValueFactory valueFactory, final XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        final var arg = args.get(0);
        // TODO: Add type check failure
        if (!arg.isNumericValue())
            return null;
        return valueFactory.number(arg.numericValue().abs());
    }

    public XQueryValue ceiling(final XQueryValueFactory valueFactory, final XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        final var arg = args.get(0);
        // TODO: Add type check failure
        if (!arg.isNumericValue())
            return null;
        return valueFactory.number(arg.numericValue().setScale(0, RoundingMode.CEILING));
    }

    public XQueryValue floor(final XQueryValueFactory valueFactory, final XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        final var arg = args.get(0);
        // TODO: Add type check failure
        if (!arg.isNumericValue())
            return null;
        return valueFactory.number(arg.numericValue().setScale(0, RoundingMode.FLOOR));
    }

    public XQueryValue round(final XQueryValueFactory valueFactory, final XQueryVisitingContext context, final List<XQueryValue> args) {
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

    public XQueryValue numericAdd(final XQueryValueFactory valueFactory, final XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 2;
        final var val1 = args.get(0);
        final var val2 = args.get(1);
        // TODO: Add type check failure
        if (!val1.isNumericValue() || !val2.isNumericValue())
            return null;
        try {
            return val1.add(valueFactory, val2);
        } catch (final XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue numericSubtract(final XQueryValueFactory valueFactory, final XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 2;
        final var val1 = args.get(0);
        final var val2 = args.get(1);
        // TODO: Add type check failure
        if (!val1.isNumericValue() || !val2.isNumericValue())
            return null;
        try {
            return val1.subtract(valueFactory, val2);
        } catch (final XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue numericMultiply(final XQueryValueFactory valueFactory, final XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 2;
        final var val1 = args.get(0);
        final var val2 = args.get(1);
        // TODO: Add type check failure
        if (!val1.isNumericValue() || !val2.isNumericValue())
            return null;
        try {
            return val1.multiply(valueFactory, val2);
        } catch (final XQueryUnsupportedOperation e) {
            return null;
        }
    }


    public XQueryValue numericDivide(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 2;
        final var val1 = args.get(0);
        final var val2 = args.get(1);
        // TODO: Add type check failure
        if (!val1.isNumericValue() || !val2.isNumericValue())
            return null;
        try {
            return val1.divide(valueFactory, val2);
        } catch (final XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue numericIntegerDivide(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 2;
        final var val1 = args.get(0);
        final var val2 = args.get(1);
        // TODO: Add type check failure
        if (!val1.isNumericValue() || !val2.isNumericValue())
            return null;
        try {
            return val1.integerDivide(valueFactory, val2);
        } catch (final XQueryUnsupportedOperation e) {
            return null;
        }
    }


    public XQueryValue numericMod(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 2;
        final var val1 = args.get(0);
        final var val2 = args.get(1);
        // TODO: Add type check failure
        if (!val1.isNumericValue() || !val2.isNumericValue())
            return null;
        try {
            return val1.modulus(valueFactory, val2);
        } catch (final XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue numericUnaryPlus(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        final var val1 = args.get(0);
        // TODO: Add type check failure
        if (!val1.isNumericValue())
            return null;
        return val1;
    }

    public XQueryValue numericUnaryMinus(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        final var val1 = args.get(0);
        // TODO: Add type check failure
        if (!val1.isNumericValue())
            return null;
        return valueFactory.number(val1.numericValue().negate());
    }

    public XQueryValue true_(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 0;
        return XQueryBoolean.TRUE;
    }

    public XQueryValue false_(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 0;
        return XQueryBoolean.FALSE;
    }

    public XQueryValue pi(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 0;
        return valueFactory.number(new BigDecimal(Math.PI));
    }

    public XQueryValue empty(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        var arg = args.get(0);
        try {
            return arg.empty(valueFactory);
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue exists(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        try {
            return empty(valueFactory, context, args).not(valueFactory);
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue head(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        try {
            return args.get(0).head(valueFactory);
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue tail(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        try {
            return args.get(0).tail(valueFactory);
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }


    public XQueryValue insertBefore(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 3;
        try {
            var target = args.get(0);
            var position = args.get(1);
            var inserts = args.get(2);
            return target.insertBefore(valueFactory, position, inserts);
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue remove(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 2;
        try {
            var target = args.get(0);
            var position = args.get(1);
            return target.remove(valueFactory, position);
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue reverse(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        try {
            var target = args.get(0);
            if (target.isAtomic()) {
                return valueFactory.sequence(List.of(target));
            }
            return target.reverse(valueFactory);
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }


    public XQueryValue subsequence(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args) {
        try {
            return switch (args.size()) {
                case 3 -> {
                    var target = args.get(0);
                    var position = args.get(1).numericValue().intValue();
                    var length = args.get(2).numericValue().intValue();
                    yield target.subsequence(valueFactory, position, length);
                }
                case 2 -> {
                    var target = args.get(0);
                    var position = args.get(1).numericValue().intValue();
                    yield target.subsequence(valueFactory, position);
                }
                default -> null;
            };
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue substring(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args) {
        try {
            return switch (args.size()) {
                case 3 -> {
                    var target = args.get(0);
                    var position = args.get(1).numericValue().intValue();
                    var length = args.get(2).numericValue().intValue();
                    yield target.substring(valueFactory, position, length);
                }
                case 2 -> {
                    var target = args.get(0);
                    var position = args.get(1).numericValue().intValue();
                    yield target.substring(valueFactory, position);
                }
                default -> null;
            };
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue distinctValues(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        try {
            var target = args.get(0);
            return target.distinctValues(valueFactory);
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue zeroOrOne(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        try {
            var target = args.get(0);
            return target.zeroOrOne(valueFactory);
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue oneOrMore(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        try {
            var target = args.get(0);
            return target.oneOrMore(valueFactory);
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue exactlyOne(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        try {
            var target = args.get(0);
            return target.exactlyOne(valueFactory);
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue data(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        try {
            var target = args.get(0);
            return target.data(valueFactory);
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue contains(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 2;
        try {
            var target = args.get(0);
            var what = args.get(1);
            return target.contains(valueFactory, what);
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue startsWith(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 2;
        try {
            var target = args.get(0);
            var what = args.get(1);
            return target.startsWith(valueFactory, what);
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue endsWith(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 2;
        try {
            var target = args.get(0);
            var what = args.get(1);
            return target.endsWith(valueFactory, what);
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }


    public XQueryValue substringAfter(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 2;
        try {
            var target = args.get(0);
            var what = args.get(1);
            return target.substringAfter(valueFactory, what);
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }


    public XQueryValue substringBefore(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 2;
        try {
            var target = args.get(0);
            var what = args.get(1);
            return target.substringBefore(valueFactory, what);
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }


    public XQueryValue uppercase(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        try {
            var target = args.get(0);
            return target.uppercase(valueFactory);
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }


    public XQueryValue lowercase(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 1;
        try {
            var target = args.get(0);
            return target.lowercase(valueFactory);
        } catch (XQueryUnsupportedOperation e) {
            return null;
        }
    }

    public XQueryValue string(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args) {
        var target = switch (args.size()) {
            case 0 -> context.getItem();
            case 1 -> args.get(0);
            default -> null;
        };
        return valueFactory.string(target.stringValue());
    }

    public XQueryValue concat(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() >= 2;
        String joined = args.stream().map(XQueryValue::stringValue).collect(Collectors.joining());
        return valueFactory.string(joined);
    }

    public XQueryValue stringJoin(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args) {
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

    public XQueryValue position(final XQueryValueFactory valueFactory, final XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 0;
        return valueFactory.number(context.getPosition());
    }

    public XQueryValue last(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args) {
        assert args.size() == 0;
        return valueFactory.number(context.getSize());
    }

    public XQueryValue stringLength(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args) {
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
    public XQueryValue normalizeSpace(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args) {
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

    public XQueryValue replace(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args) {
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

    @Override
    public XQueryValue call(String functionName, XQueryValueFactory valueFactory, XQueryVisitingContext context,
            List<XQueryValue> args) {
        return functions.get(functionName).call(valueFactory, context, args);
    }

    @Override
    public XQueryValue getFunctionReference(String functionName, XQueryValueFactory valueFactory) {
        return valueFactory.functionReference(functions.get(functionName));
    }

}
