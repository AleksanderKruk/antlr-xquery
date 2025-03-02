package com.github.akruk.antlrxquery.result;

import java.math.BigDecimal;
import java.util.List;

public interface XQueryValue {
  // Function
  BigDecimal numericValue();
  String stringValue();
  boolean booleanValue();
  List<XQueryValue> collection();
  XQueryValue filter();
  XQueryValue reference();
  XQueryValue add(XQueryValue other);
  XQueryValue subtract(XQueryValue other);
  XQueryValue multiply(XQueryValue other);
  XQueryValue divide(XQueryValue other);
  XQueryValue integerDivide(XQueryValue other);
  XQueryValue modulus(XQueryValue other);
  XQueryValue concatenate(XQueryValue other);
  XQueryValue valueEqual(XQueryValue other);
  XQueryValue valueLessThan(XQueryValue other);
  XQueryValue valueLessEqual(XQueryValue other);
  XQueryValue valueGreaterThan(XQueryValue other);
  XQueryValue valueGreaterEqual(XQueryValue other);
  XQueryValue generalEqual(XQueryValue other);
  XQueryValue generalLessThan(XQueryValue other);
  XQueryValue generalLessEqual(XQueryValue other);
  XQueryValue generalGreaterThan(XQueryValue other);
  XQueryValue generalGreaterEqual(XQueryValue other);
}