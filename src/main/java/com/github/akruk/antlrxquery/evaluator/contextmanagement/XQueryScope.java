package com.github.akruk.antlrxquery.evaluator.contextmanagement;

import com.github.akruk.antlrxquery.values.XQueryValue;

public interface XQueryScope {
    boolean provideVariable(String variableName, XQueryValue assignedValue);
    XQueryValue getVariable(String variableName);
    boolean hasVariable(String variableName);
}