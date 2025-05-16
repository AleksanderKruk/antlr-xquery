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
        public XQuerySequenceType call(final XQueryTypeFactory typeFactory,
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

    public XQuerySequenceType not(final XQueryTypeFactory valueFactory, final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // try {
        //     return args.get(0).not(valueFactory);
        // } catch (final XQueryUnsupportedOperation e) {
        //     // TODO Auto-generated catch block
        //     e.printStackTrace();
        //     return null;
        // }
    }

    // fn:abs($arg as xs:numeric?) as xs:numeric?
    public XQuerySequenceType abs(final XQueryTypeFactory valueFactory, final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // final var arg = args.get(0);
        // // TODO: Add type check failure
        // if (!arg.isNumericValue())
        //     return null;
        // return valueFactory.number(arg.numericValue().abs());
    }

    public XQuerySequenceType ceiling(final XQueryTypeFactory valueFactory, final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // final var arg = args.get(0);
        // // TODO: Add type check failure
        // if (!arg.isNumericValue())
        //     return null;
        // return valueFactory.number(arg.numericValue().setScale(0, RoundingMode.CEILING));
    }

    public XQuerySequenceType floor(final XQueryTypeFactory valueFactory, final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // final var arg = args.get(0);
        // // TODO: Add type check failure
        // if (!arg.isNumericValue())
        //     return null;
        // return valueFactory.number(arg.numericValue().setScale(0, RoundingMode.FLOOR));
    }

    public XQuerySequenceType round(final XQueryTypeFactory valueFactory, final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1 || args.size() == 2;
        // final var arg1 = args.get(0);
        // final var number1 = arg1.numericValue();
        // final var negativeNumber = number1.compareTo(BigDecimal.ZERO) == -1;
        // final var oneArg = args.size() == 1;
        // if (oneArg && negativeNumber) {
        //     return valueFactory.number(number1.setScale(0, RoundingMode.HALF_DOWN));
        // }
        // if (oneArg) {
        //     return valueFactory.number(number1.setScale(0, RoundingMode.HALF_UP));
        // }
        // final var number2 = args.get(1).numericValue();
        // final int scale = number2.intValue();
        // if (negativeNumber) {
        //     return valueFactory.number(arg1.numericValue().setScale(scale, RoundingMode.HALF_DOWN));
        // }
        // if (scale > 0) {
        //     final var roundedNumberNormalNotation = number1.setScale(scale, RoundingMode.HALF_UP);
        //     return valueFactory.number(roundedNumberNormalNotation);
        // }
        // final var roundedNumber = number1.setScale(scale, RoundingMode.HALF_UP);
        // final var roundedNumberNormalNotation = roundedNumber.setScale(0, RoundingMode.HALF_UP);
        // return valueFactory.number(roundedNumberNormalNotation);
    }

    public XQuerySequenceType numericAdd(final XQueryTypeFactory valueFactory, final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 2;
        // final var val1 = args.get(0);
        // final var val2 = args.get(1);
        // // TODO: Add type check failure
        // if (!val1.isNumericValue() || !val2.isNumericValue())
        //     return null;
        // try {
        //     return val1.add(valueFactory, val2);
        // } catch (final XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public XQuerySequenceType numericSubtract(final XQueryTypeFactory valueFactory, final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 2;
        // final var val1 = args.get(0);
        // final var val2 = args.get(1);
        // // TODO: Add type check failure
        // if (!val1.isNumericValue() || !val2.isNumericValue())
        //     return null;
        // try {
        //     return val1.subtract(valueFactory, val2);
        // } catch (final XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public XQuerySequenceType numericMultiply(final XQueryTypeFactory valueFactory, final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 2;
        // final var val1 = args.get(0);
        // final var val2 = args.get(1);
        // // TODO: Add type check failure
        // if (!val1.isNumericValue() || !val2.isNumericValue())
        //     return null;
        // try {
        //     return val1.multiply(valueFactory, val2);
        // } catch (final XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }


    public XQuerySequenceType numericDivide(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 2;
        // final var val1 = args.get(0);
        // final var val2 = args.get(1);
        // // TODO: Add type check failure
        // if (!val1.isNumericValue() || !val2.isNumericValue())
        //     return null;
        // try {
        //     return val1.divide(valueFactory, val2);
        // } catch (final XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public XQuerySequenceType numericIntegerDivide(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 2;
        // final var val1 = args.get(0);
        // final var val2 = args.get(1);
        // // TODO: Add type check failure
        // if (!val1.isNumericValue() || !val2.isNumericValue())
        //     return null;
        // try {
        //     return val1.integerDivide(valueFactory, val2);
        // } catch (final XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }


    public XQuerySequenceType numericMod(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 2;
        // final var val1 = args.get(0);
        // final var val2 = args.get(1);
        // // TODO: Add type check failure
        // if (!val1.isNumericValue() || !val2.isNumericValue())
        //     return null;
        // try {
        //     return val1.modulus(valueFactory, val2);
        // } catch (final XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public XQuerySequenceType numericUnaryPlus(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // final var val1 = args.get(0);
        // // TODO: Add type check failure
        // if (!val1.isNumericValue())
        //     return null;
        // return val1;
    }

    public XQuerySequenceType numericUnaryMinus(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // final var val1 = args.get(0);
        // // TODO: Add type check failure
        // if (!val1.isNumericValue())
        //     return null;
        // return valueFactory.number(val1.numericValue().negate());
    }

    public XQuerySequenceType true_(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 0;
        // return XQueryBoolean.TRUE;
    }

    public XQuerySequenceType false_(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 0;
        // return XQueryBoolean.FALSE;
    }

    public XQuerySequenceType pi(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 0;
        // return valueFactory.number(new BigDecimal(Math.PI));
    }

    public XQuerySequenceType empty(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // var arg = args.get(0);
        // try {
        //     return arg.empty(valueFactory);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public XQuerySequenceType exists(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // try {
        //     return empty(valueFactory, context, args).not(valueFactory);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public XQuerySequenceType head(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // try {
        //     return args.get(0).head(valueFactory);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public XQuerySequenceType tail(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // try {
        //     return args.get(0).tail(valueFactory);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }


    public XQuerySequenceType insertBefore(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 3;
        // try {
        //     var target = args.get(0);
        //     var position = args.get(1);
        //     var inserts = args.get(2);
        //     return target.insertBefore(valueFactory, position, inserts);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public XQuerySequenceType remove(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 2;
        // try {
        //     var target = args.get(0);
        //     var position = args.get(1);
        //     return target.remove(valueFactory, position);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public XQuerySequenceType reverse(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // try {
        //     var target = args.get(0);
        //     if (target.isAtomic()) {
        //         return valueFactory.sequence(List.of(target));
        //     }
        //     return target.reverse(valueFactory);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }


    public XQuerySequenceType subsequence(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // try {
        //     return switch (args.size()) {
        //         case 3 -> {
        //             var target = args.get(0);
        //             var position = args.get(1).numericValue().intValue();
        //             var length = args.get(2).numericValue().intValue();
        //             yield target.subsequence(valueFactory, position, length);
        //         }
        //         case 2 -> {
        //             var target = args.get(0);
        //             var position = args.get(1).numericValue().intValue();
        //             yield target.subsequence(valueFactory, position);
        //         }
        //         default -> null;
        //     };
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public XQuerySequenceType substring(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // try {
        //     return switch (args.size()) {
        //         case 3 -> {
        //             var target = args.get(0);
        //             var position = args.get(1).numericValue().intValue();
        //             var length = args.get(2).numericValue().intValue();
        //             yield target.substring(valueFactory, position, length);
        //         }
        //         case 2 -> {
        //             var target = args.get(0);
        //             var position = args.get(1).numericValue().intValue();
        //             yield target.substring(valueFactory, position);
        //         }
        //         default -> null;
        //     };
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public XQuerySequenceType distinctValues(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // try {
        //     var target = args.get(0);
        //     return target.distinctValues(valueFactory);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public XQuerySequenceType zeroOrOne(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // try {
        //     var target = args.get(0);
        //     return target.zeroOrOne(valueFactory);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public XQuerySequenceType oneOrMore(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // try {
        //     var target = args.get(0);
        //     return target.oneOrMore(valueFactory);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public XQuerySequenceType exactlyOne(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // try {
        //     var target = args.get(0);
        //     return target.exactlyOne(valueFactory);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public XQuerySequenceType data(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // try {
        //     var target = args.get(0);
        //     return target.data(valueFactory);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public XQuerySequenceType contains(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 2;
        // try {
        //     var target = args.get(0);
        //     var what = args.get(1);
        //     return target.contains(valueFactory, what);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public XQuerySequenceType startsWith(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 2;
        // try {
        //     var target = args.get(0);
        //     var what = args.get(1);
        //     return target.startsWith(valueFactory, what);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public XQuerySequenceType endsWith(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 2;
        // try {
        //     var target = args.get(0);
        //     var what = args.get(1);
        //     return target.endsWith(valueFactory, what);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }


    public XQuerySequenceType substringAfter(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 2;
        // try {
        //     var target = args.get(0);
        //     var what = args.get(1);
        //     return target.substringAfter(valueFactory, what);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }


    public XQuerySequenceType substringBefore(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 2;
        // try {
        //     var target = args.get(0);
        //     var what = args.get(1);
        //     return target.substringBefore(valueFactory, what);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }


    public XQuerySequenceType uppercase(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // try {
        //     var target = args.get(0);
        //     return target.uppercase(valueFactory);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }


    public XQuerySequenceType lowercase(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // try {
        //     var target = args.get(0);
        //     return target.lowercase(valueFactory);
        // } catch (XQueryUnsupportedOperation e) {
        //     return null;
        // }
    }

    public XQuerySequenceType string(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // var target = switch (args.size()) {
        //     case 0 -> context.getItem();
        //     case 1 -> args.get(0);
        //     default -> null;
        // };
        // return valueFactory.string(target.stringValue());
    }

    public XQuerySequenceType concat(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() >= 2;
        // String joined = args.stream().map(XQuerySequenceType::stringValue).collect(Collectors.joining());
        // return valueFactory.string(joined);
    }

    public XQuerySequenceType stringJoin(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // return switch (args.size()) {
        //     case 1 -> {
        //         var sequence = args.get(0).sequence();
        //         String joined = sequence.stream().map(XQuerySequenceType::stringValue).collect(Collectors.joining());
        //         yield valueFactory.string(joined);
        //     }
        //     case 2 -> {
        //         var sequence = args.get(0).sequence();
        //         var delimiter = args.get(1).stringValue();
        //         String joined = sequence.stream().map(XQuerySequenceType::stringValue).collect(Collectors.joining(delimiter));
        //         yield valueFactory.string(joined);
        //     }
        //     default -> null;
        // };
    }

    public XQuerySequenceType position(final XQueryTypeFactory valueFactory, final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 0;
        // return valueFactory.number(context.getPosition());
    }

    public XQuerySequenceType last(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 0;
        // return valueFactory.number(context.getSize());
    }

    public XQuerySequenceType stringLength(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // return switch (args.size()) {
        //     case 0 -> {
        //         var string = context.getItem().stringValue();
        //         yield valueFactory.number(string.length());
        //     }
        //     case 1 -> {
        //         var string = args.get(0).stringValue();
        //         yield valueFactory.number(string.length());
        //     }
        //     default -> null;
        // };
    }

    public Pattern whitespace = Pattern.compile("\\s+");
    public UnaryOperator<String> normalize = (String s) -> {
        var trimmed = s.trim();
        return whitespace.matcher(trimmed).replaceAll(" ");
    };
    public XQuerySequenceType normalizeSpace(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // return switch (args.size()) {
        //     case 0 -> {
        //         String string = context.getItem().stringValue();
        //         String normalized = normalize.apply(string);
        //         yield valueFactory.string(normalized);
        //     }
        //     case 1 -> {
        //         String string = args.get(0).stringValue();
        //         String normalized = normalize.apply(string);
        //         yield valueFactory.string(normalized);
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

    public XQuerySequenceType replace(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // return switch (args.size()) {
        //     case 3 -> {
        //         String input = args.get(0).stringValue();
        //         String pattern = args.get(1).stringValue();
        //         String replacement = args.get(2).stringValue();
        //         String result = input.replaceAll(pattern, replacement);
        //         yield valueFactory.string(result);
        //     }
        //     case 4 -> {
        //         String input = args.get(0).stringValue();
        //         String pattern = args.get(1).stringValue();
        //         String replacement = args.get(2).stringValue();
        //         String flags = args.get(3).stringValue();
        //         ParseFlagsResult parsedFlags = parseFlags(flags, pattern, replacement);
        //         var matcher = Pattern.compile(parsedFlags.newPattern(), parsedFlags.flags()).matcher(input);
        //         String result = matcher.replaceAll(parsedFlags.newReplacement());
        //         yield valueFactory.string(result);
        //     }
        //     default -> null;
        // };
    }

    @Override
    public XQuerySequenceType call(
        String functionName,
        XQueryTypeFactory valueFactory,
        XQueryVisitingSemanticContext context,
        List<XQuerySequenceType> args)
    {

        return null;
        // return functions.get(functionName).call(valueFactory, context, args);
    }

    @Override
    public XQuerySequenceType getFunctionReference(String functionName, XQueryTypeFactory valueFactory) {
        return null;
        // return valueFactory.functionReference(functions.get(functionName));
    }

}
