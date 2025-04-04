package com.github.akruk.antlrxquery.contextmanagement;

import com.github.akruk.antlrxquery.values.XQueryValue;

public interface XQueryContext {
  void leaveScope();
  void enterScope();
}
