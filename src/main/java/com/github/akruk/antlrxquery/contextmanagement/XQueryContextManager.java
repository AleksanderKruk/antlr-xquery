package com.github.akruk.antlrxquery.contextmanagement;

public interface XQueryContextManager {
    void enterScope();
    void leaveScope();
    void enterContext();
    void leaveContext();
    XQueryScope currentScope();
    XQueryContext currentContext();
}
