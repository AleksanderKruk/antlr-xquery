package com.github.akruk.antlrxquery.evaluator.contextmanagement;

import com.github.akruk.antlrxquery.values.XQueryValue;

public interface XQueryContext {
  void leaveScope();
  void enterScope();
  XQueryScope currentScope();
  boolean provideVariable(String variableName, XQueryValue assignedValue);
  XQueryValue getVariable(String variableName);
}
