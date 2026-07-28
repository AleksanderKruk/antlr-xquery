package com.github.akruk.antlrquery.evaluator.dynamiccontext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;

public class AntlrQueryDynamicContext {
    final List<DynamicScope> scopes;
    final Supplier<DynamicScope> scopeFactory;

    public AntlrQueryDynamicContext() {
        this(DynamicScope::new);
    }

    public AntlrQueryDynamicContext(Supplier<DynamicScope> scopeFactory) {
        this.scopeFactory = scopeFactory;
        this.scopes = new ArrayList<>();
    }

    public void leaveScope() {
        this.scopes.removeLast();
    }

    public void enterScope() {
        this.scopes.add(scopeFactory.get());
    }


    public DynamicScope currentScope() {
        return scopes.getLast();
    }

    public AntlrQueryValue getVariable(String variableName) {
        for (var scope : scopes.reversed()) {
            var value = scope.getVariable(variableName);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    public boolean provideVariable(String variableName, AntlrQueryValue assignedValue) {
        return currentScope().provideVariable(variableName, assignedValue);
    }
}
