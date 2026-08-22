package com.github.akruk.antlrquery.semanticanalyzer.semanticfunctioncaller;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import com.github.akruk.antlrquery.semanticanalyzer.DiagnosticWarning;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.eclipse.lsp4j.Location;

import com.github.akruk.antlrquery.AntlrQueryParser.FunctionDeclContext;
import com.github.akruk.antlrquery.AntlrQueryParser.ModuleDeclContext;
import com.github.akruk.antlrquery.AntlrQueryParser.NamedRecordTypeDeclContext;
import com.github.akruk.antlrquery.AntlrQueryParser.QnameContext;
import com.github.akruk.antlrquery.AntlrQueryParser.VarNameContext;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;
import com.github.akruk.antlrquery.inputgrammaranalyzer.InputGrammarAnalyzer.QualifiedGrammarAnalysisResult;
import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver.QualifiedName;
import com.github.akruk.antlrquery.semanticanalyzer.DiagnosticError;
import com.github.akruk.antlrquery.semanticanalyzer.ErrorType;
import com.github.akruk.antlrquery.semanticanalyzer.VisitingSemanticContext;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.AntlrQuerySemanticContext;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.AntlrQuerySemanticContextManager;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.AntlrQuerySemanticScope;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.AntlrQuerySemanticScope.EntypingResult;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.AntlrQuerySemanticScope.VariableInfo;
import com.github.akruk.antlrquery.semanticanalyzer.visitors.AntlrQuerySemanticAnalyzer;
import com.github.akruk.antlrquery.semanticanalyzer.visitors.AntlrQuerySemanticAnalyzer.UnresolvedFunctionSpecification;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.types.TypeInContext;
import com.github.akruk.antlrquery.typesystem.typeoperations.Types;
import com.github.akruk.antlrquery.typesystem.typeoperations.Types.EffectiveBooleanValueType;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;

@DefaultQualifier(NonNull.class)
public class SemanticSymbolManager {

    public record ProtoSemanticSymbolManager(
        AntlrQueryTypeFactory typeFactory,
        AntlrQuerySemanticContextManager contextManager,
        List<List<SimplifiedFunctionSpecification>> functionSets
    ) {
        public SemanticSymbolManager withAnalyzer(AntlrQuerySemanticAnalyzer analyzer) {
            return new SemanticSymbolManager(typeFactory, contextManager, functionSets, analyzer);
        }
    }

    public record ArgumentSpecification(
        String name,
        AntlrQuerySequenceType type,
        @Nullable ParseTree defaultArgument) {}

    public record UsedArg(
        TypeInContext type,
        @Nullable AntlrQueryValue value,
        @Nullable ParseTree tree
        ) {}


    public record FunctionCallAnalysis(
            TypeInContext returnedType,
            List<DiagnosticError> errors,
            List<DiagnosticWarning> warnings
    ){
        public static FunctionCallAnalysis typeOnly(TypeInContext returnedType) {
            return new FunctionCallAnalysis(returnedType, List.of(), List.of());
        }

    }

    @FunctionalInterface
    public interface GrainedFunctionCallAnalysis {
        FunctionCallAnalysis analyze(
            List<UsedArg> args,
            VisitingSemanticContext context,
            ParseTree functionBody,
            AntlrQuerySemanticContext typeContext
        );

    }

    public record FunctionSpecification(
        long minArity,
        long maxArity,
        List<ArgumentSpecification> args,
        AntlrQuerySequenceType returnedType,
        @Nullable AntlrQuerySequenceType requiredContextValueType,
        boolean requiresPosition,
        boolean requiresSize,
        @Nullable ParseTree body,
        SemanticSymbolManager.@Nullable GrainedFunctionCallAnalysis grainedAnalysis)
    { }

    public record SimplifiedFunctionSpecification(
        QualifiedName qname,
        List<ArgumentSpecification> args,
        AntlrQuerySequenceType returnedType,
        @Nullable AntlrQuerySequenceType requiredContextValueType,
        boolean requiresPosition,
        boolean requiresSize,
        @Nullable ParseTree body,
        SemanticSymbolManager.@Nullable GrainedFunctionCallAnalysis grainedAnalysis)
    { }

    public record NamespaceInfo(String name, QnameContext declaration) {}
    public record ModuleInfo(String name, ModuleDeclContext declaration) {}
    public record FunctionInfo(String name, FunctionDeclContext declaration) {}
    public record RecordInfo(String name, NamedRecordTypeDeclContext declaration) {}


    public enum DeclarationStatus {
        OK, COLLISION
    }

    public record DeclarationResult(
        DeclarationStatus status,
        List<ParserRuleContext> collisions
    ) {}

    record SpecAndErrors(@Nullable FunctionSpecification spec, List<DiagnosticError> errors) {
    }

    private final AntlrQueryTypeFactory typeFactory;

    private final AntlrQuerySemanticAnalyzer analyzer;

    private final Map<String, Map<String, List<FunctionSpecification>>> functionNamespaces;

    private final Map<String, QualifiedGrammarAnalysisResult> grammars;
    private final AntlrQuerySemanticContextManager contextManager;
    private final AntlrQuerySequenceType zeroOrMoreItems;

    final Map<QualifiedName, List<UnresolvedFunctionSpecification>> functionDeclarations;

    private SemanticSymbolManager(
        final AntlrQueryTypeFactory typeFactory,
        final AntlrQuerySemanticContextManager contextManager,
        final List<List<SimplifiedFunctionSpecification>> functionSets,
        final AntlrQuerySemanticAnalyzer analyzer)
    {
        this.analyzer = analyzer;
        this.typeFactory = typeFactory;
        this.functionNamespaces = new HashMap<>(10);
        for (final var functionSet : functionSets) {
            for (final var f : functionSet) {
                final var functionName = f.qname();
                uncheckedRegisterFunction(
                    functionName.namespace(),
                    functionName.name(),
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
        this.contextManager = contextManager;
        this.zeroOrMoreItems = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
    }

    public void enterContext() {
        contextManager.enterContext();
    }
    public void enterScope() {
        contextManager.enterScope();
    }
    public void leaveContext() {
        contextManager.leaveContext();
    }
    public void leaveScope() {
        contextManager.leaveScope();
    }
    public AntlrQuerySemanticContext currentContext() {
        return contextManager.currentContext();
    }
    public AntlrQuerySemanticScope currentScope() {
        return contextManager.currentScope();
    }

    public EntypingResult entypeVariable(
        final String variableName,
        final @Nullable VarNameContext locationCtx,
        final @Nullable Location location,
        final TypeInContext assignedType
        )
    {
        return contextManager.entypeVariable(
            variableName,
            locationCtx,
            location,
            assignedType
            );
    }


    public @Nullable VariableInfo getVariable(final String variableName) {
        return contextManager.getVariable(variableName);
    }

    public TypeInContext typeInContext(final AntlrQuerySequenceType type) {
        return contextManager.typeInContext(type);
    }

    public TypeInContext resolveEffectiveBooleanValue(final TypeInContext type) {
        return contextManager.resolveEffectiveBooleanValue(type);
    }

    public TypeInContext resolveEffectiveBooleanValue(final TypeInContext type, final EffectiveBooleanValueType ebvType) {
        return contextManager.resolveEffectiveBooleanValue(type, ebvType);
    }

    public FunctionCallAnalysis call(
        final ParserRuleContext location,
        final QualifiedName qName,
        final List<TypeInContext> positionalArgs,
        final Map<String, TypeInContext> keywordArgs,
        final VisitingSemanticContext context,
        final AntlrQuerySemanticContext typeContext
        )
    {
        final var anyItems = typeContext.currentScope().typeInContext(zeroOrMoreItems);
        final var namespace = qName.namespace();
        final var name = qName.name();
        if (!functionNamespaces.containsKey(qName.namespace())) {
            final DiagnosticError error = DiagnosticError.of(location, ErrorType.FUNCTION__UNKNOWN_NAMESPACE, List.of(qName.namespace()));
            final List<DiagnosticError> errors = List.of(error);
            return new FunctionCallAnalysis(anyItems, errors, List.of());
        }

        final var namespaceFunctions = functionNamespaces.get(qName.namespace());
        final boolean noFunctions = !namespaceFunctions.containsKey(qName.name());
        final List<UnresolvedFunctionSpecification> declarations = functionDeclarations.get(qName);

        if (noFunctions && (declarations == null || declarations.isEmpty())) {
            final DiagnosticError error = DiagnosticError.of(location, ErrorType.FUNCTION__UNKNOWN_FUNCTION, List.of(qName.namespace(), qName.name()));
            return handleUnknownFunction(error, anyItems);
        }


        final var namedFunctions = namespaceFunctions.get(qName.name());
        final int positionalArgsCount = positionalArgs.size();
        final var requiredArity = positionalArgsCount + keywordArgs.size();
        final List<String> mismatchReasons = new ArrayList<>();
        final SpecAndErrors specAndErrors = getFunctionSpecification(
            location,
            qName,
            namedFunctions,
            requiredArity);
        if (specAndErrors.spec == null) {
            return new FunctionCallAnalysis(anyItems, specAndErrors.errors, List.of());
        }
        final var spec = specAndErrors.spec;
        // used positional arguments need to have matching types
        final List<String> reasons = new ArrayList<>();
        final boolean positionalTypeMismatch = tryToMatchPositionalArgs(
            positionalArgs, positionalArgsCount, spec, reasons);

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
        checkIfKeywordNotInPositionalArgs(namespace + ":" + name, keywordArgs, mismatchReasons, reasons, remainingArgNames);

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
            assert defaultArg.defaultArgument() != null;
            final var receivedType = Objects.requireNonNull(defaultArg.defaultArgument().accept(analyzer));
            if (Types.notCoercible(typeFactory, receivedType.type, expectedType))
            {
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
                return new FunctionCallAnalysis(typeContext.typeInContext(spec.returnedType), List.of(), List.of());
            } else {
                final List<UsedArg> args = new ArrayList<>(positionalArgs.size() + keywordArgs.size());
                for (final TypeInContext positional : positionalArgs) {
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

                final FunctionCallAnalysis granularAnalysis = spec.grainedAnalysis.analyze(
                    args, context, location, typeContext);

                return new FunctionCallAnalysis(granularAnalysis.returnedType, granularAnalysis.errors, granularAnalysis.warnings);
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

    public FunctionCallAnalysis getFunctionReference(final ParserRuleContext location,
                                                final QualifiedName qName,
                                                final int arity,
                                                final AntlrQuerySemanticContext context)
    {
        // TODO: Verify logic
        final var namespace = qName.namespace();
        final var functionName = qName.name();
        final var fallback = context.currentScope().typeInContext(typeFactory.anyFunction());
        if (!functionNamespaces.containsKey(namespace)) {
            final DiagnosticError error = DiagnosticError.of(location, ErrorType.FUNCTION__UNKNOWN_NAMESPACE, List.of(namespace));
            return handleUnknownNamespace(error, fallback);
        }
        final var namespaceFunctions = functionNamespaces.get(namespace);
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
            return new FunctionCallAnalysis(fallback, List.of(error), List.of());
        }
        final TypeInContext returnedType = context.typeInContext(specAndErrors.spec.returnedType);
        final List<AntlrQuerySequenceType> argTypes = specAndErrors.spec.args.stream()
            .map(ArgumentSpecification::type)
            .toList()
            .subList(0, arity);
        final var functionItem = typeFactory.function(returnedType.type, argTypes);
        return new FunctionCallAnalysis(context.currentScope().typeInContext(functionItem), specAndErrors.errors, List.of());

    }

    public @Nullable FunctionSpecification getNamedFunctionSpecification(
        final ParserRuleContext location,
        final QualifiedName qName,
        final int arity)
    {
        if (!functionNamespaces.containsKey(qName.namespace())) {
            return null;
        }
        final var namespaceFunctions = functionNamespaces.get(qName.namespace());
        if (!namespaceFunctions.containsKey(qName.name())) {
            return null;
        }

        final var namedFunctions = namespaceFunctions.get(qName.name());
        final SpecAndErrors specAndErrors = getFunctionSpecification(
            location, qName, namedFunctions, arity);
        return specAndErrors.spec;
    }

    public DeclarationResult declareFunction(final UnresolvedFunctionSpecification function)
    {
        final var minArity = function.minArity();
        final var maxArity = function.maxArity();
        final var alreadyDeclared = functionDeclarations.computeIfAbsent(
            function.name(), _ -> new ArrayList<>());
        final var overlapping = alreadyDeclared.stream().filter(f ->
            minArity <= f.maxArity() && f.minArity() <= maxArity
        ).map(UnresolvedFunctionSpecification::location).toList();
        if (overlapping.isEmpty()) {
            return new DeclarationResult(DeclarationStatus.OK, overlapping);
        } else {
            return new DeclarationResult(DeclarationStatus.COLLISION, overlapping);
        }
    }

    public void registerFunction(
            final String namespace,
            final String functionName,
            final List<ArgumentSpecification> args,
            final AntlrQuerySequenceType returnedType) {
        registerFunction(namespace, functionName, args, returnedType, null, false, false, null,
                (_, _, _, ctx)
                        -> new FunctionCallAnalysis(ctx.currentScope().typeInContext(returnedType), List.of(), List.of())
        );
    }

    public void registerFunction(
            final String namespace,
            final String functionName,
            final List<ArgumentSpecification> args,
            final AntlrQuerySequenceType returnedType,
            final @Nullable ParseTree body) {
        registerFunction(
                namespace,
                functionName,
                args,
                returnedType,
                null,
                false,
                false,
                body,
                (final List<UsedArg> arguments, final VisitingSemanticContext _, final ParseTree _, final AntlrQuerySemanticContext typeCtx)
                        -> {
                    assert body != null;
                    return defaultGrainedFunctionAnalysis(args, body, arguments, typeCtx);
                }
        );
    }

    public void registerFunction(
            final String namespace,
            final String functionName,
            final List<ArgumentSpecification> args,
            final AntlrQuerySequenceType returnedType,
            final @Nullable AntlrQuerySequenceType requiredContextValueType,
            final boolean requiresPosition,
            final boolean requiresLength,
            final @Nullable ParseTree body,
            final GrainedFunctionCallAnalysis analysis)
    {
        final long minArity = args.stream()
                .filter(arg -> arg.defaultArgument() == null)
                .count();
        final long maxArity = args.size();
        FunctionSpecification newFunctionSpec = new FunctionSpecification(
                minArity, maxArity, args, returnedType, requiredContextValueType,
                requiresPosition, requiresLength, body, analysis);
        if (!functionNamespaces.containsKey(namespace)) {
            final Map<String, List<FunctionSpecification>> functions = new HashMap<>();
            final List<FunctionSpecification> functionList = new ArrayList<>();
            functionList.add(
                    newFunctionSpec);
            functions.put(functionName, functionList);
            functionNamespaces.put(namespace, functions);
            return;
        }
        final var namespaceMapping = functionNamespaces.get(namespace);
        if (!namespaceMapping.containsKey(functionName)) {
            final List<FunctionSpecification> functionList = new ArrayList<>();
            functionList.add(newFunctionSpec);
            namespaceMapping.put(functionName, functionList);
            return;
        }
        final List<FunctionSpecification> alreadyRegistered = namespaceMapping.get(functionName);
        final var noOverlapping = alreadyRegistered.stream().noneMatch(f ->
            minArity <= f.maxArity && f.minArity <= maxArity
        );

        if (!noOverlapping) {
            return;
        }
        alreadyRegistered.add(newFunctionSpec);
    }


    public boolean namespaceExists(final String namespace) {
        return functionNamespaces.containsKey(namespace);
    }

    public void provideNamespace(final String namespace) {
        functionNamespaces.putIfAbsent(namespace, new HashMap<>());
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
            location, ErrorType.FUNCTION__NO_MATCHING_FUNCTION, List.of(namespace, name, requiredArity, mismatchReasons));
        return new SpecAndErrors(null, List.of(error));
    }

    private FunctionCallAnalysis handleUnknownNamespace(
        final DiagnosticError error,
        final TypeInContext fallbackType
        )
    {
        final List<DiagnosticError> errors = List.of(error);
        return new FunctionCallAnalysis(fallbackType, errors, List.of());
    }

    private FunctionCallAnalysis handleUnknownFunction(final DiagnosticError errorMessageSupplier, final TypeInContext fallbackType)
    {
        final List<DiagnosticError> errors = List.of(errorMessageSupplier);
        return new FunctionCallAnalysis(fallbackType, errors, List.of());
    }

    private FunctionCallAnalysis handleNoMatchingFunction(
        final DiagnosticError errorMessageSupplier,
        final TypeInContext fallbackType
        )
    {
        final List<DiagnosticError> errors = List.of(errorMessageSupplier);
        return new FunctionCallAnalysis(fallbackType, errors, List.of());
    }

    private void checkIfCorrectContext(final FunctionSpecification spec, final VisitingSemanticContext context, final List<String> mismatchReasons)
    {
        if (spec.requiresPosition && context.getPositionType() == null) {
            mismatchReasons.add("Function requires context position");
        }
        if (spec.requiresSize && context.getSizeType() == null) {
            mismatchReasons.add("Function requires context size");
        }
        if (spec.requiredContextValueType != null
            && Types.notCoercible(typeFactory, context.getType().type, spec.requiredContextValueType)
        )
        {
            final String message = getIncorrectContextValueMessage(spec, context);
			mismatchReasons.add(message);
        }
	}


    private String getIncorrectContextValueMessage(final FunctionSpecification spec, final VisitingSemanticContext context) {
        return "Invalid context value: " +
                context.getType().toString() +
                " is not subtype of " +
                spec.requiredContextValueType;
    }

    private boolean checkIfTypesMatchForKeywordArgs(
            final Map<String, TypeInContext> keywordArgs,
            final List<String> reasons,
            final Map<Boolean, List<ArgumentSpecification>> partitioned)
    {
        boolean keywordTypeMismatch = false;
        for (final ArgumentSpecification arg : partitioned.get(true)) {
            final AntlrQuerySequenceType passedType = keywordArgs.get(arg.name()).type;
            if (Types.notCoercible(typeFactory, passedType, arg.type())) {
                reasons.add("Keyword argument '" + arg.name() + "' type mismatch:"
                        + "\n        expected: " + arg.type()
                        + "\n        received: " + passedType);
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
        if (!new HashSet<>(remainingArgNames).containsAll(keywordArgs.keySet())) {
            reasons.add("Keyword argument(s) overlap with positional arguments: " + keywordArgs.keySet().stream()
                    .filter(k -> !remainingArgNames.contains(k)).collect(Collectors.joining(", ")));
            mismatchReasons.add("Function " + name + ": " + String.join("; ", reasons));
        }
    }

    private void checkIfCorrectKeywordNames(
        final String name, final Map<String, TypeInContext> keywordArgs,
        final List<String> mismatchReasons, final List<String> reasons, final List<String> allArgNames)
    {
        if (!new HashSet<>(allArgNames).containsAll(keywordArgs.keySet())) {
            reasons.add("Unknown keyword argument(s): " + keywordArgs.keySet().stream()
                    .filter(k -> !allArgNames.contains(k)).collect(Collectors.joining(", ")));
            mismatchReasons.add("Function " + name + ": " + String.join("; ", reasons));
        }
    }

    private boolean tryToMatchPositionalArgs(
        final List<TypeInContext> positionalArgs,
        final int positionalArgsCount,
        final FunctionSpecification spec,
        final List<String> reasons)
    {
        boolean positionalTypeMismatch = false;
        for (int i = 0; i < positionalArgsCount; i++) {
            final var positionalArg = positionalArgs.get(i);
            final var expectedArg = spec.args.get(i);
            if (Types.notCoercible(typeFactory, positionalArg.type, expectedArg.type)) {
                reasons.add("Positional argument " + (i + 1) + " type mismatch:"
                        + "\n    expected: " + expectedArg.type()
                        + "\n    received: " + positionalArg);
                positionalTypeMismatch = true;
            }
        }
        return positionalTypeMismatch;
    }

    private FunctionCallAnalysis defaultGrainedFunctionAnalysis(final List<ArgumentSpecification> args, final ParseTree body,
            final List<UsedArg> arguments, final AntlrQuerySemanticContext typeCtx) {
        for (int i = 0 ; i < args.size(); i++) {
            final ArgumentSpecification argSpec = args.get(i);
            final UsedArg usedArg = arguments.get(i);
            typeCtx.entypeVariable(argSpec.name, (VarNameContext) usedArg.tree, null, usedArg.type);
        }
        var returnedType = Objects.requireNonNull(body.accept(analyzer));
        return new FunctionCallAnalysis(returnedType, List.of(), List.of());
    }

    private void uncheckedRegisterFunction(
            final String namespace,
            final String functionName,
            final List<ArgumentSpecification> args,
            final AntlrQuerySequenceType returnedType,
            final @Nullable AntlrQuerySequenceType requiredContextValueType,
            final boolean requiresPosition,
            final boolean requiresLength,
            final @Nullable ParseTree body,
            final SemanticSymbolManager.@Nullable GrainedFunctionCallAnalysis analysis)
    {
        final long minArity = args.stream().filter(arg -> arg.defaultArgument() == null).count();
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
        functionNamespaces.computeIfAbsent(namespace, _ ->new HashMap<>())
            .computeIfAbsent(functionName, _ ->new ArrayList<>())
            .add(function);
    }


}
