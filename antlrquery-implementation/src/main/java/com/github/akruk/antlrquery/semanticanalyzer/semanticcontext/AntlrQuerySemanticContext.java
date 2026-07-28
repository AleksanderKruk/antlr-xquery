package com.github.akruk.antlrquery.semanticanalyzer.semanticcontext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.lsp4j.Location;

import com.github.akruk.antlrquery.AntlrXqueryParser.VarNameContext;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.AntlrQuerySemanticScope.EntypingResult;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.AntlrQuerySemanticScope.VariableInfo;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.types.TypeInContext;
import com.github.akruk.antlrquery.typesystem.typeoperations.Types.EffectiveBooleanValueType;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;


public class AntlrQuerySemanticContext {
    final List<AntlrQuerySemanticScope> scopes;
    final private AntlrQueryTypeFactory typeFactory;

    public AntlrQuerySemanticContext(AntlrQueryTypeFactory typeFactory) {
        this.typeFactory = typeFactory;
        this.scopes = new ArrayList<>();
    }

    public void leaveScope() {
        this.scopes.removeLast();
    }

    public void enterScope() {
        if (this.scopes.isEmpty()) {
            this.scopes.add(new AntlrQuerySemanticScope(this, typeFactory));
        } else {
            this.scopes.add(new AntlrQuerySemanticScope(this, currentScope(), typeFactory));
        }
    }

    public AntlrQuerySemanticScope currentScope() {
        return scopes.getLast();
    }

    public VariableInfo getVariable(String variableName) {
        return currentScope().getVariable(variableName);
    }

    public void applyImplications(TypeInContext type)
    {
        var implicationsForType = currentScope().resolveImplicationsForType(type);
        for (var implication : implicationsForType) {
            if (implication.isApplicable(this)) {
                implication.transform(this);
            }

        }
    }

    public Map<String, VariableInfo> getVariables() {
        return currentScope().variables;
    }

    public EntypingResult entypeVariable(
        final String variableName,
        final VarNameContext locationCtx,
        final Location location,
        final TypeInContext assignedType)
    {
        return currentScope().entypeVariable(
            variableName,
            locationCtx,
            location,
            assignedType
            );
    }

    public TypeInContext typeInContext(AntlrQuerySequenceType type) {
        return currentScope().typeInContext(type);
    }

    public TypeInContext resolveEffectiveBooleanValue(TypeInContext type) {
        return currentScope().resolveEffectiveBooleanValue(type);
    }

    public TypeInContext resolveEffectiveBooleanValue(TypeInContext type, EffectiveBooleanValueType ebvType) {
        return currentScope().resolveEffectiveBooleanValue(type, ebvType);
    }
}
