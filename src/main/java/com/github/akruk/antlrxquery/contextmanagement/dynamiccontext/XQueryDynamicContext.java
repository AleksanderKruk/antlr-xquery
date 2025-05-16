package com.github.akruk.antlrxquery.contextmanagement.dynamiccontext;

import com.github.akruk.antlrxquery.contextmanagement.XQueryContext;
import com.github.akruk.antlrxquery.values.XQueryValue;

public interface XQueryDynamicContext extends XQueryContext {
  XQueryDynamicScope currentScope();
  boolean provideVariable(String variableName, XQueryValue assignedValue);
  XQueryValue getVariable(String variableName);
}
