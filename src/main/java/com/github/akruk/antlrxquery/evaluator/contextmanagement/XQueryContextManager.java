package com.github.akruk.antlrxquery.evaluator.contextmanagement;

import com.github.akruk.antlrxquery.values.XQueryValue;

public interface XQueryContextManager {
    void enterScope();
    void leaveScope();
    void enterContext();
    void leaveContext();
    XQueryScope currentScope();
    XQueryContext currentContext();
    boolean provideVariable(String variableName, XQueryValue assignedValue);
    XQueryValue getVariable(String variableName);
}
