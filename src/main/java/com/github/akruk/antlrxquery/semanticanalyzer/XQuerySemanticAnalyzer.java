package com.github.akruk.antlrxquery.semanticanalyzer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.github.akruk.antlrxquery.AntlrXqueryParser.*;
import com.github.akruk.antlrxquery.contextmanagement.semanticcontext.XQuerySemanticContextManager;
import com.github.akruk.antlrxquery.AntlrXqueryParserBaseVisitor;
import com.github.akruk.antlrxquery.charescaper.XQuerySemanticCharEscaper;
import com.github.akruk.antlrxquery.charescaper.XQuerySemanticCharEscaper.XQuerySemanticCharEscaperResult;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.IXQuerySemanticFunctionManager;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.IXQuerySemanticFunctionManager.CallAnalysisResult;
import com.github.akruk.antlrxquery.typesystem.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.XQueryRecordField;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

public class XQuerySemanticAnalyzer extends AntlrXqueryParserBaseVisitor<XQuerySequenceType>  {
    final XQuerySemanticContextManager contextManager;
    final List<String> errors;
    final XQueryTypeFactory typeFactory;
    final XQueryValueFactory valueFactory;
    final IXQuerySemanticFunctionManager functionCaller;
    final Parser parser;
    XQueryVisitingSemanticContext context;
    List<XQuerySequenceType> visitedPositionalArguments;
    Map<String, XQuerySequenceType> visitedKeywordArguments;
    List<TupleElementType> visitedTupleStreamType;


    public List<String> getErrors() {
        return errors;
    }

    private record TupleElementType(String name, XQuerySequenceType type, String positionalName){};

    public XQuerySemanticAnalyzer(
            final Parser parser,
            final XQuerySemanticContextManager contextManager,
            final XQueryTypeFactory typeFactory,
            final XQueryValueFactory valueFactory,
            final IXQuerySemanticFunctionManager functionCaller)
    {
        this.context = new XQueryVisitingSemanticContext();
        this.parser = parser;
        this.typeFactory = typeFactory;
        this.valueFactory = valueFactory;
        this.functionCaller = functionCaller;
        this.contextManager = contextManager;
        this.contextManager.enterContext();
        this.context.setType(typeFactory.anyNode());
        this.context.setPositionType(typeFactory.number());
        this.errors = new ArrayList<>();
    }



    @Override
    public XQuerySequenceType visitFLWORExpr(final FLWORExprContext ctx) {
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

    private int saveReturnedOccurence() {
        final var saved = returnedOccurrence;
        returnedOccurrence = 1;
        return saved;
	}



	@Override
    public XQuerySequenceType visitLetClause(final LetClauseContext ctx) {
        for (final var letBinding : ctx.letBinding()) {
            final String variableName = letBinding.varName().getText();
            final XQuerySequenceType assignedValue = letBinding.exprSingle().accept(this);
            if (letBinding.typeDeclaration() == null) {
                contextManager.entypeVariable(variableName, assignedValue);
                continue;
            }
            final XQuerySequenceType type = letBinding.typeDeclaration().accept(this);
            if (!assignedValue.isSubtypeOf(type)) {
                final String msg = String.format("Type of variable %s is not compatible with the assigned value", variableName);
                addError(letBinding, msg);
            }
            contextManager.entypeVariable(variableName, type);
        }
        return null;
    }


    @Override
    public XQuerySequenceType visitForClause(final ForClauseContext ctx) {
        for (final var forBinding : ctx.forBinding()) {
            final String variableName = forBinding.varName().getText();
            final XQuerySequenceType sequenceType = forBinding.exprSingle().accept(this);
            returnedOccurrence = mergeFLWOROccurrence(sequenceType);

            if (forBinding.positionalVar() != null) {
                final String positionalVariableName = forBinding.positionalVar().varName().getText();
                contextManager.entypeVariable(positionalVariableName, typeFactory.number());
            }

            final XQueryItemType itemType = sequenceType.getItemType();
            final XQuerySequenceType iteratorType =  (forBinding.allowingEmpty() != null) ?
                                                        typeFactory.zeroOrOne(itemType) :
                                                        typeFactory.one(itemType);
            if (forBinding.typeDeclaration() == null) {
                contextManager.entypeVariable(variableName, iteratorType);
                continue;
            }
            final XQuerySequenceType type = forBinding.typeDeclaration().accept(this);
            if (!iteratorType.isSubtypeOf(type)) {
                final String msg = String.format(
                    "Type of variable %s is not compatible with the assigned value: %s is not subtype of %s",
                    variableName, iteratorType, type);
                addError(forBinding, msg);
            }
            contextManager.entypeVariable(variableName, type);
        }
        return null;
    }



    @Override
    public XQuerySequenceType visitSequenceType(final SequenceTypeContext ctx) {
        if (ctx.EMPTY_SEQUENCE() != null) {
            return typeFactory.emptySequence();
        }
        final var itemType = ctx.itemType().accept(this).getItemType();
        if (ctx.occurrenceIndicator() == null) {
            return typeFactory.one(itemType);
        }
        return switch(ctx.occurrenceIndicator().getText()) {
            case "?" -> typeFactory.zeroOrOne(itemType);
            case "*" -> typeFactory.zeroOrMore(itemType);
            case "+" -> typeFactory.oneOrMore(itemType);
            default -> null;
        };
    }

    @Override
    public XQuerySequenceType visitAnyItemTest(final AnyItemTestContext ctx) {
        return typeFactory.anyItem();
    }

    @Override
    public XQuerySequenceType visitChoiceItemType(final ChoiceItemTypeContext ctx) {
        List<ItemTypeContext> itemTypes = ctx.itemType();
        if (itemTypes.size() == 1) {
            return ctx.itemType(0).accept(this);
        }
        var choiceItemNames = itemTypes.stream().map(i->i.getText()).collect(Collectors.toSet());
        if (choiceItemNames.size() != itemTypes.size()) {
            addError(ctx, "Duplicated type signatures in choice item type declaration");
        }
        var choiceItems = itemTypes.stream().map(i->i.accept(this)).map(sequenceType->sequenceType.getItemType()).toList();
        return typeFactory.choice(choiceItems);
    }

    @Override
    public XQuerySequenceType visitTypeName(final TypeNameContext ctx) {
        // TODO: Add proper type resolution
        return switch(ctx.getText()) {
            case "number" -> typeFactory.number();
            case "string" -> typeFactory.string();
            case "boolean" -> typeFactory.boolean_();
            default -> {
                final String msg = String.format("Type %s is not recognized", ctx.getText());
                addError(ctx, msg);
                yield typeFactory.anyItem();
            }
        };
    }

    @Override
    public XQuerySequenceType visitAnyKindTest(final AnyKindTestContext ctx) {
        return typeFactory.anyNode();
    }

    @Override
    public XQuerySequenceType visitElementTest(final ElementTestContext ctx) {
        final Set<String> elementNames = ctx.nameTestUnion().nameTest().stream().map(e->e.toString()).collect(Collectors.toSet());
        return typeFactory.element(elementNames);
    }

    @Override
    public XQuerySequenceType visitFunctionType(final FunctionTypeContext ctx) {
        if (ctx.anyFunctionType() != null) {
            return typeFactory.anyFunction();
        }
        final var func = ctx.typedFunctionType();
        final List<XQuerySequenceType> parameterTypes = func.typedFunctionParam().stream()
                .map(p-> p.sequenceType().accept(this))
                .collect(Collectors.toList());
        return typeFactory.function(func.sequenceType().accept(this), parameterTypes);
    }

    @Override
    public XQuerySequenceType visitMapType(final MapTypeContext ctx) {
        if (ctx.anyMapType() != null) {
            return typeFactory.anyMap();
        }
        final var map = ctx.typedMapType();
        final XQueryItemType keyType = map.itemType().accept(this).getItemType();
        final XQuerySequenceType valueType = map.sequenceType().accept(this);
        return typeFactory.map(keyType, valueType);
    }

    @Override
    public XQuerySequenceType visitArrayType(final ArrayTypeContext ctx) {
        if (ctx.anyArrayType() != null) {
            return typeFactory.anyArray();
        }
        final var array = ctx.typedArrayType();
        final XQuerySequenceType sequenceType = array.sequenceType().accept(this);
        return typeFactory.array(sequenceType);
    }

    @Override
    public XQuerySequenceType visitRecordType(final RecordTypeContext ctx) {
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
    public XQuerySequenceType visitEnumerationType(final EnumerationTypeContext ctx) {
        final Set<String> enumMembers = ctx.STRING().stream()
                .map(TerminalNode::getText)
                .collect(Collectors.toSet());
        return typeFactory.enum_(enumMembers);
    }

    @Override
    public XQuerySequenceType visitCountClause(final CountClauseContext ctx) {
        final String countVariableName = ctx.varName().getText();
        final var number = typeFactory.number();
        contextManager.entypeVariable(countVariableName, number);
        return number;
    }

    @Override
    public XQuerySequenceType visitWhereClause(final WhereClauseContext ctx) {
        final var filteringExpression = ctx.exprSingle();
        final var filteringExpressionType = filteringExpression.accept(this);
        if (!filteringExpressionType.hasEffectiveBooleanValue()) {
            addError(filteringExpression, "Filtering expression must have effective boolean value");
        }
        returnedOccurrence = addOptionality(returnedOccurrence);
        return null;
    }

    @Override
    public XQuerySequenceType visitVarRef(final VarRefContext ctx) {
        final String variableName = ctx.varName().getText();
        final XQuerySequenceType variableValue = contextManager.getVariable(variableName);
        return variableValue;
    }


    private static final int[][] OCCURRENCE_MERGE_AUTOMATA = {
        // returnedOccurrence 0 (Zero)
        {0, 0, 0, 0, 0},
        // returnedOccurrence 1 (One)
        {0, 1, 2, 3, 4},
        // returnedOccurrence 2 (ZeroOrOne)
        {0, 2, 2, 3, 3},
        // returnedOccurrence 3 (ZeroOrMore)
        {0, 3, 3, 3, 3},
        // returnedOccurrence 4 (OneOrMore/Other)
        {0, 4, 3, 3, 4}
    };

    private int occurrence(final XQuerySequenceType type) {
        if (type.isZero()) return 0;
        if (type.isOne()) return 1;
        if (type.isZeroOrOne()) return 2;
        if (type.isZeroOrMore()) return 3;
        return 4;
    }

    private int mergeFLWOROccurrence(final XQuerySequenceType type) {
        final int typeOccurrence = occurrence(type);
        return OCCURRENCE_MERGE_AUTOMATA[returnedOccurrence][typeOccurrence];
    }

    @Override
    public XQuerySequenceType visitReturnClause(final ReturnClauseContext ctx) {
        final var type = ctx.exprSingle().accept(this);
        final var itemType = type.getItemType();
        returnedOccurrence = mergeFLWOROccurrence(type);
        return switch (returnedOccurrence) {
            case 0 -> typeFactory.emptySequence();
            case 1 -> typeFactory.one(itemType);
            case 2 -> typeFactory.zeroOrOne(itemType);
            case 3 -> typeFactory.zeroOrMore(itemType);
            default -> typeFactory.oneOrMore(itemType);
        };
    }

    @Override
    public XQuerySequenceType visitWhileClause(final WhileClauseContext ctx) {
        final var filteringExpression = ctx.exprSingle();
        final var filteringExpressionType = filteringExpression.accept(this);
        if (!filteringExpressionType.hasEffectiveBooleanValue()) {
            addError(filteringExpression, "Filtering expression must have effective boolean value");
        }
        returnedOccurrence = addOptionality(returnedOccurrence);
        return null;

    }



    private int addOptionality(final int occurence) {
        return switch(returnedOccurrence){
            case 0 -> 0;
            case 1 -> 2;
            case 2 -> 2;
            default -> 3;
        };
    }

    @Override
    public XQuerySequenceType visitLiteral(final LiteralContext ctx) {
        if (ctx.STRING() != null) {
            final String rawText = ctx.getText();
            final String content = unescapeString(ctx, rawText.substring(1, rawText.length() - 1));
            valueFactory.string(content);
            return typeFactory.string();
        }

        final var numeric = ctx.numericLiteral();
        if (numeric.IntegerLiteral() != null) {
            final String value = numeric.IntegerLiteral().getText().replace("_", "");
            valueFactory.number(new BigDecimal(value));
            return typeFactory.number();
        }

        if (numeric.HexIntegerLiteral() != null) {
            final String raw = numeric.HexIntegerLiteral().getText();
            final String hex = raw.replace("_", "").substring(2);
            valueFactory.number(new BigDecimal(new java.math.BigInteger(hex, 16)));
            return typeFactory.number();
        }

        if (numeric.BinaryIntegerLiteral() != null) {
            final String raw = numeric.BinaryIntegerLiteral().getText();
            final String binary = raw.replace("_", "").substring(2);
            valueFactory.number(new BigDecimal(new java.math.BigInteger(binary, 2)));
            return typeFactory.number();
        }

        if (numeric.DecimalLiteral() != null) {
            final String cleaned = numeric.DecimalLiteral().getText().replace("_", "");
            valueFactory.number(new BigDecimal(cleaned));
            return typeFactory.number();
        }

        if (numeric.DoubleLiteral() != null) {
            final String cleaned = numeric.DoubleLiteral().getText().replace("_", "");
            valueFactory.number(new BigDecimal(cleaned));
            return typeFactory.number();
        }
        return null;
    }


    @Override
    public XQuerySequenceType visitParenthesizedExpr(final ParenthesizedExprContext ctx) {
        // Empty parentheses mean an empty sequence '()'
        if (ctx.expr() == null) {
            valueFactory.sequence(List.of());
            return typeFactory.emptySequence();
        }
        return ctx.expr().accept(this);
    }

    @Override
    public XQuerySequenceType visitExpr(final ExprContext ctx) {
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

    private String unescapeString(final ParserRuleContext where, final String str) {
        final var charEscaper = new XQuerySemanticCharEscaper();
        final XQuerySemanticCharEscaperResult result = charEscaper.escapeWithDiagnostics(str);
        for (final var e : result.errors()){
            addError(where, e.message());
        }
        return result.unescaped();
    }

    @Override
    public XQuerySequenceType visitFunctionCall(final FunctionCallContext ctx) {
        final String fullName = ctx.functionName().getText();
        final String namespaces[] = fullName.split(":", 2);
        final boolean hasNamespace = namespaces.length == 2;
        final String namespace = (hasNamespace)? namespaces[0] : defaultNamespace;
        final String functionName = (hasNamespace)? namespaces[1] : namespaces[0];

        final var savedArgs = saveVisitedArguments();
        final var savedKwargs = saveVisitedKeywordArguments();

        ctx.argumentList().accept(this);

        final CallAnalysisResult callAnalysisResult = functionCaller.call(
            namespace, functionName, visitedPositionalArguments, visitedKeywordArguments, context);
        errors.addAll(callAnalysisResult.errors());

        visitedPositionalArguments = savedArgs;
        visitedKeywordArguments = savedKwargs;
        return callAnalysisResult.result();
    }

    @Override
    public XQuerySequenceType visitQuantifiedExpr(final QuantifiedExprContext ctx) {
        final List<String> variableNames = ctx.varName().stream()
            .map(VarNameContext::qname)
            .map(QnameContext::getText)
            .toList();
        final int variableExpressionCount = ctx.exprSingle().size()-1;
        final List<XQuerySequenceType> variableTypes = new ArrayList<>(variableExpressionCount);
        for (final var expr : ctx.exprSingle().subList(0, variableExpressionCount)) {
            final var sequenceType = expr.accept(this);
            variableTypes.add(sequenceType);
        }

        final var criterionNode = ctx.exprSingle().getLast();
        for (int i = 0; i < variableNames.size(); i++) {
            contextManager.entypeVariable(variableNames.get(i), variableTypes.get(i));
        }
        final XQuerySequenceType queriedType = criterionNode.accept(this);
        if (!queriedType.hasEffectiveBooleanValue()) {
            addError(criterionNode, "Criterion value needs to have effective boolean value");
        }
        return typeFactory.boolean_();
    }


    @Override
    public XQuerySequenceType visitOrExpr(final OrExprContext ctx) {
        if (ctx.OR().isEmpty()) {
            return ctx.andExpr(0).accept(this);
        }
        final XQuerySequenceType boolean_ = typeFactory.boolean_();
        final var orCount = ctx.OR().size();
        for (int i = 0; i <= orCount; i++) {
            final var visitedType = ctx.andExpr(i).accept(this);
            if (!visitedType.hasEffectiveBooleanValue()) {
                addError(ctx.andExpr(i), "Operands of 'or expression' need to have effective boolean value");
            }
            i++;
        }
        return boolean_;
    }



    @Override
    public XQuerySequenceType visitRangeExpr(final RangeExprContext ctx) {
        if (ctx.TO() == null) {
            return ctx.additiveExpr(0).accept(this);
        }
        final var fromValue = ctx.additiveExpr(0).accept(this);
        final var toValue = ctx.additiveExpr(1).accept(this);
        final var optionalNumber = typeFactory.zeroOrOne(typeFactory.itemNumber());
        if (!fromValue.isSubtypeOf(optionalNumber)) {
            addError(ctx.additiveExpr(0), "Wrong type in 'from' operand of 'range expression': '<number?> to <number?>'");
        }
        if (!toValue.isSubtypeOf(optionalNumber)) {
            addError(ctx.additiveExpr(1), "Wrong type in 'to' operand of range expression: '<number?> to <number?>'");
        }
        return typeFactory.zeroOrMore(typeFactory.itemNumber());
    }


    @Override
    public XQuerySequenceType visitPathExpr(final PathExprContext ctx) {
        final boolean pathExpressionFromRoot = ctx.SLASH() != null;
        if (pathExpressionFromRoot) {
            // TODO: Context nodes
            final var resultingNodeSequence = ctx.relativePathExpr().accept(this);
            return resultingNodeSequence;
        }
        final boolean useDescendantOrSelfAxis = ctx.SLASHES() != null;
        if (useDescendantOrSelfAxis) {
            final var resultingNodeSequence = ctx.relativePathExpr().accept(this);
            return resultingNodeSequence;
        }
        return ctx.relativePathExpr().accept(this);
    }

    @Override
    public XQuerySequenceType visitRelativePathExpr(final RelativePathExprContext ctx) {
        if (ctx.pathOperator().isEmpty()) {
            return ctx.stepExpr(0).accept(this);
        }
        // final XQuerySequenceType visitedNodeSequence = ctx.stepExpr(0).accept(this);
        final var operationCount = ctx.pathOperator().size();
        for (int i = 1; i <= operationCount; i++) {
            // matchedNodes = switch (ctx.pathOperator(i-1).getText()) {
            //     case "//" -> {
            //         List<ParseTree> descendantsOrSelf = getAllDescendantsOrSelf(matchedTreeNodes());
            //         yield ctx.stepExpr(i).accept(this);
            //     }
            //     case "/" -> ctx.stepExpr(i).accept(this);
            //     default -> null;
            // };
            i++;
        }
        return null;
    }

    @Override
    public XQuerySequenceType visitStepExpr(final StepExprContext ctx) {
        if (ctx.postfixExpr() != null)
            return ctx.postfixExpr().accept(this);
        return ctx.axisStep().accept(this);
    }


    @Override
    public XQuerySequenceType visitAxisStep(final AxisStepContext ctx) {
        XQuerySequenceType stepResult = null;
        if (ctx.reverseStep() != null)
            stepResult = ctx.reverseStep().accept(this);
        else if (ctx.forwardStep() != null)
            stepResult = ctx.forwardStep().accept(this);
        if (ctx.predicateList().predicate().isEmpty()) {
            return stepResult;
        }
        final var savedArgs = saveVisitedArguments();
        for (final var predicate : ctx.predicateList().predicate()) {
            stepResult = predicate.accept(this);
        }
        visitedPositionalArguments = savedArgs;
        return stepResult;
    }

    // @Override
    // public XQuerySequenceType visitPredicateList(PredicateListContext ctx) {
    //     var result = match;
    //     for (var predicate : ctx.predicate()) {
    //         predicate.accept(this);
    //     }
    //     return matchedTreeNodes();
    // }

    @Override
    public XQuerySequenceType visitPostfixExpr(final PostfixExprContext ctx) {
        if (ctx.postfix().isEmpty()) {
            return ctx.primaryExpr().accept(this);
        }

        final var savedArgs = saveVisitedArguments();
        var value = ctx.primaryExpr().accept(this);
        for (final var postfix : ctx.postfix()) {
            context.setType(value);
            value = postfix.accept(this);
        }
        visitedPositionalArguments = savedArgs;
        return value;
    }

    @Override
    public XQuerySequenceType visitPredicate(final PredicateContext ctx) {
        final var contextType = context.getType();
        final var predicateExpression = ctx.expr().accept(this);
        if (predicateExpression.isSubtypeOf(typeFactory.emptySequence()))
            return typeFactory.emptySequence();
        if (predicateExpression.isSubtypeOf(typeFactory.zeroOrOne(typeFactory.itemNumber()))) {
            final var item = contextType.getItemType();
            final var decucedType =  typeFactory.zeroOrOne(item);
            return decucedType;
        }
        if (predicateExpression.isSubtypeOf(typeFactory.zeroOrMore(typeFactory.itemNumber()))) {
            final var item = contextType.getItemType();
            final var decucedType =  typeFactory.zeroOrMore(item);
            context.setType(decucedType);
            return decucedType;
        }
        if (!predicateExpression.hasEffectiveBooleanValue()) {
            final var msg = String.format("Predicate requires either number* type (for item by index aquisition) or a value that has effective boolean value, provided type: %s", predicateExpression);
            addError(ctx.expr(), msg);
        }
        return contextType.addOptionality();
    }

    // private XQueryVisitingSemanticContext saveContext() {
    //     var saved = context;
    //     context = new XQueryVisitingSemanticContext();
    //     return saved;
    // }



    @Override
    public XQuerySequenceType visitContextItemExpr(final ContextItemExprContext ctx) {
        return context.getType();
    }

    @Override
    public XQuerySequenceType visitForwardStep(final ForwardStepContext ctx) {
        if (ctx.forwardAxis() != null) {
            ctx.forwardAxis().accept(this);
        }
        else {
        }
        return ctx.nodeTest().accept(this);
    }

    @Override
    public XQuerySequenceType visitReverseStep(final ReverseStepContext ctx) {
        if (ctx.abbrevReverseStep() != null) {
            return ctx.abbrevReverseStep().accept(this);
        }
        ctx.reverseAxis().accept(this);
        return ctx.nodeTest().accept(this);
    }

    @Override
    public XQuerySequenceType visitNodeTest(final NodeTestContext ctx) {
        return ctx.nameTest().accept(this);
    }

    private final Predicate<String> canBeTokenName = Pattern.compile("^[\\p{IsUppercase}].*").asPredicate();
    private final String defaultNamespace = "fn";

    @Override
    public XQuerySequenceType visitNameTest(final NameTestContext ctx) {
        if (ctx.wildcard() != null) {
            return switch (ctx.wildcard().getText()) {
                case "*" -> typeFactory.zeroOrMore(typeFactory.itemAnyNode());
                // case "*:" -> ;
                // case ":*" -> ;
                default -> throw new AssertionError("Not implemented wildcard");
            };
        }
        final String name = ctx.qname().toString();
        if (canBeTokenName.test(name)) {
            // test for token type
            final int tokenType = parser.getTokenType(name);
            if (tokenType == Token.INVALID_TYPE) {
                final String msg = String.format("Token name: %s is not recognized by parser %s", name, parser.toString());
                addError(ctx.qname(), msg);
            }
            return typeFactory.zeroOrMore(typeFactory.itemElement(Set.of(name)));
        } else { // test for rule
            final int ruleIndex = parser.getRuleIndex(name);
            if (ruleIndex == -1) {
                final String msg = String.format("Rule name: %s is not recognized by parser %s", name, parser.toString());
                addError(ctx.qname(), msg);
            }
            return typeFactory.zeroOrMore(typeFactory.itemElement(Set.of(name)));
        }
    }

    @Override
    public XQuerySequenceType visitStringConcatExpr(final StringConcatExprContext ctx) {
        if (ctx.CONCATENATION().isEmpty()) {
            return ctx.rangeExpr(0).accept(this);
        }
        final XQuerySequenceType anyItemOptional = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
        for (int i = 0; i < ctx.rangeExpr().size(); i++) {
            final var visitedType = ctx.rangeExpr(i).accept(this);
            if (!visitedType.isSubtypeOf(anyItemOptional)) {
                addError(ctx.rangeExpr(i), "Operands of 'or expression' need to be subtype of item()?");
            }
        }
        return typeFactory.string();
    }

    @Override
    public XQuerySequenceType visitSimpleMapExpr(final SimpleMapExprContext ctx) {
        if (ctx.EXCLAMATION_MARK().isEmpty())
            return ctx.pathExpr(0).accept(this);
        final XQuerySequenceType firstExpressionType = ctx.pathExpr(0).accept(this);
        final XQuerySequenceType iterator = firstExpressionType.iteratedItem();
        context.setType(iterator);
        XQuerySequenceType result = firstExpressionType;
        final var theRest = ctx.pathExpr().subList(1, ctx.pathExpr().size());
        for (final var mappedExpression: theRest) {
            final XQuerySequenceType type = mappedExpression.accept(this);
            result = result.mapping(type);
            context.setType(result.iteratedItem());
        }
        return result;
    }


    @Override
    public XQuerySequenceType visitArrowFunctionSpecifier(final ArrowFunctionSpecifierContext ctx) {
        if (ctx.ID() != null) {
            // final CallAnalysisResult call = functionCaller.getFunctionReference(ctx.ID().getText(), typeFactory);
        }


        if (ctx.varRef() != null)
            return ctx.varRef().accept(this);
        return ctx.parenthesizedExpr().accept(this);

    }


    @Override
    public XQuerySequenceType visitAndExpr(final AndExprContext ctx) {
        if (ctx.AND().isEmpty()) {
            return ctx.comparisonExpr(0).accept(this);
        }
        final XQuerySequenceType boolean_ = typeFactory.boolean_();
        final var operatorCount = ctx.AND().size();
        for (int i = 0; i <= operatorCount; i++) {
            final var visitedType = ctx.comparisonExpr(i).accept(this);
            if (!visitedType.hasEffectiveBooleanValue()) {
                addError(ctx.comparisonExpr(i), "Operands of 'or expression' need to have effective boolean value");
            }
            i++;
        }
        return boolean_;
    }

    @Override
    public XQuerySequenceType visitAdditiveExpr(final AdditiveExprContext ctx) {
        if (ctx.additiveOperator().isEmpty()) {
            return ctx.multiplicativeExpr(0).accept(this);
        }
        final XQuerySequenceType number = typeFactory.number();
        for (final var operandExpr : ctx.multiplicativeExpr()) {
            final var operand = operandExpr.accept(this);
            if (!operand.isSubtypeOf(number)) {
                addError(operandExpr, "Operands in additive expression must be numeric, received: " + operand.toString());
            }
        }
        return typeFactory.number();
    }

    @Override
    public XQuerySequenceType visitComparisonExpr(final ComparisonExprContext ctx) {
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

    private XQuerySequenceType handleGeneralComparison(final ComparisonExprContext ctx) {
        final var leftHandSide = ctx.otherwiseExpr(0).accept(this);
        final var rightHandSide = ctx.otherwiseExpr(1).accept(this);
        if (!leftHandSide.isSubtypeOf(rightHandSide)) {
            final String msg = String.format("The types: %s and %s in general comparison are not comparable",
                    leftHandSide.toString(), rightHandSide.toString());
            addError(ctx, msg);
        }
        return typeFactory.boolean_();
    }

    private XQuerySequenceType handleValueComparison(final ComparisonExprContext ctx) {
        final var leftHandSide = ctx.otherwiseExpr(0).accept(this);
        final var rightHandSide = ctx.otherwiseExpr(1).accept(this);
        final var optionalItem = typeFactory.zeroOrOne(typeFactory.itemAnyItem());
        final var optionalBoolean = typeFactory.zeroOrOne(typeFactory.itemBoolean());
        if (!leftHandSide.isSubtypeOf(optionalItem)) {
            addError(ctx.otherwiseExpr(0),
                    "Left hand side of 'or expression' must be of type 'item()?', received: " + leftHandSide.toString());
        }
        if (!rightHandSide.isSubtypeOf(optionalItem)) {
            addError(ctx.otherwiseExpr(1),
                    "Right hand side of 'or expression' must be of type 'item()?', received: "  + leftHandSide.toString());
        }
        if (!leftHandSide.isValueComparableWith(rightHandSide)) {
            final String msg = String.format("The types: %s and %s in value comparison are not comparable",
                                        leftHandSide.toString(), rightHandSide.toString());
            addError(ctx, msg);
        }
        if (leftHandSide.isSubtypeOf(typeFactory.anyItem())
                && rightHandSide.isSubtypeOf(typeFactory.anyItem()))
        {
            return typeFactory.boolean_();
        }
        return optionalBoolean;
    }

    private XQuerySequenceType handleNodeComp(final ComparisonExprContext ctx) {
        final var anyNode = typeFactory.zeroOrOne(typeFactory.itemAnyNode());
        final var optionalBoolean = typeFactory.zeroOrOne(typeFactory.itemBoolean());
        final var visitedLeft = ctx.otherwiseExpr(0).accept(this);
        if (!visitedLeft.isSubtypeOf(anyNode)) {
            addError(ctx.otherwiseExpr(0), "Operands of node comparison must be of type 'node()?', received: "+ visitedLeft.toString());
        }
        final var visitedRight = ctx.otherwiseExpr(1).accept(this);
        if (!visitedRight.isSubtypeOf(anyNode)) {
            addError(ctx.otherwiseExpr(1), "Operands of node comparison must be of type 'node()?', received: " +visitedRight.toString());
        }
        return optionalBoolean;

    }

    @Override
    public XQuerySequenceType visitMultiplicativeExpr(final MultiplicativeExprContext ctx) {
        if (ctx.multiplicativeOperator().isEmpty()) {
            return ctx.unionExpr(0).accept(this);
        }
        final XQuerySequenceType number = typeFactory.number();
        for (final var expr: ctx.unionExpr()) {
            final var visitedType = expr.accept(this);
            if (!visitedType.isSubtypeOf(number)) {
                addError(ctx, "Multiplicative expression requires a number, received: " + visitedType.toString());
            }
        }
        return typeFactory.number();
    }

    @Override
    public XQuerySequenceType visitOtherwiseExpr(final OtherwiseExprContext ctx) {
        if (ctx.OTHERWISE().isEmpty())
            return ctx.stringConcatExpr(0).accept(this);
        final int length = ctx.stringConcatExpr().size();
        XQuerySequenceType merged = ctx.stringConcatExpr(0).accept(this);
        for (int i = 1; i < length; i++) {
            final var expr = ctx.stringConcatExpr(i);
            final XQuerySequenceType exprType = expr.accept(this);
            merged = exprType.typeAlternative(merged);
        }
        return merged;
    }

    @Override
    public XQuerySequenceType visitUnionExpr(final UnionExprContext ctx) {
        if (ctx.unionOperator().isEmpty()) {
            return ctx.intersectExpr(0).accept(this);
        }
        final var zeroOrMoreNodes = typeFactory.zeroOrMore(typeFactory.itemAnyNode());
        var expressionNode = ctx.intersectExpr(0);
        var expressionType = expressionNode.accept(this);
        if (!expressionType.isSubtypeOf(zeroOrMoreNodes)) {
            addError(expressionNode, "Expression of union operator node()* | node()* does match the type 'node()', received type: " + expressionType.toString());
            expressionType = zeroOrMoreNodes;
        }
        final var unionCount = ctx.unionOperator().size();
        for (int i = 1; i <= unionCount; i++) {
            expressionNode = ctx.intersectExpr(i);
            final var visitedType = expressionNode.accept(this);
            if (!visitedType.isSubtypeOf(zeroOrMoreNodes)) {
                addError(expressionNode, "Expression of union operator node()* | node()* does match the type 'node()', received type: " + expressionType.toString());
                expressionType = zeroOrMoreNodes;
            } else {
                expressionType = expressionType.unionMerge(visitedType);
            }
        }
        return expressionType;
    }

    @Override
    public XQuerySequenceType visitIntersectExpr(final IntersectExprContext ctx) {
        if (ctx.exceptOrIntersect().isEmpty()) {
            return ctx.instanceofExpr(0).accept(this);
        }
        var expressionType = ctx.instanceofExpr(0).accept(this);
        final var zeroOrMoreNodes = typeFactory.zeroOrMore(typeFactory.itemAnyNode());
        if (!expressionType.isSubtypeOf(zeroOrMoreNodes)) {
            addError(ctx.instanceofExpr(0),
                    "Expression of operator node()* except/intersect node()* does match the type 'node()', received type: " + expressionType.toString());
            expressionType = zeroOrMoreNodes;
        }
        final var operatorCount = ctx.exceptOrIntersect().size();
        for (int i = 1; i <= operatorCount; i++) {
            final var instanceofExpr = ctx.instanceofExpr(i);
            final var visitedType = instanceofExpr.accept(this);
            if (!visitedType.isSubtypeOf(zeroOrMoreNodes)) {
                addError(ctx.instanceofExpr(i),
                        "Expression of operator node()* except/intersect node()* does match the type 'node()', received type: " + expressionType.toString());
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
    public XQuerySequenceType visitUnaryExpr(final UnaryExprContext ctx) {
        if (ctx.MINUS() == null && ctx.PLUS() == null) {
            return ctx.simpleMapExpr().accept(this);
        }
        final var type = ctx.simpleMapExpr().accept(this);
        if (!type.isSubtypeOf(typeFactory.number())) {
            addError(ctx, "Arithmetic unary expression requires a number");
        }
        return typeFactory.number();
    }

    @Override
    public XQuerySequenceType visitSwitchExpr(final SwitchExprContext ctx) {
        final Map<XQuerySequenceType, ParseTree> valueToExpression = ctx.switchCaseClause().stream()
                .flatMap(clause -> clause.switchCaseOperand()
                                            .stream().map(operand -> Map.entry(operand.accept(this), clause.exprSingle())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        final XQuerySequenceType switchedValue = ctx.switchedExpr.accept(this);
        final ParseTree toBeExecuted = valueToExpression.getOrDefault(switchedValue, ctx.defaultExpr);
        return toBeExecuted.accept(this);
    }


    @Override
    public XQuerySequenceType visitArgument(final ArgumentContext ctx) {
        final var value =  super.visitArgument(ctx);
        visitedPositionalArguments.add(value);
        return value;
    }

    @Override
    public XQuerySequenceType visitKeywordArgument(KeywordArgumentContext ctx) {
        final ExprSingleContext keywordAssignedTypeExpr = ctx.argument().exprSingle();
        if (keywordAssignedTypeExpr != null) {
            final var keywordType = keywordAssignedTypeExpr.accept(this);
            final String keyword = ctx.qname().getText();
            visitedKeywordArguments.put(keyword, keywordType);
        }
        // TODO: add placeholder
        return null;

    }


    private List<XQuerySequenceType> saveVisitedArguments() {
        final var saved = visitedPositionalArguments;
        visitedPositionalArguments = new ArrayList<>();
        return saved;
    }

    private Map<String, XQuerySequenceType> saveVisitedKeywordArguments() {
        final var saved = visitedKeywordArguments;
        visitedKeywordArguments = new HashMap<>();
        return saved;
    }

    void addError(final ParserRuleContext where, final String message) {
        final Token start = where.getStart();
        final Token stop = where.getStop();
        errors.add(String.format("[line:%s, column:%s] %s [/line:%s, column:%s]",
                start.getLine(), start.getCharPositionInLine(),
                message,
                stop.getLine(), stop.getCharPositionInLine()));
    }


    record LineEndCharPosEnd(int lineEnd, int charPosEnd) {
    }

    LineEndCharPosEnd getLineEndCharPosEnd(final Token end) {
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
        final int charPositionInLineEnd = newlineCount == 0 ?
                end.getCharPositionInLine() + length : length - lastNewlineIndex;
        return new LineEndCharPosEnd(lineEnd, charPositionInLineEnd);
    }

    void addError(final ParserRuleContext where, final Function<ParserRuleContext, String> message) {
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
    public XQuerySequenceType visitIfExpr(final IfExprContext ctx) {
        final var conditionType = ctx.expr().accept(this);
        if (!conditionType.hasEffectiveBooleanValue()) {
            final var msg = String.format(
                    "If condition must have an effective boolean value and the type %s doesn't have one",
                    conditionType.toString());
            addError(ctx, msg);
        }
        XQuerySequenceType trueType = null;
        XQuerySequenceType falseType = null;
        if (ctx.bracedAction() != null) {
            trueType = ctx.bracedAction().enclosedExpr().accept(this);
            falseType = typeFactory.emptySequence();
        }
        else {
            trueType = ctx.unbracedActions().exprSingle(0).accept(this);
            falseType = ctx.unbracedActions().exprSingle(1).accept(this);
        }
        // TODO: Add union types
        return trueType.typeAlternative(falseType);
    }

    @Override
    public XQuerySequenceType visitStringConstructor(final StringConstructorContext ctx) {
        return typeFactory.string();
    }


}
