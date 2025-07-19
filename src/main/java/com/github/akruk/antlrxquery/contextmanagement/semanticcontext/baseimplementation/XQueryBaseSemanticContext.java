package com.github.akruk.antlrxquery.contextmanagement.semanticcontext.baseimplementation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.github.akruk.antlrxquery.contextmanagement.semanticcontext.XQuerySemanticContext;
import com.github.akruk.antlrxquery.contextmanagement.semanticcontext.XQuerySemanticScope;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;

public class XQueryBaseSemanticContext implements XQuerySemanticContext {
    final List<XQuerySemanticScope> scopes;
    final Supplier<XQuerySemanticScope> scopeFactory;

    public XQueryBaseSemanticContext() {
        this(XQueryBaseSemanticScope::new);
    }

    public XQueryBaseSemanticContext(Supplier<XQuerySemanticScope> scopeFactory) {
        this.scopeFactory = scopeFactory;
        this.scopes = new ArrayList<>();
    }

    @Override
    public void leaveScope() {
        this.scopes.removeLast();
    }

    @Override
    public void enterScope() {
        this.scopes.add(scopeFactory.get());
    }

    @Override
    public XQuerySemanticScope currentScope() {
        return scopes.getLast();
    }

    @Override
    public XQuerySequenceType getVariable(String variableName) {
        for (var scope : scopes.reversed()) {
            var variable = scope.getVariable(variableName);
            if (variable != null) {
                return variable;
            }
        }
        return null;
    }
    @Override
    public boolean entypeVariable(String variableName, XQuerySequenceType assignedType) {
        return currentScope().entypeVariable(variableName, assignedType);
    }
}
