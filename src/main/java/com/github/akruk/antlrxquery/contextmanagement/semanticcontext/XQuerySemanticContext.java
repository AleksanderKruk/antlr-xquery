package com.github.akruk.antlrxquery.contextmanagement.semanticcontext;

import com.github.akruk.antlrxquery.contextmanagement.XQueryContext;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;

public interface XQuerySemanticContext extends XQueryContext {
  XQuerySemanticScope currentScope();
  boolean entypeVariable(String variableName, XQuerySequenceType assignedType);
  XQuerySequenceType getVariable(String variableName);
}
