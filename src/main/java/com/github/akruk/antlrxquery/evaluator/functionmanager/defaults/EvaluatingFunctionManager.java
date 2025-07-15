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
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.AntlrXqueryLexer;
import com.github.akruk.antlrxquery.AntlrXqueryParser;
import com.github.akruk.antlrxquery.evaluator.XQueryEvaluatorVisitor;
import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;
import com.github.akruk.antlrxquery.evaluator.collations.Collations;
import com.github.akruk.antlrxquery.evaluator.functionmanager.IXQueryEvaluatingFunctionManager;
import com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions.Accessors;
import com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions.CardinalityFunctions;
import com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions.FunctionsBasedOnSubstringMatching;
import com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions.FunctionsOnNumericValues;
import com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions.FunctionsOnSequencesOfNodes;
import com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions.FunctionsOnStringValues;
import com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions.MathFunctions;
import com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions.NumericOperators;
import com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions.OtherFunctionsOnNodes;
import com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions.ParsingNumbers;
import com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions.ProcessingSequencesFunctions;
import com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions.ProcessingStrings;
import com.github.akruk.antlrxquery.values.XQueryError;
import com.github.akruk.antlrxquery.values.XQueryFunction;
import com.github.akruk.antlrxquery.values.XQueryValue;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;
import com.github.akruk.nodegetter.INodeGetter;

public class EvaluatingFunctionManager implements IXQueryEvaluatingFunctionManager {
    private static final ParseTree CONTEXT_VALUE = getTree(".", parser -> parser.contextItemExpr());
    private static final ParseTree DEFAULT_COLLATION = getTree("fn:default-collation()", parser->parser.functionCall());
    private static final ParseTree EMPTY_SEQUENCE = getTree("()", p->p.parenthesizedExpr());
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
    private final NumericOperators numericOperators;
    private final Accessors accessors;

    // Added fields
    private final OtherFunctionsOnNodes otherFuctionsOnNodes;
    private final FunctionsOnSequencesOfNodes functionsOnSequencesOfNodes;
    private final ParsingNumbers parsingNumbers;
    private final ProcessingStrings processingStrings;
    private final ProcessingSequencesFunctions processingSequences;

    public EvaluatingFunctionManager(final XQueryEvaluatorVisitor evaluator,
                                        final Parser parser,
                                        final XQueryValueFactory valueFactory,
                                        final INodeGetter nodeGetter)
    {
        this.valueFactory = valueFactory;
        this.evaluator = evaluator;
        this.namespaces = new HashMap<>(10);
        this.mathFunctions = new MathFunctions(valueFactory);
        this.accessors = new Accessors(valueFactory, parser);
        this.functionsOnStringValues = new FunctionsOnStringValues(valueFactory);
        this.functionsOnNumericValues = new FunctionsOnNumericValues(valueFactory);
        this.functionsBasedOnSubstringMatching = new FunctionsBasedOnSubstringMatching(valueFactory);
        this.cardinalityFunctions = new CardinalityFunctions(valueFactory);
        this.numericOperators = new NumericOperators(valueFactory);
        this.otherFuctionsOnNodes = new OtherFunctionsOnNodes(valueFactory, nodeGetter, parser);
        this.functionsOnSequencesOfNodes = new FunctionsOnSequencesOfNodes(valueFactory, parser);
        this.processingSequences = new ProcessingSequencesFunctions(valueFactory, parser);
        this.parsingNumbers = new ParsingNumbers(valueFactory, parser);
        this.processingStrings = new ProcessingStrings(valueFactory, parser);

        final Map<String, ParseTree> optionalNodeArg = Map.of( "node", CONTEXT_VALUE);

        // Accessors
        final Map<String, ParseTree> defaultNodeArg = Map.of("node", CONTEXT_VALUE);
        final Map<String, ParseTree> defaultValueArg = Map.of("value", CONTEXT_VALUE);
        final Map<String, ParseTree> defaultInputArg = Map.of("input", CONTEXT_VALUE);

        registerFunction("fn", "name", accessors::nodeName, List.of("node"), defaultNodeArg);
        registerFunction("fn", "node-name", accessors::nodeName, List.of("node"), defaultNodeArg);
        final List<String> valueArg = List.of("value");
        registerFunction("fn", "string", accessors::string, valueArg, defaultValueArg);
        registerFunction("fn", "data", accessors::data, List.of("input"), defaultInputArg);



        // Other functions on nodes
        // Rejestracja funkcji na węzłach
        registerFunction("fn", "root", otherFuctionsOnNodes::root, List.of("node"), defaultNodeArg);
        registerFunction("fn", "has-children", otherFuctionsOnNodes::hasChildren, List.of("node"), defaultNodeArg);
        registerFunction("fn", "siblings", otherFuctionsOnNodes::siblings, List.of("node"), defaultNodeArg);

        registerFunction("fn", "true", this::true_, List.of(), Map.of());
        registerFunction("fn", "false", this::false_, List.of(), Map.of());

        registerFunction("fn", "not", this::not, List.of("input"), Map.of());

        // Processing numerics
        registerFunction("fn", "abs", functionsOnNumericValues::abs,
            valueArg, Map.of());

        registerFunction("fn", "ceiling", functionsOnNumericValues::ceiling,
            valueArg, Map.of());

        registerFunction("fn", "floor", functionsOnNumericValues::floor,
            valueArg, Map.of());

        registerFunction("fn", "round", functionsOnNumericValues::round,
            List.of("value", "precision", "mode"),
            Map.of(
                "precision", ZERO_LITERAL,
                "mode", DEFAULT_ROUNDING_MODE
            ));

        registerFunction("fn", "round-half-to-even", functionsOnNumericValues::roundHalfToEven,
            List.of("value", "precision"),
            Map.of("precision", ZERO_LITERAL));

        registerFunction("fn", "divide-decimals", functionsOnNumericValues::divideDecimals,
            List.of("value", "divisor", "precision"),
            Map.of("precision", ZERO_LITERAL));


        // Processing sequences
        final Map<String, ParseTree> emptyInputArg = Map.of("input", EMPTY_SEQUENCE);
        registerFunction("fn", "empty", processingSequences::empty,
            List.of("input"), Map.of());

        registerFunction("fn", "exists", processingSequences::exists,
            List.of("input"), Map.of());

        registerFunction("fn", "foot", processingSequences::foot,
            List.of("input"), Map.of());

        registerFunction("fn", "head", processingSequences::head,
            List.of("input"), Map.of());

        registerFunction("fn", "identity", processingSequences::identity,
            List.of("input"), Map.of());

        registerFunction("fn", "insert-before", processingSequences::insertBefore,
            List.of("input", "position", "insert"), Map.of());

        registerFunction("fn", "items-at", processingSequences::itemsAt,
            List.of("input", "at"), Map.of());

        registerFunction("fn", "remove", processingSequences::remove,
            List.of("input", "positions"), Map.of());

        registerFunction("fn", "replicate", processingSequences::replicate,
            List.of("input", "count"), Map.of());

        registerFunction("fn", "reverse", processingSequences::reverse,
            List.of("input"), Map.of());

        registerFunction("fn", "sequence-join", processingSequences::sequenceJoin,
            List.of("input", "separator"), Map.of());

        registerFunction("fn", "slice", processingSequences::slice,
            List.of("input", "start", "end", "step"),
            Map.of(
                "start", EMPTY_SEQUENCE,
                "end", EMPTY_SEQUENCE,
                "step", EMPTY_SEQUENCE
            ));

        registerFunction("fn", "subsequence", processingSequences::subsequence,
            List.of("input", "start", "length"),
            Map.of("length", EMPTY_SEQUENCE));

        registerFunction("fn", "tail", processingSequences::tail,
            List.of("input"), Map.of());

        registerFunction("fn", "trunk", processingSequences::trunk,
            List.of("input"), Map.of());

        registerFunction("fn", "unordered", processingSequences::unordered,
            List.of("input"), Map.of());

        registerFunction("fn", "void", processingSequences::voidFunction,
            List.of("input"), emptyInputArg);



        registerFunction("fn", "distinct-values", this::distinctValues,
            List.of("values", "collation"), Map.of("collation", DEFAULT_COLLATION));

        registerFunction("fn", "zero-or-one", cardinalityFunctions::zeroOrOne,
            List.of("input"), Map.of());

        registerFunction("fn", "one-or-more", cardinalityFunctions::oneOrMore,
            List.of("input"), Map.of());

        registerFunction("fn", "exactly-one", cardinalityFunctions::exactlyOne,
            List.of("input"), Map.of());


        registerFunction("fn", "default-collation", this::defaultCollation, List.of(), Map.of());

        final Map<String, ParseTree> DEFAULT_COLLATION_MAP = Map.of("collation", DEFAULT_COLLATION);
        final List<String> valueSubstringCollation = List.of("value", "substring", "collation");
        registerFunction("fn", "contains",
            functionsBasedOnSubstringMatching::contains, valueSubstringCollation, DEFAULT_COLLATION_MAP);
        registerFunction("fn", "starts-with",
            functionsBasedOnSubstringMatching::startsWith, valueSubstringCollation, DEFAULT_COLLATION_MAP);
        registerFunction("fn", "ends-with",
            functionsBasedOnSubstringMatching::endsWith, valueSubstringCollation, DEFAULT_COLLATION_MAP);
        registerFunction("fn", "substring-after",
            functionsBasedOnSubstringMatching::substringAfter, valueSubstringCollation, DEFAULT_COLLATION_MAP);
        registerFunction("fn", "substring-before",
            functionsBasedOnSubstringMatching::substringBefore, valueSubstringCollation, DEFAULT_COLLATION_MAP);


        registerFunction("fn", "char", functionsOnStringValues::char_, valueArg, Map.of());

        registerFunction("fn", "characters", functionsOnStringValues::characters, valueArg, Map.of());

        registerFunction("fn", "graphemes", functionsOnStringValues::graphemes, valueArg, Map.of());

        registerFunction("fn", "concat", functionsOnStringValues::concat,
            List.of("values"), Map.of("values", EMPTY_SEQUENCE));

        registerFunction("fn", "string-join", functionsOnStringValues::stringJoin,
            List.of("values", "separator"), Map.of("separator", EMPTY_STRING));

        registerFunction("fn", "substring", functionsOnStringValues::substring,
            List.of("value", "start", "length"), Map.of("length", EMPTY_SEQUENCE));

        registerFunction("fn", "string-length", functionsOnStringValues::stringLength,
            valueArg, Map.of("value", CONTEXT_VALUE)); // := fn:string(.)

        registerFunction("fn", "normalize-space", functionsOnStringValues::normalizeSpace,
            valueArg, Map.of("value", CONTEXT_VALUE)); // := fn:string(.)

        registerFunction("fn", "normalize-unicode", functionsOnStringValues::normalizeUnicode,
            List.of("value", "form"), Map.of("form", NFC));

        registerFunction("fn", "upper-case", functionsOnStringValues::upperCase,
            valueArg, Map.of());

        registerFunction("fn", "lower-case", functionsOnStringValues::lowerCase,
            valueArg, Map.of());

        registerFunction("fn", "translate", functionsOnStringValues::translate,
            List.of("value", "map", "trans"), Map.of());


        registerFunction("fn", "replace", this::replace, List.of(), Map.of());
        registerFunction("fn", "position", this::position, List.of(), Map.of());
        registerFunction("fn", "last", this::last, List.of(), Map.of());

        registerFunction("math", "pi", mathFunctions::pi,
            List.of(), Map.of());

        registerFunction("math", "e", mathFunctions::e,
            List.of(), Map.of());

        registerFunction("math", "exp", mathFunctions::exp,
            valueArg, Map.of());

        registerFunction("math", "exp10", mathFunctions::exp10,
            valueArg, Map.of());

        registerFunction("math", "log", mathFunctions::log,
            valueArg, Map.of());

        registerFunction("math", "log10", mathFunctions::log10,
            valueArg, Map.of());

        registerFunction("math", "pow", mathFunctions::pow,
            List.of("x", "y"), Map.of());

        registerFunction("math", "sqrt", mathFunctions::sqrt,
            valueArg, Map.of());

        registerFunction("math", "sin", mathFunctions::sin,
            List.of("radians"), Map.of());

        registerFunction("math", "cos", mathFunctions::cos,
            List.of("radians"), Map.of());

        registerFunction("math", "tan", mathFunctions::tan,
            List.of("radians"), Map.of());

        registerFunction("math", "asin", mathFunctions::asin,
            valueArg, Map.of());

        registerFunction("math", "acos", mathFunctions::acos,
            valueArg, Map.of());

        registerFunction("math", "atan", mathFunctions::atan,
            valueArg, Map.of());

        registerFunction("math", "atan2", mathFunctions::atan2,
            List.of("y", "x"), Map.of());

        registerFunction("math", "sinh", mathFunctions::sinh,
            valueArg, Map.of());

        registerFunction("math", "cosh", mathFunctions::cosh,
            valueArg, Map.of());

        registerFunction("math", "tanh", mathFunctions::tanh,
            valueArg, Map.of());


        registerFunction("op", "numeric-add", numericOperators::numericAdd,
            List.of("arg1", "arg2"), Map.of());

        registerFunction("op", "numeric-subtract", numericOperators::numericSubtract,
            List.of("arg1", "arg2"), Map.of());

        registerFunction("op", "numeric-multiply", numericOperators::numericMultiply,
            List.of("arg1", "arg2"), Map.of());

        registerFunction("op", "numeric-divide", numericOperators::numericDivide,
            List.of("arg1", "arg2"), Map.of());

        registerFunction("op", "numeric-integer-divide", numericOperators::numericIntegerDivide,
            List.of("arg1", "arg2"), Map.of());

        registerFunction("op", "numeric-mod", numericOperators::numericMod,
            List.of("arg1", "arg2"), Map.of());

        registerFunction("op", "numeric-unary-plus", numericOperators::numericUnaryPlus,
            List.of("arg"), Map.of());

        registerFunction("op", "numeric-unary-minus", numericOperators::numericUnaryMinus,
            List.of("arg"), Map.of());

        registerFunction("op", "numeric-equal", numericOperators::numericEqual,
            List.of("arg1", "arg2"), Map.of());

        registerFunction("op", "numeric-less-than", numericOperators::numericLessThan,
            List.of("arg1", "arg2"), Map.of());

        registerFunction("op", "numeric-less-than-or-equal", numericOperators::numericLessThanOrEqual,
            List.of("arg1", "arg2"), Map.of());

        registerFunction("op", "numeric-greater-than", numericOperators::numericGreaterThan,
            List.of("arg1", "arg2"), Map.of());

        registerFunction("op", "numeric-greater-than-or-equal", numericOperators::numericGreaterThanOrEqual,
            List.of("arg1", "arg2"), Map.of());

    }

    /**
     * fn:default-collation() as xs:string
     * Returns the URI of the default collation from the dynamic context.
     */
    public XQueryValue defaultCollation(
            final XQueryVisitingContext context,
            final List<XQueryValue> args,
            final Map<String,XQueryValue> kwargs)
    {
        return valueFactory.string(Collations.CODEPOINT_URI);
    }

    public XQueryValue not(final XQueryVisitingContext context, final List<XQueryValue> args, final Map<String, XQueryValue> kwargs) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        return args.get(0).not();
    }


    public XQueryValue true_(final XQueryVisitingContext context, final List<XQueryValue> args, final Map<String, XQueryValue> kwargs) {
        if (!args.isEmpty()) return XQueryError.WrongNumberOfArguments;
        return valueFactory.bool(true);
    }

    public XQueryValue false_(final XQueryVisitingContext context, final List<XQueryValue> args, final Map<String, XQueryValue> kwargs) {
        if (!args.isEmpty()) return XQueryError.WrongNumberOfArguments;
        return valueFactory.bool(false);
    }

    public XQueryValue distinctValues(final XQueryVisitingContext ctx, final List<XQueryValue> args, final Map<String, XQueryValue> kwargs) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        return args.get(0).distinctValues();
    }



    public XQueryValue position(final XQueryVisitingContext context, final List<XQueryValue> args, final Map<String, XQueryValue> kwargs) {
        if (!args.isEmpty()) return XQueryError.WrongNumberOfArguments;
        return valueFactory.number(context.getPosition());
    }

    public XQueryValue last(final XQueryVisitingContext context, final List<XQueryValue> args, final Map<String, XQueryValue> kwargs) {
        if (!args.isEmpty()) return XQueryError.WrongNumberOfArguments;
        return valueFactory.number(context.getSize());
    }

    record ParseFlagsResult(int flags, String newPattern, String newReplacement) {}

    public ParseFlagsResult parseFlags(final String flags, String pattern, String replacement) {
        int flagBitMap = 0;
        final Set<Character> uniqueFlags = flags.chars().mapToObj(i->(char) i).collect(Collectors.toSet());
        for (final char c : uniqueFlags) {
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

    public XQueryValue replace(final XQueryVisitingContext context,
                                final List<XQueryValue> args,
                                final Map<String, XQueryValue> kwargs)
    {
        if (args.size() == 3) {
            try {
                final String input = args.get(0).stringValue();
                final String pattern = args.get(1).stringValue();
                final String replacement = args.get(2).stringValue();
                final String result = input.replaceAll(pattern, replacement);
                return valueFactory.string(result);
            } catch (final Exception e) {
                return XQueryError.InvalidRegex;
            }
        } else if (args.size() == 4) {
            try {
                final String input = args.get(0).stringValue();
                final String pattern = args.get(1).stringValue();
                final String replacement = args.get(2).stringValue();
                final String flags = args.get(3).stringValue();
                final var parsed = parseFlags(flags, pattern, replacement);
                final Pattern compiled = Pattern.compile(parsed.newPattern(), parsed.flags());
                final String result = compiled.matcher(input).replaceAll(parsed.newReplacement());
                return valueFactory.string(result);
            } catch (final Exception e) {
                return XQueryError.InvalidRegexFlags;
            }
        } else {
            return XQueryError.WrongNumberOfArguments;
        }
    }

    private void registerFunction(final String namespace, final String localName, final XQueryFunction function, final List<String> argNames, final Map<String, ParseTree> defaultArguments) {
        final var maxArity = argNames.size();
        final var minArity = maxArity - defaultArguments.size();
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
    private FunctionOrError getFunction(final String namespace, final String functionName, final long arity) {
        final Map<String, List<FunctionEntry>> functionsInNs = namespaces.get(namespace);
        if (functionsInNs == null) {
            return new FunctionOrError(null, XQueryError.UnknownFunctionName);
        }
        final List<FunctionEntry> functionsWithGivenName = functionsInNs.get(functionName);
        final Predicate<FunctionEntry> withinArityRange = f -> (f.minArity() <= arity && arity <= f.maxArity());
        final Optional<FunctionEntry> functionWithRequiredArity = functionsWithGivenName.stream().filter(withinArityRange).findFirst();
        if (!functionWithRequiredArity.isPresent())
            return new FunctionOrError(null, XQueryError.WrongNumberOfArguments);
        return new FunctionOrError(functionWithRequiredArity.get(), null);
    }

    @Override
    public XQueryValue call(final String namespace, final String functionName, final XQueryVisitingContext context,
            final List<XQueryValue> args, final Map<String, XQueryValue> keywordArgs)
    {
        final long arity = args.size() + keywordArgs.size();
        final FunctionOrError functionOrError = getFunction(namespace, functionName, arity);
        if (functionOrError.isError())
            return functionOrError.error();
        final FunctionEntry functionEntry = functionOrError.entry();
        // functionEntry.defaultArguments;
        final var function = functionEntry.function;
        final var defaultArgs = functionEntry.defaultArguments;
        final Stream<String> defaultedArgumentNames = functionEntry.defaultArguments.keySet().stream().filter(name-> !keywordArgs.containsKey(name));
        final Map<String, XQueryValue> defaultedArguments = defaultedArgumentNames.collect(Collectors.toMap(name->name, name->{
                final ParseTree default_ = defaultArgs.get(name);
                return default_.accept(evaluator);
            }));
        keywordArgs.putAll(defaultedArguments);
        return function.call(context, args, keywordArgs);
    }

    @Override
    public XQueryValue getFunctionReference(final String namespace, final String functionName, final long arity) {
        final FunctionOrError function = getFunction(namespace, functionName, arity);
        if (function.isError()) {
            return XQueryError.UnknownFunctionName;
        }
        return valueFactory.functionReference(function.entry().function);
        // return null;
    }


    private static ParseTree getTree(final String xquery, final Function<AntlrXqueryParser, ParseTree> initialRule) {
        final CodePointCharStream charStream = CharStreams.fromString(xquery);
        final AntlrXqueryLexer lexer = new AntlrXqueryLexer(charStream);
        final CommonTokenStream stream = new CommonTokenStream(lexer);
        final AntlrXqueryParser parser = new AntlrXqueryParser(stream);
        return initialRule.apply(parser);
    }

}
