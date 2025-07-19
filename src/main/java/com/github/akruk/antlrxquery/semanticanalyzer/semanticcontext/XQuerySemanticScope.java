package com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext;

import java.util.HashMap;
import java.util.Map;

<<<<<<< HEAD:src/main/java/com/github/akruk/antlrxquery/contextmanagement/semanticcontext/baseimplementation/XQueryBaseSemanticScope.java
import com.github.akruk.antlrxquery.contextmanagement.semanticcontext.XQuerySemanticScope;
=======
>>>>>>> language-features/lookup-expression:src/main/java/com/github/akruk/antlrxquery/semanticanalyzer/semanticcontext/XQuerySemanticScope.java
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;


public class XQuerySemanticScope {
    Map<String, XQuerySequenceType> variables = new HashMap<>();

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
