package com.github.akruk.antlrxquery.contextmanagement;

import com.github.akruk.antlrxquery.values.XQueryValue;

public interface XQueryScope {
    boolean hasVariable(String variableName);
}