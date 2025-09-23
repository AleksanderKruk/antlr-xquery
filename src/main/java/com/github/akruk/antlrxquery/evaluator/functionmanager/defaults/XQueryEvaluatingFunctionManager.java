package com.github.akruk.antlrxquery.evaluator.functionmanager.defaults;

import static com.github.akruk.antlrxquery.evaluator.values.XQueryValue.error;

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
import com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions.*;
import com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions.AggregateFunctions;
import com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions.CardinalityFunctions;
import com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions.FunctionsBasedOnSubstringMatching;
import com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions.FunctionsOnNumericValues;
import com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions.FunctionsOnStringValues;
import com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions.MathFunctions;
import com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions.NumericOperators;
import com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions.OtherFunctionsOnNodes;
import com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions.ProcessingBooleans;
import com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions.ProcessingSequencesFunctions;
import com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions.ProcessingStrings;
import com.github.akruk.antlrxquery.evaluator.values.XQueryError;
import com.github.akruk.antlrxquery.evaluator.values.XQueryFunction;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;
import com.github.akruk.antlrxquery.evaluator.values.operations.EffectiveBooleanValue;
import com.github.akruk.antlrxquery.evaluator.values.operations.Stringifier;
import com.github.akruk.antlrxquery.evaluator.values.operations.ValueAtomizer;
import com.github.akruk.antlrxquery.evaluator.values.operations.ValueComparisonOperator;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;
import com.github.akruk.nodegetter.NodeGetter;

public class XQueryEvaluatingFunctionManager {
    private static final ParseTree CONTEXT_VALUE = getTree(".", parser -> parser.contextValueRef());
    private static final ParseTree DEFAULT_COLLATION = getTree("fn:default-collation()",
            parser -> parser.functionCall());
    private static final ParseTree EMPTY_SEQUENCE = getTree("()", p -> p.parenthesizedExpr());
    private static final ParseTree DEFAULT_ROUNDING_MODE = getTree("'half-to-ceiling'", parser -> parser.literal());
    private static final ParseTree ZERO_LITERAL = getTree("0", parser -> parser.literal());
    private static final ParseTree NFC = getTree("\"NFC\"", parser -> parser.literal());
    // private static final ParseTree STRING_AT_CONTEXT_VALUE =
    // getTree("fn:string(.)", (parser) -> parser.functionCall());
    private static final ParseTree EMPTY_STRING = getTree("\"\"", (parser) -> parser.literal());

    record FunctionEntry(XQueryFunction function, long minArity, long maxArity, List<String> argNames,
            Map<String, ParseTree> defaultArguments, String variadicArg, XQuerySequenceType type) {
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
    private final AggregateFunctions aggregateFunctions;
    private final OtherFunctionsOnNodes otherFuctionsOnNodes;
    // private final FunctionsOnSequencesOfNodes functionsOnSequencesOfNodes;
    // private final ParsingNumbers parsingNumbers;
    private final ProcessingStrings processingStrings;
    private final ProcessingBooleans processingBooleans;
    private final ProcessingSequencesFunctions processingSequences;
    private final XQueryTypeFactory typeFactory;

    public XQueryEvaluatingFunctionManager(final XQueryEvaluatorVisitor evaluator, final Parser parser,
            final XQueryValueFactory valueFactory, final NodeGetter nodeGetter, XQueryTypeFactory typeFactory, EffectiveBooleanValue ebv, ValueAtomizer atomizer, ValueComparisonOperator valueComparisonOperator)
    {
        this.valueFactory = valueFactory;
        this.evaluator = evaluator;
        this.typeFactory = typeFactory;
        this.namespaces = new HashMap<>(10);
        this.mathFunctions = new MathFunctions(valueFactory);
        this.accessors = new Accessors(valueFactory, parser, atomizer);
        this.functionsOnStringValues = new FunctionsOnStringValues(valueFactory, atomizer, new Stringifier(valueFactory, ebv));
        this.functionsOnNumericValues = new FunctionsOnNumericValues(valueFactory);
        this.functionsBasedOnSubstringMatching = new FunctionsBasedOnSubstringMatching(valueFactory);
        this.cardinalityFunctions = new CardinalityFunctions(valueFactory, atomizer);
        this.numericOperators = new NumericOperators(valueFactory);
        this.otherFuctionsOnNodes = new OtherFunctionsOnNodes(valueFactory, nodeGetter, parser);
        // this.functionsOnSequencesOfNodes = new FunctionsOnSequencesOfNodes(valueFactory, parser);
        this.processingSequences = new ProcessingSequencesFunctions(valueFactory, parser, atomizer);
        // this.parsingNumbers = new ParsingNumbers(valueFactory, parser);
        final Collator defaultCollator = Collations.DEFAULT_COLLATOR;
        final Map<String, Collator> collators = Map.of(Collations.CODEPOINT_URI, defaultCollator);
        this.processingStrings = new ProcessingStrings(valueFactory, parser, defaultCollator,
                collators, Locale.getDefault(), atomizer, ebv);
        this.processingBooleans = new ProcessingBooleans(valueFactory, parser, ebv);
        this.aggregateFunctions = new AggregateFunctions(valueFactory, parser, collators, atomizer, valueComparisonOperator);

        // Accessors
        final Map<String, ParseTree> defaultNodeArg = Map.of("node", CONTEXT_VALUE);
        final Map<String, ParseTree> defaultValueArg = Map.of("value", CONTEXT_VALUE);
        final Map<String, ParseTree> defaultInputArg = Map.of("input", CONTEXT_VALUE);

        registerFunction("fn", "name", accessors::nodeName, List.of("node"), defaultNodeArg);
        registerFunction("fn", "node-name", accessors::nodeName, List.of("node"), defaultNodeArg);
        final List<String> valueArg = List.of("value");
        registerFunction("fn", "string", accessors::string, valueArg, defaultValueArg);
        registerFunction("fn", "data", accessors::data, List.of("input"), defaultInputArg);

        final Map<String, ParseTree> noDefaults = Map.of();
        final Map<String, ParseTree> collationDefault = Map.of("collation", DEFAULT_COLLATION);
        final Map<String, ParseTree> zeroDefault = Map.of("zero", ZERO_LITERAL);

        registerFunction("fn", "count", aggregateFunctions::count, List.of("input"), noDefaults);

        registerFunction("fn", "avg", aggregateFunctions::avg, List.of("values"), noDefaults);

        registerFunction("fn", "max", aggregateFunctions::max, List.of("values", "collation"), collationDefault);

        registerFunction("fn", "min", aggregateFunctions::min, List.of("values", "collation"), collationDefault);

        registerFunction("fn", "sum", aggregateFunctions::sum, List.of("values", "zero"), zeroDefault);

        registerFunction("fn", "all-equal", aggregateFunctions::allEqual, List.of("values", "collation"),
                collationDefault);

        registerFunction("fn", "all-different", aggregateFunctions::allDifferent, List.of("values", "collation"),
                collationDefault);

        // Other functions on nodes
        registerFunction("fn", "root", otherFuctionsOnNodes::root, List.of("node"), defaultNodeArg);
        registerFunction("fn", "has-children", otherFuctionsOnNodes::hasChildren, List.of("node"), defaultNodeArg);
        registerFunction("fn", "siblings", otherFuctionsOnNodes::siblings, List.of("node"), defaultNodeArg);

        registerFunction("fn", "true", processingBooleans::true_, List.of(), Map.of());
        registerFunction("fn", "false", processingBooleans::false_, List.of(), Map.of());
        registerFunction("fn", "boolean", processingBooleans::boolean_, List.of("input"), Map.of());
        registerFunction("fn", "not", processingBooleans::not, List.of("input"), Map.of());

        registerFunction("op", "boolean-equal", processingBooleans::booleanEqual, List.of("arg1", "arg2"), Map.of());

        registerFunction("op", "boolean-less-than", processingBooleans::booleanLessThan, List.of("arg1", "arg2"),
                Map.of());

        registerFunction("op", "boolean-less-than-or-equal", processingBooleans::booleanLessThanOrEqual,
                List.of("arg1", "arg2"), Map.of());

        registerFunction("op", "boolean-greater-than", processingBooleans::booleanGreaterThan, List.of("arg1", "arg2"),
                Map.of());

        registerFunction("op", "boolean-greater-than-or-equal", processingBooleans::booleanGreaterThanOrEqual,
                List.of("arg1", "arg2"), Map.of());

        // Processing numerics
        registerFunction("fn", "abs", functionsOnNumericValues::abs, valueArg, Map.of());

        registerFunction("fn", "ceiling", functionsOnNumericValues::ceiling, valueArg, Map.of());

        registerFunction("fn", "floor", functionsOnNumericValues::floor, valueArg, Map.of());

        registerFunction("fn", "round", functionsOnNumericValues::round, List.of("value", "precision", "mode"),
                Map.of("precision", ZERO_LITERAL, "mode", DEFAULT_ROUNDING_MODE));

        registerFunction("fn", "round-half-to-even", functionsOnNumericValues::roundHalfToEven,
                List.of("value", "precision"), Map.of("precision", ZERO_LITERAL));

        registerFunction("fn", "divide-decimals", functionsOnNumericValues::divideDecimals,
                List.of("value", "divisor", "precision"), Map.of("precision", ZERO_LITERAL));

        // Processing sequences
        final Map<String, ParseTree> emptyInputArg = Map.of("input", EMPTY_SEQUENCE);
        registerFunction("fn", "empty", processingSequences::empty, List.of("input"), Map.of());

        registerFunction("fn", "exists", processingSequences::exists, List.of("input"), Map.of());

        registerFunction("fn", "foot", processingSequences::foot, List.of("input"), Map.of());

        registerFunction("fn", "head", processingSequences::head, List.of("input"), Map.of());

        registerFunction("fn", "identity", processingSequences::identity, List.of("input"), Map.of());

        registerFunction("fn", "insert-before", processingSequences::insertBefore,
                List.of("input", "position", "insert"), Map.of());

        registerFunction("fn", "items-at", processingSequences::itemsAt, List.of("input", "at"), Map.of());

        registerFunction("fn", "remove", processingSequences::remove, List.of("input", "positions"), Map.of());

        registerFunction("fn", "replicate", processingSequences::replicate, List.of("input", "count"), Map.of());

        registerFunction("fn", "reverse", processingSequences::reverse, List.of("input"), Map.of());

        registerFunction("fn", "sequence-join", processingSequences::sequenceJoin, List.of("input", "separator"),
                Map.of());

        registerFunction("fn", "slice", processingSequences::slice, List.of("input", "start", "end", "step"),
                Map.of("start", EMPTY_SEQUENCE, "end", EMPTY_SEQUENCE, "step", EMPTY_SEQUENCE));

        registerFunction("fn", "subsequence", processingSequences::subsequence, List.of("input", "start", "length"),
                Map.of("length", EMPTY_SEQUENCE));

        registerFunction("fn", "tail", processingSequences::tail, List.of("input"), Map.of());

        registerFunction("fn", "trunk", processingSequences::trunk, List.of("input"), Map.of());

        registerFunction("fn", "unordered", processingSequences::unordered, List.of("input"), Map.of());

        registerFunction("fn", "void", processingSequences::voidFunction, List.of("input"), emptyInputArg);

        registerFunction("fn", "codepoints-to-string", processingStrings::codepointsToString, List.of("values"),
                Map.of());

        registerFunction("fn", "string-to-codepoints", processingStrings::stringToCodepoints, List.of("value"),
                Map.of());

        registerFunction("fn", "codepoint-equal", processingStrings::codepointEqual, List.of("value1", "value2"),
                Map.of());

        // registerFunction("fn", "collation", processingStrings::collation,
        // List.of("options"), Map.of());

        registerFunction("fn", "collation-available", processingStrings::collationAvailable,
                List.of("collation", "usage"), Map.of("usage", EMPTY_SEQUENCE));

        // registerFunction("fn", "collation-key", processingStrings::collationKey,
        // List.of("value", "collation"),
        // Map.of("collation", DEFAULT_COLLATION));

        registerFunction("fn", "contains-token", processingStrings::containsToken,
                List.of("value", "token", "collation"), Map.of("collation", DEFAULT_COLLATION));

        registerFunction("fn", "distinct-values", this::distinctValues, List.of("values", "collation"),
                Map.of("collation", DEFAULT_COLLATION));

        registerFunction("fn", "zero-or-one", cardinalityFunctions::zeroOrOne, List.of("input"), Map.of());

        registerFunction("fn", "one-or-more", cardinalityFunctions::oneOrMore, List.of("input"), Map.of());

        registerFunction("fn", "exactly-one", cardinalityFunctions::exactlyOne, List.of("input"), Map.of());

        registerFunction("fn", "default-collation", this::defaultCollation, List.of(), Map.of());

        final Map<String, ParseTree> DEFAULT_COLLATION_MAP = Map.of("collation", DEFAULT_COLLATION);
        final List<String> valueSubstringCollation = List.of("value", "substring", "collation");
        registerFunction("fn", "contains", functionsBasedOnSubstringMatching::contains, valueSubstringCollation,
                DEFAULT_COLLATION_MAP);
        registerFunction("fn", "starts-with", functionsBasedOnSubstringMatching::startsWith, valueSubstringCollation,
                DEFAULT_COLLATION_MAP);
        registerFunction("fn", "ends-with", functionsBasedOnSubstringMatching::endsWith, valueSubstringCollation,
                DEFAULT_COLLATION_MAP);
        registerFunction("fn", "substring-after", functionsBasedOnSubstringMatching::substringAfter,
                valueSubstringCollation, DEFAULT_COLLATION_MAP);
        registerFunction("fn", "substring-before", functionsBasedOnSubstringMatching::substringBefore,
                valueSubstringCollation, DEFAULT_COLLATION_MAP);

        registerFunction("fn", "char", functionsOnStringValues::char_, valueArg, Map.of());

        registerFunction("fn", "characters", functionsOnStringValues::characters, valueArg, Map.of());

        registerFunction("fn", "graphemes", functionsOnStringValues::graphemes, valueArg, Map.of());

        registerVariadicFunction("fn", "concat", functionsOnStringValues::concat, "values");

        registerFunction("fn", "string-join", functionsOnStringValues::stringJoin, List.of("values", "separator"),
                Map.of("separator", EMPTY_STRING));

        registerFunction("fn", "substring", functionsOnStringValues::substring, List.of("value", "start", "length"),
                Map.of("length", EMPTY_SEQUENCE));

        registerFunction("fn", "string-length", functionsOnStringValues::stringLength, valueArg,
                Map.of("value", CONTEXT_VALUE)); // := fn:string(.)

        registerFunction("fn", "normalize-space", functionsOnStringValues::normalizeSpace, valueArg,
                Map.of("value", CONTEXT_VALUE)); // := fn:string(.)

        registerFunction("fn", "normalize-unicode", functionsOnStringValues::normalizeUnicode, List.of("value", "form"),
                Map.of("form", NFC));

        registerFunction("fn", "upper-case", functionsOnStringValues::upperCase, valueArg, Map.of());

        registerFunction("fn", "lower-case", functionsOnStringValues::lowerCase, valueArg, Map.of());

        registerFunction("fn", "translate", functionsOnStringValues::translate, List.of("value", "map", "trans"),
                Map.of());

        registerFunction("fn", "replace", this::replace, List.of(), Map.of());
        registerFunction("fn", "position", this::position, List.of(), Map.of());
        registerFunction("fn", "last", this::last, List.of(), Map.of());

        registerFunction("math", "pi", mathFunctions::pi, List.of(), Map.of());

        registerFunction("math", "e", mathFunctions::e, List.of(), Map.of());

        registerFunction("math", "exp", mathFunctions::exp, valueArg, Map.of());

        registerFunction("math", "exp10", mathFunctions::exp10, valueArg, Map.of());

        registerFunction("math", "log", mathFunctions::log, valueArg, Map.of());

        registerFunction("math", "log10", mathFunctions::log10, valueArg, Map.of());

        registerFunction("math", "pow", mathFunctions::pow, List.of("x", "y"), Map.of());

        registerFunction("math", "sqrt", mathFunctions::sqrt, valueArg, Map.of());

        registerFunction("math", "sin", mathFunctions::sin, List.of("radians"), Map.of());

        registerFunction("math", "cos", mathFunctions::cos, List.of("radians"), Map.of());

        registerFunction("math", "tan", mathFunctions::tan, List.of("radians"), Map.of());

        registerFunction("math", "asin", mathFunctions::asin, valueArg, Map.of());

        registerFunction("math", "acos", mathFunctions::acos, valueArg, Map.of());

        registerFunction("math", "atan", mathFunctions::atan, valueArg, Map.of());

        registerFunction("math", "atan2", mathFunctions::atan2, List.of("y", "x"), Map.of());

        registerFunction("math", "sinh", mathFunctions::sinh, valueArg, Map.of());

        registerFunction("math", "cosh", mathFunctions::cosh, valueArg, Map.of());

        registerFunction("math", "tanh", mathFunctions::tanh, valueArg, Map.of());

        registerFunction("op", "numeric-add", numericOperators::numericAdd, List.of("arg1", "arg2"), Map.of());

        registerFunction("op", "numeric-subtract", numericOperators::numericSubtract, List.of("arg1", "arg2"),
                Map.of());

        registerFunction("op", "numeric-multiply", numericOperators::numericMultiply, List.of("arg1", "arg2"),
                Map.of());

        registerFunction("op", "numeric-divide", numericOperators::numericDivide, List.of("arg1", "arg2"), Map.of());

        registerFunction("op", "numeric-integer-divide", numericOperators::numericIntegerDivide,
                List.of("arg1", "arg2"), Map.of());

        registerFunction("op", "numeric-mod", numericOperators::numericMod, List.of("arg1", "arg2"), Map.of());

        registerFunction("op", "numeric-unary-plus", numericOperators::numericUnaryPlus, List.of("arg"), Map.of());

        registerFunction("op", "numeric-unary-minus", numericOperators::numericUnaryMinus, List.of("arg"), Map.of());

        registerFunction("op", "numeric-equal", numericOperators::numericEqual, List.of("arg1", "arg2"), Map.of());

        registerFunction("op", "numeric-less-than", numericOperators::numericLessThan, List.of("arg1", "arg2"),
                Map.of());

        registerFunction("op", "numeric-less-than-or-equal", numericOperators::numericLessThanOrEqual,
                List.of("arg1", "arg2"), Map.of());

        registerFunction("op", "numeric-greater-than", numericOperators::numericGreaterThan, List.of("arg1", "arg2"),
                Map.of());

        registerFunction("op", "numeric-greater-than-or-equal", numericOperators::numericGreaterThanOrEqual,
                List.of("arg1", "arg2"), Map.of());

    }

    /**
     * fn:default-collation() as xs:string Returns the URI of the default collation
     * from the dynamic context.
     */
    public XQueryValue defaultCollation(final XQueryVisitingContext context, final List<XQueryValue> args) {
        return valueFactory.string(Collations.CODEPOINT_URI);
    }

    public XQueryValue distinctValues(final XQueryVisitingContext ctx, final List<XQueryValue> args) {
        return null;
        // return args.get(0).distinctValues();
    }

    public XQueryValue position(final XQueryVisitingContext context, final List<XQueryValue> args) {
        if (!args.isEmpty())
            return error(XQueryError.WrongNumberOfArguments, "", typeFactory.error());
        return valueFactory.number(context.getPosition());
    }

    public XQueryValue last(final XQueryVisitingContext context, final List<XQueryValue> args) {
        if (!args.isEmpty())
            return error(XQueryError.WrongNumberOfArguments, "", typeFactory.error());
        return valueFactory.number(context.getSize());
    }

    record ParseFlagsResult(int flags, String newPattern, String newReplacement) {
    }

    public ParseFlagsResult parseFlags(final String flags, String pattern, String replacement) {
        int flagBitMap = 0;
        final Set<Character> uniqueFlags = flags.chars().mapToObj(i -> (char) i).collect(Collectors.toSet());
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

    public XQueryValue replace(final XQueryVisitingContext context, final List<XQueryValue> args) {
        if (args.size() == 3) {
            try {
                final String input = args.get(0).stringValue;
                final String pattern = args.get(1).stringValue;
                final String replacement = args.get(2).stringValue;
                final String result = input.replaceAll(pattern, replacement);
                return valueFactory.string(result);
            } catch (final Exception e) {
                return error(XQueryError.InvalidRegex, "", typeFactory.error());
            }
        } else if (args.size() == 4) {
            try {
                final String input = args.get(0).stringValue;
                final String pattern = args.get(1).stringValue;
                final String replacement = args.get(2).stringValue;
                final String flags = args.get(3).stringValue;
                final var parsed = parseFlags(flags, pattern, replacement);
                final Pattern compiled = Pattern.compile(parsed.newPattern(), parsed.flags());
                final String result = compiled.matcher(input).replaceAll(parsed.newReplacement());
                return valueFactory.string(result);
            } catch (final Exception e) {
                return error(XQueryError.InvalidRegex, "", typeFactory.error());
            }
        } else {
            return error(XQueryError.WrongNumberOfArguments, "", typeFactory.error());
        }
    }

    public void registerFunction(final String namespace, final String localName, final XQueryFunction function,
            final List<String> argNames, final Map<String, ParseTree> defaultArguments) {
        final var maxArity = argNames.size();
        final var minArity = maxArity - defaultArguments.size();
        final FunctionEntry functionEntry = new FunctionEntry(function, minArity, maxArity, argNames, defaultArguments,
                null, null);
        final Map<String, List<FunctionEntry>> namespaceFunctions = namespaces.computeIfAbsent(namespace,
                _ -> new HashMap<>());
        final List<FunctionEntry> functionsWithDesiredName = namespaceFunctions.computeIfAbsent(localName,
                _ -> new ArrayList<FunctionEntry>());
        functionsWithDesiredName.add(functionEntry);
    }

    public void registerVariadicFunction(final String namespace, final String localName, final XQueryFunction function,
            final String variadicArg) {
        final var maxArity = Long.MAX_VALUE;
        final var minArity = 0;
        final FunctionEntry functionEntry = new FunctionEntry(function, minArity, maxArity, List.of(), Map.of(),
                variadicArg, null);
        final Map<String, List<FunctionEntry>> namespaceFunctions = namespaces.computeIfAbsent(namespace,
                _ -> new HashMap<>());
        final List<FunctionEntry> functionsWithDesiredName = namespaceFunctions.computeIfAbsent(localName,
                _ -> new ArrayList<FunctionEntry>());
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
            return new FunctionOrError(null, error(XQueryError.UnknownFunctionName, "", typeFactory.error()));
        }
        final List<FunctionEntry> functionsWithGivenName = functionsInNs.get(functionName);
        final Predicate<FunctionEntry> withinArityRange = f -> (f.minArity() <= arity && arity <= f.maxArity());
        final Optional<FunctionEntry> functionWithRequiredArity = functionsWithGivenName.stream()
                .filter(withinArityRange).findFirst();
        if (!functionWithRequiredArity.isPresent())
            return new FunctionOrError(null, error(XQueryError.WrongNumberOfArguments, "", typeFactory.error()));
        return new FunctionOrError(functionWithRequiredArity.get(), null);
    }

    public XQueryValue call(final String namespace, final String functionName, final XQueryVisitingContext context,
            final List<XQueryValue> args, final Map<String, XQueryValue> keywordArgs) {
        // Copy is made to allow for immutable hashmaps
        final var keywordArgs_ = new HashMap<>(keywordArgs);
        final int argsCount = args.size();
        final long arity = argsCount + keywordArgs_.size();
        final FunctionOrError functionOrError = getFunction(namespace, functionName, arity);
        if (functionOrError.isError())
            return functionOrError.error();
        final FunctionEntry functionEntry = functionOrError.entry();
        if (functionEntry.variadicArg != null) {
            return callVariadicFunction(functionEntry, context, args, keywordArgs);
        }
        final var function = functionEntry.function;
        final var defaultArgs = functionEntry.defaultArguments;

        // Additional positional args are ignored as per function coersion rules
        final var positionalSize = Math.min(argsCount, functionEntry.maxArity);
        // Too few args are however an error
        if (positionalSize < functionEntry.minArity)
            return error(XQueryError.WrongNumberOfArguments, "", typeFactory.error());

        final List<String> remainingArgs = functionEntry.argNames.subList(argsCount, (int) functionEntry.maxArity);
        final Stream<String> defaultedArgumentNames = functionEntry.defaultArguments.keySet().stream()
                .filter(name -> remainingArgs.contains(name) && !keywordArgs_.containsKey(name));
        final Map<String, XQueryValue> defaultedArguments = defaultedArgumentNames
                .collect(Collectors.toMap(name -> name, name -> {
                    final ParseTree default_ = defaultArgs.get(name);
                    return default_.accept(evaluator);
                }));
        keywordArgs_.putAll(defaultedArguments);
        var rearranged = rearrangeArguments(remainingArgs, context, args, keywordArgs_);
        return function.call(context, rearranged);
    }

    private XQueryValue callVariadicFunction(FunctionEntry functionEntry, XQueryVisitingContext context,
            List<XQueryValue> args, Map<String, XQueryValue> keywordArgs) {
        return functionEntry.function.call(context, args);
    }

    public XQueryValue getFunctionReference(final String namespace, final String functionName, final long arity) {
        final FunctionOrError function = getFunction(namespace, functionName, arity);
        if (function.isError()) {
            return error(XQueryError.UnknownFunctionName, "", typeFactory.error());
        }
        return valueFactory.functionReference(function.entry().function, null);
    }

    private static ParseTree getTree(final String xquery, final Function<AntlrXqueryParser, ParseTree> initialRule) {
        final CodePointCharStream charStream = CharStreams.fromString(xquery);
        final AntlrXqueryLexer lexer = new AntlrXqueryLexer(charStream);
        final CommonTokenStream stream = new CommonTokenStream(lexer);
        final AntlrXqueryParser parser = new AntlrXqueryParser(stream);
        return initialRule.apply(parser);
    }

    List<XQueryValue> rearrangeArguments(final List<String> remainingArgs, final XQueryVisitingContext context,
            final List<XQueryValue> args, final Map<String, XQueryValue> keywordArgs) {
        final List<XQueryValue> rearranged = new ArrayList<>(args.size() + keywordArgs.size());
        rearranged.addAll(args);
        for (var arg : remainingArgs) {
            var argValue = keywordArgs.get(arg);
            if (argValue == null) {
                argValue = valueFactory.error(XQueryError.WrongNumberOfArguments, "Argument " + arg + " is missing");
            }
            rearranged.add(argValue);
        }
        return rearranged;
    }

}
