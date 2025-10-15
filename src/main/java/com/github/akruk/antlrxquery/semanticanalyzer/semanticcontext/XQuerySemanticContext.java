package com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.github.akruk.antlrxquery.typesystem.defaults.TypeInContext;


public class XQuerySemanticContext {
    final List<XQuerySemanticScope> scopes;
    final Supplier<XQuerySemanticScope> scopeFactory;

    public XQuerySemanticContext() {
        this(XQuerySemanticScope::new);
    }

    public XQuerySemanticContext(Supplier<XQuerySemanticScope> scopeFactory) {
        this.scopeFactory = scopeFactory;
        this.scopes = new ArrayList<>();
    }

    public void leaveScope() {
        this.scopes.removeLast();
    }

    public void enterScope() {
        this.scopes.add(scopeFactory.get());
    }

    public XQuerySemanticScope currentScope() {
        return scopes.getLast();
    }

    public TypeInContext getVariable(String variableName) {
        for (var scope : scopes.reversed()) {
            var variable = scope.getVariable(variableName);
            if (variable != null) {
                return variable;
            }
        }
        return null;
    }

    public Map<String, TypeInContext> getVariables() {
        HashMap<String, TypeInContext> allvars = new HashMap<>();
        for (var scope : scopes) {
            var scopedVars = scope.getVariables();
            allvars.putAll(scopedVars);
        }
        return allvars;
    }

    public boolean entypeVariable(String variableName, TypeInContext assignedType) {
        return currentScope().entypeVariable(variableName, assignedType);
    }
}
