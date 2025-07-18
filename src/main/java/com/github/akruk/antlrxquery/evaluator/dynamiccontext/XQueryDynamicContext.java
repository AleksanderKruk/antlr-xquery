package com.github.akruk.antlrxquery.evaluator.dynamiccontext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.github.akruk.antlrxquery.values.XQueryValue;

public class XQueryDynamicContext {
    final List<XQueryDynamicScope> scopes;
    final Supplier<XQueryDynamicScope> scopeFactory;

    public XQueryDynamicContext() {
        this(XQueryDynamicScope::new);
    }

    public XQueryDynamicContext(Supplier<XQueryDynamicScope> scopeFactory) {
        this.scopeFactory = scopeFactory;
        this.scopes = new ArrayList<>();
    }

    public void leaveScope() {
        this.scopes.removeLast();
    }

    public void enterScope() {
        this.scopes.add(scopeFactory.get());
    }


    public XQueryDynamicScope currentScope() {
        return scopes.getLast();
    }

    public XQueryValue getVariable(String variableName) {
        return currentScope().getVariable(variableName);
    }

    public boolean provideVariable(String variableName, XQueryValue assignedValue) {
        return currentScope().provideVariable(variableName, assignedValue);
    }
}
