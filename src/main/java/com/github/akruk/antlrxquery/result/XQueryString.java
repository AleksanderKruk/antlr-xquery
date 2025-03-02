package com.github.akruk.antlrxquery.result;

public class XQueryString  extends XQueryValueBase<String> {
  public XQueryString(String string) {
    value = string;
  }

  @Override
  public XQueryValue add(XQueryValue other) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'add'");
  }

  @Override
  public XQueryValue subtract(XQueryValue other) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'subtract'");
  }

  @Override
  public XQueryValue multiply(XQueryValue other) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'multiply'");
  }

  @Override
  public XQueryValue divide(XQueryValue other) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'divide'");
  }

  @Override
  public XQueryValue integerDivide(XQueryValue other) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'integerDivide'");
  }

  @Override
  public XQueryValue modulus(XQueryValue other) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'modulus'");
  }

  @Override
  public XQueryValue concatenate(XQueryValue other) {
    return new XQueryString(stringValue() + other.stringValue());
  }

  @Override
  public XQueryValue valueEqual(XQueryValue other) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'valueEqual'");
  }

  @Override
  public XQueryValue valueLessThan(XQueryValue other) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'valueLessThan'");
  }

  @Override
  public XQueryValue valueLessEqual(XQueryValue other) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'valueLessEqual'");
  }

  @Override
  public XQueryValue valueGreaterThan(XQueryValue other) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'valueGreaterThan'");
  }

  @Override
  public XQueryValue valueGreaterEqual(XQueryValue other) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'valueGreaterEqual'");
  }

  @Override
  public XQueryValue generalEqual(XQueryValue other) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'generalEqual'");
  }

  @Override
  public XQueryValue generalLessThan(XQueryValue other) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'generalLessThan'");
  }

  @Override
  public XQueryValue generalLessEqual(XQueryValue other) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'generalLessEqual'");
  }

  @Override
  public XQueryValue generalGreaterThan(XQueryValue other) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'generalGreaterThan'");
  }

  @Override
  public XQueryValue generalGreaterEqual(XQueryValue other) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'generalGreateurn al'");
  }

}
