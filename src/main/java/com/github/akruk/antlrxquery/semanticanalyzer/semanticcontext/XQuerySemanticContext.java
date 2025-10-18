package com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.akruk.antlrxquery.typesystem.defaults.TypeInContext;


public class XQuerySemanticContext {
    final List<XQuerySemanticScope> scopes;

    public XQuerySemanticContext() {
        this.scopes = new ArrayList<>();
    }

    public void leaveScope() {
        this.scopes.removeLast();
    }

    public void enterScope() {
        if (this.scopes.isEmpty()) {
            this.scopes.add(new XQuerySemanticScope(this));
        } else {
            this.scopes.add(new XQuerySemanticScope(this, currentScope()));
        }
    }

    public XQuerySemanticScope currentScope() {
        return scopes.getLast();
    }

    public TypeInContext getVariable(String variableName) {
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

    public Map<String, TypeInContext> getVariables() {
        return currentScope().variables;
    }

    public boolean entypeVariable(String variableName, TypeInContext assignedType) {
        return currentScope().entypeVariable(variableName, assignedType);
    }
}
