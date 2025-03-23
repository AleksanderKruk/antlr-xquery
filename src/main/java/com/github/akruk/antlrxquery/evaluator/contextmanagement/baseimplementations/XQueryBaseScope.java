package com.github.akruk.antlrxquery.evaluator.contextmanagement.baseimplementations;

import java.util.HashMap;
import java.util.Map;

import com.github.akruk.antlrxquery.evaluator.contextmanagement.XQueryScope;
import com.github.akruk.antlrxquery.values.XQueryValue;

public class XQueryBaseScope implements XQueryScope {
    Map<String, XQueryValue> variables = new HashMap<>();
    @Override
    public boolean provideVariable(String variableName, XQueryValue assignedValue) {
        boolean addedVariable = variables.containsKey(variableName);
        variables.put(variableName, assignedValue);
        return addedVariable;
    }


    @Override
    public XQueryValue getVariable(String variableName) {
        return variables.getOrDefault(variableName, null);
    }


    @Override
    public boolean hasVariable(String variableName) {
        return variables.containsKey(variableName);
    }
}