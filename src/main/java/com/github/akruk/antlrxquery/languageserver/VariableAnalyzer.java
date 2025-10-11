package com.github.akruk.antlrxquery.languageserver;

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import com.github.akruk.antlrxquery.AntlrXqueryParser.*;
import com.github.akruk.antlrxquery.semanticanalyzer.XQuerySemanticAnalyzer;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.XQuerySemanticContextManager;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.XQuerySemanticFunctionManager;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;
import com.github.akruk.antlrxquery.inputgrammaranalyzer.InputGrammarAnalyzer.GrammarAnalysisResult;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;


// Major refactoring idea:
//  var ref has been standardised as variable '$varname' part so adding tokens can be greatly simplified
public class VariableAnalyzer extends XQuerySemanticAnalyzer {

    public record TypedVariable(Range range, String name, VarRefContext varRef, XQuerySequenceType type) {}
    public final List<TypedVariable> variablesMappedToTypes = new ArrayList<>();
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
            final VarNameAndTypeContext varNameAndType = letBinding.varNameAndType();
            mapTypedVariableDeclaration(varNameAndType, visitExprSingle(letBinding.exprSingle()));
        }
        return og;
    }


    private void mapTypedVariableDeclaration(final VarNameAndTypeContext varNameAndType, XQuerySequenceType assignedType) {
        final VarRefContext varRef = varNameAndType.varRef();
        final String variableName = varRef.qname().getText();
        final var range = getRange(varRef);
        if (varNameAndType.typeDeclaration() == null) {
            variablesMappedToTypes.add(new TypedVariable(range, variableName, varRef, assignedType));
        } else {
            final XQuerySequenceType type = varNameAndType.typeDeclaration().accept(this);
            variablesMappedToTypes.add(new TypedVariable(range, variableName, varRef, type));
        }
    }

    private void mapTypedVariableDeclaration(final VarRefContext varRef, XQuerySequenceType type) {
        final String variableName = varRef.qname().getText();
        final var range = getRange(varRef);
        variablesMappedToTypes.add(new TypedVariable(range, variableName, varRef, type));
    }


    @Override
    public XQuerySequenceType visitSlidingWindowClause(SlidingWindowClauseContext ctx) {
        var og = super.visitSlidingWindowClause(ctx);
        final var iteratedType = visitExprSingle(ctx.exprSingle());
        final var iterator = iteratedType.iteratorType();
        final var optionalIterator = iterator.addOptionality();
        final VarNameAndTypeContext varNameAndType = ctx.varNameAndType();
        mapTypedVariableDeclaration(varNameAndType, visitExprSingle(ctx.exprSingle()));
        if (ctx.windowStartCondition() != null)
            mapWindowConditionVariables(iterator, optionalIterator, ctx.windowStartCondition().windowVars());
        if (ctx.windowEndCondition() != null)
            mapWindowConditionVariables(iterator, optionalIterator, ctx.windowEndCondition().windowVars());
        return og;
    }




    @Override
    public XQuerySequenceType visitTumblingWindowClause(TumblingWindowClauseContext ctx) {
        var og = super.visitTumblingWindowClause(ctx);
        final var iteratedType = visitExprSingle(ctx.exprSingle());
        final var iterator = iteratedType.iteratorType();
        final var optionalIterator = iterator.addOptionality();
        final VarNameAndTypeContext varNameAndType = ctx.varNameAndType();
        mapTypedVariableDeclaration(varNameAndType, visitExprSingle(ctx.exprSingle()));
        if (ctx.windowStartCondition() != null)
            mapWindowConditionVariables(iterator, optionalIterator, ctx.windowStartCondition().windowVars());
        if (ctx.windowEndCondition() != null)
            mapWindowConditionVariables(iterator, optionalIterator, ctx.windowEndCondition().windowVars());
        return og;
    }

    private void mapWindowConditionVariables(
        XQuerySequenceType iterator,
        XQuerySequenceType optionalIterator,
        WindowVarsContext windowVars)
    {
        {var currentVar = windowVars.currentVar();
        if (currentVar != null) {
            mapTypedVariableDeclaration(currentVar.varRef(), iterator);
        }}
        {var currentVarPos = windowVars.positionalVar();
        if (currentVarPos != null) {
            mapTypedVariableDeclaration(currentVarPos.varRef(), typeFactory.number());
        }}
        {var previousVar = windowVars.previousVar();
        if (previousVar != null) {
            mapTypedVariableDeclaration(previousVar.varRef(), optionalIterator);
        }}
        {var nextVar = windowVars.nextVar();
        if (nextVar != null) {
            mapTypedVariableDeclaration(nextVar.varRef(), optionalIterator);
        }}
    }


    @Override
    public XQuerySequenceType visitGroupByClause(GroupByClauseContext ctx) {
        var og = super.visitGroupByClause(ctx);
        for (var gs : ctx.groupingSpec()) {
            if (gs.exprSingle() != null) {
                mapTypedVariableDeclaration(gs.varNameAndType(), visitExprSingle(gs.exprSingle()));
            } else {
                mapTypedVariableDeclaration(gs.varNameAndType().varRef(), visitExprSingle(gs.exprSingle()));
            }
        }
        return og;
    }


    private Range getRange(final ParserRuleContext rule) {
        Token dollarSymbol = rule.getStart();
        var startPosition = new Position(
            dollarSymbol.getLine()-1,
            dollarSymbol.getCharPositionInLine()
        );
        Token stopSymbol = rule.getStop();
        var endPosition = new Position(
            stopSymbol.getLine()-1,
            stopSymbol.getCharPositionInLine() + rule.getText().length() - 1 // verify -1
        );
        var range = new Range(startPosition, endPosition);
        return range;
    }

    @Override
    public void processForItemBinding(final ForItemBindingContext ctx) {
        super.processForItemBinding(ctx);
        final String variableName = ctx.varNameAndType().varRef().qname().getText();
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
            final String positionalVariableName = ctx.varRef().qname().getText();
            var range = getRange(ctx.varRef());
            variablesMappedToTypes.add(new TypedVariable(range, positionalVariableName, ctx.varRef(), number));
        }
    }

    @Override
    public void processForMemberBinding(final ForMemberBindingContext ctx) {
        super.processForMemberBinding(ctx);

        final String variableName = ctx.varNameAndType().varRef().qname().getText();
        final XQuerySequenceType type = contextManager.getVariable(variableName);

        processVariableTypeDeclaration(ctx.varNameAndType(), type, variableName, ctx);
        handlePositionalVariable(ctx.positionalVar());
    }


    @Override
    public void processForEntryBinding(final ForEntryBindingContext ctx) {
        super.processForEntryBinding(ctx);

        final XQuerySequenceType mapType = ctx.exprSingle().accept(this);

        final ForEntryKeyBindingContext keyBinding = ctx.forEntryKeyBinding();
        final ForEntryValueBindingContext valueBinding = ctx.forEntryValueBinding();

        if (keyBinding != null) {
            final String keyVariableName = keyBinding.varNameAndType().varRef().qname().getText();
            final XQueryItemType keyType = mapType.itemType.mapKeyType;
            final XQuerySequenceType keyIteratorType = typeFactory.one(keyType);

            processVariableTypeDeclaration(keyBinding.varNameAndType(), keyIteratorType, keyVariableName, ctx);
        }

        if (valueBinding != null) {
            final String valueVariableName = valueBinding.varNameAndType().varRef().qname().getText();
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
        final var range = getRange(varNameAndType.varRef());
        if (varNameAndType.typeDeclaration() == null) {
            variablesMappedToTypes.add(new TypedVariable(range, variableName, varNameAndType.varRef(), inferredType));
            return;
        }

        final XQuerySequenceType declaredType = varNameAndType.typeDeclaration().accept(this);
        variablesMappedToTypes.add(new TypedVariable(range, variableName, varNameAndType.varRef(), declaredType));
    }

    @Override
    public XQuerySequenceType visitCountClause(final CountClauseContext ctx)
    {
        var og = super.visitCountClause(ctx);
        final String countVariableName = ctx.varRef().getText();
        final var range = getRange(ctx.varRef());
        variablesMappedToTypes.add(new TypedVariable(range, countVariableName, ctx.varRef(), number));
        return og;
    }

    @Override
    public XQuerySequenceType visitVarRef(final VarRefContext ctx)
    {
        var og = super.visitVarRef(ctx);
        final String variableName = ctx.qname().getText();
        final XQuerySequenceType variableType = contextManager.getVariable(variableName);
        final var range = getRange(ctx);
        variablesMappedToTypes.add(new TypedVariable(range, variableName, ctx, variableType));
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
            final var range = getRange(varNameAndTypeContext.varRef());
            final String varname = varNameAndTypeContext.varRef().qname().getText();
            if (desiredType != null) {
                variablesMappedToTypes.add(
                    new TypedVariable(range, varname, varNameAndTypeContext.varRef(), desiredType));

            } else {
                variablesMappedToTypes.add(
                    new TypedVariable(range, varname, varNameAndTypeContext.varRef(), assignedType));
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
            final String parameterName = parameter.varRef().qname().getText();
            final TypeDeclarationContext typeDeclaration = parameter.typeDeclaration();
            final XQuerySequenceType parameterType = typeDeclaration != null
                ? typeDeclaration.accept(this)
                : zeroOrMoreItems;
            var range = getRange(parameter.varRef());
            variablesMappedToTypes.add(new TypedVariable(range, parameterName, parameter.varRef(), parameterType));
        }
        return og;
    }

}
