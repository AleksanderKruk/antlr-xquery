package com.github.akruk.antlrquery.semanticanalyzer.semanticcontext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.eclipse.lsp4j.Location;

import com.github.akruk.antlrquery.AntlrQueryParser.VarNameContext;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.AntlrQuerySemanticScope.EntypingResult;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.AntlrQuerySemanticScope.VariableInfo;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.types.TypeInContext;
import com.github.akruk.antlrquery.typesystem.typeoperations.Types.EffectiveBooleanValueType;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;



public class AntlrQuerySemanticContextManager {
    final List<AntlrQuerySemanticContext> contexts;
    final Supplier<AntlrQuerySemanticContext> contextFactory;

    public AntlrQuerySemanticContextManager(AntlrQueryTypeFactory typeFactory) {
        this(() -> new AntlrQuerySemanticContext(typeFactory));
    }

    public AntlrQuerySemanticContextManager(Supplier<AntlrQuerySemanticContext> contextFactory) {
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

    public AntlrQuerySemanticContext currentContext() {
        return contexts.getLast();
    }

    public AntlrQuerySemanticScope currentScope() {
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
