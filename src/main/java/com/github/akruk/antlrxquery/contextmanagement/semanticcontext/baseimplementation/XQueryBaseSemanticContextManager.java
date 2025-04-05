package com.github.akruk.antlrxquery.contextmanagement.semanticcontext.baseimplementation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.github.akruk.antlrxquery.contextmanagement.semanticcontext.XQuerySemanticContext;
import com.github.akruk.antlrxquery.contextmanagement.semanticcontext.XQuerySemanticContextManager;
import com.github.akruk.antlrxquery.contextmanagement.semanticcontext.XQuerySemanticScope;
import com.github.akruk.antlrxquery.typesystem.XQueryType;

public class XQueryBaseSemanticContextManager implements XQuerySemanticContextManager {
    final List<XQuerySemanticContext> contexts;
    final Supplier<XQuerySemanticContext> contextFactory;

    public XQueryBaseSemanticContextManager() {
        this(XQueryBaseSemanticContext::new);
    }

    public XQueryBaseSemanticContextManager(Supplier<XQuerySemanticContext> contextFactory) {
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
    public XQuerySemanticContext currentContext() {
        return contexts.getLast();
    }

    @Override
    public XQuerySemanticScope currentScope() {
        return currentContext().currentScope();
    }

    @Override
    public boolean entypeVariable(String variableName, XQueryType assignedType) {
        return currentContext().entypeVariable(variableName, assignedType);
    }

    @Override
    public XQueryType getVariable(String variableName) {
        return currentContext().getVariable(variableName);
    }

}
