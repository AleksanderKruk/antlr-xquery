package com.github.akruk.antlrxquery.contextmanagement.semanticcontext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;

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

    public XQuerySequenceType getVariable(String variableName) {
        for (var scope : scopes.reversed()) {
            var variable = scope.getVariable(variableName);
            if (variable != null) {
                return variable;
            }
        }
        return null;
    }

    public boolean entypeVariable(String variableName, XQuerySequenceType assignedType) {
        return currentScope().entypeVariable(variableName, assignedType);
    }
}
