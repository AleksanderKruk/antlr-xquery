package com.github.akruk.antlrxquery.contextmanagement.semanticcontext;

import com.github.akruk.antlrxquery.contextmanagement.XQueryContextManager;
import com.github.akruk.antlrxquery.typesystem.XQueryType;

public interface XQuerySemanticContextManager extends XQueryContextManager {
    boolean entypeVariable(String variableName, XQueryType assignedType);
    XQueryType getVariable(String variableName);
}
