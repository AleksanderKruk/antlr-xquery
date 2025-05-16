package com.github.akruk.antlrxquery.contextmanagement.dynamiccontext;

import com.github.akruk.antlrxquery.contextmanagement.XQueryScope;
import com.github.akruk.antlrxquery.values.XQueryValue;

public interface XQueryDynamicScope extends XQueryScope {
    boolean provideVariable(String variableName, XQueryValue assignedValue);
    XQueryValue getVariable(String variableName);
}
