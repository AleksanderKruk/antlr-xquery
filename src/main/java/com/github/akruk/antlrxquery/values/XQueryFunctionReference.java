package com.github.akruk.antlrxquery.values;

public class XQueryFunctionReference extends XQueryValueBase<XQueryFunction> {

  public XQueryFunctionReference(XQueryFunction xQueryFunction) {
    value = xQueryFunction;
  }

  @Override
  public XQueryValue valueEqual(XQueryValue other) {
    return XQueryBoolean.of(value == other.functionValue());
  }

  @Override
  public XQueryFunction functionValue() {
    return value;
  }

  @Override
  public boolean isFunction() {
    return true;
  }

  @Override
  public XQueryValue valueLessThan(XQueryValue other) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public XQueryValue copy() {
    // TODO Auto-generated method stub
    return null;
  }

}
