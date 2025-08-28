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
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.github.akruk.antlrxquery.AntlrXqueryParser.*;
import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver;
import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver.ResolvedName;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.XQuerySemanticContextManager;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.defaults.XQuerySemanticFunctionManager;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.defaults.XQuerySemanticFunctionManager.AnalysisResult;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.defaults.XQuerySemanticFunctionManager.ArgumentSpecification;
import com.github.akruk.antlrxquery.AntlrXqueryParserBaseVisitor;
import com.github.akruk.antlrxquery.XQueryAxis;
import com.github.akruk.antlrxquery.charescaper.XQuerySemanticCharEscaper;
import com.github.akruk.antlrxquery.charescaper.XQuerySemanticCharEscaper.XQuerySemanticCharEscaperResult;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;
import com.github.akruk.antlrxquery.inputgrammaranalyzer.InputGrammarAnalyzer.GrammarAnalysisResult;
import com.github.akruk.antlrxquery.typesystem.XQueryRecordField;
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


public class XQuerySemanticAnalyzer extends AntlrXqueryParserBaseVisitor<XQuerySequenceType> {
    private final XQuerySemanticContextManager contextManager;
    private final List<String> errors;
    private final List<String> warnings;
    private final XQueryTypeFactory typeFactory;
    private final XQueryValueFactory valueFactory;
    private final XQuerySemanticFunctionManager functionManager;
    private XQueryVisitingSemanticContext context;
    private List<XQuerySequenceType> visitedPositionalArguments;
    private Map<String, XQuerySequenceType> visitedKeywordArguments;

    private final XQuerySequenceType anyArrayOrMap;
    private final XQuerySequenceType anyItems;
    private final XQuerySequenceType emptySequence;
    private final GrammarAnalysisResult grammarAnalysisResult;
    private final SequencetypePathOperator pathOperator;

    public List<String> getErrors()
    {
        return errors;
    }

    // private record TupleElementType(String name, XQuerySequenceType type, String positionalName) {
    // };

    public XQuerySemanticAnalyzer(
        final Parser parser,
        final XQuerySemanticContextManager contextManager,
        final XQueryTypeFactory typeFactory,
        final XQueryValueFactory valueFactory,
        final XQuerySemanticFunctionManager functionCaller,
        final GrammarAnalysisResult grammarAnalysisResult)
    {
        this.grammarAnalysisResult = grammarAnalysisResult;
        this.typeFactory = typeFactory;
        this.valueFactory = valueFactory;
        this.functionManager = functionCaller;
        this.contextManager = contextManager;
        this.contextManager.enterContext();
        this.context = new XQueryVisitingSemanticContext();
        this.context.setType(typeFactory.anyNode());
        this.context.setPositionType(null);
        this.context.setSizeType(null);
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.anyArrayOrMap = typeFactory.zeroOrMore(typeFactory.itemChoice(Set.of(typeFactory.itemAnyMap(), typeFactory.itemAnyArray())));
        this.anyItems = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
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

        this.atomizer = new SequencetypeAtomization(typeFactory);
        this.castability = new SequencetypeCastable(typeFactory, atomizer);
        this.anyNodes = typeFactory.zeroOrMore(typeFactory.itemAnyNode());
        this.pathOperator = new SequencetypePathOperator(typeFactory, parser);
    }

    @Override
    public XQuerySequenceType visitFLWORExpr(final FLWORExprContext ctx)
    {
        final var saveReturnedOccurence = saveReturnedOccurence();
        contextManager.enterScope();
        ctx.initialClause().accept(this);
        for (final var clause : ctx.intermediateClause()) {
            clause.accept(this);
        }
        // at this point visitedTupleStream should contain all tuples
        final var expressionValue = ctx.returnClause().accept(this);
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
    public XQuerySequenceType visitLetClause(final LetClauseContext ctx)
    {
        for (final var letBinding : ctx.letBinding()) {
            final String variableName = letBinding.varName().getText();
            final XQuerySequenceType assignedValue = letBinding.exprSingle().accept(this);
            if (letBinding.typeDeclaration() == null) {
                contextManager.entypeVariable(variableName, assignedValue);
                continue;
            }
            final XQuerySequenceType type = letBinding.typeDeclaration().accept(this);
            if (!assignedValue.isSubtypeOf(type)) {
                final String msg = String.format("Type of variable %s is not compatible with the assigned value",
                    variableName);
                error(letBinding, msg);
            }
            contextManager.entypeVariable(variableName, type);
        }
        return null;
    }

    @Override
    public XQuerySequenceType visitForClause(final ForClauseContext ctx) {
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

    private void processForItemBinding(final ForItemBindingContext ctx) {
        final String variableName = ctx.varNameAndType().qname().getText();
        final XQuerySequenceType sequenceType = ctx.exprSingle().accept(this);
        returnedOccurrence = mergeFLWOROccurrence(sequenceType);

        checkPositionalVariableDistinct(ctx.positionalVar(), variableName, ctx);

        final XQueryItemType itemType = sequenceType.itemType;
        final XQuerySequenceType iteratorType = (ctx.allowingEmpty() != null)
                ? typeFactory.zeroOrOne(itemType)
                : typeFactory.one(itemType);

        processVariableTypeDeclaration(ctx.varNameAndType(), iteratorType, variableName, ctx);

        if (ctx.positionalVar() != null) {
            final String positionalVariableName = ctx.positionalVar().varName().getText();
            contextManager.entypeVariable(positionalVariableName, number);
        }
    }

    private void processForMemberBinding(final ForMemberBindingContext ctx) {
        final String variableName = ctx.varNameAndType().qname().getText();
        final XQuerySequenceType arrayType = ctx.exprSingle().accept(this);
        returnedOccurrence = arrayMergeFLWOROccurence();

        if (!arrayType.isSubtypeOf(anyArray)) {
            error(ctx, "XPTY0141: ForMemberBinding requires a single array value; received: " + arrayType);
        }

        checkPositionalVariableDistinct(ctx.positionalVar(), variableName, ctx);

        final XQuerySequenceType memberType = arrayType.itemType.arrayMemberType;
        final XQuerySequenceType iteratorType = memberType.addOptionality();

        processVariableTypeDeclaration(ctx.varNameAndType(), iteratorType, variableName, ctx);

        if (ctx.positionalVar() != null) {
            final String positionalVariableName = ctx.positionalVar().varName().getText();
            contextManager.entypeVariable(positionalVariableName, number);
        }
    }

    private void processForEntryBinding(final ForEntryBindingContext ctx) {
        final XQuerySequenceType mapType = ctx.exprSingle().accept(this);
        returnedOccurrence = arrayMergeFLWOROccurence();

        if (!mapType.isSubtypeOf(anyMap)) {
            error(ctx, "XPTY0141: ForEntryBinding requires a single map value");
            return;
        }

        final ForEntryKeyBindingContext keyBinding = ctx.forEntryKeyBinding();
        final ForEntryValueBindingContext valueBinding = ctx.forEntryValueBinding();

        // Check for duplicate key and value variable names
        if (keyBinding != null && valueBinding != null) {
            final String keyVarName = keyBinding.varNameAndType().qname().getText();
            final String valueVarName = valueBinding.varNameAndType().qname().getText();
            if (keyVarName.equals(valueVarName)) {
                error(ctx, "XQST0089: Key and value variable names must be distinct");
                return;
            }
        }

        // Process key binding
        if (keyBinding != null) {
            final String keyVariableName = keyBinding.varNameAndType().qname().getText();
            final XQueryItemType keyType = mapType.itemType.mapKeyType;
            final XQuerySequenceType keyIteratorType = typeFactory.one(keyType);

            checkPositionalVariableDistinct(ctx.positionalVar(), keyVariableName, ctx);
            processVariableTypeDeclaration(keyBinding.varNameAndType(), keyIteratorType, keyVariableName, ctx);
        }

        // Process value binding
        if (valueBinding != null) {
            final String valueVariableName = valueBinding.varNameAndType().qname().getText();
            final XQuerySequenceType valueType = mapType.itemType.mapValueType;

            checkPositionalVariableDistinct(ctx.positionalVar(), valueVariableName, ctx);
            processVariableTypeDeclaration(valueBinding.varNameAndType(), valueType, valueVariableName, ctx);
        }

        if (ctx.positionalVar() != null) {
            final String positionalVariableName = ctx.positionalVar().varName().getText();
            contextManager.entypeVariable(positionalVariableName, number);
        }
    }

    private void checkPositionalVariableDistinct(final PositionalVarContext positionalVar,
                                            final String mainVariableName,
                                            final ParserRuleContext context)
    {
        if (positionalVar != null) {
            final String positionalVariableName = positionalVar.varName().getText();
            if (mainVariableName.equals(positionalVariableName)) {
                error(context, "XQST0089: Positional variable name must be distinct from main variable name");
            }
        }
    }

private void processVariableTypeDeclaration(final VarNameAndTypeContext varNameAndType,
                                          final XQuerySequenceType inferredType,
                                          final String variableName,
                                          final ParseTree context)
{
    if (varNameAndType.typeDeclaration() == null) {
        contextManager.entypeVariable(variableName, inferredType);
        return;
    }

    final XQuerySequenceType declaredType = varNameAndType.typeDeclaration().accept(this);
    if (!inferredType.isSubtypeOf(declaredType)) {
        final String msg = String.format(
                "Type of variable %s is not compatible with the assigned value: %s is not subtype of %s",
                variableName, inferredType, declaredType);
        error((ParserRuleContext)context, msg);
    }
    contextManager.entypeVariable(variableName, declaredType);
}
    @Override
    public XQuerySequenceType visitSequenceType(final SequenceTypeContext ctx)
    {
        if (ctx.EMPTY_SEQUENCE() != null) {
            return emptySequence;
        }
        final var itemType = ctx.itemType().accept(this).itemType;
        if (ctx.occurrenceIndicator() == null) {
            return typeFactory.one(itemType);
        }
        return switch (ctx.occurrenceIndicator().getText()) {
            case "?" -> typeFactory.zeroOrOne(itemType);
            case "*" -> typeFactory.zeroOrMore(itemType);
            case "+" -> typeFactory.oneOrMore(itemType);
            default -> null;
        };
    }

    @Override
    public XQuerySequenceType visitAnyItemTest(final AnyItemTestContext ctx)
    {
        return typeFactory.anyItem();
    }

    @Override
    public XQuerySequenceType visitChoiceItemType(final ChoiceItemTypeContext ctx)
    {
        final List<ItemTypeContext> itemTypes = ctx.itemType();
        if (itemTypes.size() == 1) {
            return ctx.itemType(0).accept(this);
        }
        final var choiceItemNames = itemTypes.stream().map(i -> i.getText()).collect(Collectors.toSet());
        if (choiceItemNames.size() != itemTypes.size()) {
            error(ctx, "Duplicated type signatures in choice item type declaration");
        }
        final var choiceItems = itemTypes.stream().map(i -> i.accept(this))
            .map(sequenceType -> sequenceType.itemType)
            .toList();
        return typeFactory.choice(choiceItems);
    }

    @Override
    public XQuerySequenceType visitTypeName(final TypeNameContext ctx)
    {
        return switch (ctx.getText()) {
            case "number" -> number;
            case "string" -> string;
            case "boolean" -> boolean_;
            default -> {
                var type = typeFactory.namedType(ctx.getText());
                if (type != null)
                    yield type;
                final String msg = String.format("Type %s is not recognized", ctx.getText());
                error(ctx, msg);
                yield anyItem;
            }
        };
    }

    @Override
    public XQuerySequenceType visitAnyKindTest(final AnyKindTestContext ctx)
    {
        return typeFactory.anyNode();
    }

    @Override
    public XQuerySequenceType visitElementTest(final ElementTestContext ctx)
    {
        final Set<String> elementNames = ctx.nameTestUnion().nameTest().stream().map(e -> e.getText())
            .collect(Collectors.toSet());
        return typeFactory.element(elementNames);
    }

    @Override
    public XQuerySequenceType visitFunctionType(final FunctionTypeContext ctx)
    {
        if (ctx.anyFunctionType() != null) {
            return typeFactory.anyFunction();
        }
        final var func = ctx.typedFunctionType();
        final List<XQuerySequenceType> parameterTypes = func.typedFunctionParam().stream()
            .map(p -> p.sequenceType().accept(this))
            .collect(Collectors.toList());
        return typeFactory.function(func.sequenceType().accept(this), parameterTypes);
    }

    @Override
    public XQuerySequenceType visitMapType(final MapTypeContext ctx)
    {
        if (ctx.anyMapType() != null) {
            return typeFactory.anyMap();
        }
        final var map = ctx.typedMapType();
        final XQueryItemType keyType = map.itemType().accept(this).itemType;
        final XQuerySequenceType valueType = map.sequenceType().accept(this);
        return typeFactory.map(keyType, valueType);
    }

    @Override
    public XQuerySequenceType visitArrayType(final ArrayTypeContext ctx)
    {
        if (ctx.anyArrayType() != null) {
            return typeFactory.anyArray();
        }
        final var array = ctx.typedArrayType();
        final XQuerySequenceType sequenceType = array.sequenceType().accept(this);
        return typeFactory.array(sequenceType);
    }

    @Override
    public XQuerySequenceType visitRecordType(final RecordTypeContext ctx)
    {
        if (ctx.anyRecordType() != null) {
            return typeFactory.anyMap();
        }
        final var record = ctx.typedRecordType();
        final var fieldDeclarations = record.fieldDeclaration();
        final Map<String, XQueryRecordField> fields = new HashMap<>(fieldDeclarations.size());
        for (final var field : fieldDeclarations) {
            final String fieldName = field.fieldName().getText();
            final XQuerySequenceType fieldType = field.sequenceType().accept(this);
            final boolean isRequired = field.QUESTION_MARK() != null;
            final XQueryRecordField recordField = new XQueryRecordField(fieldType, isRequired);
            fields.put(fieldName, recordField);
        }
        if (record.extensibleFlag() == null) {
            return typeFactory.extensibleRecord(fields);
        }
        return typeFactory.record(fields);
    }

    @Override
    public XQuerySequenceType visitEnumerationType(final EnumerationTypeContext ctx)
    {
        final Set<String> enumMembers = ctx.STRING().stream()
            .map(TerminalNode::getText)
            .collect(Collectors.toSet());
        return typeFactory.enum_(enumMembers);
    }

    @Override
    public XQuerySequenceType visitCountClause(final CountClauseContext ctx)
    {
        final String countVariableName = ctx.varName().getText();
        contextManager.entypeVariable(countVariableName, number);
        return number;
    }

    @Override
    public XQuerySequenceType visitWhereClause(final WhereClauseContext ctx)
    {
        final var filteringExpression = ctx.exprSingle();
        final var filteringExpressionType = filteringExpression.accept(this);
        if (!filteringExpressionType.hasEffectiveBooleanValue) {
            error(filteringExpression, "Filtering expression must have effective boolean value");
        }
        returnedOccurrence = addOptionality(returnedOccurrence);
        return null;
    }

    @Override
    public XQuerySequenceType visitVarRef(final VarRefContext ctx)
    {
        final String variableName = ctx.varName().getText();
        final XQuerySequenceType variableType = contextManager.getVariable(variableName);
        return variableType;
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
    public XQuerySequenceType visitReturnClause(final ReturnClauseContext ctx)
    {
        final var type = ctx.exprSingle().accept(this);
        final var itemType = type.itemType;
        returnedOccurrence = mergeFLWOROccurrence(type);
        return switch (returnedOccurrence) {
            case 0 -> emptySequence;
            case 1 -> typeFactory.one(itemType);
            case 2 -> typeFactory.zeroOrOne(itemType);
            case 3 -> typeFactory.zeroOrMore(itemType);
            default -> typeFactory.oneOrMore(itemType);
        };
    }

    @Override
    public XQuerySequenceType visitWhileClause(final WhileClauseContext ctx)
    {
        final var filteringExpression = ctx.exprSingle();
        final var filteringExpressionType = filteringExpression.accept(this);
        if (!filteringExpressionType.hasEffectiveBooleanValue) {
            error(filteringExpression, "Filtering expression must have effective boolean value");
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
    public XQuerySequenceType visitLiteral(final LiteralContext ctx)
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
            return number;
        }

        if (numeric.BinaryIntegerLiteral() != null) {
            final String raw = numeric.BinaryIntegerLiteral().getText();
            final String binary = raw.replace("_", "").substring(2);
            valueFactory.number(new BigDecimal(new java.math.BigInteger(binary, 2)));
            return number;
        }

        if (numeric.DecimalLiteral() != null) {
            final String cleaned = numeric.DecimalLiteral().getText().replace("_", "");
            valueFactory.number(new BigDecimal(cleaned));
            return number;
        }

        if (numeric.DoubleLiteral() != null) {
            final String cleaned = numeric.DoubleLiteral().getText().replace("_", "");
            valueFactory.number(new BigDecimal(cleaned));
            return number;
        }
        return null;
    }

    private XQuerySequenceType handleNumber(final TerminalNode numeric) {
        final String value = numeric.getText().replace("_", "");
        valueFactory.number(new BigDecimal(value));
        return number;
    }

    private XQuerySequenceType handleNumber(final NumericLiteralContext numeric) {
        final String value = numeric.IntegerLiteral().getText().replace("_", "");
        valueFactory.number(new BigDecimal(value));
        return number;
    }

    private XQuerySequenceType handleString(final ParserRuleContext ctx) {
        final String content = processStringLiteral(ctx);
        return typeFactory.enum_(Set.of(content));
    }

    private String processStringLiteral(final ParserRuleContext ctx) {
        final String rawText = ctx.getText();
        final String content = unescapeString(ctx, rawText.substring(1, rawText.length() - 1));
        valueFactory.string(content);
        return content;
    }

    @Override
    public XQuerySequenceType visitParenthesizedExpr(final ParenthesizedExprContext ctx)
    {
        // Empty parentheses mean an empty sequence '()'
        if (ctx.expr() == null) {
            valueFactory.sequence(List.of());
            return emptySequence;
        }
        return ctx.expr().accept(this);
    }

    @Override
    public XQuerySequenceType visitExpr(final ExprContext ctx)
    {
        // Only one expression
        // e.g. 13
        if (ctx.exprSingle().size() == 1) {
            return ctx.exprSingle(0).accept(this);
        }
        // More than one expression
        final var previousExpr = ctx.exprSingle(0);
        var previousExprType = previousExpr.accept(this);
        final int size = ctx.exprSingle().size();
        for (int i = 1; i < size; i++) {
            final var exprSingle = ctx.exprSingle(i);
            final XQuerySequenceType expressionType = exprSingle.accept(this);
            previousExprType = previousExprType.sequenceMerge(expressionType);
        }
        return previousExprType;
    }

    private String unescapeString(final ParserRuleContext where, final String str)
    {
        final var charEscaper = new XQuerySemanticCharEscaper();
        final XQuerySemanticCharEscaperResult result = charEscaper.escapeWithDiagnostics(str);
        for (final var e : result.errors()) {
            error(where, e.message());
        }
        return result.unescaped();
    }

    private final NamespaceResolver namespaceResolver = new NamespaceResolver("fn");

    @Override
    public XQuerySequenceType visitFunctionCall(final FunctionCallContext ctx)
    {
        final String fullName = ctx.functionName().getText();
        final var resolution = namespaceResolver.resolve(fullName);
        final String namespace = resolution.namespace();
        final String functionName = resolution.name();

        final var savedArgs = saveVisitedArguments();
        final var savedKwargs = saveVisitedKeywordArguments();

        ctx.argumentList().accept(this);

        final AnalysisResult callAnalysisResult = functionManager.call(
            namespace, functionName, visitedPositionalArguments, visitedKeywordArguments, context);
        errors.addAll(callAnalysisResult.errors());
        for (final ArgumentSpecification defaultArg : callAnalysisResult.requiredDefaultArguments()) {
            final var expectedType = defaultArg.type();
            final var receivedType = defaultArg.defaultArgument().accept(this);
            if (!receivedType.isSubtypeOf(expectedType)) {
                error(ctx, String.format(
                    "Type mismatch for default argument '%s': expected '%s', but got '%s'.",
                    defaultArg.name(),
                    expectedType,
                    receivedType));
            }
        }
        visitedPositionalArguments = savedArgs;
        visitedKeywordArguments = savedKwargs;
        return callAnalysisResult.result();
    }

    @Override
    public XQuerySequenceType visitQuantifiedExpr(final QuantifiedExprContext ctx) {
        final List<QuantifierBindingContext> quantifierBindings = ctx.quantifierBinding();

        final List<String> variableNames = quantifierBindings.stream()
                .map(binding -> binding.varNameAndType().qname().getText())
                .toList();

        final List<XQuerySequenceType> coercedTypes = quantifierBindings.stream()
                .map(binding -> {
                    final TypeDeclarationContext typeDeclaration = binding.varNameAndType().typeDeclaration();
                    return typeDeclaration != null? typeDeclaration.accept(this) : null;
                })
                .toList();

        final List<XQuerySequenceType> variableTypes = quantifierBindings.stream()
                .map(binding -> binding.exprSingle().accept(this))
                .toList();

        final ExprSingleContext criterionNode = ctx.exprSingle();

        for (int i = 0; i < variableNames.size(); i++) {
            final var assignedType = variableTypes.get(i);
            final var desiredType = coercedTypes.get(i);
            if (desiredType !=null
                && assignedType.coerceableTo(desiredType) == RelativeCoercability.NEVER)
            {
                error(ctx.quantifierBinding(i).varNameAndType(), String.format("Type: %s is not coercable to %s", assignedType, desiredType));
            }

            contextManager.entypeVariable(variableNames.get(i), variableTypes.get(i));
        }

        final XQuerySequenceType queriedType = criterionNode.accept(this);
        if (!queriedType.hasEffectiveBooleanValue) {
            error(criterionNode, "Criterion value needs to have effective boolean value");
        }

        return boolean_;
    }

    @Override
    public XQuerySequenceType visitOrExpr(final OrExprContext ctx)
    {
        if (ctx.OR().isEmpty()) {
            return ctx.andExpr(0).accept(this);
        }
        final var orCount = ctx.OR().size();
        for (int i = 0; i <= orCount; i++) {
            final var visitedType = ctx.andExpr(i).accept(this);
            if (!visitedType.hasEffectiveBooleanValue) {
                error(ctx.andExpr(i), "Operands of 'or expression' need to have effective boolean value");
            }
            i++;
        }
        return boolean_;
    }

    @Override
    public XQuerySequenceType visitRangeExpr(final RangeExprContext ctx)
    {
        if (ctx.TO() == null) {
            return ctx.additiveExpr(0).accept(this);
        }
        final var fromValue = ctx.additiveExpr(0).accept(this);
        final var toValue = ctx.additiveExpr(1).accept(this);
        if (!fromValue.isSubtypeOf(optionalNumber)) {
            error(ctx.additiveExpr(0),
                "Wrong type in 'from' operand of 'range expression': '<number?> to <number?>'");
        }
        if (!toValue.isSubtypeOf(optionalNumber)) {
            error(ctx.additiveExpr(1), "Wrong type in 'to' operand of range expression: '<number?> to <number?>'");
        }
        return anyNumbers;
    }

    @Override
    public XQuerySequenceType visitPathExpr(final PathExprContext ctx)
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
    public XQuerySequenceType visitNodeTest(final NodeTestContext ctx)
    {
        XQuerySequenceType nodeType = context.getType();
        if (!nodeType.isSubtypeOf(anyNodes)) {
            error(ctx, "Path expression requires left hand side argument to be of type node()*, found: " + nodeType);
        }
        PathOperatorResult result;
        if (ctx.wildcard() != null) {
            result = pathOperator.pathOperator(nodeType, currentAxis, null, grammarAnalysisResult);
        } else {
            Set<String> names = ctx.pathNameTestUnion().qname().stream()
                .map(t->t.getText())
                .collect(Collectors.toSet());
            result = pathOperator.pathOperator(nodeType, currentAxis, names, grammarAnalysisResult);
        }
        if (result.isEmptyTarget()) {
            warn(ctx, "Empty sequence as target of path operator");
        }
        if (!result.invalidNames().isEmpty()) {
            String joinedNames = result.invalidNames().stream().collect(Collectors.joining(", "));
            error(ctx, "Path expression references unrecognized rule names: " + joinedNames);
        }
        if (!result.duplicateNames().isEmpty()) {
            String joinedNames = result.invalidNames().stream().collect(Collectors.joining(", "));
            warn(ctx, "Step expression contains duplicated names: " + joinedNames);
        }
        return result.result();
    }

    /**
     * Makes sure that context type is subtype of node()*
     * If it is not, error is recorded and the value is corrected to node()*
     * @param ctx rule where the error potentially has occured
     */
    private void contextTypeMustBeAnyNodes(final PathExprContext ctx)
    {
        XQuerySequenceType contexttype = context.getType();
        if (contexttype == null) {
            error(ctx, "Path expression starting from root requires context to be present and of type node()*");
            context.setType(anyNodes);
        } else if (!contexttype.isSubtypeOf(anyNodes)) {
            error(ctx,
                "Path expression starting from root requires context to be of type node()*; found " + contexttype);
            context.setType(anyNodes);
        }
    }

    @Override
    public XQuerySequenceType visitRelativePathExpr(final RelativePathExprContext ctx)
    {
        if (ctx.pathOperator().isEmpty()) {
            return ctx.stepExpr(0).accept(this);
        }
        final var savedContext = saveContext();
        XQuerySequenceType result = ctx.stepExpr(0).accept(this);
        final var operationCount = ctx.pathOperator().size();
        for (int i = 1; i <= operationCount; i++) {
            result = switch (ctx.pathOperator(i-1).getText()) {
                case "//" -> {
                    if (result.isZero) {
                        warn(ctx, "Zero nodes at expr " + i);
                        yield result;
                    }
                    if (!result.isSubtypeOf(zeroOrMoreNodes)) {
                        error(ctx, "Invalid type at " + i);
                        yield zeroOrMoreNodes;
                    }
                    if (grammarAnalysisResult != null && result.itemType.type == XQueryTypes.ELEMENT) {
                        // TODO: FIX
                        // final var ancestorsOrSelf = grammarAnalysisResult.ancestorsOrSelf();
                        // final Set<String> allPossibleNames = result.itemType.elementNames.stream()
                        //     .flatMap(elementName->ancestorsOrSelf.get(elementName).stream())
                        //     .collect(Collectors.toSet());
                        // if (allPossibleNames.isEmpty()) {
                        //     warn(ctx, "Unlikely path expression, no descendants possible");
                        // }
                        // final XQuerySequenceType type = typeFactory.element(allPossibleNames);
                        // context.setType(type);
                    } else {
                        context.setType(anyNodes);
                    }
                    yield ctx.stepExpr(i).accept(this);
                }
                case "/" -> ctx.stepExpr(i).accept(this);
                default -> null;
            };
            i++;
        }
        context = savedContext;
        return result;
    }




    @Override
    public XQuerySequenceType visitStepExpr(final StepExprContext ctx)
    {
        if (ctx.postfixExpr() != null)
            return ctx.postfixExpr().accept(this);
        return ctx.axisStep().accept(this);
    }

    @Override
    public XQuerySequenceType visitAxisStep(final AxisStepContext ctx)
    {
        XQuerySequenceType stepResult = null;
        if (ctx.reverseStep() != null)
            stepResult = ctx.reverseStep().accept(this);
        else if (ctx.forwardStep() != null)
            stepResult = ctx.forwardStep().accept(this);
        if (ctx.predicateList().predicate().isEmpty()) {
            return stepResult;
        }
        final var savedArgs = saveVisitedArguments();
        final var savedContext = saveContext();
        context.setType(savedContext.getType());
        context.setPositionType(number);
        context.setSizeType(number);
        for (final var predicate : ctx.predicateList().predicate()) {
            stepResult = predicate.accept(this);
        }
        visitedPositionalArguments = savedArgs;
        context = savedContext;
        return stepResult;
    }

    private XQueryVisitingSemanticContext saveContext() {
        final var saved = context;
        context = new XQueryVisitingSemanticContext();
        return saved;
    }


    @Override
    public XQuerySequenceType visitFilterExpr(final FilterExprContext ctx) {
        final XQuerySequenceType expr = ctx.postfixExpr().accept(this);
        final var savedContext = saveContext();
        context.setType(expr);
        final var filtered = ctx.predicate().accept(this);
        context = savedContext;
        return filtered;
    }
    @Override
    public XQuerySequenceType visitPredicate(final PredicateContext ctx)
    {
        final var contextType = context.getType();
        final var predicateExpression = ctx.expr().accept(this);
        final var savedContext = saveContext();
        context.setType(savedContext.getType());
        context.setPositionType(number);
        context.setSizeType(number);
        if (predicateExpression.isSubtypeOf(emptySequence))
            return emptySequence;
        if (predicateExpression.isSubtypeOf(typeFactory.zeroOrOne(typeFactory.itemNumber()))) {
            final var item = contextType.itemType;
            final var deducedType = typeFactory.zeroOrOne(item);
            return deducedType;
        }
        if (predicateExpression.isSubtypeOf(typeFactory.zeroOrMore(typeFactory.itemNumber()))) {
            final var item = contextType.itemType;
            final var deducedType = typeFactory.zeroOrMore(item);
            context.setType(deducedType);
            return deducedType;
        }
        if (!predicateExpression.hasEffectiveBooleanValue) {
            final var msg = String.format(
                "Predicate requires either number* type (for item by index aquisition) or a value that has effective boolean value, provided type: %s",
                predicateExpression);
            error(ctx.expr(), msg);
        }
        context = savedContext;
        return contextType.addOptionality();
    }

    @Override
    public XQuerySequenceType visitDynamicFunctionCall(final DynamicFunctionCallContext ctx) {
        final var savedArgs = saveVisitedArguments();
        final var savedContext = saveContext();
        context.setType(savedContext.getType());
        context.setPositionType(number);
        context.setSizeType(number);
        final XQuerySequenceType value = ctx.postfixExpr().accept(this);
        final boolean isCallable = value.isSubtypeOf(typeFactory.anyFunction());
        if (!isCallable) {
            error(ctx.postfixExpr(),
                "Expected function in dynamic function call expression, received: " + value);
        }
        ctx.positionalArgumentList().accept(this);
        visitedPositionalArguments = savedArgs;


        context = savedContext;

        if (isCallable)
            return value.itemType.returnedType;
        else
            return typeFactory.zeroOrMore(typeFactory.itemAnyItem());
    }



    @Override
    public XQuerySequenceType visitLookupExpr(final LookupExprContext ctx) {
        final var targetType = ctx.postfixExpr().accept(this);
        final XQuerySequenceType keySpecifierType = getKeySpecifier(ctx);
        final LookupContext lookup = ctx.lookup();
        return typecheckLookup(ctx, lookup, lookup.keySpecifier(), targetType, keySpecifierType);
    }


    private XQuerySequenceType typecheckLookup(
        final ParserRuleContext ctx,
        final LookupContext lookup,
        final KeySpecifierContext keySpecifier,
        final XQuerySequenceType targetType,
        XQuerySequenceType keySpecifierType)
    {
        if (targetType.isZero) {
            warn(ctx, "Target type of lookup expression is an empty sequence");
            return emptySequence;
        }
        final boolean isWildcard = keySpecifierType == null;
        if (!isWildcard && keySpecifierType.isZero) {
            warn(ctx, "Empty sequence as key specifier in lookup expression");
            return emptySequence;
        }
        if (!targetType.isSubtypeOf(anyArrayOrMap)) {
            error(ctx, "Left side of lookup expression '<left> ? ...' must be map(*)* or array(*)*");
            return anyItems;
        }

        switch (targetType.itemType.type) {
            case ARRAY:
                final XQuerySequenceType targetItemType = targetType.itemType.arrayMemberType;
                if (targetItemType == null)
                    return anyItems;
                final XQuerySequenceType result = targetItemType.sequenceMerge(targetItemType).addOptionality();
                if (isWildcard) {
                    return result;
                }
                if (!keySpecifierType.itemtypeIsSubtypeOf(typeFactory.zeroOrMore(typeFactory.itemNumber())))
                {
                    error(lookup, "Key type for lookup expression on " + targetType + " must be of type number*");
                }
                return result;
            case ANY_ARRAY:
                if (isWildcard) {
                    return anyItems;
                }
                if (!keySpecifierType.isSubtypeOf(typeFactory.zeroOrMore(typeFactory.itemNumber())))
                {
                    error(lookup, "Key type for lookup expression on " + targetType + " must be of type number*");
                }
                return anyItems;
            case MAP:
                return getMapLookuptype(ctx, lookup, keySpecifier, targetType, keySpecifierType, isWildcard);
            case EXTENSIBLE_RECORD:
                return getExtensibleRecordLookupType(ctx, lookup, keySpecifier,targetType, keySpecifierType, isWildcard);
            case RECORD:
                return getRecordLookupType(ctx, lookup, keySpecifier,targetType, keySpecifierType, isWildcard);
            case ANY_MAP:
                return anyItems;
            default:
                return getAnyArrayOrMapLookupType(lookup, isWildcard, targetType, keySpecifierType);
        }
    }


    XQuerySequenceType getAnyArrayOrMapLookupType(LookupContext ctx, boolean isWildcard, XQuerySequenceType targetType, XQuerySequenceType keySpecifierType) {
        if (isWildcard) {
            return null;
        }
        final XQueryItemType targetItemType = targetType.itemType;
        final Collection<XQueryItemType> choiceItemTypes = targetItemType.itemTypes;
        XQueryItemType targetKeyItemType = null;
        XQuerySequenceType resultingType = null;
        for (final var itemType : choiceItemTypes) {
            if (resultingType == null) {
                if (!isWildcard)
                    resultingType = switch(keySpecifierType.occurence) {
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
                    resultingType = anyItems;
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
            error(ctx, "Key type for lookup expression on " + targetType + " must be subtype of type " + expectedKeyItemtype);
        }
        return resultingType;
    }

    @Override
    public XQuerySequenceType visitUnaryLookup(UnaryLookupContext ctx) {
        var contextType = context.getType();
        var keySpecifierType = ctx.lookup().keySpecifier().accept(this);
        return typecheckLookup(ctx, ctx.lookup(), ctx.lookup().keySpecifier(),  contextType, keySpecifierType);
    }



    private  XQuerySequenceType getMapLookuptype(
            final ParserRuleContext target,
            final LookupContext lookup,
            final KeySpecifierContext keySpecifier,
            final XQuerySequenceType targetType,
            final XQuerySequenceType keySpecifierType,
            final boolean isWildcard)
    {
        final XQueryItemType targetKeyItemType = targetType.itemType.mapKeyType;
        final XQuerySequenceType targetValueType = targetType.itemType.mapValueType;
        final XQueryItemType targetValueItemtype = targetValueType.itemType;
        if (isWildcard) {
            return typeFactory.zeroOrMore(targetValueItemtype);
        }
        final XQuerySequenceType result = switch(keySpecifierType.occurence) {
                case ONE -> typeFactory.zeroOrOne(targetValueItemtype);
                default -> typeFactory.zeroOrMore(targetValueItemtype);
            };
        final XQuerySequenceType expectedKeyItemtype = typeFactory.zeroOrMore(targetKeyItemType);
        if (!keySpecifierType.isSubtypeOf(expectedKeyItemtype)) {
            error(lookup, "Key type for lookup expression on " + targetType + " must be subtype of type " + expectedKeyItemtype);
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
        final XQuerySequenceType targetType,
        final XQuerySequenceType keySpecifierType,
        final boolean isWildcard)
    {
        final XQueryItemType targetKeyItemType = typeFactory.itemString();
        final Map<String, XQueryRecordField> recordFields = targetType.itemType.recordFields;
        if (recordFields.isEmpty()) {
            warn(target, "Empty record will always return empty sequence...");
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
            error(keySpecifier, "Key specifier on a record type should be subtype of string*");
            return anyItems;
        }
        final var string = keySpecifier.STRING();
        if (string != null) {
            final String key = processStringLiteral(keySpecifier);
            final var valueType = recordFields.get(key);
            if (valueType == null) {
                error(keySpecifier, "Key specifier: " + key + " does not match record of type " + targetType);
                return anyItems;
            }
            return valueType.type();
        }
        final XQuerySequenceType expectedKeyItemtype = typeFactory.zeroOrMore(targetKeyItemType);
        if (!keySpecifierType.isSubtypeOf(expectedKeyItemtype)) {
            error(lookup, "Key type for lookup expression on " + targetType + " must be subtype of type " + expectedKeyItemtype);
        }
        if (keySpecifierType.itemType.type == XQueryTypes.ENUM) {
            final var members = keySpecifierType.itemType.enumMembers;
            final var firstField = members.stream().findFirst().get();
            final var firstRecordField = recordFields.get(firstField);
            XQuerySequenceType merged = firstRecordField.isRequired() ? firstRecordField.type() : firstRecordField.type().addOptionality();
            for (final var member : members) {
                if (member.equals(firstField))
                    continue;
                final var recordField = recordFields.get(member);
                if (recordField == null) {
                    warn(lookup, "The following enum member: " + member + "does not match any record field");
                    return anyItems;
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
        final XQuerySequenceType targetType,
            final XQuerySequenceType keySpecifierType, final boolean isWildcard)
    {
        final XQueryItemType targetKeyItemType = typeFactory.itemString();
        final Map<String, XQueryRecordField> recordFields = targetType.itemType.recordFields;
        if (recordFields.isEmpty()) {
            warn(ctx, "Empty record will always return empty sequence...");
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
            error(ctx, "Key specifier on a record type should be subtype of string*");
            return anyItems;
        }
        final var string = keySpecifier.STRING();
        if (string != null) {
            final String key = processStringLiteral(keySpecifier);
            final var valueType = recordFields.get(key);
            if (valueType == null) {
                return anyItems;
            }
            return valueType.type();
        }
        final XQuerySequenceType expectedKeyItemtype = typeFactory.zeroOrMore(targetKeyItemType);
        if (!keySpecifierType.isSubtypeOf(expectedKeyItemtype)) {
            error(lookup, "Key type for lookup expression on " + targetType + " must be subtype of type " + expectedKeyItemtype);
        }
        if (keySpecifierType.itemType.type == XQueryTypes.ENUM) {
            final var members = keySpecifierType.itemType.enumMembers;
            final var firstField = members.stream().findFirst().get();
            final var firstRecordField = recordFields.get(firstField);
            XQuerySequenceType merged = firstRecordField.isRequired() ? firstRecordField.type() : firstRecordField.type().addOptionality();
            for (final var member : members) {
                if (member.equals(firstField))
                    continue;
                final var recordField = recordFields.get(member);
                if (recordField == null)  {
                    return anyItems;
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





    XQuerySequenceType getKeySpecifier(final LookupExprContext ctx) {
        final KeySpecifierContext keySpecifier = ctx.lookup().keySpecifier();
        if (keySpecifier.qname() != null) {
            return typeFactory.enum_(Set.of(keySpecifier.qname().getText()));
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
    public XQuerySequenceType visitContextItemExpr(final ContextItemExprContext ctx)
    {
        return context.getType();
    }


    @Override
    public XQuerySequenceType visitForwardAxis(final ForwardAxisContext ctx) {
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
    public XQuerySequenceType visitReverseAxis(final ReverseAxisContext ctx) {
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
    public XQuerySequenceType visitForwardStep(final ForwardStepContext ctx)
    {
        if (ctx.forwardAxis() != null) {
            ctx.forwardAxis().accept(this);
        } else {
            if (currentAxis == null) {
                currentAxis = XQueryAxis.CHILD;
            }
        }
        return ctx.nodeTest().accept(this);
    }

    @Override
    public XQuerySequenceType visitReverseStep(final ReverseStepContext ctx)
    {
        if (ctx.abbrevReverseStep() != null) {
            return ctx.abbrevReverseStep().accept(this);
        }
        ctx.reverseAxis().accept(this);
        return ctx.nodeTest().accept(this);
    }

    private final XQuerySequenceType number;
    private final XQuerySequenceType zeroOrMoreNodes;
    private final XQuerySequenceType anyArray;
    private final XQuerySequenceType anyMap;
    private final XQuerySequenceType boolean_;
    private final XQuerySequenceType string;
    private final XQuerySequenceType optionalNumber;
    private final XQuerySequenceType anyNumbers;
    private final XQuerySequenceType optionalString;
    private final XQuerySequenceType anyItem;





    @Override
    public XQuerySequenceType visitStringConcatExpr(final StringConcatExprContext ctx)
    {
        if (ctx.CONCATENATION().isEmpty()) {
            return ctx.rangeExpr(0).accept(this);
        }
        for (int i = 0; i < ctx.rangeExpr().size(); i++) {
            final var visitedType = ctx.rangeExpr(i).accept(this);
            if (!visitedType.isSubtypeOf(anyItems)) {
                error(ctx.rangeExpr(i), "Operands of 'or expression' need to be subtype of item()?");
            }
        }
        return string;
    }

    @Override
    public XQuerySequenceType visitSimpleMapExpr(final SimpleMapExprContext ctx)
    {
        if (ctx.EXCLAMATION_MARK().isEmpty())
            return ctx.pathExpr(0).accept(this);
        final XQuerySequenceType firstExpressionType = ctx.pathExpr(0).accept(this);
        final XQuerySequenceType iterator = firstExpressionType.iteratorType();
        final var savedContext = saveContext();
        context.setType(iterator);
        context.setPositionType(number);
        context.setSizeType(number);
        XQuerySequenceType result = firstExpressionType;
        final var theRest = ctx.pathExpr().subList(1, ctx.pathExpr().size());
        for (final var mappedExpression : theRest) {
            final XQuerySequenceType type = mappedExpression.accept(this);
            result = result.mapping(type);
            context.setType(result.iteratorType());
        }
        context = savedContext;
        return result;
    }

    @Override
    public XQuerySequenceType visitInstanceofExpr(final InstanceofExprContext ctx)
    {
        final XQuerySequenceType expression = ctx.treatExpr().accept(this);
        if (ctx.INSTANCE() == null) {
            return expression;
        }
        final var testedType = ctx.sequenceType().accept(this);
        if (expression.isSubtypeOf(testedType)) {
            warn(ctx, "Unnecessary instance of expression is always true");
        }
        return this.boolean_;
    }

    @Override
    public XQuerySequenceType visitTreatExpr(final TreatExprContext ctx)
    {
        final XQuerySequenceType expression = ctx.castableExpr().accept(this);
        if (ctx.TREAT() == null) {
            return expression;
        }
        final var relevantType = ctx.sequenceType().accept(this);
        if (!relevantType.isSubtypeOf(expression)
            && !expression.isSubtypeOf(relevantType))
        {
            warn(ctx, "Unlikely treat expression");
        }
        return relevantType;
    }

    private final SequencetypeAtomization atomizer;



    private final SequencetypeCastable castability;
    private final XQuerySequenceType anyNodes;

    @Override
    public XQuerySequenceType visitCastableExpr(CastableExprContext ctx) {
        if (ctx.CASTABLE() == null)
            return ctx.castExpr().accept(this);
        final var type = this.visitCastTarget(ctx.castTarget());
        final var tested = this.visitCastExpr(ctx.castExpr());
        final boolean emptyAllowed = ctx.castTarget().QUESTION_MARK() != null;
        IsCastableResult result = castability.isCastable(type, tested, emptyAllowed);
        verifyCastability(ctx, type, tested, result.castability(), result);
        return result.resultingType();
    }

    private <T> void  verifyCastability(
            final ParserRuleContext ctx,
            final T type,
            final XQuerySequenceType tested,
            final Castability castability,
            final IsCastableResult result)
    {
        // TODO: add atomized info
        switch(castability) {
        case POSSIBLE:
            break;
        case ALWAYS_POSSIBLE_CASTING_TO_SAME:
            warn(ctx, "Casting from " + tested + " to type " + type + " is a selfcast");
            break;
        case ALWAYS_POSSIBLE_CASTING_TO_SUBTYPE:
            warn(ctx, "Casting from subtype " + tested + " supertype " + type + " will always succeed");
            break;
        case ALWAYS_POSSIBLE_CASTING_TO_TARGET:
            warn(ctx, "Casting from type " + tested + " to type " + type + " will always succeed");
            break;
        case ALWAYS_POSSIBLE_MANY_ITEMTYPES:
            warn(ctx, "Casting from type " + tested + " to type " + type + " will always succeed");
            final XQueryItemType[] wrongItemtypes = result.wrongItemtypes();
            final int itemtypeCount = wrongItemtypes.length;
            for (int i = 0; i < itemtypeCount; i++) {
                verifyCastability(ctx, wrongItemtypes[i], tested, result.problems()[i], null);
            }
            break;
        case ALWAYS_POSSIBLE_MANY_SEQUENCETYPES:
            warn(ctx, "Casting from type " + tested + " to type " + type + " will always succeed");
            break;
        case IMPOSSIBLE:
            warn(ctx, "Casting from type " + tested + " to type " + type + " will never succeed");
            break;
        case TESTED_EXPRESSION_CAN_BE_EMPTY_SEQUENCE_WITHOUT_FLAG:
            error(ctx, "Tested expression of type " + tested + " can be an empty sequence without flag '?'");
            break;
        case TESTED_EXPRESSION_IS_EMPTY_SEQUENCE:
            error(ctx, "Tested expression is an empty sequence");
            break;
        case TESTED_EXPRESSION_IS_ZERO_OR_MORE:
            error(ctx, "Tested expression of type " + tested + " can be a sequence of cardinality greater than one (or '?')");
            break;
        case WRONG_TARGET_TYPE:
            error(ctx, "Type: " + type + " is invalid casting target");
            break;
        }
    }


    @Override
    public XQuerySequenceType visitCastExpr(CastExprContext ctx) {
        if (ctx.CAST() == null)
            return ctx.pipelineExpr().accept(this);
        final var type = this.visitCastTarget(ctx.castTarget());
        final var tested = this.visitPipelineExpr(ctx.pipelineExpr());
        final boolean emptyAllowed = ctx.castTarget().QUESTION_MARK() != null;
        IsCastableResult result = castability.isCastable(type, tested, emptyAllowed);
        verifyCastability(ctx, type, tested, result.castability(), result);
        return result.resultingType();
    }

    XQuerySequenceType handleCastable(
            CastableExprContext ctx,
            XQuerySequenceType atomized,
            XQuerySequenceType tested,
            XQuerySequenceType type,
            XQuerySequenceType result)
    {
        if (atomized.itemtypeIsSubtypeOf(tested)) {
            warn(ctx, "Unnecessary castability test");
            return type;
        }
        final XQueryItemType atomizedItemtype = atomized.itemType;
        final XQueryTypes atomizedItemtypeType = atomizedItemtype.type;
        final XQueryTypes castTargetType = type.itemType.type;
        if (atomizedItemtypeType == XQueryTypes.CHOICE)
        {
            final var itemtypes = atomizedItemtype.itemTypes;
            for (final var itemtype : itemtypes) {
                testCastable(ctx, castTargetType, errorMessageOnChoiceFailedCasting(atomized, tested, type, itemtype));
            }
            return result;
        }
        testCastable(ctx, castTargetType, errorMessageOnFailedCasting(atomized, tested, type, atomizedItemtype));
        return result;
    }

    private Supplier<String> errorMessageOnFailedCasting(
            final XQuerySequenceType atomized,
            final XQuerySequenceType tested,
            final XQuerySequenceType type,
            final XQueryItemType itemtype)
    {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Type ");
        stringBuilder.append(tested);
        stringBuilder.append(" atomized as type ");
        stringBuilder.append(atomized);
        stringBuilder.append(" cannot be cast to ");
        stringBuilder.append(type);
        return ()-> stringBuilder.toString();
    }


    private Supplier<String> errorMessageOnChoiceFailedCasting(XQuerySequenceType atomized, XQuerySequenceType tested, XQuerySequenceType type,
            final XQueryItemType itemtype) {
        return ()->"Itemtype " + itemtype
                                                  + " that is a member of itemtype of type "
                                                  + tested
                                                  + " atomized as type "
                                                  + atomized
                                                  + " cannot be cast to "
                                                  + type;
    }

    void testCastable(ParserRuleContext ctx, XQueryTypes castTargetType, Supplier<String> errorMessageSupplier)
    {
        switch (castTargetType) {
            case STRING, NUMBER, ENUM, BOOLEAN:
                break;
            default:
                error(ctx, errorMessageSupplier.get());
        };
    }


    XQuerySequenceType handleOne(
            CastableExprContext ctx,
            XQuerySequenceType atomized,
            XQuerySequenceType tested,
            XQuerySequenceType type,
            XQuerySequenceType result)
    {
        if (atomized.itemtypeIsSubtypeOf(tested)) {
            warn(ctx, "Unnecessary castability test");
            return type;
        }
        final XQueryItemType atomizedItemtype = atomized.itemType;
        final XQueryTypes atomizedItemtypeType = atomizedItemtype.type;
        final XQueryTypes castTargetType = type.itemType.type;
        if (atomizedItemtypeType == XQueryTypes.CHOICE)
        {
            final var itemtypes = atomizedItemtype.itemTypes;
            for (final var itemtype : itemtypes) {
                testCastingOne(ctx, castTargetType, errorMessageOnChoiceFailedCasting(atomized, tested, type, itemtype));
            }
            return result;
        }
        testCastingOne(ctx, castTargetType, errorMessageOnFailedCasting(atomized, tested, type, atomizedItemtype));
        return result;
    }

    void testCastingOne(CastableExprContext ctx, XQueryTypes castTargetType, Supplier<String> errorMessageSupplier)
    {
        switch (castTargetType) {
            case STRING, NUMBER, ENUM, BOOLEAN:
                break;
            default:
                error(ctx, errorMessageSupplier.get());
        };
    }

    @Override
    public XQuerySequenceType visitCastTarget(CastTargetContext ctx) {
        var type = super.visitCastTarget(ctx);
        if (ctx.QUESTION_MARK() != null)
            type = type.addOptionality();
        return type;
    }

    @Override
    public XQuerySequenceType visitNamedFunctionRef(final NamedFunctionRefContext ctx)
    {
        final int arity = Integer.parseInt(ctx.IntegerLiteral().getText());
        final ResolvedName resolvedName = namespaceResolver.resolve(ctx.qname().getText());
        final var analysis = functionManager.getFunctionReference(resolvedName.namespace(), resolvedName.name(), arity);
        errors.addAll(analysis.errors());
        return analysis.result();
    }

    @Override
    public XQuerySequenceType visitSquareArrayConstructor(final SquareArrayConstructorContext ctx)
    {
        if (ctx.exprSingle().isEmpty()) {
            return anyArray;
        }
        final XQuerySequenceType arrayType = ctx.exprSingle().stream()
            .map(expr -> expr.accept(this))
            .reduce((t1, t2) -> t1.alternativeMerge(t2))
            .get();
        return typeFactory.array(arrayType);
    }

    @Override
    public XQuerySequenceType visitCurlyArrayConstructor(final CurlyArrayConstructorContext ctx)
    {
        final var expressions = ctx.enclosedExpr().expr();
        if (expressions == null) {
            return anyArray;
        }

        final XQuerySequenceType arrayType = expressions.exprSingle().stream()
            .map(expr -> expr.accept(this))
            .reduce((t1, t2) -> t1.alternativeMerge(t2))
            .get();
        return typeFactory.array(arrayType);

    }

    @Override
    public XQuerySequenceType visitPipelineExpr(final PipelineExprContext ctx)
    {
        if (ctx.PIPE_ARROW().isEmpty())
            return ctx.arrowExpr(0).accept(this);
        final var saved = saveContext();
        final int size = ctx.arrowExpr().size();
        XQuerySequenceType contextType = ctx.arrowExpr(0).accept(this);
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
    public XQuerySequenceType visitTryCatchExpr(final TryCatchExprContext ctx)
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
                        var typeRef = typeFactory.itemNamedType(error.getText());
                        if (typeRef == null) {
                            typeRef = errorType;
                            error(c, "Unknown error in try/catch: " + error.getText());
                        }
                        if (!typeRef.itemtypeIsSubtypeOf(errorType)) {
                            typeRef = errorType;
                            error(c,
                                "Type " + typeRef.toString() + " is not an error in try/catch: " + error.getText());
                        }
                        foundErrors.add(typeRef);
                    }
                    choicedErrors = typeFactory.choice(foundErrors);
                } else {
                    choicedErrors = typeFactory.error();
                }
                context.setType(choicedErrors);
                context.setPositionType(null);
                context.setSizeType(null);
                contextManager.enterScope();
                contextManager.entypeVariable("$err:code", string);
                contextManager.entypeVariable("$err:description", optionalString);
                contextManager.entypeVariable("$err:value", typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
                contextManager.entypeVariable("$err:module", typeFactory.zeroOrOne(typeFactory.itemString()));
                contextManager.entypeVariable("$err:line-number", typeFactory.zeroOrOne(typeFactory.itemNumber()));
                contextManager.entypeVariable("$err:column-number", typeFactory.zeroOrOne(typeFactory.itemNumber()));
                contextManager.entypeVariable("$err:stack-trace", typeFactory.zeroOrOne(typeFactory.itemString()));
                contextManager.entypeVariable("$err:additional", typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
                contextManager.entypeVariable("$err:map", typeFactory.anyMap());

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
                        error(qname, "Error: " + name + "already used in catch clause");
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
                error(catchClause, "Unnecessary catch clause, wildcard already used");
            }
        }

        final FinallyClauseContext finallyClause = ctx.finallyClause();
        if (finallyClause != null) {
            context = new XQueryVisitingSemanticContext();
            context.setType(typeFactory.anyNode());
            final XQuerySequenceType finallyType = finallyClause.enclosedExpr().accept(this);
            if (!finallyType.isSubtypeOf(emptySequence)) {
                error(finallyClause,
                    "Finally clause needs to evaluate to empty sequence, currently:" + finallyType.toString());
            }
        }
        context = savedContext;
        final var mergedAlternativeCatches = alternativeCatches.reduce(XQuerySequenceType::alternativeMerge).get();
        return testedExprType.alternativeMerge(mergedAlternativeCatches);
    }

    @SuppressWarnings("unchecked")
    @Override
    public XQuerySequenceType visitMapConstructor(final MapConstructorContext ctx)
    {
        final var entries = ctx.mapConstructorEntry();
        if (entries.isEmpty())
            return typeFactory.anyMap();
        final XQueryItemType keyType = entries.stream()
            .map(e -> e.mapKeyExpr().accept(this).itemType)
            .reduce((t1, t2) -> t1.alternativeMerge(t2))
            .get();
        if (keyType.type == XQueryTypes.ENUM) {
            final var enum_ = keyType;
            final var enumMembers = enum_.enumMembers;
            final List<Entry<String, XQueryRecordField>> recordEntries = new ArrayList<>(enumMembers.size());
            int i = 0;
            for (final var enumMember : enumMembers) {
                final var valueType = entries.get(i).mapValueExpr().accept(this);
                recordEntries.add(Map.entry(enumMember, new XQueryRecordField(valueType, true)));
                i++;
            }
            return typeFactory.record(Map.ofEntries(recordEntries.toArray(Entry[]::new)));
        }
        // TODO: refine
        final XQuerySequenceType valueType = entries.stream()
            .map(e -> e.mapValueExpr().accept(this))
            .reduce((t1, t2) -> t1.alternativeMerge(t2)).get();
        return typeFactory.map(keyType, valueType);
    }

    @Override
    public XQuerySequenceType visitArrowFunctionSpecifier(final ArrowFunctionSpecifierContext ctx)
    {
        if (ctx.ID() != null) {
            // TODO:
            // final CallAnalysisResult call =
            // functionCaller.getFunctionReference(ctx.ID().getText(), typeFactory);
        }

        if (ctx.varRef() != null)
            return ctx.varRef().accept(this);
        return ctx.parenthesizedExpr().accept(this);

    }

    @Override
    public XQuerySequenceType visitAndExpr(final AndExprContext ctx)
    {
        if (ctx.AND().isEmpty()) {
            return ctx.comparisonExpr(0).accept(this);
        }
        final XQuerySequenceType boolean_ = this.boolean_;
        final var operatorCount = ctx.AND().size();
        for (int i = 0; i <= operatorCount; i++) {
            final var visitedType = ctx.comparisonExpr(i).accept(this);
            if (!visitedType.hasEffectiveBooleanValue) {
                error(ctx.comparisonExpr(i), "Operands of 'or expression' need to have effective boolean value");
            }
            i++;
        }
        return boolean_;
    }

    @Override
    public XQuerySequenceType visitAdditiveExpr(final AdditiveExprContext ctx)
    {
        if (ctx.additiveOperator().isEmpty()) {
            return ctx.multiplicativeExpr(0).accept(this);
        }
        for (final var operandExpr : ctx.multiplicativeExpr()) {
            final var operand = operandExpr.accept(this);
            if (!operand.isSubtypeOf(number)) {
                error(operandExpr,
                    "Operands in additive expression must be numeric, received: " + operand.toString());
            }
        }
        return number;
    }

    @Override
    public XQuerySequenceType visitComparisonExpr(final ComparisonExprContext ctx)
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

    private XQuerySequenceType handleGeneralComparison(final ComparisonExprContext ctx)
    {
        final var leftHandSide = ctx.otherwiseExpr(0).accept(this);
        final var rightHandSide = ctx.otherwiseExpr(1).accept(this);
        if (!leftHandSide.isSubtypeOf(rightHandSide)) {
            final String msg = String.format("The types: %s and %s in general comparison are not comparable",
                leftHandSide.toString(), rightHandSide.toString());
            error(ctx, msg);
        }
        return typeFactory.boolean_();
    }

    private XQuerySequenceType handleValueComparison(final ComparisonExprContext ctx)
    {
        final var leftHandSide = ctx.otherwiseExpr(0).accept(this);
        final var rightHandSide = ctx.otherwiseExpr(1).accept(this);
        final var optionalItem = typeFactory.zeroOrOne(typeFactory.itemAnyItem());
        final var optionalBoolean = typeFactory.zeroOrOne(typeFactory.itemBoolean());
        if (!leftHandSide.isSubtypeOf(optionalItem)) {
            error(ctx.otherwiseExpr(0),
                "Left hand side of 'or expression' must be of type 'item()?', received: "
                    + leftHandSide.toString());
        }
        if (!rightHandSide.isSubtypeOf(optionalItem)) {
            error(ctx.otherwiseExpr(1),
                "Right hand side of 'or expression' must be of type 'item()?', received: "
                    + leftHandSide.toString());
        }
        if (!leftHandSide.isValueComparableWith(rightHandSide)) {
            final String msg = String.format("The types: %s and %s in value comparison are not comparable",
                leftHandSide.toString(), rightHandSide.toString());
            error(ctx, msg);
        }
        if (leftHandSide.isSubtypeOf(typeFactory.anyItem())
            && rightHandSide.isSubtypeOf(typeFactory.anyItem())) {
            return typeFactory.boolean_();
        }
        return optionalBoolean;
    }

    private XQuerySequenceType handleNodeComp(final ComparisonExprContext ctx)
    {
        final var anyNode = typeFactory.zeroOrOne(typeFactory.itemAnyNode());
        final var optionalBoolean = typeFactory.zeroOrOne(typeFactory.itemBoolean());
        final var visitedLeft = ctx.otherwiseExpr(0).accept(this);
        if (!visitedLeft.isSubtypeOf(anyNode)) {
            error(ctx.otherwiseExpr(0),
                "Operands of node comparison must be of type 'node()?', received: " + visitedLeft.toString());
        }
        final var visitedRight = ctx.otherwiseExpr(1).accept(this);
        if (!visitedRight.isSubtypeOf(anyNode)) {
            error(ctx.otherwiseExpr(1),
                "Operands of node comparison must be of type 'node()?', received: " + visitedRight.toString());
        }
        return optionalBoolean;

    }

    @Override
    public XQuerySequenceType visitMultiplicativeExpr(final MultiplicativeExprContext ctx)
    {
        if (ctx.multiplicativeOperator().isEmpty()) {
            return ctx.unionExpr(0).accept(this);
        }
        for (final var expr : ctx.unionExpr()) {
            final var visitedType = expr.accept(this);
            if (!visitedType.isSubtypeOf(number)) {
                error(ctx, "Multiplicative expression requires a number, received: " + visitedType.toString());
            }
        }
        return number;
    }

    @Override
    public XQuerySequenceType visitOtherwiseExpr(final OtherwiseExprContext ctx)
    {
        if (ctx.OTHERWISE().isEmpty())
            return ctx.stringConcatExpr(0).accept(this);
        final int length = ctx.stringConcatExpr().size();
        XQuerySequenceType merged = ctx.stringConcatExpr(0).accept(this);
        for (int i = 1; i < length; i++) {
            final var expr = ctx.stringConcatExpr(i);
            final XQuerySequenceType exprType = expr.accept(this);
            merged = exprType.alternativeMerge(merged);
        }
        return merged;
    }

    @Override
    public XQuerySequenceType visitUnionExpr(final UnionExprContext ctx)
    {
        if (ctx.unionOperator().isEmpty()) {
            return ctx.intersectExpr(0).accept(this);
        }
        final var zeroOrMoreNodes = typeFactory.zeroOrMore(typeFactory.itemAnyNode());
        var expressionNode = ctx.intersectExpr(0);
        var expressionType = expressionNode.accept(this);
        if (!expressionType.isSubtypeOf(zeroOrMoreNodes)) {
            error(expressionNode,
                "Expression of union operator node()* | node()* does match the type 'node()', received type: "
                    + expressionType.toString());
            expressionType = zeroOrMoreNodes;
        }
        final var unionCount = ctx.unionOperator().size();
        for (int i = 1; i <= unionCount; i++) {
            expressionNode = ctx.intersectExpr(i);
            final var visitedType = expressionNode.accept(this);
            if (!visitedType.isSubtypeOf(zeroOrMoreNodes)) {
                error(expressionNode,
                    "Expression of union operator node()* | node()* does match the type 'node()', received type: "
                        + expressionType.toString());
                expressionType = zeroOrMoreNodes;
            } else {
                expressionType = expressionType.unionMerge(visitedType);
            }
        }
        return expressionType;
    }

    @Override
    public XQuerySequenceType visitIntersectExpr(final IntersectExprContext ctx)
    {
        if (ctx.exceptOrIntersect().isEmpty()) {
            return ctx.instanceofExpr(0).accept(this);
        }
        var expressionType = ctx.instanceofExpr(0).accept(this);
        final var zeroOrMoreNodes = typeFactory.zeroOrMore(typeFactory.itemAnyNode());
        if (!expressionType.isSubtypeOf(zeroOrMoreNodes)) {
            error(ctx.instanceofExpr(0),
                "Expression of operator node()* except/intersect node()* does match the type 'node()', received type: "
                    + expressionType.toString());
            expressionType = zeroOrMoreNodes;
        }
        final var operatorCount = ctx.exceptOrIntersect().size();
        for (int i = 1; i <= operatorCount; i++) {
            final var instanceofExpr = ctx.instanceofExpr(i);
            final var visitedType = instanceofExpr.accept(this);
            if (!visitedType.isSubtypeOf(zeroOrMoreNodes)) {
                error(ctx.instanceofExpr(i),
                    "Expression of operator node()* except/intersect node()* does match the type 'node()', received type: "
                        + expressionType.toString());
                expressionType = zeroOrMoreNodes;
            } else {
                if (ctx.exceptOrIntersect(i).EXCEPT() != null)
                    expressionType = expressionType.exceptionMerge(visitedType);
                else
                    expressionType = expressionType.intersectionMerge(visitedType);
            }
        }
        return expressionType;
    }

    @Override
    public XQuerySequenceType visitUnaryExpr(final UnaryExprContext ctx)
    {
        if (ctx.MINUS() == null && ctx.PLUS() == null) {
            return ctx.simpleMapExpr().accept(this);
        }
        final var type = ctx.simpleMapExpr().accept(this);
        if (!type.isSubtypeOf(number)) {
            error(ctx, "Arithmetic unary expression requires a number");
        }
        return number;
    }

    @Override
    public XQuerySequenceType visitSwitchExpr(final SwitchExprContext ctx) {
        final SwitchComparandContext switchComparand = ctx.switchComparand();

        final var comparand = switchComparand.switchedExpr.accept(this);
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
                .reduce(XQuerySequenceType::alternativeMerge)
                .get();
            if (!operandType.isSubtypeOf(comparand)) {
                error(clause, "Invalid operand type; " + operandType + " is not a subtype of " + comparand);
            }
            final var returned = clause.exprSingle().accept(this);
            if (merged == null) {
                merged = returned;
                continue;
            }
            merged = merged.alternativeMerge(returned);
        }
        return merged.alternativeMerge(defaultExpr.accept(this));
    }

    @Override
    public XQuerySequenceType visitArgument(final ArgumentContext ctx)
    {
        final var value = super.visitArgument(ctx);
        visitedPositionalArguments.add(value);
        return value;
    }

    @Override
    public XQuerySequenceType visitKeywordArgument(final KeywordArgumentContext ctx)
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

    private List<XQuerySequenceType> saveVisitedArguments()
    {
        final var saved = visitedPositionalArguments;
        visitedPositionalArguments = new ArrayList<>();
        return saved;
    }

    private Map<String, XQuerySequenceType> saveVisitedKeywordArguments()
    {
        final var saved = visitedKeywordArguments;
        visitedKeywordArguments = new HashMap<>();
        return saved;
    }

    void error(final ParserRuleContext where, final String message)
    {
        final Token start = where.getStart();
        final Token stop = where.getStop();
        errors.add(String.format("[line:%s, column:%s] %s [/line:%s, column:%s]",
            start.getLine(), start.getCharPositionInLine(),
            message,
            stop.getLine(), stop.getCharPositionInLine()));
    }

    void warn(final ParserRuleContext where, final String message)
    {
        final Token start = where.getStart();
        final Token stop = where.getStop();
        warnings.add(String.format("[line:%s, column:%s] %s [/line:%s, column:%s]",
            start.getLine(), start.getCharPositionInLine(),
            message,
            stop.getLine(), stop.getCharPositionInLine()));
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

    void addError(final ParserRuleContext where, final Function<ParserRuleContext, String> message)
    {
        final Token start = where.getStart();
        final Token stop = where.getStop();
        final int line = start.getLine();
        final int charPositionInLine = start.getCharPositionInLine();
        final LineEndCharPosEnd lineEndCharPosEnd = getLineEndCharPosEnd(stop);
        final int lineEnd = lineEndCharPosEnd.lineEnd();
        final int charPositionInLineEnd = lineEndCharPosEnd.charPosEnd();
        errors.add(String.format("[line:%s, column:%s] %s [/line:%s, column:%s]",
            line, charPositionInLine,
            message,
            lineEnd, charPositionInLineEnd));
    }

    @Override
    public XQuerySequenceType visitIfExpr(final IfExprContext ctx)
    {
        final var conditionType = ctx.expr().accept(this);
        if (!conditionType.hasEffectiveBooleanValue) {
            final var msg = String.format(
                "If condition must have an effective boolean value and the type %s doesn't have one",
                conditionType.toString());
            error(ctx, msg);
        }
        XQuerySequenceType trueType = null;
        XQuerySequenceType falseType = null;
        if (ctx.bracedAction() != null) {
            trueType = ctx.bracedAction().enclosedExpr().accept(this);
            falseType = emptySequence;
        } else {
            trueType = ctx.unbracedActions().exprSingle(0).accept(this);
            falseType = ctx.unbracedActions().exprSingle(1).accept(this);
        }
        // TODO: Add union types
        return trueType.alternativeMerge(falseType);
    }

    @Override
    public XQuerySequenceType visitStringConstructor(final StringConstructorContext ctx)
    {
        return typeFactory.string();
    }

    @Override
    public XQuerySequenceType visitInlineFunctionExpr(final InlineFunctionExprContext ctx)
    {
        // Is a focus function?
        if (ctx.functionSignature() == null) {
            // TODO: implement focus function
            return typeFactory.anyFunction();
        }
        final Set<String> argumentNames = new HashSet<>();
        final List<XQuerySequenceType> args = new ArrayList<>();
        final var functionSignature = ctx.functionSignature();
        final var returnTypeDeclaration = functionSignature.typeDeclaration();
        contextManager.enterScope();
        for (final var parameter : functionSignature.paramList().varNameAndType()) {
            final String parameterName = parameter.qname().getText();
            final TypeDeclarationContext typeDeclaration = parameter.typeDeclaration();
            final XQuerySequenceType parameterType = typeDeclaration != null
                ? typeDeclaration.accept(this)
                : anyItems;
            if (argumentNames.contains(parameterName))
                error(parameter, "Duplicate parameter name: " + parameterName);
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
                final String msg = String.format(
                    "Function body type %s is not a subtype of the declared return type %s",
                    inlineType.toString(), returnedType.toString());
                error(ctx.functionBody(), msg);
            }
        } else {
            returnedType = inlineType;
        }

        contextManager.leaveScope();
        return typeFactory.function(returnedType, args);
    }

    @Override
    public XQuerySequenceType visitEnclosedExpr(final EnclosedExprContext ctx)
    {
        if (ctx.expr() != null) {
            return ctx.expr().accept(this);
        }
        return emptySequence;
    }


    XQueryAxis currentAxis;

    private XQueryAxis saveAxis() {
        final var saved = currentAxis;
        currentAxis = null;
        return saved;
    }


}
