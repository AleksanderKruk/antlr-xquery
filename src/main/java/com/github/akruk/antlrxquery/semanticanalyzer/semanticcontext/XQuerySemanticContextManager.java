package com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.eclipse.lsp4j.Location;

import com.github.akruk.antlrxquery.AntlrXqueryParser.VarNameContext;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.XQuerySemanticScope.EntypingResult;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.XQuerySemanticScope.VariableInfo;
import com.github.akruk.antlrxquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.types.TypeInContext;
import com.github.akruk.antlrxquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.types.AntlrQuerySequenceType.EffectiveBooleanValueType;



public class XQuerySemanticContextManager {
    final List<XQuerySemanticContext> contexts;
    final Supplier<XQuerySemanticContext> contextFactory;

    public XQuerySemanticContextManager(AntlrQueryTypeFactory typeFactory) {
        this(() -> new XQuerySemanticContext(typeFactory));
    }

    public XQuerySemanticContextManager(Supplier<XQuerySemanticContext> contextFactory) {
        this.contexts = new ArrayList<>();
        this.contextFactory = contextFactory;
    }

    public void enterContext() {
        contexts.add(contextFactory.get());
        enterScope();
    }

    public void enterScope() {
        currentContext().enterScope();
    }

    public void leaveContext() {
        contexts.removeLast();
    }

    public void leaveScope() {
        currentContext().leaveScope();
    }

    public XQuerySemanticContext currentContext() {
        return contexts.getLast();
    }

    public XQuerySemanticScope currentScope() {
        return currentContext().currentScope();
    }

    /**
     * Either creates variable with required type
     * or overrides existing variable
     * @param variableName
     * @param variableDefinition
     * @param assignedType
     * @return EntypingResult {
     *      VariableInfo? oldVariable;
     *      VariableInfo  newVariable;
     * }
     */
    public EntypingResult entypeVariable(
        String variableName,
        VarNameContext locationCtx,
        Location location,
        TypeInContext assignedType)
    {
        return currentContext().entypeVariable(variableName, locationCtx, location, assignedType);
    }

    public VariableInfo getVariable(String variableName) {
        return currentContext().getVariable(variableName);
    }

    public TypeInContext typeInContext(AntlrQuerySequenceType type) {
        return currentContext().currentScope().typeInContext(type);
    }

    public TypeInContext resolveEffectiveBooleanValue(TypeInContext type) {
        return currentContext().currentScope().resolveEffectiveBooleanValue(type);
    }

    public TypeInContext resolveEffectiveBooleanValue(TypeInContext type, EffectiveBooleanValueType ebvType) {
        return currentContext().currentScope().resolveEffectiveBooleanValue(type, ebvType);
    }

}
