package com.github.akruk.antlrxquery.evaluator.dynamiccontext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;

public class XQueryDynamicContextManager {
    final List<XQueryDynamicContext> contexts;
    final Supplier<XQueryDynamicContext> contextFactory;

    public XQueryDynamicContextManager() {
        this(XQueryDynamicContext::new);
    }

    public XQueryDynamicContextManager(Supplier<XQueryDynamicContext> contextFactory) {
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

    public XQueryDynamicContext currentContext() {
        return contexts.getLast();
    }

    public XQueryDynamicScope currentScope() {
        return currentContext().currentScope();
    }

    public boolean provideVariable(String variableName, XQueryValue assignedValue) {
        return currentContext().provideVariable(variableName, assignedValue);
    }

    public XQueryValue getVariable(String variableName) {
        return currentContext().getVariable(variableName);
    }

}
