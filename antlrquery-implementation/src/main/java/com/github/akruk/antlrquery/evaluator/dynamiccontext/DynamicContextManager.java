package com.github.akruk.antlrquery.evaluator.dynamiccontext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;

public class DynamicContextManager {
    final List<AntlrQueryDynamicContext> contexts;
    final Supplier<AntlrQueryDynamicContext> contextFactory;

    public DynamicContextManager() {
        this(AntlrQueryDynamicContext::new);
    }

    public DynamicContextManager(Supplier<AntlrQueryDynamicContext> contextFactory) {
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

    public AntlrQueryDynamicContext currentContext() {
        return contexts.getLast();
    }

    public DynamicScope currentScope() {
        return currentContext().currentScope();
    }

    public boolean provideVariable(String variableName, AntlrQueryValue assignedValue) {
        return currentContext().provideVariable(variableName, assignedValue);
    }

    public AntlrQueryValue getVariable(String variableName) {
        return currentContext().getVariable(variableName);
    }

}
