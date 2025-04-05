package com.github.akruk.antlrxquery.contextmanagement.semanticcontext.baseimplementation;

import java.util.HashMap;
import java.util.Map;

import com.github.akruk.antlrxquery.contextmanagement.semanticcontext.XQuerySemanticScope;
import com.github.akruk.antlrxquery.typesystem.XQueryType;

public class XQueryBaseSemanticScope implements XQuerySemanticScope {
    Map<String, XQueryType> variables = new HashMap<>();

    @Override
    public boolean entypeVariable(String variableName, XQueryType assignedValue) {
        boolean addedVariable = variables.containsKey(variableName);
        variables.put(variableName, assignedValue);
        return addedVariable;
    }


    @Override
    public XQueryType getVariable(String variableName) {
        return variables.getOrDefault(variableName, null);
    }


    @Override
    public boolean hasVariable(String variableName) {
        return variables.containsKey(variableName);
    }
}