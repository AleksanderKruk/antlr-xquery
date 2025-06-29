package com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.defaults;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.akruk.antlrxquery.semanticanalyzer.XQueryVisitingSemanticContext;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.IXQuerySemanticFunctionManager;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQuerySemanticFunctionManager implements IXQuerySemanticFunctionManager {

    public interface XQuerySemanticFunction {
        public CallAnalysisResult call(final XQueryTypeFactory typeFactory,
                final XQueryVisitingSemanticContext context,
                final List<XQuerySequenceType> types);
    }

    // private final FunctionTrie functionTrie = new FunctionTrie();

    private final XQueryTypeFactory typeFactory;

    public XQuerySemanticFunctionManager(final XQueryTypeFactory typeFactory) {
        this.typeFactory = typeFactory;
        this.namespaces = new HashMap<>(6);

        // --------------------------------------------------------------------
        // 0-argument functions
        // --------------------------------------------------------------------
        register("fn", "true", List.of(), typeFactory.boolean_());
        register("fn", "false", List.of(), typeFactory.boolean_());
        register("fn", "position", List.of(), typeFactory.number());
        register("fn", "last", List.of(), typeFactory.number());

        // --------------------------------------------------------------------
        // Boolean functions
        // --------------------------------------------------------------------
        final XQuerySequenceType zeroOrMoreItems = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
        ArgumentSpecification argItems = new ArgumentSpecification("input", true, zeroOrMoreItems);
        register("fn", "not", List.of(argItems), typeFactory.boolean_());

        // --------------------------------------------------------------------
        // Numeric functions
        // --------------------------------------------------------------------
        final XQuerySequenceType optionalNumber = typeFactory.zeroOrOne(typeFactory.itemNumber());
        final ArgumentSpecification valueNum =
            new ArgumentSpecification("value", true,
                optionalNumber
            );
        ArgumentSpecification precision =
            new ArgumentSpecification("precision", false,
                typeFactory.enum_(Set.of("floor",
                                            "ceiling",
                                            "toward-zero",
                                            "away-from-zero",
                                            "half-to-floor",
                                            "half-to-ceiling",
                                            "half-toward-zero",
                                            "half-away-from-zero",
                                            "half-to-even"))
            );
        ArgumentSpecification roundingMode =
            new ArgumentSpecification("mode", true,
                typeFactory.one(typeFactory.itemNumber())
            );


        register("fn", "abs", List.of(valueNum), optionalNumber);
        register("fn", "ceiling", List.of(valueNum), optionalNumber);
        register("fn", "floor", List.of(valueNum), optionalNumber);
        register("fn", "round",
            List.of(valueNum, precision, roundingMode), optionalNumber);

        // --------------------------------------------------------------------
        // Sequence-manipulation
        // --------------------------------------------------------------------
        final ArgumentSpecification sequence =
            new ArgumentSpecification("input", true, zeroOrMoreItems);
        final ArgumentSpecification position =
            new ArgumentSpecification("position", true,
                typeFactory.one(typeFactory.itemNumber()));

        final var optionalItem = typeFactory.zeroOrOne(typeFactory.itemAnyItem());

        register("fn", "empty", List.of(sequence), typeFactory.boolean_()
        );
        register("fn", "exists", List.of(sequence), typeFactory.boolean_()
        );
        register("fn", "head", List.of(sequence), optionalItem));
        register("fn", "tail", List.of(sequence), zeroOrMoreItems);

        final ArgumentSpecification insert =
            new ArgumentSpecification("insert", true, zeroOrMoreItems);

        register("fn", "insert-before",
            List.of(sequence, position, insert),
            zeroOrMoreItems
        );

        final var zeroOrMoreNumbers = typeFactory.zeroOrMore(typeFactory.itemNumber());

        final ArgumentSpecification positions =
            new ArgumentSpecification("positions", true, zeroOrMoreNumbers);

        register("fn", "remove",
            List.of(sequence, positions), zeroOrMoreItems);

        register("fn", "reverse",
            List.of(sequence), zeroOrMoreItems);

        final var start = new ArgumentSpecification("start", true, typeFactory.number());
        final var optionalLength = new ArgumentSpecification("length", false, optionalNumber);
        register("fn", "subsequence",
            List.of(sequence, start, optionalLength), zeroOrMoreItems);

        // --------------------------------------------------------------------
        // String functions
        // --------------------------------------------------------------------
        ArgumentSpecification sourceString =
            new ArgumentSpecification("sourceString", true,
                typeFactory.one(typeFactory.itemString())
            );
        final ArgumentSpecification pattern =
            new ArgumentSpecification("pattern", true, typeFactory.string());
        final ArgumentSpecification replacement =
            new ArgumentSpecification("replacement", true,
                typeFactory.string()
            );
        ArgumentSpecification flags =
            new ArgumentSpecification("flags", true,
                typeFactory.one(typeFactory.itemString())
            );

        final ArgumentSpecification optionalStringValue =
            new ArgumentSpecification("value", true,
                typeFactory.zeroOrOne(typeFactory.itemString())
            );
        register("fn", "substring",
            List.of(optionalStringValue, start, optionalLength),
            typeFactory.string()
        );

        final ArgumentSpecification optionalSubstring =
            new ArgumentSpecification("substring", true,
                typeFactory.zeroOrOne(typeFactory.itemString())
            );

        // TODO: collation
        register("fn", "contains",
            List.of(optionalStringValue, optionalSubstring),
            typeFactory.boolean_()
        );

        // TODO: collation
        register("fn", "starts-with",
            List.of(optionalStringValue, optionalSubstring),
            typeFactory.boolean_()
        );

        // TODO: collation
        register("fn", "ends-with",
            List.of(optionalStringValue, optionalSubstring),
            typeFactory.boolean_()
        );

        // TODO: collation
        register("fn", "substring-before",
            List.of(optionalStringValue, optionalSubstring),
            typeFactory.string()
        );

        // TODO: collation
        register("fn", "substring-after",
            List.of(optionalStringValue, optionalSubstring),
            typeFactory.string()
        );

        final var normalizedValue =
            new ArgumentSpecification("value", false, typeFactory.string());

        register("fn", "normalize-space",
            List.of(normalizedValue), typeFactory.string());

        register("fn", "upper-case", List.of(optionalStringValue), typeFactory.string());
        register("fn", "lower-case", List.of(optionalStringValue), typeFactory.string());



        register("fn", "replace",
            List.of(optionalStringValue, pattern, replacement),
            typeFactory.string()
        );
        register("fn", "replace",
            List.of(sourceString, pattern, replacement, flags),
            typeFactory.itemString()
        );

        // string(), concat(), string-join(), string-length()
        register("fn", "string",
            List.of(),
            typeFactory.itemString()
        );
        register("fn", "string",
            List.of(new ArgumentSpecification("arg", true, typeFactory.one(typeFactory.itemAnyItem()))),
            typeFactory.itemString()
        );
        register("fn", "concat",
            List.of(
                new ArgumentSpecification("s1", true, typeFactory.one(typeFactory.itemString())),
                new ArgumentSpecification("s2", true, typeFactory.one(typeFactory.itemString()))
            ),
            typeFactory.itemString()
        );
        register("fn", "string-join",
            List.of(new ArgumentSpecification("seq", true, typeFactory.zeroOrMore(typeFactory.itemString()))),
            typeFactory.itemString()
        );
        register("fn", "string-join",
            List.of(
                new ArgumentSpecification("seq", true, typeFactory.zeroOrMore(typeFactory.itemString())),
                new ArgumentSpecification("separator", true, typeFactory.one(typeFactory.itemString()))
            ),
            typeFactory.itemString()
        );
        register("fn", "string-length",
            List.of(new ArgumentSpecification("arg", false, typeFactory.zeroOrOne(typeFactory.itemString()))),
            typeFactory.itemNumber()
        );
        register("fn", "string-length",
            List.of(sourceString),
            typeFactory.itemNumber()
        );

        // --------------------------------------------------------------------
        // Sequence cardinality helpers
        // --------------------------------------------------------------------
        register("fn", "zero-or-one",
            List.of(sequence),
            typeFactory.zeroOrOne(typeFactory.itemAnyItem())
        );
        register("fn", "one-or-more",
            List.of(sequence),
            typeFactory.oneOrMore(typeFactory.itemAnyItem())
        );
        register("fn", "exactly-one",
            List.of(sequence),
            typeFactory.one(typeFactory.itemAnyItem())
        );

        // --------------------------------------------------------------------
        // Atomic data
        // --------------------------------------------------------------------
        register("fn", "data",
            List.of(sequence),
            typeFactory.zeroOrMore(typeFactory.itemAtomic())
        );

        // --------------------------------------------------------------------
        // Distinct-values
        // --------------------------------------------------------------------
        register("fn", "distinct-values",
            List.of(sequence),
            zeroOrMoreItems
        );
    }

    record FunctionSpecification(int minArity, int maxArity, List<ArgumentSpecification> args, XQuerySequenceType returnedType) {}
    final Map<String, Map<String, List<FunctionSpecification>>> namespaces;


    @Override
    public CallAnalysisResult call(String namespace,
                                String name,
                                List<XQuerySequenceType> positionalargs,
                                Map<String, XQuerySequenceType> keywordArgs,
                                XQueryVisitingSemanticContext context)
    {
        // TODO: add grained reporting for function mismatches if no function is found
        if (!namespaces.containsKey(namespace)) {
            var anyItems = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
            List<String> errors = List.of("Unknown function namespace: " + namespace);
            return new CallAnalysisResult(anyItems, errors);
        }

        final var namespaceFunctions = namespaces.get(namespace);
        if (!namespaceFunctions.containsKey(name)) {
            var anyItems = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
            List<String> errors = List.of("Unknown function: " + namespace + ":" + name);
            return new CallAnalysisResult(anyItems, errors);
        }

        final var namedFunctions = namespaceFunctions.get(name);
        final int positionalArgsCount = positionalargs.size();
        final var requiredArity = positionalArgsCount + keywordArgs.size();

        SPECIFICATION:
        for (final FunctionSpecification spec : namedFunctions) {
            if (spec.minArity() <= requiredArity && requiredArity <= spec.maxArity()) {
                // used positional arguments need to have matching types
                for (int i = 0; i < positionalArgsCount; i++) {
                    final var positionalArg = positionalargs.get(i);
                    final var expectedArg = spec.args.get(i);
                    if (!positionalArg.isSubtypeOf(expectedArg.type())) {
                        continue SPECIFICATION;
                    }
                }
                final List<String> allArgNames = spec.args.stream().map(ArgumentSpecification::name).toList();
                // used keywords need to match argument names in function declaration
                if (!allArgNames.containsAll(keywordArgs.keySet())) {
                    continue SPECIFICATION;
                }
                final int specifiedArgsSize = spec.args.size();
                final List<String> remainingArgNames = allArgNames.subList(positionalArgsCount, specifiedArgsSize);
                // used keywords mustn't be any of the used positional args
                if (!remainingArgNames.containsAll(keywordArgs.keySet())) {
                    continue SPECIFICATION;
                }
                // args that have not been positionally assigned
                final var remainingArgs = spec.args.subList(positionalArgsCount, specifiedArgsSize);
                final var usedAsKeywordCriterion = Collectors.<ArgumentSpecification>partitioningBy(arg->keywordArgs.containsKey(arg.name()));
                final var partitioned = remainingArgs.parallelStream()
                                                     .collect(usedAsKeywordCriterion);
                // all the arguments that HAVE NOT been used as keywords in call need to be optional
                if (partitioned.get(false).parallelStream().noneMatch(ArgumentSpecification::isRequired)) {
                    continue SPECIFICATION;
                }
                // all the arguments that HAVE been used as keywords in call need to have matching type
                partitioned.get(true).parallelStream().allMatch(arg->{
                    final XQuerySequenceType passedType = keywordArgs.get(arg.name());
                    return passedType.isSubtypeOf(arg.type());
                });
                return new CallAnalysisResult(spec.returnedType(), List.of());
            }
        }

        final var anyItems = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
        List<String> errors = List.of(
            "No matching function" + namespace + ":" + name + " for arity " + requiredArity + " in function " + namespace + ":" + name
        );
        return new CallAnalysisResult(anyItems, errors);
    }

    @Override
    public CallAnalysisResult getFunctionReference(String namespace, String functionName, int arity) {
        // Optional<CallAnalysisResult> result = functionTrie.resolve(functionName);
        // if (result.isPresent()) {
        //     return result.get();
        // }
        // var fallback = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
        // return new CallAnalysisResult(fallback, List.of("Unknown function reference: " + functionName));
        return null;
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
}
