package com.github.akruk.antlrxquery.evaluator.functionmanager.defaults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.AntlrXqueryLexer;
import com.github.akruk.antlrxquery.AntlrXqueryParser;
import com.github.akruk.antlrxquery.evaluator.XQueryEvaluatorVisitor;
import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;
import com.github.akruk.antlrxquery.evaluator.collations.Collations;
import com.github.akruk.antlrxquery.evaluator.functionmanager.IXQueryEvaluatingFunctionManager;
import com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions.CardinalityFunctions;
import com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions.FunctionsBasedOnSubstringMatching;
import com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions.FunctionsOnNumericValues;
import com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions.FunctionsOnStringValues;
import com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions.MathFunctions;
import com.github.akruk.antlrxquery.values.XQueryError;
import com.github.akruk.antlrxquery.values.XQueryFunction;
import com.github.akruk.antlrxquery.values.XQueryValue;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

public class EvaluatingFunctionManager implements IXQueryEvaluatingFunctionManager {
    private static final ParseTree CONTEXT_VALUE = getTree(".", parser -> parser.contextItemExpr());
    private static final ParseTree DEFAULT_COLLATION = getTree("fn:default-collation()", parser->parser.functionCall());
    private static final ParseTree EMPTY_LIST = getTree("()", p->p.parenthesizedExpr());
    private static final ParseTree DEFAULT_ROUNDING_MODE = getTree("'half-to-ceiling'", parser->parser.literal());
    private static final ParseTree ZERO_LITERAL = getTree("0", parser->parser.literal());
    private static final ParseTree NFC = getTree("\"NFC\"", parser -> parser.literal());
    private static final ParseTree STRING_AT_CONTEXT_VALUE = getTree("fn:string(.)", (parser) -> parser.functionCall());
    private static final ParseTree EMPTY_STRING = getTree("\"\"", (parser)->parser.literal());


    record FunctionEntry(
            XQueryFunction function,
            long minArity,
            long maxArity,
            Map<String, ParseTree> defaultArguments) {
    }

    private final Map<String, Map<String, List<FunctionEntry>>> namespaces;
    private final XQueryValueFactory valueFactory;
    private final MathFunctions mathFunctions;
    private final FunctionsOnStringValues functionsOnStringValues;
    private final XQueryEvaluatorVisitor evaluator;
    private final FunctionsBasedOnSubstringMatching functionsBasedOnSubstringMatching;
    private final FunctionsOnNumericValues functionsOnNumericValues;
    private final CardinalityFunctions cardinalityFunctions;

    public EvaluatingFunctionManager(final XQueryEvaluatorVisitor evaluator, final XQueryValueFactory valueFactory) {
        this.valueFactory = valueFactory;
        this.evaluator = evaluator;
        this.namespaces = new HashMap<>(10);
        this.mathFunctions = new MathFunctions(valueFactory);
        this.functionsOnStringValues = new FunctionsOnStringValues(valueFactory);
        this.functionsOnNumericValues = new FunctionsOnNumericValues(valueFactory);
        this.functionsBasedOnSubstringMatching = new FunctionsBasedOnSubstringMatching(valueFactory);
        this.cardinalityFunctions = new CardinalityFunctions(valueFactory);

        registerFunction("fn", "true", this::true_, 0, 0, Map.of());
        registerFunction("fn", "false", this::false_, 0, 0, Map.of());

        registerFunction("fn", "not", this::not, 1, 1, Map.of());

        registerFunction("fn", "abs", functionsOnNumericValues::abs, 1, 1, Map.of());
        registerFunction("fn", "ceiling", functionsOnNumericValues::ceiling, 1, 1, Map.of());
        registerFunction("fn", "floor", functionsOnNumericValues::floor, 1, 1, Map.of());
        registerFunction("fn", "round", functionsOnNumericValues::round, 1, 3, Map.of("precision", ZERO_LITERAL, "mode", DEFAULT_ROUNDING_MODE));
        registerFunction("fn", "round-half-to-even", functionsOnNumericValues::roundHalfToEven, 1, 2, Map.of("precision", ZERO_LITERAL));
        // registerFunction("fn", "divide-decimals", functionsOnNumericValues::divideDecimals, 2, 3, Map.of("precision", ZERO_LITERAL));

        // registerFunction("fn", "is-NaN", functionsOnNumericValues::isNaN, 1, 1, Map.of());

        registerFunction("fn", "empty", this::empty, 0, 0, Map.of());
        registerFunction("fn", "exists", this::exists, 0, 0, Map.of());
        registerFunction("fn", "head", this::head, 0, 0, Map.of());
        registerFunction("fn", "tail", this::tail, 0, 0, Map.of());
        registerFunction("fn", "insert-before", this::insertBefore, 0, 0, Map.of());
        registerFunction("fn", "remove", this::remove, 0, 0, Map.of());
        registerFunction("fn", "reverse", this::reverse, 0, 0, Map.of());
        registerFunction("fn", "subsequence", this::subsequence, 0, 0, Map.of());
        registerFunction("fn", "distinct-values", this::distinctValues, 0, 0, Map.of());

        registerFunction("fn", "zero-or-one", cardinalityFunctions::zeroOrOne, 1, 1, Map.of());
        registerFunction("fn", "one-or-more", cardinalityFunctions::oneOrMore, 1, 1, Map.of());
        registerFunction("fn", "exactly-one", cardinalityFunctions::exactlyOne, 1, 1, Map.of());

        registerFunction("fn", "data", this::data, 0, 0, Map.of());
        registerFunction("fn", "string", this::string, 0, 0, Map.of());

        registerFunction("fn", "default-collation", this::defaultCollation, 0, 0, Map.of());

        final ParseTree DEFAULT_COLLATION = getTree("fn:default-collation()", parser->parser.functionCall());
        final Map<String, ParseTree> DEFAULT_COLLATION_MAP = Map.of("collation", DEFAULT_COLLATION);
        registerFunction("fn", "contains",
            functionsBasedOnSubstringMatching::contains, 2, 3, DEFAULT_COLLATION_MAP);
        registerFunction("fn", "starts-with",
            functionsBasedOnSubstringMatching::startsWith, 2, 3, DEFAULT_COLLATION_MAP);
        registerFunction("fn", "ends-with",
            functionsBasedOnSubstringMatching::endsWith, 2, 3, DEFAULT_COLLATION_MAP);
        registerFunction("fn", "substring-after",
            functionsBasedOnSubstringMatching::substringAfter, 2, 3, DEFAULT_COLLATION_MAP);
        registerFunction("fn", "substring-before",
            functionsBasedOnSubstringMatching::substringBefore, 2, 3, DEFAULT_COLLATION_MAP);


        registerFunction("fn", "char", functionsOnStringValues::char_, 1, 1, Map.of());
        registerFunction("fn", "characters", functionsOnStringValues::characters, 1, 1, Map.of());
        registerFunction("fn", "graphemes", functionsOnStringValues::graphemes, 1, 1, Map.of());
        registerFunction("fn", "concat", functionsOnStringValues::concat, 0, 1, Map.of());
        registerFunction("fn", "string-join", functionsOnStringValues::stringJoin, 1, 2, Map.of());
        registerFunction("fn", "substring", functionsOnStringValues::substring, 2, 3, Map.of());
        registerFunction("fn", "string-length", functionsOnStringValues::stringLength, 0, 1, Map.of());
        registerFunction("fn", "normalize-space", functionsOnStringValues::normalizeSpace, 0, 1, Map.of());
        registerFunction("fn", "normalize-unicode", functionsOnStringValues::normalizeUnicode, 1, 2, Map.of());
        registerFunction("fn", "upper-case", functionsOnStringValues::uppercase, 1, 1, Map.of());
        registerFunction("fn", "lower-case", functionsOnStringValues::lowercase, 1, 1, Map.of());
        registerFunction("fn", "translate", functionsOnStringValues::translate, 3, 3, Map.of());

        registerFunction("fn", "replace", this::replace, 0, 0, Map.of());
        registerFunction("fn", "position", this::position, 0, 0, Map.of());
        registerFunction("fn", "last", this::last, 0, 0, Map.of());

        registerFunction("math", "pi", mathFunctions::pi, 0, 0, Map.of());
        registerFunction("math", "e", mathFunctions::e, 0, 0, Map.of());
        registerFunction("math", "exp", mathFunctions::exp, 1, 1, Map.of());
        registerFunction("math", "exp10", mathFunctions::exp10,1, 1, Map.of());
        registerFunction("math", "log", mathFunctions::log, 1, 1, Map.of());
        registerFunction("math", "log10", mathFunctions::log10, 1, 1, Map.of());
        registerFunction("math", "pow", mathFunctions::pow, 2, 2, Map.of());
        registerFunction("math", "sqrt", mathFunctions::sqrt, 1, 1, Map.of());
        registerFunction("math", "sin", mathFunctions::sin, 1, 1, Map.of());
        registerFunction("math", "cos", mathFunctions::cos, 1, 1, Map.of());
        registerFunction("math", "tan", mathFunctions::tan, 1, 1, Map.of());
        registerFunction("math", "asin", mathFunctions::asin, 1, 1, Map.of());
        registerFunction("math", "acos", mathFunctions::acos, 1, 1, Map.of());
        registerFunction("math", "atan", mathFunctions::atan, 1, 1, Map.of());
        registerFunction("math", "atan2", mathFunctions::atan2, 2, 2, Map.of());
        registerFunction("math", "sinh", mathFunctions::sinh, 1, 1, Map.of());
        registerFunction("math", "cosh", mathFunctions::cosh, 1, 1, Map.of());
        registerFunction("math", "tanh", mathFunctions::tanh, 1, 1, Map.of());

        registerFunction("op", "numeric-add", this::numericAdd, 0, 0, Map.of());
        registerFunction("op", "numeric-subtract", this::numericSubtract, 0, 0, Map.of());
        registerFunction("op", "numeric-multiply", this::numericMultiply, 0, 0, Map.of());
        registerFunction("op", "numeric-divide", this::numericDivide, 0, 0, Map.of());
        registerFunction("op", "numeric-integer-divide", this::numericIntegerDivide, 0, 0, Map.of());
        registerFunction("op", "numeric-mod", this::numericMod, 0, 0, Map.of());
        registerFunction("op", "numeric-unary-plus", this::numericUnaryPlus, 0, 0, Map.of());
        registerFunction("op", "numeric-unary-minus", this::numericUnaryMinus, 0, 0, Map.of());
    }

    /**
     * fn:default-collation() as xs:string
     * Returns the URI of the default collation from the dynamic context.
     */
    public XQueryValue defaultCollation(
            XQueryVisitingContext context,
            List<XQueryValue> args,
            Map<String,XQueryValue> kwargs)
    {
        return valueFactory.string(Collations.CODEPOINT_URI);
    }

    public XQueryValue not(XQueryVisitingContext context, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        return args.get(0).not();
    }


    public XQueryValue numericAdd(XQueryVisitingContext context, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() != 2) return XQueryError.WrongNumberOfArguments;
        var a = args.get(0); var b = args.get(1);
        if (!a.isNumericValue() || !b.isNumericValue()) return XQueryError.InvalidArgumentType;
        return a.add(b);
    }

    public XQueryValue numericSubtract(XQueryVisitingContext context, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() != 2) return XQueryError.WrongNumberOfArguments;
        var a = args.get(0); var b = args.get(1);
        if (!a.isNumericValue() || !b.isNumericValue()) return XQueryError.InvalidArgumentType;
        return a.subtract(b);
    }

    public XQueryValue numericMultiply(XQueryVisitingContext context, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() != 2) return XQueryError.WrongNumberOfArguments;
        var a = args.get(0); var b = args.get(1);
        if (!a.isNumericValue() || !b.isNumericValue()) return XQueryError.InvalidArgumentType;
        return a.multiply(b);
    }

    public XQueryValue numericDivide(XQueryVisitingContext context, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() != 2) return XQueryError.WrongNumberOfArguments;
        var a = args.get(0); var b = args.get(1);
        if (!a.isNumericValue() || !b.isNumericValue()) return XQueryError.InvalidArgumentType;
        return a.divide(b);
    }

    public XQueryValue numericIntegerDivide(XQueryVisitingContext context, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() != 2) return XQueryError.WrongNumberOfArguments;
        var a = args.get(0); var b = args.get(1);
        if (!a.isNumericValue() || !b.isNumericValue()) return XQueryError.InvalidArgumentType;
        return a.integerDivide(b);
    }

    public XQueryValue numericMod(XQueryVisitingContext context, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() != 2) return XQueryError.WrongNumberOfArguments;
        var a = args.get(0); var b = args.get(1);
        if (!a.isNumericValue() || !b.isNumericValue()) return XQueryError.InvalidArgumentType;
        return a.modulus(b);
    }

    public XQueryValue numericUnaryPlus(XQueryVisitingContext context, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        var a = args.get(0);
        if (!a.isNumericValue()) return XQueryError.InvalidArgumentType;
        return a;
    }

    public XQueryValue numericUnaryMinus(XQueryVisitingContext context, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        var a = args.get(0);
        if (!a.isNumericValue()) return XQueryError.InvalidArgumentType;
        return valueFactory.number(a.numericValue().negate());
    }

    public XQueryValue true_(XQueryVisitingContext context, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (!args.isEmpty()) return XQueryError.WrongNumberOfArguments;
        return valueFactory.bool(true);
    }

    public XQueryValue false_(XQueryVisitingContext context, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (!args.isEmpty()) return XQueryError.WrongNumberOfArguments;
        return valueFactory.bool(false);
    }

    public XQueryValue empty(XQueryVisitingContext ctx, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        return args.get(0).empty();
    }

    public XQueryValue exists(XQueryVisitingContext ctx, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        var result = empty(ctx, args, Map.of());
        if (result.isError()) return result;
        return result.not();
    }

    public XQueryValue head(XQueryVisitingContext ctx, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        return args.get(0).head();
    }

    public XQueryValue tail(XQueryVisitingContext ctx, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        return args.get(0).tail();
    }

    public XQueryValue insertBefore(XQueryVisitingContext ctx, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() != 3) return XQueryError.WrongNumberOfArguments;
        return args.get(0).insertBefore(args.get(1), args.get(2));
    }

    public XQueryValue remove(XQueryVisitingContext ctx, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() != 2) return XQueryError.WrongNumberOfArguments;
        return args.get(0).remove(args.get(1));
    }

    public XQueryValue reverse(XQueryVisitingContext ctx, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        var target = args.get(0);
        return target.isAtomic()
            ? valueFactory.sequence(List.of(target))
            : target.reverse();
    }

    public XQueryValue subsequence(XQueryVisitingContext ctx, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
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

    public XQueryValue distinctValues(XQueryVisitingContext ctx, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        return args.get(0).distinctValues();
    }

    public XQueryValue data(XQueryVisitingContext ctx, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        return args.get(0).data();
    }

    public XQueryValue string(XQueryVisitingContext context, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() > 1) return XQueryError.WrongNumberOfArguments;
        var target = args.isEmpty() ? context.getItem() : args.get(0);
        return valueFactory.string(target.stringValue());
    }

    public XQueryValue position(XQueryVisitingContext context, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (!args.isEmpty()) return XQueryError.WrongNumberOfArguments;
        return valueFactory.number(context.getPosition());
    }

    public XQueryValue last(XQueryVisitingContext context, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (!args.isEmpty()) return XQueryError.WrongNumberOfArguments;
        return valueFactory.number(context.getSize());
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

    public XQueryValue replace(XQueryVisitingContext context,
                                List<XQueryValue> args,
                                Map<String, XQueryValue> kwargs)
    {
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

    private void registerFunction(String namespace, String localName, XQueryFunction function, int minArity, int maxArity, Map<String, ParseTree> defaultArguments) {
        final FunctionEntry functionEntry = new FunctionEntry(function, minArity, maxArity, defaultArguments);
        final Map<String, List<FunctionEntry>> namespaceFunctions = namespaces.computeIfAbsent(namespace, _ -> new HashMap<>());
        final List<FunctionEntry> functionsWithDesiredName = namespaceFunctions.computeIfAbsent(localName, _ -> new ArrayList<FunctionEntry>());
        functionsWithDesiredName.add(functionEntry);
    }

    public static record FunctionOrError(FunctionEntry entry, XQueryValue error) {
        public boolean isError() {
            return error != null;
        }
    }
    private FunctionOrError getFunction(String namespace, String functionName, long arity) {
        Map<String, List<FunctionEntry>> functionsInNs = namespaces.get(namespace);
        if (functionsInNs == null) {
            return new FunctionOrError(null, XQueryError.UnknownFunctionName);
        }
        List<FunctionEntry> functionsWithGivenName = functionsInNs.get(functionName);
        Predicate<FunctionEntry> withinArityRange = f -> (f.minArity() <= arity && arity <= f.maxArity());
        Optional<FunctionEntry> functionWithRequiredArity = functionsWithGivenName.stream().filter(withinArityRange).findFirst();
        if (!functionWithRequiredArity.isPresent())
            return new FunctionOrError(null, XQueryError.WrongNumberOfArguments);
        return new FunctionOrError(functionWithRequiredArity.get(), null);
    }

    @Override
    public XQueryValue call(String namespace, String functionName, XQueryVisitingContext context,
            List<XQueryValue> args, Map<String, XQueryValue> keywordArgs)
    {
        final long arity = args.size() + keywordArgs.size();
        FunctionOrError functionOrError = getFunction(namespace, functionName, arity);
        if (functionOrError.isError())
            return functionOrError.error();
        FunctionEntry functionEntry = functionOrError.entry();
        // functionEntry.defaultArguments;
        final var function = functionEntry.function;
        final var defaultArgs = functionEntry.defaultArguments;
        final Stream<String> defaultedArgumentNames = functionEntry.defaultArguments.keySet().stream().filter(name-> !keywordArgs.containsKey(name));
        final Map<String, XQueryValue> defaultedArguments = defaultedArgumentNames.collect(Collectors.toMap(name->name, name->{
                ParseTree default_ = defaultArgs.get(name);
                return default_.accept(evaluator);
            }));
        keywordArgs.putAll(defaultedArguments);
        return function.call(context, args, keywordArgs);
    }

    @Override
    public XQueryValue getFunctionReference(String namespace, String functionName, long arity) {
        FunctionOrError function = getFunction(namespace, functionName, arity);
        if (function.isError()) {
            return XQueryError.UnknownFunctionName;
        }
        return valueFactory.functionReference(function.entry().function);
        // return null;
    }


    private static ParseTree getTree(final String xquery, Function<AntlrXqueryParser, ParseTree> initialRule) {
        final CodePointCharStream charStream = CharStreams.fromString(xquery);
        final AntlrXqueryLexer lexer = new AntlrXqueryLexer(charStream);
        final CommonTokenStream stream = new CommonTokenStream(lexer);
        final AntlrXqueryParser parser = new AntlrXqueryParser(stream);
        return initialRule.apply(parser);
    }

}
