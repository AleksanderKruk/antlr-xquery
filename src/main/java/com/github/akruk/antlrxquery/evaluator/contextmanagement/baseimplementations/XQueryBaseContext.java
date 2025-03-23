package com.github.akruk.antlrxquery.evaluator.contextmanagement.baseimplementations;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.github.akruk.antlrxquery.evaluator.contextmanagement.XQueryContext;
import com.github.akruk.antlrxquery.evaluator.contextmanagement.XQueryScope;
import com.github.akruk.antlrxquery.values.XQueryValue;

public class XQueryBaseContext implements XQueryContext {
    final List<XQueryScope> scopes;
    final Supplier<XQueryScope> scopeFactory;

    public XQueryBaseContext() {
        this(XQueryBaseScope::new);
    }

    public XQueryBaseContext(Supplier<XQueryScope> scopeFactory) {
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
    public XQueryScope currentScope() {
        return scopes.getLast();
    }

    @Override
    public XQueryValue getVariable(String variableName) {
        return currentScope().getVariable(variableName);
    }
    @Override
    public boolean provideVariable(String variableName, XQueryValue assignedValue) {
        return currentScope().provideVariable(variableName, assignedValue);
    }
}
