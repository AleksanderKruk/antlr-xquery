package com.github.akruk.antlrxquery.result;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Predicate;

import org.antlr.v4.runtime.tree.ParseTree;

public interface XQueryValue {
  // Function
  public default BigDecimal numericValue() {
    return null;
  };

  public default String stringValue() {
    return null;
  };

  public default Boolean booleanValue() {
    return null;
  };

  public default List<XQueryValue> collection() {
    return null;
  }

  public default Predicate<ParseTree> filter() {
    return null;
  }

  public default XQueryValue reference() {
    return null;
  }

  public default boolean isNumericValue() {
    return numericValue() != null;
  }

  public default boolean isStringValue() {
    return stringValue() != null;
  }

  public default boolean isBooleanValue() {
    return booleanValue() != null;
  }

  public default boolean isCollection() {
    return collection() != null;
  }

  public default boolean isFilter() {
    return filter() != null;
  }

  public default boolean isReference() {
    return reference() != null;
  }

  public XQueryValue add(XQueryValue other);
  public XQueryValue subtract(XQueryValue other);
  public XQueryValue multiply(XQueryValue other);
  public XQueryValue divide(XQueryValue other);
  public XQueryValue integerDivide(XQueryValue other);
  public XQueryValue modulus(XQueryValue other);
  public XQueryValue concatenate(XQueryValue other);
  public XQueryValue valueEqual(XQueryValue other);
  public XQueryValue valueLessThan(XQueryValue other);
  public XQueryValue valueLessEqual(XQueryValue other);
  public XQueryValue valueGreaterThan(XQueryValue other);
  public XQueryValue valueGreaterEqual(XQueryValue other);
  public XQueryValue generalEqual(XQueryValue other);
  public XQueryValue generalLessThan(XQueryValue other);
  public XQueryValue generalLessEqual(XQueryValue other);
  public XQueryValue generalGreaterThan(XQueryValue other);
  public XQueryValue generalGreaterEqual(XQueryValue other);
}