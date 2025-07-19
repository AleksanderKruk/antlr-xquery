package com.github.akruk.antlrxquery.contextmanagement.semanticcontext;

import com.github.akruk.antlrxquery.contextmanagement.XQueryScope;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;

public interface XQuerySemanticScope extends XQueryScope {
    boolean entypeVariable(String variableName, XQuerySequenceType assignedType);
    XQuerySequenceType getVariable(String variableName);
}
