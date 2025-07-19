package com.github.akruk.antlrxquery.contextmanagement;

public interface XQueryContext {
  void leaveScope();
  void enterScope();
}
