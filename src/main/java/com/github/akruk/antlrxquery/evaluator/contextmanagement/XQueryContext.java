package com.github.akruk.antlrxquery.evaluator.contextmanagement;

public interface XQueryContext {

  void leaveScope();

void enterScope();

XQueryScope currentScope();

}
