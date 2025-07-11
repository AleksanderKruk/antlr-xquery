package com.github.akruk.antlrxquery.contextmanagement.semanticcontext;

import com.github.akruk.antlrxquery.contextmanagement.XQueryContextManager;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;

public interface XQuerySemanticContextManager extends XQueryContextManager {
    boolean entypeVariable(String variableName, XQuerySequenceType assignedType);
    boolean placeholderVariable(String variableName);
    boolean restrainVariable(String variableName, XQuerySequenceType type);
    XQuerySequenceType getVariable(String variableName);
}
