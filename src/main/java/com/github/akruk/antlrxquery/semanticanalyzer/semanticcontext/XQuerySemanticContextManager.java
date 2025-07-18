package com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;

public class XQuerySemanticContextManager {
    final List<XQuerySemanticContext> contexts;
    final Supplier<XQuerySemanticContext> contextFactory;

    public XQuerySemanticContextManager() {
        this(XQuerySemanticContext::new);
    }

    public XQuerySemanticContextManager(Supplier<XQuerySemanticContext> contextFactory) {
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

    public XQuerySemanticContext currentContext() {
        return contexts.getLast();
    }

    public XQuerySemanticScope currentScope() {
        return currentContext().currentScope();
    }

    public boolean entypeVariable(String variableName, XQuerySequenceType assignedType) {
        return currentContext().entypeVariable(variableName, assignedType);
    }

    public XQuerySequenceType getVariable(String variableName) {
        return currentContext().getVariable(variableName);
    }

}
