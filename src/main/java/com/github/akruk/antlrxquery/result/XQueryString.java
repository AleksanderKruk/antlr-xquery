package com.github.akruk.antlrxquery.result;

public class XQueryString  extends XQueryValueBase<String> {
  public XQueryString(String string) {
    value = string;
  }

  @Override
  public XQueryValue concatenate(XQueryValue other) {
    return new XQueryString(stringValue() + other.stringValue());
  }

  @Override
  public XQueryValue valueEqual(XQueryValue other) {
    return new XQueryBoolean(stringValue().equals(other.stringValue()));
  }

  @Override
  public XQueryValue valueLessThan(XQueryValue other) {
    return new XQueryBoolean(stringValue().compareTo(other.stringValue()) == -1);
  }
}
