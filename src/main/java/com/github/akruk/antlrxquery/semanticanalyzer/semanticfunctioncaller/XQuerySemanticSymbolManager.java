package com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.inputgrammaranalyzer.InputGrammarAnalyzer.QualifiedGrammarAnalysisResult;
import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver.QualifiedName;
import com.github.akruk.antlrxquery.semanticanalyzer.DiagnosticError;
import com.github.akruk.antlrxquery.semanticanalyzer.ErrorType;
import com.github.akruk.antlrxquery.semanticanalyzer.XQuerySemanticAnalyzer;
import com.github.akruk.antlrxquery.semanticanalyzer.XQuerySemanticAnalyzer.UnresolvedFunctionSpecification;
import com.github.akruk.antlrxquery.semanticanalyzer.XQuerySemanticError;
import com.github.akruk.antlrxquery.semanticanalyzer.XQueryVisitingSemanticContext;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.XQuerySemanticContext;
import com.github.akruk.antlrxquery.typesystem.defaults.TypeInContext;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQuerySemanticSymbolManager {
    public static record AnalysisResult(
        TypeInContext result,
        List<DiagnosticError> errors
        )
    {}

    public static record ArgumentSpecification(
        String name,
        XQuerySequenceType type,
        ParseTree defaultArgument) {}

    public static record UsedArg(
        TypeInContext type,
        XQueryValue value,
        ParseTree tree
        ) {}
    public interface GrainedAnalysis {
        TypeInContext analyze(
            List<UsedArg> args,
            XQueryVisitingSemanticContext context,
            ParseTree functionBody,
            XQuerySemanticContext typeContext
        );

    }
    public static record FunctionSpecification(
        long minArity,
        long maxArity,
        List<ArgumentSpecification> args,
        XQuerySequenceType returnedType,
        XQuerySequenceType requiredContextValueType,
        boolean requiresPosition,
        boolean requiresSize,
        ParseTree body,
        GrainedAnalysis grainedAnalysis)
    { }

    public static record SimplifiedFunctionSpecification(
        QualifiedName qname,
        List<ArgumentSpecification> args,
        XQuerySequenceType returnedType,
        XQuerySequenceType requiredContextValueType,
        boolean requiresPosition,
        boolean requiresSize,
        ParseTree body,
        GrainedAnalysis grainedAnalysis)
    { }



    public interface XQuerySemanticFunction {
        public AnalysisResult call(final XQueryTypeFactory typeFactory,
                final XQueryVisitingSemanticContext context,
                final List<XQuerySequenceType> types);
    }
    private final XQueryTypeFactory typeFactory;
    private XQuerySemanticAnalyzer analyzer;

    public void setAnalyzer(
        final XQuerySemanticAnalyzer analyzer
        )
    {
        this.analyzer = analyzer;
    }

    public XQuerySemanticSymbolManager(
        final XQueryTypeFactory typeFactory,
        final List<List<SimplifiedFunctionSpecification>> functionSets)
    {
        this.typeFactory = typeFactory;
        this.namespaces = new HashMap<>(10);
        for (final var fset : functionSets) {
            for (final var f : fset) {
                final var functionname = f.qname();
                uncheckedRegisterFunction(
                    functionname.namespace(),
                    functionname.name(),
                    f.args,
                    f.returnedType,
                    f.requiredContextValueType,
                    f.requiresPosition,
                    f.requiresSize,
                    f.body,
                    f.grainedAnalysis);
            }
        }
        this.grammars = new HashMap<>();
        this.functionDeclarations = new HashMap<>();
    }

    final Map<String, Map<String, List<FunctionSpecification>>> namespaces;
    final Map<String, QualifiedGrammarAnalysisResult> grammars;

    private AnalysisResult handleUnknownNamespace(
        final String namespace,
        final DiagnosticError errorMessageSupplier,
        final TypeInContext fallbackType
        )
    {
        final List<DiagnosticError> errors = List.of(errorMessageSupplier);
        return new AnalysisResult(fallbackType, errors);
    }

    private AnalysisResult handleUnknownFunction(final DiagnosticError errorMessageSupplier, final TypeInContext fallbackType)
    {
        final List<DiagnosticError> errors = List.of(errorMessageSupplier);
        return new AnalysisResult(fallbackType, errors);
    }

    private AnalysisResult handleNoMatchingFunction(
        final DiagnosticError errorMessageSupplier,
        final TypeInContext fallbackType
        )
    {
        final List<DiagnosticError> errors = List.of(errorMessageSupplier);
        return new AnalysisResult(fallbackType, errors);
    }

    record SpecAndErrors(FunctionSpecification spec, List<DiagnosticError> errors) {
    }

    SpecAndErrors getFunctionSpecification(
        final ParserRuleContext location,
        final QualifiedName qName,
        final List<FunctionSpecification> namedFunctions,
        final long requiredArity
        )
    {
        final var namespace = qName.namespace();
        final var name = qName.name();
        final List<String> mismatchReasons = new ArrayList<>();
        for (final FunctionSpecification spec : namedFunctions) {
            final List<String> reasons = new ArrayList<>();
            if (!(spec.minArity() <= requiredArity && requiredArity <= spec.maxArity())) {
                reasons.add(
                    "Arity mismatch: expected between "
                        + spec.minArity() + " and " + spec.maxArity()
                        + ", got " + requiredArity);
                mismatchReasons.add("Function " + name + ": " + String.join("; ", reasons));
                continue;
            }
            // used positional arguments need to have matching types
            return new SpecAndErrors(spec, List.of());
        }
        final DiagnosticError error = DiagnosticError.of(
            location, ErrorType.FUNCTION__NO_MATCHING_FUNCTION, List.of(namespace, name, requiredArity));
        return new SpecAndErrors(null, List.of(error));
    }

    public AnalysisResult call(
            final ParserRuleContext location,
            final QualifiedName qName,
            final List<TypeInContext> positionalargs,
            final Map<String, TypeInContext> keywordArgs,
            final XQueryVisitingSemanticContext context,
            final XQuerySemanticContext typeContext
            )
    {
        final var anyItems = typeContext.currentScope().typeInContext(typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        final var namespace = qName.namespace();
        final var name = qName.name();
        if (!namespaces.containsKey(qName.namespace())) {
            final DiagnosticError error = DiagnosticError.of(location, ErrorType.FUNCTION__UNKNOWN_NAMESPACE, List.of(qName.namespace()));
            final DiagnosticError errorMessageSupplier = error;
            final List<DiagnosticError> errors = List.of(errorMessageSupplier);
            return new AnalysisResult(anyItems, errors);
        }

        final var namespaceFunctions = namespaces.get(qName.namespace());
        final boolean noFunctions = !namespaceFunctions.containsKey(qName.name());
        final List<UnresolvedFunctionSpecification> declarations = functionDeclarations.get(qName);

        if (noFunctions && (declarations == null || declarations.isEmpty())) {
            final DiagnosticError error = DiagnosticError.of(location, ErrorType.FUNCTION__UNKNOWN_FUNCTION, List.of(qName.namespace(), qName.name()));
            return handleUnknownFunction(error, anyItems);
        } else if (noFunctions) {
            // final int positionalArgsCount = positionalargs.size();
            // final var requiredArity = positionalArgsCount + keywordArgs.size();
            // for (var decl : declarations ) {
            //     if (decl.maxArity() >= requiredArity && decl.minArity() <= requiredArity) {
            //         if (decl.name().equals(qName)) {

            //         }
            //     }
            // }

        }


        final var namedFunctions = namespaceFunctions.get(qName.name());
        final int positionalArgsCount = positionalargs.size();
        final var requiredArity = positionalArgsCount + keywordArgs.size();
        final List<String> mismatchReasons = new ArrayList<>();
        final SpecAndErrors specAndErrors = getFunctionSpecification(
            location,
            qName,
            namedFunctions,
            requiredArity);
        if (specAndErrors.spec == null) {
            return new AnalysisResult(anyItems, specAndErrors.errors);
        }
        final var spec = specAndErrors.spec;
        // used positional arguments need to have matching types
        final List<String> reasons = new ArrayList<>();
        final boolean positionalTypeMismatch = tryToMatchPositionalArgs(
            positionalargs, positionalArgsCount, spec, reasons);

        if (positionalTypeMismatch) {
            mismatchReasons.add("Function " + name + ": " + String.join("; ", reasons));
        }

        checkIfCorrectContext(spec, context, mismatchReasons);

        final List<String> allArgNames = spec.args.stream().map(ArgumentSpecification::name).toList();
        // used keywords need to match argument names in function declaration
        checkIfCorrectKeywordNames(name, keywordArgs, mismatchReasons, reasons, allArgNames);

        // TODO: unique keyword names
        final int specifiedArgsSize = spec.args.size();
        final List<String> remainingArgNames = allArgNames.subList(positionalArgsCount, specifiedArgsSize);
        // used keywords mustn't be any of the used positional args
        checkIfKeywordNotInPositionalArgs(name, keywordArgs, mismatchReasons, reasons, remainingArgNames);

        // args that have not been positionally assigned
        final var remainingArgs = spec.args.subList(positionalArgsCount, specifiedArgsSize);
        final var usedAsKeywordCriterion = Collectors
                .<ArgumentSpecification>partitioningBy(arg -> keywordArgs.containsKey(arg.name()));
        final var unusedArgs = remainingArgs.parallelStream().collect(usedAsKeywordCriterion);
        final var unusedArgs_ = unusedArgs.get(false);
        checkIfAllUnusedArgumentsAreOptional(name, mismatchReasons, reasons, unusedArgs_);

        final Stream<ArgumentSpecification> defaultArgs = unusedArgs_.stream().filter(arg->arg.defaultArgument() != null);

        // all the arguments that HAVE been used as keywords in call need to have
        // matching type
        final boolean keywordTypeMismatch = checkIfTypesMatchForKeywordArgs(keywordArgs, reasons, unusedArgs);
        if (keywordTypeMismatch) {
            mismatchReasons.add("Function " + name + ": " + String.join("; ", reasons));
        }


        final Map<ArgumentSpecification, TypeInContext> defaultArgTypes = new HashMap<>();
        for (final ArgumentSpecification defaultArg : defaultArgs.toList()) {
            final var expectedType = defaultArg.type();
            final var receivedType = defaultArg.defaultArgument().accept(analyzer);
            if (!receivedType.type.isSubtypeOf(expectedType)) {
                mismatchReasons.add(String.format(
                    "Type mismatch for default argument '%s': expected '%s', but got '%s'.",
                    defaultArg.name(),
                    expectedType,
                    receivedType));
            }
            defaultArgTypes.put(defaultArg, receivedType);
        }

        if (mismatchReasons.isEmpty()) {
            if (spec.grainedAnalysis==null) {
                return new AnalysisResult(typeContext.typeInContext(spec.returnedType), List.of());
            } else {
                final List<UsedArg> args = new ArrayList<>(positionalargs.size() + keywordArgs.size());
                for (final TypeInContext positional : positionalargs) {
                    args.add(new UsedArg(positional, null, null));
                }
                for (final ArgumentSpecification arg : remainingArgs) {
                    final TypeInContext type = defaultArgTypes.get(arg);
                    if (type != null) {
                        args.add(new UsedArg(type, null, null));
                    } else {
                        final TypeInContext keywordType = keywordArgs.get(arg.name);
                        args.add(new UsedArg(keywordType, null, null));
                    }
                }

                final TypeInContext granularType = spec.grainedAnalysis.analyze(
                    args, context, location, typeContext);
                return new AnalysisResult(granularType, List.of());
            }
        } else {
            final DiagnosticError error = DiagnosticError.of(
                location,
                ErrorType.FUNCTION__NO_MATCHING_FUNCTION,
                List.of(namespace, name, requiredArity, mismatchReasons)
                );
            return handleNoMatchingFunction(error, typeContext.currentScope().typeInContext(spec.returnedType));

        }
    }

    private void checkIfCorrectContext(final FunctionSpecification spec, final XQueryVisitingSemanticContext context, final List<String> mismatchReasons)
    {
        if (spec.requiresPosition && context.getPositionType() == null) {
            mismatchReasons.add("Function requires context position");
        }
        if (spec.requiresSize && context.getSizeType() == null) {
            mismatchReasons.add("Function requires context size");
        }
        if (spec.requiredContextValueType != null
            && !context.getType().isSubtypeOf(spec.requiredContextValueType))
        {
            final String message = getIncorrectContextValueMessage(spec, context);
			mismatchReasons.add(message);
        }
	}

    private String getIncorrectContextValueMessage(final FunctionSpecification spec, final XQueryVisitingSemanticContext context) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Invalid context value: ");
        stringBuilder.append(context.getType().toString());
        stringBuilder.append(" is not subtype of ");
        stringBuilder.append(spec.requiredContextValueType.toString());
        return stringBuilder.toString();
    }

    private boolean checkIfTypesMatchForKeywordArgs(
            final Map<String, TypeInContext> keywordArgs,
            final List<String> reasons,
            final Map<Boolean, List<ArgumentSpecification>> partitioned)
    {
        boolean keywordTypeMismatch = false;
        for (final ArgumentSpecification arg : partitioned.get(true)) {
            final XQuerySequenceType passedType = keywordArgs.get(arg.name()).type;
            if (!passedType.isSubtypeOf(arg.type())) {
                reasons.add("Keyword argument '" + arg.name() + "' type mismatch:"
                        + "\n        expected: " + arg.type()
                        + "\n        found   : " + passedType);
                keywordTypeMismatch = true;
            }
        }
        return keywordTypeMismatch;
    }

    private void checkIfAllUnusedArgumentsAreOptional(final String name, final List<String> mismatchReasons,
            final List<String> reasons,
            final List<ArgumentSpecification> unusedArgs)
    {
        // all the arguments that HAVE NOT been used as keywords in call need to be
        // optional
        final boolean missingRequired = unusedArgs.parallelStream()
                .anyMatch(arg->arg.defaultArgument() == null);
        if (missingRequired) {
            final Stream<ArgumentSpecification> requiredUnusedArgs = unusedArgs.stream().filter(arg->arg.defaultArgument() == null);
            final Stream<String> requiredUnusedArgsNames = requiredUnusedArgs.map(ArgumentSpecification::name);
            final String missingRequiredArguments = requiredUnusedArgsNames.collect(Collectors.joining(", "));
            reasons.add("Missing required keyword argument(s): " + missingRequiredArguments);
            mismatchReasons.add("Function " + name + ": " + String.join("; ", reasons));
        }
    }

    private void checkIfKeywordNotInPositionalArgs(
        final String name,
        final Map<String, TypeInContext> keywordArgs,
        final List<String> mismatchReasons,
        final List<String> reasons,
        final List<String> remainingArgNames
        )
    {
        if (!remainingArgNames.containsAll(keywordArgs.keySet())) {
            reasons.add("Keyword argument(s) overlap with positional arguments: " + keywordArgs.keySet().stream()
                    .filter(k -> !remainingArgNames.contains(k)).collect(Collectors.joining(", ")));
            mismatchReasons.add("Function " + name + ": " + String.join("; ", reasons));
        }
    }

    private void checkIfCorrectKeywordNames(
        final String name, final Map<String, TypeInContext> keywordArgs,
        final List<String> mismatchReasons, final List<String> reasons, final List<String> allArgNames)
    {
        if (!allArgNames.containsAll(keywordArgs.keySet())) {
            reasons.add("Unknown keyword argument(s): " + keywordArgs.keySet().stream()
                    .filter(k -> !allArgNames.contains(k)).collect(Collectors.joining(", ")));
            mismatchReasons.add("Function " + name + ": " + String.join("; ", reasons));
        }
    }

    private boolean tryToMatchPositionalArgs(
        final List<TypeInContext> positionalargs,
        final int positionalArgsCount,
        final FunctionSpecification spec,
        final List<String> reasons)
    {
        boolean positionalTypeMismatch = false;
        for (int i = 0; i < positionalArgsCount; i++) {
            final var positionalArg = positionalargs.get(i);
            final var expectedArg = spec.args.get(i);
            if (!positionalArg.isSubtypeOf(expectedArg.type())) {
                reasons.add("Positional argument " + (i + 1) + " type mismatch: expected " + expectedArg.type()
                        + ", got " + positionalArg);
                positionalTypeMismatch = true;
            }
        }
        return positionalTypeMismatch;
    }

    public AnalysisResult getFunctionReference(final ParserRuleContext location,
                                                final QualifiedName qName,
                                                final int arity,
                                                final XQuerySemanticContext context)
    {
        // TODO: Verify logic
        var namespace = qName.namespace();
        var functionName = qName.name();
        final var fallback = context.currentScope().typeInContext(typeFactory.anyFunction());
        if (!namespaces.containsKey(namespace)) {
            final DiagnosticError error = DiagnosticError.of(location, ErrorType.FUNCTION__UNKNOWN_NAMESPACE, List.of(namespace));
            return handleUnknownNamespace(namespace, error, fallback);
        }
        final var namespaceFunctions = namespaces.get(namespace);
        if (!namespaceFunctions.containsKey(functionName)) {
            final DiagnosticError error = DiagnosticError.of(location, ErrorType.FUNCTION__UNKNOWN_FUNCTION, List.of(namespace, functionName));
            return handleUnknownFunction(error, fallback);
        }

        final var namedFunctions = namespaceFunctions.get(functionName);
        final SpecAndErrors specAndErrors = getFunctionSpecification(
            location, qName, namedFunctions, arity);
        if (specAndErrors.spec == null) {
            final DiagnosticError error = DiagnosticError.of(
                location, ErrorType.FUNCTION_REFERENCE__UNKNOWN, List.of(namespace, functionName, arity));
            return new AnalysisResult(fallback, List.of(error));
        }
        final TypeInContext returnedType = context.typeInContext(specAndErrors.spec.returnedType);
        final List<XQuerySequenceType> argTypes = specAndErrors.spec.args.stream()
            .map(arg->arg.type())
            .toList()
            .subList(0, arity);
        final var functionItem = typeFactory.function(returnedType.type, argTypes);
        return new AnalysisResult(context.currentScope().typeInContext(functionItem), specAndErrors.errors);

    }

    public FunctionSpecification getNamedFunctionSpecification(
        final ParserRuleContext location,
        final QualifiedName qName,
        final int arity)
    {
        if (!namespaces.containsKey(qName.namespace())) {
            return null;
        }
        final var namespaceFunctions = namespaces.get(qName.namespace());
        if (!namespaceFunctions.containsKey(qName.name())) {
            return null;
        }

        final var namedFunctions = namespaceFunctions.get(qName.name());
        final SpecAndErrors specAndErrors = getFunctionSpecification(
            location, qName, namedFunctions, arity);
        return specAndErrors.spec;
    }


    Map<QualifiedName, List<UnresolvedFunctionSpecification>> functionDeclarations;

    public enum DeclarationStatus {
        OK, COLLISION
    }

    public record DeclarationResult(
        DeclarationStatus status,
        List<ParserRuleContext> collisions
    ) {}

    public DeclarationResult declareFunction(final UnresolvedFunctionSpecification function)
    {
        final var minArity = function.minArity();
        final var maxArity = function.maxArity();
        final var alreadyDeclared = functionDeclarations.computeIfAbsent(
            function.name(), _ -> new ArrayList<UnresolvedFunctionSpecification>());
        final var overlapping = alreadyDeclared.stream().filter(f ->
            minArity <= f.maxArity() && f.minArity() <= maxArity
        ).map(UnresolvedFunctionSpecification::location).toList();
        if (overlapping.isEmpty()) {
            return new DeclarationResult(DeclarationStatus.OK, overlapping);
        } else {
            return new DeclarationResult(DeclarationStatus.COLLISION, overlapping);
        }
    }

    public XQuerySemanticError registerFunction(
            final String namespace,
            final String functionName,
            final List<ArgumentSpecification> args,
            final XQuerySequenceType returnedType) {
        return registerFunction(namespace, functionName, args, returnedType, null, false, false, null,
            (_, _, _, ctx) -> ctx.currentScope().typeInContext(returnedType));
    }

    public XQuerySemanticError registerFunction(
            final String namespace,
            final String functionName,
            final List<ArgumentSpecification> args,
            final XQuerySequenceType returnedType,
            final GrainedAnalysis analysis)
    {
        return registerFunction(namespace, functionName, args, returnedType,
            null, false,
            false, null, analysis);
    }

    public XQuerySemanticError registerFunction(
            final String namespace,
            final String functionName,
            final List<ArgumentSpecification> args,
            final XQuerySequenceType returnedType,
            final ParseTree body) {
        return registerFunction(
            namespace,
            functionName,
            args,
            returnedType,
            null,
            false,
            false,
            body,
            (List<UsedArg> arguments, XQueryVisitingSemanticContext _, ParseTree _, XQuerySemanticContext typeCtx) -> defaultGrainedFunctionAnalysis(args, body, arguments, typeCtx)
            );
    }

    private TypeInContext defaultGrainedFunctionAnalysis(final List<ArgumentSpecification> args, final ParseTree body,
            List<UsedArg> arguments, XQuerySemanticContext typeCtx) {
        for (int i = 0 ; i < args.size(); i++) {
            ArgumentSpecification argSpec = args.get(i);
            UsedArg usedArg = arguments.get(i);
            typeCtx.entypeVariable(argSpec.name, usedArg.type);
        }
        return body.accept(analyzer);
    }

    public XQuerySemanticError registerFunction(
            final String namespace,
            final String functionName,
            final List<ArgumentSpecification> args,
            final XQuerySequenceType returnedType,
            final ParseTree body,
            final GrainedAnalysis analysis) {
        return registerFunction(namespace, functionName, args, returnedType, null, false, false, body, analysis);
    }

    public XQuerySemanticError uncheckedRegisterFunction(
            final String namespace,
            final String functionName,
            final List<ArgumentSpecification> args,
            final XQuerySequenceType returnedType) {
        return uncheckedRegisterFunction(namespace, functionName, args, returnedType, null, false, false, null,
            ((_, _, _, ctx) -> ctx.currentScope().typeInContext(returnedType)));
    }


    public XQuerySemanticError uncheckedRegisterFunction(
            final String namespace,
            final String functionName,
            final List<ArgumentSpecification> args,
            final XQuerySequenceType returnedType,
            final ParseTree body,
            final GrainedAnalysis analysis) {
        return uncheckedRegisterFunction(
            namespace,
            functionName,
            args,
            returnedType,
            null,
            false,
            false,
            body,
            analysis
            );
    }

    private XQuerySemanticError uncheckedRegisterFunction(
            final String namespace,
            final String functionName,
            final List<ArgumentSpecification> args,
            final XQuerySequenceType returnedType,
            final XQuerySequenceType requiredContextValueType,
            final boolean requiresPosition,
            final boolean requiresLength,
            final ParseTree body,
            final GrainedAnalysis analysis)
    {
        final long minArity = args.stream().filter(arg -> arg.defaultArgument() == null).collect(Collectors.counting());
        final long maxArity = args.size();
        final FunctionSpecification function = new FunctionSpecification(
            minArity,
            maxArity,
            args,
            returnedType,
            requiredContextValueType,
            requiresPosition,
            requiresLength,
            body,
            analysis
            );
        namespaces.computeIfAbsent(namespace, x->new HashMap<>())
            .computeIfAbsent(functionName, x->new ArrayList<>())
            .add(function);
        return null;
    }


    public XQuerySemanticError registerFunction(
            final String namespace,
            final String functionName,
            final List<ArgumentSpecification> args,
            final XQuerySequenceType returnedType,
            final XQuerySequenceType requiredContextValueType,
            final boolean requiresPosition,
            final boolean requiresLength,
            final ParseTree body,
            final GrainedAnalysis analysis)
    {
        final long minArity = args.stream()
            .filter(arg -> arg.defaultArgument() == null)
            .collect(Collectors.counting());
        final long maxArity = args.size();
        if (!namespaces.containsKey(namespace)) {
            final Map<String, List<FunctionSpecification>> functions = new HashMap<>();
            final List<FunctionSpecification> functionList = new ArrayList<>();
            functionList.add(
                new FunctionSpecification(
                    minArity,
                    maxArity,
                    args,
                    returnedType,
                    requiredContextValueType,
                    requiresPosition,
                    requiresLength,
                    body,
                    analysis));
            functions.put(functionName, functionList);
            namespaces.put(namespace, functions);
            return null;
        }
        final var namespaceMapping = namespaces.get(namespace);
        if (!namespaceMapping.containsKey(functionName)) {
            final List<FunctionSpecification> functionList = new ArrayList<>();
            functionList.add(new FunctionSpecification(
                minArity, maxArity, args, returnedType, requiredContextValueType,
                    requiresPosition, requiresLength, body, analysis));
            namespaceMapping.put(functionName, functionList);
            return null;
        }
        final List<FunctionSpecification> alreadyRegistered = namespaceMapping.get(functionName);
        final var noOverlapping = alreadyRegistered.stream().noneMatch(f ->
            minArity <= f.maxArity && f.minArity <= maxArity
        );

        if (!noOverlapping) {
            return XQuerySemanticError.FunctionNameArityConflict;
        }
        alreadyRegistered.add(new FunctionSpecification(
            minArity, maxArity, args, returnedType, requiredContextValueType,
            requiresPosition, requiresLength, body, analysis));
        return null;
    }

    public boolean namespaceExists(final String namespace) {
        return namespaces.containsKey(namespace);
    }

    public void provideNamespace(final String namespace) {
        namespaces.putIfAbsent(namespace, new HashMap<>());
    }

    public void registerGrammar(final String namespace, final QualifiedGrammarAnalysisResult result) {
        grammars.putIfAbsent(namespace, result);
    }

    public boolean grammarExists(final String namespace) {
        return grammars.get(namespace) != null;
    }

	public QualifiedGrammarAnalysisResult getGrammar(final String grammar) {
        return grammars.get(grammar);
	}


}
