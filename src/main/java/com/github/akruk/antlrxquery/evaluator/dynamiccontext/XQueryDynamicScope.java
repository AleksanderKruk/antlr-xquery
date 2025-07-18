package com.github.akruk.antlrxquery.evaluator.dynamiccontext;

import java.util.HashMap;
import java.util.Map;

import com.github.akruk.antlrxquery.values.XQueryValue;

public class XQueryDynamicScope {
    Map<String, XQueryValue> variables = new HashMap<>();

    public boolean provideVariable(String variableName, XQueryValue assignedValue) {
        boolean addedVariable = variables.containsKey(variableName);
        variables.put(variableName, assignedValue);
        return addedVariable;
    }



    public XQueryValue getVariable(String variableName) {
        return variables.getOrDefault(variableName, null);
    }



    public boolean hasVariable(String variableName) {
        return variables.containsKey(variableName);
    }
}
