package com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.defaults;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    private final FunctionTrie functionTrie = new FunctionTrie();

    private final XQueryTypeFactory typeFactory;

    public XQueryBaseSemanticFunctionCaller(final XQueryTypeFactory typeFactory) {
        this.typeFactory = typeFactory;
        functionTrie.register("fn", "true", typeFactory.one(typeFactory.itemBoolean()), List.of(), this::true_);
        functionTrie.register("fn", "false", typeFactory.one(typeFactory.itemBoolean()), List.of(), this::false_);
        functionTrie.register("fn", "not", typeFactory.one(typeFactory.itemBoolean()),
                List.of(typeFactory.one(typeFactory.itemBoolean())), this::not);
        functionTrie.register("fn", "abs", typeFactory.one(typeFactory.itemNumber()),
                List.of(typeFactory.one(typeFactory.itemNumber())), this::abs);
        functionTrie.register("fn", "ceiling", typeFactory.one(typeFactory.itemNumber()),
                List.of(typeFactory.one(typeFactory.itemNumber())), this::ceiling);
        functionTrie.register("fn", "floor", typeFactory.one(typeFactory.itemNumber()),
                List.of(typeFactory.one(typeFactory.itemNumber())), this::floor);
        functionTrie.register("fn", "round", typeFactory.one(typeFactory.itemNumber()),
                List.of(typeFactory.one(typeFactory.itemNumber())), this::round);
        functionTrie.register("fn", "round", typeFactory.one(typeFactory.itemNumber()),
                List.of(typeFactory.one(typeFactory.itemNumber()), typeFactory.one(typeFactory.itemNumber())),
                this::round);
        functionTrie.register("fn", "empty", typeFactory.one(typeFactory.itemBoolean()),
                List.of(typeFactory.zeroOrMore(typeFactory.itemAnyItem())), this::empty);
        functionTrie.register("fn", "exists", typeFactory.one(typeFactory.itemBoolean()),
                List.of(typeFactory.zeroOrMore(typeFactory.itemAnyItem())), this::exists);
        functionTrie.register("fn", "head", typeFactory.zeroOrOne(typeFactory.itemAnyItem()),
                List.of(typeFactory.zeroOrMore(typeFactory.itemAnyItem())), this::head);
        functionTrie.register("fn", "tail", typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                List.of(typeFactory.zeroOrMore(typeFactory.itemAnyItem())), this::tail);
        functionTrie.register("fn", "insert-before", typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                List.of(typeFactory.zeroOrMore(typeFactory.itemAnyItem()), typeFactory.one(typeFactory.itemNumber()),
                        typeFactory.zeroOrMore(typeFactory.itemAnyItem())),
                this::insertBefore);
        functionTrie.register("fn", "remove", typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                List.of(typeFactory.zeroOrMore(typeFactory.itemAnyItem()), typeFactory.one(typeFactory.itemNumber())),
                this::remove);
        functionTrie.register("fn", "reverse", typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                List.of(typeFactory.zeroOrMore(typeFactory.itemAnyItem())), this::reverse);
        functionTrie.register("fn", "subsequence", typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                List.of(typeFactory.zeroOrMore(typeFactory.itemAnyItem()), typeFactory.one(typeFactory.itemNumber())),
                this::subsequence);
        functionTrie
                .register("fn", "subsequence", typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                        List.of(typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                                typeFactory.one(typeFactory.itemNumber()), typeFactory.one(typeFactory.itemNumber())),
                        this::subsequence);
        functionTrie.register("fn", "substring", typeFactory.one(typeFactory.itemString()),
                List.of(typeFactory.one(typeFactory.itemString()), typeFactory.one(typeFactory.itemNumber())),
                this::substring);
        functionTrie.register("fn", "substring", typeFactory.one(typeFactory.itemString()),
                List.of(typeFactory.one(typeFactory.itemString()), typeFactory.one(typeFactory.itemNumber()),
                        typeFactory.one(typeFactory.itemNumber())),
                this::substring);
        functionTrie.register("fn", "distinct-values", typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                List.of(typeFactory.zeroOrMore(typeFactory.itemAnyItem())), this::distinctValues);
        functionTrie.register("fn", "zero-or-one", typeFactory.zeroOrOne(typeFactory.itemAnyItem()),
                List.of(typeFactory.zeroOrMore(typeFactory.itemAnyItem())), this::zeroOrOne);
        functionTrie.register("fn", "one-or-more", typeFactory.oneOrMore(typeFactory.itemAnyItem()),
                List.of(typeFactory.zeroOrMore(typeFactory.itemAnyItem())), this::oneOrMore);
        functionTrie.register("fn", "exactly-one", typeFactory.one(typeFactory.itemAnyItem()),
                List.of(typeFactory.zeroOrMore(typeFactory.itemAnyItem())), this::exactlyOne);
        functionTrie.register("fn", "data", typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                List.of(typeFactory.zeroOrMore(typeFactory.itemAnyItem())), this::data);
        functionTrie.register("fn", "contains", typeFactory.one(typeFactory.itemBoolean()),
                List.of(typeFactory.one(typeFactory.itemString()), typeFactory.one(typeFactory.itemString())),
                this::contains);
        functionTrie.register("fn", "starts-with", typeFactory.one(typeFactory.itemBoolean()),
                List.of(typeFactory.one(typeFactory.itemString()), typeFactory.one(typeFactory.itemString())),
                this::startsWith);
        functionTrie.register("fn", "ends-with", typeFactory.one(typeFactory.itemBoolean()),
                List.of(typeFactory.one(typeFactory.itemString()), typeFactory.one(typeFactory.itemString())),
                this::endsWith);
        functionTrie.register("fn", "substring-after", typeFactory.one(typeFactory.itemString()),
                List.of(typeFactory.one(typeFactory.itemString()), typeFactory.one(typeFactory.itemString())),
                this::substringAfter);
        functionTrie.register("fn", "substring-before", typeFactory.one(typeFactory.itemString()),
                List.of(typeFactory.one(typeFactory.itemString()), typeFactory.one(typeFactory.itemString())),
                this::substringBefore);
        functionTrie.register("fn", "upper-case", typeFactory.one(typeFactory.itemString()),
                List.of(typeFactory.one(typeFactory.itemString())), this::uppercase);
        functionTrie.register("fn", "lower-case", typeFactory.one(typeFactory.itemString()),
                List.of(typeFactory.one(typeFactory.itemString())), this::lowercase);
        functionTrie.register("fn", "string", typeFactory.one(typeFactory.itemString()), List.of(), this::string);
        functionTrie.register("fn", "string", typeFactory.one(typeFactory.itemString()),
                List.of(typeFactory.one(typeFactory.itemAnyItem())), this::string);
        functionTrie.register("fn", "concat", typeFactory.one(typeFactory.itemString()),
                List.of(typeFactory.one(typeFactory.itemString()), typeFactory.one(typeFactory.itemString())),
                this::concat);
        functionTrie.register("fn", "string-join", typeFactory.one(typeFactory.itemString()),
                List.of(typeFactory.zeroOrMore(typeFactory.itemString())), this::stringJoin);
        functionTrie.register("fn", "string-join", typeFactory.one(typeFactory.itemString()),
                List.of(typeFactory.zeroOrMore(typeFactory.itemString()), typeFactory.one(typeFactory.itemString())),
                this::stringJoin);
        functionTrie.register("fn", "string-length", typeFactory.one(typeFactory.itemNumber()), List.of(),
                this::stringLength);
        functionTrie.register("fn", "string-length", typeFactory.one(typeFactory.itemNumber()),
                List.of(typeFactory.one(typeFactory.itemString())), this::stringLength);
        functionTrie.register("fn", "normalize-space", typeFactory.one(typeFactory.itemString()), List.of(),
                this::normalizeSpace);
        functionTrie.register("fn", "normalize-space", typeFactory.one(typeFactory.itemString()),
                List.of(typeFactory.one(typeFactory.itemString())), this::normalizeSpace);
        functionTrie.register("fn", "replace", typeFactory.one(typeFactory.itemString()),
                List.of(typeFactory.one(typeFactory.itemString()), typeFactory.one(typeFactory.itemString()),
                        typeFactory.one(typeFactory.itemString())),
                this::replace);
        functionTrie.register("fn", "replace", typeFactory.one(typeFactory.itemString()),
                List.of(typeFactory.one(typeFactory.itemString()), typeFactory.one(typeFactory.itemString()),
                        typeFactory.one(typeFactory.itemString()), typeFactory.one(typeFactory.itemString())),
                this::replace);
        functionTrie.register("fn", "position", typeFactory.one(typeFactory.itemNumber()), List.of(), this::position);
        functionTrie.register("fn", "last", typeFactory.one(typeFactory.itemNumber()), List.of(), this::last);

    }

    // @Override
    // public CallAnalysisResult call(
    // final String functionNamespace,
    // final String functionName,
    // final XQueryTypeFactory typeFactory,
    // final XQueryVisitingSemanticContext context,
    // final List<XQuerySequenceType> args)
    // {
    // if (!namespaces.containsKey(functionNamespace)) {
    // var anyItems = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
    // List<String> errors = List.of("Unknown function namespace: " +
    // functionNamespace);
    // return new CallAnalysisResult(anyItems, errors);
    // }
    // final var namespace = namespaces.get(functionNamespace);
    // if (!namespace.containsKey(functionName)) {
    // var anyItems = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
    // List<String> errors = List.of("Unknown function: " + functionNamespace + ":"
    // + functionName);
    // return new CallAnalysisResult(anyItems, errors);
    // }
    // return namespace.get(functionName).call(typeFactory, context, args);
    // }

    @Override
    public CallAnalysisResult call(String namespace,
            String name,
            XQueryTypeFactory typeFactory,
            XQueryVisitingSemanticContext context,
            List<XQuerySequenceType> args) {
        Optional<CallAnalysisResult> result = functionTrie.resolve(namespace, name, typeFactory, context, args);
        if (result.isPresent())
            return result.get();

        var fallback = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
        return new CallAnalysisResult(fallback,
                List.of("No matching function signature for: " + namespace + ":" + name));
    }

    @Override
    public CallAnalysisResult getFunctionReference(String functionName, XQueryTypeFactory typeFactory) {
        Optional<CallAnalysisResult> result = functionTrie.resolve(functionName);
        if (result.isPresent()) {
            return result.get();
        }
        var fallback = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
        return new CallAnalysisResult(fallback, List.of("Unknown function reference: " + functionName));
    }

    private String wrongNumberOfArguments(String functionName, int expected, int actual) {
        return "Wrong number of arguments for function" + functionName + " : expected " + expected + ", got " + actual;
    }

    public CallAnalysisResult not(final XQueryTypeFactory typeFactory, final XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        if (args.size() != 1) {
            String message = wrongNumberOfArguments("fn:not()", 1, args.size());
            return new CallAnalysisResult(typeFactory.boolean_(), List.of(message));
        }
        return new CallAnalysisResult(typeFactory.boolean_(), List.of());
    }

    // fn:abs($arg as xs:numeric?) as xs:numeric?
    public CallAnalysisResult abs(final XQueryTypeFactory typeFactory, final XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // final var arg = args.get(0);
        // // TODO: Add type check failure
        // if (!arg.isNumericValue())
        // return null;
        // return typeFactory.number(arg.numericValue().abs());
    }

    public CallAnalysisResult ceiling(final XQueryTypeFactory typeFactory, final XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // final var arg = args.get(0);
        // // TODO: Add type check failure
        // if (!arg.isNumericValue())
        // return null;
        // return typeFactory.number(arg.numericValue().setScale(0,
        // RoundingMode.CEILING));
    }

    public CallAnalysisResult floor(final XQueryTypeFactory typeFactory, final XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // final var arg = args.get(0);
        // // TODO: Add type check failure
        // if (!arg.isNumericValue())
        // return null;
        // return typeFactory.number(arg.numericValue().setScale(0,
        // RoundingMode.FLOOR));
    }

    public CallAnalysisResult round(final XQueryTypeFactory typeFactory, final XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1 || args.size() == 2;
        // final var arg1 = args.get(0);
        // final var number1 = arg1.numericValue();
        // final var negativeNumber = number1.compareTo(BigDecimal.ZERO) == -1;
        // final var oneArg = args.size() == 1;
        // if (oneArg && negativeNumber) {
        // return typeFactory.number(number1.setScale(0, RoundingMode.HALF_DOWN));
        // }
        // if (oneArg) {
        // return typeFactory.number(number1.setScale(0, RoundingMode.HALF_UP));
        // }
        // final var number2 = args.get(1).numericValue();
        // final int scale = number2.intValue();
        // if (negativeNumber) {
        // return typeFactory.number(arg1.numericValue().setScale(scale,
        // RoundingMode.HALF_DOWN));
        // }
        // if (scale > 0) {
        // final var roundedNumberNormalNotation = number1.setScale(scale,
        // RoundingMode.HALF_UP);
        // return typeFactory.number(roundedNumberNormalNotation);
        // }
        // final var roundedNumber = number1.setScale(scale, RoundingMode.HALF_UP);
        // final var roundedNumberNormalNotation = roundedNumber.setScale(0,
        // RoundingMode.HALF_UP);
        // return typeFactory.number(roundedNumberNormalNotation);
    }

    public CallAnalysisResult numericAdd(final XQueryTypeFactory typeFactory,
            final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 2;
        // final var val1 = args.get(0);
        // final var val2 = args.get(1);
        // // TODO: Add type check failure
        // if (!val1.isNumericValue() || !val2.isNumericValue())
        // return null;
        // try {
        // return val1.add(typeFactory, val2);
        // } catch (final XQueryUnsupportedOperation e) {
        // return null;
        // }
    }

    public CallAnalysisResult numericSubtract(final XQueryTypeFactory typeFactory,
            final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 2;
        // final var val1 = args.get(0);
        // final var val2 = args.get(1);
        // // TODO: Add type check failure
        // if (!val1.isNumericValue() || !val2.isNumericValue())
        // return null;
        // try {
        // return val1.subtract(typeFactory, val2);
        // } catch (final XQueryUnsupportedOperation e) {
        // return null;
        // }
    }

    public CallAnalysisResult numericMultiply(final XQueryTypeFactory typeFactory,
            final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 2;
        // final var val1 = args.get(0);
        // final var val2 = args.get(1);
        // // TODO: Add type check failure
        // if (!val1.isNumericValue() || !val2.isNumericValue())
        // return null;
        // try {
        // return val1.multiply(typeFactory, val2);
        // } catch (final XQueryUnsupportedOperation e) {
        // return null;
        // }
    }

    public CallAnalysisResult numericDivide(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 2;
        // final var val1 = args.get(0);
        // final var val2 = args.get(1);
        // // TODO: Add type check failure
        // if (!val1.isNumericValue() || !val2.isNumericValue())
        // return null;
        // try {
        // return val1.divide(typeFactory, val2);
        // } catch (final XQueryUnsupportedOperation e) {
        // return null;
        // }
    }

    public CallAnalysisResult numericIntegerDivide(final XQueryTypeFactory typeFactory,
            XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 2;
        // final var val1 = args.get(0);
        // final var val2 = args.get(1);
        // // TODO: Add type check failure
        // if (!val1.isNumericValue() || !val2.isNumericValue())
        // return null;
        // try {
        // return val1.integerDivide(typeFactory, val2);
        // } catch (final XQueryUnsupportedOperation e) {
        // return null;
        // }
    }

    public CallAnalysisResult numericMod(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 2;
        // final var val1 = args.get(0);
        // final var val2 = args.get(1);
        // // TODO: Add type check failure
        // if (!val1.isNumericValue() || !val2.isNumericValue())
        // return null;
        // try {
        // return val1.modulus(typeFactory, val2);
        // } catch (final XQueryUnsupportedOperation e) {
        // return null;
        // }
    }

    public CallAnalysisResult numericUnaryPlus(final XQueryTypeFactory typeFactory,
            XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // final var val1 = args.get(0);
        // // TODO: Add type check failure
        // if (!val1.isNumericValue())
        // return null;
        // return val1;
    }

    public CallAnalysisResult numericUnaryMinus(final XQueryTypeFactory typeFactory,
            XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // final var val1 = args.get(0);
        // // TODO: Add type check failure
        // if (!val1.isNumericValue())
        // return null;
        // return typeFactory.number(val1.numericValue().negate());
    }

    public CallAnalysisResult true_(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        if (args.size() != 0) {
            String message = wrongNumberOfArguments("fn:true()", 0, args.size());
            return new CallAnalysisResult(typeFactory.boolean_(), List.of(message));
        }
        return new CallAnalysisResult(typeFactory.boolean_(), List.of());
    }

    public CallAnalysisResult false_(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        if (args.size() != 0) {
            String message = wrongNumberOfArguments("fn:false()", 0, args.size());
            return new CallAnalysisResult(typeFactory.boolean_(), List.of(message));
        }
        return new CallAnalysisResult(typeFactory.boolean_(), List.of());
    }

    public CallAnalysisResult pi(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 0;
        // return typeFactory.number(new BigDecimal(Math.PI));
    }

    public CallAnalysisResult empty(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // var arg = args.get(0);
        // try {
        // return arg.empty(typeFactory);
        // } catch (XQueryUnsupportedOperation e) {
        // return null;
        // }
    }

    public CallAnalysisResult exists(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // try {
        // return empty(typeFactory, context, args).not(typeFactory);
        // } catch (XQueryUnsupportedOperation e) {
        // return null;
        // }
    }

    public CallAnalysisResult head(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // try {
        // return args.get(0).head(typeFactory);
        // } catch (XQueryUnsupportedOperation e) {
        // return null;
        // }
    }

    public CallAnalysisResult tail(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // try {
        // return args.get(0).tail(typeFactory);
        // } catch (XQueryUnsupportedOperation e) {
        // return null;
        // }
    }

    public CallAnalysisResult insertBefore(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 3;
        // try {
        // var target = args.get(0);
        // var position = args.get(1);
        // var inserts = args.get(2);
        // return target.insertBefore(typeFactory, position, inserts);
        // } catch (XQueryUnsupportedOperation e) {
        // return null;
        // }
    }

    public CallAnalysisResult remove(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 2;
        // try {
        // var target = args.get(0);
        // var position = args.get(1);
        // return target.remove(typeFactory, position);
        // } catch (XQueryUnsupportedOperation e) {
        // return null;
        // }
    }

    public CallAnalysisResult reverse(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // try {
        // var target = args.get(0);
        // if (target.isAtomic()) {
        // return typeFactory.sequence(List.of(target));
        // }
        // return target.reverse(typeFactory);
        // } catch (XQueryUnsupportedOperation e) {
        // return null;
        // }
    }

    public CallAnalysisResult subsequence(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        return null;
        // try {
        // return switch (args.size()) {
        // case 3 -> {
        // var target = args.get(0);
        // var position = args.get(1).numericValue().intValue();
        // var length = args.get(2).numericValue().intValue();
        // yield target.subsequence(typeFactory, position, length);
        // }
        // case 2 -> {
        // var target = args.get(0);
        // var position = args.get(1).numericValue().intValue();
        // yield target.subsequence(typeFactory, position);
        // }
        // default -> null;
        // };
        // } catch (XQueryUnsupportedOperation e) {
        // return null;
        // }
    }

    public CallAnalysisResult substring(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        return null;
        // try {
        // return switch (args.size()) {
        // case 3 -> {
        // var target = args.get(0);
        // var position = args.get(1).numericValue().intValue();
        // var length = args.get(2).numericValue().intValue();
        // yield target.substring(typeFactory, position, length);
        // }
        // case 2 -> {
        // var target = args.get(0);
        // var position = args.get(1).numericValue().intValue();
        // yield target.substring(typeFactory, position);
        // }
        // default -> null;
        // };
        // } catch (XQueryUnsupportedOperation e) {
        // return null;
        // }
    }

    public CallAnalysisResult distinctValues(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // try {
        // var target = args.get(0);
        // return target.distinctValues(typeFactory);
        // } catch (XQueryUnsupportedOperation e) {
        // return null;
        // }
    }

    public CallAnalysisResult zeroOrOne(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // try {
        // var target = args.get(0);
        // return target.zeroOrOne(typeFactory);
        // } catch (XQueryUnsupportedOperation e) {
        // return null;
        // }
    }

    public CallAnalysisResult oneOrMore(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // try {
        // var target = args.get(0);
        // return target.oneOrMore(typeFactory);
        // } catch (XQueryUnsupportedOperation e) {
        // return null;
        // }
    }

    public CallAnalysisResult exactlyOne(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // try {
        // var target = args.get(0);
        // return target.exactlyOne(typeFactory);
        // } catch (XQueryUnsupportedOperation e) {
        // return null;
        // }
    }

    public CallAnalysisResult data(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // try {
        // var target = args.get(0);
        // return target.data(typeFactory);
        // } catch (XQueryUnsupportedOperation e) {
        // return null;
        // }
    }

    public CallAnalysisResult contains(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 2;
        // try {
        // var target = args.get(0);
        // var what = args.get(1);
        // return target.contains(typeFactory, what);
        // } catch (XQueryUnsupportedOperation e) {
        // return null;
        // }
    }

    public CallAnalysisResult startsWith(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 2;
        // try {
        // var target = args.get(0);
        // var what = args.get(1);
        // return target.startsWith(typeFactory, what);
        // } catch (XQueryUnsupportedOperation e) {
        // return null;
        // }
    }

    public CallAnalysisResult endsWith(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 2;
        // try {
        // var target = args.get(0);
        // var what = args.get(1);
        // return target.endsWith(typeFactory, what);
        // } catch (XQueryUnsupportedOperation e) {
        // return null;
        // }
    }

    public CallAnalysisResult substringAfter(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 2;
        // try {
        // var target = args.get(0);
        // var what = args.get(1);
        // return target.substringAfter(typeFactory, what);
        // } catch (XQueryUnsupportedOperation e) {
        // return null;
        // }
    }

    public CallAnalysisResult substringBefore(final XQueryTypeFactory typeFactory,
            XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 2;
        // try {
        // var target = args.get(0);
        // var what = args.get(1);
        // return target.substringBefore(typeFactory, what);
        // } catch (XQueryUnsupportedOperation e) {
        // return null;
        // }
    }

    public CallAnalysisResult uppercase(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // try {
        // var target = args.get(0);
        // return target.uppercase(typeFactory);
        // } catch (XQueryUnsupportedOperation e) {
        // return null;
        // }
    }

    public CallAnalysisResult lowercase(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 1;
        // try {
        // var target = args.get(0);
        // return target.lowercase(typeFactory);
        // } catch (XQueryUnsupportedOperation e) {
        // return null;
        // }
    }

    public CallAnalysisResult string(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        return null;
        // var target = switch (args.size()) {
        // case 0 -> context.getItem();
        // case 1 -> args.get(0);
        // default -> null;
        // };
        // return typeFactory.string(target.stringValue());
    }

    public CallAnalysisResult concat(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() >= 2;
        // String joined =
        // args.stream().map(XQuerySequenceType::stringValue).collect(Collectors.joining());
        // return typeFactory.string(joined);
    }

    public CallAnalysisResult stringJoin(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        return null;
        // return switch (args.size()) {
        // case 1 -> {
        // var sequence = args.get(0).sequence();
        // String joined =
        // sequence.stream().map(XQuerySequenceType::stringValue).collect(Collectors.joining());
        // yield typeFactory.string(joined);
        // }
        // case 2 -> {
        // var sequence = args.get(0).sequence();
        // var delimiter = args.get(1).stringValue();
        // String joined =
        // sequence.stream().map(XQuerySequenceType::stringValue).collect(Collectors.joining(delimiter));
        // yield typeFactory.string(joined);
        // }
        // default -> null;
        // };
    }

    public CallAnalysisResult position(final XQueryTypeFactory typeFactory, final XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 0;
        // return typeFactory.number(context.getPosition());
    }

    public CallAnalysisResult last(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        return null;
        // assert args.size() == 0;
        // return typeFactory.number(context.getSize());
    }

    public CallAnalysisResult stringLength(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        return null;
        // return switch (args.size()) {
        // case 0 -> {
        // var string = context.getItem().stringValue();
        // yield typeFactory.number(string.length());
        // }
        // case 1 -> {
        // var string = args.get(0).stringValue();
        // yield typeFactory.number(string.length());
        // }
        // default -> null;
        // };
    }

    public Pattern whitespace = Pattern.compile("\\s+");
    public UnaryOperator<String> normalize = (String s) -> {
        var trimmed = s.trim();
        return whitespace.matcher(trimmed).replaceAll(" ");
    };

    public CallAnalysisResult normalizeSpace(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        return null;
        // return switch (args.size()) {
        // case 0 -> {
        // String string = context.getItem().stringValue();
        // String normalized = normalize.apply(string);
        // yield typeFactory.string(normalized);
        // }
        // case 1 -> {
        // String string = args.get(0).stringValue();
        // String normalized = normalize.apply(string);
        // yield typeFactory.string(normalized);
        // }
        // default -> null;
        // };
    }

    record ParseFlagsResult(int flags, String newPattern, String newReplacement) {
    }

    public ParseFlagsResult parseFlags(String flags, String pattern, String replacement) {
        int flagBitMap = 0;
        Set<Character> uniqueFlags = flags.chars().mapToObj(i -> (char) i).collect(Collectors.toSet());
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

    public CallAnalysisResult replace(final XQueryTypeFactory typeFactory, XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        return null;
        // return switch (args.size()) {
        // case 3 -> {
        // String input = args.get(0).stringValue();
        // String pattern = args.get(1).stringValue();
        // String replacement = args.get(2).stringValue();
        // String result = input.replaceAll(pattern, replacement);
        // yield typeFactory.string(result);
        // }
        // case 4 -> {
        // String input = args.get(0).stringValue();
        // String pattern = args.get(1).stringValue();
        // String replacement = args.get(2).stringValue();
        // String flags = args.get(3).stringValue();
        // ParseFlagsResult parsedFlags = parseFlags(flags, pattern, replacement);
        // var matcher = Pattern.compile(parsedFlags.newPattern(),
        // parsedFlags.flags()).matcher(input);
        // String result = matcher.replaceAll(parsedFlags.newReplacement());
        // yield typeFactory.string(result);
        // }
        // default -> null;
        // };
    }

}
