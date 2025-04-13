package com.github.akruk.antlrxquery.contextmanagement.semanticcontext.baseimplementation;

import java.util.HashMap;
import java.util.Map;

import com.github.akruk.antlrxquery.contextmanagement.semanticcontext.XQuerySemanticScope;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;

public class XQueryBaseSemanticScope implements XQuerySemanticScope {
    Map<String, XQuerySequenceType> variables = new HashMap<>();

    @Override
    public boolean entypeVariable(String variableName, XQuerySequenceType assignedType) {
        boolean addedVariable = variables.containsKey(variableName);
        variables.put(variableName, assignedType);
        return addedVariable;
    }


    @Override
    public XQuerySequenceType getVariable(String variableName) {
        return variables.getOrDefault(variableName, null);
    }


    @Override
    public boolean hasVariable(String variableName) {
        return variables.containsKey(variableName);
    }
}