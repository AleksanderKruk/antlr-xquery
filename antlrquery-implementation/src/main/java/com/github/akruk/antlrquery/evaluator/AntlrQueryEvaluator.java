package com.github.akruk.antlrquery.evaluator;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.github.akruk.antlrquery.*;
import com.github.akruk.antlrquery.evaluator.dynamiccontext.DynamicContextManager;
import com.github.akruk.antlrquery.semanticanalyzer.visitors.TypeVisitor;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import com.github.akruk.antlrquery.charescaper.AntlrQueryCharEscaper;
import com.github.akruk.antlrquery.evaluator.functionmanager.EvaluatingFunctionManager;
import com.github.akruk.antlrquery.evaluator.values.*;
import com.github.akruk.antlrquery.evaluator.values.factories.AntlrQueryValueFactory;
import com.github.akruk.antlrquery.evaluator.values.operations.*;
import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver;
import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver.QualifiedName;
import com.github.akruk.antlrquery.semanticanalyzer.ModuleManager;
import com.github.akruk.antlrquery.semanticanalyzer.visitors.AntlrQuerySemanticAnalyzer;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.typeoperations.Types;
import com.github.akruk.nodegetter.NodeGetter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;


@DefaultQualifier(NonNull.class)
public class AntlrQueryEvaluator extends AntlrQueryParserBaseVisitor<AntlrQueryValue> {
    private final AntlrQueryValue root;
    private final Parser parser;
    private final DynamicContextManager contextManager;
    private final AntlrQueryValueFactory valueFactory;
    private final AntlrQueryTypeFactory typeFactory;
    private final EvaluatingFunctionManager functionManager;

    // Implementations of operators and other evaluation logic
    private final ValueComparisonOperator valueComparisonOperator;
    private final GeneralComparisonOperator generalComparisonOperator;
    private final NodeComparisonOperator nodeComparisonOperator;
    private final ValueBooleanOperator booleanOperator;
    private final NodeOperator nodeOperator;
    private final EffectiveBooleanValue effectiveBooleanValue;
    private final ValueAtomizer atomizer;
    private final Caster caster;
    private final NodeGetter nodeGetter;
    // private final StreamNodeGetter streamNodeGetter;
    private final ModuleManager moduleManager;

    // Functions used in logic
    private final AntlrQueryFunction concat;
    private final AntlrQueryFunction addition;
    private final AntlrQueryFunction subtraction;
    private final AntlrQueryFunction multiplication;
    private final AntlrQueryFunction division;
    private final AntlrQueryFunction integerDivision;
    private final AntlrQueryFunction modulus;
    private final AntlrQueryFunction unaryPlus;
    private final AntlrQueryFunction unaryMinus;
    private final AntlrQueryFunction string;

    private final AntlrQueryValue emptySequence;

    private final AntlrQuerySemanticAnalyzer semanticAnalyzer;
    private final TypeVisitor typeVisitor;
    // private final XQueryTypeFactory typeFactory;

    private @Nullable AntlrQueryAxis currentAxis;
    private @Nullable List<AntlrQueryValue> visitedPositionalArguments;
    private @Nullable AntlrQueryVisitingContext context;
    private @Nullable Stream<List<VariableCoupling>> visitedTupleStream;
    private @Nullable Map<String, AntlrQueryValue> visitedKeywordArguments;

    record VariableCoupling(
            @Nullable Variable item,
            @Nullable Variable key,
            @Nullable Variable value,
            @Nullable Variable position) {
    }

    record Variable(String name, AntlrQueryValue value) {
    }


    public AntlrQueryEvaluator(
            final ParseTree tree,
            final Parser parser,
            final AntlrQueryValueFactory valueFactory,
            final AntlrQuerySemanticAnalyzer analyzer,
            final AntlrQueryTypeFactory typeFactory,
            final ModuleManager moduleManager,
            final Map<String, AntlrQueryValue> externalVariables, TypeVisitor typeVisitor)
    {
        this.semanticAnalyzer = analyzer;
        this.moduleManager = moduleManager;
        this.typeFactory = typeFactory;
        this.typeVisitor = typeVisitor;
        this.root = valueFactory.node("", tree);
        this.context = new AntlrQueryVisitingContext();
        this.context.setValue(root);
        this.context.setPosition(0);
        this.context.setSize(0);
        this.parser = parser;
        this.valueFactory = valueFactory;
        // this.typeFactory = typeFactory;
        this.effectiveBooleanValue = new EffectiveBooleanValue(valueFactory);
        Stringifier stringifier = new Stringifier(valueFactory, effectiveBooleanValue);
        this.valueComparisonOperator = new ValueComparisonOperator(valueFactory);
        this.atomizer = new ValueAtomizer();
        this.nodeGetter = new NodeGetter();
        // this.streamNodeGetter = new StreamNodeGetter();
        this.generalComparisonOperator = new GeneralComparisonOperator(valueFactory, atomizer, valueComparisonOperator);
        this.nodeComparisonOperator = new NodeComparisonOperator(valueFactory, root.node);
        this.booleanOperator = new ValueBooleanOperator(this, valueFactory, effectiveBooleanValue);
        this.nodeOperator = new NodeOperator(valueFactory);
        this.functionManager = new EvaluatingFunctionManager(this, parser, valueFactory, nodeGetter, typeFactory,
            effectiveBooleanValue, atomizer, stringifier, valueComparisonOperator);
        this.contextManager = new DynamicContextManager();
        this.concat = functionManager.getFunctionReference("fn", "concat", 2).functionValue;
        this.string = functionManager.getFunctionReference("fn", "string", 1).functionValue;
        this.addition = functionManager.getFunctionReference("op", "numeric-add", 2).functionValue;
        this.subtraction = functionManager.getFunctionReference("op", "numeric-subtract", 2).functionValue;
        this.multiplication = functionManager.getFunctionReference("op", "numeric-multiply", 2).functionValue;
        this.division = functionManager.getFunctionReference("op", "numeric-divide", 2).functionValue;
        this.integerDivision = functionManager.getFunctionReference("op", "numeric-integer-divide", 2).functionValue;
        this.modulus = functionManager.getFunctionReference("op", "numeric-mod", 2).functionValue;
        this.unaryPlus = functionManager.getFunctionReference("op", "numeric-unary-plus", 1).functionValue;
        this.unaryMinus = functionManager.getFunctionReference("op", "numeric-unary-minus", 1).functionValue;
        this.emptySequence = valueFactory.emptySequence();
        this.caster = new Caster(typeFactory, valueFactory, stringifier, effectiveBooleanValue);
        contextManager.enterContext();
    }

    @Override
    public AntlrQueryValue visitXquery(final AntlrQueryParser.XqueryContext ctx)
    {
        if (ctx.libraryModule() != null)
            return visitLibraryModule(ctx.libraryModule());
        return visitMainModule(ctx.mainModule());
    }

    @Override
    public AntlrQueryValue visitFLWORExpr(final AntlrQueryParser.FLWORExprContext ctx)
    {
        final var savedTupleStream = saveVisitedTupleStream();
        contextManager.enterScope();
        // visitedTupleStream will be manipulated to prepare result stream
        visitInitialClause(ctx.initialClause());
        for (final var clause : ctx.intermediateClause()) {
            clause.accept(this);
        }
        // at this point visitedTupleStream should contain all tuples
        final var expressionValue = visitReturnClause(ctx.returnClause());
        final var atomized = atomizer.atomize(expressionValue);
        contextManager.leaveScope();
        visitedTupleStream = savedTupleStream;
        return valueFactory.sequence(atomized);
    }

    @Override
    public AntlrQueryValue visitLetClause(final AntlrQueryParser.LetClauseContext ctx)
    {
        final int newVariableCount = ctx.letBinding().size();
        visitedTupleStream = visitedTupleStream.map(tuple -> {
            final var newTuple = new ArrayList<VariableCoupling>(tuple.size() + newVariableCount);
            newTuple.addAll(tuple);
            for (final AntlrQueryParser.LetBindingContext streamVariable : ctx.letBinding()) {
                final String variableName = streamVariable.varNameAndType().varName().qname().getText();
                final AntlrQueryValue assignedValue = streamVariable.exprSingle().accept(this);
                final var element = new VariableCoupling(new Variable(variableName, assignedValue), null, null, null);
                newTuple.add(element);
                contextManager.provideVariable(variableName, assignedValue);
            }
            return newTuple;
        });
        return null;
    }

    @Override
    public @Nullable AntlrQueryValue visitOrderByClause(final AntlrQueryParser.OrderByClauseContext ctx)
    {
        final int sortingExprCount = ctx.orderSpecList().orderSpec().size();
        final var orderSpecs = ctx.orderSpecList().orderSpec();
        final int[] modifierMaskArray = orderSpecs.stream()
            .map(com.github.akruk.antlrquery.AntlrQueryParser.OrderSpecContext::orderModifier)
            .mapToInt(m -> {
                final int isDescending = m.DESCENDING() != null ? 1 : 0;
                final int isEmptyLeast = m.LEAST() != null ? 1 : 0;
                return (isDescending << 1) | isEmptyLeast;
            })
            .toArray();
        assert visitedTupleStream != null;
        visitedTupleStream = visitedTupleStream.sorted((tuple1, tuple2) -> {
            var comparator = comparatorFromNthOrderSpec(orderSpecs, modifierMaskArray, 0);
            for (int i = 1; i < sortingExprCount; i++) {
                final var nextComparator = comparatorFromNthOrderSpec(orderSpecs, modifierMaskArray, i);
                comparator = comparator.thenComparing(nextComparator);
            }
            return comparator.compare(tuple1, tuple2);
        }).peek(this::provideVariables);
        return null;
    }

    @Override
    public AntlrQueryValue visitNamedRecordTypeDecl(AntlrQueryParser.NamedRecordTypeDeclContext ctx)
    {
        final var qName = namespaceResolver.resolveType(ctx.qname().getText());
        final var defaultArgs = new HashMap<String, ParseTree>();
        final var mandatoryArgs = new ArrayList<String>();
        final var optionalArgs = new ArrayList<String>();

        for (final AntlrQueryParser.ExtendedFieldDeclarationContext field : ctx.extendedFieldDeclaration()) {
            final var fieldName = field.fieldDeclaration().fieldName().getText();
            final boolean isRequired = field.fieldDeclaration().QUESTION_MARK() == null;
            final AntlrQueryParser.ExprSingleContext defaultExpr = field.exprSingle();
            if (isRequired) {
                if (defaultExpr == null) {
                    mandatoryArgs.add(fieldName);
                } else {
                    optionalArgs.add(fieldName);
                    defaultArgs.put(fieldName, defaultExpr);
                }
            } else {
                optionalArgs.add(fieldName);
                defaultArgs.put(fieldName, new HelperTrees().EMPTY_SEQUENCE);
            }
        }
        final var argNames = new ArrayList<String>();
        argNames.addAll(mandatoryArgs);
        argNames.addAll(optionalArgs);
        functionManager.registerFunction(qName.namespace(), qName.name(), constructorFunction(argNames), argNames, defaultArgs);
        return null;
    }


    @Override
    public @Nullable AntlrQueryValue visitForClause(final AntlrQueryParser.ForClauseContext ctx)
    {
        final int numberOfVariables = ctx.forBinding().size();
        visitedTupleStream = visitedTupleStream.flatMap(tuple -> {
            final List<List<VariableCoupling>> newTupleLike = tuple.stream().map(List::of)
                .collect(Collectors.toList());

            for (final AntlrQueryParser.ForBindingContext forBinding : ctx.forBinding()) {
                final List<VariableCoupling> tupleElements = processForBinding(forBinding);
                newTupleLike.add(tupleElements);
            }

            return cartesianProduct(newTupleLike);
        }).peek(tuple -> {
            final List<VariableCoupling> addedVariables = tuple.subList(tuple.size() - numberOfVariables, tuple.size());
            provideVariables(addedVariables);
        });
        return null;
    }

    private List<VariableCoupling> processForBinding(final AntlrQueryParser.ForBindingContext forBinding)
    {
        if (forBinding.forItemBinding() != null) {
            return processForItemBinding(forBinding.forItemBinding());
        }
        if (forBinding.forMemberBinding() != null) {
            return processForMemberBinding(forBinding.forMemberBinding());
        }
        if (forBinding.forEntryBinding() != null) {
            return processForEntryBinding(forBinding.forEntryBinding());
        }
        throw new IllegalStateException("Unknown for binding type");
    }

    private List<VariableCoupling> processForItemBinding(final AntlrQueryParser.ForItemBindingContext ctx)
    {
        final String variableName = ctx.varNameAndType().varName().qname().getText();
        final List<AntlrQueryValue> sequence = visitExprSingle(ctx.exprSingle()).sequence;
        final AntlrQueryParser.PositionalVarContext positional = ctx.positionalVar();
        final boolean allowingEmpty = ctx.allowingEmpty() != null;
        String positionalName = null;
        Variable positionalVar = null;
        if (positional != null) {
            positionalName = positional.varName().qname().getText();
            positionalVar = new Variable(positionalName, valueFactory.number(0));
        }

        if (sequence.isEmpty() && allowingEmpty) {
            final var emptyVar = new Variable(variableName, emptySequence);
            final var element = new VariableCoupling(emptyVar, null, null, positionalVar);
            return List.of(element);
        }

        if (positional != null) {
            final List<VariableCoupling> elementsWithIndex = new ArrayList<>();
            for (int i = 0; i < sequence.size(); i++) {
                final AntlrQueryValue value = sequence.get(i);
                final VariableCoupling element = new VariableCoupling(new Variable(variableName, value),
                    null,
                    null,
                    new Variable(positionalName, valueFactory.number(i + 1)));
                elementsWithIndex.add(element);
            }
            return elementsWithIndex;
        }

        return sequence.stream()
            .map(value -> new VariableCoupling(new Variable(variableName, value), null, null, null))
            .toList();
    }

    private List<VariableCoupling> processForMemberBinding(final AntlrQueryParser.ForMemberBindingContext ctx)
    {
        final String variableName = ctx.varNameAndType().varName().qname().getText();
        final AntlrQueryValue arrayValue = visitExprSingle(ctx.exprSingle());
        final AntlrQueryParser.PositionalVarContext positional = ctx.positionalVar();

        final List<AntlrQueryValue> arrayMembers = arrayValue.arrayMembers;

        if (positional != null) {
            final String positionalName = positional.varName().qname().getText();
            final List<VariableCoupling> elementsWithIndex = new ArrayList<>();
            for (int i = 0; i < arrayMembers.size(); i++) {
                final AntlrQueryValue member = arrayMembers.get(i);
                final VariableCoupling element = new VariableCoupling(new Variable(variableName, member),
                    null,
                    null,
                    new Variable(positionalName, valueFactory.number(i + 1)));
                elementsWithIndex.add(element);
            }
            return elementsWithIndex;
        }

        return arrayMembers.stream()
            .map(value -> new VariableCoupling(new Variable(variableName, value), null, null, null))
            .toList();
    }

    private List<VariableCoupling> processForEntryBinding(final AntlrQueryParser.ForEntryBindingContext ctx)
    {
        final AntlrQueryValue mapValue = visitExprSingle(ctx.exprSingle());
        final AntlrQueryParser.PositionalVarContext positional = ctx.positionalVar();

        final AntlrQueryParser.ForEntryKeyBindingContext keyBinding = ctx.forEntryKeyBinding();
        final AntlrQueryParser.ForEntryValueBindingContext valueBinding = ctx.forEntryValueBinding();

        final Map<AntlrQueryValue, AntlrQueryValue> mapEntries = mapValue.mapEntries;
        final List<VariableCoupling> tupleElements = new ArrayList<>();

        int index = 1;
        for (final Map.Entry<AntlrQueryValue, AntlrQueryValue> entry : mapEntries.entrySet()) {
            @Nullable Variable positionVar = null;
            if (positional != null) {
                final String positionalName = positional.varName().qname().getText();
                final AntlrQueryValue position = valueFactory.number(index);
                positionVar = new Variable(positionalName, position);
            }

            @Nullable Variable keyVar = null;
            if (keyBinding != null) {
                final String keyName = keyBinding.varNameAndType().varName().qname().getText();
                final AntlrQueryValue keyValue = entry.getKey();
                keyVar = new Variable(keyName, keyValue);
            }

            @Nullable Variable valueVar = null;
            if (valueBinding != null) {
                final String valueName = valueBinding.varNameAndType().varName().qname().getText();
                final AntlrQueryValue valueValue = entry.getValue();
                valueVar = new Variable(valueName, valueValue);
            }

            tupleElements.add(new VariableCoupling(null, keyVar, valueVar, positionVar));
            index++;
        }

        return tupleElements;
    }

    private static class MutableInt {
        public int i = 0;
    }

    @Override
    public AntlrQueryValue visitCountClause(final AntlrQueryParser.CountClauseContext ctx)
    {
        final String countVariableName = ctx.varName().qname().getText();
        final MutableInt index = new MutableInt();
        index.i = 1;
        assert visitedTupleStream != null;
        visitedTupleStream = visitedTupleStream.map(tuple -> {
            final var newTuple = new ArrayList<VariableCoupling>(tuple.size() + 1);
            newTuple.addAll(tuple);
            final var element = new VariableCoupling(new Variable(countVariableName, valueFactory.number(index.i++)),
                null, null, null);
            assert element.item != null;
            contextManager.provideVariable(element.item.name, element.item.value);
            newTuple.add(element);
            return newTuple;
        });
        return null;
    }

    @Override
    public AntlrQueryValue visitWhereClause(final AntlrQueryParser.WhereClauseContext ctx)
    {
        final var filteringExpression = ctx.exprSingle();
        visitedTupleStream = visitedTupleStream.filter(_ -> {
            final AntlrQueryValue filter = filteringExpression.accept(this);
            return effectiveBooleanValue.effectiveBooleanValue(filter).booleanValue;
        });
        return null;
    }

    @Override
    public AntlrQueryValue visitReturnClause(final AntlrQueryParser.ReturnClauseContext ctx)
    {
        final List<AntlrQueryValue> results = visitedTupleStream.map((tupleStream) -> {
            provideVariables(tupleStream);
            return visitExprSingle(ctx.exprSingle());
        }).toList();
        if (results.size() == 1) {
            return results.getFirst();
        }
        return valueFactory.sequence(results);
    }

    @Override
    public AntlrQueryValue visitWhileClause(final AntlrQueryParser.WhileClauseContext ctx)
    {
        final var filteringExpression = ctx.exprSingle();
        visitedTupleStream = visitedTupleStream.takeWhile(_ -> {
            final AntlrQueryValue filter = filteringExpression.accept(this);
            return effectiveBooleanValue.effectiveBooleanValue(filter).booleanValue;
        });
        return null;
    }

    @Override
    public AntlrQueryValue visitVarRef(final AntlrQueryParser.VarRefContext ctx)
    {
        final String variableName = ctx.qname().getText();
        return contextManager.getVariable(variableName);
    }

    @Override
    public AntlrQueryValue visitIntegerLiteral(AntlrQueryParser.IntegerLiteralContext ctx) {
        return handleInteger(ctx.IntegerLiteral());
    }

    @Override
    public AntlrQueryValue visitHexIntegerLiteral(AntlrQueryParser.HexIntegerLiteralContext ctx) {
        final String raw = ctx.HexIntegerLiteral().getText();
        final String hex = raw.replace("_", "").substring(2);
        return valueFactory.number(new BigDecimal(new java.math.BigInteger(hex, 16)));
    }

    @Override
    public AntlrQueryValue visitBinaryIntegerLiteral(AntlrQueryParser.BinaryIntegerLiteralContext ctx) {
        final String raw = ctx.BinaryIntegerLiteral().getText();
        final String binary = raw.replace("_", "").substring(2);
        return valueFactory.number(new BigDecimal(new java.math.BigInteger(binary, 2)));
    }

    @Override
    public AntlrQueryValue visitDecimalLiteral(AntlrQueryParser.DecimalLiteralContext ctx) {
        final String cleaned = ctx.DecimalLiteral().getText().replace("_", "");
        return valueFactory.number(new BigDecimal(cleaned));
    }

    @Override
    public AntlrQueryValue visitDoubleLiteral(AntlrQueryParser.DoubleLiteralContext ctx) {
        final String cleaned = ctx.DoubleLiteral().getText().replace("_", "");
        return valueFactory.number(new BigDecimal(cleaned));
    }

    @Override
    public AntlrQueryValue visitLiteral(final AntlrQueryParser.LiteralContext ctx)
    {
        if (ctx.STRING() != null) {
            return handleString(ctx);
        }
        return ctx.numericLiteral().accept(this);
    }

    private AntlrQueryValue handleInteger(final TerminalNode integerLiteral)
    {
        final String value = integerLiteral.getText().replace("_", "");
        return valueFactory.number(new BigDecimal(value));
    }

    @Override
    public AntlrQueryValue visitParenthesizedExpr(final AntlrQueryParser.ParenthesizedExprContext ctx)
    {
        // Empty parentheses mean an empty sequence '()'
        if (ctx.expr() == null) {
            return valueFactory.sequence(List.of());
        }
        return visitExpr(ctx.expr());
    }

    @Override
    public AntlrQueryValue visitExpr(final AntlrQueryParser.ExprContext ctx)
    {
        // Only one expression
        // e.g. 13
        if (ctx.exprSingle().size() == 1) {
            return ctx.exprSingle(0).accept(this);
        }
        // More than one expression
        // are turned into a flattened list
        final List<AntlrQueryValue> result = new ArrayList<>();
        for (final var exprSingle : ctx.exprSingle()) {
            final var expressionValue = exprSingle.accept(this);
            if (expressionValue.isError)
                return expressionValue;
            if (expressionValue.size == 1) {
                result.add(expressionValue.sequence.getFirst());
                continue;
            }
            // If the result is not atomic we atomize it
            // and extend the result list
            final var atomizedValues = atomizer.atomize(expressionValue);
            result.addAll(atomizedValues);
        }
        return valueFactory.sequence(result);
    }

    private String unescapeString(final String str)
    {
        final var charEscaper = new AntlrQueryCharEscaper();
        return charEscaper.escapeChars(str);
    }

    @Override
    public AntlrQueryValue visitFunctionCall(final AntlrQueryParser.FunctionCallContext ctx)
    {
        final List<AntlrQueryValue> savedArgs = saveVisitedArguments();
        final Map<String, AntlrQueryValue> savedKwargs = saveVisitedKeywordArguments();
        ctx.argumentList().accept(this);
        final String functionQname = ctx.functionName().getText();
        final AntlrQueryValue callResult = callFunction(functionQname, visitedPositionalArguments, visitedKeywordArguments);
        visitedPositionalArguments = savedArgs;
        visitedKeywordArguments = savedKwargs;
        return callResult;
    }

    private AntlrQueryValue callFunction(
        final String qname,
        final List<AntlrQueryValue> args,
        final Map<String, AntlrQueryValue> kwargs)
    {
        final String[] parts = resolveNamespace(qname);
        final String namespace = parts.length == 2 ? parts[0] : "fn";
        final String functionName = parts.length == 2 ? parts[1] : parts[0];
        return functionManager.call(namespace, functionName, context, args, kwargs);
    }

    private Map<String, AntlrQueryValue> saveVisitedKeywordArguments()
    {
        final var saved = visitedKeywordArguments;
        visitedKeywordArguments = new HashMap<>();
        return saved;
    }

    public static <T> Stream<List<T>> cartesianProduct(final List<List<T>> lists)
    {
        if (lists.isEmpty()) {
            return Stream.of(List.of());
        }

        final int size = lists.size();
        return lists.getFirst().stream()
            .flatMap(firstElement -> cartesianProduct(lists.subList(1, size))
                .map(rest -> {
                    final List<T> combination = new ArrayList<>(size);
                    combination.add(firstElement);
                    combination.addAll(rest);
                    return combination;
                }));
    }

    @Override
    public AntlrQueryValue visitQuantifiedExpr(final AntlrQueryParser.QuantifiedExprContext ctx)
    {
        final List<AntlrQueryParser.QuantifierBindingContext> quantifierBindings = ctx.quantifierBinding();

        final List<String> variableNames = quantifierBindings.stream()
            .map(binding -> binding.varNameAndType().varName().qname().getText())
            .toList();

        final List<List<AntlrQueryValue>> sequences = quantifierBindings.stream()
            .map(binding -> binding.exprSingle().accept(this).sequence)
            .toList();

        final AntlrQueryParser.ExprSingleContext criterionNode = ctx.exprSingle();

        if (ctx.EVERY() != null) {
            final boolean every = cartesianProduct(sequences).allMatch(variableProduct -> {
                for (int i = 0; i < variableNames.size(); i++) {
                    contextManager.provideVariable(variableNames.get(i), variableProduct.get(i));
                }
                return effectiveBooleanValue.effectiveBooleanValue(criterionNode.accept(this)).booleanValue;
            });
            return valueFactory.bool(every);
        }

        if (ctx.SOME() != null) {
            final boolean some = cartesianProduct(sequences).anyMatch(variableProduct -> {
                for (int i = 0; i < variableNames.size(); i++) {
                    contextManager.provideVariable(variableNames.get(i), variableProduct.get(i));
                }
                final AntlrQueryValue accept = criterionNode.accept(this);
                return effectiveBooleanValue.effectiveBooleanValue(accept).booleanValue;
            });
            return valueFactory.bool(some);
        }

        return null;
    }

    private AntlrQueryValue handleNodeComp(final AntlrQueryParser.ComparisonExprContext ctx)
    {
        final var visitedLeft = ctx.otherwiseExpr(0).accept(this);
        final var visitedRight = ctx.otherwiseExpr(1).accept(this);
        return switch (ctx.nodeComp().getText()) {
            case "is" -> nodeComparisonOperator.is(visitedLeft, visitedRight);
            case "is-not" -> nodeComparisonOperator.isNot(visitedLeft, visitedRight);
            case "precedes", "<<" -> nodeComparisonOperator.precedes(visitedLeft, visitedRight);
            case "precedes-or-is" -> nodeComparisonOperator.precedesOrIs(visitedLeft, visitedRight);
            case "follows", ">>" -> nodeComparisonOperator.follows(visitedLeft, visitedRight);
            case "follows-or-is" -> nodeComparisonOperator.followsOrIs(visitedLeft, visitedRight);
            default -> throw new IllegalStateException("unhandled node comparison operator");
        };
    }

    @Override
    public AntlrQueryValue visitEnclosedExpr(final AntlrQueryParser.EnclosedExprContext ctx)
    {
        if (ctx.expr() == null)
            return emptySequence;
        return visitExpr(ctx.expr());
    }

    @Override
    public AntlrQueryValue visitRangeExpr(final AntlrQueryParser.RangeExprContext ctx)
    {
        final var fromValue = ctx.additiveExpr(0).accept(this);
        if (ctx.TO() == null)
            return fromValue;
        final var toValue = ctx.additiveExpr(1).accept(this);
        if (toValue.isEmptySequence)
            return emptySequence;
        if (fromValue.isEmptySequence)
            return emptySequence;
        final int fromInt = fromValue.numericValue.intValue();
        final int toInt = toValue.numericValue.intValue();
        if (fromInt > toInt)
            return emptySequence;
        final List<AntlrQueryValue> values = IntStream.rangeClosed(fromInt, toInt)
            .mapToObj(valueFactory::number)
            .collect(Collectors.toList());
        return valueFactory.sequence(values);
    }

    @Override
    public AntlrQueryValue visitPathExpr(final AntlrQueryParser.PathExprContext ctx)
    {
        final boolean pathExpressionFromRoot = ctx.SLASH() != null;
        if (pathExpressionFromRoot) {
            final var savedContext = saveContext();
            final var savedAxis = saveAxis();
            context.setValue(root); // TODO: use root of context value
            context.setPosition(1);
            context.setSize(1);
            currentAxis = AntlrQueryAxis.CHILD;
            final var resultingNodeSequence = visitRelativePathExpr(ctx.relativePathExpr());
            context = savedContext;
            currentAxis = savedAxis;
            return resultingNodeSequence;
        }
        final boolean useDescendantOrSelfAxis = ctx.SLASHES() != null;
        if (useDescendantOrSelfAxis) {
            final var savedContext = saveContext();
            final var savedAxis = saveAxis();
            context.setValue(root); // TODO: use root of context value
            // var x = nodeGetter.getAllAncestors(valueToNodeList(savedContext.getValue())).get(1);
            context.setPosition(1);
            context.setSize(1);
            currentAxis = AntlrQueryAxis.DESCENDANT_OR_SELF;
            final var resultingNodeSequence = visitRelativePathExpr(ctx.relativePathExpr());
            context = savedContext;
            currentAxis = savedAxis;
            return resultingNodeSequence;
        }
        return visitRelativePathExpr(ctx.relativePathExpr());
    }

    @Override
    public AntlrQueryValue visitRelativePathExpr(final AntlrQueryParser.RelativePathExprContext ctx)
    {
        if (ctx.pathOperator().isEmpty()) {
            return ctx.stepExpr(0).accept(this);
        }
        var savedContext = saveContext();
        AntlrQueryValue visitedNodeSequence = ctx.stepExpr(0).accept(this);
        context.setValue(visitedNodeSequence);
        final var operationCount = ctx.pathOperator().size();
        for (int i = 1; i <= operationCount; i++) {
            visitedNodeSequence = switch (ctx.pathOperator(i - 1).getText()) {
                case "//" -> {
                    final List<ParseTree> descendantsOrSelf = nodeGetter.getAllDescendantsOrSelf(valueToNodeList(visitedNodeSequence));
                    final AntlrQueryValue descendantsOrSelfAsNodes = nodeSequence(descendantsOrSelf);
                    context.setValue(descendantsOrSelfAsNodes);
                    yield ctx.stepExpr(i).accept(this);
                }
                case "/" -> ctx.stepExpr(i).accept(this);
                default -> null;
            };
            context.setValue(visitedNodeSequence);
        }
        context = savedContext;
        return visitedNodeSequence;
    }

    @Override
    public AntlrQueryValue visitPostfixPrimary(AntlrQueryParser.PostfixPrimaryContext ctx) {

        final AntlrQueryValue sequenceValue = context.getValue();
        if (sequenceValue.isEmptySequence) {
            return sequenceValue;
        }
        final AntlrQueryVisitingContext saved = saveContext();
        final int iteratedsize = sequenceValue.size;
        if (sequenceValue.size == 1) {
            context.setValue(sequenceValue);
            var value = super.visitPostfixPrimary(ctx);
            context = saved;
            return value;
        }
        final List<AntlrQueryValue> values = new ArrayList<>(iteratedsize);
        context.setSize(iteratedsize);
        for (int i = 0; i < iteratedsize; i++) {
            final AntlrQueryValue item = sequenceValue.sequence.get(i);
            context.setValue(item);
            context.setPosition(i+1);
            final AntlrQueryValue value = super.visitPostfixPrimary(ctx);
            values.add(value);
        }
        context = saved;
        return valueFactory.sequence(values);
    }




    private AntlrQueryValue nodeSequence(final List<ParseTree> treenodes)
    {
        final List<AntlrQueryValue> nodeSequence = treenodes.stream()
            .distinct()
            .map(n -> valueFactory.node("", n))
            .collect(Collectors.toList());
        return valueFactory.sequence(nodeSequence);
    }

    private static List<ParseTree> valueToNodeList(AntlrQueryValue matchedNodes)
    {
        return matchedNodes.sequence.stream().map(v -> v.node).distinct().toList();
    }

    @Override
    public AntlrQueryValue visitStepExpr(final AntlrQueryParser.StepExprContext ctx)
    {
        if (ctx.postfixExpr() != null)
            return ctx.postfixExpr().accept(this);
        return visitAxisStep(ctx.axisStep());
    }

    @Override
    public AntlrQueryValue visitAxisStep(final AntlrQueryParser.AxisStepContext ctx)
    {
        AntlrQueryValue stepResult = null;
        if (ctx.reverseStep() != null)
            stepResult = visitReverseStep(ctx.reverseStep());
        else if (ctx.forwardStep() != null)
            stepResult = visitForwardStep(ctx.forwardStep());
        if (ctx.predicateList().predicate().isEmpty()) {
            return stepResult;
        }
        final var savedContext = saveContext();
        final var savedArgs = saveVisitedArguments();
        int index = 1;
        context.setSize(stepResult.sequence.size());
        for (final var predicate : ctx.predicateList().predicate()) {
            context.setValue(stepResult);
            context.setPosition(index);
            stepResult = predicate.accept(this);
            index++;
        }
        context = savedContext;
        visitedPositionalArguments = savedArgs;
        return stepResult;
    }

    @Override
    public AntlrQueryValue visitFilterExpr(final AntlrQueryParser.FilterExprContext ctx)
    {
        final var savedContext = saveContext();
        final var savedArgs = saveVisitedArguments();
        final var value = ctx.postfixExpr().accept(this);
        context.setValue(value);
        final var filtered = visitPredicate(ctx.predicate());
        context = savedContext;
        visitedPositionalArguments = savedArgs;
        return filtered;
    }

    @Override
    public AntlrQueryValue visitPredicate(final AntlrQueryParser.PredicateContext ctx)
    {
        final var contextValue = context.getValue();
        final var filteredSequence = atomizer.atomize(contextValue);
        final var filteredValues = new ArrayList<AntlrQueryValue>(filteredSequence.size());
        final var savedContext = saveContext();
        int index = 1;
        context.setSize(filteredSequence.size());
        for (final var contextItem : filteredSequence) {
            context.setValue(contextItem);
            context.setPosition(index);
            final AntlrQueryValue visitedExpression = visitExpr(ctx.expr());
            if (visitedExpression.sequence.stream().allMatch(v -> v.isNumeric)) {
                final AntlrQueryValue items = handleAsItemGetter(filteredSequence, visitedExpression);
                context = savedContext;
                return items;
            }

            if (effectiveBooleanValue.effectiveBooleanValue(visitedExpression).booleanValue) {
                filteredValues.add(contextItem);
            }
            index++;
        }
        context = savedContext;
        return valueFactory.sequence(filteredValues);
    }

    @Override
    public AntlrQueryValue visitArrowTarget(final AntlrQueryParser.ArrowTargetContext ctx)
    {
        if (ctx.functionCall() != null) {
            ctx.functionCall().argumentList().accept(this);
            final String functionQname = ctx.functionCall().functionName().getText();
            return callFunction(functionQname, visitedPositionalArguments, visitedKeywordArguments);
        }
        return ctx.restrictedDynamicCall().accept(this);
    }

    @Override
    public AntlrQueryValue visitVarDecl(final AntlrQueryParser.VarDeclContext ctx)
    {
        if (ctx.EXTERNAL() != null)
            return null;
        final var name = ctx.varNameAndType().varName().qname().getText();
        final var value = visitVarValue(ctx.varValue());
        contextManager.provideVariable(name, value);
        return null;
    }

    @Override
    public AntlrQueryValue visitDynamicFunctionCall(final AntlrQueryParser.DynamicFunctionCallContext ctx)
    {
        // TODO: verify logic
        final var contextItem = context.getValue();
        final var function = contextItem.functionValue;
        return function.call(context, visitedPositionalArguments);
    }

    @Override
    public AntlrQueryValue visitFunctionDecl(final AntlrQueryParser.FunctionDeclContext ctx)
    {
        if (ctx.EXTERNAL() != null) {
            return null;
        }
        final String qname = ctx.qname().getText();
        final QualifiedName resolved = namespaceResolver.resolveFunction(qname);
        final var argNames = new ArrayList<String>();
        final Map<String, ParseTree> defaults = new HashMap<>();
        contextManager.enterScope();
        if (ctx.paramListWithDefaults() != null) {
            final var params = ctx.paramListWithDefaults().paramWithDefault();
            for (final AntlrQueryParser.ParamWithDefaultContext param : params) {
                final var argName = param.varNameAndType().varName().qname().anyName(0).getText();
                final var defaultValue = param.exprSingle();
                if (defaultValue != null) {
                    defaults.put(argName, defaultValue);
                }
                argNames.add(argName);
            }
        }
        final var body = ctx.functionBody().enclosedExpr();
        functionManager.registerFunction(
            resolved.namespace(), resolved.name(),
            standardQueryFunction(argNames, body),
            argNames, defaults);

        contextManager.leaveScope();
        return null;
    }

    private AntlrQueryFunction standardQueryFunction(final ArrayList<String> argNames, final AntlrQueryParser.EnclosedExprContext body)
    {
        return (context, positionalArguments) -> {
            final var saved = saveContext();
            contextManager.enterContext();
            this.context = context;
            for (int i = 0; i < positionalArguments.size(); i++) {
                final var arg = positionalArguments.get(i);
                final var argname = argNames.get(i);
                contextManager.provideVariable(argname, arg);
            }
            final var result = visitEnclosedExpr(body);
            contextManager.leaveContext();
            context = saved;
            return result;
        };
    }

    private AntlrQueryFunction constructorFunction(final ArrayList<String> argNames)
    {
        return (context, positionalArguments) -> {
            var map = new HashMap<AntlrQueryValue, AntlrQueryValue>();
            for (int i = 0; i < positionalArguments.size(); i++) {
                final var arg = positionalArguments.get(i);
                final var argname = valueFactory.string(argNames.get(i));
                map.put(arg, argname);
            }
            return valueFactory.map(map);
        };
    }

    AntlrQueryValue handleAsItemGetter(final List<AntlrQueryValue> sequence,
                                       final AntlrQueryValue getter)
    {
        if (getter.isEmptySequence)
            return emptySequence;
        if (getter.size == 1) {
            final int i = getter.numericValue.intValue() - 1;
            if (i >= sequence.size() || i < 0) {
                return emptySequence;
            }
            return sequence.get(i);
        }
        final List<AntlrQueryValue> items = new ArrayList<>();
        for (final var sequenceIndex : getter.sequence) {
            final int i = sequenceIndex.numericValue.intValue() - 1;
            if (i >= sequence.size() || i < 0) {
                continue;
            }
            items.add(sequence.get(i));
        }
        return valueFactory.sequence(items);
    }

    @Override
    public AntlrQueryValue visitContextValueRef(final AntlrQueryParser.ContextValueRefContext ctx)
    {
        return context.getValue();
    }

    final AxisVisitor axisVisitor = new AxisVisitor();

    @Override
    public AntlrQueryValue visitForwardStep(final AntlrQueryParser.ForwardStepContext ctx)
    {
        if (ctx.forwardAxis() != null) {
            currentAxis = axisVisitor.visit(ctx.forwardAxis());
        } else {
            // the first slash will work
            // because of the fake root
            // '/*' will return the real root
            if (currentAxis == null) {
                currentAxis = AntlrQueryAxis.CHILD;
            }
        }
        return visitNodeTest(ctx.nodeTest());
    }

    @Override
    public AntlrQueryValue visitReverseStep(final AntlrQueryParser.ReverseStepContext ctx)
    {
        if (ctx.abbrevReverseStep() != null) {
            return visitAbbrevReverseStep(ctx.abbrevReverseStep());
        }
        currentAxis = axisVisitor.visit(ctx.reverseAxis());
        return visitNodeTest(ctx.nodeTest());
    }

    @Override
    public AntlrQueryValue visitAbbrevReverseStep(final AntlrQueryParser.AbbrevReverseStepContext ctx)
    {
        final var matchedParents = nodeGetter.getAllParents(valueToNodeList(context.getValue()));
        return nodeSequence(matchedParents);
    }

    @Override
    public AntlrQueryValue visitNodeTest(final AntlrQueryParser.NodeTestContext ctx)
    {
        var matchedTreeNodes = valueToNodeList(context.getValue());
        final Function<NodeGetter, Function<List<ParseTree>, List<ParseTree>>> axisFunctionSelector
            = AXIS_DISPATCH_TABLE[currentAxis.ordinal()];
        final Function<List<ParseTree>, List<ParseTree>> axisFunction = axisFunctionSelector.apply(nodeGetter);
        final List<ParseTree> stepNodes = axisFunction.apply(matchedTreeNodes);

        if (ctx.wildcard() != null) {
            return nodeSequence(stepNodes);
        }
        final Set<String> names = ctx.pathNameTestUnion().qname().stream()
            .map(com.github.akruk.antlrquery.AntlrQueryParser.QnameContext::getText).collect(Collectors.toSet());

        matchedTreeNodes = new ArrayList<>(stepNodes.size());

        final boolean[] isToken = new boolean[names.size()];
        final int[] ruleOrTokenIndices = new int[names.size()];
        int i = 0;
        for (final String name : names) {
            final boolean isToken_ = canBeTokenName.test(name);
            isToken[i] = isToken_;
            if (isToken_) {
                ruleOrTokenIndices[i] = parser.getTokenType(name);
            } else {
                ruleOrTokenIndices[i] = parser.getRuleIndex(name);
            }
            i++;
        }
        for (final var node : stepNodes) {
            int j = 0;
            for (final String _ : names) {
                final int targetRuleOrTokenIndex = ruleOrTokenIndices[j];
                if (isToken[j]) {
                    if (!(node instanceof final TerminalNode tokenNode))
                        continue;
                    final Token token = tokenNode.getSymbol();
                    if (token.getType() == targetRuleOrTokenIndex) {
                        matchedTreeNodes.add(tokenNode);
                    }
                } else {
                    if (!(node instanceof final ParserRuleContext testedRule))
                        continue;
                    if (testedRule.getRuleIndex() == targetRuleOrTokenIndex) {
                        matchedTreeNodes.add(testedRule);
                    }
                }
                j++;
            }

        }
        return nodeSequence(matchedTreeNodes);
    }

    private final Predicate<String> canBeTokenName = Pattern.compile("^\\p{IsUppercase}.*").asPredicate();

    private static final Function<NodeGetter, Function<List<ParseTree>, List<ParseTree>>>[] AXIS_DISPATCH_TABLE;

    static {
        @SuppressWarnings("unchecked")
        final Function<NodeGetter, Function<List<ParseTree>, List<ParseTree>>>[] table = (Function<NodeGetter, Function<List<ParseTree>, List<ParseTree>>>[]) new Function[AntlrQueryAxis
            .values().length];
        table[AntlrQueryAxis.ANCESTOR.ordinal()] = nodeGetter -> nodeGetter::getAllAncestors;
        table[AntlrQueryAxis.ANCESTOR_OR_SELF.ordinal()] = nodeGetter -> nodeGetter::getAllAncestorsOrSelf;
        table[AntlrQueryAxis.CHILD.ordinal()] = nodeGetter -> nodeGetter::getAllChildren;
        table[AntlrQueryAxis.DESCENDANT.ordinal()] = nodeGetter -> nodeGetter::getAllDescendants;
        table[AntlrQueryAxis.DESCENDANT_OR_SELF.ordinal()] = nodeGetter -> nodeGetter::getAllDescendantsOrSelf;
        table[AntlrQueryAxis.FOLLOWING.ordinal()] = nodeGetter -> nodeGetter::getAllFollowing;
        table[AntlrQueryAxis.FOLLOWING_SIBLING.ordinal()] = nodeGetter -> nodeGetter::getAllFollowingSiblings;
        table[AntlrQueryAxis.FOLLOWING_OR_SELF.ordinal()] = nodeGetter -> nodeGetter::getAllFollowingOrSelf;
        table[AntlrQueryAxis.FOLLOWING_SIBLING_OR_SELF.ordinal()] = nodeGetter -> nodeGetter::getAllFollowingSiblingsOrSelf;
        table[AntlrQueryAxis.PARENT.ordinal()] = nodeGetter -> nodeGetter::getAllParents;
        table[AntlrQueryAxis.PRECEDING.ordinal()] = nodeGetter -> nodeGetter::getAllPreceding;
        table[AntlrQueryAxis.PRECEDING_SIBLING.ordinal()] = nodeGetter -> nodeGetter::getAllPrecedingSiblings;
        table[AntlrQueryAxis.PRECEDING_OR_SELF.ordinal()] = nodeGetter -> nodeGetter::getAllPrecedingOrSelf;
        table[AntlrQueryAxis.PRECEDING_SIBLING_OR_SELF.ordinal()] = nodeGetter -> nodeGetter::getAllPrecedingSiblingsOrSelf;
        table[AntlrQueryAxis.SELF.ordinal()] = _ -> nodes -> nodes; // identity for SELF
        AXIS_DISPATCH_TABLE = table;
    }


    @Override
    public AntlrQueryValue visitGroupByClause(final AntlrQueryParser.GroupByClauseContext ctx)
    {
        final int groupingCount = ctx.groupingSpec().size();
        final List<String> groupingVars = new ArrayList<>(groupingCount);
        final List<AntlrQueryParser.ExprSingleContext> groupingExpressions = new ArrayList<>(groupingCount);

        for (final AntlrQueryParser.GroupingSpecContext gs : ctx.groupingSpec()) {
            groupingVars.add(gs.varNameAndType().varName().qname().getText());
            groupingExpressions.add(gs.exprSingle());
        }

        final Map<List<AntlrQueryValue>, List<List<VariableCoupling>>> grouped = new LinkedHashMap<>();

        assert visitedTupleStream != null;
        visitedTupleStream.forEach((final List<VariableCoupling> tuple) -> {
            final List<AntlrQueryValue> key = new ArrayList<>(groupingCount);

            for (int i = 0; i < groupingCount; i++) {
                final String varName = groupingVars.get(i);
                final AntlrQueryParser.ExprSingleContext expr = groupingExpressions.get(i);
                final AntlrQueryValue value = expr != null
                    ? visitExprSingle(expr)
                    : getVariableValue(tuple.reversed(), varName);
                key.add(value);
            }

            grouped.computeIfAbsent(key, _ -> new ArrayList<>()).add(tuple);
        });

        visitedTupleStream = grouped.entrySet()
            .stream()
            .map(
                (final Map.Entry<List<AntlrQueryValue>, List<List<VariableCoupling>>> entry) -> {
                    final List<AntlrQueryValue> keyValues = entry.getKey();
                    final List<List<VariableCoupling>> groupTuples = entry.getValue();

                    final List<VariableCoupling> resultTuple = new ArrayList<>();

                    for (int i = 0; i < groupingVars.size(); i++) {
                        final String name = groupingVars.get(i);
                        final AntlrQueryValue value = keyValues.get(i);
                        resultTuple.add(new VariableCoupling(
                            new Variable(name, value),
                            null, null, null));
                    }

                    final Map<String, List<AntlrQueryValue>> collected = new LinkedHashMap<>();

                    for (final List<VariableCoupling> tuple : groupTuples) {
                        for (final VariableCoupling coupling : tuple) {
                            final List<Variable> vars = new ArrayList<>(4);
                            vars.add(coupling.item);
                            vars.add(coupling.key);
                            vars.add(coupling.value);
                            vars.add(coupling.position);
                            for (final Variable var : vars) {
                                if (var != null && !groupingVars.contains(var.name)) {
                                    collected.computeIfAbsent(var.name, _ -> new ArrayList<>()).add(var.value);
                                }
                            }
                        }
                    }

                    for (final Map.Entry<String, List<AntlrQueryValue>> e : collected.entrySet()) {
                        resultTuple.add(new VariableCoupling(
                            new Variable(e.getKey(), valueFactory.sequence(e.getValue())),
                            null, null, null));
                    }

                    return resultTuple;
                });
        return null;
    }

    private AntlrQueryValue getVariableValue(final List<VariableCoupling> tuple, final String name)
    {
        for (final VariableCoupling coupling : tuple) {
            for (final Variable var : List.of( coupling.item, coupling.key, coupling.value, coupling.position)) {
                if (var != null && var.name().equals(name)) {
                    return var.value;
                }
            }
        }
        throw new IllegalStateException(name + " variable is missing");
    }

    @Override
    public AntlrQueryValue visitStringConcatExpr(final AntlrQueryParser.StringConcatExprContext ctx)
    {
        final var firstValue = ctx.rangeExpr(0).accept(this);
        if (ctx.CONCATENATION().isEmpty())
            return firstValue;
        final List<AntlrQueryValue> arguments = ctx.rangeExpr().stream().map(this::visit).toList();
        return concat.call(context, arguments);
    }

    @Override
    public AntlrQueryValue visitArrowExpr(final AntlrQueryParser.ArrowExprContext ctx)
    {
        final boolean notSequenceArrow = ctx.sequenceArrowTarget().isEmpty();
        final boolean notMappingArrow = ctx.mappingArrowTarget().isEmpty();
        if (notSequenceArrow && notMappingArrow) {
            return visitUnaryExpr(ctx.unaryExpr());
        }
        final var savedArgs = saveVisitedArguments();
        final var savedKwargs = saveVisitedKeywordArguments();

        var contextArgument = ctx.unaryExpr().accept(this);
        visitedPositionalArguments.add(contextArgument);
        for (final var arrowexpr : ctx.children.subList(1, ctx.children.size())) {
            contextArgument = arrowexpr.accept(this);
            visitedPositionalArguments = new ArrayList<>();
            visitedPositionalArguments.add(contextArgument);
            visitedKeywordArguments = new HashMap<>();
        }

        visitedPositionalArguments = savedArgs;
        visitedKeywordArguments = savedKwargs;
        return contextArgument;
    }

    @Override
    public AntlrQueryValue visitMappingArrowTarget(final AntlrQueryParser.MappingArrowTargetContext ctx)
    {
        final AntlrQueryValue mappedSequence = visitedPositionalArguments.getLast();
        final ArrayList<AntlrQueryValue> resultingSequence = new ArrayList<>(mappedSequence.size);
        for (final AntlrQueryValue el : mappedSequence.sequence) {
            visitedPositionalArguments = new ArrayList<>();
            visitedPositionalArguments.add(el);
            final var call = ctx.arrowTarget().accept(this);
            if (call.isError)
                return call;
            resultingSequence.add(call);
        }
        return valueFactory.sequence(resultingSequence);
    }

    @Override
    public AntlrQueryValue visitRestrictedDynamicCall(final AntlrQueryParser.RestrictedDynamicCallContext ctx)
    {
        final var function = ctx.children.getFirst().accept(this);
        if (function.isError)
            return function;
        ctx.positionalArgumentList().accept(this);
        return function.functionValue.call(context, visitedPositionalArguments);
    }

    final NamespaceResolver namespaceResolver = new NamespaceResolver(
        "fn",
        "",
        "",
        "",
        ""
    );

    private String[] resolveNamespace(final String functionName)
    {
        return functionName.split(":", 2);
    }

    @Override
    public AntlrQueryValue visitOrExpr(final AntlrQueryParser.OrExprContext ctx)
    {
        final var value = ctx.andExpr(0).accept(this);
        if (ctx.OR().isEmpty())
            return value;
        return booleanOperator.or(ctx.andExpr());
    }

    @Override
    public AntlrQueryValue visitAndExpr(final AntlrQueryParser.AndExprContext ctx)
    {
        if (ctx.AND().isEmpty())
            return ctx.comparisonExpr(0).accept(this);
        return booleanOperator.and(ctx.comparisonExpr());
    }

    @Override
    public AntlrQueryValue visitAdditiveExpr(final AntlrQueryParser.AdditiveExprContext ctx)
    {
        var value = ctx.multiplicativeExpr(0).accept(this);
        if (ctx.additiveOperator().isEmpty())
            return value;
        final var operatorCount = ctx.additiveOperator().size();
        for (int i = 1; i <= operatorCount; i++) {
            final var visitedExpression = ctx.multiplicativeExpr(i).accept(this);
            value = switch (ctx.additiveOperator(i - 1).getText()) {
                case "+" -> addition.call(context, List.of(value, visitedExpression));
                case "-" -> subtraction.call(context, List.of(value, visitedExpression));
                default -> null;
            };
        }
        return value;
    }

    @Override
    public AntlrQueryValue visitComparisonExpr(final AntlrQueryParser.ComparisonExprContext ctx)
    {
        if (ctx.generalComp() != null)
            return handleGeneralComparison(ctx);
        if (ctx.valueComp() != null)
            return handleValueComparison(ctx);
        if (ctx.nodeComp() != null)
            return handleNodeComp(ctx);
        return ctx.otherwiseExpr(0).accept(this);
    }

    private AntlrQueryValue handleGeneralComparison(final AntlrQueryParser.ComparisonExprContext ctx)
    {
        final var value = ctx.otherwiseExpr(0).accept(this);
        final var visitedExpression = ctx.otherwiseExpr(1).accept(this);
        return switch (ctx.generalComp().getText()) {
            case "=" -> generalComparisonOperator.generalEquals(value, visitedExpression);
            case "!=" -> generalComparisonOperator.generalUnequals(value, visitedExpression);
            case ">" -> generalComparisonOperator.generalGreaterThan(value, visitedExpression);
            // Operators such as < and > can use the full-width forms ＜ and ＞ to avoid the need for XML escaping.
            case "＞" -> generalComparisonOperator.generalGreaterThan(value, visitedExpression);
            case "＜", "<" -> generalComparisonOperator.generalLessThan(value, visitedExpression);
            case "<=" -> generalComparisonOperator.generalLessEqual(value, visitedExpression);
            case ">=" -> generalComparisonOperator.generalGreaterEqual(value, visitedExpression);
            default -> throw new IllegalStateException("unhandled general comparison operator");
        };
    }

    private AntlrQueryValue handleValueComparison(final AntlrQueryParser.ComparisonExprContext ctx)
    {
        final var value = ctx.otherwiseExpr(0).accept(this);
        final var visitedExpression = ctx.otherwiseExpr(1).accept(this);
        if (value.isEmptySequence) {
            return emptySequence;
        }
        if (visitedExpression.isEmptySequence) {
            return emptySequence;
        }
        return switch (ctx.valueComp().getText()) {
            case "eq" -> valueComparisonOperator.valueEquals(value, visitedExpression);
            case "ne" -> valueComparisonOperator.valueUnequal(value, visitedExpression);
            case "lt" -> valueComparisonOperator.valueLessThan(value, visitedExpression);
            case "gt" -> valueComparisonOperator.valueGreaterThan(value, visitedExpression);
            case "le" -> valueComparisonOperator.valueLessEqual(value, visitedExpression);
            case "ge" -> valueComparisonOperator.valueGreaterEqual(value, visitedExpression);
            default -> null;
        };
    }

    @Override
    public AntlrQueryValue visitOtherwiseExpr(final AntlrQueryParser.OtherwiseExprContext ctx)
    {
        if (ctx.OTHERWISE().isEmpty())
            return ctx.stringConcatExpr(0).accept(this);
        final int length = ctx.stringConcatExpr().size();
        for (int i = 0; i < length - 1; i++) {
            final var expr = ctx.stringConcatExpr(i);
            final AntlrQueryValue exprValue = expr.accept(this);
            if (exprValue.isEmptySequence)
                continue;
            return exprValue;
        }
        return ctx.stringConcatExpr(length - 1).accept(this);
    }

    @Override
    public AntlrQueryValue visitMultiplicativeExpr(final AntlrQueryParser.MultiplicativeExprContext ctx)
    {
        var value = ctx.unionExpr(0).accept(this);
        if (ctx.multiplicativeOperator().isEmpty())
            return value;
        final var orCount = ctx.multiplicativeOperator().size();
        for (int i = 1; i <= orCount; i++) {
            final var visitedExpression = ctx.unionExpr(i).accept(this);
            value = switch (ctx.multiplicativeOperator(i - 1).getText()) {
                case "*", "x" -> multiplication.call(context, List.of(value, visitedExpression));
                case "div", "÷" -> division.call(context, List.of(value, visitedExpression));
                case "idiv" -> integerDivision.call(context, List.of(value, visitedExpression));
                case "mod" -> modulus.call(context, List.of(value, visitedExpression));
                default -> throw new IllegalStateException("unhandled multiplicative operator");
            };
        }
        return value;
    }

    @Override
    public AntlrQueryValue visitUnionExpr(final AntlrQueryParser.UnionExprContext ctx)
    {
        if (ctx.unionOperator().isEmpty())
            return ctx.intersectExpr(0).accept(this);
        final var values = ctx.intersectExpr().stream().map(this::visit).toList();
        return nodeOperator.union(values);
    }

    @Override
    public AntlrQueryValue visitIntersectExpr(final AntlrQueryParser.IntersectExprContext ctx)
    {
        var value = ctx.instanceofExpr(0).accept(this);
        if (ctx.exceptOrIntersect().isEmpty())
            return value;
        final var operatorCount = ctx.exceptOrIntersect().size();
        for (int i = 1; i <= operatorCount; i++) {
            final var visitedExpression = ctx.instanceofExpr(i).accept(this);
            final boolean isExcept = ctx.exceptOrIntersect(i - 1).EXCEPT() != null;
            if (isExcept) {
                value = nodeOperator.except(List.of(value, visitedExpression));
            } else {
                value = nodeOperator.intersect(List.of(value, visitedExpression));
            }
        }
        return value;
    }

    @Override
    public AntlrQueryValue visitSimpleMapExpr(final AntlrQueryParser.SimpleMapExprContext ctx)
    {
        final List<AntlrQueryParser.PathExprContext> terms = ctx.pathExpr();
        // if there's only one term, no mapping needed
        if (terms.size() == 1) {
            return terms.getFirst().accept(this);
        }

        // start with the initial sequence
        final AntlrQueryValue current = terms.getFirst().accept(this);
        List<AntlrQueryValue> sequence = atomizer.atomize(current);

        // for each subsequent “! expr”
        for (int i = 1; i < terms.size(); i++) {
            final List<AntlrQueryValue> nextSequence = new ArrayList<>();
            for (final AntlrQueryValue item : sequence) {
                context.setValue(item);
                final AntlrQueryValue mapped = terms.get(i).accept(this);
                nextSequence.addAll(atomizer.atomize(mapped));
            }
            sequence = nextSequence;
        }

        return valueFactory.sequence(sequence);
    }

    @Override
    public AntlrQueryValue visitUnaryExpr(final AntlrQueryParser.UnaryExprContext ctx)
    {
        final var value = visitSimpleMapExpr(ctx.simpleMapExpr());
        if (ctx.PLUS() != null)
            return unaryPlus.call(context, List.of(value));
        if (ctx.MINUS() != null)
            return unaryMinus.call(context, List.of(value));
        return value;
    }

    @Override
    public AntlrQueryValue visitSwitchExpr(final AntlrQueryParser.SwitchExprContext ctx)
    {
        final AntlrQueryParser.SwitchComparandContext switchComparand = ctx.switchComparand();

        final AntlrQueryValue switchedValue = switchComparand.switchedExpr != null
            ? switchComparand.switchedExpr.accept(this)
            : null;

        final AntlrQueryParser.SwitchCasesContext switchCasesCtx = ctx.switchCases();
        final AntlrQueryParser.SwitchCasesContext switchCases = switchCasesCtx != null
            ? switchCasesCtx
            : ctx.bracedSwitchCases().switchCases();

        final List<AntlrQueryParser.SwitchCaseClauseContext> caseClauseList = switchCases.switchCaseClause();

        final Map<AntlrQueryValue, ParseTree> valueToExpression = caseClauseList.stream()
            .flatMap(clause -> {
                final AntlrQueryParser.ExprSingleContext exprSingle = clause.exprSingle();
                return clause.switchCaseOperand().stream()
                    .map(operand -> Map.entry(operand.expr().accept(this), exprSingle));
            })
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        final ParseTree toBeExecuted = valueToExpression.getOrDefault(switchedValue, switchCases.defaultExpr);

        return toBeExecuted.accept(this);
    }

    @Override
    public AntlrQueryValue visitArgument(final AntlrQueryParser.ArgumentContext ctx)
    {
        final var value = super.visitArgument(ctx);
        visitedPositionalArguments.add(value);
        return value;
    }

    private List<AntlrQueryValue> saveVisitedArguments()
    {
        final var saved = visitedPositionalArguments;
        visitedPositionalArguments = new ArrayList<>();
        return saved;
    }

    private Stream<List<VariableCoupling>> saveVisitedTupleStream()
    {
        final Stream<List<VariableCoupling>> saved = visitedTupleStream;
        visitedTupleStream = Stream.of(List.of());
        return saved;
    }

    private AntlrQueryVisitingContext saveContext()
    {
        final AntlrQueryVisitingContext saved = context;
        context = new AntlrQueryVisitingContext();
        context.setValue(saved.getValue());
        context.setSize(saved.getSize());
        context.setPosition(saved.getPosition());
        return saved;
    }

    private AntlrQueryAxis saveAxis()
    {
        final var saved = currentAxis;
        currentAxis = null;
        return saved;
    }

    private Comparator<List<VariableCoupling>> ascendingEmptyGreatest(final ParseTree expr)
    {
        return (tuple1, tuple2) -> {
            provideVariables(tuple1);
            final AntlrQueryValue value1 = expr.accept(this);
            provideVariables(tuple2);
            final AntlrQueryValue value2 = expr.accept(this);
            final boolean value1IsEmptySequence = value1.isEmptySequence;
            final boolean value2IsEmptySequence = value2.isEmptySequence;
            if (value1IsEmptySequence && !value2IsEmptySequence) {
                // empty greatest
                return 1;
            }
            return compareValues(value1, value2);
        };
    }

    private Comparator<List<VariableCoupling>> ascendingEmptyLeast(final ParseTree expr)
    {
        return (tuple1, tuple2) -> {
            provideVariables(tuple1);
            final AntlrQueryValue value1 = expr.accept(this);
            provideVariables(tuple2);
            final AntlrQueryValue value2 = expr.accept(this);
            final boolean value1IsEmptySequence = value1.isEmptySequence;
            final boolean value2IsEmptySequence = value2.isEmptySequence;
            if (value1IsEmptySequence && !value2IsEmptySequence) {
                // empty greatest
                return -1;
            }
            return compareValues(value1, value2);
        };
    }

    private Comparator<List<VariableCoupling>> descendingEmptyGreatest(final ParseTree expr)
    {
        return (tuple1, tuple2) -> {
            provideVariables(tuple1);
            final AntlrQueryValue value1 = expr.accept(this);
            provideVariables(tuple2);
            final AntlrQueryValue value2 = expr.accept(this);
            final boolean value1IsEmptySequence = value1.isEmptySequence;
            final boolean value2IsEmptySequence = value2.isEmptySequence;
            if (value1IsEmptySequence && !value2IsEmptySequence) {
                // empty greatest
                return -1;
            }
            return -compareValues(value1, value2);
        };
    }

    private Comparator<List<VariableCoupling>> descendingEmptyLeast(final ParseTree expr)
    {
        return (tuple1, tuple2) -> {
            provideVariables(tuple1);
            final AntlrQueryValue value1 = expr.accept(this);
            provideVariables(tuple2);
            final AntlrQueryValue value2 = expr.accept(this);
            final boolean value1IsEmptySequence = value1.isEmptySequence;
            final boolean value2IsEmptySequence = value2.isEmptySequence;
            if (value1IsEmptySequence && !value2IsEmptySequence) {
                // empty greatest
                return -1;
            }
            return -compareValues(value1, value2);
        };
    }

    private Comparator<List<VariableCoupling>> comparatorFromNthOrderSpec(final List<AntlrQueryParser.OrderSpecContext> orderSpecs,
        final int[] modifierMaskArray, final int i)
    {
        final AntlrQueryParser.OrderSpecContext orderSpec = orderSpecs.getFirst();
        final AntlrQueryParser.ExprSingleContext expr = orderSpec.exprSingle();
        final int modifierMask = modifierMaskArray[i];
        return switch (modifierMask) {
            // ascending, empty greatest
            case 0b00 -> ascendingEmptyGreatest(expr);
            // ascending, empty least
            case 0b01 -> ascendingEmptyLeast(expr);
            // descending, empty greatest
            case 0b10 -> descendingEmptyGreatest(expr);
            // descending, empty least
            case 0b11 -> descendingEmptyLeast(expr);
            default -> null;
        };
    }

    @Override
    public AntlrQueryValue visitIfExpr(final AntlrQueryParser.IfExprContext ctx)
    {
        final var condition = ctx.expr().accept(this);
        final var effectiveBooleanValue = this.effectiveBooleanValue.effectiveBooleanValue(condition);
        final var isBraced = ctx.bracedAction() != null;
        if (isBraced) {
            if (effectiveBooleanValue.booleanValue) {
                return ctx.bracedAction().enclosedExpr().accept(this);
            } else {
                return emptySequence;
            }
        } else {
            if (effectiveBooleanValue.booleanValue)
                return ctx.unbracedActions().exprSingle(0).accept(this);
            else
                return ctx.unbracedActions().exprSingle(1).accept(this);
        }

    }

    @Override
    public AntlrQueryValue visitWindowClause(final AntlrQueryParser.WindowClauseContext ctx)
    {
        if (ctx.tumblingWindowClause() != null) {
            return visitTumblingWindowClause(ctx.tumblingWindowClause());
        } else if (ctx.slidingWindowClause() != null) {
            return visitSlidingWindowClause(ctx.slidingWindowClause());
        }
        return null;
    }

    public AntlrQueryValue visitTumblingWindowClause(final AntlrQueryParser.TumblingWindowClauseContext ctx)
    {
        final String windowVarName = ctx.varNameAndType().varName().qname().getText();
        final AntlrQueryValue sequence = visitExprSingle(ctx.exprSingle());

        final String startVarName = getStartCurrentVarName(ctx.windowStartCondition());
        final String startPosVarName = getStartPositionalVarName(ctx.windowStartCondition());
        final String startPrevVarName = getStartPreviousVarName(ctx.windowStartCondition());
        final String startNextVarName = getStartNextVarName(ctx.windowStartCondition());

        final String endVarName = getEndCurrentVarName(ctx.windowEndCondition());
        final String endPosVarName = getEndPositionalVarName(ctx.windowEndCondition());
        final String endPrevVarName = getEndPreviousVarName(ctx.windowEndCondition());
        final String endNextVarName = getEndNextVarName(ctx.windowEndCondition());

        visitedTupleStream = visitedTupleStream
            .flatMap(tuple -> processTumblingWindowSubSequences(sequence, ctx, windowVarName,
                startVarName, startPosVarName, startPrevVarName, startNextVarName,
                endVarName, endPosVarName, endPrevVarName, endNextVarName,
                new ArrayList<>(tuple)));

        return null;
    }

    public AntlrQueryValue visitSlidingWindowClause(final AntlrQueryParser.SlidingWindowClauseContext ctx)
    {
        final String windowVarName = ctx.varNameAndType().varName().qname().getText();
        final AntlrQueryValue sequence = visitExprSingle(ctx.exprSingle());

        final String startVarName = getStartCurrentVarName(ctx.windowStartCondition());
        final String startPosVarName = getStartPositionalVarName(ctx.windowStartCondition());
        final String startPrevVarName = getStartPreviousVarName(ctx.windowStartCondition());
        final String startNextVarName = getStartNextVarName(ctx.windowStartCondition());

        final String endVarName = getEndCurrentVarName(ctx.windowEndCondition());
        final String endPosVarName = getEndPositionalVarName(ctx.windowEndCondition());
        final String endPrevVarName = getEndPreviousVarName(ctx.windowEndCondition());
        final String endNextVarName = getEndNextVarName(ctx.windowEndCondition());

        visitedTupleStream = visitedTupleStream
            .flatMap(tuple -> processSlidingWindowSubSequences(sequence, ctx, windowVarName,
                startVarName, startPosVarName, startPrevVarName, startNextVarName,
                endVarName, endPosVarName, endPrevVarName, endNextVarName,
                new ArrayList<>(tuple)));

        return null;
    }

    private String getStartCurrentVarName(final AntlrQueryParser.WindowStartConditionContext condition)
    {
        return condition != null && condition.windowVars() != null && condition.windowVars().currentVar() != null
            ? condition.windowVars().currentVar().varName().qname().getText()
            : null;
    }

    private String getStartPositionalVarName(final AntlrQueryParser.WindowStartConditionContext condition)
    {
        return condition != null && condition.windowVars() != null && condition.windowVars().positionalVar() != null
            ? condition.windowVars().positionalVar().varName().qname().getText()
            : null;
    }

    private String getStartPreviousVarName(final AntlrQueryParser.WindowStartConditionContext condition)
    {
        return condition != null && condition.windowVars() != null && condition.windowVars().previousVar() != null
            ? condition.windowVars().previousVar().varName().qname().getText()
            : null;
    }

    private String getStartNextVarName(final AntlrQueryParser.WindowStartConditionContext condition)
    {
        return condition != null && condition.windowVars() != null && condition.windowVars().nextVar() != null
            ? condition.windowVars().nextVar().varName().qname().getText()
            : null;
    }

    private String getEndCurrentVarName(final AntlrQueryParser.WindowEndConditionContext condition)
    {
        return condition != null && condition.windowVars() != null && condition.windowVars().currentVar() != null
            ? condition.windowVars().currentVar().varName().qname().getText()
            : null;
    }

    private String getEndPositionalVarName(final AntlrQueryParser.WindowEndConditionContext condition)
    {
        return condition != null && condition.windowVars() != null && condition.windowVars().positionalVar() != null
            ? condition.windowVars().positionalVar().varName().qname().getText()
            : null;
    }

    private String getEndPreviousVarName(final AntlrQueryParser.WindowEndConditionContext condition)
    {
        return condition != null && condition.windowVars() != null && condition.windowVars().previousVar() != null
            ? condition.windowVars().previousVar().varName().qname().getText()
            : null;
    }

    private String getEndNextVarName(final AntlrQueryParser.WindowEndConditionContext condition)
    {
        return condition != null && condition.windowVars() != null && condition.windowVars().nextVar() != null
            ? condition.windowVars().nextVar().varName().qname().getText()
            : null;
    }

    private Stream<List<VariableCoupling>> processTumblingWindowSubSequences(final AntlrQueryValue sequence,
        final AntlrQueryParser.TumblingWindowClauseContext ctx,
        final String windowVarName, final String startVarName, final String startPosVarName,
        final String startPrevVarName, final String startNextVarName,
        final String endVarName, final String endPosVarName, final String endPrevVarName, final String endNextVarName,
        final List<VariableCoupling> initialTupleElements)
    {

        final List<AntlrQueryValue> sequenceList = sequence.sequence;
        final List<List<VariableCoupling>> allTuples = new ArrayList<>();
        int startIndex = 0;

        while (startIndex < sequenceList.size()) {
            final AntlrQueryParser.WindowStartConditionContext windowStartCondition = ctx.windowStartCondition();
            if (isStartConditionMet(windowStartCondition, startIndex, sequenceList)) {
                final AntlrQueryParser.WindowEndConditionContext windowEndCondition = ctx.windowEndCondition();
                final int endIndex = findEndIndex(windowStartCondition, windowEndCondition, startIndex, sequenceList);

                if (endIndex < sequenceList.size() || !isOnlyEnd(windowEndCondition)) {
                    final List<AntlrQueryValue> subSequence = sequenceList.subList(startIndex, endIndex + 1);
                    final List<VariableCoupling> windowTupleElements = new ArrayList<>(initialTupleElements);

                    addWindowVariables(windowTupleElements, windowVarName, subSequence, startIndex, endIndex,
                        startVarName, startPosVarName, startPrevVarName, startNextVarName,
                        endVarName, endPosVarName, endPrevVarName, endNextVarName);

                    allTuples.add(windowTupleElements);
                    if (endIndex + 1 > sequenceList.size() - 1)
                        break;
                    startIndex = endIndex + 1;
                } else {
                    break;
                }
            } else {
                if (startIndex + 1 > sequenceList.size() - 1)
                    break;
                startIndex++;
            }
        }

        return allTuples.stream();
    }

    private Stream<List<VariableCoupling>> processSlidingWindowSubSequences(final AntlrQueryValue sequence,
        final AntlrQueryParser.SlidingWindowClauseContext ctx,
        final String windowVarName, final String startVarName, final String startPosVarName,
        final String startPrevVarName, final String startNextVarName,
        final String endVarName, final String endPosVarName, final String endPrevVarName, final String endNextVarName,
        final List<VariableCoupling> initialTupleElements)
    {

        final List<AntlrQueryValue> sequenceList = sequence.sequence;
        final List<List<VariableCoupling>> allTuples = new ArrayList<>();
        int startIndex = 0;

        while (startIndex < sequenceList.size()) {
            final AntlrQueryParser.WindowStartConditionContext windowStartCondition = ctx.windowStartCondition();
            if (isStartConditionMet(windowStartCondition, startIndex, sequenceList)) {
                final AntlrQueryParser.WindowEndConditionContext windowEndCondition = ctx.windowEndCondition();
                final int endIndex = findEndIndex(windowStartCondition, windowEndCondition, startIndex, sequenceList);

                if (endIndex < sequenceList.size() || !isOnlyEnd(windowEndCondition)) {
                    final List<AntlrQueryValue> subSequence = sequenceList.subList(startIndex, endIndex + 1);
                    final List<VariableCoupling> windowTupleElements = new ArrayList<>(initialTupleElements);

                    addWindowVariables(windowTupleElements, windowVarName, subSequence, startIndex, endIndex,
                        startVarName, startPosVarName, startPrevVarName, startNextVarName,
                        endVarName, endPosVarName, endPrevVarName, endNextVarName);

                    allTuples.add(windowTupleElements);
                    if (startIndex + 1 > sequenceList.size() - 1)
                        break;
                    startIndex++;
                } else {
                    break;
                }
            } else {
                if (startIndex + 1 > sequenceList.size() - 1)
                    break;
                startIndex++;
            }
        }

        return allTuples.stream();
    }

    private void addWindowVariables(final List<VariableCoupling> windowTupleElements, final String windowVarName,
        final List<AntlrQueryValue> subSequence,
        final int startIndex, final int endIndex, final String startVarName, final String startPosVarName,
        final String startPrevVarName, final String startNextVarName,
        final String endVarName, final String endPosVarName, final String endPrevVarName, final String endNextVarName)
    {

        windowTupleElements.add(
            new VariableCoupling(new Variable(windowVarName, valueFactory.sequence(subSequence)), null, null, null));

        addStartVariables(windowTupleElements, subSequence, startIndex, startVarName, startPosVarName, startPrevVarName,
            startNextVarName);
        addEndVariables(windowTupleElements, subSequence, endIndex, endVarName, endPosVarName, endPrevVarName,
            endNextVarName);
    }

    private void addStartVariables(final List<VariableCoupling> windowTupleElements,
                                   final List<AntlrQueryValue> subSequence, final int startIndex,
                                   final @Nullable String startVarName,
                                   final @Nullable String startPosVarName,
                                   final @Nullable String startPrevVarName,
                                   final @Nullable String startNextVarName)
    {

        if (startVarName != null) {
            final Variable startVar = new Variable(startVarName, subSequence.getFirst());
            windowTupleElements.add(new VariableCoupling(startVar, null, null, null));
        }
        if (startPosVarName != null) {
            final Variable startPosVar = new Variable(startPosVarName, valueFactory.number(startIndex + 1));
            windowTupleElements.add(new VariableCoupling(startPosVar, null, null, null));
        }
        if (startPrevVarName != null) {
            final AntlrQueryValue startPrevValue = startIndex > 0 ? subSequence.getFirst() : emptySequence;
            final Variable startPrevVar = new Variable(startPrevVarName, startPrevValue);
            windowTupleElements.add(new VariableCoupling(startPrevVar, null, null, null));
        }
        if (startNextVarName != null) {
            final AntlrQueryValue startNextValue = startIndex < subSequence.size() - 1 ? subSequence.get(1) : emptySequence;
            final Variable startNextVar = new Variable(startNextVarName, startNextValue);
            windowTupleElements.add(new VariableCoupling(startNextVar, null, null, null));
        }
    }

    private void addEndVariables(final List<VariableCoupling> windowTupleElements,
                                 final List<AntlrQueryValue> subSequence,
                                 final int endIndex,
                                 final @Nullable String endVarName,
                                 final @Nullable String endPosVarName,
                                 final @Nullable String endPrevVarName,
                                 final @Nullable String endNextVarName)
    {

        if (endVarName != null) {
            final Variable endVar = new Variable(endVarName, subSequence.getLast());
            windowTupleElements.add(new VariableCoupling(endVar, null, null, null));
        }
        if (endPosVarName != null) {
            final Variable endPosVar = new Variable(endPosVarName, valueFactory.number(endIndex + 1));
            windowTupleElements.add(new VariableCoupling(endPosVar, null, null, null));
        }
        if (endPrevVarName != null) {
            final var vl = subSequence.size() > 1 ? subSequence.get(subSequence.size() - 2) : emptySequence;
            final Variable endPrevVar = new Variable(endPrevVarName, vl);
            windowTupleElements.add(new VariableCoupling(endPrevVar, null, null, null));
        }
        if (endNextVarName != null) {
            final Variable endNextVar = new Variable(endNextVarName, emptySequence);
            windowTupleElements.add(new VariableCoupling(endNextVar, null, null, null));
        }
    }

    private boolean isStartConditionMet(final AntlrQueryParser.WindowStartConditionContext ctx, final int currentIndex,
        final List<AntlrQueryValue> sequenceList)
    {
        if (ctx != null && ctx.exprSingle() != null) {
            provideVariablesForCondition(ctx, currentIndex, sequenceList);
            final var visited = ctx.exprSingle().accept(this);
            return effectiveBooleanValue.effectiveBooleanValue(visited).booleanValue;
        }
        return true;
    }

    private int findEndIndex(final AntlrQueryParser.WindowStartConditionContext startCtx,
        final AntlrQueryParser.@Nullable WindowEndConditionContext ctx,
        final int startIndex,
        final List<AntlrQueryValue> sequenceList)
    {
        int endIndex = startIndex;
        if (ctx != null && ctx.exprSingle() != null) {
            while (endIndex < sequenceList.size()) {
                provideVariablesForCondition(startCtx, startIndex, sequenceList);
                provideVariablesForCondition(ctx, endIndex, sequenceList);
                final AntlrQueryValue accepted = ctx.exprSingle().accept(this);
                if (effectiveBooleanValue.effectiveBooleanValue(accepted).booleanValue) {
                    break;
                }
                if (endIndex + 1 > sequenceList.size() - 1)
                    break;
                endIndex++;
            }
        } else {
            endIndex = sequenceList.size() - 1;
        }
        return endIndex;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isOnlyEnd(final AntlrQueryParser.@Nullable WindowEndConditionContext ctx)
    {
        return ctx != null && ctx.ONLY() != null;
    }

    private void provideVariablesForCondition(final AntlrQueryParser.WindowStartConditionContext ctx, final int currentIndex,
        final List<AntlrQueryValue> sequenceList)
    {
        final var windowVars = ctx.windowVars();
        provideCurrentVariable(windowVars, currentIndex, sequenceList);
        providePositionalVariable(windowVars, currentIndex);
        providePreviousVariable(windowVars, currentIndex, sequenceList);
        provideNextVariable(windowVars, currentIndex, sequenceList);
    }

    private void provideVariablesForCondition(final AntlrQueryParser.WindowEndConditionContext ctx, final int currentIndex,
        final List<AntlrQueryValue> sequenceList)
    {
        final var windowVars = ctx.windowVars();
        provideCurrentVariable(windowVars, currentIndex, sequenceList);
        providePositionalVariable(windowVars, currentIndex);
        providePreviousVariable(windowVars, currentIndex, sequenceList);
        provideNextVariable(windowVars, currentIndex, sequenceList);
    }

    private void provideCurrentVariable(final AntlrQueryParser.WindowVarsContext vars, final int currentIndex,
        final List<AntlrQueryValue> sequenceList)
    {
        if (vars.currentVar() != null) {
            final String currentVarName = vars.currentVar().varName().qname().getText();
            contextManager.provideVariable(currentVarName, sequenceList.get(currentIndex));
        }
    }

    private void providePositionalVariable(final AntlrQueryParser.WindowVarsContext vars, final int currentIndex)
    {
        if (vars.positionalVar() != null) {
            final String positionalVarName = vars.positionalVar().varName().qname().getText();
            contextManager.provideVariable(positionalVarName, valueFactory.number(currentIndex + 1));
        }
    }

    private void providePreviousVariable(final AntlrQueryParser.WindowVarsContext vars, final int currentIndex,
        final List<AntlrQueryValue> sequenceList)
    {
        if (vars.previousVar() != null) {
            final String previousVarName = vars.previousVar().varName().qname().getText();
            contextManager.provideVariable(previousVarName,
                currentIndex > 0 ? sequenceList.get(currentIndex - 1) : emptySequence);
        }
    }

    private void provideNextVariable(final AntlrQueryParser.WindowVarsContext vars, final int currentIndex,
        final List<AntlrQueryValue> sequenceList)
    {
        if (vars.nextVar() != null) {
            final String nextVarName = vars.nextVar().varName().qname().getText();
            contextManager.provideVariable(nextVarName,
                currentIndex < sequenceList.size() - 1 ? sequenceList.get(currentIndex + 1) : emptySequence);
        }
    }

    private int compareValues(final AntlrQueryValue value1, final AntlrQueryValue value2)
    {
        if (valueComparisonOperator.valueEquals(value1, value2).booleanValue) {
            return 0;
        } else {
            if (valueComparisonOperator.valueLessThan(value1, value2).booleanValue) {
                return -1;
            }
            return 1;
        }
    }

    private void provideVariables(final List<VariableCoupling> tuple)
    {
        for (final var e : tuple) {
            if (e.item != null)
                contextManager.provideVariable(e.item.name, e.item.value);
            if (e.key != null)
                contextManager.provideVariable(e.key.name, e.key.value);
            if (e.value != null)
                contextManager.provideVariable(e.value.name, e.value.value);
            if (e.position != null)
                contextManager.provideVariable(e.position.name, e.position.value);
        }
    }

    @Override
    public AntlrQueryValue visitStringInterpolation(final AntlrQueryParser.StringInterpolationContext ctx)
    {
        final StringBuilder result = new StringBuilder();

        if (ctx.stringInterpolationContent() != null) {
            final AntlrQueryParser.StringInterpolationContentContext ctx1 = ctx.stringInterpolationContent();

            for (int i = 0; i < ctx1.getChildCount(); i++) {
                final var child = ctx1.getChild(i);

                if (child instanceof final AntlrQueryParser.InterpolationCharsContext charsCtx) {
                    // simple chars - unescape and append
                    final StringBuilder result11 = new StringBuilder();

                    for (int i1 = 0; i1 < charsCtx.getChildCount(); i1++) {
                        final ParseTree child1 = charsCtx.getChild(i1);

                        if (child1 instanceof final TerminalNode terminal) {
                            result11.append(terminal.getText());
                        }
                    }
                    result.append(unescapeConstructorChars(result11.toString()));

                } else if (child instanceof final AntlrQueryParser.InterpolationInterpolationContext interpolationCtx) {
                    // interpolation - evaluate and append
                    // Is { expr } or {} ?
                    if (interpolationCtx.expr() != null) {
                        // { expr } -> expr.stringValue
                        final AntlrQueryValue exprValue = visitExpr(interpolationCtx.expr());
                        result.append(processInterpolationValue(exprValue));
                    } else {
                        // {} -> empty string
                    }
                }
            }
        }
        return valueFactory.string(result.toString());
    }

    @Override
    public AntlrQueryValue visitStringConstructor(final AntlrQueryParser.StringConstructorContext ctx)
    {
        final StringBuilder result = new StringBuilder();

        if (ctx.stringConstructorContent() != null) {
            final AntlrQueryParser.StringConstructorContentContext ctx1 = ctx.stringConstructorContent();

            for (int i = 0; i < ctx1.getChildCount(); i++) {
                final var child = ctx1.getChild(i);

                if (child instanceof final AntlrQueryParser.ConstructorCharsContext charsCtx) {
                    // simple chars - unescape and append
                    final StringBuilder result11 = new StringBuilder();

                    for (int i1 = 0; i1 < charsCtx.getChildCount(); i1++) {
                        final ParseTree child1 = charsCtx.getChild(i1);

                        if (child1 instanceof final TerminalNode terminal) {
                            result11.append(terminal.getText());
                        }
                    }
                    result.append(unescapeConstructorChars(result11.toString()));

                } else if (child instanceof final AntlrQueryParser.ConstructorInterpolationContext interpolationCtx) {
                    // interpolation - evaluate and append
                    // Is { expr } or {} ?
                    if (interpolationCtx.expr() != null) {
                        // { expr } -> expr.stringValue
                        final AntlrQueryValue exprValue = visitExpr(interpolationCtx.expr());
                        result.append(processInterpolationValue(exprValue));
                    } else {
                        // {} -> empty string
                    }
                }
            }
        }
        return valueFactory.string(result.toString());
    }

    private String unescapeConstructorChars(final String str)
    {
        if (str == null || str.isEmpty()) {
            return str;
        }

        final StringBuilder result = new StringBuilder();
        final int length = str.length();

        for (int i = 0; i < length; i++) {
            final char ch = str.charAt(i);

            if (ch == '`' && i + 1 < length) {
                final char nextChar = str.charAt(i + 1);
                if (nextChar == '`') {
                    // Escaped backtick: `` -> `
                    result.append('`');
                    i++;
                } else if (nextChar == '{') {
                    // Escaped opening brace sequence: `{ -> {
                    result.append('{');
                    i++;
                } else {
                    // Normal backtick
                    result.append(ch);
                }
            } else if (ch == '\\' && i + 1 < length) {
                final char nextChar = str.charAt(i + 1);
                switch (nextChar) {
                    case '\\':
                        // Escaped backslash: \\ -> \
                        result.append('\\');
                        i++;
                        break;
                    case 'n':
                        // Newline: \n -> newline
                        result.append('\n');
                        i++;
                        break;
                    case 't':
                        // Tab: \t -> tab
                        result.append('\t');
                        i++;
                        break;
                    case 'r':
                        // Carriage return: \r -> CR
                        result.append('\r');
                        i++;
                        break;
                    case '"':
                        // Escaped double quote: \" -> "
                        result.append('"');
                        i++;
                        break;
                    case '\'':
                        // Escaped single quote: \' -> '
                        result.append('\'');
                        i++;
                        break;
                    case '{':
                        // Escaped opening brace: \{ -> {
                        result.append('{');
                        i++;
                        break;
                    case '}':
                        // Escaped closing brace: \} -> }
                        result.append('}');
                        i++;
                        break;
                    default:
                        // Unrecognized escape, ignore...
                        result.append(ch);
                        break;
                }
            } else {
                // Normal character
                result.append(ch);
            }
        }

        return result.toString();
    }

    private String processInterpolationValue(final AntlrQueryValue value)
    {
        return atomizer.atomize(value).stream()
            .map(v -> string.call(context, List.of(v)).stringValue)
            .collect(Collectors.joining(" "));
    }

    @Override
    public AntlrQueryValue visitCurlyArrayConstructor(final AntlrQueryParser.CurlyArrayConstructorContext ctx)
    {
        final AntlrQueryValue enclosedValue = visitEnclosedExpr(ctx.enclosedExpr());
        return valueFactory.array(enclosedValue.sequence);
    }

    @Override
    public AntlrQueryValue visitSquareArrayConstructor(final AntlrQueryParser.SquareArrayConstructorContext ctx)
    {
        final List<AntlrQueryValue> values = ctx.exprSingle().stream().map(this::visit).toList();
        return valueFactory.array(values);
    }

    @Override
    public AntlrQueryValue visitMapConstructor(final AntlrQueryParser.MapConstructorContext ctx)
    {
        final var map = ctx.mapConstructorEntry().stream()
            .collect(Collectors.toMap(
                entry -> entry.mapKeyExpr().accept(this),
                entry -> entry.mapValueExpr().accept(this),
                (existing, _) -> existing,
                LinkedHashMap::new
            ));
        return valueFactory.map(Collections.unmodifiableMap(map));
    }

    @Override
    public AntlrQueryValue visitPipelineExpr(final AntlrQueryParser.PipelineExprContext ctx)
    {
        if (ctx.PIPE_ARROW().isEmpty())
            return ctx.arrowExpr(0).accept(this);
        final var saved = saveContext();
        final int size = ctx.arrowExpr().size();
        AntlrQueryValue contextValue = ctx.arrowExpr(0).accept(this);
        for (var i = 1; i < size; i++) {
            final var contextualizedExpr = ctx.arrowExpr(i);
            context.setValue(contextValue);
            contextValue = contextualizedExpr.accept(this);
        }
        context = saved;
        return contextValue;
    }

    @Override
    public AntlrQueryValue visitLookupExpr(final AntlrQueryParser.LookupExprContext ctx)
    {
        final var target = ctx.postfixExpr().accept(this);
        final var keySpecifier = getKeySpecifier(ctx);
        return evaluateLookup(target, keySpecifier);

    }

    private AntlrQueryValue evaluateLookup(final AntlrQueryValue target, final AntlrQueryValue keySpecifier)
    {
        if (keySpecifier == null) {
            return evaluateWildcardLookup(target);
        } else {
            return evaluateKeyLookup(target, keySpecifier);
        }
    }

    private AntlrQueryValue evaluateKeyLookup(final AntlrQueryValue target, final AntlrQueryValue keySpecifier)
    {
        final int resultsize = target.size * keySpecifier.size;
        final ArrayList<AntlrQueryValue> results = new ArrayList<>(resultsize);
        for (final AntlrQueryValue element : target.sequence) {
            switch (element.valueType) {
                case ARRAY:
                    if (!keySpecifier.isNumeric)
                        continue;
                    final int index = keySpecifier.numericValue.intValue() - 1;
                    if (index > element.arrayMembers.size() || index < 0)
                        valueFactory.error(AntlrQueryError.ArrayIndexOutOfBounds,
                            getLookupArrayErrorMessage(keySpecifier, element));
                    results.add(element.arrayMembers.get(index));
                    break;
                case MAP:
                    final AntlrQueryValue value = element.mapEntries.get(keySpecifier);
                    if (value != null)
                        results.add(value);
                    break;
                default:
                    break;
            }

        }
        return valueFactory.sequence(results);
    }

    private String getLookupArrayErrorMessage(final AntlrQueryValue keySpecifier, final AntlrQueryValue element)
    {
        return "Index: " + keySpecifier.numericValue + " is out of bounds for array " + element + " of size "
            + element.size;
    }

    private AntlrQueryValue evaluateWildcardLookup(final AntlrQueryValue target)
    {
        final int resultsize = target.size;
        final ArrayList<AntlrQueryValue> results = new ArrayList<>(resultsize * resultsize);
        for (final AntlrQueryValue element : target.sequence) {
            switch (element.valueType) {
                case ARRAY:
                    results.addAll(element.arrayMembers);
                    break;
                case MAP:
                    results.addAll(element.mapEntries.values());
                    break;
                default:
                    return valueFactory.error(AntlrQueryError.InvalidArgumentType,
                        "Target of a lookup must be a sequence of arrays and maps, encountered: " + element);
            }
        }
        return valueFactory.sequence(results);
    }

    @Override
    public AntlrQueryValue visitUnaryLookup(final AntlrQueryParser.UnaryLookupContext ctx)
    {
        final var target = context.getValue();
        final var keySpecifier = ctx.lookup().keySpecifier().accept(this);
        return evaluateLookup(target, keySpecifier);
    }

    AntlrQueryValue getKeySpecifier(final AntlrQueryParser.LookupExprContext ctx)
    {
        final AntlrQueryParser.KeySpecifierContext keySpecifier = ctx.lookup().keySpecifier();
        if (keySpecifier.qname() != null) {
            return valueFactory.string(keySpecifier.qname().getText());
        }
        if (keySpecifier.STRING() != null) {
            return handleString(keySpecifier);
        }
        if (keySpecifier.IntegerLiteral() != null) {
            return handleInteger(keySpecifier.IntegerLiteral());
        }
        return keySpecifier.accept(this);
    }

    private AntlrQueryValue handleString(final ParserRuleContext ctx)
    {
        final String content = processStringLiteral(ctx);
        return valueFactory.string(content);
    }

    private String processStringLiteral(final ParserRuleContext ctx)
    {
        final String rawText = ctx.getText();
        final String content = unescapeString(rawText.substring(1, rawText.length() - 1));
        valueFactory.string(content);
        return content;
    }

    @Override
    public AntlrQueryValue visitInstanceofExpr(final AntlrQueryParser.InstanceofExprContext ctx)
    {
        if (ctx.INSTANCE() == null)
            return visitTreatExpr(ctx.treatExpr());
        final var visited = visitTreatExpr(ctx.treatExpr());
        final var expectedType = ctx.type().accept(typeVisitor);
        final boolean result = Types.isSubtype(typeFactory, visited.type, expectedType);
        return valueFactory.bool(result);
    }

    @Override
    public AntlrQueryValue visitTreatExpr(final AntlrQueryParser.TreatExprContext ctx)
    {
        if (ctx.TREAT() == null)
            return visitCastableExpr(ctx.castableExpr());
        final var type = ctx.type().accept(typeVisitor);
        final var expr = visitCastableExpr(ctx.castableExpr());
        if (!Types.isSubtype(typeFactory, expr.type, type)) {
            return valueFactory.error(AntlrQueryError.TreatAsTypeMismatch,
                "Type: " + expr.type + " cannot be treated as " + type);
        }
        return expr;
    }

    @Override
    public AntlrQueryValue visitCastableExpr(final AntlrQueryParser.CastableExprContext ctx)
    {
        if (ctx.CASTABLE() == null)
            return visitCastExpr(ctx.castExpr());
        final AntlrQuerySequenceType targetType = semanticAnalyzer.visitCastTarget(ctx.castTarget()).type;
        final AntlrQueryValue testedValue = visitCastExpr(ctx.castExpr());
        final boolean isCastable = !caster.cast(targetType, testedValue).isError;
        return valueFactory.bool(isCastable);
    }

    @Override
    public AntlrQueryValue visitCastExpr(final AntlrQueryParser.CastExprContext ctx)
    {
        if (ctx.CAST() == null)
            return visitPipelineExpr(ctx.pipelineExpr());
        final AntlrQuerySequenceType targetType = semanticAnalyzer.visitCastTarget(ctx.castTarget()).type;
        final AntlrQueryValue testedValue = visitPipelineExpr(ctx.pipelineExpr());
        return caster.cast(targetType, testedValue);
    }

    private String stringContents(final TerminalNode ctx)
    {
        final var text = ctx.getText();
        return text.substring(1, text.length() - 1);
    }

    @Override
    public AntlrQueryValue visitPathModuleImport(final AntlrQueryParser.PathModuleImportContext ctx)
    {
        final var result = moduleManager.pathModuleImport(stringContents(ctx.STRING()));
        this.visit(result.tree());
        return null;
    }

    @Override
    public AntlrQueryValue visitDefaultPathModuleImport(final AntlrQueryParser.DefaultPathModuleImportContext ctx)
    {
        final var result = moduleManager.defaultPathModuleImport(ctx.getText().replace(":", "/"));
        this.visit(result.tree());
        return null;
    }

    @Override
    public AntlrQueryValue visitNamespaceModuleImport(final AntlrQueryParser.NamespaceModuleImportContext ctx)
    {
        final var result = moduleManager.pathModuleImport(stringContents(ctx.STRING()));
        this.visit(result.tree());
        return null;
    }

    @Override
    public AntlrQueryValue visitTypeswitchExpr(AntlrQueryParser.TypeswitchExprContext ctx)
    {
        var switched = visitExpr(ctx.expr());
        var cases = ctx.bracedTypeswitchCases() != null
            ? ctx.bracedTypeswitchCases().typeswitchCases()
            : ctx.typeswitchCases()
            ;
        var clauses = cases.caseClause();
        for (var typeswitchCase : clauses) {
            var type = typeVisitor.visitType(typeswitchCase.type());
            if (Types.isSubtype(typeFactory, switched.type, type)) {
                if (typeswitchCase.varName() != null) {
                    var caseVarName = cases.varName().qname().getText();
                    contextManager.provideVariable(caseVarName, switched);
                }
                return visitExprSingle(typeswitchCase.exprSingle());
            }
        }
        if (cases.varName() != null) {
            var defaultName = cases.varName().qname().getText();
            contextManager.provideVariable(defaultName, switched);
        }
        return visitExprSingle(cases.exprSingle());
    }
}

