package com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext;

import java.util.HashMap;
import java.util.Map;

import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;


public class XQuerySemanticScope {
    final Map<String, XQuerySequenceType> variables = new HashMap<>();

    public Map<String, XQuerySequenceType> getVariables() {
        return variables;
    }

    public boolean entypeVariable(String variableName, XQuerySequenceType assignedType) {
        boolean addedVariable = variables.containsKey(variableName);
        variables.put(variableName, assignedType);
        return addedVariable;
    }


    public XQuerySequenceType getVariable(String variableName) {
        return variables.getOrDefault(variableName, null);
    }


    public boolean hasVariable(String variableName) {
        return variables.containsKey(variableName);
    }
}
