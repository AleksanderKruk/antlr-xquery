package com.github.akruk.antlrxquery.contextmanagement.semanticcontext;

import com.github.akruk.antlrxquery.contextmanagement.XQueryContext;
import com.github.akruk.antlrxquery.typesystem.XQueryType;

public interface XQuerySemanticContext extends XQueryContext {
  XQuerySemanticScope currentScope();
  boolean entypeVariable(String variableName, XQueryType assignedType);
  XQueryType getVariable(String variableName);
}
