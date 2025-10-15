package com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext;

import java.util.HashMap;
import java.util.Map;

import com.github.akruk.antlrxquery.typesystem.defaults.TypeInContext;


public class XQuerySemanticScope {
    final Map<String, TypeInContext> variables = new HashMap<>();

    public Map<String, TypeInContext> getVariables() {
        return variables;
    }

    public boolean entypeVariable(String variableName, TypeInContext assignedType) {
        boolean addedVariable = variables.containsKey(variableName);
        variables.put(variableName, assignedType);
        return addedVariable;
    }


    public TypeInContext getVariable(String variableName) {
        return variables.getOrDefault(variableName, null);
    }


    public boolean hasVariable(String variableName) {
        return variables.containsKey(variableName);
    }
}
