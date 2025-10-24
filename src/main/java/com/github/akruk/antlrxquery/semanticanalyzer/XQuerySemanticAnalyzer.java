package com.github.akruk.antlrxquery.semanticanalyzer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.github.akruk.antlrxquery.AntlrXqueryParser.*;
import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver;
import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver.ResolvedName;
import com.github.akruk.antlrxquery.semanticanalyzer.ModuleManager.ImportResult;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.Assumption;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.XQuerySemanticContextManager;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.XQuerySemanticFunctionManager;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.XQuerySemanticFunctionManager.AnalysisResult;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.XQuerySemanticFunctionManager.ArgumentSpecification;
import com.github.akruk.antlrxquery.AntlrXqueryParserBaseVisitor;
import com.github.akruk.antlrxquery.XQueryAxis;
import com.github.akruk.antlrxquery.charescaper.XQuerySemanticCharEscaper;
import com.github.akruk.antlrxquery.charescaper.XQuerySemanticCharEscaper.XQuerySemanticCharEscaperResult;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;
import com.github.akruk.antlrxquery.inputgrammaranalyzer.InputGrammarAnalyzer.GrammarAnalysisResult;
import com.github.akruk.antlrxquery.typesystem.XQueryRecordField;
import com.github.akruk.antlrxquery.typesystem.defaults.TypeInContext;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryTypes;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType.RelativeCoercability;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.typeoperations.SequencetypeAtomization;
import com.github.akruk.antlrxquery.typesystem.typeoperations.SequencetypeCastable;
import com.github.akruk.antlrxquery.typesystem.typeoperations.SequencetypePathOperator;
import com.github.akruk.antlrxquery.typesystem.typeoperations.SequencetypeCastable.Castability;
import com.github.akruk.antlrxquery.typesystem.typeoperations.SequencetypeCastable.IsCastableResult;
import com.github.akruk.antlrxquery.typesystem.typeoperations.SequencetypePathOperator.PathOperatorResult;


public class XQuerySemanticAnalyzer extends AntlrXqueryParserBaseVisitor<TypeInContext> {

    private final XQuerySemanticContextManager contextManager;
    private final List<DiagnosticError> errors;
    private final List<DiagnosticWarning> warnings;
    private final XQueryTypeFactory typeFactory;
    private final XQueryValueFactory valueFactory;
    private final XQuerySemanticFunctionManager functionManager;
    private final SequencetypePathOperator pathOperator;
    // private final Parser parser;
    // private final List<Path> modulePaths;
    private final ModuleManager moduleManager;
    private XQueryVisitingSemanticContext context;
    private List<TypeInContext> visitedPositionalArguments;
    private Map<String, TypeInContext> visitedKeywordArguments;
    private final GrammarAnalysisResult grammarAnalysisResult;

    protected final XQuerySequenceType number;
    protected final XQuerySequenceType zeroOrMoreNodes;
    protected final XQuerySequenceType anyArray;
    protected final XQuerySequenceType anyMap;
    protected final XQuerySequenceType boolean_;
    protected final XQuerySequenceType string;
    protected final XQuerySequenceType optionalNumber;
    protected final XQuerySequenceType anyNumbers;
    protected final XQuerySequenceType optionalString;
    protected final XQuerySequenceType anyItem;
    protected final XQuerySequenceType anyArrayOrMap;
    protected final XQuerySequenceType zeroOrMoreItems;
    protected final XQuerySequenceType emptySequence;

    public List<DiagnosticError> getErrors()
    {
        return errors;
    }

    public List<DiagnosticWarning> getWarnings()
    {
        return warnings;
    }

    public XQuerySemanticFunctionManager getFunctionManager() {
        return functionManager;
    }

    @Override
    public TypeInContext visitXquery(final XqueryContext ctx)
    {
        if (ctx.libraryModule() != null)
            return visitLibraryModule(ctx.libraryModule());
        return visitMainModule(ctx.mainModule());
    }

    public XQuerySemanticAnalyzer(
        final Parser parser,
        final XQuerySemanticContextManager contextManager,
        final XQueryTypeFactory typeFactory,
        final XQueryValueFactory valueFactory,
        final XQuerySemanticFunctionManager functionCaller,
        final GrammarAnalysisResult grammarAnalysisResult,
        final ModuleManager moduleManager)
    {
        this.grammarAnalysisResult = grammarAnalysisResult;
        // this.parser = parser;
        // this.modulePaths = modulePaths;
        this.typeFactory = typeFactory;
        this.valueFactory = valueFactory;
        this.functionManager = functionCaller;
        this.functionManager.setAnalyzer(this);
        this.contextManager = contextManager;
        this.contextManager.enterContext();
        this.context = new XQueryVisitingSemanticContext();
        this.context.setType(contextManager.typeInContext(typeFactory.anyNode()));
        this.context.setPositionType(null);
        this.context.setSizeType(null);
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.anyArrayOrMap = typeFactory.zeroOrMore(typeFactory.itemChoice(Set.of(typeFactory.itemAnyMap(), typeFactory.itemAnyArray())));
        this.zeroOrMoreItems = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
        this.emptySequence = typeFactory.emptySequence();
        this.number = typeFactory.number();
        this.zeroOrMoreNodes = typeFactory.zeroOrMore(typeFactory.itemAnyNode());
        this.anyArray = typeFactory.anyArray();
        this.anyMap = typeFactory.anyMap();
        this.boolean_ = typeFactory.boolean_();
        this.string = typeFactory.string();
        this.optionalNumber = typeFactory.zeroOrOne(typeFactory.itemNumber());
        this.anyNumbers = typeFactory.zeroOrMore(typeFactory.itemNumber());
        this.optionalString = typeFactory.zeroOrOne(typeFactory.itemString());
        this.anyItem = typeFactory.anyItem();
        this.zeroOrOneItem = typeFactory.zeroOrOne(typeFactory.itemAnyItem());

        this.atomizer = new SequencetypeAtomization(typeFactory);
        this.castability = new SequencetypeCastable(typeFactory, atomizer);
        this.anyNodes = typeFactory.zeroOrMore(typeFactory.itemAnyNode());
        this.pathOperator = new SequencetypePathOperator(typeFactory, parser);
        this.moduleManager = moduleManager;
    }

    @Override
    public TypeInContext visitFLWORExpr(final FLWORExprContext ctx)
    {
        final var saveReturnedOccurence = saveReturnedOccurence();
        contextManager.enterScope();
        visitInitialClause(ctx.initialClause());
        for (final var clause : ctx.intermediateClause()) {
            clause.accept(this);
        }
        // at this point visitedTupleStream should contain all tuples
        final var expressionValue = visitReturnClause(ctx.returnClause());
        contextManager.leaveScope();
        returnedOccurrence = saveReturnedOccurence;
        return expressionValue;
    }

    private int returnedOccurrence = 1;

    private int saveReturnedOccurence()
    {
        final var saved = returnedOccurrence;
        returnedOccurrence = 1;
        return saved;
    }

    @Override
    public TypeInContext visitLetClause(final LetClauseContext ctx)
    {
        for (final var letBinding : ctx.letBinding()) {
            final VarNameAndTypeContext varNameAndType = letBinding.varNameAndType();
            entypeVariable(letBinding, varNameAndType, letBinding.exprSingle());
        }
        return null;
    }

    private void entypeVariable(final ParserRuleContext ctx,
                                final VarNameAndTypeContext varNameAndType,
                                final ExprSingleContext assignedValueCtx)
    {
        final String variableName = varNameAndType.varRef().qname().getText();
        final TypeInContext assignedValue = visitExprSingle(assignedValueCtx);
        if (varNameAndType.typeDeclaration() == null) {
            contextManager.entypeVariable(variableName, assignedValue);
        } else {
            final TypeInContext type = varNameAndType.typeDeclaration().accept(this);
            if (!assignedValue.isSubtypeOf(type)) {
                // final String msg = String.format(
                //     "Type of variable %s is not compatible with the assigned value: %s is not subtype of %s",
                //     variableName, assignedValue, type);
                error(ctx, ErrorType.LOOKUP__INVALID_TARGET, List.of(variableName, assignedValue, type));
            }
            contextManager.entypeVariable(variableName, type);
        }
    }


    @Override
    public TypeInContext visitForClause(final ForClauseContext ctx) {
        // TODO: add coercion
        for (final ForBindingContext forBinding : ctx.forBinding()) {
            if (forBinding.forItemBinding() != null) {
                processForItemBinding(forBinding.forItemBinding());
            } else if (forBinding.forMemberBinding() != null) {
                processForMemberBinding(forBinding.forMemberBinding());
            } else if (forBinding.forEntryBinding() != null) {
                processForEntryBinding(forBinding.forEntryBinding());
            }
        }
        return null;
    }

    @Override
    public TypeInContext visitTumblingWindowClause(final TumblingWindowClauseContext ctx) {
        final var iteratedType = visitExprSingle(ctx.exprSingle());
        final var iterator = contextManager.typeInContext(iteratedType.iteratorType());
        final var optionalIterator = contextManager.typeInContext(iterator.type.addOptionality());
        final String windowVariableName = ctx.varNameAndType().varRef().qname().getText();
        final TypeInContext windowSequenceType = contextManager.typeInContext(typeFactory.oneOrMore(iterator.type.itemType));

        returnedOccurrence = arrayMergeFLWOROccurence();
        handleWindowStartClause(ctx.windowStartCondition(), iterator, optionalIterator);
        handleWindowEndClause(ctx.windowEndCondition(), iterator, optionalIterator);
        handleWindowIterator(ctx.varNameAndType(), windowVariableName, windowSequenceType);
        return null;
    }

    private void entypeWindowVariables(
        final TypeInContext iterator,
        final TypeInContext optionalIterator,
        final WindowVarsContext windowVars)
    {
        final var currentVar = windowVars.currentVar();
        if (currentVar != null) {
            contextManager.entypeVariable(currentVar.varRef().qname().getText(), iterator);
        }
        final var currentVarPos = windowVars.positionalVar();
        if (currentVarPos != null) {
            contextManager.entypeVariable(currentVarPos.varRef().qname().getText(), contextManager.typeInContext(typeFactory.number()));
        }
        final var previousVar = windowVars.previousVar();
        if (previousVar != null) {
            contextManager.entypeVariable(previousVar.varRef().qname().getText(), optionalIterator);
        }
        final var nextVar = windowVars.nextVar();
        if (nextVar != null) {
            contextManager.entypeVariable(nextVar.varRef().qname().getText(), optionalIterator);
        }
    }

    @Override
    public TypeInContext visitSlidingWindowClause(final SlidingWindowClauseContext ctx) {
        final var iteratedType = visitExprSingle(ctx.exprSingle());
        final var iterator = contextManager.typeInContext(iteratedType.iteratorType());
        final var optionalIterator = contextManager.typeInContext(iterator.type.addOptionality());
        final String windowVariableName = ctx.varNameAndType().varRef().qname().getText();
        final TypeInContext windowSequenceType = contextManager.typeInContext(typeFactory.oneOrMore(iterator.type.itemType));

        returnedOccurrence = arrayMergeFLWOROccurence();
        handleWindowStartClause(ctx.windowStartCondition(), iterator, optionalIterator);
        handleWindowEndClause(ctx.windowEndCondition(), iterator, optionalIterator);
        handleWindowIterator(ctx.varNameAndType(), windowVariableName, windowSequenceType);
        return null;
    }

    private void handleWindowIterator(final VarNameAndTypeContext ctx, final String windowVariableName,
            final TypeInContext windowSequenceType) {
        if (ctx.typeDeclaration() != null) {
            final TypeInContext windowDeclaredVarType = visitTypeDeclaration(ctx.typeDeclaration());
            if (!windowDeclaredVarType.isSubtypeOf(windowSequenceType)) {
                error(ctx, ErrorType.WINDOW__DECLARATION_MISMATCH, List.of(windowDeclaredVarType, windowSequenceType));
            }
            contextManager.entypeVariable(windowVariableName, windowDeclaredVarType);
        } else {
            contextManager.entypeVariable(windowVariableName, windowSequenceType);
        }
    }

    private void handleWindowStartClause(
        final WindowStartConditionContext windowStartCondition,
        final TypeInContext iterator,
        final TypeInContext optionalIterator)
    {
        if (windowStartCondition != null) {
            final var windowVars = windowStartCondition.windowVars();
            entypeWindowVariables(iterator, optionalIterator, windowVars);
            if (windowStartCondition.WHEN() != null) {
                final var conditionType = visitExprSingle(windowStartCondition.exprSingle());
                if (!conditionType.type.hasEffectiveBooleanValue()) {
                    error(
                        windowStartCondition.exprSingle(),
                        ErrorType.WINDOW__START_CLAUSE_CONDITION_NOT_EBV,
                        List.of(conditionType)
                        );
                }
            }
        }
    }

    private void handleWindowEndClause(
        final WindowEndConditionContext windowEndConditionContext,
        final TypeInContext iterator,
        final TypeInContext optionalIterator)
    {
        if (windowEndConditionContext != null) {
            final var windowVars = windowEndConditionContext.windowVars();
            entypeWindowVariables(iterator, optionalIterator, windowVars);
            if (windowEndConditionContext.WHEN() != null) {
                final var conditionType = visitExprSingle(windowEndConditionContext.exprSingle());
                if (!conditionType.type.hasEffectiveBooleanValue()) {
                    error(
                        windowEndConditionContext.exprSingle(),
                        ErrorType.WINDOW__END_CLAUSE_CONDITION_NOT_EBV,
                        List.of(conditionType)
                        );
                }
            }
        }
    }

    @Override
    public TypeInContext visitGroupByClause(final GroupByClauseContext ctx) {
        final List<String> groupingVars = new ArrayList<>(ctx.groupingSpec().size());
        for (final var gs : ctx.groupingSpec()) {
            if (gs.exprSingle() != null) {
                entypeVariable(gs, gs.varNameAndType(), gs.exprSingle());
            } else {
                final String varname = gs.varNameAndType().varRef().qname().getText();
                TypeInContext variableType = contextManager.getVariable(varname);
                if (variableType == null) {
                    error(
                        gs.varNameAndType().varRef(),
                        ErrorType.GROUP_BY__UNDEFINED_GROUPING_VARIABLE,
                        List.of(varname)
                        );
                    variableType = contextManager.typeInContext(zeroOrMoreItems);
                }
                final XQuerySequenceType atomizedType = atomizer.atomize(variableType.type);
                if (!atomizedType.isSubtypeOf(zeroOrOneItem)) {
                    error(
                        gs.varNameAndType().varRef(),
                        ErrorType.GROUP_BY__WRONG_GROUPING_VAR_TYPE,
                        List.of(varname, zeroOrOneItem, atomizedType)
                        );

                }
                contextManager.entypeVariable(varname, contextManager.typeInContext(atomizedType.iteratorType()));
                if (groupingVars.contains(varname)) {
                    error(gs.varNameAndType().varRef(), ErrorType.GROUP_BY__DUPLICATED_VAR, List.of(varname));
                } else {
                    groupingVars.add(varname);
                }
            }
        }
        final Set<Entry<String, TypeInContext>> variablesInContext = contextManager.currentContext().getVariables().entrySet();
        for (final var variableNameAndType : variablesInContext) {
            final String varName = variableNameAndType.getKey();
            if (groupingVars.contains(varName)) {
                continue;
            }
            final var varType = variableNameAndType.getValue();
            contextManager.entypeVariable(varName, contextManager.typeInContext(varType.type.addOptionality()));
        }
        return null;
    }

    public void processForItemBinding(final ForItemBindingContext ctx) {
        final String variableName = ctx.varNameAndType().varRef().qname().getText();
        final TypeInContext sequenceType = ctx.exprSingle().accept(this);
        returnedOccurrence = mergeFLWOROccurrence(sequenceType.type);

        checkPositionalVariableDistinct(ctx.positionalVar(), variableName, ctx);

        final XQueryItemType itemType = sequenceType.type.itemType;
        final XQuerySequenceType iteratorType = (ctx.allowingEmpty() != null)
                ? typeFactory.zeroOrOne(itemType)
                : typeFactory.one(itemType);

        processVariableTypeDeclaration(ctx.varNameAndType(), contextManager.typeInContext(iteratorType), variableName, ctx);

        if (ctx.positionalVar() != null) {
            final String positionalVariableName = ctx.positionalVar().varRef().qname().getText();
            contextManager.entypeVariable(positionalVariableName, contextManager.typeInContext(number));
        }
    }

    public void processForMemberBinding(final ForMemberBindingContext ctx) {
        final String variableName = ctx.varNameAndType().varRef().qname().getText();
        final TypeInContext arrayType = ctx.exprSingle().accept(this);
        returnedOccurrence = arrayMergeFLWOROccurence();

        if (!arrayType.type.isSubtypeOf(anyArray)) {
            error(ctx, ErrorType.FOR_MEMBER__WRONG_ITERABLE_TYPE, List.of(arrayType));
        }

        checkPositionalVariableDistinct(ctx.positionalVar(), variableName, ctx);

        final XQuerySequenceType memberType = arrayType.type.itemType.arrayMemberType;

        processVariableTypeDeclaration(ctx.varNameAndType(), contextManager.typeInContext(memberType), variableName, ctx);

        if (ctx.positionalVar() != null) {
            final String positionalVariableName = ctx.positionalVar().varRef().qname().getText();
            contextManager.entypeVariable(positionalVariableName, contextManager.typeInContext(number));
        }
    }

    public void processForEntryBinding(final ForEntryBindingContext ctx) {
        final TypeInContext mapType = ctx.exprSingle().accept(this);
        returnedOccurrence = arrayMergeFLWOROccurence();

        if (!mapType.type.isSubtypeOf(anyMap)) {
            error(
                ctx,
                ErrorType.FOR_ENTRY__WRONG_ITERABLE_TYPE,
                List.of());
            return;
        }

        final ForEntryKeyBindingContext keyBinding = ctx.forEntryKeyBinding();
        final ForEntryValueBindingContext valueBinding = ctx.forEntryValueBinding();

        // Check for duplicate key and value variable names
        if (keyBinding != null && valueBinding != null) {
            final String keyVarName = keyBinding.varNameAndType().varRef().qname().getText();
            final String valueVarName = valueBinding.varNameAndType().varRef().qname().getText();
            if (keyVarName.equals(valueVarName)) {
                error(ctx, ErrorType.FOR_ENTRY__KEY_VALUE_VARS_DUPLICATED_NAME, List.of());
            }
        }

        // Process key binding
        if (keyBinding != null) {
            final String keyVariableName = keyBinding.varNameAndType().varRef().qname().getText();
            final XQueryItemType keyType = mapType.type.itemType.mapKeyType;
            final XQuerySequenceType keyIteratorType = typeFactory.one(keyType);

            checkPositionalVariableDistinct(ctx.positionalVar(), keyVariableName, ctx);
            processVariableTypeDeclaration(keyBinding.varNameAndType(), contextManager.typeInContext(keyIteratorType), keyVariableName, ctx);
        }

        // Process value binding
        if (valueBinding != null) {
            final String valueVariableName = valueBinding.varNameAndType().varRef().qname().getText();
            final XQuerySequenceType valueType = mapType.type.itemType.mapValueType;

            checkPositionalVariableDistinct(ctx.positionalVar(), valueVariableName, ctx);
            processVariableTypeDeclaration(valueBinding.varNameAndType(), contextManager.typeInContext(valueType), valueVariableName, ctx);
        }

        if (ctx.positionalVar() != null) {
            final String positionalVariableName = ctx.positionalVar().varRef().qname().getText();
            contextManager.entypeVariable(positionalVariableName, contextManager.typeInContext(number));
        }
    }

    private void checkPositionalVariableDistinct(final PositionalVarContext positionalVar,
                                            final String mainVariableName,
                                            final ParserRuleContext context)
    {
        if (positionalVar != null) {
            final String positionalVariableName = positionalVar.varRef().qname().getText();
            if (mainVariableName.equals(positionalVariableName)) {
                error(context, ErrorType.FOR_ENTRY__POSITIONAL_VARIABLE_SAME_AS_MAIN_VARIABLE_NAME, List.of());
            }
        }
    }

    protected void processVariableTypeDeclaration(final VarNameAndTypeContext varNameAndType,
                                            final TypeInContext inferredType,
                                            final String variableName,
                                            final ParseTree context)
    {
        if (varNameAndType.typeDeclaration() == null) {
            contextManager.entypeVariable(variableName, inferredType);
            return;
        }

        final TypeInContext declaredType = visitTypeDeclaration(varNameAndType.typeDeclaration());
        if (!inferredType.isSubtypeOf(declaredType)) {
            error(
                (ParserRuleContext)context,
                ErrorType.VARIABLE_DECLARATION__ASSIGNED_TYPE_INCOMPATIBLE,
                List.of(variableName, inferredType, declaredType)
                );
        }
        contextManager.entypeVariable(variableName, declaredType);
    }


    @Override
    public TypeInContext visitSequenceType(final SequenceTypeContext ctx)
    {
        if (ctx.emptySequence() != null) {
            return contextManager.typeInContext(emptySequence);
        }
        final var itemType = ctx.itemType().accept(this).type.itemType;
        if (ctx.occurrenceIndicator() == null) {
            return contextManager.typeInContext(typeFactory.one(itemType));
        }
        return switch (ctx.occurrenceIndicator().getText()) {
            case "?" -> contextManager.typeInContext(typeFactory.zeroOrOne(itemType));
            case "*" -> contextManager.typeInContext(typeFactory.zeroOrMore(itemType));
            case "+" -> contextManager.typeInContext(typeFactory.oneOrMore(itemType));
            default -> null;
        };
    }

    @Override
    public TypeInContext visitAnyItemTest(final AnyItemTestContext ctx)
    {
        return contextManager.typeInContext(typeFactory.anyItem());
    }

    @Override
    public TypeInContext visitChoiceItemType(final ChoiceItemTypeContext ctx)
    {
        final List<ItemTypeContext> itemTypes = ctx.itemType();
        if (itemTypes.size() == 1) {
            return ctx.itemType(0).accept(this);
        }
        final var choiceItemNames = itemTypes.stream().map(i -> i.getText()).collect(Collectors.toSet());
        if (choiceItemNames.size() != itemTypes.size()) {
            error(ctx, ErrorType.CHOICE_ITEM_TYPE__DUPLICATED, List.of());
        }
        final List<XQueryItemType> choiceItems = itemTypes.stream().map(i -> i.accept(this))
            .map(sequenceType -> sequenceType.type.itemType)
            .toList();
        return contextManager.typeInContext(typeFactory.choice(choiceItems));
    }

    @Override
    public TypeInContext visitTypeName(final TypeNameContext ctx)
    {
        final var result = switch (ctx.getText()) {
            case "number" -> number;
            case "string" -> string;
            case "boolean" -> boolean_;
            default -> {
                final var type = typeFactory.namedType(ctx.getText());
                if (type != null)
                    yield type;
                error(ctx, ErrorType.CHOICE_ITEM_TYPE__DUPLICATED, List.of(ctx.getText()));
                yield anyItem;
            }
        };
        return contextManager.typeInContext(result);
    }

    @Override
    public TypeInContext visitAnyKindTest(final AnyKindTestContext ctx)
    {
        return contextManager.typeInContext(typeFactory.anyNode());
    }

    @Override
    public TypeInContext visitElementTest(final ElementTestContext ctx)
    {
        final Set<String> elementNames = ctx.nameTestUnion().nameTest().stream().map(e -> e.getText())
            .collect(Collectors.toSet());
        return contextManager.typeInContext(typeFactory.element(elementNames));
    }

    @Override
    public TypeInContext visitFunctionType(final FunctionTypeContext ctx)
    {
        if (ctx.anyFunctionType() != null) {
            return contextManager.typeInContext(typeFactory.anyFunction());
        }
        final var func = ctx.typedFunctionType();
        final List<XQuerySequenceType> parameterTypes = func.typedFunctionParam().stream()
            .map(p -> visitSequenceType(p.sequenceType()).type)
            .collect(Collectors.toList());
        final var function =  typeFactory.function(visitSequenceType(func.sequenceType()).type, parameterTypes);
        return contextManager.typeInContext(function);
    }

    @Override
    public TypeInContext visitMapType(final MapTypeContext ctx)
    {
        if (ctx.anyMapType() != null) {
            return contextManager.typeInContext(typeFactory.anyMap());
        }
        final var map = ctx.typedMapType();
        final XQueryItemType keyType = map.itemType().accept(this).type.itemType;
        final TypeInContext valueType = visitSequenceType(map.sequenceType());
        return contextManager.typeInContext(typeFactory.map(keyType, valueType.type));
    }

    @Override
    public TypeInContext visitArrayType(final ArrayTypeContext ctx)
    {
        if (ctx.anyArrayType() != null) {
            return contextManager.typeInContext(typeFactory.anyArray());
        }
        final var array = ctx.typedArrayType();
        final var sequenceType = visitSequenceType(array.sequenceType());
        return contextManager.typeInContext(typeFactory.array(sequenceType.type));
    }

    @Override
    public TypeInContext visitRecordType(final RecordTypeContext ctx)
    {
        if (ctx.anyRecordType() != null) {
            return contextManager.typeInContext(typeFactory.anyMap());
        }
        final var record = ctx.typedRecordType();
        final var fieldDeclarations = record.fieldDeclaration();
        final Map<String, XQueryRecordField> fields = new HashMap<>(fieldDeclarations.size());
        for (final var field : fieldDeclarations) {
            final String fieldName = field.fieldName().getText();
            final var fieldType = visitSequenceType(field.sequenceType());
            final boolean isRequired = field.QUESTION_MARK() != null;
            final XQueryRecordField recordField = new XQueryRecordField(fieldType.type, isRequired);
            fields.put(fieldName, recordField);
        }
        if (record.extensibleFlag() == null) {
            return contextManager.typeInContext(typeFactory.extensibleRecord(fields));
        }
        return contextManager.typeInContext(typeFactory.record(fields));
    }

    @Override
    public TypeInContext visitEnumerationType(final EnumerationTypeContext ctx)
    {
        final Set<String> enumMembers = ctx.STRING().stream()
            .map(TerminalNode::getText)
            .map(s->s.substring(1, s.length()-1))
            .collect(Collectors.toSet());
        return contextManager.typeInContext(typeFactory.enum_(enumMembers));
    }

    @Override
    public TypeInContext visitCountClause(final CountClauseContext ctx)
    {
        final String countVariableName = ctx.varRef().qname().getText();
        contextManager.entypeVariable(countVariableName, number);
        return contextManager.typeInContext(number);
    }

    @Override
    public TypeInContext visitWhereClause(final WhereClauseContext ctx)
    {
        final var filteringExpression = ctx.exprSingle();
        final var filteringExpressionType = filteringExpression.accept(this);
        if (!filteringExpressionType.type.hasEffectiveBooleanValue()) {
            error(filteringExpression, ErrorType.FILTERING__EXPR_NOT_EBV, List.of(filteringExpressionType));
        }
        returnedOccurrence = addOptionality(returnedOccurrence);
        return null;
    }

    @Override
    public TypeInContext visitVarRef(final VarRefContext ctx)
    {
        final String variableName = ctx.qname().getText();
        final TypeInContext variableType = contextManager.getVariable(variableName);
        if (variableType == null) {
            error(ctx, ErrorType.VAR_REF__UNDECLARED, List.of(variableName));
            return contextManager.typeInContext(zeroOrMoreItems);
        } else {
            return variableType;
        }
    }

    private static final int[][] OCCURRENCE_MERGE_AUTOMATA = {
        // returnedOccurrence 0 (Zero)
        { 0, 0, 0, 0, 0 },
        // returnedOccurrence 1 (One)
        { 0, 1, 2, 3, 4 },
        // returnedOccurrence 2 (ZeroOrOne)
        { 0, 2, 2, 3, 3 },
        // returnedOccurrence 3 (ZeroOrMore)
        { 0, 3, 3, 3, 3 },
        // returnedOccurrence 4 (OneOrMore/Other)
        { 0, 4, 3, 3, 4 }
    };

    private int occurrence(final XQuerySequenceType type)
    {
        if (type.isZero)
            return 0;
        if (type.isOne)
            return 1;
        if (type.isZeroOrOne)
            return 2;
        if (type.isZeroOrMore)
            return 3;
        return 4;
    }

    private int mergeFLWOROccurrence(final XQuerySequenceType type)
    {
        final int typeOccurrence = occurrence(type);
        return OCCURRENCE_MERGE_AUTOMATA[returnedOccurrence][typeOccurrence];
    }

    private int arrayMergeFLWOROccurence() {
        if (returnedOccurrence == 0)
            return 0;
        return 3;
    }

    @Override
    public TypeInContext visitReturnClause(final ReturnClauseContext ctx)
    {
        final var type = ctx.exprSingle().accept(this);
        final var itemType = type.type.itemType;
        returnedOccurrence = mergeFLWOROccurrence(type.type);
        final var sequenceType = switch (returnedOccurrence) {
            case 0 -> emptySequence;
            case 1 -> typeFactory.one(itemType);
            case 2 -> typeFactory.zeroOrOne(itemType);
            case 3 -> typeFactory.zeroOrMore(itemType);
            default -> typeFactory.oneOrMore(itemType);
        };
        return contextManager.typeInContext(sequenceType);
    }

    @Override
    public TypeInContext visitWhileClause(final WhileClauseContext ctx)
    {
        final var filteringExpression = ctx.exprSingle();
        final var filteringExpressionType = filteringExpression.accept(this);
        if (!filteringExpressionType.type.hasEffectiveBooleanValue()) {
            error(filteringExpression, ErrorType.FILTERING__EXPR_NOT_EBV, List.of(filteringExpressionType));
        }
        returnedOccurrence = addOptionality(returnedOccurrence);
        return null;

    }

    private int addOptionality(final int occurence)
    {
        return switch (returnedOccurrence) {
            case 0 -> 0;
            case 1 -> 2;
            case 2 -> 2;
            default -> 3;
        };
    }

    @Override
    public TypeInContext visitLiteral(final LiteralContext ctx)
    {
        if (ctx.STRING() != null) {
            return handleString(ctx);
        }

        final var numeric = ctx.numericLiteral();
        if (numeric.IntegerLiteral() != null) {
            return handleNumber(numeric);
        }

        if (numeric.HexIntegerLiteral() != null) {
            final String raw = numeric.HexIntegerLiteral().getText();
            final String hex = raw.replace("_", "").substring(2);
            valueFactory.number(new BigDecimal(new java.math.BigInteger(hex, 16)));
            return contextManager.typeInContext(number);
        }

        if (numeric.BinaryIntegerLiteral() != null) {
            final String raw = numeric.BinaryIntegerLiteral().getText();
            final String binary = raw.replace("_", "").substring(2);
            valueFactory.number(new BigDecimal(new java.math.BigInteger(binary, 2)));
            return contextManager.typeInContext(number);
        }

        if (numeric.DecimalLiteral() != null) {
            final String cleaned = numeric.DecimalLiteral().getText().replace("_", "");
            valueFactory.number(new BigDecimal(cleaned));
            return contextManager.typeInContext(number);
        }

        if (numeric.DoubleLiteral() != null) {
            final String cleaned = numeric.DoubleLiteral().getText().replace("_", "");
            valueFactory.number(new BigDecimal(cleaned));
            return contextManager.typeInContext(number);
        }
        return null;
    }

    private TypeInContext handleNumber(final TerminalNode numeric) {
        final String value = numeric.getText().replace("_", "");
        valueFactory.number(new BigDecimal(value));
        return contextManager.typeInContext(number);
    }

    private TypeInContext handleNumber(final NumericLiteralContext numeric) {
        final String value = numeric.IntegerLiteral().getText().replace("_", "");
        valueFactory.number(new BigDecimal(value));
        return contextManager.typeInContext(number);
    }

    private TypeInContext handleString(final ParserRuleContext ctx) {
        final String content = processStringLiteral(ctx);
        return contextManager.typeInContext(typeFactory.enum_(Set.of(content)));
    }

    private String processStringLiteral(final ParserRuleContext ctx) {
        final String rawText = ctx.getText();
        final String content = unescapeString(ctx, rawText.substring(1, rawText.length() - 1));
        valueFactory.string(content);
        return content;
    }

    @Override
    public TypeInContext visitParenthesizedExpr(final ParenthesizedExprContext ctx)
    {
        // Empty parentheses mean an empty sequence '()'
        if (ctx.expr() == null) {
            valueFactory.sequence(List.of());
            return contextManager.typeInContext(emptySequence);
        }
        return ctx.expr().accept(this);
    }

    @Override
    public TypeInContext visitExpr(final ExprContext ctx)
    {
        // Only one expression
        // e.g. 13
        if (ctx.exprSingle().size() == 1) {
            return ctx.exprSingle(0).accept(this);
        }
        // More than one expression
        final var previousExpr = ctx.exprSingle(0);
        var previousExprType = visitExprSingle(previousExpr).type;
        final int size = ctx.exprSingle().size();
        for (int i = 1; i < size; i++) {
            final var exprSingle = ctx.exprSingle(i);
            final TypeInContext expressionType = exprSingle.accept(this);
            previousExprType = previousExprType.sequenceMerge(expressionType.type);
        }
        return contextManager.typeInContext(previousExprType);
    }

    private String unescapeString(final ParserRuleContext where, final String str)
    {
        final var charEscaper = new XQuerySemanticCharEscaper();
        final XQuerySemanticCharEscaperResult result = charEscaper.escapeWithDiagnostics(where, str);
        errors.addAll(result.errors());
        return result.unescaped();
    }

    private final NamespaceResolver namespaceResolver = new NamespaceResolver("fn");

    @Override
    public TypeInContext visitFunctionCall(final FunctionCallContext ctx)
    {
        final var savedArgs = saveVisitedArguments();
        final var savedKwargs = saveVisitedKeywordArguments();

        ctx.argumentList().accept(this);

        final TypeInContext callAnalysisResult = callFunction(
            ctx, ctx.functionName().getText(), visitedPositionalArguments, visitedKeywordArguments);

        visitedPositionalArguments = savedArgs;
        visitedKeywordArguments = savedKwargs;
        return callAnalysisResult;
    }

    private TypeInContext callFunction(
        final ParserRuleContext ctx,
        final String functionQname,
        final List<TypeInContext> args,
        final Map<String, TypeInContext> kwargs
    )
    {
        final String fullName = functionQname;
        final var resolution = namespaceResolver.resolve(fullName);
        final String namespace = resolution.namespace();
        final String functionName = resolution.name();

        final AnalysisResult callAnalysisResult = functionManager.call(
            ctx, namespace, functionName, visitedPositionalArguments,
            visitedKeywordArguments, context, contextManager.currentContext());
        errors.addAll(callAnalysisResult.errors());
        return callAnalysisResult.result();
    }

    @Override
    public TypeInContext visitQuantifiedExpr(final QuantifiedExprContext ctx) {
        final List<QuantifierBindingContext> quantifierBindings = ctx.quantifierBinding();

        final List<String> variableNames = quantifierBindings.stream()
                .map(binding -> binding.varNameAndType().varRef().qname().getText())
                .toList();

        final List<XQuerySequenceType> coercedTypes = quantifierBindings.stream()
                .map(binding -> {
                    final TypeDeclarationContext typeDeclaration = binding.varNameAndType().typeDeclaration();
                    return typeDeclaration != null? typeDeclaration.accept(this).type : null;
                })
                .toList();

        final List<XQuerySequenceType> variableTypes = quantifierBindings.stream()
                .map(binding -> binding.exprSingle().accept(this).type)
                .toList();

        final ExprSingleContext criterionNode = ctx.exprSingle();

        for (int i = 0; i < variableNames.size(); i++) {
            final var assignedType = variableTypes.get(i);
            final var desiredType = coercedTypes.get(i);
            if (desiredType !=null) {
                if (assignedType.coerceableTo(desiredType) == RelativeCoercability.NEVER){
                    error(
                        ctx.quantifierBinding(i).varNameAndType(),
                        ErrorType.VAR_DECL_WITH_COERSION__INVALID,
                        List.of(assignedType, desiredType));
                }
                contextManager.entypeVariable(variableNames.get(i), desiredType);
                continue;
            }
            contextManager.entypeVariable(variableNames.get(i), assignedType);
        }

        final XQuerySequenceType queriedType = criterionNode.accept(this).type;
        if (!queriedType.hasEffectiveBooleanValue()) {
            error(criterionNode, ErrorType.QUANTIFIED__CRITERION_NON_EBV, List.of(queriedType));
        }

        return contextManager.typeInContext(boolean_);
    }

    @Override
    public TypeInContext visitOrExpr(final OrExprContext ctx)
    {
        if (ctx.OR().isEmpty()) {
            return ctx.andExpr(0).accept(this);
        }
        final var orCount = ctx.OR().size();
        for (int i = 0; i <= orCount; i++) {
            final var visitedType = ctx.andExpr(i).accept(this);
            if (!visitedType.type.hasEffectiveBooleanValue()) {
                error(ctx.andExpr(i), ErrorType.OR__NON_EBV, List.of(visitedType));
            }
        }
        final var andBool = contextManager.typeInContext(boolean_);
        // contextManager.currentScope().imply(andBool);
        return andBool;
    }

    @Override
    public TypeInContext visitRangeExpr(final RangeExprContext ctx)
    {
        if (ctx.TO() == null) {
            return ctx.additiveExpr(0).accept(this);
        }
        final var fromValue = ctx.additiveExpr(0).accept(this);
        final var toValue = ctx.additiveExpr(1).accept(this);
        final boolean validFrom = fromValue.type.isSubtypeOf(optionalNumber);
        final boolean validTo = toValue.type.isSubtypeOf(optionalNumber);
        if (!validFrom && !validTo) {
            error(ctx.additiveExpr(0), ErrorType.RANGE__INVALID_BOTH, List.of(fromValue, toValue));
        } else if (!validFrom) {
            error(ctx.additiveExpr(0), ErrorType.RANGE__INVALID_FROM, List.of(fromValue));
        } else if (!validTo) {
            error(ctx.additiveExpr(1), ErrorType.RANGE__INVALID_TO, List.of(toValue));
        }
        return contextManager.typeInContext(anyNumbers);
    }

    @Override
    public TypeInContext visitPathExpr(final PathExprContext ctx)
    {
        final boolean pathExpressionFromRoot = ctx.SLASH() != null;
        if (pathExpressionFromRoot) {
            final var savedAxis = saveAxis();
            contextTypeMustBeAnyNodes(ctx);
            currentAxis = XQueryAxis.CHILD;
            final var resultingNodeSequence = ctx.relativePathExpr().accept(this);
            currentAxis = savedAxis;
            return resultingNodeSequence;
        }
        final boolean useDescendantOrSelfAxis = ctx.SLASHES() != null;
        if (useDescendantOrSelfAxis) {
            final var savedAxis = saveAxis();
            contextTypeMustBeAnyNodes(ctx);
            currentAxis = XQueryAxis.DESCENDANT_OR_SELF;
            final var resultingNodeSequence = ctx.relativePathExpr().accept(this);
            currentAxis = savedAxis;
            return resultingNodeSequence;
        }
        return ctx.relativePathExpr().accept(this);
    }

    @Override
    public TypeInContext visitNodeTest(final NodeTestContext ctx)
    {
        final XQuerySequenceType nodeType = context.getType().type;
        if (!nodeType.isSubtypeOf(anyNodes)) {
            error(ctx, ErrorType.NODE_TEST__LHS_INVALID, List.of(nodeType));
        }
        PathOperatorResult result;
        if (ctx.wildcard() != null) {
            result = pathOperator.pathOperator(nodeType, currentAxis, null, grammarAnalysisResult);
        } else {
            final Set<String> names = ctx.pathNameTestUnion().qname().stream()
                .map(t->t.getText())
                .collect(Collectors.toSet());
            result = pathOperator.pathOperator(nodeType, currentAxis, names, grammarAnalysisResult);
        }
        if (result.isEmptyTarget()) {
            warn(ctx, WarningType.PATH_OPERATOR__EMPTY_SEQUENCE, List.of());
        }
        if (!result.invalidNames().isEmpty()) {
            final String joinedNames = result.invalidNames().stream().collect(Collectors.joining(", "));
            error(ctx, ErrorType.NODE_TEST__UNRECOGNIZED_RULE_NAMES, List.of(joinedNames));
        }
        if (!result.duplicateNames().isEmpty()) {
            final String joinedNames = result.invalidNames().stream().collect(Collectors.joining(", "));
            warn(ctx, WarningType.NODE_TEST__DUPLICATED_NAME, List.of(joinedNames));
        }
        return contextManager.typeInContext(result.result());
    }

    /**
     * Makes sure that context type is subtype of node()*
     * If it is not, error is recorded and the value is corrected to node()*
     * @param ctx rule where the error potentially has occured
     */
    private void contextTypeMustBeAnyNodes(final PathExprContext ctx)
    {
        final XQuerySequenceType contexttype = context.getType().type;
        if (contexttype == null) {
            error(ctx, ErrorType.PATH_EXPR__CONTEXT_TYPE_ABSENT, List.of());
            context.setType(contextManager.typeInContext(anyNodes));
        } else if (!contexttype.isSubtypeOf(anyNodes)) {
            error(ctx, ErrorType.PATH_EXPR__CONTEXT_NOT_NODES, List.of(contexttype));
            context.setType(contextManager.typeInContext(anyNodes));
        }
    }

    @Override
    public TypeInContext visitRelativePathExpr(final RelativePathExprContext ctx)
    {
        if (ctx.pathOperator().isEmpty()) {
            return ctx.stepExpr(0).accept(this);
        }
        final var savedContext = saveContext();
        context.setType(savedContext.getType());
        context.setPositionType(savedContext.getPositionType());
        context.setSizeType(savedContext.getSizeType());
        TypeInContext result = visitStepExpr(ctx.stepExpr(0));
        context.setType(result);
        final var operationCount = ctx.pathOperator().size();
        for (int i = 1; i <= operationCount; i++) {
            currentAxis = (ctx.pathOperator(i-1).SLASH() != null)
                ? XQueryAxis.DESCENDANT_OR_SELF
                : XQueryAxis.CHILD;
            result = visitStepExpr(ctx.stepExpr(i));
            context.setType(result);
        }
        context = savedContext;
        return result;
    }




    @Override
    public TypeInContext visitStepExpr(final StepExprContext ctx)
    {
        if (ctx.postfixExpr() != null)
            return ctx.postfixExpr().accept(this);
        return visitAxisStep(ctx.axisStep());
    }

    @Override
    public TypeInContext visitAxisStep(final AxisStepContext ctx)
    {
        XQuerySequenceType stepResult = null;
        if (ctx.reverseStep() != null)
            stepResult = visitReverseStep(ctx.reverseStep()).type;
        else if (ctx.forwardStep() != null)
            stepResult = visitForwardStep(ctx.forwardStep()).type;
        if (ctx.predicateList().predicate().isEmpty()) {
            return contextManager.typeInContext(stepResult);
        }
        final var savedArgs = saveVisitedArguments();
        final var savedContext = saveContext();
        context.setType(contextManager.typeInContext(stepResult.iteratorType()));
        context.setPositionType(contextManager.typeInContext(number));
        context.setSizeType(contextManager.typeInContext(number));
        for (final var predicate : ctx.predicateList().predicate()) {
            predicate.accept(this);
        }
        visitedPositionalArguments = savedArgs;
        context = savedContext;
        return contextManager.typeInContext(stepResult);
    }

    private XQueryVisitingSemanticContext saveContext() {
        final var saved = context;
        context = new XQueryVisitingSemanticContext();
        return saved;
    }


    @Override
    public TypeInContext visitFilterExpr(final FilterExprContext ctx)
    {
        final XQuerySequenceType expr = ctx.postfixExpr().accept(this).type;
        final var savedContext = saveContext();
        context.setType(contextManager.typeInContext(expr.iteratorType()));
        final var filtered = visitPredicate(ctx.predicate());
        context = savedContext;
        return filtered;
    }

    @Override
    public TypeInContext visitPredicate(final PredicateContext ctx)
    {
        final var contextType = context.getType();
        final var predicateExpression = ctx.expr().accept(this);
        final var savedContext = saveContext();
        context.setType(contextManager.typeInContext(savedContext.getType().iteratorType()));
        context.setPositionType(contextManager.typeInContext(number));
        context.setSizeType(contextManager.typeInContext(number));
        if (predicateExpression.type.isSubtypeOf(emptySequence))
            return contextManager.typeInContext(emptySequence);
        if (predicateExpression.type.isSubtypeOf(typeFactory.zeroOrOne(typeFactory.itemNumber()))) {
            final var item = contextType.type.itemType;
            final var deducedType = typeFactory.zeroOrOne(item);
            return contextManager.typeInContext(deducedType);
        }
        if (predicateExpression.type.isSubtypeOf(typeFactory.zeroOrMore(typeFactory.itemNumber()))) {
            final var item = contextType.type.itemType;
            final var deducedType = typeFactory.zeroOrMore(item);
            final TypeInContext deducedInContext = contextManager.typeInContext(deducedType);
            context.setType(deducedInContext);
            return deducedInContext;
        }
        if (!predicateExpression.type.hasEffectiveBooleanValue()) {
            error(ctx.expr(), ErrorType.PREDICATE__NON_EBV, List.of(predicateExpression));
        }
        context = savedContext;
        return contextManager.typeInContext(contextType.type.addOptionality());
    }

    @Override
    public TypeInContext visitDynamicFunctionCall(final DynamicFunctionCallContext ctx) {
        final var savedArgs = saveVisitedArguments();
        final var savedContext = saveContext();
        context.setType(savedContext.getType());
        context.setPositionType(contextManager.typeInContext(number));
        context.setSizeType(contextManager.typeInContext(number));
        final XQuerySequenceType value = ctx.postfixExpr().accept(this).type;
        final boolean isCallable = value.isSubtypeOf(typeFactory.anyFunction());
        if (!isCallable) {
            error(ctx.postfixExpr(), ErrorType.PREDICATE__NON_EBV, List.of(value));
        }
        ctx.positionalArgumentList().accept(this);
        visitedPositionalArguments = savedArgs;


        context = savedContext;

        if (isCallable)
            return contextManager.typeInContext(value.itemType.returnedType);
        else
            return contextManager.typeInContext(typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }



    @Override
    public TypeInContext visitLookupExpr(final LookupExprContext ctx) {
        final var targetType = ctx.postfixExpr().accept(this);
        final TypeInContext keySpecifierType = getKeySpecifier(ctx);
        final LookupContext lookup = ctx.lookup();
        final var lookupType = typecheckLookup(ctx, lookup, lookup.keySpecifier(), targetType, keySpecifierType);
        return contextManager.typeInContext(lookupType);
    }


    private XQuerySequenceType typecheckLookup(
        final ParserRuleContext ctx,
        final LookupContext lookup,
        final KeySpecifierContext keySpecifier,
        final TypeInContext targetType,
        final TypeInContext keySpecifierType)
    {
        if (targetType.type.isZero) {
            warn(ctx, WarningType.LOOKUP__TARGET_EMPTY, List.of());
            return emptySequence;
        }
        final boolean isWildcard = keySpecifierType == null;
        if (!isWildcard && keySpecifierType.type.isZero) {
            warn(ctx, WarningType.LOOKUP__KEY_EMPTY, List.of());
            return emptySequence;
        }

        if (!targetType.isSubtypeOf(anyArrayOrMap)) {
            error(ctx, ErrorType.LOOKUP__INVALID_TARGET, List.of(targetType));
            return zeroOrMoreItems;
        }

        switch (targetType.type.itemType.type) {
            case ARRAY:
                final XQuerySequenceType targetItemType = targetType.type.itemType.arrayMemberType;
                if (targetItemType == null)
                    return zeroOrMoreItems;
                final XQuerySequenceType result = targetItemType.sequenceMerge(targetItemType).addOptionality();
                if (isWildcard) {
                    return result;
                }
                if (!keySpecifierType.type.itemtypeIsSubtypeOf(typeFactory.zeroOrMore(typeFactory.itemNumber())))
                {
                    error(lookup, ErrorType.LOOKUP__ARRAY_INVALID_KEY, List.of(targetType));
                }
                return result;
            case ANY_ARRAY:
                if (isWildcard) {
                    return zeroOrMoreItems;
                }
                if (!keySpecifierType.isSubtypeOf(typeFactory.zeroOrMore(typeFactory.itemNumber())))
                {
                    error(lookup, ErrorType.LOOKUP__ARRAY_INVALID_KEY, List.of(targetType, keySpecifierType));
                }
                return zeroOrMoreItems;
            case MAP:
                return getMapLookuptype(ctx, lookup, keySpecifier, targetType, keySpecifierType, isWildcard);
            case EXTENSIBLE_RECORD:
                return getExtensibleRecordLookupType(ctx, lookup, keySpecifier,targetType, keySpecifierType, isWildcard);
            case RECORD:
                return getRecordLookupType(ctx, lookup, keySpecifier,targetType, keySpecifierType, isWildcard);
            case ANY_MAP:
                return zeroOrMoreItems;
            default:
                return getAnyArrayOrMapLookupType(lookup, isWildcard, targetType, keySpecifierType);
        }
    }


    XQuerySequenceType getAnyArrayOrMapLookupType(
        final LookupContext ctx,
        final boolean isWildcard,
        final TypeInContext targetType,
        final TypeInContext keySpecifierType)
    {
        if (isWildcard) {
            return null;
        }
        final XQueryItemType targetItemType = targetType.type.itemType;
        final Collection<XQueryItemType> choiceItemTypes = targetItemType.itemTypes;
        XQueryItemType targetKeyItemType = null;
        XQuerySequenceType resultingType = null;
        for (final var itemType : choiceItemTypes) {
            if (resultingType == null) {
                if (!isWildcard)
                    resultingType = switch(keySpecifierType.type.occurence) {
                        case ONE -> typeFactory.zeroOrOne(itemType);
                        default -> typeFactory.zeroOrMore(itemType);
                    };
                else {
                    resultingType = typeFactory.zeroOrMore(itemType);
                }
                continue;
            }

            switch (itemType.type) {
                case ARRAY:
                    resultingType = resultingType.alternativeMerge(itemType.arrayMemberType);
                    targetKeyItemType = targetItemType.alternativeMerge(typeFactory.itemNumber());
                    break;
                case MAP:
                    resultingType = resultingType.alternativeMerge(itemType.mapValueType);
                    targetKeyItemType = targetItemType.alternativeMerge(itemType.mapKeyType);
                    break;
                default:
                    resultingType = zeroOrMoreItems;
                    targetKeyItemType = typeFactory.itemAnyItem();
            }
        }
        resultingType = resultingType.addOptionality();
        if (isWildcard) {
            return resultingType;
        }
        final XQueryItemType numberOrKey = targetKeyItemType.alternativeMerge(typeFactory.itemNumber());

        final XQuerySequenceType expectedKeyItemtype = typeFactory.zeroOrMore(numberOrKey);
        if (!keySpecifierType.itemtypeIsSubtypeOf(expectedKeyItemtype)) {
            error(ctx, ErrorType.LOOKUP__ARRAY_OR_MAP_INVALID_KEY, List.of(targetType, expectedKeyItemtype));
        }
        return resultingType;
    }

    @Override
    public TypeInContext visitUnaryLookup(final UnaryLookupContext ctx) {
        final var contextType = context.getType();
        final var keySpecifierType = visitKeySpecifier(ctx.lookup().keySpecifier());
        final var lookupType =  typecheckLookup(ctx, ctx.lookup(), ctx.lookup().keySpecifier(), contextType, keySpecifierType);
        return contextManager.typeInContext(lookupType);
    }



    private  XQuerySequenceType getMapLookuptype(
            final ParserRuleContext target,
            final LookupContext lookup,
            final KeySpecifierContext keySpecifier,
            final TypeInContext targetType,
            final TypeInContext keySpecifierType,
            final boolean isWildcard)
    {
        final XQueryItemType targetKeyItemType = targetType.type.itemType.mapKeyType;
        final XQuerySequenceType targetValueType = targetType.type.itemType.mapValueType;
        final XQueryItemType targetValueItemtype = targetValueType.itemType;
        if (isWildcard) {
            return typeFactory.zeroOrMore(targetValueItemtype);
        }
        final XQuerySequenceType result = switch(keySpecifierType.type.occurence) {
                case ONE -> typeFactory.zeroOrOne(targetValueItemtype);
                default -> typeFactory.zeroOrMore(targetValueItemtype);
            };
        final XQuerySequenceType expectedKeyItemtype = typeFactory.zeroOrMore(targetKeyItemType);
        if (!keySpecifierType.isSubtypeOf(expectedKeyItemtype)) {
            error(lookup, ErrorType.LOOKUP__MAP_INVALID_KEY, List.of(targetType, expectedKeyItemtype));
        }
        if (targetValueItemtype.type == XQueryTypes.RECORD) {
            return result;
        }
        return result.addOptionality();
    }

    private XQuerySequenceType getRecordLookupType(
        final ParserRuleContext target,
        final LookupContext lookup,
        final KeySpecifierContext keySpecifier,
        final TypeInContext targetType,
        final TypeInContext keySpecifierType,
        final boolean isWildcard)
    {
        final XQueryItemType targetKeyItemType = typeFactory.itemString();
        final Map<String, XQueryRecordField> recordFields = targetType.type.itemType.recordFields;
        if (recordFields.isEmpty()) {
            warn(target, WarningType.LOOKUP__EMPTY_RECORD, List.of());
            return emptySequence;
        }
        final XQuerySequenceType mergedRecordFieldTypes = recordFields
            .values()
            .stream()
            .map(t -> t.isRequired()? t.type() : t.type().addOptionality())
            .reduce((x, y)->x.alternativeMerge(y))
            .get();
        if (isWildcard) {
            return mergedRecordFieldTypes;
        }
        if (!keySpecifierType.isSubtypeOf(typeFactory.zeroOrMore(typeFactory.itemString()))) {
            error(keySpecifier, ErrorType.LOOKUP__INVALID_RECORD_KEY_TYPE, List.of(targetType, keySpecifierType));
            return zeroOrMoreItems;
        }
        final var string = keySpecifier.STRING();
        if (string != null) {
            final String key = processStringLiteral(keySpecifier);
            final var valueType = recordFields.get(key);
            if (valueType == null) {
                error(keySpecifier, ErrorType.LOOKUP__INVALID_RECORD_KEY_NAME, List.of(key, targetType));
                return zeroOrMoreItems;
            }
            return valueType.type();
        }
        final XQuerySequenceType expectedKeyItemtype = typeFactory.zeroOrMore(targetKeyItemType);
        if (!keySpecifierType.isSubtypeOf(expectedKeyItemtype)) {
            error(lookup, ErrorType.LOOKUP__INVALID_RECORD_KEY_TYPE, List.of(targetType, expectedKeyItemtype));
        }
        if (keySpecifierType.type.itemType.type == XQueryTypes.ENUM) {
            final var members = keySpecifierType.type.itemType.enumMembers;
            final var firstField = members.stream().findFirst().get();
            final var firstRecordField = recordFields.get(firstField);
            XQuerySequenceType merged = firstRecordField.isRequired() ? firstRecordField.type() : firstRecordField.type().addOptionality();
            for (final var member : members) {
                if (member.equals(firstField))
                    continue;
                final var recordField = recordFields.get(member);
                if (recordField == null) {
                    warn(lookup, WarningType.LOOKUP__IMPOSSIBLE_RECORD_FIELD, List.of(member));
                    return zeroOrMoreItems;
                }
                if (recordField.isRequired()) {
                    merged = merged.sequenceMerge(recordField.type());
                } else {
                    merged = merged.sequenceMerge(recordField.type().addOptionality());
                }
            }
            return merged;
        }
        return mergedRecordFieldTypes.addOptionality();
    }

    private XQuerySequenceType getExtensibleRecordLookupType(
        final ParserRuleContext ctx,
        final LookupContext lookup,
        final KeySpecifierContext keySpecifier,
        final TypeInContext targetType,
        final TypeInContext keySpecifierType,
        final boolean isWildcard)
    {
        final XQueryItemType targetKeyItemType = typeFactory.itemString();
        final Map<String, XQueryRecordField> recordFields = targetType.type.itemType.recordFields;
        if (recordFields.isEmpty()) {
            warn(ctx, WarningType.LOOKUP__RETURNS_ALWAYS_EMPTY, List.of());
            return emptySequence;
        }
        final XQuerySequenceType mergedRecordFieldTypes = recordFields
            .values()
            .stream()
            .map(t -> t.isRequired()? t.type() : t.type().addOptionality())
            .reduce((x, y)->x.alternativeMerge(y))
            .get();
        if (isWildcard) {
            return mergedRecordFieldTypes;
        }
        if (!keySpecifierType.isSubtypeOf(typeFactory.zeroOrMore(typeFactory.itemString()))) {
            error(ctx, ErrorType.LOOKUP__INVALID_EXTENDED_RECORD_KEY_TYPE, List.of());
            return zeroOrMoreItems;
        }
        final var string = keySpecifier.STRING();
        if (string != null) {
            final String key = processStringLiteral(keySpecifier);
            final var valueType = recordFields.get(key);
            if (valueType == null) {
                return zeroOrMoreItems;
            }
            return valueType.type();
        }
        final XQuerySequenceType expectedKeyItemtype = typeFactory.zeroOrMore(targetKeyItemType);
        if (!keySpecifierType.isSubtypeOf(expectedKeyItemtype)) {
            error(lookup, ErrorType.LOOKUP__INVALID_EXTENDED_RECORD_KEY_TYPE, List.of(targetType, expectedKeyItemtype));
        }
        if (keySpecifierType.type.itemType.type == XQueryTypes.ENUM) {
            final var members = keySpecifierType.type.itemType.enumMembers;
            final var firstField = members.stream().findFirst().get();
            final var firstRecordField = recordFields.get(firstField);
            XQuerySequenceType merged = firstRecordField.isRequired() ? firstRecordField.type() : firstRecordField.type().addOptionality();
            for (final var member : members) {
                if (member.equals(firstField))
                    continue;
                final var recordField = recordFields.get(member);
                if (recordField == null)  {
                    return zeroOrMoreItems;
                }
                if (recordField.isRequired()) {
                    merged = merged.alternativeMerge(recordField.type());
                } else {
                    merged = merged.alternativeMerge(recordField.type().addOptionality());
                }
            }
            return merged;
        }
        return mergedRecordFieldTypes.addOptionality();
    }

    TypeInContext getKeySpecifier(final LookupExprContext ctx) {
        final KeySpecifierContext keySpecifier = ctx.lookup().keySpecifier();
        if (keySpecifier.qname() != null) {
            final XQuerySequenceType enum_ = typeFactory.enum_(Set.of(keySpecifier.qname().getText()));
            return contextManager.typeInContext(enum_);
        }
        if (keySpecifier.STRING() != null ) {
            return handleString(keySpecifier);
        }
        if (keySpecifier.IntegerLiteral() != null) {
            return handleNumber(keySpecifier.IntegerLiteral());
        }
        return keySpecifier.accept(this);
    }


    @Override
    public TypeInContext visitContextValueRef(final ContextValueRefContext ctx)
    {
        return context.getType();
    }


    @Override
    public TypeInContext visitForwardAxis(final ForwardAxisContext ctx) {
        if (ctx.CHILD() != null)
            currentAxis = XQueryAxis.CHILD;
        if (ctx.DESCENDANT() != null)
            currentAxis = XQueryAxis.DESCENDANT;
        if (ctx.SELF() != null)
            currentAxis = XQueryAxis.SELF;
        if (ctx.DESCENDANT_OR_SELF() != null)
            currentAxis = XQueryAxis.DESCENDANT_OR_SELF;
        if (ctx.FOLLOWING_SIBLING() != null)
            currentAxis = XQueryAxis.FOLLOWING_SIBLING;
        if (ctx.FOLLOWING() != null)
            currentAxis = XQueryAxis.FOLLOWING;
        if (ctx.FOLLOWING_SIBLING_OR_SELF() != null)
            currentAxis = XQueryAxis.FOLLOWING_SIBLING_OR_SELF;
        if (ctx.FOLLOWING_OR_SELF() != null)
            currentAxis = XQueryAxis.FOLLOWING_OR_SELF;
        return null;
    }

    @Override
    public TypeInContext visitReverseAxis(final ReverseAxisContext ctx) {
        if (ctx.PARENT() != null)
            currentAxis = XQueryAxis.PARENT;
        if (ctx.ANCESTOR() != null)
            currentAxis = XQueryAxis.ANCESTOR;
        if (ctx.PRECEDING_SIBLING_OR_SELF() != null)
            currentAxis = XQueryAxis.PRECEDING_SIBLING_OR_SELF;
        if (ctx.PRECEDING_OR_SELF() != null)
            currentAxis = XQueryAxis.PRECEDING_OR_SELF;
        if (ctx.PRECEDING_SIBLING() != null)
            currentAxis = XQueryAxis.PRECEDING_SIBLING;
        if (ctx.PRECEDING() != null)
            currentAxis = XQueryAxis.PRECEDING;
        if (ctx.ANCESTOR_OR_SELF() != null)
            currentAxis = XQueryAxis.ANCESTOR_OR_SELF;
        return null;
    }



    @Override
    public TypeInContext visitForwardStep(final ForwardStepContext ctx)
    {
        if (ctx.forwardAxis() != null) {
            ctx.forwardAxis().accept(this);
        } else {
            if (currentAxis == null) {
                currentAxis = XQueryAxis.CHILD;
            }
        }
        return visitNodeTest(ctx.nodeTest());
    }

    @Override
    public TypeInContext visitReverseStep(final ReverseStepContext ctx)
    {
        if (ctx.abbrevReverseStep() != null) {
            return ctx.abbrevReverseStep().accept(this);
        }
        ctx.reverseAxis().accept(this);
        return visitNodeTest(ctx.nodeTest());
    }




    @Override
    public TypeInContext visitStringConcatExpr(final StringConcatExprContext ctx)
    {
        if (ctx.CONCATENATION().isEmpty()) {
            return visitRangeExpr(ctx.rangeExpr(0));
        }
        for (int i = 0; i < ctx.rangeExpr().size(); i++) {
            final var visitedType = visitRangeExpr(ctx.rangeExpr(i)).type;
            if (!visitedType.isSubtypeOf(zeroOrMoreItems)) {
                error(ctx.rangeExpr(i), ErrorType.CONCAT__INVALID, List.of());
            }
        }
        return contextManager.typeInContext(string);
    }

    @Override
    public TypeInContext visitSimpleMapExpr(final SimpleMapExprContext ctx)
    {
        if (ctx.EXCLAMATION_MARK().isEmpty())
            return visitPathExpr(ctx.pathExpr(0));
        final TypeInContext firstExpressionType = visitPathExpr(ctx.pathExpr(0));
        final XQuerySequenceType iterator = firstExpressionType.iteratorType();
        final var savedContext = saveContext();
        context.setType(contextManager.typeInContext(iterator));
        context.setPositionType(contextManager.typeInContext(number));
        context.setSizeType(contextManager.typeInContext(number));
        TypeInContext result = firstExpressionType;
        final var theRest = ctx.pathExpr().subList(1, ctx.pathExpr().size());
        for (final var mappedExpression : theRest) {
            final TypeInContext type = visitPathExpr(mappedExpression);
            result = contextManager.typeInContext(result.type.mapping(type.type));
            context.setType(contextManager.typeInContext(result.iteratorType()));
        }
        context = savedContext;
        return result;
    }

    @Override
    public TypeInContext visitInstanceofExpr(final InstanceofExprContext ctx)
    {
        final TypeInContext expression = visitTreatExpr(ctx.treatExpr());
        if (ctx.INSTANCE() == null) {
            return expression;
        }
        final var testedType = ctx.sequenceType().accept(this);
        if (expression.isSubtypeOf(testedType)) {
            // UNNECESSARY_INSTANCE_OF__ALWAYS_TRUE
            warn(ctx, WarningType.INSTANCE_OF__ALWAYS_TRUE, List.of());
        }
        // TODO: add warning on impossible instance of tests
        final var bool = contextManager.typeInContext(this.boolean_);
        contextManager.currentScope().imply(bool, new InstanceOfSuccessImplication(bool, true, expression, testedType));
        return bool;
    }

    @Override
    public TypeInContext visitTreatExpr(final TreatExprContext ctx)
    {
        final TypeInContext expression = visitCastableExpr(ctx.castableExpr());
        if (ctx.TREAT() == null) {
            return expression;
        }
        final var relevantType = visitSequenceType(ctx.sequenceType());
        if (!relevantType.isSubtypeOf(expression)
            && !expression.isSubtypeOf(relevantType))
        {
            warn(ctx, WarningType.TREAT__UNLIKELY, List.of(expression, relevantType));
        }
        return relevantType;
    }

    private final SequencetypeAtomization atomizer;



    private final SequencetypeCastable castability;
    private final XQuerySequenceType anyNodes;

    @Override
    public TypeInContext visitCastableExpr(final CastableExprContext ctx) {
        if (ctx.CASTABLE() == null)
            return this.visitCastExpr(ctx.castExpr());
        final var type = this.visitCastTarget(ctx.castTarget());
        final var tested = this.visitCastExpr(ctx.castExpr());
        final boolean emptyAllowed = ctx.castTarget().QUESTION_MARK() != null;
        final IsCastableResult result = castability.isCastable(type.type, tested.type, emptyAllowed);
        verifyCastability(ctx, type, tested.type, result.castability(), result);
        return contextManager.typeInContext(result.resultingType());
    }

    private <T> void verifyCastability(
            final ParserRuleContext ctx,
            final T type,
            final XQuerySequenceType tested,
            final Castability castability,
            final IsCastableResult result)
    {
        // TODO: add atomized info
        switch(castability) {
            case POSSIBLE: { break; }
            case ALWAYS_POSSIBLE_CASTING_TO_SAME: {
                warn(ctx, WarningType.CAST__SELFCAST, List.of(tested, type));
            }
            case ALWAYS_POSSIBLE_CASTING_TO_SUBTYPE: {
                warn(ctx, WarningType.CAST__SUBTYPE_CAST, List.of(tested, type));
            }
            case ALWAYS_POSSIBLE_CASTING_TO_TARGET:{
                warn(ctx, WarningType.CAST__TARGET_CAST, List.of(tested, type));
            }
            case ALWAYS_POSSIBLE_MANY_ITEMTYPES: {
                warn(ctx, WarningType.CAST__POSSIBLE_MANY_ITEMTYPES, List.of(tested, type));
                final XQueryItemType[] wrongItemtypes = result.wrongItemtypes();
                final int itemtypeCount = wrongItemtypes.length;
                for (int i = 0; i < itemtypeCount; i++) {
                    verifyCastability(ctx, wrongItemtypes[i], tested, result.problems()[i], null);
                }
                break;
            }
            case ALWAYS_POSSIBLE_MANY_SEQUENCETYPES: {
                warn(ctx, WarningType.CAST__POSSIBLE_MANY_SEQUENCETYPES, List.of(tested, type));
                break;
            }
            case IMPOSSIBLE: {
                error(ctx, ErrorType.CAST__IMPOSSIBLE, List.of(tested, type));
                break;
            }
            case TESTED_EXPRESSION_CAN_BE_EMPTY_SEQUENCE_WITHOUT_FLAG: {
                error(ctx, ErrorType.CAST__EMPTY_WITHOUT_FLAG, List.of(tested));
                break;
            }
            case TESTED_EXPRESSION_IS_EMPTY_SEQUENCE: {
                error(ctx, ErrorType.CAST__EMPTY_SEQUENCE, List.of());
                break;
            }
            case TESTED_EXPRESSION_IS_ZERO_OR_MORE: {
                error(ctx, ErrorType.CAST__ZERO_OR_MORE, List.of(tested));
                break;
            }
            case WRONG_TARGET_TYPE: {
                error(ctx, ErrorType.CAST__WRONG_TARGET_TYPE, List.of(type));
                break;
            }
        }
    }


    @Override
    public TypeInContext visitCastExpr(final CastExprContext ctx) {
        if (ctx.CAST() == null)
            return this.visitPipelineExpr(ctx.pipelineExpr());
        final var type = this.visitCastTarget(ctx.castTarget());
        final var tested = this.visitPipelineExpr(ctx.pipelineExpr());
        final boolean emptyAllowed = ctx.castTarget().QUESTION_MARK() != null;
        final IsCastableResult result = castability.isCastable(type.type, tested.type, emptyAllowed);
        verifyCastability(ctx, type.type, tested.type, result.castability(), result);
        return contextManager.typeInContext(result.resultingType());
    }


    @Override
    public TypeInContext visitCastTarget(final CastTargetContext ctx) {
        var type = super.visitCastTarget(ctx);
        if (ctx.QUESTION_MARK() != null)
            type = contextManager.typeInContext(type.type.addOptionality());
        return type;
    }

    @Override
    public TypeInContext visitNamedFunctionRef(final NamedFunctionRefContext ctx)
    {
        final int arity = Integer.parseInt(ctx.IntegerLiteral().getText());
        final ResolvedName resolvedName = namespaceResolver.resolve(ctx.qname().getText());
        final var analysis = functionManager.getFunctionReference(
            ctx, resolvedName.namespace(), resolvedName.name(), arity, contextManager.currentContext());
        errors.addAll(analysis.errors());
        return analysis.result();
    }

    @Override
    public TypeInContext visitSquareArrayConstructor(final SquareArrayConstructorContext ctx)
    {
        if (ctx.exprSingle().isEmpty()) {
            return contextManager.typeInContext(anyArray);
        }
        final XQuerySequenceType arrayType = ctx.exprSingle().stream()
            .map(expr -> expr.accept(this).type)
            .reduce((t1, t2) -> t1.alternativeMerge(t2))
            .get();
        return contextManager.typeInContext(typeFactory.array(arrayType));
    }

    @Override
    public TypeInContext visitCurlyArrayConstructor(final CurlyArrayConstructorContext ctx)
    {
        final var expressions = ctx.enclosedExpr().expr();
        if (expressions == null) {
            return contextManager.typeInContext(anyArray);
        }

        final XQuerySequenceType arrayType = expressions.exprSingle().stream()
            .map(expr -> expr.accept(this).type)
            .reduce((t1, t2) -> t1.alternativeMerge(t2))
            .get();
        return contextManager.typeInContext(typeFactory.array(arrayType));

    }

    @Override
    public TypeInContext visitPipelineExpr(final PipelineExprContext ctx)
    {
        if (ctx.PIPE_ARROW().isEmpty())
            return ctx.arrowExpr(0).accept(this);
        final var saved = saveContext();
        final int size = ctx.arrowExpr().size();
        TypeInContext contextType = visitArrowExpr(ctx.arrowExpr(0));
        for (var i = 1; i < size; i++) {
            final var contextualizedExpr = ctx.arrowExpr(i);
            context.setType(contextType);
            context.setPositionType(null);
            context.setSizeType(null);
            contextType = contextualizedExpr.accept(this);
        }
        context = saved;
        return contextType;
    }

    @Override
    public TypeInContext visitTryCatchExpr(final TryCatchExprContext ctx)
    {
        final var savedContext = saveContext();
        final XQueryItemType errorType = typeFactory.itemError();
        final var testedExprType = ctx.tryClause().enclosedExpr().accept(this);
        final var alternativeCatches = ctx.catchClause().stream()
            .map(c -> {
                XQuerySequenceType choicedErrors;
                if (c.pureNameTestUnion() != null) {
                    final var foundErrors = new ArrayList<XQueryItemType>();
                    for (final var error : c.pureNameTestUnion().nameTest()) {
                        String errorText = error.getText();
                        var typeRef = typeFactory.itemNamedType(errorText);
                        if (typeRef == null) {
                            error(c, ErrorType.TRY_CATCH__UNKNOWN_ERROR, List.of(errorText));
                            typeRef = errorType;
                        }
                        if (!typeRef.itemtypeIsSubtypeOf(errorType)) {
                            error(c, ErrorType.TRY_CATCH__NON_ERROR, List.of(typeRef, errorText));
                            typeRef = errorType;
                        }
                        foundErrors.add(typeRef);
                    }
                    choicedErrors = typeFactory.choice(foundErrors);
                } else {
                    choicedErrors = typeFactory.error();
                }
                context.setType(contextManager.typeInContext(choicedErrors));
                context.setPositionType(null);
                context.setSizeType(null);
                contextManager.enterScope();
                contextManager.entypeVariable("err:code", string);
                contextManager.entypeVariable("err:description", optionalString);
                contextManager.entypeVariable("err:value", typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
                contextManager.entypeVariable("err:module", typeFactory.zeroOrOne(typeFactory.itemString()));
                contextManager.entypeVariable("err:line-number", typeFactory.zeroOrOne(typeFactory.itemNumber()));
                contextManager.entypeVariable("err:column-number", typeFactory.zeroOrOne(typeFactory.itemNumber()));
                contextManager.entypeVariable("err:stack-trace", typeFactory.zeroOrOne(typeFactory.itemString()));
                contextManager.entypeVariable("err:additional", typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
                contextManager.entypeVariable("err:map", typeFactory.anyMap());

                final var visited = c.enclosedExpr().accept(this);
                contextManager.leaveScope();
                return visited;
            });

        final Set<String> errors = new HashSet<>();
        // Marking duplicate error type names as errors
        for (final var catchClause : ctx.catchClause()) {
            if (catchClause.pureNameTestUnion() != null) {
                for (final var qname : catchClause.pureNameTestUnion().nameTest()) {
                    final String name = qname.getText();
                    if (errors.contains(name)) {
                        error(qname, ErrorType.TRY_CATCH__DUPLICATED_ERROR, List.of(name));
                    } else {
                        errors.add(name);
                    }

                }
            }
        }

        // Marking multiple catch * {} as errors
        int wildcardCount = 0;
        for (final var catchClause : ctx.catchClause()) {
            if (catchClause.wildcard() != null && wildcardCount++ > 1) {
                error(
                    catchClause,
                    ErrorType.TRY_CATCH__UNNECESSARY_ERROR_BECAUSE_OF_WILDCARD,
                    List.of());
            }
        }

        final FinallyClauseContext finallyClause = ctx.finallyClause();
        if (finallyClause != null) {
            context = new XQueryVisitingSemanticContext();
            context.setType(contextManager.typeInContext(typeFactory.anyNode()));
            final XQuerySequenceType finallyType = visitEnclosedExpr(finallyClause.enclosedExpr()).type;
            if (!finallyType.isSubtypeOf(emptySequence)) {
                error(finallyClause, ErrorType.TRY_CATCH__FINALLY_NON_EMPTY, List.of(finallyType));
            }
        }
        context = savedContext;
        final XQuerySequenceType mergedAlternativeCatches = alternativeCatches
            .map(x->x.type)
            .reduce(XQuerySequenceType::alternativeMerge)
            .get();
        final var merged = testedExprType.type.alternativeMerge(mergedAlternativeCatches);
        return contextManager.typeInContext(merged);
    }

    @SuppressWarnings("unchecked")
    @Override
    public TypeInContext visitMapConstructor(final MapConstructorContext ctx)
    {
        final var entries = ctx.mapConstructorEntry();
        if (entries.isEmpty()) {
            // return contextManager.typeInContext(typeFactory.record(Map.of()));
            return contextManager.typeInContext(typeFactory.anyMap());
        }
        final XQueryItemType keyType = entries.stream()
            .map(e -> e.mapKeyExpr().accept(this).type.itemType)
            .reduce((t1, t2) -> t1.alternativeMerge(t2))
            .get();
        if (keyType.type == XQueryTypes.ENUM) {
            final var enum_ = keyType;
            final var enumMembers = enum_.enumMembers;
            final List<Entry<String, XQueryRecordField>> recordEntries = new ArrayList<>(enumMembers.size());
            int i = 0;
            for (final var enumMember : enumMembers) {
                final var valueType = entries.get(i).mapValueExpr().accept(this);
                recordEntries.add(Map.entry(enumMember, new XQueryRecordField(valueType.type, true)));
                i++;
            }
            return contextManager.typeInContext(typeFactory.record(Map.ofEntries(recordEntries.toArray(Entry[]::new))));
        }
        // TODO: refine
        final XQuerySequenceType valueType = entries.stream()
            .map(e -> visitMapValueExpr(e.mapValueExpr()).type)
            .reduce((t1, t2) -> t1.alternativeMerge(t2))
            .get();
        return contextManager.typeInContext(typeFactory.map(keyType, valueType));
    }


    @Override
    public TypeInContext visitArrowExpr(final ArrowExprContext ctx) {
        final boolean notSequenceArrow = ctx.sequenceArrowTarget().isEmpty();
        final boolean notMappingArrow = ctx.mappingArrowTarget().isEmpty();
        if (notSequenceArrow && notMappingArrow) {
            return ctx.unaryExpr().accept(this);
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
    public TypeInContext visitArrowTarget(final ArrowTargetContext ctx) {
        if (ctx.functionCall() != null) {
            ctx.functionCall().argumentList().accept(this);
            final String functionQname = ctx.functionCall().functionName().getText();
            return callFunction(
                ctx.functionCall(),
                functionQname,
                visitedPositionalArguments,
                visitedKeywordArguments);
        }
        return ctx.restrictedDynamicCall().accept(this);
    }


    @Override
    public TypeInContext visitMappingArrowTarget(final MappingArrowTargetContext ctx) {
        final TypeInContext mappedSequence = visitedPositionalArguments
            .get(visitedPositionalArguments.size() - 1) ;


        if (mappedSequence.type.isZero) {
            return mappedSequence;
        }
        final XQuerySequenceType iterator = mappedSequence.iteratorType();
        visitedPositionalArguments = new ArrayList<>();
        visitedPositionalArguments.add(contextManager.typeInContext(iterator));
        final var call = ctx.arrowTarget().accept(this);
        return switch(mappedSequence.type.occurence) {
            case ONE -> call;
            case ONE_OR_MORE -> contextManager.typeInContext(call.type.sequenceMerge(call.type));
            case ZERO_OR_MORE -> contextManager.typeInContext(call.type.sequenceMerge(call.type).addOptionality());
            case ZERO_OR_ONE -> contextManager.typeInContext(call.type.addOptionality());
            case ZERO -> {
                error(ctx, ErrorType.MAPPING__EMPTY_SEQUENCE, List.of());
                yield contextManager.typeInContext(emptySequence);
            }
        };
    }

    @Override
    public TypeInContext visitRestrictedDynamicCall(final RestrictedDynamicCallContext ctx) {
        final var value = ctx.children.get(0).accept(this);
        final boolean isCallable = value.isSubtypeOf(typeFactory.anyFunction());
        if (!isCallable) {
            error(ctx, ErrorType.RESTRICTED_DYNAMIC_CALL__NON_FUNCTION, List.of(value));
        }
        ctx.positionalArgumentList().accept(this);

        final List<XQuerySequenceType> args = visitedPositionalArguments.stream().map(a->a.type).toList();
        final var expectedFunction = typeFactory.itemFunction(zeroOrMoreItems, args);
        if (!value.type.itemType.itemtypeIsSubtypeOf(expectedFunction))
        {
            error(ctx, ErrorType.RESTRICTED_DYNAMIC_CALL__INVALID_FUNCTION, List.of(expectedFunction, value));
        }

        if (isCallable)
            return contextManager.typeInContext(value.type.itemType.returnedType);
        else
            return contextManager.typeInContext(zeroOrMoreItems);
    }






    @Override
    public TypeInContext visitAndExpr(final AndExprContext ctx)
    {
        if (ctx.AND().isEmpty()) {
            return ctx.comparisonExpr(0).accept(this);
        }
        final var operatorCount = ctx.AND().size();
        final List<TypeInContext> andExprEbvs = new ArrayList<>(operatorCount+1);
        contextManager.enterScope();
        for (int i = 0; i <= operatorCount; i++) {
            final var visitedType = ctx.comparisonExpr(i).accept(this);
            if (!visitedType.type.hasEffectiveBooleanValue()) {
                error(ctx.comparisonExpr(i), ErrorType.AND__NON_EBV, List.of(visitedType));
            } else {
                final var ebv = contextManager.typeInContext(typeFactory.boolean_());
                contextManager.currentScope().imply(ebv, new EffectiveBooleanValueTrue(ebv, visitedType, typeFactory));
                contextManager.currentScope().assume(ebv, new Assumption(ebv, true));
                andExprEbvs.add(ebv);
            }
        }
        contextManager.leaveScope();
        final var andExpr = contextManager.typeInContext(boolean_);
        contextManager.currentScope().imply(andExpr, new AndTrueImplication(andExpr, andExprEbvs));
        return andExpr;
    }

    @Override
    public TypeInContext visitAdditiveExpr(final AdditiveExprContext ctx)
    {
        if (ctx.additiveOperator().isEmpty()) {
            return ctx.multiplicativeExpr(0).accept(this);
        }
        for (final var operandExpr : ctx.multiplicativeExpr()) {
            final var operand = operandExpr.accept(this);
            if (!operand.isSubtypeOf(number)) {
                error(operandExpr, ErrorType.ADDITIVE__INVALID, List.of(operand));
            }
        }
        return contextManager.typeInContext(number);
    }

    @Override
    public TypeInContext visitComparisonExpr(final ComparisonExprContext ctx)
    {
        if (ctx.generalComp() != null) {
            return handleGeneralComparison(ctx);
        }
        if (ctx.valueComp() != null) {
            return handleValueComparison(ctx);
        }
        if (ctx.nodeComp() != null) {
            return handleNodeComp(ctx);
        }
        return ctx.otherwiseExpr(0).accept(this);
    }

    private TypeInContext handleGeneralComparison(final ComparisonExprContext ctx)
    {
        final var firstOtherwise = visitOtherwiseExpr(ctx.otherwiseExpr(0));
        final var secondOtherwise = visitOtherwiseExpr(ctx.otherwiseExpr(1));
        final var leftHandSide = atomizer.atomize(firstOtherwise.type);
        final var rightHandSide = atomizer.atomize(secondOtherwise.type);
        if (!leftHandSide.isSubtypeOf(rightHandSide) && !rightHandSide.isSubtypeOf(leftHandSide)) {
            error(ctx, ErrorType.GENERAL_COMP__INVALID, List.of(leftHandSide, rightHandSide));
        }
        return contextManager.typeInContext(typeFactory.boolean_());
    }

    private TypeInContext handleValueComparison(final ComparisonExprContext ctx)
    {
        final var leftHandSide = ctx.otherwiseExpr(0).accept(this);
        final var rightHandSide = ctx.otherwiseExpr(1).accept(this);
        final var optionalItem = typeFactory.zeroOrOne(typeFactory.itemAnyItem());
        final var optionalBoolean = typeFactory.zeroOrOne(typeFactory.itemBoolean());
        final boolean invalidLeft = !leftHandSide.isSubtypeOf(optionalItem);
        final boolean invalidRight = !rightHandSide.isSubtypeOf(optionalItem);
        if (invalidLeft && invalidRight) {
            error(ctx.otherwiseExpr(0), ErrorType.VALUE_COMP__BOTH_INVALID, List.of(leftHandSide, rightHandSide));
        }
        else if (invalidLeft) {
            error(ctx.otherwiseExpr(0), ErrorType.VALUE_COMP__LHS_INVALID, List.of(leftHandSide));
        }
        else if (invalidRight) {
            error(ctx.otherwiseExpr(1), ErrorType.VALUE_COMP__RHS_INVALID, List.of(rightHandSide));
        }
        else if (!leftHandSide.type.isValueComparableWith(rightHandSide.type)) {
            error(ctx, ErrorType.VALUE_COMP__INCOMPARABLE, List.of(leftHandSide, rightHandSide));
        }
        if (leftHandSide.isSubtypeOf(typeFactory.anyItem())
            && rightHandSide.isSubtypeOf(typeFactory.anyItem()))
        {
            return contextManager.typeInContext(typeFactory.boolean_());
        }
        return contextManager.typeInContext(optionalBoolean);
    }

    private TypeInContext handleNodeComp(final ComparisonExprContext ctx)
    {
        final var anyNode = typeFactory.zeroOrOne(typeFactory.itemAnyNode());
        final var optionalBoolean = typeFactory.zeroOrOne(typeFactory.itemBoolean());
        final var visitedLeft = visitOtherwiseExpr(ctx.otherwiseExpr(0));
        final var visitedRight = visitOtherwiseExpr(ctx.otherwiseExpr(1));
        boolean validLhs = visitedLeft.isSubtypeOf(anyNode);
        boolean validRhs = visitedRight.isSubtypeOf(anyNode);
        if (!validLhs && !validRhs) {
            error(ctx.otherwiseExpr(0), ErrorType.NODE_COMP__BOTH_INVALID, List.of(visitedLeft, visitedRight));
        }
        else if (!validLhs) {
            error(ctx.otherwiseExpr(0), ErrorType.NODE_COMP__LHS_INVALID, List.of(visitedLeft));
        }
        else if (!validRhs) {
            error(ctx.otherwiseExpr(1), ErrorType.NODE_COMP__RHS_INVALID, List.of(visitedRight));
        }
        return contextManager.typeInContext(optionalBoolean);

    }

    @Override
    public TypeInContext visitMultiplicativeExpr(final MultiplicativeExprContext ctx)
    {
        if (ctx.multiplicativeOperator().isEmpty()) {
            return ctx.unionExpr(0).accept(this);
        }
        for (final var expr : ctx.unionExpr()) {
            final var visitedType = expr.accept(this);
            if (!visitedType.isSubtypeOf(number)) {
                error(ctx, ErrorType.MUL__INVALID, List.of(visitedType));
            }
        }
        return contextManager.typeInContext(number);
    }

    @Override
    public TypeInContext visitOtherwiseExpr(final OtherwiseExprContext ctx)
    {
        if (ctx.OTHERWISE().isEmpty())
            return ctx.stringConcatExpr(0).accept(this);
        final int length = ctx.stringConcatExpr().size();
        XQuerySequenceType merged = visitStringConcatExpr(ctx.stringConcatExpr(0)).type;
        if (merged.isOne || merged.isOneOrMore) {
            warn(ctx.stringConcatExpr(0), WarningType.OTHERWISE__IMPOSSIBLE, List.of(merged));
        }
        for (int i = 1; i < length; i++) {
            final var expr = ctx.stringConcatExpr(i);
            final XQuerySequenceType exprType = visitStringConcatExpr(expr).type;
            if (exprType.isOne || exprType.isOneOrMore) {
                warn(expr, WarningType.OTHERWISE__IMPOSSIBLE, List.of(exprType));
            }
            merged = exprType.alternativeMerge(merged);
        }
        return contextManager.typeInContext(merged);
    }

    @Override
    public TypeInContext visitUnionExpr(final UnionExprContext ctx)
    {
        if (ctx.unionOperator().isEmpty()) {
            return ctx.intersectExpr(0).accept(this);
        }
        final var zeroOrMoreNodes = typeFactory.zeroOrMore(typeFactory.itemAnyNode());
        var expressionNode = ctx.intersectExpr(0);
        var expressionType = expressionNode.accept(this);
        if (!expressionType.isSubtypeOf(zeroOrMoreNodes)) {
            error(expressionNode, ErrorType.UNION__INVALID, List.of(expressionType));
            expressionType = contextManager.typeInContext(zeroOrMoreNodes);
        }
        final var unionCount = ctx.unionOperator().size();
        for (int i = 1; i <= unionCount; i++) {
            expressionNode = ctx.intersectExpr(i);
            final var visitedType = expressionNode.accept(this);
            if (!visitedType.isSubtypeOf(zeroOrMoreNodes)) {
                error(expressionNode, ErrorType.UNION__INVALID, List.of(expressionType));
                expressionType = contextManager.typeInContext(zeroOrMoreNodes);
            } else {
                expressionType = contextManager.typeInContext(expressionType.type.unionMerge(visitedType.type));
            }
        }
        return expressionType;
    }

    @Override
    public TypeInContext visitIntersectExpr(final IntersectExprContext ctx)
    {
        if (ctx.exceptOrIntersect().isEmpty()) {
            return ctx.instanceofExpr(0).accept(this);
        }
        var expressionType = ctx.instanceofExpr(0).accept(this);
        final var zeroOrMoreNodes = typeFactory.zeroOrMore(typeFactory.itemAnyNode());
        if (!expressionType.isSubtypeOf(zeroOrMoreNodes)) {
            error(ctx.instanceofExpr(0), ErrorType.INTERSECT_OR_EXCEPT__INVALID, List.of(expressionType));
            expressionType = contextManager.typeInContext(zeroOrMoreNodes);
        }
        final var operatorCount = ctx.exceptOrIntersect().size();
        for (int i = 1; i < operatorCount; i++) {
            final var instanceofExpr = ctx.instanceofExpr(i);
            final var visitedType = instanceofExpr.accept(this);
            if (!visitedType.isSubtypeOf(zeroOrMoreNodes)) {
                error(ctx.instanceofExpr(i), ErrorType.INTERSECT_OR_EXCEPT__INVALID, List.of(expressionType));
                expressionType = contextManager.typeInContext(zeroOrMoreNodes);
            } else {
                if (ctx.exceptOrIntersect(i).EXCEPT() != null)
                    expressionType = contextManager.typeInContext(expressionType.type.exceptionMerge(visitedType.type));
                else
                    expressionType = contextManager.typeInContext(expressionType.type.intersectionMerge(visitedType.type));
            }
        }
        return expressionType;
    }

    @Override
    public TypeInContext visitUnaryExpr(final UnaryExprContext ctx)
    {
        if (ctx.MINUS() == null && ctx.PLUS() == null) {
            return ctx.simpleMapExpr().accept(this);
        }
        final var type = ctx.simpleMapExpr().accept(this);
        if (!type.isSubtypeOf(number)) {
            error(ctx, ErrorType.UNARY__INVALID, List.of(type));
        }
        return contextManager.typeInContext(number);
    }

    @Override
    public TypeInContext visitSwitchExpr(final SwitchExprContext ctx) {
        final SwitchComparandContext switchComparand = ctx.switchComparand();

        final TypeInContext comparand = visitExpr(switchComparand.switchedExpr);
        final SwitchCasesContext switchCases = ctx.switchCases();
        final boolean notBraced = switchCases != null;
        final var defaultExpr = notBraced
            ? switchCases.defaultExpr
            : ctx.bracedSwitchCases().switchCases().defaultExpr;
        final var clauses = notBraced
            ? switchCases.switchCaseClause()
            : ctx.bracedSwitchCases().switchCases().switchCaseClause();

        XQuerySequenceType merged = null;
        for (final var clause : clauses) {
            final var operandType = clause.switchCaseOperand().stream()
                .map(this::visit)
                .map(x->x.type)
                .reduce(XQuerySequenceType::alternativeMerge)
                .get();
            if (!operandType.isSubtypeOf(comparand.type)) {
                error(clause, ErrorType.SWITCH__INVALID_CASE, List.of(operandType, comparand));
            }
            final var returned = clause.exprSingle().accept(this);
            if (merged == null) {
                merged = returned.type;
                continue;
            }
            merged = merged.alternativeMerge(returned.type);
        }
        final var merg =  merged.alternativeMerge(visitExprSingle(defaultExpr).type);
        return contextManager.typeInContext(merg);
    }

    @Override
    public TypeInContext visitArgument(final ArgumentContext ctx)
    {
        final var value = super.visitArgument(ctx);
        visitedPositionalArguments.add(value);
        return value;
    }

    @Override
    public TypeInContext visitKeywordArgument(final KeywordArgumentContext ctx)
    {
        final ExprSingleContext keywordAssignedTypeExpr = ctx.argument().exprSingle();
        if (keywordAssignedTypeExpr != null) {
            final var keywordType = keywordAssignedTypeExpr.accept(this);
            final String keyword = ctx.qname().getText();
            visitedKeywordArguments.put(keyword, keywordType);
        }
        // TODO: add placeholder
        return null;

    }

    private List<TypeInContext> saveVisitedArguments()
    {
        final var saved = visitedPositionalArguments;
        visitedPositionalArguments = new ArrayList<>();
        return saved;
    }

    private Map<String, TypeInContext> saveVisitedKeywordArguments()
    {
        final var saved = visitedKeywordArguments;
        visitedKeywordArguments = new HashMap<>();
        return saved;
    }


    void error(final ParserRuleContext where, final ErrorType errorType, final List<Object> data)
    {
        final Token start = where.getStart();
        final Token stop = where.getStop();
        final DiagnosticError error = DiagnosticError.of(start, stop, errorType, data);
        errors.add(error);
    }

    void warn(final ParserRuleContext where, final WarningType type, final List<Object> data)
    {
        final Token start = where.getStart();
        final Token stop = where.getStop();
        warnings.add(DiagnosticWarning.of(start, stop, type, data));
    }



    record LineEndCharPosEnd(int lineEnd, int charPosEnd) {
    }

    LineEndCharPosEnd getLineEndCharPosEnd(final Token end)
    {
        final var string = end.getText();
        final int length = string.length();

        int newlineCount = 0;
        int lastNewlineIndex = 0;
        for (int i = 0; i < length; i++) {
            if (string.codePointAt(i) == '\n') {
                newlineCount++;
                lastNewlineIndex = i;
            }
        }

        final int lineEnd = end.getLine() + newlineCount;
        final int charPositionInLineEnd = newlineCount == 0 ? end.getCharPositionInLine() + length
            : length - lastNewlineIndex;
        return new LineEndCharPosEnd(lineEnd, charPositionInLineEnd);
    }


    @Override
    public TypeInContext visitIfExpr(final IfExprContext ctx)
    {
        final var conditionType = visitExpr(ctx.expr());
        TypeInContext ebv;
        final XQueryTypes ebvtype = conditionType.type.effectiveBooleanValueType();
        if (ebvtype == null) { // no effective boolean value
            ebv = contextManager.currentScope().typeInContext(typeFactory.boolean_());
            error(ctx, ErrorType.IF__CONDITION_NON_EBV, List.of(conditionType));
        } else {
            ebv = contextManager.resolveEffectiveBooleanValue(conditionType, ebvtype);
        }
        TypeInContext trueType = null;
        TypeInContext falseType = null;
        contextManager.currentScope().imply(ebv, new EffectiveBooleanValueTrue(ebv, conditionType, typeFactory));
        if (ctx.bracedAction() != null) {
            contextManager.enterScope();
            contextManager.currentScope().assume(ebv, new Assumption(ebv, true));
            trueType = visitEnclosedExpr(ctx.bracedAction().enclosedExpr());
            contextManager.leaveScope();

            contextManager.enterScope();
            contextManager.currentScope().assume(ebv, new Assumption(ebv, false));
            falseType = contextManager.typeInContext(emptySequence);
            contextManager.leaveScope();
        } else {
            contextManager.enterScope();
            contextManager.currentScope().assume(ebv, new Assumption(ebv, true));
            trueType = ctx.unbracedActions().exprSingle(0).accept(this);
            contextManager.leaveScope();
            contextManager.enterScope();
            contextManager.currentScope().assume(ebv, new Assumption(ebv, false));
            falseType = ctx.unbracedActions().exprSingle(1).accept(this);
            contextManager.leaveScope();
        }
        return contextManager.typeInContext(trueType.type.alternativeMerge(falseType.type));
    }


    @Override
    public TypeInContext visitStringConstructor(final StringConstructorContext ctx)
    {
        return contextManager.typeInContext(typeFactory.string());
    }

    @Override
    public TypeInContext visitStringInterpolation(final StringInterpolationContext ctx)
    {
        return contextManager.typeInContext(typeFactory.string());
    }


    @Override
    public TypeInContext visitInlineFunctionExpr(final InlineFunctionExprContext ctx)
    {
        // Is a focus function?
        if (ctx.functionSignature() == null) {
            // TODO: implement focus function
            return contextManager.typeInContext(typeFactory.anyFunction());
        }
        final Set<String> argumentNames = new HashSet<>();
        final List<XQuerySequenceType> args = new ArrayList<>();
        final var functionSignature = ctx.functionSignature();
        final var returnTypeDeclaration = functionSignature.typeDeclaration();
        contextManager.enterScope();
        for (final var parameter : functionSignature.paramList().varNameAndType()) {
            final String parameterName = parameter.varRef().qname().getText();
            final TypeDeclarationContext typeDeclaration = parameter.typeDeclaration();
            final XQuerySequenceType parameterType = typeDeclaration != null
                ? typeDeclaration.accept(this).type
                : zeroOrMoreItems;
            if (argumentNames.contains(parameterName)) {
                error(parameter, ErrorType.FUNCTION__DUPLICATED_ARG_NAME, List.of(parameterName));
            }
            argumentNames.add(parameterName);
            args.add(parameterType);
            contextManager.entypeVariable(parameterName, parameterType);
        }
        final var inlineType = ctx.functionBody().enclosedExpr().accept(this);
        var returnedType = (returnTypeDeclaration != null) ? returnTypeDeclaration.accept(this)
            : inlineType;
        if (returnTypeDeclaration != null) {
            returnedType = returnTypeDeclaration.accept(this);
            if (!inlineType.isSubtypeOf(returnedType)) {
                error(ctx.functionBody(), ErrorType.FUNCTION__INVALID_BODY_TYPE, List.of(inlineType, returnedType));
            }
        } else {
            returnedType = inlineType;
        }

        contextManager.leaveScope();
        return contextManager.typeInContext(typeFactory.function(returnedType.type, args));
    }

    @Override
    public TypeInContext visitEnclosedExpr(final EnclosedExprContext ctx)
    {
        if (ctx.expr() != null) {
            return visitExpr(ctx.expr());
        }
        return contextManager.typeInContext(emptySequence);
    }

    @Override
    public TypeInContext visitFunctionDecl(final FunctionDeclContext ctx)
    {
        final String qname = ctx.qname().getText();
        final ResolvedName resolved = namespaceResolver.resolve(qname);
        int i = 0;
        final var args = new ArrayList<ArgumentSpecification>();
        contextManager.enterContext();
        if (ctx.paramListWithDefaults() != null) {
            final Set<String> argNames = new HashSet<>();
            final var params = ctx.paramListWithDefaults().paramWithDefault();
            for (final var param : params) {
                final var defaultValue = param.exprSingle();
                if (defaultValue != null)
                    break;
                final var argName = getArgName(param);
                XQuerySequenceType paramType = null;
                if (param.varNameAndType().typeDeclaration() == null) {
                    paramType = zeroOrMoreItems;
                } else {
                    paramType = param.varNameAndType().typeDeclaration().accept(this).type;
                  }
                final var argDecl = new ArgumentSpecification(argName, paramType, null);
                final boolean added = argNames.add(argName);
                if (!added) {
                    error(param, ErrorType.FUNCTION__DUPLICATED_ARG_NAME, List.of(argName));
                }
                args.add(argDecl);
                i++;
            }
            for (final ParamWithDefaultContext param : params.subList(i, params.size())) {
                final var argName = getArgName(param);
                final var paramType = param.varNameAndType().typeDeclaration().accept(this);
                final var defaultValue = param.exprSingle();
                if (defaultValue == null) {
                    error(param, ErrorType.FUNCTION__POSITIONAL_ARG_BEFORE_DEFAULT, List.of());
                    continue;
                } else {
                    final var dvt = defaultValue.accept(this);
                    if (!dvt.isSubtypeOf(paramType)) {
                        error(defaultValue, ErrorType.FUNCTION__INVALID_DEFAULT, List.of(dvt, paramType));
                    }
                }
                final var argDecl = new ArgumentSpecification(argName, paramType.type, defaultValue);
                final boolean added = argNames.add(argName);
                if (!added) {
                    error(param.getParent(), ErrorType.FUNCTION__DUPLICATED_ARG_NAME, List.of(argName));
                }
                args.add(argDecl);
            }
            for (final var arg : args) {
                contextManager.entypeVariable(arg.name(), arg.type());
            }
        }

        final TypeInContext returned = ctx.typeDeclaration() != null? visitTypeDeclaration(ctx.typeDeclaration()) : contextManager.typeInContext(zeroOrMoreItems);
        functionManager.register(resolved.namespace(), resolved.name(), args, returned.type);
        final FunctionBodyContext functionBody = ctx.functionBody();
        if (functionBody != null) {
            final var bodyType = visitEnclosedExpr(functionBody.enclosedExpr());
            if (!bodyType.isSubtypeOf(returned)) {
                error(functionBody, ErrorType.FUNCTION__INVALID_RETURNED_TYPE, List.of(bodyType, returned));
            }
        }

        contextManager.leaveContext();
        return null;
    }

    private String getArgName(final ParamWithDefaultContext param)
    {
        final var paramName = param.varNameAndType().varRef().qname();
        if (paramName.namespace().size() != 0)
        {
            error(param, ErrorType.PARAM__NAMESPACE, List.of(paramName.anyName().getText()));
        }
        return paramName.anyName().getText();
    }


    @Override
    public TypeInContext visitVarDecl(final VarDeclContext ctx)
    {
        final var name = ctx.varNameAndType().varRef().qname().getText();
        final var declaredType = visitTypeDeclaration(ctx.varNameAndType().typeDeclaration());
        if (ctx.EXTERNAL() == null) {
            final var assignedType = visitVarValue(ctx.varValue()).type;
            if (assignedType.coerceableTo(declaredType.type) == RelativeCoercability.NEVER) {
                error(ctx, ErrorType.VAR_DECL__UNCOERSABLE, List.of(name, declaredType, assignedType));
            }
        }
        contextManager.entypeVariable(name, declaredType);
        return null;
    }

    @Override
    public TypeInContext visitItemTypeDecl(final ItemTypeDeclContext ctx)
    {
        final var typeName = ctx.qname().getText();
        final var itemType = ctx.itemType().accept(this).type.itemType;
        final var status = typeFactory.registerItemNamedType(typeName, itemType);
        switch (status) {
            case ALREADY_REGISTERED_DIFFERENT: {
                var expected = typeFactory.namedType(typeName);
                error(ctx, ErrorType.ITEM_DECLARATION__ALREADY_REGISTERED_DIFFERENT, List.of(typeName, expected));
                break;
            }
            case ALREADY_REGISTERED_SAME: {
                error(ctx, ErrorType.ITEM_DECLARATION__ALREADY_REGISTERED_SAME, List.of(typeName));
                break;
            }
            case OK:
                break;
        }
        return null;

    }


    @Override
    public TypeInContext visitNamedRecordTypeDecl(final NamedRecordTypeDeclContext ctx)
    {
        final var typeName = ctx.qname().getText();
        final ResolvedName qName = namespaceResolver.resolve(typeName);
        final List<ExtendedFieldDeclarationContext> extendedFieldDeclaration = ctx.extendedFieldDeclaration();
        final int size = extendedFieldDeclaration.size();
        final Map<String, XQueryRecordField> fields = new HashMap<>(size);
        final List<ArgumentSpecification> mandatoryArgs = new ArrayList<>(size);
        final List<ArgumentSpecification> optionalArgs = new ArrayList<>(size);
        for (final ExtendedFieldDeclarationContext field : extendedFieldDeclaration) {
            final var fieldName = field.fieldDeclaration().fieldName().getText();
            final var fieldTypeCtx = field.fieldDeclaration().sequenceType();
            XQuerySequenceType fieldType = zeroOrMoreItems;
            if (fieldTypeCtx != null) {
                fieldType = visitSequenceType(fieldTypeCtx).type;
            }
            final boolean isRequired = field.fieldDeclaration().QUESTION_MARK() == null;
            final ExprSingleContext defaultExpr = field.exprSingle();
            fields.put(fieldName, new XQueryRecordField(fieldType, isRequired));
            if (isRequired) {
                if (defaultExpr == null) {
                    mandatoryArgs.add(new ArgumentSpecification(fieldName, fieldType, null));
                }
                else {
                    optionalArgs.add(new ArgumentSpecification(fieldName, fieldType, defaultExpr));
                }
            } else {
                optionalArgs
                    .add(new ArgumentSpecification(fieldName, fieldType, XQuerySemanticFunctionManager.EMPTY_SEQUENCE));
            }
        }
        mandatoryArgs.addAll(optionalArgs);
        final var itemRecordType = ctx.extensibleFlag() == null
            ? typeFactory.itemRecord(fields)
            : typeFactory.itemExtensibleRecord(fields);
        functionManager.register(qName.namespace(), qName.name(), mandatoryArgs, typeFactory.one(itemRecordType));
        final var status = typeFactory.registerItemNamedType(typeName, itemRecordType);
        switch (status) {
            case ALREADY_REGISTERED_DIFFERENT: {
                XQuerySequenceType expr = typeFactory.namedType(typeName);
                error(ctx, ErrorType.RECORD_DECLARATION__ALREADY_REGISTERED_DIFFERENT, List.of(typeName, expr));
                break;
            }
            case ALREADY_REGISTERED_SAME: {
                error(ctx, ErrorType.RECORD_DECLARATION__ALREADY_REGISTERED_SAME, List.of(typeName));
                break;
            }
            case OK:{}
                break;
        }
        return null;
    }



    XQueryAxis currentAxis;
    private final XQuerySequenceType zeroOrOneItem;

    private XQueryAxis saveAxis() {
        final var saved = currentAxis;
        currentAxis = null;
        return saved;
    }


    // @Override
    // public TypeInContext visitGrammarImport(GrammarImportContext ctx)
    // {
    //     var strings = ctx.STRING();
    //     var grammarAnalysisResult = analyzeGrammar(ctx, strings.get(0).getText());
    //     this.grammarAnalysisResult = grammarAnalysisResult;
    //     return null;
    // }




    @Override
    public TypeInContext visitPathModuleImport(final PathModuleImportContext ctx) {
        final String pathQuery = stringContents(ctx.STRING());
        final var result = moduleManager.pathModuleImport(pathQuery);
        return handleModuleImport(ctx, result);
    }


    @Override
    public TypeInContext visitDefaultPathModuleImport(final DefaultPathModuleImportContext ctx) {
        final String pathQuery = ctx.qname().getText().replace(":", "/");
        final var result = moduleManager.defaultPathModuleImport(pathQuery);
        return handleModuleImport(ctx, result);
    }

    @Override
    public TypeInContext visitNamespaceModuleImport(final NamespaceModuleImportContext ctx) {
        final String pathQuery = stringContents(ctx.STRING());
        final var result = moduleManager.namespaceModuleImport(pathQuery);
        return handleModuleImport(ctx, result);
    }


    private TypeInContext handleModuleImport(final ParserRuleContext ctx, final ImportResult result) {
        switch (result.status()) {
            case NO_PATH_FOUND: {
                final StringBuilder message = getNoPathMessageFromImport(result);
                error(ctx, ErrorType.IMPORT_MODULE__NO_PATH_FOUND, List.of(message));
                return null;

            }
            case MANY_VALID_PATHS: {
                warn(ctx, WarningType.IMPORT_MODULE__MANY_VALID_PATHS, List.of(result.validPaths()));
                return result.tree().accept(this);
            }
            case OK: {
                return result.tree().accept(this);
            }
        }
        return null;
    }

    private StringBuilder getNoPathMessageFromImport(final ImportResult result) {
        final StringBuilder message = new StringBuilder("No path was found");
        int i = 0;
        for (final var p : result.resolvedPaths()) {
            switch(result.resolvingStatuses().get(i)) {
                case FOUND_OTHER_THAN_FILE:
                    message.append("\n\t");
                    message.append(p);
                    message.append(" is not a file");
                    break;
                case UNREADABLE:
                    message.append("\n\t");
                    message.append(p);
                    message.append(" cannot be read");
                    break;
                case OK:
                    // Unreachable
                    break;
            }
            i++;
        }
        return message;
    }

    private String stringContents(final TerminalNode ctx)
    {
        final var text = ctx.getText();
        return text.substring(1, text.length() - 1);
    }


    // private GrammarAnalysisResult analyzeGrammar(ParserRuleContext ctx, String path)
    // {
    //     Path target = Path.of(path);
    //     var grammarAnalyzer = new InputGrammarAnalyzer();
    //     try {
    //         return grammarAnalyzer.analyze(CharStreams.fromPath(target.toAbsolutePath()));
    //     } catch (IOException e) {
    //         error(ctx, "Invalid grammar import path: " + e.getMessage());
    //     }
    //     return null;
    // }

}
