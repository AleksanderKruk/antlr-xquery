package com.github.akruk.antlrxquery.contextmanagement.dynamiccontext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.github.akruk.antlrxquery.contextmanagement.XQueryScope;
import com.github.akruk.antlrxquery.values.XQueryValue;

public class XQueryBaseDynamicContext implements XQueryDynamicContext {
    final List<XQueryDynamicScope> scopes;
    final Supplier<XQueryDynamicScope> scopeFactory;

    public XQueryBaseDynamicContext() {
        this(XQueryBaseDynamicScope::new);
    }

    public XQueryBaseDynamicContext(Supplier<XQueryDynamicScope> scopeFactory) {
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
    public XQueryDynamicScope currentScope() {
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
