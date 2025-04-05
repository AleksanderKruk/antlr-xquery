package com.github.akruk.antlrxquery.contextmanagement.semanticcontext;

import com.github.akruk.antlrxquery.contextmanagement.XQueryScope;
import com.github.akruk.antlrxquery.typesystem.XQueryType;

public interface XQuerySemanticScope extends XQueryScope {
    boolean entypeVariable(String variableName, XQueryType assignedValue);
    XQueryType getVariable(String variableName);
}
