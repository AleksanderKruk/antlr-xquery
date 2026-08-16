package com.github.akruk.antlrquery.evaluator.dynamiccontext;

import java.util.HashMap;
import java.util.Map;

import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;

public class DynamicScope {
    final Map<String, AntlrQueryValue> variables = new HashMap<>();

    public boolean provideVariable(String variableName, AntlrQueryValue assignedValue) {
        boolean addedVariable = variables.containsKey(variableName);
        variables.put(variableName, assignedValue);
        return addedVariable;
    }



    public AntlrQueryValue getVariable(String variableName) {
        return variables.getOrDefault(variableName, null);
    }



    public boolean hasVariable(String variableName) {
        return variables.containsKey(variableName);
    }
}
