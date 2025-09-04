package com.github.akruk.antlrxquery.languageserver;

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import com.github.akruk.antlrxquery.AntlrXqueryParser.*;
import com.github.akruk.antlrxquery.semanticanalyzer.XQuerySemanticAnalyzer;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.XQuerySemanticContextManager;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.defaults.XQuerySemanticFunctionManager;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;
import com.github.akruk.antlrxquery.inputgrammaranalyzer.InputGrammarAnalyzer.GrammarAnalysisResult;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;


public class VariableAnalyzer extends XQuerySemanticAnalyzer {

    public record TypedVariable(Range range, String name, XQuerySequenceType type) {}
    public static final List<TypedVariable> variablesMappedToTypes = new ArrayList<>();
    private final XQueryTypeFactory typeFactory;
    private final XQuerySemanticContextManager contextManager;

    public VariableAnalyzer(
        final Parser parser,
        final XQuerySemanticContextManager contextManager,
        final XQueryTypeFactory typeFactory,
        final XQueryValueFactory valueFactory,
        final XQuerySemanticFunctionManager functionCaller,
        final GrammarAnalysisResult grammarAnalysisResult)
    {
        super(parser, contextManager, typeFactory, valueFactory, functionCaller, grammarAnalysisResult);
        this.contextManager = contextManager;
        this.typeFactory = typeFactory;
    }


    @Override
    public XQuerySequenceType visitLetClause(final LetClauseContext ctx)
    {
        var og = super.visitLetClause(ctx);
        for (final var letBinding : ctx.letBinding()) {
            final String variableName = letBinding.varName().getText();
            final XQuerySequenceType assignedType = letBinding.exprSingle().accept(this);
            final var range = getRange(letBinding.DOLLAR(), letBinding.varName());
            if (letBinding.typeDeclaration() == null) {
                variablesMappedToTypes.add(new TypedVariable(range, variableName, assignedType));
                continue;
            }
            final XQuerySequenceType type = letBinding.typeDeclaration().accept(this);
            variablesMappedToTypes.add(new TypedVariable(range, variableName, type));
        }
        return og;
    }


    private Range getRange(final TerminalNode startRule, final ParserRuleContext endRule) {
        Token dollarSymbol = startRule.getSymbol();
        var startPosition = new Position(
            dollarSymbol.getLine()-1,
            dollarSymbol.getCharPositionInLine()
        );
        Token stopSymbol = endRule.getStop();
        var endPosition = new Position(
            stopSymbol.getLine()-1,
            stopSymbol.getCharPositionInLine() + endRule.getText().length()
        );
        var range = new Range(startPosition, endPosition);
        return range;
    }

    @Override
    public void processForItemBinding(final ForItemBindingContext ctx) {
        super.processForItemBinding(ctx);
        final String variableName = ctx.varNameAndType().qname().getText();
        final XQuerySequenceType sequenceType = ctx.exprSingle().accept(this);
        final XQueryItemType itemType = sequenceType.itemType;
        final XQuerySequenceType iteratorType = (ctx.allowingEmpty() != null)
                ? typeFactory.zeroOrOne(itemType)
                : typeFactory.one(itemType);

        processVariableTypeDeclaration(ctx.varNameAndType(), iteratorType, variableName, ctx);
        handlePositionalVariable(ctx.positionalVar());
    }


    private void handlePositionalVariable(final PositionalVarContext ctx) {
        if (ctx != null) {
            final String positionalVariableName = ctx.varName().getText();
            var range = getRange(ctx.DOLLAR(), ctx.varName());
            variablesMappedToTypes.add(new TypedVariable(range, positionalVariableName, number));
        }
    }

    @Override
    public void processForMemberBinding(final ForMemberBindingContext ctx) {
        super.processForMemberBinding(ctx);

        final String variableName = ctx.varNameAndType().qname().getText();
        final XQuerySequenceType arrayType = ctx.exprSingle().accept(this);

        final XQuerySequenceType memberType = arrayType.itemType.arrayMemberType;
        final XQuerySequenceType iteratorType = memberType.addOptionality();

        processVariableTypeDeclaration(ctx.varNameAndType(), iteratorType, variableName, ctx);
        handlePositionalVariable(ctx.positionalVar());
    }


    @Override
    public void processForEntryBinding(final ForEntryBindingContext ctx) {
        super.processForEntryBinding(ctx);

        final XQuerySequenceType mapType = ctx.exprSingle().accept(this);

        final ForEntryKeyBindingContext keyBinding = ctx.forEntryKeyBinding();
        final ForEntryValueBindingContext valueBinding = ctx.forEntryValueBinding();

        // Process key binding
        if (keyBinding != null) {
            final String keyVariableName = keyBinding.varNameAndType().qname().getText();
            final XQueryItemType keyType = mapType.itemType.mapKeyType;
            final XQuerySequenceType keyIteratorType = typeFactory.one(keyType);

            processVariableTypeDeclaration(keyBinding.varNameAndType(), keyIteratorType, keyVariableName, ctx);
        }

        // Process value binding
        if (valueBinding != null) {
            final String valueVariableName = valueBinding.varNameAndType().qname().getText();
            final XQuerySequenceType valueType = mapType.itemType.mapValueType;

            processVariableTypeDeclaration(valueBinding.varNameAndType(), valueType, valueVariableName, ctx);
        }

        handlePositionalVariable(ctx.positionalVar());
    }

    @Override
    protected void processVariableTypeDeclaration(final VarNameAndTypeContext varNameAndType,
                                            final XQuerySequenceType inferredType,
                                            final String variableName,
                                            final ParseTree context)
    {
        super.processVariableTypeDeclaration(varNameAndType, inferredType, variableName, context);
        final var range = getRange(varNameAndType.DOLLAR(), varNameAndType.qname());
        if (varNameAndType.typeDeclaration() == null) {
            variablesMappedToTypes.add(new TypedVariable(range, variableName, inferredType));
            return;
        }

        final XQuerySequenceType declaredType = varNameAndType.typeDeclaration().accept(this);
        variablesMappedToTypes.add(new TypedVariable(range, variableName, declaredType));
    }

    @Override
    public XQuerySequenceType visitCountClause(final CountClauseContext ctx)
    {
        var og = super.visitCountClause(ctx);
        final String countVariableName = ctx.varName().getText();
        final var range = getRange(ctx.DOLLAR(), ctx.varName());
        variablesMappedToTypes.add(new TypedVariable(range, countVariableName, number));
        return og;
    }

    @Override
    public XQuerySequenceType visitVarRef(final VarRefContext ctx)
    {
        var og = super.visitVarRef(ctx);
        final String variableName = ctx.varName().getText();
        final XQuerySequenceType variableType = contextManager.getVariable(variableName);
        final var range = getRange(ctx.DOLLAR(), ctx.varName());
        variablesMappedToTypes.add(new TypedVariable(range, variableName, variableType));
        return og;
    }

    @Override
    public XQuerySequenceType visitQuantifiedExpr(final QuantifiedExprContext ctx) {
        var og = super.visitQuantifiedExpr(ctx);
        final List<QuantifierBindingContext> quantifierBindings = ctx.quantifierBinding();

        final List<VarNameAndTypeContext> variables = quantifierBindings.stream()
                .map(binding -> binding.varNameAndType())
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


        for (int i = 0; i < variables.size(); i++) {
            final var assignedType = variableTypes.get(i);
            final var desiredType = coercedTypes.get(i);
            final VarNameAndTypeContext varNameAndTypeContext = variables.get(i);
            final var range = getRange(varNameAndTypeContext.DOLLAR(), varNameAndTypeContext.qname());
            final String varname = varNameAndTypeContext.qname().getText();
            if (desiredType != null) {
                variablesMappedToTypes.add(
                    new TypedVariable(range, varname, desiredType));

            } else {
                variablesMappedToTypes.add(
                    new TypedVariable(range, varname, assignedType));
            }
        }
        return og;
    }

    @Override
    public XQuerySequenceType visitInlineFunctionExpr(final InlineFunctionExprContext ctx)
    {
        final var og = super.visitInlineFunctionExpr(ctx);
        final var functionSignature = ctx.functionSignature();
        for (final var parameter : functionSignature.paramList().varNameAndType()) {
            final String parameterName = parameter.qname().getText();
            final TypeDeclarationContext typeDeclaration = parameter.typeDeclaration();
            final XQuerySequenceType parameterType = typeDeclaration != null
                ? typeDeclaration.accept(this)
                : anyItems;
            var range = getRange(parameter.DOLLAR(), parameter.qname());
            variablesMappedToTypes.add(new TypedVariable(range, parameterName, parameterType));
        }
        return og;
    }

}
