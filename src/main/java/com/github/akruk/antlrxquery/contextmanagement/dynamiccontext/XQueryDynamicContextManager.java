package com.github.akruk.antlrxquery.contextmanagement.dynamiccontext;

import com.github.akruk.antlrxquery.contextmanagement.XQueryContextManager;
import com.github.akruk.antlrxquery.values.XQueryValue;

public interface XQueryDynamicContextManager extends XQueryContextManager {
    boolean provideVariable(String variableName, XQueryValue assignedValue);
    XQueryValue getVariable(String variableName);
}
