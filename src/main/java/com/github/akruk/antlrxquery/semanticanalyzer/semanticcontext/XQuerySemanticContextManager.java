package com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.github.akruk.antlrxquery.typesystem.defaults.TypeInContext;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType.EffectiveBooleanValueType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;



public class XQuerySemanticContextManager {
    final List<XQuerySemanticContext> contexts;
    final Supplier<XQuerySemanticContext> contextFactory;

    public XQuerySemanticContextManager(XQueryTypeFactory typeFactory) {
        this(() -> new XQuerySemanticContext(typeFactory));
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

    /**
     * Either creates variable with required type
     * or overrides existing variable in the current scope
     * @param variableName
     * @param assignedType
     * @return true if variable was added
     */
    public boolean entypeVariable(String variableName, TypeInContext assignedType) {
        return currentContext().entypeVariable(variableName, assignedType);
    }

    public TypeInContext getVariable(String variableName) {
        return currentContext().getVariable(variableName);
    }

    public TypeInContext typeInContext(XQuerySequenceType type) {
        return currentContext().currentScope().typeInContext(type);
    }

    public TypeInContext resolveEffectiveBooleanValue(TypeInContext type) {
        return currentContext().currentScope().resolveEffectiveBooleanValue(type);
    }

    public TypeInContext resolveEffectiveBooleanValue(TypeInContext type, EffectiveBooleanValueType ebvType) {
        return currentContext().currentScope().resolveEffectiveBooleanValue(type, ebvType);
    }

}
