package com.github.akruk.antlrxquery.evaluator.contextmanagement.baseimplementations;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.github.akruk.antlrxquery.evaluator.contextmanagement.XQueryContext;
import com.github.akruk.antlrxquery.evaluator.contextmanagement.XQueryContextManager;
import com.github.akruk.antlrxquery.evaluator.contextmanagement.XQueryScope;
import com.github.akruk.antlrxquery.values.XQueryValue;

public class XQueryBaseContextManager implements XQueryContextManager {
    final List<XQueryContext> contexts;
    final Supplier<XQueryContext> contextFactory;

    public XQueryBaseContextManager() {
        this(XQueryBaseContext::new);
    }

    public XQueryBaseContextManager(Supplier<XQueryContext> contextFactory) {
        this.contexts = new ArrayList<>();
        this.contextFactory = contextFactory;
    }

    @Override
    public void enterContext() {
        contexts.add(contextFactory.get());
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
    public XQueryContext currentContext() {
        return contexts.getLast();
    }

    @Override
    public XQueryScope currentScope() {
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
