package com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.akruk.antlrxquery.typesystem.defaults.TypeInContext;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType.EffectiveBooleanValueType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;


public class XQuerySemanticContext {
    final List<XQuerySemanticScope> scopes;
    final private XQueryTypeFactory typeFactory;

    public XQuerySemanticContext(XQueryTypeFactory typeFactory) {
        this.typeFactory = typeFactory;
        this.scopes = new ArrayList<>();
    }

    public void leaveScope() {
        this.scopes.removeLast();
    }

    public void enterScope() {
        if (this.scopes.isEmpty()) {
            this.scopes.add(new XQuerySemanticScope(this, typeFactory));
        } else {
            this.scopes.add(new XQuerySemanticScope(this, currentScope(), typeFactory));
        }
    }

    public XQuerySemanticScope currentScope() {
        return scopes.getLast();
    }

    public TypeInContext getVariable(String variableName) {
        return currentScope().getVariable(variableName);
    }

    public void applyImplications(TypeInContext type)
    {
        var implicationsForType = currentScope().resolveImplicationsForType(type);
        for (var implication : implicationsForType) {
            if (implication.isApplicable(this)) {
                implication.transform(this);
            }

        }
    }

    public Map<String, TypeInContext> getVariables() {
        return currentScope().variables;
    }

    public boolean entypeVariable(String variableName, TypeInContext assignedType) {
        return currentScope().entypeVariable(variableName, assignedType);
    }

    public TypeInContext typeInContext(XQuerySequenceType type) {
        return currentScope().typeInContext(type);
    }

    public TypeInContext resolveEffectiveBooleanValue(TypeInContext type) {
        return currentScope().resolveEffectiveBooleanValue(type);
    }

    public TypeInContext resolveEffectiveBooleanValue(TypeInContext type, EffectiveBooleanValueType ebvType) {
        return currentScope().resolveEffectiveBooleanValue(type, ebvType);
    }
}
