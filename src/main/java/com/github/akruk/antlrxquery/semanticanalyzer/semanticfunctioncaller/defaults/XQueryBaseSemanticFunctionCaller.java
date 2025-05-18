package com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.defaults;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.akruk.antlrxquery.semanticanalyzer.XQueryVisitingSemanticContext;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.XQuerySemanticFunctionCaller;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQueryBaseSemanticFunctionCaller implements XQuerySemanticFunctionCaller {


    public interface XQuerySemanticFunction {
        public CallAnalysisResult call(final XQueryTypeFactory typeFactory,
                                        final XQueryVisitingSemanticContext context,
                                        final List<XQuerySequenceType> types);
    }

    private final Map<String, XQuerySemanticFunction> functions;
    public XQueryBaseSemanticFunctionCaller() {
        functions = new HashMap<>(300);
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

    private String wrongNumberOfArguments(String functionName, int expected, int actual) {
        return "Wrong number of arguments for function" + functionName + " : expected " + expected + ", got " + actual;
    }


    public CallAnalysisResult not(final XQueryTypeFactory typeFactory, final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        if (args.size() != 1) {
            String message = wrongNumberOfArguments("fn:not()", 1, args.size());
            return new CallAnalysisResult(typeFactory.boolean_(), List.of(message));
        }
        return new CallAnalysisResult(typeFactory.boolean_(), List.of());
    }

    // fn:abs($arg as xs:numeric?) as xs:numeric?
    public CallAnalysisResult abs(final XQueryTypeFactory typeFactory, final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // final var arg = args.get(0);
        // // TODO: Add type check failure
        // if (!arg.isNumericValue())
        //     return null;
        // return typeFactory.number(arg.numericValue().abs());
    }

    public CallAnalysisResult ceiling(final XQueryTypeFactory typeFactory, final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // final var arg = args.get(0);
        // // TODO: Add type check failure
        // if (!arg.isNumericValue())
        //     return null;
        // return typeFactory.number(arg.numericValue().setScale(0, RoundingMode.CEILING));
    }

    public CallAnalysisResult floor(final XQueryTypeFactory typeFactory, final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // final var arg = args.get(0);
        // // TODO: Add type check failure
        // if (!arg.isNumericValue())
        //     return null;
        // return typeFactory.number(arg.numericValue().setScale(0, RoundingMode.FLOOR));
    }

    public CallAnalysisResult round(final XQueryTypeFactory typeFactory, final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1 || args.size() == 2;
        // final var arg1 = args.get(0);
        // final var number1 = arg1.numericValue();
        // final var negativeNumber = number1.compareTo(BigDecimal.ZERO) == -1;
        // final var oneArg = args.size() == 1;
        // if (oneArg && negativeNumber) {
        //     return typeFactory.number(number1.setScale(0, RoundingMode.HALF_DOWN));
        // }
        // if (oneArg) {
        //     return typeFactory.number(number1.setScale(0, RoundingMode.HALF_UP));
        // }
        // final var number2 = args.get(1).numericValue();
        // final int scale = number2.intValue();
        // if (negativeNumber) {
        //     return typeFactory.number(arg1.numericValue().setScale(scale, RoundingMode.HALF_DOWN));
        // }
        // if (scale > 0) {
        //     final var roundedNumberNormalNotation = number1.setScale(scale, RoundingMode.HALF_UP);
        //     return typeFactory.number(roundedNumberNormalNotation);
        // }
        // final var roundedNumber = number1.setScale(scale, RoundingMode.HALF_UP);
        // final var roundedNumberNormalNotation = roundedNumber.setScale(0, RoundingMode.HALF_UP);
        // return typeFactory.number(roundedNumberNormalNotation);
    }

    public CallAnalysisResult numericAdd(final XQueryTypeFactory typeFactory, final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 2;
        // final var val1 = args.get(0);
        // final var val2 = args.get(1);
        // // TODO: Add type check failure
        // if (!val1.isNumericValue() || !val2.isNumericValue())
        //     return null;
        // try {
        //     return val1.add(typeFactory, val2);
        // } catch (final XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public CallAnalysisResult numericSubtract(final XQueryTypeFactory typeFactory, final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 2;
        // final var val1 = args.get(0);
        // final var val2 = args.get(1);
        // // TODO: Add type check failure
        // if (!val1.isNumericValue() || !val2.isNumericValue())
        //     return null;
        // try {
        //     return val1.subtract(typeFactory, val2);
        // } catch (final XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public CallAnalysisResult numericMultiply(final XQueryTypeFactory typeFactory, final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 2;
        // final var val1 = args.get(0);
        // final var val2 = args.get(1);
        // // TODO: Add type check failure
        // if (!val1.isNumericValue() || !val2.isNumericValue())
        //     return null;
        // try {
        //     return val1.multiply(typeFactory, val2);
        // } catch (final XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }


    public CallAnalysisResult numericDivide(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 2;
        // final var val1 = args.get(0);
        // final var val2 = args.get(1);
        // // TODO: Add type check failure
        // if (!val1.isNumericValue() || !val2.isNumericValue())
        //     return null;
        // try {
        //     return val1.divide(typeFactory, val2);
        // } catch (final XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public CallAnalysisResult numericIntegerDivide(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 2;
        // final var val1 = args.get(0);
        // final var val2 = args.get(1);
        // // TODO: Add type check failure
        // if (!val1.isNumericValue() || !val2.isNumericValue())
        //     return null;
        // try {
        //     return val1.integerDivide(typeFactory, val2);
        // } catch (final XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }


    public CallAnalysisResult numericMod(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 2;
        // final var val1 = args.get(0);
        // final var val2 = args.get(1);
        // // TODO: Add type check failure
        // if (!val1.isNumericValue() || !val2.isNumericValue())
        //     return null;
        // try {
        //     return val1.modulus(typeFactory, val2);
        // } catch (final XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public CallAnalysisResult numericUnaryPlus(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // final var val1 = args.get(0);
        // // TODO: Add type check failure
        // if (!val1.isNumericValue())
        //     return null;
        // return val1;
    }

    public CallAnalysisResult numericUnaryMinus(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // final var val1 = args.get(0);
        // // TODO: Add type check failure
        // if (!val1.isNumericValue())
        //     return null;
        // return typeFactory.number(val1.numericValue().negate());
    }

    public CallAnalysisResult true_(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        if (args.size() != 0) {
            String message = wrongNumberOfArguments("fn:true()", 0, args.size());
            return new CallAnalysisResult(typeFactory.boolean_(), List.of(message));
        }
        return new CallAnalysisResult(typeFactory.boolean_(), List.of());
    }

    public CallAnalysisResult false_(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        if (args.size() != 0) {
            String message = wrongNumberOfArguments("fn:false()", 0, args.size());
            return new CallAnalysisResult(typeFactory.boolean_(), List.of(message));
        }
        return new CallAnalysisResult(typeFactory.boolean_(), List.of());
    }

    public CallAnalysisResult pi(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 0;
        // return typeFactory.number(new BigDecimal(Math.PI));
    }

    public CallAnalysisResult empty(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // var arg = args.get(0);
        // try {
        //     return arg.empty(typeFactory);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public CallAnalysisResult exists(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // try {
        //     return empty(typeFactory, context, args).not(typeFactory);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public CallAnalysisResult head(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // try {
        //     return args.get(0).head(typeFactory);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public CallAnalysisResult tail(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // try {
        //     return args.get(0).tail(typeFactory);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }


    public CallAnalysisResult insertBefore(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 3;
        // try {
        //     var target = args.get(0);
        //     var position = args.get(1);
        //     var inserts = args.get(2);
        //     return target.insertBefore(typeFactory, position, inserts);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public CallAnalysisResult remove(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 2;
        // try {
        //     var target = args.get(0);
        //     var position = args.get(1);
        //     return target.remove(typeFactory, position);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public CallAnalysisResult reverse(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // try {
        //     var target = args.get(0);
        //     if (target.isAtomic()) {
        //         return typeFactory.sequence(List.of(target));
        //     }
        //     return target.reverse(typeFactory);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }


    public CallAnalysisResult subsequence(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // try {
        //     return switch (args.size()) {
        //         case 3 -> {
        //             var target = args.get(0);
        //             var position = args.get(1).numericValue().intValue();
        //             var length = args.get(2).numericValue().intValue();
        //             yield target.subsequence(typeFactory, position, length);
        //         }
        //         case 2 -> {
        //             var target = args.get(0);
        //             var position = args.get(1).numericValue().intValue();
        //             yield target.subsequence(typeFactory, position);
        //         }
        //         default -> null;
        //     };
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public CallAnalysisResult substring(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // try {
        //     return switch (args.size()) {
        //         case 3 -> {
        //             var target = args.get(0);
        //             var position = args.get(1).numericValue().intValue();
        //             var length = args.get(2).numericValue().intValue();
        //             yield target.substring(typeFactory, position, length);
        //         }
        //         case 2 -> {
        //             var target = args.get(0);
        //             var position = args.get(1).numericValue().intValue();
        //             yield target.substring(typeFactory, position);
        //         }
        //         default -> null;
        //     };
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public CallAnalysisResult distinctValues(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // try {
        //     var target = args.get(0);
        //     return target.distinctValues(typeFactory);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public CallAnalysisResult zeroOrOne(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // try {
        //     var target = args.get(0);
        //     return target.zeroOrOne(typeFactory);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public CallAnalysisResult oneOrMore(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // try {
        //     var target = args.get(0);
        //     return target.oneOrMore(typeFactory);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public CallAnalysisResult exactlyOne(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // try {
        //     var target = args.get(0);
        //     return target.exactlyOne(typeFactory);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public CallAnalysisResult data(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // try {
        //     var target = args.get(0);
        //     return target.data(typeFactory);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public CallAnalysisResult contains(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 2;
        // try {
        //     var target = args.get(0);
        //     var what = args.get(1);
        //     return target.contains(typeFactory, what);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public CallAnalysisResult startsWith(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 2;
        // try {
        //     var target = args.get(0);
        //     var what = args.get(1);
        //     return target.startsWith(typeFactory, what);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public CallAnalysisResult endsWith(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 2;
        // try {
        //     var target = args.get(0);
        //     var what = args.get(1);
        //     return target.endsWith(typeFactory, what);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }


    public CallAnalysisResult substringAfter(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 2;
        // try {
        //     var target = args.get(0);
        //     var what = args.get(1);
        //     return target.substringAfter(typeFactory, what);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }


    public CallAnalysisResult substringBefore(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 2;
        // try {
        //     var target = args.get(0);
        //     var what = args.get(1);
        //     return target.substringBefore(typeFactory, what);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }


    public CallAnalysisResult uppercase(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // try {
        //     var target = args.get(0);
        //     return target.uppercase(typeFactory);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }


    public CallAnalysisResult lowercase(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // try {
        //     var target = args.get(0);
        //     return target.lowercase(typeFactory);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public CallAnalysisResult string(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // var target = switch (args.size()) {
        //     case 0 -> context.getItem();
        //     case 1 -> args.get(0);
        //     default -> null;
        // };
        // return typeFactory.string(target.stringValue());
    }

    public CallAnalysisResult concat(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() >= 2;
        // String joined = args.stream().map(XQuerySequenceType::stringValue).collect(Collectors.joining());
        // return typeFactory.string(joined);
    }

    public CallAnalysisResult stringJoin(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // return switch (args.size()) {
        //     case 1 -> {
        //         var sequence = args.get(0).sequence();
        //         String joined = sequence.stream().map(XQuerySequenceType::stringValue).collect(Collectors.joining());
        //         yield typeFactory.string(joined);
        //     }
        //     case 2 -> {
        //         var sequence = args.get(0).sequence();
        //         var delimiter = args.get(1).stringValue();
        //         String joined = sequence.stream().map(XQuerySequenceType::stringValue).collect(Collectors.joining(delimiter));
        //         yield typeFactory.string(joined);
        //     }
        //     default -> null;
        // };
    }

    public CallAnalysisResult position(final XQueryTypeFactory typeFactory, final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 0;
        // return typeFactory.number(context.getPosition());
    }

    public CallAnalysisResult last(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 0;
        // return typeFactory.number(context.getSize());
    }

    public CallAnalysisResult stringLength(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // return switch (args.size()) {
        //     case 0 -> {
        //         var string = context.getItem().stringValue();
        //         yield typeFactory.number(string.length());
        //     }
        //     case 1 -> {
        //         var string = args.get(0).stringValue();
        //         yield typeFactory.number(string.length());
        //     }
        //     default -> null;
        // };
    }

    public Pattern whitespace = Pattern.compile("\\s+");
    public UnaryOperator<String> normalize = (String s) -> {
        var trimmed = s.trim();
        return whitespace.matcher(trimmed).replaceAll(" ");
    };
    public CallAnalysisResult normalizeSpace(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // return switch (args.size()) {
        //     case 0 -> {
        //         String string = context.getItem().stringValue();
        //         String normalized = normalize.apply(string);
        //         yield typeFactory.string(normalized);
        //     }
        //     case 1 -> {
        //         String string = args.get(0).stringValue();
        //         String normalized = normalize.apply(string);
        //         yield typeFactory.string(normalized);
        //     }
        //     default -> null;
        // };
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

    public CallAnalysisResult replace(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // return switch (args.size()) {
        //     case 3 -> {
        //         String input = args.get(0).stringValue();
        //         String pattern = args.get(1).stringValue();
        //         String replacement = args.get(2).stringValue();
        //         String result = input.replaceAll(pattern, replacement);
        //         yield typeFactory.string(result);
        //     }
        //     case 4 -> {
        //         String input = args.get(0).stringValue();
        //         String pattern = args.get(1).stringValue();
        //         String replacement = args.get(2).stringValue();
        //         String flags = args.get(3).stringValue();
        //         ParseFlagsResult parsedFlags = parseFlags(flags, pattern, replacement);
        //         var matcher = Pattern.compile(parsedFlags.newPattern(), parsedFlags.flags()).matcher(input);
        //         String result = matcher.replaceAll(parsedFlags.newReplacement());
        //         yield typeFactory.string(result);
        //     }
        //     default -> null;
        // };
    }

    @Override
    public CallAnalysisResult call(
        final String functionName,
        final XQueryTypeFactory typeFactory,
        final XQueryVisitingSemanticContext context,
        final List<XQuerySequenceType> args)
    {

        // return null;
        return functions.get(functionName).call(typeFactory, context, args);
    }

    @Override
    public CallAnalysisResult getFunctionReference(String functionName, XQueryTypeFactory typeFactory) {
        return null;
        // return typeFactory.functionReference(functions.get(functionName));
    }

}
