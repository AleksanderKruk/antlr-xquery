package com.github.akruk.antlrxquery.contextmanagement.dynamiccontext.baseimplementation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.github.akruk.antlrxquery.contextmanagement.dynamiccontext.XQueryDynamicContext;
import com.github.akruk.antlrxquery.contextmanagement.dynamiccontext.XQueryDynamicContextManager;
import com.github.akruk.antlrxquery.contextmanagement.dynamiccontext.XQueryDynamicScope;
import com.github.akruk.antlrxquery.values.XQueryValue;

public class XQueryBaseDynamicContextManager implements XQueryDynamicContextManager {
    final List<XQueryDynamicContext> contexts;
    final Supplier<XQueryDynamicContext> contextFactory;

    public XQueryBaseDynamicContextManager() {
        this(XQueryBaseDynamicContext::new);
    }

    public XQueryBaseDynamicContextManager(Supplier<XQueryDynamicContext> contextFactory) {
        this.contexts = new ArrayList<>();
        this.contextFactory = contextFactory;
    }

    @Override
    public void enterContext() {
        contexts.add(contextFactory.get());
        enterScope();
    }

    @Override
    public void enterScope() {
        currentContext().enterScope();
    }

    @Override
    public void leaveContext() {
        contexts.removeLast();
    }

    @Override
    public void leaveScope() {
        currentContext().leaveScope();
    }

    @Override
    public XQueryDynamicContext currentContext() {
        return contexts.getLast();
    }

    @Override
    public XQueryDynamicScope currentScope() {
        return currentContext().currentScope();
    }

    @Override
    public boolean provideVariable(String variableName, XQueryValue assignedValue) {
        return currentContext().provideVariable(variableName, assignedValue);
    }

    @Override
    public XQueryValue getVariable(String variableName) {
        return currentContext().getVariable(variableName);
    }

}
